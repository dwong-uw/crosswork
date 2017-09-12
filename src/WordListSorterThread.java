import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;

public class WordListSorterThread extends Thread {
	
	DefaultListModel<XWord> list;
	CrossworkBoardState model;
	WordFinder finder;
	boolean focused_isHorizontal;
	int focused_x;
	int focused_y;
	CrossworkProgressListener progressListener;
	
	public WordListSorterThread(DefaultListModel<XWord> list, CrossworkBoardState model, WordFinder finder, boolean focused_isHorizontal, int focused_x, int focused_y, CrossworkProgressListener cpl){
		this.list = list;
		this.model = model;
		this.finder = finder;
		this.focused_isHorizontal = focused_isHorizontal;
		this.focused_x = focused_x;
		this.focused_y = focused_y;
		this.progressListener = cpl;
	}
	
	public void run() {
    	Set<XWord> scoredWords = new TreeSet<XWord>();
    	
    	int wordSize = list.get(0).word.length();
    	
    	Map<Integer, Double> scores = new HashMap<Integer, Double>();	
    	// Integer key encodes character index of word concatenated with the ascii value of the character
		// e.g. "DOG"
    	// letter "O" is at index 1
		// ascii value of "O" is 79
    	// The integer key for "DOG" will be 179 (1*100 + 79)
    	
    	List<Set<Character>> verifiedChars = new ArrayList<Set<Character>>(wordSize);
    	List<Set<Character>> bannedChars = new ArrayList<Set<Character>>(wordSize);
    	for (int i = 0; i < wordSize; i++) {
    		verifiedChars.add(new HashSet<Character>());
    		bannedChars.add(new HashSet<Character>());
    	}
    	
    	for (int i = 0; i < list.getSize(); i++) {
    		progressListener.updateSortingProgress(i/(list.getSize()+0.0));
    		XWord w = list.elementAt(i);
    		w.score = 1;
    		
    		// if any of the words are found to be restricted, that means the list is already sorted
    		// stop doing further work
    		if (w.state == XWord.RESTRICTED_STATE) break;
    		
    		// if any of the letters in the word are banned (have 0 orthogonal combinations), it is marked as restricted
    		for (int j = 0; j < w.word.length(); j++) {
    			char c = w.word.charAt(j);
    			if (bannedChars.get(j).contains(c)) {
        			w.state = XWord.RESTRICTED_STATE;
        			w.score = 0;
    			}
    		}
    		
    		// if the word has been marked as restricted, add it to the list and go to the next word
    		if (w.state == XWord.RESTRICTED_STATE) {
    			scoredWords.add(w);
    			continue;
    		}
    		
    		// find the word on the board, iterate across each letter, and then check how many words
    		// can be made with each letter in the perpendicular direction
    		int idx = model.findStartOfWord(focused_isHorizontal, focused_x, focused_y);
    		int col = idx/1000000;
    		int row = idx%1000000;
    		for (int j = 0; j < w.word.length(); j++) {
    			char c = w.word.charAt(j);
    			
    			// check the caches to see if this letter at this position has already been scored
    			if (verifiedChars.get(j).contains(c)) {
    				w.score *= scores.get(j*100+c);
            		if (focused_isHorizontal) col++;
            		else row++;
    				continue;
    			}
    			
    			// if the letter is already locked in, don't check it
    			if ((model.getState(col, row) & model.WORD_STATE_LOCKED) != 0){ 
            		if (focused_isHorizontal) col++;
            		else row++;
    				continue;
    			}
    			
    			// temporarily lock the letter and then run a query on the query string perpendicular to this cell
    			model.set(w.word.charAt(j), col, row);
    			model.lock(col, row);
    			String perpendicularQuery = model.getWord(!focused_isHorizontal, col, row, true);
    			model.unlock(col, row);
    			
    			double score = finder.findAllWords(perpendicularQuery).size();
        		if (score == 0) {
        			w.state = XWord.RESTRICTED_STATE;
        			w.score = 0;
        			bannedChars.get(j).add(w.word.charAt(j));
        			break;
        		} else {
        			// adjust the word's score
        			w.score *= score;
        			// add this letter's score to the cache
        			verifiedChars.get(j).add(w.word.charAt(j));
        			scores.put(j*100+c, score);
        		}
        		
        		if (focused_isHorizontal) col++;
        		else row++;
    		}
    		scoredWords.add(w);
    	}
    	progressListener.sortingComplete(scoredWords);
	}

}
