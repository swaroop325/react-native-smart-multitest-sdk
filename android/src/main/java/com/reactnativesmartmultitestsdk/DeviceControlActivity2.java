package com.reactnativesmartmultitestsdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */

public class DeviceControlActivity2 extends Activity {
	 private final static String TAG = DeviceControlActivity.class.getSimpleName();

	    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	    private String mDeviceName;

	    private BluetoothLeService mBluetoothLeService;
	    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
	            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	    private boolean mConnected = false;
	    private BluetoothGattCharacteristic mNotifyCharacteristic;

	    private final String LIST_NAME = "NAME";
	    private final String LIST_UUID = "UUID";
	    private byte[] revdataall=new byte[100];
	    private int revstarti=0;

	    private final byte replyBleData[] = new byte[] { (byte) 0x68, (byte) 0x02, (byte) 0x51, (byte) 0x53,(byte) 0x00 };
	    private final byte replyBleDataHist[] = new byte[] { (byte) 0x68, (byte) 0x02, (byte) 0x52, (byte) 0x54,(byte) 0x00 };

	    // Code to manage Service lifecycle.
	    private final ServiceConnection mServiceConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName componentName, IBinder service) {
	            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
	            if (!mBluetoothLeService.initialize()) {
	                Log.e(TAG, "Unable to initialize Bluetooth");
	                finish();
	            }
	            // Automatically connects to the device upon successful start-up initialization.
//	            boolean connState= mBluetoothLeService.connect(mDeviceAddress);
	        }

	        @Override
	        public void onServiceDisconnected(ComponentName componentName) {
	            mBluetoothLeService = null;
	        }
	    };

	    // Handles various events fired by the Service.
	    // ACTION_GATT_CONNECTED: connected to a GATT server.
	    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	    //                        or notification operations.
	    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
	        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
          @Override
	        public void onReceive(Context context, Intent intent) {
	            final String action = intent.getAction();

	            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
	                mConnected = true;
	                invalidateOptionsMenu();
	                setWaits();
	            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
	                mConnected = false;
	                invalidateOptionsMenu();
	                clearUI();

	            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
	                // Show all the supported services and characteristics on the user interface.
	                displayGattServices(mBluetoothLeService.getSupportedGattServices());
	            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
	            	byte data[]=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);

	                if (data==null) return;
	                int tmpLen=data.length;
	                if (tmpLen==0) return;

					int tmpRes=checkReceiveData(data);

					if (tmpRes==2)
					{
						if (mBluetoothLeService==null || mConnected==false) return;
	                	new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								mBluetoothLeService.writeCharacteristic(replyBleData);
							}
						}, 10);

					}
					else if (tmpRes==5)
					{
						if (mBluetoothLeService==null || mConnected==false) return;
	                	new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub

								mBluetoothLeService.writeCharacteristic(replyBleDataHist);

							}
						}, 10);

					}
	            }

	        }

	    };

	    private void clearUI() {
	        //
	    }

	    private void setWaits() {

	    }

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        final Intent intent = getIntent();
	        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);

	        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
	        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

	    }

	    @Override
	    protected void onResume() {
	        super.onResume();
	        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	        if (mBluetoothLeService != null) {
	            Log.d(TAG, "Connect request result=" + "result");
	        }

	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        unregisterReceiver(mGattUpdateReceiver);

	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        unbindService(mServiceConnection);
	        mBluetoothLeService = null;

	    }

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {

	        return true;
	    }


		// Demonstrates how to iterate through the supported GATT Services/Characteristics.
	    // In this sample, we populate the data structure that is bound to the ExpandableListView
	    // on the UI.
	    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
      private void displayGattServices(List<BluetoothGattService> gattServices) {
	        if (gattServices == null) return;
	        String uuid = null;
	        String unknownServiceString = getResources().getString(Integer.parseInt(String.valueOf(0x7f05000b)));
	        String unknownCharaString = getResources().getString(Integer.parseInt(String.valueOf(0x7f05000b)));
	        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
	        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
	                = new ArrayList<ArrayList<HashMap<String, String>>>();
	        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	        // Loops through available GATT Services.
	        for (BluetoothGattService gattService : gattServices) {
	            HashMap<String, String> currentServiceData = new HashMap<String, String>();
	            uuid = gattService.getUuid().toString();

	            if (uuid.contains("ffe0"))
	            {
	            	currentServiceData.put(
	                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
	                currentServiceData.put(LIST_UUID, uuid);
	                gattServiceData.add(currentServiceData);

	                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
	                        new ArrayList<HashMap<String, String>>();
	                List<BluetoothGattCharacteristic> gattCharacteristics =
	                        gattService.getCharacteristics();
	                ArrayList<BluetoothGattCharacteristic> charas =
	                        new ArrayList<BluetoothGattCharacteristic>();

	                // Loops through available Characteristics.
	                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
	                    charas.add(gattCharacteristic);
	                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
	                    uuid = gattCharacteristic.getUuid().toString();
	                    if (uuid.contains("ffe4"))
	                    {
	                    	currentCharaData.put(
	                                LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
	                        currentCharaData.put(LIST_UUID, uuid);
	                        gattCharacteristicGroupData.add(currentCharaData);

	                        if (mGattCharacteristics != null) {
	                            final BluetoothGattCharacteristic characteristic =gattCharacteristic;

	                            final int charaProp = characteristic.getProperties();
	                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
	                                // If there is an active notification on a characteristic, clear
	                                // it first so it doesn't update the data field on the user interface.
	                                if (mNotifyCharacteristic != null) {
	                                    mBluetoothLeService.setCharacteristicNotification(
	                                            mNotifyCharacteristic, false);
	                                    mNotifyCharacteristic = null;
	                                }
	                                mBluetoothLeService.readCharacteristic(characteristic);
	                            }
	                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
	                                mNotifyCharacteristic = characteristic;
	                                mBluetoothLeService.setCharacteristicNotification(
	                                        characteristic, true);
	                                }

	                            }
	                    }
	                }
	                mGattCharacteristics.add(charas);
	                gattCharacteristicData.add(gattCharacteristicGroupData);

	            }
	        }

	    }

	    private static IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
	        return intentFilter;
	    }

	    private int checkReceiveData(byte revData[]) {
			int startChar=0x68;
			int dataLen=0;
			int cmdChar=0x51;
			int chkSum=0x00;
			byte tmpdata[]=null;
			int dataType=0;//
			double tmpVal=0.0;
			int tmpIVal=0;
			boolean isHistData=false;
			String viewData="";
			String viewTime="";
			String typeStr="";
			String Units="mg/dL";
			String DataTypeStr="";

			if (revData==null) return 0;

			int i=revData.length;
			if (i<4 || i>100) return 0;

			int mflag=revData[0] & 0xff;
			if (mflag==startChar)
			{
				revstarti=0;
				for (int j=0;j<i;j++)
				{
					revdataall[revstarti++]=revData[j];
				}
			}
			else
			{
				if (revstarti>50) return 0;
				for (int j=0;j<i;j++)
				{
					revdataall[revstarti++]=revData[j];
				}

			}

			if (revstarti>50)
			{
				tmpdata=new byte[revstarti];
				for (int j=0;j<revstarti;j++)
					tmpdata[j]=revdataall[j];
				mflag=tmpdata[0] & 0xff;
				int size=revstarti;
				if (mflag!=startChar) return 0;
				dataLen=tmpdata[1] & 0xff;
				if (dataLen+2!=revstarti) return 0;

				cmdChar=tmpdata[2] & 0xff;
				dataType=tmpdata[11] & 0xff;
				chkSum=tmpdata[dataLen+1] & 0xff;
				int tmpSum=0;
				for (int j=1;j<=dataLen;j++)
					tmpSum+=(tmpdata[j] & 0xff);
				tmpSum=tmpSum & 0xff;
				if (cmdChar==161)
					isHistData=false;
				else if (cmdChar==162)
					isHistData=true;
				else
					isHistData=false;

				if (chkSum==tmpSum)
				{
					int tmpYear=0,tmpMonth=0,tmpDay=0,tmpHour=0,tmpMin=0,tmpsec=0;
					int tmpHVal=0,tmpLval=0;
					tmpVal=0.0;
					tmpIVal=0;
					tmpYear=tmpdata[5] & 0xff;
					tmpMonth=tmpdata[6] & 0xff;
					tmpDay=tmpdata[7] & 0xff;
					tmpHour=tmpdata[8] & 0xff;
					tmpMin=tmpdata[9] & 0xff;
					tmpsec=tmpdata[10] & 0xff;
					tmpHVal=tmpdata[3] & 0xff;
					tmpLval=tmpdata[4] & 0xff;
					tmpVal=(tmpHVal*256.0+tmpLval)/100;
					tmpIVal=tmpHVal*256+tmpLval;

					tmpYear+=2000;
					viewTime=Integer.toString(tmpYear)+"-"+Integer.toString(tmpMonth)+"-"+Integer.toString(tmpDay)+" ";
					if (tmpHour<10) viewTime=viewTime+"0";
					viewTime=viewTime+Integer.toString(tmpHour)+":";
					if (tmpMin<10) viewTime=viewTime+"0";
					viewTime=viewTime+Integer.toString(tmpMin)+":";
					if (tmpsec<10) viewTime=viewTime+"0";
					viewTime=viewTime+Integer.toString(tmpsec);

					DataTypeStr="";

					if (dataType==0xA2)
					{
						DataTypeStr="Blood Glucose";
						typeStr="1";
						Units="mg/dL";
					}
					else if (dataType==0xA3)
					{
						DataTypeStr="Uric Acid";
						typeStr="3";
						Units="mg/dL";
					}
					else if (dataType==0xA4)
					{
						DataTypeStr="Total Cholesterol";
						typeStr="4";
						Units="mg/dL";
					}
					else if (dataType==0xA5)
					{
						DataTypeStr="Hemoglobin";
						typeStr="5";
						Units="g/dL";
					}
					else
					{
						DataTypeStr=" ";
					}

					viewData="";
					if (typeStr.equals("3"))
					{
						if (tmpVal<=1.48)
							viewData="Lo";
						else if (tmpVal>=19.809)
							viewData="Hi";
						else
							viewData=Double.toString(tmpVal);
					}
					else if (typeStr.equals("5"))
					{
						if (tmpVal<5.0)
							viewData="Lo";
						else if (tmpVal>27.0)
							viewData="Hi";
						else
							viewData=Double.toString(tmpVal);
					}
					else
					{
						viewData=Integer.toString(tmpIVal);
						if (typeStr.equals("1"))
						{
							if (tmpIVal<=20)
								viewData="Lo";
							else if (tmpIVal>=600)
								viewData="Hi";
							else
								viewData=Integer.toString(tmpIVal);
						}
						else if (typeStr.equals("4"))
						{
							if (tmpIVal<=103)
								viewData="Lo";
							else if (tmpIVal>=413)
								viewData="Hi";
							else
								viewData=Integer.toString(tmpIVal);
						}
						else
						{
							viewData=Integer.toString(tmpIVal);
						}
					}

					if (viewData.length()>0)
					{
						// results to be passed
					}

					if (isHistData)
						return 5;
					else
						return 2;

				}
				else
				{
					return 0;
				}

			}

			return 0;
		}


}
