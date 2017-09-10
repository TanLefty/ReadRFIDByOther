package com.raylinks.demo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
    
	ModuleControl moduleControl = new ModuleControl();
	private BluetoothAdapter mBluetoothAdapter;
	private static boolean connFlag = false;
	
	ListView LvMain;
	private ArrayList<HashMap<String, String>> arrayMenu;
	private static ArrayList<String> deviceList = new ArrayList<String>();
	private SimpleAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        arrayMenu = new ArrayList<HashMap<String, String>>();
        
        LvMain = (ListView)findViewById(R.id.mainLv);
        
        //初始化arrayMenu
        String[] array = {"连接/断开", "识别标签", "读取数据", "写入数据", "擦除数据", "锁定标签", "销毁标签", 
        		"选择操作", "功率设置", "频率设置", "寄存器操作", "固件升级", "其它操作", "关于/帮助", "退出"};
        for(int i=0;i<array.length;i++)
        {
        	HashMap<String, String> item = new HashMap<String, String>();
    		item.put("menuItem", array[i]);
    		arrayMenu.add(item);
        }
        
        adapter = new SimpleAdapter(this, 
        		arrayMenu, //数据源
				R.layout.mainlv_items,//ListItem的XML实现   
                new String[]{"menuItem"}, //动态数组与Item对应的子项
                new int[]{R.id.TvMenu //子项的id定义
				});
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (mBluetoothAdapter == null) { 
		    // Device does not support Bluetooth 
			Toast.makeText(MainActivity.this, "该设备没有蓝牙", 1).show();
			
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) { 
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
		    startActivityForResult(enableBtIntent, 10); 
		}
        
        LvMain.setAdapter(adapter);
        LvMain.setOnItemClickListener(new LvMainItemClickListener());
    }
    
    @SuppressWarnings("unchecked")
    class LvMainItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			HashMap<String, String> item = (HashMap<String, String>) LvMain.getItemAtPosition(position);
			String itemStr = item.get("menuItem");			
			
			if(itemStr.equals("连接/断开")){
				
				PairedDevList();
				
				Intent intent = new Intent();

				intent.putExtra("connFlag", connFlag);
				intent.putStringArrayListExtra("deviceList", deviceList);
				intent.setClass(MainActivity.this, ConnectDev.class);
				startActivityForResult(intent, 1);
			}else if(itemStr.equals("识别标签")){
				/**/
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, InventoryTag.class);
				startActivity(intent);
			}else if(itemStr.equals("读取数据")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, ReadSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("写入数据")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, WriteSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("擦除数据")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, EraseSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("锁定标签")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, LockSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("销毁标签")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, KillTagSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("选择操作")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, SelectSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("功率设置")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, PowerSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("频率设置")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, FrequencySetting.class);
				startActivity(intent);
			}else if(itemStr.equals("寄存器操作")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, RegisterSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("固件升级")){
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, UpdateSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("其它操作")){
				if(connFlag == false)
				{
					Toast.makeText(getApplicationContext(), "请先进行连接", 0).show();
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, OtherSetting.class);
				startActivity(intent);
			}else if(itemStr.equals("关于/帮助")){
				Intent intent = new Intent();
				intent.putExtra("connFlag", connFlag);
				intent.setClass(MainActivity.this, About.class);
				startActivity(intent);
			}else if(itemStr.equals("退出")){
				
				exit();
			}
		}
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == 1){
			Bundle extra = data.getExtras();
			
			if(extra != null)
			{
				connFlag = extra.getBoolean("connFlag");	
			}
		}
	}

	private void PairedDevList()
    {
    	deviceList.clear();
		
		if(!mBluetoothAdapter.isEnabled()){
			//请求打开蓝牙设备
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(intent);
		}

		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		
		String deviceStr = "";
		if(devices.size()>0){
			for(Iterator<BluetoothDevice> iterator = devices.iterator();iterator.hasNext();){
				BluetoothDevice bluetootdevice = (BluetoothDevice)iterator.next();
				
				deviceStr = bluetootdevice.getName() + "\n" + bluetootdevice.getAddress();
				deviceList.add(deviceStr);
			}
		}
    }

	@Override
	public void onBackPressed() {
		
		exit();
	}
    
	public void exit(){
		new AlertDialog.Builder(this)
		.setTitle("消息")
		.setMessage("确认退出！")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				moduleControl.UhfReaderDisconnect();
				connFlag = false;
				//finish();
				System.exit(0);
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).show();
	}
}