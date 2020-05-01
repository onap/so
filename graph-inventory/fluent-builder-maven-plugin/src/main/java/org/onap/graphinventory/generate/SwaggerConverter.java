package org.onap.graphinventory.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.plugin.logging.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.parser.SwaggerParser;

public class SwaggerConverter {

    private final Log log;

    public SwaggerConverter(Log log) {
        this.log = log;
    }

    public Map<String, ObjectType> getDoc(String swaggerLocation) throws JsonProcessingException {
        Swagger swagger = new SwaggerParser().read(swaggerLocation);

        Map<String, Path> paths = swagger.getPaths().entrySet().stream()
                .filter(item -> !item.getKey().endsWith("/relationship-list/relationship"))
                .collect(Collectors.toMap(item -> item.getKey(), item -> item.getValue()));

        Pattern pluralPattern = Pattern.compile(".*(?<partial>/(?<name>[^{]*$))");
        Pattern singularPattern = Pattern.compile(".*(?<partial>/(?<name>[^/{}]*)/\\{.*$)");
        Pattern topLevelPattern = Pattern.compile("^/([^/]+)/.*");
        Pattern urlTemplatePattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher pluralMatcher;
        Matcher singularMatcher;
        Matcher topLevelMatcher;

        Map<String, ObjectType> output = new HashMap<>();
        for (Map.Entry<String, Path> entry : paths.entrySet()) {

            pluralMatcher = pluralPattern.matcher(entry.getKey());
            singularMatcher = singularPattern.matcher(entry.getKey());
            topLevelMatcher = topLevelPattern.matcher(entry.getKey());
            ObjectType item;
            if (pluralMatcher.matches()) {
                if (!output.containsKey(pluralMatcher.group("name"))) {
                    output.put(pluralMatcher.group("name"), new ObjectType());
                }
                item = output.get(pluralMatcher.group("name"));
                item.setType("plural");
                item.setName(pluralMatcher.group("name"));
                item.setPartialUri(pluralMatcher.group("partial"));
                item.getPaths().add(entry.getKey());

                if (topLevelMatcher.matches()) {
                    item.setTopLevel(topLevelMatcher.group(1));
                    if (!output.containsKey(topLevelMatcher.group(1))) {
                        output.put(topLevelMatcher.group(1), new ObjectType());
                        output.get(topLevelMatcher.group(1)).setType("top level");
                        output.get(topLevelMatcher.group(1)).setName(topLevelMatcher.group(1));
                        output.get(topLevelMatcher.group(1)).setPartialUri("/" + topLevelMatcher.group(1));
                        output.get(topLevelMatcher.group(1)).getPaths().add("/" + topLevelMatcher.group(1));

                    }
                }
            } else if (singularMatcher.matches()) {

                if (!output.containsKey(singularMatcher.group("name"))) {
                    output.put(singularMatcher.group("name"), new ObjectType());

                    item = output.get(singularMatcher.group("name"));

                    item.setType("singular");
                    item.setName(singularMatcher.group("name"));
                    item.setPartialUri(singularMatcher.group("partial"));

                    item.getPaths().add(entry.getKey());

                    if (topLevelMatcher.matches()) {
                        item.setTopLevel(topLevelMatcher.group(1));
                        if (!output.containsKey(topLevelMatcher.group(1))) {
                            output.put(topLevelMatcher.group(1), new ObjectType());
                            output.get(topLevelMatcher.group(1)).setType("top level");
                            output.get(topLevelMatcher.group(1)).setName(topLevelMatcher.group(1));
                            output.get(topLevelMatcher.group(1)).setPartialUri("/" + topLevelMatcher.group(1));
                            output.get(topLevelMatcher.group(1)).getPaths().add("/" + topLevelMatcher.group(1));
                        }
                    }
                    List<Parameter> parameters = entry.getValue().getGet().getParameters();

                    if (parameters != null) {
                        parameters.stream().filter(param -> "path".equals(param.getIn())).collect(Collectors.toList());
                    }
                    for (Parameter p : parameters) {
                        ObjectField field = new ObjectField();

                        field.setName(p.getName());
                        field.setType(((SerializableParameter) p).getType());
                        item.getFields().add(field);
                    }

                } else {
                    item = output.get(singularMatcher.group("name"));
                    if (singularMatcher.group("partial").contains(item.getName() + ".")) {
                        item.setPartialUri(singularMatcher.group("partial"));
                    }
                    item.getPaths().add(entry.getKey());
                }
            }

        }
        ObjectMapper mapper = new ObjectMapper();

        for (ObjectType item : output.values()) {
            for (String path : item.getPaths()) {
                String partialUriReplacer = item.getPartialUri().replaceAll("\\{[^}]+\\}", "[^/]+");
                String childParentUri = path.replaceFirst(partialUriReplacer + "$", "");
                for (ObjectType itemToUpdate : output.values()) {
                    if (itemToUpdate.getPaths().stream()
                            .anyMatch(itemToUpdateUri -> itemToUpdateUri.equals(childParentUri))) {
                        itemToUpdate.getChildren().add(item.getName());
                    }
                }
            }
        }

        log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output));

        for (Map.Entry<String, ObjectType> item : output.entrySet()) {

            if (item.getValue().getType().equals("plural")) {
                Set<String> children = item.getValue().getChildren();
                // should only be one
                if (!children.isEmpty()) {
                    item.getValue().setAdditionalName(children.iterator().next());
                }
            }
            Set<String> pluralChildren = new HashSet<>();
            for (String child : item.getValue().getChildren()) {
                if (output.get(child).getType().equals("plural")) {
                    Set<String> children = output.get(child).getChildren();
                    pluralChildren.addAll(children);
                }
            }
            item.getValue().getChildren().addAll(pluralChildren);

            if (item.getValue().getType().equals("plural")) {
                for (String child : item.getValue().getChildren()) {
                    output.get(child)
                            .setPartialUri(item.getValue().getPartialUri() + output.get(child).getPartialUri());
                }
            }

            if (!item.getValue().getFields().isEmpty()) {
                Matcher templates = urlTemplatePattern.matcher(item.getValue().getPartialUri());
                List<String> localFields = new ArrayList<>();
                while (templates.find()) {
                    localFields.add(templates.group(1));
                }
                item.getValue().setFields(item.getValue().getFields().stream()
                        .filter(f -> localFields.contains(f.getName())).collect(Collectors.toList()));
            }
        }

        output.values().stream().filter(item -> item.getType().equals("plural"))
                .forEach(item -> item.getChildren().clear());

        return output;
    }
}
