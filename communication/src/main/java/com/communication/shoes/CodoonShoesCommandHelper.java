package com.communication.shoes;

import com.communication.common.BaseCommandHelper;

/**
 * Created by enlong on 2016/12/8.
 */

public class CodoonShoesCommandHelper extends BaseCommandHelper{



    public CodoonShoesCommandHelper(){
     }

    public byte[] getBindCommand(){
        return getCommand(CodoonShoesCommand.CODE_BIND);

    }

    /**读设备ID**/
    public byte[] getIDCommand(){
        return getCommand(CodoonShoesCommand.CODE_READ_ID);
    }

    /**获取设备类型和版本号**/
    public byte[] getVersionCommand(){
        return getCommand(CodoonShoesCommand.CODE_VERSION);
    }

    /**更新设备时间**/
    public byte[] getUpdateTimeCommand(){
        return getCommand(CodoonShoesCommand.CODE_UPDATE_TIME, getTimeArray(System.currentTimeMillis()));
    }

    /**读取运动数据帧数**/
    public byte[] getStepTotalFrameCommand(){
        return getCommand(CodoonShoesCommand.CODE_TOTAL_STEP_FRAME);
    }

    /**准备同步数据命令**/
    public byte[] getSyncReadyCommand(){
        return getCommand(CodoonShoesCommand.CODE_READY_DATA);
    }

    /**clear data**/
    public byte[] getClearCommand(){
        return getCommand(CodoonShoesCommand.CODE_CLEAR_SPORT_DATA);
    }

    /**加速度传感器标定**/
    public byte[] getAccessoryBDCommand(){
        return getCommand(CodoonShoesCommand.CODE_ACCESSORY_BD);
    }

    /**开始跑步**/
    public byte[] getStartRunCommand(){
        return getCommand(CodoonShoesCommand.CODE_START_RUN);

    }

    /**结束跑步**/
    public byte[] getStopRunCommand(){
        return getCommand(CodoonShoesCommand.CODE_STOP_RUN);

    }

    /**读取咕咚智能鞋跑步数据帧数**/
    public byte[] getTotalRunFrameCommand(){
        return getCommand(CodoonShoesCommand.CODE_RUN_DATA_TOTAL_FRAME);
    }

    /**读取总里程**/
    public byte[] getTotalKm(){
        return getCommand(CodoonShoesCommand.CODE_SHOES_TOTAL_RUN);

    }

    /**读取跑步状态**/
    public byte[] getMinRunState(){
        return getCommand(CodoonShoesCommand.CODE_RUN_STATE_DATA);

    }

    /**读取当前状态**/
    public byte[] getShoesStateComand(){
        return getCommand(CodoonShoesCommand.CODE_SHOES_STAE);

    }


    /**
     *
     * @param height   cm
     * @param weight   kg
     * @param age
     * @return
     */
    public byte[] getSetUserInfoCommand(int height, int weight, int age){
        byte[] datas = new byte[14];
        datas[0] = (byte) (height & 0xff);
        datas[1] = (byte) (weight & 0xff);
        datas[2] = (byte) (age & 0xff);
        for (int i = 3; i < datas.length; i++){
            datas[i] = 0;
        }
        return getCommand(CodoonShoesCommand.CODE_UPDATE_USER_INFO, datas);
    }

    /**
     * 获取电量、闹钟等情况
     * @return
     */
    public byte[] getClockInfoCmd(){
        return getCommand(CodoonShoesCommand.CODE_GET_CLOCK);
    }

    /**
     * 设置闹钟
     * @param clocks
     * @return
     */
    public byte[] getSetClokCmd(byte[] clocks){
        return getCommand(CodoonShoesCommand.CODE_SET_CLOCK, clocks);
    }

    /**
     * 获取时间
     * @return
     */
    public byte[] getDeviceTimeCmd(){
        return getCommand(CodoonShoesCommand.CODE_GET_TIME);
    }

    /**
     * 获取原始的三轴传感器数据
     * @return
     */
    public byte[] getOriginData(){
        return getCommand(CodoonShoesCommand.CODE_GET_ORIGIN_DATA);
    }
}
