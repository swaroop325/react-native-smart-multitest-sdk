package com.example.bluetooth.le;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.example.bluetoothg01u.le.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */

public class DeviceControlActivity extends Activity {
	 private final static String TAG = DeviceControlActivity.class.getSimpleName();

	    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	    private TextView mConnectionState;
	    private TextView mDataField;
	    private String mDeviceName;
	    private String mDeviceAddress;
	    private TextView mTimeField;
	    private TextView mUnitField;
	    private TextView mItemField;
	    
	    private BluetoothLeService mBluetoothLeService;
	    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
	            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	    private boolean mConnected = false;
	    private BluetoothGattCharacteristic mNotifyCharacteristic;
	    
	    private final String LIST_NAME = "NAME";
	    private final String LIST_UUID = "UUID";
	    
	    private String mdatPartOne="";
	    private String mdatPartTwo="";
	    
	    private final byte replyBleData[] = new byte[] { (byte) 0x69, (byte) 0x02, (byte) 0xA1, (byte) 0xA3,(byte) 0x00 };
	    private final byte replyBleData2[] = new byte[] { (byte) 0x5A, (byte) 0x02, (byte) 0x51, (byte) 0x53,(byte) 0x00 };
	    private final byte replyBleData3[] = new byte[] { (byte) 0x5A, (byte) 0x02, (byte) 0x52, (byte) 0x54,(byte) 0x00 };
	    private final byte replyBleConn[] = new byte[] { (byte) 0x69, (byte) 0x02, (byte) 0xA2, (byte) 0xA4,(byte) 0x00 };
	    
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
	            boolean connState=mBluetoothLeService.connect(mDeviceAddress);
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
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            final String action = intent.getAction();
	            
	            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
	                mConnected = true;
	                updateConnectionState(R.string.connected);
	                invalidateOptionsMenu();
	                setWaits();
	            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
	                mConnected = false;
	                updateConnectionState(R.string.disconnected);
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
					
					if (tmpRes==1)
					{
						if (mBluetoothLeService==null || mConnected==false) return;
	                	new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mBluetoothLeService.writeCharacteristic(replyBleConn);
							}
						}, 10);
						
					}
					else if (tmpRes==2)
					{
						if (mBluetoothLeService==null || mConnected==false) return;
	                	new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
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
								mBluetoothLeService.writeCharacteristic(replyBleData2);
							}
						}, 10);
						
					}
					else if (tmpRes==6)
					{
						if (mBluetoothLeService==null || mConnected==false) return;
	                	new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mBluetoothLeService.writeCharacteristic(replyBleData3);

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
	        setContentView(R.layout.gatt_services_characteristics);

	        final Intent intent = getIntent();
	        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
	        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
	        mConnectionState = (TextView) findViewById(R.id.connection_state);
	        mDataField = (TextView) findViewById(R.id.textView_data);
	        mTimeField = (TextView) findViewById(R.id.textView_time);
	        mUnitField = (TextView) findViewById(R.id.textView_unit);
	        mItemField = (TextView) findViewById(R.id.textView_name);
	        
	        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
	        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	        
	    }

	    @Override
	    protected void onResume() {
	        super.onResume();
	        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	        if (mBluetoothLeService != null) {
	            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
	            Log.d(TAG, "Connect request result=" + result);
	        }
	        
	        mdatPartOne="";
	        mdatPartTwo="";
	        mDataField.setText("");
	        mTimeField.setText("");
	        mUnitField.setText("");
	        mItemField.setText("");
	        
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

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch(item.getItemId()) {
	            case R.id.menu_connect:
	                mBluetoothLeService.connect(mDeviceAddress);
	                return true;
	            case R.id.menu_disconnect:
	                mBluetoothLeService.disconnect();
	                return true;
	            case android.R.id.home:
	                onBackPressed();
	                return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }

	    private void updateConnectionState(final int resourceId) {
	        runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	                mConnectionState.setText(resourceId);
	            }
	        });
	    }
	    
		// Demonstrates how to iterate through the supported GATT Services/Characteristics.
	    // In this sample, we populate the data structure that is bound to the ExpandableListView
	    // on the UI.
	    private void displayGattServices(List<BluetoothGattService> gattServices) {
	        if (gattServices == null) return;
	        String uuid = null;
	        String unknownServiceString = getResources().getString(R.string.unknown_service);
	        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
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

			int startChar=0x69;//起始符
			int startChar2=0xA5;//质控起始符
			int dataLen=0;
			int cmdChar=0x51;
			int chkSum=0x00;
			int dataType=0;
			double tmpVal=0.0;
			String viewData="";
			String viewTime="";
			String Units="mg/L";
			String DataTypeStr="";
			if (revData==null) return 0;
			
			int i=revData.length;
			if (i<4 || i>100) return 0;
			
			int mflag=revData[0] & 0xff;
			
			if ((mflag!=startChar) && (mflag!=startChar2)) return 0;
			
			dataLen=revData[1] & 0xff;
			
			if (dataLen==2)
			{
				int d2=revData[2] & 0xff;
				int d3=revData[3] & 0xff;
				if (d2==0x52 && d3==0x54)
					return 1;
				else
					return 0;
			}
			
			if (dataLen>2)
			{
				cmdChar=revData[2] & 0xff;
				dataType=revData[dataLen] & 0xff;
				chkSum=revData[dataLen+1] & 0xff;
				int tmpSum=0;
				for (int j=1;j<=dataLen;j++)
					tmpSum+=(revData[j] & 0xff);
				tmpSum=tmpSum & 0xff;
				
				if (cmdChar==26)
				{
					
					if (chkSum==tmpSum)
					{
						return 5;
					}
					else
					{
						return 0;
					}
					
				}
				else if (cmdChar==42)
				{
					if (chkSum==tmpSum)
					{
						return 6;
					}
					else
					{
						return 0;
					}
				}
				
				if (cmdChar!=81) return 0;
				
				if (chkSum==tmpSum)
				{
					int tmpYear=0,tmpMonth=0,tmpDay=0,tmpHour=0,tmpMin=0,tmpsec=0;
					int tmpHVal=0,tmpLval=0;
					tmpVal=0.0;
					
					tmpYear=revData[3] & 0xff;
					tmpMonth=revData[4] & 0xff;
					tmpDay=revData[5] & 0xff;
					tmpHour=revData[6] & 0xff;
					tmpMin=revData[7] & 0xff;
					tmpsec=revData[8] & 0xff;
					tmpHVal=revData[9] & 0xff;
					tmpLval=revData[10] & 0xff;
					
					tmpVal=tmpHVal*256+tmpLval;
					
					tmpYear+=2000;
					viewTime=Integer.toString(tmpYear)+"-"+Integer.toString(tmpMonth)+"-"+Integer.toString(tmpDay)+" ";
					
					if (tmpHour<10) viewTime=viewTime+"0";
					viewTime=viewTime+Integer.toString(tmpHour)+":";
					if (tmpMin<10) viewTime=viewTime+"0";
					viewTime=viewTime+Integer.toString(tmpMin)+":";
					if (tmpsec<10) viewTime=viewTime+"0";
					viewTime=viewTime+Integer.toString(tmpsec);
					
					DataTypeStr="";
					viewData="";
					if (dataType==0xA1)
					{
						DataTypeStr="尿糖";
						viewData=Double.toString(tmpVal);
					}
					else if (dataType==0xA2)
					{
						DataTypeStr="血糖";
						if (tmpVal<=20)
							viewData="Lo";
						else if (tmpVal>=600)
							viewData="Hi";
						else
							viewData=Double.toString(tmpVal);
					}
					
					if (viewData.length()>0)
					{
						mTimeField.setText(viewTime);
						mDataField.setText(viewData);
						mItemField.setText(DataTypeStr);
						mUnitField.setText(Units);
					}
					
					return 2;
					
				}
				
			}
			
			return 0;
		}
	  	
}
