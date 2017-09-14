/*
 * Copyright © 2017 Quali and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.common.base.*;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;



import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.VtnKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.Vbridge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.VbridgeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.vtn.mappable.vinterface.list.Vinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.*;
import java.io.*;


import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.Vtn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.Vtns;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.VtnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;

import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeUpdateMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnErrorTag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnUpdateOperationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnUpdateType;



import java.util.Base64;
import java.util.concurrent.ExecutionException;

import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.VtnVlanMapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.


import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;





public class PacketHandler implements PacketProcessingListener {

    private final static Logger LOG = LoggerFactory.getLogger(PacketHandler.class);
    private final FlowProcessingService flowProcessingService;
    private static final byte[] ETH_TYPE_IPV4 = new byte[] { 0x08, 0x00 };
    private final FileRouteProcessingService fileRouteProcessingService;
    private final VtnVlanMapService vtnVlanMapService;
    private final DataBroker dataBroker;


    public PacketHandler(final FlowProcessingService flowProcessingService,
                         final FileRouteProcessingService fileRouteProcessingService,
                         final VtnVlanMapService vtnVlanMapService,
                         final DataBroker dataBroker) {

        this.flowProcessingService = flowProcessingService;
        this.fileRouteProcessingService = fileRouteProcessingService;
        this.vtnVlanMapService = vtnVlanMapService;
        this.dataBroker = dataBroker;
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

    public static byte[] extractDstMac(byte[] payload) {
        return Arrays.copyOfRange(payload, 0, 6);
    }

    public static byte[] extractSrcMac(byte[] payload) {
        return Arrays.copyOfRange(payload, 6, 12);
    }

    public static byte[] extractEtherType(byte[] payload) {
        return Arrays.copyOfRange(payload, 12, 14);
    }

    public static Integer extractVlan(byte[] payload) {
        byte[] bytes = Arrays.copyOfRange(payload, 14, 16);
        return new BigInteger(bytes).intValue();
    }

    public static MacAddress rawMacToMac(byte[] rawMac) {
        MacAddress mac = null;
        if (rawMac != null && rawMac.length == 6) {
            StringBuffer sb = new StringBuffer();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            mac = new MacAddress(sb.substring(1));
        }
        return mac;
    }

    public static NodeConnectorKey getNodeConnectorKey(InstanceIdentifier<?> nodeConnectorPath) {
        return nodeConnectorPath.firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
    }

    @Override
    public void onPacketReceived(PacketReceived packetReceived) {
        NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());
        String inPort = ingressKey.getId().getValue();

        String[] inPortParts = inPort.split(":");
        String switchId = inPortParts[0] + ":" + inPortParts[1];
        Integer port = Integer.parseInt(inPortParts[2]);

        byte[] dstMacRaw = extractDstMac(packetReceived.getPayload());
        byte[] srcMacRaw = extractSrcMac(packetReceived.getPayload());
        byte[] etherType = extractEtherType(packetReceived.getPayload());

        MacAddress dstMac = rawMacToMac(dstMacRaw);
        MacAddress srcMac = rawMacToMac(srcMacRaw);

        System.out.println("################################################");
        System.out.println("IN port connector: " + inPort);
        System.out.println("Received packet from MAC match: " + srcMac);
        System.out.println("Received packet to MAC match: " + dstMac);
        System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
        System.out.println("################################################");

//        if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
        // todo: rework this check for bytes check
        if (Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()).equals("8100")) {

            System.out.println("################################################");
            System.out.println("802.1Q PACKET NOTIFICATION");

            Integer vlan = extractVlan(packetReceived.getPayload());
            System.out.println("################################################");
            System.out.println("VLAN IS: " + vlan);
            System.out.println("################################################");

            // todo: STEP 2: get CS_TRUNKS VTN DATA
            System.out.println("################################################");

            List<Vtn> vtns1 = new VtnsBuilder().getVtn();


            System.out.println("VTNS:" + vtns1);

            InstanceIdentifier<Vbridge> vbridgeIdRef = InstanceIdentifier.builder(Vtns.class)
                    .child(Vtn.class, new VtnKey(new VnodeName("CS_TRUNKS")))
                    .child(Vbridge.class, new VbridgeKey(new VnodeName("CS_VBRIDGE")))
                    .build();

            ReadOnlyTransaction readTx = dataBroker.newReadOnlyTransaction();
            System.out.println("STRAT READING VTN!!!!!!!");


            try {
                com.google.common.base.Optional<Vbridge> data = readTx.read(LogicalDatastoreType.OPERATIONAL, vbridgeIdRef).get();
                System.out.println("READ DATA!!!!!!");
                if (data.isPresent()) {
                    Vbridge vbridgeData = data.get();
                    List<Vinterface> ifaces = vbridgeData.getVinterface();

                    for(Vinterface iface : ifaces)  {
                        String mappedPort = iface.getVinterfaceStatus().getMappedPort().getValue();
                        System.out.println("TRUNK iface connector is !!!!!!" + mappedPort);

                        if (inPort.equals(mappedPort)) {
                            System.out.println("HOLLY CRAP its working !!!");

                            System.out.println("SENDING POST REQUEST !!!!!!!!!!!!!!: ");
                            AddVlanMapInput input = new AddVlanMapInputBuilder()
                                    .setVlanId(new VlanId(vlan))
                                    .setTenantName("CS_TRUNKS")
                                    .setBridgeName("CS_VBRIDGE")
                                    .build();

                            this.vtnVlanMapService.addVlanMap(input);
                            // todo: if status code == 409 (conflict) - then delete vlan-map from the CS_TRUNKS --- this todo should be done on the python side NO !!!!?? ITS WORK??
                        }
                    }
                }
            } catch (InterruptedException|ExecutionException e) {
                System.out.println("SHIT HAPPENs !!!" + e.getMessage());
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
