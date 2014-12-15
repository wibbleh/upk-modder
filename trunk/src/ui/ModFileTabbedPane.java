package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import model.modtree.ModGenericLeaf;
import model.modtree.ModOffsetLeaf;
import model.modtree.ModOperandNode;
import model.modtree.ModReferenceLeaf;
import model.modtree.ModStringLeaf;
import model.modtree.ModTree;
import model.modtree.ModTreeNode;
import model.upk.UpkFile;

import org.jdesktop.swingx.JXEditorPane;

import ui.dialogs.ReferenceUpdateDialog;
import ui.editor.ModContext;
import ui.frames.MainFrame;
import ui.trees.ProjectTreeModel.FileNode;
import ui.trees.ProjectTreeModel.ModFileNode;
import ui.trees.ProjectTreeModel.ProjectNode;
import util.unrealhex.HexSearchAndReplace;

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
	{
		logger.setLevel(Level.ALL);
	}
	
	/**
	 * The shared collection of styles for mod file editor pane contents inside tabs.
	 */
	private ModContext modContext;

	/**
	 * Constructs a mod file tabbed pane. 
	 */
	public ModFileTabbedPane() {
		super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		this.modContext = new ModContext();
	}
	
	@Override
	public void insertTab(String title, Icon icon, Component component,
			String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		
		this.setFontAt(index, Constants.TAB_PANE_FONT_UNKNOWN);
		this.setIconAt(index, Constants.FILE_ICON);
	}
	
	@Override
	public void removeTabAt(int index) {
		ModFileTab thisTab = (ModFileTab) this.getComponentAt(index);
		// check whether the tab has unsaved changes
		if (thisTab.isModified()) {
			// show confirmation dialog
			int res = JOptionPane.showOptionDialog(this, "The tab you're about to close has unsaved changes, close anyway?",
					"Confirm Close", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, new String[] { "Close Without Save", "Save And Close", "Cancel" }, "Cancel");
			 if ((res == JOptionPane.CANCEL_OPTION) || (res == JOptionPane.CLOSED_OPTION)) {
				 // abort on cancel
				 return;
			 } else if (res == JOptionPane.NO_OPTION) {
				 // save before closing
				 try {
					 thisTab.saveFile();
				 } catch (IOException e) {
					 logger.log(Level.SEVERE, "Failed to save mod file \'" + thisTab.getModFile() + "\'", e);
				 }
			 }
		}
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
				MainFrame.getInstance().getUpkCache().remove(upkFile.getPath());
			}
		}
		// FIXME
//		UpkModderProperties.removeOpenModFile(thisTab.getModFile());
		super.removeTabAt(index);
	}
	
	@Override
	public void setTitleAt(int index, String title) {
		super.setTitleAt(index, title);
		this.updateUI();
	}

	/**
	 * Retrieves the tab associated with the provided mod file.
	 * @param modPath the mod file path
	 * @return the tab or <code>null</code> if not found
	 */
	public ModFileTab getTab(Path modPath) {
		for (int i = 0; i < this.getTabCount(); i++) {
			ModFileTab tab = (ModFileTab) this.getComponentAt(i);
//			if (modPath.equals(tab.getModFilePath())) {
			if (tab.getModFile().equals(modPath)) {
				return tab;
			}
		}
		return null;
	}

	/**
	 * Creates a new tab containing the specified mod file.
	 * @param modPath the mod file path
	 * @param modNode The Project tree node of the file being opened
	 * @return the newly created tab or <code>null</code> if an error occurred
	 */
	public ModFileTab openModFile(Path modPath, ModFileNode modNode) {
		try {
			ModFileTab modTab = this.getTab(modPath);
			if (modTab == null) {
				modTab = new ModFileTab(modPath, modNode);
				this.addTab(modPath.getFileName().toString(), modTab);
			}
			this.setSelectedComponent(modTab);
			this.setApplyStatusAt(this.indexOfComponent(modTab), modNode.getStatus());
			return modTab;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to load mod file \'" + modPath.getFileName() + "\'", e);
		}
		return null;
	}

	/**
	 * Closes the currently opened mod file tab.
	 */
	public void closeModFile() {
		// FIXME
//		UpkModderProperties.removeOpenModFile(this.getActiveModFile());
		this.remove(this.getSelectedComponent());
	}

	/**
	 * Closes all tabs.
	 */
	public void closeAllModFiles() {
		// FIXME
//		UpkModderProperties.removeAllOpenModFiles();
		this.removeAll();
	}

	/**
	 * Returns the mod file of the currently selected tab.
	 * @return the path to the active mod file
	 */
	public Path getActiveModFile() {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			Path modPath = ((ModFileTab) selComp).getModFile();
			if ((modPath == null) || Files.notExists(modPath)) {
				modPath = Paths.get(BrowseAbstractAction.getLastSelectedFile().getParent(),
						this.getTitleAt(this.getSelectedIndex()) + ".upk_mod");
			}
			return modPath;
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
			Path modPath = tab.getModFile();
			if (modPath != null) {
				try {
					tab.saveFile();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Failed to save mod file \'" + modPath.getFileName() + "\'", e);
				}
			} else {
				ActionCache.getAction("saveModFileAs").actionPerformed(null);
			}
		}
	}

	/**
	 * Saves the contents of the currently selected tab to the specified file.
	 * @param targetPath the path to the file to save to
	 */
	public void saveModFileAs(Path targetPath) {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			String fileName = targetPath.getFileName().toString();
			this.setTitleAt(this.getSelectedIndex(), fileName);
			this.updateUI(); // needed to update tab with
			ModFileTab tab = (ModFileTab) selComp;
			// FIXME
//			UpkModderProperties.removeOpenModFile(this.getActiveModFile());
			tab.setModFile(targetPath);
			// FIXME
//			UpkModderProperties.addOpenModFile(modPath);
			try {
				tab.saveFile();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to save mod file \'" + fileName + "\'", e);
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
	 * @return the apply state (may be <code>APPLY_ERROR</code> if something goes wrong
	 */
	public ApplyStatus applyModFile() {
		return this.applyRevertModFile(true);
	}

	/**
	 * Reverts the currently selected tab's mod file hex changes.
	 * @return the apply state (may be <code>APPLY_ERROR</code> if something goes wrong
	 */
	public ApplyStatus revertModFile() {
		return this.applyRevertModFile(false);
	}
	
	/**
	 * Helper method to either apply or revert changes detailed in the currently
	 * active mod file.
	 * @param apply <code>true</code> if the <code>BEFORE</code> block contents
	 *  shall be replaced with the <code>AFTER</code> block contents,
	 *  <code>false</code> if it's the other way round
	 * @return returns the apply state (may be <code>APPLY_ERROR</code> if something goes wrong
	 */
	private ApplyStatus applyRevertModFile(boolean apply) {
		ApplyStatus status = ApplyStatus.APPLY_ERROR;
		int selectedIndex = this.getSelectedIndex();
		Component selComp = this.getComponentAt(selectedIndex);
		if (selComp != null) {
			ModFileTab tab = (ModFileTab) selComp;
			if (apply) {
				if (tab.applyChanges()) {
					status = ApplyStatus.AFTER_HEX_PRESENT;
				}
			} else {
				if (tab.revertChanges()) {
					status = ApplyStatus.BEFORE_HEX_PRESENT;
				}
			}
		}
		// TODO: here would be a good place to show an error message if (status == ApplyStatus.APPLY_ERROR)
		this.setApplyStatusAt(selectedIndex, status);
		return status;
	}

	/**
	 * Tests the currently active mod file's apply status and sets tab visuals
	 * accordingly
	 */
	public ApplyStatus testModFileStatus() {
		return this.testModFileStatus(null);
	}
	
	/**
	 * Tests a specified modfile's apply status and sets tab coloring accordingly
	 * @param modFilePath the path to the mod file to test
	 */
	public ApplyStatus testModFileStatus(Path modFilePath) {
		ModFileTab tab = null;
		int index = -1;
		if (modFilePath == null) {
			index = this.getSelectedIndex();
			if (index >= 0) {
				tab = (ModFileTab) this.getComponentAt(index);
			}
		} else {
			tab = this.getTab(modFilePath);
			index = this.indexOfComponent(tab);
		}
		if (tab != null) {
			ApplyStatus status = this.testApplyStatus(tab);
			this.setApplyStatusAt(index, status);
			return status;
		}
		return null;
	}
	
	/**
	 * Tests the specified tab's apply status.
	 * @param tab the tab to test
	 * @return the result of the test
	 */
	private ApplyStatus testApplyStatus(ModFileTab tab) {
		return HexSearchAndReplace.testFileStatus(tab.getModTree());
	}
	
	/**
	 * Sets the mod file apply state of the specified tab to the specified status.
	 * @param index the tab index
	 * @param status the apply status
	 */
	public void setApplyStatusAt(int index, ApplyStatus status) {
		ModFileTab tab = (ModFileTab) this.getComponentAt(index);
		tab.setApplyStatus(status);

		this.setForegroundAt(index,  status.getForeground());
		this.setFontAt(index, status.getFont());
		this.setToolTipTextAt(index, status.getToolTipText());
		this.setIconAt(index, new CompoundIcon(Constants.FILE_ICON, status.getIcon()));
		
//		this.updateUI(); // needed to update tab 
	}

	/**
	 * Associates the provided UPK file with the currently active mod file tab.
	 * @param upkFile the UPK file to associate
	 * @return <code>true</code> if association succeeded, <code>false</code> otherwise
	 */
	public boolean associateUpk(UpkFile upkFile) {
		Component selComp = this.getSelectedComponent();
		if (selComp != null) {
			ModFileTab tab = (ModFileTab) selComp;
			tab.setUpkFile(upkFile);
			return true;
		}
		return false;
	}

	/**
	 * The basic component inside the tabbed pane.
	 * @author XMS
	 */
	public class ModFileTab extends JSplitPane {

		/**
		 * The modfile editor instance.
		 */
		private ModFileEditor modEditor;

		/**
		 * Flag denoting whether the underlying mod file has been modified.
		 */
		private boolean modified;

		/**
		 * The reference to the project tree's mod file node. May be
		 * <code>null</code> for mod files outside the project hierarchy.
		 */
		private ModFileNode modNode;
		
		/**
		 * The mod file apply state.
		 */
		private ApplyStatus status;
		
		/**
		 * Creates a new tab from the specified mod file path and mod file node
		 * from the project tree.
		 * @param modFile the path to the mod file to parse
		 * @param modNode the mod file node in the project tree, may be
		 *  <code>null</code> for mod files outside of the project hierarchy
		 * @throws Exception if the mod file could not be parsed
		 */
		public ModFileTab(Path modFile, ModFileNode modNode) throws Exception {
			super(JSplitPane.HORIZONTAL_SPLIT);
			this.modNode = modNode;
			
			this.initComponents(modFile);
		}

		/**
		 * Creates and lays out the components of the tab.
		 * @param modFile
		 */
		private void initComponents(Path modFile) throws Exception {
			// create right-hand editor pane
			modEditor = new ModFileEditor(modFile);
			
			// wrap editor in scroll pane
			JScrollPane modEditorScpn = new JScrollPane(modEditor,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//			modEditorScpn.setRowHeaderView(new LineNumberMargin(modEditor));
			modEditorScpn.setPreferredSize(new Dimension(650, 600));
			
			// create tree view of right-hand mod editor
//			modTree = new ModTree(modDocument);
			ModTree modTree = modEditor.getModTree();
			final JTree modElemTree = new JTree(modTree); // draw from ModTree
			JScrollPane modElemTreeScpn = new JScrollPane(modElemTree,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			modElemTreeScpn.setPreferredSize(new Dimension());
			
			modEditorScpn.setRowHeaderView(new TextLineNumber(modEditor, 4, modTree));
			// configure look and feel of tree view
			modElemTree.setRootVisible(false);
			
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
					comp.setFont(Constants.TREE_PANE_FONT);
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

		/**
		 * Saves the editor's contents to the file associated with this tab.
		 * @throws IOException if an I/O error occurs
		 */
		public void saveFile() throws IOException {
			modEditor.write(Files.newBufferedWriter(this.getModFile(), Charset.defaultCharset()));
			if (this.isModified()) {
				// modify tab title, remove leading asterisk
				ModFileTabbedPane tabPane = ModFileTabbedPane.this;
				int index = tabPane.indexOfComponent(this);
				tabPane.setTitleAt(index, tabPane.getTitleAt(index).substring(1));
				this.setModified(false);
			}
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
			ModTree modTree = this.getModTree();
			boolean res = HexSearchAndReplace.applyRevertChanges(true, modTree);
			// if a resize operation, reload the UPKFile
			if (modTree.getResizeAmount() != 0) {
				modTree.getTargetUpk().reload();
			}
			return res;
		}

		/**
		 * Searches the associated UPK file for the byte data of the <code>AFTER</code>
		 * block(s) and overwrites it using the byte data of the <code>BEFORE</code> block(s).
		 * @return true if changes reverted successfully, false otherwise
		 */
		public boolean revertChanges() {
			ModTree modTree = this.getModTree();
			boolean res = HexSearchAndReplace.applyRevertChanges(false, modTree);
			// if a resize operation, reload the UPKFile
			if (modTree.getResizeAmount() != 0) {
				modTree.getTargetUpk().reload();
			}
			return res;
		}
		
		/**
		 * Shows a reference update dialog for the ModTree instance wrapped by
		 * this tab.
		 */
		public void showReferenceUpdateDialog() {
			new ReferenceUpdateDialog(this.getModTree()).setVisible(true);
		}

		/**
		 * Returns the mod file tree associated with this tab.
		 * @return the mod file tree
		 */
		public ModTree getModTree() {
			return modEditor.getModTree();
		}
		
		/**
		 * Returns the mod file reference of this tab.
		 * @return the mod file
		 */
		public Path getModFile() {
			return modEditor.getModFile();
		}
		
		/**
		 * Sets the mod file reference of this tab.
		 * @param modFile the mod file to set
		 */
		public void setModFile(Path modFile) {
			this.modEditor.setModFile(modFile);
		}
		
		/**
		 * Returns the reference to the mod file node in the project tree.
		 * @return the mod file node or <code>null</code> if the mod file
		 *  is outside the project hierarchy
		 */
		public ModFileNode getModFileNode() {
			return modNode;
		}
		
		/**
		 * Returns the UPK file associated with this tab.
		 * @return the UPK file
		 */
		public UpkFile getUpkFile() {
			return this.getModTree().getTargetUpk();
		}

		/**
		 * Sets the UPK file associated with this tab.
		 * @param upkFile the upk file to set
		 */
		public void setUpkFile(UpkFile upkFile) {
			ModTree modTree = this.getModTree();
			modTree.setTargetUpk(upkFile);
			// update project UPK file associations
			if (this.modNode != null) {
				ProjectNode project = this.modNode.getProject();
				if (project != null) {
					Path upkPath = upkFile.getPath();
					Path prevPath = project.addUpkPath(modTree.getUpkName(), upkPath);
					if (!upkPath.equals(prevPath)) {
						// mapping has been changed, update all associations of
						// other opened mod file tabs of the same project
						// TODO: implement this
						// TODO: also maybe find cases where it's necessary to remove mappings again
						// TODO: maybe create a dialog for project nodes to manage associations directly instead of going through files separately (though typically there's only two major associations, at least for XCOM)
					}
				}
			}
		}
		
		/**
		 * Returns whether the underlying mod file has been modified.
		 * @return <code>true</code> if the mod file has been modified, <code>false</code> otherwise
		 */
		public boolean isModified() {
			return modified;
		}
		
		/**
		 * Sets whether the underlying model should be marked as being modified.<br>
		 * Modifies the tab title to have an asterisk in front.
		 * @param modified <code>true</code> if the mod file has been modified, <code>false</code> otherwise
		 */
		public void setModified(boolean modified) {
			if (!this.modified) {
				// modify tab title, prepend asterisk
				ModFileTabbedPane tabPane = ModFileTabbedPane.this;
				int index = tabPane.indexOfComponent(this);
				tabPane.setTitleAt(index, "*" + tabPane.getTitleAt(index));
			}
			this.modified = modified;
		}
		
		/**
		 * Returns the apply state.
		 * @return the apply state
		 */
		public ApplyStatus getApplyStatus() {
			return status;
		}

		/**
		 * Sets the apply state.
		 * @param status the status
		 */
		public void setApplyStatus(ApplyStatus status) {
			this.status = status;
			modNode.setStatus(status);
			// propagate status up the project hierarchy
			TreeNode parent = modNode.getParent();
			while (parent instanceof FileNode) {
				((FileNode) parent).determineStatus();
				parent = parent.getParent();
			}
		}
		
		/**
		 * Editor implementation for auto-styling mod file contents.
		 * @author XMS
		 */
		private class ModFileEditor extends JXEditorPane {
			
			/**
			 * The mod file associated with this editor.
			 */
			private Path modFile;
			
			/**
			 * The mod file tree structure backing this editor.
			 */
			private ModTree modTree;
			
			/**
			 * The reference to the mod tree structure updating background worker.
			 */
			private TreeWorker treeWorker;
			
			/**
			 * The reference to the mod editor document restyling background worker. 
			 */
			private StyleWorker styleWorker;

			/**
			 * Creates a mod file editor initialized using the contents of the
			 * mod file the provided path points to.
			 * @param modFile the mod file to initially read from
			 * @throws IOException if an I/O error occurs
			 */
			public ModFileEditor(Path modFile) throws IOException {
				super();
				this.modFile = modFile;
				
				// install editor kit
				this.setEditorKit(new ModStyledEditorKit());

				// read provided file, if possible
				if (modFile != null) {
					this.read(Files.newInputStream(modFile), null);
				}
				
				// listen to document changes
				final DocumentListener docListener = new DocumentListener() {
					
					/** Timer to delay updates until after document events cease coming in. */
					private Timer timer = new Timer(1000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							if (treeWorker != null) {
								// terminate worker prematurely
								treeWorker.cancel(true);
							}
							// start new worker to restyle editor document
							treeWorker = new TreeWorker(ModFileEditor.this);
							treeWorker.execute();
						}
					});
					
					@Override public void removeUpdate(DocumentEvent evt) { this.rebuildTree(); }
					@Override public void insertUpdate(DocumentEvent evt) { this.rebuildTree(); }
					
					/**
					 * Rebuild the mod tree structure.
					 */
					private void rebuildTree() {
						ModFileTab.this.setModified(true);
						
						timer.setRepeats(false);
						timer.restart();
					}
					
					@Override public void changedUpdate(DocumentEvent evt) { /* do nothing */ }
				};
				this.getDocument().addDocumentListener(docListener);
				
				// init mod tree
				this.modTree = new ModTree(this.getText(), true);
				
				// listen to tree changes
				modTree.addTreeModelListener(new TreeModelListener() {
					
					@Override public void treeStructureChanged(TreeModelEvent evt) { this.restyleDocument(); }
					
					/**
					 * Restyle the mod editor document.
					 */
					private void restyleDocument() {
						ModFileTab.this.setModified(true);
						
						if (styleWorker != null) {
							// terminate worker prematurely
							styleWorker.cancel(true);
						}
						// start new worker to restyle editor document
						styleWorker = new StyleWorker(ModFileEditor.this);
						styleWorker.execute();
					}

					/* unused overrides */
					@Override public void treeNodesRemoved(TreeModelEvent evt) { }
					@Override public void treeNodesInserted(TreeModelEvent evt) { }
					@Override public void treeNodesChanged(TreeModelEvent evt) { }
				});
				
				// listen to complete document replacements
				this.addPropertyChangeListener("document", new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						// re-install document listener on new document
						Document newDoc = (Document) evt.getNewValue();
						newDoc.addDocumentListener(docListener);
					}
				});
				
				// initialize tree contents and document styling
				(styleWorker = new StyleWorker(this)).execute();
				
				// register undo/redo keystrokes
				InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				inputMap.put(KeyStroke.getKeyStroke("control Z"), "undo");
				inputMap.put(KeyStroke.getKeyStroke("control Y"), "redo");
				
				// misc. configurations
				this.setFont(Constants.TEXT_PANE_FONT);
				this.setDragEnabled(true);

			}
			
			/**
			 * Returns the mod file associated with this editor.
			 * @return the mod file
			 */
			public Path getModFile() {
				return modFile;
			}
			
			/**
			 * Sets the mod file associated with this editor.
			 * @param modFile the mod file
			 */
			public void setModFile(Path modFile) {
				this.modFile = modFile;
			}
			
			/**
			 * Returns the mod tree structure backing this editor.
			 * @return the mod tree
			 */
			public ModTree getModTree() {
				return modTree;
			}
			
			/**
			 * Background worker for rebuilding mod file tree structures.
			 * @author XMS
			 */
			private class TreeWorker extends SwingWorker<Object, Object> {
				
				/**
				 * The mod file editor pane.
				 */
				private ModFileEditor modEditor;

				/**
				 * Creates a worker to rebuild the mod file tree structure from
				 * the document contents of the specified mod file editor
				 * component.
				 * @param modEditor the mod file editor
				 */
				public TreeWorker(ModFileEditor modEditor) {
					this.modEditor = modEditor;
				}

				@Override
				protected Object doInBackground() throws Exception {
					// extract and update mod tree
					ModTree modTree = modEditor.getModTree();
					// TODO: better to use a separate worker for tree rebuilding since from this point tree parsing cannot be interrupted properly
					modTree.parseText(modEditor.getText(), true);
					
					return null;
				}
				
			}
			
			/**
			 * Background worker implementation for restyling editor documents.
			 * @author XMS
			 */
			private class StyleWorker extends SwingWorker<Object, Object> {

				/**
				 * The mod file editor pane.
				 */
				private ModFileEditor modEditor;
				
				/**
				 * Creates a worker to style text contents of a mod file tree
				 * structure and apply them to the specified mod file editor
				 * component.
				 * @param modEditor the mod file editor receiving the resulting
				 *  styled document
				 */
				public StyleWorker(ModFileEditor modEditor) {
					this.modEditor = modEditor;
				}

				@Override
				protected Object doInBackground() throws Exception {
					try {
						long startTime = System.currentTimeMillis();

						// init blank document using shared style cache
						DefaultStyledDocument document = new DefaultStyledDocument(
								ModFileTabbedPane.this.modContext);
						
						// recursively fill out document with styled node contents
						this.insertNodeContents(modTree.getRoot(), document);
						
						logger.log(Level.FINE, "Styled Document, took " + (System.currentTimeMillis() - startTime) + "ms");
						
						if (this.isCancelled()) {
							return null;
						}
						// apply styled document to editor
						int position = modEditor.getCaretPosition();
						modEditor.setDocument(document);
						// FIXME: typing quickly messes up the caret position
						modEditor.setCaretPosition(position);
						
						return null;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				
				/**
				 * Styles the text contents of the specified mod tree node
				 * appends it to the provided document and recursively repeats
				 * this process for all child nodes.
				 * @param node the node to style and append
				 * @param document the document to append to
				 */
				private void insertNodeContents(ModTreeNode node,
						DefaultStyledDocument document) {
					try {
						if (this.isCancelled()) {
							return;
						}
						// append styled node text contents
						String text = node.getText();
						if (!text.isEmpty()) {
							// fetch style
							Style style = document.getStyle(
									ModFileTabbedPane.this.modContext.getStyleNameByNode(node));
							
							if (text.endsWith(" ")) {
								// don't style trailing spaces
								document.insertString(document.getLength(), text.trim(), style);
								document.insertString(document.getLength(), " ", null);
							} else {
								document.insertString(document.getLength(), text.replace("\r", ""), style);
							}
						}
						// recurse through node children
						Enumeration<ModTreeNode> children = node.children();
						while (children.hasMoreElements()) {
							if (this.isCancelled()) {
								return;
							}
							ModTreeNode child = children.nextElement();
							this.insertNodeContents(child, document);
						}
					} catch (Exception e) {
						// ignore, can't happen
					}
				}
				
			}
			
		}

	}
	
}
