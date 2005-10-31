package cx.prutser.nonogram;

import java.util.Properties;
import java.util.StringTokenizer;

import cx.prutser.nonogram.LowerLimitExceededException;
import cx.prutser.nonogram.TileColor;
import cx.prutser.nonogram.UpperLimitExceededException;

/**
 * For more info on the game's rules, check:
 * http://www.pro.or.jp/~fuji/java/puzzle/nonogram/index-eng.html
 * 
 * @author	Erik van Zijst - erik@marketxs.com
 */
public class Nonogram {

	TileColor[][] board;
	int[][] xHints;
	int[][] yHints;
	long count = 0;	// positions evaluated
	
	public Nonogram(Properties puzzle) {
		
		loadBoard(puzzle);
	}

	/**
	 * Sets up the board, xHints and yHints variables.
	 * 
	 * Reads the puzzle from a properties file with the following format:
	 * 
	 * hints.top = 1,1 1 1 2
	 * hints.left = 1 1 1,2 1
	 * 
	 * This describes the following puzzle:
	 * 
	 *     1
	 *     1 1 1 2
	 *   1 # . . .
	 *   1 . # . .
	 * 1,2 # . # #
	 *   1 . . . #
	 *
	 */
	private void loadBoard(Properties props) {
		
		int index, w, h;
		
		StringTokenizer topTok = new StringTokenizer(props.getProperty("hints.top", "	 "));
		w = topTok.countTokens();
		yHints = new int[w][];

		for(index = 0; topTok.hasMoreTokens();) {
			StringTokenizer tok2 = new StringTokenizer(topTok.nextToken(), ",");
			int i = 0;
			yHints[index] = new int[tok2.countTokens()];

			while(tok2.hasMoreTokens()) {
				yHints[index][i++] = Integer.parseInt(tok2.nextToken());
			}
			index++;
		}

		StringTokenizer leftTok = new StringTokenizer(props.getProperty("hints.left", "	 "));
		h = leftTok.countTokens();
		xHints = new int[h][];

		for(index = 0; leftTok.hasMoreTokens();) {
			StringTokenizer tok2 = new StringTokenizer(leftTok.nextToken(), ",");
			int i = 0;
			xHints[index] = new int[tok2.countTokens()];

			while(tok2.hasMoreTokens()) {
				xHints[index][i++] = Integer.parseInt(tok2.nextToken());
			}
			index++;
		}

		// initialize board
		board = new TileColor[w][h];
		for(int nX = 0; nX < board.length; nX++) {
			for(int mX = 0; mX < board[nX].length; mX++) {
				board[nX][mX] = TileColor.UNDEFINED;
			}
		}
	}
	
	public boolean solve() {

		boolean solved = false;
		Coord coord = new Coord();
		count = 0;
/*		
 		This should go in a unit test some day.
 
		// test steps
		try {
			while(true) {
				coord.moveRight();
				System.out.println("One right step made, now at " + coord.toString());
			}
		} catch(ArrayIndexOutOfBoundsException ie) {
			System.out.println(ie.getMessage());
		}
		try {
			while(true) {
				coord.moveLeft();
				System.out.println("One left step made, now at " + coord.toString());
			}
		} catch(ArrayIndexOutOfBoundsException ie) {
			System.out.println(ie.getMessage());
		}
*/
/*
		// test hints
		board[0][0] = Color.WHITE;
		board[1][0] = Color.WHITE;
		board[1][1] = Color.UNDEFINED;
		board[0][1] = Color.BLACK;
		printBoard();
		System.out.println("Legal " + coord.toString() + ": " + isLegal(coord));
		Coord c = new Coord(1, 1);
		System.out.println("Legal " + c.toString() + ": " + isLegal(c));
		c = new Coord(1, 0);
		System.out.println("Legal " + c.toString() + ": " + isLegal(c));
*/
		
		while(!solved) {
			
			try {
				if(board[coord.x][coord.y] == TileColor.UNDEFINED) {
					// first try making it white
					board[coord.x][coord.y] = TileColor.WHITE;
					if(isLegal(coord)) {
						coord.moveRight();
					}
				} else if(board[coord.x][coord.y] == TileColor.WHITE) {
					// white doesn't work, try black
					board[coord.x][coord.y] = TileColor.BLACK;
					if(isLegal(coord)) {
						coord.moveRight();
					}
				} else {
					// black didn't work either, move back left
					board[coord.x][coord.y] = TileColor.UNDEFINED;
					coord.moveLeft();
				}
			} catch(UpperLimitExceededException le) {
				solved = true;
			} catch(LowerLimitExceededException le) {
				break;
			}
			
			count++;
		}
		
		return solved;
	}
	
	/**
	 * Returns false if the x and y column of this coordinate violate the
	 * puzzle's hints. True otherwise.
	 * 
	 * @param coord
	 * @return
	 */
	private boolean isLegal(Coord coord) {
		
		return isRowLegal(coord) && isColLegal(coord);
	}
	
	private boolean isRowLegal(Coord coord) {
		
		boolean prevWasBlack = false;
		int hintNum = -1;	// which hint are we processing
		int consecCount = 0;	// how many consecutive black tiles so far
		int nX;

		for(nX = 0; nX < board.length; nX++) {
			if(board[nX][coord.y] == TileColor.BLACK) {
				if(!prevWasBlack) {
					// new black sequence encountered
					hintNum++;
				}
				consecCount++;
				if(hintNum >= xHints[coord.y].length || consecCount > xHints[coord.y][hintNum]) {
					// there are more black tile strips than there are hints,
					// or the current black strip is larger than the hints permit
					return false;
				}
				prevWasBlack = true;
			} else if(board[nX][coord.y] == TileColor.WHITE) {
				if(prevWasBlack) {
					if(consecCount != xHints[coord.y][hintNum]) {
						return false;
					}
					prevWasBlack = false;
					consecCount = 0;
				}
			} else {
				// if it's still legal when we encounter the first undefined
				// tile, the entire row is legal
				break;
			}
		}
		if(nX == board.length) {
			// the entire row is filled, so the hints must be completey matched
			if(hintNum == (xHints[coord.y].length - 1)) {
				
				if(prevWasBlack) {
					// we haven't done the check for the last black strip, so
					// do it now
					if(consecCount == xHints[coord.y][xHints[coord.y].length-1]) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isColLegal(Coord coord) {
		
		boolean prevWasBlack = false;
		int hintNum = -1;	// which hint are we processing
		int consecCount = 0;	// how many consecutive black tiles so far
		int nX;

		for(nX = 0; nX < board[coord.x].length; nX++) {
			if(board[coord.x][nX] == TileColor.BLACK) {
				if(!prevWasBlack) {
					// new black sequence encountered
					hintNum++;
				}
				consecCount++;
				if(hintNum >= yHints[coord.x].length || consecCount > yHints[coord.x][hintNum]) {
					// there are more black tile strips than there are hints,
					// or the current black strip is larger than the hints permit
					return false;
				}
				prevWasBlack = true;
			} else if(board[coord.x][nX] == TileColor.WHITE) {
				if(prevWasBlack) {
					if(consecCount != yHints[coord.x][hintNum]) {
						return false;
					}
					prevWasBlack = false;
					consecCount = 0;
				}
			} else {
				// if it's still legal when we encounter the first undefined
				// tile, the entire row is legal
				return true;
			}
		}
		if(nX == board.length) {
			// the entire row is filled, so the hints must be completey matched
			if(hintNum == (yHints[coord.x].length - 1)) {
				
				if(prevWasBlack) {
					// we haven't done the check for the last black strip, so
					// do it now
					if(consecCount == yHints[coord.x][yHints[coord.x].length-1]) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a defensive copy of the game's board as a two-dimensional array
	 * of <tt>TileColor</tt> instances. If this method is called
	 * after the <tt>solve()</tt> was called successfully, the board will
	 * contain the puzzle's solution. However, if <tt>solve()</tt> has not yet
	 * been called, or if <tt>solve()</tt> returned <tt>false</tt> to indicate
	 * that the puzzle is insoluble, the returned board will only contain
	 * references to <tt>TileColor.UNDEFINED</tt>.
	 * 
	 * @return
	 */
	public TileColor[][] getBoard() {

		TileColor[][] copyBoard;
		
		copyBoard = new TileColor[board.length][];
		for(int nX = 0; nX < board.length; nX++) {

			copyBoard[nX] = new TileColor[board[nX].length];
			System.arraycopy(board[nX], 0, copyBoard[nX], 0, board[nX].length);
		}
		
		return copyBoard;
	}
	
	public int[][] getTopHints() {
		
		return copyTwoDimIntArray(yHints);
	}
	
	public int[][] getLeftHints() {
		
		return copyTwoDimIntArray(xHints);
	}
	
	private int[][] copyTwoDimIntArray(int[][] arr) {
		
		int[][] copy;
		
		copy = new int[arr.length][];
		for(int nX = 0; nX < arr.length; nX++) {

			copy[nX] = new int[arr[nX].length];
			System.arraycopy(arr[nX], 0, copy[nX], 0, arr[nX].length);
		}
		
		return copy;
	}
	
/*
 	TODO: put this in a unit test
	// . . . . # . . . . # 1 4
	private void testXLegal() {
		
		board = new Color[10][1];
		board[0][0] = Color.WHITE;
		board[1][0] = Color.WHITE;
		board[2][0] = Color.BLACK;
		board[3][0] = Color.WHITE;
		board[4][0] = Color.WHITE;
		board[5][0] = Color.WHITE;
		board[6][0] = Color.BLACK;
		board[7][0] = Color.BLACK;
		board[8][0] = Color.BLACK;
		board[9][0] = Color.BLACK;
		
		xHints = new int[1][2];
		xHints[0][0] = 1;
		xHints[0][1] = 4;
		
		System.out.println(isXLegal(new Coord(9, 0)));
	}
*/
	private class Coord {
		public int x, y;
		
		public Coord() {
			this(0, 0);
		}
		
		public Coord(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void moveRight() throws UpperLimitExceededException {

			int newX = x + 1;
			int newY = y;

			if(newX >= board.length) {
				newX = 0;
				newY++;
				if(newY >= board[newX].length) {
					throw new UpperLimitExceededException("Cannot step beyond the puzzle's last tile (which is " + toString() + ").");
				}
			}
			
			x = newX;
			y = newY;
		}

		public void moveLeft() throws LowerLimitExceededException {
			
			int newX = x - 1;
			int newY = y;
			
			if(newX < 0) {
				newX = board.length - 1;
				newY--;
				if(newY < 0) {
					throw new LowerLimitExceededException("Cannot step before the puzzle's first tile (which is " + toString() + ").");
				}
			}
			
			x = newX;
			y = newY;
		}
		
		/**
		 * Returns "(x, y)"
		 */
		public String toString() {
			
			return "(" + x + ", " + y + ")";
		}
	}
}
