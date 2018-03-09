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

package org.openecomp.mso.global_tests.asdc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openecomp.sdc.api.consumer.IDistributionStatusMessage;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.mso.asdc.installer.VfModuleMetaData;
import org.openecomp.mso.asdc.installer.IVfModuleData;
import org.openecomp.sdc.utils.DistributionStatusEnum;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.ASDCController;
import org.openecomp.mso.asdc.client.exceptions.ASDCControllerException;
import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.HeatFiles;
import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.global_tests.ArquillianPackagerForITCases;
import org.openecomp.mso.global_tests.asdc.notif_emulator.DistributionClientEmulator;
import org.openecomp.mso.global_tests.asdc.notif_emulator.JsonNotificationData;

@RunWith(Arquillian.class)
public class ASDCITCase {

	/**
	 * Add the resources in the right folder of a jar
	 * @param jar The jarArchive
	 * @param dir The main dir containing things that must be added
	 * @throws Exception In case of issues with the files
	 */
	private static void addFiles(JavaArchive jar, File dir,String destFolder) throws Exception  {

		if (!dir.isDirectory()) {
			throw new Exception("not a directory");
		}
		for (File f : dir.listFiles()) {


			if (f.isFile()) {
				jar.addAsResource(f, destFolder + "/" + f.getName());
			} else {

				addFiles(jar, f, destFolder+"/"+f.getName());
			}
		}
	}

	@Deployment(name="asdc-controller",testable=true)
	public static Archive<?> createAsdcControllerWarDeployment () throws Exception {
		System.out.println("Deploying ASDC Controller WAR with additional resources on default server");

		WebArchive warArchive = (WebArchive)ArquillianPackagerForITCases.createPackageFromExistingOne("../../asdc-controller/target/", "asdc-controller*.war", "asdc-controller.war");

		// Add the current test class
		JavaArchive testclasses = ShrinkWrap.create (JavaArchive.class, "testClasses.jar");

		testclasses.addPackage("org.openecomp.mso.global_tests.asdc.notif_emulator");

		addFiles(testclasses,new File(Thread.currentThread().getContextClassLoader().getResource("resource-examples/asdc").getFile()),"resource-examples/asdc");

		System.out.println(testclasses.toString(true));
		warArchive.addAsLibraries(testclasses);



		// BE CAREFUL a settings.xml file must be located in ${home.user}/.m2/settings.xml
		warArchive.addAsLibraries(Maven.resolver()
				.resolve("org.mockito:mockito-all:1.10.19")
				.withoutTransitivity ()
				.asFile ());

		//warArchive.addPackage("org.openecomp.mso.global_tests.asdc.notif_emulator");
		//addFiles(warArchive,new File(ASDCITCase.class.getClassLoader().getResource("resource-examples").getPath()),"resource-examples");

		// Take one war randomly to make arquilian happy
		Testable.archiveToTest(warArchive);


		System.out.println(warArchive.toString(true));

		return warArchive;
	}

	@BeforeClass
	public static final void waitBeforeStart() throws InterruptedException,
			IOException,
			URISyntaxException,
			NoSuchAlgorithmException {
		System.out.println("Executing " + ASDCITCase.class.getName());
	}

	@AfterClass
	public static final void waitAfterStart() throws InterruptedException,
			IOException,
			URISyntaxException,
			NoSuchAlgorithmException {
		System.out.println("Waiting 60000ms " + ASDCITCase.class.getName());
		Thread.sleep(60000);
	}

	/**
	 * Be careful when using that notification fake structure, the UUID of notif artifacts MUST be different.
	 * There is a static Map behind the scene.
	 */
	private JsonNotificationData notifDataWithoutModuleInfo;
	private DistributionClientEmulator distribClientWithoutModuleInfo;

	private JsonNotificationData notifDataV1, notifDataV2, notifDataV3, notifDataV4, notifDataV5,notifDataDNS,notifDataVFW;
	private DistributionClientEmulator distribClientV1, distribClientV2, distribClientV3, distribClientV4, distribClientV5, distribClientV1ForSameNotif, distribClientDNS,distribClientVFW;


	@Before
	public final void beforeEachTest() throws IOException {

		distribClientV1= new DistributionClientEmulator("/resource-examples/asdc/simpleNotif-V1");
		distribClientV1ForSameNotif= new DistributionClientEmulator("/resource-examples/asdc/simpleNotif-V1");
		notifDataV1 = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/simpleNotif-V1");

		// This is a duplicate in version 2 of the version 1
		distribClientV2= new DistributionClientEmulator("/resource-examples/asdc/simpleNotif-V2");
		notifDataV2 = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/simpleNotif-V2");

		distribClientV3= new DistributionClientEmulator("/resource-examples/asdc/simpleNotif-V3");
		notifDataV3 = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/simpleNotif-V3");

		// This is a duplicate in version 4 of the version 3
		distribClientV4= new DistributionClientEmulator("/resource-examples/asdc/simpleNotif-V4");
		notifDataV4 = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/simpleNotif-V4");

		// This notification is to test the deployment of volume with nested + an artifact not used (should send notification with DEPLOY_ERROR
		distribClientV5= new DistributionClientEmulator("/resource-examples/asdc/simpleNotif-V5");
		notifDataV5 = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/simpleNotif-V5");


		distribClientWithoutModuleInfo= new DistributionClientEmulator("/resource-examples/asdc/notif-without-modules-metadata");
		notifDataWithoutModuleInfo = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/notif-without-modules-metadata");


		distribClientDNS= new DistributionClientEmulator("/resource-examples/asdc/demo-dns-V1");
		notifDataDNS = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/demo-dns-V1");


		distribClientVFW= new DistributionClientEmulator("/resource-examples/asdc/demo-vfw-V1");
		notifDataVFW = JsonNotificationData.instantiateNotifFromJsonFile("/resource-examples/asdc/demo-vfw-V1");


	}

	@Test
	@OperateOnDeployment("asdc-controller")
	public void testNotifWithoutModuleInfo () throws NoSuchAlgorithmException,
			IOException,
			URISyntaxException,
			ArtifactInstallerException, ASDCControllerException, ASDCParametersException {



		ASDCController asdcController = new ASDCController("asdc-controller1", distribClientWithoutModuleInfo);
		asdcController.initASDC();
		asdcController.treatNotification(notifDataWithoutModuleInfo);

		assertTrue(distribClientWithoutModuleInfo.getDistributionMessageReceived().size() > 0);

		int badDeployment=0;
		for (IDistributionStatusMessage message:distribClientWithoutModuleInfo.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			if(message.getStatus().equals(DistributionStatusEnum.DEPLOY_ERROR)) {
				badDeployment++;
			}
		}
		assertTrue(badDeployment == 3);

		// Check if something has been recorder in DB, as it should not
		CatalogDatabase catalogDB = CatalogDatabase.getInstance();

		HeatTemplate heatTemplate = catalogDB.getHeatTemplate("Whot-nimbus-oam_v1.0.yaml", "1.0", "resourceName-1");
		assertNull(heatTemplate);
	}

	private void validateVnfResource(JsonNotificationData inputNotification, List<IVfModuleData> moduleList) {

		CatalogDatabase catalogDB = CatalogDatabase.getInstance();


		for (IResourceInstance resource:inputNotification.getResources()) {
			VnfResource vnfResourceDB = catalogDB.getVnfResource(inputNotification.getServiceName()+"/"+resource.getResourceInstanceName(), inputNotification.getServiceVersion());
			assertNotNull(vnfResourceDB);

			//assertTrue(vnfResourceDB.getAsdcUuid().equals(resource.getResourceUUID()));
			assertTrue(vnfResourceDB.getDescription().equals(inputNotification.getServiceDescription()));
			assertTrue(vnfResourceDB.getModelInvariantUuid().equals(resource.getResourceInvariantUUID()));
			assertTrue(vnfResourceDB.getModelVersion().equals(resource.getResourceVersion()));
			assertTrue(vnfResourceDB.getOrchestrationMode().equals("HEAT"));
			assertTrue(vnfResourceDB.getVersion().equals(inputNotification.getServiceVersion()));
			//assertTrue(vnfResourceDB.getVnfType().equals(inputNotification.getServiceName()+"/"+resource.getResourceInstanceName()));
			//assertTrue(vnfResourceDB.getModelCustomizationName().equals(resource.getResourceInstanceName()));
			assertTrue(vnfResourceDB.getModelName().equals(resource.getResourceName()));
			//assertTrue(vnfResourceDB.getServiceModelInvariantUUID().equals(inputNotification.getServiceInvariantUUID()));

			for (IVfModuleData module:moduleList) {

				VfModule vfModuleDB = catalogDB.getVfModuleModelName(module.getVfModuleModelName(),inputNotification.getServiceVersion());
				assertNotNull(vfModuleDB);
				assertTrue(module.getVfModuleModelName().equals(vfModuleDB.getModelName()));

			//	assertTrue((inputNotification.getServiceName()+"/"+resource.getResourceInstanceName()+"::"+vfModuleDB.getModelName()).equals(vfModuleDB.getType()));
			//	assertTrue(vnfResourceDB.getId()!=0);
				//assertNotNull(vfModuleDB.getVnfResourceId());

			//	assertTrue(vnfResourceDB.getId()==vfModuleDB.getVnfResourceId().intValue());

				for (String artifactUUID:module.getArtifacts()) {
					IArtifactInfo artifact = null;
					for (IArtifactInfo artifactTemp:resource.getArtifacts()) {
						if (artifactTemp.getArtifactUUID().equals(artifactUUID)) {
							artifact = artifactTemp;
							break;
						}
					}
					assertNotNull(artifact);

					switch (artifact.getArtifactType()) {
						case ASDCConfiguration.HEAT:
							HeatTemplate heatTemplateDB= catalogDB.getHeatTemplate(vfModuleDB.getHeatTemplateArtifactUUId());
							assertNotNull(heatTemplateDB);
							//assertTrue(heatTemplateDB.getAsdcResourceName().equals(resource.getResourceName()));
							assertTrue(heatTemplateDB.getAsdcUuid().equals(artifact.getArtifactUUID()));
							assertTrue(heatTemplateDB.getDescription().equals(artifact.getArtifactDescription()));
							assertTrue(heatTemplateDB.getTemplateBody() != null && !heatTemplateDB.getTemplateBody().isEmpty());
							assertTrue(heatTemplateDB.getParameters().size()>0);

							assertTrue(heatTemplateDB.getTemplateName().equals(artifact.getArtifactName()));

							if (artifact.getArtifactTimeout() != null) {
								assertTrue(heatTemplateDB.getTimeoutMinutes()== artifact.getArtifactTimeout().intValue());
							} else {
								assertTrue(heatTemplateDB.getTimeoutMinutes()== 240);
							}
							assertTrue(heatTemplateDB.getVersion().equals(artifact.getArtifactVersion()));

							assertFalse(heatTemplateDB.getTemplateBody().contains("file:///"));
							break;
						case ASDCConfiguration.HEAT_ENV:

							HeatEnvironment heatEnvironmentDB = catalogDB.getHeatEnvironment(artifact.getArtifactName(), artifact.getArtifactVersion(), inputNotification.getServiceName()+"/"+resource.getResourceInstanceName());

							assertNotNull(heatEnvironmentDB);
//							assertTrue((vfModuleDB.getVolEnvironmentId() != null && vfModuleDB.getVolEnvironmentId().intValue() == heatEnvironmentDB.getId())
//									|| (vfModuleDB.getEnvironmentId() != null && vfModuleDB.getEnvironmentId() == heatEnvironmentDB.getId()));
//
//							assertTrue(heatEnvironmentDB.getAsdcResourceName().equals(inputNotification.getServiceName()+"/"+resource.getResourceInstanceName()));
//
//							assertTrue(heatEnvironmentDB.getAsdcUuid().equals(artifact.getArtifactUUID()));
							assertTrue(heatEnvironmentDB.getDescription().equals(artifact.getArtifactDescription()));
							assertTrue(heatEnvironmentDB.getVersion().equals(artifact.getArtifactVersion()));
							assertTrue(heatEnvironmentDB.getName().equals(artifact.getArtifactName()));
							assertTrue(heatEnvironmentDB.getEnvironment() != null);
							assertFalse(heatEnvironmentDB.getEnvironment().contains("file:///"));

							break;
						case ASDCConfiguration.HEAT_NESTED:
							Map<String,Object> listNestedDBMainHeat=new HashMap<String,Object>();
							Map<String,Object> listNestedDBVolHeat=new HashMap<String,Object>();

							if (vfModuleDB.getHeatTemplateArtifactUUId() != null) {
								listNestedDBMainHeat = catalogDB.getNestedTemplates(vfModuleDB.getHeatTemplateArtifactUUId());
							}
							if (vfModuleDB.getVolHeatTemplateArtifactUUId() != null) {
								listNestedDBVolHeat = catalogDB.getNestedTemplates(vfModuleDB.getVolHeatTemplateArtifactUUId());
							}

							assertTrue(listNestedDBMainHeat.size() > 0 || listNestedDBVolHeat.size() > 0);


							assertTrue(listNestedDBMainHeat.get(artifact.getArtifactName()) != null
									|| listNestedDBVolHeat.get(artifact.getArtifactName()) != null);

							HeatTemplate rightNestedTemplateDB = catalogDB.getHeatTemplate(artifact.getArtifactName(), artifact.getArtifactVersion(), resource.getResourceName());
							assertNotNull(rightNestedTemplateDB);
							//assertTrue(catalogDB.getNestedHeatTemplate(vfModuleDB.getTemplateId(), rightNestedTemplateDB.getId()) != null || catalogDB.getNestedHeatTemplate(vfModuleDB.getVolTemplateId(), rightNestedTemplateDB.getId()) != null);

							//assertTrue(rightNestedTemplateDB.getAsdcResourceName().equals(resource.getResourceName()));
							assertTrue(rightNestedTemplateDB.getAsdcUuid().equals(artifact.getArtifactUUID()));
							assertTrue(rightNestedTemplateDB.getDescription().equals(artifact.getArtifactDescription()));
							assertTrue(rightNestedTemplateDB.getTemplateBody() != null && !rightNestedTemplateDB.getTemplateBody().isEmpty());
							assertTrue(rightNestedTemplateDB.getTemplateName().equals(artifact.getArtifactName()));

							if (artifact.getArtifactTimeout() != null) {
								assertTrue(rightNestedTemplateDB.getTimeoutMinutes()== artifact.getArtifactTimeout().intValue());
							} else {
								assertTrue(rightNestedTemplateDB.getTimeoutMinutes()== 240);
							}
							assertTrue(rightNestedTemplateDB.getVersion().equals(artifact.getArtifactVersion()));
							assertFalse(rightNestedTemplateDB.getTemplateBody().contains("file:///"));

							break;
						case ASDCConfiguration.HEAT_VOL:
							HeatTemplate heatTemplateVolDB = catalogDB.getHeatTemplate(vfModuleDB.getVolHeatTemplateArtifactUUId());
							assertNotNull(heatTemplateVolDB);

							//assertTrue(heatTemplateVolDB.getAsdcResourceName().equals(resource.getResourceName()));
							assertTrue(heatTemplateVolDB.getAsdcUuid().equals(artifact.getArtifactUUID()));
							assertTrue(heatTemplateVolDB.getDescription().equals(artifact.getArtifactDescription()));
							assertTrue(heatTemplateVolDB.getTemplateBody() != null && !heatTemplateVolDB.getTemplateBody().isEmpty());
							assertTrue(heatTemplateVolDB.getTemplateName().equals(artifact.getArtifactName()));

							if (artifact.getArtifactTimeout() != null) {
								assertTrue(heatTemplateVolDB.getTimeoutMinutes()== artifact.getArtifactTimeout().intValue());
							} else {
								assertTrue(heatTemplateVolDB.getTimeoutMinutes()== 240);
							}
							assertTrue(heatTemplateVolDB.getVersion().equals(artifact.getArtifactVersion()));
							assertFalse(heatTemplateVolDB.getTemplateBody().contains("file:///"));

							break;
						case ASDCConfiguration.HEAT_ARTIFACT:
							Map<String,HeatFiles> heatFilesDB= catalogDB.getHeatFilesForVfModule(vfModuleDB.getModelUUID());
							assertTrue(heatFilesDB.size()>0);
							HeatFiles rightHeatFilesDB=heatFilesDB.get( artifact.getArtifactName());
							assertNotNull(rightHeatFilesDB);

							//assertTrue(rightHeatFilesDB.getAsdcResourceName().equals(resource.getResourceName()));
							assertTrue(rightHeatFilesDB.getAsdcUuid().equals(artifact.getArtifactUUID()));
							assertTrue(rightHeatFilesDB.getDescription().equals(artifact.getArtifactDescription()));
							assertTrue(rightHeatFilesDB.getFileBody() != null && !rightHeatFilesDB.getFileBody().isEmpty());
							assertTrue(rightHeatFilesDB.getFileName().equals( artifact.getArtifactName()));
							assertTrue(rightHeatFilesDB.getVersion().equals(artifact.getArtifactVersion()));

							break;
						default:
							break;

					}
				}

			}

		}

		Service service = catalogDB.getServiceByModelUUID(inputNotification.getServiceUUID());
		assertNotNull(service);
		assertTrue(service.getCreated() !=null && service.getCreated().getTime()>0);
		assertTrue(service.getDescription().equals(inputNotification.getServiceDescription()));
		assertTrue(service.getModelInvariantUUID().equals(inputNotification.getServiceInvariantUUID()));
		assertTrue(service.getModelName().equals(inputNotification.getServiceName()));
		assertTrue(service.getModelUUID().equals(inputNotification.getServiceUUID()));
		assertTrue(service.getVersion().equals(inputNotification.getServiceVersion()));

	}

	@Test
	@OperateOnDeployment("asdc-controller")
	public void testNotifsDeployment () throws NoSuchAlgorithmException,
			IOException,
			URISyntaxException,
			ArtifactInstallerException, ASDCControllerException, ASDCParametersException {



		ASDCController asdcControllerV1 = new ASDCController("asdc-controller1", distribClientV1);
		asdcControllerV1.initASDC();
		asdcControllerV1.treatNotification(notifDataV1);

		assertTrue(distribClientV1.getDistributionMessageReceived().size() > 0);
		for (IDistributionStatusMessage message:distribClientV1.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
		}

		this.validateVnfResource(notifDataV1,distribClientV1.getListVFModuleMetaData());



		// Try again to load the same notif
		ASDCController asdcControllerNewNotif = new ASDCController("asdc-controller1", distribClientV1ForSameNotif);
		asdcControllerNewNotif.initASDC();
		asdcControllerNewNotif.treatNotification(notifDataV1);

		for (IDistributionStatusMessage message:distribClientV1ForSameNotif.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			assertTrue(message.getStatus().equals(DistributionStatusEnum.ALREADY_DEPLOYED) || message.getStatus().equals(DistributionStatusEnum.ALREADY_DOWNLOADED));
		}


		// Try again to load same notif but in V2
		ASDCController asdcControllerV2 = new ASDCController("asdc-controller1", distribClientV2);
		asdcControllerV2.initASDC();
		asdcControllerV2.treatNotification(notifDataV2);

		for (IDistributionStatusMessage message:distribClientV2.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
		}

		this.validateVnfResource(notifDataV2,distribClientV2.getListVFModuleMetaData());


		// Try again to load same notif + Script + Volume artifacts and in service V3
		ASDCController asdcControllerV3 = new ASDCController("asdc-controller1", distribClientV3);
		asdcControllerV3.initASDC();
		asdcControllerV3.treatNotification(notifDataV3);

		for (IDistributionStatusMessage message:distribClientV3.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
		}

		this.validateVnfResource(notifDataV3,distribClientV3.getListVFModuleMetaData());

		// Try again to load same notif + Script + Volume artifacts and in service V4
		ASDCController asdcControllerV4 = new ASDCController("asdc-controller1", distribClientV4);
		asdcControllerV4.initASDC();
		asdcControllerV4.treatNotification(notifDataV4);

		for (IDistributionStatusMessage message:distribClientV4.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
		}

		this.validateVnfResource(notifDataV4,distribClientV4.getListVFModuleMetaData());


		// Try again with service V5 (Nested template attached to Volume + HEat artifact not used by module),
		//this should force the notification DEPLOY_ERROR to be sent for this artifact
		ASDCController asdcControllerV5 = new ASDCController("asdc-controller1", distribClientV5);
		asdcControllerV5.initASDC();
		asdcControllerV5.treatNotification(notifDataV5);

		for (IDistributionStatusMessage message:distribClientV5.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
			if ("cloud-nimbus.sh".equals(message.getArtifactURL())) {
				assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_ERROR) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
			} else {
				assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
			}
		}

		this.validateVnfResource(notifDataV5,distribClientV5.getListVFModuleMetaData());


		// Try again with demo DNS
		ASDCController asdcControllerDNS = new ASDCController("asdc-controller1", distribClientDNS);
		asdcControllerDNS.initASDC();
		asdcControllerDNS.treatNotification(notifDataDNS);

		for (IDistributionStatusMessage message:distribClientDNS.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:"+message.getArtifactURL()+", Value:"+message.getStatus().name());
				assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK) || message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
		}

		this.validateVnfResource(notifDataDNS,distribClientDNS.getListVFModuleMetaData());

		// Try again with demo VFW
		ASDCController asdcControllerVFW = new ASDCController("asdc-controller1", distribClientVFW);
		asdcControllerVFW.initASDC();
		asdcControllerVFW.treatNotification(notifDataVFW);

		for (IDistributionStatusMessage message : distribClientVFW.getDistributionMessageReceived()) {
			System.out.println("Message received, URL:" + message.getArtifactURL() + ", Value:" + message.getStatus().name());
			assertTrue(message.getStatus().equals(DistributionStatusEnum.DEPLOY_OK)
						|| message.getStatus().equals(DistributionStatusEnum.DOWNLOAD_OK));
		}

		this.validateVnfResource(notifDataVFW, distribClientVFW.getListVFModuleMetaData());

	}
}
