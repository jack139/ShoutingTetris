package com.jack139.tetrisbase;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class InitOnce {
	public static final int RESTART = 123574127;//leet for restart
  public static Bitmap explosionSpriteSheet;
  public static Map<String,Integer> voice_cmd=new HashMap<String, Integer>();
	public static String local_path;
	public static String tmp_path;
	public static String lm_file;
	public static String dic_file;
	public static long highScore;

  public InitOnce() {
    // Utility class.
  }

}
