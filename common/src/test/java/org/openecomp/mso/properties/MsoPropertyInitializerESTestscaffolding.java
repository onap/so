/**
 * Scaffolding file used to store all the setups needed to run 
 * tests automatically generated by EvoSuite
 * Mon Nov 14 08:01:07 GMT 2016
 */

package org.openecomp.mso.properties;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.AfterClass;
import org.evosuite.runtime.sandbox.Sandbox;

@EvoSuiteClassExclude
public class MsoPropertyInitializerESTestscaffolding {

  @org.junit.Rule 
  public org.evosuite.runtime.vnet.NonFunctionalRequirementRule nfr = new org.evosuite.runtime.vnet.NonFunctionalRequirementRule();

  private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone(); 

  private org.evosuite.runtime.thread.ThreadStopper threadStopper =  new org.evosuite.runtime.thread.ThreadStopper (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(), 3000);

  @BeforeClass 
  public static void initEvoSuiteFramework() { 
    org.evosuite.runtime.RuntimeSettings.className = "org.openecomp.mso.properties.MsoPropertyInitializer"; 
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
    org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(MsoPropertyInitializerESTestscaffolding.class.getClassLoader() ,
      "org.openecomp.mso.properties.AbstractMsoProperties",
      "org.apache.xerces.xni.parser.XMLDTDContentModelFilter",
      "org.apache.xerces.xs.PSVIProvider",
      "org.apache.xerces.impl.XMLEntityManager$ByteBufferPool",
      "org.apache.xerces.impl.dtd.XMLEntityDecl",
      "org.apache.xerces.xs.ItemPSVI",
      "org.apache.xerces.xni.parser.XMLEntityResolver",
      "org.apache.xerces.impl.dtd.XMLNSDTDValidator",
      "org.apache.xerces.impl.XMLDTDScannerImpl",
      "org.apache.xerces.parsers.ObjectFactory",
      "org.apache.xerces.xni.parser.XMLConfigurationException",
      "org.apache.xerces.impl.XML11NSDocumentScannerImpl",
      "org.apache.xerces.impl.XMLEntityManager$CharacterBufferPool",
      "org.apache.xerces.parsers.SAXParser",
      "com.att.eelf.i18n.EELFResolvableErrorEnum",
      "org.apache.xerces.xni.XMLResourceIdentifier",
      "org.apache.xerces.impl.XMLEntityManager$RewindableInputStream",
      "org.apache.xerces.impl.XMLEntityManager",
      "org.apache.xerces.impl.dtd.XMLDTDDescription",
      "org.apache.xerces.xni.parser.XMLInputSource",
      "com.att.eelf.i18n.EELFMsgs",
      "org.apache.xerces.xni.parser.XMLComponentManager",
      "com.att.eelf.configuration.EELFLogger$Level",
      "org.apache.xerces.impl.io.UTF8Reader",
      "org.apache.xerces.impl.dv.InvalidDatatypeValueException",
      "org.apache.xerces.jaxp.UnparsedEntityHandler",
      "org.apache.xerces.parsers.AbstractXMLDocumentParser",
      "org.apache.xerces.impl.XMLScanner",
      "org.apache.xerces.impl.dtd.XMLSimpleType",
      "org.apache.xerces.impl.XML11DocumentScannerImpl",
      "org.apache.xerces.xni.parser.XMLParseException",
      "org.apache.xerces.util.AugmentationsImpl$AugmentationsItemsContainer",
      "org.apache.xerces.impl.XMLEntityScanner",
      "org.apache.xerces.util.URI$MalformedURIException",
      "org.apache.xerces.util.XMLChar",
      "org.apache.xerces.impl.XMLNSDocumentScannerImpl",
      "org.apache.xerces.impl.XML11DTDScannerImpl",
      "org.apache.xerces.util.URI",
      "org.apache.xerces.xni.parser.XMLDocumentFilter",
      "org.apache.xerces.xni.parser.XMLDTDSource",
      "org.apache.xerces.impl.dtd.XMLElementDecl",
      "org.apache.xerces.impl.dtd.XMLAttributeDecl",
      "com.att.eelf.i18n.EELFResourceManager$RESOURCE_TYPES",
      "org.apache.xerces.xni.grammars.Grammar",
      "org.apache.xerces.parsers.XMLParser",
      "org.apache.xerces.impl.dtd.models.ContentModelValidator",
      "com.att.eelf.configuration.EELFLogger",
      "org.apache.xerces.xni.grammars.XMLGrammarLoader",
      "org.apache.xerces.xni.XMLDocumentHandler",
      "org.openecomp.mso.properties.MsoJavaProperties",
      "org.apache.xerces.util.SymbolTable",
      "org.apache.xerces.impl.io.UCSReader",
      "org.apache.xerces.impl.XMLEntityManager$CharacterBuffer",
      "org.apache.xerces.impl.io.Latin1Reader",
      "org.apache.xerces.impl.dv.ValidationContext",
      "org.apache.xerces.impl.dtd.XMLDTDValidator",
      "org.apache.xerces.impl.dtd.XML11NSDTDValidator",
      "org.apache.xerces.impl.validation.ValidationState",
      "org.apache.xerces.impl.XMLEntityManager$Entity",
      "org.apache.xerces.util.XMLResourceIdentifierImpl",
      "org.apache.xerces.util.AugmentationsImpl",
      "org.apache.xerces.impl.dv.ObjectFactory",
      "org.apache.xerces.impl.dv.DatatypeValidator",
      "org.apache.xerces.xni.NamespaceContext",
      "org.apache.xerces.impl.dtd.XMLDTDLoader",
      "org.apache.xerces.jaxp.SAXParserImpl",
      "org.apache.xerces.util.XMLSymbols",
      "org.apache.xerces.parsers.ObjectFactory$ConfigurationError",
      "org.apache.xerces.xni.grammars.XMLGrammarDescription",
      "org.apache.xerces.xni.parser.XMLErrorHandler",
      "org.apache.xerces.impl.io.ASCIIReader",
      "org.apache.xerces.util.MessageFormatter",
      "org.openecomp.mso.properties.MsoPropertiesParameters",
      "org.apache.xerces.impl.dtd.XMLDTDProcessor",
      "org.apache.xerces.impl.XMLDocumentFragmentScannerImpl",
      "org.apache.xerces.xni.parser.XMLDTDScanner",
      "org.openecomp.mso.logger.MsoLogger$ErrorCode",
      "org.apache.xerces.xni.XMLAttributes",
      "org.apache.xerces.impl.io.MalformedByteSequenceException",
      "org.apache.xerces.impl.Constants$ArrayEnumeration",
      "org.apache.xerces.impl.XML11EntityScanner",
      "org.apache.xerces.impl.dtd.DTDGrammar",
      "org.apache.xerces.impl.dv.DTDDVFactory",
      "com.att.eelf.configuration.SLF4jWrapper",
      "org.openecomp.mso.properties.MsoPropertiesException",
      "org.apache.xerces.impl.validation.ValidationManager",
      "org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl",
      "org.apache.xerces.xni.XNIException",
      "org.apache.xerces.impl.dtd.XMLContentSpec",
      "org.openecomp.mso.logger.MsoLogger",
      "org.apache.xerces.xs.AttributePSVI",
      "org.apache.xerces.impl.dtd.DTDGrammarBucket",
      "org.apache.xerces.impl.msg.XMLMessageFormatter",
      "org.apache.xerces.xni.parser.XMLDocumentScanner",
      "org.apache.xerces.impl.XMLVersionDetector",
      "org.apache.xerces.impl.XMLDocumentScannerImpl",
      "org.apache.xerces.xni.parser.XMLPullParserConfiguration",
      "org.apache.xerces.xni.parser.XMLDocumentSource",
      "org.apache.xerces.impl.XMLDocumentFragmentScannerImpl$Dispatcher",
      "org.openecomp.mso.properties.MsoPropertiesFactory",
      "org.apache.xerces.xni.XMLDTDContentModelHandler",
      "org.apache.xerces.impl.xs.XMLSchemaValidator",
      "org.openecomp.mso.logger.MsoLogger$Catalog",
      "org.apache.xerces.xni.grammars.XMLDTDDescription",
      "org.apache.xerces.util.AugmentationsImpl$SmallContainer",
      "org.apache.xerces.impl.XMLErrorReporter",
      "org.apache.xerces.xni.QName",
      "org.apache.xerces.jaxp.TeeXMLDocumentFilterImpl",
      "org.apache.xerces.util.XMLAttributesImpl",
      "org.apache.xerces.impl.Constants",
      "org.apache.xerces.util.XMLStringBuffer",
      "org.apache.xerces.impl.XMLEntityManager$InternalEntity",
      "org.apache.xerces.jaxp.JAXPConstants",
      "org.openecomp.mso.properties.MsoPropertiesParameters$MsoPropertiesType",
      "org.apache.xerces.impl.RevalidationHandler",
      "org.apache.xerces.xni.parser.XMLParserConfiguration",
      "org.apache.xerces.xni.XMLString",
      "org.apache.xerces.impl.dv.DVFactoryException",
      "org.apache.xerces.impl.dv.DatatypeException",
      "org.apache.xerces.parsers.XML11Configurable",
      "org.apache.xerces.util.AugmentationsImpl$LargeContainer",
      "org.apache.xerces.impl.dtd.BalancedDTDGrammar",
      "org.apache.xerces.parsers.XIncludeAwareParserConfiguration",
      "org.apache.xerces.xni.XMLDTDHandler",
      "org.apache.xerces.impl.dtd.XML11DTDProcessor",
      "org.apache.xerces.parsers.XML11Configuration",
      "org.apache.xerces.impl.dtd.XMLDTDValidatorFilter",
      "org.apache.xerces.impl.xs.identity.FieldActivator",
      "org.apache.xerces.impl.XMLEntityScanner$1",
      "com.att.eelf.i18n.EELFResourceManager",
      "org.apache.xerces.jaxp.SAXParserFactoryImpl",
      "org.apache.xerces.xs.ElementPSVI",
      "org.apache.xerces.parsers.AbstractSAXParser",
      "org.apache.xerces.xni.parser.XMLDTDFilter",
      "org.apache.xerces.xni.parser.XMLDTDContentModelSource",
      "org.openecomp.mso.logger.MessageEnum",
      "org.openecomp.mso.logger.MsoLogger$ResponseCode",
      "org.openecomp.mso.properties.MsoJsonProperties",
      "org.openecomp.mso.entity.MsoRequest",
      "org.openecomp.mso.properties.MsoPropertyInitializer",
      "org.openecomp.mso.logger.MsoLogger$StatusCode",
      "org.apache.xerces.xni.XMLLocator",
      "com.att.eelf.configuration.EELFManager",
      "org.apache.xerces.impl.validation.EntityState",
      "org.apache.xerces.impl.XMLEntityManager$ExternalEntity",
      "org.apache.xerces.util.ParserConfigurationSettings",
      "org.apache.xerces.jaxp.SAXParserImpl$JAXPSAXParser",
      "org.apache.xerces.xni.Augmentations",
      "org.apache.xerces.impl.XMLEntityHandler",
      "org.apache.xerces.impl.dv.ObjectFactory$ConfigurationError",
      "org.apache.xerces.xni.parser.XMLComponent",
      "com.att.eelf.i18n.EELFResourceManager$1",
      "org.apache.xerces.impl.dtd.XML11DTDValidator",
      "org.apache.xerces.impl.XMLEntityManager$ScannedEntity",
      "org.apache.xerces.jaxp.JAXPValidatorComponent"
    );
  } 

  private static void resetClasses() {
    org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(MsoPropertyInitializerESTestscaffolding.class.getClassLoader());

    org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
      "com.att.eelf.i18n.EELFResourceManager",
      "org.openecomp.mso.logger.MessageEnum",
      "org.openecomp.mso.logger.MsoLogger$Catalog",
      "org.openecomp.mso.logger.MsoLogger$ErrorCode",
      "org.openecomp.mso.logger.MsoLogger",
      "com.att.eelf.i18n.EELFMsgs",
      "com.att.eelf.i18n.EELFResourceManager$RESOURCE_TYPES",
      "org.apache.xerces.jaxp.SAXParserFactoryImpl",
      "org.apache.xerces.jaxp.SAXParserImpl",
      "org.apache.xerces.parsers.XMLParser",
      "org.apache.xerces.parsers.AbstractSAXParser",
      "org.apache.xerces.parsers.SAXParser",
      "org.apache.xerces.parsers.ObjectFactory",
      "org.apache.xerces.util.ParserConfigurationSettings",
      "org.apache.xerces.parsers.XML11Configuration",
      "org.apache.xerces.parsers.XIncludeAwareParserConfiguration",
      "org.apache.xerces.util.SymbolTable",
      "org.apache.xerces.impl.XMLEntityManager",
      "org.apache.xerces.util.AugmentationsImpl$SmallContainer",
      "org.apache.xerces.impl.XMLEntityManager$ByteBufferPool",
      "org.apache.xerces.impl.XMLEntityManager$CharacterBufferPool",
      "org.apache.xerces.impl.XMLEntityScanner$1",
      "org.apache.xerces.impl.XMLEntityScanner",
      "org.apache.xerces.impl.XMLErrorReporter",
      "org.apache.xerces.impl.XMLScanner",
      "org.apache.xerces.impl.XMLDocumentFragmentScannerImpl",
      "org.apache.xerces.impl.XMLDocumentScannerImpl",
      "org.apache.xerces.util.XMLStringBuffer",
      "org.apache.xerces.util.XMLAttributesImpl",
      "org.apache.xerces.impl.XMLDTDScannerImpl",
      "org.apache.xerces.impl.dtd.XMLDTDProcessor",
      "org.apache.xerces.impl.dtd.XMLDTDValidator",
      "org.apache.xerces.impl.validation.ValidationState",
      "org.apache.xerces.impl.dtd.XMLElementDecl",
      "org.apache.xerces.impl.dtd.XMLSimpleType",
      "org.apache.xerces.impl.dv.DTDDVFactory",
      "org.apache.xerces.impl.dv.ObjectFactory",
      "org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl",
      "org.apache.xerces.impl.XMLVersionDetector",
      "org.apache.xerces.impl.msg.XMLMessageFormatter",
      "org.apache.xerces.impl.io.UTF8Reader",
      "org.apache.xerces.util.XMLSymbols",
      "org.apache.xerces.xni.NamespaceContext",
      "org.apache.xerces.util.XMLChar",
      "org.apache.xerces.impl.Constants",
      "com.att.eelf.configuration.EELFLogger$Level",
      "com.att.eelf.configuration.EELFManager",
      "org.openecomp.mso.properties.MsoPropertiesFactory"
    );
  }
}
