package com.example.enlong.blescandemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 2016/12/7.
 */

public class DemoShoes extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        EventBus.getDefault().register(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(MsgEvent event){

    }
}
