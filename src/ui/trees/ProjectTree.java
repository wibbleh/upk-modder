package ui.trees;

import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

import ui.ActionCache;
import ui.ApplyStatus;
import ui.CompoundIcon;
import ui.Constants;
import ui.frames.MainFrame;
import ui.trees.ProjectTreeModel.FileNode;
import ui.trees.ProjectTreeModel.ModFileNode;
import ui.trees.ProjectTreeModel.ProjectNode;

/**
 * Tree view implementation for the application's project pane.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ProjectTree extends JXTree {
	
	ProjectTreeModel theTreeModel;
	
	/**
	 * Constructs a project tree using a default project tree model.
	 * @throws IOException
	 */
	public ProjectTree() throws IOException {
		this(new ProjectTreeModel());
	}
	
	/**
	 * Constructs a project tree from the specified project tree model.
	 * @param treeModel the project tree model
	 */
	public ProjectTree(ProjectTreeModel treeModel) {
		super(treeModel);
		this.theTreeModel = treeModel;
		this.initComponents();
		
		// launch file watching service
		new DirectoryChangeWorker(treeModel).execute();
	}

	/**
	 * Configures the tree's visuals and functionality.
	 */
	private void initComponents() {
		// hide root node
		this.setRootVisible(false);
		
//		// TODO: maybe use better custom icons for projects/modpackages/modfiles
//		// @XMTS: I think I've made a bit of a mess of this because of my inability to use the Nimbus icons for overlay
//		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
//			/** The renderer delegate. */
//			private TreeCellRenderer delegate = ProjectTree.this.getCellRenderer();
//
//			@Override
//			public Component getTreeCellRendererComponent(
//					JTree tree, Object value, boolean sel, boolean expanded,
//					boolean leaf, int row, boolean hasFocus) {
//				
//				// get pre-configured component from delegate renderer
//				JLabel rendererLbl = (JLabel) tree.getCellRenderer().getTreeCellRendererComponent(
//						tree, value, sel, expanded, leaf, row, hasFocus);
//				
//				// modify renderer component depending on context
//				if (value instanceof FileNode) {
//					FileNode fileNode = (FileNode) value;
//					// extract status
//					ApplyStatus status = fileNode.getStatus();
//					
//					// set up visual parameters
//					Icon statusIcon = (status != null) ? status.getIcon() : null;
//					Icon baseIcon = null;
//					Font font = null;
//					if (fileNode instanceof ProjectNode) {
//						font = Constants.PROJECT_NAME_FONT;
//						baseIcon = Constants.HEX_SMALL_ICON;
//					} else {
//						font = Constants.PROJECT_ENTRY_FONT;
//						baseIcon = (leaf && !Files.isDirectory(fileNode.getFilePath()))
//								? Constants.FILE_ICON : Constants.DIRECTORY_ICON;
//					}
//					rendererLbl.setIcon(new CompoundIcon(baseIcon, statusIcon));
//					rendererLbl.setFont(font);
//				}
//				return rendererLbl;
//			}
//		};
//		this.setCellRenderer(renderer);
		
		this.addHighlighter(new AbstractHighlighter() {
			@Override
			protected Component doHighlight(Component component,
					ComponentAdapter adapter) {
				Object value = adapter.getValue();
				if (value instanceof FileNode) {
					FileNode fileNode = (FileNode) value;
					// extract status
					ApplyStatus status = fileNode.getStatus();
					
					// set up visual parameters
					Icon statusIcon = (status != null) ? status.getIcon() : null;
					Icon baseIcon = null;
					Font font = null;
					if (fileNode instanceof ProjectNode) {
						font = Constants.PROJECT_NAME_FONT;
						baseIcon = Constants.HEX_SMALL_ICON;
					} else {
						font = Constants.PROJECT_ENTRY_FONT;
						baseIcon = (adapter.isLeaf() && !Files.isDirectory(fileNode.getFilePath()))
								? Constants.FILE_ICON : Constants.DIRECTORY_ICON;
					}
					((JLabel) component).setIcon(new CompoundIcon(baseIcon, statusIcon));
					component.setFont(font);
				}
				return component;
			}
		});
		
		final ContextPopupMenu contextMenu = new ContextPopupMenu();

		// mouse adapter to handle opening files from the project pane
		this.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent evt) { this.processEvent(evt); }
			@Override public void mouseReleased(MouseEvent evt) { this.processEvent(evt); }

			private void processEvent(MouseEvent evt) {
				TreePath treePath =
						ProjectTree.this.getPathForLocation(evt.getX(), evt.getY());
				// check click count and type of button
				if ((evt.getClickCount() == 1) && evt.isPopupTrigger()) {
					// show context menu on right-click
					ProjectTree.this.setSelectionPath(treePath);
					contextMenu.show(ProjectTree.this, evt.getX(), evt.getY());
				} else if ((evt.getClickCount() == 2) && !evt.isPopupTrigger()) {
					// open mod file on double-click
					if (treePath != null) {
						// check whether a mod file was targeted
						if (treePath.getLastPathComponent() instanceof ModFileNode) {
							ModFileNode modNode = (ModFileNode) treePath.getLastPathComponent();
							Path path = modNode.getFilePath();
							MainFrame.getInstance().openModFile(path, modNode);
						}
					}
				}
			}
		});
		
		// only allow single selections and monitor them
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent evt) {
				MainFrame.getInstance().updateTitle();
			}
		});
		
		// init drag and drop functionality
		this.setDragEnabled(true);
		this.setDropMode(DropMode.ON_OR_INSERT);
		this.setTransferHandler(new TreeTransferHandler());
	}
	
	/**
	 * Creates a new project in the specified directory.
	 * @param projectPath the path to the project root directory
	 * @return the path to the project configuration file
	 */
	public Path createProject(Path projectPath) {
		Path xmlPath = this.getModel().createProject(projectPath);
		// expand root
		this.expandPath(new TreePath(this.getModel().getRoot()));
		
		return xmlPath;
	}

	/**
	 * Opens the project associated with the specified project XML file.
	 * @param xmlPath the path to the project XML file
	 */
	public void openProject(Path xmlPath) {
		this.getModel().openProject(xmlPath);
		// expand root
		this.expandPath(new TreePath(this.getModel().getRoot()));
	}

	/**
	 * Removes the specified project from the tree. If <code>null</code> is provided the
	 * currently active project will be removed.
	 * @param projNode the project to remove
	 * @return the path to the project's XML file or <code>null</code> if removal failed
	 */
	public Path removeProject(ProjectNode projNode) {
		// retrieve active project if null was provided
		if (projNode == null) {
			projNode = this.getActiveProject();
		}
		// forward operation to model
		if (this.getModel().removeProject(projNode)) {
			return projNode.getProjectFile();
		}
		return null;
	}

	/**
	 * Removes the currently active project and deletes all associated files.
	 */
	public void deleteProject() {
		ProjectNode activeProject = this.getActiveProject();
		ProjectTreeModel model = this.getModel();
		model.deleteProject(activeProject);
	}
	
	/**
	 * Returns the currently active project.
	 * @return the currently active project
	 */
	public ProjectNode getActiveProject() {
		return this.getProjectForPath(this.getSelectionPath());
	}
	
	/**
	 * Returns the project node of the specified tree path.
	 * @param path the path
	 * @return the project node or <code>null</code> if the path does not
	 *  contain a project node
	 */
	private ProjectNode getProjectForPath(TreePath path) {
		if (path != null) {
			// extract project node from path
			Object pathComp = path.getPathComponent(1);
			if (pathComp instanceof ProjectNode) {	// should always be a project file
				return (ProjectNode) pathComp;
			}
		}
		return null;
	}

	/**
	 * Returns the currently active directory.
	 * @return the currently active directory or project
	 */
	public FileNode getActiveDirectory() {
		return this.getDirectoryForPath(this.getSelectionPath());
	}
	
	/**
	 * Returns the most deeply nested directory node of the specified tree path.
	 * @param path the path
	 * @return the parent directory, project, or <code>null</code> if the path
	 *  contains neither
	 */
	private FileNode getDirectoryForPath(TreePath path) {
		if (path != null) {
			// iterate nodes from the end of the path
			for (int i = path.getPathCount() - 1; i > 0; i--) {
				// all nodes descend from FileNode, so it's safe to cast
				FileNode fileNode = (FileNode) path.getPathComponent(i);
				if ((fileNode instanceof ProjectNode)
						|| Files.isDirectory(fileNode.getFilePath())) {
					// accept both directories or project nodes
					return fileNode;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new mod file under the specified file node using the specified
	 * filename.
	 * @param dirNode the parent directory node
	 * @param filename the filename of the mod file to create
	 * @return a ModFileNode representing the newly created mod file or
	 *  <code>null</code> if an error occurred
	 */
	public ModFileNode createModFile(FileNode dirNode, String filename) {
		ModFileNode node = this.getModel().createModFile(dirNode, filename);
		if (node != null) {
			this.setSelectionPath(new TreePath(node.getPath()));
		}
		return node;
	}

	/**
	 * Returns the underlying project tree model.
	 * @return 
	 */
	@Override
	public ProjectTreeModel getModel() {
		return (ProjectTreeModel) treeModel;
	}

	/**
	 * Background worker for monitoring project directory structure changes.
	 * @author Amineri
	 */
	private class DirectoryChangeWorker extends SwingWorker<Object, Object> {

		/** The tree model which does all the watch event processing. */
		private ProjectTreeModel model;

		/**
		 * Creates a project directory watcher implemented in the specified
		 * project tree model.
		 * @param model the project tree model
		 */
		public DirectoryChangeWorker(ProjectTreeModel model) {
			super();
			this.model = model;
		}

		@Override
		protected Object doInBackground() throws Exception {
			model.processEvents();
			return null;
		}

	}

	/**
	 * Context-sensitive popup menu implementation for the project tree.
	 * @author XMS
	 */
	private class ContextPopupMenu extends JPopupMenu {
		
		/**
		 * The targeted node or <code>null</code> if nothing is targeted.
		 */
		private Object target;
		
		/**
		 * Creates a context-sensitive popup menu.
		 */
		public ContextPopupMenu() {
			super();
			
			this.initActions();
			
			this.initComponents();
		}

		/**
		 * Creates and configures the context menu's local action cache.
		 */
		private void initActions() {
			ActionMap actionMap = this.getActionMap();
			
			Action openModFileAction = new AbstractAction("Open Mod File") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ModFileNode modNode = (ModFileNode) target;
					MainFrame.getInstance().openModFile(modNode.getFilePath(), modNode);
				}
			};
			
			Action removeProjectAction = new AbstractAction("Remove Project") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					MainFrame.getInstance().removeProject((ProjectNode) target);
				}
			};
			
			Action newModFileAction = new AbstractAction(
					"New Mod File", UIManager.getIcon("FileView.fileIcon")) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					MainFrame.getInstance().createNewModFile((FileNode) target);
				}
			};
			newModFileAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
			newModFileAction.putValue(Action.MNEMONIC_KEY, (int) 'n');

			Action renameFileAction = new AbstractAction("Rename File") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						MainFrame.getInstance().renameFile((FileNode) target);
					} catch (IOException e) {
						// log failure to rename file
						Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, e);
					}
				}
			};

			Action deleteModFileAction = new AbstractAction("Delete Mod File") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						MainFrame.getInstance().deleteModFile((FileNode) target);
					} catch (IOException e) {
						// log failure to delete file
						Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, e);
					}
				}
			};

			Action newFolderAction = new AbstractAction("New Folder",
					UIManager.getIcon("FileChooser.newFolderIcon")) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						MainFrame.getInstance().createNewFolder((FileNode) target);
					} catch (IOException e) {
						// log failure to create folder
						Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, e);
					}
				}
			};
			newFolderAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));

			Action deleteFolderAction = new AbstractAction("Delete Folder") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						MainFrame.getInstance().deleteFolder((FileNode) target);
					} catch (IOException e) {
						// TODO: log failure to create folder
						Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, e);
					}
				}
			};
                        
			Action testModFileAction = new AbstractAction("Test Status") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ModFileStatusWorker worker = new ModFileStatusWorker((FileNode) target);
					worker.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if ("progress".equals(evt.getPropertyName())) {
								// forward to main frame
								MainFrame.getInstance().setProgress(
										(Integer) evt.getNewValue());
							}
						}
					});
					worker.execute();
				}
			};

			Action bulkApplyModFileAction = new AbstractAction("Apply file(s)") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ModFileBulkApplyRevertWorker worker =
							new ModFileBulkApplyRevertWorker((FileNode) target, true);
					worker.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if ("progress".equals(evt.getPropertyName())) {
								// forward to main frame
								MainFrame.getInstance().setProgress(
										(Integer) evt.getNewValue());
							}
						}
					});
					worker.execute();
				}
			};

			Action bulkRevertModFileAction = new AbstractAction("Revert file(s)") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ModFileBulkApplyRevertWorker worker =
							new ModFileBulkApplyRevertWorker((FileNode) target, false);
					worker.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if ("progress".equals(evt.getPropertyName())) {
								// forward to main frame
								MainFrame.getInstance().setProgress((Integer) evt.getNewValue());
							}
						}
					});
					worker.execute();
				}
			};

			actionMap.put("openModFile", openModFileAction);
			actionMap.put("removeProject", removeProjectAction);
			actionMap.put("deleteModFile", deleteModFileAction);
			actionMap.put("newModFile", newModFileAction);
			actionMap.put("renameFile", renameFileAction);
			actionMap.put("newFolder", newFolderAction);
			actionMap.put("deleteFolder", deleteFolderAction);
			actionMap.put("testModFile", testModFileAction);
			actionMap.put("bulkApply", bulkApplyModFileAction);
			actionMap.put("bulkRevert", bulkRevertModFileAction);
		}
					
		/**
		 * Creates the context menu's entries.
		 */
		private void initComponents() {
			ActionMap actionMap = this.getActionMap();
			
			JMenu newMenu = new JMenu("New...");
			newMenu.add(ActionCache.getAction("newProject"));
			newMenu.add(actionMap.get("newModFile"));
			newMenu.addSeparator();
			newMenu.add(actionMap.get("newFolder"));
			
			this.add(newMenu);
			this.addSeparator();
			this.add(actionMap.get("openModFile"));
			this.addSeparator();
			this.add(actionMap.get("renameFile"));
			this.add(actionMap.get("removeProject"));
			this.add(actionMap.get("deleteModFile"));
			this.add(actionMap.get("deleteFolder"));
			this.addSeparator();
			this.add(actionMap.get("testModFile"));
			this.add(actionMap.get("bulkApply"));
			this.add(actionMap.get("bulkRevert"));
		}
		
		@Override
		public void show(Component invoker, int x, int y) {
			TreePath treePath = ((JTree) invoker).getPathForLocation(x, y);
			this.configureTargetContext(treePath);
		
			super.show(invoker, x, y);
		}

		/**
		 * Configures the available actions depending on the context established
		 * by the specified target path.
		 * @param targetPath the target path
		 */
		private void configureTargetContext(TreePath targetPath) {
			ActionMap actionMap = this.getActionMap();
			
			// determine context
			if (targetPath != null) {
				// a tree node has been targeted
				Object node = targetPath.getLastPathComponent();
				target = node;
		
				// sanity check, all nodes descend from FileNode
				if (node instanceof FileNode) {
					// either a project node, a generic file/directory node or
					// a mod file node has been targeted
					if (node instanceof ProjectNode) {
						actionMap.get("openModFile").setEnabled(false);
//						actionMap.get("removeProject").setEnabled(true);
						this.findItem(actionMap.get("removeProject")).setVisible(true);
//						actionMap.get("deleteModFile").setEnabled(false);
						this.findItem(actionMap.get("deleteModFile")).setVisible(false);
//						actionMap.get("deleteFolder").setEnabled(false);
						this.findItem(actionMap.get("deleteFolder")).setVisible(false);
						actionMap.get("renameFile").setEnabled(false);
						actionMap.get("testModFile").setEnabled(true);
					} else {
//						actionMap.get("removeProject").setEnabled(false);
						this.findItem(actionMap.get("removeProject")).setVisible(false);
						if (node instanceof ModFileNode) {
							actionMap.get("openModFile").setEnabled(true);
							actionMap.get("testModFile").setEnabled(true);
							actionMap.get("bulkApply").setEnabled(true);
							actionMap.get("bulkRevert").setEnabled(true);
//							actionMap.get("deleteModFile").setEnabled(true);
							this.findItem(actionMap.get("deleteModFile")).setVisible(true);
//							actionMap.get("deleteFolder").setEnabled(false);
							this.findItem(actionMap.get("deleteFolder")).setVisible(false);
							actionMap.get("renameFile").setEnabled(true);
						} else {
							actionMap.get("openModFile").setEnabled(false);
							// either a directory or a non-mod file node has been targeted
							if (Files.isRegularFile(((FileNode) node).getFilePath())) {
								// non-mod file
								actionMap.get("renameFile").setEnabled(true);
								actionMap.get("testModFile").setEnabled(false);
								this.findItem(actionMap.get("deleteModFile")).setVisible(false);
							} else {
								// directory
								actionMap.get("newFolder").setEnabled(true);
//								actionMap.get("deleteFolder").setEnabled(true);
								this.findItem(actionMap.get("deleteFolder")).setVisible(true);
//								actionMap.get("deleteModFile").setEnabled(false);
								this.findItem(actionMap.get("deleteModFile")).setVisible(false);
								actionMap.get("renameFile").setEnabled(false);
								actionMap.get("testModFile").setEnabled(true);
							}
						}
					}
				}
			} else {
				// empty space has been targeted
				actionMap.get("testModFile").setEnabled(false);
			}
		}
		
		private JMenuItem findItem(Action action) {
			return this.findItem(this, action);
		}
		
		private JMenuItem findItem(JComponent menu, Action action) {
			for (Component comp : menu.getComponents()) {
				if (comp instanceof JMenu) {
					JMenuItem item = this.findItem((JMenu) comp, action);
					if (item != null) {
						return item;
					}
				} else if (comp instanceof JMenuItem) {
					JMenuItem item = (JMenuItem) comp;
					if (item.getAction().equals(action)) {
						return item;
					}
				}
			}
			return null;
		}
		
	}

	/**
	 * Background worker for determining apply states of mod files.
	 * @author Amineri
	 */
	@SuppressWarnings("unchecked")
	public class ModFileStatusWorker extends SwingWorker<Object, Object> {
		
		/** The parent file node. */
		private FileNode parentNode;

		/**
		 * Creates a status-determining worker for mod files below the specified
		 * parent file node.
		 * @param parentNode the parent file node
		 */
		public ModFileStatusWorker(FileNode parentNode) {
			super();
			this.parentNode = parentNode;
		}

		@Override
		protected Object doInBackground() throws Exception {
			// TODO: make application appear busy
			this.setProgress(0);
			
			// determine total mod file count
			double total = 0.0;
			Enumeration<FileNode> dfe = parentNode.depthFirstEnumeration();
			while (dfe.hasMoreElements()) {
				FileNode fileNode = (FileNode) dfe.nextElement();
				if (fileNode instanceof ModFileNode) {
					total++;
				}
			}
			
			// update mod file status and fire progress events
			int current = 0;
			dfe = parentNode.depthFirstEnumeration();
			while (dfe.hasMoreElements()) {
				FileNode fileNode = (FileNode) dfe.nextElement();
				if (fileNode instanceof ModFileNode) {
					MainFrame.getInstance().testModFileStatus((ModFileNode) fileNode);
					this.setProgress((int) (++current / total * 100.0));
				} else {
					if (!fileNode.isLeaf()) {
						try {
//							fileNode.setStatus(ProjectTree.this.determineStatus(fileNode));
							fileNode.determineStatus();
						} catch (Exception ex) {
							Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
			
			return null;
		}
		
		@Override
		protected void done() {
			// TODO: make application stop appearing busy
			this.setProgress(100);
		}
		
	}

	/**
	 * Background worker for batch-applying/reverting mod files.
	 * @author Amineri
	 */
	@SuppressWarnings("unchecked")
	public class ModFileBulkApplyRevertWorker extends SwingWorker<Object, Object> {
		
		/** The root of the sub-tree to perform operations on. */
		private FileNode root;
		/** Flag denoting whether an apply or a revert operation shall be performed. */
		private boolean apply;

		/**
		 * Creates a bulk processing worker for mod files in the subtree rooted
		 * at the specified file node.
		 * @param root the parent file node
		 * @param apply <code>true</code> if apply operations shall be performed,
		 *  <code>false</code> for revert operations
		 */
		public ModFileBulkApplyRevertWorker(FileNode root, boolean apply) {
			super();
			this.root = root;
			this.apply = apply;
		}

		@Override
		protected Object doInBackground() throws Exception {
			// TODO: make application appear busy
			this.setProgress(0);
			
			// determine total mod file count
			double total = 0.0;
			Enumeration<FileNode> dfe = root.depthFirstEnumeration();
			while (dfe.hasMoreElements()) {
				FileNode fileNode = (FileNode) dfe.nextElement();
				FileNode parentNode = (FileNode) fileNode.getParent();
				if (!(fileNode instanceof ProjectNode)
						&& (parentNode != null)
						&& parentNode.isExcluded()) {
					continue;
				}
				if (fileNode instanceof ModFileNode) {
					total++;
				}
			}
			
			// update mod file status and fire progress events
			int current = 0;
			dfe = root.depthFirstEnumeration();
			while (dfe.hasMoreElements()) {
				FileNode fileNode = (FileNode) dfe.nextElement();
				FileNode parentNode = (FileNode) fileNode.getParent();
				if (!(fileNode instanceof ProjectNode)
						&& (parentNode != null)
						&& parentNode.isExcluded()) {
					continue;
				}
				if (fileNode instanceof ModFileNode) {
					ModFileNode modFileNode = (ModFileNode) fileNode;
					MainFrame.getInstance().testModFileStatus(modFileNode);
					if (!fileNode.isExcluded()) {
						if (this.apply) {
							if(fileNode.getStatus() == ApplyStatus.BEFORE_HEX_PRESENT) {
								// @Amineri if the MainFrame's doing the actual heavy lifting why not move the worker there? That way it can also be added to the global action cache for use in menu/toolbar items
								MainFrame.getInstance().bulkApplyRevertModFile(modFileNode, true);
							}
						} else {
							if(fileNode.getStatus() == ApplyStatus.AFTER_HEX_PRESENT) {
								MainFrame.getInstance().bulkApplyRevertModFile(modFileNode, false);
							}
						}
					}
					this.setProgress((int) (++current / total * 100.0));
				} else {
					if (!fileNode.isLeaf()) {
						try {
//							fileNode.setStatus(ProjectTree.this.determineStatus(fileNode));
							fileNode.determineStatus();
						} catch (Exception ex) {
							Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
			
			return null;
		}
		
		@Override
		protected void done() {
			// TODO: make application stop appearing busy
			this.setProgress(100);
		}
		
	}
        
    /**
     * TreeTransferHandler handles drag/drop operations within the ProjectTree
     * code from : http://stackoverflow.com/questions/4588109/drag-and-drop-nodes-in-jtree
     */      
    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        DefaultMutableTreeNode[] nodesToRemove;

        public TreeTransferHandler() {
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                                  ";class=\"" +
                    javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                                  "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch(ClassNotFoundException e) {
                System.out.println("ClassNotFound: " + e.getMessage());
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) {
                return false;
            }

            if(!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
            JTree tree = (JTree)support.getComponent();
            int dropRow = tree.getRowForPath(dl.getPath());
            int[] selRows = tree.getSelectionRows();
            TreePath selPath = tree.getPathForRow(selRows[0]);
            FileNode source = (FileNode)selPath.getLastPathComponent();
            TreePath destPath = dl.getPath();
            FileNode target = (FileNode)destPath.getLastPathComponent();

            // Do not allow a drop on the drag source selections.
            for(int i = 0; i < selRows.length; i++) {
                if(selRows[i] == dropRow) {
                    return false;
                }
            }
            // Do not allow dropping on non-directory nodes
            if(!target.getFilePath().toFile().isDirectory()) {     
                return false;
            }
            // Do not allow dragging between different projects
            if(!source.getProject().equals(target.getProject())) {
                return false;
            }
            // Do not allow a non-leaf node to be copied to a descendant
            if(selPath.isDescendant(destPath)) {
                return false;
            }
            support.setShowDropLocation(true);
            return true;                
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree)c;
            TreePath[] paths = tree.getSelectionPaths();
            if(paths != null) {
                // Make up a node array of copies for transfer and
                // another for/of the nodes that will be removed in
                // exportDone after a successful drop.
                List<DefaultMutableTreeNode> copies = new ArrayList<>();
                List<DefaultMutableTreeNode> toRemove = new ArrayList<>();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
                DefaultMutableTreeNode copy = copy(node);
                copies.add(copy);
                toRemove.add(node);
                for(int i = 1; i < paths.length; i++) {
                    DefaultMutableTreeNode next =
                        (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                    // Do not allow higher level nodes to be added to list.
                    if(next.getLevel() < node.getLevel()) {
                        break;
                    } else if(next.getLevel() > node.getLevel()) {  // child node
                        copy.add(copy(next));
                        // node already contains child
                    } else {                                        // sibling
                        copies.add(copy(next));
                        toRemove.add(next);
                    }
                }
                DefaultMutableTreeNode[] nodes = copies.toArray(new DefaultMutableTreeNode[copies.size()]);
                nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
                return new NodesTransferable(nodes);
            }
            return null;
        }

        /** Defensive copy used in createTransferable. */
        private DefaultMutableTreeNode copy(TreeNode node) {
            return new DefaultMutableTreeNode(node);
        }

//        @Override
//        protected void exportDone(JComponent source, Transferable data, int action) {
//            if((action & MOVE) == MOVE) {
//                JTree tree = (JTree)source;
//                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
//                for (DefaultMutableTreeNode node : nodesToRemove) {
//                    model.removeNodeFromParent(node);
//                }
//            }
//        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if(!canImport(support)) {
                return false;
            }
            // Extract transfer data.
            DefaultMutableTreeNode[] nodes = null;
            try {
                Transferable t = support.getTransferable();
                nodes = (DefaultMutableTreeNode[])t.getTransferData(nodesFlavor);
            } catch(UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch(java.io.IOException ioe) {
                System.out.println("I/O error: " + ioe.getMessage());
            }
            // Get drop location info.
            JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
            JTree tree = (JTree)support.getComponent();
            int dropRow = tree.getRowForPath(dl.getPath());
            int[] selRows = tree.getSelectionRows();
            FileNode source = (FileNode)tree.getPathForRow(selRows[0]).getLastPathComponent();
            FileNode target = (FileNode)dl.getPath().getLastPathComponent();
            Path sourcePath = source.getFilePath();
            Path destPath = target.getFilePath().resolve(sourcePath.getFileName());
            if(!sourcePath.equals(destPath)) {
                if(sourcePath.toFile().isDirectory()) {
                    try {
                        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                WatchKey key = theTreeModel.getWatchKey(dir);
                                if(key != null) {
                                    key.cancel();
                                }
                                return super.preVisitDirectory(dir, attrs);
                            }
                        });
                    } catch (IOException ex) {
                        Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    Files.move(sourcePath, destPath);
                } catch (IOException ex) {
                    Logger.getLogger(ProjectTree.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            
//            JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
//            int childIndex = dl.getChildIndex();
//            TreePath dest = dl.getPath();
//            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)dest.getLastPathComponent();
//            JTree tree = (JTree)support.getComponent();
//            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
//            // Configure for drop mode.
//            int index = childIndex;    // DropMode.INSERT
//            if(childIndex == -1) {     // DropMode.ON
//                index = parent.getChildCount();
//            }
//            for (DefaultMutableTreeNode node : nodes) {
//                model.insertNodeInto(node, parent, index++);
//            }
            return true;
        }

        @Override
        public String toString() {
            return getClass().getName();
        }

        public class NodesTransferable implements Transferable {
            DefaultMutableTreeNode[] nodes;

            public NodesTransferable(DefaultMutableTreeNode[] nodes) {
                this.nodes = nodes;
             }

            @Override
            public Object getTransferData(DataFlavor flavor)
                                     throws UnsupportedFlavorException {
                if(!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);
                return nodes;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }
}
