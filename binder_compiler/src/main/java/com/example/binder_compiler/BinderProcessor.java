package com.example.binder_compiler;

import com.example.bean.VariableInfo;
import com.example.binder_annotation.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;


/**
 * Created by Administrator on 2018/8/18.
 */
@AutoService(Processor.class)
public class BinderProcessor extends AbstractProcessor {
    private Filer filer;
    private Elements elementsUtils;

    private Map<String, List<VariableInfo>> variableInfoMap = new HashMap<>();
    private Map<String, TypeElement> typeElementMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        elementsUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {

        return SourceVersion.RELEASE_7;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotatonTypes = new HashSet<>();
        annotatonTypes.add("com.example.binder_annotation.BindView");
        return annotatonTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //首先将所有的注解收集到map中
        collectInfo(roundEnvironment);
        //第二步生成我们的辅助文件这里用到了我们的com.squareup:javapoet:1.7.0包 squarec出品
        generateElementFile();
        return true;
    }


    private void collectInfo(RoundEnvironment roundEnvironment) {
        //获取所有被 bindView注解后的属性元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {

            //首先获取到我们的注解id
            int resId = element.getAnnotation(BindView.class).resid();
            //强转成我们需要的类型变量
            VariableElement variableElement = (VariableElement) element;
            //获取外部类信息
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();

            String classFullName = typeElement.getQualifiedName().toString();

            List<VariableInfo> variableInfos = variableInfoMap.get(classFullName);

            if (variableInfos == null) {
                variableInfos = new ArrayList<>();
                variableInfoMap.put(classFullName, variableInfos);
                //这里存放我们的TypeElement信息方便我们生成对应的辅助类
                typeElementMap.put(classFullName, typeElement);
            }
            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setResid(resId);
            variableInfo.setVariableElement(variableElement);
            variableInfos.add(variableInfo);

        }

    }

    //生成相应的文件采用构造器方式实现
    private void generateElementFile() {
        try {
            Set<String> entrySet = variableInfoMap.keySet();
            for (String classFullName : entrySet) {
                TypeElement typeElement = typeElementMap.get(classFullName);
                //首先生成构造器
                MethodSpec.Builder constructorSpec = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "activity").build());

                //开始循环遍历相应的变量  自动生成findViewByid
                List<VariableInfo> variableInfos = variableInfoMap.get(classFullName);
                for (VariableInfo variableInfo : variableInfos) {

                    int resId = variableInfo.getResid();
                    VariableElement variableElement = variableInfo.getVariableElement();
                    //首先考虑一点 findViewById需要变量的名称  变量的全类名  以及变量的id
                    String variableName = variableElement.getSimpleName().toString();
                    String variableFullName = variableElement.asType().toString();
                    constructorSpec.addStatement("activity.$L=($L)activity.findViewById($L)", variableName, variableFullName, resId);
                }
                //下一步我们开始进行生成类

                TypeSpec typeBuilder = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$bindInject")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructorSpec.build())
                        .build();

                //最后一步生成我们的文件

                String packgeName = elementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
                JavaFile javaFile = JavaFile.builder(packgeName, typeBuilder).build();
                javaFile.writeTo(filer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
