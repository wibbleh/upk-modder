package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import model.upk.UpkFile;
import ui.ModFileTabbedPane.ModFileTab;
import ui.dialogs.AboutDialog;
import ui.tree.ProjectTree;
import ui.tree.ProjectTreeModel.FileNode;
import ui.tree.ProjectTreeModel.ModFileNode;
import ui.tree.ProjectTreeModel.ProjectNode;

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

		// create and lay out the frame's components
		this.initComponents();
		
		// TODO: add separate thread here to re-open projects and files
		appState = ApplicationState.readState();
		this.restoreApplicationState();
		
		// make closing the main frame terminate the application
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				appState.storeState();
			}
		});
		
		// adjust frame size
		this.pack();
		this.setMinimumSize(this.getSize());
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
		modTabPane.setPreferredSize(new Dimension(1000, 600));
		
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
					
					// enable/disable 'update', 'apply' and 'revert' actions
					setEditActionsEnabled((upkFile != null));
					
					// enable UPK selection button
					ActionCache.getAction("associateUpk").setEnabled(true);
				} else {
					// last tab has been removed, reset to defaults
					setEditActionsEnabled(false);
					ActionCache.getAction("associateUpk").setEnabled(false);
				}
				// show file name in status bar (or missing file hint)
				statusBar.setUpkPath(upkPath);
			}
		});

		// create left-hand project pane
		projectTree = new ProjectTree();
		JScrollPane projectScpn = new JScrollPane(projectTree);
		projectScpn.setPreferredSize(new Dimension(320, 600));

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
		toolBar.add(ActionCache.getAction("hexApply"));
		toolBar.add(ActionCache.getAction("hexRevert"));
		
		return toolBar;
	}

//	public void setTargetUpk(ModFileTab tab, Path filePath) {
//
//			// grab UPK file from cache
//			UpkFile upkFile = upkCache.get(filePath);
//			if (upkFile == null) {
//				// if cache doesn't contain UPK file instantiate a new one
//				upkFile = new UpkFile(filePath);
//			}
//
//			// TODO: create function for upk association
//			// check whether UPK file is valid (i.e. header parsing worked properly)
//			if (upkFile.getHeader() != null) {
//				// store UPK file in cache
//				upkCache.put(filePath, upkFile);
//				// link UPK file to tab
//				tab.setUpkFile(upkFile);
//				// show file name in status bar
//				upkTtf.setText(filePath.toString());
//				// enable 'update', 'apply' and 'revert' actions
//				setEditActionsEnabled(true);
//
//				if(tab.getModFile() != null) {
//					// persistently store file-to-upk association
//					// FIXME
//					UpkModderProperties.setUpkProperty(tab.getModFile().getName(), filePath.toAbsolutePath().toString());
//				}
//			} else {
//				// TODO: show error/warning message
//			}
//	}
	

	
	@Override
	public void setTitle(String title) {
		super.setTitle(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + title);
	}

	@Override
		public void dispose() {
			super.dispose();
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
	 * Convenience method to set the enable state of the 'Update
	 * References...', 'Apply Hex Changes' and 'Revert Hex Changes' menu
	 * items.
	 * @param enabled the enable state
	 */
	public void setEditActionsEnabled(boolean enabled) {
		ActionCache.getAction("hexApply").setEnabled(enabled);
		ActionCache.getAction("hexRevert").setEnabled(enabled);
		ActionCache.getAction("testFile").setEnabled(enabled);
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
	 * Shows the message log dialog.
	 */
	public void showLogDialog() {
		statusBar.showLogDialog();
	}

	/**
	 * Creates a new project to be placed inside the specified project directory.
	 * @param projectPath the project directory
	 */
	public void createNewProject(Path projectPath) {
		projectTree.createProject(projectPath);
	}

	/**
	 * Opens a project defined in the specified project XML file.
	 * @param xmlPath the project XML
	 */
	public void openProject(Path xmlPath) {
		projectTree.openProject(xmlPath);
		
		// store opened file
		appState.addProjectFile(xmlPath);
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
			if ((res != null) && !res.isEmpty() && isValidName(res)) {
				// create a new mod file node
				// TODO: should always succeed if error checking is in place
				if(!res.toLowerCase().endsWith(".upk_mod")) {
					res += ".upk_mod";   // append file extention if user did not type it
				}
				ModFileNode node = projectTree.createModFile(dirNode, res);
				if (node != null) {
					// create a new mod file tab
					ModFileTab tab = this.openModFile(node.getFilePath());
					if (tab == null) {
						// tab creation failed, show error message
						JOptionPane.showMessageDialog(this,
								"Failed to create mod file tab, see message log for details.",
								"Error", JOptionPane.ERROR_MESSAGE);
						// TODO: perform clean-up?
					} else {
						// TODO: associate node with tab (or the other way round?)
					}
				} else {
					// node creation failed, show error message
					JOptionPane.showMessageDialog(this,
							"Failed to create mod file, see message log for details.",
							"Error", JOptionPane.ERROR_MESSAGE);
					// TODO: perform clean-up?
				}
			}
		} else {
			// we shouldn't get here as the corresponding action(s) should be disabled
			throw new IllegalArgumentException();
		}
	}

	// TODO : move to utility function after validation -- we don't seem to have UI-related utilities yet...
	// code from http://stackoverflow.com/questions/6730009/validate-a-file-name-on-windows
	public static boolean isValidName(String text)
	{
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
	 * @param modPath the mod file to open
	 * @return the newly created mod file tab or <code>null</code> if an error occurred
	 */
	public ModFileTab openModFile(Path modPath) {
		ModFileTab newTab = modTabPane.openModFile(modPath);
		this.setFileActionsEnabled(newTab != null);
		if (newTab != null) {
			this.setFileActionsEnabled(true);
			return newTab;
			// TODO: create function for upk re-association
			// re-associate upk if possible
			// FIXME
//			String uFileName = UpkModderProperties.getUpkProperty(modPath.getName());
//			if (uFileName != null) {
//				File uFile = new File(uFileName);
//				ModFileTab tab = modTabPane.getTab(modPath);
//				setTargetUpk(tab, uFile.toPath());
//			}
			// TODO: associate mod file with active project
		}
		return null;
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
		modTabPane.applyModFile();
	}

	/**
	 * Reverts the currently selected mod file tab's hex changes.
	 */
	public void revertModFile() {
		modTabPane.revertModFile();
	}

	/**
	 * Tests whether a modfile is applied or not
	 */
	public void testStatusModFile() {
		modTabPane.testStatusModFile();
	}

	/**
	 * Associates the specified UPK file with the currently active mod file tab.
	 * @param upkPath the path to the UPK file to associate
	 */
	public void associateUpk(Path upkPath) {
		UpkFile upkFile = upkCache.get(upkPath);
		if (upkFile == null) {
			upkFile = new UpkFile(upkPath);
			upkCache.put(upkPath, upkFile);
		}
		if (modTabPane.associateUpk(upkFile)) {
			statusBar.setUpkPath(upkPath);
		}
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
