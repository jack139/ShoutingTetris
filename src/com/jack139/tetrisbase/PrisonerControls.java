package com.jack139.tetrisbase;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.LayoutInflater;
import android.graphics.Rect;
import android.os.Bundle;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;

public class PrisonerControls implements RecognitionListener,OnTouchListener{
	public boolean running = true, pause = false;
	public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
	static int explosionSpriteSize;
	
	Thread prisonerPhysicsThread, tetrisThread;
	MainActivity mainActivity;
	GameCanvasView gameCanvasView;
	float xDown, yDown;
	boolean isTap = false;
	long tDown;
	RecognizerTask rec;
	Thread rec_thread;	
	LinkedBlockingQueue<Integer> input_queue = new LinkedBlockingQueue<Integer>(); 
	static String voice_input = "";
	String used_input = "";
	long rec_last_restart_time;
	
	public PrisonerControls(MainActivity act){
		super();
		
		explosionSpriteSize = InitOnce.explosionSpriteSheet.getHeight();
		
		mainActivity = act;
		gameCanvasView = MainActivity.gameCanvasView;
		TetrisPiece.createRotationOffsets();

		rec = new RecognizerTask();
		rec_thread = new Thread(rec);
		rec_last_restart_time = System.currentTimeMillis();
		rec.setRecognitionListener(this);
		
		createTicker();
		createTetrisControlThread();
		
		rec_thread.start();
		rec.start();
}
	private void setDownHere(MotionEvent event){
		xDown = event.getX();
		yDown = event.getY();
		tDown = event.getEventTime();
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int swipeLength = gameCanvasView.myWidth/5;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setDownHere(event);
			isTap = true;
			break;
		case MotionEvent.ACTION_MOVE:
			isTap = false;
			float dx= event.getX() - xDown, dy= event.getY() - yDown;
			long dt = event.getEventTime() - tDown;
			if ((dx*dx > swipeLength * swipeLength) || (dy*dy > swipeLength * swipeLength)){
				if (dx > swipeLength) { input_queue.offer((int)'R'); }
				if (dx < -swipeLength){ input_queue.offer((int)'L'); }
				if (dy > swipeLength) { input_queue.offer((int)'D'); }
				if (dy < -swipeLength){ input_queue.offer((int)'U'); }
				
				setDownHere(event);
			} else if (dt > 666){
				setDownHere(event);
			} 
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			System.out.println("touch action " + event.getAction());
			break;
		}
		return true;
	}
	private void createTicker() {
		tetrisThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("ShoutingTetris", getClass().getName()+": createTicker ---------- ALIVE!");
				long max_T=(3 - mainActivity.myAIDifficulty)*100+50;
				while (gameCanvasView.grid == null);
				while (running) {
					while (pause);
					long time = System.currentTimeMillis();
					int status = gameCanvasView.grid.gameUpdate(); 
					if (status < 0) { //game over
						if (gameCanvasView.grid.complete){ // show dialog to show finnal score
							mainActivity.readytoQuit();
						}else{
							killThreads();
						}
						break;
					}
					
					//  rec retart to make raw file not very large
					if (time-rec_last_restart_time>10000){  // 10 seconds
						rec.restart();
						rec_last_restart_time=System.currentTimeMillis();
					}

					long dt = System.currentTimeMillis() - time;
					if (dt > max_T) dt = max_T;
					try {Thread.sleep((status == 1 ? max_T+66 : (max_T+66)*2) - dt);} catch (InterruptedException e) {e.printStackTrace();}
				}
				Log.d("ShoutingTetris", getClass().getName()+": createTicker ---------- BYEBYE!");
			}
		});
		tetrisThread.start();
	}
	void createTetrisControlThread(){
        Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
					Log.d("ShoutingTetris", getClass().getName()+": createTetrisControlThread ---------- ALIVE!");
					while (running) {
						int input;
						
						//Log.d("ShoutingTetris", getClass().getName()+": check QUEUE");
						try{
							input = input_queue.take();
							Log.d("ShoutingTetris", getClass().getName()+": got " + (char)input);
							
							switch (input) {
							case 'U':
								if (gameCanvasView.grid.currentPiece != null){
									gameCanvasView.grid.currentPiece.rotate();
									gameCanvasView.grid.currentPiece.draw();
								}
								break;
							case 'L':
								if (gameCanvasView.grid.currentPiece != null){
									gameCanvasView.grid.currentPiece.move(false);
									gameCanvasView.grid.currentPiece.draw();
								}
								break;
							case 'R':
								if (gameCanvasView.grid.currentPiece != null){
									gameCanvasView.grid.currentPiece.move(true);
									gameCanvasView.grid.currentPiece.draw();
								}
								break;
							case 'D':
								if (gameCanvasView.grid.currentPiece != null){
									gameCanvasView.grid.currentPiece.drop();
									gameCanvasView.grid.currentPiece.draw();
								}
								break;
/*								
							case 'T':
								if (gameCanvasView.grid.currentPiece != null){
									gameCanvasView.grid.gameUpdate();
								}
								break;
*/	
							default:
								break;
							}
						}catch (InterruptedException e) {
							Log.d("ShoutingTetris", getClass().getName()+": Interrupted in input_queue.take");
						}
					}
					// prepare to finish
					rec.stop();
					rec.shutdown();
					//rec=null;
					//rec_thread=null;

					Log.d("ShoutingTetris", getClass().getName()+": createTetrisControlThread ---------- BYEBYE!");
			}
		});
        readThread.start();
	}
	
	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final PrisonerControls that = this;
		final String hyp = b.getString("hyp");
		String[] inputs;
		String new_raw, new_input;
		Integer to_mov;
				
		if (hyp!=null && hyp.length()>0){
			/*
				case 1: raw="LEFT RIGHT" used="LEFT"	new_used="LEFT" input="RIGHT"		--- normal
				case 2: raw="LEFT DROP" used="LEFT RIGHT" new_used="LEFT" input="DROP" --- recognise fault
				case 3: raw="LEFT" used="LEFT RIGHT" new_used="LEFT" input=""      --- alter recognise
				case 4: raw="" used ="LEFT RIGHT" new_used="" input=""         --- audio retart
			*/
			//Log.d("ShoutingTetris", getClass().getName()+": raw  '"+new_raw+"'");
			//Log.d("ShoutingTetris", getClass().getName()+": old used '"+used_input+"'");
			new_raw=hyp.trim();
			if (new_raw.length()<used_input.length()) 
					used_input=used_input.substring(0,new_raw.length());
			for (int i=0; i<used_input.length(); i++){
				if (used_input.charAt(i)==new_raw.charAt(i)) continue;
				else{
					used_input = new_raw.substring(0,i).trim();
					break;
				}
			}
			new_input =new_raw.substring(used_input.length()).trim();
			//Log.d("ShoutingTetris", getClass().getName()+": new used '"+used_input+"'");
			//Log.d("ShoutingTetris", getClass().getName()+": input '"+new_input+"'");
			if (new_input.length()>0){
				inputs = new_input.split(" ");
				for (int i=0; i<inputs.length; i++){
					//Log.d("ShoutingTetris", getClass().getName()+": input '"+inputs[i]+"'");
					to_mov = InitOnce.voice_cmd.get(inputs[i]);
					if (to_mov==null) continue; // not in command list
					switch(to_mov) {
						case 1:
							input_queue.offer((int)'L');
							break;
						case 2:
							input_queue.offer((int)'R');
							break;
						case 3:
							input_queue.offer((int)'D');
							break;
						case 4:
							input_queue.offer((int)'U');	
							break;
					}
				}
				voice_input = new_input; // to show
			}
			used_input = new_raw;
		}
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
/*
		final String hyp = b.getString("hyp");
		final PrisonerControls that = this;

		if (hyp==null) return;
		voice_input = ""+hyp;
*/		
	}

	public void onError(int err) {
/*		final PrisonerControls that = this;
		that.edit_text.post(new Runnable() {
			public void run() {
				that.rec_dialog.dismiss();
			}
		});*/
	}

	public void pauseThreads(){
		pause = true;
		rec.stop();
	}
	public void resumeThreads(){
		pause = false;
		rec.start();
	}
	public void killThreads(){
		running = false;
		input_queue.offer((int)'X');  // unknown event to wake up the TetrisControlThread
	}
	public void createExplosionThread(final int row){
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Explosion> temp = new ArrayList<Explosion>();
				synchronized (explosions) {
					for (int i = 0; i < 10; i++) {
						Explosion e = new Explosion(row, i);
						temp.add(e);
						explosions.add(e);
					}
				}
				for (int i = 0; i < 5; i++) {
					for (Explosion e : temp) {
						e.updateFrame(i);
					}
					try {Thread.sleep(25);} catch (InterruptedException e) {e.printStackTrace();}
				}
				synchronized (explosions) {
					explosions.removeAll(temp);
				}
			}
		});
		thread.start();
	}
	public class Explosion{
		Rect bitmapFrame, positionRect;

		public Explosion(int row, int col){
			bitmapFrame = new Rect(0, 0, explosionSpriteSize, explosionSpriteSize);
			positionRect = new Rect();
			positionRect.left = (int) (GameCanvasView.blockSize * (col - 0.904761));
			positionRect.top = (int) ((19 - row - 6.0/7.0) * GameCanvasView.blockSize);
			positionRect.right = (int) (GameCanvasView.blockSize * (col + 1.904761));
			positionRect.bottom = (int) ((20 - row + .9524) * GameCanvasView.blockSize);
		}
		void updateFrame(int frameNum){
			bitmapFrame.left = frameNum * explosionSpriteSize;
			bitmapFrame.right = bitmapFrame.left + explosionSpriteSize;
		}
	}

}
