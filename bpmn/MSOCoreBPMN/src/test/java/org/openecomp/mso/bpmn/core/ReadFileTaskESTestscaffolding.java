/**
 * Scaffolding file used to store all the setups needed to run 
 * tests automatically generated by EvoSuite
 * Mon Nov 14 11:30:51 GMT 2016
 */

package org.openecomp.mso.bpmn.core;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import org.junit.AfterClass;
import org.evosuite.runtime.sandbox.Sandbox;

@EvoSuiteClassExclude
public class ReadFileTaskESTestscaffolding {

  @org.junit.Rule 
  public org.evosuite.runtime.vnet.NonFunctionalRequirementRule nfr = new org.evosuite.runtime.vnet.NonFunctionalRequirementRule();

  private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone(); 

  private org.evosuite.runtime.thread.ThreadStopper threadStopper =  new org.evosuite.runtime.thread.ThreadStopper (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(), 3000);

  @BeforeClass 
  public static void initEvoSuiteFramework() { 
    org.evosuite.runtime.RuntimeSettings.className = "org.openecomp.mso.bpmn.core.ReadFileTask"; 
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
    org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(ReadFileTaskESTestscaffolding.class.getClassLoader() ,
      "org.camunda.bpm.engine.impl.pvm.process.TransitionImpl",
      "org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution",
      "org.camunda.bpm.engine.impl.context.CaseExecutionContext",
      "org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformFactory",
      "org.camunda.bpm.engine.impl.pvm.PvmScope",
      "org.camunda.bpm.engine.delegate.JavaDelegate",
      "org.camunda.bpm.engine.impl.tree.TreeWalker$WalkCondition",
      "org.camunda.bpm.engine.runtime.ProcessInstance",
      "org.camunda.bpm.application.ProcessApplicationReference",
      "org.camunda.bpm.engine.delegate.BpmnModelExecutionContext",
      "org.camunda.bpm.engine.impl.jobexecutor.FailedJobCommandFactory",
      "org.openecomp.mso.bpmn.core.ReadFileTask",
      "org.camunda.bpm.engine.delegate.DelegateExecution",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnIfPartDeclaration",
      "org.camunda.bpm.engine.delegate.CmmnModelExecutionContext",
      "org.camunda.bpm.engine.impl.persistence.entity.util.FormPropertyStartContext",
      "com.att.eelf.i18n.EELFMsgs",
      "org.camunda.bpm.engine.impl.javax.el.ELContext",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityInstanceStart",
      "org.camunda.bpm.engine.impl.core.variable.event.VariableEventDispatcher",
      "org.camunda.bpm.engine.variable.type.ValueType",
      "org.camunda.bpm.engine.impl.interceptor.CommandContextListener",
      "org.camunda.bpm.engine.impl.core.variable.mapping.IoMapping",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationInterruptScope",
      "org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance",
      "org.camunda.bpm.engine.impl.db.HasDbRevision",
      "org.camunda.bpm.model.bpmn.instance.FlowElement",
      "org.camunda.bpm.engine.delegate.Expression",
      "org.camunda.bpm.engine.variable.type.SerializableValueType",
      "org.camunda.bpm.engine.impl.pvm.PvmActivity",
      "org.camunda.bpm.engine.impl.cfg.BpmnParseFactory",
      "org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance",
      "org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance",
      "org.camunda.bpm.engine.impl.pvm.PvmException",
      "org.camunda.bpm.engine.impl.core.variable.VariableMapImpl",
      "org.camunda.bpm.application.ProcessApplicationUnavailableException",
      "org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition",
      "org.camunda.bpm.engine.delegate.DelegateCaseExecution",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionNotifyListenerTake",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityNotifyListenerEnd",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnOnPartDeclaration",
      "org.camunda.bpm.engine.runtime.CaseExecution",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition",
      "org.camunda.bpm.engine.impl.core.variable.scope.SimpleVariableStore",
      "org.camunda.bpm.engine.repository.ProcessDefinition",
      "org.camunda.bpm.engine.impl.pvm.process.ScopeImpl",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionNotifyListenerStart",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityStartConcurrent",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.AbstractPvmAtomicOperationTransitionNotifyListenerTake",
      "org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl",
      "org.openecomp.mso.logger.MsoLogger$ErrorCode",
      "org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation",
      "org.camunda.bpm.engine.impl.interceptor.SessionFactory",
      "org.camunda.bpm.engine.delegate.DelegateTask",
      "org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation",
      "org.camunda.bpm.engine.impl.core.model.CoreActivity",
      "org.camunda.bpm.model.bpmn.BpmnModelInstance",
      "org.camunda.bpm.engine.delegate.VariableScope",
      "org.camunda.bpm.engine.ProcessEngine",
      "org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl",
      "org.camunda.bpm.engine.impl.interceptor.CommandContext",
      "org.camunda.bpm.engine.impl.variable.listener.CaseVariableListenerInvocation",
      "org.openecomp.mso.logger.MsoLogger$Catalog",
      "org.camunda.bpm.engine.BadUserRequestException",
      "org.camunda.bpm.engine.impl.context.ExecutionContext",
      "org.camunda.bpm.engine.impl.persistence.entity.TaskEntity",
      "org.camunda.bpm.engine.impl.context.Context",
      "org.camunda.bpm.engine.ProcessEngineServices",
      "org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl",
      "org.camunda.bpm.engine.runtime.Execution",
      "org.camunda.bpm.engine.impl.pvm.process.Lane",
      "org.camunda.bpm.engine.impl.pvm.process.ParticipantProcess",
      "org.camunda.bpm.engine.impl.interceptor.CommandExecutor",
      "org.camunda.bpm.engine.delegate.ProcessEngineServicesAware",
      "org.camunda.bpm.engine.ProcessEngineConfiguration",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity",
      "org.camunda.bpm.engine.impl.util.BitMaskUtil",
      "org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition",
      "org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior",
      "org.camunda.bpm.engine.impl.core.variable.event.VariableEvent",
      "org.camunda.bpm.engine.delegate.BaseDelegateExecution",
      "org.camunda.bpm.engine.impl.pvm.PvmExecution",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityStartCreateScope",
      "org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity",
      "org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableStore",
      "org.camunda.bpm.engine.runtime.VariableInstance",
      "org.camunda.bpm.engine.runtime.Job",
      "org.camunda.bpm.engine.runtime.Incident",
      "org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState$CaseExecutionStateImpl",
      "org.camunda.bpm.model.xml.ModelInstance",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityStart",
      "org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl",
      "org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity",
      "org.openecomp.mso.entity.MsoRequest",
      "org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl",
      "org.openecomp.mso.bpmn.core.BadInjectedFieldException",
      "org.camunda.bpm.engine.impl.variable.serializer.ValueFields",
      "org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation",
      "org.camunda.bpm.engine.impl.task.TaskDecorator",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityInitStackNotifyListenerStart",
      "org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext",
      "org.camunda.bpm.engine.impl.task.delegate.TaskListenerInvocation",
      "org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationsTransitionInterruptFlowScope",
      "org.camunda.bpm.model.xml.instance.ModelElementInstance",
      "org.camunda.bpm.engine.ProcessEngineException",
      "org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration",
      "org.camunda.bpm.engine.impl.persistence.entity.SuspensionState$SuspensionStateImpl",
      "org.camunda.bpm.engine.impl.db.DbEntity",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionNotifyListenerEnd",
      "org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationFireActivityEnd",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationStartTransitionNotifyListenerTake",
      "org.camunda.bpm.engine.runtime.EventSubscription",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionDestroyScope",
      "org.camunda.bpm.engine.impl.core.variable.value.UntypedValueImpl",
      "org.camunda.bpm.engine.management.JobDefinition",
      "com.att.eelf.i18n.EELFResolvableErrorEnum",
      "com.att.eelf.configuration.EELFLogger$Level",
      "org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior",
      "org.camunda.bpm.engine.impl.core.variable.scope.MapBasedVariableStore",
      "org.camunda.bpm.engine.impl.javax.el.ValueReference",
      "org.camunda.bpm.engine.impl.persistence.entity.JobEntity",
      "org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext",
      "org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance",
      "org.camunda.bpm.engine.impl.pvm.PvmProcessElement",
      "org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntityVariableStore",
      "com.att.eelf.i18n.EELFResourceManager$RESOURCE_TYPES",
      "org.camunda.bpm.engine.impl.delegate.DelegateInvocation",
      "org.camunda.bpm.engine.impl.javax.el.ValueExpression",
      "com.att.eelf.configuration.EELFLogger",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationFireProcessStart",
      "org.camunda.bpm.engine.runtime.CaseInstance",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationCreateScope",
      "org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware",
      "org.camunda.bpm.engine.impl.pvm.process.ActivityImpl",
      "org.camunda.bpm.engine.impl.core.model.CoreModelElement",
      "org.camunda.bpm.engine.impl.interceptor.CommandInterceptor",
      "org.camunda.bpm.engine.impl.pvm.PvmProcessInstance",
      "org.camunda.bpm.engine.impl.cmmn.execution.CaseSentryPartImpl",
      "org.camunda.bpm.engine.variable.value.TypedValue",
      "org.apache.ibatis.transaction.TransactionFactory",
      "org.camunda.bpm.engine.impl.cmmn.execution.CmmnSentryPart",
      "org.camunda.bpm.engine.impl.pvm.process.LaneSet",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityInitStack",
      "org.camunda.bpm.engine.delegate.DelegateListener",
      "org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution",
      "org.camunda.bpm.engine.delegate.ExecutionListener",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationDeleteCascadeFireActivityEnd",
      "org.camunda.bpm.engine.exception.NotAllowedException",
      "org.camunda.bpm.engine.impl.history.event.HistoryEventType",
      "org.openecomp.mso.bpmn.core.MissingInjectedFieldException",
      "org.camunda.bpm.engine.impl.pvm.process.HasDIBounds",
      "com.att.eelf.configuration.SLF4jWrapper",
      "org.camunda.bpm.engine.task.Task",
      "org.camunda.bpm.engine.impl.javax.el.Expression",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityEnd",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.AbstractPvmEventAtomicOperation",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityInstanceEnd",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationDeleteCascade",
      "org.camunda.bpm.engine.impl.context.CoreExecutionContext",
      "org.camunda.bpm.engine.delegate.DelegateVariableInstance",
      "org.openecomp.mso.logger.MsoLogger",
      "org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope",
      "org.camunda.bpm.model.cmmn.CmmnModelInstance",
      "org.camunda.bpm.engine.variable.VariableMap",
      "org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState$ActivityInstanceStateImpl",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationProcessEnd",
      "org.camunda.bpm.engine.impl.interceptor.CommandExecutorImpl",
      "org.camunda.bpm.model.bpmn.instance.BaseElement",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationCreateConcurrentExecution",
      "org.camunda.bpm.engine.exception.NullValueException",
      "org.camunda.bpm.engine.impl.core.variable.scope.CoreVariableStore",
      "org.camunda.bpm.engine.impl.context.BpmnExecutionContext",
      "org.openecomp.mso.bpmn.core.BaseTask",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityStartInterruptEventScope",
      "org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity",
      "org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity",
      "org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityStartCancelScope",
      "org.apache.ibatis.session.SqlSessionFactory",
      "org.camunda.bpm.engine.impl.variable.AbstractPersistentVariableStore",
      "org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext",
      "com.att.eelf.i18n.EELFResourceManager",
      "org.camunda.bpm.engine.impl.core.delegate.CoreActivityBehavior",
      "org.openecomp.mso.logger.MessageEnum",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionCreateScope",
      "org.openecomp.mso.logger.MsoLogger$ResponseCode",
      "org.camunda.bpm.engine.SuspendedEntityInteractionException",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationCancelActivity",
      "org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState",
      "org.openecomp.mso.logger.MsoLogger$StatusCode",
      "org.camunda.bpm.engine.impl.db.HasDbReferences",
      "org.camunda.bpm.engine.impl.tree.Collector",
      "com.att.eelf.configuration.EELFManager",
      "org.camunda.bpm.engine.impl.pvm.runtime.ExecutionStartContext",
      "org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation",
      "org.camunda.bpm.engine.impl.pvm.PvmTransition",
      "org.camunda.bpm.engine.impl.persistence.entity.SuspensionState",
      "org.camunda.bpm.model.cmmn.instance.CmmnElement",
      "org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration",
      "org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity",
      "com.att.eelf.i18n.EELFResourceManager$1",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationProcessStart",
      "org.camunda.bpm.engine.impl.core.instance.CoreExecution",
      "org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance",
      "org.camunda.bpm.engine.variable.type.PrimitiveValueType",
      "org.camunda.bpm.engine.delegate.VariableListener",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityExecute",
      "org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl$1"
    );
  } 

  private static void resetClasses() {
    org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(ReadFileTaskESTestscaffolding.class.getClassLoader());

    org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
      "org.openecomp.mso.logger.MsoLogger$Catalog",
      "org.openecomp.mso.logger.MsoLogger",
      "com.att.eelf.i18n.EELFResourceManager",
      "com.att.eelf.i18n.EELFMsgs",
      "com.att.eelf.i18n.EELFResourceManager$RESOURCE_TYPES",
      "com.att.eelf.configuration.EELFLogger$Level",
      "com.att.eelf.configuration.EELFManager",
      "org.openecomp.mso.logger.MessageEnum",
      "org.openecomp.mso.bpmn.core.ReadFileTask",
      "org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope",
      "org.camunda.bpm.engine.impl.core.instance.CoreExecution",
      "org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl",
      "org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity",
      "org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState",
      "org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntityVariableStore",
      "org.camunda.bpm.engine.impl.persistence.entity.SuspensionState",
      "org.camunda.bpm.engine.impl.javax.el.Expression",
      "org.camunda.bpm.engine.impl.javax.el.ValueExpression",
      "org.camunda.bpm.engine.impl.context.Context",
      "org.camunda.bpm.engine.ProcessEngineException",
      "org.openecomp.mso.bpmn.core.BadInjectedFieldException",
      "org.camunda.bpm.engine.exception.NullValueException",
      "org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl",
      "org.openecomp.mso.bpmn.core.MissingInjectedFieldException",
      "org.camunda.bpm.engine.impl.core.model.CoreModelElement",
      "org.camunda.bpm.engine.impl.core.model.CoreActivity",
      "org.camunda.bpm.engine.impl.pvm.process.ScopeImpl",
      "org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity",
      "org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition",
      "org.camunda.bpm.engine.impl.pvm.PvmException",
      "org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution",
      "org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl",
      "org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState",
      "org.camunda.bpm.engine.impl.core.variable.VariableMapImpl",
      "org.camunda.bpm.engine.impl.pvm.process.ActivityImpl",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityInstanceEnd",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationProcessEnd",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationCreateScope",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationActivityExecute",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperationTransitionDestroyScope",
      "org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation",
      "org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl$1",
      "org.camunda.bpm.engine.impl.util.BitMaskUtil",
      "org.camunda.bpm.engine.impl.pvm.process.TransitionImpl",
      "org.camunda.bpm.engine.impl.core.variable.value.UntypedValueImpl"
    );
  }
}
