package com.raylinks.demo;


import java.util.ArrayList;
import java.util.HashMap;
import com.raylinks.Function;
import com.raylinks.ModuleControl;
import com.raylinks.ModuleCommand.Srecord;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SelectList extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	byte[] bStatus = new byte[1]; 
	Srecord[] pSrecord = new Srecord[1];
	
	static ArrayList<HashMap<String, String>> selectList;
	SimpleAdapter adapter;
	
	ListView LvSelect;
	TextView TvIndex_SelectList;
	TextView TvTarget_SelectList;
	TextView TvAction_SelectList;
	TextView TvTruncate_SelectList;
	TextView TvBank_SelectList;
	TextView TvPtr_SelectList;
	TextView TvLen_SelectList;
	TextView TvMask_SelectList;
	Button BtBack_Select;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectlist);
		
		selectList = new ArrayList<HashMap<String, String>>();
		
		adapter = new SimpleAdapter(SelectList.this,
				selectList,
				R.layout.selectlist_items,
				new String[]{"index","target","action","truncate","bank","ptr","len","mask"},
				new int[]{R.id.TvIndex_SelectList, R.id.TvTarget_SelectList, R.id.TvAction_SelectList, 
				R.id.TvTruncate_SelectList, R.id.TvBank_SelectList, R.id.TvPtr_SelectList,
				R.id.TvLen_SelectList, R.id.TvMask_SelectList});
		
		LvSelect = (ListView)findViewById(R.id.LvSelect);
		TvIndex_SelectList = (TextView)findViewById(R.id.TvIndex_SelectList);
		TvTarget_SelectList = (TextView)findViewById(R.id.TvTarget_SelectList);
		TvAction_SelectList = (TextView)findViewById(R.id.TvAction_SelectList);
		TvTruncate_SelectList = (TextView)findViewById(R.id.TvTruncate_SelectList);
		TvBank_SelectList = (TextView)findViewById(R.id.TvBank_SelectList);
		TvPtr_SelectList = (TextView)findViewById(R.id.TvPtr_SelectList);
		TvLen_SelectList = (TextView)findViewById(R.id.TvLen_SelectList);
		TvMask_SelectList = (TextView)findViewById(R.id.TvMask_SelectList);
		BtBack_Select = (Button)findViewById(R.id.BtBack_Select);
		
		BtBack_Select.setOnClickListener(new BtBack_SelectClickListener());
		
		new ReadFilterLoopThread().start();
	}
		
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){
						
			Bundle bundle = msg.getData();
			
			String index = bundle.getString("index");
			String target = bundle.getString("target");
			String action = bundle.getString("action");
			String truncate = bundle.getString("truncate");
			String bank = bundle.getString("bank");
			String ptr = bundle.getString("ptr");
			String len = bundle.getString("len");
			String mask = bundle.getString("mask");
			
			HashMap<String, String> map = new HashMap<String, String>();
			
			map.put("index", index);
			map.put("target", target);
			map.put("action", action);
			map.put("truncate", truncate);
			map.put("bank", bank);
			map.put("ptr", ptr);
			map.put("len", len);
			map.put("mask", mask);

			selectList.add(map);
			LvSelect.setAdapter(adapter);
		}
	};
	
	class ReadFilterLoopThread extends Thread{
		
		HashMap<String, String> map;
		
		@SuppressWarnings("static-access")
		public void run()
		{
			//bStatus[0] = (byte)0xFF;
			bStatus[0] = 0x00;
						
			while(bStatus[0] == 0x00)
			{
				int iPtr = 0;
				if(moduleControl.UhfReadFilterByIndex(bStatus, pSrecord))
				{
					String index = String.valueOf(pSrecord[0].Sindex);
					String target = String.valueOf(pSrecord[0].Target);
					String action = String.valueOf(pSrecord[0].Action);
					String truncate = String.valueOf(pSrecord[0].Trancate);
					String bank = String.valueOf(pSrecord[0].Bank);
					if((pSrecord[0].Ptr[0] & 0x80) == 0x80)
                    {
                        iPtr = ((pSrecord[0].Ptr[0] & 0x7F) << 7) + pSrecord[0].Ptr[1];
                    }
                    else
                        iPtr = (int)pSrecord[0].Ptr[0];
					
					String ptr = String.valueOf(iPtr);
					
					String len = String.valueOf(pSrecord[0].Len);
					String mask = fun.bytesToHexString(pSrecord[0].Mask, (int)(pSrecord[0].Len/8));
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("index", index);
					bundle.putString("target", target);
					bundle.putString("action", action);
					bundle.putString("truncate", truncate);
					bundle.putString("bank", bank);
					bundle.putString("ptr", ptr);
					bundle.putString("len", len);
					bundle.putString("mask", mask);
					
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	public class BtBack_SelectClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			finish();
		}
	}

}
