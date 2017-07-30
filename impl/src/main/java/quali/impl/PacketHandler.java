/*
 * Copyright © 2017 Quali and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;


import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;


import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;



import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;


import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;


import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


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

        Class<? extends PacketInReason> pktInReason = packetReceived.getPacketInReason();

        // read src MAC and dst MAC
        byte[] dstMacRaw = extractDstMac(packetReceived.getPayload());
        byte[] srcMacRaw = extractSrcMac(packetReceived.getPayload());
        byte[] etherType = extractEtherType(packetReceived.getPayload());
//
        MacAddress dstMac = rawMacToMac(dstMacRaw);
        MacAddress srcMac = rawMacToMac(srcMacRaw);
        String srcMacVal = srcMac.getValue();
        String dstMacVal = dstMac.getValue();

        // check if there route file exists
        if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
            NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());
            String inPort = ingressKey.getId().getValue();

//            if (!this.fileRouteProcessingService.isRouteExists(srcMacVal, dstMacVal)) {
//
                System.out.println("################################################");
                System.out.println("================== PACKET BLOCKED ==============");
                System.out.println("IN port connector: " + inPort);
                System.out.println("Received packet from MAC match: " + srcMac);
                System.out.println("Received packet to MAC match: " + dstMac);
                System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
                System.out.println("################################################");

                // todo: check case for rule dropping when the src and dst MACs will be changed placed <---- in RPC API
                String[] array = inPort.split(":");

                this.flowProcessingService.createDropActionFlow(array[0] + ":" + array[1], Integer.parseInt(array[array.length - 1]), srcMacVal, dstMacVal);
//                this.flowProcessingService.removeDropActionFlow(array[0] + ":" + array[1], Integer.parseInt(array[array.length - 1]), srcMacVal, dstMacVal);
//
//            } else {
//                System.out.println("################################################");
//                System.out.println("================== packet allowed ==============");
//                System.out.println("IN port connector: " + inPort);
//                System.out.println("Received packet from MAC match: " + srcMac);
//                System.out.println("Received packet to MAC match: " + dstMac);
//                System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
//                System.out.println("################################################");
//            }
        }








        // todo: check if can use instanceof instead!
        // todo: check if it will catch all packets, even without ctrl action records
//        if (pktInReason == SendToController.class) { // use it only for new controller action (created from route save)

//            System.out.println("Received CONTROLLER NOTIFICATION !!!!!!!!!!!!");
//
//
//        if (pktInReason == SendToController.class) { // use it only for new controller action (created from route save)
//            System.out.println("GOT SEND TO CTRL ACTION");
//            System.out.println("################################################");
//            System.out.println("ETH_TYPE_IPV4 !!!! ETH_TYPE_IPV4");
//            System.out.println("Received packet from MAC match: " + srcMac);
//            System.out.println("Received packet to MAC match: " + dstMac);
//            System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
//            System.out.println("################################################");
//
//        }
//
//            if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
//
//                System.out.println("################################################");
//                System.out.println("ETH_TYPE_IPV4 !!!! ETH_TYPE_IPV4");
//                System.out.println("Received packet from MAC match: " + srcMac);
//                System.out.println("Received packet to MAC match: " + dstMac);
//                System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));
//
//
//                // todo: step 1: get incoming switch and port
//                // todo: step 2: open file with route
//                // todo: step 3: route exists - create rules with timeout (or endless????)
//                // todo: step 4: route does not exist - create rule to drop it/deny packet  (with timeout!!!! or endless????)
//                // stub
//
//                try {
//                    // stub mapping beetwen ports
//                    HashMap<String, String> connectedPorts = new HashMap<String, String>();
////                    connectedPorts.put("00:00:00:00:00:01", "00:00:00:00:00:04");
////                    connectedPorts.put("00:00:00:00:00:04", "00:00:00:00:00:01");
//
//                    String srcMacVal = srcMac.getValue();
//                    String dstMacVal = dstMac.getValue();
//
//
//                    String dstPort = connectedPorts.get(srcMacVal);
//                    System.out.println("HERE !!!!!!!!!!!!!!!! 3");
//
//                    if (dstMacVal.equals(dstPort)) {
//                        System.out.println("!!!!!!!!!!!!! ALLOW RULE, DO NOTHING !!!!!!!!!!!!!!");
//                        System.out.println("################################################");
//
//                    } else {
//
//                        System.out.println("!!!!!!!!!!!!! NEED TO DROP !!!!!!!!!!!!!!");
//                        System.out.println("################################################");
//
//
//                        NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());
//                        System.out.println(ingressKey);
//                        String inPort = ingressKey.getId().getValue();
//
//                        System.out.println("!!!!!!!!!!!!! IN port connector !!!!!!!!!!!!!!:   " + inPort);
//
//                        String[] array = inPort.split(":");
//
//
//                        System.out.println("!!!!!!!!!!!!! IN port PARSED NODE connector !!!!!!!!!!!!!!:   " + array[0] + ":" + array[1]);
//                        System.out.println("!!!!!!!!!!!!! IN port PARSED PORT connector !!!!!!!!!!!!!!:   " + Integer.parseInt(array[array.length - 1]));
//
//                        // todo: rework split in normal way !!!
//                        this.flowProcessingService.createDropActionFlow(array[0] + ":" + array[1], Integer.parseInt(array[array.length - 1]), srcMacVal, dstMacVal);
//
//                        // todo: add to rule ethernet match
//                        System.out.println("!!!!!!!!!!!!! DENY RULE TBD !!!!!!!!!!!!!!");
//                    }
//                } catch (Exception e) {
//                    System.out.println("!!!!!!!!!!!!! EXCEPTION!!!!!!!! !!!!!!!!!!!!!!");
//                    e.printStackTrace();
//
//                }

//                if (srcMac.getValue().equals("00:00:00:00:00:01")) {
//                    // stub don't allow route
//                    String  nodeId = "openflow:1";
//                    Integer port = 1;
//                    this.flowProcessingService.createDropActionFlow(nodeId, port);
//                } else {
//                    System.out.println("Packet can passss with the God's help");
////                    this.flowProcessingService.createOutputActionFlow(nodeId, port);
//                }
                /// check file -last port -> if matches with dst_mac from the package - allow
//                this.createControllerFlow("openflow:1", 1);
//                System.out.println("DROP FLOW BUILDED!!!1!!!!!!!!!");

                // todo: drop packet if no route there !!!!
//
//                try {
//                    System.out.println("MAtched IN connector");
//                        NodeConnectorRef igness = packetReceived.getIngress();
//                        System.out.println("DAFAK???");
//                        System.out.println(igness.getValue());
//                    } catch (Exception e) {
//                        System.out.println("SOME SHIT HAPPENS");
//                        System.out.println(e.getMessage());
//                        e.printStackTrace();
//                    }
//
//                try {
//                    System.out.println(packetReceived.getMatch().getEthernetMatch().getEthernetSource().toString());
//                    System.out.println(packetReceived.getMatch().getEthernetMatch().getEthernetDestination().toString());
//                } catch (Exception e) {
//                    System.out.println("SOME SHIT HAPPENS");
//                    System.out.println(e.getMessage());
//                    e.printStackTrace();
//                }
//
//                try {
//                    System.out.println("Matchecd L3 data");
//                    System.out.println(packetReceived.getMatch().getLayer3Match().toString());
//                } catch (Exception e) {
//                    System.out.println("SOME SHIT HAPPENS");
//                    System.out.println(e.getMessage());
//                    e.printStackTrace();
//                }
//
//                try {
//                    System.out.println("Matchecd Metatdata");
//                    System.out.println(packetReceived.getMatch().getMetadata());
//                } catch (Exception e) {
//                    System.out.println("SOME SHIT HAPPENS");
//                    System.out.println(e.getMessage());
//                    e.printStackTrace();
//                }
//                System.out.println("################################################");

//            }


//            InstanceIdentifier<?> ingressPort = packetReceived.getIngress().getValue();
//            InstanceIdentifier<Node> nodeOfPacket = ingressPort.firstIdentifierOf(Node.class);
//
//                System.out.println("PAYLOAD:::");
//                byte[] payload = packetReceived.getPayload();
//                System.out.println("PAYLOAD::: DONE");
//                System.out.println(payload);
//
//                byte[] dstMacRaw = extractDstMac(packetReceived.getPayload());
//                System.out.println("MAC ADDRESS!!!");
//                System.out.println(rawMacToMac(dstMacRaw));







//            System.out.println("PacketReceived  invoked INVOKED  SUPERPACKET !!!!!!!!!!!! !!!!!!!");
//            try {
//                System.out.println("MAtched IN connector");
//                NodeConnectorRef igness = packetReceived.getIngress();
//                System.out.println("DAFAK???");
//                System.out.println(igness.getValue());
//            } catch (Exception e) {
//                System.out.println("SOME SHIT HAPPENS");
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
//
//
//
//
//                //        System.out.println(packetReceived.toString());
//                //        System.out.println(packetReceived.getIngress().toString());
//
//
//                //            NodeConnectorId nodeConnectorId = packetReceived.getMatch().getInPort();
//                //            System.out.println("MAtched IN PORT");
//                //            System.out.println(nodeConnectorId.toString());
//                //            System.out.println(nodeConnectorId.getValue());
//
//            try {
//                System.out.println("Matchecd Ethernet data");
//                System.out.println(packetReceived.getMatch().toString());
//            } catch (Exception e) {
//                System.out.println("SOME SHIT HAPPENS");
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
//
////            System.out.println(packetReceived.getMatch().getEthernetMatch().getEthernetSource().toString());
////            System.out.println(packetReceived.getMatch().getEthernetMatch().getEthernetDestination().toString());
//
//            try {
//                System.out.println("Matchecd L3 data");
//                System.out.println(packetReceived.getMatch().getLayer3Match().toString());
//            } catch (Exception e) {
//                System.out.println("SOME SHIT HAPPENS");
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
//
//            try {
//                System.out.println("Matchecd Metatdata");
//                System.out.println(packetReceived.getMatch().getMetadata());
//            } catch (Exception e) {
//                System.out.println("SOME SHIT HAPPENS");
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
//
//        } else {
//            System.out.println("Not our client, ignore it");
//        }


//        Class<? extends PacketInReason> packedInReason = packetReceived.getPacketInReason();


//
//        packetReceived.getPayload();
//
//
//        short tableId = packetReceived.getTableId().getValue();
//        byte[] data = packetReceived.getPayload();
//        System.out.println(tableId);
//        System.out.println(data);

//        // BigInteger metadata =
//        // notification.getMatch().getMetadata().getMetadata();
//        Ethernet res = new Ethernet();
//
//        try {
//            res.deserialize(data, 0, data.length * NetUtils.NumBitsInAByte);
//        } catch (Exception e) {
//            LOG.warn("Failed to decode Packet ", e);
//            return;
//        }
//        try {
//            Packet pkt = res.getPayload();
//            if (pkt instanceof IPv4) {
//                IPv4 ipv4 = (IPv4) pkt;
//                // Handle IPv4 packet
//            }
//            return;
//
//        } catch (Exception ex) {
//            // Failed to handle packet
//            LOG.error("Failed to handle subnetroute packets ", ex);
//        }
//        return;

    }
}