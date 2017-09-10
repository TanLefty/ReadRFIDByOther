package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class FrequencySetting extends Activity {

	Function fun = new Function();
	ModuleControl moduleControl = new ModuleControl();
	
	private static byte flagCrc;
	private static boolean connFlag;
	private byte bMode;
	private byte bBase;
	private byte bSpace;
	private byte bHop;
	
	Button BtSetPower;
    Button BtGetPower;
    Button BtSetFre;
    Button BtGetFre;
    Button BtAddI;
    Button BtReduceI;
    Button BtAddD;
    Button BtReduceD;
    Button BtCalRange;
    Spinner SpinnerMode;
    Spinner SpinnerBase;
    Spinner SpinnerSpace;
    Spinner SpinnerHop;
    EditText EtPower;
    EditText EtFreRange;
    EditText EtStartFreI;
    EditText EtStartFreD;
    EditText EtBase;
    EditText EtChannelCount;
    EditText EtSpace;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frequency);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		BtSetPower = (Button)findViewById(R.id.BtSetPower);
	    BtGetPower = (Button)findViewById(R.id.BtGetPower);
	    BtSetFre = (Button)findViewById(R.id.BtSetFre);
	    BtGetFre = (Button)findViewById(R.id.BtGetFre);
	    BtAddI = (Button)findViewById(R.id.BtAddI);
	    BtReduceI = (Button)findViewById(R.id.BtReduceI);
	    BtAddD = (Button)findViewById(R.id.BtAddD);
	    BtReduceD = (Button)findViewById(R.id.BtReduceD);
	    BtCalRange = (Button)findViewById(R.id.BtCalRange);
	    
	    SpinnerMode = (Spinner)findViewById(R.id.SpinnerMode);
	    SpinnerBase = (Spinner)findViewById(R.id.SpinnerBase);
	    SpinnerSpace = (Spinner)findViewById(R.id.SpinnerSpace);
	    SpinnerHop = (Spinner)findViewById(R.id.SpinnerHop);
	    
	    EtFreRange = (EditText)findViewById(R.id.EtFreRange);
	    EtBase = (EditText)findViewById(R.id.EtBase);
	    EtChannelCount = (EditText)findViewById(R.id.EtChannelCount);
	    EtSpace = (EditText)findViewById(R.id.EtSpace);
	    EtStartFreI = (EditText)findViewById(R.id.EtStartFreI);
	    EtStartFreD = (EditText)findViewById(R.id.EtStartFreD);
	    
	    EtFreRange.setKeyListener(null);
	    EtBase.setKeyListener(null);
	    EtSpace.setKeyListener(null);
	    EtStartFreI.setKeyListener(null);
	    EtStartFreD.setKeyListener(null);
	    
        SpinnerMode.setOnItemSelectedListener(new ModeItemSelectedListener());
        SpinnerBase.setOnItemSelectedListener(new BaseItemSelectedListener());
        SpinnerSpace.setOnItemSelectedListener(new SpaceItemSelectedListener());
        SpinnerHop.setOnItemSelectedListener(new HopItemSelectedListener());
        
        BtSetFre.setOnClickListener(new SetFreOnclickListener());
        BtGetFre.setOnClickListener(new GetFreOnclickListener());
        BtAddI.setOnClickListener(new AddIOnclickListener());
        BtReduceI.setOnClickListener(new ReduceIOnclickListener());
        BtAddD.setOnClickListener(new AddDOnclickListener());
        BtReduceD.setOnClickListener(new ReduceDOnclickListener());
        BtCalRange.setOnClickListener(new CalFreDOnclickListener());
	}
	
	public class ModeItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			if(SpinnerMode.getSelectedItemPosition() == 0){
				bMode = 0;
			}else if(SpinnerMode.getSelectedItemPosition() == 1){
				bMode = 1;
			}else if(SpinnerMode.getSelectedItemPosition() == 2){
				bMode = 2;
			}else  if(SpinnerMode.getSelectedItemPosition() == 3){
				bMode = 3;
			}else{
				bMode = 4;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
	
	public class BaseItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			if(SpinnerBase.getSelectedItemPosition() == 0){
				bBase = 0;
				EtBase.setText("50");
				EtStartFreD.setText("50");
				EtSpace.setText("50");
			}else{
				bBase = 1;
				EtBase.setText("125");
				EtStartFreD.setText("125");
				EtSpace.setText("125");
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
	
	public class SpaceItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			bSpace = Byte.valueOf((String) SpinnerSpace.getSelectedItem(), 10);
			int temp = Integer.parseInt(EtBase.getText().toString(), 10);
			EtSpace.setText("" + (bSpace*temp));
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
	
	public class HopItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
				
			if(SpinnerHop.getSelectedItemPosition() == 0){
				
				bHop = 0;
			}else if(SpinnerMode.getSelectedItemPosition() == 1){
				
				bHop = 1;
			}else{
				bHop = 2;
			}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
	
	public class GetFreOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(FrequencySetting.this, "请先进行连接", 0).show();
				return;
			}
			
			byte[] bFreMode = new byte[1];
			byte[] bFreBase = new byte[1];
			byte[] bBaseFre = new byte[2];
			byte[] bChannNum = new byte[1];
			byte[] bChannSpc = new byte[1];
			byte[] bFreHop = new byte[1];
			String freStr;
			if(moduleControl.UhfGetFrequency(bFreMode, bFreBase, bBaseFre, bChannNum, bChannSpc, bFreHop, flagCrc)){
				
//				String freBase0 = Integer.toHexString((int)bBaseFre[0]);
//				String freBase1 = Integer.toHexString((int)bBaseFre[1]);
//				
//				short iFreBase0 = Short.parseShort(freBase0, 16);
//				short iFreBase1 = Short.parseShort(freBase1, 16);
				
				int iFreBase0 = bBaseFre[0] & 0xFF;
				int iFreBase1 = bBaseFre[1] & 0xFF;
				
				int freI = (iFreBase0 << 3) + (iFreBase1 >> 5);
				int freD = 0;
				int eFreD = 0;
				int eFreI = 0;
				
				if(bFreBase[0] == 0){
					//freD = bBaseFre[1]*50;
					freD = (iFreBase1& 0x1F)*50;
					eFreD = (freD + bChannSpc[0] * 50 * (bChannNum[0]-1))%1000;
					eFreI = freI + (freD + bChannSpc[0] * 50 * (bChannNum[0]-1))/1000;
					
				}else{
					//freD = bBaseFre[1]*125;
					freD = (iFreBase1& 0x1F)*125;
					eFreD = (freD + bChannSpc[0] * 125 * (bChannNum[0]-1))%1000;
					eFreI = freI + (freD + bChannSpc[0] * 125 * (bChannNum[0]-1))/1000;
				}
				
				freStr = String.valueOf(freI) + "." + String.valueOf(freD) + "~" + String.valueOf(eFreI) +"." + String.valueOf(eFreD) + "MHz";
				EtFreRange.setText(freStr);
				Toast.makeText(getApplicationContext(), "读取频率成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "读取频率失败", 0).show();
			}
		}
		
	}
	
	public class SetFreOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(FrequencySetting.this, "请先进行连接", 0).show();
				return;
			}
			
			byte bFreMode;
			byte bFreBase;
			byte bChannNum;
			byte bChannSpc;
			byte bFreHop;
			byte[] bBaseFre = new byte[2];
			
			if(bMode == 0)
			{
				if(moduleControl.UhfSetFrequency((byte)0, (byte)0, bBaseFre, (byte)0, (byte)0, (byte)0, flagCrc))
				{
					Toast.makeText(FrequencySetting.this, "设置频率成功", 1).show();
				}else{
					Toast.makeText(FrequencySetting.this, "设置频率失败", 1).show();
				}
			}else if(bMode == 1){
				if(moduleControl.UhfSetFrequency((byte)1, (byte)0, bBaseFre, (byte)0, (byte)0, (byte)0, flagCrc))
				{
					Toast.makeText(FrequencySetting.this, "设置频率成功", 1).show();
				}else{
					Toast.makeText(FrequencySetting.this, "设置频率失败", 1).show();
				}
			}else if(bMode == 2){
				if(moduleControl.UhfSetFrequency((byte)2, (byte)0, bBaseFre, (byte)0, (byte)0, (byte)0, flagCrc))
				{
					Toast.makeText(FrequencySetting.this, "设置频率成功", 1).show();
				}else{
					Toast.makeText(FrequencySetting.this, "设置频率失败", 1).show();
				}
			}else if(bMode == 3){
				if(moduleControl.UhfSetFrequency((byte)3, (byte)0, bBaseFre, (byte)0, (byte)0, (byte)0, flagCrc))
				{
					Toast.makeText(FrequencySetting.this, "设置频率成功", 1).show();
				}else{
					Toast.makeText(FrequencySetting.this, "设置频率失败", 1).show();
				}
			}else{
				
				long IFre = Integer.parseInt(EtStartFreI.getText().toString(), 10);
				long DFre = Integer.parseInt(EtStartFreD.getText().toString(), 10);
				bBaseFre[0] = (byte) ((IFre <<5)>>8);
				if(bBase == 0)
				{
					bBaseFre[1] = (byte) ((IFre <<5) | ((DFre/50) & 0x1F));
				}else{
					bBaseFre[1] = (byte) ((IFre <<5) | ((DFre/125) & 0x1F));
				}
				
				bFreMode = (byte)4;
				bFreBase = bBase;
				bChannNum = Byte.parseByte(EtChannelCount.getText().toString(), 10);
				bChannSpc = bSpace;
				bFreHop = bHop;
				
				if(moduleControl.UhfSetFrequency(bFreMode, bFreBase, bBaseFre, bChannNum, bChannSpc, bFreHop, flagCrc))
				{
					Toast.makeText(FrequencySetting.this, "设置频率成功", 1).show();
				}else{
					Toast.makeText(FrequencySetting.this, "设置频率失败", 1).show();
				}
			}
		}
	}
	
	public class AddIOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int startI = Integer.parseInt(EtStartFreI.getText().toString(), 10);
			if(startI<960)
			{
				startI = startI + 1;
				EtStartFreI.setText(startI+"");
			}else{
				Toast.makeText(FrequencySetting.this, "起始频率整数应不大于960MHz", 0).show();
			}
		}
	}
	
	public class ReduceIOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int startI = Integer.parseInt(EtStartFreI.getText().toString(), 10);
			if(startI>840)
			{
				startI = startI - 1;
				EtStartFreI.setText(startI+"");
			}else{
				Toast.makeText(FrequencySetting.this, "起始频率整数应不小于840MHz", 0).show();
			}
		}
	}
	
	public class AddDOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int startD = Integer.parseInt(EtStartFreD.getText().toString(), 10);
			if(bBase == 0)
			{
				startD = (startD + 50)%1000;
				EtStartFreD.setText(""+startD);
			}else{
				startD = (startD + 125)%1000;
				EtStartFreD.setText(""+startD);
			}
		}
	}
	
	public class ReduceDOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int startD = Integer.parseInt(EtStartFreD.getText().toString(), 10);
			if(bBase == 0)
			{
				if(startD>0)
				{
					startD = startD - 50;
					EtStartFreD.setText(""+startD);
				}else{
					Toast.makeText(FrequencySetting.this, "起始频率小数部分应不小于0", 0).show();
				}
				
			}else{
				if(startD>0)
				{
					startD = startD - 125;
					EtStartFreD.setText(""+startD);
				}else{
					Toast.makeText(FrequencySetting.this, "起始频率小数部分应不小于0", 0).show();
				}
			}
		}
	}
	
	public class CalFreDOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			int temp = (Integer.parseInt(EtChannelCount.getText().toString(), 10) - 1) * (Integer.parseInt(EtSpace.getText().toString())) + (Integer.parseInt(EtStartFreD.getText().toString()));
			int endFreI = temp/1000 + Integer.parseInt(EtStartFreI.getText().toString());
			int endFreD = temp%1000;
			if(endFreI>960)
			{
				Toast.makeText(FrequencySetting.this, "最终频率不能大于960MHz", 0).show();
			}else{
				
				if(bBase == 0)
				{
					if(temp>12000)
					{
						Toast.makeText(FrequencySetting.this, "频道带宽不能大于12MHz", 0).show();
					}else{
						
						String rangeStr = EtStartFreI.getText().toString() + "." + EtStartFreD.getText().toString() + "~" + String.valueOf(endFreI) + String.valueOf(endFreD) + "MHz";
						EtFreRange.setText(rangeStr);
					}
				}else{
					if(temp>32000)
					{
						Toast.makeText(FrequencySetting.this, "频道带宽不能大于32MHz", 0).show();
					}else{
						
						String rangeStr = EtStartFreI.getText().toString() + "." + EtStartFreD.getText().toString() + "~" + String.valueOf(endFreI) + "." + String.valueOf(endFreD) + "MHz";
						EtFreRange.setText(rangeStr);
					}
				}
			}
		}
	}
}
