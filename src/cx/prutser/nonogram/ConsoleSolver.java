package cx.prutser.nonogram;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Simple class that provides a console-based solver. It expects a puzzle
 * description in a properties file, reads it, calls the Nonogram class to
 * solve the puzzle and prints the result to standard out.
 *
 * @author	Erik van Zijst - erik@marketxs.com
 * @version	30.oct.2005
 */
public class ConsoleSolver {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		if(args.length == 1) {
			
			Properties puzzle = new Properties();
			InputStream input = new FileInputStream(args[0]);
			puzzle.load(input);
			Nonogram solver = new Nonogram(puzzle);
			System.out.println("Nonogram loaded:");
			System.out.print(formatBoard(solver.getBoard(), solver.getLeftHints(), solver.getTopHints()));
			
			long start = System.currentTimeMillis();
			if(solver.solve()) {
				System.out.println("Solved in " + (System.currentTimeMillis() - start) + "ms and " + solver.count + " evaluation cycles:");
				System.out.print(formatBoard(solver.getBoard(), solver.getLeftHints(), solver.getTopHints()));
			} else {
				System.out.println("Insoluble.");
			}
		} else {
			System.out.println("Usage: java -jar nonogram.jar puzzle.txt");
		}
	}
	
	/**
	 * Prints the board to to a string in the following "ASCII-art" format:
	 * 
	 *     # . . . 1
	 *     . # . . 1
	 *     # . # # 1 2
	 *     . . . # 1
	 *     1 1 1 2
	 *     1
	 *
	 *
	 */
	public static String formatBoard(TileColor[][] board, int[][] leftHints, int[][] topHints) {

		StringBuffer buf = new StringBuffer();
		
		for(int y = 0; y < board[0].length; y++) {
			for(int x = 0; x < board.length; x++) {
				buf.append( (board[x][y] == TileColor.BLACK ? "#" : 
					(board[x][y] == TileColor.WHITE ? "." : "?")  ) + " ");
			}
			// now print the hints for this row
			for(int nX = 0; nX < leftHints[y].length; nX++) {
				buf.append(leftHints[y][nX] + " ");
			}
			buf.append("\n");
		}
		// now print the hints for each column
		boolean moreHints = true;
		for(int hintNum = 0; moreHints; hintNum++) {
			moreHints = false;
			for(int col = 0; col < topHints.length; col++) {
				if(hintNum < topHints[col].length) {
					buf.append(topHints[col][hintNum] + " ");
					moreHints = true;
				} else {
					buf.append("  ");
				}
			}
			buf.append("\n");
		}
		
		return buf.toString();
	}
}
