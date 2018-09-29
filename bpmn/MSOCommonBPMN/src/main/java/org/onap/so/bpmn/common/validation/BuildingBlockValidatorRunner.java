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

import org.camunda.bpm.engine.delegate.BpmnError;
import org.javatuples.Pair;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * Controls running all pre and post validation for building blocks.
 * 
 * To define a validation you must make it a spring bean and implement either {@link org.onap.so.bpmn.common.validation.PreBuildingBlockValidator} or 
 * {@link org.onap.so.bpmn.common.validation.PostBuildingBlockValidator} your validation will automatically be
 * run by this class.
 *
 */
@Component
public class BuildingBlockValidatorRunner {

	private static Logger logger = LoggerFactory.getLogger(BuildingBlockValidatorRunner.class);
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	
	private List<PreBuildingBlockValidator> preBuildingBlockValidators;
	private List<PostBuildingBlockValidator> postBuildingBlockValidators;

	
	@PostConstruct
	protected void init() {
		
		preBuildingBlockValidators = new ArrayList<>(
				Optional.ofNullable(context.getBeansOfType(PreBuildingBlockValidator.class)).orElse(new HashMap<>()).values());
		postBuildingBlockValidators = new ArrayList<>(
				Optional.ofNullable(context.getBeansOfType(PostBuildingBlockValidator.class)).orElse(new HashMap<>()).values());
	}
	
	public boolean preValidate(String bbName, BuildingBlockExecution execution) {
		return validate(preBuildingBlockValidators, bbName, execution);
	}
	
	
	public boolean postValidate(String bbName, BuildingBlockExecution execution) {
		return validate(postBuildingBlockValidators, bbName, execution);
	}
	
	
	protected boolean validate(List<? extends BuildingBlockValidator> validators, String bbName, BuildingBlockExecution execution) {
		List<Pair<String, Boolean>> results = runValidations(validators, bbName, execution);
		
		if (!results.isEmpty()) {
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
					"Failed Validations:\n" + results.stream().map(item -> item.getValue0()).collect(Collectors.joining("\n")));
		}
		
		return true;
		
	}
	protected List<Pair<String, Boolean>> runValidations(List<? extends BuildingBlockValidator> validators, String bbName, BuildingBlockExecution execution) {
		
		List<BuildingBlockValidator> filtered = filterValidators(validators, bbName);
		
		List<Pair<String,Boolean>> results = new ArrayList<>();
		filtered.forEach(item -> results.add(new Pair<>(item.getClass().getName(), item.validate(execution))));
		
		return results.stream().filter(item -> item.getValue1().equals(false)).collect(Collectors.toList());
	}
	
	protected List<BuildingBlockValidator> filterValidators(List<? extends BuildingBlockValidator> validators, String bbName) {
		return validators.stream()
				.filter(item -> {
					return item.forBuildingBlock().contains(bbName);
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
	
	protected List<PreBuildingBlockValidator> getPreBuildingBlockValidators() {
		return this.preBuildingBlockValidators;
	}
	
	protected List<PostBuildingBlockValidator> getPostBuildingBlockValidators() {
		return this.postBuildingBlockValidators;
	}
	
}
