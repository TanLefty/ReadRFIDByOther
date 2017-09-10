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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ReadSetting extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();

	private static byte flagCrc;
	private static boolean connFlag;
	private static String optionRead;
	
	byte[] bAccessPwd = {0x00, 0x00, 0x00, 0x00}; 
	byte bBank_1; 
	byte[] bPtr_1 = new byte[2]; 
	byte bCnt_1;
	byte bBank_2; 
	byte[] bPtr_2 = new byte[2]; 
	byte bCnt_2;
	byte[] bUii = new byte[255];
	byte[] reUii = new byte[255];
	byte[] bLenUii = new byte[1];
	byte[] bReadData = new byte[255]; 
	byte[] bErrorCode = new byte[1];
	
	CheckBox CkWithUii_Read;
	EditText EtTagUii_Read;
	Spinner SpinnerBank_Read;
	EditText EtPtr_Read;
	EditText EtLen_Read;
	EditText EtAccessPwd_Read;
	Spinner SpinnerOption_Read;
	Spinner SpinnerBank2_Read;
	EditText EtPtr2_Read;
	EditText EtLen2_Read;
	EditText EtData_Read;
	Button BtUii_Read;
	Button BtRead;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		optionRead = "单次读";
		bBank_1 = 0x00;
		bBank_2 = 0x00;
		
		CkWithUii_Read = (CheckBox)findViewById(R.id.CkWithUii_Read);
		EtTagUii_Read = (EditText)findViewById(R.id.EtTagUii_Read);
		SpinnerBank_Read = (Spinner)findViewById(R.id.SpinnerBank_Read);
		EtPtr_Read = (EditText)findViewById(R.id.EtPtr_Read);
		EtLen_Read = (EditText)findViewById(R.id.EtLen_Read);
		EtAccessPwd_Read = (EditText)findViewById(R.id.EtAccessPwd_Read);
		SpinnerOption_Read = (Spinner)findViewById(R.id.SpinnerOption_Read);
		SpinnerBank2_Read = (Spinner)findViewById(R.id.SpinnerBank2_Read);
		EtPtr2_Read = (EditText)findViewById(R.id.EtPtr2_Read);
		EtLen2_Read = (EditText)findViewById(R.id.EtLen2_Read);
		EtData_Read = (EditText)findViewById(R.id.EtData_Read);
		BtUii_Read = (Button)findViewById(R.id.BtUii_Read);
		BtRead = (Button)findViewById(R.id.BtRead);
		
		EtTagUii_Read.setKeyListener(null);
		BtUii_Read.setEnabled(false);
		SpinnerBank2_Read.setEnabled(false);
		EtPtr2_Read.setEnabled(false);
		EtLen2_Read.setEnabled(false);

		CkWithUii_Read.setOnClickListener(new CkWithUii_ReadClickListener());
		BtUii_Read.setOnClickListener(new BtUii_ReadClickListener());
		BtRead.setOnClickListener(new BtReadClickListener());
		SpinnerOption_Read.setOnItemSelectedListener(new SpinnerOption_ReadSelectedListener());
		SpinnerBank_Read.setOnItemSelectedListener(new SpinnerBank_ReadSelectedListener());
		SpinnerBank2_Read.setOnItemSelectedListener(new SpinnerBank2_ReadSelectedListener());
	}
	
	public class BtUii_ReadClickListener implements OnClickListener{

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
				EtTagUii_Read.setText(uiiStr);
			}else{
				EtTagUii_Read.setText("");
				Toast.makeText(ReadSetting.this, "读取标签号失败", 0).show();
			}
		}
	}
	
	public class BtReadClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			String ptrStr = EtPtr_Read.getText().toString().trim();
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
					bPtr_1[0] = (byte) (Integer.parseInt(ptrStr)>>7 | 0x80);
					bPtr_1[1] = (byte) (Integer.parseInt(ptrStr) & 0x7F);
				}else{
					bPtr_1[0] = (byte) (Integer.parseInt(ptrStr));
				}
			}
			
			String cntStr = EtLen_Read.getText().toString().trim();
			if(cntStr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "长度不能为空", 0).show();
				return;
			}else if(!fun.isDecimal(cntStr)){
				Toast.makeText(getApplicationContext(), "长度必须为十进制数据", 0).show();
				return;
			}else{
				bCnt_1 = Byte.parseByte(cntStr);
			}
			
			String pwdStr = EtAccessPwd_Read.getText().toString().trim();
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
			
			if(optionRead.equals("防碰撞读（2级）")){
				String ptrStr_2 = EtPtr2_Read.getText().toString().trim();
				if(ptrStr_2.equals(""))
				{
					Toast.makeText(getApplicationContext(), "地址不能为空", 0).show();
					return;
				}else if(!fun.isDecimal(ptrStr_2)){
					Toast.makeText(getApplicationContext(), "地址必须为十进制数据", 0).show();
					return;
				}else{
					if(Integer.parseInt(ptrStr_2)>127)
					{
						bPtr_2[0] = (byte) (Integer.parseInt(ptrStr_2)>>7 | 0x80);
						bPtr_2[1] = (byte) (Integer.parseInt(ptrStr_2) & 0x7F);
					}else{
						bPtr_2[0] = (byte) (Integer.parseInt(ptrStr_2));
						Log.v("BreakPoint", "bPtr_2_0:" + bPtr_2[0]);
					}
					
					Log.v("BreakPoint", "bPtr_2_1:" + bPtr_2[0]);
				}
				
				String cntStr_2 = EtLen2_Read.getText().toString().trim();
				if(cntStr_2.equals(""))
				{
					Toast.makeText(getApplicationContext(), "长度不能为空", 0).show();
					return;
				}else if(!fun.isDecimal(cntStr_2)){
					Toast.makeText(getApplicationContext(), "长度必须为十进制数据", 0).show();
					return;
				}else{
					bCnt_2 = Byte.parseByte(cntStr_2);
				}
			}
			if(optionRead.equals("单次读")){
				
				if(CkWithUii_Read.isChecked())//指定标签
				{
				    String uiiStr = EtTagUii_Read.getText().toString().trim();
					if(uiiStr.equals(""))
					{
						Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
						return;
					}else{
						bUii = fun.HexStringToBytes(uiiStr);
					}
					
					if(moduleControl.UhfReadDataByEPC(bAccessPwd, bBank_1, bPtr_1, bCnt_1, bUii, bReadData, bErrorCode, flagCrc))
					{
						String dataStr = fun.bytesToHexString(bReadData, bCnt_1*2);
						EtData_Read.setText(dataStr);
					}else{
						Toast.makeText(getApplicationContext(), "读取数据失败", 0).show();
					}
				}else{
					if(moduleControl.UhfReadDataFromSingleTag(bAccessPwd, bBank_1, bPtr_1, bCnt_1, bReadData, reUii, bLenUii, bErrorCode, flagCrc))
					{
						String dataStr = fun.bytesToHexString(bReadData, bCnt_1*2);
						String uiiStr = fun.bytesToHexString(reUii, bLenUii[0]);
						EtData_Read.setText("uii: " + uiiStr + "\n" + "数据：" + dataStr);
					}else{
						Toast.makeText(getApplicationContext(), "读取数据失败", 0).show();
					}
				}
			}else if(optionRead.equals("循环读")){
				Intent intent = new Intent();
				if(CkWithUii_Read.isChecked())
				{
					String uiiStr = EtTagUii_Read.getText().toString().trim();
					if(uiiStr.equals(""))
					{
						Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
						return;
					}
					intent.putExtra("uiiStr", uiiStr);
				}
				
				intent.putExtra("optionRead", "循环读");			
				intent.putExtra("bBank_1", bBank_1);
				intent.putExtra("bPtr_1", bPtr_1);
				intent.putExtra("bCnt_1", bCnt_1);
				intent.putExtra("bAccessPwd", bAccessPwd);
				intent.setClass(ReadSetting.this, ReadDataList.class);
				startActivity(intent);
				
			}else if(optionRead.equals("防碰撞读（1级）")){
				Intent intent = new Intent();
				
				intent.putExtra("optionRead", "防碰撞读（1级）");			
				intent.putExtra("bBank_1", bBank_1);
				intent.putExtra("bPtr_1", bPtr_1);
				intent.putExtra("bCnt_1", bCnt_1);
				intent.putExtra("bAccessPwd", bAccessPwd);
				intent.setClass(ReadSetting.this, ReadDataList.class);
				startActivity(intent);
			}else{
				Intent intent = new Intent();
				
				intent.putExtra("optionRead", "防碰撞读（2级）");			
				intent.putExtra("bBank_1", bBank_1);
				intent.putExtra("bPtr_1", bPtr_1);
				intent.putExtra("bCnt_1", bCnt_1);
				intent.putExtra("bBank_2", bBank_2);
				intent.putExtra("bPtr_2", bPtr_2);
				intent.putExtra("bCnt_2", bCnt_2);
				intent.putExtra("bAccessPwd", bAccessPwd);
				intent.setClass(ReadSetting.this, ReadDataList.class);
				startActivity(intent);
			}
		}
	}
	
	public class CkWithUii_ReadClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			EtTagUii_Read.setText("");
			
			if(CkWithUii_Read.isChecked())
			{
				BtUii_Read.setEnabled(true);
			}else{
				BtUii_Read.setEnabled(false);
			}
		}
	}
	
	public class SpinnerOption_ReadSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			optionRead = (String) SpinnerOption_Read.getSelectedItem();
			if(optionRead.equals("单次读")){

				SpinnerBank2_Read.setEnabled(false);
				EtPtr2_Read.setEnabled(false);
				EtLen2_Read.setEnabled(false);
			}else if(optionRead.equals("循环读")){

				SpinnerBank2_Read.setEnabled(false);
				EtPtr2_Read.setEnabled(false);
				EtLen2_Read.setEnabled(false);
			}else if(optionRead.equals("防碰撞读（1级）")){

				SpinnerBank2_Read.setEnabled(false);
				EtPtr2_Read.setEnabled(false);
				EtLen2_Read.setEnabled(false);
			}else{

				SpinnerBank2_Read.setEnabled(true);
				EtPtr2_Read.setEnabled(true);
				EtLen2_Read.setEnabled(true);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
	
	public class SpinnerBank_ReadSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				bBank_1 = 0x00;
			}else if(position == 1){
				bBank_1 = 0x01;
			}else if(position == 2){
				bBank_1 = 0x02;
			}else{
				bBank_1 = 0x03;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
	
	public class SpinnerBank2_ReadSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				bBank_2 = 0x00;
			}else if(position == 1){
				bBank_2 = 0x01;
			}else if(position == 2){
				bBank_2 = 0x02;
			}else{
				bBank_2 = 0x03;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
}
