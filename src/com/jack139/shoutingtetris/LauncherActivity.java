package com.jack139.shoutingtetris;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.jack139.tetrisbase.InitOnce;
import com.jack139.tetrisbase.MainActivity;
import com.jack139.tetrisbase.FileUtils;

public class LauncherActivity extends Activity{
	private static final String log_path="/Android/data/com.jack139.shoutingtetris";
	private static final String lm_file="/cn_lm";
	private static final String dic_file="/cn_dic";
	static int versionNumber;

	static {
		InitOnce.voice_cmd.put("左边", 1);  
  	InitOnce.voice_cmd.put("右边", 2);  
  	InitOnce.voice_cmd.put("落下", 3);  
  	InitOnce.voice_cmd.put("旋转", 4);  
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState==null) Log.d("ShoutingTetris", getClass().getName()+": NEW instance");
		else Log.d("ShoutingTetris", getClass().getName()+": Welcome BACK");

		try {
			versionNumber = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			versionNumber = 999;
		} 

		InitOnce.lm_file=this.lm_file;
		InitOnce.dic_file=this.dic_file;
		InitOnce.local_path = getApplicationContext().getFilesDir().getAbsolutePath();
		InitOnce.tmp_path = Environment.getExternalStorageDirectory().getAbsolutePath()+log_path;
		FileUtils.makeDirectories(new File(InitOnce.tmp_path+"/raw"), 0755);
		copyResourcesToLocal();
		InitOnce.highScore=FileUtils.readHighScore();
				
		InitOnce.explosionSpriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.explosion_sprites);
		startActivityForResult(new Intent(this, MainActivity.class), 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == InitOnce.RESTART){
			startActivityForResult(new Intent(this, MainActivity.class), 0);
		} else {
			System.out.println("Exiting game");
			finish();
		}
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
  	super.onSaveInstanceState(savedInstanceState);
  	//Log.d("ShoutingTetris", getClass().getName()+": onSaveInstanceState");
	}
	
	private boolean needsToBeUpdated(String filename, InputStream content) {
		File script = new File(filename);
		FileInputStream fin;

		if (!script.exists()) {
			Log.d("ShoutingTetris", getClass().getName()+": "+filename+" not found!");
			return true;
		}

		try {
			String fVersion = FileUtils.readToString(new File(InitOnce.local_path + "/versionCode"));
			int fileVersion = Integer.valueOf(fVersion).intValue();			

			if (fileVersion<versionNumber){
					Log.d("ShoutingTetris", getClass().getName()+": Old version "+fileVersion+" need to update " + filename);
					return true;
			}
		} catch (Exception e) {
			Log.e("ShoutingTetris", getClass().getName()+": "+e.getMessage());
			return true;
		}
		//Log.d("ShoutingTetris", getClass().getName()+": No need to update " + filename);
		return false;
	}
	
	private void copyResourcesToLocal() {
		String name, sFileName;
		InputStream content;
		R.raw a = new R.raw();
		java.lang.reflect.Field[] t = R.raw.class.getFields();
		Resources resources = getResources();
		try {
			for (int i = 0; i < t.length; i++) {
				name = resources.getText(t[i].getInt(a)).toString();
				sFileName = name.substring(name.lastIndexOf('/') + 1, name
						.length());
				content = getResources().openRawResource(t[i].getInt(a));

				// Copies script to internal memory only if changes were made
				sFileName = InitOnce.local_path + "/" + sFileName;
				if (needsToBeUpdated(sFileName, content)) {
					Log.d("ShoutingTetris", getClass().getName()+": Copying from stream " + sFileName);
					content.reset();
					FileUtils.copyFromStream(sFileName, content);
				}
				FileUtils.chmod(new File(sFileName), 0755);
			}
			// update versionCode to current
			File f=FileUtils.writeToFile(InitOnce.local_path + "/versionCode", ""+versionNumber);
			FileUtils.chmod(f, 0755);
		} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("ShoutingTetris", getClass().getName()+": "+e.getMessage());
		}

	}
	
}
