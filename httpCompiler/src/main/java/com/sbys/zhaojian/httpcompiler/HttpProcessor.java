package com.sbys.zhaojian.httpcompiler;

import com.google.auto.service.AutoService;
import com.sbys.zhaojian.httpan.InvokeByProxy;
import com.sbys.zhaojian.httpan.ResultMayNull;
import com.sbys.zhaojian.httpan.SupportOffline;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import retrofit2.http.POST;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ // 标注注解处理器支持的注解类型
        //"com.mobiledoctor.httpannotation.METHOD",
        "com.mobiledoctor.httpannotation.ResultMayNull",
        "com.mobiledoctor.httpannotation.SupportOffline",
        "com.mobiledoctor.httpannotation.InvokeByProxy",
        "retrofit2.http.POST"
})
public class HttpProcessor extends AbstractProcessor
{
    private Filer mFiler;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
    {
        //Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(METHOD.class);
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(POST.class);
        Set<? extends Element> supportElements = roundEnvironment.getElementsAnnotatedWith(InvokeByProxy.class);
        filterElemnet(elements, supportElements);
        Set<? extends Element> nullableElements = roundEnvironment.getElementsAnnotatedWith(ResultMayNull.class);
        Set<? extends Element> offlineElements = roundEnvironment.getElementsAnnotatedWith(SupportOffline.class);
        boolean requestProxySuccess = createRequestProxy(elements, nullableElements);
        boolean onlineRequestProxySuccess = createOnlineRequestProxy(elements, nullableElements);
        boolean offlineSuccess = createAbstractOfflineStrategy(offlineElements, nullableElements);
        return requestProxySuccess && onlineRequestProxySuccess && offlineSuccess;
    }


    /**
     * 过滤RequestApi中重复定义的method
     * @param all
     * @param support
     */
    private void filterElemnet(Set<? extends Element> all, Set<? extends Element> support)
    {
        List<String> postNames = new ArrayList<>();
        /*由于原先接口设计的比较坑，所以存在一个接口在不同入参时存在两种格式返回值的情况
        * 因此暂时不过滤重复的method*/
        /*for (Element element : all)
        {
            POST method = element.getAnnotation(POST.class);
            if (postNames.contains(method.value()))
            {
                throw new IllegalArgumentException(String.format("method @%s in RequestApi has already defined ", method.value()));
            }
            else
            {
                postNames.add(method.value());
            }
        }*/
        all.removeIf(element -> !support.contains(element));
    }

    private boolean createRequestProxy(Set<? extends Element> methodElements,Set<? extends Element> nullableElements)
    {
        try
        {
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder("RequestProxy")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            classBuilder.addField(parseField());

            classBuilder.addMethod(parseSetMethod());

            for (Element element : methodElements)
            {
                boolean isResultMayNull = checkIsResultMayNull(element, nullableElements);
                MethodSpec methodSpec = parseMethod(element, isResultMayNull);
                classBuilder.addMethod(methodSpec);
            }

            JavaFile javaFile = JavaFile.builder("com.sbys.httplib", classBuilder.build())
                    .build();

            javaFile.writeTo(mFiler);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean createAbstractOfflineStrategy(Set<? extends Element> offlineElements, Set<? extends Element> nullableElements)
    {
        try
        {
            TypeSpec.Builder interfaceBuilder = TypeSpec.classBuilder("AbstractOfflineStrategy")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            for (Element element : offlineElements)
            {
                boolean isResultMayNull = checkIsResultMayNull(element, nullableElements);
                MethodSpec methodSpec = parseOfflineMethod(element, isResultMayNull);
                interfaceBuilder.addMethod(methodSpec);
            }

            JavaFile javaFile = JavaFile.builder("com.sbys.httplib", interfaceBuilder.build())
                    .build();

            javaFile.writeTo(mFiler);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean createOnlineRequestProxy(Set<? extends Element> methodElements, Set<? extends Element> nullableElements)
    {
        try
        {
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder("OnlineRequestProxy")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for (Element element : methodElements)
            {
                boolean isResultMayNull = checkIsResultMayNull(element, nullableElements);
                MethodSpec methodSpec = parseOnlineMethod(element, isResultMayNull);
                classBuilder.addMethod(methodSpec);
            }

            JavaFile javaFile = JavaFile.builder("com.sbys.httplib", classBuilder.build())
                    .build();

            javaFile.writeTo(mFiler);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean checkIsResultMayNull(Element element, Set<? extends Element> elements)
    {
        for (Element element1 : elements)
        {
            if (element1.equals(element))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();

    }

    private FieldSpec parseField()
    {
        ClassName loginStrategy = ClassName.get("com.sbys.httplib", "ILoginStrategy");
        ClassName onlineLoginStrategy = ClassName.get("com.sbys.httplib", "OnlineLoginStrategy");
        return FieldSpec.builder(loginStrategy, "loginStrategy")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .initializer(CodeBlock.of("new $T()",onlineLoginStrategy)).build();
    }

    private MethodSpec parseSetMethod()
    {
        ClassName loginStrategy = ClassName.get("com.sbys.httplib", "ILoginStrategy");
        ClassName requestProxy = ClassName.get("com.sbys.httplib", "RequestProxy");
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("setLoginStrategy")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("$T.loginStrategy = loginStrategy", requestProxy)
                .addParameter(loginStrategy, "loginStrategy");
        return methodBuilder.build();
    }

    private MethodSpec parseMethod(Element element, boolean isResultMayNull)
    {
        if (element.getKind() != ElementKind.METHOD)
        {
            throw new IllegalArgumentException(String.format("Only method can be annotated with @%s",
                    POST.class.getSimpleName()));
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        MethodEntity methodEntity = new MethodEntity(executableElement);

        ClassName inputType = ClassName.get("java.util", "Map");
        ClassName requestParam = ClassName.get("com.sbys.httplib.data", "RequestParams");
        //ClassName apiManger = ClassName.get("com.sbys.httplib", "APIManager");
        //ClassName requestAPI = ClassName.get("com.sbys.httplib", "RequestAPI");
        //ClassName httpError = ClassName.get("com.sbys.httplib", "HttpError");
        //ClassName preProcessFlatMap = ClassName.get("com.sbys.httplib", "PreProcessFlatMap");
        //TypeName httpErrorType = ParameterizedTypeName.get(httpError, methodEntity.getThirdReturnType());
        //TypeName preProcessFlatMapType = ParameterizedTypeName.get(preProcessFlatMap, methodEntity.getThirdReturnType());

        if (isResultMayNull)
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(methodEntity.getReturnType())
                    .addStatement("$T requestParams = new $T()", requestParam, requestParam)
                    .addStatement("requestParams.paramMap = paramMap")
                    .addStatement("requestParams.localMethodName = $S", element.getSimpleName().toString())
                    .addStatement("requestParams.method = $S", methodEntity.getAnnotationValue())
                    .addStatement("return loginStrategy.handleRequest(requestParams)")
                    .addParameter(inputType, "paramMap");
            return methodBuilder.build();
        }
        else
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(methodEntity.getSubReturnType())
                    .addStatement("$T requestParams = new $T()", requestParam, requestParam)
                    .addStatement("requestParams.paramMap = paramMap")
                    .addStatement("requestParams.localMethodName = $S", element.getSimpleName().toString())
                    .addStatement("requestParams.method = $S", methodEntity.getAnnotationValue())
                    .addStatement("return loginStrategy.handleRequest(requestParams)")
                    .addParameter(inputType, "paramMap");
            return methodBuilder.build();
        }
    }

    private MethodSpec parseOfflineMethod(Element element, boolean isResultMayNull)
    {
        if (element.getKind() != ElementKind.METHOD)
        {
            throw new IllegalArgumentException(String.format("Only method can be annotated with @%s",
                    POST.class.getSimpleName()));
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        OfflineEntity offlineEntity = new OfflineEntity(executableElement);

        ClassName requestParam = ClassName.get("com.sbys.httplib.data", "RequestParams");
        if (isResultMayNull)
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(offlineEntity.getReturnType())
                    .addStatement("return null")
                    .addParameter(requestParam, "requestParams");
            return methodBuilder.build();
        }
        else
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(offlineEntity.getSubReturnType())
                    .addStatement("return null")
                    .addParameter(requestParam, "requestParams");
            return methodBuilder.build();
        }
    }

    private MethodSpec parseOnlineMethod(Element element, boolean isResultMayNull)
    {
        if (element.getKind() != ElementKind.METHOD)
        {
            throw new IllegalArgumentException(String.format("Only method can be annotated with @%s",
                    POST.class.getSimpleName()));
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        MethodEntity methodEntity = new MethodEntity(executableElement);

        ClassName inputType = ClassName.get("java.util", "Map");
        ClassName requestParam = ClassName.get("com.sbys.httplib.data", "RequestParams");
        ClassName apiManger = ClassName.get("com.sbys.httplib", "APIManager");
        ClassName requestAPI = ClassName.get("com.sbys.httplib", "RequestAPI");
        ClassName httpError = ClassName.get("com.sbys.httplib", "HttpError");
        ClassName preProcessFlatMap = ClassName.get("com.sbys.httplib", "PreProcessFlatMap");
        TypeName httpErrorType = ParameterizedTypeName.get(httpError, methodEntity.getThirdReturnType());
        TypeName preProcessFlatMapType = ParameterizedTypeName.get(preProcessFlatMap, methodEntity.getThirdReturnType());

        if (isResultMayNull)
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(methodEntity.getReturnType())
                    .addStatement("return $T.getAPI($T.class).$L(requestParams)" +
                                    ".onErrorReturn(new $T())",
                            apiManger, requestAPI, executableElement.getSimpleName(), httpErrorType)
                    .addParameter(requestParam, "requestParams");
            return methodBuilder.build();
        }
        else
        {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(element.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(methodEntity.getSubReturnType())
                    .addStatement("requestParams.localMethodName = $S", element.getSimpleName().toString())
                    .addStatement("return $T.getAPI($T.class).$L(requestParams)" +
                                    ".onErrorReturn(new $T())" +
                                    ".flatMap(new $T())",
                            apiManger, requestAPI, executableElement.getSimpleName(), httpErrorType, preProcessFlatMapType)
                    .addParameter(requestParam, "requestParams");
            return methodBuilder.build();
        }
    }


}
