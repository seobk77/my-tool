package free.my.tool.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Observable;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

@SuppressWarnings("deprecation")
public class SshManager extends Observable {
	private Session session = null;
	private Channel shellChannel = null; 
	private OutputStream out = null;
	
	public SshManager() {
	}

	public boolean connect(String host, int port, String id, String password, String privateKeyPath) {
		boolean flag = false;

		try {
			JSch jsch = new JSch();
			
            if(StringUtils.isNotEmpty(privateKeyPath)) {
                jsch.addIdentity(privateKeyPath);
            } 
            
            session = jsch.getSession(id, host, port);
            
            if(StringUtils.isEmpty(privateKeyPath)) {
                session.setPassword(password);
            }
			
			UserInfo ui = new MyUserInfo() {
				public boolean promptYesNo(String message) {
					return true;
				}
			};
			session.setUserInfo(ui);

			//session.connect();
			session.connect(30000); // making a connection with timeout.

			this.connectShell();
			
			flag = true;
		} catch (Exception e) {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}

			flag = false;
		}
		
		return flag;
	}

	public void disconnect() {
		if(shellChannel != null && shellChannel.isConnected()) {
			shellChannel.disconnect();
		}
		if(session != null && session.isConnected()) {
			session.disconnect();
		}
	}

	private void connectShell() {
		try{
			shellChannel = session.openChannel("shell");

			out = shellChannel.getOutputStream();

			InputStream in = shellChannel.getInputStream();
			ReadThread readThread = new ReadThread(in);
			readThread.start();
			
			//shellChannel.connect();
			shellChannel.connect(30000);
		} catch(JSchException | IOException e){
			e.printStackTrace();
		}
	}
	
	class ReadThread extends Thread {
		private BufferedReader buf = null;

		public ReadThread(InputStream in) {
			buf = new BufferedReader(new InputStreamReader(in));
		}

		public void run() {
			String str = "";
			while(str != null) {
				try {
					str = buf.readLine();
					SshManager.this.commandResult(str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			SshManager.this.commandResult("Disconnected!!\n");

			if(shellChannel.isConnected()) {
				shellChannel.disconnect();
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
	
	private void commandResult(String result) {
		setChanged();
        notifyObservers(result);
	}
	
	public void shellExec(String command) {
		try {
			Thread.sleep(500);
			
			out.write((command+"\n").getBytes());
			out.flush();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static abstract class MyUserInfo implements UserInfo {
		public String getPassword() {
			return null;
		}

		public boolean promptYesNo(String str) {
			return false;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return false;
		}

		public boolean promptPassword(String message) {
			return false;
		}

		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}
	}
	
	public String exec(String command) {
		StringBuffer sb = new StringBuffer();

		Channel channel = null;
		try {
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			channel.setInputStream(null);

			//channel.setOutputStream(System.out);

			//FileOutputStream fos = new FileOutputStream("/tmp/stderr");
			//((ChannelExec)channel).setErrStream(fos);
			//((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					sb.append(new String(tmp, 0, i));
				}
				
				if (channel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					//sb.append("exit-status: " + channel.getExitStatus());
					break;
				}
				
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
		} catch (JSchException | IOException e) {
			e.getStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}

		return sb.toString();
	}
}
