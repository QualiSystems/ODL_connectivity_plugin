/*
 * Copyright © 2016 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quali.cli.api.CloudshellCliCommands;

public class CloudshellCliCommandsImpl implements CloudshellCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(CloudshellCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public CloudshellCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("CloudshellCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}