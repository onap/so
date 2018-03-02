
package org.openecomp.mso.client.policy.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dictionaryType", "dictionary" })
public class DictionaryItemsRequest {

	@JsonProperty("dictionary")
	private String dictionary;
	@JsonProperty("dictionaryType")
	private String dictionaryType;

	@JsonProperty("dictionary")
	public String getDictionary() {
		return dictionary;
	}

	@JsonProperty("dictionary")
	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}

	@JsonProperty("dictionaryType")
	public String getDictionaryType() {
		return dictionaryType;
	}

	@JsonProperty("dictionaryType")
	public void setDictionaryType(String dictionaryType) {
		this.dictionaryType = dictionaryType;
	}
}
