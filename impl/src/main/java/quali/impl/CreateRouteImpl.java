/*
 * Copyright © 2016 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CloudshellService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CreateRouteOutputBuilder;
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

        String srcSwitch = input.getSrcSwitch();
        Integer srcPort = input.getSrcPort();
        String dstSwitch = input.getDstSwitch();
        Integer dstPort = input.getDstPort();
        String srcRules = input.getSrcRules();
        String dstRules = input.getDstRules();
        Boolean allow = input.isAllow();


        if (allow) {
            this.flowProcessingService.createOutputControlleFlow(srcSwitch, srcPort);
            this.flowProcessingService.createOutputControlleFlow(dstSwitch, dstPort);

            this.fileRouteProcessingService.createRouteFile(srcSwitch, srcPort, srcRules);
            this.fileRouteProcessingService.createRouteFile(dstSwitch, dstPort, dstRules);
            // todo: add
        } else {
            this.fileRouteProcessingService.deleteRouteFile(srcSwitch, srcPort);
            this.fileRouteProcessingService.deleteRouteFile(dstSwitch, dstPort);
        }

        CreateRouteOutputBuilder createRouteBuilder = new CreateRouteOutputBuilder();
        createRouteBuilder.setSuccess(Boolean.TRUE);

        return RpcResultBuilder.success(createRouteBuilder.build()).buildFuture();
    }
}
