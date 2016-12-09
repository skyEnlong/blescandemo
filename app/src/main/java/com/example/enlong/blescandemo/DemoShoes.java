package com.example.enlong.blescandemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.communication.bean.CodoonHealthDevice;
import com.communication.shoes.CodoonShoesCommandHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 2016/12/7.
 */

public class DemoShoes extends Activity {


    @BindView(R.id.button7)
    Button button7;
    @BindView(R.id.button9)
    Button button9;
    @BindView(R.id.button10)
    Button button10;
    @BindView(R.id.button11)
    Button button11;
    @BindView(R.id.button12)
    Button button12;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button15)
    Button button15;
    @BindView(R.id.button6)
    Button button6;
    @BindView(R.id.button16)
    Button button16;
    @BindView(R.id.button13)
    Button button13;
    @BindView(R.id.button4)
    Button button4;
    @BindView(R.id.button8)
    Button button8;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.button5)
    Button button5;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button14)
    Button button14;
    @BindView(R.id.button17)
    Button button17;
    private DemoSyncManger manger;
    private BleScanMananfer scanMananfer;
    private TextView receiveText;
    private TextView sendText;
    private CodoonShoesCommandHelper commandHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_shoes);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        manger = new DemoSyncManger(this.getApplicationContext());
        scanMananfer = new BleScanMananfer(this);
        sendText = (TextView) findViewById(R.id.textView2);
        receiveText = (TextView) findViewById(R.id.textView4);
        commandHelper = new CodoonShoesCommandHelper();
    }

    @OnClick(R.id.button3)
    void startScanAndConnect() {
        sendText.setText("开始扫描");
        scanMananfer.setCallBack(new OnDeviceSearch<CodoonHealthDevice>() {
            @Override
            public void onDeviceSearch(CodoonHealthDevice dev) {
                if (dev.device_type_name.toLowerCase().equals("cod_shoes")) {
                    scanMananfer.stopScan();
                    sendText.setText("开始连接");
                    manger.start(dev);
                }
            }
        });
        scanMananfer.startScan();

    }

    @OnClick(R.id.button)
    void updateTime() {

        manger.writeCommand(commandHelper.getUpdateTimeCommand());

    }

    @OnClick(R.id.button15)
    void bind() {
        manger.writeCommand(commandHelper.getBindCommand());
    }


    @OnClick(R.id.button4)
    void getVersion() {
        manger.writeCommand(commandHelper.getVersionCommand());
    }


    @OnClick(R.id.button5)
    void getId() {

        manger.writeCommand(commandHelper.getIDCommand());
    }

    @OnClick(R.id.button16)
    void getAccessoryBD() {
        manger.writeCommand(commandHelper.getAccessoryBDCommand());
    }

    @OnClick(R.id.button6)
    void getShoesState() {
        manger.writeCommand(commandHelper.getShoesStateComand());
    }

    @OnClick(R.id.button7)
    void updateUserInfo() {
        manger.writeCommand(commandHelper.getSetUserInfoCommand(170, 60, 20));
    }


    @OnClick(R.id.button13)
    void getMinuRunState() {
        manger.writeCommand(commandHelper.getMinRunState());
    }

    @OnClick(R.id.button11)
    void getTotalKm() {
        manger.writeCommand(commandHelper.getTotalKm());
    }

    @OnClick(R.id.button12)
    void clearDatas() {
        manger.writeCommand(commandHelper.getShoesStateComand());
    }

    @OnClick(R.id.button8)
    void getSyncReady() {
        manger.writeCommand(commandHelper.getSyncReadyCommand());

    }

    @OnClick(R.id.button9)
    void beginSyncStep() {
        manger.writeCommand(commandHelper.getStepTotalFrameCommand());
    }

    @OnClick(R.id.button10)
    void beginSyncRun() {
        manger.writeCommand(commandHelper.getTotalRunFrameCommand());
    }

    @OnClick(R.id.button2)
    void startRUn() {
        manger.writeCommand(commandHelper.getStartRunCommand());
    }

    @OnClick(R.id.button17)
    void stopRun() {
        manger.writeCommand(commandHelper.getStopRunCommand());
    }

    @OnClick(R.id.button14)
    void clearData() {
        manger.writeCommand(commandHelper.getClearCommand());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(MsgEvent event) {
        receiveText.setText(event.msg);
    }


    @OnClick({R.id.button7,
            R.id.button9,
            R.id.button10,
            R.id.button11,
            R.id.button12,
            R.id.button,
            R.id.button15,
            R.id.button6,
            R.id.button16,
            R.id.button13,
            R.id.button4,
            R.id.button8,
            R.id.button3,
            R.id.button5,
            R.id.button2,
            R.id.button14,
            R.id.button17})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button7:
                updateUserInfo();
                break;
            case R.id.button9:
                beginSyncStep();
                break;
            case R.id.button10:
                beginSyncRun();
                break;
            case R.id.button11:
                getTotalKm();
                break;
            case R.id.button12:
                clearDatas();
                break;
            case R.id.button:
                updateTime();
                break;
            case R.id.button15:
                bind();
                break;
            case R.id.button6:
                getShoesState();
                break;
            case R.id.button16:
                getAccessoryBD();
                break;
            case R.id.button13:
                getMinuRunState();
                break;
            case R.id.button4:
                getVersion();
                break;
            case R.id.button8:
                getSyncReady();
                break;
            case R.id.button3:
                getSyncReady();
                break;
            case R.id.button5:
                getId();
                break;
            case R.id.button2:
                startRUn();
                break;
            case R.id.button14:
                clearData();
                break;
            case R.id.button17:
                stopRun();
                break;
        }
    }
}
