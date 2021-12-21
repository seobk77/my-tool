package free.my.tool.ui.viewer;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang3.StringUtils;

import free.my.tool.ui.MainFrame;
import free.my.tool.ui.comp.LineNumberComponent;
import free.my.tool.ui.comp.LineNumberModel;
import free.my.tool.ui.panel.ZookeeperPanel;

import javax.swing.JCheckBox;

public class SpreadedDialog extends JDialog {
	private static final long serialVersionUID = -2572017178715962223L;

	private String path = null;
	@SuppressWarnings("unused")
	private ZookeeperPanel zookeeperPanel = null;

	private JCheckBox chckbxLineWarp = null;
	private JTextArea textArea = new JTextArea();
	private LineNumberModelImpl lineNumberModel = new LineNumberModelImpl();
	private LineNumberComponent lineNumberComponent = new LineNumberComponent(lineNumberModel);
	
	private UndoManager undoManager = new UndoManager();
	
	private Robot robot = null;

	private boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}

	public SpreadedDialog(String path, GraphicsConfiguration gc, ZookeeperPanel zookeeperPanel) {
		this.path = path;
		this.zookeeperPanel = zookeeperPanel;

		setAlwaysOnTop(true);
		setModal(true);
		this.setTitle("["+path+"] Node Data Editor");

		if(this.isWindows()) {
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/super_man.png")));
		}
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				zookeeperPanel.backNodeData(textArea.getText());
				SpreadedDialog.this.dispose();
			}
		}); 
		
		try {
			robot = new Robot();
		} catch (AWTException e3) {
			//무시
		}

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		//textArea = new JTextArea();
		textArea.setFont(new Font("굴림", Font.PLAIN, 16));
		textArea.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				lineNumberComponent.adjustWidth();
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				lineNumberComponent.adjustWidth();
			}
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				lineNumberComponent.adjustWidth();
			}
		});
		textArea.setCaretPosition(0);
		textArea.requestFocus();
		if(textArea.isFocusable()) {
			robot.keyPress(KeyEvent.VK_SPACE);
			robot.keyPress(KeyEvent.VK_BACK_SPACE);
		}
		
		//---------------------
		// Ctrl+S 키 SAVE 처리
		String saveKeyStrokeAndKey = "control S";
		KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(saveKeyStrokeAndKey);
		textArea.getInputMap().put(saveKeyStroke, saveKeyStrokeAndKey);
		textArea.getActionMap().put(saveKeyStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SpreadedDialog.this.saveData(zookeeperPanel);
			}
		});
		
		//UNDO, REDO 처리
		textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				undoManager.addEdit(e.getEdit());
			}
		});

		// Ctrl+Z 키 undo
		String undoKeyStrokeAndKey = "control Z";
		KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(undoKeyStrokeAndKey);
		textArea.getInputMap().put(undoKeyStroke, undoKeyStrokeAndKey);
		textArea.getActionMap().put(undoKeyStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager.canUndo()) {
					undoManager.undo();
				}
			}
		});
		
		// Ctrl+Y 키 redo 
		String redoKeyStrokeAndKey = "control Y";
		KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(redoKeyStrokeAndKey);
		textArea.getInputMap().put(redoKeyStroke, redoKeyStrokeAndKey);
		textArea.getActionMap().put(redoKeyStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager.canRedo()) {
					undoManager.redo();
				}
			}
		});
		
		// Ctrl+F 키 find 
		String findKeyStrokeAndKey = "control F";
		KeyStroke findKeyStroke = KeyStroke.getKeyStroke(findKeyStrokeAndKey);
		textArea.getInputMap().put(findKeyStroke, findKeyStrokeAndKey);
		textArea.getActionMap().put(findKeyStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				SpreadedDialog.this.processFindText();
			}
		});
		
		// Ctrl+W 키 Line Warp 
		String lineWarpStrokeAndKey = "control W";
		KeyStroke lineWarpKeyStroke = KeyStroke.getKeyStroke(lineWarpStrokeAndKey);
		textArea.getInputMap().put(lineWarpKeyStroke, lineWarpStrokeAndKey);
		textArea.getActionMap().put(lineWarpStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(chckbxLineWarp.isSelected()) {
					chckbxLineWarp.setSelected(false);
				} else {
					chckbxLineWarp.setSelected(true);
				}
				
				SpreadedDialog.this.processLineWarp();
			}
		});

		// 마우스 오른쪽 클릭 popmenu 
		this.addTextMenuItems(textArea);
		//---------------------
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setRowHeaderView(lineNumberComponent);
		lineNumberComponent.setAlignment(LineNumberComponent.CENTER_ALIGNMENT);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3, BorderLayout.SOUTH);
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel_8 = new JPanel();
		panel_3.add(panel_8, BorderLayout.CENTER);

		JButton saveButton = new JButton("Save");
		saveButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/save.png"))));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SpreadedDialog.this.saveData(zookeeperPanel);
			}
		});
		panel_8.add(saveButton);

		JButton closeButton = new JButton("Close");
		closeButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/close.png"))));
		panel_8.add(closeButton);
		
		JPanel panel = new JPanel();
		panel_3.add(panel, BorderLayout.WEST);
		
		chckbxLineWarp = new JCheckBox("Line Warp");		
		chckbxLineWarp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SpreadedDialog.this.processLineWarp();
			}
		});
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(chckbxLineWarp);

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zookeeperPanel.backNodeData(textArea.getText());
				SpreadedDialog.this.dispose();
			}
		});
		
		
		//this.setSize(812, 432);
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		this.setSize(screenSize.width, screenSize.height);
//		this.setLocationRelativeTo(null);
		Rectangle bounds = gc.getBounds();
		this.setBounds(bounds);
		this.setResizable(true);
	}

	public void setText(String text) {
		textArea.setText(text);
		textArea.setCaretPosition(0);
	}

	private class LineNumberModelImpl implements LineNumberModel {
		@Override
		public int getNumberLines() {
			return textArea.getLineCount();
		}

		@SuppressWarnings("deprecation")
        @Override
		public Rectangle getLineRect(int line) {
			try{
				return textArea.modelToView(textArea.getLineStartOffset(line));
			    //return textArea.modelToView2D(textArea.getLineStartOffset(line));
			}catch(BadLocationException e){
				e.printStackTrace();
				return new Rectangle();
			}
		}
	}
	
	private void saveData(ZookeeperPanel zookeeperPanel) {
		int result = JOptionPane.showConfirmDialog(SpreadedDialog.this,
				("[ "+ SpreadedDialog.this.path + " ] Node의 데이터를 저장(수정) 하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

		if(result == 0) {
			zookeeperPanel.saveNodeData(SpreadedDialog.this.path, textArea.getText());

			SpreadedDialog.this.dispose();
		}
	}
	
	private void addTextMenuItems(JTextComponent textField){
		final JPopupMenu popup = new JPopupMenu();
		popup.add(this.composeMenuItemFromActionOfComponent("find-text", textField, "Find", "find"));
		popup.addSeparator();
		popup.add(this.composeMenuItemFromActionOfComponent(DefaultEditorKit.selectAllAction,textField, "Select All", "select_all"));
		popup.addSeparator();
		popup.add(this.composeMenuItemFromActionOfComponent(DefaultEditorKit.copyAction, textField,"Copy", "copy"));
		popup.add(this.composeMenuItemFromActionOfComponent(DefaultEditorKit.cutAction, textField, "Cut", "cut"));
		popup.addSeparator();
		popup.add(this.composeMenuItemFromActionOfComponent(DefaultEditorKit.pasteAction, textField, "Paste", "paste"));

		textField.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()) {
					boolean isTextSelected = textField.getSelectedText() != null;
					for(MenuElement element : popup.getSubElements()){
						JMenuItem menuItem = ((JMenuItem) element);
						if(menuItem.getAction() != null && menuItem.getAction().isEnabled()) {
							if (isTextSelected){
								menuItem.setEnabled(true);
							}else if (!(menuItem.getClientProperty("id").equals("select_all")))
								menuItem.setEnabled(false);
						}
						if(menuItem.getClientProperty("id").equals("paste")){
							boolean isPastAvailable = false;
							for(DataFlavor flavor : Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors()){
								if(flavor.getRepresentationClass() == String.class){
									isPastAvailable = true;
									break;
								}
							}
							menuItem.setEnabled(isPastAvailable);
						}
						if(menuItem.getClientProperty("id").equals("find")){
							menuItem.setEnabled(true);
						}
					}
					e.getComponent().requestFocus();
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
	}
	
	private JMenuItem composeMenuItemFromActionOfComponent(String actionTag, JComponent component, String text, String idTag) {
		Action action = null;
		if("find-text".equals(actionTag)) {
			action = new Action() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SpreadedDialog.this.processFindText();
				}
				
				@Override
				public Object getValue(String key) {
					return null;
				}

				@Override
				public void putValue(String key, Object value) {
				}

				@Override
				public void setEnabled(boolean b) {
				}

				@Override
				public boolean isEnabled() {
					return false;
				}

				@Override
				public void addPropertyChangeListener(PropertyChangeListener listener) {
				}

				@Override
				public void removePropertyChangeListener(PropertyChangeListener listener) {
				}
			};
		} else {
			action = component.getActionMap().get(actionTag);
		}
		
		JMenuItem menuItem = new JMenuItem(action);
		menuItem.setText(text);
		menuItem.putClientProperty("id", idTag);

		return menuItem;
	}
	
	private void processFindText() {
		final String findText = JOptionPane.showInputDialog(SpreadedDialog.this, "Search text:", "Find", JOptionPane.PLAIN_MESSAGE);
		if(StringUtils.isNotEmpty(findText)) {
			int idx = textArea.getCaretPosition();
			idx = textArea.getText().indexOf(findText, idx+1 );
			if( idx != -1 ){   
				textArea.select( idx, (idx+findText.length()) );
			} else {
				JOptionPane.showMessageDialog(SpreadedDialog.this, "Search text not Found");
			}
		}
	}
	
	private void processLineWarp() {
		if(chckbxLineWarp.isSelected()) {
			textArea.setLineWrap(true);
		} else {
			textArea.setLineWrap(false);
		}
	}
}
