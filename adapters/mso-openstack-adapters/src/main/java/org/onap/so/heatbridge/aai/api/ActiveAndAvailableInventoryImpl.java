/*-
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
 * limitations under the License.de
 */

package org.onap.so.heatbridge.aai.api;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import feign.Response;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.http.HttpStatus;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.heatbridge.aai.client.ActiveAndAvailableInventoryClient;
import org.onap.so.heatbridge.aai.factory.AaiObjectBuilder;
import org.onap.so.heatbridge.decoder.XmlResponseDecoderUsingJackson;
import org.onap.so.heatbridge.utils.FeignUtils;
import org.onap.so.logger.MsoLogger;
import org.springframework.web.util.UriUtils;

public class ActiveAndAvailableInventoryImpl implements ActiveAndAvailableInventory {

    private static final String NOT_FOUND_IN_AAI = " not found in AAI ";
    private static final String FOUND_IN_AAI = " found in AAI";
    private static final int DEFAULT_GET_PNF_BY_NAME_DEPTH = 1;
    private static final String STR_EXCEPTION = " Exception : ";
    private static final String STR_ALL = "all";
    private static final String STR_GENERIC_VNF_ID = " generic-vnf-id: ";
    private static final String STR_LINK_NAME = " linkName:";
    private static final String P_INTERFACE_INTERFACE_NAME = "p-interface.interface-name";
    private static final String P_INTERFACE = "p-interface";
    private static final String PSERVER = "pserver";
    private static final String PNF_PNF_NAME = "pnf.pnf-name";
    private static final String STR_NULL = "null";

    private ActiveAndAvailableInventoryClient inventoryClient = ActiveAndAvailableInventoryClient.connect();
    private AaiObjectBuilder builder;
    private static final String CLASSNAME = "ActiveAndAvailableInventoryImpl";
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ActiveAndAvailableInventoryImpl.class);



    /*
     * Cloud Infrastructure Objects
     */
    @Override
    public void addVserver(Vserver vserver, String cloudOwner, String cloudRegionId,
        String tenantId) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": addVserver : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("vserver:%s cloudOwner:%s cloudRegionId:%s tenantID:%s",
            vserver == null ? STR_NULL : vserver.getVserverId(), cloudOwner, cloudRegionId, tenantId);
        try {
            Preconditions.checkNotNull(vserver, "addVserver vserver parameter is null");
            Preconditions.checkState(!Strings.isNullOrEmpty(vserver.getVserverId()), "addVserver vserver-id key is not set");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "addVserver cloudOwner is not set");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "addVserver cloudRegionId is not set");
            Preconditions.checkState(!Strings.isNullOrEmpty(tenantId), "addVserver tenantId is not set");
            inventoryClient.addVserver(vserver, cloudOwner, cloudRegionId, tenantId, vserver.getVserverId());
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);

        } catch (Exception ex) {
            String errMsg = "Unable to add vserver for input" + inputData;
            logErrorToDebugAndAudit(startTime, errMsg, ex);
            throw new ActiveAndAvailableInventoryException(errMsg + STR_EXCEPTION + ex, ex);
        }
    }

    @Override
    public void deleteVservers(final String cloudOwner,  final String cloudRegionId,
         String tenantId,  final List<String> vserverIds) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": deleteVservers : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("cloudOwner:%s cloudRegionId:%s tenantId:%s vserverIds:%s",
            cloudOwner, cloudRegionId, tenantId, vserverIds == null ? STR_NULL : vserverIds);
        //check input
        try{
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "deleteVservers cloudOwner parameter is not set!");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "deleteVservers cloudRegionId parameter is not set!");
            Preconditions.checkState(!Strings.isNullOrEmpty(tenantId), "deleteVservers tenantId parameter is not set!");

            for (String vserverId : vserverIds) {
                inputData = String.format("cloudOwner:%s cloudRegionId:%s tenantId:%s vserverId:%s",
                    cloudOwner, cloudRegionId, tenantId, vserverId);
                Response response = inventoryClient.deleteVserver(cloudOwner, cloudRegionId, tenantId, vserverId);
                checkResponse(response);
                response.close();
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + inputData);
                startTime = System.currentTimeMillis(); //log each invocation
            }
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to delete vserver for input" + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void addImage(Image image,  String cloudOwner,  String cloudRegionId) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": addImage : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("image:%s cloudOwner:%s cloudRegionId:%s",
            image.getImageId(), cloudOwner, cloudRegionId);
        try {
            Preconditions.checkNotNull(image, "addImage expects non-null image parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(image.getImageId()), "addImage image.image-id is not set!");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "getImageIfPresent expects non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "getImageIfPresent expects non-empty cloudRegionId parameter");
            Response response = inventoryClient.addImage(image, cloudOwner, cloudRegionId, image.getImageId());
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to add image for input " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public PInterface getPserverPInterfaceByName(String pserverHostName, String pInterfaceName) throws
        ActiveAndAvailableInventoryException {
        long startTime = System.currentTimeMillis();
        final String methodName = CLASSNAME + ": getPserverPInterfaceByName : ";
        String inputData = String.format("pserverHostName:%s pInterfaceName:%s ",
            pserverHostName, pInterfaceName);

        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(pserverHostName), "getPserverPInterfaceByName expected non-empty pserverHostName parameter!");
            Preconditions.checkState(!Strings.isNullOrEmpty(pInterfaceName), "getPserverPInterfaceByName expected non-empty pInterfaceName parameter!");
            final PInterface pint = inventoryClient.getPhysicalInterfaceForPserver(pserverHostName, pInterfaceName);
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);
            return pint;
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to get p-server's p-interface "
                + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public Pserver getPserverByServerName(String serverName) throws ActiveAndAvailableInventoryException {
        long startTime = System.currentTimeMillis();
        final String methodName = CLASSNAME + ": getPserverByServerName : ";
        String inputData = String.format("serverName:%s ", serverName);

        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(serverName),
                "getPserverByServerName expected non-empty serverName parameter!");
            final Pserver pserver = inventoryClient.getPserver(serverName);
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);
            return pserver;
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to get Physical server "
                + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T getAaiObjectByUriIfPresent(@Nonnull String uri, @Nonnull Class<T> clazz) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": getAaiObjectByUriIfPresent : ";
        long startTime = System.currentTimeMillis();
        try {
            Response response = checkResponseByAllowing404(inventoryClient.getObjectFromUriAsFeignResponse(uri));
            if (response == null) {
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + " "
                        + "object: " + uri + NOT_FOUND_IN_AAI);
                return null;
            } else {
                T aaiObject = new XmlResponseDecoderUsingJackson(clazz).decode(response);
                response.close();
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc,
                    methodName + " AAI object found:" + uri + FOUND_IN_AAI);
                return aaiObject;
            }
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, uri, ex);
            throw new ActiveAndAvailableInventoryException("Unable to get: " + uri + ". Error: " + ex, ex);
        }
    }

    @Override
    public Image getImageIfPresent(String cloudOwner,  String cloudRegionId,  String imageId) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": getImageIfPresent : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("cloudOwner:%s cloudRegionId:%s imageId:%s", cloudOwner, cloudRegionId, imageId);
        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "getImageIfPresent expects non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "getImageIfPresent expects non-empty cloudRegionId parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(imageId), "getImageIfPresent expects non-empty imageId parameter");
            Response response = checkResponseByAllowing404(inventoryClient.getImage(cloudOwner, cloudRegionId, imageId));
            if(response == null) {
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + NOT_FOUND_IN_AAI + inputData);
                return null;
            } else {
                Image image = new XmlResponseDecoderUsingJackson(Image.class).decode(response);
                response.close();
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + FOUND_IN_AAI + inputData);
                return image;
            }
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to fetch image-id for input data " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public Flavor getFlavorIfPresent(String cloudOwner,  String cloudRegionId,  String flavorId) throws
        ActiveAndAvailableInventoryException {
        //TODO: convert to Optional<Flavor>
        final String methodName = CLASSNAME + ": getFlavorIfPresent : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("cloudOwner:%s cloudRegionId:%s flavorId:%s", cloudOwner, cloudRegionId, flavorId);
        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "deleteImages expected non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "deleteImages expected non-empty cloudRegionId parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(flavorId), "deleteImages expected non-empty flavorId parameter");
            Response response = checkResponseByAllowing404(
                inventoryClient.getFlavor(cloudOwner, cloudRegionId, flavorId));
            if(response == null) {
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + " flavor-id not found for input " + inputData );
                return null;
            } else {
                Flavor flavor = new XmlResponseDecoderUsingJackson(Flavor.class).decode(response);
                response.close();
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + "flavor-id was found in AAI for input " + inputData );
                return flavor;
            }
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to fetch flavor for input  " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteImages(final String cloudOwner,  final String cloudRegionId,
         final List<String> imageIds) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": deleteImages : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("cloudOwner: %s cloudRegionId: %s imageIds:%s",
            cloudOwner, cloudRegionId, imageIds == null ? STR_NULL : "[" + String.join(",", imageIds) + "]");

        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner),
                "deleteImages expected non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId),
                "deleteImages expected non-empty cloudRegionId parameter");
            Preconditions.checkNotNull(imageIds, "deleteImages expected non-null imageIds list.");

            for (String imageId : imageIds) {
                inputData = String.format("cloudOwner: %s cloudRegionId: %s imageId:%s",
                    cloudOwner, cloudRegionId, imageId);
                Response response = inventoryClient.deleteImage(cloudOwner, cloudRegionId, imageId);
                checkResponse(response);
                response.close();
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc, methodName + inputData);
                startTime = System.currentTimeMillis(); //logging how long it took for each invocation.
            }

        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to delete image "
                + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void addFlavor(Flavor flavor,  String cloudOwner,  String cloudRegionId) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": addFlavor : ";
        long startTime = System.currentTimeMillis();
        String inputData = "";
        try {
            inputData = String .format("flavor:%s cloudOwner:%s cloudRegionId:%s", flavor != null ? flavor.getFlavorId() : STR_NULL,
                    cloudOwner, cloudRegionId);
            Preconditions.checkNotNull(flavor, "addFlavor expects non-null flavor parameter");
            Preconditions .checkState(!Strings.isNullOrEmpty(cloudOwner),
                "addFlavor expected non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId),
                "addFlavor expected non-empty cloudRegionId parameter");
            Response response = inventoryClient.addFlavor(flavor, cloudOwner, cloudRegionId, flavor.getFlavorId());
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc,
                methodName + " flavor was found in AAI for input " + inputData);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException(
                "Unable to add flavor for input " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteFlavors(final String cloudOwner,  final String cloudRegionId,
         final List<String> flavorIds) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": deleteFlavors : ";
        long startTime = System.currentTimeMillis();
        String inputData = String
            .format("cloudOwner:%s cloudRegionId:%s flavorIds:%s", cloudOwner, cloudRegionId, flavorIds);
        try {
            Preconditions.checkNotNull(flavorIds, "deleteFlavors expects non-null flavorIds parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner),
                "deleteFlavors expected non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId),
                "deleteFlavors expected non-empty cloudRegionId parameter");
            for (String flavorId : flavorIds) {
                inputData = String.format("cloudOwner:%s cloudRegionId:%s flavor:%s",
                    cloudOwner, cloudRegionId, flavorId);
                Response response = inventoryClient.deleteFlavor(cloudOwner, cloudRegionId, flavorId);
                checkResponse(response);
                response.close();
                LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                    MsoLogger.ResponseCode.Suc,
                    methodName + " for input" + inputData);
                startTime = System.currentTimeMillis();
            }
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException(
                "Unable to delete flavor for input " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void addLInterfaceToVserver(LInterface lIf, String cloudOwner,
        String cloudRegionId, String tenantId, String vserverId) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": addLInterfaceToVserver : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("lIf:%s cloudOwner:%s cloudRegionId:%s tenantId:%s vserverId:%s",
            lIf != null ? lIf.getInterfaceName() : STR_NULL, cloudOwner, cloudRegionId, tenantId, vserverId);
        try {
            Preconditions.checkNotNull(lIf, "addLInterfaceToVserver expects non-null lIf parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "addLInterfaceToVserver expected non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "addLInterfaceToVserver expected non-empty cloudRegionId parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(tenantId), "addLInterfaceToVserver expected non-empty tenantId parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(vserverId), "addLInterfaceToVserver expected non-empty vserverId parameter");
            Response response = inventoryClient.addLInterfaceToVserver(lIf, cloudOwner, cloudRegionId, tenantId, vserverId, lIf.getInterfaceName());
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to add l-interface to vserver for input " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteLInterfaceFromVserver(LInterface lIf, String cloudOwner,
        String cloudRegionId, String tenantId, String vserverId) throws ActiveAndAvailableInventoryException {
        final String methodName = CLASSNAME + ": deleteLInterfaceFromVserver : ";
        long startTime = System.currentTimeMillis();
        String inputData = String.format("lIf:%s cloudOwner:%s cloudRegionId:%s tenantId:%s vserverId:%s",
            lIf != null ?lIf.getInterfaceName() : STR_NULL, cloudOwner, cloudRegionId, tenantId, vserverId);
        try {
            Preconditions.checkNotNull(lIf,"addLInterfaceToVserver expects non-null lIf parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudOwner), "addLInterfaceToVserver expected non-empty cloudOwner parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(cloudRegionId), "addLInterfaceToVserver expected non-empty cloudRegionId parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(tenantId), "addLInterfaceToVserver expected non-empty tenantId parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(vserverId), "addLInterfaceToVserver expected non-empty vserverId parameter");
            Response response = inventoryClient.deleteLInterfaceFromVserver(lIf, cloudOwner, cloudRegionId, tenantId, vserverId,
                lIf.getInterfaceName());
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to delete l-interface from vserver " + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void addSriovPfToPserverPInterface(SriovPf sriovPf, String pServerName, String pInterfaceName)
        throws ActiveAndAvailableInventoryException {
        long startTime = System.currentTimeMillis();
        final String methodName = CLASSNAME + ": addSriovPfToPserverPInterface : ";
        String inputData = String.format("sriov-pf PCI-ID:%s to p-interface-id:%s on pserver-id:%s",
            sriovPf == null ? STR_NULL : sriovPf.getPfPciId(),
            pInterfaceName, pServerName);
        try {
            Preconditions.checkNotNull(sriovPf, "addSriovPfToPserverPInterface expected non-null sriovPf parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(pServerName),
                "addSriovPfToPserverPInterface expected non-empty pServerName parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(pInterfaceName),
                "addSriovPfToPserverPInterface expected non-empty pInterfaceName parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(sriovPf.getPfPciId()),
                "addSriovPfToPserverPInterface expected sriovPf parameter to have a non-empty getPfPciID");
            Response response = inventoryClient
                .createSriovPfForPserverPInterface(sriovPf, pServerName, pInterfaceName, sriovPf.getPfPciId());
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + " " + inputData);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException("Unable to addSriovPfToPserverPInterface for input data : "
                + inputData + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteSriovPfFromPserverPInterface(String pServerName, String pInterfaceName, String pfPciId)
        throws ActiveAndAvailableInventoryException {
        long startTime = System.currentTimeMillis();
        String inputData = String.format("sriov-pf PCI-ID: %s from p-interface-id: %s on pserver-id: %s",
            pfPciId, pInterfaceName, pServerName);
        final String methodName = CLASSNAME + ": deleteSriovPfFromPserverPInterface : ";
        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(pServerName), "deleteSriovPfFromPserverPInterface expected non-empty pServerName parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(pInterfaceName), "deleteSriovPfFromPserverPInterface expected non-empty pInterfaceName parameter");
            Preconditions.checkState(!Strings.isNullOrEmpty(pfPciId), "deleteSriovPfFromPserverPInterface expected non-empty pfPciId parameter");
            Response response = inventoryClient.deleteSriovPfFromPserverPInterface(pServerName, pInterfaceName, pfPciId);
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + inputData);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, inputData, ex);
            throw new ActiveAndAvailableInventoryException(
                "Unable to delete sriov-pf from: " + pServerName + "/" + pInterfaceName + "/" + pfPciId + STR_EXCEPTION + ex.getMessage(), ex);
        }
    }

    private Response checkResponse(final Response response) throws ActiveAndAvailableInventoryException {
        if (response == null) {
            throw new ActiveAndAvailableInventoryException("Received STR_NULL response from AAI");
        }
        if(!isNonErrorHttpReponseCode (response.status())) {
            response.close();
            throw new ActiveAndAvailableInventoryException(
                "Received an unsuccessful response from AAI: (" + response.status() + ") " + FeignUtils
                    .extractResponseBody(response), response);
        }
        return response;
    }

    private Response checkResponseByAllowing404(final Response response) throws ActiveAndAvailableInventoryException {
        if (response == null) {
            throw new ActiveAndAvailableInventoryException("Received STR_NULL response from AAI");
        } else if(response.status() == HttpStatus.SC_NOT_FOUND) {
            response.close();
            return null;
        }
        else if (isNonErrorHttpReponseCode(response.status())) {
            return response;
        } else {
            response.close();
            throw new ActiveAndAvailableInventoryException(
                "Received an unsuccessful response from AAI: (" + response.status() + ") " + FeignUtils
                    .extractResponseBody(response), response);
        }
    }

    /*
     * Tenants
     */

    @Override
    public void deleteByUri(String uri) throws ActiveAndAvailableInventoryException {
        long startTime = System.currentTimeMillis();
        final String methodName = CLASSNAME + ": deleteByUri : ";
        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(uri), "deleteByUri was expecting a non-empty uri parameter ");
            Response response = inventoryClient.deleteByUri(uri);
            checkResponse(response);
            response.close();
            LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE,
                MsoLogger.ResponseCode.Suc, methodName + " for URI: " + uri);
        } catch (Exception ex) {
            logErrorToDebugAndAudit(startTime, methodName, uri, ex);
            throw new ActiveAndAvailableInventoryException("Unable to delete the object at: " + uri + ". Error"
                + " cause: " + ex.getMessage(), ex);
        }
    }

    private String encode(String inputData) throws UnsupportedEncodingException {
        return UriUtils.encode(inputData, StandardCharsets.UTF_8.name());
    }

    /**
     * Check if the status code is a non-error
     * @param status HTTP status code
     * @return true if the status code is between 200 (OK) and 300 (MULTIPLE_CHOICES)
     */
    private boolean isNonErrorHttpReponseCode(int status){
        return status >= HttpStatus.SC_OK && status <= HttpStatus.SC_MULTIPLE_CHOICES;
    }

    private ObjectMapper objecMapperIncludingNonNulls(){
        return new ObjectMapper().setSerializationInclusion(Include.NON_NULL)
            .setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector());
    }

    /**
     *  Log full stack trace to debug error log and log the error message to audit.
     * @param startTime timestamp when the operation started (taken right at the method start)
     * @param methodName Invoker's method name
     * @param ex Exception received
     */
    private void logErrorToDebugAndAudit(long startTime, String methodName, Exception ex ) {
        String errMsg = methodName + STR_EXCEPTION;
        LOGGER.debug(errMsg + ex);
        LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError,
            errMsg + ex.getMessage());
    }

    /**
     * Log full stack trace to debug error log and log the error message to audit.
     * @param startTime timestamp when the operation started (taken right at the method start)
     * @param methodName Invoker's method name
     * @param inputParams Input parameter string
     * @param ex Exception received
     */
    private void logErrorToDebugAndAudit(long startTime, String methodName, String inputParams, Exception ex ) {
        String errMsg = methodName + inputParams + " " + STR_EXCEPTION;
        LOGGER.debug(errMsg + ex);
        LOGGER.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError,
            errMsg + ex.getMessage());
    }
}
