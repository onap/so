package org.onap.graphinventory.generate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.logging.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class FluentGenerator {

    private final Map<String, ObjectType> doc;
    private final String location;
    private final String CLASSPATH;

    private final String singularBuilderClass;
    private final String pluralBuilderClass;
    private final String topLevelBuilderClass;
    private final String baseBuilderClass;
    private final String singularClass;
    private final String pluralClass;
    private final String builderName;
    private final String nameClass;
    private final String singleFragmentClass;
    private final String pluralFragmentClass;

    public FluentGenerator(Log log, String location, String destinationClasspath, String swaggerLocation,
            String builderName, String singularBuilderClass, String pluralBuilderClass, String topLevelBuilderClass,
            String baseBuilderClass, String singularClass, String pluralClass, String nameClass,
            String singleFragmentClass, String pluralFragmentClass) throws JsonProcessingException {

        this.location = location;
        this.CLASSPATH = destinationClasspath;
        this.builderName = builderName;
        this.singularBuilderClass = singularBuilderClass;
        this.pluralBuilderClass = pluralBuilderClass;
        this.topLevelBuilderClass = topLevelBuilderClass;
        this.baseBuilderClass = baseBuilderClass;
        this.singularClass = singularClass;
        this.pluralClass = pluralClass;
        this.nameClass = nameClass;
        this.singleFragmentClass = singleFragmentClass;
        this.pluralFragmentClass = pluralFragmentClass;
        System.setProperty("maxYamlCodePoints", "999999999");
        doc = new SwaggerConverter(log).getDoc(swaggerLocation);
    }

    public void run() throws IOException {
        List<JavaFile> files = new ArrayList<>();
        for (Entry<String, ObjectType> entry : doc.entrySet()) {
            // String key = "routing-instance";
            // ObjectType oType = test;
            String key = entry.getKey();
            ObjectType oType = entry.getValue();
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED);
            List<ParameterSpec> constructorParams = new ArrayList<>();
            List<FieldSpec> classFields = new ArrayList<>();

            if (!oType.getType().equals("top level")) {
                Pair<String, String> path = splitClasspath(this.baseBuilderClass);
                ClassName parameterizedTypeName = ClassName.get(path.getLeft(), path.getRight());
                constructorParams.add(ParameterSpec.builder(parameterizedTypeName, "parentObj").build());
                classFields.add(FieldSpec.builder(parameterizedTypeName, "parentObj")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build());
            }
            List<ParameterSpec> typeParams = new ArrayList<>();

            for (ObjectField oF : oType.getFields()) {
                if (oF.getType().equals("string")) {
                    typeParams.add(ParameterSpec.builder(String.class, lowerCamel(makeValidJavaVariable(oF.getName())))
                            .build());
                    classFields.add(FieldSpec.builder(String.class, lowerCamel(makeValidJavaVariable(oF.getName())))
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build());
                } else if (oF.getType().equals("integer")) {
                    typeParams.add(
                            ParameterSpec.builder(int.class, lowerCamel(makeValidJavaVariable(oF.getName()))).build());
                    classFields.add(FieldSpec.builder(int.class, lowerCamel(makeValidJavaVariable(oF.getName())))
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build());
                }
            }
            constructorParams.addAll(typeParams);
            constructor.addParameters(constructorParams);
            for (ParameterSpec p : constructorParams) {
                constructor.addStatement("this.$L = $L", p.name, p.name);

            }
            List<MethodSpec> methods = new ArrayList<>();
            methods.add(constructor.build());

            methods.addAll(createChildMethods(oType));

            methods.addAll(createInterfaceMethods(oType, typeParams));

            ClassName superType = null;
            if (oType.getType().equals("top level")) {
                Pair<String, String> path = splitClasspath(this.topLevelBuilderClass);
                superType = ClassName.get(path.getLeft(), path.getRight());
            } else {
                if (oType.getType().equals("singular")) {
                    Pair<String, String> path = splitClasspath(this.singularBuilderClass);
                    superType = ClassName.get(path.getLeft(), path.getRight());

                } else if (oType.getType().equals("plural")) {
                    Pair<String, String> path = splitClasspath(this.pluralBuilderClass);
                    superType = ClassName.get(path.getLeft(), path.getRight());
                }
            }

            TypeSpec type = TypeSpec.classBuilder(upperCamel(key)).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addType(createInnerInfoClass(oType)).addSuperinterface(superType).addFields(classFields)
                    .addMethods(methods).build();

            files.add(JavaFile.builder(CLASSPATH, type).build());

        }

        files.add(createBuilderClass());

        files.stream().forEach(javaFile -> {
            try {
                javaFile.writeTo(Paths.get(location, "fluent"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    protected List<MethodSpec> createInterfaceMethods(ObjectType oType, List<ParameterSpec> typeParams) {

        List<MethodSpec> methods = new ArrayList<>();

        CodeBlock.Builder uriTemplateCodeBlock = CodeBlock.builder();
        if (!oType.getType().equals("top level")) {
            uriTemplateCodeBlock.add("return this.parentObj.uriTemplate() + Info.partialUri");
        } else {
            uriTemplateCodeBlock.add("return Info.partialUri");

        }
        methods.add(MethodSpec.methodBuilder("uriTemplate").returns(String.class).addModifiers(Modifier.PUBLIC)
                .addStatement(uriTemplateCodeBlock.build()).addAnnotation(Override.class).build());

        ClassName arrayUtils = ClassName.get(ArrayUtils.class);

        CodeBlock.Builder valuesReturn = CodeBlock.builder();

        if (oType.getType().equals("top level")) {
            valuesReturn.add("return new Object[0]");
        } else {
            if (!typeParams.isEmpty()) {
                valuesReturn.add("return $T.addAll(this.parentObj.values(), $L)", arrayUtils, String.join(", ",
                        typeParams.stream().map(item -> "this." + item.name).collect(Collectors.toList())));
            } else {
                valuesReturn.add("return this.parentObj.values()");
            }
        }
        methods.add(MethodSpec.methodBuilder("values").returns(Object[].class).addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class).addStatement(valuesReturn.build()).build());

        if (!oType.getType().equals("top level")) {
            ClassName returnType = null;
            CodeBlock.Builder block = CodeBlock.builder();
            if (oType.getType().equals("singular")) {
                Pair<String, String> path = splitClasspath(this.singularClass);
                returnType = ClassName.get(path.getLeft(), path.getRight());
                block.add("return new $T(this.parentObj.uriTemplate(), Info.partialUri, Info.name, false)", returnType);
            } else if (oType.getType().equals("plural")) {
                Pair<String, String> path = splitClasspath(this.pluralClass);
                returnType = ClassName.get(path.getLeft(), path.getRight());
                block.add("return new $T(Info.name, this.parentObj.uriTemplate(), Info.partialUri)", returnType);
            }

            methods.add(MethodSpec.methodBuilder("build").returns(returnType).addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class).addStatement(block.build()).build());

        }

        return methods;
    }

    protected List<MethodSpec> createChildMethods(ObjectType oType) {
        List<MethodSpec> methods = new ArrayList<>();
        for (String child : oType.getChildren()) {
            methods.add(createAccessMethod(doc.get(child), true, false));
        }

        return methods;
    }

    protected MethodSpec createAccessMethod(ObjectType oType, boolean isChild, boolean isStatic) {

        ClassName childClass = ClassName.get(CLASSPATH, upperCamel(oType.getName()));
        MethodSpec.Builder b = MethodSpec.methodBuilder(lowerCamel(oType.getName())).returns(childClass);
        List<Modifier> modifiers = new ArrayList<>();
        if (isStatic) {
            modifiers.add(Modifier.STATIC);
        }
        modifiers.add(Modifier.PUBLIC);
        b.addModifiers(modifiers);
        List<ParameterSpec> params = new ArrayList<>();
        for (ObjectField oF : doc.get(oType.getName()).getFields()) {
            if (oF.getType().equals("string")) {
                params.add(
                        ParameterSpec.builder(String.class, lowerCamel(makeValidJavaVariable(oF.getName()))).build());
            } else if (oF.getType().equals("integer")) {
                params.add(ParameterSpec.builder(int.class, lowerCamel(makeValidJavaVariable(oF.getName()))).build());
            }
        }
        List<String> paramNames = params.stream().map(item -> item.name).collect(Collectors.toList());
        if (isChild) {
            paramNames.add(0, "this");
        }
        b.addParameters(params).addStatement("return new $T($L)", childClass, String.join(", ", paramNames));

        return b.build();
    }

    protected JavaFile createBuilderClass() {

        List<MethodSpec> methods = doc.values().stream().filter(item -> item.getType().equals("top level"))
                .map(item -> createAccessMethod(item, false, true)).collect(Collectors.toList());

        TypeSpec type = TypeSpec.classBuilder(this.builderName).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(methods).addType(createTypes()).build();

        return JavaFile.builder(CLASSPATH, type).build();

    }

    protected TypeSpec createTypes() {
        List<FieldSpec> params = doc.values().stream()
                .filter(item -> item.getType().equals("singular") || item.getType().equals("plural"))
                .sorted(Comparator.comparing(item -> item.getName())).map(item -> {
                    ClassName nameType =
                            ClassName.get(CLASSPATH, CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, item.getName()))
                                    .nestedClass("Info");
                    FieldSpec field = FieldSpec
                            .builder(nameType, CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, item.getName()),
                                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("new $T()", nameType).build();
                    return field;
                }).collect(Collectors.toList());

        TypeSpec type = TypeSpec.classBuilder("Types").addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .addFields(params).build();

        return type;

    }

    protected TypeSpec createInnerInfoClass(ObjectType oType) {
        List<FieldSpec> classFields = new ArrayList<>();
        List<MethodSpec> methods = new ArrayList<>();

        classFields.add(FieldSpec.builder(String.class, "partialUri")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$S", oType.getPartialUri()).build());

        classFields.add(FieldSpec.builder(ParameterizedTypeName.get(List.class, String.class), "paths")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$T.asList($L)", ClassName.get(Arrays.class),
                        "\"" + oType.getPaths().stream().collect(Collectors.joining("\", \"")) + "\"")
                .build());

        if (oType.getType().equals("plural")) {
            Pair<String, String> path = splitClasspath(this.pluralFragmentClass);
            ClassName fragmentClass = ClassName.get(path.getLeft(), path.getRight());
            path = splitClasspath(this.baseBuilderClass);
            ClassName baseClass = ClassName.get(path.getLeft(), path.getRight());

            classFields.add(FieldSpec.builder(fragmentClass, "fragment")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                    .initializer("new $T(new $L(new $T(){}))", fragmentClass, upperCamel(oType.getName()), baseClass)
                    .build());
        }

        ClassName superInterface;
        String name;
        if (oType.getType().equals("plural")) {
            Pair<String, String> path = splitClasspath(this.pluralBuilderClass);
            superInterface = ClassName.get(path.getLeft(), path.getRight());
            name = oType.getAdditionalName();
        } else if (oType.getType().equals("singular")) {
            Pair<String, String> path = splitClasspath(this.singularBuilderClass);
            superInterface = ClassName.get(path.getLeft(), path.getRight());
            name = oType.getName();
        } else {
            Pair<String, String> path = splitClasspath(this.topLevelBuilderClass);
            superInterface = ClassName.get(path.getLeft(), path.getRight());
            name = oType.getName();
        }
        superInterface = superInterface.nestedClass("Info");
        methods.add(MethodSpec.methodBuilder("getPaths").returns(ParameterizedTypeName.get(List.class, String.class))
                .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).addStatement("return Info.paths").build());
        methods.add(MethodSpec.methodBuilder("getPartialUri").returns(String.class).addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class).addStatement("return Info.partialUri").build());

        if (oType.getType().equals("plural")) {
            Pair<String, String> path = splitClasspath(this.pluralFragmentClass);
            ClassName fragmentClass = ClassName.get(path.getLeft(), path.getRight());
            methods.add(MethodSpec.methodBuilder("getFragment").returns(fragmentClass).addModifiers(Modifier.PUBLIC)
                    .addStatement("return fragment").build());
        } else if (oType.getType().equals("singular")) {
            Pair<String, String> path = splitClasspath(this.singleFragmentClass);
            ClassName fragmentClass = ClassName.get(path.getLeft(), path.getRight());
            path = splitClasspath(this.baseBuilderClass);
            ClassName baseClass = ClassName.get(path.getLeft(), path.getRight());
            List<ParameterSpec> typeParams = new ArrayList<>();

            for (ObjectField oF : oType.getFields()) {
                if (oF.getType().equals("string")) {
                    typeParams.add(ParameterSpec.builder(String.class, lowerCamel(makeValidJavaVariable(oF.getName())))
                            .build());
                } else if (oF.getType().equals("integer")) {
                    typeParams.add(
                            ParameterSpec.builder(int.class, lowerCamel(makeValidJavaVariable(oF.getName()))).build());
                }
            }
            methods.add(MethodSpec.methodBuilder("getFragment").returns(fragmentClass).addParameters(typeParams)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return new $T(new $L(new $T(){}, $L))", fragmentClass, upperCamel(oType.getName()),
                            baseClass, typeParams.stream().map(item -> item.name).collect(Collectors.joining(", ")))
                    .build());
        }
        if (!oType.getType().equals("top level")) {
            classFields.add(FieldSpec.builder(String.class, "name")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC).initializer("$S", name).build());
            classFields.add(FieldSpec.builder(ClassName.get("", "UriParams"), "uriParams")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                    .initializer("new $T()", ClassName.get("", "UriParams")).build());
            methods.add(MethodSpec.methodBuilder("getName").returns(String.class).addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class).addStatement("return Info.name").build());

            methods.add(MethodSpec.methodBuilder("getUriParams").returns(ClassName.get("", "UriParams"))
                    .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).addStatement("return Info.uriParams")
                    .build());
        }
        TypeSpec.Builder returnTypeSpec = TypeSpec.classBuilder("Info").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(superInterface).addFields(classFields).addMethods(methods);
        if (!oType.getType().equals("top level")) {
            returnTypeSpec.addType(createUriParamsClass(superInterface, oType));
        }
        return returnTypeSpec.build();

    }

    protected TypeSpec createUriParamsClass(ClassName parent, ObjectType oType) {

        List<FieldSpec> classFields = new ArrayList<>();
        Matcher params = Patterns.urlTemplatePattern.matcher(oType.getPartialUri());

        while (params.find()) {
            String value;
            String name;

            value = params.group(1);
            name = params.group(2);
            name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name);

            classFields.add(FieldSpec.builder(String.class, name, Modifier.PUBLIC, Modifier.FINAL)
                    .initializer("$S", value).build());
        }

        return TypeSpec.classBuilder("UriParams").addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(classFields).addSuperinterface(parent.nestedClass("UriParams")).build();
    }

    protected String makeValidJavaVariable(String name) {

        return name.replace(".", "_");
    }

    protected Pair<String, String> splitClasspath(String path) {

        return Pair.of(path.substring(0, path.lastIndexOf(".")),
                path.substring(path.lastIndexOf(".") + 1, path.length()));
    }

    protected String lowerCamel(String s) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, s);
    }

    protected String upperCamel(String s) {
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, s);
    }

}
