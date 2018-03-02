package org.openecomp.mso.client.policy.entities;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"DictionaryDatas"
})
public class DictionaryJson {

@JsonProperty("DictionaryDatas")
private List<DictionaryData> dictionaryDatas = new ArrayList<DictionaryData>();

@JsonProperty("DictionaryDatas")
public List<DictionaryData> getDictionaryDatas() {
return dictionaryDatas;
 }

@JsonProperty("DictionaryDatas")
public void setDictionaryDatas(List<DictionaryData> dictionaryDatas) {
this.dictionaryDatas = dictionaryDatas;
 }

public DictionaryJson withDictionaryDatas(List<DictionaryData> dictionaryDatas) {
this.dictionaryDatas = dictionaryDatas;
return this;
 }

}