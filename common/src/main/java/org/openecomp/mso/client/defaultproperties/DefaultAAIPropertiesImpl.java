package org.openecomp.mso.client.defaultproperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openecomp.mso.client.aai.AAIProperties;
import org.openecomp.mso.client.aai.AAIVersion;

public class DefaultAAIPropertiesImpl implements AAIProperties {

	
	final Map<Object, Object> props;
	public DefaultAAIPropertiesImpl() {
		File initialFile = new File("src/test/resources/aai.properties");
		Map<Object, Object> temp;
		try {
		    InputStream targetStream = new FileInputStream(initialFile);
			Properties properties = new Properties();
			properties.load(targetStream);
			temp = properties;
		} catch (IOException e) {
			temp = new HashMap<>();
		}
		this.props = temp;

	}
	@Override
	public URL getEndpoint() throws MalformedURLException {
		return new URL(props.get("aai.endpoint").toString());
	}

	@Override
	public String getSystemName() {
		return "MSO";
	}
	@Override
	public AAIVersion getDefaultVersion() {
		return AAIVersion.LATEST;
	}

}
