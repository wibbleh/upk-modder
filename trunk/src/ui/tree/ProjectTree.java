package ui.tree;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ui.ActionCache;
import ui.BrowseAbstractAction;
import static ui.Constants.*;
import ui.MainFrame;
import ui.tree.ProjectTreeModel.ProjectNode;

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
		
		// TODO: maybe use custom icons for projects/modpackages/modfiles
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
				if (value instanceof ProjectNode) {
					rendererLbl.setFont(PROJECT_NAME_FONT);
					if (leaf) {
						// change icon for empty projects
						rendererLbl.setIcon(UIManager.getIcon("Tree.closedIcon"));
					}
				} else if (value instanceof File) {
					File file = (File) value;
					rendererLbl.setText(file.getName());
					rendererLbl.setFont(PROJECT_ENTRY_FONT);
					if (leaf && file.isDirectory()) {
						// change icon for empty directories
						rendererLbl.setIcon(UIManager.getIcon("Tree.closedIcon"));
					}
				}
				
				return rendererLbl;
			}
		};
		this.setCellRenderer(renderer);
		
		// add listener to model to auto-expand added nodes
		this.treeModel.addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				ProjectTree.this.expandPath(new TreePath(treeModel.getRoot()));
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) { }
			@Override
			public void treeNodesRemoved(TreeModelEvent e) { }
			@Override
			public void treeNodesChanged(TreeModelEvent e) { }
		});
		
		final JPopupMenu contextMenu = new JPopupMenu();
		contextMenu.add(ActionCache.getAction("newModFile"));

		// mouse adapter to handle opening files from the project pane
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
				if ((evt.getClickCount() == 1) && evt.isPopupTrigger()) {
					// show context menu on right-click
					contextMenu.show(ProjectTree.this, evt.getX(), evt.getY());
				} else if ((evt.getClickCount() == 2) && !evt.isPopupTrigger()) {
					// open mod file on double-click
					TreePath selPath = getPathForLocation(evt.getX(), evt.getY());
					if (selPath != null) {
						if (selPath.getLastPathComponent() instanceof File) {
							File file = (File) selPath.getLastPathComponent();
							if (file.isFile()) {
								((BrowseAbstractAction) ActionCache.getAction("openModFile")).execute(file);
//								try {
//									MainFrame.getInstance().openModFile(file);
//									ModFileTab tab = new ModFileTab(file);
//									tabPane.addTab(file.getName(), tab);
//									tabPane.setSelectedComponent(tab);
//									FIXME
//									appProperties.saveOpenState(tabPane, projectMdl);

//									FIXME
//									// TODO: create function for upk re-association
//									//re-associate upk if possible
//									if(appProperties.getUpkProperty(file.getName()) != null) {
//										File ufile = new File(appProperties.getUpkProperty(file.getName()));
//										// grab UPK file from cache
//										UpkFile upkFile = upkCache.get(ufile);
//										if (upkFile == null) {
//											// if cache doesn't contain UPK file instantiate a new one
//											upkFile = new UpkFile(ufile);
//										}
//
//										// check whether UPK file is valid (i.e. header parsing worked properly)
//										if (upkFile.getHeader() != null) {
//											// store UPK file in cache
//											upkCache.put(ufile, upkFile);
//											// link UPK file to tab
//											tab.setUpkFile(upkFile);
//											// show file name in status bar
//											upkTtf.setText(ufile.getPath());
//											// enable 'update', 'apply' and 'revert' actions
//											setEditActionsEnabled(true);
//										} else {
//											// TODO: show error/warning message
//										}
//									}

//									MainFrame.getInstance().setFileActionsEnabled(true);
//								} catch (Exception ex) {
//									logger.log(Level.SEVERE, "Failure to open modfile from Project: " + ex);
//								}
							}
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
				MainFrame.getInstance().setActiveProject(
						ProjectTree.this.getProjectForPath(evt.getPath()));
			}
		});
		
	}
	
	/**
	 * Returns the project node of the specified tree path.
	 * 
	 * @param path the path
	 * @return the project node or <code>null</code> if the path does not
	 *  contain a project node.
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
	 * TODO: API
	 * @param projectDir
	 */
	public void createProject(File projectDir) {
		this.getModel().createProject(projectDir);
	}

	/**
	 * TODO: API
	 * @param xmlFile
	 */
	public void openProject(File xmlFile) {
		this.getModel().openProject(xmlFile);
	}

	/**
	 * Removes the currently active project.
	 */
	public void removeProject() {
		ProjectNode activeProject = this.getProjectForPath(this.getSelectionPath());
		ProjectTreeModel model = this.getModel();
		model.removeProject(activeProject);
	}

	/**
	 * Removes the currently active project and deletes all associated files.
	 */
	public void deleteProject() {
		ProjectNode activeProject = this.getProjectForPath(this.getSelectionPath());
		ProjectTreeModel model = this.getModel();
		model.deleteProject(activeProject);
	}

	/**
	 * Returns the underlying project tree model.
	 */
	@Override
	public ProjectTreeModel getModel() {
		return (ProjectTreeModel) treeModel;
	}

}
