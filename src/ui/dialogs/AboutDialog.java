package ui.dialogs;

import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ui.Constants;
import ui.MainFrame;

/**
 * Dialog implementation for the application's <i>About</i> dialog.
 * @author XMS
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	
	/**
	 * The singleton instance of the <i>About</i> dialog.
	 */
	private static AboutDialog instance;
	
	/**
	 * Constructs the <i>About</i> dialog.
	 */
	private AboutDialog() {
		super(MainFrame.getInstance(), "About UPK Modder", true);
		
		this.initComponents();
		
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(MainFrame.getInstance());
	}

	/**
	 * Returns the singleton instance of the <i>About</i> dialog.
	 * @return the <i>About</i> dialog
	 */
	public static AboutDialog getInstance() {
		if (instance == null) {
			instance = new AboutDialog();
		}
		return instance;
	}
	
	/**
	 * Creates and lays out the dialog contents.
	 */
	private void initComponents() {
		// TODO: prettify dialog... like, really, it's fugly
		this.setIconImage(((ImageIcon) Constants.HEX_SMALL_ICON).getImage());
		
		Container contentPane = this.getContentPane();
		
		JLabel aboutLbl = new JLabel(Constants.ABOUT_TEXT, Constants.HEX_LARGE_ICON, SwingConstants.LEFT);
		aboutLbl.setVerticalAlignment(SwingConstants.TOP);
		aboutLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		contentPane.add(aboutLbl);
	}

}
