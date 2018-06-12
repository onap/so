
create table ACTIVATE_OPERATIONAL_ENV_SERVICE_MODEL_DISTRIBUTION_STATUS (
        REQUEST_ID varchar(255) not null,
        OPERATIONAL_ENV_ID varchar(255) not null,
		SERVICE_MODEL_VERSION_ID varchar(255) not null,
		SERVICE_MOD_VER_FINAL_DISTR_STATUS varchar(255),
		RECOVERY_ACTION varchar(255),
		RETRY_COUNT_LEFT varchar(255),
		WORKLOAD_CONTEXT varchar(255),
		CREATE_TIME datetime,
		MODIFY_TIME datetime,
        primary key (REQUEST_ID,OPERATIONAL_ENV_ID, SERVICE_MODEL_VERSION_ID)
    );
    
create table OPERATION_STATUS (
        SERVICE_ID varchar(255) not null,
        OPERATION_ID varchar(255) not null,
        SERVICE_NAME varchar(255),
		OPERATION_TYPE varchar(255),
		USER_ID varchar(255),
		RESULT varchar(255),
		OPERATION_CONTENT varchar(255),
		PROGRESS varchar(255),
		REASON varchar(255),
        OPERATE_AT datetime,
		FINISHED_AT datetime,
        primary key (SERVICE_ID,OPERATION_ID)
    );
    
    
INSERT INTO PUBLIC.OPERATION_STATUS(SERVICE_ID, OPERATION_ID, OPERATION_TYPE, USER_ID, RESULT, OPERATION_CONTENT, PROGRESS, REASON, OPERATE_AT, FINISHED_AT) VALUES
('serviceId', 'operationId', 'operationType', 'userId', 'result', 'operationContent', 'progress', 'reason', '2016-11-24 13:19:10', '2016-11-24 13:19:10'); 

	create table RESOURCE_OPERATION_STATUS (
        SERVICE_ID varchar(255) not null,
        OPERATION_ID varchar(255) not null,
        RESOURCE_TEMPLATE_UUID varchar(255) not null,
		OPER_TYPE varchar(255),
		RESOURCE_INSTANCE_ID varchar(255),
		JOB_ID varchar(255),
		STATUS varchar(255),
		PROGRESS varchar(255),
		ERROR_CODE varchar(255) ,
		STATUS_DESCRIPOTION varchar(255) ,
        primary key (SERVICE_ID,OPERATION_ID,RESOURCE_TEMPLATE_UUID)
    );
               
CREATE CACHED TABLE PUBLIC.ACTIVE_REQUESTS(
    REQUEST_ID VARCHAR NOT NULL,
    CLIENT_REQUEST_ID VARCHAR,
    SERVICE_INSTANCE_ID VARCHAR,
    SUBSCRIBER_NAME VARCHAR,
    REQUEST_URI VARCHAR,
    SERVICE_TYPE VARCHAR,
    REQUEST_ACTION VARCHAR,
    NOTIFICATION_URL VARCHAR,
    REQUEST_ID_IN_PROGRESS VARCHAR,
    START_TIME TIMESTAMP,
    MODIFY_TIME TIMESTAMP,
    COMPLETION_TIME TIMESTAMP,
    RESPONSE_CODE VARCHAR,
    RESPONSE_BODY VARCHAR,
    STATUS VARCHAR,
    SERVICE_REQUEST_TIMEOUT VARCHAR,
    FINAL_ERROR_CODE VARCHAR,
    FINAL_ERROR_MESSAGE VARCHAR,
    ORDER_NUMBER VARCHAR,
    SOURCE VARCHAR,
    RESPONSE_STATUS VARCHAR,
    ORDER_VERSION VARCHAR,
    LAST_MODIFIED_BY VARCHAR,
    MOCARS_TICKET_NUM VARCHAR,
    REQUEST_BODY VARCHAR,
    REQUEST_SUB_ACTION VARCHAR,
    SDNC_CALLBACK_BPEL_URL VARCHAR,
    FEATURE_TYPE VARCHAR,
    FEATURE_INSTANCE_ID VARCHAR,
    REQUEST_TYPE VARCHAR,
    INTERIM_COMPLETION_TIME VARCHAR,
    INTERIM_STAGE_COMPLETION VARCHAR,
    SERVICE_NAME_VERSION_ID VARCHAR,
    GLOBAL_SUBSCRIBER_ID VARCHAR,
    SERVICE_ID VARCHAR,
    SERVICE_VERSION VARCHAR,
    CORRELATOR VARCHAR,
    VPN_ID VARCHAR,
    PROGRESS VARCHAR,
    STATUS_MESSAGE longtext,
    REQUESTED_SERVICE_NAME VARCHAR,
    PRODUCT_FLAVOR VARCHAR,
    SERVICE_INSTANCE_NAME VARCHAR

);           



INSERT INTO PUBLIC.ACTIVE_REQUESTS(REQUEST_ID, CLIENT_REQUEST_ID, SERVICE_INSTANCE_ID, SUBSCRIBER_NAME, REQUEST_URI, SERVICE_TYPE, REQUEST_ACTION, NOTIFICATION_URL, REQUEST_ID_IN_PROGRESS, START_TIME, MODIFY_TIME, COMPLETION_TIME, RESPONSE_CODE, RESPONSE_BODY, STATUS, SERVICE_REQUEST_TIMEOUT, FINAL_ERROR_CODE, FINAL_ERROR_MESSAGE, ORDER_NUMBER, SOURCE, RESPONSE_STATUS, ORDER_VERSION, LAST_MODIFIED_BY, MOCARS_TICKET_NUM, REQUEST_BODY, REQUEST_SUB_ACTION, SDNC_CALLBACK_BPEL_URL, FEATURE_TYPE, FEATURE_INSTANCE_ID, REQUEST_TYPE, INTERIM_COMPLETION_TIME, INTERIM_STAGE_COMPLETION, SERVICE_NAME_VERSION_ID, GLOBAL_SUBSCRIBER_ID, SERVICE_ID, SERVICE_VERSION, CORRELATOR) VALUES
('00877ab3-2107-4795-9ec5-c9c79d9cc6bc', '00877ab3-2107-4795-9ec5-c9c79d9cc6bc', 'USCEGFEUPKB850AAIIR43', 'ACME_TEST9', 'http://mtanjv9moah01.aic.cip.att.com:8080/ecomp/mso/v3/services', 'uCPE-VMS', 'Layer3ServiceActivateRequest', 'http://ci-ccd-be01.web.att.com:9191/00877ab3-2107-4795-9ec5-c9c79d9cc6bc', null, '2017-09-25 17:11:54', null, '2017-09-25 17:11:55', '501', STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<tns:service-response xmlns:msoservtypes=\"http://ecomp.att.com/mso/request/types/v1\" xmlns:tns=\"http://ecomp.att.com/mso/request/schema/v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n    <msoservtypes:request-id>00877ab3-2107-4795-9ec5-c9c79d9cc6bc</msoservtypes:request-id>\n    <msoservtypes:request-action>Layer3ServiceActivateRequest</msoservtypes:request-action>\n    <msoservtypes:source>CCD</msoservtypes:source>\n    <msoservtypes:error-code>2000</msoservtypes:error-code>\n    <msoservtypes:error-message>&lt;html&gt;&lt;head&gt;&lt;title&gt;Error&lt;/title&gt;&lt;/head&gt;&lt;body&gt;404 - Not Found&lt;/body&gt;&lt;/html&gt;</msoservtypes:error-message>\n    <msoservtypes:ack-final-indicator>Y</msoservtypes:ack-final-indicator>\n</tns:service-response>\n'), 'FAILED', '2017-09-25 20:11:54', '2000', 'ERROR MESSAGE', null, 'CCD', 'SUCCESS', null, 'APIH', null, STRINGDECODE('<service-request xmlns=\"http://ecomp.att.com/mso/request/layer3serviceactivate/schema/v1\"\n                 xmlns:msolayer3=\"http://ecomp.att.com/mso/request/layer3/schema/v1\"\n                 xmlns:msoservtypes=\"http://ecomp.att.com/mso/request/types/v1\"\n                 xmlns:ucpetypes=\"com:att:sdnctl:l3ucpe\">  \n    <msoservtypes:request-information>\n    <msoservtypes:request-id>00877ab3-2107-4795-9ec5-c9c79d9cc6bc</msoservtypes:request-id>\n    <msoservtypes:request-action>Layer3ServiceActivateRequest</msoservtypes:request-action>\n    \n    <msoservtypes:source>CCD</msoservtypes:source>\n    <msoservtypes:notification-url>http://ci-ccd-be01.web.att.com:9191/00877ab3-2107-4795-9ec5-c9c79d9cc6bc</msoservtypes:notification-url>\n  </msoservtypes:request-information>\n\n    <msoservtypes:service-information>\n    <msoservtypes:service-type>uCPE-VMS</msoservtypes:service-type>\n    <msoservtypes:service-instance-id>USCEGFEUPKB850AAIIR43</msoservtypes:service-instance-id>\n    <msoservtypes:subscriber-name>ACME_TEST9</msoservtypes:subscriber-name>\n    <msoservtypes:subscriber-global-id>TEST9</msoservtypes:subscriber-global-id>\n  </msoservtypes:service-information>\n  <service-parameters>\n    \r\n \r\n<msolayer3:ucpe-vms-service-information>\r\n    \r\n    <ucpetypes:internet-topology>LAN</ucpetypes:internet-topology>\r\n    \r\n    \r\n        <ucpetypes:wan-list>\r\n        \r\n              <ucpetypes:wan-information>\r\n              <ucpetypes:wan-port-number>WAN1</ucpetypes:wan-port-number>\r\n              <ucpetypes:wan-type>AVPN</ucpetypes:wan-type>\r\n              \r\n                  <ucpetypes:circuit-id>IZEC/515518//ATI</ucpetypes:circuit-id>\r\n                \r\n              \r\n                  <ucpetypes:dual-mode>Active</ucpetypes:dual-mode>\r\n              \r\n              \r\n              \r\n              \r\n                  <ucpetypes:media-type>E3</ucpetypes:media-type>\r\n              \r\n              \r\n              \r\n              </ucpetypes:wan-information>\r\n        \r\n              <ucpetypes:wan-information>\r\n              <ucpetypes:wan-port-number>WAN2</ucpetypes:wan-port-number>\r\n              <ucpetypes:wan-type>AVPN</ucpetypes:wan-type>\r\n              \r\n                  <ucpetypes:circuit-id>IZEC/915523//ATI</ucpetypes:circuit-id>\r\n                \r\n              \r\n                  <ucpetypes:dual-mode>Active</ucpetypes:dual-mode>\r\n              \r\n              \r\n              \r\n              \r\n                  <ucpetypes:media-type>E3</ucpetypes:media-type>\r\n              \r\n              \r\n              \r\n              </ucpetypes:wan-information>\r\n        \r\n        </ucpetypes:wan-list>\r\n    \r\n    <ucpetypes:ucpe-information>\r\n        <ucpetypes:ucpe-host-name>USCEGFEUPKB850AAIIR43</ucpetypes:ucpe-host-name>\r\n        \r\n        <ucpetypes:ucpe-activation-code>Q20-BZJ-QEP</ucpetypes:ucpe-activation-code>\r\n        \r\n        \r\n    </ucpetypes:ucpe-information>\r\n    <ucpetypes:vnf-list>\r\n    \r\n        <ucpetypes:vnf-information>\r\n        <ucpetypes:vnf-instance-id>USCEGFEUPKB850AAIRT01</ucpetypes:vnf-instance-id>\r\n        \r\n        <ucpetypes:vnf-type>RT</ucpetypes:vnf-type>\r\n        \r\n        <ucpetypes:att-part-number>L-CSR-10M-APP</ucpetypes:att-part-number>\r\n        \r\n        \r\n            <ucpetypes:version-number>3.16.1aS</ucpetypes:version-number>\r\n        \r\n        \r\n            <ucpetypes:management-option>ATT</ucpetypes:management-option>\r\n        \r\n        \r\n        \r\n            <ucpetypes:neighbor-port-list>\r\n                \r\n                      <ucpetypes:neighbor-port>\r\n                        <ucpetypes:neighbor-port-id>WAN1</ucpetypes:neighbor-port-id>\r\n                    </ucpetypes:neighbor-port>\r\n            \r\n                      <ucpetypes:neighbor-port>\r\n                        <ucpetypes:neighbor-port-id>WAN2</ucpetypes:neighbor-port-id>\r\n                    </ucpetypes:neighbor-port>\r\n            \r\n                      <ucpetypes:neighbor-port>\r\n                        <ucpetypes:neighbor-port-id>LAN1</ucpetypes:neighbor-port-id>\r\n                    </ucpetypes:neighbor-port>\r\n            \r\n            </ucpetypes:neighbor-port-list>\r\n                \r\n        </ucpetypes:vnf-information>\r\n    \r\n    </ucpetypes:vnf-list>\r\n</msolayer3:ucpe-vms-service-information>\r\n\r\n\n  </service-parameters>\n</service-request>\n'), null, null, null, null, 'service-request', null, '0', null, null, null, null, null);             
INSERT INTO PUBLIC.ACTIVE_REQUESTS(REQUEST_ID, CLIENT_REQUEST_ID, SERVICE_INSTANCE_ID, SUBSCRIBER_NAME, REQUEST_URI, SERVICE_TYPE, REQUEST_ACTION, NOTIFICATION_URL, REQUEST_ID_IN_PROGRESS, START_TIME, MODIFY_TIME, COMPLETION_TIME, RESPONSE_CODE, RESPONSE_BODY, STATUS, SERVICE_REQUEST_TIMEOUT, FINAL_ERROR_CODE, FINAL_ERROR_MESSAGE, ORDER_NUMBER, SOURCE, RESPONSE_STATUS, ORDER_VERSION, LAST_MODIFIED_BY, MOCARS_TICKET_NUM, REQUEST_BODY, REQUEST_SUB_ACTION, SDNC_CALLBACK_BPEL_URL, FEATURE_TYPE, FEATURE_INSTANCE_ID, REQUEST_TYPE, INTERIM_COMPLETION_TIME, INTERIM_STAGE_COMPLETION, SERVICE_NAME_VERSION_ID, GLOBAL_SUBSCRIBER_ID, SERVICE_ID, SERVICE_VERSION, CORRELATOR) VALUES
('04d71cff-0f73-4bbb-a19d-ad6c874be615', '10258376', 'automate-itcs-0113-test-910ac10258375', 'NB539TTEST', 'http://mtanjv9moah01-eth0.aic.cip.att.com:8080/ecomp/mso/v1/services/features', 'SDN-ETHERNET-INTERNET', 'ChangeFeatureActivateRequest', null, null, '2016-11-24 13:19:40', null, '2016-11-24 13:19:40', '501', STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<feature-response xmlns=\"http://ecomp.att.com/mso/request/schema/v1\" xmlns:msoservtypes=\"http://ecomp.att.com/mso/request/types/v1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n    <msoservtypes:request-id>10258376</msoservtypes:request-id>\n    <msoservtypes:request-action>ChangeFeatureActivateRequest</msoservtypes:request-action>\n    <msoservtypes:source>FGUI</msoservtypes:source>\n    <msoservtypes:error-code>2000</msoservtypes:error-code>\n    <msoservtypes:error-message>&lt;html&gt;&lt;head&gt;&lt;title&gt;JBoss Web/7.3.2.Final-redhat-1 - JBWEB000064: Error report&lt;/title&gt;&lt;style&gt;&lt;!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} H3 {font-family</msoservtypes:error-message>\n    <msoservtypes:ack-final-indicator>Y</msoservtypes:ack-final-indicator>\n</feature-response>\n'), 'FAILED', '2016-11-24 13:19:40', '2000', '<html><head><title>JBoss Web/7.3.2.Final-redhat-1 - JBWEB000064: Error report</title><style><!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} H3 {font-family', null, 'FGUI', 'SUCCESS', null, 'APIH', null, STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<feature-request xmlns=\"http://ecomp.att.com/mso/request/schema/v1\" \r\nxmlns:msoservtypes=\"http://ecomp.att.com/mso/request/types/v1\" \r\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\nxsi:schemaLocation=\"http://ecomp.att.com/mso/request/schema/v1\r\nMsoRequestV1.xsd\">\r\n\t<msoservtypes:request-information>\r\n\t\t<msoservtypes:request-id>10258376</msoservtypes:request-id>\r\n\t\t<msoservtypes:request-action>ChangeFeatureActivateRequest</msoservtypes:request-action>\r\n\t\t<!--msoservtypes:request-sub-action>CANCEL</msoservtypes:request-sub-action-->\r\n\t\t<msoservtypes:source>FGUI</msoservtypes:source>\r\n\t\t<!--msoservtypes:notification-url>http://localhost:9096/CSINotify</msoservtypes:notification-url-->\r\n\t\t<!--msoservtypes:order-number>String</msoservtypes:order-number>\r\n\t\t<msoservtypes:order-version>String</msoservtypes:order-version-->\r\n\t</msoservtypes:request-information>\r\n\t<msoservtypes:service-information>\r\n\t\t<msoservtypes:service-type>SDN-ETHERNET-INTERNET</msoservtypes:service-type>\r\n\t<!--service-instance-id = For Gamma internet svc,  it will be of format $layer2 EVCName_INTERNET\u00e2\u20ac\ufffd-->\r\n\t\t<msoservtypes:service-instance-id>automate-itcs-0113-test-910ac10258375</msoservtypes:service-instance-id>\r\n\t\t<msoservtypes:subscriber-name>NB539TTEST</msoservtypes:subscriber-name>\r\n\t</msoservtypes:service-information>\r\n\t<msoservtypes:feature-information>\r\n\t\t<msoservtypes:feature-type>FIREWALL-LITE</msoservtypes:feature-type>\r\n\t\t<msoservtypes:feature-instance-id>automate-itcs-0113-test-910ac10258375</msoservtypes:feature-instance-id>\r\n\t\t<msoservtypes:feature-yang-model>String</msoservtypes:feature-yang-model>\r\n\t\t<msoservtypes:feature-yang-model-version>String</msoservtypes:feature-yang-model-version>\r\n\t</msoservtypes:feature-information>\r\n\t<!--Need to add/confirm feature-parameters per aid-->\r\n\t<feature-parameters>\r\n\t<msoservtypes:firewall-name>String</msoservtypes:firewall-name>\r\n\t<msoservtypes:stateful-firewall-enabled>N</msoservtypes:stateful-firewall-enabled>\r\n\t</feature-parameters>\r\n</feature-request>'), null, null, 'FIREWALL-LITE', 'automate-itcs-0113-test-910ac10258375', 'feature-request', null, '0', null, null, null, null, null);              
INSERT INTO PUBLIC.ACTIVE_REQUESTS(REQUEST_ID, CLIENT_REQUEST_ID, SERVICE_INSTANCE_ID, SUBSCRIBER_NAME, REQUEST_URI, SERVICE_TYPE, REQUEST_ACTION, NOTIFICATION_URL, REQUEST_ID_IN_PROGRESS, START_TIME, MODIFY_TIME, COMPLETION_TIME, RESPONSE_CODE, RESPONSE_BODY, STATUS, SERVICE_REQUEST_TIMEOUT, FINAL_ERROR_CODE, FINAL_ERROR_MESSAGE, ORDER_NUMBER, SOURCE, RESPONSE_STATUS, ORDER_VERSION, LAST_MODIFIED_BY, MOCARS_TICKET_NUM, REQUEST_BODY, REQUEST_SUB_ACTION, SDNC_CALLBACK_BPEL_URL, FEATURE_TYPE, FEATURE_INSTANCE_ID, REQUEST_TYPE, INTERIM_COMPLETION_TIME, INTERIM_STAGE_COMPLETION, SERVICE_NAME_VERSION_ID, GLOBAL_SUBSCRIBER_ID, SERVICE_ID, SERVICE_VERSION, CORRELATOR) VALUES
('04f2c0c4-fbf8-44d1-967a-c9d9ab67a564', '2427766', 'automate-itcs-0103-test-910ac2427765', 'NB539TTEST', 'http://mtanjv9moah01-eth0.aic.cip.att.com:8080/ecomp/mso/v1/services/features/', 'SDN-ETHERNET-INTERNET', 'ChangeFeatureActivateRequest', 'https://csi-tst-q22.it.att.com:22443/Services/com/cingular/csi/sdn/SendManagedNetworkStatusNotification.jws', null, '2016-11-24 13:19:10', null, '2016-11-24 13:19:10', '501', null, 'FAILED', '2016-11-24 13:19:10', '2000', 'ERROR MESSAGE', null, 'FGUI', 'SUCCESS', null, 'APIH', null, STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<feature-request xmlns=\"http://ecomp.att.com/mso/request/schema/v1\"\n xmlns:msoservtypes=\"http://ecomp.att.com/mso/request/types/v1\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://ecomp.att.com/mso/request/schema/v1 MsoRequestV1.xsd\">\n <msoservtypes:request-information>\n  <msoservtypes:request-id>2427766</msoservtypes:request-id>\n  <msoservtypes:request-action>ChangeFeatureActivateRequest</msoservtypes:request-action>\n  <!--msoservtypes:request-sub-action>CANCEL</msoservtypes:request-sub-action-->\n  <msoservtypes:source>FGUI</msoservtypes:source>\n  <msoservtypes:notification-url>https://csi-tst-q22.it.att.com:22443/Services/com/cingular/csi/sdn/SendManagedNetworkStatusNotification.jws</msoservtypes:notification-url>\n  <!--msoservtypes:order-number>String</msoservtypes:order-number>\n\t\t<msoservtypes:order-version>String</msoservtypes:order-version-->\n </msoservtypes:request-information>\n <msoservtypes:service-information>\n  <msoservtypes:service-type>SDN-ETHERNET-INTERNET</msoservtypes:service-type>\n  <!--service-instance-id = For Gamma internet svc,  it will be of format $layer2 EVCName_INTERNET\u00e2\u20ac\ufffd-->\n  <msoservtypes:service-instance-id>automate-itcs-0103-test-910ac2427765</msoservtypes:service-instance-id>\n  <msoservtypes:subscriber-name>NB539TTEST</msoservtypes:subscriber-name>\n </msoservtypes:service-information>\n <msoservtypes:feature-information>\n  <msoservtypes:feature-type>FIREWALL-LITE</msoservtypes:feature-type>\n  <msoservtypes:feature-instance-id>String1</msoservtypes:feature-instance-id>\n  <msoservtypes:feature-yang-model>String1</msoservtypes:feature-yang-model>\n  <msoservtypes:feature-yang-model-version>1</msoservtypes:feature-yang-model-version>\n </msoservtypes:feature-information>\n <!--Need to add/confirm feature-parameters per aid--><feature-parameters>\n  <msoservtypes:firewall-name>StringCheese</msoservtypes:firewall-name>\n  <msoservtypes:stateful-firewall-enabled>N</msoservtypes:stateful-firewall-enabled>\n </feature-parameters>\n</feature-request>\n'), null, null, 'FIREWALL-LITE', 'feature-requestid', 'feature-request', null, '0', null, null, null, null, null); 

CREATE CACHED TABLE PUBLIC.INFRA_ACTIVE_REQUESTS(
    REQUEST_ID VARCHAR NOT NULL SELECTIVITY 100,
    CLIENT_REQUEST_ID VARCHAR SELECTIVITY 6,
    ACTION VARCHAR SELECTIVITY 1,
    REQUEST_STATUS VARCHAR SELECTIVITY 1,
    STATUS_MESSAGE longtext SELECTIVITY 36,
    PROGRESS VARCHAR SELECTIVITY 1,
    START_TIME VARCHAR,
    END_TIME VARCHAR,
    SOURCE VARCHAR SELECTIVITY 2,
    VNF_ID VARCHAR SELECTIVITY 15,
    VNF_NAME VARCHAR SELECTIVITY 11,
    VNF_TYPE VARCHAR SELECTIVITY 5,
    SERVICE_TYPE VARCHAR SELECTIVITY 1,
    AIC_NODE_CLLI VARCHAR SELECTIVITY 1,
    TENANT_ID VARCHAR SELECTIVITY 2,
    PROV_STATUS VARCHAR SELECTIVITY 1,
    VNF_PARAMS VARCHAR SELECTIVITY 1,
    VNF_OUTPUTS VARCHAR SELECTIVITY 3,
    REQUEST_BODY VARCHAR SELECTIVITY 79,
    RESPONSE_BODY VARCHAR SELECTIVITY 7,
    LAST_MODIFIED_BY VARCHAR SELECTIVITY 2,
    MODIFY_TIME VARCHAR,
    REQUEST_TYPE VARCHAR SELECTIVITY 1,
    VOLUME_GROUP_ID VARCHAR SELECTIVITY 2,
    VOLUME_GROUP_NAME VARCHAR SELECTIVITY 3,
    VF_MODULE_ID VARCHAR SELECTIVITY 5,
    VF_MODULE_NAME VARCHAR SELECTIVITY 8,
    VF_MODULE_MODEL_NAME VARCHAR SELECTIVITY 3,
    AAI_SERVICE_ID VARCHAR SELECTIVITY 1,
    AIC_CLOUD_REGION VARCHAR SELECTIVITY 1,
    CALLBACK_URL VARCHAR SELECTIVITY 1,
    CORRELATOR VARCHAR SELECTIVITY 1,
    NETWORK_ID VARCHAR SELECTIVITY 2,
    NETWORK_NAME VARCHAR SELECTIVITY 4,
    NETWORK_TYPE VARCHAR SELECTIVITY 1,
    REQUEST_SCOPE VARCHAR SELECTIVITY 1,
    REQUEST_ACTION VARCHAR SELECTIVITY 1,
    SERVICE_INSTANCE_ID VARCHAR SELECTIVITY 34,
    SERVICE_INSTANCE_NAME VARCHAR SELECTIVITY 25,
    REQUESTOR_ID VARCHAR SELECTIVITY 2,
    CONFIGURATION_ID VARCHAR SELECTIVITY 1,
    CONFIGURATION_NAME VARCHAR SELECTIVITY 2,
    OPERATIONAL_ENV_ID VARCHAR SELECTIVITY 1,
    OPERATIONAL_ENV_NAME VARCHAR SELECTIVITY 1
);          

INSERT INTO PUBLIC.INFRA_ACTIVE_REQUESTS(REQUEST_ID, CLIENT_REQUEST_ID, ACTION, REQUEST_STATUS, STATUS_MESSAGE, PROGRESS, START_TIME, END_TIME, SOURCE, VNF_ID, VNF_NAME, VNF_TYPE, SERVICE_TYPE, AIC_NODE_CLLI, TENANT_ID, PROV_STATUS, VNF_PARAMS, VNF_OUTPUTS, REQUEST_BODY, RESPONSE_BODY, LAST_MODIFIED_BY, MODIFY_TIME, REQUEST_TYPE, VOLUME_GROUP_ID, VOLUME_GROUP_NAME, VF_MODULE_ID, VF_MODULE_NAME, VF_MODULE_MODEL_NAME, AAI_SERVICE_ID, AIC_CLOUD_REGION, CALLBACK_URL, CORRELATOR, NETWORK_ID, NETWORK_NAME, NETWORK_TYPE, REQUEST_SCOPE, REQUEST_ACTION, SERVICE_INSTANCE_ID, SERVICE_INSTANCE_NAME, REQUESTOR_ID, CONFIGURATION_ID, CONFIGURATION_NAME, OPERATIONAL_ENV_ID, OPERATIONAL_ENV_NAME) VALUES
('00032ab7-3fb3-42e5-965d-8ea592502017', '00032ab7-3fb3-42e5-965d-8ea592502016', 'deleteInstance', 'COMPLETE', 'Vf Module has been deleted successfully.', '100', '2016-12-22 18:59:54', '2016-12-22 19:00:28', 'VID', 'b92f60c8-8de3-46c1-8dc1-e4390ac2b005', null, null, null, null, '6accefef3cb442ff9e644d589fb04107', null, null, null, '{"modelInfo":{"modelType":"vfModule","modelName":"vSAMP10aDEV::base::module-0"},"requestInfo":{"source":"VID"},"cloudConfiguration":{"tenantId":"6accefef3cb442ff9e644d589fb04107","lcpCloudRegionId":"mtn6"}}', null, 'BPMN', '2016-12-22 19:00:28', null, null, null, 'c7d527b1-7a91-49fd-b97d-1c8c0f4a7992', null, 'vSAMP10aDEV::base::module-0', null, 'mtn6', null, null, null, null, null, 'vfModule', 'deleteInstance', 'e3b5744d-2ad1-4cdd-8390-c999a38829bc', null, null, null, null, null, null),
('00093944-bf16-4373-ab9a-3adfe730ff2d', null, 'createInstance', 'FAILED', 'Error: Locked instance - This service (MSODEV_1707_SI_vSAMP10a_011-4) already has a request being worked with a status of IN_PROGRESS (RequestId - 278e83b1-4f9f-450e-9e7d-3700a6ed22f4). The existing request must finish or be cleaned up before proceeding.', '100', '2017-07-11 18:33:26', '2017-07-11 18:33:26', 'VID', null, null, null, null, null, '19123c2924c648eb8e42a3c1f14b7682', null, null, null, '{"modelInfo":{"modelInvariantId":"9647dfc4-2083-11e7-93ae-92361f002671","modelType":"service","modelName":"MSOTADevInfra_vSAMP10a_Service","modelVersion":"1.0","modelVersionId":"5df8b6de-2083-11e7-93ae-92361f002671"},"requestInfo":{"source":"VID","instanceName":"MSODEV_1707_SI_vSAMP10a_011-4","suppressRollback":false,"requestorId":"xxxxxx"},"subscriberInfo":{"globalSubscriberId":"MSO_1610_dev","subscriberName":"MSO_1610_dev"},"cloudConfiguration":{"tenantId":"19123c2924c648eb8e42a3c1f14b7682","lcpCloudRegionId":"mtn6"},"requestParameters":{"subscriptionServiceType":"MSO-dev-service-type","userParams":[{"name":"someUserParam","value":"someValue"}],"aLaCarte":true,"autoBuildVfModules":false,"cascadeDelete":false,"usePreload":true,"alaCarteSet":true,"alaCarte":true}}', null, 'APIH', null, null, null, null, null, null, null, null, 'mtn6', null, null, null, null, null, 'service', 'createInstance', null, 'MSODEV_1707_SI_vSAMP10a_011-4', 'xxxxxx', null, null, null, null),
('001619d2-a297-4a4b-a9f5-e2823c88458f', '001619d2-a297-4a4b-a9f5-e2823c88458f', 'CREATE_VF_MODULE', 'COMPLETE', 'COMPLETED', '100', '2016-07-01 14:11:42', '2017-05-02 16:03:34', 'PORTAL', null, 'test-vscp', 'elena_test21', null, null, '381b9ff6c75e4625b7a4182f90fc68d3', null, null, null, STRINGDECODE('<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<vnf-request xmlns=\"http://ecomp.att.com/mso/infra/vnf-request/v1\">\n    <request-info>\n        <request-id>001619d2-a297-4a4b-a9f5-e2823c88458f</request-id>\n        <action>CREATE_VF_MODULE</action>\n        <source>PORTAL</source>\n    </request-info>\n    <vnf-inputs>\n        <vnf-name>test-vscp</vnf-name>\n        <vf-module-name>moduleName</vf-module-name>\n        <vnf-type>elena_test21</vnf-type>\n        <vf-module-model-name>moduleModelName</vf-module-model-name>\n        <asdc-service-model-version>1.0</asdc-service-model-version>\n        <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>\n        <aic-cloud-region>mtn9</aic-cloud-region>\n        <tenant-id>381b9ff6c75e4625b7a4182f90fc68d3</tenant-id>\n        <persona-model-id></persona-model-id>\n        <persona-model-version></persona-model-version>\n        <is-base-vf-module>false</is-base-vf-module>\n    </vnf-inputs>\n    <vnf-params xmlns:tns=\"http://ecomp.att.com/mso/infra/vnf-request/v1\"/>\n</vnf-request>\n'), 'NONE', 'RDBTEST', '2016-07-01 14:11:42', 'VNF', null, null, null, 'MODULENAME1', 'moduleModelName', 'a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb', 'mtn9', null, null, null, null, null, 'vfModule', 'createInstance', null, null, null, null, null, null, null);           
INSERT INTO PUBLIC.INFRA_ACTIVE_REQUESTS(REQUEST_ID, CLIENT_REQUEST_ID, ACTION, REQUEST_STATUS, STATUS_MESSAGE, PROGRESS, START_TIME, END_TIME, SOURCE, VNF_ID, VNF_NAME, VNF_TYPE, SERVICE_TYPE, AIC_NODE_CLLI, TENANT_ID, PROV_STATUS, VNF_PARAMS, VNF_OUTPUTS, REQUEST_BODY, RESPONSE_BODY, LAST_MODIFIED_BY, MODIFY_TIME, REQUEST_TYPE, VOLUME_GROUP_ID, VOLUME_GROUP_NAME, VF_MODULE_ID, VF_MODULE_NAME, VF_MODULE_MODEL_NAME, AAI_SERVICE_ID, AIC_CLOUD_REGION, CALLBACK_URL, CORRELATOR, NETWORK_ID, NETWORK_NAME, NETWORK_TYPE, REQUEST_SCOPE, REQUEST_ACTION, SERVICE_INSTANCE_ID, SERVICE_INSTANCE_NAME, REQUESTOR_ID, CONFIGURATION_ID, CONFIGURATION_NAME, OPERATIONAL_ENV_ID, OPERATIONAL_ENV_NAME) VALUES
('00164b9e-784d-48a8-8973-bbad6ef818ed', null, 'createInstance', 'COMPLETE', 'Service Instance was created successfully.', '100', '2017-09-28 12:45:51', '2017-09-28 12:45:53', 'VID', null, null, null, null, null, '19123c2924c648eb8e42a3c1f14b7682', null, null, null, '{"modelInfo":{"modelCustomizationName":null,"modelInvariantId":"52b49b5d-3086-4ffd-b5e6-1b1e5e7e062f","modelType":"service","modelNameVersionId":null,"modelName":"MSO Test Network","modelVersion":"1.0","modelCustomizationUuid":null,"modelVersionId":"aed5a5b7-20d3-44f7-90a3-ddbd16f14d1e","modelCustomizationId":null,"modelUuid":null,"modelInvariantUuid":null,"modelInstanceName":null},"requestInfo":{"billingAccountNumber":null,"callbackUrl":null,"correlator":null,"orderNumber":null,"productFamilyId":null,"orderVersion":null,"source":"VID","instanceName":"DEV-MTN6-3100-0927-1","suppressRollback":false,"requestorId":"xxxxxx"},"relatedInstanceList":null,"subscriberInfo":{"globalSubscriberId":"MSO_1610_dev","subscriberName":"MSO_1610_dev"},"cloudConfiguration":{"aicNodeClli":null,"tenantId":"19123c2924c648eb8e42a3c1f14b7682","lcpCloudRegionId":"mtn6"},"requestParameters":{"subscriptionServiceType":"MSO-dev-service-type","userParams":[{"name":"someUserParam","value":"someValue"}],"aLaCarte":true,"autoBuildVfModules":false,"cascadeDelete":false,"usePreload":true,"alaCarte":true},"project":null,"owningEntity":null,"platform":null,"lineOfBusiness":null}', null, 'CreateGenericALaCarteServiceInstance', '2017-09-28 12:45:52', null, null, null, null, null, null, null, 'mtn6', null, null, null, null, null, 'service', 'createInstance', 'b2f59173-b7e5-4e0f-8440-232fd601b865', 'DEV-MTN6-3100-0927-1', 'md5621', null, null, null, null),
('00173cc9-5ce2-4673-a810-f87fefb2829e', null, 'createInstance', 'FAILED', 'Error parsing request.  No valid instanceName is specified', '100', '2017-04-14 21:08:46', '2017-04-14 21:08:46', 'VID', null, null, null, null, null, 'a259ae7b7c3f493cb3d91f95a7c18149', null, null, null, '{"modelInfo":{"modelInvariantId":"ff6163d4-7214-459e-9f76-507b4eb00f51","modelType":"service","modelName":"ConstraintsSrvcVID","modelVersion":"2.0","modelVersionId":"722d256c-a374-4fba-a14f-a59b76bb7656"},"requestInfo":{"productFamilyId":"LRSI-OSPF","source":"VID","requestorId":"xxxxxx"},"subscriberInfo":{"globalSubscriberId":"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb"},"cloudConfiguration":{"tenantId":"a259ae7b7c3f493cb3d91f95a7c18149","lcpCloudRegionId":"mtn16"},"requestParameters":{"subscriptionServiceType":"Mobility","userParams":[{"name":"neutronport6_name","value":"8"},{"name":"neutronnet5_network_name","value":"8"},{"name":"contrailv2vlansubinterface3_name","value":"false"}]}}', null, 'APIH', null, null, null, null, null, null, null, null, 'mtn16', null, null, null, null, null, 'service', 'createInstance', null, null, null, null, null, null, null),
('0017f68c-eb2d-45bb-b7c7-ec31b37dc349', null, 'activateInstance', 'UNLOCKED', null, '20', '2017-09-26 16:09:29', null, 'VID', null, null, null, null, null, null, null, null, null, '{"modelInfo":{"modelCustomizationName":null,"modelInvariantId":"1587cf0e-f12f-478d-8530-5c55ac578c39","modelType":"configuration","modelNameVersionId":null,"modelName":null,"modelVersion":null,"modelCustomizationUuid":null,"modelVersionId":"36a3a8ea-49a6-4ac8-b06c-89a545444455","modelCustomizationId":"68dc9a92-214c-11e7-93ae-92361f002671","modelUuid":null,"modelInvariantUuid":null,"modelInstanceName":null},"requestInfo":{"billingAccountNumber":null,"callbackUrl":null,"correlator":null,"orderNumber":null,"productFamilyId":null,"orderVersion":null,"source":"VID","instanceName":null,"suppressRollback":false,"requestorId":"xxxxxx"},"relatedInstanceList":[{"relatedInstance":{"instanceName":null,"instanceId":"9e15a443-af65-4f05-9000-47ae495e937d","modelInfo":{"modelCustomizationName":null,"modelInvariantId":"de19ae10-9a25-11e7-abc4-cec278b6b50a","modelType":"service","modelNameVersionId":null,"modelName":"MSOTADevInfra_Configuration_Service","modelVersion":"1.0","modelCustomizationUuid":null,"modelVersionId":"ee938612-9a25-11e7-abc4-cec278b6b50a","modelCustomizationId":null,"modelUuid":null,"modelInvariantUuid":null,"modelInstanceName":null},"instanceDirection":null}}],"subscriberInfo":null,"cloudConfiguration":{"aicNodeClli":null,"tenantId":null,"lcpCloudRegionId":"mtn6"},"requestParameters":{"subscriptionServiceType":null,"userParams":[],"aLaCarte":false,"autoBuildVfModules":false,"cascadeDelete":false,"usePreload":true,"alaCarte":false},"project":null,"owningEntity":null,"platform":null,"lineOfBusiness":null}', null, 'APIH', '2017-09-26 16:09:29', null, null, null, null, null, null, null, 'mtn6', null, null, null, null, null, 'configuration', 'activateInstance', '9e15a443-af65-4f05-9000-47ae495e937d', null, 'xxxxxx', '26ef7f15-57bb-48df-8170-e59edc26234c', null, null, null);        

CREATE CACHED TABLE PUBLIC.ARCHIVED_INFRA_REQUESTS(
    REQUEST_ID VARCHAR NOT NULL SELECTIVITY 100,
    CLIENT_REQUEST_ID VARCHAR SELECTIVITY 6,
    ACTION VARCHAR SELECTIVITY 1,
    REQUEST_STATUS VARCHAR SELECTIVITY 1,
    STATUS_MESSAGE longtext SELECTIVITY 36,
    PROGRESS VARCHAR SELECTIVITY 1,
    START_TIME VARCHAR,
    END_TIME VARCHAR,
    SOURCE VARCHAR SELECTIVITY 2,
    VNF_ID VARCHAR SELECTIVITY 15,
    VNF_NAME VARCHAR SELECTIVITY 11,
    VNF_TYPE VARCHAR SELECTIVITY 5,
    SERVICE_TYPE VARCHAR SELECTIVITY 1,
    AIC_NODE_CLLI VARCHAR SELECTIVITY 1,
    TENANT_ID VARCHAR SELECTIVITY 2,
    PROV_STATUS VARCHAR SELECTIVITY 1,
    VNF_PARAMS VARCHAR SELECTIVITY 1,
    VNF_OUTPUTS VARCHAR SELECTIVITY 3,
    REQUEST_BODY VARCHAR SELECTIVITY 79,
    RESPONSE_BODY VARCHAR SELECTIVITY 7,
    LAST_MODIFIED_BY VARCHAR SELECTIVITY 2,
    MODIFY_TIME VARCHAR,
    REQUEST_TYPE VARCHAR SELECTIVITY 1,
    VOLUME_GROUP_ID VARCHAR SELECTIVITY 2,
    VOLUME_GROUP_NAME VARCHAR SELECTIVITY 3,
    VF_MODULE_ID VARCHAR SELECTIVITY 5,
    VF_MODULE_NAME VARCHAR SELECTIVITY 8,
    VF_MODULE_MODEL_NAME VARCHAR SELECTIVITY 3,
    AAI_SERVICE_ID VARCHAR SELECTIVITY 1,
    AIC_CLOUD_REGION VARCHAR SELECTIVITY 1,
    CALLBACK_URL VARCHAR SELECTIVITY 1,
    CORRELATOR VARCHAR SELECTIVITY 1,
    NETWORK_ID VARCHAR SELECTIVITY 2,
    NETWORK_NAME VARCHAR SELECTIVITY 4,
    NETWORK_TYPE VARCHAR SELECTIVITY 1,
    REQUEST_SCOPE VARCHAR SELECTIVITY 1,
    REQUEST_ACTION VARCHAR SELECTIVITY 1,
    SERVICE_INSTANCE_ID VARCHAR SELECTIVITY 34,
    SERVICE_INSTANCE_NAME VARCHAR SELECTIVITY 25,
    REQUESTOR_ID VARCHAR SELECTIVITY 2,
    CONFIGURATION_ID VARCHAR SELECTIVITY 1,
    CONFIGURATION_NAME VARCHAR SELECTIVITY 2,
    OPERATIONAL_ENV_ID VARCHAR SELECTIVITY 1,
    OPERATIONAL_ENV_NAME VARCHAR SELECTIVITY 1
);

CREATE CACHED TABLE PUBLIC.SITE_STATUS(
    SITE_NAME VARCHAR NOT NULL,
    STATUS VARCHAR,
    CREATION_TIMESTAMP VARCHAR
);             
    
INSERT INTO PUBLIC.SITE_STATUS(SITE_NAME, STATUS, CREATION_TIMESTAMP) VALUES
('testSite', '0', '2017-11-30 15:48:09'),
('test name', null, null),
('test name2', '1', null),
('test name3', '1', null),
('test name4', '1', '2017-11-30 15:48:09'),
('test name update', '0', null);
         
CREATE CACHED TABLE PUBLIC.WATCHDOG_DISTRIBUTIONID_STATUS(
    DISTRIBUTION_ID VARCHAR NOT NULL,
    DISTRIBUTION_ID_STATUS VARCHAR,
    CREATE_TIME VARCHAR,
    MODIFY_TIME VARCHAR
); 
           
        
INSERT INTO PUBLIC.WATCHDOG_DISTRIBUTIONID_STATUS(DISTRIBUTION_ID, DISTRIBUTION_ID_STATUS, CREATE_TIME, MODIFY_TIME) VALUES
('1533c4bd-a3e3-493f-a16d-28c20614415e', '', '2017-11-30 15:48:09', '2017-11-30 15:48:09'),
('55429711-809b-4a3b-9ee5-5120d46d9de0', '', '2017-11-30 16:35:36', '2017-11-30 16:35:36'),
('67f0b2d1-9013-4b2b-9914-bbe2288284fb', '', '2017-11-30 15:54:39', '2017-11-30 15:54:39');   

CREATE CACHED TABLE PUBLIC.WATCHDOG_PER_COMPONENT_DISTRIBUTION_STATUS(
    DISTRIBUTION_ID VARCHAR NOT NULL,
    COMPONENT_NAME VARCHAR NOT NULL,
    COMPONENT_DISTRIBUTION_STATUS VARCHAR,
    CREATE_TIME VARCHAR,
    MODIFY_TIME VARCHAR
);        

           
INSERT INTO PUBLIC.WATCHDOG_PER_COMPONENT_DISTRIBUTION_STATUS(DISTRIBUTION_ID, COMPONENT_NAME, COMPONENT_DISTRIBUTION_STATUS, CREATE_TIME, MODIFY_TIME) VALUES
('1533c4bd-a3e3-493f-a16d-28c20614415e', 'MSO', 'COMPONENT_DONE_OK', '2017-11-30 15:48:09', '2017-11-30 15:48:09'),
('55429711-809b-4a3b-9ee5-5120d46d9de0', 'MSO', 'COMPONENT_DONE_ERROR', '2017-11-30 16:35:36', '2017-11-30 16:35:36'),
('67f0b2d1-9013-4b2b-9914-bbe2288284fb', 'MSO', 'COMPONENT_DONE_OK', '2017-11-30 15:54:39', '2017-11-30 15:54:39');        

CREATE CACHED TABLE PUBLIC.WATCHDOG_SERVICE_MOD_VER_ID_LOOKUP(
    DISTRIBUTION_ID VARCHAR NOT NULL,
    SERVICE_MODEL_VERSION_ID VARCHAR NOT NULL,
    CREATE_TIME VARCHAR,
    MODIFY_TIME VARCHAR
);
        
  
INSERT INTO PUBLIC.WATCHDOG_SERVICE_MOD_VER_ID_LOOKUP(DISTRIBUTION_ID, SERVICE_MODEL_VERSION_ID, CREATE_TIME, MODIFY_TIME) VALUES
('1533c4bd-a3e3-493f-a16d-28c20614415e', '7e813ab5-88d3-4fcb-86c0-498c5d7eef9a', '2017-11-30 15:48:08', '2017-11-30 15:48:08'),
('55429711-809b-4a3b-9ee5-5120d46d9de0', 'cc031e75-4442-4d1a-b774-8a2b434e0a50', '2017-11-30 16:35:36', '2017-11-30 16:35:36'),
('67f0b2d1-9013-4b2b-9914-bbe2288284fb', 'eade1e9d-c1ec-4ef3-bc31-60570fba1573', '2017-11-30 15:54:39', '2017-11-30 15:54:39');    

ALTER TABLE PUBLIC.INFRA_ACTIVE_REQUESTS ADD CONSTRAINT PUBLIC.CONSTRAINT_E PRIMARY KEY(REQUEST_ID);          
ALTER TABLE PUBLIC.ACTIVE_REQUESTS ADD CONSTRAINT PUBLIC.CONSTRAINT_5 PRIMARY KEY(REQUEST_ID);
ALTER TABLE PUBLIC.ACTIVE_REQUESTS ADD CONSTRAINT PUBLIC.UK_F0HDK7XBW5MB2TRNXX0FVLH3X UNIQUE(CLIENT_REQUEST_ID);       
ALTER TABLE PUBLIC.SITE_STATUS ADD CONSTRAINT PUBLIC.CONSTRAINT_C PRIMARY KEY(SITE_NAME); 
ALTER TABLE PUBLIC.WATCHDOG_DISTRIBUTIONID_STATUS ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY(DISTRIBUTION_ID); 
ALTER TABLE PUBLIC.WATCHDOG_PER_COMPONENT_DISTRIBUTION_STATUS ADD CONSTRAINT PUBLIC.CONSTRAINT_D PRIMARY KEY(DISTRIBUTION_ID, COMPONENT_NAME);
ALTER TABLE PUBLIC.WATCHDOG_SERVICE_MOD_VER_ID_LOOKUP ADD CONSTRAINT PUBLIC.CONSTRAINT_6 PRIMARY KEY(DISTRIBUTION_ID, SERVICE_MODEL_VERSION_ID);   
ALTER TABLE PUBLIC.WATCHDOG_PER_COMPONENT_DISTRIBUTION_STATUS ADD CONSTRAINT PUBLIC.CONSTRAINT_DE FOREIGN KEY(DISTRIBUTION_ID) REFERENCES PUBLIC.WATCHDOG_DISTRIBUTIONID_STATUS(DISTRIBUTION_ID) NOCHECK; 
                   
 
