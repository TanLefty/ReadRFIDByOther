package com.raylinks.demo;


import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.raylinks.Function;
import com.raylinks.ModuleControl;

import java.util.ArrayList;
import java.util.HashMap;

public class InventoryTag extends Activity {

	ModuleControl moduleControl = new ModuleControl();
	Function fun = new Function();
	private static boolean loopFlag;
	private static int inventoryFlag;
	private static boolean connFlag;
	private byte flagCrc;
	private byte initQ;
	
	private SoundPool soundpool;
	private static int soundId;
	
	static ArrayList<HashMap<String, String>> tagList;
	//static ArrayList<String> uiiList;
	SimpleAdapter adapter;
	
	Button BtClear;
	EditText EtCountOfTags;
	RadioGroup RgInventory;
	RadioButton RbInventorySingle;
	RadioButton RbInventoryLoop;
	RadioButton RbInventoryAnti;
	Spinner SpinnerQ;
	Button BtInventory;
	ListView LvTags;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inventorytag);
		
		connFlag = this.getIntent().getBooleanExtra("connFlag", false);
		
		flagCrc = 0x01;		//十六进制的标识码
		loopFlag = false;
		inventoryFlag = 1;
		tagList = new ArrayList<HashMap<String, String>>();
		
		soundpool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 5);
		soundId = soundpool.load(InventoryTag.this, R.raw.beep, 1);
		
		BtClear = (Button)findViewById(R.id.BtClear);
		EtCountOfTags = (EditText)findViewById(R.id.EtCountOfTags);
		RgInventory = (RadioGroup)findViewById(R.id.RgInventory);
		RbInventorySingle = (RadioButton)findViewById(R.id.RbInventorySingle);
		RbInventoryLoop = (RadioButton)findViewById(R.id.RbInventoryLoop);
		RbInventoryAnti = (RadioButton)findViewById(R.id.RbInventoryAnti);
		SpinnerQ = (Spinner)findViewById(R.id.SpinnerQ);
		BtInventory = (Button)findViewById(R.id.BtInventory);
		//LvTag = (ListView)findViewById(R.id.LvTags);
		LvTags = (ListView)findViewById(R.id.LvTags);
		
		adapter = new SimpleAdapter(InventoryTag.this,
				tagList,
				R.layout.listtag_items,
				new String[]{"tagUii", "tagLen", "tagCount"},
				new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount});
		
		BtClear.setOnClickListener(new BtClearClickListener());
		EtCountOfTags.setKeyListener(null);
		RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
		BtInventory.setOnClickListener(new BtInventoryClickListener());
		SpinnerQ.setEnabled(false);
		SpinnerQ.setOnItemSelectedListener(new QItemSelectedListener());
		//LvTags.setOnItemClickListener(new LvTagsItemClickListener());
	}

	public class BtClearClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!connFlag)
			{
				Toast.makeText(getApplicationContext(), "尚未连接蓝牙设备", Toast.LENGTH_SHORT).show();
				return;
			}
			
			EtCountOfTags.setText("");
			
			tagList.clear();
			adapter.notifyDataSetChanged();
		}
	}
	
	
	public class RgInventoryCheckedListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if(checkedId == RbInventorySingle.getId()){
				//单步识别
				inventoryFlag = 0;
				SpinnerQ.setEnabled(false);
			}else if(checkedId == RbInventoryLoop.getId()){
				//单标签循环识别
				inventoryFlag = 1;
				SpinnerQ.setEnabled(false);
			}else{
				//防碰撞识别
				inventoryFlag = 2;
				SpinnerQ.setEnabled(true);
			}
		}
	}

	public class QItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			initQ = Byte.valueOf((String) SpinnerQ.getSelectedItem(), 10);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	}

	public class BtInventoryClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(!connFlag)
			{
				Toast.makeText(InventoryTag.this, "请先进行连接", Toast.LENGTH_SHORT).show();
				return;
			}
			Log.v("BreakPoint", "");
						
			if(BtInventory.getText().equals("识别标签"))
			{
				//单标签识别标签
				if(inventoryFlag == 0)
				{
					byte[] bLenUii =new byte[1];
					byte[] bUii = new byte[255];	//算是生成RFID标签码的必要元素之一
					int index = -1;

					/**
					 *	传入一个RFID长度元素
					 *	RFID_bUii码
					 *  一个十六进制的码
					 *  通过这三个参数，计算出一个RFID标签
					 *
					 *
					 */
					if(moduleControl.UhfInventorySingleTag(bLenUii, bUii, flagCrc))
					{
						soundpool.play(soundId, 1, 1, 0, 0, 1);
						String sUii = fun.bytesToHexString(bUii, bLenUii[0]);
						
						HashMap<String, String> map = new HashMap<String, String>();
						
						map.put("tagUii", sUii);
						map.put("tagLen", String.valueOf(bLenUii[0]));
						map.put("tagCount", String.valueOf(1));
						
						index = checkIsExist(sUii,tagList);
						
						if(index == -1)
						{
							tagList.add(map);
							LvTags.setAdapter(adapter);
						}
						else{
							int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;
							
							map.put("tagCount", String.valueOf(tagcount));
							tagList.set(index, map);
							adapter.notifyDataSetChanged();
						}
						EtCountOfTags.setText("" + adapter.getCount());
					}else{
						Toast.makeText(InventoryTag.this, "单步识别失败", Toast.LENGTH_SHORT).show();
					}

					//单标签循环识别
				}else if(inventoryFlag == 1){

					//开始解码
					if(moduleControl.UhfStartInventory((byte)0, (byte)0, flagCrc))
					{
						BtInventory.setText("停止识别");
						loopFlag = true;
						new TagThread().start();					
					}else{
						moduleControl.UhfStopOperation(flagCrc);
					}
					//防碰撞识别
				}else{
					if(moduleControl.UhfStartInventory((byte)1, initQ, flagCrc))
					{
						BtInventory.setText("停止识别");
						loopFlag = true;
						new TagThread().start();
					}else{
						moduleControl.UhfStopOperation(flagCrc);
					}
				}
			}else{//停止识别
				loopFlag = false;
				
				if(moduleControl.UhfStopOperation(flagCrc))
				{
					BtInventory.setText("识别标签");					
				}else{
					
					Toast.makeText(InventoryTag.this, "停止识别标签失败", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	public int checkIsExist(String uiiStr, ArrayList<HashMap<String, String>> tagList)
	{
		int existFlag = -1;
		String tempStr = "";
		for(int i=0;i<tagList.size();i++)
		{
			HashMap<String, String> temp = new HashMap<String, String>();


			/**
			 * 获取tagList中下标为0的map对象指向temp
			 * 获取名字叫temp的map，key值为"tagUii"的值，并赋值给teapStr
			 * 判断，传入的值不等于空，并且这个值与map对象的值是相同。
			 *
			 *
			 */

			temp = tagList.get(i);
			
			tempStr = temp.get("tagUii");
			
			if(uiiStr != "" && uiiStr.equals(tempStr))
			{
				existFlag = i;
			}
		}
		
		return existFlag;
	}
	
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){

			Bundle bundle = msg.getData();
			String tagUii = bundle.getString("tagUii");
			byte tagLen = bundle.getByte("tagLen");
			
			HashMap<String, String> map = new HashMap<String, String>();
			
			map.put("tagUii", tagUii);
			map.put("tagLen", String.valueOf(tagLen));
			map.put("tagCount", String.valueOf(1));
			Log.d("TagData","the TagData is:" + map.toString());
			
			boolean bool = false;
			int index = -1;
			try {
				bool = (tagUii != "") && (!tagUii.equals(null));
			} catch (Exception e) {
				Log.v("BreakPoint", "异常："+e.getMessage());
				e.printStackTrace();
			}
				
     		if(bool)
     		{
     			index = checkIsExist(tagUii,tagList);

     			if(index == -1)
     			{
     				tagList.add(map);
     				LvTags.setAdapter(adapter);
     				EtCountOfTags.setText("" + adapter.getCount());
     			}
     			else{
     				int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;
     				
     				map.put("tagCount", String.valueOf(tagcount));
     				
     				tagList.set(index, map);
     				adapter.notifyDataSetChanged();
     			}
             } 
		}
	};

	class TagThread extends Thread{
		

		
		public void run()
		{
			byte[] bLenUii = new byte[1];
			byte[] bUii = new byte[255];
			
			while(loopFlag)			//此处loopFlag = true;
			{
				if(moduleControl.UhfReadInventory(bLenUii, bUii))
				{	
					soundpool.play(soundId, 1, 1, 0, 0, 1);
					String sUii = fun.bytesToHexString(bUii, bLenUii[0]);
					
					Message msg = handler.obtainMessage();
					Bundle bundle = new Bundle();
					bundle.putString("tagUii", sUii);
					bundle.putByte("tagLen", bLenUii[0]);
										
					msg.setData(bundle);
					handler.sendMessage(msg);
				}

			}
		}
	}

//	public class LvTagsItemClickListener implements OnItemClickListener{
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public void onItemClick(AdapterView<?> parent, View view, int position,
//				long id) {
//			
//			HashMap<String, String> map = (HashMap<String, String>)adapter.getItem(position);
//			String uiiStr = map.get("tagUii");
//			
//			Intent intent = new Intent();
//			intent.putExtra("tagUii", uiiStr);
//			intent.setClass(InventoryTag.this, ChooseUii.class);
//			startActivity(intent);
//		}
//		
//	}
	
//	public class LvTagsItemLongClickListener implements OnItemLongClickListener{
//
//		@Override
//		public boolean onItemLongClick(AdapterView<?> parent, View view,
//				int position, long id) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//		
//	}
}
