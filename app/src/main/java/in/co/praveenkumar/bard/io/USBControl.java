package in.co.praveenkumar.bard.io;

import in.co.praveenkumar.bard.activities.MainActivity;
import in.co.praveenkumar.bard.graphics.Frame;
import in.co.praveenkumar.bard.utils.DebugDump;
import in.co.praveenkumar.bard.utils.Globals;
import in.co.praveenkumar.bard.utils.RLE;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

/**
 * Configures a USB accessory and its input/output streams.
 * 
 * Call this.send to sent a byte array to the accessory Override onReceive to
 * process incoming bytes from accessory
 */

public abstract class USBControl extends Thread {

	// The permission action
	private static final String ACTION_USB_PERMISSION = "in.co.praveenkumar.bard.activities.MainActivity.USBPERMISSION";

	// An instance of accessory and manager
	private UsbAccessory mAccessory;
	private UsbManager mManager;
	private Context context;
	private Handler UIHandler;
	private Handler controlSender;
	private Thread controlListener;
	boolean connected = false;
	private ParcelFileDescriptor mFileDescriptor;
	//private FileInputStream input;
	private IUsbInputStream input;
	public static FileOutputStream mOutputStream = null;;

	// Receiver for connect/disconnect events
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent
							.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {

					}

				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory) intent
						.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}

		}
	};

	// Configures the usb connection
	public USBControl(Context main, Handler ui) {
		super("USBControlSender");
		UIHandler = ui;
		context = main;

		mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		UsbAccessory[] accessoryList = mManager.getAccessoryList();
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context,
				0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		context.registerReceiver(mUsbReceiver, filter);

		UsbAccessory mAccessory = (accessoryList == null ? null
				: accessoryList[0]);

		if (Globals.DEBUG){
			openAccessory(mAccessory);

		}else {
			if (mAccessory != null) {

				while (!mManager.hasPermission(mAccessory)) {
					mManager.requestPermission(mAccessory, mPermissionIntent);
				}
				openAccessory(mAccessory);

			}
		}

	}

	// Send byte array over connection
	public void send(byte[] command) {
		if (controlSender != null) {
			Message msg = controlSender.obtainMessage();
			msg.obj = command;
			controlSender.sendMessage(msg);
		}
	}

	public abstract void onReceive(byte[] msg);

	public abstract void onNotify(String msg);

	public abstract void onConnected();

	public abstract void onDisconnected();

	@Override
	public void run() {
		// Listens for messages from usb accessory
		controlListener = new Thread(new Runnable() {
			boolean running = true;

			public void run() {
				DebugDump debugDump = new DebugDump();
				int i = 0;
				//debugDump.init();

				while (running) {
					try {
						if (Globals.RLE){

							byte[] packetSizeBuffer = new byte[4100];
//							byte[] totalByte = null;
							while (input != null && input.read(packetSizeBuffer, 0, 512) != -1
									&& running) {

								final int one = (int)packetSizeBuffer[4];
								final int two = ((int)packetSizeBuffer[5]);

								final int payloadSize = ((int)packetSizeBuffer[4] & 0xFF) +
														(((int)packetSizeBuffer[5] & 0xFF) << 8);

								boolean rleUsed = (int)packetSizeBuffer[3] != 0;

								final int remainingCount = payloadSize - 506;

								if (remainingCount > 0 && remainingCount < 512){
									input.read(packetSizeBuffer, 512, 512);
								}
								else if (remainingCount > 512){
									UIHandler.post(new Runnable() {
										public void run() {
											MainActivity.editText.setText(payloadSize + "   " + remainingCount + "\n" + one + " " + two);
										}
									});
									input.read(packetSizeBuffer, 512, remainingCount);
								}

								final int pageIndex = ((int)packetSizeBuffer[2] & 0xFF) +
										(((int)packetSizeBuffer[3] & 0xFF) << 8);

								UIHandler.post(new Runnable() {
									public void run() {
										MainActivity.editText.setText("total bytes");
									}
								});

								final int framePos = pageIndex * 4096;
								try {
									i++;

									final byte[] totalByte = RLE.decode(packetSizeBuffer, 6, payloadSize + 6);

//									if (Globals.DEBUG && i < 100) {
//										debugDump.init();
//										debugDump.dumpActualBuffer(packetSizeBuffer);
//										debugDump.dumpRleDecodedPayload(totalByte);
//										debugDump.close();
//									}

									UIHandler.post(new Runnable() {
										public void run() {
											MainActivity.editText.setText(totalByte.length + "");
										}
									});

									//int framePos = pageIndex * 4096;
									if ((framePos - (totalByte.length)) <= Frame.FRAME_LENGTH) {
										Frame.frameBuffer.position(framePos);
										Frame.frameBuffer.put(totalByte, 0, totalByte.length);
									}
								}
								catch (final Exception ex){
									UIHandler.post(new Runnable() {
										public void run() {
											MainActivity.editText.setText(pageIndex + ex.getMessage());
										}
									});
								}

/*
//								int payloadSize = (int)(packetSizeBuffer[0] & 0x000000ff) +
//										(int)(packetSizeBuffer[1] << 8 & 0x0000ff00) +
//										(int)(packetSizeBuffer[1] << 16 & 0x00ff0000) +
//										(int)(packetSizeBuffer[1] << 24 & 0xff000000);

								int payloadSize = (int)packetSizeBuffer[0] + ((int)packetSizeBuffer[1] << 8) +
												((int)packetSizeBuffer[2] << 16) + ((int)packetSizeBuffer[3] << 24);

								System.out.println("PacketSize = " + payloadSize);

								byte[] msg = new byte[Globals.DATA_SIZE];
								byte[] packet = new byte[payloadSize];
								input.read(packet);

								// receive(msg);
								System.out.println("Read USB data");
								int id = (int)(packet[0] & 0x000000ff);

								if (id == Globals.DATA_VIDEO){
									int pageIndex = (int) (packet[1] & 0x0000000ff)
											+ (int) (packet[2] << 8 & 0x0000ff00);

									System.out.println("Page index : " + pageIndex);

									msg = RLE.decode(packet, 4);

									// Update frame data
									int framePos = pageIndex * 4096;
									if ((framePos - (packet.length - 2)) <= Frame.FRAME_LENGTH) {
										Frame.frameBuffer.position(framePos);
										Frame.frameBuffer.put(msg, 0, msg.length);
									}
								}
								else if (id == Globals.DATA_AUDIO){
									byte[] buffer = new byte[Globals.DATA_SIZE];
									System.arraycopy(packet, Globals.DATA_HEADER_SIZE, buffer, 0, Globals.DATA_SIZE);
									MainActivity.audioData.put(buffer);
								}
								*/
							}

						}else{
							byte[] msg = new byte[Globals.DATA_PACKET_SIZE]; //new byte[4100];
							// Handle incoming messages
							while (input != null && input.read(msg) != -1
									&& running) {
//
//							int size = (int)(test[0] & 0x000000ff) + (int)(test[1] << 8 & 0x0000ff00) +
//									(int)(test[1] << 16 & 0x00ff0000) + (int)(test[1] << 24 & 0xff000000);
//
							/*int size = (int)test[0] + ((int)test[1] << 8) + ((int)test[2] << 16) + ((int)test[3] << 24);

							if (size != 4100) {
								input.read(msg);
								continue;
							}*/

								//input.read(msg);
								// receive(msg);
								System.out.println("Read USB data");
								int id = (int)(msg[0] & 0x000000ff);

								if (id == Globals.DATA_VIDEO){
									int pageIndex = (int) (msg[1] & 0x0000000ff)
											+ (int) (msg[2] << 8 & 0x0000ff00);

									System.out.println("Page index : " + pageIndex);

									// Update frame data
									int framePos = pageIndex * 4096;
									if ((framePos - (msg.length - 2)) <= Frame.FRAME_LENGTH) {
										Frame.frameBuffer.position(framePos);
										Frame.frameBuffer.put(msg, 4, msg.length - 4);
									}
								}
								else if (id == Globals.DATA_AUDIO){
									byte[] buffer = new byte[Globals.DATA_SIZE];
									System.arraycopy(msg, Globals.DATA_HEADER_SIZE, buffer, 0, Globals.DATA_SIZE);
									MainActivity.audioData.put(buffer);
								}
							}
						}

					} catch (final Exception e) {
						UIHandler.post(new Runnable() {
							public void run() {
								MainActivity.editText.setText(e.toString());

								onNotify("USB Receive Failed " + e.toString()
										+ "\n");
								closeAccessory();
							}
						});
						running = false;
					}
				}

				debugDump.close();
			}
		});
		controlListener.setDaemon(true);
		controlListener.setName("USBCommandListener");
		controlListener.start();
	}

	// Sets up filestreams
	private void openAccessory(UsbAccessory accessory) {
		mAccessory = accessory;

		if (Globals.DEBUG){
			input = new DummyInputStream();
		}
		else {
			mFileDescriptor = mManager.openAccessory(accessory);
			if (mFileDescriptor != null) {
				FileDescriptor fd = mFileDescriptor.getFileDescriptor();

				//input = new FileInputStream(fd);
				input = new UsbInputStream(fd);
				mOutputStream = new FileOutputStream(fd);
			}
		}
		this.start();
		onConnected();
	}

	// Cleans up accessory
	public void closeAccessory() {

		// halt i/o
		controlSender.getLooper().quit();
		controlListener.interrupt();

		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}

		onDisconnected();
	}

	// Removes the usb receiver
	public void destroyReceiver() {
		context.unregisterReceiver(mUsbReceiver);
	}

}
