package org.openecomp.mso.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

public class ResponseExceptionMapperImpl extends ResponseExceptionMapper {

	@Override
	public Optional<String> extractMessage(InputStream stream) throws IOException {
		final String input = IOUtils.toString(stream, "UTF-8");
		IOUtils.closeQuietly(stream);
		return Optional.of(input);
	}
	

}
