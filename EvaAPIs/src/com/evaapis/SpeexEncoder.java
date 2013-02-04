package com.evaapis;

public class SpeexEncoder {

	public static final String VERSION = "speex-1.2rc1";
	static {
	    System.loadLibrary("speex");
	}

	native public void init(int speexMode, int speexQuality, int sampleRate,int channels) ;

	native public int getFrameSize() ;
	native public void processData(byte[] temp, int i, int pcmPacketSize); 
	native public int getProcessedData(byte[] temp, int i) ;

}
