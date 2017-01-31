This artifact is the MSO VNF adapter.  It serves both SOAP and REST requests to the following URLs:

  * http://host:port/vnfs/VnfAdapter?wsdl
  * http://host:port/vnfs/VnfAdapterAsync?wsdl
  * http://host:port/vnfs/rest/v1/vnfs/healthcheck
  * http://host:port/vnfs/rest/v1/vnfs/{aaiVnfId}/vf-modules
  * http://host:port/vnfs/rest/v1/vnfs/{aaiVnfId}/vf-modules/{aaiVfModuleId}
  * http://host:port/vnfs/rest/v1/vnfs/{aaiVnfId}/vf-modules/{aaiVfModuleId}/rollback
  * http://host:port/vnfs/rest/v1/volume-groups
  * http://host:port/vnfs/rest/v1/volume-groups/{aaiVolumeGroupId}
  * http://host:port/vnfs/rest/v1/volume-groups/{aaiVolumeGroupId}/rollback
