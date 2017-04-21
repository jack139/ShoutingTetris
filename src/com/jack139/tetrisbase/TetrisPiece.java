package com.jack139.tetrisbase;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;

public class TetrisPiece {
	TetrisBlock[] blocks = new TetrisBlock[4];
	int orientation = 0;
	int type;
	TetrisGrid grid;

	Bitmap bmap;
	boolean currentBitmap; 
	private Canvas canvas;
	static int[][][][] rotationOffsets;
	public TetrisPiece(TetrisGrid tetrisGrid, int type){
		this.type = type;
		grid = tetrisGrid;
		switch (type) {
		case 0://I
			blocks[0] = new TetrisBlock(grid, type, 19, 4);
			blocks[1] = new TetrisBlock(grid, type, 18, 4);
			blocks[2] = new TetrisBlock(grid, type, 17, 4);
			blocks[3] = new TetrisBlock(grid, type, 16, 4);
			break;
		case 1://O
			blocks[0] = new TetrisBlock(grid, type, 19, 4);
			blocks[1] = new TetrisBlock(grid, type, 19, 5);
			blocks[2] = new TetrisBlock(grid, type, 18, 4);
			blocks[3] = new TetrisBlock(grid, type, 18, 5);
			break;
		case 2://T
			blocks[1] = new TetrisBlock(grid, type, 19, 4);
			blocks[0] = new TetrisBlock(grid, type, 18, 4);
			blocks[3] = new TetrisBlock(grid, type, 17, 4);
			blocks[2] = new TetrisBlock(grid, type, 18, 5);
			break;
		case 3://L
			blocks[1] = new TetrisBlock(grid, type, 19, 4);
			blocks[0] = new TetrisBlock(grid, type, 18, 4);
			blocks[2] = new TetrisBlock(grid, type, 17, 4);
			blocks[3] = new TetrisBlock(grid, type, 17, 5);
			break;
		case 4://J
			blocks[1] = new TetrisBlock(grid, type, 19, 5);
			blocks[0] = new TetrisBlock(grid, type, 18, 5);
			blocks[2] = new TetrisBlock(grid, type, 17, 5);
			blocks[3] = new TetrisBlock(grid, type, 17, 4);
			break;
		case 5://S
			blocks[2] = new TetrisBlock(grid, type, 19, 5);
			blocks[3] = new TetrisBlock(grid, type, 19, 6);
			blocks[0] = new TetrisBlock(grid, type, 18, 4);
			blocks[1] = new TetrisBlock(grid, type, 18, 5);
			break;
		case 6://Z
			blocks[2] = new TetrisBlock(grid, type, 18, 5);
			blocks[3] = new TetrisBlock(grid, type, 18, 6);
			blocks[0] = new TetrisBlock(grid, type, 19, 4);
			blocks[1] = new TetrisBlock(grid, type, 19, 5);
			break;
		default:
			break;
		}
		bmap = Bitmap.createBitmap(grid.width, grid.height, Config.ARGB_4444);
		canvas = new Canvas(bmap);
	}
	boolean addToGrid(){
		boolean out = true;
		for (TetrisBlock block : blocks) {
			if (grid.blockGrid[block.row][block.col] != null) out = false;
			grid.blockGrid[block.row][block.col] = block;
		}
		return out;
	}
	void removeFromGrid(){
		for (TetrisBlock block : blocks) {
			grid.blockGrid[block.row][block.col] = null;
		}
	}
	boolean rotate(){
		if (grid == null) return false;
		for (int i = 0; i < 4; i++) {
			if (!grid.spotIsEmtpy(blocks[i].row - rotationOffsets[type][orientation][i][0], 
					blocks[i].col + rotationOffsets[type][orientation][i][1])) {
				return false;
			}
		}
		removeFromGrid();
		for (int i = 0; i < 4; i++) {
			blocks[i].row -= rotationOffsets[type][orientation][i][0];
			blocks[i].col += rotationOffsets[type][orientation][i][1];
			blocks[i].rotate();
		}
				
		addToGrid();
		orientation = (orientation + 1) % 4;
		return true;
	}
	boolean fall(){
		for (TetrisBlock block : blocks) {
			if (!grid.spotIsEmtpy(block.row - 1, block.col)) {
				return false;
			}
		}
		for (TetrisBlock block : blocks){
			block.fall(1);
		}
		return true;
	}
	void drop(){
		int min = 20;
		for (TetrisBlock block : blocks) {
			for (int i = 1; i <= min; i++) {
				if (!grid.spotIsEmtpy(block.row - i, block.col)) {
					min = i - 1;
					if (min < 1) return;
					break;
				}
			}
		}
		removeFromGrid();
		for (TetrisBlock block : blocks) {
			block.fall(min);
		}
		addToGrid();
	}
	boolean move(boolean right){
		int offset = right ? 1 : -1;
		for (TetrisBlock block : blocks){
			if (!grid.spotIsEmtpy(block.row, block.col + offset)){
				return false;
			}
		}
		removeFromGrid();
		for (TetrisBlock block : blocks){
			block.move(0, offset);
		}
		addToGrid();
		return true;
	}
	int getLocation(){
		int min = 20;
		for (TetrisBlock block : blocks) {
			if (block.col < min) min = block.col;
		}
		return min;
	}
	void draw(){
		bmap.eraseColor(Color.TRANSPARENT);
		for (TetrisBlock block : blocks){
			canvas.drawRoundRect(block.getBounds(), GameCanvasView.blockSize/12, GameCanvasView.blockSize/12, TetrisGridView.blockPaints[block.type]);
		}
	}
	void settle(){
		for (TetrisBlock block : blocks){
			block.stationary = true;
			block.partOfCurrent = false;
		}
	}
	static void createRotationOffsets(){
		rotationOffsets = new int[][][][]{
				{//I
					{{1,2},{0,1},{-1, 0},{-2, -1}},
					{{2,-2},{1,-1},{0, 0},{-1, 1}},
					{{-2,-1},{-1,0},{0, 1},{1, 2}},
					{{-1,1},{0,0},{1, -1},{2, -2}}
				},
				{//O
					{{0,1},{1,0},{-1, 0},{0, -1}},
					{{1, 0},{0,-1},{0, 1},{-1,0}},
					{{0, -1},{-1, 0},{1,0},{0,1}},
					{{-1,0},{0, 1},{0,-1},{1, 0}}
				},
				{//T
					{{0,0},{1,1},{1,-1},{-1,-1}},
					{{0,0},{1,-1},{-1,-1},{-1,1}},
					{{0,0},{-1,-1},{-1,1},{1,1}},
					{{0,0},{-1,1},{1,1},{1,-1}}
				},
				{//L
					{{0,0},{1,1},{-1,-1},{0,-2}},
					{{0,0},{1,-1},{-1,1},{-2,0}},
					{{0,0},{-1,-1},{1,1},{0,2}},
					{{0,0},{-1,1},{1,-1},{2,0}}
				},
				{//J
					{{0,0},{1,1},{-1,-1},{-2,0}},
					{{0,0},{1,-1},{-1,1},{0,2}},
					{{0,0},{-1,-1},{1,1},{2,0}},
					{{0,0},{-1,1},{1,-1},{0,-2}}
				},
				{//S
					{{-1,0},{0,-1},{1,0},{2,-1}},
					{{0,2},{-1,1},{0,0},{-1,-1}},
					{{2,-1},{1,0},{0,-1},{-1,0}},
					{{-1,-1},{0,0},{-1,1},{0,2}}
				},
				{//Z
					{{0,1},{1,0},{0,-1},{1,-2}},
					{{1,1},{0,0},{-1,1},{-2,0}},
					{{1,-2},{0,-1},{1,0},{0,1},},
					{{-2,0},{-1,1},{0,0},{1,1}}
				}
			};
	}
}
