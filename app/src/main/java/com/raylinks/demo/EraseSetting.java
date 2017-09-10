package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class EraseSetting extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	
	private static byte flagCrc;
	private static boolean connFlag;
	
	byte bBank; 
	byte[] bPtr = new byte[2]; 
	byte bCnt;
	byte[] bAccessPwd = {0x00, 0x00, 0x00, 0x00}; 
	byte[] bUii = new byte[255];
	byte[] bErrorCode = new byte[1];
	
	CheckBox CkWithUii_Erase;
	EditText EtTagUii_Erase;
	Spinner SpinnerBank_Erase;
	EditText EtPtr_Erase;
	EditText EtLen_Erase;
	EditText EtAccessPwd_Erase;
	Button BtUii_Erase;
	Button BtErase;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.erase);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		CkWithUii_Erase = (CheckBox)findViewById(R.id.CkWithUii_Erase);
		EtTagUii_Erase = (EditText)findViewById(R.id.EtTagUii_Erase);
		SpinnerBank_Erase = (Spinner)findViewById(R.id.SpinnerBank_Erase);
		EtPtr_Erase = (EditText)findViewById(R.id.EtPtr_Erase);
		EtLen_Erase = (EditText)findViewById(R.id.EtLen_Erase);
		EtAccessPwd_Erase = (EditText)findViewById(R.id.EtAccessPwd_Erase);
		BtUii_Erase = (Button)findViewById(R.id.BtUii_Erase);
		BtErase = (Button)findViewById(R.id.BtErase);
		
		EtTagUii_Erase.setKeyListener(null);
		BtUii_Erase.setEnabled(false);
		CkWithUii_Erase.setOnClickListener(new CkWithUii_ReadClickListener());
		BtUii_Erase.setOnClickListener(new BtUii_EraseClickListener());
		SpinnerBank_Erase.setOnItemSelectedListener(new SpinnerBank_EraseSelectedListener());
		BtErase.setOnClickListener(new BtEraseClickListener());
	}
	
	public class BtUii_EraseClickListener implements OnClickListener{

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
				EtTagUii_Erase.setText(uiiStr);
			}else{
				EtTagUii_Erase.setText("");
				Toast.makeText(EraseSetting.this, "读取标签号失败", 0).show();
			}
		}
	}

	public class BtEraseClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(EraseSetting.this, "请先进行连接", 0).show();
				return;
			}
			
			String ptrStr = EtPtr_Erase.getText().toString().trim();
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
			}
			
			String cntStr = EtLen_Erase.getText().toString().trim();
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
			
			String pwdStr = EtAccessPwd_Erase.getText().toString().trim();
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
			
			if(CkWithUii_Erase.isChecked())//指定标签
			{
			    String uiiStr = EtTagUii_Erase.getText().toString().trim();
				if(uiiStr.equals(""))
				{
					Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
					return;
				}else{
					bUii = fun.HexStringToBytes(uiiStr);
				}
				if(moduleControl.UhfEraseDataByEPC(bAccessPwd, bBank, bPtr, bCnt, bUii, bErrorCode, flagCrc))
				{
					Toast.makeText(getApplicationContext(), "擦除数据成功", 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "擦除数据失败", 0).show();
				}
				
			}else{
				if(moduleControl.UhfEraseDataFromSingleTag(bAccessPwd, bBank, bPtr, bCnt, bUii, bErrorCode, flagCrc))
				{
					String uiiStr = fun.bytesToHexString(bUii, ((bUii[0]>>3)+1)*2);
					Toast.makeText(getApplicationContext(), "擦除数据成功\nUII: " + uiiStr, 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "擦除数据失败", 0).show();
				}
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
			
			EtTagUii_Erase.setText("");
			
			if(CkWithUii_Erase.isChecked())
			{
				BtUii_Erase.setEnabled(true);
			}else{
				BtUii_Erase.setEnabled(false);
			}
		}
	}

	public class SpinnerBank_EraseSelectedListener implements OnItemSelectedListener{

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
}
