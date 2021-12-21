package free.my.tool.ui.viewer;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import free.my.tool.ui.MainFrame;
import free.my.tool.util.DateUtil;

public class KafkaConsumerViewer extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane = null;
	private JTextArea textArea = null;
	public JTextField bufferField = null;

	public boolean logWriteFlag = true;

	private JLabel findLabel = null;
	private JTextField findTextField = null;
	private JButton upButton = null;
	private JButton downButton = null;

	private JToggleButton tglbtnScrollLock = null; 

	private Robot robot = null;

	private String bootstrapServers;
	private String topic;
	private KafkaConsumer<String, String> consumer;
	private Producer<String, String> producer;
	private boolean startFalg = true;

	private String strMaxLength = "100";
	private int maxLength = 100;

	private boolean isShowEntireMessage = false;
	private JTextField countField;
	private long messageCount = 0l;
	private JTextField messageField;
	private JTextField partitionField;

	private boolean toggleFlag = false;
	private JButton sendButton;

	private boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}

	public KafkaConsumerViewer(int consumerViewerCount, GraphicsConfiguration gc, String topic) {

		try {
			robot = new Robot();
		} catch (AWTException e3) {
			//무시
		}

		//this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				KafkaConsumerViewer.this.exitKafkaConsumerViewer();
			}
		});

		this.setTitle("Kafka Consumer");
		if(isWindows()) {
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/super_man.png")));
		}

		//this.setSize(857, 700);
		Rectangle bounds = gc.getBounds();
		int x = bounds.x + (consumerViewerCount % 5) * 45;
		int y = bounds.y + (consumerViewerCount % 5) * 40;
		this.setBounds(x, y, 1063, 700);
		//this.setResizable(false);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		textArea = new JTextArea();
		textArea.setFont(new Font("굴림", Font.PLAIN, 13));
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.WHITE);
		textArea.setCaretColor(Color.WHITE);
		textArea.setFocusAccelerator('q');
		textArea.setLineWrap(false);
		textArea.setWrapStyleWord(true);
		//textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		panel_2.add(scrollPane);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_7.getLayout();
		flowLayout_3.setAlignment(FlowLayout.RIGHT);
		panel_3.add(panel_7, BorderLayout.CENTER);

		findLabel = new JLabel("찾기:");
		findLabel.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_find.png"))));
		panel_7.add(findLabel);
		findLabel.setEnabled(false);

		findTextField = new JTextField();
		panel_7.add(findTextField);
		findTextField.setText("");
		findTextField.setColumns(20);
		findTextField.setEnabled(false);

		upButton = new JButton("위로");
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int idx = textArea.getCaretPosition();
				KafkaConsumerViewer.this.searchLog("UP", idx);
			}
		});
		upButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_white_get.png"))));
		panel_7.add(upButton);
		upButton.setEnabled(false); 

		downButton = new JButton("아래로");
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = textArea.getCaretPosition();
				KafkaConsumerViewer.this.searchLog("DOWN", idx);
			}
		});
		downButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_white_put.png"))));
		panel_7.add(downButton);
		downButton.setEnabled(false);

		JPanel panel_8 = new JPanel();
		panel_3.add(panel_8, BorderLayout.EAST);


		//		tglbtnScrollLock = new JToggleButton("Log 출력 - OFF");
		tglbtnScrollLock = new JToggleButton("Log 출력 - Suspend");
		tglbtnScrollLock.setForeground(Color.BLUE);
		tglbtnScrollLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/control_pause_blue.png"))));

		panel_8.add(tglbtnScrollLock);

		JButton btnClear = new JButton("Clear");
		btnClear.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/image_delete.png"))));

		panel_8.add(btnClear);

		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4, BorderLayout.WEST);
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.RIGHT);

		JLabel lblNewLabel_2 = new JLabel("Count:");
		panel_4.add(lblNewLabel_2);

		countField = new JTextField();
		panel_4.add(countField);
		countField.setText("0");
		countField.setEditable(false);
		countField.setColumns(20);

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_12 = new JPanel();
		panel_1.add(panel_12, BorderLayout.SOUTH);

		CardLayout cardLayout = new CardLayout();
		panel_12.setLayout(cardLayout);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_12.add(panel_6, "show");
		panel_6.setPreferredSize(new Dimension(panel_12.getWidth(), 0));
//		panel_6.setPreferredSize(new Dimension(panel_12.getWidth(), 30));
		panel_6.setLayout(new BorderLayout(0, 0));

		JPanel panel_16 = new JPanel();
		panel_6.add(panel_16, BorderLayout.WEST);
		panel_16.setLayout(new BorderLayout(0, 0));

		JLabel lblSendMessage = new JLabel("Target partition:");
		panel_16.add(lblSendMessage, BorderLayout.WEST);

		partitionField = new JTextField();
		panel_16.add(partitionField);
		partitionField.setText("0");
		partitionField.setColumns(3);

		JPanel panel_17 = new JPanel();
		panel_6.add(panel_17, BorderLayout.CENTER);
		panel_17.setLayout(new BorderLayout(0, 0));

		JPanel panel_18 = new JPanel();
		panel_17.add(panel_18, BorderLayout.CENTER);
				panel_18.setLayout(new BorderLayout(0, 0));
		
				JLabel lblNewLabel_3 = new JLabel("     Message:");
				panel_18.add(lblNewLabel_3, BorderLayout.WEST);

		messageField = new JTextField();
		panel_18.add(messageField);
		messageField.setColumns(66);
		
		JLabel lblNewLabel_4 = new JLabel(" ");
		panel_18.add(lblNewLabel_4, BorderLayout.EAST);
		
		JPanel panel_19 = new JPanel();
		panel_17.add(panel_19, BorderLayout.EAST);
				panel_19.setLayout(new BorderLayout(5, 5));
		
				sendButton = new JButton("전 송");
				sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel_19.add(sendButton);
				sendButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/control_play_blue.png"))));
				sendButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						KafkaConsumerViewer.this.sendMessage();
					}
				});
		messageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					KafkaConsumerViewer.this.sendMessage();
				}
			}
		});

		JPanel panel_14 = new JPanel();
		panel_14.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.add(panel_14, BorderLayout.CENTER);
		panel_14.setLayout(new BorderLayout(0, 0));

		JPanel panel_11 = new JPanel();
		panel_14.add(panel_11, BorderLayout.CENTER);
		panel_11.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel_1 = new JLabel("Topic: " + topic);
		panel_11.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 12));

		JPanel panel_9 = new JPanel();
		panel_14.add(panel_9, BorderLayout.EAST);
		FlowLayout flowLayout = (FlowLayout) panel_9.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);

		JPanel panel_5 = new JPanel();
		panel_9.add(panel_5);

		JCheckBox chckbxNewCheckBox = new JCheckBox("Show entire message");
		panel_5.add(chckbxNewCheckBox);

		JPanel panel_15 = new JPanel();
		panel_15.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_9.add(panel_15);

		JLabel lblNewLabel = new JLabel("Buffer(로그 출력 라인수):");
		panel_15.add(lblNewLabel);

		bufferField = new JTextField();
		panel_15.add(bufferField);
		bufferField.setColumns(5);
		bufferField.setText("100");
		chckbxNewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isShowEntireMessage = chckbxNewCheckBox.isSelected();
				if(isShowEntireMessage) {
					KafkaConsumerViewer.this.logWriteFlag = false;
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					textArea.setLineWrap(true);
					textArea.setText("");
					bufferField.setText("50");
					KafkaConsumerViewer.this.logWriteFlag = true;
				} else {
					KafkaConsumerViewer.this.logWriteFlag = false;
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					textArea.setLineWrap(false);
					textArea.setText("");
					bufferField.setText("100");
					KafkaConsumerViewer.this.logWriteFlag = true;
				}
			}
		});

		JPanel panel_10 = new JPanel();
		panel_14.add(panel_10, BorderLayout.WEST);
		panel_10.setLayout(new BorderLayout(0, 0));

		JPanel panel_13 = new JPanel();
		panel_10.add(panel_13, BorderLayout.CENTER);
		panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));


		JLabel labelButton = new JLabel(); 
		panel_13.add(labelButton);
		labelButton.setPreferredSize(new Dimension(18, 18));
		labelButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/plus.png"))));
		labelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				cardLayout.next(panel_12);

				if(toggleFlag) {
					panel_6.setPreferredSize(new Dimension(panel_12.getWidth(), 0));
					labelButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/plus.png"))));

					toggleFlag = false;
				} else {
					panel_6.setPreferredSize(new Dimension(panel_12.getWidth(), 30));
					labelButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/minus.png"))));
					toggleFlag = true;
				}
			}
		});


		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");

				messageCount = 0l;
				countField.setText("0");
			}
		});

		tglbtnScrollLock.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if( tglbtnScrollLock.isSelected()) {
					KafkaConsumerViewer.this.logWriteFlag = false;

					tglbtnScrollLock.setText("Log 출력 - ON");
					tglbtnScrollLock.setForeground(Color.RED);
					tglbtnScrollLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/control_play_blue.png"))));

					findLabel.setEnabled(true);
					findTextField.setEnabled(true);
					upButton.setEnabled(true); 
					downButton.setEnabled(true); 

				} else {
					KafkaConsumerViewer.this.logWriteFlag = true;

					tglbtnScrollLock.setText("Log 출력 - OFF");
					tglbtnScrollLock.setForeground(Color.BLUE);
					tglbtnScrollLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/control_pause_blue.png"))));

					findLabel.setEnabled(false);
					findTextField.setEnabled(false);
					upButton.setEnabled(false); 
					downButton.setEnabled(false);
				}

			}
		});

		//this.setVisible(true);
	}

	protected void searchLog(String direction, int idx) {
		String findText = findTextField.getText();

		if("UP".equals(direction)) {
			do{
				String text = textArea.getText().substring(0, (idx - findText.length()) );
				idx = text.lastIndexOf( findText, idx+1 );

				if( idx != -1 ){   
					textArea.select( idx, (idx+findText.length()) );

					//textArea 로 포커스 이동을 위해
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_Q);
					robot.keyRelease(KeyEvent.VK_ALT);
					robot.keyRelease(KeyEvent.VK_Q);

					break;
				} else {
					JOptionPane.showMessageDialog(this, "찾으시는 문자열이 존재하지 않습니다.");
				}
			}while( idx != -1 );

		} else {
			do{
				idx = textArea.getText().indexOf( findText, idx+1 );

				if( idx != -1 ){   
					textArea.select( idx, (idx+findText.length()) );

					//textArea 로 포커스 이동을 위해
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_Q);
					robot.keyRelease(KeyEvent.VK_ALT);
					robot.keyRelease(KeyEvent.VK_Q);

					break;
				} else {
					JOptionPane.showMessageDialog(this, "찾으시는 문자열이 존재하지 않습니다.");
				}
			}while( idx != -1 );
		}
	}

	public void startConsumer(String bootstrapServers, String topic) {
		this.bootstrapServers = bootstrapServers;
		this.topic = topic;

		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrapServers);
		String localIp = null;
		try {
			localIp = InetAddress.getLocalHost().getHostAddress();
			localIp = localIp.replaceAll("[.]", "");
		} catch (UnknownHostException e) {
			localIp = "000000000000";
			e.printStackTrace();
		}
		String groupId = "tool-" + localIp;

		props.put("group.id", groupId);
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		//props.put("auto.offset.reset", "earliest");
		//props.setProperty("auto.offset.reset", "latest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topic));
		textArea.append("["+topic+"] Consumer Starting...\n");

		//		List<PartitionInfo> partitionInfoList = consumer.partitionsFor(topic);
		//		List<TopicPartition> partitionInfos = new ArrayList<>();
		//		for (PartitionInfo partitionInfo : partitionInfoList) {
		//			TopicPartition topicPartition = new TopicPartition(topic, partitionInfo.partition());
		//			partitionInfos.add(topicPartition);
		//		}
		//		consumer.seekToEnd(partitionInfos);

		new Thread(() -> {
			String printFormat = "Partition: [%d], Offset: [%d], Timestamp: [%s], Message: [%s]\n";

			String value =  null;
			//			int length = 0;
			//			boolean flag = true;
			while (startFalg) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

				// 지정한 timestamp 이후 모든 recode 가져오기(처음 matching 된 recode 이후 모든 recode를 가져옴)
				//				if(flag) {
				//					long timestamp = System.currentTimeMillis();
				//					
				//					for (PartitionInfo partitionInfo : partitionInfoList) {
				//				        Map<TopicPartition, Long> query = new HashMap<>();
				//				        query.put(new TopicPartition(topic, partitionInfo.partition()), timestamp);
				//	
				//				        Map<TopicPartition, OffsetAndTimestamp> result = consumer.offsetsForTimes(query);
				//				        result.entrySet().stream().forEach(entry -> consumer.seek(entry.getKey(), entry.getValue().offset()));
				//					}
				//			        flag = false;
				//			    }

				//				if(flag) {
				//					List<TopicPartition> partitionInfos = new ArrayList<>();
				//					for (final ConsumerRecord<String, String> record : records) {
				//						TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
				//						partitionInfos.add(topicPartition);
				//					}
				//					consumer.seekToEnd(partitionInfos);
				//					flag = false;
				//					
				//					continue;
				//				}

				if(logWriteFlag) {
					for (ConsumerRecord<String, String> record : records) {
						value = record.value();

						if(isShowEntireMessage) {
							KafkaConsumerViewer.this.updateTextArea(value +"\n");
						} else {
							if(value.length() > 100) {
								KafkaConsumerViewer.this.updateTextArea(String.format(printFormat, record.partition(),  record.offset(), DateUtil.format(record.timestamp(), "yyyy-MM-dd HH:mm:ss.SSS"), (value.substring(0, 100) + "...")));
							} else {
								KafkaConsumerViewer.this.updateTextArea(String.format(printFormat, record.partition(),  record.offset(), DateUtil.format(record.timestamp(), "yyyy-MM-dd HH:mm:ss.SSS"), value));
							}
						}

						messageCount ++;
						countField.setText(String.valueOf(messageCount));
					}
				}
			}
		}).start();
	}

	private void exitKafkaConsumerViewer() {
		this.startFalg = false;

		if(consumer != null) {
			try {
				Thread.sleep(500);

				consumer.unsubscribe();
				consumer.close();
				consumer = null;
			} catch (Exception e) {
				e.printStackTrace(); //무시
			}
		}

		KafkaConsumerViewer.this.setVisible(false);
		KafkaConsumerViewer.this.dispose();
	}

	private void updateTextArea(String text) {
		strMaxLength = bufferField.getText();
		maxLength = Integer.parseInt(strMaxLength);

		textArea.append(text);
		try {
			//버퍼이상 라인 지우기
			if (textArea.getLineCount() > maxLength) {
				textArea.replaceRange("", textArea.getLineStartOffset(0), textArea.getLineEndOffset(textArea.getLineCount() - maxLength - 1));
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		textArea.setCaretPosition((textArea.getText()).length());
	}

	private void createProducer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", this.bootstrapServers);
		
//		props.put("acks", "1");
//		props.put("retries", 1);
//		props.put("batch.size", 16384);
//		props.put("linger.ms", 0);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("linger.ms", 1);
		
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		//props.put("buffer.memory", 52428800); // 50M

		this.producer = new KafkaProducer<>(props);
	}

	private void sendMessage() {
		int partition = 0;
		String partitionStr = partitionField.getText();
		try {
			partition = Integer.parseInt(partitionStr);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Target partition은 integer 값을 넣으세요.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);

			partitionField.setText("0");
			return;
		}

		String message = messageField.getText();
		if(StringUtils.isEmpty(message.trim())) {
			JOptionPane.showMessageDialog(this,
					"전송할 Message를 입력하세요.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if(message.length() > 300) {
			JOptionPane.showMessageDialog(this,
					"전송할 Message의 max length는 300입니다.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		partitionField.setEnabled(false);
		messageField.setEnabled(false);
		sendButton.setEnabled(false);

		try {
			if(this.producer == null) {
				this.createProducer();
			}
			//producer.send(new ProducerRecord<String, String>(this.topic, partition, "key", message));
			producer.send(new ProducerRecord<String, String>(this.topic, partition, "key", message));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			producer.close();
			producer = null;
		}

		partitionField.setEnabled(true);
		messageField.setEnabled(true);
		sendButton.setEnabled(true);
	}
}
