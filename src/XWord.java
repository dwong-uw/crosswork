
public class XWord implements Comparable<XWord>{
	static final int NORMAL_STATE = 0;
	static final int RESTRICTED_STATE = 1;
	static final int BANNED_STATE = 2;
	
	public String word;
	public int state;
	public double score;	// for sorting
	
	public XWord (String s) {
		state = NORMAL_STATE;
		word = s;
	}
	
	public String toString() {
		String dispString = word;
		if (state == BANNED_STATE) {
			dispString += "(!)";
		} else if (state == RESTRICTED_STATE) {
			dispString += "(*)";
		}
		return dispString;
	}

	@Override
	public int compareTo(XWord other) {
		if (this.score < other.score) return 1;
		return -1;
	}
	
}
