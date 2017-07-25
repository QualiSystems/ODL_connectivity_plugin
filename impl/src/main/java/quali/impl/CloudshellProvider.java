/*
 * Copyright © 2016 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloudshell.rev150105.CloudshellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;

public class CloudshellProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CloudshellProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration<CloudshellService> serviceRegistration;
    private final SalFlowService salFlowService;


    public CloudshellProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry, final SalFlowService salFlowService) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.salFlowService = salFlowService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(CloudshellService.class, new CreateRouteImpl(this.salFlowService));
        LOG.info("CloudshellProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("CloudshellProvider Closed");
    }
}