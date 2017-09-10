package com.raylinks.demo;

import java.io.FileInputStream;
import java.io.IOException;
import com.raylinks.Function;
import com.raylinks.ModuleControl;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UpdateSetting extends Activity {
	
	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	
	byte[] bStatus = new byte[1];
	byte[] RN32 = new byte[4];
	byte[] reRN32 = new byte[4];
	byte[] bfile_len = new byte[4];
	
	private static byte flagCrc;
	
	EditText EtBinPath;
	EditText EtMac;
	private String macStr;
	
	ProgressBar PbUpdate;
	Button BtUpdate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);
		
		flagCrc = 0x00;
		
		EtBinPath = (EditText)findViewById(R.id.EtBinPath);
		EtMac = (EditText)findViewById(R.id.EtMac);
		PbUpdate = (ProgressBar)findViewById(R.id.PbUpdate);
		BtUpdate = (Button)findViewById(R.id.BtUpdate);

		PbUpdate.setVisibility(View.INVISIBLE);
		BtUpdate.setOnClickListener(new BtUpdateClickListener());
	}
	
		
	public class BtUpdateClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(EtBinPath.getText().toString().trim().equals(""))
			{
				Toast.makeText(UpdateSetting.this, "请输入Bin文件的完整名称", 0).show();
				return;
			}
			
			if(EtMac.getText().toString().trim().equals(""))
			{
				Toast.makeText(UpdateSetting.this, "请输入蓝牙设备的MAC地址", 0).show();
				return;
			}else{
				macStr = EtMac.getText().toString().trim();
			}
			
			String path = "/mnt/sdcard/" + EtBinPath.getText().toString().trim();
			//String path = "/mnt/sdcard/RLM300_V0.8.4.bin";
			
			FileInputStream fin = null;
			byte[] packages = new byte[1024];
			int i = 0;
			long file_byte_size = 0;
			int lastpack_len;
			byte package_num;
			
			try {
				fin = new FileInputStream(path);
			} catch (Exception e2) {
				
				e2.printStackTrace();
			} 

			try {
				file_byte_size = fin.available();
			} catch (IOException e2) {
				
				Toast.makeText(UpdateSetting.this, "文件不存在", 0).show();
				e2.printStackTrace();
			} 
            package_num = (byte) (file_byte_size / 1024);//获取完整1024byte数据包个数
            lastpack_len = (int) (file_byte_size % 1024);//获取最后一个数据包长度  byte

			bfile_len[0] = (byte)(file_byte_size >> 24);
            bfile_len[1] = (byte)(file_byte_size >> 16);
            bfile_len[2] = (byte)(file_byte_size >> 8);
            bfile_len[3] = (byte)file_byte_size;
            
            if(!moduleControl.UhfUpdataInit(macStr, bStatus, RN32, flagCrc))
            {
            	Toast.makeText(UpdateSetting.this, "升级失败1", 0).show();
				return;
				
			}else{
				PbUpdate.setVisibility(View.VISIBLE);
				for(i=0;i<4;i++)
				{
					reRN32[i] = (byte) ~RN32[i];
				}
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
			
			if(!moduleControl.UhfUpdateSendRN32(reRN32, bStatus, flagCrc))
			{
				PbUpdate.setVisibility(View.INVISIBLE);
				Toast.makeText(UpdateSetting.this, "升级失败2", 0).show();
				return;
			}
			      
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
			
            if(!moduleControl.UhfUpdateSendSize(bStatus, bfile_len, flagCrc))
			{
            	PbUpdate.setVisibility(View.INVISIBLE);
				Toast.makeText(UpdateSetting.this, "升级失败3", 0).show();
				return;
			}
            
            try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
		     
        	for (i = 0; i < package_num; i++)//发送完整数据包
        	{
        		try {
        			fin.read(packages, 0, 1024);
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
        		
        		if(!moduleControl.UhfUpdateSendData(bStatus, (byte) i, (byte) 0x00, 1024, packages, flagCrc))
        		{
        			PbUpdate.setVisibility(View.INVISIBLE);
        			try {
						fin.close();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
        			return;
        		}
        		
        		try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
        	}
        	
        	for (int j = 0; j < lastpack_len; j++)
        	{
        		try {
					packages[j] = (byte) fin.read();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
        	}

        	if (!moduleControl.UhfUpdateSendData(bStatus, (byte) i, (byte) 0x01, lastpack_len, packages, flagCrc))
        	{
        		PbUpdate.setVisibility(View.INVISIBLE);
        		try {
					fin.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
    			return;
        	}
        	
        	try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
        	
        	if (moduleControl.UhfUpdataCommit(bStatus, flagCrc))
        	{
        		AlertDialog.Builder builder = new Builder(UpdateSetting.this);
        		builder.setMessage("升级成功");
        		builder.setTitle("提示");
        			
        		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
        			
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				
        			}
        		});
        		
        		builder.create().show();
        	}
        	
        	PbUpdate.setVisibility(View.INVISIBLE);
        	
        	moduleControl.UhfReaderDisconnect();
		}
	}
}
