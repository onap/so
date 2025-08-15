SDNC Adapter is a conduit between MSO BPEL and SDNC
SOAP requests from BPEL are sent as REST requests to SDNC 
BPEL get a SYNC response (with no data) right away
On SDNC SYNC response, BPEL is  sent an ASYNC (callback) response
On SDNC ASYNC responses/notifications, BPEL is sent ASYNC responses
Failure to send request to SDNC results in header to BPEL with respCode(4xx,5xx) and msg
Success in sending requests to SDNC results in header to BPEL with 2xx respCode to BPEL and data received from SDNC 
SDNC data might have failures or success

SDNCAdapter behaviour on being deployed in Tomcat Springboot 
-------------------------------------------------



		