package com.raylinks.demo;


import java.util.ArrayList;
import java.util.HashMap;
import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class WriteDataList extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	private static byte flagCrc;
	private static String uii;
	private static String optionWrite; 
	private static byte bBank;
	private static byte[] bPtr = {0x00, 0x00};
	private static byte bCnt;
	private static byte[] bAccessPwd = {0x00, 0x00, 0x00, 0x00};
	private static byte[] bWriteData = new byte[255];
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
		uii = this.getIntent().getStringExtra("uiiStr");
		optionWrite = this.getIntent().getStringExtra("optionWrite");
		bBank = this.getIntent().getByteExtra("bBank", (byte)0);
		bPtr = this.getIntent().getByteArrayExtra("bPtr");
		
		bCnt = this.getIntent().getByteExtra("bCnt", (byte)0);
		bAccessPwd = this.getIntent().getByteArrayExtra("bAccessPwd");
		bWriteData = this.getIntent().getByteArrayExtra("bWriteData");
		
		dataList = new ArrayList<HashMap<String, String>>();
		adapter = new SimpleAdapter(WriteDataList.this,
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
		
		writeDataLoop();
	}
	
	public class BtStopClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(BtStop.getText().equals("Í£Ö¹"))
			{				
				loopFlag = false;
				BtStop.setText("¼ÌÐø");
			}else{
				writeDataLoop();
				BtStop.setText("Í£Ö¹");
			}
		}
	}
	
	public class BtBack1ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			finish();
		}
	}
	
	private void writeDataLoop(){
		
		loopFlag = true;
		
		if(optionWrite.equals("µ¥×ÖÑ­»·"))
		{
			if(uii == null)
				new WriteSingleDataToSingleTagLoopThread().start();
			else
				new WriteSingleDataByEPCLoopThread().start();
			
		}else{
			if(uii == null)
				new WriteMultiDataToSingleTagLoopThread().start();
			else
				new WriteMultiDataByEPCLoopThread().start();
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
		
//		if(uiiStr.equals("") || bankStr.equals("") || ptrStr.equals("") || lenStr.equals("") || dataStr.equals(""))
//			return existFlag; 
		
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

	class WriteSingleDataToSingleTagLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			byte[] bErrorCode = new byte[1];
			
			while(loopFlag)
			{				
				if(moduleControl.UhfWriteDataToSingleTag(bAccessPwd, bBank, bPtr, (byte) 1, bWriteData, bUii, bLenUii, bErrorCode, flagCrc))
				{
					String sUii = fun.bytesToHexString(bUii, bLenUii[0]);
					String sWriteData = fun.bytesToHexString(bWriteData, 2);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", sUii);
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", "00");
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}else{
					
					String sWriteData = fun.bytesToHexString(bWriteData, 2);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", "");
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", fun.byteToHexString(bErrorCode[0]));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	class WriteSingleDataByEPCLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bUii = fun.HexStringToBytes(uii);
			byte[] bErrorCode = new byte[1];
			bUii = fun.HexStringToBytes(uii);
			
			while(loopFlag)
			{						
				if(moduleControl.UhfWriteDataByEPC(bAccessPwd, bBank, bPtr, (byte) 1, bUii, bWriteData, bErrorCode, flagCrc))
				{
					String sWriteData = fun.bytesToHexString(bWriteData, 2);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", uii);
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");
					
					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", "00");
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}else{
					String sWriteData = fun.bytesToHexString(bWriteData, 2);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", uii);
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");
					
					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", fun.byteToHexString(bErrorCode[0]));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	class WriteMultiDataToSingleTagLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			byte[] bStatus = new byte[1];
			byte[] bErrorCode = new byte[1];
			byte[] bWriteLen = new byte[1];
			
			while(loopFlag)
			{	
				if(moduleControl.UhfBlockWriteDataToSingleTag(bAccessPwd, bBank, bPtr, bCnt, bWriteData, bUii, bLenUii, bStatus, bErrorCode, bWriteLen, flagCrc))
				{
					String sUii = fun.bytesToHexString(bUii, bLenUii[0]);
					String sWriteData = fun.bytesToHexString(bWriteData, 2*bCnt);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", sUii);
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", "00");
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}else{
					String sWriteData = fun.bytesToHexString(bWriteData, 2*bCnt);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", "");
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", fun.byteToHexString(bErrorCode[0]));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	class WriteMultiDataByEPCLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		public void run()
		{
			byte[] bUii = fun.HexStringToBytes(uii);
			byte[] RuUii = new byte[255];
			byte[] bWriteLen = new byte[1];
			byte[] bStatus = new byte[1];
			byte[] bErrorCode = new byte[1];
			bUii = fun.HexStringToBytes(uii);
			while(loopFlag)
			{
				if(moduleControl.UhfBlockWriteDataByEPC(bAccessPwd, bBank, bPtr, bCnt, bUii, bWriteData, bErrorCode, bStatus, bWriteLen, RuUii, flagCrc))
				{
					String sWriteData = fun.bytesToHexString(bWriteData, 2*bCnt);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", uii);
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", "00");
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}else{
					String sWriteData = fun.bytesToHexString(bWriteData, 2*bCnt);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					
					bundle.putString("uiiStr", uii);
					bundle.putString("dataStr", sWriteData);
					
					if(bBank == 0)
						bundle.putString("bank", "RESERVED");
					else if(bBank == 1)
						bundle.putString("bank", "UII");
					else if(bBank == 2)
						bundle.putString("bank", "TID");
					else
						bundle.putString("bank", "USER");

					if((bPtr[0] & 0x80) == 0x80)
					{
						bundle.putString("ptr", String.valueOf((bPtr[0]&0x7F)*127 + bPtr[1]));	
					}else{
						bundle.putString("ptr", String.valueOf(bPtr[0]));	
					}
					
					bundle.putString("cnt", String.valueOf(bCnt));
					bundle.putString("error", fun.byteToHexString(bErrorCode[0]));
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
}
