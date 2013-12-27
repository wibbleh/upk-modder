package ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import ui.Constants.ExtensionFileFilter;


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
	 * Flag denoting whether a save dialog shall be used when choosing a file.
	 */
	private boolean save;

	/**
	 * Constructs a browse action listener for showing a file selection dialog
	 * atop the specified parent component using the specified file filter. 
	 * @param parent the parent component
	 * @param filter the file filter to use
	 */
	public BrowseActionListener(Component parent, FileFilter filter) {
		this(parent, filter, false);
	}

	/**
	 * Constructs a browse action listener for showing either an open or save
	 * file selection dialog atop the specified parent component using the
	 * specified file filter.
	 * 
	 * @param parent the parent component
	 * @param filter the file filter to use
	 * @param save <code>true</code> if a save dialog shall be shown,
	 *  <code>false</code> if an open dialog shall be shown
	 */
	public BrowseActionListener(Component parent, FileFilter filter, boolean save) {
		this.parent = parent;
		this.filter = filter;
		this.save = save;
	}
	
	/**
	 * Returns the initial target file of the file chooser. Default
	 * implementation always returns <code>null</code>.
	 * <p>
	 * Sub-classes can override this method to provide a custom target to the
	 * file chooser.
	 * @return <code>null</code>
	 */
	public File getTarget() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		File target = this.getTarget();
		if (target != null) {
			lastSelectedFile = target;
		}
		// create and configure file chooser instance
		JFileChooser chooser = new JFileChooser(getLastSelectedFile());
		chooser.setFileFilter(this.filter);
		chooser.setMultiSelectionEnabled(false);
		chooser.setAcceptAllFileFilterUsed(false);
		// show file selection dialog
		int res = (this.save) ? chooser.showSaveDialog(this.parent) : chooser.showOpenDialog(this.parent);
		// TODO: implement prompting for confirmation in save dialog
		if (res == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			if (!this.filter.accept(selectedFile)) {
				selectedFile = new File(selectedFile.getPath() + ((ExtensionFileFilter) this.filter).getExtension());
				if (!this.filter.accept(selectedFile)) {
					throw new IllegalArgumentException("Unusable filename was provided: " + selectedFile.getName());
				}
			}
			// execute operation
			this.execute(selectedFile.toPath());
			// store reference to selected file
			lastSelectedFile = selectedFile;
		}
	}
	
	/**
	 * Executes upon successfully seleting a file. Subclasses need to
	 * implement this to perform operations on the selected file.
	 * @param filePath the file to perform operations on
	 */
	protected abstract void execute(Path filePath);
	
	/**
	 * Returns the reference to the last selected file.
	 * @return the last selected file
	 */
	public static File getLastSelectedFile() {
		if (lastSelectedFile == null) {
			// TODO: check whether this works as intended >_>
			lastSelectedFile = new File(".");
		}
		return lastSelectedFile;
	}
	
}
