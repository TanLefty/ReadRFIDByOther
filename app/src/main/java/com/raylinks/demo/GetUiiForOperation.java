package com.raylinks.demo;


import java.util.ArrayList;
import java.util.HashMap;
import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GetUiiForOperation extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	private static byte OptionForActivity;
	private static boolean loopFlag;
	private static boolean connFlag;
	private byte flagCrc;
	//private static String uiiStr;
	static ArrayList<HashMap<String, String>> uiiList;
	SimpleAdapter uiiAdapter;
	
	ListView LvUiiList;
	Button BtReadUii;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.getuiiforoperation);
		
		flagCrc = 0x00;
		uiiList = new ArrayList<HashMap<String, String>>();
		OptionForActivity = this.getIntent().getByteExtra("OptionForActivity", (byte) 0);
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		LvUiiList = (ListView)findViewById(R.id.LvUiiList);
		BtReadUii = (Button)findViewById(R.id.BtReadUii);
		
		uiiAdapter = new SimpleAdapter(GetUiiForOperation.this,
				uiiList,
				R.layout.getuiiforoperation_items,
				new String[]{"tagUii", "uiiCount"},
				new int[]{R.id.TvUiiItem, R.id.TvUiiCountItem});
		
		BtReadUii.setOnClickListener(new BtReadUiiClickListener());
		LvUiiList.setOnItemClickListener(new LvUiiListItemClickListener());
	}

	public class BtReadUiiClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			if(BtReadUii.getText().equals("读取UII"))//识别标签
			{
				if(moduleControl.UhfStartInventory((byte)1, (byte) 1, flagCrc))
				{
					BtReadUii.setText("停止");
					loopFlag = true;
					new TagThread().start();
				}
			}else{//停止识别
				loopFlag = false;
				if(moduleControl.UhfStopOperation(flagCrc))
				{
					BtReadUii.setText("读取UII");					
				}else{
					
					Toast.makeText(GetUiiForOperation.this, "停止失败", 0).show();
				}
			}
		}
	}
	
	public class LvUiiListItemClickListener implements OnItemClickListener{

		@SuppressWarnings("unchecked")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			HashMap<String, String> map = (HashMap<String, String>)uiiAdapter.getItem(position);
			String uiiStr = map.get("tagUii");
			Log.v("BreakPoint", "uiiStr: "+uiiStr);
			Intent intent = new Intent();
			intent.putExtra("tagUii", uiiStr);
			switch(OptionForActivity)
			{
				case 0://read
					intent.setClass(GetUiiForOperation.this, ReadSetting.class);
					GetUiiForOperation.this.setResult(RESULT_OK, intent);
					break;
				case 1://write
					intent.setClass(GetUiiForOperation.this, WriteSetting.class);
					GetUiiForOperation.this.setResult(RESULT_OK, intent);
					break;
				case 2://erase
					intent.setClass(GetUiiForOperation.this, EraseSetting.class);
					GetUiiForOperation.this.setResult(RESULT_OK, intent);
					break;
				case 3://lock
					intent.setClass(GetUiiForOperation.this, LockSetting.class);
					GetUiiForOperation.this.setResult(RESULT_OK, intent);
					break;
				case 4://kill
					intent.setClass(GetUiiForOperation.this, KillTagSetting.class);
					GetUiiForOperation.this.setResult(RESULT_OK, intent);
					break;
				default:
					intent.setClass(GetUiiForOperation.this, ReadSetting.class);
					break;
			}

			finish();
		}
	}
	
	public int checkIsExist(String uiiStr, ArrayList<HashMap<String, String>> tagList)
	{
		int existFlag = -1;
		String tempStr = "";
		for(int i=0;i<tagList.size();i++)
		{
			HashMap<String, String> temp = new HashMap<String, String>();
			temp = tagList.get(i);
			
			tempStr = temp.get("tagUii");
			
			if(uiiStr != "" && uiiStr.equals(tempStr))
			{
				existFlag = i;
			}
		}
		
		return existFlag;
	}
	
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){

			Bundle bundle = msg.getData();
			String tagUii = bundle.getString("tagUii");
			//Log.v("BreakPoint", "tagUii: " + tagUii);
			HashMap<String, String> map = new HashMap<String, String>();
			
			map.put("tagUii", tagUii);
			map.put("uiiCount", String.valueOf(1));
			
			boolean bool = false;
			int index = -1;
			try {
				bool = (tagUii != "") && (!tagUii.equals(null));
			} catch (Exception e) {
				Log.v("BreakPoint", "异常："+e.getMessage());
				e.printStackTrace();
			}
     		
     		if(bool)
     		{
     			index = checkIsExist(tagUii,uiiList);

     			if(index == -1)
     			{
     				uiiList.add(map);
     				LvUiiList.setAdapter(uiiAdapter);
     			}
     			else{
     				int tagcount = Integer.parseInt(uiiList.get(index).get("uiiCount"), 10) + 1;
     				
     				map.put("uiiCount", String.valueOf(tagcount));
     				
     				uiiList.set(index, map);
     				uiiAdapter.notifyDataSetChanged();
     			}
             } 
		}
	};

	class TagThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			
			while(loopFlag)
			{
				if(moduleControl.UhfReadInventory(bLenUii, bUii))
				{
					String sUii = fun.bytesToHexString(bUii, bLenUii[0]);
					//Log.v("BreakPoint", sUii);
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("tagUii", sUii);
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
			
			/**
			 * Debug
			 */
//			if(moduleControl.UhfStopOperation(flagCrc))
//			{
//				BtReadUii.setText("读取UII");					
//			}else{
//				
//				Toast.makeText(GetUiiForOperation.this, "停止失败", 0).show();
//			}
		}
	}
}
