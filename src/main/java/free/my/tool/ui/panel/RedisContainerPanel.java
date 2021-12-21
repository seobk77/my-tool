package free.my.tool.ui.panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import free.my.tool.ui.MainFrame;

public class RedisContainerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private MainFrame frame;
	private JPanel centerPanel;
	private List<RedisPanelInterface> redisPanelList; 
	private boolean redisListConnectFlag;
	private JTextField textField;
	
	private boolean isShowDistinctKeyCount;
	
	/**
	 * Create the panel.
	 */
	public RedisContainerPanel(MainFrame frame) {
		this.frame = frame;
		
		this.setLayout(new BorderLayout());
		centerPanel = new JPanel();
		this.add(centerPanel);
		
		JPanel panel = new JPanel();
		this.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel cardPanel = new JPanel();
		panel.add(cardPanel, BorderLayout.CENTER);
		CardLayout cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		
		JPanel hidePanel = new JPanel();
		hidePanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 38));
		cardPanel.add(hidePanel, "minus");
		hidePanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_5 = new JPanel();
		hidePanel.add(panel_5, BorderLayout.EAST);
		
		JPanel showPanel = new JPanel();
		showPanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 38));
		cardPanel.add(showPanel, "plus");
		showPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		showPanel.add(panel_1, BorderLayout.EAST);
		
		JPanel panel_2 = new JPanel();
		showPanel.add(panel_2, BorderLayout.CENTER);
		
		JPanel panel_3 = new JPanel();
		hidePanel.add(panel_3, BorderLayout.CENTER);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{60, 570, 308, 0};
		gbl_panel_3.rowHeights = new int[]{33, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		JPanel panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.insets = new Insets(0, 0, 0, 5);
		gbc_panel_7.gridx = 0;
		gbc_panel_7.gridy = 0;
		panel_3.add(panel_7, gbc_panel_7);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		JLabel lblRedis = new JLabel(" * 전체");
		panel_7.add(lblRedis);
		lblRedis.setFont(new Font("굴림", Font.BOLD, 12));
		
		JPanel panel_6 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_6.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.insets = new Insets(0, 0, 0, 5);
		gbc_panel_6.gridx = 1;
		gbc_panel_6.gridy = 0;
		panel_3.add(panel_6, gbc_panel_6);
		
		JLabel lblNewLabel = new JLabel("Key:");
		panel_6.add(lblNewLabel);
		
		textField = new JTextField("*");
		panel_6.add(textField);
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					RedisContainerPanel.this.searchKeyList();
				}
			}
		});
		textField.setColumns(25);
		
		JButton btnNewButton = new JButton("조회");
		panel_6.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RedisContainerPanel.this.searchKeyList();
			}
		});
		btnNewButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_find.png"))));
		
		JButton btnNewButton_1 = new JButton("결과내 찾기");
		btnNewButton_1.setEnabled(false);
		panel_6.add(btnNewButton_1);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String key = textField.getText();
				redisPanelList.forEach(redis -> redis.searchKey(key));
			}
		});
		btnNewButton_1.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_find.png"))));
		
		JPanel panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 2;
		gbc_panel_4.gridy = 0;
		panel_3.add(panel_4, gbc_panel_4);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("Display key deduplication count   ");
		chckbxNewCheckBox.setEnabled(false);
		chckbxNewCheckBox.setSelected(false);
		chckbxNewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isShowDistinctKeyCount = chckbxNewCheckBox.isSelected();
			}
		});
		panel_4.setLayout(new BorderLayout(0, 0));
		panel_4.add(chckbxNewCheckBox, BorderLayout.EAST);
		
		JButton plusButton = new JButton();
		plusButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/down_hide.png"))));
		
		plusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.next(cardPanel);
				
				hidePanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 38));
				showPanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 38));
			}
		});
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_1.add(plusButton, BorderLayout.NORTH);
		
		JButton minusButton = new JButton();
		minusButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/up_show.png"))));
		minusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.next(cardPanel);
				
				hidePanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 18));
				showPanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 18));
			}
		});
		panel_5.setLayout(new BorderLayout(0, 0));
		panel_5.add(minusButton, BorderLayout.SOUTH);
	}

	public void prepareRedisPanel(Map<String, String> redisInfoMap) {
		if(redisListConnectFlag) {
			return;
		}
		
		Set<String> keySet = redisInfoMap.keySet();
		List<String> keyList = keySet.stream()
		        .filter(key -> key.startsWith("redis.server"))
		        .filter(key -> key.lastIndexOf(".host") >= 0)
		        .sorted()
		        .collect(Collectors.toList());
		//keyList.forEach(System.out::println);
		
		List<String> serverInfoList = new ArrayList<>();
		for(String key : keyList) {
			int beginIndex = key.indexOf("server");
			int endIndex = key.lastIndexOf(".host");
			
			serverInfoList.add(key.substring(beginIndex, endIndex));
		}
		//serverInfoList.forEach(System.out::println);
		
		String encoding = redisInfoMap.get("redis.value.base64.encoding");
		boolean applyBase64Encoding = Boolean.valueOf(encoding);
		
		String clusterMode = redisInfoMap.get("redis.connection.cluster.mode");
		boolean isClusterMode = false;
		if(StringUtils.isNotEmpty(clusterMode)) {
		    isClusterMode = Boolean.parseBoolean(clusterMode);
		}
		
		String temp = null;
		List<String> hostList = new ArrayList<>();
		List<String> portList = new ArrayList<>();
		List<String> passwordList = new ArrayList<>();
		List<String> databaseList = new ArrayList<>();
		List<String> numOfDatabaseList = new ArrayList<>();
		for(String server : serverInfoList) {
			hostList.add( redisInfoMap.get("redis." +server+ ".host") );
			portList.add( redisInfoMap.get("redis." +server+ ".port") );
			passwordList.add( redisInfoMap.get("redis." +server+ ".password") );
			passwordList.add( redisInfoMap.get("redis." +server+ ".password") );
			
			temp = redisInfoMap.get("redis." +server+ ".database");
			databaseList.add( StringUtils.isEmpty(temp) ? "0" : temp);
			
			temp = redisInfoMap.get("redis." +server+ ".database.num");
			numOfDatabaseList.add( StringUtils.isEmpty(temp) ? "16" : temp );
		}
		
		centerPanel.setLayout(new GridLayout(1, hostList.size()));
		
		redisPanelList = new ArrayList<>();
		for(int i=0; i<hostList.size(); i++) {
		    if(isClusterMode) {
		        RedisClusterPanel redisClusterPanel = new RedisClusterPanel(frame);
                centerPanel.add(redisClusterPanel);
                
                redisClusterPanel.connectRedis(hostList.get(i), null, passwordList.get(i), null, null, applyBase64Encoding);
                
                redisPanelList.add(redisClusterPanel);
		    } else {
		        RedisPanel redisPanel = new RedisPanel(frame);
		        centerPanel.add(redisPanel);
		        
		        redisPanel.connectRedis(hostList.get(i), portList.get(i), passwordList.get(i), databaseList.get(i), numOfDatabaseList.get(i), applyBase64Encoding);
		        
		        redisPanelList.add(redisPanel);
		    }
		}
		
		redisListConnectFlag = true;
	}
	
	public void disconnectAllRedis() {
		redisPanelList.forEach(redis -> redis.disconnectRedis());
		redisListConnectFlag = false;
	}
	
	private void searchKeyList() {
		new Thread(() -> {
			frame.showProgressBar();

			String key = textField.getText();
			
			if(isShowDistinctKeyCount) {
				List<String> allList = new ArrayList<>();
				redisPanelList.forEach(redisPanel -> {
					List<String> keyList = redisPanel.getKeyList(true, key);
					if(keyList != null) {
						allList.addAll(keyList);
					}
				});
	
				long count = allList.stream().distinct().count();
				System.out.println("\nAll Redis Server: ["+key+"] Key Deduplication Count -> " + count);
			} else {
				redisPanelList.forEach(redisPanel -> {
					redisPanel.getKeyList(true, key);
				});
			}
			
			frame.closeProgressBar();
		}).start();
	}
}