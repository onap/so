/**
 * Scaffolding file used to store all the setups needed to run
 * tests automatically generated by EvoSuite
 * Mon Mar 13 15:55:23 GMT 2017
 */

package org.openecomp.mso.asdc.util;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.AfterClass;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.sandbox.Sandbox.SandboxMode;

@EvoSuiteClassExclude
public class ASDCNotificationLoggingESTestscaffolding {

  @org.junit.Rule
  public org.evosuite.runtime.vnet.NonFunctionalRequirementRule nfr = new org.evosuite.runtime.vnet.NonFunctionalRequirementRule();

  private static final java.util.Properties defaultProperties = (java.util.Properties) System.getProperties().clone();

  private org.evosuite.runtime.thread.ThreadStopper threadStopper =  new org.evosuite.runtime.thread.ThreadStopper (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(), 3000);

  @BeforeClass
  public static void initEvoSuiteFramework() {
    org.evosuite.runtime.RuntimeSettings.className = "org.openecomp.mso.asdc.util.ASDCNotificationLogging";
    org.evosuite.runtime.GuiSupport.initialize();
    org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100;
    org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000;
    org.evosuite.runtime.RuntimeSettings.mockSystemIn = true;
    org.evosuite.runtime.RuntimeSettings.sandboxMode = SandboxMode.RECOMMENDED;
    Sandbox.initializeSecurityManagerForSUT();
    org.evosuite.runtime.classhandling.JDKClassResetter.init();
    initializeClasses();
    org.evosuite.runtime.Runtime.getInstance().resetRuntime();
  }

  @AfterClass
  public static void clearEvoSuiteFramework(){
    Sandbox.resetDefaultSecurityManager();
    System.setProperties((java.util.Properties) defaultProperties.clone());
  }

  @Before
  public void initTestCase(){
    threadStopper.storeCurrentThreads();
    threadStopper.startRecordingTime();
    org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().initHandler();
    Sandbox.goingToExecuteSUTCode();
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
    Sandbox.doneWithExecutingSUTCode();
    org.evosuite.runtime.agent.InstrumentingAgent.deactivate();
    org.evosuite.runtime.GuiSupport.restoreHeadlessMode();
  }

  private static void initializeClasses() {
    org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(ASDCNotificationLoggingESTestscaffolding.class.getClassLoader() ,
      "org.openecomp.sdc.api.notification.INotificationData",
      "org.openecomp.mso.asdc.installer.VfModuleMetaData",
      "org.openecomp.mso.asdc.installer.IVfModuleData",
      "org.openecomp.sdc.api.notification.IResourceInstance",
      "org.openecomp.sdc.api.notification.IArtifactInfo",
      "org.openecomp.mso.asdc.util.ASDCNotificationLogging"
    );
  }

  private static void resetClasses() {
  }
}
