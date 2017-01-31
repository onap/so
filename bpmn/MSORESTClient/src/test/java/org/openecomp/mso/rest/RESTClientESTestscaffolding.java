/**
 * Scaffolding file used to store all the setups needed to run 
 * tests automatically generated by EvoSuite
 * Mon Nov 14 11:49:09 GMT 2016
 */

package org.openecomp.mso.rest;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.AfterClass;
import org.evosuite.runtime.sandbox.Sandbox;

@EvoSuiteClassExclude
public class RESTClientESTestscaffolding {

  @org.junit.Rule 
  public org.evosuite.runtime.vnet.NonFunctionalRequirementRule nfr = new org.evosuite.runtime.vnet.NonFunctionalRequirementRule();

  private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone(); 

  private org.evosuite.runtime.thread.ThreadStopper threadStopper =  new org.evosuite.runtime.thread.ThreadStopper (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(), 3000);

  @BeforeClass 
  public static void initEvoSuiteFramework() { 
    org.evosuite.runtime.RuntimeSettings.className = "org.openecomp.mso.rest.RESTClient"; 
    org.evosuite.runtime.GuiSupport.initialize(); 
    org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100; 
    org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000; 
    org.evosuite.runtime.RuntimeSettings.mockSystemIn = true; 
    org.evosuite.runtime.RuntimeSettings.sandboxMode = org.evosuite.runtime.sandbox.Sandbox.SandboxMode.RECOMMENDED; 
    org.evosuite.runtime.sandbox.Sandbox.initializeSecurityManagerForSUT(); 
    org.evosuite.runtime.classhandling.JDKClassResetter.init(); 
    initializeClasses();
    org.evosuite.runtime.Runtime.getInstance().resetRuntime(); 
  } 

  @AfterClass 
  public static void clearEvoSuiteFramework(){ 
    Sandbox.resetDefaultSecurityManager(); 
    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone()); 
  } 

  @Before 
  public void initTestCase(){ 
    threadStopper.storeCurrentThreads();
    threadStopper.startRecordingTime();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().initHandler(); 
    org.evosuite.runtime.sandbox.Sandbox.goingToExecuteSUTCode(); 
     
    org.evosuite.runtime.GuiSupport.setHeadless(); 
    org.evosuite.runtime.Runtime.getInstance().resetRuntime(); 
    org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
  } 

  @After 
  public void doneWithTestCase(){ 
    threadStopper.killAndJoinClientThreads();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().safeExecuteAddedHooks(); 
    org.evosuite.runtime.classhandling.JDKClassResetter.reset(); 
    resetClasses(); 
    org.evosuite.runtime.sandbox.Sandbox.doneWithExecutingSUTCode(); 
    org.evosuite.runtime.agent.InstrumentingAgent.deactivate(); 
    org.evosuite.runtime.GuiSupport.restoreHeadlessMode(); 
  } 


  private static void initializeClasses() {
    org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(RESTClientESTestscaffolding.class.getClassLoader() ,
      "org.apache.http.client.methods.HttpPatch",
      "org.apache.http.io.HttpMessageParserFactory",
      "org.apache.http.impl.conn.DefaultHttpResponseParserFactory",
      "org.apache.http.impl.execchain.RequestAbortedException",
      "org.apache.http.impl.execchain.ProtocolExec",
      "org.apache.http.config.Registry",
      "org.apache.http.cookie.MalformedCookieException",
      "org.apache.http.impl.conn.SystemDefaultDnsResolver",
      "org.apache.http.client.protocol.RequestClientConnControl",
      "org.apache.http.conn.ConnectionRequest",
      "org.apache.http.impl.client.DefaultUserTokenHandler",
      "org.apache.http.impl.conn.DefaultManagedHttpClientConnection",
      "org.apache.http.conn.HttpClientConnectionManager",
      "org.apache.http.client.protocol.RequestAcceptEncoding",
      "org.apache.http.HttpException",
      "org.apache.http.cookie.CookieSpec",
      "org.apache.http.impl.cookie.RFC2965SpecFactory",
      "org.apache.http.pool.AbstractConnPool$1",
      "org.apache.http.conn.ssl.AllowAllHostnameVerifier",
      "org.apache.http.client.CredentialsProvider",
      "org.apache.http.client.ClientProtocolException",
      "org.apache.http.pool.RouteSpecificPool",
      "org.apache.http.client.methods.Configurable",
      "org.apache.http.config.RegistryBuilder",
      "org.apache.http.params.AbstractHttpParams",
      "org.apache.http.io.HttpTransportMetrics",
      "org.apache.http.conn.ssl.AbstractVerifier",
      "org.openecomp.mso.rest.RESTConfig",
      "org.apache.http.auth.Credentials",
      "org.apache.http.io.HttpMessageParser",
      "org.apache.http.client.methods.AbstractExecutionAwareRequest",
      "org.apache.http.impl.BHttpConnectionBase",
      "org.apache.http.HttpConnectionMetrics",
      "org.apache.http.io.HttpMessageWriter",
      "org.apache.http.HttpClientConnection",
      "org.apache.http.conn.ConnectionPoolTimeoutException",
      "org.apache.http.conn.routing.HttpRouteDirector",
      "org.apache.http.pool.ConnPool",
      "org.apache.http.protocol.HttpProcessor",
      "org.apache.http.auth.AuthProtocolState",
      "org.apache.http.client.RedirectStrategy",
      "org.apache.http.impl.client.BasicCookieStore",
      "org.apache.http.conn.routing.BasicRouteDirector",
      "org.apache.http.protocol.HttpContext",
      "org.apache.http.params.HttpParams",
      "org.apache.http.client.NonRepeatableRequestException",
      "org.apache.http.HttpResponse",
      "org.apache.http.impl.client.AuthenticationStrategyImpl",
      "org.apache.http.impl.client.HttpClientBuilder",
      "org.apache.http.message.HeaderGroup",
      "org.apache.http.impl.io.DefaultHttpRequestWriterFactory",
      "org.apache.http.client.protocol.RequestAuthCache",
      "org.apache.http.impl.conn.PoolingHttpClientConnectionManager$InternalConnectionFactory",
      "org.apache.http.impl.conn.DefaultSchemePortResolver",
      "org.apache.http.config.MessageConstraints",
      "org.apache.http.Header",
      "org.apache.http.conn.HttpHostConnectException",
      "org.apache.http.util.EntityUtils",
      "org.apache.http.impl.NoConnectionReuseStrategy",
      "org.apache.http.impl.client.BasicCredentialsProvider",
      "org.apache.http.conn.ConnectionKeepAliveStrategy",
      "org.apache.http.cookie.CookieSpecFactory",
      "org.apache.http.conn.ssl.X509HostnameVerifier",
      "org.apache.http.protocol.ChainBuilder",
      "org.apache.http.impl.client.DefaultHttpRequestRetryHandler",
      "org.apache.http.impl.conn.PoolingHttpClientConnectionManager",
      "org.apache.http.impl.conn.DefaultProxyRoutePlanner",
      "org.apache.http.impl.auth.KerberosSchemeFactory",
      "org.apache.http.util.ByteArrayBuffer",
      "org.apache.http.cookie.CookieOrigin",
      "org.apache.http.client.methods.HttpRequestBase",
      "org.apache.http.HttpEntity",
      "org.apache.http.pool.PoolEntryCallback",
      "org.apache.http.entity.StringEntity",
      "org.apache.http.impl.DefaultConnectionReuseStrategy",
      "org.apache.http.pool.ConnFactory",
      "org.apache.http.client.methods.HttpGet",
      "org.apache.http.protocol.BasicHttpContext",
      "org.apache.commons.logging.impl.Jdk14Logger",
      "org.apache.http.impl.execchain.ClientExecChain",
      "org.apache.http.HttpVersion",
      "org.apache.http.conn.SchemePortResolver",
      "org.apache.http.message.BasicStatusLine",
      "org.apache.http.conn.DnsResolver",
      "org.apache.http.impl.client.TargetAuthenticationStrategy",
      "org.apache.http.params.CoreProtocolPNames",
      "org.apache.http.auth.AuthScheme",
      "org.apache.http.message.AbstractHttpMessage",
      "org.apache.http.auth.MalformedChallengeException",
      "org.apache.http.HttpEntityEnclosingRequest",
      "org.apache.http.entity.AbstractHttpEntity",
      "org.apache.http.ReasonPhraseCatalog",
      "org.apache.http.impl.cookie.BrowserCompatSpecFactory$SecurityLevel",
      "org.apache.http.client.UserTokenHandler",
      "org.apache.http.impl.auth.DigestSchemeFactory",
      "org.apache.http.impl.conn.HttpClientConnectionOperator",
      "org.apache.http.HttpResponseFactory",
      "org.apache.http.client.methods.HttpPut",
      "org.openecomp.mso.rest.RESTClient",
      "org.apache.http.ConnectionReuseStrategy",
      "org.apache.http.client.protocol.RequestDefaultHeaders",
      "org.apache.http.message.BasicHeader",
      "org.apache.http.impl.conn.ConnectionShutdownException",
      "org.apache.http.conn.ManagedHttpClientConnection",
      "org.apache.http.client.protocol.ResponseContentEncoding",
      "org.apache.http.message.BasicLineParser",
      "org.apache.http.client.methods.HttpPost",
      "org.apache.http.auth.AuthSchemeProvider",
      "org.apache.http.config.SocketConfig",
      "org.apache.http.util.Asserts",
      "org.apache.http.client.config.RequestConfig",
      "org.apache.http.StatusLine",
      "org.apache.http.impl.DefaultBHttpClientConnection",
      "org.apache.http.impl.DefaultHttpResponseFactory",
      "org.apache.http.io.SessionOutputBuffer",
      "org.apache.http.RequestLine",
      "org.apache.http.conn.HttpConnectionFactory",
      "org.apache.http.protocol.RequestContent",
      "org.apache.http.cookie.CookieIdentityComparator",
      "org.apache.http.config.Lookup",
      "org.apache.http.HttpMessage",
      "org.apache.http.impl.cookie.NetscapeDraftSpecFactory",
      "org.apache.http.HttpRequestInterceptor",
      "org.apache.http.HeaderElementIterator",
      "org.apache.http.client.AuthCache",
      "org.apache.http.pool.AbstractConnPool",
      "org.apache.http.HeaderIterator",
      "org.apache.http.conn.ClientConnectionManager",
      "org.apache.http.HttpInetConnection",
      "org.apache.http.entity.ContentType",
      "org.apache.http.message.LineFormatter",
      "org.apache.http.cookie.CookieSpecProvider",
      "org.apache.http.HttpRequest",
      "org.apache.http.pool.ConnPoolControl",
      "org.openecomp.mso.rest.APIResponse",
      "org.apache.http.client.BackoffManager",
      "org.openecomp.mso.rest.HostNameVerifier",
      "org.apache.http.client.AuthenticationStrategy",
      "org.apache.http.conn.socket.ConnectionSocketFactory",
      "org.apache.http.protocol.RequestTargetHost",
      "org.apache.http.pool.PoolEntry",
      "org.apache.http.message.BasicLineFormatter",
      "org.apache.http.client.methods.HttpUriRequest",
      "org.apache.http.protocol.HttpRequestExecutor",
      "org.apache.http.client.methods.HttpRequestWrapper",
      "org.apache.http.io.SessionInputBuffer",
      "org.apache.http.impl.cookie.IgnoreSpecFactory",
      "org.apache.http.impl.auth.HttpAuthenticator",
      "org.apache.http.impl.conn.ManagedHttpClientConnectionFactory",
      "org.apache.http.conn.ConnectTimeoutException",
      "org.apache.http.client.methods.AbortableHttpRequest",
      "org.apache.http.client.HttpClient",
      "org.apache.http.auth.AuthSchemeFactory",
      "org.apache.http.cookie.Cookie",
      "org.apache.http.protocol.ImmutableHttpProcessor",
      "org.apache.http.impl.auth.SPNegoSchemeFactory",
      "org.apache.http.protocol.HTTP",
      "org.apache.http.impl.conn.PoolingHttpClientConnectionManager$ConfigData",
      "org.openecomp.mso.rest.RESTClient$HttpDeleteWithBody",
      "org.apache.http.TokenIterator",
      "org.openecomp.mso.rest.HttpHeader",
      "org.apache.http.client.methods.HttpRequestWrapper$HttpEntityEnclosingRequestWrapper",
      "org.apache.http.protocol.HttpCoreContext",
      "org.apache.http.impl.conn.CPool",
      "org.apache.http.impl.auth.NTLMSchemeFactory",
      "org.apache.http.client.utils.URIUtils",
      "org.apache.http.ProtocolVersion",
      "org.apache.http.client.protocol.RequestExpectContinue",
      "org.apache.http.util.VersionInfo",
      "org.apache.http.impl.cookie.RFC2109SpecFactory",
      "org.apache.http.entity.InputStreamEntity",
      "org.apache.http.HttpHost",
      "org.apache.http.conn.UnsupportedSchemeException",
      "org.apache.http.ProtocolException",
      "org.apache.http.impl.cookie.BrowserCompatSpecFactory",
      "org.apache.http.client.methods.HttpEntityEnclosingRequestBase",
      "org.apache.http.params.BasicHttpParams",
      "org.apache.http.client.protocol.HttpClientContext",
      "org.apache.http.impl.client.ProxyAuthenticationStrategy",
      "org.apache.http.conn.ssl.StrictHostnameVerifier",
      "org.apache.http.io.HttpMessageWriterFactory",
      "org.apache.http.concurrent.Cancellable",
      "org.apache.http.impl.execchain.MainClientExec",
      "org.apache.http.protocol.HttpProcessorBuilder",
      "org.apache.http.entity.ContentLengthStrategy",
      "org.apache.http.impl.execchain.TunnelRefusedException",
      "org.apache.http.conn.routing.HttpRoutePlanner",
      "org.apache.http.Consts",
      "org.apache.http.conn.ssl.SSLConnectionSocketFactory",
      "org.apache.http.message.LineParser",
      "org.apache.http.impl.cookie.BestMatchSpecFactory",
      "org.apache.http.params.HttpParamsNames",
      "org.apache.http.conn.ssl.SSLInitializationException",
      "org.openecomp.mso.rest.RESTException",
      "org.apache.http.util.Args",
      "org.apache.http.params.HttpProtocolParams",
      "org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy",
      "org.apache.http.protocol.RequestUserAgent",
      "org.apache.http.config.ConnectionConfig",
      "org.apache.http.conn.socket.LayeredConnectionSocketFactory",
      "org.apache.http.conn.ssl.BrowserCompatHostnameVerifier",
      "org.apache.http.util.TextUtils",
      "org.apache.http.HttpResponseInterceptor",
      "org.apache.http.impl.EnglishReasonPhraseCatalog",
      "org.apache.http.client.config.RequestConfig$Builder",
      "org.apache.http.auth.AuthenticationException",
      "org.apache.http.auth.AuthState",
      "org.apache.http.client.protocol.RequestAddCookies",
      "org.apache.http.impl.conn.DefaultRoutePlanner",
      "org.apache.http.conn.routing.HttpRoute",
      "org.apache.http.impl.conn.CPoolEntry",
      "org.apache.http.client.CookieStore",
      "org.apache.http.impl.auth.BasicSchemeFactory",
      "org.apache.http.conn.socket.PlainConnectionSocketFactory",
      "org.apache.http.client.HttpRequestRetryHandler",
      "org.apache.http.ParseException",
      "org.apache.http.impl.client.CloseableHttpClient",
      "org.apache.http.client.protocol.ResponseProcessCookies",
      "org.apache.http.message.BasicRequestLine",
      "org.apache.http.client.ServiceUnavailableRetryStrategy",
      "org.apache.http.client.methods.HttpExecutionAware",
      "org.apache.http.impl.client.InternalHttpClient",
      "org.apache.http.HeaderElement",
      "org.apache.http.client.ConnectionBackoffStrategy",
      "org.apache.http.util.CharArrayBuffer",
      "org.apache.http.impl.execchain.RetryExec",
      "org.apache.http.conn.routing.RouteInfo",
      "org.apache.http.client.ResponseHandler",
      "org.apache.http.HttpConnection",
      "org.apache.http.message.ParserCursor"
    );
  } 

  private static void resetClasses() {
    org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(RESTClientESTestscaffolding.class.getClassLoader());

    org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
      "org.openecomp.mso.rest.RESTClient$HttpDeleteWithBody",
      "org.apache.http.conn.socket.PlainConnectionSocketFactory",
      "org.apache.http.conn.ssl.AbstractVerifier",
      "org.apache.commons.logging.impl.Jdk14Logger",
      "org.apache.http.conn.ssl.SSLConnectionSocketFactory",
      "org.apache.http.impl.conn.CPool",
      "org.apache.http.message.BasicLineFormatter",
      "org.apache.http.impl.io.DefaultHttpRequestWriterFactory",
      "org.apache.http.ProtocolVersion",
      "org.apache.http.HttpVersion",
      "org.apache.http.message.BasicLineParser",
      "org.apache.http.impl.EnglishReasonPhraseCatalog",
      "org.apache.http.impl.DefaultHttpResponseFactory",
      "org.apache.http.impl.conn.DefaultHttpResponseParserFactory",
      "org.apache.http.impl.conn.ManagedHttpClientConnectionFactory",
      "org.apache.http.impl.conn.HttpClientConnectionOperator",
      "org.apache.http.impl.conn.DefaultSchemePortResolver",
      "org.apache.http.impl.conn.SystemDefaultDnsResolver",
      "org.apache.http.util.VersionInfo",
      "org.apache.http.impl.client.HttpClientBuilder",
      "org.apache.http.protocol.HttpRequestExecutor",
      "org.apache.http.impl.DefaultConnectionReuseStrategy",
      "org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy",
      "org.apache.http.impl.client.AuthenticationStrategyImpl",
      "org.apache.http.impl.client.TargetAuthenticationStrategy",
      "org.apache.http.impl.client.ProxyAuthenticationStrategy",
      "org.apache.http.impl.client.DefaultUserTokenHandler",
      "org.apache.http.client.protocol.RequestClientConnControl",
      "org.apache.http.client.protocol.ResponseContentEncoding",
      "org.apache.http.impl.client.DefaultHttpRequestRetryHandler",
      "org.apache.http.impl.cookie.BrowserCompatSpecFactory$SecurityLevel",
      "org.apache.http.impl.client.BasicCookieStore",
      "org.apache.http.cookie.CookieIdentityComparator",
      "org.apache.http.client.config.RequestConfig",
      "org.apache.http.client.methods.HttpPut",
      "org.apache.http.message.HeaderGroup",
      "org.apache.http.message.BasicHeader",
      "org.apache.http.entity.AbstractHttpEntity",
      "org.apache.http.Consts",
      "org.apache.http.entity.ContentType",
      "org.apache.http.util.CharArrayBuffer",
      "org.apache.http.params.BasicHttpParams",
      "org.apache.http.message.BasicRequestLine",
      "org.apache.http.protocol.HttpCoreContext",
      "org.apache.http.client.protocol.HttpClientContext",
      "org.apache.http.auth.AuthProtocolState",
      "org.apache.http.client.methods.HttpPost",
      "org.apache.http.HttpHost",
      "org.apache.http.client.methods.HttpGet",
      "org.apache.http.client.methods.HttpPatch",
      "org.openecomp.mso.rest.RESTException",
      "org.apache.http.client.ClientProtocolException",
      "org.apache.http.protocol.HTTP",
      "org.apache.http.message.BasicStatusLine",
      "org.apache.http.util.ByteArrayBuffer"
    );
  }
}
