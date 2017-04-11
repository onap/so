package org.openecomp.mso.bpmn.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows a subclass of WorkflowTest to specify one or more WireMock
 * response transformers.  A transformer must be declared as a public
 * static field in the subclass.  For example:
 * <pre>
 *     @WorkflowTestTransformer
 *     public static final ResponseTransformer sdncAdapterMockTransformer =
 *         new SDNCAdapterMockTransformer();
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WorkflowTestTransformer {
}