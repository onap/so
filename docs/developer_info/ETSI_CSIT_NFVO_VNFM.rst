.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2022 Ericsson Software Technologies

SO ETSI CSIT (NFVO and VNFM)
============================
This guide will go through the CSIT of the ETSI NFVO and VNFM.

Ensure you have a healthy ONAP Deployment running. The following components will be required/used as part of this guide:

- SO
- SDC
- AAI
- DMAAP
- Modeling

What is ETSI?
-------------
The European Telecommunications Standards Institute (ETSI) produces globally-applicable standards
for Information and Communications Technologies. ETSI  set out standards covering the functionalities
of the interfaces specified on the reference points, which use the acronym NFV-SOL
(standing for “NFV Solutions”). As of ONAPs Dublin release the  SO SVNFM adapter supports
SOL003 standards for Create, Instantiate, Terminate and Delete operations with Granting, Subscription
and Notification. As of ONAP Honolulu release, the SO ETSI NFVO sub-component supports
SOL005 (NBI) and SOL003 (SBI) standards for Create, Instantiate, Terminate and Delete NS and VNF
(through the SOL003 Adapter) operations.

How to Run CSIT Tests
---------------------
The follow steps are to install and run on an Ubuntu 18.04 (desktop) installation.
Later versions of Ubuntu and Java may work for the tests.

First pull the CSIT repo from Gerrit, either with or without the hooks

.. code-block::

    git clone "https://gerrit.onap.org/r/integration/csit"

or

.. code-block::

    git clone "https://gerrit.onap.org/r/integration/csit" && (cd "csit" && mkdir -p .git/hooks && curl -Lo `git rev-parse --git-dir`/hooks/commit-msg https://gerrit.onap.org/r/tools/hooks/commit-msg; chmod +x `git rev-parse --git-dir`/hooks/commit-msg)

Once this is downloaded a few more installations are required.

Install pip (if required)

.. code-block::

    sudo apt install python-pip

Install Robot Framework through pip

.. code-block::

    pip install robotframework

Run Script

Once all of this is done, then the tests should be run by calling the run-csit.sh script and giving it the location of our test folder (csit/plans/so/integration-etsi-testing).
From the csit projects root folder run the following command:

.. code-block::

    ./run-csit.sh plans/so/integration-etsi-testing

This should successfully run the ETSI CSIT suite

How to run tests against specific SO versions
---------------------------------------------
It is possible to run the ETSI CSIT suite against local docker images although it is not the default.
Through this method specific versions of SO can be tested.
There are two changes required to make this work.
The env file, located at [containing folder]/csit/plans/so/integration-etsi-testing/config/env,
first needs to be changed. The DOCKER_ENVIROMENT needs to be changed from "remote" to "local".
Also the TAG value might need to be changed. This Tag relates to the version of images being used.

Secondly all of the required docker images must be present on system.

This should be enough to run the ETSI CSIT test suite locally.

ETSI NFVO Automated CSIT Tests High Level Scenarios
---------------------------------------------------
**Step 1:**

Perform Configuration / Setup Steps prior to running tests

**Step 2:**

Onboard SOL004 Package and SOL007 directly into ETSI Catalog using ROBOT framework

**Step 3:**

ETSI Catalog Gets Package from SDC Simulator (New: ETSI Catalog and Modeling ETSI Catalog DB will need to be set up and spun up for CSIT. May be some impact on SDC Simulator here)

**Step 4:**

ETSI Catalog Stores Package in its Modeling ETSI Catalog DB

**Step 5:**

ROBOT framework used to trigger NS LCM requests, e.g., INSTANTIATE NS

**Step 6:**

ETSI NFVO NS LCM gets required data from ETSI Catalog, e.g., Get and Parse NSD

**Step 7:**

If e.g., a CREATE NS task, ETSI NFVO NS LCM checks to see if already exists in ETSI NFVO DB

**Step 8:**

Create Generic VNF and connect to Service Instance in A&AI Simulator (May be some impact on A&AI Simulator here)

**Step 9:**

Instantiate VNF through SOL003 Adapter

**Step 10:**

SOL003 Adapter processes requests through A&AI and (May be some impact on A&AI Simulator here)

**Step 11:**

SOL003 Adapter processes requests through ETSI-Catalog

**Step 12:**

SOL003 Adapter sends notification to SOL003 NBI, etc.

What are the tests doing?
-------------------------
There are three tests currently being run "Distribute Service Template", "Invoke Service Instantiation",
"Invoke NS Instantiation", "Delete NS Instance", "Invoke VNF Instantiation", "Delete VNF Instance" and
"Delete Service Instance".

Distribute Service Template

As the name would suggest the aim for the "Distribute Service Template" test is to distribute a service
template within the SDC controller pod. Once a http session of the SDC controller is created a post request
can be made to it. This post requests sends binary data from "distributeServiceTemplate.json".
This json file contains resources and artifacts required to distribute a service. Once this post request
is sent, the response status code is checked to see if it is 200. If the code is not equal to 200 then
the test is thought to be a failure.

Invoke Service Instantiation

The aim of the "Invoke Service Instantiation" test is to invoke the service distributed to the sdc controller
in the previous test. A http session of the api handler pod is created. This session is sent a post request
containing "serviceInstantiationRequest.json". Once this request is made the response is checked if it
a valid code is returned.  A for loop is used to continually make calls to check the orchestration request,
to check the status of service instantiation. Only once this orchestration returns either a fail or success,
will we break out of the for loop.Once outside the for loop a final statement is used to check if service
has been successfully instantiated.

Invoke NS Instance

The aim of "Invoke NS Instantiation" test is to now instantiate the NS that relates to service in the
previous test. This test requires the ID of the service instance created in the previous test. If this is
not provided then the test will fail from the get go. Once again a http session of the api handler pod is
created. Similarly a post request using the json data within "nsInstantiationRequest.json".
Once this request is made if it returns a success code then the test moves on to a for loop. Within this
for a loop an orchestration request is made each time, when this request signals that either the instantiation
request has failed or fully succeeded then the loop is escaped. The test will either be a pass or fail depending
on this final orchestration request.

Delete NS Instance

This test will delete the NS Instance created in the previous test. Both the ID of the NS instance created
in the previous test and the service instance created in the test before that. If either of these values is
not provided then the test will fail. This test once again makes use of a session of the api handler pod.
A post request is made using the data from  "nsDeleteRequest.json". Once this request is made if it returns
a success code then the test moves on to a for loop. Within this for a loop an orchestration request is made
each time, when this request signals that either the instantiation request has failed or fully succeeded
then the loop is escaped. The test will either be a pass or fail depending on this final orchestration request.

Invoke VNF Instance

The aim of "Invoke VNF Instantiation" test is to now instantiate the VNF that relates to service in
the previous test. This test requires the ID of the service instance created in the previous test.
If this is not provided then the test will fail from the get go. Once again a http session of the
api handler pod is created. Similarly a post request using the json data within "vnfInstantiationRequest.json".
Once this request is made if it returns a success code then the test moves on to a for loop. Within this
for a loop an orchestration request is made each time, when this request signals that either the instantiation
request has failed or fully succeeded then the loop is escaped. The test will either be a pass or fail
depending on this final orchestration request.

Delete VNF Instance

This test will delete the VNF Instance created in the previous test. Both the ID of the vnf instance created
in the previous test and the service instance created in the test before that. If either of these values is
not provided then the test will fail. This test once again makes use of a session of the api handler pod.
A post request is made using the data from  "vnfDeleteRequest.json". Once this request is made if it returns
a success code then the test moves on to a for loop. Within this for a loop an orchestration request is made
each time, when this request signals that either the instantiation request has failed or fully succeeded then
the loop is escaped. The test will either be a pass or fail depending on this final orchestration request.

Delete Service Instance

This test will delete the service instance created in earlier test. To delete the service the ID of previously
 created Service Instance is required, if this is not supplied then the test will fail before starting.
 A post request is then made to the API handler containing data from "serviceDeleteRquest.json".
 Once this request is made if it returns a success code then the test moves on to a for loop.
 Within this for a loop an orchestration request is made each time, when this request signals that either
 the instantiation request has failed or fully succeeded then the loop is escaped. The test will either be
 a pass or fail depending on this final orchestration request.

Troubleshooting
---------------
There are a number of simple issues relating from Python and its libraries

A correct installation of the robot framework to run our tests requiring python and the following pip libraries.

- robotframework
- robotframework-extendedselenium2library
- robotframework-httplibrary
- robotframework-onap
- robotframework-requests
- robotframework-selenium2library

To make sure each of the previous libraries is installed run the following command

.. code-block::

    pip -list
