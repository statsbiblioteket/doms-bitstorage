/* $Id: RealTimeService.java $
 * $Revision: $
 * $Date: 2009-12-15 3:23:09 PM $
 * $Author: jrgatsb $
 *
 * The DOMS project.
 * Copyright (C) 2007-2009  The State and University Library
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
 */
package dk.statsbiblioteket.doms.bitstorage.lowlevel.surveillance;

import dk.statsbiblioteket.doms.bitstorage.lowlevel.*;
import dk.statsbiblioteket.doms.surveillance.status.Status;
import dk.statsbiblioteket.doms.surveillance.status.StatusMessage;
import dk.statsbiblioteket.doms.surveillance.status.Surveyable;
import dk.statsbiblioteket.util.qa.QAInfo;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/** Class that exposes real time system info as surveyable messages over REST.
 */
@QAInfo(level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_NEEDED,
        author = "jrg",
        reviewers = {""})
@Path("/RealTimeService/")      // Part of the url to this webservice, inserted
                                // at * in the relevant url-pattern in web.xml
public class RealTimeService implements Surveyable {
    private static Logger log = Logger.getLogger(RealTimeService.class);

    @Context
    private ServletConfig servletConfig;

    private String location;
    private int preferredSpaceInBitstorage; // In bytes
    private int requiredSpaceInBitstorage;  // In bytes

    private void initialize() {
        if (servletConfig != null) {
            String preferredBytesLeft;
            String requiredBytesLeft;
            location = servletConfig.getInitParameter(
                    "dk.statsbiblioteket.doms.bitstorage.lowlevel.location");
            preferredBytesLeft = servletConfig.getInitParameter(
                    "dk.statsbiblioteket.doms.bitstorage.lowlevel"
                            + ".preferredBytesLeft");
            requiredBytesLeft = servletConfig.getInitParameter(
                    "dk.statsbiblioteket.doms.bitstorage.lowlevel"
                            + ".requiredBytesLeft");
            preferredSpaceInBitstorage = Integer.parseInt(preferredBytesLeft);
            requiredSpaceInBitstorage = Integer.parseInt(requiredBytesLeft);
        } else {
        }
    }

    @GET
    @Path("getStatusSince/{date}")
    @Produces("application/xml")
    /** Returns only the current real time info.
     *
     * @param time This given date is ignored.
     * @return A status containing list of log messages.
     */
    public Status getStatusSince(@PathParam("date") long time) {
        return getStatus();
    }
    
    @GET
    @Path("getStatus")
    @Produces("application/xml")
    /** Returns real time info about the current state of the lowlevel
     * bitstorage webservice
     *
     * @return A status containing list of log messages.
     */
    public Status getStatus() {
        log.debug("Entered method getStatus");
        // This service accessible from
        // localhost:8080/lowlevelbitstorage/
        //   lowlevelbitstoragerealtimesurveillanceservice/RealTimeService/
        //   getStatus
        //
        // Or, in one line, for testing convenience:
        // http://localhost:8080/lowlevelbitstorage/lowlevelbitstoragerealtimesurveillanceservice/RealTimeService/getStatus

        
        String surveyeeName = "Low-level bitstorage";
        LowlevelBitstorageSoapWebserviceService bitstorageWebserviceFactory;
        URL wsdlLocation = null;
        LowlevelBitstorageSoapWebservice bitstorageService;
        Long spaceLeftInBitstorage;              // In bytes.
        ArrayList<StatusMessage> messageList = new ArrayList<StatusMessage>();
        StatusMessage message;

        try {
            log.debug("Making URL from location: '" + location + "'");
            wsdlLocation = new URL(location);
        } catch (MalformedURLException e) {
            throw new Error(e);
        }

        initialize();

        try {
            bitstorageWebserviceFactory
                    = new LowlevelBitstorageSoapWebserviceService(wsdlLocation,
                    new QName("http://"
                            + "lowlevel.bitstorage.doms.statsbiblioteket.dk/",
                            "LowlevelBitstorageSoapWebserviceService"));

            bitstorageService
                    = bitstorageWebserviceFactory.getLowlevelBitstorageSoapWebservicePort();
        } catch (Exception e) {
            // Report lowlevel bitstorage webservice is unreachable
            message = new StatusMessage("Lowlevel bitstorage webservice"
                    + " is unreachable.",
                    StatusMessage.Severity.RED,
                    System.currentTimeMillis(), false);
            messageList.add(message);
            return new Status(surveyeeName, messageList);
        }

        try {
            spaceLeftInBitstorage = bitstorageService.spaceleft();
        } catch (CommunicationException e) {
            // Report no comms with backend ssh-server
            message = new StatusMessage("Lowlevel bitstorage webservice"
                    + " was called but it couldn't communicate with"
                    + " backend ssh-server.",
                    StatusMessage.Severity.RED,
                    System.currentTimeMillis(), false);
            messageList.add(message);
            return new Status(surveyeeName, messageList);
        } catch (Exception e) {
            // Report something went wrong
            message = new StatusMessage("Something went wrong calling"
                    + " the lowlevel bitstorage webservice. Exception '"
                    + e.getClass().getName() + "' was thrown.",
                    StatusMessage.Severity.RED,
                    System.currentTimeMillis(), false);
            messageList.add(message);
            return new Status(surveyeeName, messageList);
        }

        if (spaceLeftInBitstorage < requiredSpaceInBitstorage) {
            // Report too little space
            message = new StatusMessage("Not enough space" +
                    "in bitstorage. Remaining size must be atleast "
                    + requiredSpaceInBitstorage + " bytes.",
                    StatusMessage.Severity.RED, System.currentTimeMillis(),
                    false);
        } else if (spaceLeftInBitstorage < preferredSpaceInBitstorage) {
            // Report close to too little space
            message = new StatusMessage("Space left in bitstorage is getting"
                    + " dangerously close to the lower limit in bitstorage."
                    + " Remaining size should be atleast "
                    + preferredSpaceInBitstorage + " bytes.",
                    StatusMessage.Severity.YELLOW, System.currentTimeMillis(),
                    false);
        } else {
            // Report everything ok
            message = new StatusMessage("Lowlevel bitstorage"
                    + " is up, and there is enough space.",
                    StatusMessage.Severity.GREEN,
                    System.currentTimeMillis(), false);
        }
        messageList.add(message);
        return new Status(surveyeeName, messageList);
    }

}


