.. This work is licensed under a Creative Commons Attribution 4.0
.. International License.  http://creativecommons.org/licenses/by/4.0
.. Copyright 2017 AT&T Intellectual Property.  All rights reserved.


Instantiate
===========
The following guides are provided to describe tasks that a user of
ONAP may need to perform when instantiating a service.

Instantiation includes the following topics:

.. toctree::
   :maxdepth: 2

   Pre-instantiation operations <./pre_instantiation/index.rst>

   Instantiation operation(s) <./instantiation/index.rst>


**e2eServiceInstance** method is a hard-coded approach with dedicated/specific
service BPMN workflow. That means it is linked to ONAP source code
and lifecycle.

**A La Carte** method requires the Operations actor to build and send
a lot of operations. To build those requests, Operator actor needs to
define/collect by himself all VNF parameters/values.

**Macro** method required the Operations actor to build and send only one
request and, thanks to CDS Blueprint templates, ONAP will collect and assign
all required parameters/values by itself.
