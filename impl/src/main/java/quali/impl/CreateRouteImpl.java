/*
 * Copyright © 2016 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CloudshellService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.create.route.input.Route;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;


public class CreateRouteImpl implements CloudshellService {

    private final FlowProcessingService flowProcessingService;
    private final FileRouteProcessingService fileRouteProcessingService;

    public CreateRouteImpl(final FlowProcessingService flowProcessingService,
                           final FileRouteProcessingService fileRouteProcessingService) {

        this.flowProcessingService = flowProcessingService;
        this.fileRouteProcessingService = fileRouteProcessingService;
    }

    @Override
    public Future<RpcResult<CreateRouteOutput>> createRoute(CreateRouteInput input) {
        String srcMac = input.getSrcMac();
        String dstMac = input.getDstMac();
        List<Route> route = input.getRoute();
        Boolean allow = input.isAllow();
        // todo: fetch all nodes and create remove for each one

        if (allow) {
            // todo: do it for all nodes between them ---- check if it helps
            // for route in routes
            for (Route rule: route) {
                this.flowProcessingService.createOutputActionFlow(rule.getSwitch(), rule.getPortIn(),
                        rule.getPortOut(), srcMac, dstMac);

                this.flowProcessingService.createOutputActionFlow(rule.getSwitch(), rule.getPortOut(),
                        rule.getPortIn(), dstMac, srcMac);
            }


//            this.flowProcessingService.removeDropActionFlow(nodeId, port, dstMac, srcMac);

            // todo: node will not be the same here !!!
//            this.flowProcessingService.removeDropActionFlow(nodeId, port, dstMac, srcMac);
//            this.fileRouteProcessingService.createRouteFile(srcMac, dstMac);

        } else {
            for (Route rule: route) {
                this.flowProcessingService.removeOutputActionFlow(rule.getSwitch(), rule.getPortIn(),
                        rule.getPortOut(), srcMac, dstMac);

                this.flowProcessingService.removeOutputActionFlow(rule.getSwitch(), rule.getPortOut(),
                        rule.getPortIn(), dstMac, srcMac);
            }
 
            // todo: remove
//            this.fileRouteProcessingService.deleteRouteFile(srcMac, dstMac);
//            this.flowProcessingService.createDropActionFlow(nodeId, port, dstMac, srcMac);
        }

        CreateRouteOutputBuilder createRouteBuilder = new CreateRouteOutputBuilder();
        createRouteBuilder.setSuccess(Boolean.TRUE);

        return RpcResultBuilder.success(createRouteBuilder.build()).buildFuture();
    }
}
