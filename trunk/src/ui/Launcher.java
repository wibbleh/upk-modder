package ui;

import java.awt.Insets;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ui.frames.MainFrame;

/**
 * Launcher class for graphical user interface.
 * 
 * @author XMS
 */
public class Launcher {

	/**
	 * Main entry point for graphical user interface.
	 * @param args commandline arguments (none supported so far)
	 */
	public static void main(String[] args) {
		// configure LAF and locale
		configureLookAndFeel();
		// init and show main frame instance
		MainFrame.getInstance().setVisible(true);
		
//		new PrototypeEditorFrame().setVisible(true);
	}

	/**
	 * Configures the Look and Feel and locale of the graphical user interface.
	 */
	private static void configureLookAndFeel() {
		// set locale to English
		Locale.setDefault(Locale.ENGLISH);
		// try setting Nimbus LAF
		try {
			// iterate installed LAFs, look for Nimbus LAF to install
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
			// enlarge toolbar button margins
			UIManager.put("ToolBar:Button.contentMargins", new Insets(6, 6, 6, 6));
		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
	}
	
}
