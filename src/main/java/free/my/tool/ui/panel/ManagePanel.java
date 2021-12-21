package free.my.tool.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import free.my.tool.manager.SshManager;
import free.my.tool.ui.MainFrame;

public class ManagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private int manageServerCount = 0;
	
	private MainFrame frame;
	
	private SshManager sshManager;
	private boolean sshConnectFlag;
	
	private Map<String, String> manageInfo = null;
	private String[] hostList = null;
	private int port = 22;
	private String userId = null;
	private String userPassword = null;
	private String privateKeyPath = null;
	
	private DefaultListModel<String> zookeeperStatusListModel;
	private DefaultListModel<String> kafkaStatusListModel;
	
	private JButton startZookeeperButton;
	private JButton stopZookeeperButton;
	private JButton startKafkaButton;
	private JButton stopKafkaButton;
	
	
	/**
	 * Create the panel.
	 */
	public ManagePanel(MainFrame frame) {
		this.frame = frame;
		setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(panel_5, BorderLayout.NORTH);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Zookeeper");
		lblNewLabel.setFont(new Font("굴림", Font.BOLD, 12));
		panel_5.add(lblNewLabel, BorderLayout.WEST);
		
		JButton btnNewButton = new JButton("Check");
		btnNewButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/refresh.png"))));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ManagePanel.this.checkAllZookeeperStatus();
			}
		});
		panel_5.add(btnNewButton, BorderLayout.EAST);
		
		JPanel panel_7 = new JPanel();
		panel.add(panel_7, BorderLayout.CENTER);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		zookeeperStatusListModel = new DefaultListModel<>();
		JList<String> zookeeperStatusList = new JList<>(zookeeperStatusListModel);
		zookeeperStatusList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_7.add(zookeeperStatusList, BorderLayout.CENTER);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(panel_8, BorderLayout.SOUTH);
		panel_8.setLayout(new GridLayout(0, 2, 2, 0));
		
		startZookeeperButton = new JButton("Start All Node");
		startZookeeperButton.setEnabled(false);
		startZookeeperButton.setForeground(Color.BLUE);
		startZookeeperButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startZookeeperButton.setEnabled(false);
				ManagePanel.this.startAllZookeeper();				
			}
		});
		panel_8.add(startZookeeperButton);
		
		stopZookeeperButton = new JButton("Stop All Node");
		stopZookeeperButton.setEnabled(false);
		stopZookeeperButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(ManagePanel.this,
						("모든 Zookeeper Node를 Stop 하시겠습니까?\n먼저 Kafka를 모두 Stop 하셔야 합니다."), "Choose one", JOptionPane.YES_NO_OPTION);

				if(result == 0) {
					ManagePanel.this.stopAllZookeeper();
//					startZookeeperButton.setEnabled(true);
				}
			}
		});
		stopZookeeperButton.setForeground(Color.RED);
		panel_8.add(stopZookeeperButton);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_4.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_1 = new JLabel("Kafka");
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 12));
		panel_2.add(lblNewLabel_1, BorderLayout.WEST);
		
		JButton btnNewButton_1 = new JButton("Check");
		btnNewButton_1.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/refresh.png"))));
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ManagePanel.this.checkAllKafkaStatus();
			}
		});
		panel_2.add(btnNewButton_1, BorderLayout.EAST);
		
		JPanel panel_3 = new JPanel();
		panel_4.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		kafkaStatusListModel = new DefaultListModel<>();
		JList<String> kafkaStatusList = new JList<>(kafkaStatusListModel);
		kafkaStatusList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_3.add(kafkaStatusList, BorderLayout.CENTER);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_4.add(panel_6, BorderLayout.SOUTH);
		panel_6.setLayout(new GridLayout(0, 2, 2, 0));
		
		startKafkaButton = new JButton("Start All Node");
		startKafkaButton.setEnabled(false);
		startKafkaButton.setForeground(Color.BLUE);
		startKafkaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startKafkaButton.setEnabled(false);
				ManagePanel.this.startAllKafka();
			}
		});
		panel_6.add(startKafkaButton);
		
		stopKafkaButton = new JButton("Stop All Node");
		stopKafkaButton.setEnabled(false);
		stopKafkaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(ManagePanel.this,
						("모든 Kafka Node를 Stop 하시겠습니까?"), "Choose one", JOptionPane.YES_NO_OPTION);

				if(result == 0) {
					ManagePanel.this.stopAllKafka();
//					startKafkaButton.setEnabled(true);
				}
			}
		});
		stopKafkaButton.setForeground(Color.RED);
		panel_6.add(stopKafkaButton);
		
		
		this.setEnableRec(this, false); //전체 disable
	}

	protected void checkAllZookeeperStatus() {
		zookeeperStatusListModel.clear();
		
		String command = manageInfo.get("manage.server.zookeeper.jps");
		new Thread(() -> this.runAllServerCommandWithJList(command, "zookeeper", zookeeperStatusListModel)).start();
	}

	protected void checkAllKafkaStatus() {
		kafkaStatusListModel.clear();
		
		String command = manageInfo.get("manage.server.kafka.jps");
		new Thread(() -> this.runAllServerCommandWithJList(command, "kafka", kafkaStatusListModel)).start();
	}

	protected void startAllZookeeper() {
		String command = manageInfo.get("manage.server.zookeeper.start.script");
		new Thread(() -> {
			this.runAllServerCommand(command, "zookeeper", zookeeperStatusListModel);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.checkAllZookeeperStatus();
		}).start();
	}
	protected void stopAllZookeeper() {
		String command = manageInfo.get("manage.server.zookeeper.stop.script");
		new Thread(() -> { 
			this.runAllServerCommand(command, "zookeeper", zookeeperStatusListModel);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.checkAllZookeeperStatus();
		}).start();
	}
	
	protected void startAllKafka() {
		String command = manageInfo.get("manage.server.kafka.start.script");
		new Thread(() -> {
			this.runAllServerCommand(command, "kafka", kafkaStatusListModel);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.checkAllKafkaStatus();
		}).start();
	}
	
	protected void stopAllKafka() {
		String command = manageInfo.get("manage.server.kafka.stop.script");
		new Thread(() -> {
			this.runAllServerCommand(command, "kafka", kafkaStatusListModel);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.checkAllKafkaStatus();
		}).start();
	}
	
	public void setManageServerInfo(Map<String, String> manageInfo) {
		this.checkContractPeriod(); //기간 체크
		
		this.manageInfo = manageInfo;
		
		String hosts = manageInfo.get("manage.server.ssh.hosts");
		this.hostList = hosts.split(",");
		this.manageServerCount = hostList.length;
		this.port = Integer.parseInt(manageInfo.get("manage.server.ssh.port"));
		this.userId = manageInfo.get("manage.server.ssh.user.id");
		this.userPassword = manageInfo.get("manage.server.ssh.user.pwd");
		this.privateKeyPath = manageInfo.get("manage.server.ssh.user.pk.path");
	}
	
	private void runAllServerCommand(String command, String serverType, DefaultListModel<String> listModel) {
		frame.showProgressBar();
		for(String host : hostList) {
			this.connectSshServer(host);
			this.displayRunResult(host, command);
			this.disconnectSshServer();
		}
		frame.closeProgressBar();
	}
	
	private void runAllServerCommandWithJList(String command, String serverType, DefaultListModel<String> listModel) {
		frame.showProgressBar();
		for(String host : hostList) {
			this.connectSshServer(host);
			this.displayRunResultWithJList(host, command, serverType, listModel);
			this.disconnectSshServer();
		}
		frame.closeProgressBar();
	}
	
	public void connectSshServer(String host) {
		sshManager = new SshManager();
		sshConnectFlag = sshManager.connect(host, port, userId, userPassword, privateKeyPath);
	}
	
	public void disconnectSshServer() {
		if(sshManager != null) {
			sshManager.disconnect();
		}
		
		sshConnectFlag = false;
	}
	
	private void displayRunResultWithJList(String host, String command, String serverType, DefaultListModel<String> listModel) {
		if(sshConnectFlag) {
			String result = sshManager.exec(command).trim();
			System.out.println("[HOST: "+ host + "] [COMMAND: " + command + "]");
			System.out.println(result);
			
			if(!"".equals(result)) {
				String[] splitResult = result.split("\\s{2,}+|[\\t++\\r++\\n++]");
				String[] processInfo = null;
				for(String res : splitResult) {
					processInfo = res.split(" ");
					if("java".equals(processInfo[1])) {
					    listModel.addElement("["+host+"]  " + processInfo[1] + "("+processInfo[0]+")");
					}
				}
			}
			
			int size = listModel.size();
			switch(serverType) {
			case "zookeeper":
				if(size == 0) {
					startZookeeperButton.setEnabled(true);
					stopZookeeperButton.setEnabled(false);
				} else if(size == this.manageServerCount) {
					startZookeeperButton.setEnabled(false);
					stopZookeeperButton.setEnabled(true);
				} else {
					startZookeeperButton.setEnabled(true);
					stopZookeeperButton.setEnabled(true);
				}
				break;
				
			case "kafka":
				if(size == 0) {
					startKafkaButton.setEnabled(true);
					stopKafkaButton.setEnabled(false);
				} else if(size == this.manageServerCount) {
					startKafkaButton.setEnabled(false);
					stopKafkaButton.setEnabled(true);
				} else {
					startKafkaButton.setEnabled(true);
					stopKafkaButton.setEnabled(true);
				}
				break;
				
			default:
				break;
			}
		}
	}
	
	private void displayRunResult(String host, String command) {
		if(sshConnectFlag) {
			String result = sshManager.exec(command);
			System.out.println("[HOST: "+ host + "] [COMMAND: " + command + "]");
			System.out.println(result);
		}
	}
	
	public void checkContractPeriod() {
/*	    
		String key = EtcPropertiesUtil.readPassKey();
		if(StringUtils.isEmpty(key)) {
			key = "rj1v+JxS+N7M8pl3bBUm+J4ECROH3/d1vokYu9KdA+U=";  //2018-03-31 default
//			        pass.key=x4whWRufVwuA7cu//JeM4AR/ssthfarMWPoJXnin5LM\=
//			        pass.key=vRIt7qaro0kLTFOFddB8bmckpn0wNi15UBD48spd53A= //만료
		}
		try {
			EncUtil eu = new EncUtil();
			String dec = eu.decAES(key);
			String sub1 = dec.substring(3, 5);
			String sub2 = dec.substring(7, 9);
			String sub3 = dec.substring(11, 13);
			String sub4 = dec.substring(15, 17);

			String inputDate = String.format("%s%s-%s-%s", sub1, sub2, sub3, sub4);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date compareDate = formatter.parse(inputDate);
			long compareTime = compareDate.getTime();
			long currentTime = System.currentTimeMillis();
			long result = compareTime - currentTime;

			if(result > 0l) {
				//통과
			} else {
				System.out.print(0); //인트로 로딩 window 없애기 위해(꼼수)

				JOptionPane.showMessageDialog(null,
						"Sorry!\nIt's expiration of contract period.",
						"ERROR",
						JOptionPane.ERROR_MESSAGE);

				System.exit(ERROR);
			}

		} catch (Exception e1) {
			System.exit(ERROR);
		}
*/		
	}
	
	
	//JPanel 전체 disable/enable
	private void setEnableRec(Container container, boolean enable) {
	    container.setEnabled(enable);

	    try {
	        Component[] components = container.getComponents();
	        for(int i = 0; i < components.length; i++) {
	            this.setEnableRec((Container) components[i], enable);
	        }
	    } catch(ClassCastException e) {
	        e.printStackTrace();
	    }
	}
	
}
