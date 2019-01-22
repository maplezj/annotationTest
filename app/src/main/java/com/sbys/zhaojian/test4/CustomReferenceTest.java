package com.sbys.zhaojian.test4;


import android.util.Log;

/**
 * @author zhaojian
 * @time 2019/1/7 15:42
 * @describe
 */
public class CustomReferenceTest
{
    private static final String TAG = "CustomReferenceTest";
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        Log.d(TAG, "finalize:------------------> ");
        MainActivity.customReferenceTest = this;
    }
}
