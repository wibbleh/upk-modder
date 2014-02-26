package ui.trees;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
public class ProjectTree extends JTree {
	
	/**
	 * Constructs a project tree using a default project tree model.
	 */
	public ProjectTree() {
		this(new ProjectTreeModel());
	}
	
	/**
	 * Constructs a project tree from the specified project tree model.
	 * @param treeModel the project tree model
	 */
	public ProjectTree(ProjectTreeModel treeModel) {
		super(treeModel);
		
		this.initComponents();
	}

	/**
	 * Configures the tree's visuals and functionality.
	 */
	private void initComponents() {
		// hide root node
		this.setRootVisible(false);
		
		// TODO: maybe use better custom icons for projects/modpackages/modfiles
		// @XMTS: I think I've made a bit of a mess of this because of my inability to use the Nimbus icons for overlay
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			/** The renderer delegate. */
			private TreeCellRenderer delegate = ProjectTree.this.getCellRenderer();

			@Override
			public Component getTreeCellRendererComponent(
					JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				
				// get pre-configured component from delegate renderer
				JLabel rendererLbl = (JLabel) delegate.getTreeCellRendererComponent(
						tree, value, sel, expanded, leaf, row, hasFocus);
				
				// modify renderer component depending on context
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
						baseIcon = (leaf && !Files.isDirectory(fileNode.getFilePath()))
								? Constants.FILE_ICON : Constants.DIRECTORY_ICON;
					}
					rendererLbl.setIcon(new CompoundIcon(baseIcon, statusIcon));
					rendererLbl.setFont(font);
				}
				return rendererLbl;
			}
		};
		this.setCellRenderer(renderer);
		
		final ContextPopupMenu contextMenu = new ContextPopupMenu();

		// mouse adapter to handle opening files from the project pane
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
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
		
	}
	
	/**
	 * Creates a new project in the specified directorz.
	 * @param projectPath the path to the project root directorz
	 */
	public void createProject(Path projectPath) {
		this.getModel().createProject(projectPath);
		// expand root
		this.expandPath(new TreePath(this.getModel().getRoot()));
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
	 * Context-sensitive popup menu implementation for the project tree.
	 * @author XMS
	 */
	private class ContextPopupMenu extends JPopupMenu {
		
		/**
		 * The targeted node or <code>null</code> if nothing is targeted.
		 */
		private Object target;
		
		/**
		 * The cache of tree-specific context actions.
		 */
		private Map<String, Action> localActions;
		
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
			localActions = new HashMap<>();
			
			Action removeProjectAction = new AbstractAction("Remove Project") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					MainFrame.getInstance().removeProject((ProjectNode) target);
				}
			};
			
			Action newModFileAction = new AbstractAction("New Mod File") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					MainFrame.getInstance().createNewModFile((FileNode) target);
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

					ModFileBulkApplyRevertWorker worker = new ModFileBulkApplyRevertWorker((FileNode) target, true);
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

					ModFileBulkApplyRevertWorker worker = new ModFileBulkApplyRevertWorker((FileNode) target, false);
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
			
			localActions.put("removeProject", removeProjectAction);
			localActions.put("newModFile", newModFileAction);
			localActions.put("testModFile", testModFileAction);
			localActions.put("bulkApply", bulkApplyModFileAction);
			localActions.put("bulkRevert", bulkRevertModFileAction);
		}
					
		/**
		 * Creates the context menu's entries.
		 */
		private void initComponents() {
			this.add(ActionCache.getAction("newProject"));
			this.add(localActions.get("removeProject"));
			this.addSeparator();
			this.add(localActions.get("newModFile"));
			this.addSeparator();
			this.add(localActions.get("testModFile"));
			this.addSeparator();
			this.add(localActions.get("bulkApply"));
			this.add(localActions.get("bulkRevert"));
		}
		
		
		/**
		 * Configures the available actions depending on the context established
		 * by the specified target path.
		 * @param targetPath the target path
		 */
		private void configureTargetContext(TreePath targetPath) {
			// determine context
			if (targetPath != null) {
				// a tree node has been targeted
				Object node = targetPath.getLastPathComponent();
				target = node;
				
				if (node instanceof FileNode) {	// sanity check, all nodes descend from FileNode
					// either a project node, a generic file/directory node or
					// a mod file node has been targeted
					if (node instanceof ProjectNode) {
						localActions.get("removeProject").setEnabled(true);
					} else {
						localActions.get("removeProject").setEnabled(false);
						if (node instanceof ModFileNode) {
							localActions.get("testModFile").setEnabled(true);
							localActions.get("bulkApply").setEnabled(true);
							localActions.get("bulkRevert").setEnabled(true);
						} else {
							// either a directory or a non-mod file node has been targeted
							if (Files.isRegularFile(((FileNode) node).getFilePath())) {
								// non-mod file

							} else {
								// directory
								
							}
						}
					}
				}
			} else {
				// empty space has been targeted
				localActions.get("testModFile").setEnabled(false);
			}
		}
		
		@Override
		public void show(Component invoker, int x, int y) {
			TreePath treePath = ((JTree) invoker).getPathForLocation(x, y);
			this.configureTargetContext(treePath);
			
			super.show(invoker, x, y);
		}
		
	}

	@SuppressWarnings("unchecked")
	public class ModFileStatusWorker extends SwingWorker<Object, Object> {
		
		private FileNode parentNode;

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
						} catch (Exception e) {
							e.printStackTrace();
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

	@SuppressWarnings("unchecked")
	public class ModFileBulkApplyRevertWorker extends SwingWorker<Object, Object> {
		
		private FileNode parentNode;
		private boolean apply;

		public ModFileBulkApplyRevertWorker(FileNode parentNode, boolean apply) {
			super();
			this.parentNode = parentNode;
			this.apply = apply;
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
				if(((FileNode) fileNode.getParent()).isExcluded()) {
					continue;
				}
				if (fileNode instanceof ModFileNode) {
					total++;
				}
			}
			
			// update mod file status and fire progress events
			int current = 0;
			dfe = parentNode.depthFirstEnumeration();
			while (dfe.hasMoreElements()) {
				FileNode fileNode = (FileNode) dfe.nextElement();
				if(((FileNode) fileNode.getParent()).isExcluded()) {
					continue;
				}
				if (fileNode instanceof ModFileNode) {
					MainFrame.getInstance().testModFileStatus((ModFileNode) fileNode);
					if(!fileNode.isExcluded()) {
						if(this.apply) {
							if(fileNode.getStatus() == ApplyStatus.BEFORE_HEX_PRESENT) {
								MainFrame.getInstance().bulkApplyRevertModFile((ModFileNode) fileNode, true);
							}
						} else {
							if(fileNode.getStatus() == ApplyStatus.AFTER_HEX_PRESENT) {
								MainFrame.getInstance().bulkApplyRevertModFile((ModFileNode) fileNode, false);
							}
						}
					}
					this.setProgress((int) (++current / total * 100.0));
				} else {
					if (!fileNode.isLeaf()) {
						try {
//							fileNode.setStatus(ProjectTree.this.determineStatus(fileNode));
							fileNode.determineStatus();
						} catch (Exception e) {
							e.printStackTrace();
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

}
