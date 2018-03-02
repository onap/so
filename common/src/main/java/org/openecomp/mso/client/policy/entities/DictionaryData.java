package org.openecomp.mso.client.policy.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"bbid",
"workstep",
"treatments"
})
public class DictionaryData {

@JsonProperty("id")
private Id id;
@JsonProperty("bbid")
private Bbid bbid;
@JsonProperty("workstep")
private Workstep workstep;
@JsonProperty("treatments")
private Treatments treatments;

@JsonProperty("id")
public Id getId() {
return id;
 }

@JsonProperty("id")
public void setId(Id id) {
this.id = id;
 }

public DictionaryData withId(Id id) {
this.id = id;
return this;
 }

@JsonProperty("bbid")
public Bbid getBbid() {
return bbid;
 }

@JsonProperty("bbid")
public void setBbid(Bbid bbid) {
this.bbid = bbid;
 }

public DictionaryData withBbid(Bbid bbid) {
this.bbid = bbid;
return this;
 }

@JsonProperty("workstep")
public Workstep getWorkstep() {
return workstep;
 }

@JsonProperty("workstep")
public void setWorkstep(Workstep workstep) {
this.workstep = workstep;
 }

public DictionaryData withWorkstep(Workstep workstep) {
this.workstep = workstep;
return this;
 }

@JsonProperty("treatments")
public Treatments getTreatments() {
return treatments;
 }

@JsonProperty("treatments")
public void setTreatments(Treatments treatments) {
this.treatments = treatments;
 }

public DictionaryData withTreatments(Treatments treatments) {
this.treatments = treatments;
return this;
 }

}