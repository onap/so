/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.validation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.so.client.exception.ExceptionBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * Controls running all pre and post validation for workflows.
 * 
 * To define a validation you must make it a spring bean and implement either {@link org.onap.so.bpmn.common.validation.PreWorkflowValidator} or 
 * {@link org.onap.so.bpmn.common.validation.PostWorkflowValidator} your validation will automatically be
 * run by this class.
 *
 */
@Component
public class WorkflowValidatorRunner {

	private static Logger logger = LoggerFactory.getLogger(WorkflowValidatorRunner.class);
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	
	private List<PreWorkflowValidator> preWorkflowValidators;
	private List<PostWorkflowValidator> postWorkflowValidators;

	
	@PostConstruct
	protected void init() {
		
		preWorkflowValidators = new ArrayList<>(
				Optional.ofNullable(context.getBeansOfType(PreWorkflowValidator.class)).orElse(new HashMap<>()).values());
		postWorkflowValidators = new ArrayList<>(
				Optional.ofNullable(context.getBeansOfType(PostWorkflowValidator.class)).orElse(new HashMap<>()).values());
	}
	
	public boolean preValidate(String workflow, DelegateExecution execution) {
		return validate(preWorkflowValidators, workflow, execution);
	}
	
	
	public boolean postValidate(String workflow, DelegateExecution execution) {
		return validate(postWorkflowValidators, workflow, execution);
	}
	
	
	protected boolean validate(List<? extends WorkflowValidator> validators, String workflow, DelegateExecution execution) {
		List<Pair<String, Boolean>> results = runValidations(validators, workflow, execution);
		
		if (!results.isEmpty()) {
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
					"Failed Validations:\n" + results.stream().map(item -> item.getValue0()).collect(Collectors.joining("\n")));
		}
		
		return true;
		
	}
	protected List<Pair<String, Boolean>> runValidations(List<? extends WorkflowValidator> validators, String workflow, DelegateExecution execution) {
		
		List<WorkflowValidator> filtered = filterValidators(validators, workflow);
		
		List<Pair<String,Boolean>> results = new ArrayList<>();
		filtered.forEach(item -> results.add(new Pair<>(item.getClass().getName(), item.validate(execution))));
		
		return results.stream().filter(item -> item.getValue1().equals(false)).collect(Collectors.toList());
	}
	
	protected List<WorkflowValidator> filterValidators(List<? extends WorkflowValidator> validators, String workflow) {
		return validators.stream()
				.filter(item -> {
					return item.forWorkflowAction().contains(workflow);
				})
				.sorted(Comparator.comparing(item -> {
					Priority p = Optional.ofNullable(item.getClass().getAnnotation(Priority.class)).orElse(new Priority() {
						public int value() {
							return 1000;
						}

						@Override
						public Class<? extends Annotation> annotationType() {
							return Priority.class;
						}
					});
					return p.value();
				})).collect(Collectors.toList());
	}
	
	protected <T> List<T> buildalidatorList(Reflections reflections, Class<T> clazz) {
		List<T> result = new ArrayList<>();
		try {
			for (Class<? extends T> klass : reflections.getSubTypesOf(clazz)) {
				result.add(klass.newInstance());
			}
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("failed to build validator list for " + clazz.getName(), e);
			throw new RuntimeException(e);
		}
		
		return result;
	}
	
	protected List<PreWorkflowValidator> getPreWorkflowValidators() {
		return this.preWorkflowValidators;
	}
	
	protected List<PostWorkflowValidator> getPostWorkflowValidators() {
		return this.postWorkflowValidators;
	}
	
}
