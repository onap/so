package org.openecomp.mso.client.dmaap.rest;

import java.util.Properties;

public class PropertiesBean {

	private String username;
	private String password;
	private String environment;
	private String partition;
	private String contentType;
	private String host;
	private String topic;
	private String timeout;
	
	
	public PropertiesBean(Properties properties) {
		this.withUsername(properties.getProperty("username"))
		.withPassword(properties.getProperty("password"))
		.withTopic(properties.getProperty("topic"))
		.withEnvironment(properties.getProperty("environment"))
		.withHost(properties.getProperty("host"))
		.withTimeout(properties.getProperty("timeout", "20000"))
		.withPartition(properties.getProperty("partition"))
		.withContentType(properties.getProperty("contentType", "application/json"));
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public PropertiesBean withUsername(String username) {
		this.username = username;
		return this;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public PropertiesBean withPassword(String password) {
		this.password = password;
		return this;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public PropertiesBean withEnvironment(String environment) {
		this.environment = environment;
		return this;
	}
	public String getPartition() {
		return partition;
	}
	public void setPartition(String partition) {
		this.partition = partition;
	}
	public PropertiesBean withPartition(String partition) {
		this.partition = partition;
		return this;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public PropertiesBean withContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public PropertiesBean withHost(String host) {
		this.host = host;
		return this;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public PropertiesBean withTopic(String topic) {
		this.topic = topic;
		return this;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
	public PropertiesBean withTimeout(String timeout) {
		this.timeout = timeout;
		return this;
	}
	
	
	
	
}
