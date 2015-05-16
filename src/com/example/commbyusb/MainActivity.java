package com.example.commbyusb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class MainActivity extends Activity implements OnClickListener {

	private UsbManager manager;
	private List<UsbSerialDriver> availableDrivers;
	private TextView dataView,countView;
	private EditText sendText;
	private UsbSerialPort port;
	private UsbSerialDriver driver;
	private UsbDeviceConnection connection = null;
	private byte[] receiveByte = {};
	private String reString = " ";
	private byte[] imageByte = new byte[9154]; 
	int count = 0;
	private final ByteBuffer mReadBuffer = ByteBuffer.allocate(4096);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dataView = (TextView) findViewById(R.id.textView1);
		sendText = (EditText) findViewById(R.id.sendText);
		countView = (TextView) findViewById(R.id.countView);
		
		findViewById(R.id.send).setOnClickListener(this);
		findViewById(R.id.connect).setOnClickListener(this);
		findViewById(R.id.disconnect).setOnClickListener(this);

		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(
				manager);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send:
			if (connection == null) {
				ToastUtil.shortToast(MainActivity.this, "no connection");
				return;
			} else {
				sendMessage();
			}
			break;
		case R.id.connect:
			if (connection == null) {
				openUsbConnection();
				ToastUtil.shortToast(MainActivity.this, port.getSerial()
						+ "connection builded");
			} else {
				ToastUtil.shortToast(MainActivity.this,
						"it's already connected");
			}
			break;
		case R.id.disconnect:
//			if (connection != null) {
//				try {
//					mThread.interrupt();
//					connection.close();
//					connection = null;
//					port.close();
//					port = null;
//					Toast.makeText(MainActivity.this, "connection canceled",
//							Toast.LENGTH_SHORT).show();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
			break;
		default:
			break;
		}
	}

	private void openUsbConnection() {
		if (availableDrivers.isEmpty()) {
			return;
		}
		driver = availableDrivers.get(0);
		connection = manager.openDevice(driver.getDevice());
		if (connection == null) {
			// UsbManager.requestPermission(driver.getDevice(), ..)
			manager.requestPermission(driver.getDevice(), null);
			return;
		}
		port = driver.getPorts().get(0);
		// List<UsbSerialPort> ports = driver.getPorts();
		// UsbSerialPort port = ports.get(0);
		try {
			port.open(connection);
			port.setParameters(115200, UsbSerialPort.DATABITS_8,
					UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mThread.start();
	}

	private void sendMessage() {
		String sendString = sendText.getText().toString();
		byte[] send_buffer = sendString.getBytes();
		try {
			port.write(send_buffer, 1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Thread mThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					int len = port.read(mReadBuffer.array(), 200);
					if (len > 0) {
						count ++;
						receiveByte = new byte[len];
						mReadBuffer.get(receiveByte, 0, len);
						for (int i = 0; i < len; i++) {
							imageByte[i + count * len] = receiveByte[i];
						}						
						reString = new String(receiveByte);
						mHandler.sendMessage(mHandler.obtainMessage());
					}
					mReadBuffer.clear();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	});
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			dataView.setText(" ");
			dataView.append(reString);
			countView.setText(" ");
			countView.append(String.valueOf(count));
			dataView.invalidate();
			countView.invalidate();
		}
	};
}
