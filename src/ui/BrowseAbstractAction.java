package ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import ui.Constants.ExtensionFileFilter;

/**
 * Convenience implementation of the AbstractAction class for selecting
 * a file and doing an operation on it.
 * 
 * @author XMS
 */
@SuppressWarnings("serial")
public abstract class BrowseAbstractAction extends AbstractAction {

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
	 * @param name
	 * @param parent the parent component
	 * @param filter the file filter to use
	 */
	public BrowseAbstractAction(String name, Component parent, FileFilter filter) {
		this(name, parent, filter, false);
	}

	/**
	 * Constructs a browse action listener for showing either an open or save
	 * file selection dialog atop the specified parent component using the
	 * specified file filter.
	 * 
	 * @param name
	 * @param parent the parent component
	 * @param filter the file filter to use
	 * @param save <code>true</code> if a save dialog shall be shown,
	 *  <code>false</code> if an open dialog shall be shown
	 */
	public BrowseAbstractAction(String name, Component parent, FileFilter filter, boolean save) {
		super(name);
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
				// added condition to make directory filter work for creating new projects
				if(this.filter instanceof ExtensionFileFilter) {
					selectedFile = new File(selectedFile.getPath() + ((ExtensionFileFilter) this.filter).getExtension());
					if (!this.filter.accept(selectedFile)) {
						throw new IllegalArgumentException("Unusable filename was provided: " + selectedFile.getName());
					}
				}
			}
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
		if (lastSelectedFile == null) {
			// TODO: check whether this works as intended >_>
			lastSelectedFile = new File(".");
		}
		return lastSelectedFile;
	}

}
