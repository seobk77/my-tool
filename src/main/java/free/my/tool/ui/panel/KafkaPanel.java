package free.my.tool.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
//import java.util.Observable;
//import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

//import free.my.tool.manager.SshManager;
import free.my.tool.ui.MainFrame;
import free.my.tool.ui.viewer.KafkaConsumerViewer;

//@SuppressWarnings("deprecation")
public class KafkaPanel extends JPanel {// implements Observer {
	private static final long serialVersionUID = 1L;

	private MainFrame frame;

//	private Observable kafkaObservable; 
//	private boolean kafkaSshConnectFlag;
//	private SshManager kafkaSshManager;
	private Map<String, String> kafkaCommandMap;
	
	private JTable table;
	private JTextField countField;
	private JList<String> serviceList;
	private JList<String> createTopicList;
	
	private DefaultTableModel defaultTableModel;
	private DefaultListModel<String> serviceListModel;
	private DefaultListModel<String> createTopicListModel;
	private JTextField partitionTextField;
	private JTextField replicationTextField;
	
	private List<String> topicList = new ArrayList<>();
	
	private JTextField topicTextField;
	
	private JRadioButton listSelectedButton;
	private JRadioButton directInputButton;
	
	private int consumerViewerCount = 0;
	
	/**
	 * Create the panel.
	 */
	public KafkaPanel(MainFrame frame) {
		this.frame = frame;
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel_5 = new JPanel();
		add(panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] {487, 10, 230, 260, 238, 0};
		gbl_panel_5.rowHeights = new int[]{300, 0};
		gbl_panel_5.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		panel_5.add(panel, gbc_panel);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true); //cell만 선택
		table.setDragEnabled(false);
		table.setRowHeight(table.getRowHeight() + 2);
		
		String[] columnNames = {"Topic Name", "Info.", "Consumer", "Topic"};
		Object[][] rowData = new Object[][] {};
		defaultTableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
				//return false; //all cells false
			}
		};
		
		table.setModel(defaultTableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(295);
		table.getColumnModel().getColumn(1).setPreferredWidth(35);
		table.getColumnModel().getColumn(2).setPreferredWidth(35);
		table.getColumnModel().getColumn(3).setPreferredWidth(35);
		//테이블 헤더 중앙 정렬
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer(); 
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		//테이블 헤더 drag 못하게 
		table.getTableHeader().setReorderingAllowed(false);
		
		table.setAutoCreateRowSorter(true);
		TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(defaultTableModel);
		table.setRowSorter(trs);
		
		table.getColumn("Info.").setCellRenderer(new ButtonRenderer());		
		table.getColumn("Info.").setCellEditor(new ButtonEditor(new JCheckBox()));
		table.getColumn("Consumer").setCellRenderer(new ButtonRenderer());		
		table.getColumn("Consumer").setCellEditor(new ButtonEditor(new JCheckBox()));
		table.getColumn("Topic").setCellRenderer(new ButtonRenderer());		
		table.getColumn("Topic").setCellEditor(new ButtonEditor(new JCheckBox()));
		
		scrollPane.setViewportView(table);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Created Topic");
		lblNewLabel.setFont(new Font("굴림", Font.BOLD, 12));
		panel_1.add(lblNewLabel, BorderLayout.WEST);
		
//		JButton refreshButton = new JButton("Refresh");
		JButton refreshButton = new JButton();
		refreshButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/refresh.png"))));
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(()-> {
					frame.showProgressBar();
					KafkaPanel.this.runKafkaTopicListCommand();
				}).start();
			}
		});
		
		panel_1.add(refreshButton, BorderLayout.EAST);
		
		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_4.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panel.add(panel_4, BorderLayout.SOUTH);
		
		JLabel lblNewLabel_2 = new JLabel("Total Count:");
		panel_4.add(lblNewLabel_2);
		
		countField = new JTextField();
		countField.setEditable(false);
		countField.setText("0");
		panel_4.add(countField);
		countField.setColumns(5);
		
		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.insets = new Insets(0, 0, 0, 5);
		gbc_panel_2.gridx = 2;
		gbc_panel_2.gridy = 0;
		panel_5.add(panel_2, gbc_panel_2);
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_2.setLayout(new BorderLayout(0, 0));
		
		serviceList = new JList<>();
		serviceList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		serviceList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				KafkaPanel.this.getTargetTopicByService(serviceList.getSelectedValue());
			}
		});
		serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		serviceListModel = new DefaultListModel<>();
		serviceList.setModel(serviceListModel);
		
		panel_2.add(serviceList, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_2.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel lblNewLabel_1 = new JLabel("Domain");
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 12));
		panel_3.add(lblNewLabel_1);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.insets = new Insets(0, 0, 0, 5);
		gbc_panel_7.gridx = 3;
		gbc_panel_7.gridy = 0;
		panel_5.add(panel_7, gbc_panel_7);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout_4 = (FlowLayout) panel_9.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		panel_7.add(panel_9, BorderLayout.NORTH);
		
		JLabel lblSubmitTargetService = new JLabel("Target Topic");
		lblSubmitTargetService.setFont(new Font("굴림", Font.BOLD, 12));
		panel_9.add(lblSubmitTargetService);
		createTopicListModel = new DefaultListModel<>();
		
		JPanel panel_23 = new JPanel();
		panel_7.add(panel_23, BorderLayout.CENTER);
		GridBagLayout gbl_panel_23 = new GridBagLayout();
		gbl_panel_23.columnWidths = new int[]{0, 0};
		gbl_panel_23.rowHeights = new int[]{250, 0, 0};
		gbl_panel_23.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_23.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_23.setLayout(gbl_panel_23);
		
		JPanel panel_24 = new JPanel();
		panel_24.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_24 = new GridBagConstraints();
		gbc_panel_24.insets = new Insets(0, 0, 5, 0);
		gbc_panel_24.fill = GridBagConstraints.BOTH;
		gbc_panel_24.gridx = 0;
		gbc_panel_24.gridy = 0;
		panel_23.add(panel_24, gbc_panel_24);
		panel_24.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_21 = new JPanel();
		panel_24.add(panel_21, BorderLayout.CENTER);
		panel_21.setLayout(new BorderLayout(0, 0));
		
		createTopicList = new JList<>();
		createTopicList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_21.add(createTopicList, BorderLayout.CENTER);
		createTopicList.setModel(createTopicListModel);
		
		JPanel panel_27 = new JPanel();
		FlowLayout flowLayout_7 = (FlowLayout) panel_27.getLayout();
		flowLayout_7.setAlignment(FlowLayout.LEFT);
		panel_24.add(panel_27, BorderLayout.NORTH);
		
		ButtonGroup group = new ButtonGroup();
		listSelectedButton = new JRadioButton("List에서 선택");
		listSelectedButton.setSelected(true);
		group.add(listSelectedButton);
		listSelectedButton.addActionListener(e -> {
			serviceList.setEnabled(true);
			createTopicList.setEnabled(true);
			
			topicTextField.setEnabled(false);
			
		});
		panel_27.add(listSelectedButton);
		
		JPanel panel_25 = new JPanel();
		panel_25.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_25 = new GridBagConstraints();
		gbc_panel_25.fill = GridBagConstraints.BOTH;
		gbc_panel_25.gridx = 0;
		gbc_panel_25.gridy = 1;
		panel_23.add(panel_25, gbc_panel_25);
		panel_25.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_22 = new JPanel();
		panel_25.add(panel_22, BorderLayout.CENTER);
		panel_22.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel panel_28 = new JPanel();
		FlowLayout flowLayout_8 = (FlowLayout) panel_28.getLayout();
		flowLayout_8.setAlignment(FlowLayout.LEFT);
		panel_22.add(panel_28);
		
		directInputButton = new JRadioButton("직접 입력");
		group.add(directInputButton);
		directInputButton.addActionListener(e -> {
			serviceList.setEnabled(false);
			createTopicList.setEnabled(false);
			
			topicTextField.setEnabled(true);
		});
		panel_28.add(directInputButton);
		
		JPanel panel_26 = new JPanel();
		panel_22.add(panel_26);
		panel_26.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblNewLabel_7 = new JLabel("Topic Name:");
		panel_26.add(lblNewLabel_7);
		
		topicTextField = new JTextField();
		topicTextField.setEnabled(false);
		panel_26.add(topicTextField);
		topicTextField.setColumns(14);
		
		JPanel panel_10 = new JPanel();
		panel_10.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_10 = new GridBagConstraints();
		gbc_panel_10.fill = GridBagConstraints.BOTH;
		gbc_panel_10.gridx = 4;
		gbc_panel_10.gridy = 0;
		panel_5.add(panel_10, gbc_panel_10);
		panel_10.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_11 = new JPanel();
		panel_11.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout_3 = (FlowLayout) panel_11.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		panel_10.add(panel_11, BorderLayout.NORTH);
		
		JLabel lblNewLabel_4 = new JLabel("Control");
		lblNewLabel_4.setFont(new Font("굴림", Font.BOLD, 12));
		panel_11.add(lblNewLabel_4);
		
		JPanel panel_12 = new JPanel();
		panel_12.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_10.add(panel_12, BorderLayout.CENTER);
		GridBagLayout gbl_panel_12 = new GridBagLayout();
		gbl_panel_12.columnWidths = new int[]{230, 0};
		gbl_panel_12.rowHeights = new int[]{52, 52, 20, 108, 0};
		gbl_panel_12.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_12.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_12.setLayout(gbl_panel_12);
		
		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.insets = new Insets(0, 0, 5, 0);
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 0;
		panel_12.add(panel_6, gbc_panel_6);
		panel_6.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel panel_15 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_15.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_6.add(panel_15);
		
		JLabel lblNewLabel_3 = new JLabel("partition:");
		panel_15.add(lblNewLabel_3);
		
		partitionTextField = new JTextField();
		panel_15.add(partitionTextField);
		partitionTextField.setColumns(3);
		
		JPanel panel_16 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_16.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel_6.add(panel_16);
		
		JLabel lblNewLabel_5 = new JLabel("replication:");
		panel_16.add(lblNewLabel_5);
		
		replicationTextField = new JTextField();
		replicationTextField.setText("");
		panel_16.add(replicationTextField);
		replicationTextField.setColumns(3);
		
		JButton submitButton = new JButton("Create Topic");
		GridBagConstraints gbc_submitButton = new GridBagConstraints();
		gbc_submitButton.fill = GridBagConstraints.BOTH;
		gbc_submitButton.insets = new Insets(0, 0, 5, 0);
		gbc_submitButton.gridx = 0;
		gbc_submitButton.gridy = 1;
		panel_12.add(submitButton, gbc_submitButton);
		submitButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/check.png"))));
		submitButton.addActionListener(e -> KafkaPanel.this.createTopic());
		
		JPanel panel_17 = new JPanel();
		GridBagConstraints gbc_panel_17 = new GridBagConstraints();
		gbc_panel_17.fill = GridBagConstraints.BOTH;
		gbc_panel_17.insets = new Insets(0, 0, 5, 0);
		gbc_panel_17.gridx = 0;
		gbc_panel_17.gridy = 2;
		panel_12.add(panel_17, gbc_panel_17);
		panel_17.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_13 = new JPanel();
		GridBagConstraints gbc_panel_13 = new GridBagConstraints();
		gbc_panel_13.fill = GridBagConstraints.BOTH;
		gbc_panel_13.gridx = 0;
		gbc_panel_13.gridy = 3;
		panel_12.add(panel_13, gbc_panel_13);
		GridBagLayout gbl_panel_13 = new GridBagLayout();
		gbl_panel_13.columnWidths = new int[]{230, 0};
		gbl_panel_13.rowHeights = new int[]{25, 77, 42, 0};
		gbl_panel_13.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_13.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_13.setLayout(gbl_panel_13);
		
		JPanel panel_19 = new JPanel();
		GridBagConstraints gbc_panel_19 = new GridBagConstraints();
		gbc_panel_19.fill = GridBagConstraints.BOTH;
		gbc_panel_19.gridx = 0;
		gbc_panel_19.gridy = 0;
		panel_13.add(panel_19, gbc_panel_19);
	}

	public void connectSshKafka(Map<String, String> kafkaCommandMap) {
		this.kafkaCommandMap = kafkaCommandMap;
		
		new Thread(() -> this.connectSshKafka()).start();
	}
	
	public void connectSshKafka() {
//		if(!kafkaSshConnectFlag) {
			frame.showProgressBar();
			
//			kafkaSshManager = new SshManager();
//			
//			this.kafkaObservable = kafkaSshManager; //옵져버 등록                            
//	        kafkaObservable.addObserver(this);
//		
//			String host = kafkaCommandMap.get("kafka.server.ssh.host");
//			String port = kafkaCommandMap.get("kafka.server.ssh.port");
//			String userId = kafkaCommandMap.get("kafka.server.ssh.user.id");
//			String password = kafkaCommandMap.get("kafka.server.ssh.user.pwd");
//			String privateKeyPath = kafkaCommandMap.get("kafka.server.ssh.user.pk.path");
//			
//			kafkaSshConnectFlag = kafkaSshManager.connect(host, Integer.parseInt(port), userId, password, privateKeyPath);
			
			this.runKafkaTopicListCommand(); 
			this.displayServiceList();
			
			String partition = kafkaCommandMap.get("kafka.command.topic.default.partition");
			partitionTextField.setText(partition);
			
			String replication = kafkaCommandMap.get("kafka.command.topic.default.replication");
			replicationTextField.setText(replication);
//		}
	}
	
	public void disconnectSshKafka() {
//		if(kafkaSshManager != null) {
//			kafkaSshManager.disconnect();
//		}
	}
	
	private void runKafkaTopicListCommand() {
		String bootstrapServers = kafkaCommandMap.get("kafka.bootstrap.server");
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try(Admin admin = Admin.create(props)) {
            ListTopicsResult listTopics = admin.listTopics();
            try {
                Set<String> topics = listTopics.names().get();
                List<String> filteredTopicList = topics.stream()
                        .filter(topic -> !topic.contains("__amazon_msk_canary") && !topic.contains("__consumer_offsets"))
                        .collect(Collectors.toList());
                
                topicList.clear();
                topicList.addAll(filteredTopicList);
                this.updateKafkaTopicList();
                
            } catch(InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }//try
	}
	
	private void updateKafkaTopicList() {
		// 테이블 데이터 전체 삭제
		while(defaultTableModel.getRowCount() > 0) {
			defaultTableModel.removeRow(0);
		}
		
		// 테이블 sorter 초기화
        RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
        rowSorter.setSortKeys(null);
		
		Collections.sort(topicList);
		topicList.forEach(topic -> {
			Object[] rowData = new Object[4];
			rowData[0] = topic;
			rowData[1] = "Desc.";
			rowData[2] = "Open";
			rowData[3] = "Remove";
			
			defaultTableModel.addRow(rowData);
		});
		
		int size = topicList.size();
		countField.setText(String.valueOf(size));
		System.out.println("\n Topic Total Count: " + size);
		
		if(size != 0) {
			defaultTableModel.fireTableRowsInserted(0, size-1);
		}
		
		frame.closeProgressBar();
	}
	
	private void displayServiceList() {
		String topicPrefix = "kafka.topic";
		
		Set<String> keySet = kafkaCommandMap.keySet();
		List<String> kafkaTopicList = keySet.stream().filter(key -> key.contains(topicPrefix)).collect(Collectors.toList());
		
		List<String> serviceProjectNameList = kafkaTopicList.stream().map(topicName -> {
			int endIndex = topicName.indexOf('-');
			String substring = topicName.substring(0, endIndex);
			int startIndex = substring.lastIndexOf('.');
			return substring.substring(startIndex+1, endIndex); 
		}).distinct().sorted().collect(Collectors.toList());
		
		serviceListModel.clear();
		serviceProjectNameList.forEach(item -> serviceListModel.addElement(item));
	}

/*	
	@Override
	public void update(Observable obj, Object message) {
		if(message == null) {
			return;
		}
		
		if(obj instanceof SshManager) {
			String msg = (String)message;
//			if(msg.length() > 300) { //너무 긴 text 처리시 느려짐 방지를 위해 짜름
//				msg = msg.substring(0, 300)+"....";
//			}
			System.out.println(msg);
		}
		
		//frame.closeProgressBar();
		if(message.toString().contains("Exception")) {
			frame.closeProgressBar(); // 이거 정확하지 않음 -> 대충~
		}
	}
*/	
	
	private void runRemoveTopicCommand(int selectedRow) {
		int selectedRowIndex = table.convertRowIndexToModel(selectedRow);
		String topicName = (String) defaultTableModel.getValueAt(selectedRowIndex, 0);

		int result = JOptionPane.showConfirmDialog(this,
				(topicName + "을 삭제하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

		if(result == 0) {
			frame.showProgressBar();
			
			String bootstrapServers = kafkaCommandMap.get("kafka.bootstrap.server");
	        Properties props = new Properties();
	        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	        try(Admin admin = Admin.create(props)) {
	            DeleteTopicsResult deleteTopics = admin.deleteTopics(Arrays.asList(topicName));
	            KafkaFuture<Void> all = deleteTopics.all();
	            try {
                    all.get();
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
	        }
	        this.runKafkaTopicListCommand();
		}
	}
	
	private void printTopicInfo(int selectedRow) {
		new Thread(() -> {
			frame.showProgressBar();
			
			int selectedRowIndex = table.convertRowIndexToModel(selectedRow);
			String topicName = (String) defaultTableModel.getValueAt(selectedRowIndex, 0);
			
			String bootstrapServers = kafkaCommandMap.get("kafka.bootstrap.server");
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            try(Admin admin = Admin.create(props)) {
                DescribeTopicsResult describeTopics = admin.describeTopics(Arrays.asList(topicName));
                KafkaFuture<Map<String, TopicDescription>> all = describeTopics.all();
                try {
                    Map<String, TopicDescription> result = all.get();
                    TopicDescription topicDescription = result.get(topicName);
                    //--
                    String resultTopicName = topicDescription.name();
                    String resultTopicId = topicDescription.topicId().toString();
                    
                    List<TopicPartitionInfo> partitions = topicDescription.partitions();
                    int partitionCount = partitions.size();
                    
                    
                    int replicasCount = 0;
                    for(TopicPartitionInfo info: partitions) {
                        List<Node> replicas = info.replicas();
                        replicasCount = replicasCount + replicas.size();
                    }
                    int replicationFactor =  replicasCount / replicasCount;
                    
                    System.out.println("\n [Topic Infomation]");
                    System.out.printf(" - TopicName: %s\n", resultTopicName);
                    System.out.printf(" - TopicId: %s\n", resultTopicId);
                    System.out.printf(" - PartitionCount:%s\n", partitionCount);
                    System.out.printf(" - ReplicationFactor:%s\n", replicationFactor);
                } catch(InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
            frame.closeProgressBar();
		}).start();
	}
	
	private void displayConsumer(int selectedRow) {
		int selectedRowIndex = table.convertRowIndexToModel(selectedRow);
		String topicName = (String) defaultTableModel.getValueAt(selectedRowIndex, 0);
	
		String bootstrapServers = kafkaCommandMap.get("kafka.bootstrap.server");

		new Thread(() -> {
			frame.showProgressBar();

			KafkaConsumerViewer viewer = new KafkaConsumerViewer(KafkaPanel.this.consumerViewerCount, frame.getGraphicsConfiguration(), topicName);
			viewer.startConsumer(bootstrapServers, topicName);
			viewer.setVisible(true);
			KafkaPanel.this.consumerViewerCount ++;

			frame.closeProgressBar();
		}).start();
	}
	
	private void getTargetTopicByService(String selectedValue) {
		String servicePrefix = "kafka.topic." + selectedValue;
		
		Set<String> keySet = kafkaCommandMap.keySet();
		List<String> targetKeyList = keySet.stream().filter(key -> key.contains(servicePrefix)).collect(Collectors.toList());
		Collections.sort(targetKeyList);
		
		createTopicListModel.clear();
		targetKeyList.forEach(item -> createTopicListModel.addElement(kafkaCommandMap.get(item)));
	}

	private void createTopic() {
		List<String> selectedValuesList = null;
		if(listSelectedButton.isSelected()) {
			String serviceName = serviceList.getSelectedValue();
			selectedValuesList = createTopicList.getSelectedValuesList();
			
			if(StringUtils.isEmpty(serviceName)) {
				JOptionPane.showMessageDialog(KafkaPanel.this,
						"Service를 선택하세요.",
						"Information",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			if(selectedValuesList.size() == 0) {
				JOptionPane.showMessageDialog(KafkaPanel.this,
						"생성할 Topic을 List에서 선택하세요.",
						"Information",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		
		if(directInputButton.isSelected()) {
			String topicName = topicTextField.getText().trim();
			
			if(StringUtils.isEmpty(topicName)) {
				JOptionPane.showMessageDialog(KafkaPanel.this,
						"생성할 Topic Name을 입력하세요.",
						"Information",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			selectedValuesList = Arrays.asList(topicName);
		}
		
		String partition = partitionTextField.getText();
		if(StringUtils.isEmpty(partition)) {
			JOptionPane.showMessageDialog(KafkaPanel.this,
					"partition을 입력하세요.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int intPartition = Integer.parseInt(partition);
		if(intPartition < 0 || intPartition > 100) {
			JOptionPane.showMessageDialog(KafkaPanel.this,
					"partition 설정 값의 범위는 1 ~ 100 입니다.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		String replication = replicationTextField.getText();
		if(StringUtils.isEmpty(replication)) {
			JOptionPane.showMessageDialog(KafkaPanel.this,
					"replication을 입력하세요.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int intReplication = Integer.parseInt(replication);
		if(intReplication < 0 || intReplication > 100) {
			JOptionPane.showMessageDialog(KafkaPanel.this,
					"replication 설정 값의 범위는 1 ~ 100 입니다.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
//		if(kafkaSshConnectFlag) {
			frame.showProgressBar();
			
			String bootstrapServers = kafkaCommandMap.get("kafka.bootstrap.server");
			Properties props = new Properties();
			props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
			try(Admin admin = Admin.create(props)) {
			    List<NewTopic> topics = selectedValuesList.stream().map(topic -> new NewTopic(topic, Integer.parseInt(partition), Short.parseShort(replication))).collect(Collectors.toList());
			    
			    // Create a compacted topic
			    CreateTopicsResult result = admin.createTopics(topics);

			    selectedValuesList.forEach(topic -> {
			        // Call values() to get the result for a specific topic
			        KafkaFuture<Void> future = result.values().get(topic);

			        // Call get() to block until the topic creation is complete or has failed
			        // if creation failed the ExecutionException wraps the underlying cause.
			        try {
			            future.get();
			        } catch(InterruptedException | ExecutionException e) {
			            e.printStackTrace();
			        }
			    });
			}//try
			
			this.runKafkaTopicListCommand();
			//frame.closeProgressBar();
//		}
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
				if(column == 3) {
					setForeground(Color.RED);
				} else {
					setForeground(table.getSelectionForeground());
				}
				setBackground(table.getSelectionBackground());
			} else {
				if(column == 3) {
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
					KafkaPanel.this.printTopicInfo(selectedRow);
					break;
				case 2:
					KafkaPanel.this.displayConsumer(selectedRow);
					break;
				case 3:
					KafkaPanel.this.runRemoveTopicCommand(selectedRow);
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
}