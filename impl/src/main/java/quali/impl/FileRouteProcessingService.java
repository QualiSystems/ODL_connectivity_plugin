/*
 * Copyright © 2017 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileRouteProcessingService {
    private final static Logger LOG = LoggerFactory.getLogger(FileRouteProcessingService.class);
    private static final String ROUTES_PATH = "/home/shellroutes";
    private static final String SRC_DST_MAC_DELIMETER = "-";

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        this.createRoutesDir();
        LOG.info("FlowProcessingService Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("FlowProcessingService Closed");
    }

    private void createRoutesDir() {
        File routesDir = new File(ROUTES_PATH);

        if (routesDir.exists()) {
            routesDir.delete();
        }


//        File routesDir = new File(ROUTES_PATH);

        if (!routesDir.exists()) {
            routesDir.mkdir();
        }
    }

    private Boolean createFile(String fileName) {
        File f = new File(fileName);

        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    private Boolean deleteFile(String fileName) {
        File f = new File(fileName);
        f.delete();
        return Boolean.TRUE;
    }

    public Boolean createRouteFile (String srcMac, String dstMac) {
        Boolean srcRouteSuccess = this.createFile(srcMac + SRC_DST_MAC_DELIMETER + dstMac);
        Boolean dstRouteSuccess = this.createFile(dstMac + SRC_DST_MAC_DELIMETER + srcMac);

        return srcRouteSuccess && dstRouteSuccess;
    }

    public Boolean deleteRouteFile (String srcMac, String dstMac) {
        Boolean srcRouteSuccess = this.deleteFile(srcMac + SRC_DST_MAC_DELIMETER + dstMac);
        Boolean dstRouteSuccess = this.deleteFile(dstMac + SRC_DST_MAC_DELIMETER + srcMac);

        return srcRouteSuccess && dstRouteSuccess;
    }
    public Boolean isRouteExists(String srcMac, String dstMac) {
        File f = new File(srcMac + SRC_DST_MAC_DELIMETER + dstMac);
        return f.isFile();
    }

}
