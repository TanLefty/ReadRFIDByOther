package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class OtherSetting extends Activity {

	Function fun = new Function();
	ModuleControl moduleControl = new ModuleControl();
	
	private static byte flagCrc;
	private static boolean connFlag;
	
	Button BtReadRlmInfo;
	Button BtReadUID;
	Button BtSleep;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.other);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		BtReadRlmInfo = (Button)findViewById(R.id.BtReadRlmInfo);
		BtReadUID = (Button)findViewById(R.id.BtReadUID);
		BtSleep = (Button)findViewById(R.id.BtSleep);
				
		BtReadRlmInfo.setOnClickListener(new BtReadRlmInfoClickListener());
		BtReadUID.setOnClickListener(new BtReadUIDClickListener());
		BtSleep.setOnClickListener(new BtSleepClickListener());
	}
		
	public class BtReadRlmInfoClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(OtherSetting.this, "请先进行连接", 0).show();
				return;
			}
			byte[] bSerial = new byte[6];
			byte[] bVersion = new byte[3];
			if(moduleControl.UhfGetVersion(bSerial, bVersion, flagCrc))
			{
				String serialStr = fun.bytesToHexString(bSerial, 6);
				String versionStr = fun.bytesToHexString(bVersion, 3);
				Toast.makeText(getApplicationContext(), "读取RLM信息成功\n硬件序列号："+serialStr+"\n软件版本号："+versionStr, 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "读取RLM信息失败", 0).show();
			}
		}
	}
	
	public class BtReadUIDClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(OtherSetting.this, "请先进行连接", 0).show();
				return;
			}
			
			byte[] bUid = new byte[12];
			if(moduleControl.UhfGetReaderUID(bUid, flagCrc))
			{
				String uidStr = fun.bytesToHexString(bUid, 12);
				Toast.makeText(getApplicationContext(), "读取UID信息成功\nUID: "+uidStr, 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "读取UID信息失败", 0).show();
			}
		}
	}
	
	public class BtSleepClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(OtherSetting.this, "请先进行连接", 0).show();
				return;
			}
			if(moduleControl.UhfEnterSleepMode(flagCrc))
				Toast.makeText(getApplicationContext(), "进入sleep模式成功", 0).show();
			else
				Toast.makeText(getApplicationContext(), "进入sleep模式失败", 0).show();
		}
	}

}
