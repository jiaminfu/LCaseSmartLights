package cn.com.lcase.app.pcmplayer;

import android.media.AudioFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MyThread implements Runnable {
	final int EVENT_PLAY_OVER = 0x100;
	
	byte []data;
	Handler mHandler;
	
	public MyThread(byte []data, Handler handler) {
		this.data = data;
		mHandler = handler;
	}
	public void run() {
		Log.i("MyThread", "run..");
		
		if (data == null || data.length == 0){
			return ;
		}
		@SuppressWarnings("deprecation")
        MyAudioTrack myAudioTrack = new MyAudioTrack(16000,
									AudioFormat.CHANNEL_CONFIGURATION_MONO,
									AudioFormat.ENCODING_PCM_16BIT);
		
		myAudioTrack.init();
		
		int playSize = myAudioTrack.getPrimePlaySize();
		
		Log.i("MyThread", "total data size = " + data.length + ", playSize = " + playSize);
		
		int index = 0;
		int offset = 0;
		while(true){
            try {
	                Thread.sleep(0);
	                offset = index * playSize;
	                if (offset >= data.length){
	                    break;
                }
                if (index==data.length/playSize) {
                	myAudioTrack.playAudioTrack(data, offset, data.length-offset-1);
				}else {
					myAudioTrack.playAudioTrack(data, offset, playSize);
				}
                Log.i("MyThread", "index = " + index);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            index++;
    }
		myAudioTrack.release();
		Message msg = mHandler.obtainMessage(EVENT_PLAY_OVER);
		msg.sendToTarget();
	}
}