package com.example.enlong.blescandemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.communication.bean.CodoonHealthDevice;
import com.communication.bean.CodoonShoesMinuteModel;
import com.communication.data.DataUtil;
import com.communication.shoes.CodoonShoesCommandHelper;
import com.communication.shoes.CodoonShoesParseHelper;

import java.io.IOException;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 2017/2/6.
 */

public class DemoUnion extends Activity implements View.OnClickListener

{


    private String upFile;
    private UnionPayDeviceConnector manger;
    private BleScanMananfer scanMananfer;
    private TextView receiveText;
    private TextView sendText;
    private CodoonShoesCommandHelper commandHelper;
    private boolean isStartSport;
    private CodoonShoesParseHelper parseHelper;
    private Handler mHandler;
    private ScrollView scrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_union);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        scrollView = (ScrollView) findViewById(R.id.scrollView_rec);
        mHandler = new Handler();
        parseHelper = new CodoonShoesParseHelper();
        manger = new UnionPayDeviceConnector(this.getApplicationContext());
        scanMananfer = new BleScanMananfer(this);
        sendText = (TextView) findViewById(R.id.textView2);
        receiveText = (TextView) findViewById(R.id.textView4);
        commandHelper = new CodoonShoesCommandHelper();
        int[] butnId = new int[]{R.id.button7,
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
                R.id.button17};

        for (int id : butnId) {
            View v = findViewById(id);
            if (null != v)
                v.setOnClickListener(this);
        }

        String[] fs = new String[0];
        try {
            fs = getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.button3)
    void startScanAndConnect() {
        sendText.setText("开始扫描");
        scanMananfer.setCallBack(new OnDeviceSearch<CodoonHealthDevice>() {
            @Override
            public void onDeviceSearch(CodoonHealthDevice dev) {
                if (dev.device_type_name.toUpperCase().startsWith("UPWEAR")) {
                    if (isStartSport) {
                        DataUtil.DebugPrint(dev.manufacturer);
                        byte[] brod = Arrays.copyOfRange(dev.manufacturer, 13, 21);
                        CodoonShoesMinuteModel data = parseHelper.parsePercentsInBroad(brod);
                        if (null != data) {
                            MsgEvent event = new MsgEvent();
                            event.msg = "广播得到数据：" + data.toString();
                            event.event_id = 1;
                            EventBus.getDefault().post(event);
                        }
                    } else {
                        scanMananfer.stopScan();
                        MsgEvent event = new MsgEvent();
                        event.msg = "开始连接：";
                        event.event_id = 0;
                        EventBus.getDefault().post(event);
                        manger.startBindDevice(dev);
                    }

                }
            }
        });
        scanMananfer.startScan();

    }

    @OnClick(R.id.button)
    void updateTime() {

        manger.writeCommand(commandHelper.getUpdateTimeCommand());

    }



    @OnClick(R.id.button4)
    void getVersion() {
        manger.getVersion();
    }


    @OnClick(R.id.button5)
    void getId() {

        manger.writeCommand(commandHelper.getIDCommand());
    }

    @OnClick(R.id.button16)
    void getAccessoryBD() {
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
//        manger.writeCommand(commandHelper.getAccessoryBDCommand());
    }

    @OnClick(R.id.button6)
    void getShoesState() {
//        manger.writeCommand(commandHelper.getShoesStateComand());
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
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
//        manger.writeCommand(commandHelper.getTotalKm());
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button12)
    void startUpGrade() {
//        File f = new File(upFile);
//        if (!f.exists()) {
//            Toast.makeText(this, "请将升级文件至于 " + upFile, Toast.LENGTH_SHORT).show();
//            return;
//        }
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();

//        manger.startUpgrade(upFile);
    }

    @OnClick(R.id.button8)
    void getSyncReady() {
//        manger.writeCommand(commandHelper.getSyncReadyCommand());
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button9)
    void beginSyncStep() {
        manger.writeCommand(commandHelper.getStepTotalFrameCommand());
    }

    @OnClick(R.id.button10)
    void beginSyncRun() {
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
//        manger.writeCommand(commandHelper.getTotalRunFrameCommand());
    }

    @OnClick(R.id.button2)
    void startRUn() {
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
//        manger.writeCommand(commandHelper.getStartRunCommand());
//        isStartSport = true;
    }

    @OnClick(R.id.button17)
    void stopRun() {
//        manger.writeCommand(commandHelper.getStopRunCommand());
//        isStartSport = false;
//        scanMananfer.stopScan();
        Toast.makeText(this, "not support!", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.button14)
    void clearData() {
        manger.writeCommand(commandHelper.getClearCommand());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        scanMananfer.stopScan();
        manger.stop();
    }

    public void onEventMainThread(MsgEvent event) {
        if (event.event_id == 0) {
            receiveText.setText(receiveText.getText() + "\n" + event.msg);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });


        } else {
            sendText.setText(event.msg);

        }

    }


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
                startUpGrade();
                break;
            case R.id.button:
                updateTime();
                break;
            case R.id.button15:
               setClock();
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
                startScanAndConnect();
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

    private void setClock() {
        byte[] data =new byte[13];
        for(int i =0; i < data.length; i++){
            data[i] = 0;
        }
        manger.writeCommand(commandHelper.getSetClokCmd(data));

    }
}
