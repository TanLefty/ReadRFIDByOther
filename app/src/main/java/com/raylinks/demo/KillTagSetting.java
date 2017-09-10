package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class KillTagSetting extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	
	private static byte flagCrc;
	private static boolean connFlag;
	
	byte[] bKillPwd = new byte[4]; 
	byte[] bUii = new byte[255];
	byte[] bErrorCode = new byte[1];
	
	CheckBox CkWithUii_Kill;
	EditText EtTagUii_Kill;
	EditText EtKillPwd_Kill;
	Button BtUii_Kill;
	Button BtKill;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.killtag);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		CkWithUii_Kill = (CheckBox)findViewById(R.id.CkWithUii_Kill);
		EtTagUii_Kill = (EditText)findViewById(R.id.EtTagUii_Kill);
		EtKillPwd_Kill = (EditText)findViewById(R.id.EtKillPwd_Kill);
		BtUii_Kill = (Button)findViewById(R.id.BtUii_Kill);
		BtKill = (Button)findViewById(R.id.BtKill);
		
		EtTagUii_Kill.setKeyListener(null);
		BtUii_Kill.setEnabled(false);
		CkWithUii_Kill.setOnClickListener(new CkWithUii_KillClickListener());
		BtUii_Kill.setOnClickListener(new BtUii_KillClickListener());
		BtKill.setOnClickListener(new BtKillClickListener());
	}
	
	public class BtUii_KillClickListener implements OnClickListener{

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
				EtTagUii_Kill.setText(uiiStr);
			}else{
				EtTagUii_Kill.setText("");
				Toast.makeText(KillTagSetting.this, "读取标签号失败", 0).show();
			}
		}
	}

	public class BtKillClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(KillTagSetting.this, "请先进行连接", 0).show();
				return;
			}
			String killStr = EtKillPwd_Kill.getText().toString().trim();
			if(killStr.length() != 8){
				Toast.makeText(getApplicationContext(), "KillPwd的长度必须为8", 0).show();
				return;
				
			}else if(!fun.isHex(killStr)){
				Toast.makeText(getApplicationContext(), "KillPwd必须为十六进制数据", 0).show();
				return;
			}else{
				bKillPwd = fun.HexStringToBytes(killStr);
			}
			
			if(CkWithUii_Kill.isChecked())//指定标签
			{
				String uiiStr = EtTagUii_Kill.getText().toString().trim();
				if(uiiStr.equals(""))
				{
					Toast.makeText(getApplicationContext(), "标签号不能为空", 0).show();
					return;
				}else{
					bUii = fun.HexStringToBytes(uiiStr);
				}
				
				if(moduleControl.UhfKillTagByEPC(bKillPwd, bUii, bErrorCode, flagCrc))
				{
					Toast.makeText(getApplicationContext(), "销毁标签成功", 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "销毁标签失败", 0).show();
				}
			}else{
				if(moduleControl.UhfKillSingleTag(bKillPwd, bUii, bErrorCode, flagCrc))
				{
					String uiiStr = fun.bytesToHexString(bUii, ((bUii[0]>>3)+1)*2);
					Toast.makeText(getApplicationContext(), "销毁标签成功\nUII: "+ uiiStr, 0).show();
				}else{
					Toast.makeText(getApplicationContext(), "销毁标签失败", 0).show();
				}
			}
		}
	}
	
	public class CkWithUii_KillClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			EtTagUii_Kill.setText("");
			
			if(CkWithUii_Kill.isChecked())
			{
				BtUii_Kill.setEnabled(true);
			}else{
				BtUii_Kill.setEnabled(false);
			}
		}
	}
}
