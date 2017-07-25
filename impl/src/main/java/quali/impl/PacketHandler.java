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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;


import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class PacketHandler implements PacketProcessingListener {

    private final static Logger LOG = LoggerFactory.getLogger(PacketHandler.class);
    private final DataBroker dataBroker;
    private final PacketProcessingService packetProcessor;

    private static final byte[] ETH_TYPE_IPV4 = new byte[] { 0x08, 0x00 };


    public PacketHandler(DataBroker dataBroker, PacketProcessingService packetProcessor) {
        super();
        LOG.info("PacketHandler Session Instabstiated !!!!");
        this.dataBroker = dataBroker;
        this.packetProcessor = packetProcessor;
        System.out.println("PacketHandler INTIALIZED !!!!!!!");
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

    private void createControllerFlow(String nodeId, Integer port) {

//        System.out.println("BUILT FLOW START !!!! >>>>>>>>");
//        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();
//
//
//        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(nodeId))).build();
//        flowBuilder.setNode(new NodeRef(nodeIdRef));
//
//
//        flowBuilder.setBarrier(true);
//        flowBuilder.setTableId((short) 0);
//        flowBuilder.setPriority(RULE_PRIORITY);
//        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId, port.toString()));
//        flowBuilder.setIdleTimeout(RULE_TIMEOUT);
//
//        NodeConnectorId outputAction = new NodeConnectorId(nodeId + ":" + port.toString());
//
//        // set match
//        MatchBuilder matchBuilder = new MatchBuilder();
//        matchBuilder.setInPort(outputAction);
//        flowBuilder.setMatch(matchBuilder.build());
//
//        InstructionBuilder ib = new InstructionBuilder();
//        List<Instruction> instructions = new ArrayList<Instruction>();
//        InstructionsBuilder isb = new InstructionsBuilder();
//
//
//        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
//        List<Action> actionList = new ArrayList<Action>();
//        ActionBuilder ab = new ActionBuilder();
//        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
//        Uri outputActionUri = new Uri(OutputPortValues.CONTROLLER.toString());
//        outputActionBuilder.setOutputNodeConnector(outputActionUri);
//
//        ab.setAction(new OutputActionCaseBuilder().setOutputAction(outputActionBuilder.build()).build());
//        ab.setOrder(0);
//        ab.setKey(new ActionKey(0));
//        actionList.add(ab.build()); // now action list is ready
//
//        //apply the actions
//        applyActionsBuilder.setAction(actionList);
//
//        // now wrap it into instructions
//        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
//        ib.setOrder(0);
//        instructions.add(ib.build());
//        isb.setInstruction(instructions);
//        flowBuilder.setInstructions(isb.build());
//
//        System.out.println("BUILT FLOW DONE>>>>>>>>");
//
//        this.salFlowService.addFlow(flowBuilder.build());

    }

//    private Boolean writeToFile(String fileName, String data) {
//        try {
//            FileWriter writer = new FileWriter(fileName);
//            writer.write(data);
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Boolean.FALSE;
//        }
//
//        return Boolean.TRUE;
//    }

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


//    public static NodeConnectorKey getNodeConnectorKey(InstanceIdentifier<?> nodeConnectorPath) {
//        return nodeConnectorPath.firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
//    }

    @Override
    public void onPacketReceived(PacketReceived packetReceived) {

        Class<? extends PacketInReason> pktInReason = packetReceived.getPacketInReason();

        // todo: check if can use instanceof instead!
        if (pktInReason == SendToController.class) {

            // read src MAC and dst MAC
            byte[] dstMacRaw = extractDstMac(packetReceived.getPayload());
            byte[] srcMacRaw = extractSrcMac(packetReceived.getPayload());
            byte[] etherType = extractEtherType(packetReceived.getPayload());


            MacAddress dstMac = rawMacToMac(dstMacRaw);
            MacAddress srcMac = rawMacToMac(srcMacRaw);
//            NodeConnectorKey ingressKey = getNodeConnectorKey(packetReceived.getIngress().getValue());


            System.out.println("Received packet from MAC match: " + srcMac);
            System.out.println("Received packet to MAC match: " + dstMac);
            System.out.println("Ethertype: " + Integer.toHexString(0x0000ffff & ByteBuffer.wrap(etherType).getShort()));


            if (Arrays.equals(ETH_TYPE_IPV4, etherType)) {
                System.out.println("ETH_TYPE_IPV4 !!!! ETH_TYPE_IPV4");
            }


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

        } else {
            System.out.println("Not our client, ignore it");
        }


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