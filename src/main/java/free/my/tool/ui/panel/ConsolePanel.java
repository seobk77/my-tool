package free.my.tool.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.BadLocationException;

import free.my.tool.ui.MainFrame;

public class ConsolePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int MAX_LINES = 3000;
	
	private JTextArea consoleArea;
	private JLabel propertyLabel;
	
	/**
	 * Create the panel.
	 */
	public ConsolePanel() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblConsole = new JLabel(" Console");
		lblConsole.setFont(new Font("굴림", Font.BOLD, 12));
		panel.add(lblConsole, BorderLayout.WEST);
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setFont(new Font("굴림", Font.PLAIN, 13));
		consoleArea.setBackground(SystemColor.BLACK);
		consoleArea.setForeground(SystemColor.WHITE);
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(true);
		scrollPane.setViewportView(consoleArea);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JButton clearButton = new JButton("Clear");
		clearButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/img/image_delete.png"))));
		panel_1.add(clearButton, BorderLayout.EAST);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Setting Properties File: ");
		lblNewLabel.setFont(new Font("굴림", Font.BOLD, 12));
		panel_2.add(lblNewLabel, BorderLayout.WEST);
		
		propertyLabel = new JLabel("NONE");
		propertyLabel.setFont(new Font("굴림", Font.BOLD, 12));
		propertyLabel.setForeground(new Color(255, 0, 0));
		panel_2.add(propertyLabel);
		clearButton.addActionListener(e -> consoleArea.setText(""));
		
		//this.redirectSystemStreams();
	}
	
	public void settingPropertiesName(String name) {
		propertyLabel.setText(name);
	}
	
	//private void redirectSystemStreams() {
	public void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
	
	private void updateTextArea(String text) {
		consoleArea.append(text);

		try {
			//버퍼이상 라인 지우기
			if (consoleArea.getLineCount() > MAX_LINES) {
				consoleArea.replaceRange("", consoleArea.getLineStartOffset(0), consoleArea.getLineEndOffset(consoleArea.getLineCount() - MAX_LINES - 1));
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		consoleArea.setCaretPosition((consoleArea.getText()).length());
    }

	public void clear() {
		consoleArea.setText("");
	}
}
