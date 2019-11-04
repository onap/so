/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.util;


import java.util.List;
import java.util.Map;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;

public class ASDCNotificationLogging {

    public static String dumpASDCNotification(INotificationData asdcNotification) {

        if (asdcNotification == null) {
            return "NULL";
        }
        return "ASDC Notification:" + System.lineSeparator() + "DistributionID:"
                + testNull(asdcNotification.getDistributionID()) + System.lineSeparator() + "ServiceName:"
                + testNull(asdcNotification.getServiceName()) + System.lineSeparator() + "ServiceVersion:"
                + testNull(asdcNotification.getServiceVersion()) + System.lineSeparator() + "ServiceUUID:"
                + testNull(asdcNotification.getServiceUUID()) + System.lineSeparator() + "ServiceInvariantUUID:"
                + testNull(asdcNotification.getServiceInvariantUUID()) + System.lineSeparator() + "ServiceDescription:"
                + testNull(asdcNotification.getServiceDescription()) + System.lineSeparator()
                + "Service Artifacts List:" + System.lineSeparator()
                + testNull(dumpArtifactInfoList(asdcNotification.getServiceArtifacts())) + System.lineSeparator()
                + "Resource Instances List:" + System.lineSeparator()
                + testNull(dumpASDCResourcesList(asdcNotification)) + System.lineSeparator();
    }

    public static String dumpCSARNotification(INotificationData asdcNotification,
            ToscaResourceStructure toscaResourceStructure) {

        if (asdcNotification == null) {
            return "NULL";
        }

        ToscaResourceInstaller toscaResourceInstaller = new ToscaResourceInstaller();
        StringBuilder buffer = new StringBuilder("CSAR Notification:");
        buffer.append(System.lineSeparator());
        buffer.append(System.lineSeparator());


        ISdcCsarHelper csarHelper = toscaResourceStructure.getSdcCsarHelper();


        buffer.append("Service Level Properties:");
        buffer.append(System.lineSeparator());
        buffer.append("Name:");
        buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        buffer.append(System.lineSeparator());
        buffer.append("Description:");
        buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        buffer.append(System.lineSeparator());
        buffer.append("Model UUID:");
        buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        buffer.append(System.lineSeparator());
        buffer.append("Model Version:");
        buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        buffer.append(System.lineSeparator());
        buffer.append("Model InvariantUuid:");
        buffer.append(testNull(csarHelper.getServiceMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        buffer.append(System.lineSeparator());
        buffer.append("Service Type:");
        buffer.append(csarHelper.getServiceMetadata().getValue("serviceType"));
        buffer.append(System.lineSeparator());
        buffer.append("Service Role:");
        buffer.append(csarHelper.getServiceMetadata().getValue("serviceRole"));
        buffer.append(System.lineSeparator());
        buffer.append("WorkLoad Context:");
        buffer.append(asdcNotification.getWorkloadContext());
        buffer.append(System.lineSeparator());
        buffer.append("Environment Context:");
        buffer.append(csarHelper.getServiceMetadata().getValue("environmentContext"));
        buffer.append(System.lineSeparator());


        List<IEntityDetails> serviceProxyResourceList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.SERVICE_PROXY), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE),
                true);


        if (serviceProxyResourceList != null) {

            for (IEntityDetails serviceProxyEntity : serviceProxyResourceList) {

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("Service Proxy Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("Model Name:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                buffer.append(System.lineSeparator());
                buffer.append("Model UUID:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                buffer.append(System.lineSeparator());
                buffer.append("Description:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                buffer.append(System.lineSeparator());
                buffer.append("Version:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                buffer.append(System.lineSeparator());
                buffer.append("InvariantUuid:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                buffer.append(System.lineSeparator());

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("Service Proxy Customization Properties:");
                buffer.append(System.lineSeparator());

                buffer.append("Model Customization UUID:");
                buffer.append(
                        serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                buffer.append(System.lineSeparator());
                buffer.append("Model Instance Name:");
                buffer.append(serviceProxyEntity.getName());
                buffer.append(System.lineSeparator());
                buffer.append("Tosca Node Type:");
                buffer.append(serviceProxyEntity.getToscaType());
                buffer.append(System.lineSeparator());
                buffer.append("Version:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                buffer.append(System.lineSeparator());
                buffer.append("InvariantUuid:");
                buffer.append(serviceProxyEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                buffer.append(System.lineSeparator());

            }
        }

        List<IEntityDetails> configurationList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.CONFIGURATION), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE),
                true);

        if (configurationList != null) {
            for (IEntityDetails configEntity : configurationList) {

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("Configuration Properties:");
                buffer.append(System.lineSeparator());

                buffer.append("Model Name:");
                buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                buffer.append(System.lineSeparator());
                buffer.append("Model UUID:");
                buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                buffer.append(System.lineSeparator());
                buffer.append("Description:");
                buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                buffer.append(System.lineSeparator());
                buffer.append("Version:");
                buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                buffer.append(System.lineSeparator());
                buffer.append("InvariantUuid:");
                buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                buffer.append(System.lineSeparator());
                buffer.append("Tosca Node Type:");
                buffer.append(configEntity.getToscaType());

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("Configuration Customization Properties:");
                buffer.append(System.lineSeparator());

                buffer.append("Model Customization UUID:");
                buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                buffer.append(System.lineSeparator());
                buffer.append("Model Instance Name:");
                buffer.append(configEntity.getName());
                buffer.append(System.lineSeparator());
                buffer.append("NFFunction:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(configEntity,
                        SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
                buffer.append(System.lineSeparator());
                buffer.append("NFRole:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(configEntity,
                        SdcPropertyNames.PROPERTY_NAME_NFROLE));
                buffer.append(System.lineSeparator());
                buffer.append("NFType:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(configEntity,
                        SdcPropertyNames.PROPERTY_NAME_NFTYPE));
                buffer.append(System.lineSeparator());

            }
        }

        List<IEntityDetails> vfEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.VF), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);


        for (IEntityDetails vfEntity : vfEntityList) {

            buffer.append(System.lineSeparator());
            buffer.append(System.lineSeparator());
            buffer.append("VNF Properties:");
            buffer.append(System.lineSeparator());
            buffer.append("Model Name:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
            buffer.append(System.lineSeparator());
            buffer.append("Model UUID:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
            buffer.append(System.lineSeparator());
            buffer.append("Description:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
            buffer.append(System.lineSeparator());
            buffer.append("Version:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
            buffer.append(System.lineSeparator());
            buffer.append("Type:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_TYPE)));
            buffer.append(System.lineSeparator());
            buffer.append("Category:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY)));
            buffer.append(System.lineSeparator());
            buffer.append("InvariantUuid:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
            buffer.append(System.lineSeparator());
            buffer.append("Max Instances:");
            buffer.append(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES));
            buffer.append(System.lineSeparator());
            buffer.append("Min Instances:");
            buffer.append(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES));
            buffer.append(System.lineSeparator());

            buffer.append(System.lineSeparator());
            buffer.append("VNF Customization Properties:");
            buffer.append(System.lineSeparator());

            buffer.append("Customization UUID:");
            buffer.append(testNull(vfEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
            buffer.append(System.lineSeparator());
            buffer.append("NFFunction:");
            buffer.append(
                    toscaResourceInstaller.getLeafPropertyValue(vfEntity, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
            buffer.append(System.lineSeparator());
            buffer.append("NFCode:");
            buffer.append(toscaResourceInstaller.getLeafPropertyValue(vfEntity, "nf_naming_code"));
            buffer.append(System.lineSeparator());
            buffer.append("NFRole:");
            buffer.append(toscaResourceInstaller.getLeafPropertyValue(vfEntity, SdcPropertyNames.PROPERTY_NAME_NFROLE));
            buffer.append(System.lineSeparator());
            buffer.append("NFType:");
            buffer.append(toscaResourceInstaller.getLeafPropertyValue(vfEntity, SdcPropertyNames.PROPERTY_NAME_NFTYPE));
            buffer.append(System.lineSeparator());
            buffer.append("MultiStageDesign:");
            buffer.append(toscaResourceInstaller.getLeafPropertyValue(vfEntity, "multi_stage_design"));
            buffer.append(System.lineSeparator());


            List<IEntityDetails> vfcInstanceEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                    EntityQuery.newBuilder("org.openecomp.groups.VfcInstanceGroup"),
                    TopologyTemplateQuery.newBuilder(SdcTypes.VF)
                            .customizationUUID(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID),
                    false);

            if (vfcInstanceEntityList != null) {
                for (IEntityDetails vfcEntity : vfcInstanceEntityList) {
                    Metadata instanceMetadata = vfcEntity.getMetadata();

                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());
                    buffer.append("VNFC Instance Group Properties:");
                    buffer.append(System.lineSeparator());

                    buffer.append("Model Name:");
                    buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                    buffer.append(System.lineSeparator());
                    buffer.append("Version:");
                    buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                    buffer.append(System.lineSeparator());
                    buffer.append("Type:");
                    buffer.append(vfcEntity.getToscaType());
                    buffer.append(System.lineSeparator());
                    buffer.append("InvariantUuid:");
                    buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                    buffer.append(System.lineSeparator());
                }

            }

            List<IEntityDetails> vfModuleEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                    EntityQuery.newBuilder("org.openecomp.groups.VfModule"),
                    TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE)
                            .customizationUUID(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID),
                    false);


            for (IEntityDetails vfModuleEntity : vfModuleEntityList) {

                Metadata vfMetadata = vfModuleEntity.getMetadata();

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("VF Module Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("ModelInvariantUuid:");
                buffer.append(testNull(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelName:");
                buffer.append(testNull(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelUuid:");
                buffer.append(testNull(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelVersion:");
                buffer.append(testNull(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION)));
                buffer.append(System.lineSeparator());
                buffer.append("Description:");
                buffer.append(testNull(vfMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                buffer.append(System.lineSeparator());

                List<IEntityDetails> groupMembers = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder("org.openecomp.groups.VfModule")
                                .uUID(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID),
                        TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);

                for (IEntityDetails node : groupMembers) {
                    buffer.append("Member Name:");
                    buffer.append(testNull(node.getName()));
                    buffer.append(System.lineSeparator());
                }

                buffer.append(System.lineSeparator());
                buffer.append("VF Module Customization Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("Model Customization UUID:");
                buffer.append(testNull(toscaResourceInstaller.getLeafPropertyValue(vfModuleEntity,
                        SdcPropertyNames.PROPERTY_NAME_VFMODULECUSTOMIZATIONUUID)));
                buffer.append(System.lineSeparator());

            }

            List<IEntityDetails> fabricEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                    EntityQuery.newBuilder(SdcTypes.CONFIGURATION), TopologyTemplateQuery.newBuilder(SdcTypes.VF),
                    false);

            if (fabricEntityList != null) {
                for (IEntityDetails configEntity : fabricEntityList) {

                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());
                    buffer.append("Fabric Configuration Properties:");
                    buffer.append(System.lineSeparator());

                    buffer.append("Model Name:");
                    buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model UUID:");
                    buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                    buffer.append(System.lineSeparator());
                    buffer.append("Description:");
                    buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                    buffer.append(System.lineSeparator());
                    buffer.append("Version:");
                    buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                    buffer.append(System.lineSeparator());
                    buffer.append("InvariantUuid:");
                    buffer.append(configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                    buffer.append(System.lineSeparator());
                    buffer.append("Tosca Node Type:");
                    buffer.append(configEntity.getToscaType());

                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());
                    buffer.append("Fabric Configuration Customization Properties:");
                    buffer.append(System.lineSeparator());

                    buffer.append("Model Customization UUID:");
                    buffer.append(
                            configEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Instance Name:");
                    buffer.append(configEntity.getName());
                    buffer.append(System.lineSeparator());
                    buffer.append("NFFunction:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(configEntity,
                            SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFRole:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(configEntity,
                            SdcPropertyNames.PROPERTY_NAME_NFROLE));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFType:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(configEntity,
                            SdcPropertyNames.PROPERTY_NAME_NFTYPE));
                    buffer.append(System.lineSeparator());

                }
            }

            List<IEntityDetails> cvnfcEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                    EntityQuery.newBuilder(SdcTypes.CVFC), TopologyTemplateQuery.newBuilder(SdcTypes.VF), false);

            for (IEntityDetails cvnfcEntity : cvnfcEntityList) {

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("CVNFC Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("ModelCustomizationUuid:");
                buffer.append(
                        testNull(cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelInvariantUuid:");
                buffer.append(
                        testNull(cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelName:");
                buffer.append(testNull(cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelUuid:");
                buffer.append(testNull(cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
                buffer.append(System.lineSeparator());
                buffer.append("ModelVersion:");
                buffer.append(testNull(cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                buffer.append(System.lineSeparator());
                buffer.append("Description:");
                buffer.append(testNull(cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                buffer.append(System.lineSeparator());
                buffer.append("Template Name:");
                buffer.append(testNull(cvnfcEntity.getName()));
                buffer.append(System.lineSeparator());

                List<IEntityDetails> vfcEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder(SdcTypes.VFC),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CVFC).customizationUUID(
                                cvnfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);

                for (IEntityDetails vfcEntity : vfcEntityList) {
                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());
                    buffer.append("VNFC Properties:");
                    buffer.append(System.lineSeparator());
                    buffer.append("ModelCustomizationUuid:");
                    buffer.append(testNull(
                            vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("ModelInvariantUuid:");
                    buffer.append(
                            testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("ModelName:");
                    buffer.append(testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                    buffer.append(System.lineSeparator());
                    buffer.append("ModelUuid:");
                    buffer.append(testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("ModelVersion:");
                    buffer.append(testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Description:");
                    buffer.append(
                            testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Sub Category:");
                    buffer.append(
                            testNull(vfcEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
                    buffer.append(System.lineSeparator());

                }

            }

        }

        List<IEntityDetails> vlEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.VL), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (vlEntityList != null) {

            for (IEntityDetails vlEntity : vlEntityList) {

                buffer.append(System.lineSeparator());
                buffer.append(System.lineSeparator());
                buffer.append("NETWORK Level Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("Model Name:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                buffer.append(System.lineSeparator());
                buffer.append("Model InvariantUuid:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
                buffer.append(System.lineSeparator());
                buffer.append("Model UUID:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
                buffer.append(System.lineSeparator());
                buffer.append("Model Version:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                buffer.append(System.lineSeparator());
                buffer.append("AIC Max Version:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
                buffer.append(System.lineSeparator());
                buffer.append("AIC Min Version:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                buffer.append(System.lineSeparator());
                buffer.append("Tosca Node Type:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_TYPE)));
                buffer.append(System.lineSeparator());
                buffer.append("Description:");
                buffer.append(testNull(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                buffer.append(System.lineSeparator());


                buffer.append(System.lineSeparator());
                buffer.append("NETWORK Customization Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("CustomizationUUID:");
                buffer.append(vlEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                buffer.append(System.lineSeparator());
                buffer.append("Network Technology:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(vlEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));
                buffer.append(System.lineSeparator());
                buffer.append("Network Type:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(vlEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
                buffer.append(System.lineSeparator());
                buffer.append("Network Role:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(vlEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
                buffer.append(System.lineSeparator());
                buffer.append("Network Scope:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(vlEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
                buffer.append(System.lineSeparator());

            }

        }

        List<IEntityDetails> crEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.CR), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (crEntityList != null) {
            for (IEntityDetails crEntity : crEntityList) {
                buffer.append(System.lineSeparator());
                buffer.append("Network Collection Properties:");
                buffer.append(System.lineSeparator());
                buffer.append("Model Name:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                buffer.append(System.lineSeparator());
                buffer.append("Model UUID:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                buffer.append(System.lineSeparator());
                buffer.append("Customization UUID:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                buffer.append(System.lineSeparator());
                buffer.append("InvariantUuid:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                buffer.append(System.lineSeparator());
                buffer.append("Description:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                buffer.append(System.lineSeparator());
                buffer.append("Version:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                buffer.append(System.lineSeparator());
                buffer.append("Tosca Node Type:");
                buffer.append(crEntity.getToscaType());
                buffer.append(System.lineSeparator());
                buffer.append("CR Function:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity, "cr_function"));
                buffer.append(System.lineSeparator());
                buffer.append("CR Role:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity, "cr_role"));
                buffer.append(System.lineSeparator());
                buffer.append("CR Type:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity, "cr_type"));
                buffer.append(System.lineSeparator());

                List<IEntityDetails> networkEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder(SdcTypes.VL),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CR).customizationUUID(
                                crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);


                for (IEntityDetails vlEntity : networkEntityList) {

                    Metadata vlMetadata = vlEntity.getMetadata();

                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());
                    buffer.append("Network CR VL Properties:");
                    buffer.append(System.lineSeparator());

                    buffer.append("Model Name:");
                    buffer.append(vlMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model UUID:");
                    buffer.append(vlMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                    buffer.append(System.lineSeparator());
                    buffer.append("InvariantUuid:");
                    buffer.append(vlMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                    buffer.append(System.lineSeparator());
                    buffer.append("Version:");
                    buffer.append(vlMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                    buffer.append(System.lineSeparator());
                    buffer.append("AIC Max Version:");
                    buffer.append(vlMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES));
                    buffer.append(System.lineSeparator());
                    buffer.append("Description:");
                    buffer.append(vlMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                    buffer.append(System.lineSeparator());
                    buffer.append("Tosca Node Type:");
                    buffer.append(vlEntity.getToscaType());
                    buffer.append(System.lineSeparator());

                }

                List<IEntityDetails> ncEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                        EntityQuery.newBuilder("org.openecomp.groups.NetworkCollection"),
                        TopologyTemplateQuery.newBuilder(SdcTypes.CR).customizationUUID(
                                crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)),
                        false);

                if (ncEntityList != null) {
                    for (IEntityDetails ncEntity : ncEntityList) {
                        Metadata instanceMetadata = ncEntity.getMetadata();
                        buffer.append(System.lineSeparator());
                        buffer.append(System.lineSeparator());
                        buffer.append("Network Instance Group Properties:");
                        buffer.append(System.lineSeparator());

                        buffer.append("Model Name:");
                        buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        buffer.append(System.lineSeparator());
                        buffer.append("Model UUID:");
                        buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                        buffer.append(System.lineSeparator());
                        buffer.append("InvariantUuid:");
                        buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        buffer.append(System.lineSeparator());
                        buffer.append("Version:");
                        buffer.append(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
                        buffer.append(System.lineSeparator());
                    }

                }

                buffer.append(System.lineSeparator());
                buffer.append("Network Collection Customization Properties:");
                buffer.append(System.lineSeparator());

                buffer.append("Model Customization UUID:");
                buffer.append(crEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                buffer.append(System.lineSeparator());
                buffer.append("Model Instance Name:");
                buffer.append(crEntity.getName());
                buffer.append(System.lineSeparator());
                buffer.append("Network Scope:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
                buffer.append(System.lineSeparator());
                buffer.append("Network Role:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
                buffer.append(System.lineSeparator());
                buffer.append("Network Type:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
                buffer.append(System.lineSeparator());
                buffer.append("Network Technology:");
                buffer.append(toscaResourceInstaller.getLeafPropertyValue(crEntity,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));
                buffer.append(System.lineSeparator());

            }
        }

        List<IEntityDetails> arEntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.VF), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (arEntityList != null) {

            buffer.append(System.lineSeparator());
            buffer.append("VF Allotted Resource Properties:");
            buffer.append(System.lineSeparator());

            for (IEntityDetails arEntity : arEntityList) {

                Metadata metadata = arEntity.getMetadata();
                String category = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);

                if ("Allotted Resource".equalsIgnoreCase(category)) {

                    buffer.append(System.lineSeparator());
                    buffer.append("Allotted Resource Properties:");
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Name:");
                    buffer.append(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Name:");
                    buffer.append(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model InvariantUuid:");
                    buffer.append(
                            testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Version:");
                    buffer.append(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model UUID:");
                    buffer.append(testNull(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Subcategory:");
                    buffer.append(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Category:");
                    buffer.append(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Description:");
                    buffer.append(arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());


                    buffer.append("Allotted Resource Customization Properties:");
                    buffer.append(System.lineSeparator());

                    buffer.append("Model Cutomization UUID:");
                    buffer.append(testNull(
                            arEntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFFunction:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(arEntity,
                            SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFCode:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(arEntity, "nf_naming_code"));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFRole:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(arEntity,
                            SdcPropertyNames.PROPERTY_NAME_NFROLE));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFType:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(arEntity,
                            SdcPropertyNames.PROPERTY_NAME_NFTYPE));
                    buffer.append(System.lineSeparator());
                }

            }
        }

        List<IEntityDetails> pnfAREntityList = toscaResourceInstaller.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(SdcTypes.PNF), TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false);

        if (pnfAREntityList != null) {

            buffer.append(System.lineSeparator());
            buffer.append("PNF Allotted Resource Properties:");
            buffer.append(System.lineSeparator());

            for (IEntityDetails pnfAREntity : pnfAREntityList) {

                Metadata metadata = pnfAREntity.getMetadata();
                String category = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);

                if ("Allotted Resource".equalsIgnoreCase(category)) {

                    buffer.append(System.lineSeparator());
                    buffer.append("Allotted Resource Properties:");
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Name:");
                    buffer.append(testNull(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Name:");
                    buffer.append(testNull(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model InvariantUuid:");
                    buffer.append(
                            testNull(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Version:");
                    buffer.append(testNull(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model UUID:");
                    buffer.append(testNull(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Subcategory:");
                    buffer.append(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Category:");
                    buffer.append(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
                    buffer.append(System.lineSeparator());
                    buffer.append("Model Description:");
                    buffer.append(pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                    buffer.append(System.lineSeparator());
                    buffer.append(System.lineSeparator());

                    buffer.append("Allotted Resource Customization Properties:");
                    buffer.append(System.lineSeparator());

                    buffer.append("Model Cutomization UUID:");
                    buffer.append(testNull(
                            pnfAREntity.getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFFunction:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(pnfAREntity,
                            SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFCode:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(pnfAREntity, "nf_naming_code"));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFRole:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(pnfAREntity,
                            SdcPropertyNames.PROPERTY_NAME_NFROLE));
                    buffer.append(System.lineSeparator());
                    buffer.append("NFType:");
                    buffer.append(toscaResourceInstaller.getLeafPropertyValue(pnfAREntity,
                            SdcPropertyNames.PROPERTY_NAME_NFTYPE));
                    buffer.append(System.lineSeparator());
                }

            }
        }



        return buffer.toString();
    }

    public static String dumpVfModuleMetaDataList(List<IVfModuleData> moduleMetaDataList) {
        if (moduleMetaDataList == null) {
            return null;
        }

        StringBuilder buffer = new StringBuilder("VfModuleMetaData List:");
        buffer.append(System.lineSeparator());

        buffer.append("{");

        for (IVfModuleData moduleMetaData : moduleMetaDataList) {
            buffer.append(System.lineSeparator());
            buffer.append(testNull(dumpVfModuleMetaData(moduleMetaData)));
            buffer.append(System.lineSeparator());
            buffer.append(",");

        }
        buffer.replace(buffer.length() - 1, buffer.length(), System.lineSeparator());
        buffer.append("}");
        buffer.append(System.lineSeparator());

        return buffer.toString();
    }

    private static String dumpVfModuleMetaData(IVfModuleData moduleMetaData) {

        if (moduleMetaData == null) {
            return "NULL";
        }

        StringBuilder stringBuilder = new StringBuilder("VfModuleMetaData:");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("VfModuleModelName:");
        stringBuilder.append(testNull(moduleMetaData.getVfModuleModelName()));
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("VfModuleModelVersion:");
        stringBuilder.append(testNull(moduleMetaData.getVfModuleModelVersion()));
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("VfModuleModelUUID:");
        stringBuilder.append(testNull(moduleMetaData.getVfModuleModelUUID()));
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("VfModuleModelInvariantUUID:");
        stringBuilder.append(testNull(moduleMetaData.getVfModuleModelInvariantUUID()));
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("VfModuleModelDescription:");
        stringBuilder.append(testNull(moduleMetaData.getVfModuleModelDescription()));
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("Artifacts UUID List:");

        if (moduleMetaData.getArtifacts() != null) {
            stringBuilder.append("{");

            for (String artifactUUID : moduleMetaData.getArtifacts()) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(testNull(artifactUUID));
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(",");
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), System.lineSeparator());
            stringBuilder.append("}");
            stringBuilder.append(System.lineSeparator());
        } else {
            stringBuilder.append("NULL");
        }

        if (moduleMetaData.getProperties() != null) {
            Map<String, String> vfModuleMap = moduleMetaData.getProperties();
            stringBuilder.append("Properties List:");
            stringBuilder.append("{");

            for (Map.Entry<String, String> entry : vfModuleMap.entrySet()) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append("  ").append(entry.getKey()).append(" : ").append(entry.getValue());
            }
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), System.lineSeparator());
            stringBuilder.append("}");
            stringBuilder.append(System.lineSeparator());
        } else {
            stringBuilder.append("NULL");
        }


        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("isBase:");
        stringBuilder.append(moduleMetaData.isBase());
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }

    private static String testNull(Object object) {
        if (object == null) {
            return "NULL";
        } else if (object instanceof Integer) {
            return object.toString();
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return "Type not recognized";
        }
    }

    private static String dumpASDCResourcesList(INotificationData asdcNotification) {
        if (asdcNotification == null || asdcNotification.getResources() == null) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("{");

        for (IResourceInstance resourceInstanceElem : asdcNotification.getResources()) {
            buffer.append(System.lineSeparator());
            buffer.append(testNull(dumpASDCResourceInstance(resourceInstanceElem)));
            buffer.append(System.lineSeparator());
            buffer.append(",");
        }
        buffer.replace(buffer.length() - 1, buffer.length(), System.lineSeparator());
        buffer.append("}");
        buffer.append(System.lineSeparator());

        return buffer.toString();

    }

    private static String dumpASDCResourceInstance(IResourceInstance resourceInstance) {

        if (resourceInstance == null) {
            return null;
        }

        return "Resource Instance Info:" + System.lineSeparator() + "ResourceInstanceName:"
                + testNull(resourceInstance.getResourceInstanceName()) + System.lineSeparator()
                + "ResourceCustomizationUUID:" + testNull(resourceInstance.getResourceCustomizationUUID())
                + System.lineSeparator() + "ResourceInvariantUUID:"
                + testNull(resourceInstance.getResourceInvariantUUID()) + System.lineSeparator() + "ResourceName:"
                + testNull(resourceInstance.getResourceName()) + System.lineSeparator() + "ResourceType:"
                + testNull(resourceInstance.getResourceType()) + System.lineSeparator() + "ResourceUUID:"
                + testNull(resourceInstance.getResourceUUID()) + System.lineSeparator() + "ResourceVersion:"
                + testNull(resourceInstance.getResourceVersion()) + System.lineSeparator() + "Category:"
                + testNull(resourceInstance.getCategory()) + System.lineSeparator() + "SubCategory:"
                + testNull(resourceInstance.getSubcategory()) + System.lineSeparator() + "Resource Artifacts List:"
                + System.lineSeparator() + testNull(dumpArtifactInfoList(resourceInstance.getArtifacts()))
                + System.lineSeparator();
    }


    private static String dumpArtifactInfoList(List<IArtifactInfo> artifactsList) {

        if (artifactsList == null || artifactsList.isEmpty()) {
            return null;
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        for (IArtifactInfo artifactInfoElem : artifactsList) {
            buffer.append(System.lineSeparator());
            buffer.append(testNull(dumpASDCArtifactInfo(artifactInfoElem)));
            buffer.append(System.lineSeparator());
            buffer.append(",");

        }
        buffer.replace(buffer.length() - 1, buffer.length(), System.lineSeparator());
        buffer.append("}");
        buffer.append(System.lineSeparator());

        return buffer.toString();
    }

    private static String dumpASDCArtifactInfo(IArtifactInfo artifactInfo) {

        if (artifactInfo == null) {
            return null;
        }

        StringBuilder buffer = new StringBuilder("Service Artifacts Info:");
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactName:");
        buffer.append(testNull(artifactInfo.getArtifactName()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactVersion:");
        buffer.append(testNull(artifactInfo.getArtifactVersion()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactType:");
        buffer.append(testNull(artifactInfo.getArtifactType()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactDescription:");
        buffer.append(testNull(artifactInfo.getArtifactDescription()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactTimeout:");
        buffer.append(testNull(artifactInfo.getArtifactTimeout()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactURL:");
        buffer.append(testNull(artifactInfo.getArtifactURL()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactUUID:");
        buffer.append(testNull(artifactInfo.getArtifactUUID()));
        buffer.append(System.lineSeparator());

        buffer.append("ArtifactChecksum:");
        buffer.append(testNull(artifactInfo.getArtifactChecksum()));
        buffer.append(System.lineSeparator());

        buffer.append("GeneratedArtifact:");
        buffer.append("{");
        buffer.append(testNull(dumpASDCArtifactInfo(artifactInfo.getGeneratedArtifact())));
        buffer.append(System.lineSeparator());
        buffer.append("}");
        buffer.append(System.lineSeparator());

        buffer.append("RelatedArtifacts:");


        if (artifactInfo.getRelatedArtifacts() != null) {
            buffer.append("{");
            buffer.append(System.lineSeparator());
            for (IArtifactInfo artifactInfoElem : artifactInfo.getRelatedArtifacts()) {

                buffer.append(testNull(dumpASDCArtifactInfo(artifactInfoElem)));
                buffer.append(System.lineSeparator());
                buffer.append(",");

            }
            buffer.replace(buffer.length() - 1, buffer.length(), System.lineSeparator());
            buffer.append("}");
            buffer.append(System.lineSeparator());
        } else {
            buffer.append("NULL");
        }

        buffer.append(System.lineSeparator());

        return buffer.toString();
    }
}
