package ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


/**
 * Convenience implementation of the action listener interface for selecting
 * a file and doing an operation on it.
 * 
 * @author XMS
 */
public abstract class BrowseActionListener implements ActionListener {

	/**
	 * The shared reference to the last selected file.
	 */
	private static File lastSelectedFile;
	
	/**
	 * The parent component of the file selection dialog.
	 */
	private Component parent;

	/**
	 * The file filter used in the file selection dialog.
	 */
	private FileFilter filter;
	
	/**
	 * Constructs a browse action listener showing its file selection dialog
	 * atop the specified parent component.
	 * 
	 * @param parent
	 */
	public BrowseActionListener(Component parent, FileFilter filter) {
		this.parent = parent;
		this.filter = filter;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		// create and configure file chooser instance
		JFileChooser chooser = new JFileChooser(getLastSelectedFile());
		chooser.setFileFilter(this.filter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		// show file selection dialog
		int res = chooser.showOpenDialog(this.parent);
		if (res == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			// execute operation
			this.execute(selectedFile);
			// store reference to selected file
			lastSelectedFile = selectedFile;
		}
	}
	
	/**
	 * Executes upon successfully seleting a file. Subclasses need to
	 * implement this to perform operations on the selected file.
	 * @param file the file to perform operations on
	 */
	protected abstract void execute(File file);
	
	/**
	 * Returns the reference to the last selected file.
	 * @return the last selected file
	 */
	public static File getLastSelectedFile() {
		return lastSelectedFile;
	}
	
}
