/*
 * Copyright © 2017 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FlowProcessingService {
    private static final String CTRL_RULE_PREFIX = "cloudshell-ctrl-rule";
    private static final Integer OUTPUT_RULE_PRIORITY = 1010;
    private static final Integer OUTPUT_RULE_TIMEOUT = 100;
    private static final Integer DROP_RULE_TIMEOUT = 0;
    private static final Integer DROP_RULE_PRIORITY = 510;
    private static final Integer CTRL_RULE_TIMEOUT = 0;
    private static final Integer CTRL_RULE_PRIORITY = 900;

    private final static Logger LOG = LoggerFactory.getLogger(FlowProcessingService.class);
    private static final byte[] ETH_TYPE_IPV4 = new byte[] { 0x08, 0x00 };
    private final SalFlowService salFlowService;

    public FlowProcessingService(final SalFlowService salFlowService) {
        this.salFlowService = salFlowService;
    }

    public void createDropActionFlow(String nodeId, Integer port, String srcMac, String dstMac) {
        System.out.println("BUILT DROp FLOW START !!!! >>>>>>>>");
        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();


        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class,
                new NodeKey(new NodeId(nodeId))).build();

        flowBuilder.setNode(new NodeRef(nodeIdRef));

        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);


        flowBuilder.setPriority(DROP_RULE_PRIORITY);
        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId, port.toString()));
        flowBuilder.setIdleTimeout(DROP_RULE_TIMEOUT);

        NodeConnectorId inPortNodeConnectorId = new NodeConnectorId(nodeId + ":" + port.toString());

        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
//        matchBuilder.setInPort(inPortNodeConnectorId);


        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
        ethernetSourceBuilder.setAddress(new MacAddress(srcMac));
        ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());

        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(new MacAddress(dstMac));
        ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());

        ethernetMatchBuilder.setEthernetType(new EthernetTypeBuilder()
                .setType(new EtherType((long) 0x800))
                .build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());


        flowBuilder.setMatch(matchBuilder.build());

        InstructionBuilder ib = new InstructionBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        InstructionsBuilder isb = new InstructionsBuilder();


        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        ab.setAction(new DropActionCaseBuilder().build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build()); // now action list is ready

        //apply the actions
        applyActionsBuilder.setAction(actionList);

        // now wrap it into instructions
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
        ib.setOrder(0);
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        flowBuilder.setInstructions(isb.build());

        System.out.println("ADD BUILT DROP FLOW DONE>>>>>>>>");

        this.salFlowService.addFlow(flowBuilder.build());
    }

    public void createOutputActionFlow(String nodeId, Integer inPort, Integer outPort, String srcMac, String dstMac) {
        System.out.println("BUILT OUTPUT FLOW START !!!! >>>>>>>>");
        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();


        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class,
                new NodeKey(new NodeId(nodeId))).build();

        flowBuilder.setNode(new NodeRef(nodeIdRef));

        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);

        NodeConnectorId inPortNodeConnectorId = new NodeConnectorId(nodeId + ":" + inPort.toString());

        flowBuilder.setPriority(OUTPUT_RULE_PRIORITY);
        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId, inPort.toString(), outPort.toString()));
        flowBuilder.setIdleTimeout(OUTPUT_RULE_TIMEOUT);

        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(inPortNodeConnectorId);

        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
        ethernetSourceBuilder.setAddress(new MacAddress(srcMac));
        ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());

        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(new MacAddress(dstMac));
        ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());

//        ethernetMatchBuilder.setEthernetType(new EthernetTypeBuilder()
//                .setType(new EtherType((long) 0x800))
//                .build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());


        flowBuilder.setMatch(matchBuilder.build());

        InstructionBuilder ib = new InstructionBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        InstructionsBuilder isb = new InstructionsBuilder();


        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();


        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
//        Uri outputActionUri = new Uri(nodeId + ":" + outPort);
        Uri outputActionUri = new Uri(outPort.toString());
        outputActionBuilder.setOutputNodeConnector(outputActionUri);

        ab.setAction(new OutputActionCaseBuilder().setOutputAction(outputActionBuilder.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build()); // now action list is ready

        //apply the actions
        applyActionsBuilder.setAction(actionList);

        // now wrap it into instructions
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
        ib.setOrder(0);
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        flowBuilder.setInstructions(isb.build());

        System.out.println("ADD BUILT DROP FLOW DONE>>>>>>>>");

        this.salFlowService.addFlow(flowBuilder.build());
    }

    public void createOutputControlleFlow(String nodeId, Integer inPort) {
        System.out.println("BUILT OUTPUT CONTROLLER FLOW START !!!! >>>>>>>>");
        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();


        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class,
                new NodeKey(new NodeId(nodeId))).build();

        flowBuilder.setNode(new NodeRef(nodeIdRef));

        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);

        NodeConnectorId inPortNodeConnectorId = new NodeConnectorId(nodeId + ":" + inPort.toString());

        flowBuilder.setPriority(CTRL_RULE_PRIORITY);
        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId, inPort.toString()));
        flowBuilder.setIdleTimeout(CTRL_RULE_TIMEOUT);

        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(inPortNodeConnectorId);

        flowBuilder.setMatch(matchBuilder.build());

        InstructionBuilder ib = new InstructionBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        InstructionsBuilder isb = new InstructionsBuilder();


        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();


        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
        Uri outputActionUri = new Uri(OutputPortValues.CONTROLLER.toString());
        // hardcoded for now

        outputActionBuilder.setOutputNodeConnector(outputActionUri);
        outputActionBuilder.setMaxLength(65535);

        ab.setAction(new OutputActionCaseBuilder().setOutputAction(outputActionBuilder.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build()); // now action list is ready

        //apply the actions
        applyActionsBuilder.setAction(actionList);

        // now wrap it into instructions
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
        ib.setOrder(0);
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        flowBuilder.setInstructions(isb.build());

        System.out.println("ADD BUILT CONTROLLER OUTPUT FLOW DONE>>>>>>>>");

        this.salFlowService.addFlow(flowBuilder.build());
    }

    public void removeOutputActionFlow(String nodeId, Integer inPort, Integer outPort, String srcMac, String dstMac) {
        System.out.println("BUILT REMOVE OUTPUT FLOW START !!!! >>>>>>>>");
        RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder();


        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class,
                new NodeKey(new NodeId(nodeId))).build();

        flowBuilder.setNode(new NodeRef(nodeIdRef));

        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);

        NodeConnectorId inPortNodeConnectorId = new NodeConnectorId(nodeId + ":" + inPort.toString());

        flowBuilder.setPriority(DROP_RULE_PRIORITY);
        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId));
        flowBuilder.setIdleTimeout(DROP_RULE_TIMEOUT);

        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(inPortNodeConnectorId);

        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
        ethernetSourceBuilder.setAddress(new MacAddress(srcMac));
        ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());

        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(new MacAddress(dstMac));
        ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());

        ethernetMatchBuilder.setEthernetType(new EthernetTypeBuilder()
                .setType(new EtherType((long) 0x800))
                .build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());


        flowBuilder.setMatch(matchBuilder.build());

        InstructionBuilder ib = new InstructionBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        InstructionsBuilder isb = new InstructionsBuilder();


        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();


        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
//        Uri outputActionUri = new Uri(nodeId + ":" + outPort);
        Uri outputActionUri = new Uri(outPort.toString());

        outputActionBuilder.setOutputNodeConnector(outputActionUri);

        ab.setAction(new OutputActionCaseBuilder().setOutputAction(outputActionBuilder.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build()); // now action list is ready

        //apply the actions
        applyActionsBuilder.setAction(actionList);

        // now wrap it into instructions
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActionsBuilder.build()).build());
        ib.setOrder(0);
        instructions.add(ib.build());
        isb.setInstruction(instructions);
        flowBuilder.setInstructions(isb.build());

        System.out.println("REMOVE OUTPUT DROP FLOW DONE>>>>>>>>");

        this.salFlowService.removeFlow(flowBuilder.build());
    }


    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("FlowProcessingService Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("FlowProcessingService Closed");
    }


}
