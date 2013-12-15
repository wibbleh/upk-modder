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

import org.bounce.text.LineNumberMargin;

import ui.dialogs.ReferenceUpdateDialog;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

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
		
		UIManager.getDefaults().put("TabbedPane:TabbedPaneTabArea.contentMargins", new InsetsUIResource(0, 0, 0, 0));
		UIManager.getDefaults().put("TabbedPane:TabbedPaneTab.contentMargins", new InsetsUIResource(2, 8, 3, 3));
		tabPane = new ButtonTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
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
			protected void execute(File upkFile) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					tab.setUpkFile(upkFile);
					upkTtf.setText((upkFile != null) ? upkFile.getPath() : "no UPK file selected");
				}
			}
		});
		
		upkPnl.setBorder(upkTtf.getBorder());
		upkTtf.setBorder(null);
		
		upkPnl.add(upkTtf, BorderLayout.CENTER);
		upkPnl.add(upkBtn, BorderLayout.EAST);
		
		tabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				Component selComp = tabPane.getSelectedComponent();
				if (selComp != null) {
					ModTab tab = (ModTab) selComp;
					File upkFile = tab.getUpkFile();
					upkTtf.setText((upkFile != null) ? upkFile.getPath() : "no UPK file selected");
					upkBtn.setEnabled(true);
				} else {
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
		
		JTextField statusTtf = new JTextField("Generic status message");
		statusTtf.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		statusTtf.setEditable(false);
		statusTtf.setBackground(bgCol);
		
		statusBar.add(upkPnl, CC.xy(1, 1));
		statusBar.add(progressBar, CC.xy(2, 1));
		statusBar.add(statusTtf, CC.xy(3, 1));
		
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
		
		/**
		 * The 'Save' menu item.
		 */
		private JMenuItem saveItem;
		
		/**
		 * The 'Save As...' menu item.
		 */
		private JMenuItem saveAsItem;
		
		/**
		 * The 'Update References...' menu item.
		 */
		private JMenuItem refUpdateItem;
		
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
			// TODO: add icons
			JMenuItem newItem = new JMenuItem("New", UIManager.getIcon("FileView.fileIcon"));
			newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
			newItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ModTab tab = new ModTab();
					tabPane.addTab("New File", tab);
					tabPane.setSelectedComponent(tab);
					
					saveItem.setEnabled(true);
					saveAsItem.setEnabled(true);
					refUpdateItem.setEnabled(true);
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
						
						saveItem.setEnabled(true);
						saveAsItem.setEnabled(true);
						refUpdateItem.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
			
			// add items to edit menu
			editMenu.add(refUpdateItem);
			
			// create help menu
			JMenu helpMenu = new JMenu("Help");
			
			// create help menu items
			JMenuItem helpItem = new JMenuItem("Help");
			helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
			helpItem.setEnabled(false);
			
			JMenuItem aboutItem = new JMenuItem("About");
			aboutItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
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
		 * Returns the 'Save' menu item.
		 * @return the 'Save' menu item
		 */
		public JMenuItem getSaveItem() {
			return this.saveItem;
		}
		
		/**
		 * Returns the 'Save As...' menu item.
		 * @return the 'Save As...' menu item
		 */
		public JMenuItem getSaveAsItem() {
			return this.saveAsItem;
		}

		/**
		 * Returns the 'Update References...' menu item
		 * @return the 'Update References...' menu item
		 */
		public Component getRefUpdateItem() {
			return this.refUpdateItem;
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
		
		/**
		 * The UPK file associated with this tab.
		 */
		private File upkFile;

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
		public File getUpkFile() {
			return upkFile;
		}

		/**
		 * Sets the UPK file associated with this tab.
		 * @param upkFile the upk file to set
		 */
		public void setUpkFile(File upkFile) {
			this.upkFile = upkFile;
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
			this.setTabComponentAt(this.getTabCount() - 1, new ButtonTabComponent());
		}
		
		@Override
		public void remove(Component component) {
			// TODO Auto-generated method stub
			super.remove(component);
		}
		
		@Override
		public void removeTabAt(int index) {
			super.removeTabAt(index);
			if (this.getTabCount() == 0) {
				MainMenuBar mainMenu = (MainMenuBar) MainFrame.this.getJMenuBar();
				mainMenu.getSaveItem().setEnabled(false);
				mainMenu.getSaveAsItem().setEnabled(false);
				mainMenu.getRefUpdateItem().setEnabled(false);
			}
		}
		
		@Override
		public void setSelectedComponent(Component c) {
			// TODO Auto-generated method stub
			super.setSelectedComponent(c);
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
