/*
 * Copyright © 2017 Quali and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.Formatter;
import java.util.List;
import com.google.common.base.Optional;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.SetPortMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.SetPortMapInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.VtnKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.Vbridge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.VbridgeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.vtn.mappable.vinterface.list.Vinterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.Vtn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.Vtns;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.VtnVbridgeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.VtnVinterfaceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.VtnPortMapService;


public class PacketHandler implements PacketProcessingListener {

    private final DataBroker dataBroker;
    private final VtnVbridgeService vtnVbridgeService;
    private final VtnVinterfaceService vtnVinterfaceService;
    private final VtnPortMapService vtnPortMapService;
    private final static Logger LOG = LoggerFactory.getLogger(PacketHandler.class);
    private static final String ETH_TYPE_802_1Q = "8100";
    private static final String VTN_TRUNKS_NAME = "CS_TRUNKS";
    private static final String VBRIDGE_NAME = "CS_VBRIDGE";

    public PacketHandler(final DataBroker dataBroker,
                         final VtnVbridgeService vtnVbridgeService,
                         final VtnVinterfaceService vtnVinterfaceService,
                         final VtnPortMapService vtnPortMapService) {

        this.dataBroker = dataBroker;
        this.vtnVbridgeService = vtnVbridgeService;
        this.vtnVinterfaceService = vtnVinterfaceService;
        this.vtnPortMapService = vtnPortMapService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("PacketHandler Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("PacketHandler Closed");
    }

    /**
     * Extract ethernet type from the packet payload and convert it to the hex string
     */
    public static String extractEtherType(byte[] payload) {
        byte[] bytes = Arrays.copyOfRange(payload, 12, 14);
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * Extract VLAN tag from the packet payload and convert it to the integer
     */
    public static Integer extractVlan(byte[] payload) {
        byte[] bytes = Arrays.copyOfRange(payload, 14, 16);
        return new BigInteger(bytes).intValue();
    }

    public static NodeConnectorKey getNodeConnectorKey(InstanceIdentifier<?> nodeConnectorPath) {
        return nodeConnectorPath.firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
    }

    @Override
    public void onPacketReceived(PacketReceived packetReceived) {
        String etherType = extractEtherType(packetReceived.getPayload());

        if (etherType.equals(ETH_TYPE_802_1Q)) {
            NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());
            String inPort = ingressKey.getId().getValue();
            Integer vlan = extractVlan(packetReceived.getPayload());

            LOG.info(String.format("Processing frame on port %s with VLAN %d", inPort, vlan));

            InstanceIdentifier<Vbridge> vbridgeIdRef = InstanceIdentifier.builder(Vtns.class)
                    .child(Vtn.class, new VtnKey(new VnodeName(VTN_TRUNKS_NAME)))
                    .child(Vbridge.class, new VbridgeKey(new VnodeName(VBRIDGE_NAME)))
                    .build();

            ReadOnlyTransaction readTx = dataBroker.newReadOnlyTransaction();

            try {
                Optional<Vbridge> data = readTx.read(LogicalDatastoreType.OPERATIONAL, vbridgeIdRef).get();

                if (data.isPresent()) {
                    LOG.info(String.format("Trunks VTN '%s' is present. Checking whether incoming node %s " +
                            "is a trunk port or not", inPort, VTN_TRUNKS_NAME));

                    Vbridge vbridgeData = data.get();
                    List<Vinterface> ifaces = vbridgeData.getVinterface();

                    for(Vinterface currIface : ifaces)  {
                        String mappedPort = currIface.getVinterfaceStatus().getMappedPort().getValue();

                        if (inPort.equals(mappedPort)) {
                            LOG.info(String.format("Incoming node %s is a trunk. Adding vBridge '%d' to trunks " +
                                    "VTN '%s'", inPort, vlan, VTN_TRUNKS_NAME));

                            String vBridgeName = vlan.toString();

                            UpdateVbridgeInput updateVbridgeInput = new UpdateVbridgeInputBuilder()
                                    .setTenantName(VTN_TRUNKS_NAME)
                                    .setBridgeName(vBridgeName)
                                    .build();

                            this.vtnVbridgeService.updateVbridge(updateVbridgeInput);

                            // add all interfaces from trunks VTN to VLAN vBridge
                            for(Vinterface iface : ifaces) {
                                String ifaceName = iface.getName().getValue();
                                String portName = iface.getPortMapConfig().getPortName();
                                NodeId nodeId = iface.getPortMapConfig().getNode();

                                UpdateVinterfaceInput updateVinterfaceInput = new UpdateVinterfaceInputBuilder()
                                        .setTenantName(VTN_TRUNKS_NAME)
                                        .setBridgeName(vBridgeName)
                                        .setInterfaceName(ifaceName)
                                        .build();

                                this.vtnVinterfaceService.updateVinterface(updateVinterfaceInput);

                                SetPortMapInput setPortMapInput = new SetPortMapInputBuilder()
                                        .setTenantName(VTN_TRUNKS_NAME)
                                        .setBridgeName(vBridgeName)
                                        .setInterfaceName(ifaceName)
                                        .setNode(nodeId)
                                        .setPortName(portName)
                                        .setVlanId(new VlanId(vlan))
                                        .build();

                                this.vtnPortMapService.setPortMap(setPortMapInput);
                            }
                            break;
                        }
                    }
                }
            } catch (InterruptedException|ExecutionException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
