package  com.communication.mancherster;

import java.util.ArrayList;

public class ManchesterEncoding implements CstCode{ 
    //
    private  final int bitLength = 8;
   
    public    ArrayList<short[]> byteStartStopList ;
   
    public static short[]  bytesHigh = new short[] { 
        	NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
        	NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH,
        	NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
        	NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH 
        	};
        
	public static short[]  bytesLow = new short[] { 
				NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW,
				NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
				NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
				NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW 
		};
 

	/**
	 * 
	 * @param isReset d
	 */
    public static void resetHighLowBit(boolean isReset) {
    	
        if (!isReset) {
            bytesLow = new short[] { 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH };
            bytesHigh = new short[] { 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW,
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW 
            		};
        } else {
            bytesHigh = new short[] { 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, 
            		NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH, NUM_HIGH,-NUM_HIGH 
            		};
            bytesLow = new short[] { 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, 
            		NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW, NUM_LOW,-NUM_LOW
            		};
        }
    }

 
    
    /**
     * 
     * @return
     */
    public ArrayList<short[]> getStart() {
        byteStartStopList = new ArrayList<short[]>();
        for (int i = 0; i < 120; i++) {
            byteStartStopList.add(bytesHigh);
            byteStartStopList.add(bytesLow);
        }
        return byteStartStopList;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<short[]> getStop() {
        byteStartStopList = new ArrayList<short[]>();
        for (int i = 0; i < 5; i++) {
            byteStartStopList.add(bytesLow);
            byteStartStopList.add(bytesHigh);
        }
        return byteStartStopList;
    }

    /**
     * 
     * @param character
     * @return
     */
    public ArrayList<short[]> getManchesterCode(int c) {
        String transData = charToManBinary(c);
        int length=transData.length();
        ArrayList<short[]> list=new ArrayList<short[]>();
        for(int i=0;i<length;i++){
            if (transData.charAt(i) == '0') {
                list.add(bytesHigh);
            } else {
                list.add(bytesLow);
            }
        }
        return list;
    }

    /**
     * 
     * @param c
     * @return
     */
    private String charToManBinary(int c) {
        
        String strBinary = Integer.toBinaryString(c);
        strBinary = "0000000".substring(0, bitLength - strBinary.length())+strBinary;

        //
        //String resultBinary = START_STOP + START_BIT;
        String resultBinary =START_BIT;

        int length = bitLength - 1;
        int evenIndex = 0;
        char bc = '0';
        //
        for (int i = length; i >=0; i--) {
            bc = strBinary.charAt(i);

            if (bc == '1') {
                ++evenIndex;
                //  
                resultBinary += ONE;
            } else {
                resultBinary += ZERO;
            }
        }
        //  
        if (evenIndex % 2 == 0) {
            //  
            resultBinary += VERIFY_BIT_EVEN;
        } else {
            //  
            resultBinary += VERIFY_BIT_UNEVEN;
        }

        //  
       // resultBinary += STOP_BIT + START_STOP;
        resultBinary += STOP_BIT ;
        return resultBinary;
    } 
}
