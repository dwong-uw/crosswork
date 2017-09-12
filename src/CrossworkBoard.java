import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CrossworkBoard extends Component {
	
	private static final long serialVersionUID = 7076735823639347648L;
	
	int GRID_SIZE = 20;
	int boardSize = 16;
	
	final String NO_MATCHES_MSG = "(no matches)";
	final int MAX_UNDOS = 500;
	
	CrossworkBoardState boardState;
	WordFinder finder;
	CrossworkBoardState[] saveStates;
	
	List<CrossworkBoardState> undoStack;
	List<CrossworkBoardState> redoStack;
	
	JList<XWord> listAccessory;
	JLabel slotLabelAccessory;
	JLabel statusLabelAccessory;
	JTextField consoleTextAccessory;
	
    Color gridColor;
    Color cursorColor;
    Color selectionColor;
    Color blockCursorColor;
    
    Color lockedColor;
    Color junkColor;
    Color editColor;
    Color tempColor;
    
    Color lostFocusFontColor;
    Color hasFocusFontColor;
    Color lostFocusBgColor;
    Color hasFocusBgColor;
    
    Color newStatusFontColor;
    Color oldStatusFontColor;
    
    Color constructionColor;
    Color busyColor;
    
    Font font;
    Font numberFont;
    
    int sel_x = -1;
    int sel_y = -1;
    
    
    boolean xHighlight = true;
    
    boolean autocompleteFocusMode = false;
    String focusedEditWord = "";
    int focused_x = -1;
    int focused_y = -1;
    boolean focused_isHorizontal = true;
    
    boolean constructionMode = false;
    boolean numberDisplayMode = false;
    
    boolean newStatus = false;
    boolean isLoading = false;
    
    int slotIdx = -1;
    
    Set<String> banList;
    
    CrossworkProgressListener progressListener;
    
    File saveFile;
	
	/*
	 * initialization
	 */
	
    public CrossworkBoard () {
    	super();
    	
    	this.setFocusable(true);
    	this.setFocusTraversalKeysEnabled(false);
    	
    	saveFile = new File(getClass().getResource("savestates.sav").getPath());

    	boardState = new CrossworkBoardState(boardSize);
    	this.loadSaveStates();
    	
    	undoStack = new ArrayList<CrossworkBoardState>();
    	redoStack = new ArrayList<CrossworkBoardState>();
    	
    	int c = 192;
    	gridColor = new Color(c,c,c);
    	cursorColor = new Color(206, 220, 235);
    	selectionColor = new Color(226, 240, 255);
    	blockCursorColor = new Color(113, 120, 128);
    	
    	c = 192;
    	editColor = new Color(c,c,c);
    	lockedColor = Color.BLACK;
    	junkColor = Color.RED;
    	tempColor = new Color(144, 164, 188);
    	
    	c = 160;
        lostFocusFontColor = new Color(c,c,c);
        hasFocusFontColor = Color.BLACK;
        c = 208;
        lostFocusBgColor = new Color(c,c,c);
        c = 226;
        hasFocusBgColor = new Color(c,c,c);
        
        c = 160;
        oldStatusFontColor = new Color(c,c,c);
        newStatusFontColor = Color.BLACK;
        
        constructionColor = new Color(178,182,238);
        busyColor = new Color(255,161,140);

    	font = new Font("Calibri", Font.BOLD, 18);
    	numberFont = new Font("Calibri", Font.PLAIN, 10);
    	
    	banList = new HashSet<String>();
    	
    	progressListener = new CrossworkProgressListener() {
			void updateDictionaryProgress(double progress) {
    			displayStatus("Loading dictionary... ("+(int)(progress*100)+"% complete)");
    			statusLabelAccessory.setOpaque(true);
    			statusLabelAccessory.setBackground(busyColor);
			}

			void dictionaryLoadingComplete() {
    			displayStatus("Welcome to Crosswork!");
    			isLoading = false;
    			statusLabelAccessory.setBackground(constructionColor);
    			statusLabelAccessory.setOpaque(false);
			}

			void updateSortingProgress(double progress) {
    			displayStatus("Sorting words... ("+(int)(progress*100)+"% complete)");
    			statusLabelAccessory.setOpaque(true);
    			statusLabelAccessory.setBackground(busyColor);
			}

			void sortingComplete(Set<XWord> sortedList) {
				doneSortingWordList(sortedList);
    			displayStatus("Done sorting words.");
    			isLoading = false;
    			statusLabelAccessory.setBackground(constructionColor);
    			statusLabelAccessory.setOpaque(false);
			}
		};
    }
    
    public void loadDictionary() {
    	finder = new WordFinder(progressListener);
    }
    
    public void setGridSize(int s) {
    	if (GRID_SIZE != s) {
    		GRID_SIZE = s;
    		repaint();
    	}
    }

    /*
     * Board drawing
     */

    public void displayStatus(String s) {
    	statusLabelAccessory.setText(s);
		statusLabelAccessory.setForeground(newStatusFontColor);
		newStatus = true;
    }
    
    public Dimension getPreferredSize(){
        return new Dimension((int)(GRID_SIZE*boardSize+1), (int)(GRID_SIZE*boardSize+1));
    }
    
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, GRID_SIZE*boardSize, GRID_SIZE*boardSize);
        
        // draw grid
        g2.setColor(gridColor);
        for (int i = 0 ; i < boardSize+1; i++) {
            g2.draw(new Line2D.Double(0, GRID_SIZE*i, GRID_SIZE*boardSize, GRID_SIZE*i));
            g2.draw(new Line2D.Double(GRID_SIZE*i, 0, GRID_SIZE*i, GRID_SIZE*boardSize));
        }
        
        // draw highlighted squares
        g2.setColor(selectionColor);
        if (sel_x > -1 && sel_y > -1 && !constructionMode) {
    		int start = boardState.findStartOfWord(xHighlight, sel_x, sel_y);
        	int currx = start/1000000;
        	int curry = start%1000000;
        	
        	while (boardState.get(currx, curry) != boardState.END) {
                g2.fillRect(currx*GRID_SIZE+1, curry*GRID_SIZE+1, GRID_SIZE-1, GRID_SIZE-1);
                if (xHighlight) currx++;
                else curry++;
        	}
        }
        
        // draw cursor square
        g2.setColor(cursorColor);
        if (sel_x > -1 && sel_y > -1) {
            g2.fillRect(sel_x*GRID_SIZE+1, sel_y*GRID_SIZE+1, GRID_SIZE-1, GRID_SIZE-1);
        }
        
        // iterate across model
        int currNumber = 1;
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j < boardSize; j++) {
            	char c = boardState.get(j, i);
            	if (c == boardState.END) {
                    g2.setColor(Color.BLACK);
            		if (sel_x == j && sel_y == i) {
            			g2.setColor(blockCursorColor);
            		}
            		g2.fillRect(j*GRID_SIZE, i*GRID_SIZE, GRID_SIZE, GRID_SIZE);
            		
            		continue;
            	}
            	if (c != boardState.BLANK) {
            		Color letterColor = editColor;
            		if ((boardState.getState(j, i) & boardState.WORD_STATE_LOCKED) > 0) {
            			letterColor = lockedColor;
            		}
            		else if ((boardState.getState(j, i) & boardState.WORD_STATE_TEMP) > 0) {
            			letterColor = tempColor;
            		}
            		else if ((boardState.getState(j, i) & boardState.WORD_STATE_JUNK) > 0) {
            			letterColor = junkColor;
            		}
            		if (numberDisplayMode) {
            			double fadePercentage = 0.90;
                		letterColor = new Color(
                				(int)(255*fadePercentage+letterColor.getRed()*(1-fadePercentage)),
                				(int)(255*fadePercentage+letterColor.getGreen()*(1-fadePercentage)),
                				(int)(255*fadePercentage+letterColor.getBlue()*(1-fadePercentage))
                				);
            		}
            		drawLetter(g2, c, j, i, letterColor);
            	}
            	if (numberDisplayMode) {
            		if ((boardState.get(j-1, i) == boardState.END && boardState.get(j+1, i) != boardState.END) ||
            			(boardState.get(j, i-1) == boardState.END && boardState.get(j, i+1) != boardState.END)
            				) {
            			drawNumber(g2, currNumber, j, i, Color.BLACK);
            			currNumber++;
            		}
            	}
        	}
        }
        
    }
    
    private void drawLetter(Graphics2D g2, char c, int col, int row, Color color) {
    	FontMetrics fm = g2.getFontMetrics();
    	Color origColor = g2.getColor();
    	g2.setColor(color);
    	g2.drawString(c+"",
    			col*GRID_SIZE+(GRID_SIZE-fm.charWidth(c))/2.0f,
    			row*GRID_SIZE+fm.getAscent()+(GRID_SIZE-fm.getAscent())/2.0f-1.0f);
    	g2.setColor(origColor);
    }
    
    private void drawNumber(Graphics2D g2, int number, int col, int row, Color color) {
    	g2.setFont(numberFont);
    	FontMetrics fm = g2.getFontMetrics();
    	Color origColor = g2.getColor();
    	g2.setColor(color);
    	g2.drawString(number+"",
    			col*GRID_SIZE+1,
    			row*GRID_SIZE+fm.getAscent());
    	g2.setColor(origColor);
    	g2.setFont(font);
    }
    
    /*
     * Board functions
     */
    
    public void inputLetter(char letter) {
		saveUndoState();
		boardState.inputLetter(letter, sel_x, sel_y);
		advanceCursor(xHighlight, true, false);
    }
    
    public void loadState(int stateIdx) {
		saveUndoState();
		int saveSlot = stateIdx;
		boardState = new CrossworkBoardState(saveStates[saveSlot].getStateString());
		slotIdx = saveSlot;
		slotLabelAccessory.setText("Slot " + slotIdx);
		displayStatus("LOADING slot " + saveSlot);
    }
    
    public void saveState(int stateIdx) {
		int saveSlot = stateIdx;
		saveSaveState(saveSlot);
		slotIdx = saveSlot;
		slotLabelAccessory.setText("Slot " + slotIdx);
		displayStatus("SAVED in slot " + saveSlot);
    }
    
    public void undo() {
		if (undoStack.size() > 0) {
			redoStack.add(boardState);
			boardState = undoStack.remove(undoStack.size()-1);
		}
    }
    
    public void redo() {
		if (redoStack.size() > 0) {
			undoStack.add(boardState);
			boardState = redoStack.remove(redoStack.size()-1);
		}
    }
    
    public void deleteLetter() {
		saveUndoState();
		boardState.inputLetter(boardState.BLANK, sel_x, sel_y);
		boardState.unlock(sel_x, sel_y);
    }
    
    public void backspace() {
    	deleteLetter();
		advanceCursor(xHighlight, false, false);
    }
    
    // deletes all letters in current word that are part of perpendicular words who are not fully locked
    public void deleteLettersOfUnfinalizedWords() {
		this.saveUndoState();
		int idx = boardState.findStartOfWord(xHighlight, sel_x, sel_y);
		int col = idx/1000000;
		int row = idx%1000000;
		
		while (boardState.get(col, row) != boardState.END) {
			
			// check if letters in the perpendicular word are finalized (all filled-in, all locked)
			
			int pidx = boardState.findStartOfWord(!xHighlight, col, row);
			int pcol = pidx/1000000;
			int prow = pidx%1000000;
			boolean deletable = false;
			char c;
			int squareCount = 0;
			
			while ((c = boardState.get(pcol, prow)) != boardState.END) {
				if (c == boardState.BLANK || boardState.getState(pcol, prow) != boardState.WORD_STATE_LOCKED) {
					deletable = true;
					break;
				}
				
				if (!xHighlight) pcol++;
				else prow++;
				
				squareCount++;
			}
			
			// delete if perpendicular word is unfinalized
			
			if (deletable || squareCount == 1) {
				boardState.inputLetter(boardState.BLANK, col, row);
				boardState.unlock(col, row);
			}
			
			if (xHighlight) col++;
			else row++;
		}
    }
    
    public void cursorUp() {
		sel_y--;
		if (sel_y < 0) {
			sel_y = boardSize - 1;
		}
    }
    
    public void cursorDown() {
		sel_y++;
		if (sel_y >= boardSize) {
			sel_y = 0;
		}
    }
    
    public void cursorLeft() {
		sel_x--;
		if (sel_x < 0) {
			sel_x = boardSize - 1;
			sel_y--;
			if (sel_y < 0) sel_y = boardSize - 1;
		}
    }
    
    public void cursorRight() {
		sel_x++;
		if (sel_x >= boardSize) {
			sel_x = 0;
			sel_y++;
			if (sel_y >= boardSize) sel_y = 0;
		}    	
    }
    
    public void toggleConstructionBlock() {
		this.saveUndoState();
		if (boardState.get(sel_x, sel_y) == boardState.END) {
    		boardState.set(boardState.BLANK, sel_x, sel_y);
    		boardState.set(boardState.BLANK, boardSize-sel_x-1, boardSize-sel_y-1);
		} else {
    		boardState.set(boardState.END, sel_x, sel_y);
    		boardState.set(boardState.END, boardSize-sel_x-1, boardSize-sel_y-1);
		}
		repaint();
    }
    
    public void requestSortWordList() {
		if (!autocompleteFocusMode) {
			if (!autocomplete()) return;
		}

		DefaultListModel<XWord> wordList = (DefaultListModel<XWord>)listAccessory.getModel();
		sortWordList(wordList);
		listAccessory.setModel(wordList);
		listAccessory.revalidate();
		listAccessory.requestFocus();
		listAccessory.setSelectedIndex(0);
    }
    
    public void toggleConstructionMode() {
		constructionMode ^= true;
		displayStatus("Construction mode " + (constructionMode ? "ON" : "OFF"));
		statusLabelAccessory.setBackground(constructionColor);
		slotLabelAccessory.setBackground(constructionColor);
		statusLabelAccessory.setOpaque(constructionMode);
		slotLabelAccessory.setOpaque(constructionMode);
    }
    
    public void toggleNumberDisplayMode() {
		numberDisplayMode ^= true;
		displayStatus("Number display " + (numberDisplayMode ? "enabled" : "disabled") + ".");
    }
    
    public void saveCurrentState() {
		if (slotIdx > -1) {
			this.saveSaveState(slotIdx);
			displayStatus("SAVED in slot " + slotIdx);
		} else {
			displayStatus("Please choose a save slot.");
		}
    }
    
    public void newBoard() {
		this.saveUndoState();
		slotIdx = -1;
		slotLabelAccessory.setText("(no slot)");
		displayStatus("Starting new project...");
		boardState = new CrossworkBoardState(boardSize);
    }
    
    public void toggleHighlightOrientation() {
		if (boardState.get(sel_x, sel_y) != boardState.END) {
			xHighlight ^= true;
		}
    }
    
    public void lockCurrentLetter() {
		this.saveUndoState();
		boardState.lock(sel_x, sel_y);
		this.advanceCursor(xHighlight, true, false);
    }
    
    public void lockCurrentWord() {
		this.saveUndoState();
		boardState.lockWord(xHighlight, sel_x, sel_y);
    }
    
    public void scrollToNextWord() {
    	skipWord(false);
    }
    
    public void scrollToPrevWord() {
    	skipWord(true);
    }
    
   	private void skipWord(boolean goBackwards) {
		if (xHighlight) {
			int last_x = sel_x;
			while (true) {
        		advanceCursor(true, !goBackwards, true);
        		if (Math.abs(sel_x - last_x) != 1) break;
        		last_x = sel_x;
			}
		} else {
    		advanceCursor(true, !goBackwards, true);
		}
    }
    
    public void scrollToBeginningOfWord() {
		int idx = boardState.findStartOfWord(xHighlight, sel_x, sel_y);
		sel_x = idx/1000000;
		sel_y = idx%1000000;
    }
    
    public void scrollToEndOfWord() {
		int old;
		do {
			old = xHighlight ? sel_x : sel_y;
			this.advanceCursor(xHighlight, true, false);
		} while (old != (xHighlight ? sel_x : sel_y));
    }
    
    public boolean autocomplete () {
    	
		String query = boardState.getWord(xHighlight, sel_x, sel_y, true);
		
		boolean needsAutocomplete = false;
		for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) != boardState.BLANK) {
				needsAutocomplete = true;
				break;
			}
		}
		
		if (needsAutocomplete) {
			if (autocompleteFocusMode) {
				this.clearAutocompleteFocusMode();
			}
			this.saveUndoState();
			
			List<String> words = finder.findAllWords(query);
    		DefaultListModel<XWord> wordList = new DefaultListModel<XWord>();
    		
			for (String w : words) {
				wordList.addElement(new XWord(w));
			}
			
			if (words.size() == 0) {
				wordList.addElement(new XWord(NO_MATCHES_MSG));
				// suggest constraint relaxations
				for (int i = 0; i < query.length(); i++) {
					char c = query.charAt(i);
					String newQuery = "";
					for (int j = 0; j < query.length(); j++) {
						newQuery += j == i ? boardState.BLANK : query.charAt(j);
					}
					int count = finder.findAllWords(newQuery).size();
					if (count > 0) {
						wordList.addElement(new XWord("Relax " + c + " (" + count + ")"));
					}
				}
    			listAccessory.setModel(wordList);
    			listAccessory.setPreferredSize(new Dimension(listAccessory.getWidth()-24, words.size()*GRID_SIZE));
    			listAccessory.revalidate();
			}
			
			if (words.size() > 0) {
    			focused_x = sel_x;
    			focused_y = sel_y;
    			focused_isHorizontal = xHighlight;
    			autocompleteFocusMode = true;
    			focusedEditWord = boardState.getWord(xHighlight, sel_x, sel_y, false);
    			
    			this.filterBannedWords(wordList);
    			listAccessory.setModel(wordList);
    			listAccessory.setPreferredSize(new Dimension(listAccessory.getWidth()-24, words.size()*GRID_SIZE));
    			listAccessory.revalidate();
    			
    			listAccessory.requestFocus();
    			listAccessory.setSelectedIndex(0);
			}
		}
		return autocompleteFocusMode;
    }
    
    public void showValidLettersForCurrentCell() {
		if (autocompleteFocusMode) return;
		if (boardState.getState(sel_x, sel_y) == boardState.WORD_STATE_LOCKED) return;
		
		char oldChar = boardState.get(sel_x, sel_y);
		
		List<Character> goodChars = new ArrayList<Character>();
		
		for (char i = 'A'; i <= 'Z'; i++) {
			boardState.inputLetter(i, sel_x, sel_y);
			
			boardState.lock(sel_x, sel_y);
			boolean isGood = finder.hasWords(boardState.getWord(true, sel_x, sel_y, true));
			if (isGood) {
				isGood = finder.hasWords(boardState.getWord(false, sel_x, sel_y, true));
			}
			boardState.unlock(sel_x, sel_y);
			
			if (isGood) {
				goodChars.add(i);
			}
		}
		boardState.set(oldChar, sel_x, sel_y);

		DefaultListModel<XWord> wordList = new DefaultListModel<XWord>();
		
		if (goodChars.size() == 26) {
			wordList.addElement(new XWord("> All letters"));
    		listAccessory.setModel(wordList);
    		listAccessory.revalidate();
			return;
		}
		
		int goodVowelCount = 0;
		if (goodChars.contains('A')) goodVowelCount++;
		if (goodChars.contains('E')) goodVowelCount++;
		if (goodChars.contains('I')) goodVowelCount++;
		if (goodChars.contains('O')) goodVowelCount++;
		if (goodChars.contains('U')) goodVowelCount++;
		if (goodChars.contains('Y')) goodVowelCount++;
		
		if (goodVowelCount == 6) {
			wordList.addElement(new XWord("> All vowels"));
		} else {
			if (goodChars.contains('A')) wordList.addElement(new XWord("A"));
			if (goodChars.contains('E')) wordList.addElement(new XWord("E"));
			if (goodChars.contains('I')) wordList.addElement(new XWord("I"));
			if (goodChars.contains('O')) wordList.addElement(new XWord("O"));
			if (goodChars.contains('U')) wordList.addElement(new XWord("U"));
			if (goodChars.contains('Y')) wordList.addElement(new XWord("Y"));
		}
		
		if (goodChars.size() - goodVowelCount > 10) {
			wordList.addElement(new XWord("> All cons"));
			for (char i = 'A'; i <= 'Z'; i++) {
				if (i != 'A' && i != 'E' && i != 'I' && i != 'O' && i != 'U' && i != 'Y') {
        			if (!goodChars.contains(i)) {
            			wordList.addElement(new XWord("not " + i));
        			}
				}
			}
		} else {
			for (Character c : goodChars) {
				if (c != 'A' && c != 'E' && c != 'I' && c != 'O' && c != 'U' && c != 'Y') {
        			wordList.addElement(new XWord(c+""));
				}
			}
		}
		if (wordList.isEmpty()) {
			wordList.addElement(new XWord(NO_MATCHES_MSG));
		}
		
		listAccessory.setModel(wordList);
		listAccessory.revalidate();
    }
    
    public void sortAutocomplete() {
		if (!autocompleteFocusMode) return;
		DefaultListModel<XWord> wordList = (DefaultListModel<XWord>)listAccessory.getModel();
		this.sortWordList(wordList);
		listAccessory.setModel(wordList);
		listAccessory.revalidate();
		listAccessory.requestFocus();
		listAccessory.setSelectedIndex(0);
    }
    
    /*
     * UI handling
     */
    
    public void mousePressed (int x, int y) {
    	if (isLoading) return;
    	
    	if (sel_x == x/GRID_SIZE && sel_y == y/GRID_SIZE) {
    		xHighlight ^= true;
    		repaint();
    	}
    	sel_x = x/GRID_SIZE;
    	sel_y = y/GRID_SIZE;
    	this.requestFocus();
    	repaint();
    }
    
    public boolean requestKeypress() {
    	
    	if (isLoading) return false;
    	
    	if (newStatus) {
    		statusLabelAccessory.setForeground(oldStatusFontColor);
    		newStatus = false;
    	}
    	
    	if (sel_x < 0 || sel_y < 0) return false;
    	
    	return true;
    }
    
    /*
     * private helpers
     */
    
    private void advanceCursor(boolean horizontal, boolean forward, boolean skipBlocks) {
    	if (!skipBlocks && boardState.get(
    			horizontal ? (forward ? sel_x + 1 : sel_x - 1) : sel_x,
				!horizontal ? (forward ? sel_y + 1 : sel_y - 1) : sel_y
						) == boardState.END) return;
    	if (horizontal) {
    		do {
    			sel_x += forward ? 1 : -1;
    			if (sel_x < 0) {
    				sel_x = boardSize-1;
    				sel_y--;
    				if (sel_y < 0) {
    					sel_x = boardSize-1;
    					sel_y = boardSize-1;
    				}
    			} else if (sel_x >= boardSize) {
    				sel_x = 0;
    				sel_y++;
    				if (sel_y >= boardSize) {
    					sel_x = 0;
    					sel_y = 0;
    				}
    			}
    		} while (skipBlocks && boardState.get(sel_x, sel_y) == boardState.END);
    	} else {
    		do {
				sel_y += forward ? 1 : -1;
				if (sel_y < 0) {
					sel_y = boardSize-1;
					sel_x--;
					if (sel_x < 0) {
						sel_x = boardSize-1;
						sel_y = boardSize-1;
					}
				} else if (sel_y >= boardSize) {
					sel_y = 0;
					sel_x++;
					if (sel_x >= boardSize) {
						sel_x = 0;
						sel_y = 0;
					}
				}
    		} while (skipBlocks && boardState.get(sel_x, sel_y) == boardState.END);
    	}
    }
    
    public void clearAutocompleteFocusMode() {
    	if (autocompleteFocusMode) {
        	boardState.inputWord(focusedEditWord, focused_isHorizontal, focused_x, focused_y);
        	boardState.untempWord(focused_isHorizontal, focused_x, focused_y);
    	}
    	autocompleteFocusMode = false;
    	focusedEditWord = "";
    	focused_x = -1;
    	focused_y = -1;
    	
		listAccessory.setModel(new DefaultListModel<XWord>());
		listAccessory.setPreferredSize(new Dimension(listAccessory.getWidth()-24, 96));
		listAccessory.revalidate();
    }
    
    private void saveUndoState() {
    	if (autocompleteFocusMode) return;
    	if (undoStack.size() > 0 && undoStack.get(undoStack.size()-1).getStateString().equals(boardState.getStateString())) return;
    	
    	if (undoStack.size() == MAX_UNDOS) {
    		undoStack.remove(0);
    	}
    	undoStack.add(new CrossworkBoardState(boardState.getStateString()));
    	redoStack.clear();
    }
    
    private void sortWordList(DefaultListModel<XWord> list) {
    	isLoading = true;
    	WordListSorterThread t = new WordListSorterThread(list, boardState, finder, focused_isHorizontal, focused_x, focused_y, progressListener);
    	t.start();
    }
    
    public void doneSortingWordList(Set<XWord> scoredWords) {
    	DefaultListModel<XWord> list = new DefaultListModel<XWord>();
    	for (XWord w : scoredWords) {
    		list.addElement(w);
    	}
    	listAccessory.setModel(list);
    	listAccessory.setSelectedIndex(0);
    }
    
    private void filterBannedWords(DefaultListModel<XWord> list) {
    	List<XWord> buffer = new ArrayList<XWord>();
    	
    	for (int i = 0; i < list.getSize(); i++) {
    		XWord w = list.elementAt(i);
    		if (w.state == XWord.BANNED_STATE) {
        		if (!banList.contains(w.word)) {
        			w.state = XWord.NORMAL_STATE;
        		}
    		} else {
        		if (banList.contains(w.word)) {
        			w.state = XWord.BANNED_STATE;
        			buffer.add(w);
        		}
    		}
    	}
    	
    	for (XWord b : buffer) {
			list.removeElement(b);
			list.addElement(b);
    	}
    }
    
    private void saveSaveState(int slot) {
    	saveStates[slot] = new CrossworkBoardState(boardState.getStateString());
		try {
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile)));
			
			for (int i = 0; i < saveStates.length; i++) {
				w.write(saveStates[i].getStateString());
				if (i != saveStates.length-1) {
					w.write("$");
				}
			}
			
			w.close();
		} catch (IOException e) {
			System.err.println("Error writing save file");
		}
    }
    
    private void loadSaveStates() {
    	saveStates = new CrossworkBoardState[10];
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(saveFile))));
			
			// iterate through file
			String currLine;
			while ((currLine = r.readLine()) != null) {
				// interpret line
				String[] s = currLine.split("\\$");
				for (int i = 0; i < saveStates.length; i++) {
					saveStates[i] = new CrossworkBoardState(s[i]);
				}
			}
			// done reading file
			r.close();
		} catch (IOException e) {
			System.err.println("Error reading save file.");
			saveStates = null;
		}
    }

    /*
     * word list accessory methods
     */
    
    public void setListAccessory(JList<XWord> la) {
    	listAccessory = la;
    	la.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				// ESC
	    		if (keyCode == 27) {
					clearAutocompleteFocusMode();
	    			requestFocus();
	    			repaint();
	    		}
	    		// ENTER
	    		if (keyCode == 10) {
	    			if (!autocompleteFocusMode) return;
	    			
	    			String word = listAccessory.getModel().getElementAt(listAccessory.getSelectedIndex()).word;
	    			boolean finalized_isHorizontal = focused_isHorizontal;
	    			int finalized_x = focused_x;
	    			int finalized_y = focused_y;
	    			clearAutocompleteFocusMode();
	    			boardState.inputWord(word, finalized_isHorizontal, finalized_x, finalized_y);
	    			requestFocus();
	    			repaint();
	    		}
				// DEL
	    		if (keyCode == 127) {
	    			int idx = listAccessory.getSelectedIndex();
	    			String word = listAccessory.getModel().getElementAt(idx).word;
	    			if (banList.contains(word)) {
	    				banList.remove(word);
	    			} else {
		    			banList.add(word);
	    			}
	    			filterBannedWords((DefaultListModel<XWord>)listAccessory.getModel());
	    			listAccessory.setSelectedIndex(idx);
	    			listAccessory.revalidate();
	    		}
				// INSERT
	    		if ((keyCode == 155) ||
		    		(keyCode == 32 && (e.getModifiers() & 3) == 1)){
	    			
	    			if (!autocompleteFocusMode) return;
	    			DefaultListModel<XWord> wordList = (DefaultListModel<XWord>)listAccessory.getModel();
	    			sortWordList(wordList);
	    			
	    			listAccessory.setModel(wordList);
	    			listAccessory.revalidate();
	    			
	    			listAccessory.requestFocus();
	    			listAccessory.setSelectedIndex(0);

	    		}
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
    	
    	la.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!autocompleteFocusMode) return;
				if (listAccessory.getSelectedIndex() < 0) return;
				if (listAccessory.getModel().getElementAt(0).equals(NO_MATCHES_MSG)) return;
				
    			String word = listAccessory.getModel().getElementAt(listAccessory.getSelectedIndex()).word;
    			boardState.tempWord(focused_isHorizontal, focused_x, focused_y);
    			boardState.inputWord(word, focused_isHorizontal, focused_x, focused_y);
    			repaint();
			}
		});
    	
    }
    
    public void setSlotLabelAccessory (JLabel label) {
    	slotLabelAccessory = label;
    }
    
    public void setStatusLabelAccessory (JLabel label) {
    	statusLabelAccessory = label;
    }
    
    public void setConsoleTextAccessory (JTextField field) {
    	consoleTextAccessory = field;
    	field.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				consoleTextAccessory.setForeground(lostFocusFontColor);
				consoleTextAccessory.setBackground(lostFocusBgColor);
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				consoleTextAccessory.setForeground(hasFocusFontColor);
				consoleTextAccessory.setBackground(hasFocusBgColor);
			}
		});
    	field.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
			}
			
			public void keyReleased(KeyEvent e) {
			}
			
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				// ENTER
				if (keyCode == 10) {
					clearAutocompleteFocusMode();
					String query = consoleTextAccessory.getText();
					List<String> results = finder.findAllWords(query);
					
	        		DefaultListModel<XWord> wordList = new DefaultListModel<XWord>();
					if (results == null || results.size() == 0) {
						wordList.addElement(new XWord(NO_MATCHES_MSG));
					} else {
						for (String r : results) {
			        		wordList.addElement(new XWord(r));
						}
					}
	    			listAccessory.setModel(wordList);
	    			listAccessory.setPreferredSize(new Dimension(listAccessory.getWidth()-24, results.size()*GRID_SIZE));
	    			listAccessory.revalidate();
				}
				
				// ESC
				else if (keyCode == 27) {
					clearAutocompleteFocusMode();
					requestFocus();
					consoleTextAccessory.setText("");
				}
			}
		});
    }
    
}
