/*
 * $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The DOMS project.
 * Copyright (C) 2007-2010  The State and University Library
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package dk.statsbiblioteket.doms.bitstorage.highlevel.fedora;

import dk.statsbiblioteket.doms.bitstorage.characteriser.Characterisation;
import dk.statsbiblioteket.doms.bitstorage.highlevel.UrlProvider;
import dk.statsbiblioteket.doms.bitstorage.highlevel.fedora.exceptions.*;
import dk.statsbiblioteket.doms.bitstorage.highlevel.fedora.generated.*;
import org.apache.http.HttpRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Jan 20, 2010
 * Time: 9:48:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class FedoraSpeakerRestImpl implements FedoraSpeaker {

    private String contentDatastreamName;
    private String characterisationDatastreamName;


    private Unmarshaller unmarshaller;
    private Marshaller marshaller;

    private FedoraBasicRestSpeaker rest;


    public FedoraSpeakerRestImpl(String contentDatastreamName,
                                 String characterisationDatastreamName,
                                 String username,
                                 String password,
                                 String server,
                                 int port) {
        rest = new FedoraBasicRestSpeaker(username, password, server, port);
        this.contentDatastreamName = contentDatastreamName;
        this.characterisationDatastreamName = characterisationDatastreamName;

    }

    public void createContentDatastream(String pid,
                                        String url,
                                        String checksum)
            throws
            FedoraObjectNotFoundException,
            FedoraDatastreamAlreadyExistException,
            FedoraCommunicationException,
            FedoraChecksumFailedException {

        rest.objectExists(pid);
        try {
            rest.datastreamExists(pid, contentDatastreamName);
            throw new FedoraDatastreamAlreadyExistException(
                    "Datastream already exists, cannot create");
        } catch (FedoraDatastreamNotFoundException e) {
            //this is good, continue
        }
        try {
            rest.createExternalDatastream(pid,
                    contentDatastreamName,
                    url, checksum
            );
        } catch (ResourceNotFoundException e) {
            throw new FedoraCommunicationException("Should not fail");
        }


    }

    public void updateContentDatastream(String pid,
                                        String url,
                                        String checksum)
            throws
            FedoraObjectNotFoundException,
            FedoraCommunicationException,
            FedoraDatastreamNotFoundException {
        rest.objectExists(pid);
        rest.datastreamExists(pid, contentDatastreamName);
        try {
            rest.createExternalDatastream(pid,
                    contentDatastreamName,
                    url, checksum
            );
        } catch (ResourceNotFoundException e) {
            throw new FedoraCommunicationException("Should not fail");
        }


    }


    public Collection<String> getAllowedFormatURIs(String pid,
                                                   String datastream)
            throws
            FedoraObjectNotFoundException,
            FedoraDatastreamNotFoundException,
            FedoraCommunicationException {

        //not delegate
        ObjectProfile profile = rest.getObjectProfile(pid);
        List<String> cmodels = profile.getObjModels().getModel();

        Set<String> uris = new HashSet<String>();
        for (String cmodel : cmodels) {


            String content = rest.getDatastreamContents(cmodel,
                    "DS-COMPOSITE-MODEL");


            Object temp = null;
            try {
                temp = unmarshaller.unmarshal(new StringReader(content));
            } catch (JAXBException e) {
                throw new FedoraCommunicationException(e);
            }
            if (temp instanceof DsCompositeModel) {
                DsCompositeModel dsCompositeModel = (DsCompositeModel) temp;
                uris.addAll(extractFormatURIs(dsCompositeModel,
                        datastream));
            }
        }
        return uris;

    }

    private Set<String> extractFormatURIs(DsCompositeModel dsCompositeModel,
                                          String datastream) {
        Set<String> uris = new HashSet<String>();
        List<DsTypeModel> typemodels = dsCompositeModel.getDsTypeModel();
        for (DsTypeModel dsTypeModel : typemodels) {
            if (datastream.equals(dsTypeModel.getID())) {
                List<Form> forms = dsTypeModel.getForm();
                for (Form form : forms) {
                    String formaturi = form.getFORMATURI();
                    if (formaturi != null && !formaturi.isEmpty()) {
                        uris.add(formaturi);
                    }
                }
            }
        }
        return uris;
    }


    public void storeCharacterization(String pid,
                                      Characterisation characterisation)
            throws
            FedoraObjectNotFoundException,
            FedoraCommunicationException,
            FedoraDatastreamAlreadyExistException {


        //marshall the charac to a string or url
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //marshall charac to inputstream
        try {
            marshaller.marshal(characterisation, out);
        } catch (JAXBException e) {
            //TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        InputStream blob = new ByteArrayInputStream(out.toByteArray());


        //make blob available as URL
        String characurl;
        try {
            characurl = UrlProvider.registerBlob(blob,
                    characterisationDatastreamName,
                    "text/xml");
        } catch (IOException e) {//not gonna happen
            throw new RuntimeException(
                    "Could not read from ByteArrayInputStream somehow",
                    e);
        }

        rest.createInternalDatastream(pid,
                characterisationDatastreamName,
                characurl);
    }


    public boolean datastreamExists(String pid,
                                    String datastream)
            throws FedoraObjectNotFoundException, FedoraCommunicationException {
        try {
            rest.datastreamExists(pid, datastream);
        } catch (FedoraDatastreamNotFoundException e) {
            return false;
        }
        return true;
    }


    /**
     * Returns true if there is content in the datastream. The datastream has contet
     * if we can read the content without getting an exception
     *
     * @param pid        the pid of the object
     * @param datastream the datastream
     * @return true if as much as one character can be read from the stream
     * @throws FedoraObjectNotFoundException if the object was not found
     * @throws FedoraDatastreamNotFoundException
     *                                       if the datastream was not found
     * @throws FedoraCommunicationException  on anything else
     */
    public boolean datastreamHasContent(String pid,
                                        String datastream)
            throws
            FedoraObjectNotFoundException,
            FedoraDatastreamNotFoundException,
            FedoraCommunicationException {
        return rest.datastreamHasContent(pid, datastream);
    }

    public void deleteDatastream(String pid,
                                 String ds)
            throws
            FedoraObjectNotFoundException,
            FedoraDatastreamNotFoundException,
            FedoraCommunicationException {
        rest.deleteDatastream(pid, ds);
    }

    public String getFileUrl(String pid)
            throws
            FedoraObjectNotFoundException,
            FedoraDatastreamNotFoundException,
            FedoraCommunicationException {
        DatastreamProfile profile =
                rest.getDatastreamProfile(pid, contentDatastreamName);
        return profile.getDsLocation();

    }

    public String getFileChecksum(String pid)
            throws
            FedoraObjectNotFoundException,
            FedoraDatastreamNotFoundException,
            FedoraCommunicationException {

        DatastreamProfile profile =
                rest.getDatastreamProfile(pid, contentDatastreamName);
        return profile.getDsChecksum();
    }


}
