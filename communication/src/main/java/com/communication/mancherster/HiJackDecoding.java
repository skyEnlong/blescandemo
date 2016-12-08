package  com.communication.mancherster;


public class HiJackDecoding {
    private INumberCallback mCallback;

    private final int STARTBIT = 0, STARTBIT_FALL = 1, DECODE = 2;

    private final int SAMPLESPERBIT = 32;

    private final int SHORT = (SAMPLESPERBIT / 2 + SAMPLESPERBIT / 4);// 
    private final int LONG = (SAMPLESPERBIT + SAMPLESPERBIT / 2); //

    private int THRESHOLD = 500;

    int phase = 0;
    int phase2 = 0;
    int lastPhase2 = 0;
    int sample = 0;
    int lastSample = 0;
    int decState = STARTBIT;
    int byteCounter = 1;
    int parityTx = 0;

    int bitNum = 0;
    int uartByte = 0;

    int parityRx = 0;

    private int startBit = 0;

    private int fallBit =1;

    /**
     * 
     * @param callback
     */
    public HiJackDecoding(INumberCallback callback) {
        this.mCallback = callback;
    }
    
    /**
     * 
     * @param threshold
     */
    public void setThreshold(int threshold) {
        THRESHOLD = threshold;
    }

    /**
     * 
     * @param flg
     */
    public void resetRaiseBit(boolean flg) {
        if (flg) {
            startBit = 0;
            fallBit = 1;
        } else {
            startBit = 1;
            fallBit = 0;
        }
    }



    /**
     * 
     * @param buffer
     * @param bufferLength
     */
    public void decoding(short[] buffer, int bufferLength) {
        int inNumberFrames = bufferLength;
        short[] lchannel = buffer;

        for (int j = 0; j < inNumberFrames; j++) {
            float val = lchannel[j];

            phase2 += 1;
            if (val < THRESHOLD) {
                sample = startBit;
            } else {
                sample = fallBit;
            }

            if (sample != lastSample) {
                // transition
                int diff = phase2 - lastPhase2;
                switch (decState) {
                case STARTBIT:
                    if (lastSample == 0 && sample == 1) {
                        // low->high transition. Now wait for a long period
                        decState = STARTBIT_FALL;
                    }
                    break;
                case STARTBIT_FALL:
                    if ((SHORT < diff) && (diff < LONG)) {
                        // looks like we got a 1->0 transition.
                        bitNum = 0;
                        parityRx = 0;
                        uartByte = 0;
                        decState = DECODE;
                    } else {
                        decState = STARTBIT;
                    }
                    break;

                case DECODE:
                    if ((SHORT < diff) && (diff < LONG)) {
                        // we got a valid sample.
                        if (bitNum < 8) {
                            uartByte = ((uartByte >> 1) + (sample << 7));
                            bitNum += 1;
                            parityRx += sample;
                            // #ifdef DECDEBUG
                            // printf("Bit %d value %ld diff %ld parity %d\n",
                            // bitNum, sample, diff, parityRx & 0x01);
                            // #endif
                        } else if (bitNum == 8) {
                            // parity bit
                            if (sample != (parityRx & 0x01)) {
                                // #ifdef DECDEBUGBYTE
                                // printf(" -- parity %ld,  UartByte 0x%x\n",
                                // sample, uartByte);
                                // #endif
                                decState = STARTBIT;
                            } else {
                                // #ifdef DECDEBUG
                                // printf(" ++ good parity %ld, UartByte 0x%x\n",
                                // sample, uartByte);
                                // #endif

                                bitNum += 1;
                            }

                        } else {
                            // we should now have the stopbit
                            if (sample == 1) {

                                sendDataToUI(uartByte);

                            } else {
                                // not a valid byte.
                                // #ifdef DECDEBUGBYTE
                                // printf(" -- StopBit: %ld UartByte %d\n",
                                // sample, uartByte);
                                // #endif
                            }
                            decState = STARTBIT;
                        }
                    } else if (diff > LONG) {
                        // #ifdef DECDEBUG
                        // printf("diff too long %ld\n", diff);
                        // #endif
                        decState = STARTBIT;
                    } else {
                        // don't update the phase as we have to look for the
                        // next transition
                        lastSample = sample;
                        continue;
                    }
                    break;
                default:
                    break;
                }
                lastPhase2 = phase2;
            }
            lastSample = sample;
        }
    }

    /**
     * 
     * @param value
     */
    private void sendDataToUI(int value) {
    	mCallback.getNumber(value);
    }
}
