/*
 * This file was automatically generated by EvoSuite
 * Mon Mar 13 16:09:00 GMT 2017
 */

package org.openecomp.mso.asdc.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.shaded.org.mockito.Mockito.*;
import static org.evosuite.runtime.MockitoExtension.*;
import static org.evosuite.runtime.EvoAssertions.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.PrivateAccess;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.junit.runner.RunWith;
import org.openecomp.mso.asdc.installer.IVfModuleData;
import org.openecomp.mso.asdc.installer.VfModuleMetaData;
import org.openecomp.mso.asdc.util.ASDCNotificationLogging;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true)
public class ASDCNotificationLoggingESTest extends ASDCNotificationLoggingESTestscaffolding {

    @Test(timeout = 4000)
    public void test00()  throws Throwable  {
        LinkedList<IVfModuleData> linkedList0 = new LinkedList<IVfModuleData>();
        VfModuleMetaData vfModuleMetaData0 = new VfModuleMetaData();
        vfModuleMetaData0.setAttribute("vfModuleModelInvariantUUID", vfModuleMetaData0);
        linkedList0.add((IVfModuleData) vfModuleMetaData0);
        // Undeclared exception!
        try {
            ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList0);
            fail("Expecting exception: ClassCastException");

        } catch(ClassCastException e) {
            //
            // org.openecomp.mso.asdc.installer.VfModuleMetaData cannot be cast to java.lang.String
            //
            verifyException("org.openecomp.mso.asdc.installer.VfModuleMetaData", e);
        }
    }

    @Test(timeout = 4000)
    public void test01()  throws Throwable  {
        List<IArtifactInfo> list0 = (List<IArtifactInfo>) mock(List.class, new ViolatedAssumptionAnswer());
        doReturn(false).when(list0).isEmpty();
        doReturn((Iterator) null).when(list0).iterator();
        doReturn(0).when(list0).size();
        INotificationData iNotificationData0 = mock(INotificationData.class, new ViolatedAssumptionAnswer());
        doReturn("rGQA").when(iNotificationData0).getDistributionID();
        doReturn(list0).when(iNotificationData0).getServiceArtifacts();
        doReturn("rGQA").when(iNotificationData0).getServiceDescription();
        doReturn("").when(iNotificationData0).getServiceInvariantUUID();
        doReturn("").when(iNotificationData0).getServiceName();
        doReturn("rGQA").when(iNotificationData0).getServiceUUID();
        doReturn("").when(iNotificationData0).getServiceVersion();
        // Undeclared exception!
        try {
            ASDCNotificationLogging.dumpASDCNotification(iNotificationData0);
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.openecomp.mso.asdc.util.ASDCNotificationLogging", e);
        }
    }

    @Test(timeout = 4000)
    public void test02()  throws Throwable  {
        LinkedList<IResourceInstance> linkedList0 = new LinkedList<IResourceInstance>();
        LinkedList<IArtifactInfo> linkedList1 = new LinkedList<IArtifactInfo>();
        linkedList0.offerFirst((IResourceInstance) null);
        INotificationData iNotificationData0 = mock(INotificationData.class, new ViolatedAssumptionAnswer());
        doReturn("").when(iNotificationData0).getDistributionID();
        doReturn(linkedList0, linkedList0).when(iNotificationData0).getResources();
        doReturn(linkedList1).when(iNotificationData0).getServiceArtifacts();
        doReturn("").when(iNotificationData0).getServiceDescription();
        doReturn("]U5JAkfdX0Cs").when(iNotificationData0).getServiceInvariantUUID();
        doReturn("jV13a").when(iNotificationData0).getServiceName();
        doReturn("").when(iNotificationData0).getServiceUUID();
        doReturn("jV13a").when(iNotificationData0).getServiceVersion();
        String string0 = ASDCNotificationLogging.dumpASDCNotification(iNotificationData0);
        assertEquals("ASDC Notification:\nDistributionID:\nServiceName:jV13a\nServiceVersion:jV13a\nServiceUUID:\nServiceInvariantUUID:]U5JAkfdX0Cs\nServiceDescription:\nService Artifacts List:\nNULL\nResource Instances List:\n{\nNULL\n\n}\n\n", string0);
    }

    @Test(timeout = 4000)
    public void test03()  throws Throwable  {
        LinkedList<IResourceInstance> linkedList0 = new LinkedList<IResourceInstance>();
        LinkedList<IArtifactInfo> linkedList1 = new LinkedList<IArtifactInfo>();
        IResourceInstance iResourceInstance0 = mock(IResourceInstance.class, new ViolatedAssumptionAnswer());
        doReturn((List) null).when(iResourceInstance0).getArtifacts();
        doReturn((String) null).when(iResourceInstance0).getCategory();
        doReturn((String) null).when(iResourceInstance0).getResourceCustomizationUUID();
        doReturn((String) null).when(iResourceInstance0).getResourceInstanceName();
        doReturn((String) null).when(iResourceInstance0).getResourceInvariantUUID();
        doReturn((String) null).when(iResourceInstance0).getResourceName();
        doReturn((String) null).when(iResourceInstance0).getResourceType();
        doReturn((String) null).when(iResourceInstance0).getResourceUUID();
        doReturn((String) null).when(iResourceInstance0).getResourceVersion();
        doReturn((String) null).when(iResourceInstance0).getSubcategory();
        linkedList0.add(iResourceInstance0);
        INotificationData iNotificationData0 = mock(INotificationData.class, new ViolatedAssumptionAnswer());
        doReturn("").when(iNotificationData0).getDistributionID();
        doReturn(linkedList0, linkedList0).when(iNotificationData0).getResources();
        doReturn(linkedList1).when(iNotificationData0).getServiceArtifacts();
        doReturn("").when(iNotificationData0).getServiceDescription();
        doReturn("").when(iNotificationData0).getServiceInvariantUUID();
        doReturn("36-s.n1@").when(iNotificationData0).getServiceName();
        doReturn("36-s.n1@").when(iNotificationData0).getServiceUUID();
        doReturn("").when(iNotificationData0).getServiceVersion();
        String string0 = ASDCNotificationLogging.dumpASDCNotification(iNotificationData0);
        assertEquals("ASDC Notification:\nDistributionID:\nServiceName:36-s.n1@\nServiceVersion:\nServiceUUID:36-s.n1@\nServiceInvariantUUID:\nServiceDescription:\nService Artifacts List:\nNULL\nResource Instances List:\n{\nResource Instance Info:\nResourceInstanceName:NULL\nResourceCustomizationUUID:NULL\nResourceInvariantUUID:NULL\nResourceName:NULL\nResourceType:NULL\nResourceUUID:NULL\nResourceVersion:NULL\nCategory:NULL\nSubCategory:NULL\nResource Artifacts List:\nNULL\n\n\n}\n\n", string0);
    }

    @Test(timeout = 4000)
    public void test04()  throws Throwable  {
        INotificationData iNotificationData0 = mock(INotificationData.class, new ViolatedAssumptionAnswer());
        doReturn((String) null).when(iNotificationData0).getDistributionID();
        doReturn((List) null).when(iNotificationData0).getResources();
        doReturn((List) null).when(iNotificationData0).getServiceArtifacts();
        doReturn((String) null).when(iNotificationData0).getServiceDescription();
        doReturn((String) null).when(iNotificationData0).getServiceInvariantUUID();
        doReturn((String) null).when(iNotificationData0).getServiceName();
        doReturn((String) null).when(iNotificationData0).getServiceUUID();
        doReturn((String) null).when(iNotificationData0).getServiceVersion();
        String string0 = ASDCNotificationLogging.dumpASDCNotification(iNotificationData0);
        assertEquals("ASDC Notification:\nDistributionID:NULL\nServiceName:NULL\nServiceVersion:NULL\nServiceUUID:NULL\nServiceInvariantUUID:NULL\nServiceDescription:NULL\nService Artifacts List:\nNULL\nResource Instances List:\nNULL\n", string0);
    }

    @Test(timeout = 4000)
    public void test05()  throws Throwable  {
        LinkedList<IVfModuleData> linkedList0 = new LinkedList<IVfModuleData>();
        VfModuleMetaData vfModuleMetaData0 = new VfModuleMetaData();
        Map<String, String> map0 = vfModuleMetaData0.getProperties();
        IVfModuleData iVfModuleData0 = mock(IVfModuleData.class, new ViolatedAssumptionAnswer());
        doReturn((List<String>) null, (List<String>) null).when(iVfModuleData0).getArtifacts();
        doReturn(map0, map0, (Map<String, String>) null).when(iVfModuleData0).getProperties();
        doReturn("vfModuleModelUUID", "isBase:").when(iVfModuleData0).getVfModuleModelDescription();
        doReturn((String) null, "vfModuleModelName").when(iVfModuleData0).getVfModuleModelInvariantUUID();
        doReturn("", "").when(iVfModuleData0).getVfModuleModelName();
        doReturn("vfModuleModelUUID", "isBase:").when(iVfModuleData0).getVfModuleModelUUID();
        doReturn("|:\\KD91", "vfModuleModelName").when(iVfModuleData0).getVfModuleModelVersion();
        doReturn(false, false).when(iVfModuleData0).isBase();
        linkedList0.add(iVfModuleData0);
        String string0 = ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList0);
        assertEquals("VfModuleMetaData List:\n{\nVfModuleMetaData:\nVfModuleModelName:\nVfModuleModelVersion:|:\\KD91\nVfModuleModelUUID:vfModuleModelUUID\nVfModuleModelInvariantUUID:NULL\nVfModuleModelDescription:vfModuleModelUUID\nArtifacts UUID List:NULLProperties List:\n}\n\nisBase:false\n\n\n}\n", string0);

        String string1 = ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList0);
        assertEquals("VfModuleMetaData List:\n{\nVfModuleMetaData:\nVfModuleModelName:\nVfModuleModelVersion:vfModuleModelName\nVfModuleModelUUID:isBase:\nVfModuleModelInvariantUUID:vfModuleModelName\nVfModuleModelDescription:isBase:\nArtifacts UUID List:NULLNULL\nisBase:false\n\n\n}\n", string1);
    }

    @Test(timeout = 4000)
    public void test06()  throws Throwable  {
        LinkedList<IResourceInstance> linkedList0 = new LinkedList<IResourceInstance>();
        LinkedList<IVfModuleData> linkedList1 = new LinkedList<IVfModuleData>();
        IVfModuleData iVfModuleData0 = mock(IVfModuleData.class, new ViolatedAssumptionAnswer());
        doReturn((List) null, (List) null).when(iVfModuleData0).getArtifacts();
        doReturn((Map) null, (Map) null).when(iVfModuleData0).getProperties();
        doReturn((String) null, (String) null).when(iVfModuleData0).getVfModuleModelDescription();
        doReturn((String) null, (String) null).when(iVfModuleData0).getVfModuleModelInvariantUUID();
        doReturn((String) null, (String) null).when(iVfModuleData0).getVfModuleModelName();
        doReturn((String) null, (String) null).when(iVfModuleData0).getVfModuleModelUUID();
        doReturn((String) null, (String) null).when(iVfModuleData0).getVfModuleModelVersion();
        doReturn(false, false).when(iVfModuleData0).isBase();
        linkedList1.add(iVfModuleData0);
        String string0 = ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList1);
        String string1 = ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList1);
        assertTrue(string1.equals((Object)string0));
        assertEquals("VfModuleMetaData List:\n{\nVfModuleMetaData:\nVfModuleModelName:NULL\nVfModuleModelVersion:NULL\nVfModuleModelUUID:NULL\nVfModuleModelInvariantUUID:NULL\nVfModuleModelDescription:NULL\nArtifacts UUID List:NULLNULL\nisBase:false\n\n\n}\n", string1);
    }

    @Test(timeout = 4000)
    public void test07()  throws Throwable  {
        LinkedList<IVfModuleData> linkedList0 = new LinkedList<IVfModuleData>();
        VfModuleMetaData vfModuleMetaData0 = new VfModuleMetaData();
        linkedList0.add((IVfModuleData) vfModuleMetaData0);
        UnaryOperator<IVfModuleData> unaryOperator0 = (UnaryOperator<IVfModuleData>) mock(UnaryOperator.class, new ViolatedAssumptionAnswer());
        doReturn((Object) null).when(unaryOperator0).apply(any());
        linkedList0.replaceAll(unaryOperator0);
        String string0 = ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList0);
        assertEquals("VfModuleMetaData List:\n{\nNULL\n\n}\n", string0);
    }

    @Test(timeout = 4000)
    public void test08()  throws Throwable  {
        LinkedList<IVfModuleData> linkedList0 = new LinkedList<IVfModuleData>();
        VfModuleMetaData vfModuleMetaData0 = new VfModuleMetaData();
        linkedList0.add((IVfModuleData) vfModuleMetaData0);
        // Undeclared exception!
        try {
            ASDCNotificationLogging.dumpVfModuleMetaDataList(linkedList0);
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.openecomp.mso.asdc.installer.VfModuleMetaData", e);
        }
    }

    @Test(timeout = 4000)
    public void test09()  throws Throwable  {
        String string0 = ASDCNotificationLogging.dumpVfModuleMetaDataList((List<IVfModuleData>) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test10()  throws Throwable  {
        String string0 = ASDCNotificationLogging.dumpASDCNotification((INotificationData) null);
        assertEquals("NULL", string0);
    }

    @Test(timeout = 4000)
    public void test11()  throws Throwable  {
        LinkedList<IArtifactInfo> linkedList0 = new LinkedList<IArtifactInfo>();
        INotificationData iNotificationData0 = mock(INotificationData.class, new ViolatedAssumptionAnswer());
        doReturn("io7<{~.v|%").when(iNotificationData0).getDistributionID();
        doReturn((List<IResourceInstance>) null).when(iNotificationData0).getResources();
        doReturn(linkedList0).when(iNotificationData0).getServiceArtifacts();
        doReturn("io7<{~.v|%").when(iNotificationData0).getServiceDescription();
        doReturn("io7<{~.v|%").when(iNotificationData0).getServiceInvariantUUID();
        doReturn("io7<{~.v|%").when(iNotificationData0).getServiceName();
        doReturn((String) null).when(iNotificationData0).getServiceUUID();
        doReturn("io7<{~.v|%").when(iNotificationData0).getServiceVersion();
        String string0 = ASDCNotificationLogging.dumpASDCNotification(iNotificationData0);
        assertEquals("ASDC Notification:\nDistributionID:io7<{~.v|%\nServiceName:io7<{~.v|%\nServiceVersion:io7<{~.v|%\nServiceUUID:NULL\nServiceInvariantUUID:io7<{~.v|%\nServiceDescription:io7<{~.v|%\nService Artifacts List:\nNULL\nResource Instances List:\nNULL\n", string0);

        ASDCNotificationLogging.dumpVfModuleMetaDataList((List<IVfModuleData>) null);
        IArtifactInfo iArtifactInfo0 = mock(IArtifactInfo.class, new ViolatedAssumptionAnswer());
        doReturn((String) null).when(iArtifactInfo0).getArtifactChecksum();
        doReturn((String) null).when(iArtifactInfo0).getArtifactDescription();
        doReturn((String) null).when(iArtifactInfo0).getArtifactName();
        doReturn((Integer) null).when(iArtifactInfo0).getArtifactTimeout();
        doReturn((String) null).when(iArtifactInfo0).getArtifactType();
        doReturn((String) null).when(iArtifactInfo0).getArtifactURL();
        doReturn((String) null).when(iArtifactInfo0).getArtifactUUID();
        doReturn((String) null).when(iArtifactInfo0).getArtifactVersion();
        doReturn((IArtifactInfo) null).when(iArtifactInfo0).getGeneratedArtifact();
        doReturn((List) null).when(iArtifactInfo0).getRelatedArtifacts();
        linkedList0.push(iArtifactInfo0);
        INotificationData iNotificationData1 = mock(INotificationData.class, new ViolatedAssumptionAnswer());
        doReturn("t2J^4~*i|btm ib&").when(iNotificationData1).getDistributionID();
        doReturn((List<IResourceInstance>) null).when(iNotificationData1).getResources();
        doReturn(linkedList0).when(iNotificationData1).getServiceArtifacts();
        doReturn("N~a W7").when(iNotificationData1).getServiceDescription();
        doReturn("N~a W7").when(iNotificationData1).getServiceInvariantUUID();
        doReturn("/&*/=").when(iNotificationData1).getServiceName();
        doReturn((String) null).when(iNotificationData1).getServiceUUID();
        doReturn("ASDC Notification:\nDistributionID:io7<{~.v|%\nServiceName:io7<{~.v|%\nServiceVersion:io7<{~.v|%\nServiceUUID:NULL\nServiceInvariantUUID:io7<{~.v|%\nServiceDescription:io7<{~.v|%\nService Artifacts List:\nNULL\nResource Instances List:\nNULL\n").when(iNotificationData1).getServiceVersion();
        String string1 = ASDCNotificationLogging.dumpASDCNotification(iNotificationData1);
        assertEquals("ASDC Notification:\nDistributionID:t2J^4~*i|btm ib&\nServiceName:/&*/=\nServiceVersion:ASDC Notification:\nDistributionID:io7<{~.v|%\nServiceName:io7<{~.v|%\nServiceVersion:io7<{~.v|%\nServiceUUID:NULL\nServiceInvariantUUID:io7<{~.v|%\nServiceDescription:io7<{~.v|%\nService Artifacts List:\nNULL\nResource Instances List:\nNULL\n\nServiceUUID:NULL\nServiceInvariantUUID:N~a W7\nServiceDescription:N~a W7\nService Artifacts List:\n{\nService Artifacts Info:\nArtifactName:NULL\nArtifactVersion:NULL\nArtifactType:NULL\nArtifactDescription:NULL\nArtifactTimeout:NULL\nArtifactURL:NULL\nArtifactUUID:NULL\nArtifactChecksum:NULL\nGeneratedArtifact:{NULL\n}\nRelatedArtifacts:NULL\n\n\n}\n\nResource Instances List:\nNULL\n", string1);
    }

    @Test(timeout = 4000)
    public void test12()  throws Throwable  {
        ASDCNotificationLogging aSDCNotificationLogging0 = new ASDCNotificationLogging();
        Object object0 = PrivateAccess.callMethod((Class<ASDCNotificationLogging>) ASDCNotificationLogging.class, aSDCNotificationLogging0, "dumpASDCResourcesList", (Object) null, (Class<?>) INotificationData.class);
        assertNull(object0);
    }
}
