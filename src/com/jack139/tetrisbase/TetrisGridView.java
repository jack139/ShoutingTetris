package com.jack139.tetrisbase;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TetrisGridView extends View {
	static Paint[] blockPaints = {new Paint(), new Paint(), new Paint(), new Paint(), new Paint(), new Paint(), new Paint()};
	static Paint[] blockPaints2 = new Paint[7];
	Paint textPaint = new Paint();
	TetrisGrid grid;
	public TetrisGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(35);
		blockPaints[0].setColor(Color.rgb(0xff,0x66,0x66)); //red
		blockPaints[1].setColor(Color.rgb(0xff,0xcc,0x99));//orange
		blockPaints[2].setColor(Color.rgb(0xff,0x99,0xcc)); // pink
		blockPaints[3].setColor(Color.rgb(0xcc,0xff,0x99));//green
		blockPaints[4].setColor(Color.rgb(0xcc,0xff,0xff)); // BLUE
		blockPaints[5].setColor(Color.rgb(0xcc,0xcc,0xff));//dark
		blockPaints[6].setColor(Color.rgb(0xff,0xff,0x99));//yellow
		for (int i = 0; i < blockPaints.length; i++) {
			blockPaints2[i] = darkenPaint(blockPaints[i]);
			blockPaints2[i].setStyle(Style.STROKE);
			blockPaints2[i].setStrokeWidth(3);
		}
	}
	RectF decor = new RectF();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawText(""+PrisonerControls.voice_input, 5, 160, textPaint); // show listening
		//canvas.drawText(""+InitOnce.tmp_path, 5, 260, textPaint); 
		
		canvas.drawText("Score:", getWidth()/2 - 200, 60, textPaint);
		canvas.drawText("" + MainActivity.myScore, getWidth()/2, 60, textPaint);
		canvas.translate(GameCanvasView.transX, GameCanvasView.transY);
		if (MainActivity.gameCanvasView == null) return;
		grid = MainActivity.gameCanvasView.grid;
		if (grid == null) return;
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 10; j++) {
				TetrisBlock block = grid.blockGrid[i][j];
				if (block != null && !block.partOfCurrent){
					canvas.drawRoundRect(block.getBounds(), GameCanvasView.blockSize/12, GameCanvasView.blockSize/12, blockPaints[block.type]);
					decor.set(block.getBounds());
					decor.left += GameCanvasView.blockSize/4.0;
					decor.right -= GameCanvasView.blockSize/4.0;
					decor.top += GameCanvasView.blockSize/4.0;
					decor.bottom -= GameCanvasView.blockSize/4.0;
					canvas.drawRoundRect(decor, GameCanvasView.blockSize/12.0f, GameCanvasView.blockSize/12.0f, blockPaints2[block.type]);
				}
			}
		}
		
		grid.dirty = false;
	}
	private Paint darkenPaint(Paint p){
		Paint paint = new Paint(p);
		int color = p.getColor();
		paint.setColor(Color.rgb((int)(Color.red(color)*0.75), (int)(Color.green(color)*0.75), (int)(Color.blue(color)*0.75)));
		return paint;
	}
}
