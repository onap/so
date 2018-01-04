/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.bpmn.core;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.core.internal.VariableNameExtractor;

/**
 * Base class for service tasks.
 */
public class BaseTask implements JavaDelegate {

    /**
     * Get the value of a required field.  This method throws
     * MissingInjectedFieldException if the expression is null, and
     * BadInjectedFieldException if the expression evaluates to a null
     * value.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value
     */
    protected Object getField(Expression expression,
            DelegateExecution execution, String fieldName) {
        return getFieldImpl(expression, execution, fieldName, false);
    }

    /**
     * Gets the value of an optional field.  There are three conditions
     * in which this method returns null:
     * <p>
     * <ol>
     * <li> The expression itself is null (i.e. the field is missing
     * altogether.</li>
     * <li>The expression evaluates to a null value.</li>
     * <li>The expression references a single variable which has not
     * been set.</li>
     * </ol>
     * <p>
     * Examples:<br>
     * Expression ${x} when x is null: return null<br>
     * Expression ${x} when x is unset: return null<br>
     * Expression ${x+y} when x and/or y are unset: exception<br>
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value, possibly null
     */
    protected Object getOptionalField(Expression expression,
            DelegateExecution execution, String fieldName) {
        return getFieldImpl(expression, execution, fieldName, true);
    }

    /**
     * Get the value of a required output variable field. This method
     * throws MissingInjectedFieldException if the expression is null, and
     * BadInjectedFieldException if the expression produces a null or
     * illegal variable name.  Legal variable names contain only letters,
     * numbers, and the underscore character ('_').
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the output variable name
     */
    protected String getOutputField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, false);
        if (o instanceof String) {
            String variable = (String) o;
            if (!isLegalVariable(variable)) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), "'" + variable
                        + "' is not a legal variable name");
            }
            return variable;
        } else {
            throw new BadInjectedFieldException(
                    fieldName, getTaskName(), "expected a variable name string"
                    + ", got object of type " + o.getClass().getName());
        }
    }

    /**
     * Get the value of an optional output variable field. This method
     * throws BadInjectedFieldException if the expression produces an illegal
     * variable name.  Legal variable names contain only letters, numbers,
     * and the underscore character ('_').
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the output variable name, possibly null
     */
    protected String getOptionalOutputField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, true);
        if (o instanceof String) {
            String variable = (String) o;
            if (!isLegalVariable(variable)) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), "'" + variable
                        + "' is not a legal variable name");
            }
            return variable;
        } else if (o == null) {
            return null;
        } else {
            throw new BadInjectedFieldException(
                    fieldName, getTaskName(), "expected a variable name string"
                    + ", got object of type " + o.getClass().getName());
        }
    }

    /**
     * Get the value of a required string field.  This method throws
     * MissingInjectedFieldException if the expression is null, and
     * BadInjectedFieldException if the expression evaluates to a null
     * value.
     * <p>
     * Note: the result is coerced to a string value, if necessary.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value
     */
    protected String getStringField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, false);
        if (o instanceof String) {
            return (String) o;
        } else {
            throw new BadInjectedFieldException(
                    fieldName, getTaskName(), "cannot convert '" + o.toString()
                    + "' to Integer");
        }
    }

    /**
     * Gets the value of an optional string field.  There are three conditions
     * in which this method returns null:
     * <p>
     * <ol>
     * <li> The expression itself is null (i.e. the field is missing
     * altogether.</li>
     * <li>The expression evaluates to a null value.</li>
     * <li>The expression references a single variable which has not
     * been set.</li>
     * </ol>
     * <p>
     * Examples:<br>
     * Expression ${x} when x is null: return null<br>
     * Expression ${x} when x is unset: return null<br>
     * Expression ${x+y} when x and/or y are unset: exception<br>
     * <p>
     * Note: the result is coerced to a string value, if necessary.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value, possibly null
     */
    protected String getOptionalStringField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, true);
        if (o instanceof String) {
            return (String) o;
        } else if (o == null) {
            return null;
        } else {
            return o.toString();
        }
    }

    /**
     * Get the value of a required integer field. This method throws
     * MissingInjectedFieldException if the expression is null, and
     * BadInjectedFieldException if the expression evaluates to a null
     * value or a value that cannot be coerced to an integer.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value
     */
    protected Integer getIntegerField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, false);
        if (o instanceof Integer) {
            return (Integer) o;
        } else {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), "cannot convert '" + o.toString()
                        + "' to Integer");
            }
        }
    }

    /**
     * Gets the value of an optional integer field.  There are three conditions
     * in which this method returns null:
     * <p>
     * <ol>
     * <li> The expression itself is null (i.e. the field is missing
     * altogether.</li>
     * <li>The expression evaluates to a null value.</li>
     * <li>The expression references a single variable which has not
     * been set.</li>
     * </ol>
     * <p>
     * Examples:<br>
     * Expression ${x} when x is null: return null<br>
     * Expression ${x} when x is unset: return null<br>
     * Expression ${x+y} when x and/or y are unset: exception<br>
     * <p>
     * Note: the result is coerced to an integer value, if necessary. This
     * method throws BadInjectedFieldException if the result cannot be coerced
     * to an integer.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value, possibly null
     */
    protected Integer getOptionalIntegerField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, true);
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o == null) {
            return null;
        } else {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), "cannot convert '" + o.toString()
                        + "' to Integer");
            }
        }
    }

    /**
     * Gets the value of an optional long field.  There are three conditions
     * in which this method returns null:
     * <p>
     * <ol>
     * <li> The expression itself is null (i.e. the field is missing
     * altogether.</li>
     * <li>The expression evaluates to a null value.</li>
     * <li>The expression references a single variable which has not
     * been set.</li>
     * </ol>
     * <p>
     * Examples:<br>
     * Expression ${x} when x is null: return null<br>
     * Expression ${x} when x is unset: return null<br>
     * Expression ${x+y} when x and/or y are unset: exception<br>
     * <p>
     * Note: the result is coerced to a long value, if necessary. This
     * method throws BadInjectedFieldException if the result cannot be coerced
     * to a long.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value, possibly null
     */
    protected Long getOptionalLongField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, true);
        if (o instanceof Long) {
            return (Long) o;
        } else if (o == null) {
            return null;
        } else {
            try {
                return Long.parseLong(o.toString());
            } catch (NumberFormatException e) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), "cannot convert '" + o.toString()
                        + "' to Long");
            }
        }
    }

    /**
     * Get the value of a required long field. This method throws
     * MissingInjectedFieldException if the expression is null, and
     * BadInjectedFieldException if the expression evaluates to a null
     * value or a value that cannot be coerced to a long.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @return the field value
     */
    protected Long getLongField(Expression expression,
            DelegateExecution execution, String fieldName) {
        Object o = getFieldImpl(expression, execution, fieldName, false);
        if (o instanceof Long) {
            return (Long) o;
        } else {
            try {
                return Long.parseLong(o.toString());
            } catch (NumberFormatException e) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), "cannot convert '" + o.toString()
                        + "' to Long");
            }
        }
    }

    /**
     * Common implementation for field "getter" methods.
     *
     * @param expression the expression
     * @param execution the execution
     * @param fieldName the field name (for logging and exceptions)
     * @param optional true if the field is optional
     * @return the field value, possibly null
     */
    private Object getFieldImpl(Expression expression,
            DelegateExecution execution, String fieldName, boolean optional) {
        if (expression == null) {
            if (!optional) {
                throw new MissingInjectedFieldException(
                        fieldName, getTaskName());
            }
            return null;
        }

        Object value = null;

        try {
            value = expression.getValue(execution);
        } catch (Exception e) {
            if (!optional) {
                throw new BadInjectedFieldException(
                        fieldName, getTaskName(), e.getClass().getSimpleName(), e);
            }

            // At this point, we have an exception that occurred while
            // evaluating an expression for an optional field. A common
            // problem is that the expression is a simple reference to a
            // variable which has never been set, e.g. the expression is
            // ${x}. The normal activiti behavior is to throw an exception,
            // but we don't like that, so we have the following workaround,
            // which parses the expression text to see if it is a "simple"
            // variable reference, and if so, returns null.  If the
            // expression is anything other than a single variable
            // reference, then an exception is thrown, as it would have
            // been without this workaround.

            // Get the expression text so we can parse it
            String s = expression.getExpressionText();
            new VariableNameExtractor(s).extract().ifPresent(name -> {
                if (execution.hasVariable(name)) {
                    throw new BadInjectedFieldException(
                            fieldName, getTaskName(), e.getClass().getSimpleName(), e);
                }
            });
        }

        if (value == null && !optional) {
            throw new BadInjectedFieldException(
                    fieldName, getTaskName(), "required field has null value");
        }

        return value;
    }

    /**
     * Tests if a character is a "word" character.
     *
     * @param c the character
     * @return true if the character is a "word" character.
     */
    private static boolean isWordCharacter(char c) {
        return (Character.isLetterOrDigit(c) || c == '_');
    }

    /**
     * Tests if the specified string is a legal flow variable name.
     *
     * @param name the string
     * @return true if the string is a legal flow variable name
     */
    private boolean isLegalVariable(String name) {
        if (name == null) {
            return false;
        }

        int len = name.length();

        if (len == 0) {
            return false;
        }

        char c = name.charAt(0);

        if (!Character.isLetter(c) && c != '_') {
            return false;
        }

        for (int i = 1; i < len; i++) {
            c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the name of the task (normally the java class name).
     *
     * @return the name of the task
     */
    public String getTaskName() {
        return getClass().getSimpleName();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
    }
}
