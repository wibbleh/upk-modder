package ui;

import io.parser.OperandTableParser;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import model.modtree.ModOperandNode;
import model.modtree.ModTree;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;

import ui.dialogs.ReferenceUpdateDialog;
import util.unrealhex.HexSearchAndReplace;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The application's primary frame.
 * 
 * @author XMS
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
	 * The cache of shared UPK files.
	 */
	private Map<File, UpkFile> upkCache = new HashMap<>();
	
	/**
	 * Constructs the application's main frame.
	 * @param title the title string appearing in the frame's title bar
	 */
	private MainFrame(String title) {
		// instantiate frame
		// TODO: add application icon
		super(title);
		
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
	 * Creates and lays out the frame's components.
	 * @throws Exception if an I/O error occurs
	 */
	private void initComponents() {
		// create and install menu bar
		this.setJMenuBar(new MainMenuBar());
		
		// configure content pane layout
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new FormLayout("1000px:g", "f:600px:g, b:p"));
		
		UIManager.put("TabbedPane:TabbedPaneTabArea.contentMargins", new InsetsUIResource(3, 0, 4, 0));
		UIManager.put("TabbedPane:TabbedPaneTab.contentMargins", new InsetsUIResource(2, 8, 3, 3));
		tabPane = new ButtonTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		JPanel statusBar = this.createStatusBar();
		
		contentPane.add(tabPane, CC.xy(1, 1));
		contentPane.add(statusBar, CC.xy(1, 2));
	}
	
	/**
	 * Creates and configures the status bar.
	 * @return the status bar
	 */
	private JPanel createStatusBar() {
		JPanel statusBar = new JPanel(new FormLayout("0px:g(0.5), 0px:g(0.25), 0px:g(0.25)", "f:p"));
		Color bgCol = new Color(214, 217, 223);
		
		//TODO: move variable declarations? 
		//Amineri: I had to move them earlier to get apply/revert button working
		final JButton updateBtn = new JButton();
		final JButton updateTestBtn = new JButton();
		final JTextField updateTtf = new JTextField("no file loaded");
		
		JPanel upkPnl = new JPanel(new BorderLayout());
		
		final JTextField upkTtf = new JTextField("no modfile loaded");
		upkTtf.setEditable(false);
		upkTtf.setBackground(bgCol);
		
		final JButton upkBtn = new JButton();
		Icon normalIcon = UIManager.getIcon("FileView.directoryIcon");
		
		// create lighter and darker versions of icon
		BufferedImage normalImg = new BufferedImage(
				normalIcon.getIconWidth(), normalIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = normalImg.createGraphics();
		normalIcon.paintIcon(null, g, 0, 0);
		g.dispose();
		Icon rolloverIcon = new ImageIcon(new RescaleOp(
				new float[] { 1.1f, 1.1f, 1.1f, 1.0f }, new float[4], null).filter(normalImg, null));
		Icon pressedIcon = new ImageIcon(new RescaleOp(
				new float[] { 0.8f, 0.8f, 0.8f, 1.0f }, new float[4], null).filter(normalImg, null));

		upkBtn.setIcon(normalIcon);
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
						// enable 'update', 'apply' and 'revert' menu items
						MainMenuBar mainMenu = (MainMenuBar) MainFrame.this.getJMenuBar();
						mainMenu.setEditItemsEnabled(true);
						updateBtn.setEnabled(true);
						updateTestBtn.setEnabled(true);
						
						// test initial mod install status in upk and update button/message
						tab.setUpdateStatus(true);
						updateBtn.setEnabled(tab.modCanBeApplied());
						updateTtf.setText(tab.getUpdateMessage());
						updateTtf.setFont(tab.getUpdateFont());
						updateTtf.setBackground(tab.getUpdateBackgroundColor());
					} else {
						// TODO: show error/warning message
					}
				}
			}
		});
		
		upkPnl.setBorder(upkTtf.getBorder());
		upkTtf.setBorder(null);
		
		upkPnl.add(upkTtf, BorderLayout.CENTER);
		upkPnl.add(upkBtn, BorderLayout.EAST);
		
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
					// enable/disable 'update', 'apply' and 'revert' menu items
					MainMenuBar mainMenu = (MainMenuBar) MainFrame.this.getJMenuBar();
					mainMenu.setEditItemsEnabled(hasUpk);
					// enable UPK selection button
					upkBtn.setEnabled(true);
				} else {
					// last tab has been removed, reset to defaults
					upkTtf.setText("no modfile loaded");
					MainMenuBar mainMenu = (MainMenuBar) MainFrame.this.getJMenuBar();
					mainMenu.setEditItemsEnabled(false);
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
		

		// Create update status panel
		JPanel updatePnl = new JPanel(new BorderLayout());
//		final JTextField updateTtf = new JTextField("no file loaded"); // declaration moved to head of class
		updateTtf.setEditable(false);
		updateTtf.setBackground(bgCol);

//		final JButton updateBtn = new JButton();   // declaration moved to head of class
		// TODO: new icon / prettify
		Icon updateIcon = UIManager.getIcon("OptionPane.errorIcon");
		
		updateBtn.setIcon(updateIcon);
		updateBtn.setBorder(null);
		updateBtn.setEnabled(false);
		
		// add ActionListener to update button to apply/revert changes and update status
		updateBtn.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e)
            {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;

					if(tab.modIsApplied()) {
						// call to revert
						tab.revertChanges();
					} else {
						// call to apply
						tab.applyChanges();
					}
					updateBtn.setEnabled(tab.modCanBeApplied());
					updateTtf.setText(tab.getUpdateMessage());
					updateTtf.setFont(tab.getUpdateFont());
					updateTtf.setBackground(tab.getUpdateBackgroundColor());
				} else {
					// TODO: show error/warning message
				}
            }
        });      

		//TODO: needs new button/resizing/coloring
		Icon testIcon = UIManager.getIcon("OptionPane.questionIcon");
		updateTestBtn.setIcon(testIcon);
		updateTestBtn.setBorder(null);
		updateTestBtn.setEnabled(false);
		
		// add ActionListener to update button to test file for install to upk and update status
		updateTestBtn.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e)
            {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					
					// run tests
					tab.setUpdateStatus(false);
					
					// enable/disable update button
					updateBtn.setEnabled(tab.modCanBeApplied());
					
					// refresh update status message
					updateTtf.setText(tab.getUpdateMessage());
					updateTtf.setFont(tab.getUpdateFont());
					updateTtf.setBackground(tab.getUpdateBackgroundColor());
				} else {
					// TODO: show error/warning message
				}
            }
        });      
		
		
//		updateTtf = new JTextField("Generic update message"); // moved to head of class
		updateTtf.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		updateTtf.setEditable(false);
		updateTtf.setBackground(bgCol);
		
		updatePnl.setBorder(updateTtf.getBorder());
		updateTtf.setBorder(null);
		
		updatePnl.add(updateTestBtn, BorderLayout.WEST);
		updatePnl.add(updateTtf, BorderLayout.CENTER);
		updatePnl.add(updateBtn, BorderLayout.EAST);
		
		// install listener on tabbed pane to report update panel changes based on tab
		tabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					
					updateTtf.setText(tab.getUpdateMessage());
					updateTtf.setFont(tab.getUpdateFont());
					updateTtf.setBackground(tab.getUpdateBackgroundColor());
					updateBtn.setEnabled((tab.getUpkFile()!= null) && tab.modCanBeApplied());
				} else {
					// last tab has been removed, reset to defaults
					updateTtf.setText("no modfile loaded");
					MainMenuBar mainMenu = (MainMenuBar) MainFrame.this.getJMenuBar();
					mainMenu.setEditItemsEnabled(false);
					updateBtn.setEnabled(false);
				}
			}
		});
				
		statusBar.add(upkPnl, CC.xy(1, 1));
		statusBar.add(progressBar, CC.xy(2, 1));
		statusBar.add(updatePnl, CC.xy(3, 1));
		
		return statusBar;
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
	 * The menu bar for the application's main frame.
	 * @author XMS
	 */
	private class MainMenuBar extends JMenuBar {
		
		/** The 'Save' menu item. */
		private JMenuItem saveItem;
		/** The 'Save As...' menu item. */
		private JMenuItem saveAsItem;
		/** The 'Close' menu item. */
		private JMenuItem closeItem;
		/** The 'Close All' menu item. */
		private JMenuItem closeAllItem;
		/** The 'Update References...' menu item. */
		private JMenuItem refUpdateItem;
		/** The 'Apply Hex Changes...' menu item */
		private JMenuItem applyItem;
		/** The 'Revert Hex Changes...' menu item */
		private JMenuItem revertItem;
		
		/**
		 * Creates and initializes the main menu bar.
		 */
		public MainMenuBar() {
			super();
			
			this.initComponents();
		}

		private void initComponents() {
			// create file menu
			JMenu fileMenu = new JMenu("File");
			
			// create file menu items
			JMenuItem newItem = new JMenuItem("New", UIManager.getIcon("FileView.fileIcon"));
			newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
			newItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ModTab tab = new ModTab();
					tabPane.addTab("New File", tab);
					tabPane.setSelectedComponent(tab);

					setFileItemsEnabled(true);
				}
			});
			
			JMenuItem openItem = new JMenuItem("Open File...", UIManager.getIcon("FileView.directoryIcon"));
			openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
			openItem.addActionListener(new BrowseActionListener(this, Constants.MOD_FILE_FILTER) {
				@Override
				protected void execute(File file) {
					try {
						ModTab tab = new ModTab(file);
						tabPane.addTab(file.getName(), tab);
						tabPane.setSelectedComponent(tab);
						
						setFileItemsEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			closeItem = new JMenuItem("Close");
			closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
			closeItem.setEnabled(false);
			closeItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					tabPane.remove(tabPane.getSelectedComponent());
				}
			});
			
			closeAllItem = new JMenuItem("Close All");
			closeAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
			closeAllItem.setEnabled(false);
			closeAllItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					tabPane.removeAll();
				}
			});
			
			saveItem = new JMenuItem("Save", UIManager.getIcon("FileView.floppyDriveIcon"));
			saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
			saveItem.setEnabled(false);
			saveItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					Component selComp = tabPane.getSelectedComponent();
					if (selComp != null) {
						ModTab tab = (ModTab) selComp;
						File file = tab.getModFile();
						if (file != null) {
							tab.saveFile();
						} else {
							saveAsItem.doClick();
						}
					}
				}
			});
			
			saveAsItem = new JMenuItem("Save As...", UIManager.getIcon("FileView.floppyDriveIcon"));
			saveAsItem.setEnabled(false);
			saveAsItem.addActionListener(new BrowseActionListener(this, Constants.MOD_FILE_FILTER, true) {
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
			});
			
			// TODO: maybe add 'Save All' item
			
			JMenuItem exportItem = new JMenuItem("Export...");
			exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
			exportItem.setEnabled(false);
			
			JMenuItem exitItem = new JMenuItem("Exit");
			exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					// dispose main frame (thereby terminating the application)
					MainFrame.this.dispose();
				}
			});
			
			// add items to file menu
			fileMenu.add(newItem);
			fileMenu.add(openItem);
			fileMenu.addSeparator();
			fileMenu.add(closeItem);
			fileMenu.add(closeAllItem);
			fileMenu.addSeparator();
			fileMenu.add(saveItem);
			fileMenu.add(saveAsItem);
			fileMenu.addSeparator();
			fileMenu.add(exportItem);
			fileMenu.addSeparator();
			fileMenu.add(exitItem);
			
			// create edit menu
			JMenu editMenu = new JMenu("Edit");
			
			refUpdateItem = new JMenuItem("Update References...", UIManager.getIcon("FileChooser.detailsViewIcon"));
			refUpdateItem.setEnabled(false);
			refUpdateItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					Component selComp = tabPane.getSelectedComponent();
					if (selComp != null) {
						showRefUpdateDialog(((ModTab) selComp).getTree());
					}
				}
			});
			
			applyItem = new JMenuItem("Apply Hex Changes");
			applyItem.setEnabled(false);
			applyItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					Component selComp = tabPane.getSelectedComponent();
					if (selComp != null) {
						ModTab tab = (ModTab) selComp;
						tab.applyChanges();
					}
				}
			});
			
			revertItem = new JMenuItem("Revert Hex Changes");
			revertItem.setEnabled(false);
			revertItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					Component selComp = tabPane.getSelectedComponent();
					if (selComp != null) {
						ModTab tab = (ModTab) selComp;
						tab.revertChanges();
					}
				}
			});
			
			// add items to edit menu
			editMenu.add(refUpdateItem);
			editMenu.addSeparator();
			// TODO: create menu commands with keybinds to match buttons (update/test)
//			editMenu.add(applyItem);
//			editMenu.add(revertItem);
			
			// create help menu
			JMenu helpMenu = new JMenu("Help");

			// create help and about icons
			BufferedImage helpImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = helpImg.createGraphics();
			((AbstractRegionPainter) UIManager.get("OptionPane[Enabled].questionIconPainter")).paint(
					g2, null, 16, 16);
			g2.dispose();
			BufferedImage aboutImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			g2 = aboutImg.createGraphics();
			((AbstractRegionPainter) UIManager.get("OptionPane[Enabled].informationIconPainter")).paint(
					g2, null, 16, 16);
			g2.dispose();
			
			// create help menu items
			JMenuItem helpItem = new JMenuItem("Help", new ImageIcon(helpImg));
			helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
			helpItem.setEnabled(false);
			
			JMenuItem aboutItem = new JMenuItem("About", new ImageIcon(aboutImg));
			aboutItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					// TODO: make dialog use same icon as menu item
					showAboutDialog();
				}
			});
			
			// add items to help menu
			helpMenu.add(helpItem);
			helpMenu.addSeparator();
			helpMenu.add(aboutItem);
			
			// add menus to menu bar
			this.add(fileMenu);
			this.add(editMenu);
			this.add(helpMenu);
		}
		
		/**
		 * Convenience method to set the enable state of the 'Save', 'Save As',
		 * 'Close' and 'Close All' menu items.
		 * @param enabled the enable state
		 */
		public void setFileItemsEnabled(boolean enabled) {
			this.saveItem.setEnabled(enabled);
			this.saveAsItem.setEnabled(enabled);
			this.closeItem.setEnabled(enabled);
			this.closeAllItem.setEnabled(enabled);
		}
		
		/**
		 * Convenience method to set the enable state of the 'Update
		 * References...', 'Apply Hex Changes' and 'Revert Hex Changes' menu
		 * items.
		 * @param enabled the enable state
		 */
		public void setEditItemsEnabled(boolean enabled) {
			this.refUpdateItem.setEnabled(enabled);
			this.applyItem.setEnabled(enabled);
			this.revertItem.setEnabled(enabled);
		}
		
		
	}
	
	/**
	 * The basic component inside the tabbed pane.
	 * @author XMS
	 */
	private class ModTab extends JSplitPane {

		/**
		 * The modfile editor instance.
		 */
		private JEditorPane modEditor;

		/**
		 * The modfile tree structure.
		 */
		private ModTree modTree;

		/**
		 * The modfile associated with this tab.
		 */
		private File modFile;
		
		// TODO: consolidate message, font, color into class?
		/**
		 * The current update message.
		 */
		private String updateMessage = "no modfile loaded";
		
		/**
		 * The current update message font.
		 */
		private Font updateFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		
		/**
		 * The current update background color.
		 */
		private Color updateBGColor  = new Color(214, 217, 223);

		/**
		 * Flag indicating whether the mod is applied or not.
		 */
		private boolean modIsApplied = false;
		
		/**
		 * Flag indicating whether the mod can be applied or not (if errors or not).
		 */
		private boolean modCanBeApplied = false;

		/**
		 * The UPK file associated with this tab.
		 */
		// is now reflected from/stored in the modTree to enable ref name display in tree view
//		private UpkFile upkFile;  

		/**
		 * Creates a new tab with an empty editor.
		 */
		public ModTab() {
			this(null);
		}

		/**
		 * Creates a new tab from the specified modfile reference.
		 * @param modFile the modfile to parse
		 */
		public ModTab(File modFile) {
			super(JSplitPane.HORIZONTAL_SPLIT);
			this.modFile = modFile;
			
			try {
				this.initComponents();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Creates and lays out the components of the tab.
		 * @param modFile
		 */
		private void initComponents() throws Exception {
			// create right-hand editor pane
			modEditor = new JEditorPane();
			modEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
	
			// install editor kit
			modEditor.setEditorKit(new StyledEditorKit() {
				@Override
				public ViewFactory getViewFactory() {
					return new ViewFactory() {
				        public View create(Element elem) {
				            String kind = elem.getName();
				            if (kind != null) {
				                if (kind.equals(AbstractDocument.ContentElementName)) {
				                    return new LabelView(elem);
				                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
				                	return new ParagraphView(elem) {
				                    	/* hack to prevent line wrapping */
				                    	@Override
										public void layout(int width, int height) {
											super.layout(Short.MAX_VALUE, height);
										}
				                    	@Override
										public float getMinimumSpan(int axis) {
											return super.getPreferredSpan(axis);
										}
				                    };
				                } else if (kind.equals(AbstractDocument.SectionElementName)) {
				                    return new BoxView(elem, View.Y_AXIS);
				                } else if (kind.equals(StyleConstants.ComponentElementName)) {
				                    return new ComponentView(elem);
				                } else if (kind.equals(StyleConstants.IconElementName)) {
				                    return new IconView(elem);
				                }
				            }
				            // default to text display
				            return new LabelView(elem);
				        }
				    };
				}
			});
			
			// read provided file, if possible
			if (modFile != null) {
				modEditor.read(new FileInputStream(modFile), modFile);
			}
	
			// wrap editor in scroll pane
			JScrollPane modEditorScpn = new JScrollPane(modEditor,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			modEditorScpn.setRowHeaderView(new LineNumberMargin(modEditor));
			modEditorScpn.setPreferredSize(new Dimension(350, 600));
			
			Document modDocument = modEditor.getDocument();
			modDocument.putProperty(PlainDocument.tabSizeAttribute, 4);
	
			// create tree view of right-hand mod editor
			modTree = new ModTree(modDocument);
			final JTree modElemTree = new JTree(modTree.getRoot()); // draw from ModTree
			JScrollPane modElemTreeScpn = new JScrollPane(modElemTree,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			modElemTreeScpn.setPreferredSize(new Dimension(350, 600));
			
			// configure look and feel of tree view
			modElemTree.setRootVisible(false);
			modElemTree.setShowsRootHandles(false);
			modElemTree.putClientProperty("JTree.lineStyle", "Angled");
			
			// display alternate operand text info for opened ModOperandNodes
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
				@Override
				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean sel, boolean expanded, boolean leaf,
						int row, boolean hasFocus) {
					Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded,
							leaf, row, hasFocus);
					if (value instanceof ModOperandNode) {
						((ModOperandNode) value).expanded = expanded;
						if (expanded) {
							// TODO: do something to comp here?
						}
					}
					comp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
					return comp;
				}
			};
			renderer.setLeafIcon(null);
			renderer.setClosedIcon(null);
			renderer.setOpenIcon(null);
			modElemTree.setCellRenderer(renderer);
				
			// install document listener to refresh tree on changes to the document
			modDocument.addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent evt) {
					this.updateTree(evt);
				}
				@Override
				public void insertUpdate(DocumentEvent evt) {
					this.updateTree(evt);
				}
				@Override
				public void changedUpdate(DocumentEvent evt) {
					this.updateTree(evt);
				}
				/** Updates the tree views on document changes */
				private void updateTree(DocumentEvent evt) {
					// reset mod tree
					((DefaultTreeModel) modElemTree.getModel()).setRoot(
							modTree.getRoot());
	
//					// expand tree
//					for (int i = 0; i < modElemTree.getRowCount(); i++) {
//						modElemTree.expandRow(i);
//					}
				}
			});
	
//			// expand tree
//			for (int i = 0; i < modElemTree.getRowCount(); i++) {
//				modElemTree.expandRow(i);
//			}
			
			// wrap tree and editor in split pane
			this.setLeftComponent(modElemTreeScpn);
			this.setRightComponent(modEditorScpn);
			this.setOneTouchExpandable(true);
			// by default hide the tree view
			this.setDividerLocation(0.0);
		}

		/**
		 * Saves the editor's contents to the file associated with this tab.
		 */
		public void saveFile() {
			try {
				this.getEditor().write(new OutputStreamWriter(new FileOutputStream(this.modFile)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Searches the associated UPK file for the byte data of the <code>BEFORE</code>
		 * block(s) and overwrites it using the byte data of the <code>AFTER</code> block(s).
		 * @XTMS -- the key here is that there can be multiple non-adjacent before/after blocks
		 * see AIAddNewObjectives@XGStrategyAI.upk_mod in the sample project
		 *      -- a few lines at the end of the function are changed, as well as the header
		 */
		public void applyChanges() {
			try {
				if(this.searchAndReplace(
						HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()),
						HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()))
						) {
					this.setUpdateMessage("AFTER Hex Installed");
					this.setUpdateBackgroundColor(new Color(255, 255, 0));
					this.modIsApplied = true;
				};
			} catch(IOException ex) {
				Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
				this.setUpdateMessage("File error " + ex);
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
			}
		}

		/**
		 * Searches the associated UPK file for the byte data of the <code>AFTER</code>
		 * block(s) and overwrites it using the byte data of the <code>BEFORE</code> block(s).
		 */
		public void revertChanges() {
			try {
				if(this.searchAndReplace(
						HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()),
						HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()))
						) {
					this.setUpdateMessage("BEFORE Hex Installed");
					this.setUpdateBackgroundColor(new Color(128, 255, 128));
					this.modIsApplied = false;
				}
			} catch(IOException ex) {
				Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
				this.setUpdateMessage("File error " + ex);
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
			}
		}
		
		/**
		 * Searches the associated UPK file for the provided byte pattern and
		 * overwrites it using the provided replacement bytes.
		 * @param patterns the byte pattern to search for
		 * @param replacements the bytes to replace the search pattern with
		 * @Return true if S&R was successful, false otherwise
		 */
		private boolean searchAndReplace(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
			// perform error checking first
			long[] filePositions = testBeforeAndAfterBlocks(patterns, replacements);
			if(filePositions == null) {
				return false;
			}

			// everything matches, time to make the change(s)
			for(int i = 0 ; i < filePositions.length; i++) {
				HexSearchAndReplace.applyHexChange(replacements.get(i), this.getUpkFile(), filePositions[i]);
			}
			return true;
		}
		
		public void setUpdateStatus(boolean checkBothDirections) {
			if(this.modTree == null) {
				this.setUpdateMessage("No file data");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modIsApplied = false;
				this.modCanBeApplied = false;
				return;
			}
			if(this.getUpkFile() == null) {
				this.setUpdateMessage("No upk present");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modIsApplied = false;
				this.modCanBeApplied = false;
				return;
			}
			List<byte[]> beforeHex = HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile());
			if(beforeHex.isEmpty()) {
				this.setUpdateMessage("No/empty BEFORE Blocks");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modIsApplied = false;
				this.modCanBeApplied = false;
				return;
			}
			List<byte[]> afterHex =	HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile());
			if(afterHex.isEmpty()) {
				this.setUpdateMessage("No/empty AFTER Blocks");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modIsApplied = false;
				this.modCanBeApplied = false;
				return;
			}
			try {
				if(checkBothDirections) {
					if (testBeforeAndAfterBlocks(beforeHex, afterHex) != null) {
						this.setUpdateMessage("BEFORE Hex Installed");
						this.setUpdateBackgroundColor(new Color(128, 255, 128));
						this.modIsApplied = false;
						this.modCanBeApplied = true;
					} else if (testBeforeAndAfterBlocks(afterHex, beforeHex) != null) {
						this.setUpdateMessage("AFTER Hex Installed");
						this.setUpdateBackgroundColor(new Color(128, 255, 128));
						this.modIsApplied = true;
						this.modCanBeApplied = true;
					} else {
						modIsApplied = false;
					}
				} else { // check only based on the current status
					if(this.modIsApplied) {
						if (testBeforeAndAfterBlocks(afterHex, beforeHex) != null) {
							this.setUpdateMessage("AFTER Hex Installed");
							this.setUpdateBackgroundColor(new Color(128, 255, 128));
							this.modIsApplied = true;
							this.modCanBeApplied = true;
						}						
					} else {
						if (testBeforeAndAfterBlocks(beforeHex, afterHex) != null) {
							this.setUpdateMessage("BEFORE Hex Installed");
							this.setUpdateBackgroundColor(new Color(128, 255, 128));
							this.modIsApplied = false;
							this.modCanBeApplied = true;
						}						
					}
				}
				
			} catch(IOException ex) {
				Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
				this.setUpdateMessage("File error " + ex);
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
			}
		}
		
		private long[] testBeforeAndAfterBlocks(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
			// perform simple error checking first
			// check for same number of blocks
			if(patterns.size() != replacements.size()) {
				this.setUpdateMessage("Block count mismatch");
				this.setUpdateBackgroundColor(new Color(255, 128, 128));
				this.modCanBeApplied = false;
				return null;
			}
			// check each block has same number of bytes
			long[] filePositions = new long[patterns.size()];
			for (int i = 0; i < patterns.size() ; i++) {
				if(patterns.get(i).length != replacements.get(i).length) {
					this.setUpdateMessage("Block " + i + " bytecount mismatch");
					this.setUpdateBackgroundColor(new Color(255, 128, 128));
					this.modCanBeApplied = false;
					return null;
				}
			}
			// try and find each pattern blocks position
			for(int j = 0; j < patterns.size() ; j ++) {
				long filePos = HexSearchAndReplace.findFilePosition(patterns.get(j), this.getUpkFile(), this.modTree);
				if(filePos == -1) {
					this.setUpdateMessage("Block " + j + " not found");
					this.setUpdateBackgroundColor(new Color(255, 128, 128));
					this.modCanBeApplied = false;
					return null;
				} else {
					filePositions[j]= filePos;
				}
			}
			return filePositions;
		}
		
		/**
		 * Returns the modfile editor instance of this tab.
		 * @return the editor
		 */
		public JEditorPane getEditor() {
			return modEditor;
		}

		/**
		 * Returns the <code>ModTree</code> instance of this tab.
		 * @return the <code>ModTree</code>
		 */
		public ModTree getTree() {
			return modTree;
		}

		/**
		 * Returns the file associated with this tab.
		 * @return the file
		 */
		public File getModFile() {
			return modFile;
		}
		
		/**
		 * Sets the modfile associated with this tab.
		 * @param modFile the modfile to set
		 */
		public void setModFile(File modFile) {
			this.modFile = modFile;
		}
		
		/**
		 * Returns the UPK file associated with this tab.
		 * @return the UPK file
		 */
		public UpkFile getUpkFile() {
			return this.modTree.getSourceUpk();
//			return upkFile;
		}

		/**
		 * Sets the UPK file associated with this tab.
		 * @param upkFile the upk file to set
		 */
		public void setUpkFile(UpkFile upkFile) {
			this.modTree.setSourceUpk(upkFile);
//			this.upkFile = upkFile;
		}

		private String getUpdateMessage() {
			return this.updateMessage;
		}

		private Font getUpdateFont() {
			return this.updateFont;
		}

		private Color getUpdateBackgroundColor() {
			return this.updateBGColor;
		}
		
		private void setUpdateMessage(String msg) {
			this.updateMessage = msg;
		}

		private void setUpdateFont(Font font) {
			this.updateFont = font;
		}

		private void setUpdateBackgroundColor(Color color) {
			this.updateBGColor = color;
		}

		public boolean modIsApplied() {
			return this.modIsApplied;
		}
		
		public boolean modCanBeApplied() {
			return this.modCanBeApplied;
		}
		
	}
	
	/**
	 * Custom tabbed pane featuring a 'Close' button in its tabs.
	 * @author XMS
	 */
	private class ButtonTabbedPane extends JTabbedPane {
		
		// TODO: focus traversal on tabs is dodgy, investigate

		/**
		 * Creates a tabbed pane featuring a 'Close' button in its tabs.
		 * @param tabPlacement the placement for the tabs relative to the content
		 * @param tabLayoutPolicy the policy for laying out tabs when all tabs will not fit on one run
		 */
		private ButtonTabbedPane(int tabPlacement, int tabLayoutPolicy) {
			super(tabPlacement, tabLayoutPolicy);
		}
		
		@Override
		public void addTab(String title, Component component) {
			super.addTab(title, component);
			// add 'Close' button to new tab
			this.setTabComponentAt(this.getTabCount() - 1, new ButtonTabComponent());
		}
		
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
			
			// if last tab has been removed disable 'save' and 'close' menu items
			if (this.getTabCount() == 0) {
				MainMenuBar mainMenu = (MainMenuBar) MainFrame.this.getJMenuBar();
				mainMenu.setFileItemsEnabled(false);
			}
		}
		
		/**
		 * Component to be used inside tabbed pane tabs.<br>
		 * Based on a <a href="http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TabComponentsDemoProject/src/components/ButtonTabComponent.java">Java Tutorials Code Sample</a>
		 * @author XMS
		 */
		private class ButtonTabComponent extends JPanel {
		 
		    /**
		     * Creates a panel containing the tab title and a 'Close' button
		     * @param tabPane the reference to the parent tabbed pane
		     */
		    public ButtonTabComponent() {
		        super(new BorderLayout(5, 0));

				// make component transparent
				this.setOpaque(false);
				
				// create label, make it display its corresponding tab title as text
				JLabel label = new JLabel() {
					public String getText() {
						int index = tabPane.indexOfTabComponent
								(ButtonTabComponent.this);
						if (index != -1) {
							return tabPane.getTitleAt(index);
						}
						return null;
					}
				};

				this.add(label, BorderLayout.CENTER);
				TabButton tabButton = new TabButton();
				
				JToolBar tb = new JToolBar();
				tb.add(tabButton);
				
				this.add(tabButton, BorderLayout.EAST);
			}

		    /**
		     * 'Close' button for tabs.
		     * @author XMS
		     */
			private class TabButton extends JButton implements ActionListener {
				
				/**
				 * Constructs a 'Close' button.
				 */
				public TabButton() {
					int size = 17;
					this.setPreferredSize(new Dimension(size, size));

					// configure visuals
					this.setToolTipText("Close this document");
					this.setFocusable(false);
					
					// install action listener to close tab on click
					this.addActionListener(this);
				}

				@Override
				public void actionPerformed(ActionEvent evt) {
					int i = tabPane.indexOfTabComponent(ButtonTabComponent.this);
					if (i != -1) {
						tabPane.remove(i);
					}
				}

				// paint the cross
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setStroke(new BasicStroke(2));
					g2.setColor(Color.DARK_GRAY);
					int delta = 6;
					g2.drawLine(delta, delta,
							getWidth() - delta, getHeight() - delta);
					g2.drawLine(getWidth() - delta, delta,
							delta, getHeight() - delta);
					g2.dispose();
				}
				
			}
			
		}

	}
	
}
