package com.sbys.zhaojian.httpcompiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaojian on 2018/3/30.
 */

public class ReturnEntity
{
    private String packageName;
    private String simpleName;
    /*泛型*/
    private List<ReturnEntity> generics = new ArrayList<>();

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public String getSimpleName()
    {
        return simpleName;
    }

    public void setSimpleName(String simpleName)
    {
        this.simpleName = simpleName;
    }

    public List<ReturnEntity> getGenerics()
    {
        return generics;
    }

    public void addReturnEnetity(ReturnEntity returnEntity)
    {
        generics.add(returnEntity);
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (ReturnEntity generic : generics)
        {
            stringBuilder.append(generic.toString());
        }
        return "[ReturnEntity  pakegeName:" + packageName + "|simpleName:" + simpleName + "]" + stringBuilder.toString();
    }
}
