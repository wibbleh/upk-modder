package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import model.modtree.ModTree;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;

import ui.ModFileTabbedPane.ModFileTab;
import ui.dialogs.AboutDialog;
import ui.tree.ProjectTree;
import ui.tree.ProjectTreeModel;
import ui.tree.ProjectTreeModel.ProjectNode;
import util.unrealhex.ReferenceUpdate;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

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
	 * The mod file tabbed pane component.
	 */
	private ModFileTabbedPane modTabPane;

	/**
	 * The project pane tree component.
	 */
	private ProjectTree projectTree;
	
	/**
	 * The cache of shared UPK files.
	 */
	private Map<File, UpkFile> upkCache = new HashMap<>();
	
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

//		appProperties = new UpkModderProperties();
//		
//		// TODO: move initial/default configuration elsewhere
//		// TODO: add configuration dialogue?
//		if(appProperties.getConfigProperty("project.path") == null) {
//			appProperties.setConfigProperty("project.path", "UPKmodderProjects");
//		}
//		if(appProperties.getConfigProperty("project.template.file") == null) {
//			appProperties.setConfigProperty("project.template.file", "defaultProjectTemplate.xml");
//		}
//		if(appProperties.getConfigProperty("modfile.template.file") == null) {
//			appProperties.setConfigProperty("modfile.template.file", "defaultModfileTemplate.upk_mod");
//		}
		
		// initialize action cache
		ActionCache.initActionCache(this);

		// create and lay out the frame's components
		this.initComponents();
		
		// make closing the main frame terminate the application
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// adjust frame size
		this.pack();
		this.setMinimumSize(this.getSize());
		// center frame in screen
		this.setLocationRelativeTo(null);
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

		// create left-hand project pane
		projectTree = new ProjectTree();
		JScrollPane projectScpn = new JScrollPane(projectTree);
		projectScpn.setPreferredSize(new Dimension(320, 600));

		// wrap project pane and tabbed pane in a split pane
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectScpn, modTabPane);
		
		// create status bar
		JPanel statusBar = this.createStatusBar();
		
		// add components to frame
		this.setJMenuBar(menuBar);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(mainPane, BorderLayout.CENTER);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		// FIXME
//		// restore mappings from file to target upks
//		appProperties.restoreUpkState();
//		
//		// restore open projects/files from last time app was run
//		appProperties.restoreOpenState();
//		
//		List<String> projectPathList = appProperties.getOpenProjects();
//		if(projectPathList != null) {
//			if(!projectPathList.isEmpty()) {
//				for (String filePath : projectPathList) {
//					projectMdl.addProject(new File(filePath));
//				}
//				setFileActionsEnabled(true);
//			}
//		}
//
//		// open previously open files
//		List<String> filePathList = appProperties.getOpenFiles();
//		if(filePathList != null) {
//			if(!filePathList.isEmpty()) {
//				for (String filePath : filePathList) {
//					File file = new File(filePath);
//					if(file.exists()) {
//						//create new tab
//						ModTab tab = new ModTab(file);
//						tabPane.addTab(file.getName(), tab);
//						tabPane.setSelectedComponent(tab);
//						
//						// TODO: create function for upk re-association
//						//re-associate upk if possible
//						if(appProperties.getUpkProperty(file.getName()) != null) {
//							File ufile = new File(appProperties.getUpkProperty(file.getName()));
//							// grab UPK file from cache
//							UpkFile upkFile = upkCache.get(ufile);
//							if (upkFile == null) {
//								// if cache doesn't contain UPK file instantiate a new one
//								upkFile = new UpkFile(ufile);
//							}
//
//							// check whether UPK file is valid (i.e. header parsing worked properly)
//							if (upkFile.getHeader() != null) {
//								// store UPK file in cache
//								upkCache.put(ufile, upkFile);
//								// link UPK file to tab
//								tab.setUpkFile(upkFile);
//								// show file name in status bar
//								upkTtf.setText(ufile.getPath());
//								// enable 'update', 'apply' and 'revert' actions
//								setEditActionsEnabled(true);
//							} else {
//								// TODO: show error/warning message
//							}
//						}
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

	/**
	 * Creates and configures the status bar.
	 * @return the status bar
	 */
	private JPanel createStatusBar() {
		// TODO: maybe make status bar a JToolBar
		JPanel statusBar = new JPanel(new FormLayout("0px:g(0.4), 0px:g(0.2), 0px:g(0.4)", "f:p"));
		Color bgCol = new Color(214, 217, 223);
		
		JPanel upkPnl = new JPanel(new FormLayout("0px:g, 3px,  r:p", "b:p"));
		
		final JTextField upkTtf = new JTextField("no modfile loaded");
		upkTtf.setEditable(false);
		upkTtf.setBackground(bgCol);
		
		final JButton upkBtn = new JButton();
		Icon defaultIcon = UIManager.getIcon("FileView.directoryIcon");
		
		// create lighter and darker versions of icon
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
		
		upkBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				Component selComp = modTabPane.getSelectedComponent();
				if (selComp != null) {
					ModFileTab tab = (ModFileTab) selComp;

					// grab UPK file from cache
					UpkFile upkFile = upkCache.get(file);
					if (upkFile == null) {
						// if cache doesn't contain UPK file instantiate a new one
						upkFile = new UpkFile(file);
					}

					// TODO: create function for upk association
					// check whether UPK file is valid (i.e. header parsing worked properly)
					if (upkFile.getHeader() != null) {
						// store UPK file in cache
						upkCache.put(file, upkFile);
						// link UPK file to tab
						tab.setUpkFile(upkFile);
						// show file name in status bar
						upkTtf.setText(file.getPath());
						// enable 'update', 'apply' and 'revert' actions
						setEditActionsEnabled(true);
						// FIXME
						// persistently store file-to-upk association
//						appProperties.setUpkProperty(tab.getModFile().getName(), file.getAbsolutePath());
					} else {
						// TODO: show error/warning message
					}
				}
			}
		});
		// exchange borders
		upkPnl.setBorder(upkTtf.getBorder());
		upkTtf.setBorder(null);
		
		upkPnl.add(upkTtf, CC.xy(1, 1));
		upkPnl.add(upkBtn, CC.xy(3, 1));
		
		// install listener on tabbed pane to capture selection changes
		modTabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				Component selComp = modTabPane.getSelectedComponent();
				if (selComp != null) {
					ModFileTab tab = (ModFileTab) selComp;
					
					// get UPK file reference from tab
					UpkFile upkFile = tab.getUpkFile();
					boolean hasUpk = (upkFile != null);
					
					// show file name in status bar (or missing file hint)
					upkTtf.setText((upkFile != null) ? ((upkFile.getFile() != null) ? upkFile.getFile().getPath() : null ) : "no UPK file selected");
					// enable/disable 'update', 'apply' and 'revert' actions
					setEditActionsEnabled(hasUpk);
					// enable UPK selection button
					upkBtn.setEnabled(true);
				} else {
					// last tab has been removed, reset to defaults
					upkTtf.setText("no modfile loaded");
					setEditActionsEnabled(false);
					upkBtn.setEnabled(false);
				}
			}
		});
		
		// TODO: implement progress monitoring hooks into various processes
		UIManager.getDefaults().put("nimbusOrange",
				UIManager.getDefaults().get("nimbusFocus"));
		JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		
		JPanel statusMsgPnl = new JPanel(new FormLayout("p:g, r:p", "b:p"));
		
		final JTextField statusMsgTtf = new JTextField();
		statusMsgTtf.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		statusMsgTtf.setEditable(false);
		statusMsgTtf.setBackground(bgCol);
		
		JButton loggingBtn = new JButton();
		loggingBtn.setBorder(null);
		
		defaultIcon = UIManager.getIcon("FileChooser.listViewIcon");
		
		// create lighter and darker versions of icon
		normalImg = new BufferedImage(
				defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		g = normalImg.createGraphics();
		defaultIcon.paintIcon(null, g, 0, 0);
		g.dispose();
		rolloverIcon = new ImageIcon(new RescaleOp(
				new float[] { 1.1f, 1.1f, 1.1f, 1.0f }, new float[4], null).filter(normalImg, null));
		pressedIcon = new ImageIcon(new RescaleOp(
				new float[] { 0.8f, 0.8f, 0.8f, 1.0f }, new float[4], null).filter(normalImg, null));
		
		loggingBtn.setIcon(defaultIcon);
		loggingBtn.setRolloverIcon(rolloverIcon);
		loggingBtn.setPressedIcon(pressedIcon);
		
		// create simple message log dialog
		final JDialog loggingDlg = new JDialog(this, "Message Log");
		loggingDlg.setIconImage(normalImg);
		Container loggingCont = loggingDlg.getContentPane();
		
		// create editor pane for storing log messages
		JEditorPane loggingEditor = new JEditorPane();
		loggingEditor.setDocument(new DefaultStyledDocument());
		loggingEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		loggingEditor.setEditable(false);
		
		loggingEditor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent evt) {
				try {
					// update status message text to show added line
					statusMsgTtf.setText(evt.getDocument().getText(evt.getOffset(), evt.getLength()));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void removeUpdate(DocumentEvent evt) { }
			@Override
			public void changedUpdate(DocumentEvent evt) { }
		});
		
		// install log handler on various loggers
		Handler logHandler = new LogHandler(loggingEditor);
		ModTree.logger.addHandler(logHandler);
		ModFileTabbedPane.logger.addHandler(logHandler);
		ReferenceUpdate.logger.addHandler(logHandler);
		ProjectTreeModel.logger.addHandler(logHandler);
		
		
		JScrollPane loggingScpn = new JScrollPane(loggingEditor,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		loggingScpn.setPreferredSize(new Dimension(480, 300));
		loggingScpn.setRowHeaderView(new LineNumberMargin(loggingEditor));
		
		loggingCont.add(loggingScpn);
		
		loggingDlg.pack();
		loggingDlg.setMinimumSize(loggingDlg.getSize());
		loggingDlg.setLocationRelativeTo(this);
		
		// install listener on logging button to show dialog
		loggingBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				loggingDlg.setVisible(true);
			}
		});
		
		// exchange borders
		statusMsgPnl.setBorder(statusMsgTtf.getBorder());
		statusMsgTtf.setBorder(null);
		
		statusMsgPnl.add(statusMsgTtf, CC.xy(1, 1));
		statusMsgPnl.add(loggingBtn, CC.xy(2, 1));
				
		statusBar.add(upkPnl, CC.xy(1, 1));
		statusBar.add(progressBar, CC.xy(2, 1));
		statusBar.add(statusMsgPnl, CC.xy(3, 1));
		
		return statusBar;
	}
	
	@Override
	public void setTitle(String title) {
		super.setTitle(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + title);
	}

	@Override
		public void dispose() {
			// FIXME
	//		appProperties.saveOpenState(tabPane, projectMdl);
			super.dispose();
		}

	/**
	 * Returns the cache of UPK files.
	 * @return the UPK cache
	 */
	public Map<File, UpkFile> getUPKCache() {
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
	}
	
	/**
	 * Convenience method to set the enable state of the 'Update
	 * References...', 'Apply Hex Changes' and 'Revert Hex Changes' menu
	 * items.
	 * @param enabled the enable state
	 */
	public void setEditActionsEnabled(boolean enabled) {
		ActionCache.getAction("refUpdate").setEnabled(enabled);
		ActionCache.getAction("hexApply").setEnabled(enabled);
		ActionCache.getAction("hexRevert").setEnabled(enabled);
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
	 * Creates a new project to be placed inside the specified project directory.
	 * @param projectDir the project directory
	 */
	public void createNewProject(File projectDir) {
		projectTree.createProject(projectDir);
	}

	/**
	 * Opens a project defined in the specified project XML file.
	 * @param file the project XML
	 */
	public void openProject(File xmlFile) {
		projectTree.openProject(xmlFile);
	}

	/**
	 * Removes the currently active project from the project pane.
	 */
	public void removeProject() {
		projectTree.removeProject();
		ActionCache.getAction("removeProject").setEnabled(false);
		ActionCache.getAction("deleteProject").setEnabled(false);
		
		// FIXME
//		appProperties.saveOpenState(tabPane, projectMdl);
	}
	
	/**
	 * Removes the currently active project from the project pane and deletes
	 * all associated files.
	 */
	public void deleteProject() {
		projectTree.deleteProject();
		ActionCache.getAction("removeProject").setEnabled(false);
		ActionCache.getAction("deleteProject").setEnabled(false);
	}
	
	/**
	 * Sets the currently active project in the project pane identified by the
	 * specified project node.
	 * @param projNode the project node
	 */
	public void setActiveProject(ProjectNode projNode) {
		// TODO: maybe persistently store active project node and provide getter method
		if (projNode != null) {
			ProjectNode target = (ProjectNode) projNode;
			// extract file from project
			File projectFile = target.getProjectFile();
			// update frame title
			this.setTitle(" : " + projectFile.getName());
			// enable 'Close Project' menu item
			ActionCache.getAction("removeProject").setEnabled(true);
			ActionCache.getAction("deleteProject").setEnabled(true);
		} else {
			this.setTitle("");
			ActionCache.getAction("removeProject").setEnabled(false);
			ActionCache.getAction("deleteProject").setEnabled(false);
		}
	}

	/**
	 * Creates a new mod file tab containing a default template file.
	 */
	public void createNewModFile() {
		if (modTabPane.createNewModFile()) {
			this.setFileActionsEnabled(true);
		}
		// TODO: associate mod file with active project
	}

	/**
	 * Creates a new mod file tab containing the specified mod file contents.
	 * @param modFile the mod file to open
	 */
	public void openModFile(File modFile) {
		if (modTabPane.openModFile(modFile)) {
			this.setFileActionsEnabled(true);
		}
		// TODO: associate mod file with active project
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
		// FIXME
//		appProperties.saveOpenState(tabPane, projectMdl);
	}

	/**
	 * Closes all mod file tabs.
	 */
	public void closeAllModFiles() {
		modTabPane.closeAllModFiles();
		
		this.setFileActionsEnabled(false);
		// FIXME
//		appProperties.saveOpenState(tabPane, projectMdl);
	}

	/**
	 * Returns the mod file of the currently active mod file tab.
	 * @return the active mod file
	 */
	public File getActiveModFile() {
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
	 * @param target the target file to save to
	 */
	public void saveModFileAs(File target) {
		modTabPane.saveModFileAs(target);
		// FIXME
//		appProperties.saveOpenState(tabPane, projectMdl);
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

}
