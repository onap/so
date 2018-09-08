use catalogdb;

DELETE FROM catalogdb.service_recipe 
	where orchestration_uri = '/mso/async/services/CreateGenericMacroServiceNetworkVnf' 
		or orchestration_uri = '/mso/async/services/DeleteGenericMacroServiceNetworkVnf';