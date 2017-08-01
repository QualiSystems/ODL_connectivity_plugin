/*
 * Copyright © 2017 Qualisystems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package quali.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;



public class FileRouteProcessingService {
    private final static Logger LOG = LoggerFactory.getLogger(FileRouteProcessingService.class);
    private static final String ROUTES_PATH = "/home/shellroutes";
    private static final String SW_PORT_DELIMETER = "-";

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
        // delete existing rotes on start
        if (routesDir.exists()) {
            routesDir.delete();
        }

        routesDir.mkdir();
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

    private Boolean deleteFile(String fileName) {
        File f = new File(fileName);
        f.delete();
        return Boolean.TRUE;
    }

    public Boolean createRouteFile (String switchId, Integer port, String rules) {
        return this.writeToFile(switchId + SW_PORT_DELIMETER + port, rules);
    }

    public Boolean deleteRouteFile (String switchId, Integer port) {
        return this.deleteFile(switchId + SW_PORT_DELIMETER + port);
    }

    public Boolean isRouteExists(String switchId, Integer port) {
        File f = new File(switchId + SW_PORT_DELIMETER + port);
        return f.isFile();
    }
    public List<Rule> getRouteRules(String switchId, Integer port) {

        List<Rule> rules = new ArrayList<Rule>();
        JSONParser parser = new JSONParser();

        try {
            JSONArray array = (JSONArray) parser.parse(new FileReader(switchId + SW_PORT_DELIMETER + port));
            for (Object o : array)
            {
                JSONObject person = (JSONObject) o;

                Long portIn = (Long) person.get("port_in");
                System.out.println(portIn);

                Long portOut = (Long) person.get("port_out");
                System.out.println(portOut);

                String nodeId = (String) person.get("switch");
                System.out.println(nodeId);

                rules.add(new Rule(portIn, portOut, nodeId));
            }
        } catch (FileNotFoundException e) {
            System.out.println("JSON DONT WORK UP!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("JSON DONT WORK UP!");

            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("JSON DONT WORK UP!");
            e.printStackTrace();
        }

        return rules;
//
//        for (Object o : jsonArray) {
//            JSONObject person = (JSONObject) o;
//
//            String strName = (String) person.get("name");
//            System.out.println("Name::::" + strName);
//
//            String strCity = (String) person.get("city");
//            System.out.println("City::::" + strCity);
//
//            JSONArray arrays = (JSONArray) person.get("cars");
//            for (Object object : arrays) {
//                System.out.println("cars::::" + object);
//            }
//            String strJob = (String) person.get("job");
//            System.out.println("Job::::" + strJob);
//            System.out.println();
//
//        }

    }
}
