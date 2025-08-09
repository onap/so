/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Orange Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

class ExecutionPlan extends ExecutionCollection<ExecutionGroup> {
    private final Resource resource;
    private ExecutionGroup currentGroup = null;

    public ExecutionPlan(Resource resource, ExecutionGroup group) {
        super(resource != null ? resource.getResourceType() : WorkflowType.SERVICE);
        this.resource = resource;
        if (group != null) {
            group.addNestedPlans(Collections.singletonList(this));
        }
    }

    public void changeCurrentGroup(ExecutionGroup group) {
        if (currentGroup == null || !currentGroup.equals(group)) {
            logger.info("Change {} group[{}]", getName(), group.getName());
            if (currentGroup != null)
                currentGroup.flushBlocksFromCache(this.blocksBuiltCache);
        }
        currentGroup = group;
    }

    Resource getResource() {
        return resource;
    }

    protected String getName() {
        return super.getName() + "["
                + (resource != null ? (resource.getProcessingPriority() + ", " + resource.getResourceId()) : "") + "]";
    }

    public static ExecutionPlan build(List<Resource> resourceList, boolean ascendingOrder) {
        ExecutionPlan plan = new ExecutionPlan(null, null);
        buildExecutionPlan(plan, resourceList, ascendingOrder);
        if (plan.getNestedExecutions().size() == 1
                && plan.getNestedExecutions().get(0).getNestedExecutions().size() == 1)
            plan = plan.getNestedExecutions().get(0).getNestedExecutions().get(0);
        return plan;
    }

    private static void buildExecutionPlan(ExecutionPlan plan, List<Resource> resourceList, boolean ascendingOrder) {
        Map<WorkflowType, List<Resource>> resourceGroups = new TreeMap<>();
        for (Resource resource : resourceList) {
            if (!resourceGroups.containsKey(resource.getResourceType())) {
                resourceGroups.put(resource.getResourceType(), new ArrayList<>());
            }
            resourceGroups.get(resource.getResourceType()).add(resource);
        }
        for (WorkflowType type : resourceGroups.keySet()) {
            ExecutionGroup nestedGroup = new ExecutionGroup(type, plan);
            List<Resource> resourceGroupSorted = resourceGroups.get(type).stream()
                    .sorted(ascendingOrder ? Resource.sortByPriorityAsc : Resource.sortByPriorityDesc)
                    .collect(Collectors.toList());
            for (Resource resource : resourceGroupSorted) {
                ExecutionPlan planInGroup = new ExecutionPlan(resource, nestedGroup);
                if (resource.getChildren().size() > 0)
                    buildExecutionPlan(planInGroup, resource.getChildren(), ascendingOrder);
            }
        }
    }
}


class ExecutionGroup extends ExecutionCollection<ExecutionPlan> {

    public ExecutionGroup(WorkflowType groupType, ExecutionPlan plan) {
        super(groupType);
        plan.addNestedPlans(Collections.singletonList(this));
    }
}


class ExecutionCollection<T extends ExecutionCollection<?>> {

    protected static final Logger logger = LoggerFactory.getLogger(ExecutionCollection.class);

    protected final WorkflowType type;
    protected List<ExecuteBuildingBlock> blocksBuiltCache;
    protected final List<T> nestedExecutions;

    public ExecutionCollection(WorkflowType type) {
        this.type = type;
        this.nestedExecutions = new ArrayList<>();
        this.blocksBuiltCache = new ArrayList<>();
    }

    public WorkflowType getType() {
        return type;
    }

    public List<T> getNestedExecutions() {
        return nestedExecutions;
    }

    public void addNestedPlans(List<T> executions) {
        nestedExecutions.addAll(executions);
    }

    public void pushBlockToCache(List<ExecuteBuildingBlock> blocksCache) {
        if (blocksCache.size() == 0)
            return;
        this.flushNestedBlocksToCache();
        String blocks =
                blocksCache.stream().map(x -> x.getBuildingBlock().getBpmnFlowName() + ", ").reduce("", String::concat);
        blocks = blocks.substring(0, blocks.length() - 2);
        logger.info("Push {} ({}) blocks [{}]", getName(), blocksCache.size(), blocks);
        this.blocksBuiltCache.addAll(blocksCache);
    }

    private void flushNestedBlocksToCache() {
        for (T collection : nestedExecutions) {
            collection.flushBlocksFromCache(this.blocksBuiltCache);
        }
    }

    public void flushBlocksFromCache(List<ExecuteBuildingBlock> blockList) {
        flushNestedBlocksToCache();
        if (this.blocksBuiltCache.size() > 0) {
            String blocks = this.blocksBuiltCache.stream().map(x -> x.getBuildingBlock().getBpmnFlowName() + ", ")
                    .reduce("", String::concat);
            blocks = blocks.substring(0, blocks.length() - 2);
            logger.info("Flush {} ({}) blocks [{}]", getName(), blocksBuiltCache.size(), blocks);
            blockList.addAll(this.blocksBuiltCache);
            this.blocksBuiltCache.clear();
        }
    }

    public int getCacheSize() {
        return blocksBuiltCache.size()
                + getNestedExecutions().stream().mapToInt(x -> x.getCacheSize()).reduce(0, Integer::sum);
    }

    protected String getName() {
        return type.name();
    }
}
