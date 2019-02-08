/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventoryException;
import org.onap.so.heatbridge.aai.api.ActiveAndAvailableInventory;
import org.onap.so.logger.MsoLogger;

public class RollbackFromAai {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, RollbackFromAai.class);
    private final ActiveAndAvailableInventory aaiClient;

    public RollbackFromAai(final ActiveAndAvailableInventory aaiClient) {
        this.aaiClient = aaiClient;
    }

    public boolean execute(final List<AaiAction<ActiveAndAvailableInventory>> aaiActions) {
        String methodName = this.getClass().getSimpleName() + ": execute";
        boolean rollbackSucceed = true;
        List<AaiAction<ActiveAndAvailableInventory>> orderedAaiActions = ImmutableList.copyOf(aaiActions)
            .reverse();
        for (final AaiAction aaiAction : orderedAaiActions) {
            try {
                if (aaiAction.isSubmitted()) {
                    aaiAction.rollback(aaiClient);
                    LOGGER.debug(methodName + " Successful rollback AAI command: " + ToStringBuilder
                        .reflectionToString(aaiAction, ToStringStyle.JSON_STYLE));
                }
            } catch (final ActiveAndAvailableInventoryException e) {
                LOGGER.recordAuditEvent(System.currentTimeMillis(), MsoLogger.StatusCode.ERROR,
                    MsoLogger.ResponseCode.InternalError,
                    methodName + " Failed to rollback AAI command: " + ToStringBuilder
                        .reflectionToString(aaiAction,
                            ToStringStyle.JSON_STYLE) + " Error: " + Throwables.getRootCause(e).getMessage());
                rollbackSucceed = false;
            } catch (final Exception e) {
                LOGGER.recordAuditEvent(System.currentTimeMillis(), MsoLogger.StatusCode.ERROR,
                    MsoLogger.ResponseCode.UnknownError,
                    methodName + " Unknown exception while rolling back AAI command: " + ToStringBuilder
                        .reflectionToString(aaiAction,
                            ToStringStyle.JSON_STYLE) + " Error: " + Throwables.getRootCause(e).getMessage());
                rollbackSucceed = false;
            }
        }
        return rollbackSucceed;
    }
}
