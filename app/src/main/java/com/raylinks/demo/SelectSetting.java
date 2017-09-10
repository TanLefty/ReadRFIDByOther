package com.raylinks.demo;


import com.raylinks.Function;
import com.raylinks.ModuleControl;
import com.raylinks.ModuleCommand.Srecord;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SelectSetting extends Activity {

	Function fun = new Function();
	ModuleControl moduleControl = new ModuleControl();
	
	private static byte flagCrc;
	private static boolean connFlag;
	
	private static byte[] bStatus = new byte[1];
	
	EditText EtPtr_Select;
	EditText EtMask_Select;
	EditText EtNum_Read;
	EditText EtNum_choose;
	Spinner SpinnerBank_Select;
	Spinner SpinnerOrder_Add;
	Spinner SpinnerOrder_Del;
	Spinner SpinnerOrder_Read;
	Spinner SpinnerOrder_Choose;
	Button BtAdd_Select;
	Button BtDel_Select;
	Button BtRead_Select;
	Button BtChoose_Select;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);
		
		flagCrc = 0x00;
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		SpinnerBank_Select = (Spinner)findViewById(R.id.SpinnerBank_Select);
		EtPtr_Select = (EditText)findViewById(R.id.EtPtr_Select);
		EtMask_Select = (EditText)findViewById(R.id.EtMask_Select);
		EtNum_Read = (EditText)findViewById(R.id.EtNum_Read);
		EtNum_choose = (EditText)findViewById(R.id.EtNum_choose);
		SpinnerOrder_Add = (Spinner)findViewById(R.id.SpinnerOrder_Add);
		SpinnerOrder_Del = (Spinner)findViewById(R.id.SpinnerOrder_Del);
		SpinnerOrder_Read = (Spinner)findViewById(R.id.SpinnerOrder_Read);
		SpinnerOrder_Choose = (Spinner)findViewById(R.id.SpinnerOrder_Choose);
		BtAdd_Select = (Button)findViewById(R.id.BtAdd_Select);
		BtDel_Select = (Button)findViewById(R.id.BtDel_Select);
		BtRead_Select = (Button)findViewById(R.id.BtRead_Select);
		BtChoose_Select = (Button)findViewById(R.id.BtChoose_Select);
		
		BtAdd_Select.setOnClickListener(new BtAdd_SelectClickListener());
		BtDel_Select.setOnClickListener(new BtDel_SelectClickListener());
		BtRead_Select.setOnClickListener(new BtRead_SelectClickListener());
		BtChoose_Select.setOnClickListener(new BtChoose_SelectClickListener());
	}
	
	public class BtAdd_SelectClickListener implements OnClickListener{

		@SuppressWarnings("static-access")
		@Override
		public void onClick(View v) {

			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			Srecord[] pSrecord = new Srecord[1];
			pSrecord[0].Sindex = (byte)SpinnerOrder_Add.getSelectedItemPosition();
			pSrecord[0].Bank = (byte) SpinnerBank_Select.getSelectedItemPosition();
			pSrecord[0].Target = 0x00;
			pSrecord[0].Trancate = 0x00;
			pSrecord[0].Action = 0x04;
			pSrecord[0].Mask = new byte[32];
			pSrecord[0].Slen = 8;
			pSrecord[0].Ptr = new byte[2];
			
			String tempMask = EtMask_Select.getText().toString().trim();
			if(tempMask.equals(""))
			{
				Toast.makeText(getApplicationContext(), "掩码不能为空", 0).show();
				return;
			}else if(!fun.isHex(tempMask)){
				Toast.makeText(getApplicationContext(), "掩码必须为16进制字符串", 0).show();
				return;
			}else if(tempMask.length()%2 != 0)
			{
				Toast.makeText(getApplicationContext(), "掩码字符串的长度必须为2的倍数", 0).show();
				return;
			}else{
				pSrecord[0].Mask = fun.HexStringToBytes(tempMask);
				pSrecord[0].Len = (byte) (4*(tempMask.length()));
			}
			
			int iPtr = 0;
			String tempPtr = EtPtr_Select.getText().toString().trim();
			if(tempPtr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "起始地址不能为空", 0).show();
				return;
			}else if(!fun.isDecimal(tempPtr)){
				Toast.makeText(getApplicationContext(), "起始地址必须为10进制字符串", 0).show();
				return;
			}else{
				iPtr = Integer.parseInt(tempPtr);
				
				if (iPtr > 127)
	            {
					pSrecord[0].Ptr[0] = (byte)((iPtr >> 7) | 0x80);
					pSrecord[0].Ptr[1] = (byte)(iPtr & 0x007F);
					pSrecord[0].Slen += 1;
	            }
	            else
	            {
	            	pSrecord[0].Ptr[0] = (byte)iPtr;
	            }
			}
			
			pSrecord[0].Slen += (tempMask.length()/2);  

			if(moduleControl.UhfAddFilter(pSrecord, bStatus, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "添加select记录成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "添加select记录失败", 0).show();
			}
		}
	}
	
	public class BtDel_SelectClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			byte bSindex = (byte) SpinnerOrder_Del.getSelectedItemPosition();
			
			if(moduleControl.UhfDeleteFilterByIndex(bSindex, bStatus, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "删除select记录成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "删除select记录失败", 0).show();
			}
		}
	}
	
	public class BtRead_SelectClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			byte bSindex = (byte) SpinnerOrder_Read.getSelectedItemPosition();
			
			byte ReadNum = 0;
			String numStr = EtNum_Read.getText().toString().trim(); 
			if(numStr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "数量不能为空", 0).show();
				return;
			}else if(!fun.isDecimal(numStr)){
				Toast.makeText(getApplicationContext(), "数量必须为10进制字符串", 0).show();
				return;
			}else{
				ReadNum = Byte.parseByte(numStr);
				if(ReadNum + bSindex > 16)
				{
					Toast.makeText(getApplicationContext(), "数量过大，数量和序号之和不能大于16", 0).show();
					return;
				}
			}
			
			if(moduleControl.UhfStartGetFilterByIndex(bSindex, ReadNum, bStatus, flagCrc))
			{
				Intent intent = new Intent();
				
				intent.setClass(SelectSetting.this, SelectList.class);
				startActivity(intent);
			}else{
				Toast.makeText(getApplicationContext(), "开启读取select记录失败", 0).show();
			}
		}
	}
	
	public class BtChoose_SelectClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", 0).show();
				return;
			}
			
			byte bSindex = (byte) SpinnerOrder_Choose.getSelectedItemPosition();
			
			byte ChooseNum = 0;
			String numStr = EtNum_choose.getText().toString().trim(); 
			
			if(numStr.equals(""))
			{
				Toast.makeText(getApplicationContext(), "数量不能为空", 0).show();
				return;
			}else if(!fun.isDecimal(numStr)){
				Toast.makeText(getApplicationContext(), "数量必须为10进制字符串", 0).show();
				return;
			}else{
				ChooseNum = Byte.parseByte(numStr);
				if(ChooseNum + bSindex > 16)
				{
					Toast.makeText(getApplicationContext(), "数量过大，数量和序号之和不能大于16", 0).show();
					return;
				}
			}
			
			if(moduleControl.UhfSelectFilterByIndex(bSindex, ChooseNum, bStatus, flagCrc))
			{
				Toast.makeText(getApplicationContext(), "选择select记录成功", 0).show();
			}else{
				Toast.makeText(getApplicationContext(), "选择select记录失败", 0).show();
			}
		}
	}
}
