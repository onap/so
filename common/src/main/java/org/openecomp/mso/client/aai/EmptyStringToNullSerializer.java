package org.openecomp.mso.client.aai;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EmptyStringToNullSerializer extends StdSerializer<String> {

	private static final long serialVersionUID = 5367385969270400106L;

	public EmptyStringToNullSerializer() {
		this(null);
	}
	public EmptyStringToNullSerializer(Class<String> t) {
		super(t);
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {

		if("".equals(value)) {
			gen.writeNull();
		} else {
			gen.writeString(value);
		}
	}

}
