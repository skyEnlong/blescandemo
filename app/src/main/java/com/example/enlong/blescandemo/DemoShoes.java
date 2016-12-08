package com.example.enlong.blescandemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

import com.communication.bean.CodoonHealthDevice;
import com.communication.shoes.CodoonShoesCommandHelper;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 2016/12/7.
 */

public class DemoShoes extends Activity {

    private DemoSyncManger manger;
    private BleScanMananfer scanMananfer;
    private TextView receiveText;
    private TextView sendText;
    private CodoonShoesCommandHelper commandHelper;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.demo_shoes);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        manger = new DemoSyncManger(this.getApplicationContext());
        scanMananfer = new BleScanMananfer(this);
        sendText = (TextView) findViewById(R.id.textView2);
        receiveText = (TextView) findViewById(R.id.textView4);
    }

    @OnClick(R.id.button3)
    void startScanAndConnect(){
        sendText.setText("开始扫描");
        scanMananfer.setCallBack(new OnDeviceSearch<CodoonHealthDevice>() {
            @Override
            public void onDeviceSearch(CodoonHealthDevice dev) {
                if(dev.device_type_name.toLowerCase().equals("cod_shoes")){
                    scanMananfer.stopScan();
                    sendText.setText("开始连接");
                    manger.start(dev);
                }
            }
        });
        scanMananfer.startScan();

    }

    @OnClick(R.id.button)
     void updateTime(){

         manger.writeCommand(commandHelper.getUpdateTimeCommand());

     }

    @OnClick(R.id.button15)
    void bind(){
        manger.writeCommand(commandHelper.getBindCommand());
    }


    @OnClick(R.id.button4)
    void getVersion(){
        manger.writeCommand(commandHelper.getVersionCommand());
    }


    @OnClick(R.id.button5)
    void getId(){

        manger.writeCommand(commandHelper.getIDCommand());
    }
    @OnClick(R.id.button16)
    void getAccessoryBD(){
        manger.writeCommand(commandHelper.getAccessoryBDCommand());
    }

    @OnClick(R.id.button6)
    void getShoesState(){
        manger.writeCommand(commandHelper.getShoesStateComand());
    }


    @OnClick(R.id.button13)
    void getMinuRunState(){
        manger.writeCommand(commandHelper.getMinRunState());
    }

    @OnClick(R.id.button11)
    void getTotalKm(){
        manger.writeCommand(commandHelper.getTotalKm());
    }

    @OnClick(R.id.button8)
    void getSyncReady(){
        manger.writeCommand(commandHelper.getSyncReadyCommand());

    }

    @OnClick(R.id.button9)
    void beginSyncStep(){
        manger.writeCommand(commandHelper.getStepTotalFrameCommand());
    }

    @OnClick(R.id.button10)
    void beginSyncRun(){
        manger.writeCommand(commandHelper.getTotalRunFrameCommand());
    }

    @OnClick(R.id.button2)
    void startRUn(){
        manger.writeCommand(commandHelper.getStartRunCommand());
    }

    @OnClick(R.id.button17)
    void stopRun(){
        manger.writeCommand(commandHelper.getStopRunCommand());
    }

    @OnClick(R.id.button14)
    void clearData(){
        manger.writeCommand(commandHelper.getClearCommand());

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(MsgEvent event){
        receiveText.setText(event.msg);
    }


}
