# SO Monitoring

----

## Introduction

SO Monitoring provides multiple useful features:
* Search/Filtering Menu
* A graphical user interface
* Workflow pathing
* Subflow navigation
* Access to the workflow variables

## Compiling / Running

Compiling is simple: `mvn clean install`
Compilation may fail if your code is not formatted properly. In order to format run 

## Components

### so-monitoring-handler



### so-monitoring-service

Backend API for so-monitoring. Requires basic auth to access it.

Default credentials:
- with role GUI-Client: gui/password1$

Note that these default users should be changed for production.

### so-monitoring-ui

UI for so-monitoring
