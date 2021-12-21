package free.my.tool.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import free.my.tool.ui.MainFrame;
import free.my.tool.ui.viewer.SpreadedDialog;

/**
 * Zookeeper 노드 expend할때 마다 로딩하는 버젼
 */
public class ZookeeperPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String E_KEY = "dkfdk1akwcnj8qhk2zz8apfhd";

	private MainFrame frame;

	private boolean zookeeperConnectFlag;
	private ZooKeeper zookeeper;
	private final CountDownLatch connectedSignal = new CountDownLatch(1);

	private DefaultMutableTreeNode rootNode;
	private JTree nodeTree;

	private JLabel modifyLabel; 
	private JTextArea dataTextArea;
	private int orgDataLength = 0; 
	private UndoManager undoManager = new UndoManager();
	private JTextArea infoTextArea;

	private static final String PATH_DELIMITER = "/";
	private JTextField nodeTextField;

	private JPopupMenu popupMenu;

	private JButton saveButton;
	private JButton editorButton;

	/**
	 * Create the panel.
	 */
	public ZookeeperPanel(MainFrame frame) {
		this.frame = frame;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{360, 450, 250, 0};
		gridBagLayout.rowHeights = new int[]{300, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		this.add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel_1 = new JLabel("Node Tree");
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 12));
		panel_3.add(lblNewLabel_1);

		JButton refrashButton = new JButton();
		refrashButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/refresh.png"))));
		refrashButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataTextArea.setText("");
				infoTextArea.setText("");

				new Thread(() -> {
					frame.showProgressBar();

					DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel(); //모델정보를 가져온다
					rootNode.removeAllChildren();
					model.reload();
					ZookeeperPanel.this.initNodeTree();
					
					nodeTree.repaint();

					frame.closeProgressBar();
				}).start();
			}
		});
		panel_3.add(refrashButton, BorderLayout.EAST);

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 20));

		JScrollPane scrollPane = new JScrollPane();
		rootNode = new DefaultMutableTreeNode(PATH_DELIMITER, true);
		nodeTree = new JTree(rootNode);
		nodeTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		nodeTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3) {
					TreePath tp = nodeTree.getPathForLocation(e.getX(), e.getY());

					if(tp != null) {
						nodeTree.setSelectionPath(tp);
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		});
		nodeTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();
				if(node == null) {
					saveButton.setEnabled(false);
					editorButton.setEnabled(false);
					return;
				}
				if(node.isRoot()) {
					saveButton.setEnabled(false);
					editorButton.setEnabled(false);
					dataTextArea.setText("");
					return;
				}
				saveButton.setEnabled(false);
				editorButton.setEnabled(true);

				TreeNode[] path = node.getPath();
				String entirePath = "";
				for(TreeNode p: path) {
					entirePath = entirePath + ("/" + p.toString());
				}
				entirePath = entirePath.substring(2);

				Stat stat = ZookeeperPanel.this.existNode(entirePath);
				if (stat != null) {
					final String finalEntirePath = entirePath;
					new Thread(() -> {
						frame.showProgressBar();

						modifyLabel.setText("");
						dataTextArea.setText("");
						orgDataLength = 0;

						dataTextArea.setText(ZookeeperPanel.this.getNodeData(stat, finalEntirePath));						
						dataTextArea.setCaretPosition(0);

						orgDataLength = dataTextArea.getText().getBytes().length;
//						orgDataLength = stat.getDataLength();

						infoTextArea.setText("");
						infoTextArea.append("cZxid = " + stat.getCzxid() + "\n");
						infoTextArea.append("ctime = " + stat.getCtime() + "\n");
						infoTextArea.append("mZxid = " + stat.getMzxid() + "\n");
						infoTextArea.append("mtime = " + stat.getMtime() + "\n");
						infoTextArea.append("pZxid = " + stat.getPzxid() + "\n");
						infoTextArea.append("cversion = " + stat.getCversion() + "\n");
						infoTextArea.append("dataVersion = " + stat.getVersion() + "\n");
						infoTextArea.append("aclVersion = " + stat.getAversion() + "\n");
						infoTextArea.append("ephemeralOwner = " + stat.getEphemeralOwner() + "\n");
						infoTextArea.append("dataLength = " + stat.getDataLength() + "\n");
						infoTextArea.append("numChildren = " + stat.getNumChildren() + "\n");

						frame.closeProgressBar();
					}).start();
				} else {
					System.out.println("Node does not exists");
				}
			}
		});
		nodeTree.addTreeWillExpandListener(new TreeWillExpandListener () {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				new Thread(() -> {
					frame.showProgressBar();
					
					TreePath treePath = event.getPath();
					nodeTree.expandPath(treePath);
					nodeTree.setSelectionPath(treePath);				
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();

					int childCount = node.getChildCount();
					Object[] path = treePath.getPath();

					StringBuffer sb = new StringBuffer();
					for(Object p: path) {
						sb.append('/').append(p.toString());
					}
					String parentPath = sb.toString();

					String childNodeName = null;
					String childNodePath = null;
					for(int i=0; i<childCount; i++) {
						TreeNode childTreeNode = node.getChildAt(i);

						if(childTreeNode.getChildCount() == 0) {
							childNodeName = childTreeNode.toString();
							childNodePath = parentPath + "/" + childNodeName;
							childNodePath = childNodePath.replaceAll("//", "/");

							ZookeeperPanel.this.makeTree(childNodePath.replaceFirst("//", "/"), (DefaultMutableTreeNode)childTreeNode);
						}
					}
					
					frame.closeProgressBar();
				}).start();
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
			}
		});
/*
		nodeTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				new Thread(() -> {
					frame.showProgressBar();
					
					TreePath treePath = event.getPath();				

					nodeTree.setSelectionPath(treePath);				
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();

					int childCount = node.getChildCount();
					Object[] path = treePath.getPath();

					StringBuffer sb = new StringBuffer();
					for(Object p: path) {
						sb.append(p.toString());
					}
					String parentPath = sb.toString();

					String childNodeName = null;
					String childNodePath = null;
					for(int i=0; i<childCount; i++) {
						TreeNode childTreeNode = node.getChildAt(i);

						if(childTreeNode.getChildCount() == 0) {
							childNodeName = childTreeNode.toString();
							childNodePath = parentPath + "/" + childNodeName;

							ZookeeperPanel.this.makeTree(childNodePath.replaceFirst("//", "/"), (DefaultMutableTreeNode)childTreeNode);
						}
					}

					frame.closeProgressBar();
				}).start();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				//무시
			}
		});
*/
		nodeTree.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane.setViewportView(nodeTree);

		panel_2.add(scrollPane, BorderLayout.CENTER);

		JPanel panel_6 = new JPanel();
		panel_1.add(panel_6, BorderLayout.SOUTH);
		panel_6.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);

		JLabel lblNewLabel_2 = new JLabel("Node 명:");
		panel.add(lblNewLabel_2);

		nodeTextField = new JTextField();
		panel.add(nodeTextField);
		nodeTextField.setColumns(15);

		JButton addButton = new JButton("Node 추가");
		addButton.setForeground(Color.BLUE);
		panel.add(addButton);
		addButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/add2.png"))));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();
				if(node == null) {
					JOptionPane.showMessageDialog(ZookeeperPanel.this, "생성할 Node의 Parents Node를 먼저 선택하세요.", "Information", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				TreeNode[] path = node.getPath();
				String entirePath = "";
				for(TreeNode p: path) {
					entirePath = entirePath + ("/" + p.toString());
				}

				String nodeName = nodeTextField.getText().trim();
				if(nodeName.length() == 0) {
					JOptionPane.showMessageDialog(ZookeeperPanel.this, 
							"생성할 Node 명을 입렵하세요.", "Information", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if(node.isRoot()){ 
					entirePath = "/" + nodeName;
				} else {
					entirePath = entirePath + "/" + nodeName;
					entirePath = entirePath.substring(2);
				}

				int result = JOptionPane.showConfirmDialog(ZookeeperPanel.this,
						("[ "+ entirePath + " ] Node를 생성하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

				if(result == 0) {
					final String finalEntirePath = entirePath;
					new Thread(() -> {
						frame.showProgressBar();

						String resultNode = ZookeeperPanel.this.createNode(finalEntirePath);
						if(StringUtils.isNotEmpty(resultNode)) {
							nodeTextField.setText("");
							dataTextArea.setText("");
							infoTextArea.setText("");

							DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(nodeName);
							node.add(newTreeNode);
							DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel(); //모델정보를 가져온다
							model.reload(node);

							nodeTree.expandPath(new TreePath(path));
							//nodeTree.setExpandsSelectedPaths(true);

							//새로 추가한 노드 선택되도록
							List<TreeNode> newTreeNodeList = new ArrayList<>();
							newTreeNodeList.addAll(Arrays.asList(path));
							newTreeNodeList.add(newTreeNode);						
							TreeNode[] newTreeNodes = newTreeNodeList.toArray(new TreeNode[]{});
							nodeTree.setSelectionPath(new TreePath(newTreeNodes));

							nodeTree.repaint();
						}

						frame.closeProgressBar();
					}).start();
				}
			}
		});

		JButton deleteButton = new JButton("Node 삭제");
//		panel.add(deleteButton); // 삭제 버튼 비활성화를 위해 주석 처리
		deleteButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/cancel.png"))));
		deleteButton.setForeground(Color.RED);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();
				if(node == null) {
					JOptionPane.showMessageDialog(ZookeeperPanel.this, "삭제할 Node를 선택하세요.", "Information", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if(node.isRoot()){
					JOptionPane.showMessageDialog(ZookeeperPanel.this, "Root Node는 삭제할 수 없습니다.", "Warning", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if(!node.isLeaf()) {
					JOptionPane.showMessageDialog(ZookeeperPanel.this, "Leaf Node만 삭제할 수 있습니다.", "Information", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				TreeNode[] path = node.getPath();
				String entirePath = "";
				for(TreeNode p: path) {
					entirePath = entirePath + ("/" + p.toString());
				}
				entirePath = entirePath.substring(2);

				int result = JOptionPane.showConfirmDialog(ZookeeperPanel.this,
						("[ "+ entirePath + " ] Node를 삭제하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(result == 0) {
					final String finalEntirePath = entirePath;
					new Thread(() -> {
						frame.showProgressBar();

						ZookeeperPanel.this.deleteNode(finalEntirePath);
						System.out.println("["+ finalEntirePath + "] Node Delete Success~");

						dataTextArea.setText("");
						infoTextArea.setText("");

						TreeNode parentTreeNode = node.getParent();
						TreePath parentPath = new TreePath(path).getParentPath();
						node.removeFromParent();
						
						DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel(); //모델정보를 가져온다
						model.reload(parentTreeNode);

						nodeTree.expandPath(parentPath);
						nodeTree.setSelectionPath(parentPath);
						
						nodeTree.repaint();
						
						frame.closeProgressBar();
					}).start();
				}
			}
		});
		panel_6.add(panel);

		JPanel panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.insets = new Insets(0, 0, 0, 5);
		gbc_panel_7.gridx = 1;
		gbc_panel_7.gridy = 0;
		add(panel_7, gbc_panel_7);
		panel_7.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_7.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)  {
					ZookeeperPanel.this.showNodeDataEditer();
				}
			}
		});
		panel_4.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_7.add(panel_4, BorderLayout.NORTH);
		panel_4.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel("Node Data");
		lblNewLabel.setFont(new Font("굴림", Font.BOLD, 12));
		panel_4.add(lblNewLabel, BorderLayout.WEST);

		editorButton = new JButton();
		editorButton.setEnabled(false);
		editorButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/expand.png"))));
		editorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZookeeperPanel.this.showNodeDataEditer();
			}
		});
		panel_4.add(editorButton, BorderLayout.EAST);

		modifyLabel = new JLabel("");
		modifyLabel.setFont(new Font("굴림", Font.BOLD, 12));
		modifyLabel.setForeground(Color.RED);
		panel_4.add(modifyLabel, BorderLayout.CENTER);

		JPanel panel_5 = new JPanel();
		panel_7.add(panel_5, BorderLayout.CENTER);
		panel_5.setBorder(null);
		panel_5.setLayout(new BorderLayout(0, 0));

		dataTextArea = new JTextArea();
		dataTextArea.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				int length = dataTextArea.getText().getBytes().length;
				
				if(length != orgDataLength) {
					modifyLabel.setText(" (Edited)");
					saveButton.setEnabled(true);
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		dataTextArea.setFont(new Font("굴림", Font.PLAIN, 13));

		//---------------------
		// Ctrl+S 키 SAVE 처리
		String saveKeyStrokeAndKey = "control S";
		KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(saveKeyStrokeAndKey);
		dataTextArea.getInputMap().put(saveKeyStroke, saveKeyStrokeAndKey);
		dataTextArea.getActionMap().put(saveKeyStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(saveButton.isEnabled()) {
					ZookeeperPanel.this.saveData(frame);
				}
			}
		});

		//UNDO, REDO 처리
		dataTextArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				undoManager.addEdit(e.getEdit());
			}
		});

		// Ctrl+Z 키 undo
		String undoKeyStrokeAndKey = "control Z";
		KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(undoKeyStrokeAndKey);
		dataTextArea.getInputMap().put(undoKeyStroke, undoKeyStrokeAndKey);
		dataTextArea.getActionMap().put(undoKeyStrokeAndKey, new AbstractAction() {
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
		dataTextArea.getInputMap().put(redoKeyStroke, redoKeyStrokeAndKey);
		dataTextArea.getActionMap().put(redoKeyStrokeAndKey, new AbstractAction() {
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
		dataTextArea.getInputMap().put(findKeyStroke, findKeyStrokeAndKey);
		dataTextArea.getActionMap().put(findKeyStrokeAndKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				ZookeeperPanel.this.processFindText();
			}
		});

		// 마우스 오른쪽 클릭 popmenu 
		this.addTextMenuItems(dataTextArea);
		//---------------------

		JScrollPane scrollPane_1 = new JScrollPane(dataTextArea);
		panel_5.add(scrollPane_1, BorderLayout.CENTER);

		JPanel panel_11 = new JPanel();
		panel_7.add(panel_11, BorderLayout.SOUTH);

		saveButton = new JButton("저장(수정)");
		saveButton.setEnabled(false);
		saveButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/save2.png"))));
		saveButton.setForeground(Color.RED);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZookeeperPanel.this.saveData(frame);
			}
		});
		panel_11.add(saveButton);

		popupMenu = new JPopupMenu("Popup");
		JMenuItem addMenuItem = new JMenuItem("Node 추가");
		addMenuItem.setForeground(Color.BLUE);
		addMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String nodeName = JOptionPane.showInputDialog(ZookeeperPanel.this, "Input node name:", "Node 추가", JOptionPane.PLAIN_MESSAGE);
				if(StringUtils.isEmpty(nodeName)) {
					return;
				}

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();
				TreeNode[] path = node.getPath();
				String entirePath = "";
				for(TreeNode p: path) {
					entirePath = entirePath + ("/" + p.toString());
				}

				if(node.isRoot()){ 
					entirePath = "/" + nodeName;
				} else {
					entirePath = entirePath + "/" + nodeName;
					entirePath = entirePath.substring(2);
				}

				final String finalEntirePath = entirePath;
				new Thread(() -> {
					frame.showProgressBar();

					String resultNode = ZookeeperPanel.this.createNode(finalEntirePath);
					if(StringUtils.isNotEmpty(resultNode)) {
						nodeTextField.setText("");
						dataTextArea.setText("");
						infoTextArea.setText("");
						
						DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(nodeName);
						node.add(newTreeNode);
						DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel(); //모델정보를 가져온다
						model.reload(node);

						nodeTree.expandPath(new TreePath(path));
						//nodeTree.setExpandsSelectedPaths(true);

						//새로 추가한 노드 선택되도록
						List<TreeNode> newTreeNodeList = new ArrayList<>();
						newTreeNodeList.addAll(Arrays.asList(path));
						newTreeNodeList.add(newTreeNode);						
						TreeNode[] newTreeNodes = newTreeNodeList.toArray(new TreeNode[]{});
						nodeTree.setSelectionPath(new TreePath(newTreeNodes));
						
						nodeTree.repaint();
					}

					frame.closeProgressBar();
				}).start();
			}
		});
		popupMenu.add(addMenuItem);
		
		popupMenu.addSeparator();
		
		JMenuItem removeMenuItem = new JMenuItem("Node 삭제");
		removeMenuItem.setForeground(Color.RED);
		removeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();

				if(node.isRoot()){
					JOptionPane.showMessageDialog(ZookeeperPanel.this, "Root Node는 삭제할 수 없습니다.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}

				TreeNode[] path = node.getPath();
				String entirePath = "";
				for(TreeNode p: path) {
					entirePath = entirePath + ("/" + p.toString());
				}
				entirePath = entirePath.substring(2);

				int result = JOptionPane.showConfirmDialog(ZookeeperPanel.this,
						("[주의] 선택한 [ "+ entirePath + " ] Node 및 Children Node들을 모두 삭제하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if(result == 0) {
					final String finalEntirePath = entirePath;
					new Thread(() -> {
						frame.showProgressBar();

						ZookeeperPanel.this.deleteNodeRecursively(finalEntirePath); // 재귀 삭제

						TreeNode parentTreeNode = node.getParent();
						TreePath parentPath = new TreePath(path).getParentPath();
						node.removeFromParent();
						
						dataTextArea.setText("");
						infoTextArea.setText("");

						DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel(); //모델정보를 가져온다
						model.reload(parentTreeNode);					
						
						nodeTree.expandPath(parentPath);
						nodeTree.setSelectionPath(parentPath);
						
						nodeTree.repaint();

						frame.closeProgressBar();
					}).start();
				}
			}
		});
		popupMenu.add(removeMenuItem);
		
		JPanel panel_8 = new JPanel();
		GridBagConstraints gbc_panel_8 = new GridBagConstraints();
		gbc_panel_8.fill = GridBagConstraints.BOTH;
		gbc_panel_8.gridx = 2;
		gbc_panel_8.gridy = 0;
		add(panel_8, gbc_panel_8);
		panel_8.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_8.setLayout(new BorderLayout(0, 0));

		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout_2 = (FlowLayout) panel_9.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel_8.add(panel_9, BorderLayout.NORTH);

		JLabel lblNewLabel_3 = new JLabel("Node Info.");
		lblNewLabel_3.setFont(new Font("굴림", Font.BOLD, 12));
		panel_9.add(lblNewLabel_3);

		JPanel panel_10 = new JPanel();
		panel_10.setBorder(null);
		panel_8.add(panel_10, BorderLayout.CENTER);
		panel_10.setLayout(new BorderLayout(0, 0));

		infoTextArea = new JTextArea();
		infoTextArea.setEditable(false);

		JScrollPane scrollPane_2 = new JScrollPane(infoTextArea);
		panel_10.add(scrollPane_2, BorderLayout.CENTER);
	}

	public void connectZookeeper(Map<String, String> zooInfo) {
		String host = zooInfo.get("zookeeper.server.host");

		new Thread(() -> this.initZookeeper(host)).start();
	}

	private void initZookeeper(String host) {
		try {
			if(!zookeeperConnectFlag) {
				frame.showProgressBar();

				this.connect(host);
				zookeeperConnectFlag = true;
				System.out.println("ZooKeeper Connection Success~");

				this.initNodeTree();

				frame.closeProgressBar();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void initNodeTree() {
		Stat stat = this.existNode(PATH_DELIMITER);
		if(stat != null) {
			this.makeTree("", rootNode);

			nodeTree.expandRow(0);
		} else {
			System.out.println("ZooKeeper Node does not exists");
		}
	}

	private void makeTree(String path, DefaultMutableTreeNode node) {
		List<String> childrenList = this.getChildrenNode(path);
		
		if(childrenList != null) {
			for(String childrenName : childrenList) {
				DefaultMutableTreeNode childernNode = new DefaultMutableTreeNode(childrenName);
				node.add(childernNode);
	
				this.makeChildTree((path + PATH_DELIMITER + childrenName), childernNode);
			}
		}
	}
	
	private void makeChildTree(String path, DefaultMutableTreeNode node) {
		List<String> childrenList = this.getChildrenNode(path);
		
		if(childrenList != null) {
			for(String childrenName : childrenList) {
				DefaultMutableTreeNode childernNode = new DefaultMutableTreeNode(childrenName);
				node.add(childernNode);
			}
		}
		
		nodeTree.repaint();
	}

	private void connect(String host) throws IOException, InterruptedException {
		this.zookeeper = new ZooKeeper(host, 5000, new Watcher() {
			@Override
			public void process(WatchedEvent we) {
				if (we.getState() == KeeperState.SyncConnected) {
					connectedSignal.countDown();
				}
			}
		});

		connectedSignal.await();
	}

	public void disconnectZookeeper() {
		if(zookeeper != null) {
			try {
				zookeeper.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Stat existNode(String path) {
		Stat stat = null;
		try {
			stat = zookeeper.exists(path, true);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
		return stat;
	}

	private List<String> getChildrenNode(String path) {
		if(path.length() == 0) {
			path = PATH_DELIMITER;
		}
		
		List<String> childernList = null;
		try {
			Stat stat = this.existNode(path);
			if(stat != null) {
				childernList = zookeeper.getChildren(path, false);
				Collections.sort(childernList);
			}
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return childernList;
	}

	private boolean setData(String path, String data) {
		Stat setData = null;
		try {
			byte[] byteData = data.getBytes("UTF-8");
			setData = zookeeper.setData(path, byteData, zookeeper.exists(path, true).getVersion());
		} catch (UnsupportedEncodingException | KeeperException | InterruptedException e) {
			e.printStackTrace();
		}

		return (setData != null);
	}

	private String getNodeData(Stat stat, String path) {
		String data = null;
		try {
			byte[] byteData = zookeeper.getData(path, false, null);
			if(byteData != null) {
				data = new String(byteData, "UTF-8");
			} 
		} catch (KeeperException | InterruptedException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if(data == null) {
			System.out.println("["+path+"] Node does not have any data." );
		}

		return data;
	}

	private String createNode(String path) {
		String result = null;
		try {
			result = zookeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			System.out.println("["+ path + "] Node Create Success~");
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
			System.out.println("["+ path + "] Node Create Failed!!");
		}
		
		return result;
	}

	private void deleteNode(String path) {
		try {
			zookeeper.delete(path, zookeeper.exists(path, true).getVersion());
			System.out.println("["+ path + "] Node Remove Success~");
		} catch (InterruptedException | KeeperException e) {
			e.printStackTrace();
			System.out.println("["+ path + "] Node Remove Failed!!");
		}
	}

	private void deleteNodeRecursively(String path) {
		this.deleteChildrenNode(path);

		this.deleteNode(path);
	}

	private void deleteChildrenNode(String path) {
		List<String> childrenList = this.getChildrenNode(path);

		for(String childrenName : childrenList) {
			this.deleteNodeRecursively(path + PATH_DELIMITER + childrenName);
		}
	}

	private void showNodeDataEditer() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();
		if(node == null) {
			return;
		}

		TreeNode[] path = node.getPath();
		String entirePath = "";
		for(TreeNode p: path) {
			entirePath = entirePath + ("/" + p.toString());
		}
		entirePath = entirePath.substring(2);

		SpreadedDialog spreadedDialog = new SpreadedDialog(entirePath, frame.getGraphicsConfiguration(), ZookeeperPanel.this);
		String data = dataTextArea.getText();
		spreadedDialog.setText(data); 
		spreadedDialog.setVisible(true);
	}

	private void saveData(MainFrame frame) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeTree.getLastSelectedPathComponent();
		if(node == null) {
			JOptionPane.showMessageDialog(ZookeeperPanel.this, "Data를 저장(수정)할 Node를 먼저 선택하세요.", "Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		if(node.isRoot()) {
			JOptionPane.showMessageDialog(ZookeeperPanel.this, 
					"Root Node에 데이터는 저장(수정)할 수 없습니다.", "Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

//		String data = dataTextArea.getText().trim();
//		if(data.length() == 0) {
//			JOptionPane.showMessageDialog(ZookeeperPanel.this, 
//					"저장(수정)할 데이터를 입력하세요.", "Information", JOptionPane.INFORMATION_MESSAGE);
//			return;
//		}
		String data = dataTextArea.getText();

		TreeNode[] path = node.getPath();
		String entirePath = "";
		for(TreeNode p: path) {
			entirePath = entirePath + ("/" + p.toString());
		}
		entirePath = entirePath.substring(2);

		int result = JOptionPane.showConfirmDialog(ZookeeperPanel.this,
				("[ "+ entirePath + " ] Node의 데이터를 저장(수정) 하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

		if(result == 0) {
			final String finalEntirePath = entirePath;
			new Thread(() -> {
				frame.showProgressBar();

				this.saveNodeData(finalEntirePath, data);

				frame.closeProgressBar();
			}).start();
		}
	}

	public void saveNodeData(String entirePath, String data) {
		boolean saveResult = this.setData(entirePath, data);

		if(saveResult) {
			System.out.println("["+ entirePath + "] Node Data Save(Modify) Success~");
			modifyLabel.setText("");
			saveButton.setEnabled(false);

			Stat stat = this.existNode(entirePath);
			if (stat != null) {
				dataTextArea.setText(data);
				dataTextArea.setCaretPosition(0);
				
				orgDataLength = dataTextArea.getText().getBytes().length;

				infoTextArea.setText("");
				infoTextArea.append("cZxid = " + stat.getCzxid() + "\n");
				infoTextArea.append("ctime = " + stat.getCtime() + "\n");
				infoTextArea.append("mZxid = " + stat.getMzxid() + "\n");
				infoTextArea.append("mtime = " + stat.getMtime() + "\n");
				infoTextArea.append("pZxid = " + stat.getPzxid() + "\n");
				infoTextArea.append("cversion = " + stat.getCversion() + "\n");
				infoTextArea.append("dataVersion = " + stat.getVersion() + "\n");
				infoTextArea.append("aclVersion = " + stat.getAversion() + "\n");
				infoTextArea.append("ephemeralOwner = " + stat.getEphemeralOwner() + "\n");
				infoTextArea.append("dataLength = " + stat.getDataLength() + "\n");
				infoTextArea.append("numChildren = " + stat.getNumChildren() + "\n");
			} else {
				System.out.println("Node does not exists");
			}
		} else {
			System.out.println("["+ entirePath + "] Node Data Save(Modify) Failed~");
		}
	}

	public void backNodeData(String data) {
		dataTextArea.setText(data);
		dataTextArea.setCaretPosition(0);

		int length = dataTextArea.getText().getBytes().length;
		if(length != orgDataLength) {
			modifyLabel.setText(" (Edited)");
			saveButton.setEnabled(true);
		}
	}

	private void addTextMenuItems(JTextComponent textField){
		final JPopupMenu popup = new JPopupMenu();
		popup.add(this.composeMenuItemFromActionOfComponent("find-text", textField, "Find", "find"));
		popup.addSeparator();
		popup.add(this.composeMenuItemFromActionOfComponent(DefaultEditorKit.selectAllAction, textField, "Select All", "select_all"));
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
					boolean isSaveButtonEnabled = saveButton.isEnabled();
					if(!isSaveButtonEnabled) {
						return;
					}

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
					ZookeeperPanel.this.processFindText();
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
		final String findText = JOptionPane.showInputDialog(ZookeeperPanel.this, "Search text:", "Find", JOptionPane.PLAIN_MESSAGE);
		if(StringUtils.isNotEmpty(findText)) {
			int idx = dataTextArea.getCaretPosition();
			idx = dataTextArea.getText().indexOf(findText, idx+1 );
			if( idx != -1 ){   
				dataTextArea.select( idx, (idx+findText.length()) );
			} else {
				JOptionPane.showMessageDialog(ZookeeperPanel.this, "Search text not found!");
			}
		}
	}
}