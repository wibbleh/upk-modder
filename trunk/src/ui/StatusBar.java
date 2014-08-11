package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.nio.file.Path;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;

import ui.dialogs.LogDialog;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Status bar panel displaying the associated UPK file, a progress bar and
 * single logging messages.
 * @author XMS
 */
@SuppressWarnings("serial")
public class StatusBar extends JPanel {
	
	/**
	 * The text field component displaying the UPK file associated with the
	 * currently active mod file tab.
	 */
	private JTextField upkTtf;

	/**
	 * The progress bar indicating progress of various tasks.
	 */
	private JProgressBar progressBar;
	
	/**
	 * The text field component displaying the current status message.
	 */
	private JTextField statusMsgTtf;
	
	/**
	 * The message log dialog.
	 */
	private LogDialog loggingDlg;

	/**
	 * Creates a status bar.
	 */
	public StatusBar(Frame owner) {
		super();
		
		this.initComponents(owner);
	}
	
	/**
	 * Creates and configures the status bar components.
	 * @param owner 
	 */
	private void initComponents(Frame owner) {
		this.setLayout(new FormLayout("0px:g(0.4), 0px:g(0.2), 0px:g(0.4)", "f:p"));

		Color bgCol = new Color(214, 217, 223);
		
		JPanel upkPnl = new JPanel(new FormLayout("0px:g, 3px,  r:p", "b:p"));
		
		upkTtf = new JTextField("no modfile loaded");
		upkTtf.setEditable(false);
		upkTtf.setBackground(bgCol);
		
		final JButton upkBtn = new JButton(ActionCache.getAction("associateUpk"));
		upkBtn.setBorder(null);
		
		// create lighter and darker versions of upk icon
		Icon defaultIcon = upkBtn.getIcon();
		
		BufferedImage normalImg = new BufferedImage(
				defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = normalImg.createGraphics();
		defaultIcon.paintIcon(null, g, 0, 0);
		g.dispose();
		Icon rolloverIcon = new ImageIcon(new RescaleOp(
				new float[] { 1.1f, 1.1f, 1.1f, 1.0f }, new float[4], null).filter(normalImg, null));
		Icon pressedIcon = new ImageIcon(new RescaleOp(
				new float[] { 0.8f, 0.8f, 0.8f, 1.0f }, new float[4], null).filter(normalImg, null));

		upkBtn.setIcon(defaultIcon);
		upkBtn.setRolloverIcon(rolloverIcon);
		upkBtn.setPressedIcon(pressedIcon);
		upkBtn.setBorder(null);
		upkBtn.setEnabled(false);
		
		// exchange borders
		upkPnl.setBorder(upkTtf.getBorder());
		upkTtf.setBorder(null);
		
		upkPnl.add(upkTtf, CC.xy(1, 1));
		upkPnl.add(upkBtn, CC.xy(3, 1));
		
		// TODO: implement progress monitoring hooks into various processes
		UIManager.getDefaults().put("nimbusOrange",
				UIManager.getDefaults().get("nimbusFocus"));
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);

		// create status message panel
		JPanel statusMsgPnl = new JPanel(new FormLayout("0px:g, 3px, r:p", "b:p"));
		
		statusMsgTtf = new JTextField();
		statusMsgTtf.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		statusMsgTtf.setEditable(false);
		statusMsgTtf.setBackground(bgCol);
		
		// create message log dialog
		loggingDlg = new LogDialog(owner);
		
		// create button to show log dialog
		JButton loggingBtn = new JButton(ActionCache.getAction("showLog"));
		loggingBtn.setBorder(null);
		
		// create lighter and darker versions of logging icon
		defaultIcon = loggingBtn.getIcon();
		
		normalImg = new BufferedImage(
				defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		g = normalImg.createGraphics();
		defaultIcon.paintIcon(null, g, 0, 0);
		g.dispose();
		rolloverIcon = new ImageIcon(new RescaleOp(
				new float[] { 1.1f, 1.1f, 1.1f, 1.0f }, new float[4], null).filter(normalImg, null));
		pressedIcon = new ImageIcon(new RescaleOp(
				new float[] { 0.8f, 0.8f, 0.8f, 1.0f }, new float[4], null).filter(normalImg, null));
		
		loggingBtn.setRolloverIcon(rolloverIcon);
		loggingBtn.setPressedIcon(pressedIcon);
		
		// exchange borders
		statusMsgPnl.setBorder(statusMsgTtf.getBorder());
		statusMsgTtf.setBorder(null);
		
		statusMsgPnl.add(statusMsgTtf, CC.xy(1, 1));
		statusMsgPnl.add(loggingBtn, CC.xy(3, 1));
				
		this.add(upkPnl, CC.xy(1, 1));
		this.add(progressBar, CC.xy(2, 1));
		this.add(statusMsgPnl, CC.xy(3, 1));
	}

	/**
	 * Makes the UPK status text field display the provided UPK file path.
	 * @param upkPath the path to display
	 */
	public void setUpkPath(Path upkPath) {
		if (ActionCache.getAction("associateUpk").isEnabled()) {
			upkTtf.setText((upkPath == null) 
					? "no UPK file selected"
					: upkPath.toString());
		} else {
			upkTtf.setText("no modfile loaded");
		}
	}

	/**
	 * Shows the message log dialog.
	 */
	public void showLogDialog() {
		loggingDlg.setVisible(true);
	}

	/**
	 * Sets the status message text.
	 * @param text the status message to set
	 */
	public void setStatusMessage(String text) {
		statusMsgTtf.setText(text);
	}

	/**
	 * Sets the progress bar's value to the specified progress value.
	 * @param progress the progress to display, between <code>0</code> and <code>100</code>
	 */
	public void setProgress(int progress) {
		progressBar.setValue(progress);
	}
	
}
