package in.co.praveenkumar.bard.activities;


import java.util.concurrent.SynchronousQueue;

import in.co.praveenkumar.bard.R;
import in.co.praveenkumar.bard.graphics.Frame;
import in.co.praveenkumar.bard.graphics.FrameSettings;
import in.co.praveenkumar.bard.io.USBControl;
import in.co.praveenkumar.bard.utils.Globals;
import in.co.praveenkumar.bard.utils.InputControl;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends Activity {
    final String DEBUG_TAG = "BARD";
    final String IDENT_MANUFACTURER = "BeagleBone";
    final String USB_PERMISSION = "in.co.praveenkumar.bard.activities.MainActivity.USBPERMISSION";


    private static int AUDIO_BUFFER_SIZE = 4096 * 4;

    private final char KEYCODES[] = {
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,'\t',' ','-','=','[',
            ']','\\','\\',';','\'','`',',','.','/',0,0,0,0,0,0,0
    };


    public static SynchronousQueue<Point> mousePoints = new SynchronousQueue<Point>();
    public static SynchronousQueue<byte[]> audioData = new SynchronousQueue<byte[]>();

    private AudioTrack audioTrack;
    private Thread audioThread;
    private Thread mouseThread;
    private Point lastPosition;
    private boolean stopRequested = false;
    float downx, downy, upx, upy;


    private LinearLayout activityLayout;
    private PopupWindow metaKeyPopUp;
    ImageView remoteScreen;
    private Button leftButton;
    private Button rightButton;
    private Button keyboardButton;
    private LinearLayout linearLayout;
    public static EditText editText;
    Bitmap bitmap = Bitmap.createBitmap(1024, 768, Bitmap.Config.RGB_565);

    // Handler, Threads
    private Handler UIHandler = new Handler();

    private long oldTimeStamp = 0;
    private boolean isDragging = false;

    // Activity Lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        remoteScreen = (ImageView) findViewById(R.id.remote_screen);

        leftButton = (Button)this.findViewById(R.id.leftButton);
        rightButton = (Button)this.findViewById(R.id.rightButton);
        keyboardButton = (Button)this.findViewById(R.id.keyboardButton);
        linearLayout = (LinearLayout)this.findViewById(R.id.linearLayout);
        editText = (EditText)this.findViewById(R.id.editText);

        activityLayout = (LinearLayout)this.findViewById(R.id.linearLayout);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupLayout = layoutInflater.inflate(R.layout.menu_layout, null);
        
        metaKeyPopUp = new PopupWindow(this);
        metaKeyPopUp.setContentView(popupLayout);
        metaKeyPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        metaKeyPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        metaKeyPopUp.setFocusable(true);
        //metaKeyPopUp.setBackgroundDrawable(new BitmapDrawable());

        audioTrack = new  AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE, AudioTrack.MODE_STREAM);

        setupButton();

        setupUSB();

        // Start Frame updater thread
        frameUpdate();

        mouseThread = new Thread(){
            public void run(){
                stopRequested = false;
                lastPosition = new Point(0, 0);

                while (!stopRequested){
                    try {
                        byte data[] = new byte[8];
                        Point point = MainActivity.mousePoints.take();

                        data[InputControl.REL_X_INDEX] = (byte)point.x;
                        data[InputControl.REL_Y_INDEX] = (byte)point.y;

                        if (isDragging)
                            data[InputControl.MOUSE_BTN_INDEX] |= InputControl.BTN_LEFT;

                        sendMouseData(data);
                    }
                    catch (InterruptedException e) {
                        System.out.println("Mouse Point Fetching Interrupted");
                    }
                }
            }
        };

        audioThread = new Thread(){
            public void run(){
                stopRequested = false;
                int offset = 0;
                int count = 0;

                audioTrack.play();
                while (!stopRequested){
                    try {
                        byte[] data = MainActivity.audioData.take();
                        if (data != null){
                            System.out.println("Playing: " + count);
                            audioTrack.write(data, offset, data.length);
                            offset += data.length;
                            offset %= AUDIO_BUFFER_SIZE;
                            count++;
                        }
                    }
                    catch (InterruptedException e) {
                        System.out.println("Mouse Point Fetching Interrupted");
                    }
                }
                audioTrack.stop();
                audioTrack.release();
            }
        };

        remoteScreen.setOnTouchListener(new OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    //mouseDoubleClick();

                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    int action = e.getAction();

                    if (action == MotionEvent.ACTION_UP) {
                        long newTimeStamp = e.getEventTime();

                        long diff = newTimeStamp - oldTimeStamp;

                        if (diff < 100) {
                            isDragging = false;
                            mouseDoubleClick();
                        }

                    } else if (action == MotionEvent.ACTION_DOWN) {
                        isDragging = true;
                        oldTimeStamp = e.getEventTime();
                    }

                    return super.onDoubleTapEvent(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    mouseSingleClick(InputControl.BTN_LEFT);

                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    if (!isDragging) {
                        mouseSingleClick(InputControl.BTN_RIGHT);
                    }
                    super.onLongPress(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);

                int eid = event.getAction();
                switch (eid) {
                    case MotionEvent.ACTION_DOWN:
                        downx = event.getX();
                        downy = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        upx = event.getX();
                        upy = event.getY();

                        //Toast.makeText(getApplicationContext(), "x=" + upx + "y=" + upy, Toast.LENGTH_SHORT).show();

                        int x = (int) Math.ceil((double) (upx - downx));
                        int y = (int) Math.ceil((double) (upy - downy));

                        if (Math.abs(x) > 0) downx = upx;
                        if (Math.abs(y) > 0) downy = upy;

                        if ((Math.abs(x) > 0 || Math.abs(y) > 0) && mousePoints.size() < 1000) ;
                    {
                        try {
                            mousePoints.add(new Point(x, y));
                        } catch (Exception ex) {

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
        audioThread.start();
    }

    private void setupButton() {
        leftButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mouseSingleClick(InputControl.BTN_LEFT);

            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mouseSingleClick(InputControl.BTN_RIGHT);
            }
        });

        keyboardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                //sendKeyboardData(testKeyCodeIndex++);

            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // ToDo: Redisgn to make efficient

        Toast.makeText(getApplicationContext(), "" + (char)event.getUnicodeChar(), Toast.LENGTH_SHORT).show();

        if (keyCode == KeyEvent.KEYCODE_ENTER){
            Resources res = getResources();
            sendKeyboardData(res.getInteger(R.integer.ENTER));
        }
        if (event.getUnicodeChar() >= 'A' && event.getUnicodeChar() <= 'Z'){
            sendKeyboardData(event.getUnicodeChar() - 'A' + 4);
        }
        else if(event.getUnicodeChar() >= 'a' && event.getUnicodeChar() <= 'z'){
            sendKeyboardData(event.getUnicodeChar() - 'a' + 4);
        }
        else if(event.getUnicodeChar() >= '1' && event.getUnicodeChar() <= '9'){
            sendKeyboardData(event.getUnicodeChar() - '0' + 30);
        }
        else if(event.getUnicodeChar() == '0'){
            sendKeyboardData(event.getUnicodeChar() - '0' + 39);
        }
        else{
            for (int i=0;i<KEYCODES.length; i++){
                if (KEYCODES[i] == event.getUnicodeChar()){
                    sendKeyboardData(i);
                    break;
                }
            }
        }
        //sendKeyboardData();

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return super.onKeyDown(keyCode, event);
    }

    public void onCustomClick(View view) {
        int x = activityLayout.getWidth() - metaKeyPopUp.getWidth();
        int y = activityLayout.getHeight() - metaKeyPopUp.getHeight();
        metaKeyPopUp.showAtLocation(activityLayout, Gravity.NO_GRAVITY, x, y);
    }

    public void onMetaKeyClick(View view){
        BeagleButton beagleButton = (BeagleButton)view;

        if (beagleButton != null) {
            int keyCodeIndex = beagleButton.getKeyCodeIndex();
            sendKeyboardData(keyCodeIndex);
        }
    }

    private void sendKeyboardData(int keyIndex){
        byte buffer[] = {0,0,0,0,0,0,0,0};

        buffer[InputControl.KEY_INDEX] = (byte)keyIndex;

        Toast.makeText(getApplicationContext(), "Receiver", Toast.LENGTH_SHORT).show();

        try{
            try {
                USBControl.mOutputStream.write(buffer);

            } catch (Exception e1) {
                Toast.makeText(getApplicationContext(), "Error:", Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
        }
        catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }
    }

    public void onCheckboxClicked(View view) {
        final byte buffer[] = {0,0,0,0,0,0,0,0};
        buffer[0] = Globals.CONTROL_HEADER;
        buffer[1] = Globals.DROP_FRAME;
        boolean checked = ((CheckBox) view).isChecked();

        if (checked){
            buffer[2] = 0x04;
        }else{
            buffer[2] = 0x01;
        }

        new Thread(){
            public void run(){
                sendMouseData(buffer);
            }
        }.start();
    }

    private void sendMouseData(byte data[]){
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

    private void mouseDoubleClick(){
        byte[] data = {0, 0, 0, 0, 0, 0, 0, 0};

        data[InputControl.MOUSE_BTN_INDEX] |= InputControl.BTN_LEFT;
        sendMouseData(data);

        data[InputControl.MOUSE_BTN_INDEX] = 0;
        sendMouseData(data);

        data[InputControl.MOUSE_BTN_INDEX] |= InputControl.BTN_LEFT;
        sendMouseData(data);

        data[InputControl.MOUSE_BTN_INDEX] = 0;
        sendMouseData(data);
    }

    private void mouseSingleClick(int button){
        byte[] data = {0, 0, 0, 0, 0, 0, 0, 0};

        data[InputControl.MOUSE_BTN_INDEX] |= button;
        sendMouseData(data);

        data[InputControl.MOUSE_BTN_INDEX] = 0;
        sendMouseData(data);

        isDragging = false;
    }

    public void updateImage() {
        //System.out.println("setUpImage called");

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