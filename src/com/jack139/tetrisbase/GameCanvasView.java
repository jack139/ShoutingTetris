package com.jack139.tetrisbase;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import com.jack139.tetrisbase.PrisonerControls.Explosion;

public class GameCanvasView extends View {
	boolean initialized = false;
	int myWidth, myHeight;
	static int blockSize, titleSize;
	static int transX, transY;
	TetrisGrid grid;
	static Paint defaultPaint = new Paint(), gameBorderPaint = new Paint();
	static Paint sunlightPaint = new Paint();
	Paint randomPaint = new Paint();
	Path gameBorder = new Path();
	boolean running = false;
	PrisonerControls myControls=null;

	public GameCanvasView(Context context) {
		super(context);
	}
	public GameCanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//usually this one is called
	}
	public GameCanvasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	void startGame(final MainActivity mainActivity){
		setControls(new PrisonerControls(mainActivity));
		running = true;
		mainActivity.myScore = 0;
		invalidate();
	}

	@Override 
	public void onWindowFocusChanged(boolean hasFocus) { 
		super.onWindowFocusChanged(hasFocus);
		initialize();
	}

	void initialize(){
		titleSize = 115;
		if (initialized) return;
		if ((getHeight()-titleSize) > 2*getWidth()){
			myWidth = 10*(getWidth()/10);
			myHeight = 2*myWidth;
		} else {
			myHeight = 20 * ((getHeight()-titleSize) / 20);
			myWidth = myHeight / 2;
		}
		transX = (getWidth() - myWidth) / 2;
		transY = (getHeight()-titleSize) - myHeight;
		blockSize = myWidth/10;
		initializePaints();
		
		setupGame();

		gameBorder.moveTo(0, myHeight);
		gameBorder.lineTo(0, 0);
		gameBorder.lineTo(myWidth, 0);
		gameBorder.lineTo(myWidth, myHeight - 1);
		gameBorder.lineTo(0, myHeight - 1);
		
		initialized = true;
	}

	void setupGame(){
		setControls(null);
		running = false;
		grid = new TetrisGrid(myWidth, myHeight);
		MainActivity.tetrisGridView.postInvalidate();
	}

	void initializePaints(){		
		defaultPaint.setColor(Color.GRAY);
		defaultPaint.setStyle(Style.STROKE);
		defaultPaint.setStrokeWidth(2);
		defaultPaint.setTextSize(20);
		
		gameBorderPaint.setStyle(Style.STROKE);
		gameBorderPaint.setColor(Color.GREEN);
		gameBorderPaint.setStrokeWidth(1.0f);
		
		randomPaint.setStyle(Style.FILL);
		LinearGradient grad = new LinearGradient(myWidth/2, transY * -2, myWidth/2, (myHeight * 7)/8, Color.LTGRAY, Color.TRANSPARENT, TileMode.CLAMP);
		sunlightPaint.setShader(grad);
	}

	public void setControls(PrisonerControls controls){
		myControls = controls;
		setOnTouchListener(myControls);
	}

	Rect spriteRect = new Rect();

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!initialized){
			initialize();
			return;
		}
		canvas.translate(transX, transY);
		if (grid.currentPiece != null) canvas.drawBitmap(grid.currentPiece.bmap, 0, 0, null);
		canvas.drawPath(gameBorder, gameBorderPaint);
		if (grid.complete) {
			canvas.drawRect(1, transY/2 - 5, myWidth - 1, myHeight , sunlightPaint);
		}
		if (grid.blastLineRow > 0){
//			randomPaint.setColor(Color.HSVToColor(new float[]{(float) (Math.random()*360), 1, 1}));
//			canvas.drawRect(1, blockSize * (19 - grid.blastLineRow) - 1, myWidth - 1, blockSize * (19 - grid.blastLineRow) + 1, randomPaint);
		}
		if (myControls != null){
			synchronized (myControls.explosions) {
				for (Explosion e : myControls.explosions) {
					canvas.drawBitmap(InitOnce.explosionSpriteSheet, e.bitmapFrame, e.positionRect, null);
				}
			}
		}
		if (running) invalidate();
		
//		if (grid.dirty) {
//			grid.draw();
//		}
	}
	static long timer;
	static void startTimer(){
		timer = System.nanoTime();
	}
	static void printElapsed(){
		System.out.println(1000000000.0/(System.nanoTime() - timer));
		timer = System.nanoTime();
	}
}
