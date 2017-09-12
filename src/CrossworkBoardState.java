
public class CrossworkBoardState {
	
	char[][] board;		// [col#][row#]
	byte[][] state;		// [col#][row#]
	int boardSize;
	
	final byte WORD_STATE_LOCKED = 1;
	final byte WORD_STATE_JUNK = 2;	
	final byte WORD_STATE_TEMP = 4;
	
	public final char BLANK = '*';
	public final char END = '|';
	
	public CrossworkBoardState (int boardSize) {
		this.boardSize = boardSize;
		board = new char[boardSize][boardSize];
		state = new byte[boardSize][boardSize];
		
		for (int i = 0; i < boardSize; i++) {
			board[i][0] = END;
			board[i][boardSize-1] = END;
			board[0][i] = END;
			board[boardSize-1][i] = END;
			for (int j = 0; j < boardSize; j++) state[i][j] = 0;
		}
	}
	
	public CrossworkBoardState (String stateString) {
		boardSize = (int)Math.sqrt(stateString.length()/2);
		board = new char[boardSize][boardSize];
		state = new byte[boardSize][boardSize];
		
		for (int i = 0; i < boardSize*boardSize; i++) {
			this.set(stateString.charAt(i*2), i%boardSize,i/boardSize);
			state[i%boardSize][i/boardSize] = (byte)(stateString.charAt(i*2+1)-'0'); 
		}
	}
	
	public void inputLetter(char letter, int col, int row) {
		if (letter >= 'a' && letter <= 'z') letter -= 'a'-'A';
		
		if (letter < 'A' || letter > 'Z') {
			if (letter != BLANK)
			return;
		}
		
		if (get(col,row) != END) {
			set(letter, col, row);
		}
	}
	
	public void inputWord(String word, boolean horizontal, int col, int row) {
		int idx = this.findStartOfWord(horizontal, col, row);
		col = idx/1000000;
		row = idx%1000000;
		
		for (int i = 0; i < word.length(); i++) {
			if (state[col][row] != WORD_STATE_LOCKED) {
				this.inputLetter(word.charAt(i), col, row);
			}
			
			if (horizontal) col++;
			else row++;
		}
	}
	
	public void tempWord(boolean horizontal, int col, int row) {
		int idx = this.findStartOfWord(horizontal, col, row);
		col = idx/1000000;
		row = idx%1000000;
		
		while (board[col][row] != END) {
			state[col][row] |= WORD_STATE_TEMP;
			
			if (horizontal) col++;
			else row++;
		}
	}
	
	public void untempWord(boolean horizontal, int col, int row) {
		int idx = this.findStartOfWord(horizontal, col, row);
		col = idx/1000000;
		row = idx%1000000;
		
		while (board[col][row] != END) {
			state[col][row] &= Byte.MAX_VALUE-WORD_STATE_TEMP;
			
			if (horizontal) col++;
			else row++;
		}
	}
	
	public char get(int col, int row) {
		char c = board[col][row];
		if ((c >= 'A' && c <= 'Z') || c == END) return c;
		return BLANK;
	}
	
	public byte getState(int col, int row) {
		return state[col][row];
	}
	
	public void set(char c, int col, int row) {
		board[col][row] = c;
	}
	
	public void lock(int col, int row) {
		if (board[col][row] != BLANK && board[col][row] != END) {
			state[col][row] ^= WORD_STATE_LOCKED;
		}
	}
	
	public void unlock(int col, int row) {
		if (board[col][row] != END) {
			state[col][row] &= Byte.MAX_VALUE ^ WORD_STATE_LOCKED;
		}
	}
	
	public void lockWord(boolean horizontal, int col, int row) {
		int i = this.findStartOfWord(horizontal, col, row);
		col = i/1000000;
		row = i%1000000;
		
		boolean lock = false;
		
		int tempc = col;
		int tempr = row;
		while (board[tempc][tempr] != END) {
			if ((state[tempc][tempr] & WORD_STATE_LOCKED) != WORD_STATE_LOCKED) {
				lock = true;
				break;
			}
			if (horizontal) tempc++;
			else tempr++;
		}
		
		while (board[col][row] != END) {
			if (lock) {
				if (board[col][row] != BLANK && board[col][row] != END) {
					state[col][row] |= WORD_STATE_LOCKED;
				}
			} else {
				state[col][row] ^= WORD_STATE_LOCKED;
			}
			if (horizontal) col++;
			else row++;
		}
	}
	
	public String getStateString() {
		String stateString = "";
		for (int i = 0; i < boardSize*boardSize; i++) {
			stateString += get(i%boardSize,i/boardSize);
			stateString += (char)(state[i%boardSize][i/boardSize]+'0');
		}
		return stateString;
	}
	
	public String getWord(boolean horizontal, int col, int row, boolean lockedOnly) {
		String word = "";
		if (board[col][row] == END) return "";
		
		int i = this.findStartOfWord(horizontal, col, row);
		col = i/1000000;
		row = i%1000000;
		
		char c;
		while ((c = board[col][row]) != END) {
			if ((state[col][row] & WORD_STATE_LOCKED) > 0 || !lockedOnly) {
				word += c;
			} else {
				word += BLANK;
			}
			if (horizontal) col++;
			else row++;
		}
		
		return word;
	}
	
	public int findStartOfWord(boolean horizontal, int col, int row) {
		if (board[col][row] == END) return col*1000000 + row;
		
		while ((horizontal ? board[col-1][row] : board[col][row-1]) != END) {
			if (horizontal) col--;
			else row--;
		}
		
		return col*1000000 + row;
	}
	
}
