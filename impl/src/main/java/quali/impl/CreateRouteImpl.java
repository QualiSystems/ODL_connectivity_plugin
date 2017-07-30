/*
 * Copyright © 2016 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CloudshellService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;



import java.util.List;



import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;

import java.math.BigInteger;
import java.util.Random;
import java.util.ArrayList;

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


public class CreateRouteImpl implements CloudshellService {

    private static final String ROUTES_PATH = "/home/shellroutes";
    private static final String CTRL_RULE_PREFIX = "cloudshell-ctrl-rule";
    private static final Integer RULE_PRIORITY = 500;
    private static final Integer RULE_TIMEOUT = 1000;
    private final SalFlowService salFlowService;


    private static final Integer DROP_RULE_PRIORITY = 510;
    private static final Integer OUTPUT_RULE_PRIORITY = 1010;
    private static final Integer OUTPUT_RULE_TIMEOUT = 3000;
    private static final Integer DROP_RULE_TIMEOUT = 3000;


    public CreateRouteImpl(final SalFlowService salFlowService) {
        this.salFlowService = salFlowService;
        this.createRoutesDir();
    }

    private void createRoutesDir() {
        File routesDir = new File(ROUTES_PATH);

        if (!routesDir.exists()) {
            routesDir.mkdir();
        }
    }

    private void createControllerFlow(String nodeId, Integer port) {

        System.out.println("BUILT FLOW START !!!! >>>>>>>>");
        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();


        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(nodeId))).build();
        flowBuilder.setNode(new NodeRef(nodeIdRef));


        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);
        flowBuilder.setPriority(RULE_PRIORITY);
        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId, port.toString()));
        flowBuilder.setIdleTimeout(RULE_TIMEOUT);

        NodeConnectorId outputAction = new NodeConnectorId(nodeId + ":" + port.toString());

        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(outputAction);
        flowBuilder.setMatch(matchBuilder.build());

        InstructionBuilder ib = new InstructionBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        InstructionsBuilder isb = new InstructionsBuilder();


        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
        Uri outputActionUri = new Uri(OutputPortValues.CONTROLLER.toString());
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

        System.out.println("BUILT FLOW DONE>>>>>>>>");

        this.salFlowService.addFlow(flowBuilder.build());

    }


    private void removeControllerFlow(String nodeId, Integer port) {

        System.out.println("BUILT FLOW START !!!! >>>>>>>>");
        RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder();


        InstanceIdentifier<Node> nodeIdRef = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(nodeId))).build();
        flowBuilder.setNode(new NodeRef(nodeIdRef));


        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);
        flowBuilder.setPriority(RULE_PRIORITY);
        flowBuilder.setFlowName(String.join("-", CTRL_RULE_PREFIX, nodeId, port.toString()));
        flowBuilder.setIdleTimeout(RULE_TIMEOUT);

        NodeConnectorId outputAction = new NodeConnectorId(nodeId + ":" + port.toString());

        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(outputAction);
        flowBuilder.setMatch(matchBuilder.build());

        InstructionBuilder ib = new InstructionBuilder();
        List<Instruction> instructions = new ArrayList<Instruction>();
        InstructionsBuilder isb = new InstructionsBuilder();


        ApplyActionsBuilder applyActionsBuilder= new ApplyActionsBuilder();
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
        Uri outputActionUri = new Uri(OutputPortValues.CONTROLLER.toString());
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

        System.out.println("BUILT FLOW DONE>>>>>>>>");

        this.salFlowService.removeFlow(flowBuilder.build());

    }

    private Boolean writeToFile(String fileName, String data) {
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
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

        System.out.println("BUILT DROP FLOW DONE>>>>>>>>");

        this.salFlowService.addFlow(flowBuilder.build());
    }

    public void removeDropActionFlow(String nodeId, Integer port, String srcMac, String dstMac) {
        System.out.println("BUILT DROp FLOW START !!!! >>>>>>>>");
        RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder();


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

        System.out.println("BUILT DROP FLOW DONE>>>>>>>>");

        this.salFlowService.removeFlow(flowBuilder.build());
    }

    @Override
    public Future<RpcResult<CreateRouteOutput>> createRoute(CreateRouteInput input) {
        String nodeId = input.getSwitch();
        String route = input.getRoute();
        Integer port = input.getPort();

        // dummy check rules creation/rmoval
        if (port == 1) {
            System.out.println("!!!!!! CREATE RULE");
            this.createDropActionFlow("openflow:1", 1, "00:00:00:00:00:01", "00:00:00:00:00:04");
        } else {
            System.out.println("!!!!!! REMOVE RULE");
            this.removeDropActionFlow("openflow:1", 1, "00:00:00:00:00:01", "00:00:00:00:00:04");

        }
//        String routeFileName = String.join(File.separator, ROUTES_PATH, nodeId + port.toString());
//        System.out.println("The path is: " + routeFileName);
//
//        Boolean result = this.writeToFile(routeFileName, route);


        CreateRouteOutputBuilder createRouteBuilder = new CreateRouteOutputBuilder();
        createRouteBuilder.setSuccess(Boolean.TRUE);

        return RpcResultBuilder.success(createRouteBuilder.build()).buildFuture();
    }
}
