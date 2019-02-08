package org.onap.so.client.aai.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;


public class DSLNodeKey implements QueryStep {

	private boolean not = false;
	private final StringBuilder query = new StringBuilder();
	private final String keyName;
	private final List<String> values;
	public DSLNodeKey(String keyName, String... value) {

		this.keyName = keyName;
		this.values = Arrays.asList(value);
	}
	
	public DSLNodeKey not() {
		
		this.not = true;
		return this;
	}
	
	@Override
	public String build() {
		
		if (not) {
			query.append(" !");
		}
		query.append("('").append(keyName).append("', ");
		List<String> temp = new ArrayList<>();
		for (String item : values) {
			if (item.equals("null")) {
				temp.add(String.format("' %s '", item));
			} else if (item.equals("")){
				temp.add("' '");
			} else {
				temp.add(String.format("'%s'", item));
			}
		}
		query.append(Joiner.on(", ").join(temp)).append(")");
		
		return query.toString();
	}
}
