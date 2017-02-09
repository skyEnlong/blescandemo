package com.example.enlong.blescandemo.logic;

/**
 * Created by 2LBB2Z1 on 2016/4/21.
 */
public enum SoundFactory {

    ZERO(0), // 0
    ONE(1), // 1
    TWO(2), // 2
    THREE(3), // 3
    FOUR(4), // 4
    FIVE(5), // 5
    SIX(6), // 6
    SEVEN(7), // 7
    EIGHT(8), // 8
    NINE(9), // 9
    TEN(10), // 十
    HUNDRED(11), // 百
    THOUSAND(12), // 千
    WAN(13), // 万
    KILOMETER(14), // 公里
    Walk(15), // 走路
    Run(16), // 跑步
    Cycle(17), // 骑车
    First(18), // 首先
    Already(19), // 已经
    Last(20), // 最后
    ComeOn(21), // 加油
    Next(22), // 接下来
    Exercise_To_Relax(23), // 放松运动
    Minute(24), // 分钟
    Spend_Time(25), // 用时
    Great(26), // 太棒了！
    Completed_Goals(27), // 太棒了！你完成了今天的目标
    Completed_All_Goals(28), // 太棒了！你完成了所有目标，运动计划成功！
    Failure_Over(29), // 男：我那个去，尽然玩脱了
    Preparation(30), // 男：预备跑
    SportsOver(31), // 你已经完成目标
    Message(32), // 消息提示音
    FastWalk(33), // 快走
    SlowWalk(34), // 慢走
    RelaxSports(35), // 放松运动
    FastRun(36), // 快跑
    SlowRun(37), // 慢跑
    AverageSpeed(38), // 平均速度
    AverageSpeedUnit(39), // ,// 公里每小时
    ContinueProgram(40), // 继续计划
    DingDong(41), // 叮咚
    Second(42), // 秒
    NearByOneMile(43), // 最近一公里
    DOT(44), // 点
    SPORTS(45), HeartRateBeep(46), GpsRegain(47), GpsLoss(48), Gps(49),// 心率报警
    PauseSport(50),ContinueSport(51),
    Finish(60),//完成
    Break(61),//已经打破个人纪录
    Quarter(62),//1/4
    Half(63),//1/2
    Marathon(64),//马拉松
    Hour(65),//小时
    Lead(66),//领先
    Behind(67),//落后
    Meter(68),//米
    ChallengeSucess(69),//挑战成功
    ChallengeFail(70),//挑战失败
    FinishGoal(71),//完成目标
    Left(72),//还差
    FinalOneMile(73),//最后一公里
    BluetoothDisconnect(74),//蓝牙断开
    Walk_Start(75),
    Run_Start(76),
    Cycle_Start(77),
    LiHai(78), // 太厉害了
    Flower1Pre(79),
    Flower1Suf(80), // 咚友为你送来xxx朵玫瑰
    Flower2Pre(81),
    Flower2Suf(82), // 新增xxx个咚友在支持你
    Flower3Pre(83),
    Flower3Suf(84), // 鼓励的玫瑰xxx朵又送到
    Flower4Pre(85),
    Flower4Suf(86), // 你已获得咚友超过xxx朵玫瑰
    NetLose(87), // 网络连接丢失
    Liang(88);  // 两
    SoundFactory(int num) {

    }

    public static String getText(SoundFactory soundFactory) {
        String tmpStr = "";
        switch (soundFactory) {
            case KILOMETER:// 公里
                tmpStr = "公里";
                break;
            case Walk: // 走路
                tmpStr = "走路";
                break;
            case Run: // 跑步
                tmpStr = "跑步";
                break;
            case Cycle: // 骑行
                tmpStr = "骑行";
                break;
            case First:
                tmpStr = "首先";
                break;// 首先
            case Already: // 你已经
                tmpStr = "你已经";
                break;
            case Last: // 最后
                tmpStr = "最后";
                break;
            case ComeOn: // 加油吧
                tmpStr = "加油吧";
                break;
            case Next: // 接着
                tmpStr = "接着";
                break;
            case Exercise_To_Relax:
                tmpStr = "放松一下吧";
                break;// 放松运动
            case Minute: // 分钟
                tmpStr = "分钟";
                break;
            case Spend_Time: // 用时
                tmpStr = "用时";
                break;
            case Great: // 太棒了！
                tmpStr = "太棒了!";
                break;
            case Completed_Goals: // 太棒了！你完成了今天的目标
                tmpStr = "太棒了!你完成了今天的目标。";
                break;
            case Completed_All_Goals: // 太棒了！你完成了所有目标，运动计划成功！
                tmpStr = "太棒了!你完成了所有目标,运动计划成功!";
                break;
            case Failure_Over:// 目标还未完成，你确定要结束吗?
                tmpStr = "目标还未完成，你确定要结束吗?";
                break;
            case Preparation: // 计划开始了
                tmpStr = "计划开始了";
                break;
            case SportsOver: // 你完成了今天的目标
                tmpStr = "你完成了今天的目标";
                break;
            case Message: // 消息提示音
                break;
            case FastWalk: // 快走
                tmpStr = "快走";
                break;
            case SlowWalk: // 慢走
                tmpStr = "慢走";
                break;
            case RelaxSports: // 放松运动
                tmpStr = "放松运动";
                break;
            case FastRun: // 快跑
                tmpStr = "快跑";
                break;
            case SlowRun: // 慢跑
                tmpStr = "慢跑";
                break;
            case AverageSpeed: // 平均速度
                tmpStr = "平均速度";
                break;
            case AverageSpeedUnit:// ,// 公里每小时
                tmpStr = "公里每小时";
                break;
            case ContinueProgram:// 继续计划
                tmpStr = "再来继续计划吧";
                break;
            case NearByOneMile:
                tmpStr = "最近一公里";
                break;
            case Second:
                tmpStr = "秒";
                break;
            case SPORTS:
                tmpStr = "运动";
            case Gps:
                tmpStr = "GPS";
                break;
            case GpsRegain:
                tmpStr = "已获取";
                break;
            case GpsLoss:
                tmpStr = "已丢失";
                break;
            default:
                break;
        }
        return tmpStr;
    }

    public static SoundFactory getValue(int id) {
        SoundFactory tmpSound = SoundFactory.ZERO;
        switch (id) {
            case 0:
                tmpSound = ZERO;
                break;
            case 1:
                tmpSound = ONE;
                break;
            case 2:
                tmpSound = TWO;
                break;
            case 3:
                tmpSound = THREE;
                break;
            case 4:
                tmpSound = FOUR;
                break;
            case 5:
                tmpSound = FIVE;
                break;
            case 6:
                tmpSound = SIX;
                break;
            case 7:
                tmpSound = SEVEN;
                break;
            case 8:
                tmpSound = EIGHT;
                break;
            case 9:
                tmpSound = NINE;
        }
        return tmpSound;
    }


}
/**
 *
 *
 ZERO(0), // 0
 ONE(1), // 1
 TWO(2), // 2
 THREE(3), // 3
 FOUR(4), // 4
 FIVE(5), // 5
 SIX(6), // 6
 SEVEN(7), // 7
 EIGHT(8), // 8
 NINE(9), // 9
 TEN(10), // 十
 HUNDRED(11), // 百
 THOUSAND(12), // 千
 WAN(13), // 万
 KILOMETER(14), // 公里
 Walk(15), // 走路
 Run(16), // 跑步
 Cycle(17), // 骑车
 First(18), // 首先
 Already(19), // 已经
 Last(20), // 最后
 ComeOn(21), // 加油
 Next(22), // 接下来
 Exercise_To_Relax(23), // 放松运动
 Minute(24), // 分钟
 Spend_Time(25), // 用时
 Great(26), // 太棒了！
 Completed_Goals(27), // 太棒了！你完成了今天的目标
 Completed_All_Goals(28), // 太棒了！你完成了所有目标，运动计划成功！
 Failure_Over(29), // 男：我那个去，尽然玩脱了
 Preparation(30), // 男：预备跑
 SportsOver(31), // 你已经完成目标
 Message(32), // 消息提示音
 FastWalk(33), // 快走
 SlowWalk(34), // 慢走
 RelaxSports(35), // 放松运动
 FastRun(36), // 快跑
 SlowRun(37), // 慢跑
 AverageSpeed(38), // 平均速度
 AverageSpeedUnit(39), // ,// 公里每小时
 ContinueProgram(40), // 继续计划
 DingDong(41), // 叮咚
 Second(42), // 秒
 NearByOneMile(43), // 最近一公里
 DOT(44), // 点
 SPORTS(45), HeartRateBeep(46), GpsRegain(47), GpsLoss(48), Gps(49),// 心率报警
 PauseSport(50),ContinueSport(51),
 Finish(60),//完成
 Break(61),//已经打破个人纪录
 Quarter(62),//1/4
 Half(63),//1/2
 Marathon(64),//马拉松
 Hour(65),//小时
 Lead(66),//领先
 Behind(67),//落后
 Meter(68),//米
 ChallengeSucess(69),//挑战成功
 ChallengeFail(70),//挑战失败
 FinishGoal(71),//完成目标
 Left(72),//还差
 FinalOneMile(73),//最后一公里
 BluetoothDisconnect(74);//蓝牙断开
 SoundFactory(int num) {

 }

 public static String getText(SoundFactory soundFactory) {
 String tmpStr = "";
 switch (soundFactory) {
 case KILOMETER:// 公里
 tmpStr = "公里";
 break;
 case Walk: // 走路
 tmpStr = "走路";
 break;
 case Run: // 跑步
 tmpStr = "跑步";
 break;
 case Cycle: // 骑行
 tmpStr = "骑行";
 break;
 case First:
 tmpStr = "首先";
 break;// 首先
 case Already: // 你已经
 tmpStr = "你已经";
 break;
 case Last: // 最后
 tmpStr = "最后";
 break;
 case ComeOn: // 加油吧
 tmpStr = "加油吧";
 break;
 case Next: // 接着
 tmpStr = "接着";
 break;
 case Exercise_To_Relax:
 tmpStr = "放松一下吧";
 break;// 放松运动
 case Minute: // 分钟
 tmpStr = "分钟";
 break;
 case Spend_Time: // 用时
 tmpStr = "用时";
 break;
 case Great: // 太棒了！
 tmpStr = "太棒了!";
 break;
 case Completed_Goals: // 太棒了！你完成了今天的目标
 tmpStr = "太棒了!你完成了今天的目标。";
 break;
 case Completed_All_Goals: // 太棒了！你完成了所有目标，运动计划成功！
 tmpStr = "太棒了!你完成了所有目标,运动计划成功!";
 break;
 case Failure_Over:// 目标还未完成，你确定要结束吗?
 tmpStr = "目标还未完成，你确定要结束吗?";
 break;
 case Preparation: // 计划开始了
 tmpStr = "计划开始了";
 break;
 case SportsOver: // 你完成了今天的目标
 tmpStr = "你完成了今天的目标";
 break;
 case Message: // 消息提示音
 break;
 case FastWalk: // 快走
 tmpStr = "快走";
 break;
 case SlowWalk: // 慢走
 tmpStr = "慢走";
 break;
 case RelaxSports: // 放松运动
 tmpStr = "放松运动";
 break;
 case FastRun: // 快跑
 tmpStr = "快跑";
 break;
 case SlowRun: // 慢跑
 tmpStr = "慢跑";
 break;
 case AverageSpeed: // 平均速度
 tmpStr = "平均速度";
 break;
 case AverageSpeedUnit:// ,// 公里每小时
 tmpStr = "公里每小时";
 break;
 case ContinueProgram:// 继续计划
 tmpStr = "再来继续计划吧";
 break;
 case NearByOneMile:
 tmpStr = "最近一公里";
 break;
 case Second:
 tmpStr = "秒";
 break;
 case SPORTS:
 tmpStr = "运动";
 case Gps:
 tmpStr = "GPS";
 break;
 case GpsRegain:
 tmpStr = "已获取";
 break;
 case GpsLoss:
 tmpStr = "已丢失";
 break;
 default:
 break;
 }
 return tmpStr;
 }

 public static SoundFactory getValue(int id) {
 SoundFactory tmpSound = SoundFactory.ZERO;
 switch (id) {
 case 0:
 tmpSound = ZERO;
 break;
 case 1:
 tmpSound = ONE;
 break;
 case 2:
 tmpSound = TWO;
 break;
 case 3:
 tmpSound = THREE;
 break;
 case 4:
 tmpSound = FOUR;
 break;
 case 5:
 tmpSound = FIVE;
 break;
 case 6:
 tmpSound = SIX;
 break;
 case 7:
 tmpSound = SEVEN;
 break;
 case 8:
 tmpSound = EIGHT;
 break;
 case 9:
 tmpSound = NINE;
 }
 return tmpSound;
 }

 **/