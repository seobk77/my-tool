package free.my.tool.manager;

import java.util.Observable;

import javax.swing.JOptionPane;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;

@SuppressWarnings("deprecation")
public class SftpManager extends Observable {
	private Session session = null;
	private Channel channel = null; 
	
	public SftpManager() {
	}

	public boolean connect(String host, int port, String id, String password) {
		boolean flag = false;

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(id, host, port);
			session.setPassword(password);
			UserInfo ui = new MyUserInfo() {
				public boolean promptYesNo(String message) {
					return true;
				}
			};
			session.setUserInfo(ui);

			session.connect();
			flag = true;
		} catch (Exception e) {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}

			flag = false;
		}
		
		return flag;
	}
	
	public void upload(String localFilePath, String remoteFilePath) {
		try {
			channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp) channel;
			
//			InputStream in = System.in;
//			channelSftp.setInputStream(null);
//			PrintStream out = System.out;
//			channelSftp.setOutputStream(null);
			
			channelSftp.put(localFilePath, remoteFilePath, new MyProgressMonitor(), ChannelSftp.OVERWRITE);
		} catch (JSchException | SftpException e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
	}
	
	public void remove(String remoteFilePath) {
		try {
			channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp channelSftp = (ChannelSftp) channel;
			
//			InputStream in = System.in;
//			channelSftp.setInputStream(null);
//			PrintStream out = System.out;
//			channelSftp.setOutputStream(null);
			
			channelSftp.rm(remoteFilePath);
			sendResult("Remove Success~");
		} catch (JSchException | SftpException e) {
			e.printStackTrace();
		} finally {
			this.disconnect();
		}
	}
	
	public void disconnect() {
		if(channel != null && channel.isConnected()) {
			channel.disconnect();
		}
		if(session != null && session.isConnected()) {
			session.disconnect();
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
	
	public class MyProgressMonitor implements SftpProgressMonitor {
		private long count = 0;
		private long max = 0;
		private long percent = -1;

		@Override
		public void init(int op, String src, String dest, long max) {
			this.max = max;
			count = 0;
			percent = -1;
			
			//System.out.println("Upload Start~");
			sendResult("Upload Start~");
		}

		@Override
		public boolean count(long count) {
			this.count += count;

			if (percent >= this.count * 100 / max) {
				return true;
			}
			percent = this.count * 100 / max;
			
			//System.out.println("Completed " + this.count + "(" + percent + "%) byte out of " + max + " byte.");
			sendResult("Completed " + this.count + "(" + percent + "%) byte out of " + max + " byte.");

			return true;
		}

		@Override
		public void end() {
			//System.out.println("Upload Success~");
			sendResult("Upload Success~");
		}
	}
	
	private void sendResult(String message) {
		setChanged();
        notifyObservers(message);
	}
}
