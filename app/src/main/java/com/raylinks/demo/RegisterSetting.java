package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RegisterSetting extends Activity {

	Function fun = new Function();
	ModuleControl moduleControl = new ModuleControl();
	
	private static byte flagCrc;
	private static boolean connFlag;
	private static int RADD;
	private static int RLEN;
	private static byte[] bRegData = new byte[1];
	private static byte[] bStatus = new byte[1];
	private static int timer;
	
	EditText EtRegAddr;
	EditText EtRegLen;
	EditText EtRegData;
	EditText EtTimer;
	Spinner SpinnerBuzzer;
	Button BtSetRegister;
	Button BtGetRegister;
	Button BtSetBeep;
	Button BtSetTimer;
	Button BtSaveRegister;
	Button BtResetRegister;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		EtRegAddr = (EditText)findViewById(R.id.EtRegAddr);
		EtRegLen = (EditText)findViewById(R.id.EtRegLen);
		EtRegData = (EditText)findViewById(R.id.EtRegData);
		SpinnerBuzzer = (Spinner)findViewById(R.id.SpinnerBuzzer);
		EtTimer = (EditText)findViewById(R.id.EtTimer);
		BtSetRegister = (Button)findViewById(R.id.BtSetRegister);
		BtGetRegister = (Button)findViewById(R.id.BtGetRegister);
		BtSetBeep = (Button)findViewById(R.id.BtSetBeep);
		BtSetTimer = (Button)findViewById(R.id.BtSetTimer);
		BtSaveRegister = (Button)findViewById(R.id.BtSaveRegister);
		BtResetRegister = (Button)findViewById(R.id.BtResetRegister);
		EtRegLen.setKeyListener(null);
		
		BtSetRegister.setOnClickListener(new BtSetRegisterClickListener());
		BtGetRegister.setOnClickListener(new BtGetRegisterClickListener());
		
		BtSetBeep.setOnClickListener(new BtSetBeepClickListener());
		BtSetTimer.setOnClickListener(new BtSetTimerClickListener());
		
		BtSaveRegister.setOnClickListener(new BtSaveRegisterClickListener());
		BtResetRegister.setOnClickListener(new BtResetRegisterClickListener());
	}
	
	public class BtSetRegisterClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(RegisterSetting.this, "请先进行连接", 0).show();
				return;
			}
			String regAddr = EtRegAddr.getText().toString().trim();
			if(regAddr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "地址不能为空", 0).show();
				return;
			}
			if(!fun.isHex(regAddr))
			{
				Toast.makeText(getApplicationContext(), "地址必须为3位16进制数据", 0).show();
				return;
			}else{
				RADD = Integer.parseInt(regAddr, 16);
			}
			if(RADD<0 || RADD>511)
			{
				Toast.makeText(getApplicationContext(), "地址范围必须为0x000~0x1FF", 0).show();
				return;
			}

			String regLen = EtRegLen.getText().toString().trim();
			if(regLen.equals(""))
			{
				Toast.makeText(getApplicationContext(), "长度不能为空", 0).show();
				return;
			}
			if(!fun.isDecimal(regLen))
			{
				Toast.makeText(getApplicationContext(), "长度必须为10进制数据", 0).show();
				return;
			}else{
				RLEN = Integer.parseInt(regLen);
			}
			if((RADD+RLEN)>511)
			{
				Toast.makeText(getApplicationContext(), "指定长度过长", 0).show();
				return;
			}
			
			String regData = EtRegData.getText().toString().trim();
			if(regData.equals(""))
			{
				Toast.makeText(getApplicationContext(), "数据不能为空", 0).show();
				return;
			}
			if(!fun.isHex(regData))
			{
				Toast.makeText(getApplicationContext(), "数据必须为16进制数据", 0).show();
				return;
			}else{
				bRegData[0] = (byte) Integer.parseInt(regData, 16);
			}
						
			if(moduleControl.UhfSetRegister(RADD, RLEN, bRegData, bStatus, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "设置寄存器成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "设置寄存器失败", 0).show();
			}
		}
	}
	
	public class BtGetRegisterClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(RegisterSetting.this, "请先进行连接", 0).show();
				return;
			}
			EtRegData.setText("");
			String regAddr = EtRegAddr.getText().toString().trim();
			if(regAddr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "地址不能为空", 0).show();
				return;
			}
			if(!fun.isHex(regAddr))
			{
				Toast.makeText(getApplicationContext(), "地址必须为16进制数据", 0).show();
				return;
			}else{
				RADD = Integer.parseInt(regAddr, 16);
			}
			if(RADD<0 || RADD>511)
			{
				Toast.makeText(getApplicationContext(), "地址范围必须为0x000~0x1FF", 0).show();
				return;
			}

			String regLen = EtRegLen.getText().toString().trim();
			if(regLen.equals(""))
			{
				Toast.makeText(getApplicationContext(), "长度不能为空", 0).show();
				return;
			}
			if(!fun.isDecimal(regLen))
			{
				Toast.makeText(getApplicationContext(), "长度必须为10进制数据", 0).show();
				return;
			}else{
				RLEN = Integer.parseInt(regLen);
			}
			if((RADD+RLEN)>511)
			{
				Toast.makeText(getApplicationContext(), "指定长度过长", 0).show();
				return;
			}
			byte[] bReg = new byte[1];
			if(moduleControl.UhfGetRegister(RADD, RLEN, bStatus, bReg, flagCrc))
			{
				String regStr = fun.byteToHexString(bReg[0]);
				EtRegData.setText(regStr);
				Toast.makeText(getApplicationContext(), "读取寄存器成功\n寄存器值为：" + regStr, 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "读取寄存器失败", 0).show();
			}
		}
	}
	
	public class BtSetBeepClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(RegisterSetting.this, "请先进行连接", 0).show();
				return;
			}
			if(SpinnerBuzzer.getSelectedItemPosition() == 0)
			{
				bRegData[0] = 0;
			}else{
				bRegData[0] = 1;
			}
			if(moduleControl.UhfSetRegister(288, 1, bRegData, bStatus, flagCrc))
			{
				Toast.makeText(RegisterSetting.this, "设置蜂鸣器成功", 0).show();
			}else{
				Toast.makeText(RegisterSetting.this, "设置蜂鸣器失败", 0).show();
			}
		}
	}
	
	public class BtSetTimerClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(RegisterSetting.this, "请先进行连接", 0).show();
				return;
			}
			String timerStr = EtTimer.getText().toString().trim();
			if(timerStr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "Timer不能为空", 0).show();
				return;
			}
			if(!fun.isDecimal(timerStr))
			{
				Toast.makeText(getApplicationContext(), "Timer必须为10进制数据", 0).show();
				return;
			}else{
				timer = Integer.parseInt(timerStr);
			}
			if(timer>65535 || timer<600)
			{
				Toast.makeText(getApplicationContext(), "Timer值越界，Timer的取值范围为600~65535", 0).show();
				return;
			}
			
			byte[] bTimer = new byte[2];
			bTimer[0] = (byte)(timer >> 8);
            bTimer[1] = (byte)timer;
            
			if(moduleControl.UhfSetRegister(289, 2, bTimer, bStatus, flagCrc))
			{
				Toast.makeText(RegisterSetting.this, "设置Timer成功", 0).show();
			}else{
				Toast.makeText(RegisterSetting.this, "设置Timer失败", 0).show();
			}
			
		}
	}
	
	public class BtSaveRegisterClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(RegisterSetting.this, "请先进行连接", 0).show();
				return;
			}
			if(moduleControl.UhfSaveRegister(flagCrc))
			{
				Toast.makeText(getApplicationContext(), "保存寄存器成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "保存寄存器失败", 0).show();
			}
		}
	}
	public class BtResetRegisterClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(RegisterSetting.this, "请先进行连接", 0).show();
				return;
			}
			if(moduleControl.UhfResetRegister(flagCrc))
			{
				Toast.makeText(getApplicationContext(), "恢复寄存器默认值成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "恢复寄存器默认值失败", 0).show();
			}
		}
	}
}
