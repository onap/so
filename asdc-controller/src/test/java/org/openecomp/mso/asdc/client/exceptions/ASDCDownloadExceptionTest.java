package org.openecomp.mso.asdc.client.exceptions;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

import org.junit.Test;

public class ASDCDownloadExceptionTest {
	private String exceptionMessage = "test message for exception";
	private String throwableMessage = "separate throwable that caused asdcDownloadException";
	
	@Test
	public void asdcDownloadExceptionTest() {
		ASDCDownloadException asdcDownloadException = new ASDCDownloadException(exceptionMessage);
		
		Exception expectedException = new Exception(exceptionMessage);
		
		assertThat(asdcDownloadException, sameBeanAs(expectedException));
	}
	
	@Test
	public void asdcDownloadExceptionThrowableTest() {
		Throwable throwableCause = new Throwable(throwableMessage);
		ASDCDownloadException asdcDownloadException = new ASDCDownloadException(exceptionMessage, throwableCause);
		
		Exception expectedException = new Exception(exceptionMessage, new Throwable(throwableMessage));
		
		assertThat(asdcDownloadException, sameBeanAs(expectedException));
	}
}
