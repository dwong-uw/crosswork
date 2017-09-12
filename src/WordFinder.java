import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordFinder {
	
	private class DictionaryNode
	{
		public Map<Character, DictionaryNode> children;
		public char letter = '-';
		public DictionaryNode parent;
		
		public DictionaryNode() {
			children = new HashMap<Character, DictionaryNode>();
			parent = null;
		}
		
		public DictionaryNode(char c) {
			children = new HashMap<Character, DictionaryNode>();
			letter = c;
			parent = null;
		}
		
		public DictionaryNode findLetter(char c) {
			return children.get(c);
		}
		
		public void addChild(DictionaryNode child) {
			children.put(child.letter, child);
		}
		
		public boolean isTerminal() {
			return children.get(TERMINAL_NODE) != null;
		}
		
		public void setIsTerminal() {
			children.put(TERMINAL_NODE, new DictionaryNode());
		}
	}
	
	private DictionaryNode dictionary;
	public final char TERMINAL_NODE = '!';
	public final char WILDCARD = '*';
	
	public long startTime = 0;
	
	CrossworkProgressListener progressListener;
	
	public WordFinder (CrossworkProgressListener cpl) {
		this.progressListener = cpl;
		buildDictionary(getClass().getResource("english-words.all"));
	}
	
	public List<String> findAllWords(String query) {
		
		List<DictionaryNode> queue = new ArrayList<DictionaryNode>();
		
		queue.add(dictionary);
		
		for (int i = 0; i < query.length(); i++) {
			List<DictionaryNode> nextQueue = new ArrayList<DictionaryNode>();
			char c = query.charAt(i);
			if (c >= 'a' && c <= 'z') c += 'A'-'a';
			
			for (DictionaryNode currLetter : queue) {
				if (c == WILDCARD) {
					for (Map.Entry<Character, DictionaryNode> n : currLetter.children.entrySet()) {
						nextQueue.add(n.getValue());
					}
				} else {
					if (currLetter.children != null) {
						if (currLetter.children.get(c) != null) {
							nextQueue.add(currLetter.children.get(c));
						}
					}
				}
			}
			
			queue = nextQueue;
		}
		
		List<String> words = new ArrayList<String>();
		for (DictionaryNode w : queue) {
			if (w.children.containsKey(TERMINAL_NODE)) {
				String word = "";
				DictionaryNode currLetter = w;
				while (currLetter != null) {
					if (currLetter.letter != '-') {
						word = currLetter.letter + word;
					}
					currLetter = currLetter.parent;
				}
				words.add(word);
			}
		}
		
		return words;
	}
	
	public boolean hasWords(String query) {
		
		List<DictionaryNode> queue = new ArrayList<DictionaryNode>();
		
		queue.add(dictionary);
		
		for (int i = 0; i < query.length(); i++) {
			List<DictionaryNode> nextQueue = new ArrayList<DictionaryNode>();
			char c = query.charAt(i);
			if (c >= 'a' && c <= 'z') c += 'A'-'a';
			
			for (DictionaryNode currLetter : queue) {
				if (c == WILDCARD) {
					for (Map.Entry<Character, DictionaryNode> n : currLetter.children.entrySet()) {
						nextQueue.add(n.getValue());
					}
				} else {
					if (currLetter.children != null) {
						if (currLetter.children.get(c) != null) {
							nextQueue.add(currLetter.children.get(c));
						}
					}
				}
			}
			
			queue = nextQueue;
		}
		
		for (DictionaryNode w : queue) {
			if (w.children.containsKey(TERMINAL_NODE)) {
				return true;
			}
		}
		
		return false;
	}

	public void buildDictionary(final URL dictionaryUrl) {
		
		// load file
			Thread t = new Thread() {
				public void run() {
					try {
						// count number of lines in the file
						InputStream in = new BufferedInputStream(new FileInputStream(new File(dictionaryUrl.getPath())));
						byte[] cbuf = new byte[65536];
						int readCount = 0;
						int lineCount = 0;
						while ((readCount = in.read(cbuf)) != -1) {
							for (int i = 0; i < readCount; i++) {
								if (cbuf[i] == '\n') {
									++lineCount;
								}
							}
						}
						in.close();
						
						BufferedReader r = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(new File(dictionaryUrl.getPath())))));
						
						DictionaryNode rootDictNode = new DictionaryNode();
						String currLine;
						int currLineNum = 0;
						while ((currLine = r.readLine()) != null) {
							progressListener.updateDictionaryProgress(currLineNum/(lineCount+0.0));
							// interpret line
							String currWord = currLine;
							
							// add a new word
							DictionaryNode currDictNode = rootDictNode;
							boolean wordHasInvalidChar = false;
							for (int i = 0; i < currWord.length(); i++) {
								char c = currWord.charAt(i);
								if (c >= 'a' && c <= 'z') c -= 32; // make uppercase
								
								// handle exceptional characters
								if (c < 'A' || c > 'Z') { // invalidate words with illegal characters
									wordHasInvalidChar = true;
									break;
								}
								
								DictionaryNode nextDictNode = currDictNode.findLetter(c);
								if (nextDictNode == null) {
									nextDictNode = new DictionaryNode(c);
									currDictNode.addChild(nextDictNode);
									nextDictNode.parent = currDictNode;
								}
								currDictNode = nextDictNode;
							}
							if (!wordHasInvalidChar) currDictNode.setIsTerminal();
							// done adding a new word
							
							// done reading line
							currLineNum++;
						}
						// done reading file
						
						dictionary = rootDictNode;
						r.close();
					} catch (IOException e) {
						System.err.println("Error reading dictionary file");
						dictionary = null;
					}
					progressListener.dictionaryLoadingComplete();
				}
			};
			t.start();
	}
	
}
