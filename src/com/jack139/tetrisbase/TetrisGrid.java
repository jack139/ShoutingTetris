package com.jack139.tetrisbase;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;

public class TetrisGrid {
	int width, height;
	TetrisBlock[][] blockGrid = new TetrisBlock[20][10];
	TetrisPiece currentPiece;
	int blastLineRow = 0;
	boolean complete = false;
	ArrayList<TetrisBlock> fallingBlocks = new ArrayList<TetrisBlock>();
	boolean currentBitmap, dirty = true; 
	private boolean initialized = false;
	//0 I red
	//1 O orange
	//2 T magenta
	//3 L cyan
	//4 J cyan
	//5 S blue
	//6 z blue
	public TetrisGrid(int screenW, int screenH){
		width = screenW;
		height = screenH;
	}
	protected void onDraw(Canvas canvas) {
		if (!initialized) return;		
	}
	boolean spotIsEmtpy(int row, int col){//returns true if spot is empty
		if (col < 0 || col > 9 || row < 0 || row > 19) return false;
		return (blockGrid[row][col] == null || (!blockGrid[row][col].stationary));
	}
	boolean canGrabBlock(int row, int col){//returns true spot contains grabbable block
		if (col < 0 || col > 9 || row < 0 || row > 19) return false;
		return (blockGrid[row][col] != null && (!blockGrid[row][col].partOfCurrent));
	}
	public int gameUpdate() {
		if (currentPiece == null){
			currentPiece = new TetrisPiece(this, (int)(Math.random()*7));
			if (!currentPiece.addToGrid()) {
				currentPiece.settle();
				currentPiece = null;
				complete = true;
				if (MainActivity.tetrisGridView!=null) MainActivity.tetrisGridView.postInvalidate();
				return -1;
			} 
		} else {
			if (!currentPiece.fall()) {
				currentPiece.settle();
				currentPiece = null;
			} 
		}
		for (int i = fallingBlocks.size() - 1; i >= 0; i--) {
			TetrisBlock block = fallingBlocks.get(i);
			if (spotIsEmtpy(block.row - 1, block.col)) {
				block.fall(1);
//				MainActivity.tetrisGridView.postInvalidate();
			} else {
				block.stationary = true;
				fallingBlocks.remove(block);
			}
		}
		eliminateRows();
		
		if (MainActivity.tetrisGridView!=null) MainActivity.tetrisGridView.postInvalidate();
		else return -1;
		
		if (currentPiece == null) {
			return 1;
		} else {
			currentPiece.draw();
		}
		return 0;
	}
	void eliminateRows(){
		int gap = 0;
		for (int i = 0; i < 20; i++) {
			boolean isFull = true;
			for (int j = 0; j < 10; j++) {
				if (spotIsEmtpy(i, j)) {
					isFull = false;
					break;
				}
			}
			if (isFull){
				gap++;
				for (int j = 0; j < 10; j++) {
					blockGrid[i][j] = null;
				}
				MainActivity.gameCanvasView.myControls.createExplosionThread(i);
			} else if (gap > 0){
				for (TetrisBlock block : blockGrid[i]) {
					if (block != null && block.stationary) block.fall(gap);
				}
			}
		}
		// caculate the score 
		MainActivity.myScore += (100*gap*gap*(MainActivity.myAIDifficulty+1));
	}
}
