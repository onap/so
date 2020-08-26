package org.onap.so.adapters.nssmf.controller;

import org.onap.so.adapters.nssmf.service.NssmfManagerService;
import org.onap.so.beans.nsmf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping(value = "/api/rest/provMns/v1", produces = {APPLICATION_JSON}, consumes = {APPLICATION_JSON})
public class NssmfAdapterController {

    private static final Logger logger = LoggerFactory.getLogger(NssmfAdapterController.class);

    @Autowired
    private NssmfManagerService nssmfManagerService;

    @PostMapping(value = "/NSS/SliceProfiles")
    public ResponseEntity allocateNssi(@RequestBody NssmfAdapterNBIRequest nbiRequest) {
        return nssmfManagerService.allocateNssi(nbiRequest);
    }

    @PostMapping(value = "/NSS/SliceProfiles/{sliceProfileId}")
    public ResponseEntity deAllocateNssi(@RequestBody NssmfAdapterNBIRequest nbiRequest,
            @PathVariable("sliceProfileId") final String sliceProfileId) {
        return nssmfManagerService.deAllocateNssi(nbiRequest, sliceProfileId);
    }


    @PostMapping(value = "/NSS/{snssai}/activation")
    public ResponseEntity activateNssi(@RequestBody NssmfAdapterNBIRequest nbiRequest,
            @PathVariable("snssai") String snssai) {
        return nssmfManagerService.activateNssi(nbiRequest, snssai);
    }

    @PostMapping(value = "/NSS/{snssai}/deactivation")
    public ResponseEntity deactivateNssi(@RequestBody NssmfAdapterNBIRequest nbiRequest,
            @PathVariable("snssai") String snssai) {
        return nssmfManagerService.deActivateNssi(nbiRequest, snssai);
    }

    @PostMapping(value = "/NSS/jobs/{jobId}")
    public ResponseEntity queryJobStatus(@RequestBody NssmfAdapterNBIRequest nbiRequest,
            @PathVariable("jobId") String jobId) {
        return nssmfManagerService.queryJobStatus(nbiRequest, jobId);
    }

    @PostMapping(value = "/NSS/NSSISelectionCapability")
    public ResponseEntity queryNSSISelectionCapability(@RequestBody NssmfAdapterNBIRequest nbiRequest) {
        return nssmfManagerService.queryNSSISelectionCapability(nbiRequest);
    }

    @PostMapping(value = "/NSS/subnetCapabilityQuery")
    public ResponseEntity querySubnetCapability(@RequestBody NssmfAdapterNBIRequest nbiRequest) {
        return nssmfManagerService.querySubnetCapability(nbiRequest);
    }

}
