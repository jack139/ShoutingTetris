package com.jack139.tetrisbase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.google.android.gms.ads.*;

import java.io.File;

import com.jack139.shoutingtetris.R;

public class MainActivity extends Activity{

	static {
		System.loadLibrary("pocketsphinx_jni");
	}
	
	private static WakeLock wl=null;
	static GameCanvasView gameCanvasView=null;
	static TetrisGridView tetrisGridView=null;
	static int myScore = 0;
	static int myAIDifficulty = 0;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FileUtils.cleanDirectory(new File(InitOnce.tmp_path+"/raw"));
		
		TextView mTextView01 = (TextView) findViewById(R.id.text_high);
		mTextView01.setText(getText(R.string.txt_high)+" "+InitOnce.highScore);
		
		Button p1Button = (Button) findViewById(R.id.button_single);
		p1Button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						CharSequence[] items = {getText(R.string.lst_item1),
									 getText(R.string.lst_item2),
									 getText(R.string.lst_item3)};
						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setTitle(getText(R.string.lst_title));
						builder.setItems(items, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								myAIDifficulty = item;
								findViewById(R.id.peerselect).setVisibility(View.GONE);
								gameCanvasView.startGame(MainActivity.this);
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					}
			 });
				
		gameCanvasView = (GameCanvasView) findViewById(R.id.gameCanvasView1);
		tetrisGridView = (TetrisGridView)findViewById(R.id.tetrisGridView1);

		// load AdMob
		AdView adView = (AdView)this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

	}
	
	void quitGame(final boolean notify){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gameCanvasView.myControls.killThreads();
				gameCanvasView = null;
				tetrisGridView = null;
				setResult(InitOnce.RESTART);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		Log.d("ShoutingTetris", getClass().getName()+": onResume()");
		super.onResume();
		keepScreenOn(true);
		if (gameCanvasView!=null && gameCanvasView.myControls!=null){
			gameCanvasView.myControls.resumeThreads();
			Log.d("ShoutingTetris", getClass().getName()+": resumeThreads()");
		}
	}
	@Override
	protected void onPause() {
		Log.d("ShoutingTetris", getClass().getName()+": onPause()");
		super.onPause();
		keepScreenOn(false);
		if (gameCanvasView!=null && gameCanvasView.myControls!=null){
			gameCanvasView.myControls.pauseThreads();
			Log.d("ShoutingTetris", getClass().getName()+": pauseThreads()");
		}
	}
	
	@Override
	public void onBackPressed() {
			if (findViewById(R.id.peerselect).getVisibility() == View.GONE){
				quitGame(true);
			}	else { 	
				finish(); 
			}
//		super.onBackPressed();
	}
	
		@Override
		protected void onDestroy() {
				super.onDestroy();
		}

		void showToast(final String msg){
			runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this.getBaseContext(), msg, Toast.LENGTH_SHORT).show();				
			}
		});
		}

		void readytoQuit(){
			runOnUiThread(new Runnable() {
			@Override
			public void run() {
					AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
					if (myScore > InitOnce.highScore){
						InitOnce.highScore = myScore;
						FileUtils.saveHighScore(myScore);
						builder.setTitle(getText(R.string.dlg_title2));
					}
					else 
						builder.setTitle(getText(R.string.dlg_title));
					builder.setMessage(getText(R.string.dlg_message) + " " + myScore);
					builder.setPositiveButton(getText(R.string.dlg_button), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i) {
								quitGame(false);
						}
					});
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
								quitGame(false);
						}
					});					
					AlertDialog ad = builder.create();
					ad.show();
			}
		});
		}
		

	private void keepScreenOn(boolean on) {  
		if (on) {  
			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "==KeepScreenOn==");  
			wl.acquire();  
		}else {  
			wl.release();  
			wl = null;  
		}  
	}     
}
