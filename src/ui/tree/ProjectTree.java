package ui.tree;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ui.ActionCache;
import ui.BrowseAbstractAction;
import ui.Constants;
import ui.MainFrame;
import ui.tree.ProjectTreeModel.FileNode;
import ui.tree.ProjectTreeModel.ModFileNode;
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
				if (value instanceof FileNode) {
					FileNode fileNode = (FileNode) value;
					if (value instanceof ProjectNode) {
						rendererLbl.setFont(Constants.PROJECT_NAME_FONT);
						if (leaf) {
							// change icon for empty projects
							rendererLbl.setIcon(UIManager.getIcon("Tree.closedIcon"));
						}
					} else {
						rendererLbl.setFont(Constants.PROJECT_ENTRY_FONT);
						if (leaf && Files.isDirectory(fileNode.getFilePath())) {
							// change icon for empty directories
							rendererLbl.setIcon(UIManager.getIcon("Tree.closedIcon"));
						}
					}
				}
				
				return rendererLbl;
			}
		};
		this.setCellRenderer(renderer);
		
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
						if (selPath.getLastPathComponent() instanceof ModFileNode) {
							Path path = ((ModFileNode) selPath.getLastPathComponent()).getFilePath();
							((BrowseAbstractAction) ActionCache.getAction("openModFile")).execute(path.toFile());
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
