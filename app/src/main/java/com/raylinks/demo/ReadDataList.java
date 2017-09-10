package com.raylinks.demo;


import java.util.ArrayList;
import java.util.HashMap;
import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ReadDataList extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	private static byte flagCrc;
	private static String uii;
	private static String optionRead; 
	private static byte bBank_1;
	private static byte[] bPtr_1 = {0x00, 0x00};
	private static byte bCnt_1;
	private static byte bBank_2;
	private static byte[] bPtr_2 = {0x00, 0x00};
	private static byte bCnt_2;
	private static byte[] bAccessPwd ={0x00, 0x00, 0x00, 0x00};
	private static boolean loopFlag;
	
	static ArrayList<HashMap<String, String>> dataList;
	SimpleAdapter adapter;
	
	ListView LvData;
	Button BtStop;
	Button BtBack1;
	TextView TvTagUii_Data;
	TextView TvBank_Data;
	TextView TvPtr_Data;
	TextView TvLen_Data;
	TextView TvCount_Data;
	TextView TvData_Data;
	TextView TvError_Data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datalist);
		
		loopFlag = true;
		optionRead = this.getIntent().getStringExtra("optionRead");
		if(optionRead.equals("循环读")){
			uii = this.getIntent().getStringExtra("uiiStr");
		}
		
		bBank_1 = this.getIntent().getByteExtra("bBank_1", (byte)0);
		bPtr_1 = this.getIntent().getByteArrayExtra("bPtr_1");
		bCnt_1 = this.getIntent().getByteExtra("bCnt_1", (byte)0);
		if(optionRead.equals("防碰撞读（2级）"))
		{
			bBank_2 = this.getIntent().getByteExtra("bBank_2", (byte)0);
			bPtr_2 = this.getIntent().getByteArrayExtra("bPtr_2");
			bCnt_2 = this.getIntent().getByteExtra("bCnt_2", (byte)0);
		}
		bAccessPwd = this.getIntent().getByteArrayExtra("bAccessPwd");
		dataList = new ArrayList<HashMap<String, String>>();
		
		adapter = new SimpleAdapter(ReadDataList.this,
				dataList,
				R.layout.datalist_items,
				new String[]{"tagUii_Data","bank_Data","ptr_Data","len_Data","count_Data","data_Data", "error_Data"},
				new int[]{R.id.TvTagUii_Data, R.id.TvBank_Data, R.id.TvPtr_Data, R.id.TvLen_Data, R.id.TvCount_Data, R.id.TvData_Data, R.id.TvError_Data});
		
		LvData = (ListView)findViewById(R.id.LvRead);
		BtStop = (Button)findViewById(R.id.BtStop);
		BtBack1 = (Button)findViewById(R.id.BtBack1);
		TvTagUii_Data = (TextView)findViewById(R.id.TvTagUii_Data);
		TvBank_Data = (TextView)findViewById(R.id.TvBank_Data);
		TvPtr_Data = (TextView)findViewById(R.id.TvPtr_Data);
		TvLen_Data = (TextView)findViewById(R.id.TvLen_Data);
		TvCount_Data = (TextView)findViewById(R.id.TvCount_Data);
		TvData_Data = (TextView)findViewById(R.id.TvData_Data);
		TvError_Data = (TextView)findViewById(R.id.TvError_Data);
		
		BtStop.setOnClickListener(new BtStopClickListener());
		BtBack1.setOnClickListener(new BtBack1ClickListener());
		
		startReadData();
	}
	
	public class BtStopClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(BtStop.getText().equals("停止"))
			{
				if(!optionRead.equals("循环读"))
					moduleControl.UhfStopOperation(flagCrc);
				
				loopFlag = false;
				BtStop.setText("继续");
			}else{
				//loopFlag = true;
				startReadData();
				BtStop.setText("停止");
			}
		}
	}
	
	public class BtBack1ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!optionRead.equals("循环读"))
				moduleControl.UhfStopOperation(flagCrc);
			
			finish();
		}
	}
	
	private void startReadData(){
		
		loopFlag = true;
		
		if(optionRead.equals("循环读"))
		{
			if(uii == null)
				new ReadDataFromSingleTagLoopThread().start();
			else
				new ReadDataByEpcLoopThread().start();
			
		}else if(optionRead.equals("防碰撞读（1级）")){
			byte bOption = 0x00;
			byte[] bPayLoad = {0x03, 0x20};
			if(!moduleControl.UhfStartReadDataFromMultiTag(bAccessPwd, bBank_1, bPtr_1, bCnt_1, bOption, bPayLoad, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "开启防碰撞读数据失败", 0).show();
				return;
			}
			new AntiDataThread().start();
			
		}else{
			byte bOption = 0x01;
			byte[] bPayLoad = new byte[6];

			if((bPtr_2[0] & 0x80) == 0x80)
			{
				bPayLoad = new byte[6];
				bPayLoad[0] = bBank_2;
				bPayLoad[1] = bPtr_2[0];
				bPayLoad[3] = bPtr_2[1];
				bPayLoad[2] = bCnt_2;
				bPayLoad[4] = 0x03;
				bPayLoad[5] = 0x20;
			}else{
				bPayLoad = new byte[5];
				bPayLoad[0] = bBank_2;
				bPayLoad[1] = bPtr_2[0];
				bPayLoad[2] = bCnt_2;
				bPayLoad[3] = 0x03;
				bPayLoad[4] = 0x20;
			}
						
			if(!moduleControl.UhfStartReadDataFromMultiTag(bAccessPwd, bBank_1, bPtr_1, bCnt_1, bOption, bPayLoad, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "开启防碰撞读数据失败", 0).show();
				return;
			}
			new AntiDataThread().start();
		}
	}
	
	public int checkIsExist(String uiiStr, String bankStr, String ptrStr, String lenStr, String dataStr, String errorStr, ArrayList<HashMap<String, String>> tagList)
	{
		int existFlag = -1;
		String tempUiiStr = "";
		String tempBankStr = "";
		String tempPtrStr = "";
		String tempLenStr = "";
		String tempDataStr = "";
		String tempErrorStr = "";
		/*
		if(uiiStr.equals("") || bankStr.equals("") || ptrStr.equals("") || lenStr.equals("") || dataStr.equals("") || errorStr.equals(""))
			return existFlag; 
		*/
		for(int i=0;i<tagList.size();i++)
		{
			HashMap<String, String> temp = new HashMap<String, String>();
			temp = dataList.get(i);

			tempUiiStr = temp.get("tagUii_Data");
			tempBankStr = temp.get("bank_Data");
			tempPtrStr = temp.get("ptr_Data");
			tempLenStr = temp.get("len_Data");
			tempDataStr = temp.get("data_Data");
			tempErrorStr = temp.get("error_Data");
			
			if(uiiStr.equals(tempUiiStr) && bankStr.equals(tempBankStr) && ptrStr.equals(tempPtrStr) && lenStr.equals(tempLenStr) && dataStr.equals(tempDataStr) && errorStr.equals(tempErrorStr))
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
			String uiiStr = bundle.getString("uiiStr");
			String dataStr = bundle.getString("dataStr");
			String bank = bundle.getString("bank");
			String ptr = bundle.getString("ptr");
			String cnt = bundle.getString("cnt");
			String error = bundle.getString("error");
			
			HashMap<String, String> map = new HashMap<String, String>();
			
			map.put("tagUii_Data", uiiStr);
			map.put("bank_Data", bank);
			map.put("ptr_Data", ptr);
			map.put("len_Data", cnt);
			map.put("count_Data", String.valueOf(1));
			map.put("data_Data", dataStr);
			map.put("error_Data", error);
			
			boolean bool = false;
			int index = -1;
			bool = (!uiiStr.equals(null));
     		
     		if(bool)
     		{
     			index = checkIsExist(uiiStr, bank, ptr, cnt, dataStr, error, dataList);

     			if(index == -1)
     			{
     				dataList.add(map);
     				LvData.setAdapter(adapter);
     			}
     			else{
     				int count = Integer.parseInt(dataList.get(index).get("count_Data"), 10) + 1;
     				
     				map.put("count_Data", String.valueOf(count));
     				
     				dataList.set(index, map);
     				adapter.notifyDataSetChanged();
     			}
             } 
		}
	};

	class ReadDataFromSingleTagLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			byte[] bReadData = new byte[255];
			byte[] bErrorCode = new byte[1];
			
			while(loopFlag)
			{
				if(moduleControl.UhfReadDataFromSingleTag(bAccessPwd, bBank_1, bPtr_1, bCnt_1, bReadData, bUii, bLenUii, bErrorCode, flagCrc))
				{
					String sUii = fun.bytesToHexString(bUii, bLenUii[0]);
					String sReadData = fun.bytesToHexString(bReadData, 2*bCnt_1);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", sUii);
					bundle.putString("dataStr", sReadData);
					
					if(bBank_1 == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank_1 == 1)
						bundle.putString("bank", "UII");
					else if(bBank_1 == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr_1[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr_1[0]&0x7F)*127 + bPtr_1[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr_1[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt_1));
					bundle.putString("error", "00");
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}else{
				
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", "");
					bundle.putString("dataStr", "");
					
					if(bBank_1 == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank_1 == 1)
						bundle.putString("bank", "UII");
					else if(bBank_1 == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr_1[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr_1[0]&0x7F)*127 + bPtr_1[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr_1[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt_1));
					//bundle.putString("error", String.valueOf((bErrorCode[0]&0xFF)));
					bundle.putString("error", fun.byteToHexString(bErrorCode[0]));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	class ReadDataByEpcLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bUii = new byte[255];
			byte[] bReadData = new byte[255];
			byte[] bErrorCode = new byte[1];
			
			bUii = fun.HexStringToBytes(uii);
			while(loopFlag)
			{
				if(moduleControl.UhfReadDataByEPC(bAccessPwd, bBank_1, bPtr_1, bCnt_1, bUii, bReadData, bErrorCode, flagCrc))
				{
					String sReadData = fun.bytesToHexString(bReadData, 2*bCnt_1);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("uiiStr", uii);
					bundle.putString("dataStr", sReadData);
					
					if(bBank_1 == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank_1 == 1)
						bundle.putString("bank", "UII");
					else if(bBank_1 == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");
					
					if((bPtr_1[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr_1[0]&0x7F)*127 + bPtr_1[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr_1[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt_1));
					bundle.putString("error", "00");
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}else{
										
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("uiiStr", uii);
					bundle.putString("dataStr", "");
					
					if(bBank_1 == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank_1 == 1)
						bundle.putString("bank", "UII");
					else if(bBank_1 == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");
					
					if((bPtr_1[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr_1[0]&0x7F)*127 + bPtr_1[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr_1[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt_1));
					bundle.putString("error", fun.byteToHexString(bErrorCode[0]));
					Log.v("BreakPoint", "bErrorCode"+bErrorCode[0]);
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	class AntiDataThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			byte[] bStatus = new byte[1];
			byte[] bfDataLen = new byte[2];
			byte[] bfReadData = new byte[255];
			byte[] bsDataLen = new byte[2];
			byte[] bsReadData = new byte[255];

			while(loopFlag)
			{
				if(moduleControl.UhfGetDataFromMultiTag(bStatus, bfDataLen, bfReadData, bsDataLen, bsReadData, bUii, bLenUii))
				{
					String sUii = fun.bytesToHexString(bUii, bLenUii[0]);

					String sfReadData;
					String ssReadData;
					
					if(bStatus[0] == 0x00)
					{
						Message msg = handler.obtainMessage();
						Bundle bundle = new Bundle();
						
						sfReadData = fun.bytesToHexString(bfReadData, (bfDataLen[0]&0x7f)*127+bfDataLen[1]);
						
						bundle.putString("uiiStr", sUii);
						bundle.putString("dataStr", sfReadData);
						
						if(bBank_1 == 0)
							bundle.putString("bank", "RESERVED");
						else if(bBank_1 == 1)
							bundle.putString("bank", "UII");
						else if(bBank_1 == 2)
							bundle.putString("bank", "TID");
						else
							bundle.putString("bank", "USER");
						
						if((bPtr_1[0] & 0x80) == 0x80)
						{
							bundle.putString("ptr", String.valueOf((bPtr_1[0]&0x7F)*127 + bPtr_1[1]));	
						}else{
							bundle.putString("ptr", String.valueOf(bPtr_1[0]));	
						}
						
						bundle.putString("cnt", String.valueOf(bCnt_1));
						bundle.putString("error", "00");
						
						msg.setData(bundle);
						handler.sendMessage(msg);
						
					}else
					{	
						{   //1级数据
							Message msg = handler.obtainMessage();
							Bundle bundle = new Bundle();
							sfReadData = fun.bytesToHexString(bfReadData, (bfDataLen[0]&0x7f)*127+bfDataLen[1]);
							
							bundle.putString("uiiStr", sUii);
							bundle.putString("dataStr", sfReadData);
							
							if(bBank_1 == 0)
								bundle.putString("bank", "RESERVED");
							else if(bBank_1 == 1)
								bundle.putString("bank", "UII");
							else if(bBank_1 == 2)
								bundle.putString("bank", "TID");
							else
								bundle.putString("bank", "USER");
							
							if((bPtr_1[0] & 0x80) == 0x80)
							{
								bundle.putString("ptr", String.valueOf((bPtr_1[0]&0x7F)*127 + bPtr_1[1]));	
							}else{
								bundle.putString("ptr", String.valueOf(bPtr_1[0]));	
							}
							
							bundle.putString("cnt", String.valueOf(bCnt_1));
							bundle.putString("error", "00");
							
							msg.setData(bundle);
							handler.sendMessage(msg);
						}
						
						{	//2级数据
							Message msg = handler.obtainMessage();
							Bundle bundle = new Bundle();
							ssReadData = fun.bytesToHexString(bsReadData, (bsDataLen[0]&0x7f)*127+bsDataLen[1]);
							
							bundle.putString("uiiStr", sUii);
							bundle.putString("dataStr", ssReadData);
							
							if(bBank_2 == 0)
								bundle.putString("bank", "RESERVED");
							else if(bBank_2 == 1)
								bundle.putString("bank", "UII");
							else if(bBank_2 == 2)
								bundle.putString("bank", "TID");
							else
								bundle.putString("bank", "USER");
							
							if((bPtr_2[0] & 0x80) == 0x80)
							{
								bundle.putString("ptr", String.valueOf((bPtr_2[0]&0x7F)*127 + bPtr_2[1]));	
							}else{
								bundle.putString("ptr", String.valueOf(bPtr_2[0]));	
							}

							bundle.putString("cnt", String.valueOf(bCnt_2));
							bundle.putString("error", "00");
							
							msg.setData(bundle);
							handler.sendMessage(msg);
						}
					}
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		
		if(!optionRead.equals("循环读"))
			moduleControl.UhfStopOperation(flagCrc);
	}
}
