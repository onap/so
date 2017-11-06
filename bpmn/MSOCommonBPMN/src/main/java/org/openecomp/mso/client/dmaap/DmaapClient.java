package org.openecomp.mso.client.dmaap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public abstract class DmaapClient {
	
	protected final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
	protected final Map<String, String> msoProperties;
	protected final Properties properties;
	public DmaapClient(String filepath) throws FileNotFoundException, IOException {
		Resource resource = new ClassPathResource(filepath);
		DmaapProperties dmaapProperties = DmaapPropertiesLoader.getInstance().getImpl();
		if (dmaapProperties == null) {
			dmaapProperties = new DefaultDmaapPropertiesImpl();
		}
		this.msoProperties = dmaapProperties.getProperties();
		this.properties = new Properties();
		this.properties.load(resource.getInputStream());
		this.properties.put("password", this.deobfuscatePassword(this.getPassword()));
		this.properties.put("username", this.getUserName());
		this.properties.put("topic", this.getTopic());
	}
	protected String deobfuscatePassword(String password) {
		
		try {
			return new String(Base64.getDecoder().decode(password.getBytes()));
		} catch(IllegalArgumentException iae) {
			
			return password;
		}
	}
	
	
	public abstract String getUserName();
	public abstract String getPassword();
	public abstract String getTopic();
}
