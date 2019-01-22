package com.sbys.zhaojian.test4;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * @author zhaojian
 * @time 2018/12/17 15:59
 * @describe
 */
public class BaseActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onMessage(MessageEvent messageEvent)
    {
        showDialog();
    }

    private void showDialog()
    {
        new AlertDialog.Builder(this).setMessage("ssssssssssssss").create().show();
    }
}
