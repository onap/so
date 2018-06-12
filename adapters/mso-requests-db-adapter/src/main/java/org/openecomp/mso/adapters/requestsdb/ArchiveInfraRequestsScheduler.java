package org.openecomp.mso.adapters.requestsdb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openecomp.mso.db.request.beans.ArchivedInfraRequests;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.data.repository.ArchivedInfraRequestsRepository;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.core.SchedulerLock;

@Component
public class ArchiveInfraRequestsScheduler {
	
	private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, ArchiveInfraRequestsScheduler.class);
	
	@Autowired
	private InfraActiveRequestsRepository infraActiveRepo;
	@Autowired 
	private ArchivedInfraRequestsRepository archivedInfraRepo;
	
	@Value("${mso.infra-requests.archived.period}")
	private int archivedPeriod;
	
	/**
	 * Runs the scheduler nightly
	 * [Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]
	 */
	@Scheduled(cron="0 0 1 * * ?")
	@SchedulerLock(name = "archiveInfraRequestsScheduler")
	public void infraRequestsScheduledTask() {
		logger.debug("Start of archiveInfraRequestsScheduler");
		
		Date currentDate= new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DATE, -archivedPeriod);
		Date archivingDate = calendar.getTime();
		
		logger.debug("Date before 6 months: "+ (calendar.get(Calendar.MONTH) + 1) + "-"
		        		+ calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.YEAR));
		
		List<InfraActiveRequests> requestsByEndTime = new ArrayList<>();
		PageRequest pageRequest = new PageRequest(0, 100);
		do {
			requestsByEndTime = infraActiveRepo.findByEndTimeLessThan(archivingDate, pageRequest);
			logger.debug(requestsByEndTime.size() + " requests to be archived based on End Time" );
			archiveInfraRequests(requestsByEndTime);
		} while(requestsByEndTime.size() > 0);
		
		List<InfraActiveRequests> requestsByStartTime = new ArrayList<>();
		do {
			requestsByStartTime = infraActiveRepo.findByStartTimeLessThanAndEndTime(archivingDate, null, pageRequest);
			logger.debug(requestsByEndTime.size() + " requests to be archived based on Start Time" );
			archiveInfraRequests(requestsByStartTime);
		} while(requestsByStartTime.size() > 0);
		
		logger.debug("End of archiveInfraRequestsScheduler");
	}
	
	protected void archiveInfraRequests(List<InfraActiveRequests> requests) {
		List<ArchivedInfraRequests> newArchivedReqs = new ArrayList<>();
		List<InfraActiveRequests> oldInfraReqs = new ArrayList<>();
		
		for(InfraActiveRequests iar: requests) {
			ArchivedInfraRequests archivedInfra = new ArchivedInfraRequests();
			try {
				archivedInfra.setAaiServiceId(iar.getAaiServiceId());
				archivedInfra.setAction(iar.getAction());
				archivedInfra.setAicCloudRegion(iar.getAicCloudRegion());
				archivedInfra.setAicNodeClli(iar.getAicNodeClli());
				archivedInfra.setCallBackUrl(iar.getCallBackUrl());
				archivedInfra.setClientRequestId(iar.getClientRequestId());
				archivedInfra.setConfigurationId(iar.getConfigurationId());
				archivedInfra.setConfigurationName(iar.getConfigurationName());
				archivedInfra.setCorrelator(iar.getCorrelator());
				archivedInfra.setEndTime(iar.getEndTime());
				archivedInfra.setLastModifiedBy(iar.getLastModifiedBy());
				archivedInfra.setNetworkId(iar.getNetworkId());
				archivedInfra.setNetworkName(iar.getNetworkName());
				archivedInfra.setNetworkType(iar.getNetworkType());
				archivedInfra.setOperationalEnvId(iar.getOperationalEnvId());
				archivedInfra.setOperationalEnvName(iar.getOperationalEnvName());
				archivedInfra.setProgress(iar.getProgress());
				archivedInfra.setProvStatus(iar.getProvStatus());
				archivedInfra.setRequestAction(iar.getRequestAction());
				archivedInfra.setRequestBody(iar.getRequestBody());
				archivedInfra.setRequestId(iar.getRequestId());
				archivedInfra.setRequestorId(iar.getRequestorId());
				archivedInfra.setRequestScope(iar.getRequestScope());
				archivedInfra.setRequestStatus(iar.getRequestStatus());
				archivedInfra.setRequestType(iar.getRequestType());
				archivedInfra.setResponseBody(iar.getResponseBody());
				archivedInfra.setServiceInstanceId(iar.getServiceInstanceId());
				archivedInfra.setServiceInstanceName(iar.getServiceInstanceName());
				archivedInfra.setServiceType(iar.getServiceType());
				archivedInfra.setSource(iar.getSource());
				archivedInfra.setStartTime(iar.getStartTime());
				archivedInfra.setStatusMessage(iar.getStatusMessage());
				archivedInfra.setTenantId(iar.getTenantId());
				archivedInfra.setVfModuleId(iar.getVfModuleId());
				archivedInfra.setVfModuleModelName(iar.getVfModuleModelName());
				archivedInfra.setVfModuleName(iar.getVfModuleName());
				archivedInfra.setVnfId(iar.getVnfId());
				archivedInfra.setVnfName(iar.getVnfName());
				archivedInfra.setVnfOutputs(iar.getVnfOutputs());
				archivedInfra.setVnfParams(iar.getVnfParams());
				archivedInfra.setVnfType(iar.getVnfType());
				archivedInfra.setVolumeGroupId(iar.getVolumeGroupId());
				archivedInfra.setVolumeGroupName(iar.getVolumeGroupName());
				
				newArchivedReqs.add(archivedInfra);
				oldInfraReqs.add(iar);
			} catch(Exception e) {
				logger.error(e);
				logger.error(MessageEnum.RA_GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.UnknownError, e.getMessage());
			}
		}
		
		logger.info("Creating archivedInfraRequest records: " + newArchivedReqs.size());
		archivedInfraRepo.save(newArchivedReqs);
		
		logger.info("Deleting InfraActiveRequest records:  "+ oldInfraReqs.size());
		infraActiveRepo.delete(oldInfraReqs);
	}
}
