package free.my.tool.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.apache.commons.lang3.StringUtils;

import free.my.tool.ui.panel.ConsolePanel;
import free.my.tool.ui.panel.KafkaPanel;
import free.my.tool.ui.panel.ManagePanel;
import free.my.tool.ui.panel.RedisContainerPanel;
import free.my.tool.ui.panel.ZookeeperPanel;
import free.my.tool.util.CommandConfigPropertiesUtil;
import free.my.tool.util.EtcPropertiesUtil;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private LoadingGlassPane glassPane;
	
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private KafkaPanel kafkaPanel;
	private ZookeeperPanel zookeeperPanel;
	private ManagePanel managePanel;
	private RedisContainerPanel redisContainerPanel;
	private ConsolePanel consolePanel;

	private JRadioButtonMenuItem lafMenu0; 
	private JRadioButtonMenuItem lafMenu1; 
	private JRadioButtonMenuItem lafMenu2; 
	private JRadioButtonMenuItem lafMenu3; 
	private JRadioButtonMenuItem lafMenu4; 
	private JRadioButtonMenuItem lafMenu5; 
	private JRadioButtonMenuItem lafMenu6; 
	private JRadioButtonMenuItem lafMenu7; 

	private Map<String, String> propertyMap;
	private JMenuItem mntmLoadConfig;
	
	private JSplitPane splitPane;
	
	private boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}
	
	/**
	 * Create the frame.
	 */
	public MainFrame(GraphicsConfiguration gc, String lastFramInfo) {
		super(gc);
		
		if(isWindows()) {
			setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/victory.png")));
		}
		setTitle("My Tool - Version 0.8.0");

		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				MainFrame.this.close();
			}
		}); 
		
//		this.setSize(1280, 768);
//		this.setLocationRelativeTo(null); // 화면 중앙에 display
		this.restoreLastFrameState(lastFramInfo);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('f');
		menuBar.add(mnFile);
		
		mntmLoadConfig = new JMenuItem("Load Config");
		mntmLoadConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.selectPropertyFile();
			}
		});
		mnFile.add(mntmLoadConfig);
		
		mnFile.addSeparator();
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.close();
			}
		});
		mnFile.add(mntmExit);

		JMenu mnAbout = new JMenu("Look&Feel");
		mnAbout.setMnemonic('l');
		menuBar.add(mnAbout);

		ButtonGroup group = new ButtonGroup();
		lafMenu0 = new JRadioButtonMenuItem("Default");
		lafMenu0.addActionListener(e -> changeLookAndFeel("Default"));
		mnAbout.add(lafMenu0);		
		group.add(lafMenu0);
		
		lafMenu1 = new JRadioButtonMenuItem("Metal(Ocean)");
		lafMenu1.addActionListener(e -> changeLookAndFeel("Metal(Ocean)"));
		mnAbout.add(lafMenu1);
		group.add(lafMenu1);

		lafMenu2 = new JRadioButtonMenuItem("Smart");
		lafMenu2.addActionListener(e -> changeLookAndFeel("Smart"));
		mnAbout.add(lafMenu2);
		group.add(lafMenu2);

		lafMenu3 = new JRadioButtonMenuItem("McWin");
		lafMenu3.addActionListener(e -> changeLookAndFeel("McWin"));
		mnAbout.add(lafMenu3);
		group.add(lafMenu3);

		lafMenu4 = new JRadioButtonMenuItem("HiFi");
		lafMenu4.addActionListener(e -> changeLookAndFeel("HiFi"));
		mnAbout.add(lafMenu4);
		group.add(lafMenu4);

		lafMenu5 = new JRadioButtonMenuItem("Acryl");
		lafMenu5.addActionListener(e -> changeLookAndFeel("Acryl"));
		mnAbout.add(lafMenu5);
		group.add(lafMenu5);

		lafMenu6 = new JRadioButtonMenuItem("Bernstein");
		lafMenu6.addActionListener(e -> changeLookAndFeel("Bernstein"));
		mnAbout.add(lafMenu6);
		group.add(lafMenu6);
		
		lafMenu7 = new JRadioButtonMenuItem("Nimbus");
		lafMenu7.addActionListener(e -> changeLookAndFeel("Nimbus"));
		mnAbout.add(lafMenu7);
		group.add(lafMenu7);

		JMenu mnAbout_1 = new JMenu("About");
		mnAbout_1.setMnemonic('a');
		menuBar.add(mnAbout_1);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(e -> {
			JOptionPane.showMessageDialog(MainFrame.this,
					"My Tool\nVersion 0.8.0\n\n개발: 서봉근\nE-Mail: seobk77@gmail.com",
					"About",
					JOptionPane.INFORMATION_MESSAGE);

		});
		mnAbout_1.add(mntmAbout);

		contentPane = new JPanel();
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(e -> {
			if(consolePanel != null) {
				consolePanel.clear();
			}
			
			int selectedIndex = tabbedPane.getSelectedIndex();
			switch(selectedIndex) {
			case 0: //managePanel
				//this.initManageServer();
				break;
			case 1: //zookeeperPanel
				this.initZookeeper();
				break;
			case 2: //kafkaPanel
				this.initKafka();
				break;
			case 3: //redisContainerPanel
				this.initRedis(); 
				break;
			default:
				break;
			}
		});

		managePanel = new ManagePanel(this);
		tabbedPane.addTab("Manage", null, managePanel, null);
		
		zookeeperPanel = new ZookeeperPanel(this);
		tabbedPane.addTab("Zookeeper", null, zookeeperPanel, null);

		kafkaPanel = new KafkaPanel(this);
		tabbedPane.addTab("Kafka", null, kafkaPanel, null);

		redisContainerPanel = new RedisContainerPanel(this);
		tabbedPane.addTab("Redis", null, redisContainerPanel, null);
		
		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(tabbedPane);

		consolePanel = new ConsolePanel();
		splitPane.setBottomComponent(consolePanel);
		splitPane.setDividerSize(8);
		
		//splitPane.setDividerLocation(430);
		this.settingDividerLocation(splitPane, lastFramInfo);
		
		splitPane.setOneTouchExpandable(true);

		contentPane.add(splitPane, BorderLayout.CENTER);
		
		glassPane = new LoadingGlassPane();
		this.setGlassPane(glassPane);
	}

	public void showProgressBar() {
		if(!glassPane.isVisible()) {
			glassPane.start();
			glassPane.setVisible(true);
		}
	}

	public void closeProgressBar() {
		glassPane.setVisible(false);
		glassPane.stop();
	}

	private void close() {
		try { zookeeperPanel.disconnectZookeeper(); } catch (Exception e) {}  //무식하게 모두 무시 ㅡㅡ;
		try { redisContainerPanel.disconnectAllRedis(); } catch (Exception e) {}  
		try { kafkaPanel.disconnectSshKafka(); } catch (Exception e) {}

		this.saveLastFrameState();
		
		//this.setVisible(false);
		this.dispose();
		System.exit(0);
	}

	private void changeLookAndFeel(String lookAndFeel) {
		EtcPropertiesUtil.writeLookAndFeel(lookAndFeel);

		try {
			switch(lookAndFeel) {
			case "Default":
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				break;
			case "Metal(Ocean)":
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
				UIManager.setLookAndFeel(new MetalLookAndFeel()); 
				break;
			case "Smart":
				UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
				break;
			case "McWin":
				UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
				break;
			case "HiFi":
				UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
				break;
			case "Acryl":
				UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
				break;
			case "Bernstein":
				UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
				break;
			case "Nimbus":
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				break;	
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.updateComponentTreeUI(this);
		MainFrame.this.repaint();
	}

	public void init(String lookAndFeel) {
		switch(lookAndFeel) {
		case "Default":
			lafMenu0.setSelected(true);
			break;
		case "Metal(Ocean)":
			lafMenu1.setSelected(true);
			break;
		case "Smart":
			lafMenu2.setSelected(true);
			break;
		case "McWin":
			lafMenu3.setSelected(true);
			break;
		case "HiFi":
			lafMenu4.setSelected(true);
			break;
		case "Acryl":
			lafMenu5.setSelected(true);
			break;
		case "Bernstein":
			lafMenu6.setSelected(true);
			break;
		case "Nimbus":
			lafMenu7.setSelected(true);
			break;
		default:
			break;
		}

		//config 정보 로딩
		String configFileName = EtcPropertiesUtil.readCurrentConfigFileName();
		if(StringUtils.isEmpty(configFileName)) {
			String propertyFile = this.selectPropertyFile();
			managePanel.checkContractPeriod();//* 기간 체크
			if(propertyFile == null) {
				this.close();
			}
			
		} else {
			String userDir = System.getProperty("user.dir");
			String configDir = userDir + "//config";
			String configPath = configDir + "//" + configFileName;
			this.propertyMap = CommandConfigPropertiesUtil.readAllProperty(configPath);
			//setTitle("Just Tool - (" + configFileName + ")"); // 타이틀 바꾸고
			consolePanel.settingPropertiesName(configFileName);
			
			//this.initKafka();
			//this.initZookeeper();
			this.initManageServer();
		}
		
		System.out.print(0); //인트로 로딩 window 없애기 위해(꼼수)
	}

	public String selectPropertyFile() {
		String userDir = System.getProperty("user.dir");
		File configDir = new File(userDir + "//config"); 
		File[] fileList = configDir.listFiles();
		Arrays.sort(fileList);
		
		List<String> configList = new ArrayList<>();
		for(File file : fileList) {
			if(file.isFile()) {
				if(file.getName().contains(".properties")) {
					configList.add(file.getName());
				}
			}
		}
		
		Object[] inputs = configList.toArray();
		
		String currentCofingFileName = EtcPropertiesUtil.readCurrentConfigFileName();
		if(StringUtils.isEmpty(currentCofingFileName)) {
			currentCofingFileName = (String)inputs[0];
		}
		
		String configFileName = (String)JOptionPane.showInputDialog(this, "Select Properties", "Load Config", JOptionPane.PLAIN_MESSAGE, null, inputs, currentCofingFileName);
		if(configFileName != null) {
			EtcPropertiesUtil.writeCurrentConfigFileName(configFileName);  //파일 이름 저장해 놓고 
			
			String path = configDir + "//" + configFileName;
			this.propertyMap = CommandConfigPropertiesUtil.readAllProperty(path);
			
			//초기화!?
			try { zookeeperPanel.disconnectZookeeper(); } catch (Exception e) {}  //무식하게 모두 무시 ㅡㅡ;
			try { redisContainerPanel.disconnectAllRedis(); } catch (Exception e) {}  
			try { kafkaPanel.disconnectSshKafka(); } catch (Exception e) {}
			
			tabbedPane.removeAll();
			
			managePanel = new ManagePanel(this);
			tabbedPane.addTab("Manage", null, managePanel, null);
			
			zookeeperPanel = new ZookeeperPanel(this);
			tabbedPane.addTab("Zookeeper", null, zookeeperPanel, null);

			kafkaPanel = new KafkaPanel(this);
			tabbedPane.addTab("Kafka", null, kafkaPanel, null);

			redisContainerPanel = new RedisContainerPanel(this);
			tabbedPane.addTab("Redis", null, redisContainerPanel, null);

			//this.initKafka();
			//this.initZookeeper();
			this.initManageServer();
			
			//setTitle("Just Tool - (" + configFileName + ")"); // 타이틀 바꾸고
			consolePanel.settingPropertiesName(configFileName);
			
			System.out.println(configFileName + " Loading Success~");
		}
		
		return configFileName;
	}
	
	public void initManageServer() {
		Set<String> keySet = propertyMap.keySet(); 

		Map<String, String> manageInfo = new HashMap<>();
		keySet.forEach(key -> {
			int index = key.indexOf('.');
			String systemKey = key.substring(0, index);

			if("manage".equals(systemKey)) {
				manageInfo.put(key, propertyMap.get(key));
			}			
		});

		managePanel.setManageServerInfo(manageInfo);
	}
	
	public void initZookeeper() {
		Set<String> keySet = propertyMap.keySet();

		Map<String, String> zookeeperInfo = new HashMap<>();
		keySet.forEach(key -> {
			int index = key.indexOf('.');
			String systemKey = key.substring(0, index);

			if("zookeeper".equals(systemKey)) {
				zookeeperInfo.put(key, propertyMap.get(key));
			}			
		});

		zookeeperPanel.connectZookeeper(zookeeperInfo);
	}

	public void initRedis() {
		Set<String> keySet = propertyMap.keySet();

		Map<String, String> redisInfo = new HashMap<>();
		keySet.forEach(key -> {
			int index = key.indexOf('.');
			String systemKey = key.substring(0, index);

			if("redis".equals(systemKey)) {
				redisInfo.put(key, propertyMap.get(key));
			}			
		});

		redisContainerPanel.prepareRedisPanel(redisInfo);
	}

	public void initKafka() {
		Set<String> keySet = propertyMap.keySet();

		Map<String, String> kafkaInfo = new HashMap<>();
		keySet.forEach(key -> {
			int index = key.indexOf('.');
			String systemKey = key.substring(0, index);

			if("kafka".equals(systemKey)) {
				kafkaInfo.put(key, propertyMap.get(key));
			}			
		});

		kafkaPanel.connectSshKafka(kafkaInfo);
	}

	public void redirectSystemStreams() {
		consolePanel.redirectSystemStreams(); //-> SOP 낚아채기
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					String lookAndFeel = EtcPropertiesUtil.readLookAndFeel();
					lookAndFeel = (lookAndFeel == null)? "Default": lookAndFeel;
					switch(lookAndFeel) {
					case "Default":
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						break;
					case "Metal(Ocean)":
						MetalLookAndFeel.setCurrentTheme(new OceanTheme());
						UIManager.setLookAndFeel(new MetalLookAndFeel()); 
						break;
					case "Smart":
						UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
						break;
					case "McWin":
						UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
						break;
					case "HiFi":
						UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
						break;
					case "Acryl":
						UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
						break;
					case "Bernstein":
						UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
						break;
					case "Nimbus":
						UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
						break;	
					default:
						break;
					}
					
					String lastFramInfo = EtcPropertiesUtil.readLastFramInfo();

					MainFrame frame = new MainFrame(MainFrame.getGraphicsConfig(lastFramInfo), lastFramInfo);
					frame.init(lookAndFeel);
					frame.setVisible(true);
					
//					System.out.print(0); //인트로 로딩 window 없애기 위해(꼼수)
					frame.redirectSystemStreams(); //-> SOP 낚아채기
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void saveLastFrameState() {
		/*멀티 모니터 체크해서 어느 모니터에서 실행되고 있는지 확인*/ 
		//전체 GraphicsDevice 가져오고
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		
		//현재 GraphicsDevice 정보 가져오기
		GraphicsConfiguration graphicsConfiguration = this.getGraphicsConfiguration();
		GraphicsDevice cgd = graphicsConfiguration.getDevice();
		
		int screenNumber = 0;
		for(int i=0; i<gs.length; i++) {
			GraphicsDevice gd = gs[i];
			if(cgd.equals(gd)) {
				screenNumber = i;
				break;
			}
		}
		
		//현재 Frame 의 위치, 사이즈 등의 정보 get	
		int extendedState = this.getExtendedState();
		Rectangle bounds = this.getBounds();
		double x = bounds.getX();
		double y = bounds.getY();
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		
		int dividerLocation = splitPane.getDividerLocation();
		
		StringBuffer frameInfo = new StringBuffer();
		frameInfo.append(screenNumber).append(',');
		frameInfo.append(extendedState).append(',');
		frameInfo.append(x).append(',');
		frameInfo.append(y).append(',');
		frameInfo.append(width).append(',');
		frameInfo.append(height).append(',');
		frameInfo.append(dividerLocation);
		EtcPropertiesUtil.writeLastFramInfo(frameInfo.toString());
	}
	
	private void restoreLastFrameState(String framInfo) {
		try {
			if(StringUtils.isEmpty(framInfo)) {
				this.setSize(1280, 768);
				this.setLocationRelativeTo(null); // 화면 중앙에 display
			} else {
				String[] framInfos = framInfo.split(",");

				int extendedState = Integer.parseInt(framInfos[1]);
				double x = Double.parseDouble(framInfos[2]);
				double y = Double.parseDouble(framInfos[3]);
				double width = Double.parseDouble(framInfos[4]);
				double height = Double.parseDouble(framInfos[5]);

				this.setExtendedState(extendedState);
				this.setBounds((int)x, (int)y, (int)width, (int)height);
			}
		} catch (Exception e) {
			this.setSize(1280, 768);
			this.setLocationRelativeTo(null); // 화면 중앙에 display
		}
	}
	
	private static GraphicsConfiguration getGraphicsConfig(String framInfo) {
		GraphicsConfiguration gc = null;
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		
		try {
			if(StringUtils.isEmpty(framInfo)) {
				gc = gs[0].getDefaultConfiguration();
			} else {
				String[] framInfos = framInfo.split(",");
				int screenNumber = Integer.parseInt(framInfos[0]);
				gc = gs[screenNumber].getDefaultConfiguration();
			}
		} catch (Exception e) {
			gc = gs[0].getDefaultConfiguration();
		}
		
		return gc;
	}
	
	private void settingDividerLocation(JSplitPane jSplitPane, String framInfo) {
		try {
			if(StringUtils.isEmpty(framInfo)) {
				jSplitPane.setDividerLocation(430);
			} else {
				String[] framInfos = framInfo.split(",");
				
				int dividerLocation = Integer.parseInt(framInfos[6]);
				jSplitPane.setDividerLocation(dividerLocation);
			}
		} catch (Exception e) {
			jSplitPane.setDividerLocation(430);
		}
	}
}
