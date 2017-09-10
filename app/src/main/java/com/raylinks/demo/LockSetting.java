package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class LockSetting extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	
	private static byte flagCrc;
	private static boolean connFlag;
	private byte bkill = 0x00;
	private byte baccess = 0x00;
	private byte buii = 0x00;
	private byte btid = 0x00;
	private byte buser = 0x00;
	
	byte[] bAccessPwd = new byte[4]; 
	byte[] bUii = new byte[255];
	byte[] bLockData = new byte[3]; 
	byte[] bErrorCode = new byte[1];
	
	CheckBox CkWithUii_Lock;
	EditText EtTagUii_Lock;
	EditText EtAccessPwd_Lock;
	Spinner SpinnerKillPwd_Lock;
	Spinner SpinnerAccessPwd_Lock;
	Spinner SpinnerUII_Lock;
	Spinner SpinnerTID_Lock;
	Spinner SpinnerUSER_Lock;
	EditText EtLockCode_Lock;
	Button BtUii_Lock;
	Button BtCreateCode;
	Button BtLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lock);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		CkWithUii_Lock = (CheckBox)findViewById(R.id.CkWithUii_Lock);
		EtTagUii_Lock = (EditText)findViewById(R.id.EtTagUii_Lock);
		EtAccessPwd_Lock = (EditText)findViewById(R.id.EtAccessPwd_Lock);
		SpinnerKillPwd_Lock = (Spinner)findViewById(R.id.SpinnerKillPwd_Lock);
		SpinnerAccessPwd_Lock = (Spinner)findViewById(R.id.SpinnerAccessPwd_Lock);
		SpinnerUII_Lock = (Spinner)findViewById(R.id.SpinnerUII_Lock);
		SpinnerTID_Lock = (Spinner)findViewById(R.id.SpinnerTID_Lock);
		SpinnerUSER_Lock = (Spinner)findViewById(R.id.SpinnerUSER_Lock);
		EtLockCode_Lock = (EditText)findViewById(R.id.EtLockCode_Lock);
		BtUii_Lock = (Button)findViewById(R.id.BtUii_Lock);
		BtCreateCode = (Button)findViewById(R.id.BtCreateCode);
		BtLock = (Button)findViewById(R.id.BtLock);
		EtLockCode_Lock.setEnabled(false);
		
		EtTagUii_Lock.setKeyListener(null);
		BtUii_Lock.setEnabled(false);
		CkWithUii_Lock.setOnClickListener(new CkWithUii_LockClickListener());
		SpinnerKillPwd_Lock.setOnItemSelectedListener(new SpinnerKillPwd_LockSelectedListener());
		SpinnerAccessPwd_Lock.setOnItemSelectedListener(new SpinnerAccessPwd_LockSelectedListener());
		SpinnerUII_Lock.setOnItemSelectedListener(new SpinnerUII_LockSelectedListener());
		SpinnerTID_Lock.setOnItemSelectedListener(new SpinnerUII_LockSelectedListener());
		SpinnerUSER_Lock.setOnItemSelectedListener(new SpinnerUSER_LockSelectedListener());
		BtUii_Lock.setOnClickListener(new BtUii_LockClickListener());
		BtCreateCode.setOnClickListener(new BtCreateCodeClickListener());
		BtLock.setOnClickListener(new BtLockClickListener());
	}
	
	public class BtUii_LockClickListener implements OnClickListener{

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
				EtTagUii_Lock.setText(uiiStr);
			}else{
				EtTagUii_Lock.setText("");
				Toast.makeText(LockSetting.this, "读取标签号失败", 0).show();
			}
		}
	}

	public class CkWithUii_LockClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			EtTagUii_Lock.setText("");
			
			if(CkWithUii_Lock.isChecked())
			{
				BtUii_Lock.setEnabled(true);
			}else{
				BtUii_Lock.setEnabled(false);
			}
		}
	}

	public class SpinnerKillPwd_LockSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				bkill = 0x00;
			}else if(position == 1){
				bkill = 0x01;
			}else if(position == 2){
				bkill = 0x02;
			}else if(position == 3){
				bkill = 0x03;
			}else{
				bkill = 0x04;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
	
	public class BtCreateCodeClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(LockSetting.this, "请先进行连接", 0).show();
				return;
			}
			if(fun.LockGenCode(bkill, baccess, buii, btid, buser, bLockData))
			{
				String lockCodeStr = fun.bytesToHexString(bLockData, 3);
				EtLockCode_Lock.setText(lockCodeStr);
			}else{
				Toast.makeText(getApplicationContext(), "生成锁定码失败", 0);
			}
		}
	}
	
	public class BtLockClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(LockSetting.this, "请先进行连接", 0).show();
				return;
			}
			String AccessStr = EtAccessPwd_Lock.getText().toString().trim();
			if(!AccessStr.equals(""))
			{
				if(AccessStr.length() != 8){
					Toast.makeText(getApplicationContext(), "AccessPwd的长度必须为8", 0).show();
					return;
				}else if(!fun.isHex(AccessStr)){
					Toast.makeText(getApplicationContext(), "AccessPwd必须为十六进制数据", 0).show();
					return;
				}else{
					bAccessPwd = fun.HexStringToBytes(AccessStr);
				}
			}else{
				bAccessPwd[0] = 0x00;
				bAccessPwd[1] = 0x00;
				bAccessPwd[2] = 0x00;
				bAccessPwd[3] = 0x00;
			}
			
			
			if(CkWithUii_Lock.isChecked())//指定标签
			{
				String uiiStr = EtTagUii_Lock.getText().toString().trim();
				if(uiiStr.equals(""))
				{
					Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
					return;
				}else{
					bUii = fun.HexStringToBytes(uiiStr);
				}
				
				String lockCodeStr = EtLockCode_Lock.getText().toString().trim();
				if(lockCodeStr.equals(""))
				{
					Toast.makeText(getApplicationContext(), "锁定码不能为空", 0).show();
					return;
				}
				
				if(moduleControl.UhfLockMemByEPC(bAccessPwd, bLockData, bUii, bErrorCode, flagCrc))
				{
					Toast.makeText(getApplicationContext(), "锁定成功", 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "锁定失败", 0).show();
				}
			}else{
				String lockCodeStr = EtLockCode_Lock.getText().toString().trim();
				if(lockCodeStr.equals(""))
				{
					Toast.makeText(getApplicationContext(), "锁定码不能为空", 0).show();
					return;
				}
				
				if(moduleControl.UhfLockMemFromSingleTag(bAccessPwd, bLockData, bUii, bErrorCode, flagCrc))
				{
					String uiiStr = fun.bytesToHexString(bUii, ((bUii[0]>>3)+1)*2);
					Log.v("BreakPoint", "bUii[0]" + bUii[0]);
					Toast.makeText(getApplicationContext(), "锁定成功\nUII:" + uiiStr, 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "锁定失败", 0).show();
				}
			}
		}
	}
	
	public class SpinnerAccessPwd_LockSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				baccess = 0x00;
			}else if(position == 1){
				baccess = 0x01;
			}else if(position == 2){
				baccess = 0x02;
			}else if(position == 3){
				baccess = 0x03;
			}else{
				baccess = 0x04;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
	
	public class SpinnerUII_LockSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				buii = 0x00;
			}else if(position == 1){
				buii = 0x01;
			}else if(position == 2){
				buii = 0x02;
			}else if(position == 3){
				buii = 0x03;
			}else{
				buii = 0x04;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
	
	public class SpinnerTID_LockSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				btid = 0x00;
			}else if(position == 1){
				btid = 0x01;
			}else if(position == 2){
				btid = 0x02;
			}else if(position == 3){
				btid = 0x03;
			}else{
				btid = 0x04;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}
	
	public class SpinnerUSER_LockSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			if(position == 0){
				buser = 0x00;
			}else if(position == 1){
				buser = 0x01;
			}else if(position == 2){
				buser = 0x02;
			}else if(position == 3){
				buser = 0x03;
			}else{
				buser = 0x04;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
						
		}
	}	
}
