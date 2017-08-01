/*
 * Copyright © 2017 Quali and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;


public class PacketHandler implements PacketProcessingListener {

    private final static Logger LOG = LoggerFactory.getLogger(PacketHandler.class);
    private final FlowProcessingService flowProcessingService;
    private static final byte[] ETH_TYPE_IPV4 = new byte[] { 0x08, 0x00 };
    private final FileRouteProcessingService fileRouteProcessingService;

    public PacketHandler(final FlowProcessingService flowProcessingService,
                         final FileRouteProcessingService fileRouteProcessingService) {

        this.flowProcessingService = flowProcessingService;
        this.fileRouteProcessingService = fileRouteProcessingService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("PacketHandler Session Initiated");
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
        try {

            System.out.println("################################################");
            System.out.println("PACKET NOTIFICATION");
            System.out.println("################################################");


            Class<? extends PacketInReason> pktInReason = packetReceived.getPacketInReason();

            // read src MAC and dst MAC
            try {


                byte[] dstMacRaw = extractDstMac(packetReceived.getPayload());
                byte[] srcMacRaw = extractSrcMac(packetReceived.getPayload());
                byte[] etherType = extractEtherType(packetReceived.getPayload());

                MacAddress dstMac = rawMacToMac(dstMacRaw);
                MacAddress srcMac = rawMacToMac(srcMacRaw);
                String srcMacVal = srcMac.getValue();
                String dstMacVal = dstMac.getValue();
            } catch (Exception e) {
                System.out.println("================== NOOOOOOO WAAAY 111 ==============");
                System.out.println("Strange package !!!!");
                System.out.println(e.getMessage());
                e.printStackTrace();

                NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());
                String inPort = ingressKey.getId().getValue();


                System.out.println("================== Packet DATA ==============");
                System.out.println("in port: " + inPort);
                System.out.println("pktInReason: " + pktInReason.toString());

                try {
                    System.out.println("table ID: " + packetReceived.getTableId().getValue());
                } catch (Exception e2) {
                    System.out.println("table ID -- fail");
                }

                try {
                    System.out.println("PAyload: " + packetReceived.getPayload().toString());
                } catch (Exception e2) {
                    System.out.println("PAyload  -- fail");
                }

                try {
                    System.out.println("pkt Match: " + packetReceived.getMatch().toString());
                } catch (Exception e2) {
                    System.out.println("pkt Match -- fail");
                }

                try {
                    System.out.println("getFlowCookie: " + packetReceived.getFlowCookie().toString());
                } catch (Exception e2) {
                    System.out.println("getFlowCookie -- fail");
                }

                try {
                    System.out.println("getConnCookie: " + packetReceived.getConnectionCookie().toString());
                } catch (Exception e2) {
                    System.out.println("getConnCookie -- fail");
                }

                System.out.println("================== END PACKET DATA ==============");
            }

            byte[] dstMacRaw = extractDstMac(packetReceived.getPayload());
            byte[] srcMacRaw = extractSrcMac(packetReceived.getPayload());
            byte[] etherType = extractEtherType(packetReceived.getPayload());

            MacAddress dstMac = rawMacToMac(dstMacRaw);
            MacAddress srcMac = rawMacToMac(srcMacRaw);
            String srcMacVal = srcMac.getValue();
            String dstMacVal = dstMac.getValue();


            if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {

                System.out.println("################################################");
                System.out.println("PACKET NOTIFICATION");
                System.out.println("################################################");


                NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());
                String inPort = ingressKey.getId().getValue();

                String[] inPortParts = inPort.split(":");
                String switchId = inPortParts[0] + ":" + inPortParts[1];
                Integer port = Integer.parseInt(inPortParts[2]);

                if (this.fileRouteProcessingService.isRouteExists(switchId, port)) {

                    System.out.println("################################################");
                    System.out.println("================== packet allowed ==============");
                    System.out.println("IN port connector: " + inPort);
                    System.out.println("Received packet from MAC match: " + srcMac);
                    System.out.println("Received packet to MAC match: " + dstMac);
                    System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
                    System.out.println("################################################");

                    // todo: check case for rule dropping when the src and dst MACs will be changed placed <---- in RPC API
                    List<Rule> rules = this.fileRouteProcessingService.getRouteRules(switchId, port);

                    for (Rule rule : rules) {
                        this.flowProcessingService.createOutputActionFlow(rule.switchId, rule.portIn.intValue(),
                                rule.portOut.intValue(), srcMacVal, dstMacVal);
                    }

                } else {

                    System.out.println("################################################");
                    System.out.println("================== PACKET BLOCKED ==============");
                    System.out.println("IN port connector: " + inPort);
                    System.out.println("Received packet from MAC match: " + srcMac);
                    System.out.println("Received packet to MAC match: " + dstMac);
                    System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
                    System.out.println("################################################");

                    this.flowProcessingService.createDropActionFlow(switchId, port, srcMacVal, dstMacVal);

                }
            }

        } catch (Exception e) {
            System.out.println("================== NOOOOOOO WAAAY ==============");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}