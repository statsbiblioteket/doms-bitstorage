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

package dk.statsbiblioteket.doms.bitstorage.lowlevel.backend.exceptions;

import dk.statsbiblioteket.util.qa.QAInfo;

/**
 * An exception signalling "File not found"
 */
@QAInfo(author = "abr",
        reviewers = "kfc",
        level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class FileNotFoundException extends BitstorageException {
    /**
     * Initialise a FileNotFound exception.
     *
     * @param message A description of the error message. Should always contain
     *                information about what file is not found.
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Initialise a FileNotFound exception.
     *
     * @param message A description of the error message. Should always contain
     *                information about what file is not found.
     * @param cause   An exception that caused this exception.
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
