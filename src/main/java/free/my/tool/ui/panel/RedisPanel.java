package free.my.tool.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import free.my.tool.ui.MainFrame;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisPanel extends JPanel implements RedisPanelInterface {
	private static final long serialVersionUID = 1L;

	private MainFrame frame;

	private JLabel redisServerNameLabel;
	private JLabel connectFlagLabel;
	private boolean redisConnectFlag;

	private RedisClient redisClient;
	private StatefulRedisConnection<String, String> connection;

	private JComboBox<Integer> dbComboBox;
	private JTable table;
	private DefaultTableModel defaultTableModel;
	private JTextField cacheCountTextField;
	private JButton allRemoveButton;

	private String host; 
	private int port; 
	private String password; 
	private int database;
	private int numOfDatabase;
	private boolean applyBase64Encoding;
	
	private String tempSearchKey = "*";

	/**
	 * Create the panel.
	 */
	public RedisPanel(MainFrame frame) {
		this.frame = frame;
		setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Redis", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		String[] columnNames = {"Key", "Value", "Data"};
		Object[][] rowData = new Object[][] {};
		defaultTableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
				//return false; //all cells false
			}
		};

		table = new JTable() {
			private static final long serialVersionUID = 5918792511112385365L;

			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component component = null;
				try {
					component = super.prepareRenderer(renderer, row, column);
					if (component instanceof JComponent) {
						if(column == 0){
							JComponent jc = (JComponent) component;
							jc.setToolTipText(getValueAt(row, column).toString());
						}
					}
				} catch (IndexOutOfBoundsException iobe) {
					//무시 
				}
				return component;
			}
		};

		table.setEnabled(true);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true); //cell만 선택
		table.setDragEnabled(false);
		table.setModel(defaultTableModel);
		table.setRowHeight(table.getRowHeight() + 2);

		table.setAutoCreateRowSorter(true);
		TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(defaultTableModel);
		table.setRowSorter(trs);

		table.getColumn("Value").setCellRenderer(new ButtonRenderer());		
		table.getColumn("Value").setCellEditor(new ButtonEditor(new JCheckBox()));		
		table.getColumn("Data").setCellRenderer(new ButtonRenderer());
		table.getColumn("Data").setCellEditor(new ButtonEditor(new JCheckBox()));

		table.getColumnModel().getColumn(0).setPreferredWidth(500);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setMaxWidth(250);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setMaxWidth(250);

		//테이블 헤더 중앙 정렬
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer(); 
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		//테이블 헤더 drag 못하게 
		table.getTableHeader().setReorderingAllowed(false);

		scrollPane.setViewportView(table);

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel_5 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_5.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_3.add(panel_5, BorderLayout.CENTER);

		redisServerNameLabel = new JLabel("Redis ");
		redisServerNameLabel.setMaximumSize(new Dimension(150, 21));
		redisServerNameLabel.setMinimumSize(new Dimension(36, 21));
		panel_5.add(redisServerNameLabel);
		redisServerNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		
		JLabel dbIndexLabel = new JLabel("DB:");
		dbIndexLabel.setMaximumSize(new Dimension(25, 21));
		dbIndexLabel.setMinimumSize(new Dimension(25, 21));
		dbIndexLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		panel_5.add(dbIndexLabel);
		
		dbComboBox = new JComboBox<Integer>();
		dbComboBox.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
		        // 테이블 데이터 전체 삭제
		        if(defaultTableModel != null) {
		            while(defaultTableModel.getRowCount() > 0) {
		                defaultTableModel.removeRow(0);
		            }
		        }
		    }
		});
		dbComboBox.setPreferredSize(new Dimension(40, 21));
		dbComboBox.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {0}));
		dbComboBox.setSelectedIndex(0); //default 선택 index는 0
		panel_5.add(dbComboBox);

		connectFlagLabel = new JLabel("[Disconnected]");
		connectFlagLabel.setMinimumSize(new Dimension(89, 21));
		connectFlagLabel.setMaximumSize(new Dimension(89, 21));
		panel_3.add(connectFlagLabel, BorderLayout.EAST);
		connectFlagLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		connectFlagLabel.setForeground(Color.RED);

		JPanel panel = new JPanel();
		panel_1.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_2.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel.add(panel_2);

		JLabel lblNewLabel = new JLabel("Total Count:");
		panel_2.add(lblNewLabel);

		cacheCountTextField = new JTextField();
		panel_2.add(cacheCountTextField);
		cacheCountTextField.setEditable(false);
		cacheCountTextField.setText("0");
		cacheCountTextField.setColumns(5);
		
		JButton searchResultRemoveButton = new JButton("검색 결과 삭제");
		searchResultRemoveButton.setForeground(Color.RED);
		searchResultRemoveButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/cancel.png"))));
		searchResultRemoveButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        RedisPanel.this.searchResultRemove();
		    }
		});
		panel_2.add(searchResultRemoveButton);

		JPanel panel_4 = new JPanel();
		panel.add(panel_4, BorderLayout.EAST);

		allRemoveButton = new JButton("전체 삭제");
		allRemoveButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/cancel.png"))));
		allRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RedisPanel.this.removeAllKeyInCache();
			}
		});

		JButton btnNewButton = new JButton("All Keys"); // Refresh
		panel_4.add(btnNewButton);
		btnNewButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_find.png"))));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(() -> {
					frame.showProgressBar();
					RedisPanel.this.getKeyList(true, "");
					frame.closeProgressBar();
				}).start();
			}
		});
		allRemoveButton.setForeground(Color.RED);
		allRemoveButton.setEnabled(false);
		panel_4.add(allRemoveButton);
	}

	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void connectRedis(String host, String port, String password, String database, String numOfDatabase, boolean applyBase64Encoding) {
		this.host = host;
		this.port = Integer.parseInt(port);
		this.password = password;
		this.database = Integer.parseInt(database);
		this.numOfDatabase = Integer.parseInt(numOfDatabase);
		this.applyBase64Encoding = applyBase64Encoding;

		redisServerNameLabel.setText("> [" + host + "] ");
		
		List<Integer> dbList = new ArrayList<>();
		for(int i=0; i<this.numOfDatabase; i++) {
			dbList.add(i);
		}
		dbComboBox.setModel(new DefaultComboBoxModel(dbList.toArray()));
		dbComboBox.setSelectedIndex(this.database);

		new Thread(() -> this.connectRedisServer()).start();
	}

	private void connectRedisServer() {
		if(!redisConnectFlag) {
			frame.showProgressBar();

			RedisURI redisURI = new RedisURI();
			redisURI.setHost(host);
			redisURI.setPort(port);
			if(!StringUtils.isEmpty(password)) {
			    redisURI.setPassword(password.toCharArray());
			}
			redisURI.setDatabase(database);			
			this.redisClient = RedisClient.create(redisURI);
//			this.redisClient = RedisClient.create(String.format("redis://%s@%s:%s/%s", password, host, port, database));
			
//			EventBus eventBus = redisClient.getResources().eventBus();
//			eventBus.get().subscribe(new Action1<Event>() {
//				@Override
//				public void call(Event event) {
//					if(event instanceof ConnectedEvent) {
//						System.out.println("\nRedis Server: ["+RedisPanel.this.host+"] Connected~");
//						
//						connectFlagLabel.setText("[Connected]");
//						connectFlagLabel.setForeground(Color.BLUE);
//						allRemoveButton.setEnabled(true);
//					} else if(event instanceof DisconnectedEvent) {
//						RedisPanel.this.redisDisconnectedProcessing();
//					}
//				}
//			});

			this.connection = redisClient.connect();
			this.redisConnectFlag = true;
			connectFlagLabel.setText("[Connected]");
			connectFlagLabel.setForeground(Color.BLUE);
			allRemoveButton.setEnabled(true);

//			if(redisConnectFlag) {
//				this.updateKeyList(false);
//			}

			frame.closeProgressBar();
		}
		
		this.keepConnection();
	}

	private void keepConnection() { //커넥션 유지를 위해 
		//2분에 한번씩 ping
		new Thread(() -> {
			while(redisConnectFlag) {
				try {
					Thread.sleep(180*1000); //120초 -> 2분
				} catch (InterruptedException e) { 
					//무시
				} 
				
				if(connection.isOpen()) {
					RedisCommands<String, String> syncCommands = connection.sync();
					syncCommands.ping();
				} else {
					break;
				}
			}
		}).start();
	}
	
	@Override
    public void disconnectRedis() {
		if(redisClient != null) {
			connection.close();
			redisClient.shutdown();

			redisConnectFlag = false;
		}
	}

	private boolean checkRedisConnection() {
		if(redisConnectFlag) {
			if(connection.isOpen()) {
				connectFlagLabel.setText("[Connected]");
				connectFlagLabel.setForeground(Color.BLUE);
				allRemoveButton.setEnabled(true);
			} else {
				this.redisDisconnectedProcessing();
				return false;
			}
		} else {
			try {
				this.connectRedisServer();
			} catch (Exception e) {
				e.printStackTrace();

				this.redisDisconnectedProcessing();
				frame.closeProgressBar();
				return false;
			}
		}

		return true;
	}

	private void removeKeyInCache(int selectedRow) {
		if(!this.checkRedisConnection()) {
			return;
		}

		int selectedRowIndex = table.convertRowIndexToModel(selectedRow);
		String key = (String) defaultTableModel.getValueAt(selectedRowIndex, 0);

		int result = JOptionPane.showConfirmDialog(this,
				("선택한 DB에 [" + key + "] Key를 삭제하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

		if(result == 0) {
			try {
				RedisCommands<String, String> syncCommands = connection.sync();
				Integer selectedDB = (Integer)dbComboBox.getSelectedItem();
				syncCommands.select(selectedDB);
				
				Long deletedCount = syncCommands.del(key);
	
				if(deletedCount > 0) {
					System.out.println("\nRedis Server: ["+this.host+"] DB["+selectedDB+"] {Key: "+key+"} removed!!");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				frame.closeProgressBar();
			}
		}

		new Thread(() -> {
			frame.showProgressBar();
			//this.getKeyList(false, "");
			this.getKeyList(false, this.tempSearchKey);
			frame.closeProgressBar();
		}).start();
	}

	private void removeAllKeyInCache() {
		if(!this.checkRedisConnection()) {
			return;
		}

		int result = JOptionPane.showConfirmDialog(RedisPanel.this,
				("[주의] 선택한 DB의 전체 Key를 삭제하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);
		if(result == 0) {
			try {
				RedisCommands<String, String> syncCommands = connection.sync();
				Integer selectedDB = (Integer)dbComboBox.getSelectedItem();
				syncCommands.select(selectedDB);
				
				//String flushResult = syncCommands.flushall(); //이건 전체 DB에 대해서 삭제
				String flushResult = syncCommands.flushdb();

				if(!StringUtils.isEmpty(flushResult)) {
					System.out.println("\nRedis Server: ["+this.host+"], DB["+selectedDB+"] All Keys removed!!");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				frame.closeProgressBar();
			}
		}

		new Thread(() -> {
			frame.showProgressBar();
			this.getKeyList(false, "");
			frame.closeProgressBar();
		}).start();
	}

	private void printCacheValue(int selectedRow) {
		if(!this.checkRedisConnection()) {
			return;
		}

		int selectedRowIndex = table.convertRowIndexToModel(selectedRow);
		String key = (String) defaultTableModel.getValueAt(selectedRowIndex, 0);

		try {
			new Thread(() -> {
				frame.showProgressBar();
				
				RedisCommands<String, String> syncCommands = connection.sync();				
				Integer selectedDB = (Integer)dbComboBox.getSelectedItem();
				syncCommands.select(selectedDB);
				
				String type = syncCommands.type(key);
				StringBuffer sb = new StringBuffer();
				switch(type) {
				case "hash":
				    Map<String, String> map = syncCommands.hgetall(key);
				    sb.append("Data Type: hash\n\n");
				    map.forEach((k, v) -> {
				        sb.append(String.format("[%s : %s]\n", k, v));
				    });
				    this.printCacheValue(key, sb.toString());
				    break;
				    
				case "list":
				    Long llen = syncCommands.llen(key);
				    List<String> list = syncCommands.lrange(key, 0, llen);
				    sb.append("Data Type: list\n\n");
				    sb.append("[\n");
				    list.forEach(v -> {
				        sb.append(v).append("\n");
				    });
				    sb.append("]\n");
				    this.printCacheValue(key, sb.toString());
				    break;
				    
				case "set":
				    Set<String> set = syncCommands.smembers(key);
				    sb.append("Data Type: set\n\n");
				    sb.append("[\n");
				    set.forEach(v -> {
				        sb.append(v).append("\n");
                    });
				    sb.append("]\n");
                    this.printCacheValue(key, sb.toString());
				    break;
				    
				case "zset":
				    Long zlen = syncCommands.zcard(key);
				    List<String> zlist = syncCommands.zrange(key, 0, zlen);
				    sb.append("Data Type: zset\n\n");
				    sb.append("[\n");
				    zlist.forEach(v -> {
				        sb.append(v).append("\n");
                    });
				    sb.append("]\n");
                    this.printCacheValue(key, sb.toString());
				    break;
				    
				case "string":
				default:
				    String value = syncCommands.get(key);
				    sb.append("Data Type: string\n\n");
				    sb.append(value).append("\n");
				    this.printCacheValue(key, sb.toString());
				    break;
				}

				frame.closeProgressBar();
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			frame.closeProgressBar();
		}

	}

	private void printCacheValue(String key, String value) {
		if(!this.checkRedisConnection()) {
			return;
		}

		System.out.println("\nRedis Server: ["+this.host+"] {Key: "+key+"}");
		System.out.println("-------------------------------------");
		if(StringUtils.isEmpty(value)) {
			System.out.println("Empty!!");
		} else {
		    if(applyBase64Encoding) {
		        String encodeValue = Base64.encodeBase64String(value.getBytes());
		        System.out.println(encodeValue);
		    } else {
		        System.out.print(value);
		    }
		}
		System.out.println("-------------------------------------");
	}

	@Override
    public List<String> getKeyList(boolean isRefreshButtonClicked, String searchKey) {
		List<String> keyList = null;
		this.tempSearchKey = searchKey;
		
		if(isRefreshButtonClicked) {
			if(!this.checkRedisConnection()) {
				return keyList;
			}
		}

		try {
			RedisCommands<String, String> syncCommands = connection.sync();
			Integer selectedDB = (Integer)dbComboBox.getSelectedItem();
			syncCommands.select(selectedDB);

			//List<String> keyList = null;
			if(StringUtils.isEmpty(searchKey)) {
				keyList = syncCommands.keys("*");
			} else {
				keyList = syncCommands.keys(searchKey);
			}
			//Collections.sort(keyList);

			// 테이블 데이터 전체 삭제
			while(defaultTableModel.getRowCount() > 0) {
				defaultTableModel.removeRow(0);
			}
			
			// 테이블 sorter 초기화
            RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
            rowSorter.setSortKeys(null);

			keyList.forEach(key -> {
				Object[] rowData = new Object[3];
				rowData[0] = key;
				rowData[1] = "View (Print)";
				rowData[2] = "Remove";

				defaultTableModel.addRow(rowData);
			});

			int size = keyList.size();
			if(size != 0) {
				defaultTableModel.fireTableRowsInserted(0, size-1);
			} 
			cacheCountTextField.setText(String.valueOf(size));

			System.out.println("\nRedis Server: ["+this.host+"/"+selectedDB+"] Search Result(Key) Count -> " + size);
//			System.out.println("\nRedis Server: ["+this.host+"] Search Result(Key) Count -> " + size);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return keyList;
	}

	private void redisDisconnectedProcessing() {
		System.out.println("\nRedis Server: ["+this.host+"] Disconnected!!");

		connectFlagLabel.setText("[Disconnected]");
		connectFlagLabel.setForeground(Color.RED);

		// 테이블 데이터 전체 삭제
		while(defaultTableModel.getRowCount() > 0) {
			defaultTableModel.removeRow(0);
		}

		cacheCountTextField.setText("0");

		allRemoveButton.setEnabled(false);
	}

	class ButtonRenderer extends JButton implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if(value == null) {
				return null;
			}
			
			if (isSelected) {
				if(column == 2) {
					setForeground(Color.RED);
				} else {
					setForeground(table.getSelectionForeground());
				}
				setBackground(table.getSelectionBackground());
			} else {
				if(column == 2) {
					setForeground(Color.RED);
				} else {
					setForeground(table.getForeground());
				}
				setBackground(UIManager.getColor("Button.background"));
			}
			setText(value.toString());

			return this;
		}
	} //ButtonRenderer inner class end

	class ButtonEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private String    label;
		private int selectedRow;
		private int selectedColumn;
		private boolean   isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(e -> fireEditingStopped()); 
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if(value == null) {
				return null;
			}

			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			this.label = value.toString();
			this.selectedRow = row;
			this.selectedColumn = column;
			button.setText(label);
			isPushed = true;

			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				switch(selectedColumn) {
				case 1:
					//RedisPanel.this.printCacheContent(selectedRow);
					new Thread(() -> RedisPanel.this.printCacheValue(selectedRow)).start();
					break;				
				case 2:
					RedisPanel.this.removeKeyInCache(selectedRow);
					break;
				default:
					break;
				}
			}
			isPushed = false;
			return new String(label);
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			try{
				super.fireEditingStopped();
			} catch(Exception e) {
				String exMsg = e.toString();
				if(exMsg.contains("IndexOutOfBoundsException")) {
					//무시 
				} else {
					e.printStackTrace();
				}
			}
		}
	} //ButtonEditor inner class end

	@Override
    public void searchKey(String searchKey) {
		table.clearSelection();

		if(StringUtils.isEmpty(searchKey)) {
			return;
		}

		for (int index = 0; index < table.getRowCount(); index++) {
			String tableKey = table.getValueAt(index, 0).toString();

			if (searchKey.equals(tableKey)) {
				table.changeSelection(index, 0, false, false);
				//table.getSelectionModel().addSelectionInterval(0, 0);
			}
		}
	}
	
	private void searchResultRemove() {
	    if(!this.checkRedisConnection()) {
	        return;
	    }
	    
	    String searchResultCount = cacheCountTextField.getText();
	    if("0".equals(searchResultCount)) {
	        return;
	    }
	    
	    int result = JOptionPane.showConfirmDialog(RedisPanel.this,
	            ("[주의] 선택한 DB에서 검색된 ["+searchResultCount+"]개의 Key를 삭제하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

        if(result == 0) {
            table.clearSelection();
            
            List<String> keyList = new ArrayList<>();
            for (int index = 0; index < table.getRowCount(); index++) {
                String tableKey = table.getValueAt(index, 0).toString();
                keyList.add(tableKey);
            }
            
            try {
                RedisCommands<String, String> syncCommands = connection.sync();
                Integer selectedDB = (Integer)dbComboBox.getSelectedItem();
                syncCommands.select(selectedDB);
                
                Long deletedCount = 0l;
                if(keyList.size() > 0) {
                    deletedCount = syncCommands.del(keyList.toArray(new String[keyList.size()]));
                }
                
                if(deletedCount > 0) {
                    System.out.println("\nRedis Server: ["+this.host+"] DB["+selectedDB+"] {Total: "+deletedCount+" Keys} removed!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                frame.closeProgressBar();
            }
        }

        new Thread(() -> {
            frame.showProgressBar();
            this.getKeyList(false, this.tempSearchKey);
            frame.closeProgressBar();
        }).start();
	}
}