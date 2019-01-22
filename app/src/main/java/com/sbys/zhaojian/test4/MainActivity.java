package com.sbys.zhaojian.test4;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity
{
    private static final String TAG = "MainActivity";
    ReferenceQueue<Object> queue = new ReferenceQueue<>();
    public static CustomReferenceTest customReferenceTest;


    @SuppressLint("checkResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        customReferenceTest = new CustomReferenceTest();
        WeakReference<CustomReferenceTest> reTest = new WeakReference<>(customReferenceTest,queue);
        /*View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);*/
        //getWindow().setStatusBarColor(getResources().getColor(R.color.btn_login_normal));
       /* Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);*/
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        setContentView(R.layout.activity_main);
        /*getWindow().setStatusBarColor(getResources().getColor(R.color.btn_login_normal));*/

        findViewById(R.id.mainbtn).setOnClickListener(view ->{
            /*CustomReferenceTest customReferenceTest1 = new CustomReferenceTest();
            customReferenceTest1 = null;
            System.gc();*/
            Log.d(TAG, "onCreate: after gc---------------->");
            if (reTest.get() == null)
            {
                Log.d(TAG, "onCreate: reTest null-----------");
            }
            if (queue.poll() == null)
            {
                Log.d(TAG, "onCreate: poll null---------------->");
            }
            else
            {
                Log.d(TAG, "onCreate: poll not  null---------------->");
            }

            if (customReferenceTest == null)
            {
                Log.d(TAG, "onCreate: customReferenceTest  null------------");
            }
            else
            {
                Log.d(TAG, "onCreate: customReferenceTest not  null------------");
            }

            Intent intent = new Intent(this,SecActivity.class);
            startActivity(intent);
        });
        ActionBar actionBar=getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("测试返回");
    }

}
