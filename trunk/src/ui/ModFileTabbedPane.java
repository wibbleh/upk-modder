package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeCellRenderer;

import model.modtree.ModGenericLeaf;
import model.modtree.ModOffsetLeaf;
import model.modtree.ModOperandNode;
import model.modtree.ModReferenceLeaf;
import model.modtree.ModStringLeaf;
import model.modtree.ModTree;
import model.modtree.ModTreeNode;
import model.upk.UpkFile;

import org.bounce.text.LineNumberMargin;
import static ui.Constants.*;

import ui.dialogs.ReferenceUpdateDialog;
import util.properties.UpkModderProperties;
import util.unrealhex.HexSearchAndReplace;
import static util.unrealhex.HexSearchAndReplace.testFileStatus;

/**
 * Tabbed pane implementation for the application's mod file editor.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ModFileTabbedPane extends ButtonTabbedPane {
	
	/**
	 * The logger.
	 */
	public static final Logger logger = Logger.getLogger(ModFileTabbedPane.class.getName());

	/**
	 * Set of mappings from ModFile File to ModFileTab
	 * Used to prevent opening of duplicate tabs and to lookup tabs based on filename
	 */
	private Map<String, ModFileTab> filenameToTabMap = new HashMap<>();
	
	public ModFileTabbedPane() {
		super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
	}

	/**
	 * Retrieves the tab based on file
	 * @param file the file for the tab to retrieve
	 * @return the tab, or null if not found
	 */
	public ModFileTab getTab(File file) {
		return filenameToTabMap.get(file.getAbsolutePath());
	}
	
	@Override
	public void removeTabAt(int index) {
		ModFileTab thisTab = (ModFileTab) this.getComponentAt(index);
		// check whether the tab has a valid UPK file reference and whether
		// the same file is referenced by another tab
		UpkFile upkFile = thisTab.getUpkFile();
		if (upkFile != null) {
			boolean shared = false;
			// iterate tabs, skip the one that's about to be removed
			for (int i = 0; (i < this.getTabCount()) && (i != index); i++) {
				ModFileTab thatTab = (ModFileTab) this.getComponentAt(i);
				if (upkFile.equals(thatTab.getUpkFile())) {
					shared = true;
					break;
				}
			}
			// if referenced UPK file is unique among tabs remove it from cache
			if (!shared) {
				MainFrame.getInstance().getUPKCache().remove(upkFile.getFile());
			}
		}
		UpkModderProperties.removeOpenModFile(thisTab.getModFile());
		if(thisTab.getModFile() != null) {
			filenameToTabMap.remove(thisTab.getModFile().getAbsolutePath());
		}
		super.removeTabAt(index);
	}

	/**
	 * Creates a new tab containing a default template file.
	 * @return <code>false</code> if an error occurred, <code>true</code> otherwise
	 */
	public boolean createNewModFile() {
		try {
			// load configured template file
			ModFileTab tab = new ModFileTab(Constants.TEMPLATE_MOD_FILE, true);
			this.addTab("New File", tab);
			this.setSelectedComponent(tab);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to load template mod file", e);
			return false;
		}
		return true;
	}

	/**
	 * Creates a new tab containing the specified mod file.
	 * @param file the mod file
	 * @return <code>false</code> if an error occurred, <code>true</code> otherwise
	 */
	public boolean openModFile(File file) {
		try {
			if(filenameToTabMap.get(file.getAbsolutePath()) != null) { // file already open, switch to its tab
				this.setSelectedComponent(filenameToTabMap.get(file.getAbsolutePath()));
			} else {
				ModFileTab tab = new ModFileTab(file);
				this.addTab(file.getName(), tab);
				this.setSelectedComponent(tab);
				filenameToTabMap.put(file.getAbsolutePath(), tab);
				UpkModderProperties.addOpenModFile(file);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to load mod file \'" + file.getName() + "\'", e);
			return false;
		}
		return true;
	}

	/**
	 * Closes the currently opened mod file tab.
	 */
	public void closeModFile() {
		filenameToTabMap.put(this.getActiveModFile().getAbsolutePath(), (ModFileTab) this.getSelectedComponent());
		UpkModderProperties.removeOpenModFile(this.getActiveModFile());
		this.remove(this.getSelectedComponent());
	}

	/**
	 * Closes all tabs.
	 */
	public void closeAllModFiles() {
		filenameToTabMap.clear();
		UpkModderProperties.removeAllOpenModFiles();
		this.removeAll();
	}

	/**
	 * Returns the mod file of the currently selected tab.
	 * @return the active mod file
	 */
	public File getActiveModFile() {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			File file = ((ModFileTab) selComp).getModFile();
			if ((file == null) || !file.exists()) {
				file = new File(BrowseAbstractAction.getLastSelectedFile().getParent()
						+ this.getTitleAt(this.getSelectedIndex()) + ".upk_mod");
			}
			return file;
		}
		return null;
	}

	/**
	 * Saves the contents of the currently selected tab to its associated file.
	 */
	public void saveModFile() {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			ModFileTab tab = (ModFileTab) selComp;
			File file = tab.getModFile();
			if (file != null) {
				try {
					tab.saveFile();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed to save mod file \'" + file.getName() + "\'", e);
				}
			} else {
				ActionCache.getAction("saveModFileAs").actionPerformed(null);
			}
		}
	}

	/**
	 * Saves the contents of the currently selected tab to the specified file.
	 * @param file the file to save to
	 */
	public void saveModFileAs(File file) {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			this.setTitleAt(this.getSelectedIndex(), file.getName());
			this.updateUI(); // needed to update tab with
			ModFileTab tab = (ModFileTab) selComp;
			filenameToTabMap.remove(this.getActiveModFile().getAbsolutePath());
			UpkModderProperties.removeOpenModFile(this.getActiveModFile());
			tab.setModFile(file);
			filenameToTabMap.put(file.getAbsolutePath(), tab);
			UpkModderProperties.addOpenModFile(file);
			try {
				tab.saveFile();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to save mod file \'" + file.getName() + "\'", e);
			}
		}
	}
	
	public void showReferenceUpdateDialog() {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			((ModFileTab) selComp).showReferenceUpdateDialog();
		}
	}

	/**
	 * Applies the currently selected tab's mod file hex changes.
	 */
	public void applyModFile() {
		this.applyRevertModFile(true);
	}

	/**
	 * Reverts the currently selected tab's mod file hex changes.
	 */
	public void revertModFile() {
		this.applyRevertModFile(false);
	}
	
	/**
	 * Helper method to either apply or revert changes detailed in the currently
	 * active mod file.
	 * @param apply <code>true</code> if the <code>BEFORE</code> block contents
	 *  shall be replaced with the <code>AFTER</code> block contents,
	 *  <code>false</code> if it's the other way round
	 */
	private void applyRevertModFile(boolean apply) {
		int selectedIndex = this.getSelectedIndex();
		Component selComp = this.getComponentAt(selectedIndex);
		if (selComp != null) {
			ModFileTab tab = (ModFileTab) selComp;
			if (apply) {
				if (tab.applyChanges()) {
					// set Tab color/font/tooltip style to indicate apply/revert status
					this.setForegroundAt(selectedIndex,  new Color(0, 0, 230)); // blue indicates AFTER
					this.setFontAt(selectedIndex, TAB_PANE_FONT_APPLIED);
					this.setToolTipTextAt(selectedIndex, "Hex Applied");
					this.updateUI(); // needed to update tab with
				}
			} else {
				if (tab.revertChanges()) {
					// set Tab color/font/tooltip style to indicate apply/revert status
					this.setForegroundAt(selectedIndex,  new Color(0, 128, 0)); // green indicates BEFORE
					this.setFontAt(selectedIndex, TAB_PANE_FONT_REVERTED);
					this.setToolTipTextAt(selectedIndex, "Original Hex");
					this.updateUI(); // needed to update tab 
				}
			}
		}
	}

	/**
	 * Tests current active modfile apply status and sets tab coloring accordingly
	 */
	public void testStatusModFile() {
		int selectedIndex = this.getSelectedIndex();
		Component selComp = this.getComponentAt(selectedIndex);
		if (selComp != null) {
			ModFileTab tab = (ModFileTab) selComp;
			HexSearchAndReplace.ApplyStatus status = tab.testStatusModFile();
			
			if (status == HexSearchAndReplace.ApplyStatus.AFTER_HEX_PRESENT) {
				this.setForegroundAt(selectedIndex,  new Color(0, 0, 230)); // blue indicates AFTER
				this.setFontAt(selectedIndex, TAB_PANE_FONT_APPLIED);
				this.setToolTipTextAt(selectedIndex, "Hex Applied");
			} else if (status == HexSearchAndReplace.ApplyStatus.BEFORE_HEX_PRESENT) {
				this.setForegroundAt(selectedIndex,  new Color(0, 128, 0)); // green indicates BEFORE
				this.setFontAt(selectedIndex, TAB_PANE_FONT_REVERTED);
				this.setToolTipTextAt(selectedIndex, "Original Hex");
			} else if (status == HexSearchAndReplace.ApplyStatus.MIXED_STATUS) {
				this.setForegroundAt(selectedIndex,  new Color(232, 118, 0)); // orange indicates MIXED
				this.setFontAt(selectedIndex, TAB_PANE_FONT_REVERTED);
				this.setToolTipTextAt(selectedIndex, "Mixed Status");
			} else if (status == HexSearchAndReplace.ApplyStatus.APPLY_ERROR) {
				this.setForegroundAt(selectedIndex,  new Color(255, 0, 0)); // red indicates ERROR
				this.setFontAt(selectedIndex, TAB_PANE_FONT_REVERTED);
				this.setToolTipTextAt(selectedIndex, "ERROR");
			} else if (status ==HexSearchAndReplace.ApplyStatus.NO_UPK) {
				this.setForegroundAt(selectedIndex,  new Color(0, 0, 0)); // black indicates NOUPK
				this.setFontAt(selectedIndex, TAB_PANE_FONT_REVERTED);
				this.setToolTipTextAt(selectedIndex, "No target UPK");
			}
			this.updateUI(); // needed to update tab 
		}
	}

	
	/**
	 * The basic component inside the tabbed pane.
	 * @author XMS
	 */
	public class ModFileTab extends JSplitPane {

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
		 * Creates a new tab from the specified modfile reference.
		 * @param modFile the modfile to parse
		 * @throws Exception if the mod file could not be parsed
		 */
		public ModFileTab(File modFile) throws Exception {
			this(modFile, false);
		}

		/**
		 * Creates a new tab from the specified modfile reference.
		 * @param modFile the modfile to parse
		 * @param template specifies whether the file being opened is a template file
		 * @throws Exception if the mod file could not be parsed
		 */
		public ModFileTab(File modFile, boolean template) throws Exception {
			super(JSplitPane.HORIZONTAL_SPLIT);
			this.modFile = modFile;
			
			this.initComponents();
				
			if (template) {
				this.modFile = null;	// remove link to file
			}

		}
		
		/**
		 * Creates and lays out the components of the tab.
		 * @param modFile
		 */
		private void initComponents() throws Exception {
			// create right-hand editor pane
			modEditor = new JEditorPane();
			modEditor.setFont(TEXT_PANE_FONT);

			// install editor kit
			modEditor.setEditorKit(new ModStyledEditorKit()); // relocated to new class
//			modEditor.setEditorKit(new StyledEditorKit() {
//				@Override
//				public ViewFactory getViewFactory() {
//					return new ViewFactory() {
//						@Override
//				        public View create(Element elem) {
//				            String kind = elem.getName();
//				            if (kind != null) {
//				                if (kind.equals(AbstractDocument.ContentElementName)) {
//				                    return new LabelView(elem);
//				                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
//				                	return new ParagraphView(elem) {
//				                    	/* hack to prevent line wrapping */
////				                    	@Override
////										public void layout(int width, int height) {
////											super.layout(Short.MAX_VALUE, height);
////										}
////				                    	@Override
////										public float getMinimumSpan(int axis) {
////											return super.getPreferredSpan(axis);
////										}
//										// tab-stop code from http://java-sl.com/tip_default_tabstop_size.html
//										@Override
//										public float nextTabStop(float x, int tabOffset) {
//											TabSet tabs = getTabSet();
//											if(tabs == null) {
//												// a tab every 72 pixels.
//												return (float)(getTabBase() + (((int)x / TAB_SIZE + 1) * TAB_SIZE));
//											}
//
//											return super.nextTabStop(x, tabOffset);
//										 }
//				                    };
//				                } else if (kind.equals(AbstractDocument.SectionElementName)) {
//				                    return new BoxView(elem, View.Y_AXIS);
//				                } else if (kind.equals(StyleConstants.ComponentElementName)) {
//				                    return new ComponentView(elem);
//				                } else if (kind.equals(StyleConstants.IconElementName)) {
//				                    return new IconView(elem);
//				                }
//				            }
//				            // default to text display
//				            return new LabelView(elem);
//				        }
//				    };
//				}
//			});
			
			// read provided file, if possible
			if (modFile != null) {
				modEditor.read(new FileInputStream(modFile), modFile);
			}

			// wrap editor in scroll pane
			JScrollPane modEditorScpn = new JScrollPane(modEditor,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			modEditorScpn.setRowHeaderView(new LineNumberMargin(modEditor));
			modEditorScpn.setPreferredSize(new Dimension(650, 600));
			
			Document modDocument = modEditor.getDocument();
//			modDocument.putProperty(PlainDocument.tabSizeAttribute, 4);

			// create tree view of right-hand mod editor
			modTree = new ModTree(modDocument);
//			final JTree modElemTree = new JTree(modTree.getRoot()); // draw from ModTree
			final JTree modElemTree = new JTree(modTree); // draw from ModTree
			JScrollPane modElemTreeScpn = new JScrollPane(modElemTree,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			modElemTreeScpn.setPreferredSize(new Dimension());
			
			// configure look and feel of tree view
			modElemTree.setRootVisible(false);
//			modElemTree.setShowsRootHandles(false);
//			modElemTree.putClientProperty("JTree.lineStyle", "Angled");
			
			// display alternate operand text info for opened ModOperandNodes
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
				@Override
				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean sel, boolean expanded, boolean leaf,
						int row, boolean hasFocus) {
					if (((value instanceof ModOperandNode) && expanded )) {
						value = ((ModTreeNode) value).toString(expanded);
					} else if((value instanceof ModReferenceLeaf) 
							|| (value instanceof ModGenericLeaf) 
							|| (value instanceof ModStringLeaf) 
							|| (value instanceof ModOffsetLeaf)) {
						value = ((ModTreeNode) value).toString(true);
					}
					Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded,
							leaf, row, hasFocus);
					comp.setFont(TREE_PANE_FONT);
					return comp;
				}
			};
			renderer.setLeafIcon(null);
			renderer.setClosedIcon(null);
			renderer.setOpenIcon(null);
			modElemTree.setCellRenderer(renderer);
				
			// wrap tree and editor in split pane
			this.setLeftComponent(modEditorScpn);
			this.setRightComponent(modElemTreeScpn);
			this.setOneTouchExpandable(true);

			// initially hide right-hand tree view
			this.setSize(1000, 0);
			this.setDividerLocation(1.0);
			
		}

//		private long[] testBeforeAndAfterBlocks(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
//			// perform simple error checking first
//			// check for same number of blocks
//			if(patterns.size() != replacements.size()) {
//				logger.log(Level.INFO, "Block count mismatch");
//				return null;
//			}
//			// check each block has same number of bytes
//			long[] filePositions = new long[patterns.size()];
//			for (int i = 0; i < patterns.size() ; i++) {
//				if(patterns.get(i).length != replacements.get(i).length) {
//				logger.log(Level.INFO, "Block " + i + " bytecount mismatch. FIND = " 
//						+ patterns.get(i).length + ", REPLACE = " 
//						+ replacements.get(i).length);
//					return null;
//				}
//			}
//			// try and find each pattern blocks position
//			for(int j = 0; j < patterns.size() ; j ++) {
//				long filePos = HexSearchAndReplace.findFilePosition(patterns.get(j), this.getUpkFile(), this.modTree);
//				if(filePos == -1) {
//					logger.log(Level.INFO, "Block " + j + " FIND not found");
//					return null;
//				} else {
//					filePositions[j]= filePos;
//				}
//			}
//			return filePositions;
//		}

		/**
		 * Saves the editor's contents to the file associated with this tab.
		 * @throws IOException if an I/O error occurs
		 */
		public void saveFile() throws IOException {
			modEditor.write(new OutputStreamWriter(new FileOutputStream(modFile)));
		}

		/**
		 * Searches the associated UPK file for the byte data of the <code>BEFORE</code>
		 * block(s) and overwrites it using the byte data of the <code>AFTER</code> block(s).
		 * @return true if changes applied successfully, false if not
		 * @XTMS -- the key here is that there can be multiple non-adjacent before/after blocks
		 * see AIAddNewObjectives@XGStrategyAI.upk_mod in the sample project
		 *      -- a few lines at the end of the function are changed, as well as the header
		 */
		public boolean applyChanges() {
			try {
				if(this.modTree.getAction().equals("")) { // default action of making changes to object
					if(this.modTree.getResizeAmount() == 0) {
						// basic search and replace without file backup
						if(this.searchAndReplace(
								HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()),
								HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()))
								) {
							ModTab.logger.log(Level.INFO, "AFTER Hex Installed");
							return true;
						}
					} else {
						// advanced search and replace resizing function (many changes to upk)
						if(HexSearchAndReplace.resizeAndReplace(true, this.modTree, this.getUpkFile())) {
							ModTab.logger.log(Level.INFO, "Function resized and AFTER Hex Installed");
							return true;
						}
					}
				} else { // perform special action
					// TODO: replace within enumeration?
					if(this.modTree.getAction().equalsIgnoreCase("typechange")) {
						if(	HexSearchAndReplace.changeObjectType(true, modTree)) {
							ModTab.logger.log(Level.INFO, "Variable type changed to AFTER");
							return true;
						}
					}
				}
			} catch(IOException ex) {
				ModTab.logger.log(Level.SEVERE, "File error", ex);
			}
			return false;
		}

		/**
		 * Searches the associated UPK file for the byte data of the <code>AFTER</code>
		 * block(s) and overwrites it using the byte data of the <code>BEFORE</code> block(s).
		 * @return true if changes reverted successfully, false otherwise
		 */
		public boolean revertChanges() {
			try {
				if(this.modTree.getAction().equals("")) { // default action of making changes to object
					if(this.modTree.getResizeAmount() == 0) {
						// basic search and replace without file backup
						if(this.searchAndReplace(
								HexSearchAndReplace.consolidateAfterHex(this.modTree, this.getUpkFile()),
								HexSearchAndReplace.consolidateBeforeHex(this.modTree, this.getUpkFile()))
								) {
							ModTab.logger.log(Level.INFO, "BEFORE Hex Installed");
							return true;
						}
					} else {
						// advanced search and replace resizing function (many changes to upk)
						if(HexSearchAndReplace.resizeAndReplace(false, this.modTree, this.getUpkFile())) {
							ModTab.logger.log(Level.INFO, "Function resized and BEFORE Hex Installed");
							return true;
						}
					}
				} else { // perform special action
					// TODO: replace within enumeration?
					if(this.modTree.getAction().equalsIgnoreCase("typechange")) {
						if (HexSearchAndReplace.changeObjectType(false, modTree)) {
							ModTab.logger.log(Level.INFO, "Variable type reverted to BEFORE");
							return true;
						}
					}
				}
			} catch(IOException ex) {
				ModTab.logger.log(Level.SEVERE, "File error", ex);
			}
			return false;
		}
		
		/**
		 * Searches the associated UPK file for the provided byte pattern and
		 * overwrites it using the provided replacement bytes.
		 * @param patterns the byte pattern to search for
		 * @param replacements the bytes to replace the search pattern with
		 * @Return true if S&R was successful, false otherwise
		 */
		private boolean searchAndReplace(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
			// TODO: move to HexSearchAndReplace class
			// perform error checking first
			long[] filePositions = this.testBeforeAndAfterBlocks(patterns, replacements);
			if(filePositions == null) {
				return false;
			}

			// everything matches, time to make the change(s)
			for(int i = 0 ; i < filePositions.length; i++) {
				HexSearchAndReplace.applyHexChange(replacements.get(i), this.getUpkFile(), filePositions[i]);
			}
			return true;
		}
		
		/**
		 * TODO: API
		 * @param patterns
		 * @param replacements
		 * @return
		 * @throws IOException
		 */
		private long[] testBeforeAndAfterBlocks(List<byte[]> patterns, List<byte[]> replacements) throws IOException {
			// TODO: move to HexSearchAndReplace class
			// perform simple error checking first
			// check for same number of blocks
			if (patterns.size() != replacements.size()) {
				ModFileTabbedPane.logger.log(Level.INFO, "Block count mismatch");
				return null;
			}
			// check each block has same number of bytes
			long[] filePositions = new long[patterns.size()];
			for (int i = 0; i < patterns.size() ; i++) {
				if (patterns.get(i).length != replacements.get(i).length) {
					ModFileTabbedPane.logger.log(Level.INFO, "Block " + i + " bytecount mismatch. FIND = " 
						+ patterns.get(i).length + ", REPLACE = " 
						+ replacements.get(i).length);
					return null;
				}
			}
			// try and find each pattern blocks position
			for (int j = 0; j < patterns.size(); j++) {
				long filePos = HexSearchAndReplace.findFilePosition(patterns.get(j), this.getUpkFile(), this.modTree);
				if (filePos == -1) {
					ModFileTabbedPane.logger.log(Level.INFO, "Block " + j + " FIND not found");
					return null;
				} else {
					filePositions[j]= filePos;
				}
			}
			return filePositions;
		}

		/**
		 * Tests this tab's apply status and updates tab coloring
		 * @return the result of the test
		 */
		public HexSearchAndReplace.ApplyStatus testStatusModFile() {
			return testFileStatus(modTree);
		}
		
		/**
		 * Shows a reference update dialog for the ModTree instance wrapped by
		 * this tab.
		 */
		public void showReferenceUpdateDialog() {
			new ReferenceUpdateDialog(modTree).setVisible(true);
		}
		
		/**
		 * Returns the mod file reference of this tab.
		 * @return the mod file
		 */
		public File getModFile() {
			return modFile;
		}
		
		/**
		 * Sets the mod file reference of this tab.
		 * @param modFile the mod file to set
		 */
		public void setModFile(File modFile) {
			this.modFile = modFile;
		}
		
		/**
		 * Returns the UPK file associated with this tab.
		 * @return the UPK file
		 */
		public UpkFile getUpkFile() {
			return modTree.getSourceUpk();
		}

		/**
		 * Sets the UPK file associated with this tab.
		 * @param upkFile the upk file to set
		 */
		public void setUpkFile(UpkFile upkFile) {
			this.modTree.setSourceUpk(upkFile);
		}

	}
	
}
