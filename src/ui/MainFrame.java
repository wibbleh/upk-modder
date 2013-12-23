package ui;

import io.parser.OperandTableParser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import model.modtree.ModTree;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ui.dialogs.ReferenceUpdateDialog;
import util.unrealhex.ReferenceUpdate;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Image;
import util.properties.UpkModderProperties;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static model.modtree.ModTree.logger;
import static ui.Constants.DIRECTORY_FILTER;

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
	public static MainFrame instance;
	
	/**
	 * The tabbed pane component of the application's main frame.
	 */
	private JTabbedPane tabPane;
	
	/**
	 * The JTree pane component of the application's main frame.
	 */
//	private ProjectPaneTreeMdl projectPaneTree;
	
	
	/**
	 * Class enabling persistence of app properties.
	 * Includes:
	 *			open status of projects and files
	 *			association of files with target upks
	 *			configuration settings
	 */
	private static UpkModderProperties appProperties;
	
	/**
	 * The cache of shared UI actions.
	 */
	private Map<String, Action> actionCache;
	
	/**
	 * The cache of shared UPK files.
	 */
	private Map<File, UpkFile> upkCache = new HashMap<>();

	/**
	 * UPK status text field
	 */
	JTextField upkTtf;
		
	/**
	 * The tree model of the project pane.
	 */
	private ProjectTreeModel projectMdl;

	/**
	 * The current active project.
	 * Used to determine which project the "Close Project" action affects.
	 */
	private int currentProject = -1;
	
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


		// TODO: move this elsewhere
		try {
			new OperandTableParser(Paths.get("config/operand_data.ini")).parseFile();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failure to initialize Operand Table: " + e);
		}

		appProperties = new UpkModderProperties();
		
		// TODO: move initial/default configuration elsewhere
		// TODO: add configuration dialogue?
		if(appProperties.getConfigProperty("project.path") == null) {
			appProperties.setConfigProperty("project.path", "UPKmodderProjects");
		}
		if(appProperties.getConfigProperty("project.template.file") == null) {
			appProperties.setConfigProperty("project.template.file", "defaultProjectTemplate.xml");
		}
		if(appProperties.getConfigProperty("modfile.template.file") == null) {
			appProperties.setConfigProperty("modfile.template.file", "defaultModfileTemplate.upk_mod");
		}
		

		// init action cache
		this.initActions();
		
		// create and lay out the frame's components
		this.initComponents();
		
		// make closing the main frame terminate the application
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// adjust frame size
		this.pack();
		this.setMinimumSize(this.getSize());
		// center frame in screen
		this.setLocationRelativeTo(null);
		// show frame
		this.setVisible(true);
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
	 * Initializes the cache of actions shared between the menubar and the toolbar.
	 */
	private void initActions() {
		this.actionCache = new HashMap<>();
		
		// new project
		Action newProjectAction = new BrowseAbstractAction("New Project", this, DIRECTORY_FILTER) {
			@Override
			protected void execute(File file) {
				try {
					projectMdl.newProject(file.getName(), file.toPath());
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failure to create new project file: " + e);
				}
			}
		};
		newProjectAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.fileIcon"));
//		newProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
//		newProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'n');
		newProjectAction.putValue(Action.SHORT_DESCRIPTION, "New Project");

		// new file
		Action newFileAction = new AbstractAction("New File") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// load configured template file
				File templateFile = new File(appProperties.getConfigProperty("modfile.template.file"));
				ModTab tab = new ModTab(templateFile, true);
				tabPane.addTab("New File", tab);
				tabPane.setSelectedComponent(tab);

				setFileActionsEnabled(true);
			}
		};
		newFileAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.fileIcon"));
		newFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newFileAction.putValue(Action.MNEMONIC_KEY, (int) 'n');
		newFileAction.putValue(Action.SHORT_DESCRIPTION, "New File");

		// open file
		Action openFileAction = new BrowseAbstractAction("Open File...", this, Constants.MOD_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				try {
					ModTab tab = new ModTab(file);
					tabPane.addTab(file.getName(), tab);
					tabPane.setSelectedComponent(tab);
					appProperties.saveOpenState(tabPane, projectMdl);

					// TODO: create function for upk re-association
					//re-associate upk if possible
					if(appProperties.getUpkProperty(file.getName()) != null) {
						File ufile = new File(appProperties.getUpkProperty(file.getName()));
						// grab UPK file from cache
						UpkFile upkFile = upkCache.get(ufile);
						if (upkFile == null) {
							// if cache doesn't contain UPK file instantiate a new one
							upkFile = new UpkFile(ufile);
						}

						// check whether UPK file is valid (i.e. header parsing worked properly)
						if (upkFile.getHeader() != null) {
							// store UPK file in cache
							upkCache.put(ufile, upkFile);
							// link UPK file to tab
							tab.setUpkFile(upkFile);
							// show file name in status bar
							upkTtf.setText(ufile.getPath());
							// enable 'update', 'apply' and 'revert' actions
							setEditActionsEnabled(true);
						} else {
							// TODO: show error/warning message
						}
					}

					setFileActionsEnabled(true);

					setFileActionsEnabled(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failure to open modfile: " + e);
				}
			}
		};
		openFileAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.directoryIcon"));
//		openFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
//		openFileAction.putValue(Action.MNEMONIC_KEY, (int) 'o');
		openFileAction.putValue(Action.SHORT_DESCRIPTION, "Open File");

		// open project
		Action openProjectAction = new BrowseAbstractAction("Open Project...", this, Constants.XML_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				try {
					projectMdl.addProject(file);

					appProperties.saveOpenState(tabPane, projectMdl);
					setFileActionsEnabled(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failure to open project file: " + e);
				}
			}
		};
		openProjectAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.directoryIcon"));
		openProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'o');
		openProjectAction.putValue(Action.SHORT_DESCRIPTION, "Open Project");
		
		// close project
		Action closeProjectAction = new AbstractAction("Close Project") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if(currentProject >= 0) {
					projectMdl.removeProject(currentProject);
					currentProject = -1;
					MainFrame.instance.setTitle(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER + "");
				}
				appProperties.saveOpenState(tabPane, projectMdl);
			}
		};
//		closeProjectAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
//		closeProjectAction.putValue(Action.MNEMONIC_KEY, (int) 'c');
		closeProjectAction.putValue(Action.SHORT_DESCRIPTION, "Close Proj");
		closeProjectAction.setEnabled(true);

		
		// close file
		Action closeFileAction = new AbstractAction("Close File") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tabPane.remove(tabPane.getSelectedComponent());
				appProperties.saveOpenState(tabPane, projectMdl);
			}
		};
		closeFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		closeFileAction.putValue(Action.MNEMONIC_KEY, (int) 'c');
		closeFileAction.putValue(Action.SHORT_DESCRIPTION, "Close File");
		closeFileAction.setEnabled(false);
		
		// close all
		Action closeAllAction = new AbstractAction("Close All") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tabPane.removeAll();
				appProperties.saveOpenState(tabPane, projectMdl);
			}
		};
		closeAllAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
		closeAllAction.putValue(Action.MNEMONIC_KEY, (int) 'l');
		closeAllAction.putValue(Action.SHORT_DESCRIPTION, "Close All");
		closeAllAction.setEnabled(false);
		
		// save as
		final Action saveAsAction = new BrowseAbstractAction("Save As...", this, Constants.MOD_FILE_FILTER, true) {
			@Override
			public File getTarget() {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					File file = ((ModTab) selComp).getModFile();
					if ((file == null) || !file.exists()) {
						file = new File(getLastSelectedFile().getParent()
								+ tabPane.getTitleAt(tabPane.getSelectedIndex()) + ".upk_mod");
					}
					return file;
				}
				return super.getTarget();
			}
			@Override
			protected void execute(File file) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					tabPane.setTitleAt(tabPane.getSelectedIndex(), file.getName());
					tabPane.updateUI(); // needed to update tab with
					ModTab tab = (ModTab) selComp;
					tab.setModFile(file);
					tab.saveFile();
					appProperties.saveOpenState(tabPane, projectMdl);
				}
			}
		};
		saveAsAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.floppyDriveIcon"));
		saveAsAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
		saveAsAction.putValue(Action.MNEMONIC_KEY, (int) 'a');
		saveAsAction.setEnabled(false);
		
		// save
		Action saveAction = new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					File file = tab.getModFile();
					if (file != null) {
						tab.saveFile();
					} else {
						saveAsAction.actionPerformed(evt);
					}
				}
			}
		};
		saveAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.floppyDriveIcon"));
		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveAction.putValue(Action.MNEMONIC_KEY, (int) 's');
		saveAction.putValue(Action.SHORT_DESCRIPTION, "Save");
		saveAction.setEnabled(false);
		
		// export
		// TODO: implement export functionality, create file filters
		Action exportAction = new BrowseAbstractAction("Export...", this, null, true) {
			@Override
			protected void execute(File file) {
				// TODO: do something
			}
		};
		exportAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		exportAction.putValue(Action.MNEMONIC_KEY, (int) 'e');
		exportAction.setEnabled(false);
		
		// exit
		Action exitAction = new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// dispose main frame (thereby terminating the application)
				appProperties.saveOpenState(tabPane, projectMdl);
				MainFrame.this.dispose();
			}
		};
		exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
		exitAction.putValue(Action.MNEMONIC_KEY, (int) 'x');
		
		// update references
		Action refUpdateAction = new AbstractAction("Update References...") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					showRefUpdateDialog(((ModTab) selComp).getTree());
				}
			}
		};
		refUpdateAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileChooser.detailsViewIcon"));
		refUpdateAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
		refUpdateAction.putValue(Action.MNEMONIC_KEY, (int) 'u');
		refUpdateAction.putValue(Action.SHORT_DESCRIPTION, "Update References...");
		refUpdateAction.setEnabled(false);
		
		// apply hex
		Action hexApplyAction = new AbstractAction("Apply Hex Changes") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					if(tab.applyChanges()) {
						// set Tab color/font/tooltip style to indicate apply/revert status
						tabPane.setForegroundAt(tabPane.getSelectedIndex(),  new Color(0, 0, 230)); // blue indicates AFTER
						((ButtonTabbedPane) tabPane).setFontAt(tabPane.getSelectedIndex(), new Font(Font.MONOSPACED, Font.ITALIC, 12));
						tabPane.setToolTipTextAt(tabPane.getSelectedIndex(), "Hex Applied");
						tabPane.updateUI(); // needed to update tab with
					}
				}
			}
		}; 
		hexApplyAction.putValue(Action.SMALL_ICON, Constants.HEX_SMALL_ICON);
		hexApplyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
		hexApplyAction.putValue(Action.MNEMONIC_KEY, (int) 'a');
		hexApplyAction.putValue(Action.SHORT_DESCRIPTION, "Apply Hex Changes");
		hexApplyAction.setEnabled(false);
		
		// revert hex
		Action hexRevertAction = new AbstractAction("Revert Hex Changes") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					if(tab.revertChanges()) {
						// set Tab color/font/tooltip style to indicate apply/revert status
						tabPane.setForegroundAt(tabPane.getSelectedIndex(),  new Color(0, 128, 0)); // green indicates BEFORE
						((ButtonTabbedPane) tabPane).setFontAt(tabPane.getSelectedIndex(), new Font(Font.MONOSPACED, Font.PLAIN, 12));
						tabPane.setToolTipTextAt(tabPane.getSelectedIndex(), "Original Hex");
						tabPane.updateUI(); // needed to update tab 
					}
				}
			}
		};
		hexRevertAction.putValue(Action.SMALL_ICON, Constants.HEX_SMALL_ICON);
		hexRevertAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		hexRevertAction.putValue(Action.MNEMONIC_KEY, (int) 'r');
		hexRevertAction.putValue(Action.SHORT_DESCRIPTION, "Revert Hex Changes");
		hexRevertAction.setEnabled(false);
		
		// create help and about icons
		BufferedImage helpImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = helpImg.createGraphics();
		((AbstractRegionPainter) UIManager.get("OptionPane[Enabled].questionIconPainter")).paint(
				g2, null, 16, 16);
		g2.dispose();
		Icon helpIcon = new ImageIcon(helpImg);
		BufferedImage aboutImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		g2 = aboutImg.createGraphics();
		((AbstractRegionPainter) UIManager.get("OptionPane[Enabled].informationIconPainter")).paint(
				g2, null, 16, 16);
		g2.dispose();
		Icon aboutIcon = new ImageIcon(aboutImg);
		
		// help
		Action helpAction = new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// TODO: implement help dialog
			}
		};
		helpAction.putValue(Action.SMALL_ICON, helpIcon);
		helpAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpAction.putValue(Action.MNEMONIC_KEY, (int) 'h');
		helpAction.setEnabled(false);
		
		// about
		Action aboutAction = new AbstractAction("About") {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				// TODO: make dialog use same icon as menu item
				showAboutDialog();
			}
		};
		aboutAction.putValue(Action.SMALL_ICON, aboutIcon);
		aboutAction.putValue(Action.MNEMONIC_KEY, (int) 'a');

		// project actions
		actionCache.put("newProj", newProjectAction);
		actionCache.put("openProj", openProjectAction);
		actionCache.put("closeProj", closeProjectAction);
		// file actions
		actionCache.put("newFile", newFileAction);
		actionCache.put("openFile", openFileAction);
		actionCache.put("closeFile", closeFileAction);
		actionCache.put("closeAll", closeAllAction);
		actionCache.put("saveAs", saveAsAction);
		actionCache.put("save", saveAction);
		actionCache.put("export", exportAction);
		actionCache.put("exit", exitAction);
		// edit actions
		actionCache.put("refUpdate", refUpdateAction);
		actionCache.put("hexApply", hexApplyAction);
		actionCache.put("hexRevert", hexRevertAction);
		// help actions
		actionCache.put("help", helpAction);
		actionCache.put("about", aboutAction);
		
	}

	/**
	 * Creates and lays out the frame's components.
	 * @throws Exception if an I/O error occurs
	 */
	private void initComponents() {

//		//TODO: sort out relative pathing issues for modsrc directories in xml files, should be relative to the xml file location, I think.
		final JTree projectTree = new JTree(this.projectMdl = new ProjectTreeModel());
//		final JTree projectTree = new JTree(this.projectMdl = new ProjectPaneTreeMdl());
		
		// TODO: use custom icons for projects/modpackages/modfiles ?
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			/** The renderer delegate. */
			private TreeCellRenderer delegate = projectTree.getCellRenderer();
			
			@Override
			public Component getTreeCellRendererComponent(
					JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				Component comp = delegate.getTreeCellRendererComponent(
						tree, value, sel, expanded, leaf, row, hasFocus);
				if (value instanceof ProjectTreeModel.ProjectNode) {
					comp.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
				} else if (value instanceof File) {
					((JLabel) comp).setText(((File) value).getName());
					comp.setFont(new Font(Font.DIALOG, Font.TRUETYPE_FONT, 11));
				}
				return comp;
					}
		};
		projectTree.setCellRenderer(renderer);
		projectTree.setRootVisible(false);
		this.projectMdl.addTreeModelListener(new TreeModelListener() {
		
			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				projectTree.expandPath(new TreePath(projectMdl.getRoot()));
			}
			
			@Override
			public void treeStructureChanged(TreeModelEvent e) { }
			@Override
			public void treeNodesRemoved(TreeModelEvent e) { }
			@Override
			public void treeNodesChanged(TreeModelEvent e) { }
		});
		
		// mouse adapter to handle opening files from the project pane
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				// handler to set active project
				if (evt.getClickCount() == 1) {
					TreePath selPath = projectTree.getPathForLocation(evt.getX(), evt.getY());
					if (selPath != null) {
						if (selPath.getPathComponent(1) instanceof ProjectTreeModel.ProjectNode) { // should always be a project file
							// find the project node
							ProjectTreeModel.ProjectNode target = (ProjectTreeModel.ProjectNode) selPath.getPathComponent(1);
							int foundIndex = -1;
							for ( int i = 0 ; i < projectMdl.getRoot().getChildCount(); i++) {
								if (projectMdl.getRoot().getChildAt(i) == target) {
									foundIndex = i;
								}
							}
							if(foundIndex >= 0) {
//								projectMdl.getProjectFileAt(foundIndex).getName();
								MainFrame.instance.setTitle(Constants.APPLICATION_NAME + " " + Constants.VERSION_NUMBER 
										+ " : " + projectMdl.getProjectFileAt(foundIndex).getName());
								currentProject = foundIndex;
							}
						}
					}
				}
				
				// handler for opening files
				if (evt.getClickCount() == 2) {
					TreePath selPath = projectTree.getPathForLocation(evt.getX(), evt.getY());
					if (selPath != null) {
						if (selPath.getLastPathComponent() instanceof File) {
							File file = (File) selPath.getLastPathComponent();
							if (file.isFile()) {
								try {
									ModTab tab = new ModTab(file);
									tabPane.addTab(file.getName(), tab);
									tabPane.setSelectedComponent(tab);
									appProperties.saveOpenState(tabPane, projectMdl);
									
									// TODO: create function for upk re-association
									//re-associate upk if possible
									if(appProperties.getUpkProperty(file.getName()) != null) {
										File ufile = new File(appProperties.getUpkProperty(file.getName()));
										// grab UPK file from cache
										UpkFile upkFile = upkCache.get(ufile);
										if (upkFile == null) {
											// if cache doesn't contain UPK file instantiate a new one
											upkFile = new UpkFile(ufile);
										}

										// check whether UPK file is valid (i.e. header parsing worked properly)
										if (upkFile.getHeader() != null) {
											// store UPK file in cache
											upkCache.put(ufile, upkFile);
											// link UPK file to tab
											tab.setUpkFile(upkFile);
											// show file name in status bar
											upkTtf.setText(ufile.getPath());
											// enable 'update', 'apply' and 'revert' actions
											setEditActionsEnabled(true);
										} else {
											// TODO: show error/warning message
										}
									}

									setFileActionsEnabled(true);
								} catch (Exception ex) {
									logger.log(Level.SEVERE, "Failure to open modfile from Project: " + ex);
								}
							}
						}
					}
				}
			}
		};
		projectTree.addMouseListener(ml);
		
		JToolBar projectBar = new JToolBar();
		projectBar.setFloatable(false);
		
		projectBar.add(Box.createHorizontalGlue());
		projectBar.add(actionCache.get("newProj"));
		projectBar.add(actionCache.get("openProj"));
//		projectBar.add(new JButton(Constants.HEX_SMALL_ICON));
//		projectBar.add(new JButton(Constants.HEX_SMALL_ICON));
//		projectBar.add(new JButton(Constants.HEX_SMALL_ICON));
//		projectBar.add(new JButton(Constants.HEX_SMALL_ICON));
		
		JScrollPane projectScpn = new JScrollPane(projectTree);
		projectScpn.setPreferredSize(new Dimension(320, 600));
		projectScpn.setColumnHeaderView(projectBar);
		
		// create menu bar
		JMenuBar menuBar = this.createMenuBar();
		
		// create tool bar
		JToolBar fileToolBar = this.createFileToolBar();
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		// create central tabbed pane
		UIManager.put("TabbedPane:TabbedPaneTabArea.contentMargins", new InsetsUIResource(3, 0, 4, 0));
		UIManager.put("TabbedPane:TabbedPaneTab.contentMargins", new InsetsUIResource(2, 8, 3, 3));
		tabPane = new ButtonTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT) {
			@Override
			public void removeTabAt(int index) {
				ModTab thisTab = (ModTab) this.getComponentAt(index);
				// check whether the tab has a valid UPK file reference and whether
				// the same file is referenced by another tab
				UpkFile upkFile = thisTab.getUpkFile();
				if (upkFile != null) {
					boolean shared = false;
					// iterate tabs, skip the one that's about to be removed
					for (int i = 0; (i < this.getTabCount()) && (i != index); i++) {
						ModTab thatTab = (ModTab) this.getComponentAt(i);
						if (upkFile.equals(thatTab.getUpkFile())) {
							shared = true;
							break;
						}
					}
					// if referenced UPK file is unique among tabs remove it from cache
					if (!shared) {
						upkCache.remove(upkFile.getFile());
					}
				}
				
				super.removeTabAt(index);
				
				// if last tab has been removed disable 'save' and 'close' actions
				if (this.getTabCount() == 0) {
					setFileActionsEnabled(false);
				}
				appProperties.saveOpenState(tabPane, projectMdl);
			}
		};
		tabPane.setPreferredSize(new Dimension(1000, 600));
		
		// create status bar
		JPanel statusBar = this.createStatusBar();

		// Wrap projectpane and tabs into a split pane
		JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectScpn, tabPane);
		
		this.setJMenuBar(menuBar);
		contentPane.add(fileToolBar, BorderLayout.NORTH);
		contentPane.add(centerPane, BorderLayout.CENTER);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
		// restore mappings from file to target upks
		appProperties.restoreUpkState();
		
		// restore open projects/files from last time app was run
		appProperties.restoreOpenState();
		
		List<String> projectPathList = appProperties.getOpenProjects();
		if(projectPathList != null) {
			if(!projectPathList.isEmpty()) {
				for (String filePath : projectPathList) {
					projectMdl.addProject(new File(filePath));
				}
				setFileActionsEnabled(true);
			}
		}

		// open previously open files
		List<String> filePathList = appProperties.getOpenFiles();
		if(filePathList != null) {
			if(!filePathList.isEmpty()) {
				for (String filePath : filePathList) {
					File file = new File(filePath);
					if(file.exists()) {
						//create new tab
						ModTab tab = new ModTab(file);
						tabPane.addTab(file.getName(), tab);
						tabPane.setSelectedComponent(tab);
						
						// TODO: create function for upk re-association
						//re-associate upk if possible
						if(appProperties.getUpkProperty(file.getName()) != null) {
							File ufile = new File(appProperties.getUpkProperty(file.getName()));
							// grab UPK file from cache
							UpkFile upkFile = upkCache.get(ufile);
							if (upkFile == null) {
								// if cache doesn't contain UPK file instantiate a new one
								upkFile = new UpkFile(ufile);
							}

							// check whether UPK file is valid (i.e. header parsing worked properly)
							if (upkFile.getHeader() != null) {
								// store UPK file in cache
								upkCache.put(ufile, upkFile);
								// link UPK file to tab
								tab.setUpkFile(upkFile);
								// show file name in status bar
								upkTtf.setText(ufile.getPath());
								// enable 'update', 'apply' and 'revert' actions
								setEditActionsEnabled(true);
							} else {
								// TODO: show error/warning message
							}
						}
					}
				}
				setFileActionsEnabled(true);
			}
		}
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
		fileMenu.add(actionCache.get("newProj"));
		fileMenu.add(actionCache.get("openProj"));
		fileMenu.add(actionCache.get("closeProj"));
		fileMenu.addSeparator();
		fileMenu.add(actionCache.get("newFile"));
		fileMenu.add(actionCache.get("openFile"));
		fileMenu.add(actionCache.get("closeFile"));
		fileMenu.add(actionCache.get("closeAll"));
		fileMenu.addSeparator();
		fileMenu.add(actionCache.get("save"));
		fileMenu.add(actionCache.get("saveAs"));
		fileMenu.addSeparator();
		fileMenu.add(actionCache.get("export"));
		fileMenu.addSeparator();
		fileMenu.add(actionCache.get("exit"));
		
		// create edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');
		editMenu.add(actionCache.get("refUpdate"));
		editMenu.addSeparator();
		editMenu.add(actionCache.get("hexApply"));
		editMenu.add(actionCache.get("hexRevert"));
		
		// create help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
		helpMenu.add(actionCache.get("help"));
		helpMenu.addSeparator();
		helpMenu.add(actionCache.get("about"));

		// add menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	/**
	 * Creates and configures the file tool bar.
	 * @return the tool bar
	 */
	private JToolBar createFileToolBar() {
		
		JToolBar toolBar = new JToolBar();
		toolBar.add(actionCache.get("newFile"));
		toolBar.add(actionCache.get("openFile"));
		toolBar.addSeparator();
		toolBar.add(actionCache.get("save"));
		toolBar.addSeparator();
		toolBar.add(actionCache.get("refUpdate"));
		toolBar.addSeparator();
		toolBar.add(actionCache.get("hexApply"));
		toolBar.add(actionCache.get("hexRevert"));

		return toolBar;
	}

	/**
	 * Creates and configures the status bar.
	 * @return the status bar
	 */
	private JPanel createStatusBar() {
		// TODO: maybe make status bar a JToolBar
		JPanel statusBar = new JPanel(new FormLayout("0px:g(0.5), 0px:g(0.25), 0px:g(0.25)", "f:p"));
		Color bgCol = new Color(214, 217, 223);
		
		JPanel upkPnl = new JPanel(new FormLayout("p:g, r:p", "b:p"));
		
		upkTtf = new JTextField("no modfile loaded");
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
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;

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
						// persistently store file-to-upk association
						appProperties.setUpkProperty(tab.getModFile().getName(), file.getAbsolutePath());
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
		upkPnl.add(upkBtn, CC.xy(2, 1));
		
		// install listener on tabbed pane to capture selection changes
		tabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					
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
		progressBar.setString("progress goes here");
		progressBar.setValue(50);
		
		JPanel statusMsgPnl = new JPanel(new FormLayout("p:g, r:p", "b:p"));
		
		JTextField statusMsgTtf = new JTextField("STATUS MESSAGES GO HERE");
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
		
		JEditorPane loggingEditor = new JEditorPane();
		loggingEditor.setDocument(new DefaultStyledDocument());
		loggingEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		loggingEditor.setEditable(false);
		
		Handler logHandler = new LogHandler(loggingEditor);
		ModTree.logger.addHandler(logHandler);
		ModTab.logger.addHandler(logHandler);
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
	
	/**
	 * Convenience method to set the enable state of the 'Save', 'Save As',
	 * 'Close' and 'Close All' menu items.
	 * @param enabled the enable state
	 */
	protected void setFileActionsEnabled(boolean enabled) {
		actionCache.get("closeFile").setEnabled(enabled);
		actionCache.get("closeAll").setEnabled(enabled);
		actionCache.get("save").setEnabled(enabled);
		actionCache.get("saveAs").setEnabled(enabled);
		actionCache.get("refUpdate").setEnabled(enabled);
	}
	
	/**
	 * Convenience method to set the enable state of the 'Update
	 * References...', 'Apply Hex Changes' and 'Revert Hex Changes' menu
	 * items.
	 * @param enabled the enable state
	 */
	protected void setEditActionsEnabled(boolean enabled) {
		actionCache.get("hexApply").setEnabled(enabled);
		actionCache.get("hexRevert").setEnabled(enabled);
	}

	/**
	 * Initializes and displays a new Reference Update dialog.
	 */
	private void showRefUpdateDialog(ModTree modTree) {
		new ReferenceUpdateDialog(modTree).setVisible(true);
	}

	/**
	 * Creates and displays the application's About dialog.
	 */
	private void showAboutDialog() {
		// TODO: prettify dialog... like, really, it's fugly
		JDialog aboutDlg = new JDialog(this, "About UPK Modder", true);
		
		Container contentPane = aboutDlg.getContentPane();
		
		JLabel aboutLbl = new JLabel(Constants.ABOUT_TEXT);
		aboutLbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		contentPane.add(aboutLbl);
		
		aboutDlg.pack();
		aboutDlg.setResizable(false);
		aboutDlg.setLocationRelativeTo(this);
		aboutDlg.setVisible(true);
	}
	
	/**
	 * A hybrid tree model combining a tree node-based setup with a file tree model.
	 * @author XMS
	 */
	public static class ProjectTreeModel implements TreeModel {
	
		/**
		 * The logger.
		 */
		public static final Logger logger = Logger.getLogger(ProjectTreeModel.class.getName());
		
		/**
		 * The root of the project tree.
		 */
		private MutableTreeNode root;
		
		/**
		 * The tree model listeners.
		 */
		private List<TreeModelListener> listeners;
		
		/**
		 * Constructs a project tree model using a default root node.
		 */
		public ProjectTreeModel() {
			this.root = new DefaultMutableTreeNode("Project Root");
			this.listeners = new ArrayList<>();
}
		
		/**
		 * Creates a new project the the given name at the specified location
		 * TODO: empty projects get into bad display state if user tries to expand them
		 *		fix or disallow attempting to expand empty projects
		 * @param name
		 * @param directory
		 */
		public void newProject(String name, Path directory) {
			File xmlFile = new File(appProperties.getConfigProperty("project.template.file"));
			try {
				// create document builder
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				// parse XML
				Document doc = db.parse(xmlFile);
				
				// check if new directory exists
				File currentLocation = directory.toFile();
				// create project directory if needed
				if(!currentLocation.exists()) {
					Files.createDirectory(directory);
				}
				// create modsrc directory
				if(!directory.resolve("modsrc").toFile().exists()) {
					Files.createDirectory(directory.resolve("modsrc"));
				}
				// set name of new project
				doc.getElementsByTagName("name").item(0).setTextContent(name);
				
				// set directory of new project
				Path projectRootPath = Paths.get(appProperties.getConfigProperty("project.path")).toAbsolutePath().getParent();
				// use relative path to maximize portability
				Path newRelativePath = projectRootPath.relativize(directory.resolve("modsrc"));
				
				//set directory of new project
				doc.getElementsByTagName("source-root").item(0).setTextContent(newRelativePath.toString());

				// save new xml file to new directory
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT, "yes");
				tr.setOutputProperty(OutputKeys.METHOD, "xml");
				tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//				tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
				tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

				// send DOM to file
				File newFile = directory.resolve(name+".xml").toFile();
				tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(newFile)));
				addProject(newFile);

			} catch(ParserConfigurationException | SAXException | IOException ex) {
				logger.log(Level.INFO, "Failed to load project file \'" + xmlFile.getName() + "\'", ex);
			} catch(TransformerConfigurationException ex) {
				logger.log(Level.INFO, "Failed to configure new project file", ex);
			} catch(TransformerException ex) {
				logger.log(Level.INFO, "Failed to write new project file", ex);
			}
		}
		
		/**
		 * Adds a new project detailed within the specified XML file.
		 * @param xmlFile the project XML file
		 */
		public void addProject(File xmlFile) {
			try {
				ProjectNode projNode = new ProjectNode(xmlFile);
				int childCount = this.root.getChildCount();
				this.root.insert(projNode, childCount);
				this.fireTreeNodesInserted(new int[] { childCount }, new Object[] { projNode });
				logger.log(Level.INFO, "Project \'" + projNode + "\' successfully loaded");
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger.log(Level.INFO, "Failed to load project file \'" + xmlFile.getName() + "\'", e);
			}
		}
		
		public File getProjectFileAt(int index) {
			return ((ProjectNode) this.root.getChildAt(index)).getProjectFile();
		}
		
		/**
		 * Notifies all registered listeners that tree nodes have been added.
		 */
		private void fireTreeNodesInserted(int[] childIndices, Object[] children) {
			TreeModelEvent evt = new TreeModelEvent(this, new TreePath(this.root), childIndices, children);
			for (TreeModelListener listener : this.listeners) {
				listener.treeNodesInserted(evt);
			}
		}
		
		/**
		 * Removes the project associated with the provided XML file.
		 * @param xmlFile the project XML file
		 */
		public void removeProject(File xmlFile) {
			@SuppressWarnings("rawtypes")
			Enumeration children = this.root.children();
			while (children.hasMoreElements()) {
				ProjectNode projNode = (ProjectNode) children.nextElement();
				if (xmlFile.equals(projNode.getUserObject())) {
					this.removeProject(this.root.getIndex(projNode));
					return;
				}
			}
		}
		
		/**
		 * Removes the project of the specified index.
		 * @param index the project index
		 */
		public void removeProject(int index) {
			TreeNode child = this.root.getChildAt(index);
			this.root.remove(index);
			this.fireTreeNodesRemoved(new int[] { index }, new Object[] { child });
		}
		
		/**
		 * Notifies all registered listeners that tree nodes have been removed.
		 */
		private void fireTreeNodesRemoved(int[] childIndices, Object[] children) {
			TreeModelEvent evt = new TreeModelEvent(this, new TreePath(this.root), childIndices, children);
			for (TreeModelListener listener : this.listeners) {
				listener.treeNodesRemoved(evt);
			}
		}

		/**
		 * Convenience method checking whether the provided parent is a project
		 * node or a file itself. In the former case returns the root directory
		 * of the project, in the latter case returns the node cast to
		 * <code>File</code>.
		 * @param node the parent node
		 * @return a file reference or <code>null</code> if the parent node does
		 *  not reference a file
		 */
		private File getFileForNode(Object node) {
			File file = null;
			if (node instanceof ProjectNode) {
				// node is a project node, use project root directory
				file = ((ProjectNode) node).getProjectDirectory();
			} else if (node instanceof File) {
				// is a directory below a project's root directory, return file at index
				file = (File) node;
			}
			return file;
		}

		@Override
		public MutableTreeNode getRoot() {
			return this.root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent == this.root) {
				// parent is the tree root, return project node at index
				return this.root.getChildAt(index);
			}
			File file = this.getFileForNode(parent);
			if (file != null) {	// sanity check
				// return file at index
//				return file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER)[index];
				return file.listFiles()[index];
			}
			// fallback value, we should actually never get here
			System.err.println("ERROR: unknown node type in project tree: " + parent);
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent == this.root) {
				// parent is the tree root, return the number of project nodes
				return this.root.getChildCount();
			}
			File file = this.getFileForNode(parent);
			if (file != null) {	// sanity check
				// if file is directory return number of files and subdirectories in it
//				return (file.isFile()) ? 0 :
//					file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER).length;
				return (file.isFile()) ? 0 :
					file.listFiles().length;
			}
			// fallback value, we should actually never get here
			System.err.println("ERROR: unknown node type in project tree: " + parent);
			return 0;
		}
		
		@Override
		public boolean isLeaf(Object node) {
			if (node == this.root) {
				// node is the tree root, return whether it has any children
				return (this.root.getChildCount() == 0);
			}
			File file = this.getFileForNode(node);
			if (file != null) {	// sanity check
				// check if is empty directory
				// TODO: fix up bad icon for empty directory
				if(file.isDirectory()) {
					 return (file.list().length == 0); 
				}
				// return whether the file is not a directory
				return file.isFile();
			}
			// fallback value, we should actually never get here
			System.err.println("ERROR: unknown node type in project tree: " + node);
			return false;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent == this.root) {
				// parent is the tree root, child must be a project node, return its index
				this.root.getIndex((TreeNode) child);
			}
			File file = this.getFileForNode(parent);
			if (file != null) {	// sanity check
				// return index of file inside parent directory
				return Arrays.asList(
						file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER)).indexOf(child);
			}
			// fallback value, we should actually never get here
			System.err.println("ERROR: unknown node type in project tree: " + parent);
			return 0;
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			this.listeners.add(l);
		}
		
		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			this.listeners.remove(l);
		}
		
		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			// don't need, tree model is not supposed to be editable
		}
		
		/**
		 * Custom tree node representing a project in the project tree. Serves
		 * as the root directory for a project file structure.
		 * @author XMS
		 */
		private class ProjectNode extends DefaultMutableTreeNode {
			
			/**
			 * The project's root directory.
			 */
			private File projectDir;
			
			/**
			 * The project's name.
			 */
			private String projectName;
			
			/**
			 * Stored link to the project file.
			 */
			private File projectFile;
			
			/**
			 * Constructs a new project node by parsing the specified XML file.
			 * @param xmlFile the project XML file
			 * @throws IOException if any I/O errors occur
			 * @throws SAXException if any parse errors occur
			 */
			public ProjectNode(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
				super(xmlFile);
				
				this.parse(xmlFile);
			}
			

			public File getProjectFile() {
				return this.projectFile;
			}
			
			/**
			 * 
			 * @param xmlFile
			 * @throws IOException if any I/O errors occur
			 * @throws SAXException if any parse errors occur
			 */
			private void parse(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
				// store copy of project file for later referencing
				this.projectFile = xmlFile;
				// create document builder
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				// parse XML
				Document doc = db.parse(xmlFile);
				// extract project name and root directory
				this.projectName = doc.getElementsByTagName("name").item(0).getTextContent();
				this.projectDir = new File(doc.getElementsByTagName("source-root").item(0).getTextContent());
			}
			
			/**
			 * Returns the project's root directory.
			 * @return the project directory
			 */
			public File getProjectDirectory() {
				return projectDir;
			}

			@Override
			public String toString() {
				return this.projectName;
			}
		}
	}
	
}
