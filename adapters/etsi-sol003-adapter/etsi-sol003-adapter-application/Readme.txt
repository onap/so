The following describes how to configure authentication for the VNFM adapter.

TLS should always be configured to ensure secure communication between the VNFM-adapter <-> BPMN infra and VNFM-adapter <-> VNFM
If two-way TLS is configured then there is no need for any further authentication (i.e. no need for token or basic auth).
If two-way TLS is NOT configured then authentication is REQUIRED. Oauth token based authentication must be used for requests, while for notifications either oauth tokens or basic auth can be used.


==========================================
To confgure TLS
==========================================

---------------
VNFM Adapter
---------------
The following parameters can be set to configure the certificate for the VNFM adapter
server:
  ssl:
    key-alias: so@so.onap.org
    key--store-password: 'ywsqCy:EEo#j}HJHM7z^Rk[L'
    key-store: classpath:so-vnfm-adapter.p12
    key-store-type: PKCS12
The values shown above relate to the certificate included in the VNFM adapter jar which has been generated from AAF. If a different certificate is to be used then these values should be changed accordingly.

The following paramters can be set to configure the trust store for the VNFM adapter:
http:
  client:
    ssl:
      trust-store: classpath:org.onap.so.trust.jks
      trust-store-password: ',sx#.C*W)]wVgJC6ccFHI#:H'
The values shown above relate to the trust store included in the VNFM adapter jar which has been generated from AAI. If a different trust store is to be used then these values should be changed accordingly.

Ensure the value for the below parameter uses https instead of http
vnfmadapter:
  endpoint: http://so-vnfm-adapter.onap:9092
  
---------------
bpmn-infra
---------------
For bpmn-infra, ensure the value for the below parameter uses https instead of http
so:
  vnfm:
    adapter:
      url: https://so-vnfm-adapter.onap:9092/so/vnfm-adapter/v1/


==========================================
To use two way TLS
==========================================

Ensure the value for username and password are empty in the AAI entry for the VNFM (The VNFM adapter will use oauth instead of two way TLS if the username/password is set).
Ensure TLS has been configuered as detailed above.

---------------
VNFM adapter
---------------
Set the following parameter for the VNFM adapter:
server:
  ssl:
    client-auth: need
	
---------------
bpmn-infra:
---------------
Set the following paramters for bpmn-infra:
rest:
  http:
    client:
      configuration:
        ssl:
          keyStore: classpath:org.onap.so.p12
          keyStorePassword: 'RLe5ExMWW;Kd6GTSt0WQz;.Y'
          trustStore: classpath:org.onap.so.trust.jks
          trustStorePassword: '6V%8oSU$,%WbYp3IUe;^mWt4'
Ensure the value for the below parameter uses https instead of http
so:
  vnfm:
    adapter:
      url: https://so-vnfm-adapter.onap:9092/so/vnfm-adapter/v1/
	  
---------------	  
VNFM simulator:
---------------
Set the following parameters for the VNFM simulator (if used):
server:
  ssl:
    client-auth: need
  request:
    grant:
      auth: twowaytls

==========================================
To use oauth token base authentication
==========================================

---------------	  
VNFM adapter:
---------------
Ensure the value for username and password set set in the AAI entry for the VNFM. The VNFM adapter will use this username/password as the client credentials in the request for a token for the VNFM. The token endpoint
for the VNFM will by default will be derived from the service url for the VNFM in AAI as follows: <base of service url>/oauth/token, e.g. if the service url is https://so-vnfm-simulator.onap/vnflcm/v1 then the token url will
be taken to be https://so-vnfm-simulator.onap/oauth/token. This can be overriden using the following parameter for the VNFM adapter:
vnfmadapter:
  temp:
    vnfm:
	  oauth:
	    endpoint:
		
The VNFM adapter exposes a token point at url: https://<hostname>:<port>/oauth/token e.g. https://so-vnfm-adapter.onap:9092/oauth/token. The VNFM can request a token from this endpoint for use in grant requests and notifications
to the VNFM adapter. The username/password to be used in the token request are passed to the VNFM in a subscription request. The username/password sent by the VNFM adpater in the subscription request can be configuered using the 
following parameter:
vnfmadapter:
  auth: <encoded value>
where <encoded value> is '<username>:<password>' encoded using org.onap.so.utils.CryptoUtils with the key set by the paramter:
mso:
  key: <key>
The default username:password is vnfm-adapter:123456 when vnfm-adapter.auth is not set.
		  
---------------	  
VNFM simulator:
---------------
Set the following parameters for the simulator:
spring:
  profiles:
    active: oauth-authentication
server:
  request:
    grant:
      auth: oauth
		
==========================================
To use basic auth for notifications
==========================================		
The same username/password is used as for oauth token requests as describe above and passed to the VNFM in the subscription request.