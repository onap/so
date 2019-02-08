/*
 * Copyright (C) 2017 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.so.heatbridge.actions;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;

/**
 * Object that take AAI request an submit it.
 */
public interface AaiAction<T> extends Serializable {


    boolean isSubmitted();

    /**
     * submit an AAI request.
     *
     * @param inventory AAIV11 client
     * @throws ActiveAndAvailableInventoryException if the AAI request fails
     * @throws UnsupportedEncodingException if there is an issue with encoding
     */
    void submit(T inventory) throws ActiveAndAvailableInventoryException, UnsupportedEncodingException;

    /**
     * Execute a rollback of AAI request.
     *
     * @param inventory AAIV11 client
     * @throws ActiveAndAvailableInventoryException if the rollback AAI request fails
     * @throws UnsupportedEncodingException if there is an issue with encoding
     */
    void rollback(T inventory) throws ActiveAndAvailableInventoryException, UnsupportedEncodingException;
}
