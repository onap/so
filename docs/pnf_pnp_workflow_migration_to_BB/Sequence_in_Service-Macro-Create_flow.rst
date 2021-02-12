.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2021 NOKIA, Ltd.

Sequence in Service-Macro-Create flow
=====================================

1. AssignServiceInstanceBB
2. CreateNetworkCollectionBB
3. AssignNetworkBB
4. AssignVnfBB
5. AssignVolumeGroupBB
6. AssignVfModuleBB
7. **AssignPnfBB**
8. **WaitForPnfReadyBB**
9. **ControllerExecutionBB (action: configAssign, scope: pnf)**
10. **ControllerExecutionBB (action: configDeploy, scope: pnf)**
11. **ActivatePnfBB**
12. ConfigAssignVnfBB
13. CreateNetworkBB
14. ActivateNetworkBB
15. CreateVolumeGroupBB
16. ActivateVolumeGroupBB
17. CreateVfModuleBB
18. ActivateVfModuleBB
19. ConfigDeployVnfBB
20. ActivateVnfBB
21. ActivateNetworkCollectionBB
22. ActivateServiceInstanceBB