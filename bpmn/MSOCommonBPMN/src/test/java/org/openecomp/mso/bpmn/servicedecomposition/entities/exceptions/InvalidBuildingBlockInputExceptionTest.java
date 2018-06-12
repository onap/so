package org.openecomp.mso.bpmn.servicedecomposition.entities.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InvalidBuildingBlockInputExceptionTest {
	private static final String MESSAGE = "message";
	private static final Throwable CAUSE = new Throwable();
	private InvalidBuildingBlockInputException invalidBuildingBlockInputException;
	
	@Test
	public void defaultConstructorTest() {
		invalidBuildingBlockInputException = new InvalidBuildingBlockInputException();
		assertEquals(null, invalidBuildingBlockInputException.getMessage());
		assertEquals(null, invalidBuildingBlockInputException.getCause());
	}
	
	@Test
	public void messageConstructorTest() {
		invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(MESSAGE);
		assertEquals(MESSAGE, invalidBuildingBlockInputException.getMessage());
		assertEquals(null, invalidBuildingBlockInputException.getCause());
	}
	
	@Test
	public void causeConstructorTest() {
		invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(CAUSE);
		assertEquals(CAUSE.toString(), invalidBuildingBlockInputException.getMessage()); // CAUSE.toString because of the implementation of Exception(Throwable cause)
		assertEquals(CAUSE, invalidBuildingBlockInputException.getCause());
	}
	
	@Test
	public void messageAndCauseConstructorTest() {
		invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(MESSAGE, CAUSE);
		assertEquals(MESSAGE, invalidBuildingBlockInputException.getMessage());
		assertEquals(CAUSE, invalidBuildingBlockInputException.getCause());
	}
	
	@Test
	public void messageAndCauseAndFlagsConstructorTest() {
		invalidBuildingBlockInputException = new InvalidBuildingBlockInputException(MESSAGE, CAUSE, true, true);
		assertEquals(MESSAGE, invalidBuildingBlockInputException.getMessage());
		assertEquals(CAUSE, invalidBuildingBlockInputException.getCause());
	}

}
