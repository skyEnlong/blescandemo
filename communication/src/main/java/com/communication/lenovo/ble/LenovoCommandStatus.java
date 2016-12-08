package com.communication.lenovo.ble;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;






import com.communication.data.CLog;
import com.communication.provider.HeartBean;

import android.R.integer;
import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;
import android.view.ViewDebug.IntToString;

public class LenovoCommandStatus {
	public static final int CONNECT_SUCCESS = 1;
	public static final int GET_SETTING_VALUE = 2;
	public static final int SET_SETTING_TIME = 34;
	public static final int SET_SETTING_VALUE_USERINFO = 31;
	public static final int SET_SETTING_VALUE_ALARM_GOAL_CAL_STEP = 32;
	public static final int SET_SETTING_VALUE_ALERT_GOAL_DIS = 33;
	public static final int GET_SPORT_DATA = 4;
	public static final int GET_SLEEP_DATA = 5;
	public static final int OTHER_REPLAY = 6;
	public static final int CLEAR_DATA = 7;
	public static final int SET_HEART_RATE_MAX_MIN_VALUE = 8;
	public static final int GET_DAILY_DATA = 9;
	public static ArrayList<Integer> settingPakcet1;
	public static ArrayList<Integer> settingPakcet2;
	private SimpleDateFormat sdFormat ;
	private static final int tenMinPSec = 10*60*1000;
	private static final int sleepSecPSec = 200*1000;
	private static final int lenoveHeartIntervalTime = 20*1000;
	public static final String productId = "168";
	public LenovoCommandStatus(){
		sdFormat = new SimpleDateFormat("yyyy MM dd HH:mm");
	}
	
	public int decideWhichCommandReplay(ArrayList<Integer> relayValue){
		if(relayValue.size()>=20){
			if(relayValue.get(0)==0xEE&&relayValue.get(1)==0xEE){
				return CONNECT_SUCCESS;
			}
			if(relayValue.get(18)==0xEE&&relayValue.get(19)==0xEE){
				return GET_SETTING_VALUE;
			}
			if(relayValue.get(19)==0xFB){
				return GET_SPORT_DATA;
			}
			if(relayValue.get(19)==0xFC){
				return GET_SLEEP_DATA;
			}
			if(relayValue.get(19)==0xFF&&relayValue.get(18)==0){
				return SET_SETTING_VALUE_USERINFO;
			}
			if(relayValue.get(19)==0xFF&&relayValue.get(18)==1){
				return SET_SETTING_VALUE_ALARM_GOAL_CAL_STEP;
			}
			if(relayValue.get(19)==0xFF&&relayValue.get(18)==2){
				return SET_SETTING_VALUE_ALERT_GOAL_DIS;
			}
			if(relayValue.get(19)==0xFD&&relayValue.get(18)==0xFF&&relayValue.get(17)==0xFF){
				return CLEAR_DATA;
			}
			if(relayValue.get(0)==0x53
					&&relayValue.get(1)==0x48
					&&relayValue.get(2)==0x52
					&&relayValue.get(3)==0x5A
					&&relayValue.get(4)==0x4F
					&&relayValue.get(5)==0x4B){
				return SET_HEART_RATE_MAX_MIN_VALUE;
			}
			if(relayValue.get(19) == 0xFA){
				return GET_DAILY_DATA;
			}
		}
		
		return OTHER_REPLAY;
	}
	
	
	public  boolean isEndOfData(ArrayList<Integer> data){
		if(data.size()>=20){
			for(int i=0;i<data.size();i++){
				if(data.get(i)!=0xFF){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	//53 4C 45 45 50 YY MM DD FF FF FF FF FF FF FF FF FF FF FF FC
	//If Get Run N=none Return 52 55 4E FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 01
	//FB
	public  boolean isHasNoSportData(ArrayList<Integer> data){
		int[] noSport = new int[]{0x52,0x55,0x4E,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0x01,0xFB};
		if(data.size()==20){
			for(int i=0;i<data.size();i++){
					if(data.get(i)!=noSport[i]){
						return false;
					}
					return true;
				}
		}
		return false;
	}
	
	//53 4C 45 45 50 YY MM DD FF FF FF FF FF FF FF FF FF FF FF FC
	public  boolean isHasNoSleepData(ArrayList<Integer> data){
		int[] noSport = new int[]{0x53,0x4C,0x45,0x45,0x50};
		if(data.size()==20){
			if(data.get(19)==0xFC){
				for(int i=0;i<5;i++){
					if(data.get(i)!=noSport[i]){
						return false;
					}
					for(int j=8;j<19;j++){
						if(data.get(j)!=0xFF){
							return false;
						}
					}
					return true;
				}
			}
			
		}
		return false;
	}
	
//	for(int i=0;i<data.size();i++){
//		if(data.get(0)==0x53
//				&&data.get(1)==0x4C
//				&&data.get(2)==0x45
//				&&data.get(3)==0x45
//				&&data.get(4)==0x50
//				&&data.get(5))
//		return true;
//	}

	public  String getCurrentDayTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yy MM DD HH:mm");
		Calendar calendar = Calendar.getInstance();
		return sdf.format(calendar.getTime());
	}
	
	//ee ee 31 34 2d 36 39 30 44 ba 0 0 8 0 1 e 9 13 0 0
	public  String getModelNo(ArrayList<Integer> data){
		StringBuilder sb = new StringBuilder();
		//sb.append("M");
		sb.append(productId+"-");
		if(data.size()==20){
			for(int i=2;i<9;i++){
				sb.append((char)data.get(i).intValue());
			}
		}
		
		return sb.toString();
	}
	
	public  String getVersion(ArrayList<Integer> data){
		if(data.size()==20){
			return data.get(9).toString();
		}
		return null;
	}
	public  int countTimeInterval(ArrayList<Integer> data){
		int startYear =0,startMonth,startDay,startHour,startMin,startSec,
				endYear,endMonth,endDay,endHour,endMin,endSec;
		
		if(data.size()==20&&data.get(18)==0){
			startYear = countYear(data.get(5));
			endYear = countYear(data.get(11));
			startMonth =  data.get(4);
			endMonth =  data.get(10);
			startDay =  data.get(3);
			endDay =  data.get(9);
			startHour =  data.get(2);
			startMin =  data.get(1);
			startSec =  data.get(0);
			endHour =  data.get(8);
			endMin =  data.get(7);
			endSec =  data.get(6);
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.set(startYear, startMonth-1, startDay, startHour, startMin, startSec);
			startCalendar.setTimeInMillis(0);
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.set(endYear, endMonth-1, endDay, endHour, endMin, endSec);
			endCalendar.setTimeInMillis(0);
			return (int)(endCalendar.getTimeInMillis()-startCalendar.getTimeInMillis());
		}
		return -1;
	}
	public  int countYear(int year){
		return year+2000;
	}
	
	public  int[] getSportHead(){
		return new int[]{0xFE,0xFE,0xFE,0xFE,0xFE,0xFE};
	}
	public  int[] getSleepHead(){
		return new int[]{0xFD,0xFD,0xFD,0xFD,0xFD,0xFD};
	}
	public  int[] getStartTimeHead(ArrayList<Integer> data){
		if(data.size()==20&&data.get(18)==0){
			int year = countYear(data.get(5));
			int yearH = year/100;
			int yearL = year%100;
			return new int[]{Integer.valueOf(Integer.toHexString(yearH)),
					Integer.valueOf(Integer.toHexString(yearL)),
					data.get(4),data.get(3),data.get(2),data.get(1)};
		}
		return null;
	}
	
	//23 2b c 9 9 e  24 2b c 9 9 e 0 0 0 0 0 15 0 fffffffb---1
	//35 2b c 9 9 e 3a 2b c 9 9 e 0 0 0 0 0 16 0 fb---1
	//0 0 4 1 0 0 4 0 0 0 0 0 0 0 0 0 0 16 1 fb
	//(Exercise Time :B0=Hour, B1=Min, B2=Sec,B3=pSec;Rest Time;B09 step;b10 Distance )
	//0 0 0 0 0 0 23 0 0 1 6d 6d 0 0 0 0 14 16 2 fb
	//Calories b0-b3;Avg Speed b4-b5 km/10;Max Speed b6-b7 km/10;B08:Data Packet Number,b10 Max HRM ;b11 avg hrm;b15 hr data packet
	//0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 19 3 fb
	//0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 19 4 fb
	//b0-b15 avg speed
	//0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 19 5 fb
	//68 67 67 66 66 66 65 65 65 65 65 65 65 65 65 65 1 19 7 fb
	//b0-b15 avg hr if b16 是1
	public  ArrayList<ArrayList<Integer>> getSportSleepData(ArrayList<ArrayList<Integer>> data){
		if(null==data||data.size()==0){
			return null;
		}
		ArrayList<ArrayList<Integer>> comformData = new ArrayList<ArrayList<Integer>>();
		//Map<String, Integer[][]> sports = new HashMap<String, Integer[][]>();
		for(int i=0;i<data.size();i++){
			ArrayList<Integer> item = data.get(i);
			if(item.size()==20){
				if(item.get(19)==0xFB){
					//sportData
					if(item.get(18)==0){
						//sport data packet 0; get start and end start_time;
						
						getSportsData(comformData,data,i);
						CLog.d("enLong", "conformData size="+comformData.size());
						StringBuilder sb = new StringBuilder();
						for(int z=0;z<comformData.size();z++){
							ArrayList<Integer> dArrayList = comformData.get(z);
							for(int y=0;y<dArrayList.size();y++){
								sb.append(dArrayList.get(y)+" ");
							}
						}
						CLog.d("enLong", "conformData ="+sb.toString());
//						if(i==index){
//							i++;
//						}else{
//							i = index;
//						}
						
						
					}
				}else if(item.get(19)==0xFC){
					//sleep Data
					if(item.get(18)==0){
						//get sleep data;
						
						getSleepData(comformData,data,i);
						
//						if(i==index){
//							i++;
//						}else{
//							i = index;
//						}
						
					}
				}else if(item.get(19) == 0xFA){
					//get daily sportData
					if(item.get(18)==0){
						getDailyData(comformData,data,i);
					}
					
				}
			}
		}
		Log.d("conform","before return comformData size="+comformData.size());
		return comformData;
	}
	private String getDayString(int[] time){
		if(time.length>=3){
			return time[0]+"-"+time[1]+"-"+time[2];
		}
		return null;
	}
	private int getSportDayIndex(int hour,int min,int sec){
		return (hour*60*60+min*60+sec)/(10*60) -1;
	}
	private Integer[][] initSportDayData(){
		Integer[][] aa = new Integer[144][3];
		for(int i=0;i<144;i++){
			aa[i][0] =0;
			aa[i][1] =0;
			aa[i][2] =0;
		}
		return aa;
	}
	private int[] getStartTime(ArrayList<Integer> data){
		int sSecond =  data.get(0);
		int sMin =  data.get(1);
		int sHour =  data.get(2);
		int sDay =  data.get(3);
		int sMonth =  data.get(4);
		int sYear =  countYear(data.get(5));
		return new int[]{sYear,sMonth,sDay,sHour,sMin,sSecond};
	}
	/**
	 * 整合
	 * @param transData
	 * @param data
	 * @param index
	 */
	public void getSportsData(Map<String, Integer[][]> transData,ArrayList<ArrayList<Integer>> data,int index){
		ArrayList<Integer> pack0 = data.get(index);
		int[] time = getStartTime(pack0);
		String dayStr = getDayString(time);
		Integer[][] sDatas = null;
		if(null!=dayStr){
			sDatas = transData.get(dayStr);
		}else{
			return;
		}
		if(sDatas==null){
			sDatas = initSportDayData();
			
		}
		int startIndex = getSportDayIndex(time[3],time[4],time[5]);
		long[] startEndTime = getSportHeadStartAndEndTime(pack0);
		int runNO = pack0.get(17);
		Log.d("conform", "runNO="+runNO);
		int totalStep = 0;
		int totalDis = 0;
		int totalCal = 0;
		int totalNum = 0;
		int totalSportNum = 0;
		int totalHeartNum = 0;
		ArrayList<Integer> pack01 = data.get(index+1);
		ArrayList<Integer> pack02 = data.get(index+2);
		//get pack01 data
		if(pack01.size()==20 && pack01.get(19)==0xFB&&pack01.get(18)==1&&pack01.get(17)==runNO){
			ArrayList<Integer> analysisP1 = analysisSportPacket1(pack01);
			totalStep = analysisP1.get(2);
			totalDis = analysisP1.get(3);
		}
		
		//get pack02 data
		if(pack02.size()==20 && pack02.get(19)==0xFB&&pack02.get(18)==2&&pack02.get(17)==runNO){
			//same sport data
			ArrayList<Integer> analysisP2 = analysisSportPacket2(pack02);
			totalCal = analysisP2.get(0);
			totalSportNum = analysisP2.get(1);
			totalHeartNum = analysisP2.get(4);
			totalNum = analysisP2.get(1)+analysisP2.get(4);
			
		}
		Log.d("conform","totalStep ="+totalStep+" totalCa="+totalCal+" totalD="+totalDis+" num="+totalNum);
		long sportsDur = startEndTime[1]-startEndTime[0];
		int avgSpeedCount =(int) Math.floor(sportsDur/(60*1000));
		if(sportsDur>=tenMinPSec){
			//more than 10 min
			//if(totalSportNum!=0){
				//have avg speed data
				float totalSpeedValue = 0;
				ArrayList<Integer> speedData = getTotalSpeedValue(data,index+3,runNO,avgSpeedCount);
				int count = 0;
				int stepSum = 0;
				int disSum = 0;
				int calSum = 0;
				if(null!=speedData&&speedData.size()>0){
					totalSpeedValue = speedData.get(speedData.size()-1);
				}
				//
				int addedStep = 0;
				int addedCal = 0;
				int addedDis = 0;
				ArrayList<Float> sumBy10Min = get10MinTotalSpeedValue(speedData);
				ArrayList<Integer> stepArrayList = new ArrayList<Integer>();
				ArrayList<Integer> calArrayList = new ArrayList<Integer>();
				ArrayList<Integer> disArrayList = new ArrayList<Integer>();
				Log.d("conform","sumBy10Min size="+sumBy10Min.size());
				for(int i=0;i<sumBy10Min.size();i++){
					float ratio = sumBy10Min.get(i)/totalSpeedValue;
					int s = (int)(totalStep*ratio);
					int c = (int)(totalCal*ratio);
					int d = (int)(totalDis*ratio);
					addedStep +=s;
					addedCal += c;
					addedDis += d;
					
					stepArrayList.add(s);
					calArrayList.add(c);
					disArrayList.add(d);

				}
				int restStep = totalStep - addedStep;
				int restCal = totalCal - addedCal;
				int restDis = totalDis - addedDis;
				for(int i=0;i<restStep;i++){
					int cIndex = (int)(Math.random()*10)%(sumBy10Min.size()-1);
					stepArrayList.set(cIndex, stepArrayList.get(cIndex)+1);
				}
				for(int i=0;i<restCal;i++){
					int cIndex = (int)(Math.random()*10)%(sumBy10Min.size()-1);
					stepArrayList.set(cIndex, calArrayList.get(cIndex)+1);
				}
				for(int i=0;i<restDis;i++){
					int cIndex = (int)(Math.random()*10)%(sumBy10Min.size()-1);
					stepArrayList.set(cIndex, disArrayList.get(cIndex)+1);
				}
				for(int i=0;i<sumBy10Min.size();i++){
					Log.d("conform","stepArrayList i="+stepArrayList.get(i)+" calArrayList.get(i)="+calArrayList.get(i)+" disArrayList.get(i)="+disArrayList.get(i));
//					sportData.add(stepArrayList.get(i)/256);
//					sportData.add(stepArrayList.get(i)%256);
//					sportData.add(calArrayList.get(i)/256);
//					sportData.add(calArrayList.get(i)%256);
//					sportData.add(disArrayList.get(i)/256);
//					sportData.add(disArrayList.get(i)%256);
					sDatas[startIndex+i][0] = sDatas[startIndex+i][0] + stepArrayList.get(i);
					sDatas[startIndex+i][1] = sDatas[startIndex+i][1] + calArrayList.get(i);
					sDatas[startIndex+i][2] = sDatas[startIndex+i][2] + disArrayList.get(i);
				}
				
				
		}else{
			//less than 10 min
//			sportData.add(totalStep/256);
//			sportData.add(totalStep%256);
//			sportData.add(totalCal/256);
//			sportData.add(totalCal%256);
//			sportData.add(totalDis/256);
//			sportData.add(totalDis%256);
//			comformData.add(sportData);
//			sportData = null;
			sDatas[startIndex][0] = sDatas[startIndex][0] + totalStep;
			sDatas[startIndex][1] = sDatas[startIndex][1] + totalCal;
			sDatas[startIndex][2] = sDatas[startIndex][2] + totalCal;
		}
		transData.remove(dayStr);
		transData.put(dayStr, sDatas);
	}
	public  ArrayList<HeartBean> getHeartData(ArrayList<ArrayList<Integer>> data,String productId){
		ArrayList<HeartBean> comforData = new ArrayList<HeartBean>();
		Map<String, HeartBean> heartBeans = new HashMap<String, HeartBean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for(int i=0;i<data.size();i++){
			ArrayList<Integer> item = data.get(i);
			if(item.size()==20){
				if(item.get(19)==0xFB){
					//sportData
					if(item.get(18)==0){
						//sport data packet 0; get start and end start_time;
						int runNO = item.get(17);
						long[] startEndTimeDur = getHeartStartEndTimeDur(item);
						long[] startEndTime = getSportHeadStartAndEndTime(item);
						int heartStartIndex = (int)startEndTimeDur[0]/lenoveHeartIntervalTime;
						
						long thisSportDay0Time = getOneDayStartTime(startEndTime[0]);
						String thisDayStr = sdf.format(new Date(thisSportDay0Time));
						HeartBean hBean = null;
						int[] dayData = initDayHeartData();
						if(null!=heartBeans&&heartBeans.size()>0){
							hBean = heartBeans.get(thisDayStr);
						}
						if(null==hBean){
							hBean = new HeartBean();
							hBean.setProductId(productId);
							hBean.setDayTime(thisDayStr);
						}else{
							dayData = hBean.getHeartData();
						}
						ArrayList<Integer> pck2Data = analysisSportPacket2(data.get(i+2));
						ArrayList<Integer> heartColumData = getOneSportHeartDatas(data,i,runNO);
						
						if(null!=heartColumData){
							for(int j=0;j<heartColumData.size();j++){
								int cIndex = heartStartIndex+j;
								if(cIndex >= dayData.length){
									cIndex = dayData.length-1;
								}
								if(cIndex<0){
									cIndex = 0;
								}
								dayData[cIndex] = heartColumData.get(j);
							}
							/*StringBuilder sBuilder = new StringBuilder();
							for(int jj=0;jj<heartColumData.size();jj++){
								sBuilder.append(dayData[jj+heartStartIndex]+" ");
							}*/
							
						}
						hBean.setHeartData(dayData);
						if(heartBeans.containsKey(hBean.getDayTime())){
							heartBeans.remove(hBean.getDayTime());
						}
						heartBeans.put(hBean.getDayTime(), hBean);
						//comforData.add(hBean);
						/*StringBuilder sBuilder = new StringBuilder();
						for(int jj=heartStartIndex;jj<dayData.length;jj++){
							sBuilder.append(dayData[jj]+" ");
						}*/
						hBean = null;
						
						
					}
				}
			}
		}
		Set<String> keys =heartBeans.keySet();
		for(String key:keys){
			comforData.add(heartBeans.get(key));
		}
		return comforData;
	}
	private  ArrayList<Integer> getOneSportHeartDatas(ArrayList<ArrayList<Integer>> data,int sIndex,int runNO){
		ArrayList<Integer> hearts =new ArrayList<Integer>();
		int i = sIndex;
		
		while(i<data.size()&&data.get(i).get(19)==0xFB){
			ArrayList<Integer> item = data.get(i);
			i++;
			
			
					
			if(item.get(17)==runNO){
				if(item.get(16)==1&&item.get(18)>2){
					for(int j=0;j<16;j++){
						if(item.get(j)!=0xFF){
							
							hearts.add( item.get(j));
						}
					}
				}
				
			}else{
				
				break;
			}
		}
//		for(int i=sIndex;i<eIndex;i++){
//			
//			if(item.size()==20&&item.get(16)==1){
//				hearts = new ArrayList<Integer>();
//				for(int j=0;j<16;j++){
//					if(item.get(j)!=0xFF){
//						hearts.add( item.get(j));
//					}
//				}
//			}
//		}
		StringBuilder sb = new StringBuilder();
		Log.d("conform", "hearts size="+hearts.size());
		for(int j=0;j<hearts.size();j++){
			sb.append(hearts.get(j)+" ");
		}
		Log.d("conform","one sport heart="+sb.toString());
		return hearts;
	}
	private  long getOneDayStartTime(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	private  int[] initDayHeartData(){
		int[] dayData = new int[4320];
		for(int i=0;i<dayData.length;i++){
			dayData[i] = 0;
		}
		return dayData;
	}
	// count one sports data
	/**
	 * if sport's start_time > 10 min, break it up into 10 min
	 * else create a new sport data;
	 * @param comformData
	 * @param data
	 * @param index
	 * @return
	 */
	public  void getSportsData(ArrayList<ArrayList<Integer>> comformData,ArrayList<ArrayList<Integer>> data,int index){
		ArrayList<Integer> sportData = new ArrayList<Integer>();
		ArrayList<Integer> pack0 = data.get(index);
		long[] startEndTime = getSportHeadStartAndEndTime(pack0);
		addSportHead(sportData, startEndTime,0);
		int runNO = pack0.get(17);
		Log.d("conform", "runNO="+runNO);
		int totalStep = 0;
		int totalDis = 0;
		int totalCal = 0;
		int totalNum = 0;
		int totalSportNum = 0;
		int totalHeartNum = 0;
		ArrayList<Integer> pack01 = data.get(index+1);
		ArrayList<Integer> pack02 = data.get(index+2);
		//get pack01 data
		if(pack01.size()==20 && pack01.get(19)==0xFB&&pack01.get(18)==1&&pack01.get(17)==runNO){
			ArrayList<Integer> analysisP1 = analysisSportPacket1(pack01);
			totalStep = analysisP1.get(2);
			totalDis = analysisP1.get(3);
		}
		
		//get pack02 data
		if(pack02.size()==20 && pack02.get(19)==0xFB&&pack02.get(18)==2&&pack02.get(17)==runNO){
			//same sport data
			ArrayList<Integer> analysisP2 = analysisSportPacket2(pack02);
			totalCal = analysisP2.get(0)/100;
			totalSportNum = analysisP2.get(1);
			totalHeartNum = analysisP2.get(4);
			totalNum = analysisP2.get(1)+analysisP2.get(4);
			
		}
		Log.d("conform","totalStep ="+totalStep+" totalCa="+totalCal+" totalD="+totalDis+" num="+totalNum);
		long sportsDur = startEndTime[1]-startEndTime[0];
		int avgSpeedCount =(int) Math.floor(sportsDur/(60*1000));
		if(sportsDur>=tenMinPSec){
			//more than 10 min
			//if(totalSportNum!=0){
				//have avg speed data
				float totalSpeedValue = 0;
				ArrayList<Integer> speedData = getTotalSpeedValue(data,index+3,runNO,avgSpeedCount);
				int count = 0;
				int stepSum = 0;
				int disSum = 0;
				int calSum = 0;
				if(null!=speedData&&speedData.size()>0){
					totalSpeedValue = speedData.get(speedData.size()-1);
				}
				//
				int addedStep = 0;
				int addedCal = 0;
				int addedDis = 0;
				ArrayList<Float> sumBy10Min = get10MinTotalSpeedValue(speedData);
				ArrayList<Integer> stepArrayList = new ArrayList<Integer>();
				ArrayList<Integer> calArrayList = new ArrayList<Integer>();
				ArrayList<Integer> disArrayList = new ArrayList<Integer>();
				Log.d("conform","sumBy10Min size="+sumBy10Min.size());
				for(int i=0;i<sumBy10Min.size();i++){
					float ratio = sumBy10Min.get(i)/totalSpeedValue;
					int s = (int)(totalStep*ratio);
					int c = (int)(totalCal*ratio);
					int d = (int)(totalDis*ratio);
					addedStep +=s;
					addedCal += c;
					addedDis += d;
					stepArrayList.add(s);
					calArrayList.add(c);
					disArrayList.add(d);
//					sportData.add(s/256);
//					sportData.add(s%256);
//					sportData.add(c/256);
//					sportData.add(c%256);
//					sportData.add(d/256);
//					sportData.add(d%256);
				}
				int restStep = totalStep - addedStep;
				int restCal = totalCal - addedCal;
				int restDis = totalDis - addedDis;
				for(int i=0;i<restStep;i++){
					int cIndex = (int)(Math.random()*10)%(sumBy10Min.size());
					stepArrayList.set(cIndex, stepArrayList.get(cIndex)+1);
				}
				for(int i=0;i<restCal;i++){
					int cIndex = (int)(Math.random()*10)%(sumBy10Min.size());
					stepArrayList.set(cIndex, calArrayList.get(cIndex)+1);
				}
				for(int i=0;i<restDis;i++){
					int cIndex = (int)(Math.random()*10)%(sumBy10Min.size());
					stepArrayList.set(cIndex, disArrayList.get(cIndex)+1);
				}
				for(int i=0;i<sumBy10Min.size();i++){
					Log.d("conform","stepArrayList i="+stepArrayList.get(i)+" calArrayList.get(i)="+calArrayList.get(i)+" disArrayList.get(i)="+disArrayList.get(i));
					sportData.add(stepArrayList.get(i)/256);
					sportData.add(stepArrayList.get(i)%256);
					sportData.add(calArrayList.get(i)/256);
					sportData.add(calArrayList.get(i)%256);
					sportData.add(disArrayList.get(i)/256);
					sportData.add(disArrayList.get(i)%256);
				}
				comformData.add(sportData);
				sportData = null;
				
		}else{
			//less than 10 min
			sportData.add(totalStep/256);
			sportData.add(totalStep%256);
			sportData.add(totalCal/256);
			sportData.add(totalCal%256);
			sportData.add(totalDis/256);
			sportData.add(totalDis%256);
			comformData.add(sportData);
			sportData = null;
		}
		
	}
	private int getNexSportIndex(ArrayList<ArrayList<Integer>> data,int startIndex,int runNO){
		int i= startIndex;
		while(data.get(i).get(17)==runNO){
			i ++;
		}
		return i;
	}
	private  ArrayList<Integer> getTotalSpeedValue(ArrayList<ArrayList<Integer>> data,int startIndex,int runNO,int dataCount){
		ArrayList<Integer> returnDataArrayList = new ArrayList<Integer>();
		int totalSpeed = 0;
		int i = startIndex;
		int count = 0;
		while(data.get(i).get(17)==runNO&&data.get(i).get(16)==0&&i<data.size()){
			ArrayList<Integer> avgSpeedsArrayList = data.get(i);
			if(avgSpeedsArrayList.size()==20){
				for(int j=0;j<16;j++){
					if(avgSpeedsArrayList.get(19)==0xFB&&avgSpeedsArrayList.get(16)==0){
						//if(avgSpeedsArrayList.get(j)!=0x1A){
						  if(count <dataCount){
							  returnDataArrayList.add(avgSpeedsArrayList.get(j));
							  totalSpeed +=  avgSpeedsArrayList.get(j);
							  count ++;
						  }else{
							  break;
						  }
							
						//}
					}
				}
			}
			i++;
		}
		/*for(int i=startIndex;i<endIndex;i++){
			ArrayList<Integer> avgSpeedsArrayList = data.get(i);
			if(avgSpeedsArrayList.size()==20){
				for(int j=0;j<16;j++){
					if(avgSpeedsArrayList.get(19)==0xFB&&avgSpeedsArrayList.get(16)==0){
						if(avgSpeedsArrayList.get(j)!=0x1A){
							returnDataArrayList.add(avgSpeedsArrayList.get(j));
							totalSpeed +=  avgSpeedsArrayList.get(j);
						}
					}
				}
			}
			
		}*/
		returnDataArrayList.add(totalSpeed);
		Log.d("test", "totalSpeed="+totalSpeed);
		return returnDataArrayList;
	}
	private  ArrayList<Float> get10MinTotalSpeedValue(ArrayList<Integer> data){
		ArrayList<Float> sumBy10Min = new ArrayList<Float>();
		if(null!=data){
			int count =0;
			float sum = 0;
			for(int i=0;i<data.size()-1;i++){
				if(count < 10){
					sum +=data.get(i);
					count ++;
				}else{
					sumBy10Min.add(sum);
					Log.d("test", "sum="+sum+" count ="+count+" i="+i);
					sum = 0;
					count = 0;
					sum += data.get(i);
					count ++;
				}
//				if(data.get(i)!=0x1A){
//					if(count<=10){
//						sum +=  data.get(i);
//						count ++;
//					}else{
//						sumBy10Min.add(sum);
//						sum = 0;
//						count = 0;
//					}
//					
//				}
			}
			if(count >0){
				sumBy10Min.add(sum);
			}
			return sumBy10Min;
		}else{
			return null;
		}
		
	}
	/**
	 * break up data by start_time
	 * @param sportsDur
	 * @param totalStep
	 * @param totalCal
	 * @param totalDis
	 * @param startEndTime
	 * @param sportData
	 * @param comformData
	 */
	/*private  void avgDividSportData(long sportsDur,int totalStep,int totalCal,int totalDis,long[] startEndTime,ArrayList<Integer> sportData,ArrayList<ArrayList<Integer>> comformData){
		
		int dividNum = (int)(sportsDur/tenMinPSec);
		int restNum = (int)(sportsDur % tenMinPSec);
		
		if(restNum==0){
			int avgStep = totalStep/dividNum;
			int avgCal = totalCal/dividNum;
			int avgDis = totalDis/dividNum;
			Log.d("test","avgStep ="+avgStep+" avgCal="+avgCal+" avgDis="+avgDis);
			for(int j=1;j<dividNum;j++){
				sportData.add(avgStep/256);
				sportData.add(avgStep%256);
				sportData.add(avgCal/256);
				sportData.add(avgCal%256);
				sportData.add(avgDis/256);
				sportData.add(avgDis%256);
			}
			sportData.add((totalStep-(dividNum-1)*avgStep)/256);
			sportData.add(totalStep-(dividNum-1)*avgStep%256);
			sportData.add(totalCal-(dividNum-1)*avgCal/256);
			sportData.add(totalCal-(dividNum-1)*avgCal%256);
			sportData.add(totalDis-(dividNum-1)*avgDis/256);
			sportData.add(totalDis-(dividNum-1)*avgDis%256);
			comformData.add(sportData);
			sportData = null;
		}else{
			//add 10 min data
			float ratio = (float)tenMinPSec/(float)sportsDur;
			for(int j=0;j<dividNum;j++){
				sportData.add((int)(totalStep*ratio)/256);
				sportData.add((int)(totalStep*ratio)%256);
				sportData.add((int)(totalCal*ratio)/256);
				sportData.add((int)(totalCal*ratio)%256);
				sportData.add((int)(totalDis*ratio)/256);
				sportData.add((int)(totalDis*ratio)%256);
			}
			comformData.add(sportData);
			sportData = null;
			//add rest min data
			sportData = new ArrayList<Integer>();
			addSportHead(sportData, startEndTime,dividNum);
			sportData.add((int)(totalStep*(1-dividNum*ratio))/256);
			sportData.add((int)(totalStep*(1-dividNum*ratio))%256);
			sportData.add((int)(totalCal*(1-dividNum*ratio))/256);
			sportData.add((int)(totalCal*(1-dividNum*ratio))%256);
			sportData.add((int)(totalDis*(1-dividNum*ratio))/256);
			sportData.add((int)(totalDis*(1-dividNum*ratio))%256);
			comformData.add(sportData);
			sportData = null;
			
		}
	}*/
	private  void addSportHead(ArrayList<Integer> sportItem,int year,int month,int day,int hour){
		int[] sportHead = getSportHead();
		for(int i=0;i<sportHead.length;i++){
			sportItem.add(sportHead[i]);
		}
		
		addTimeHead(sportItem,year,month,day,hour);
		
	}
	private  void addTimeHead(ArrayList<Integer> sportItem,int year,int month,int day,int hour){
		int yearH = year/100;
		int yearL = year%100;
		sportItem.add(Integer.parseInt(String.valueOf(yearH),16));
		sportItem.add(Integer.parseInt(String.valueOf(yearL),16));
		sportItem.add(Integer.parseInt(String.valueOf(month),16));
		sportItem.add(Integer.parseInt(String.valueOf(day),16));
		
		sportItem.add(Integer.parseInt(String.valueOf(hour),16));
		sportItem.add(0);
		

	}
	private  void addSportHead(ArrayList<Integer> sportItem,long[] time,int passItems){
		int[] sportHead = getSportHead();
		for(int i=0;i<sportHead.length;i++){
			sportItem.add(sportHead[i]);
		}
		long startTime = 0;
		
		if(passItems == 0){
			startTime= time[0];
		}else{
			startTime = time[0]+((passItems+1)*10+1)*60*1000;
		}
		addTimeHead(sportItem,startTime);
		
	}
	private  void addTimeHead(ArrayList<Integer> sportItem,long startTime){
		String startTimeStr = sdFormat.format(new Date(startTime));
		String[] times = startTimeStr.split(" ");
		Log.d("conform", "times[0]"+times[0]+" 1="+times[1]+" 2="+times[2]+" 3="+times[3]+" times[0].substring(0,2)="+times[0].substring(0,2));
		sportItem.add(getValue(times[0].substring(0,2)));
		sportItem.add(getValue(times[0].substring(2,4)));
		sportItem.add(getValue(times[1]));
		sportItem.add(getValue(times[2]));
		String[] hmStr = times[3].split(":");
		sportItem.add(getValue(hmStr[0]));
		sportItem.add(getValue(hmStr[1]));
		

	}
	
	private int getValue(String a){

		int h = Integer.valueOf(a.substring(0,1));
		int l = Integer.valueOf(a.substring(1));
		return h*16+l;
		
	}
	private void getDailyData(ArrayList<ArrayList<Integer>> comformData,ArrayList<ArrayList<Integer>> data,int index){
		ArrayList<Integer> sportData = null;
		ArrayList<Integer> pack0 = data.get(index);
		int[] time = analiseDailyPack0(pack0);
		int i = index +1;
		//CLog.d("enLong", "i ="+i+" index= "+index+" data.get(i).get(18)="+data.get(i).get(18)+" size="+data.size());
		while(i<data.size()&&data.get(i).get(19)==0xFA&&data.get(i).get(18)!=0){
			ArrayList<Integer> item = data.get(i);
			//CLog.d("enLong", "i ="+i);
			if(isEffectiveNum(item,0)){
				sportData = new ArrayList<Integer>();
				int step1 = item.get(1) << 8 | item.get(0);
				int dis1 = item.get(5) << 24 | item.get(4) << 16 | item.get(3) << 8 | item.get(2);
				int cal1 = (item.get(7) << 8 | item.get(6))/100;
				int hour1 = item.get(8);
				CLog.d("enLong", "sportData="+sportData+" start_time[0]"+time[0]+" start_time[1]"+time[1]);
				addSportHead(sportData, time[0],time[1],time[2],hour1);
				get10MinSportData(step1,dis1,cal1,sportData);
				comformData.add(sportData);
				
			}
			if(isEffectiveNum(item, 9)){
				sportData = null;
				sportData = new ArrayList<Integer>();
				int step2 = item.get(10) << 8 | item.get(9);
				int dis2 = item.get(14) << 24 | item.get(13) << 16 | item.get(12) << 8 | item.get(11);
				int cal2 = (item.get(16) << 8 | item.get(15))/100;
				int hour2 = item.get(17);
				addSportHead(sportData, time[0],time[1],time[2],hour2);
				get10MinSportData(step2,dis2,cal2,sportData);
				comformData.add(sportData);
				sportData = null;
			}
			i++;
			
		}
	}
	private boolean isEffectiveNum(ArrayList<Integer> data,int startIndex){
		
		for(int i=startIndex;i<8+startIndex;i++){
			if(data.get(i)==0xFF){
				return false;
			}
			
		}
		return true;
	}
	private boolean isEffectiveData(ArrayList<Integer> data,int startIndex){
		
		for(int i=startIndex;i<8+startIndex;i++){
			if(data.get(i) >0 && data.get(i)<0xFF){
				return true;
			}
			
		}
		return false;
	}
	private void get10MinSportData(int step,int dis,int cal,ArrayList<Integer> sportData){
		int avgStep = step/6;
		int avgDis = dis/6;
		int avgCal = cal/6;
		ArrayList<Integer> stepArrayList = new ArrayList<Integer>();
		ArrayList<Integer> calArrayList = new ArrayList<Integer>();
		ArrayList<Integer> disArrayList = new ArrayList<Integer>();
		for(int i=0;i<6;i++){
			stepArrayList.add(avgStep);
			calArrayList.add(avgCal);
			disArrayList.add(avgDis);
		}
		int restStep = step%6;
		int restDis = dis%6;
		int restCal = cal%6;
		for(int i=0;i<restStep;i++){
			int cIndex = (int)(Math.random()*10)%6;
			stepArrayList.set(cIndex, stepArrayList.get(cIndex)+1);
		}
		for(int i=0;i<restDis;i++){
			int cIndex = (int)(Math.random()*10)%6;
			disArrayList.set(cIndex, disArrayList.get(cIndex)+1);
		}
		for(int i=0;i<restCal;i++){
			int cIndex = (int)(Math.random()*10)%6;
			calArrayList.set(cIndex, calArrayList.get(cIndex)+1);
		}
		for(int i=0;i<6;i++){
			sportData.add(stepArrayList.get(i)/256);
			sportData.add(stepArrayList.get(i)%256);
			sportData.add(calArrayList.get(i)/256);
			sportData.add(calArrayList.get(i)%256);
			sportData.add(disArrayList.get(i)/256);
			sportData.add(disArrayList.get(i)%256);
		}
		
	}
	private int[] analiseDailyPack0(ArrayList<Integer> data){
		int[] time = new int[3];
		if(data.get(19)==0xFA && data.get(18)==0){
			time[0] = countYear(data.get(2));
			time[1] = data.get(1);
			time[2] = data.get(0);
		}
		CLog.d("enLong", "daily start_time year="+time[0]+" month ="+time[1]+" day="+time[2]);
		return time;
	}
	public void getSleepData(ArrayList<ArrayList<Integer>> comformData,ArrayList<ArrayList<Integer>> data,int index){
		
		ArrayList<Integer> sportData = new ArrayList<Integer>();
		ArrayList<Integer> pack0 = data.get(index);
		long[] startEndTime = getSleepHeadStartAndEndTime(pack0);
		int sleepDataPackNum = (int)startEndTime[2];
		
		addSleepHead(sportData, startEndTime);
		int runNO = pack0.get(17);
		int sleepDur = (int)(startEndTime[1]-startEndTime[0]);
		int sleepColumnCount = (int)Math.ceil(sleepDur/sleepSecPSec);
		ArrayList<Integer> sleepColumDatas = getSleepMinData(data, index+2);
		float columnTime = (float)sleepDur/sleepColumDatas.size();
		ArrayList<Integer> pack01 = data.get(index+1);
		
		//get pack01 data
		if(pack01.size()==20 && pack01.get(19)==0xFC&&pack01.get(18)==1&&pack01.get(17)==runNO){
			ArrayList<Integer> analysisP1 = analysisSleepPacket1(pack01);
			
		}
		//get sleep data
		Log.d("conform","sleepCount="+sleepColumnCount+" sleepColumDatas.size()="+sleepColumDatas.size());
		for(int i=0;i<sleepColumnCount;i++){
			int cIndex = (int)Math.round(i*sleepSecPSec/columnTime);
			if(cIndex >=sleepColumDatas.size()){
				cIndex = sleepColumDatas.size()-1;
			}
			sportData.add(sleepColumDatas.get(cIndex)/256);
			sportData.add(sleepColumDatas.get(cIndex)%256);
		}
		if(sleepColumnCount%3!=0){
			int needAdd = 3-sleepColumnCount%3;
			for(int i=0;i<needAdd;i++){
				sportData.add(0);
				sportData.add(0);
			}
		}
		comformData.add(sportData);
		sportData = null;
//		if(isAddNewOne){
//			sportData = new ArrayList<Integer>();
//			addSleepHead(sportData, startEndTime);
//			int columNum = (int)Math.ceil((sleepDur%tenMinPSec)/sleepSecPSec);
//			for(int i=0;i<columNum;i++){
//				int cIndex = (int)Math.round((sleepRow+i)*sleepSecPSec/columnTime);
//				if(cIndex >=sleepColumDatas.size()){
//					cIndex = sleepColumDatas.size()-1;
//				}
//				sportData.add(sleepColumDatas.get(cIndex)/256);
//				sportData.add(sleepColumDatas.get(cIndex)%256);
//			}
//			comformData.add(sportData);
//			sportData = null;
//		}
		
		
	}
	private  ArrayList<Integer> getSleepMinData(ArrayList<ArrayList<Integer>> data,int startIndex){
		ArrayList<Integer> sleepDatas = new ArrayList<Integer>();
		int i = startIndex;
		while(i<data.size()&&data.get(i).get(18)!=0){
			ArrayList<Integer> rowData = data.get(i);
			i++;
			if(rowData.size()==20){
				for(int j=0;j<16;j++){
					if(rowData.get(j)!=0x1A){
						if(rowData.get(j)==0){
							sleepDatas.add(1);
						}else if(rowData.get(j)==1){
							sleepDatas.add(5);
						}else if(rowData.get(j)==2){
							sleepDatas.add(18);
						}else{
							sleepDatas.add( rowData.get(j));
						}
						
					}
				}
			}
		}
//		for(int i=startIndex;i<endIndex;i++){
//			ArrayList<Integer> rowData = data.get(i);
//			if(rowData.size()==20){
//				for(int j=0;j<16;j++){
//					if(rowData.get(j)!=0x1A){
//						sleepDatas.add( rowData.get(j));
//					}
//				}
//			}
//		}
		return sleepDatas;
	}
	public  void addSleepHead(ArrayList<Integer> sportData,long[] startEndTime){
		int[] sleepHead = getSleepHead();
		for(int i=0;i<sleepHead.length;i++){
			sportData.add(sleepHead[i]);
		}
		long startTime = startEndTime[0];
		
		addTimeHead(sportData,startTime);
	}
	public long[] getHeartStartEndTimeDur(ArrayList<Integer> data){
		long[] time = new long[2];
		int sSecond =  data.get(0);
		int sMin =  data.get(1);
		int sHour =  data.get(2);
		time[0] = sHour*60*60*1000+sMin*60*1000+sSecond*1000;
		int eSecond =  data.get(6);
		int eMin =  data.get(7);
		int eHour =  data.get(8);
		time[1] = eHour*60*60*1000+eMin*60*1000+eSecond*1000;
		
		return time;
	}
	public  long[] getSportHeadStartAndEndTime(ArrayList<Integer> data){
		long[] time = new long[2];
		int sSecond =  data.get(0);
		int sMin =  data.get(1);
		int sHour =  data.get(2);
		int sDay =  data.get(3);
		int sMonth =  data.get(4);
		int sYear =  countYear(data.get(5));
		int eSecond =  data.get(6);
		int eMin =  data.get(7);
		int eHour =  data.get(8);
		int eDay =  data.get(9);
		int eMonth =  data.get(10);
		int eYear =  countYear(data.get(11));
		
		time[0] = getSettedTime(sYear,sMonth,sDay,sHour,sMin,sSecond);
		time[1] = getSettedTime(eYear, eMonth, eDay, eHour, eMin, eSecond);
		return time;
	}
	//35 2b c 9 9 e 3a 2b c 9 9 e 0 0 0 0 0 16 0 fb---1
		//0 0 4 1 0 0 4 0 0 0 0 0 0 0 0 0 0 16 1 fb
		//(Exercise Time :B0=Hour, B1=Min, B2=Sec,B3=pSec;Rest Time;B09 step;b13 Distance )
	public  ArrayList<Integer> analysisSportPacket1(ArrayList<Integer> data){
		 int excersieTime = getTotalTime(data.get(0), data.get(1), data.get(2),data.get(3));
		 int restTime = getTotalTime(data.get(4), data.get(5), data.get(6),data.get(7));
		 int totalStep = data.get(12)<<24 | data.get(11)<< 16 | data.get(10) <<8 | data.get(9);
		 int totalDistance = data.get(16) <<24 | data.get(15) <<16 | data.get(14) << 8 | data.get(13);
		 Log.d("test","data 12="+data.get(12)+" 9="+data.get(9)+"data.get(10)<<8="+(data.get(10) <<8)+ "totalDistance="+totalDistance+" totalStep="+totalStep);
		 ArrayList<Integer> arrayList = new ArrayList<Integer>();
		 arrayList.add(excersieTime);
		 arrayList.add(restTime);
		 arrayList.add(totalStep);
		 arrayList.add(totalDistance);
		 return arrayList;
	}
	//0 0 0 0 0 0 23 0 0 1 6d 6d 0 0 0 0 14 16 2 fb
		//Calories b0-b3;Avg Speed b4-b5 km/10;Max Speed b6-b7 km/10;B08:Data Packet Number,b10 Max HRM ;b11 avg hrm;b15 hr data packet
		
	public  ArrayList<Integer> analysisSportPacket2(ArrayList<Integer> data){
		int totalCalories = data.get(3)<<24 | data.get(2) <<16 | data.get(1) <<8 | data.get(0);
		int dataPacketNum =  data.get(8);
		int maxHRM =  data.get(10);
		int avgHRM =  data.get(11);
		int hrPacketNum =  data.get(15);
		
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		arrayList.add(totalCalories);
		arrayList.add(dataPacketNum);
		arrayList.add(maxHRM);
		arrayList.add(avgHRM);
		arrayList.add(hrPacketNum);
		return arrayList;
	}
	
	public  ArrayList<Integer> analysisSprotHRMPacket(ArrayList<Integer> data){
		ArrayList<Integer> hrmArrayList  = new ArrayList<Integer>();
		for(int i=0;i<16;i++){
			hrmArrayList.add( data.get(i));
		}
		return hrmArrayList;
	}
	public  long[] getSleepHeadStartAndEndTime(ArrayList<Integer> data){
		long[] time = new long[3];
		int sMin =  data.get(0);
		int sHour =  data.get(1);
		int sDay =  data.get(2);
		int sMonth =  data.get(3);
		int sYear = countYear(data.get(4));
		
		int eMin = data.get(5);
		int eHour = data.get(6);
		int eDay = data.get(7);
		int eMonth = data.get(8);
		int eYear = countYear(data.get(9));
		time[0] = getSettedTime(sYear,sMonth,sDay,sHour,sMin);
		time[1] = getSettedTime(eYear, eMonth, eDay, eHour, eMin);
		time[2] = data.get(12)<<8 | data.get(11);
		return time;
		
	}
	public  ArrayList<Integer> analysisSleepPacket1(ArrayList<Integer> data){
		int totalSleepTime = getTotalTime(data.get(0),data.get(1));
		int totalDeepTime = getTotalTime(data.get(2), data.get(3));
		int totalLightTime = getTotalTime(data.get(4),data.get(5));
		int totalWakeUpTime = getTotalTime(data.get(6), data.get(7));
		int sleepTargetTime = getTotalTime(data.get(8),data.get(9));
		
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		arrayList.add(totalSleepTime);
		arrayList.add(totalDeepTime);
		arrayList.add(totalLightTime);
		arrayList.add(totalWakeUpTime);
		arrayList.add(sleepTargetTime);
		return arrayList;
		
	}
	//(00=Deep Sleep 01=Light Sleep 02=Wake up)
	public  ArrayList<Integer> analysisSleepPacket2(ArrayList<Integer> data){
		ArrayList<Integer> sleepType = new ArrayList<Integer>();
		for(int i=0;i<16;i++){
			if(data.get(i)!=0x1A){
				sleepType.add(data.get(i));
			}
		}
		
		return sleepType;
	}
	
	public  void analysisSettingPacket0(ArrayList<Integer> data){
		settingPakcet1 = data;
		
		
	}
	public  void analysisSettingPacket1(ArrayList<Integer> data){
		settingPakcet2 = data;
		
		
	}
	
	
	
	private  long getSettedTime(int year,int month,int day,int hour,int min,int sec){
		Calendar sCanlendar = Calendar.getInstance();
		sCanlendar.set(year, month-1, day, hour, min, sec);
		int year1 = sCanlendar.get(Calendar.YEAR);
		int m1 = sCanlendar.get(Calendar.MONTH);
		int d1 = sCanlendar.get(Calendar.DAY_OF_MONTH);
		int h1 = sCanlendar.get(Calendar.HOUR_OF_DAY);
		int min1 = sCanlendar.get(Calendar.MINUTE);
		int s1 = sCanlendar.get(Calendar.SECOND);
		sCanlendar.set(Calendar.MILLISECOND, 0);
		return sCanlendar.getTimeInMillis();
	}
	
	private  long getSettedTime(int year,int month,int day,int hour,int min){
		return getSettedTime(year,month,day,hour,min,0);
	}
	
	private  int getTotalTime(int hexHour,int hexMin){
		return getTotalTime(hexHour,hexMin,0,0);
	}
	private  int getTotalTime(int hour,int min,int sec,int pSec){
		return hour*60*60 + min*60 + sec + pSec/1000 ;
	}
	
	private  int hexToint(int hexValue){
		
		return Integer.parseInt(String.valueOf(hexValue), 16);
	}
	//0 16 1 1 e 1e 7 2 1 e 1 24 0 1 3a 2 0 0 0 fc
	//B10 Sleep Sequence No;B11  2  Total Number of Sleep Data (N) B13  1  Sleep Packet for start_time
	//36 8 39 7 39 0 24 0 0 8 3 0 0 0 0 0 0 0 1 fc
	//Total Sleep Time:   B0=Min, B1=Hour
	//Deep Sleep Time  B2=Min, B3=Hour
	//Light Sleep Time  B4=Min, B5=Hour
	//Wake up Time  B6=Min, B7=Hour
	//Sleep Target  B8=Min, B9=Hour
	//2 2 2 2 1 1 1 1 1 1 1 0 0 0 0 0 1a 0 2 fc
	//Sleep Type  (00=Deep Sleep 01=Light Sleep 02=Wake up) b0-b15; b16为1a时，是数据不够20bit
	/**
	 * @功能: BCD码转为10进制串(阿拉伯数据)
	 * @参数: BCD码
	 * @结果: 10进制串
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
	}

	/**
	 * @功能: 10进制串转为BCD码
	 * @参数: 10进制串
	 * @结果: BCD码
	 */
	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}
		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}
		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}
			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}
	
	//44 41 49 4C 59 YY MM DD FF FF FF FF FF FF FF FF FF FF FF FA
	public boolean hasNoDailyData(ArrayList<Integer> data){
		if(data.size() == 20&&data.get(19)==0xFA 
				&& data.get(0) == 0x44
				&& data.get(1) == 0x41
				&& data.get(2) == 0x49
				&& data.get(3) == 0x4C
				&& data.get(4) == 0x59){
			for(int i=8;i<19;i++){
				if(data.get(i) != 0xFF){
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
