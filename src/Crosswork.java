import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;


public class Crosswork extends JApplet {
	
	private static final long serialVersionUID = -134734467513047313L;
	
	CrossworkBoard cwboard;
	
    public Crosswork () {
    	cwboard = new CrossworkBoard();
    }
    
    public void start() {
        initComponents();
        JFrame f = new JFrame("Crosswork");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(640, 640);
        f.add("Center", this);
        f.pack();
        f.setVisible(true);
        f.addComponentListener(new ComponentAdapter() {
        	
        	public void componentResized(ComponentEvent e) {
            	Component c = (Component)e.getSource();
            	int width = c.getWidth();
            	int height = c.getHeight();
            	int W_PADDING = 156;
            	int H_PADDING = 58;
            	
            	width -= W_PADDING;
            	height -= H_PADDING;
            	
        		cwboard.setGridSize(Math.min(width, height)/16);
        	}
        });
        
        try {
        	URL iconUrl = getClass().getResource("cwicon.gif");
            f.setIconImage(ImageIO.read(new File(iconUrl.getPath())));
        } catch (IOException e) {
        	System.out.println("Cannot find icon file.");
        	e.printStackTrace();
        }
        
        cwboard.loadDictionary();
    }
    
    public void initComponents() {
    	Font defaultFont = new Font("Consolas", Font.BOLD, 16);
    	Font labelFont = new Font("Consolas", Font.PLAIN, 12);
    	Font textFieldFont = new Font("Consolas", Font.BOLD, 12);
    	
        setLayout(new BorderLayout());
        
        JPanel downFlow = new JPanel();
        downFlow.setLayout(new BoxLayout(downFlow, BoxLayout.PAGE_AXIS));
        
        // top flow
        JPanel mainFlow = new JPanel();
        mainFlow.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 0));
        mainFlow.setPreferredSize(new Dimension(460,340));
        
        cwboard = new CrossworkBoard();
        
        cwboard.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent e) {
				cwboard.mousePressed(e.getX(), e.getY());
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
        
        cwboard.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				boolean canAcceptKeypress = cwboard.requestKeypress();
				if (!canAcceptKeypress) {
					return;
				}
				
				final int SHIFT = 1;
				final int CTRL = 2;
				final int CTRL_SHIFT = 3;
				
				switch(e.getKeyCode()) {
				
				case 'Z':
					if ((e.getModifiers() & 3) == CTRL) {
						cwboard.undo();
					} else if ((e.getModifiers() & 3) == CTRL_SHIFT) {
						cwboard.redo();
					} else {
		    			cwboard.inputLetter((char)e.getKeyCode());
					}
					break;
					
				case 'S':
					if ((e.getModifiers() & 3) == CTRL) {
						cwboard.saveCurrentState();
					} else {
		    			cwboard.inputLetter((char)e.getKeyCode());
					}
					break;
					
				case 'N':
					if ((e.getModifiers() & 3) == CTRL) {
						cwboard.newBoard();
					} else {
		    			cwboard.inputLetter((char)e.getKeyCode());
					}
					break;

				case 8:		// backspace
					cwboard.backspace();
					break;
					
				case 38:	// up
					if ((e.getModifiers() & 3) > 0) {
						cwboard.toggleHighlightOrientation();
					} else {
						cwboard.cursorUp();
					}
					break;

				case 40:	// down
					if ((e.getModifiers() & 3) > 0) {
						cwboard.toggleHighlightOrientation();
					} else {
						cwboard.cursorDown();
					}
					break;

				case 37:	// left
					if ((e.getModifiers() & 3) > 0) {
						cwboard.toggleHighlightOrientation();
					} else {
						cwboard.cursorLeft();
					}
					break;

				case 39:	// right
					if ((e.getModifiers() & 3) > 0) {
						cwboard.toggleHighlightOrientation();
					} else {
						cwboard.cursorRight();						
					}
					break;

				case 10:	// enter
					if ((e.getModifiers() & 3) == SHIFT) {
						cwboard.lockCurrentWord();
					} else {
						cwboard.lockCurrentLetter();
					}
					break;

				case 9:		// tab
					if ((e.getModifiers() & 3) == SHIFT) {
						cwboard.scrollToPrevWord();
					} else {
						cwboard.scrollToNextWord();
					}
					break;

				case 47:	// slash (shift+/, question mark)
					if ((e.getModifiers() & 3) == SHIFT) {
						cwboard.showValidLettersForCurrentCell();
					}
					break;

				case 127:	// del
					if ((e.getModifiers() & 3) == SHIFT) {
						cwboard.deleteLettersOfUnfinalizedWords();
					} else {
						cwboard.deleteLetter();
					}
					break;

				case 32:	// space
					if (cwboard.constructionMode) {
						cwboard.toggleConstructionBlock();
					} else {
						if ((e.getModifiers() & 3) == SHIFT) {
							cwboard.requestSortWordList();
						} else {
							cwboard.autocomplete();
						}						
					}
					break;
					
				case 27:	// esc
					cwboard.clearAutocompleteFocusMode();
					break;
					
				case 36:	// home
	    			cwboard.scrollToBeginningOfWord();
	    			break;
	    			
				case 35:	// end
					cwboard.scrollToEndOfWord();
					break;
					
				case 155:	// insert
					cwboard.sortAutocomplete();
					break;
					
				case 116:	// F5
					cwboard.toggleNumberDisplayMode();
					break;
					
				case 119:	// F8
					cwboard.toggleConstructionMode();
					break;
					
				default:	// ranges (A-Z, 0-9)
					// 0-9 range
					if (e.getKeyCode() >= 48 && e.getKeyCode() <= 57) {
						int kbdNum = e.getKeyCode()-48;
						if ((e.getModifiers() & 3) == SHIFT) {
							cwboard.saveState(kbdNum);
							break;
						} else {
							cwboard.loadState(kbdNum);
							break;
						}
					}
					
					// A-Z range
		    		if (e.getKeyCode() >= 'A' && e.getKeyCode() <= 'Z') {
		    			cwboard.inputLetter((char)e.getKeyCode());
		    		}
					
				}
				cwboard.repaint();

			}
		});
        mainFlow.add(cwboard);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(128, cwboard.getPreferredSize().height));
        mainFlow.add(scrollPane);
        
    	class XWordCellRenderer extends DefaultListCellRenderer {
    		public Component getListCellRendererComponent(
    				JList<? extends XWord> list, XWord value, int index,
    				boolean isSelected, boolean cellHasFocus) {
    		
    			if (value == null) return this;
    			
    			setText(value.toString());
    			if (value.state == XWord.BANNED_STATE) {
    				setForeground(Color.WHITE);
    			}
    			else if (value.state == XWord.RESTRICTED_STATE) {
    				setForeground(Color.BLACK);
    			} else {
//    				setForeground(Color.BLACK);
    			}
    			return this;
    		}
    	}
    	
        JList<XWord> listAccessory = new JList<XWord>();
        listAccessory.setCellRenderer(new XWordCellRenderer());
        Dimension spSize = scrollPane.getPreferredSize();
        listAccessory.setPreferredSize(new Dimension(spSize.width-24, spSize.height-24));
        listAccessory.setFixedCellHeight(cwboard.GRID_SIZE);
        listAccessory.setFont(defaultFont);
        int c = 208;
        listAccessory.setBackground(new Color(c,c,c));
        c = 128;
        listAccessory.setForeground(new Color(c,c,c));
        scrollPane.setViewportView(listAccessory);
        
        cwboard.setListAccessory(listAccessory);
        
        downFlow.add(mainFlow);
        
        // mid flow
        JPanel midFlow = new JPanel();
        midFlow.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 0));

        JLabel slotLabel = new JLabel("(no slot)");
        slotLabel.setPreferredSize(new Dimension(76, 20));
        slotLabel.setFont(labelFont);
        slotLabel.setHorizontalAlignment(SwingConstants.CENTER);
        slotLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        c = 160;
        slotLabel.setForeground(new Color(c,c,c));
        slotLabel.setBackground(new Color(178,182,238));
        
        cwboard.setSlotLabelAccessory(slotLabel);
        mainFlow.add(slotLabel);
        
        JLabel statusLabel = new JLabel("Welcome to Crosswork!");
        int xSize = 0;
        xSize = cwboard.getPreferredSize().width + scrollPane.getPreferredSize().width - 76 - 128 - 5;
        statusLabel.setPreferredSize(new Dimension(xSize, 20));
        statusLabel.setFont(labelFont);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
        		BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
        		new EmptyBorder(0, 8, 0, 0)
        		));
        c = 160;
        statusLabel.setBackground(new Color(178,182,238));
        statusLabel.setForeground(new Color(c,c,c));
        cwboard.setStatusLabelAccessory(statusLabel);
        mainFlow.add(statusLabel);
        
        JTextField consoleText = new JTextField("");
        consoleText.setPreferredSize(new Dimension(128, 20));
        consoleText.setFont(textFieldFont);
        c = 208;
        consoleText.setBackground(new Color(c,c,c));
        c = 160;
        consoleText.setForeground(new Color(c,c,c));
        
        cwboard.setConsoleTextAccessory(consoleText);
        mainFlow.add(consoleText);
        
        add("North", downFlow);
    }
    
	
	public static void main(String[] args) {
		Crosswork cw = new Crosswork();
		cw.start();
	}
	
}
