package ui;

import java.io.File;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;

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
	public static final String VERSION_NUMBER = "v0.1";
	
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

	/**
	 * File filter for *.upk files.
	 */
	public static final FileFilter UPK_FILE_FILTER = new FileFilter() {
		@Override
		public String getDescription() {
			return "Unreal Engine Package File (*.upk)";
		}
		@Override
		public boolean accept(File f) {
			return (f.isDirectory() || f.getName().endsWith(".upk"));
		}
	};
	
}
