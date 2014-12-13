package ui.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InsetsUIResource;

import model.modtree.ModTree;
import model.upk.UpkFile;
import ui.ActionCache;
import ui.ApplicationState;
import ui.ApplyStatus;
import ui.Constants;
import ui.ModFileTabbedPane;
import ui.ModFileTabbedPane.ModFileTab;
import ui.StatusBar;
import ui.dialogs.AboutDialog;
import ui.trees.ProjectTree;
import ui.trees.ProjectTreeModel.FileNode;
import ui.trees.ProjectTreeModel.ModFileNode;
import ui.trees.ProjectTreeModel.ProjectNode;
import util.unrealhex.HexSearchAndReplace;

/**
 * The application's primary frame.
 * 
 * @author XMS, Amineri 
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	/**
	 * The shared singleton instance of the application's main frame.
	 */
	private static MainFrame instance;

	/**
	 * The application state container object.
	 */
	private ApplicationState appState;


	private JScrollPane projectScpn;
	
	/**
	 * The project pane tree component.
	 */
	private ProjectTree projectTree;

	/**
	 * The mod file tabbed pane component.
	 */
	private ModFileTabbedPane modTabPane;

	/**
	 * The status bar component.
	 */
	private StatusBar statusBar;
	
	/**
	 * The cache of shared UPK files.
	 */
	private Map<Path, UpkFile> upkCache = new HashMap<>();
	
	/**
	 * Constructs the application's main frame.
	 * @param title the title string appearing in the frame's title bar
	 */
	private MainFrame(String title) {
		// instantiate frame
		super(title);
		
		// set icon images
		List<Image> images = new ArrayList<>();
		images.add(((ImageIcon) Constants.HEX_SMALL_ICON).getImage());
		images.add(((ImageIcon) Constants.HEX_LARGE_ICON).getImage());
		this.setIconImages(images);

//		// TODO: move initial/default configuration elsewhere
//		// TODO: add configuration dialogue?
//		if(UpkModderProperties.getConfigProperty("project.path") == null) {
//			UpkModderProperties.setConfigProperty("project.path", "UPKmodderProjects");
//		}
		
		// initialize action cache
		ActionCache.initActionCache(this);

		// TODO: add separate thread here to re-open projects and files
		appState = ApplicationState.readState();

		// create and lay out the frame's components
		this.initComponents();

		this.restoreApplicationState();
		
		// make closing the main frame terminate the application
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				MainFrame.this.closeApplication();
			}
		});
		
		// adjust frame size
		this.pack();
		this.setMinimumSize(new Dimension(500, 300));
		// center frame in screen
		this.setLocationRelativeTo(null);
		
		//used for timing tests on opening application
//		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	/**
	 * Returns the singleton instance of the application's main frame.
	 * @return the main frame instance
	 */
	public static MainFrame getInstance() {
		if (instance == null) {
			instance = new MainFrame(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + "");
		}
		return instance;
	}

	/**
	 * Creates and lays out the frame's components.
	 * @throws Exception if an I/O error occurs
	 */
	private void initComponents() {
		
		// create menu bar
		JMenuBar menuBar = this.createMenuBar();
		
		// create tool bar
		JToolBar toolBar = this.createToolBar();
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		// create central tabbed pane
		UIManager.put("TabbedPane:TabbedPaneTabArea.contentMargins", new InsetsUIResource(3, 0, 4, 0));
		UIManager.put("TabbedPane:TabbedPaneTab.contentMargins", new InsetsUIResource(2, 8, 3, 3));
		modTabPane = new ModFileTabbedPane();
		if(appState.getModFileDimension() == null) {
			modTabPane.setPreferredSize(new Dimension(500, 300));
		} else {
			modTabPane.setPreferredSize(appState.getModFileDimension());
		}
		// install listener on tabbed pane to capture selection changes
		modTabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				Component selComp = modTabPane.getSelectedComponent();
				Path upkPath = null;
				if (selComp != null) {
					ModFileTab modTab = (ModFileTab) selComp;

					// get UPK file reference from tab
					UpkFile upkFile = modTab.getUpkFile();
					if (upkFile != null) {
						upkPath = upkFile.getPath();
					}
					
					// enable/disable 'test', 'apply' and 'revert' actions
					MainFrame.this.setEditActionsEnabled(modTab.getApplyStatus());
					
					// enable UPK selection button
					ActionCache.getAction("associateUpk").setEnabled(true);
				} else {
					// last tab has been removed, reset to defaults
					MainFrame.this.setEditActionsEnabled(false);
					ActionCache.getAction("associateUpk").setEnabled(false);
				}
				// show file name in status bar (or missing file hint)
				statusBar.setUpkPath(upkPath);
			}
		});
                try {
                    // create left-hand project pane
                    projectTree = new ProjectTree();
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
		//JScrollPane projectScpn = new JScrollPane(projectTree);
		projectScpn = new JScrollPane(projectTree);
		if(appState.getProjectPaneDimension() == null) {
			projectScpn.setPreferredSize(new Dimension(160, 300));
		} else {
			projectScpn.setPreferredSize(appState.getProjectPaneDimension());
		}
		// wrap project pane and tabbed pane in a split pane
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectScpn, modTabPane);
		
		// create status bar
		statusBar = new StatusBar(this);
		
		// add components to frame
		this.setJMenuBar(menuBar);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(mainPane, BorderLayout.CENTER);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		// FIXME
//		// TODO - move to separate method and invoke in background so application launches immediately
//		// open previously open projects
//		Set<String> projectPathSet = UpkModderProperties.getOpenProjects();
//		if(projectPathSet != null) {
//			if(!projectPathSet.isEmpty()) {
//				for (String filePath : projectPathSet) {
//					if(filePath != null) {
//						((BrowseAbstractAction) ActionCache.getAction("openProject")).execute(new File(filePath));
//					}
//				}
//				setFileActionsEnabled(true);
//			}
//		}
//
//		// open previously open files
//		Set<String> filePathSet = UpkModderProperties.getOpenFiles();
//		if(filePathSet != null) {
//			if(!filePathSet.isEmpty()) {
//				for (String filePath : filePathSet) {
//					File modFile = new File(filePath);
//					if(modFile.exists()) {
//						((BrowseAbstractAction) ActionCache.getAction("openModFile")).execute(modFile);
//					}
//				}
//				setFileActionsEnabled(true);
//			}
//		}
	}
	
	/**
	 * Creates and configures the menu bar.
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar() {
		// init menu bar
		JMenuBar menuBar = new JMenuBar();
	
		// create file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(ActionCache.getAction("newProject"));
		fileMenu.add(ActionCache.getAction("openProject"));
		fileMenu.add(ActionCache.getAction("removeProject"));
		fileMenu.add(ActionCache.getAction("deleteProject"));
		fileMenu.addSeparator();
		fileMenu.add(ActionCache.getAction("newModFile"));
		fileMenu.add(ActionCache.getAction("openModFile"));
		fileMenu.add(ActionCache.getAction("closeModFile"));
		fileMenu.add(ActionCache.getAction("closeAllModFiles"));
		fileMenu.addSeparator();
		fileMenu.add(ActionCache.getAction("saveModFile"));
		fileMenu.add(ActionCache.getAction("saveModFileAs"));
		fileMenu.addSeparator();
		fileMenu.add(ActionCache.getAction("export"));
		fileMenu.addSeparator();
		fileMenu.add(ActionCache.getAction("exit"));
		
		// create edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		editMenu.add(ActionCache.getAction("refUpdate"));
		editMenu.addSeparator();
		editMenu.add(ActionCache.getAction("hexApply"));
		editMenu.add(ActionCache.getAction("hexRevert"));
		editMenu.add(ActionCache.getAction("testFile"));
		
		// create help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
		helpMenu.add(ActionCache.getAction("help"));
		helpMenu.addSeparator();
		helpMenu.add(ActionCache.getAction("toggleLog"));
		helpMenu.addSeparator();
		helpMenu.add(ActionCache.getAction("about"));

		// add menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	/**
	 * Creates and configures the tool bar.
	 * @return the tool bar
	 */
	private JToolBar createToolBar() {
		
		JToolBar toolBar = new JToolBar();
		
		toolBar.add(ActionCache.getAction("newProject"));
		toolBar.add(ActionCache.getAction("openProject"));
		toolBar.addSeparator();
		toolBar.add(ActionCache.getAction("newModFile"));
		toolBar.add(ActionCache.getAction("openModFile"));
		toolBar.addSeparator();
		toolBar.add(ActionCache.getAction("saveModFile"));
		toolBar.addSeparator();
		toolBar.add(ActionCache.getAction("refUpdate"));
		toolBar.addSeparator();
		toolBar.add(ActionCache.getAction("testFile"));
		toolBar.add(ActionCache.getAction("hexApply"));
		toolBar.add(ActionCache.getAction("hexRevert"));
		
		Component glue = Box.createHorizontalGlue();
		glue.setFocusable(false);
		
		toolBar.add(glue);
		toolBar.add(ActionCache.getAction("toggleLog"));
		
		return toolBar;
	}
	
	protected void closeApplication() {
		// TODO: maybe implement 'Save All' action for tabs
		boolean save = false;
		for (int i = 0; i < modTabPane.getTabCount(); i++) {
			ModFileTab modTab = (ModFileTab) modTabPane.getComponentAt(i);
			if (modTab.isModified()) {
				if (!save) {
					int res = JOptionPane.showOptionDialog(this, "One or more tabs have unsaved changes, close anyway?",
							"Confirm Close", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
							null, new String[] { "Close Without Save", "Save And Close", "Cancel" }, "Cancel");
					if ((res == JOptionPane.CANCEL_OPTION) || (res == JOptionPane.CLOSED_OPTION)) {
						// 'Cancel'
						return;
					} else if (res == JOptionPane.NO_OPTION) {
						// 'Save And Close'
						save = true;
						// continue iterating tabs and save them
					} else {
						// 'Close Without Save'
						break;
					}
				}
				if (save) {
					// save before closing
					try {
						modTab.saveFile();
					} catch (IOException e) {
						ModFileTabbedPane.logger.log(
								Level.SEVERE, "Failed to save mod file \'" + modTab.getModFile() + "\'", e);
					}
				}
			}
		}

		//update various Pane sizes
		appState.setMainFrameDimension(this.getSize());
		appState.setModFileDimension(modTabPane.getSize());
		appState.setProjectPaneDimension(projectScpn.getSize());
		
		// store application state
		appState.storeState();
		
		// exit
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + title);
	}

	/**
	 * Sets the status bar's progress bar to the specified progress value.
	 * @param progress the progress, between <code>0</code> and <code>100</code>
	 */
	public void setProgress(int progress) {
		statusBar.setProgress(progress);
	}

	/**
	 * Returns the cache of UPK files.
	 * @return the UPK cache
	 */
	public Map<Path, UpkFile> getUpkCache() {
		return upkCache;
	}

	/**
	 * Convenience method to set the enable state of the 'Save', 'Save As',
	 * 'Close' and 'Close All' menu items.
	 * @param enabled the enable state
	 */
	public void setFileActionsEnabled(boolean enabled) {
		ActionCache.getAction("closeModFile").setEnabled(enabled);
		ActionCache.getAction("closeAllModFiles").setEnabled(enabled);
		ActionCache.getAction("saveModFile").setEnabled(enabled);
		ActionCache.getAction("saveModFileAs").setEnabled(enabled);
		ActionCache.getAction("refUpdate").setEnabled(enabled);
	}
	
	/**
	 * Convenience method to set the enable state of the 'Test File Status',
	 * 'Apply Hex Changes' and 'Revert Hex Changes' actions.
	 * @param enabled the enable state
	 */
	private void setEditActionsEnabled(boolean enabled) {
		ActionCache.getAction("testFile").setEnabled(enabled);
		ActionCache.getAction("hexApply").setEnabled(enabled);
		ActionCache.getAction("hexRevert").setEnabled(enabled);
	}
	
	/**
	 * Convenience method to set the enable state of the 'Test File Status',
	 * 'Apply Hex Changes' and 'Revert Hex Changes' actions according to the
	 * specified mod file apply state.
	 * @param status the apply state
	 */
	private void setEditActionsEnabled(ApplyStatus status) {
		ActionCache.getAction("testFile").setEnabled(true);
		ActionCache.getAction("hexApply").setEnabled(status == ApplyStatus.BEFORE_HEX_PRESENT);
		ActionCache.getAction("hexRevert").setEnabled(status == ApplyStatus.AFTER_HEX_PRESENT);
	}
	
	/**
	 * Shows the reference updating dialog for the currently selected mod file tab.
	 */
	public void showReferenceUpdateDialog() {
		modTabPane.showReferenceUpdateDialog();
	}

	/**
	 * Displays the application's <i>Help</i> dialog.
	 */
	public void showHelpDialog() {
		// TODO implement help dialog
	}

	/**
	 * Displays the application's <i>About</i> dialog.
	 */
	public void showAboutDialog() {
		AboutDialog.getInstance().setVisible(true);
	}

	/**
	 * Shows or hides the message log dialog.
	 */
	public void toggleLogDialog() {
		statusBar.toggleLogDialog();
	}

	/**
	 * Creates a new project to be placed inside the specified project directory.
	 * @param projectPath the project directory
	 */
	public void createNewProject(Path projectPath) {
		Path xmlPath = projectTree.createProject(projectPath);
		this.openProject(xmlPath);
	}

	/**
	 * Opens a project defined in the specified project XML file.
	 * @param xmlPath the project XML
	 */
	public void openProject(Path xmlPath) {
		if (Files.exists(xmlPath)) {
			projectTree.openProject(xmlPath);

			// store opened file
			appState.addProjectFile(xmlPath);
		}
	}

	/**
	 * Removes the currently active project from the project pane.
	 */
	public void removeProject() {
		this.removeProject(null);
	}
	
	/**
	 * Removes the specified project from the project pane.
	 * @param projNode the project to remove
	 */
	public void removeProject(ProjectNode projNode) {
		Path xmlPath = projectTree.removeProject(projNode);
		if (xmlPath != null) {
			appState.removeProjectFile(xmlPath);
			// reset title and some menu items
			this.setTitle("");
			ActionCache.getAction("removeProject").setEnabled(false);
			ActionCache.getAction("deleteProject").setEnabled(false);
		}
	}
	
	/**
	 * Removes the currently active project from the project pane and deletes
	 * all associated files.
	 */
	public void deleteProject() {
		// ask for confirmation
		int res = JOptionPane.showConfirmDialog(this,
				"<html>Are you sure you want to delete this project?<br>" +
				"This operation is irreversible.</html>",
				"Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (res == JOptionPane.OK_OPTION) {
			projectTree.deleteProject();
			ActionCache.getAction("removeProject").setEnabled(false);
			ActionCache.getAction("deleteProject").setEnabled(false);
		}
	}
	
	/**
	 * Updates the main frame's title to display the currently selected
	 * project's name.
	 */
	public void updateTitle() {
		ProjectNode activeProject = projectTree.getActiveProject();
		if (activeProject != null) {
			// extract file from project
			Path projectFile = activeProject.getProjectFile();
			// update frame title
			this.setTitle(" : " + projectFile.getFileName());
			// enable some menu items
			ActionCache.getAction("removeProject").setEnabled(true);
			ActionCache.getAction("deleteProject").setEnabled(true);
			ActionCache.getAction("newModFile").setEnabled(true);
		} else {
			// no project selected, reset some properties
			this.setTitle("");
			ActionCache.getAction("removeProject").setEnabled(false);
			ActionCache.getAction("deleteProject").setEnabled(false);
			ActionCache.getAction("newModFile").setEnabled(false);
		}
	}
	
	/**
	 * Creates a new mod file containing a default template file inside the
	 * currently active project.
	 */
	public void createNewModFile() {
		this.createNewModFile(projectTree.getActiveDirectory());
	}
	
	/**
	 * Creates a new mod file containing a default template file inside the
	 * specified directory node.
	 * @param dirNode the directory node of the project pane tree under which a mod file shall be created
	 */
	public void createNewModFile(FileNode dirNode) {
		if (dirNode != null) {
			// prompt for mod file name
			String res = JOptionPane.showInputDialog(this, "Enter Name of New Mod File",
					"New Mod File", JOptionPane.INFORMATION_MESSAGE);
			// check whether user aborted or entered invalid name
			// TODO: verify enhanced error checking to prevent invalid file names
			if ((res != null) && !res.isEmpty() && this.isValidName(res)) {
				// create a new mod file node
				// TODO: should always succeed if error checking is in place
				if (!res.toLowerCase().endsWith(".upk_mod")) {
					res += ".upk_mod";   // append file extension if user did not type it
				}
				projectTree.createModFile(dirNode, res);
//				ModFileNode node = projectTree.createModFile(dirNode, res);
//				if (node != null) {
//					// create a new mod file tab
//					this.openModFile(node.getFilePath(), node);
//				} else {
//					// node creation failed, show error message
//					JOptionPane.showMessageDialog(this,
//							"Failed to create mod file, see message log for details.",
//							"Error", JOptionPane.ERROR_MESSAGE);
//					// TODO: perform clean-up?
//				}
			}
		} else {
			// we shouldn't get here as the corresponding action(s) should be disabled
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Deletes a specified mod file 
	 * @param fileNode the fileNode associated with the file to be removed
	 * @throws IOException if an I/O error occurs
	 */
	public void deleteModFile(FileNode fileNode) throws IOException {
		Path filePath = fileNode.getFilePath();
		if ((fileNode instanceof ModFileNode) && Files.exists(filePath)) {
			boolean isApplied = fileNode.getStatus() != ApplyStatus.BEFORE_HEX_PRESENT;
			String prompt = (isApplied) ? "The file you're about to delete may be applied. Delete anyway?"
					: "Do you really want to delete this file?";
			String[] options = new String[] { "Delete", "Cancel" };
			int res = JOptionPane.showOptionDialog(this, prompt, "Confirm Delete",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
					null, options, options[1]);

			// check whether user aborted 
			if (res == JOptionPane.OK_OPTION) {
				// delete the new mod file node
				Files.delete(filePath);
			}
		} else {
			// we shouldn't get here as the corresponding action(s) should be disabled
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Renames the file associated with the specified file node.
	 * @param fileNode the file node
	 * @throws IOException if an I/O error occurs
	 */
	public void renameFile(FileNode fileNode) throws IOException {
		if (fileNode != null) {
			Path filePath = fileNode.getFilePath();
			// @Amineri surely renaming directories should be an option, too, maybe drop the file check?
			if (Files.isRegularFile(filePath)) {
				String res = JOptionPane.showInputDialog(this,
						"Enter New Name of File", "Rename",
						JOptionPane.INFORMATION_MESSAGE);
				if ((res != null) && !res.isEmpty() && this.isValidName(res)) {
					// rename file
					Files.move(filePath, filePath.resolveSibling(res));
				}
			}
		} else {
			// we shouldn't get here as the corresponding action(s) should be disabled
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Creates a new directory below the one associated with the specified file
	 * node.
	 * @param dirNode the file node pointing to the parent directory
	 * @throws IOException if an I/O error occurs
	 */
	public void createNewFolder(FileNode dirNode) throws IOException {
		if (dirNode != null) {
			Path parentPath = dirNode.getFilePath();
			if (Files.isDirectory(parentPath)) {
				// prompt for mod file name
				String res = JOptionPane.showInputDialog(this, "Enter Name of New Folder",
						"New Folder", JOptionPane.INFORMATION_MESSAGE);
				// check whether user aborted or entered invalid name
				// TODO: verify enhanced error checking to prevent invalid file names
				if ((res != null) && !res.isEmpty() && this.isValidName(res)) {
					// create a new folder node
					// TODO: should always succeed if error checking is in place
//					FileNode node = projectTree.createModFile(dirNode, res);

					Path childPath = parentPath.resolve(res);
					Files.createDirectories(childPath);
				}
			}
		} else {
			// we shouldn't get here as the corresponding action(s) should be disabled
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Deletes the directory and all its contents rooted at the specified file node.
	 * @param fileNode the file node corresponding to the directory to be deleted
	 */
	public void deleteFolder(FileNode fileNode) throws IOException {
		if (fileNode != null) {
			Path filePath = fileNode.getFilePath();
			if (Files.isDirectory(filePath)) {
				boolean isApplied = fileNode.getStatus() != ApplyStatus.BEFORE_HEX_PRESENT;
				boolean isEmpty = true;
				try (DirectoryStream<Path> ds = Files.newDirectoryStream(filePath)) {
					isEmpty = !ds.iterator().hasNext();
				}
				String prompt = (isApplied) ? "The directory you're about to delete may have applied files. Delete anyway?"
						: (!isEmpty) ? "The directory you're about to delete is not empty. Delete anyway?"
								: "Do you really want to delete this directory?";
				String[] options = new String[] { "Delete", "Cancel" };
				int res = JOptionPane.showOptionDialog(this, prompt, "Confirm Delete",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[1]);

				// check whether user aborted
				if (res == JOptionPane.OK_OPTION) {
					// recursively delete directory and its contents
					Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
						@Override public FileVisitResult postVisitDirectory(
								Path dir, IOException exc) throws IOException {
							Files.delete(dir);
							return super.postVisitDirectory(dir, exc);
						}
						@Override public FileVisitResult visitFile(Path file,
								BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return super.visitFile(file, attrs);
						}
					});
				}
			}
		} else {
			// we shouldn't get here as the corresponding action(s) should be disabled
			throw new IllegalArgumentException();
		}
	}

	// TODO : move to utility function after validation -- we don't seem to have UI-related utilities yet...
	// code from http://stackoverflow.com/questions/6730009/validate-a-file-name-on-windows
	// TODO: @Amineri, that looks like a simpler (and platform-independent) way: http://stackoverflow.com/questions/893977/java-how-to-find-out-whether-a-file-name-is-valid
	// TODO: @XMTS, my thinking is to employ more restrictive naming so that file names will be more cross platform compatible
	/**
	 * Auxiliary method to check whether the provided string is a valid file
	 * name.
	 * @param text the string to check
	 * @return <code>true</code> if the string is a valid file name,
	 *  <code>false</code> otherwise
	 */
	private boolean isValidName(String text) {
		Pattern pattern = Pattern.compile(
			"# Match a valid Windows filename (unspecified file system).          \n" +
			"^                                # Anchor to start of string.        \n" +
			"(?!                              # Assert filename is not: CON, PRN, \n" +
			"  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" +
			"    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" +
			"    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" +
			"  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" +
			"  (?:\\.[^.]*)?                  # followed by optional extension    \n" +
			"  $                              # and end of string                 \n" +
			")                                # End negative lookahead assertion. \n" +
			"[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" +
			"[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" +
			"$                                # Anchor to end of string.            ", 
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
		Matcher matcher = pattern.matcher(text);
		boolean isMatch = matcher.matches();
		return isMatch;
	}
	
	/**
	 * Creates a new mod file tab containing the specified mod file contents.
	 * @param modPath the path to the mod file to open
	 */
	public void openModFile(Path modPath) {
		this.openModFile(modPath, null);
	}
	
	/**
	 * Creates a new mod file tab containing the specified mod file contents and
	 * associates it with the specified mod file node of the project tree.
	 * @param modPath the path to the mod file to open
	 * @param modNode the mod file node of the project tree
	 */
	public void openModFile(Path modPath, ModFileNode modNode) {
		ModFileTab newTab = modTabPane.openModFile(modPath, modNode);
		if (newTab != null) {
			this.setFileActionsEnabled(true);
			
			// check whether tab is associated with a project
			if (modNode != null) {
				ProjectNode project = modNode.getProject();
				if (project != null) {
					// get targeted generic UPK file name
					String upkName = newTab.getModTree().getUpkName();
					// look up path in project's UPK file associations
					Path upkPath = project.getUpkPath(upkName);
					if (upkPath != null) {
						// associate tab with mapped UPK file
						this.associateUpk(upkPath);
					}
				}
				this.setEditActionsEnabled(newTab.getApplyStatus());
			}
		} else {
			// tab creation failed, show error message
			JOptionPane.showMessageDialog(this,
					"Failed to create mod file tab, see message log for details.",
					"Error", JOptionPane.ERROR_MESSAGE);
			// TODO: perform clean-up?
		}
	}
	

	/**
	 * Closes the currently opened mod file tab.
	 */
	public void closeModFile() {
		modTabPane.closeModFile();
		
		// if last tab has been removed disable 'save' and 'close' actions
		if (modTabPane.getTabCount() == 0) {
			this.setFileActionsEnabled(false);
		}
	}

	/**
	 * Closes all mod file tabs.
	 */
	public void closeAllModFiles() {
		modTabPane.closeAllModFiles();
		
		this.setFileActionsEnabled(false);
	}

	/**
	 * Returns the mod file of the currently active mod file tab.
	 * @return the active mod file
	 */
	public Path getActiveModFile() {
		return modTabPane.getActiveModFile();
	}

	/**
	 * Saves the contents of the currently selected mod file tab to its associated file.
	 */
	public void saveModFile() {
		modTabPane.saveModFile();
	}

	/**
	 * Saves the contents of the currently selected mod file tab to the specified target file.
	 * @param targetPath the target file to save to
	 */
	public void saveModFileAs(Path targetPath) {
		modTabPane.saveModFileAs(targetPath);
	}

	/**
	 * Applies the currently selected mod file tab's hex changes.
	 */
	public void applyModFile() {
		ApplyStatus status = modTabPane.applyModFile();
		this.setEditActionsEnabled(status);
	}

	/**
	 * Reverts the currently selected mod file tab's hex changes.
	 */
	public void revertModFile() {
		ApplyStatus status = modTabPane.revertModFile();
		this.setEditActionsEnabled(status);
	}

	/**
	 * Tests whether the current active pane's modfile's apply/revert status
	 */
	public void testModFileStatus() {
		ApplyStatus status = modTabPane.testModFileStatus();
		this.setEditActionsEnabled(status);
	}

	/**
	 * Tests whether a path-specified (possibly not opened) modfile is applied
	 * to its target upk.
	 * @param modNode the mod file node to test
	 */
	public void testModFileStatus(ModFileNode modNode) {
		if (modNode == null) {
			return;
		}
		Path modFilePath = modNode.getFilePath();
		if (modFilePath == null) {
			return;
		}

		// test if the modfile is in an opened pane
		if (modTabPane.getTab(modFilePath) != null) {
			ApplyStatus status = modTabPane.testModFileStatus(modFilePath);
			this.setEditActionsEnabled(status);
		} else {
			// if not opened, create a temporary ModTree for testing,
			// this modTree is not hooked up to a document and so cannot be
			// updated,
			// create parsed ModTree directly from supplied path
			ModTree modTree = null;
			try {
				modTree = new ModTree(new String(Files.readAllBytes(modFilePath)), true);
			} catch (IOException ex) {
				Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			if(modTree != null) {
				// find upk for tree, if possible
				ProjectNode project = modNode.getProject();
				if (project != null) {
					// get targeted generic UPK file name
					String upkName = modTree.getUpkName();
					// look up path in project's UPK file associations
					Path upkPath = project.getUpkPath(upkName);
					if (upkPath != null) {
						UpkFile upkFile = this.getUpkFile(upkPath);
						//set the target upk in the ModTree
						modTree.setTargetUpk(upkFile);
					}
				}

				// perform the test on the file
				modNode.setStatus(HexSearchAndReplace.testFileStatus(modTree));
			}
		}
	}
	
	/**
	 * Attempts to bulk apply as called from the project tree
	 */
	public void bulkApplyRevertModFile(ModFileNode modNode, boolean apply) {
		if (modNode == null) {
			return;
		}
		Path modFilePath = modNode.getFilePath();
		if (modFilePath == null) {
			return;
		}
		
		// test if the mod file is in an opened pane
		if (modTabPane.getTab(modFilePath) != null) {
			ApplyStatus status;
			modTabPane.openModFile(modFilePath, modNode);
			if (apply) {
				status = modTabPane.applyModFile();
			} else {
				status = modTabPane.revertModFile();
			}
			this.setEditActionsEnabled(status);
		} else {
			// if not opened, create a temporary ModTree for application,
			// this modTree is not hooked up to a document and so cannot be
			// updated, create parsed ModTree directly from supplied path
			ModTree modTree = null;
			try {
				modTree = new ModTree(new String(Files.readAllBytes(modFilePath)), true);
			} catch (IOException ex) {
				Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
			}

			if (modTree != null) {
				// find UPK for tree, if possible
				ProjectNode project = modNode.getProject();
				if (project != null) {
					// get targeted generic UPK file name
					String upkName = modTree.getUpkName();
					// look up path in project's UPK file associations
					Path upkPath = project.getUpkPath(upkName);
					if (upkPath != null) {
						UpkFile upkFile = this.getUpkFile(upkPath);
						//set the target upk in the ModTree
						modTree.setTargetUpk(upkFile);
					}
				}

				if (apply) {
					// attempt to apply
					if (HexSearchAndReplace.applyRevertChanges(true, modTree)) {
						modNode.setStatus(ApplyStatus.AFTER_HEX_PRESENT);
					}
				} else {
					// attempt to revert
					if (HexSearchAndReplace.applyRevertChanges(false, modTree)) {
						modNode.setStatus(ApplyStatus.BEFORE_HEX_PRESENT);
					}
				}
			}
		}		
	}
	
	
	/**
	 * Associates the specified UPK file with the currently active mod file tab.
	 * @param upkPath the path to the UPK file to associate
	 */
	public void associateUpk(Path upkPath) {
		UpkFile upkFile = this.getUpkFile(upkPath);
		if (modTabPane.associateUpk(upkFile)) {
			statusBar.setUpkPath(upkPath);
		}
	}

	/**
	 * Convenience method to get a UPK file from the local cache or, if no
	 * matching reference is present, to store a new reference in the cache.
	 * @param upkPath the path to the UPK file
	 * @return the UpkFile instance
	 */
	private UpkFile getUpkFile(Path upkPath) {
		UpkFile upkFile = upkCache.get(upkPath);
		if (upkFile == null) {
			upkFile = new UpkFile(upkPath);
			upkCache.put(upkPath, upkFile);
		}
		return upkFile;
	}
	
	/**
	 * Sets the status bar's status message text.
	 * @param text the status message to display
	 */
	public void setStatusMessage(String text) {
		statusBar.setStatusMessage(text);
	}
	
	/**
	 * Restores the application state from a deserialized state object.
	 */
	private void restoreApplicationState() {
		new RestoreWorker().execute();
	}

	/**
	 * Worker implementation to restore a serialized application state in a
	 * background thread.
	 * @author XMS
	 */
	private class RestoreWorker extends SwingWorker<Object, Object> {

		@Override
		protected Object doInBackground() throws Exception {
			// TODO: make frame appear busy
			Collection<String> openedProjects = appState.getOpenedProjectFiles();
			for (String xmlPath : openedProjects) {
				openProject(Paths.get(xmlPath));
			}
			
			return null;
		}
		
		@Override
		protected void done() {
			// TODO: do some clean up, e.g. stop appearing busy
		}
		
	}

}
