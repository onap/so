# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Scope

This is the main SO (Service Orchestration) repository for ONAP — the Maven reactor rooted at `pom.xml` with groupId `org.onap.so` (`1.19.0-SNAPSHOT`). The parent `CLAUDE.md` at `../../CLAUDE.md` covers the enclosing ONAP monorepo. ETSI and sibling orchestration adapters (`so-etsi-sol003-adapter`, `so-cnf-adapter`, etc.) live in separate repos at `../` and are pulled in as Nexus dependencies.

Note: Java source/target/release is **11** here (see `pom.xml` `<java.version>`), even though other ONAP components use 17. `springboot.version` is `2.7.18`, `camunda.version` is `7.17.0`.

## Build & Test

```bash
mvn clean install                                       # Default build: unit + Spring integration tests, javadoc, doclint
mvn clean install -P with-integration-tests             # Also enables the integration-test suite
mvn clean install -DskipTests=true -Dmaven.test.skip=true   # Skip tests entirely
mvn clean install -P docker                             # Build Docker images (skipped by default via docker.skip=true)
mvn clean install -Dmaven.javadoc.skip=true -Dadditionalparam=-Xdoclint:none   # Skip javadoc/doclint
mvn process-sources -P format                           # Apply the code formatter (required before submit — build fails on format violations)
```

The `format` profile flips `format.skipValidate=true` and `format.skipExecute=false`; without it, the default build **validates** formatting but does not rewrite files. Eclipse formatter config: `project-configs/code-tools/onap-eclipse-format.xml`.

### Running a single test

```bash
mvn -pl <module-path> -am test                                          # All tests in a module, building upstream deps
mvn -pl bpmn/so-bpmn-tasks test -Dtest=WorkflowActionTest                # Single test class
mvn -pl bpmn/so-bpmn-tasks test -Dtest=WorkflowActionTest#methodName     # Single method
mvn -pl mso-api-handlers/mso-api-handler-infra test -Dtest='*ServiceInstances*'   # Pattern
```

Surefire runs with `parallel=classes`, `rerunFailingTestsCount=2`, and sets `so.log.level=DEBUG`. `-Dsurefire.forkCount` / `-Dsurefire.threadCount` are exposed properties if needed. PowerMock is a banned dependency (enforced).

### Docker images

`docker.skip=true` is the default at every level; enable with profile `docker-image-build` (build) or `docker-image-build-push` (build + push). Final images are assembled in `packages/docker/` using `Dockerfile.so-app` / `Dockerfile.so-base-image`. Per-component Dockerfiles (e.g. `adapters/mso-openstack-adapters/Deployment/Dockerfile.adapters`) exist but are driven by the `packages` reactor.

Deployment configuration lives in a **separate** `docker-config` repo (not here). See README.md for details on `/shared/mso-docker.json` and `chef-solo` configuration of APIH/BPMN/JRA.

## High-level Architecture

The request flow for a northbound service instantiation is:

```
Client (VID/NBI) ──HTTP──▶ mso-api-handler-infra ──HTTP──▶ bpmn-infra (Camunda) ──▶ adapters ──▶ SDNC / A&AI / catalog-db / request-db / OpenStack / ...
```

### Maven modules (top-level, under `pom.xml`)

- `common/` — shared utilities, logging, HTTP clients used across all components.
- `graph-inventory/` — A&AI REST client (`aai-client`) and fluent URI builder; every adapter/BPMN module that talks to A&AI depends on these.
- `mso-catalog-db/` — Hibernate entities + JPA repositories for the Catalog DB (service/vnf/vf-module models, recipes, orchestration-flow tables, `rainy_day_handler_macro`). Not a running service; the running service is `adapters/mso-catalog-db-adapter/`.
- `mso-api-handlers/` — northbound REST tier. `mso-api-handler-infra` is the main entry point (`/onap/so/infra/...`); `mso-requests-db` + `mso-requests-db-repositories` model the Requests DB (`infraActiveRequests`, `requestProcessingData`). Does request validation, duplicate-name checks, recipe lookup, and POSTs to BPMN.
- `adapters/` — Spring Boot services that wrap external systems. Order matters when changing cross-cutting concerns: `mso-adapters-rest-interface` (shared REST DTOs), `mso-adapter-utils` (shared base classes), then the concrete adapters: `mso-catalog-db-adapter`, `mso-requests-db-adapter`, `mso-sdnc-adapter`, `mso-openstack-adapters`, `etsi-sol002-adapter`, `so-appc-orchestrator`.
- `asdc-controller/` — SDC/ASDC client. Receives distributions of TOSCA CSARs from SDC and populates the Catalog DB.
- `so-optimization-clients/`, `so-sdn-clients/` — clients for OOF (optimization) and additional SDN-C APIs used from BPMN.
- `bpmn/` — Camunda BPMN workflow engine (see below).
- `cxf-logging/` — Apache CXF interceptors for inbound/outbound request logging.
- `so-simulator/` — Spring Boot test double that mocks A&AI / SDNC / SDC / etc. for CSIT and local runs.
- `packages/docker/` — assembly + `docker-maven-plugin` wiring.
- `deployment-configs/` — certs, logger configs, scripts packaged into images via `maven-dependency-plugin` `unpack`.

### BPMN sub-structure (`bpmn/`)

- `MSOCoreBPMN/` — core Camunda engine configuration and runtime bootstrap.
- `MSOCommonBPMN/` — Groovy base classes, shared delegates, utilities used by every workflow.
- `mso-infrastructure-bpmn/` — the runnable Spring Boot + Camunda application (`bpmn-infra` docker image).
- `so-bpmn-infrastructure-flows/` — top-level process BPMNs (files in `src/main/resources/process/`) for service instantiation, network slicing, ETSI NS LCM, etc. Paired with Groovy scripts invoked from BPMN script tasks.
- `so-bpmn-infrastructure-common/` — Java code shared by infrastructure flows.
- `so-bpmn-building-blocks/` — the Building Block (BB) framework used by macro flows. BBs are small BPMN subprocesses (e.g. `AssignServiceInstanceBB`, `CreateVfModuleBB`, `ActivateVnfBB`) orchestrated by `WorkflowActionBB.bpmn` + `ExecuteBuildingBlock.bpmn` via `WorkflowAction.java` / `WorkflowActionBBTasks.java`.
- `so-bpmn-tasks/` — Java tasks referenced from BPMN (A&AI tasks, SDNC tasks, orchestration-status validation, rainy-day handler, etc.).
- `so-bpmn-moi/` — 3GPP MOI (Managed Object Instance) flows for slice management.

**Convention** (per `docs/developer_info/BPMN_Project_Structure.rst`):
- Main-process BPMNs live under `src/main/resources/process/`; subprocess BPMNs under `src/main/resources/subprocess/`.
- Each `.bpmn` has a paired Groovy script (invoked from script tasks). Each flow typically has a unit test, with fixtures under `src/test/resources/__files/`.
- Open `.bpmn` files with the standalone Camunda Modeler.

### Macro vs. à la carte orchestration

Two orchestration styles share the same entry point (`ServiceInstances.java` in `mso-api-handler-infra`):

- **à la carte** — the client drives each lifecycle step (assign → create → activate) as a separate request.
- **macro** — a single request fans out into a sequence of BBs chosen by `WorkflowAction` based on the resources present in the service model. BB sequences come from the `northbound_request_ref_lookup` + `orchestration_flow_reference` Catalog DB tables keyed on `action`/`requestScope`/`isALaCarte`/`cloudOwner`/`serviceType`.

Error handling for macro flows is policy-driven via the `rainy_day_handler_macro` Catalog DB table (`retry` / `rollback` / `abort` / `RollbackToXXX`). Rollback rewrites the remaining BB flow names using suffix flips (`AssignXXX`→`UnassignXXX`, `CreateXXX`→`DeleteXXX`, `ActivateXXX`→`DeactivateXXX`, etc.). See `docs/developer_info/BBUnderstanding.rst` for the step-by-step walkthrough.

### Data stores

- **Catalog DB** — service/resource models + recipes + macro-flow tables. Accessed by BPMN/API-handler exclusively via `mso-catalog-db-adapter` (port 8082 in default config). Entities live in `mso-catalog-db/`.
- **Requests DB** — `infraActiveRequests` (request status, progress, flow status) and `requestProcessingData`. Accessed via `mso-requests-db-adapter` (port 8083). Entities in `mso-api-handlers/mso-requests-db/`.

Tests use in-memory H2 (`com.h2database:h2`) or embedded MariaDB (`ch.vorburger.mariaDB4j`) depending on the module.

## Licensing & code hygiene

- `license-maven-plugin` (goal `check-file-header`) enforces Apache 2.0 headers on all `*.java` and `*.groovy` (excluding `**/com/att/**`). Build fails on missing/out-of-date headers. Use the license plugin or copy an existing header when adding files.
- `formatter-maven-plugin` is bound to `validate` (fails build on diff) and — under profile `format` — to `process-sources` (rewrites). Same applies to `pom.xml` files using `pom-format.properties`.
- Checkstyle is configured but `<skip>true</skip>` at the top level.

## Integration with the wider ONAP repo

This reactor is one of several top-level SO repos at `../`: `so-cnf-adapter`, `so-etsi-nfvo`, `so-etsi-sol003-adapter`, `so-etsi-sol005-adapter`, `so-nssmf-adapter`, `so-oof-adapter`, `so-monitoring`. They are **not** part of this reactor; they publish to Nexus and are pulled in by version property (e.g. `so-etsi-sol003-adapter-version`). When a change spans repos, check whether the dependency version needs bumping in `pom.xml`.
