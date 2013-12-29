package ui;

import java.awt.Font;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.OutputKeys;

/**
 * Container class for UI-related constants and helper methods.
 * 
 * @author XMS
 */
public class Constants {

	/**
	 * The application's name.
	 */
	public static final String APPLICATION_NAME = "UPK Modder";
	
	/**
	 * The application's version number.
	 */
	public static final String VERSION_NUMBER = "v0.71";
	
	/**
	 * The list of authors.
	 */
	public static final String[] AUTHORS = {
		"Amineri",
		"XMarksTheSpot"
	};

	/**
	 * The application's 'About' text.
	 */
	public static final String ABOUT_TEXT = "<html>" + APPLICATION_NAME + "<br><br>Version: " +
			VERSION_NUMBER + "<br><br>" +
			"Authors: " + Arrays.toString(AUTHORS) + "<br><br>" +
			"Released under the <a href=\"http://www.gnu.org/licenses/gpl.html\">GNU GPL v3</a> license<br>" +
			"Source code is available under <a href=\"https://code.google.com/p/upk-modder/\"https://code.google.com/p/upk-modder/</a>";

	/** <img src="../ui/resources/icons/hex16.png"/> */
	public static final Icon HEX_SMALL_ICON = new ImageIcon(Constants.class.getResource("/ui/resources/icons/hex16.png"));
	/** <img src="../ui/resources/icons/hex32.png"/> */
	public static final Icon HEX_LARGE_ICON = new ImageIcon(Constants.class.getResource("/ui/resources/icons/hex32.png"));
	
	// TODO: Make the font sizes, etc user-configurable
	/**
	 * Text Pane Tab fonts
	 */
	public static final Font TAB_PANE_FONT_UNKNOWN = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	public static final Font TAB_PANE_FONT_APPLIED = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	public static final Font TAB_PANE_FONT_REVERTED = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	public static final Font TAB_PANE_FONT_ERROR = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

	/**
	 * Text Pane font 
	 */
	public static final Font TEXT_PANE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	/**
	 * Tree Pane font 
	 */
	public static final Font TREE_PANE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	/**
	 * Status Message font 
	 */
	public static final Font STATUS_MSG_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	/**
	 * Logger Frame font 
	 */
	public static final Font LOGGER_FRAME_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

	/**
	 * Project Name font 
	 */
	public static final Font PROJECT_NAME_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	
	/**
	 * Project Entry font 
	 */
	public static final Font PROJECT_ENTRY_FONT = new Font(Font.DIALOG, Font.PLAIN, 11);
	
	/**
	 * Reference Update font 
	 */
	public static final Font REFERENCE_UPDATE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	/**
	 * Target UPK Panel font 
	 */
	public static final Font TARGET_UPK_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

	/**
	 * Tab size 
	 */
	public static final int TAB_SIZE = 24;
	
	/**
	 * The application state file.
	 */
	public static final Path APPLICATION_STATE_FILE = Paths.get("UPKModder.conf");

	/**
	 * The operand data file.
	 */
	public static final Path OPERAND_DATA_FILE = Paths.get("config/operand_data.ini");
	
	/**
	 * The default mod file template file.
	 */
	public static final File TEMPLATE_PROJECT_FILE = new File("defaultProjectTemplate.xml");
	
	/**
	 * The default mod file template file.
	 */
	public static final Path TEMPLATE_MOD_FILE = Paths.get("defaultModfileTemplate.upk_mod");
	
	/**
	 * The output properties for writing project XML files.
	 */
	public static final Properties PROJECT_XML_OUTPUT_PROPERTIES;
	static {
		Properties props = new Properties();
		props.setProperty(OutputKeys.INDENT, "yes");
		props.setProperty(OutputKeys.METHOD, "xml");
		props.setProperty(OutputKeys.ENCODING, "UTF-8");
		props.setProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		PROJECT_XML_OUTPUT_PROPERTIES = props;
	}
	
	/**
	 * File filter for *.mod files.
	 */
	public static final FileFilter MOD_FILE_FILTER =
			new ExtensionFileFilter(".upk_mod", "UPKModder Mod file (*.upk_mod)");

	/**
	 * File filter for *.upk files.
	 */
	public static final FileFilter UPK_FILE_FILTER =
			new ExtensionFileFilter(".upk", "Unreal Engine Package File (*.upk)");

	/**
	 * File filter for *.xml files.
	 */
	public static final FileFilter XML_FILE_FILTER =
			new ExtensionFileFilter(".xml", "Extensible Markup Language File (*.xml)");
	

	/**
	 * Convenience implementation of FileFilter for filtering files with specific file extensions.
	 * @author XMS
	 */
	public static class ExtensionFileFilter extends FileFilter implements java.io.FileFilter {
		
		/**
		 * The extension to filter upon.
		 */
		private String extension;
		
		/**
		 * The description to display.
		 */
		private String description;
		
		public ExtensionFileFilter(String extension, String description) {
			if (extension.isEmpty()) {
				throw new IllegalArgumentException("Extension must not be empty.");
			}
			if (!extension.startsWith(".")) {
				extension = "." + extension;
			}
			this.extension = extension;
			this.description = description;
		}
		
		/**
		 * Returns the extension.
		 * @return the extension
		 */
		public String getExtension() {
			return this.extension;
		}
		
		@Override
		public String getDescription() {
			return this.description;
		}
		
		@Override
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().endsWith(this.extension));
		}
		
	}
	
}
