package org.openecomp.mso.asdc.client.exceptions;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

import org.junit.Test;

public class ArtifactInstallerExceptionTest {
	private String exceptionMessage = "test message for exception";
	private String throwableMessage = "separate throwable that caused asdcDownloadException";
	
	@Test
	public void asdcParametersExceptionTest() {
		ArtifactInstallerException asdcDownloadException = new ArtifactInstallerException(exceptionMessage);
		
		Exception expectedException = new Exception(exceptionMessage);
		
		assertThat(asdcDownloadException, sameBeanAs(expectedException));
	}
	
	@Test
	public void asdcParametersExceptionThrowableTest() {
		Throwable throwableCause = new Throwable(throwableMessage);
		ArtifactInstallerException asdcDownloadException = new ArtifactInstallerException(exceptionMessage, throwableCause);
		
		Exception expectedException = new Exception(exceptionMessage, new Throwable(throwableMessage));
		
		assertThat(asdcDownloadException, sameBeanAs(expectedException));
	}
}
