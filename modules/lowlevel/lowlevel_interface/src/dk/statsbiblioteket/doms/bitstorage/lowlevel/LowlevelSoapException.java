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

package dk.statsbiblioteket.doms.bitstorage.lowlevel;


/**
 * Created by IntelliJ IDEA.
 * User: abr
 * Date: Jan 20, 2010
 * Time: 5:51:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class LowlevelSoapException extends Exception{
    public LowlevelSoapException() {
    }

    public LowlevelSoapException(String message) {
        super(message);
    }

    public LowlevelSoapException(String message, Throwable cause) {
        super(message, cause);
    }

    public LowlevelSoapException(Throwable cause) {
        super(cause);
    }
}
