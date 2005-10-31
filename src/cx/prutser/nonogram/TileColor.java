package cx.prutser.nonogram;

/**
 * This class replaces the earlier enum type. Although the enum was cleaner,
 * it is replaced by a final, immutable class to ensure java 1.3
 * compatibility.
 *
 * @author	Erik van Zijst - erik@marketxs.com
 * @version	0.2 - 01.nov.2005
 */
public final class TileColor {

	private static final int _BLACK = 0;
	private static final int _WHITE = 1;
	private static final int _UNDEFINED = 2;
	
	public static final TileColor BLACK = new TileColor(_BLACK);
	public static final TileColor WHITE = new TileColor(_WHITE);
	public static final TileColor UNDEFINED = new TileColor(_UNDEFINED);

	private final int color;
	
	private TileColor(int color) {
		
		this.color = color;
	}
	
	public boolean equals(Object o) {
	
		try {
			return ((TileColor)o).color == color;
		} catch(ClassCastException ce) {
			return false;
		}
	}
	
	public int hashCode() {

		return color;
	}
	
	public String toString() {
		
		String str;
		
		switch(color) {
			case _BLACK:
				str = "black";
				break;
			case _WHITE:
				str = "white";
				break;
			default:
				str = "undefined";
		}
		
		return str;
	}
}
