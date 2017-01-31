/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:38:17 GMT 2016
 */

package org.openecomp.mso.adapters.network.async.client;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.xml.bind.JAXBElement;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class ObjectFactoryESTest extends ObjectFactoryESTestscaffolding {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      MsoRequest msoRequest0 = objectFactory0.createMsoRequest();
      assertNull(msoRequest0.getServiceInstanceId());
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      NetworkRollback networkRollback0 = objectFactory0.createNetworkRollback();
      assertNull(networkRollback0.getPhysicalNetwork());
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      CreateNetworkNotification.SubnetIdMap.Entry createNetworkNotification_SubnetIdMap_Entry0 = objectFactory0.createCreateNetworkNotificationSubnetIdMapEntry();
      assertNull(createNetworkNotification_SubnetIdMap_Entry0.getKey());
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      CreateNetworkNotification.SubnetIdMap createNetworkNotification_SubnetIdMap0 = objectFactory0.createCreateNetworkNotificationSubnetIdMap();
      assertNotNull(createNetworkNotification_SubnetIdMap0);
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      UpdateNetworkNotification.SubnetIdMap.Entry updateNetworkNotification_SubnetIdMap_Entry0 = objectFactory0.createUpdateNetworkNotificationSubnetIdMapEntry();
      assertNull(updateNetworkNotification_SubnetIdMap_Entry0.getKey());
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      UpdateNetworkNotification.SubnetIdMap updateNetworkNotification_SubnetIdMap0 = objectFactory0.createUpdateNetworkNotificationSubnetIdMap();
      assertNotNull(updateNetworkNotification_SubnetIdMap0);
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      QueryNetworkNotification.SubnetIdMap.Entry queryNetworkNotification_SubnetIdMap_Entry0 = objectFactory0.createQueryNetworkNotificationSubnetIdMapEntry();
      assertNull(queryNetworkNotification_SubnetIdMap_Entry0.getKey());
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      DeleteNetworkNotification deleteNetworkNotification0 = objectFactory0.createDeleteNetworkNotification();
      JAXBElement<DeleteNetworkNotification> jAXBElement0 = objectFactory0.createDeleteNetworkNotification(deleteNetworkNotification0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      QueryNetworkNotification.SubnetIdMap queryNetworkNotification_SubnetIdMap0 = objectFactory0.createQueryNetworkNotificationSubnetIdMap();
      assertNotNull(queryNetworkNotification_SubnetIdMap0);
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      DeleteNetworkNotificationResponse deleteNetworkNotificationResponse0 = objectFactory0.createDeleteNetworkNotificationResponse();
      JAXBElement<DeleteNetworkNotificationResponse> jAXBElement0 = objectFactory0.createDeleteNetworkNotificationResponse(deleteNetworkNotificationResponse0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      RollbackNetworkNotificationResponse rollbackNetworkNotificationResponse0 = objectFactory0.createRollbackNetworkNotificationResponse();
      JAXBElement<RollbackNetworkNotificationResponse> jAXBElement0 = objectFactory0.createRollbackNetworkNotificationResponse(rollbackNetworkNotificationResponse0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      CreateNetworkNotificationResponse createNetworkNotificationResponse0 = objectFactory0.createCreateNetworkNotificationResponse();
      JAXBElement<CreateNetworkNotificationResponse> jAXBElement0 = objectFactory0.createCreateNetworkNotificationResponse(createNetworkNotificationResponse0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      RollbackNetworkNotification rollbackNetworkNotification0 = objectFactory0.createRollbackNetworkNotification();
      JAXBElement<RollbackNetworkNotification> jAXBElement0 = objectFactory0.createRollbackNetworkNotification(rollbackNetworkNotification0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      UpdateNetworkNotification updateNetworkNotification0 = objectFactory0.createUpdateNetworkNotification();
      JAXBElement<UpdateNetworkNotification> jAXBElement0 = objectFactory0.createUpdateNetworkNotification(updateNetworkNotification0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      QueryNetworkNotification queryNetworkNotification0 = objectFactory0.createQueryNetworkNotification();
      JAXBElement<QueryNetworkNotification> jAXBElement0 = objectFactory0.createQueryNetworkNotification(queryNetworkNotification0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test15()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      UpdateNetworkNotificationResponse updateNetworkNotificationResponse0 = objectFactory0.createUpdateNetworkNotificationResponse();
      JAXBElement<UpdateNetworkNotificationResponse> jAXBElement0 = objectFactory0.createUpdateNetworkNotificationResponse(updateNetworkNotificationResponse0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test16()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      QueryNetworkNotificationResponse queryNetworkNotificationResponse0 = objectFactory0.createQueryNetworkNotificationResponse();
      JAXBElement<QueryNetworkNotificationResponse> jAXBElement0 = objectFactory0.createQueryNetworkNotificationResponse(queryNetworkNotificationResponse0);
      assertNotNull(jAXBElement0);
  }

  @Test(timeout = 4000)
  public void test17()  throws Throwable  {
      ObjectFactory objectFactory0 = new ObjectFactory();
      CreateNetworkNotification createNetworkNotification0 = objectFactory0.createCreateNetworkNotification();
      JAXBElement<CreateNetworkNotification> jAXBElement0 = objectFactory0.createCreateNetworkNotification(createNetworkNotification0);
      assertNotNull(jAXBElement0);
  }
}
