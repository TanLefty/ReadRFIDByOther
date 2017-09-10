package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class WriteSetting extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();

	private static byte flagCrc;
	private static boolean connFlag;
	private static String optionWrite;
	
	byte[] bAccessPwd = {0x00, 0x00, 0x00, 0x00}; 
	byte bBank; 
	byte[] bPtr = new byte[2]; 
	byte bCnt;
	byte[] bUii = new byte[255];
	byte[] reUii = new byte[255];
	byte[] bLenUii = new byte[1];
	byte[] bWriteData = new byte[255]; 
	byte[] bStatus = new byte[1];
	byte[] bErrorCode = new byte[1];
	
	CheckBox CkWithUii_Write;
	EditText EtTagUii_Write;
	Spinner SpinnerBank_Write;
	EditText EtPtr_Write;
	EditText EtLen_Write;
	EditText EtData_Write;
	EditText EtAccessPwd_Write;
	Spinner SpinnerOption_Write;
	Button BtUii_Write;
	Button BtWrite;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		optionWrite = "单字单次";
		bBank = 0x00;
		
		CkWithUii_Write = (CheckBox)findViewById(R.id.CkWithUii_Write);
		EtTagUii_Write = (EditText)findViewById(R.id.EtTagUii_Write);
		SpinnerBank_Write = (Spinner)findViewById(R.id.SpinnerBank_Write);
		EtPtr_Write = (EditText)findViewById(R.id.EtPtr_Write);
		EtLen_Write = (EditText)findViewById(R.id.EtLen_Write);
		EtData_Write = (EditText)findViewById(R.id.EtData_Write);
		EtAccessPwd_Write = (EditText)findViewById(R.id.EtAccessPwd_Write);
		SpinnerOption_Write = (Spinner)findViewById(R.id.SpinnerOption_Write);
		BtUii_Write = (Button)findViewById(R.id.BtUii_Write);
		BtWrite = (Button)findViewById(R.id.BtWrite);
		
		EtTagUii_Write.setKeyListener(null);
		BtUii_Write.setEnabled(false);
		EtLen_Write.setEnabled(false);
		
		CkWithUii_Write.setOnClickListener(new CkWithUii_WriteClickListener());
		BtUii_Write.setOnClickListener(new BtUii_WriteClickListener());
		SpinnerBank_Write.setOnItemSelectedListener(new SpinnerBank_WriteSelectedListener());
		SpinnerOption_Write.setOnItemSelectedListener(new SpinnerOption_WriteSelectedListener());
		BtWrite.setOnClickListener(new BtWriteOnClickListener());
	}
	
	public class BtUii_WriteClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			if(moduleControl.UhfInventorySingleTag(bLenUii, bUii, flagCrc))
			{
				String uiiStr = fun.bytesToHexString(bUii, bLenUii[0]);
				EtTagUii_Write.setText(uiiStr);
			}else{
				EtTagUii_Write.setText("");
				Toast.makeText(WriteSetting.this, "读取标签号失败", 0).show();
			}
		}
	}
	
	public class BtWriteOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(WriteSetting.this, "请先进行连接", 0).show();
				return;
			}
			String ptrStr = EtPtr_Write.getText().toString().trim();
			if(ptrStr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "地址不能为空", 0).show();
				return;
			}else if(!fun.isDecimal(ptrStr)){
				Toast.makeText(getApplicationContext(), "地址必须为十进制数据", 0).show();
				return;
			}else{

				if(Integer.parseInt(ptrStr)>127)
				{
					bPtr[0] = (byte) (Integer.parseInt(ptrStr)>>7 | 0x80);
					bPtr[1] = (byte) (Integer.parseInt(ptrStr) & 0x7F);
				}else{
					bPtr[0] = (byte) (Integer.parseInt(ptrStr));
				}
				
				Log.v("BreakPoint","Ptr[0]: "+bPtr[0]);
				Log.v("BreakPoint","Ptr[1]: "+bPtr[1]);
			}
			if(optionWrite.equals("多字单次")||optionWrite.equals("多字循环"))
			{
				String cntStr = EtLen_Write.getText().toString().trim();
				if(cntStr.equals(""))
				{
					Toast.makeText(getApplicationContext(), "长度不能为空", 0).show();
					return;
				}else if(!fun.isDecimal(cntStr)){
					Toast.makeText(getApplicationContext(), "长度必须为十进制数据", 0).show();
					return;
				}else{
					bCnt = Byte.parseByte(cntStr);
				}
			}
			
			String dataStr = EtData_Write.getText().toString().trim();
			if(!dataStr.equals(""))
			{
				if(optionWrite.equals("单字单次"))
				{
					if(dataStr.length() != 4)
					{
						Toast.makeText(getApplicationContext(), "写入数据的字符串长度必须为4", 0).show();
						return;
					}
				}
				
				if((dataStr.length())%4 != 0)
				{
					Toast.makeText(getApplicationContext(), "写入数据的字符串长度必须为4的倍数", 0).show();
					return;
				}else if(!fun.isHex(dataStr)){
					Toast.makeText(getApplicationContext(), "写入数据必须为十六进制数据", 0).show();
					return;
				}else{
					bWriteData = fun.HexStringToBytes(dataStr);
				}
			}else{
				Toast.makeText(getApplicationContext(), "写入数据不能为空", 0).show();
				return;
			}
			
			String pwdStr = EtAccessPwd_Write.getText().toString().trim();
			if(!pwdStr.equals(""))
			{
				if(pwdStr.length() != 8)
				{
					Toast.makeText(getApplicationContext(), "访问密码的长度必须为8", 0).show();
					return;
				}else if(!fun.isHex(pwdStr)){
					Toast.makeText(getApplicationContext(), "访问密码必须为十六进制数据", 0).show();
					return;
				}else{
					bAccessPwd = fun.HexStringToBytes(pwdStr);
				}
			}else{
				bAccessPwd[0] = 0x00;
				bAccessPwd[1] = 0x00;
				bAccessPwd[2] = 0x00;
				bAccessPwd[3] = 0x00;
			}
			
			if(optionWrite.equals("单字单次")){
				
				if(CkWithUii_Write.isChecked())//指定标签
				{
				    String uiiStr = EtTagUii_Write.getText().toString().trim();
					if(uiiStr.equals(""))
					{
						Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
						return;
					}else{
						bUii = fun.HexStringToBytes(uiiStr);
					}
					
					if(moduleControl.UhfWriteDataByEPC(bAccessPwd, bBank, bPtr, (byte) 1, bUii, bWriteData, bErrorCode, flagCrc))
					{
						Toast.makeText(getApplicationContext(), "写入数据成功", 0).show();
					}else{
						Toast.makeText(getApplicationContext(), "写入数据失败", 0).show();
					}

				}else{
					if(moduleControl.UhfWriteDataToSingleTag(bAccessPwd, bBank, bPtr, (byte) 1, bWriteData, bUii, bLenUii, bErrorCode, flagCrc))
					{
						String uiiStr = fun.bytesToHexString(bUii, bLenUii[0]);
						Toast.makeText(getApplicationContext(), "写入数据成功\nUII: "+uiiStr, 0).show();
					}else{
						Toast.makeText(getApplicationContext(), "写入数据失败", 0).show();
					}
				}
			}else if(optionWrite.equals("多字单次")){
				if(CkWithUii_Write.isChecked())//指定标签
				{
				    String uiiStr = EtTagUii_Write.getText().toString().trim();
					if(uiiStr.equals(""))
					{
						Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
						return;
					}else{
						bUii = fun.HexStringToBytes(uiiStr);
					}
					
					byte[] bWriteLen = new byte[1];
					byte[] RuUii = new byte[255];
					if(moduleControl.UhfBlockWriteDataByEPC(bAccessPwd, bBank, bPtr, bCnt, bUii, bWriteData, bErrorCode, bStatus, bWriteLen, RuUii, flagCrc))
					{
						Toast.makeText(getApplicationContext(), "写入数据成功", 0).show();
					}else{
						Toast.makeText(getApplicationContext(), "写入数据失败", 0).show();
					}

				}else{
					
					byte[] bWriteLen = new byte[1];
					if(moduleControl.UhfBlockWriteDataToSingleTag(bAccessPwd, bBank, bPtr, bCnt, bWriteData, bUii, bLenUii, bStatus, bErrorCode, bWriteLen, flagCrc))
					{
						String uiiStr = fun.bytesToHexString(bUii, bLenUii[0]);
						Toast.makeText(getApplicationContext(), "写入数据成功\nUII: "+uiiStr, 0).show();
					}else{
						Toast.makeText(getApplicationContext(), "写入数据失败", 0).show();
					}
				}
			}else if(optionWrite.equals("单字循环")){
				Intent intent = new Intent();
				if(CkWithUii_Write.isChecked())
				{
					String uiiStr = EtTagUii_Write.getText().toString().trim();
					if(uiiStr.equals(""))
					{
						Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
						return;
					}
					intent.putExtra("uiiStr", uiiStr);
				}
				
				intent.putExtra("optionWrite", "单字循环");			
				intent.putExtra("bBank", bBank);
				intent.putExtra("bPtr", bPtr);
				intent.putExtra("bCnt", (byte)1);
				intent.putExtra("bAccessPwd", bAccessPwd);
				intent.putExtra("bWriteData", bWriteData);
				intent.setClass(WriteSetting.this, WriteDataList.class);
				startActivity(intent);
			}else{
				Intent intent = new Intent();
				if(CkWithUii_Write.isChecked())
				{
					String uiiStr = EtTagUii_Write.getText().toString().trim();
					if(uiiStr.equals(""))
					{
						Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
						return;
					}
					intent.putExtra("uiiStr", uiiStr);
				}
				
				intent.putExtra("optionWrite", "多字循环");			
				intent.putExtra("bBank", bBank);
				intent.putExtra("bPtr", bPtr);
				intent.putExtra("bCnt", bCnt);
				intent.putExtra("bAccessPwd", bAccessPwd);
				intent.putExtra("bWriteData", bWriteData);
				intent.setClass(WriteSetting.this, WriteDataList.class);
				startActivity(intent);
			}
		}
	}
	
	public class CkWithUii_WriteClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			EtTagUii_Write.setText("");
			
			if(CkWithUii_Write.isChecked())
			{
				BtUii_Write.setEnabled(true);
			}else{
				BtUii_Write.setEnabled(false);
			}
		}
	}
	
	public class SpinnerBank_WriteSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			if(position == 0){
				bBank = 0x00;
			}else if(position == 1){
				bBank = 0x01;
			}else if(position == 2){
				bBank = 0x02;
			}else{
				bBank = 0x03;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	}
	
	public class SpinnerOption_WriteSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			optionWrite = (String) SpinnerOption_Write.getSelectedItem();
			
			if(optionWrite.equals("单字单次")){
				EtLen_Write.setEnabled(false);
			}else if(optionWrite.equals("多字单次")){
				EtLen_Write.setEnabled(true);
			}else if(optionWrite.equals("单字循环")){
				EtLen_Write.setEnabled(false);
			}else{
				EtLen_Write.setEnabled(true);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
}
