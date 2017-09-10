package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PowerSetting extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	
	private static byte flagCrc;
	private static boolean connFlag;
	private static byte power;
	
	Spinner SpinnerPower;
	Button BtSetPower;
	Button BtGetPower;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.power);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		SpinnerPower = (Spinner)findViewById(R.id.SpinnerPower);
		BtSetPower = (Button)findViewById(R.id.BtSetPower);
		BtGetPower = (Button)findViewById(R.id.BtGetPower);
		SpinnerPower.setOnItemSelectedListener(new SpinnerPower_SelectedListener());
		BtSetPower.setOnClickListener(new BtSetPowerClickListener());
		BtGetPower.setOnClickListener(new BtGetPowerClickListener());
	}
	
	public class BtSetPowerClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(PowerSetting.this, "请先进行连接", 0).show();
				return;
			}
			if(moduleControl.UhfSetPower((byte) 0x01, power, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "设置功率成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "设置功率失败", 0).show();
			}
		}
	}
	
	public class BtGetPowerClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(PowerSetting.this, "请先进行连接", 0).show();
				return;
			}
			byte[] bPower = new byte[1];
			if(moduleControl.UhfGetPower(bPower, flagCrc))
			{
				byte tempPower = (byte) (bPower[0] & 0x7F);
				int position = tempPower - 5;
				SpinnerPower.setSelection(position);
				Toast.makeText(getApplicationContext(), "读取功率成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "读取功率失败", 0).show();
			}
		}
	}
	
	public class SpinnerPower_SelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			power = (byte) (position + 5);
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	}
}
