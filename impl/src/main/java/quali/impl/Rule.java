/*
 * Copyright © 2017 Quali and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

public class Rule {
    public Long portIn;
    public Long portOut;
    public String switchId;

    public Rule (Long portIn, Long portOut, String switchId){
        this.portIn = portIn;
        this.portOut = portOut;
        this.switchId = switchId;
    }
}
