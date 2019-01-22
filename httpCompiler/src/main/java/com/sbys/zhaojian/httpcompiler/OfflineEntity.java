package com.sbys.zhaojian.httpcompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by zhaojian on 2018/3/30.
 */

public class OfflineEntity
{
    String inputName;
    TypeName returnType;
    TypeName subReturnType;
    TypeName thirdReturnType;
    ReturnEntity returnEntity = new ReturnEntity();
    public OfflineEntity(ExecutableElement executableElement)
    {
        init(executableElement);
    }


    public String getInputName()
    {
        return inputName;
    }

    public TypeName getReturnType()
    {
        return returnType;
    }

    public TypeName getSubReturnType()
    {
        return subReturnType;
    }

    public TypeName getThirdReturnType()
    {
        return thirdReturnType;
    }

    public ReturnEntity getReturnEntity()
    {
        return returnEntity;
    }

    private void init(ExecutableElement executableElement)
    {
        TypeMirror typeMirror = executableElement.getReturnType();
        parseReturnEntity(typeMirror, returnEntity);
        parseInputName(executableElement);
        parseReturnType();
    }

    private void parseReturnType()
    {
        ClassName first = ClassName.get(returnEntity.getPackageName(), returnEntity.getSimpleName());
        List<ReturnEntity> subEntityList = returnEntity.getGenerics();
        if (subEntityList.size() != 1)
        {
            returnType = ParameterizedTypeName.get(first);
            return;
        }

        ReturnEntity sub1Entity = subEntityList.get(0);
        ClassName second = ClassName.get(sub1Entity.getPackageName(), sub1Entity.getSimpleName());
        List<ReturnEntity> sub2EntityList = sub1Entity.getGenerics();
        if (sub2EntityList.size() != 1)
        {
            returnType = ParameterizedTypeName.get(first, second);
            return;
        }
        ReturnEntity sub2Entity = sub2EntityList.get(0);
        ClassName third = ClassName.get(sub2Entity.getPackageName(), sub2Entity.getSimpleName());

        List<ReturnEntity> sub3EntityList = sub2Entity.getGenerics();
        if (sub3EntityList.size() == 1)
        {
            ReturnEntity sub3Entity = sub3EntityList.get(0);
            ClassName fourth = ClassName.get(sub3Entity.getPackageName(), sub3Entity.getSimpleName());
            thirdReturnType = ParameterizedTypeName.get(third, fourth);
            subReturnType = ParameterizedTypeName.get(first, thirdReturnType);
            returnType = ParameterizedTypeName.get(first, ParameterizedTypeName.get(second, thirdReturnType));
        }
        else
        {
            thirdReturnType = ClassName.get(sub2Entity.getPackageName(), sub2Entity.getSimpleName());
            subReturnType = ParameterizedTypeName.get(first, thirdReturnType);
            returnType = ParameterizedTypeName.get(first, ParameterizedTypeName.get(second, thirdReturnType));
        }

    }

    private void parseInputName(ExecutableElement executableElement)
    {
        List<? extends VariableElement> variableElements = executableElement.getParameters();
        if (variableElements.size() != 1)
        {
            throw new IllegalArgumentException("illegal input count");
        }
        inputName = variableElements.get(0).toString();
    }

    private void parseReturnEntity(TypeMirror typeMirror, ReturnEntity returnEntity)
    {
        String typeStr = typeMirror.toString();
        if (typeStr == null || typeStr.length() == 0)
        {
            return;
        }
        if (typeStr.contains("<"))
        {
            int index = typeStr.indexOf("<");
            typeStr = typeStr.substring(0, index);
        }
        int index = typeStr.lastIndexOf(".");
        returnEntity.setPackageName(typeStr.substring(0, index));
        returnEntity.setSimpleName(typeStr.substring(index+1, typeStr.length()));
        if (typeMirror instanceof DeclaredType)
        {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            List<? extends TypeMirror> arguments = declaredType.getTypeArguments();
            if (arguments != null && arguments.size() > 0)
            {
                for (TypeMirror argument : arguments)
                {
                    ReturnEntity subReturnEntity = new ReturnEntity();
                    returnEntity.addReturnEnetity(subReturnEntity);
                    parseReturnEntity(argument, subReturnEntity);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "annotationValue:" + "|inputName:" + inputName + "|returnEntity:" + returnEntity.toString();
    }
}