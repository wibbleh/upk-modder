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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
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
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.swing.text.DefaultStyledDocument;

import model.modtree.ModTree;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;

import ui.dialogs.ReferenceUpdateDialog;
import util.unrealhex.ReferenceUpdate;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import model.modproject.ProjectTreeMdl;

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
//	private JTree projectTree;
	private ProjectPaneTree projectPaneTree;
	
	/**
	 * The cache of shared UI actions.
	 */
	private Map<String, Action> actionCache;
	
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
		try {
			this.setIconImage(ImageIO.read(this.getClass().getResource("/ui/resources/icons/hex16.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// init action cache
		this.initActions();
		
		// create and lay out the frame's components
		this.initComponents();
		
		// TODO: move this elsewhere
		try {
			new OperandTableParser(Paths.get("config/operand_data.ini")).parseFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		
		// new
		Action newAction = new AbstractAction("New") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ModTab tab = new ModTab();
				tabPane.addTab("New File", tab);
				tabPane.setSelectedComponent(tab);

				setFileActionsEnabled(true);
			}
		};
		newAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.fileIcon"));
		newAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newAction.putValue(Action.MNEMONIC_KEY, (int) 'n');
		newAction.putValue(Action.SHORT_DESCRIPTION, "New");

		// open
		Action openAction = new BrowseAbstractAction("Open File...", this, Constants.MOD_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				try {
					ModTab tab = new ModTab(file);
					tabPane.addTab(file.getName(), tab);
					tabPane.setSelectedComponent(tab);

					setFileActionsEnabled(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		openAction.putValue(Action.SMALL_ICON, UIManager.getIcon("FileView.directoryIcon"));
		openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openAction.putValue(Action.MNEMONIC_KEY, (int) 'o');
		openAction.putValue(Action.SHORT_DESCRIPTION, "Open");
		
		// close
		Action closeAction = new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tabPane.remove(tabPane.getSelectedComponent());
			}
		};
		closeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		closeAction.putValue(Action.MNEMONIC_KEY, (int) 'c');
		closeAction.putValue(Action.SHORT_DESCRIPTION, "Close");
		closeAction.setEnabled(false);
		
		// close all
		Action closeAllAction = new AbstractAction("Close All") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				tabPane.removeAll();
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
								+ tabPane.getTitleAt(tabPane.getSelectedIndex()) + ".mod");
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
		
		Icon hexIcon = new ImageIcon(this.getClass().getResource("/ui/resources/icons/hex16.png"));

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
		hexApplyAction.putValue(Action.SMALL_ICON, hexIcon);
		hexApplyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
		hexApplyAction.putValue(Action.MNEMONIC_KEY, (int) 'a');
		hexApplyAction.putValue(Action.SHORT_DESCRIPTION, "Apply Hex Changes");
		hexApplyAction.setEnabled(false);
		
		// revert hex
		Action hexRevertAction = new AbstractAction("Revert Hex Changes") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					if(tab.revertChanges()) {
						// set Tab color/font/tooltip style to indicate apply/revert status
						tabPane.setForegroundAt(tabPane.getSelectedIndex(),  new Color(0, 128, 0)); // green indicates BEFORE
						((ButtonTabbedPane) tabPane).setFontAt(tabPane.getSelectedIndex(), new Font(Font.MONOSPACED, Font.PLAIN, 12));
						tabPane.setToolTipTextAt(tabPane.getSelectedIndex(), "Original Hex");
						tabPane.updateUI(); // needed to update tab with
					}
				}
			}
		};
		hexRevertAction.putValue(Action.SMALL_ICON, hexIcon);
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
		
		// file actions
		actionCache.put("new", newAction);
		actionCache.put("open", openAction);
		actionCache.put("close", closeAction);
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

		// TODO: wrap this under "Open Project..." and "New Project" actions
		projectPaneTree = new ProjectPaneTree(new File("UPKmodderProjects/Expanded Perk Tree EW/Expanded Perk Tree EW.xml"));
		projectPaneTree.addProject(new File("UPKmodderProjects/Base Missions EU/Base Missions EU.xml"));
		//TODO: sort out relative pathing issues for modsrc directories in xml files
		// Should be relative to the xml file location, I think.
		
		// alter display characteristics
		final JTree projectTree = new JTree(projectPaneTree);
		// TODO: use custom icons for projects/modpackages/modfiles ?
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {

				if(value instanceof ProjectTreeMdl) {
					ProjectTreeMdl model = (ProjectTreeMdl) value;
					value = model.getName();
					this.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
					this.setIcon(null);
				}
				if(value instanceof File) {
					File file = (File) value;
					if(file.isFile()) {
						value = file.getName();
						this.setFont(new Font(Font.DIALOG, Font.TRUETYPE_FONT, 11));
					}
					if(file.isDirectory()) {
						value = file.getName();
						this.setFont(new Font(Font.DIALOG, Font.TRUETYPE_FONT, 11));
					}
				}		
				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				if(value instanceof ProjectTreeMdl) {
					this.setIcon(null);
				}
				return this;
			}
		};
		projectTree.setCellRenderer(renderer);
		projectTree.setRootVisible(false);
		
		// mouse adapter to handle opening files from the project pane
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = projectTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if(e.getClickCount() == 1) {
//						mySingleClick(selRow, selPath);
					} else if(e.getClickCount() == 2) {
						if(selPath.getLastPathComponent() instanceof File) {
							File file = (File) selPath.getLastPathComponent();
							if(file.isFile()) {
								try {
									ModTab tab = new ModTab(file);
									tabPane.addTab(file.getName(), tab);
									tabPane.setSelectedComponent(tab);

									setFileActionsEnabled(true);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						}
					}
				}
			}
		};
		projectTree.addMouseListener(ml);
		
		JScrollPane projectPane = new JScrollPane(projectTree);
		projectPane.setPreferredSize(new Dimension(320, 600));
		
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
			}
		};
		tabPane.setPreferredSize(new Dimension(1000, 600));
		
		// create status bar
		JPanel statusBar = this.createStatusBar();

		// Wrap projectpane and tabs into a split pane
		JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectPane, tabPane);
		
		this.setJMenuBar(menuBar);
		contentPane.add(toolBar, BorderLayout.NORTH);
//		contentPane.add(projectPane, BorderLayout.WEST);
//		contentPane.add(tabPane, BorderLayout.EAST);
		contentPane.add(centerPane, BorderLayout.CENTER);
		contentPane.add(statusBar, BorderLayout.SOUTH);
		
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
		fileMenu.add(actionCache.get("new"));
		fileMenu.add(actionCache.get("open"));
		fileMenu.addSeparator();
		fileMenu.add(actionCache.get("close"));
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
	 * Creates and configures the tool bar.
	 * @return the tool bar
	 */
	private JToolBar createToolBar() {
		
		JToolBar toolBar = new JToolBar();
		toolBar.add(actionCache.get("new"));
		toolBar.add(actionCache.get("open"));
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
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;

					// grab UPK file from cache
					UpkFile upkFile = upkCache.get(file);
					if (upkFile == null) {
						// if cache doesn't contain UPK file instantiate a new one
						upkFile = new UpkFile(file);
					}

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
					upkTtf.setText(hasUpk ? upkFile.getFile().getPath() : "no UPK file selected");
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
		
		Handler logHandler = new LogHandler(loggingEditor,
				ModTree.logger, ModTab.logger, ReferenceUpdate.logger);
		
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
		actionCache.get("close").setEnabled(enabled);
		actionCache.get("closeAll").setEnabled(enabled);
		actionCache.get("save").setEnabled(enabled);
		actionCache.get("saveAs").setEnabled(enabled);
	}
	
	/**
	 * Convenience method to set the enable state of the 'Update
	 * References...', 'Apply Hex Changes' and 'Revert Hex Changes' menu
	 * items.
	 * @param enabled the enable state
	 */
	protected void setEditActionsEnabled(boolean enabled) {
		actionCache.get("refUpdate").setEnabled(enabled);
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
	 * Container class for multiple projects to display in project pane.
	 */
	private class ProjectPaneTree extends ProjectTreeMdl {

		private final List<ProjectTreeMdl> projectRoot;

		/**
		 * Construct an empty list with no projects.
		 */
		public ProjectPaneTree() {
			this.projectRoot = new ArrayList<>();
		}
		
		/**
		 * Construct a list initially populated with the specified project.
		 * @param project modproject xml file
		 */
		public ProjectPaneTree(File project) {
			this.projectRoot = new ArrayList<>();
			this.projectRoot.add(new ProjectTreeMdl(project));
		}

		/**
		 * Adds an existing project to the list.
		 * @param project File link to the project xml file
		 */
		public void addProject(File project) {
			projectRoot.add(new ProjectTreeMdl(project));
		}

		/**
		 * Creates a new project directory and xml file at the specified directory.
		 * @param directory File link to the directory to create the new project at
		 */
		public void createNewProject(File directory) {
			// TODO: implement
		}
		
		/**
		 * Removes the designated project.
		 * @param i project index to remove.
		 */
		public void removeProjectAt(int i) {
			if(i >= 0 && i < this.projectRoot.size()) {
				this.projectRoot.remove(i);
			}
		}
		
		@Override
		public Object getRoot() {
			return this.projectRoot;
		}

		@Override
		public Object getChild(Object o, int i) {
			if (o.equals(this.projectRoot)) { // getting project
				return this.projectRoot.get(i);
			} else {
				return super.getChild(o, i);
			}
		}

		@Override
		public int getChildCount(Object o) {
			if(o.equals(this.projectRoot)) {
				return this.projectRoot.size();
			} else {
				return super.getChildCount(o);
			}
		}

		@Override
		public boolean isLeaf(Object o) {
			if(o.equals(this.projectRoot)) {
				return this.projectRoot.isEmpty();
			} else {
				return super.isLeaf(o);
			}
		}

		@Override
		public int getIndexOfChild(Object o, Object o1) {
			if(o.equals(this.projectRoot)) {
				return this.projectRoot.indexOf(o1);
			} else {
				return super.getIndexOfChild(o, o1);
			}
		}
	}
	
}
