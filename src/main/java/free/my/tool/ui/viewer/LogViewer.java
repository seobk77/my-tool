package free.my.tool.ui.viewer;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
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
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import free.my.tool.ui.MainFrame;

public class LogViewer extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel glassPane = null;
	private JPanel contentPane = null;
	private JTextArea textArea = null;
	public JTextField bufferField = null;

	private Session session = null;
	private Channel channel = null;
	private OutputStream out = null;

	public boolean logWriteFlag = true;
	public boolean loggingFlag = false;

	private JLabel findLabel = null;
	private JTextField findTextField = null;
	private JButton upButton = null;
	private JButton downButton = null;

	private JToggleButton tglbtnScrollLock = null; 

	private Robot robot = null;

	private boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);
	}
	
	public LogViewer(GraphicsConfiguration gc, String title) {
		try {
			robot = new Robot();
		} catch (AWTException e3) {
			//무시
		}

		//this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				LogViewer.this.exitLogViewer();
			}
		});
		
		this.setTitle("Kafka Consumer");
		if(isWindows()) {
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/super_man.png")));
		}
		
		//this.setSize(857, 700);
		Rectangle bounds = gc.getBounds();
		this.setBounds(bounds.x, bounds.y, 991, 700);
		//this.setResizable(false);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		glassPane = (JPanel) this.getGlassPane();
		this.settingGlassPane();

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
		textArea.setLineWrap(true);
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
				LogViewer.this.searchLog("UP", idx);
			}
		});
		upButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/page_white_get.png"))));
		panel_7.add(upButton);
		upButton.setEnabled(false); 

		downButton = new JButton("아래로");
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int idx = textArea.getCaretPosition();
				LogViewer.this.searchLog("DOWN", idx);
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

		JPanel panel_9 = new JPanel();
		panel_3.add(panel_9, BorderLayout.WEST);
		FlowLayout flowLayout = (FlowLayout) panel_9.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_9.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		JLabel lblNewLabel = new JLabel("Buffer(로그 출력 라인수):");
		panel_9.add(lblNewLabel);

		bufferField = new JTextField();
		panel_9.add(bufferField);
		bufferField.setColumns(5);
		bufferField.setText("100");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout_1 = (FlowLayout) panel_1.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel.add(panel_1, BorderLayout.NORTH);
		
		JLabel lblNewLabel_1 = new JLabel(title);
		lblNewLabel_1.setFont(new Font("굴림", Font.BOLD, 12));
		panel_1.add(lblNewLabel_1);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});

		tglbtnScrollLock.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {

				if( tglbtnScrollLock.isSelected()) {
					LogViewer.this.logWriteFlag = false;

					tglbtnScrollLock.setText("Log 출력 - ON");
					tglbtnScrollLock.setForeground(Color.RED);
					tglbtnScrollLock.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/control_play_blue.png"))));

					findLabel.setEnabled(true);
					findTextField.setEnabled(true);
					upButton.setEnabled(true); 
					downButton.setEnabled(true); 

				} else {
					LogViewer.this.logWriteFlag = true;

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
		
		this.setVisible(true);
	}
	
	private void settingGlassPane() {
		glassPane.setLayout(null);
		glassPane.setOpaque(false);
		//glassPane.setBackground(new Color(0f, 0f, 0f, 0.7f));
		
		glassPane.addMouseListener(new MouseAdapter() {});
		glassPane.addMouseMotionListener(new MouseMotionAdapter() {});
		glassPane.addKeyListener(new KeyAdapter() {});

		Image image = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/loading.gif"));
		//Image scaledImage = image.getScaledInstance(300, 300, Image.SCALE_DEFAULT);
		Icon imgIcon = new ImageIcon(image);
		JLabel imageLabel = new JLabel(imgIcon);
		
		Dimension frameSize = this.getSize();
		int x1 = (frameSize.width - 40) / 2;
		int y1 = (frameSize.height - 40) / 2 - 40;
		imageLabel.setBounds(x1, y1, 40, 40);
		glassPane.add(imageLabel);
	}
	
	public void showProgressBar() {
		glassPane.setVisible(true);
	}
	
	public void closeProgressBar() {
		glassPane.setVisible(false);
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

	public void init(String host, String port, String userId, String password, String command) {
		this.showProgressBar();
		
		this.connect(host, Integer.parseInt(port), userId, password); //서버 접속
		loggingFlag = true;
		
		try {
			Thread.sleep(1000);
			out.write((command+"\n").getBytes());
			out.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		this.closeProgressBar();
	}
	
	private void connect(String host, int port, String userId, String password) {
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(userId, host, port);
			session.setPassword(password);

			UserInfo ui = new MyUserInfo(){
				public boolean promptYesNo(String message){
					return  true;
				}
			};
			session.setUserInfo(ui);

			//session.connect();
			session.connect(30000);   // making a connection with timeout.

			channel = session.openChannel("shell");

			InputStream in = channel.getInputStream();
			ReadThread read = new ReadThread(in);
			read.start();

			out = channel.getOutputStream();

			//channel.connect();
			channel.connect(30000);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private void exitLogViewer() {
		this.showProgressBar();
		
		try {
			if(loggingFlag) {
				byte ASCII_CTRL_C = 3;
				byte[] ctrlc = { ASCII_CTRL_C };
				out.write( ctrlc );
				out.flush();

				Thread.sleep(500);
				out.write("exit\n".getBytes());
				out.flush();

				Thread.sleep(500);
				if(channel.isConnected()) {
					channel.disconnect();
				}
				if(session.isConnected()) {
					session.disconnect();
				}
			}

			Thread.sleep(300);
			
			LogViewer.this.setVisible(false);
			LogViewer.this.dispose();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}
	
	class ReadThread extends Thread {
		BufferedReader buf = null;
		String str = null;

		public ReadThread(InputStream in) {
			buf = new BufferedReader(new InputStreamReader(in));
		}

		public void run() {
			String strMaxLength = null;
			int maxLength = 100;
			str = "";
			while(str != null) {
				strMaxLength = bufferField.getText();
				maxLength = Integer.parseInt(strMaxLength);

				try {
					str = buf.readLine();

					if(LogViewer.this.logWriteFlag) {
						textArea.append(str + "\n");

						textArea.setCaretPosition((textArea.getText()).length());
					}

					//버퍼이상 라인 지우기
					if (textArea.getLineCount() > maxLength) {
						textArea.replaceRange("", textArea.getLineStartOffset(0), textArea.getLineEndOffset(textArea.getLineCount() - maxLength - 1));
					}

				} catch (IOException | BadLocationException e) {
					e.printStackTrace();
				}
			}

			textArea.append("Disconnected~~~\n");
			textArea.setCaretPosition((textArea.getText()).length());

			if(channel.isConnected()) {
				channel.disconnect();
			}
			if(session.isConnected()) {
				session.disconnect();
			}

			try {
				buf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static abstract class MyUserInfo implements UserInfo {
		public String getPassword(){ return null; }
		public boolean promptYesNo(String str){ return false; }
		public String getPassphrase(){ return null; }
		public boolean promptPassphrase(String message){ return false; }
		public boolean promptPassword(String message){ return false; }
		public void showMessage(String message){ }
	}
}
