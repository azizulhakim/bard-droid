package in.co.praveenkumar.bard.activities;

import java.util.concurrent.SynchronousQueue;

import in.co.praveenkumar.bard.R;
import in.co.praveenkumar.bard.graphics.Frame;
import in.co.praveenkumar.bard.graphics.FrameSettings;
import in.co.praveenkumar.bard.io.USBControl;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MainActivity extends Activity {
	final String DEBUG_TAG = "BARD";
	final String IDENT_MANUFACTURER = "BeagleBone";
	final String USB_PERMISSION = "in.co.praveenkumar.bard.activities.MainActivity.USBPERMISSION";
	
	public static SynchronousQueue<Point> mousePoints = new SynchronousQueue<Point>();
	private Thread mouseThread;
	private Point lastPosition;
	private boolean stopRequested = false;
	float downx, downy, upx, upy;
	

	
	ImageView remoteScreen;
	Bitmap bitmap = Bitmap.createBitmap(1024, 768, Bitmap.Config.RGB_565);

	// Handler, Threads
	private Handler UIHandler = new Handler();

	// Activity Lifecycle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		remoteScreen = (ImageView) findViewById(R.id.remote_screen);

		setupUSB();

		// Start Frame updater thread
		frameUpdate();
		
		mouseThread = new Thread(){
        	public void run(){
        		stopRequested = false;
        		lastPosition = new Point(0, 0);
        		byte data[] = new byte[8];
        		
        		while (!stopRequested){
        			try {
        				int i = 0;
        				data[i++] = (byte)getResources().getInteger(R.integer.MOUSECONTROL);	// this is mouse data
        				data[i++] = (byte)getResources().getInteger(R.integer.MOUSEMOVE);
        				
        				Point point = MainActivity.mousePoints.take();
        				
        				data[i+1] = (byte) (point.x);// - lastPosition.x);
        				data[i+3] = (byte) (point.y);// - lastPosition.y);
        				data[i+0] = point.x < 0.0 ? (byte)1 : (byte)0; 
        				data[i+2] = point.y < 0.0 ? (byte)1 : (byte)0;
        				data[i+1] = (byte) Math.abs(point.x);
        				data[i+3] = (byte) Math.abs(point.y);
        				lastPosition = point;
        				
        				i += 2;
        				sendMouseData(data);
        			} 
        			catch (InterruptedException e) {
        				System.out.println("Mouse Point Fetching Interrupted");
        			}
        		}
        	}
        };
        
        remoteScreen.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int eid = event.getAction();
				switch (eid){
					case MotionEvent.ACTION_DOWN:
						downx = event.getX();
						downy = event.getY();
						break;
						
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_UP:
						upx = event.getX();
						upy = event.getY();
						
						//Toast.makeText(getApplicationContext(), "x=" + upx + "y=" + upy, Toast.LENGTH_SHORT).show();
						
						int x = (int)Math.ceil((double)(upx - downx));
						int y = (int)Math.ceil((double)(upy - downy));
						
						if (Math.abs(x) > 0) downx = upx;
						if (Math.abs(y) > 0) downy = upy;
						
						if ((Math.abs(x) > 0 || Math.abs(y) > 0) && mousePoints.size() < 1000);
						{
							try{
								mousePoints.add(new Point(x, y));
							}
							catch(Exception ex){
								
							}
						}
							
						break;
					
					default:
						break;
				}	
					
				return true;
			}
		});
        
        mouseThread.start();

	}
	
	private void sendMouseData(byte data[]){
    	byte buffer[] = {0,0,0,0,0,0,0,0};

		if (data.length < buffer.length){
			//System.arraycopy(data, 0, buffer, 0, data.length);
		}
		
		try{
        	try {
        		USBControl.mOutputStream.write(data);
        		
			} catch (Exception e1) {
				e1.printStackTrace();
			}
    	}
    	catch (Exception ex){
    	}
    }

	public void updateImage() {
		System.out.println("setUpImage called");

		/*
		 * -TODO- - Some strange thing here. Sometimes copyPixelsFromBuffer is
		 * reading outside the buffer range - A possible race condition with
		 * position being of Frame being set from reader thread.
		 */
		try {
			Frame.frameBuffer.position(0);
			bitmap.copyPixelsFromBuffer(Frame.frameBuffer);
			remoteScreen.setImageBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception with copyPixelsFromBuffer");
		}

	}

	private void setupUSB() {

		System.out.println("Starting USB...");
		new USBControlServer(UIHandler);
		System.out.println("Done\n");
	}

	public class USBControlServer extends USBControl {

		public USBControlServer(Handler ui) {
			super(getApplicationContext(), ui);
		}

		@Override
		public void onReceive(byte[] msg) {

		}

		@Override
		public void onNotify(String msg) {
			// console(msg);
		}

		@Override
		public void onConnected() {
			// usb.enable();
		}

		@Override
		public void onDisconnected() {
			// usb.pause();
			finish();
		}

	}

	private void frameUpdate() {
		updateImage();

		// Wait before doing next frame update
		Handler myHandler = new Handler();
		myHandler.postDelayed(frameUpdater, 1000 / FrameSettings.FPS);
	}

	private Runnable frameUpdater = new Runnable() {
		@Override
		public void run() {
			frameUpdate();
		}
	};

}