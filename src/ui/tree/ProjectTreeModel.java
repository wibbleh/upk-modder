package ui.tree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ui.Constants;

/**
 * A hybrid tree model combining a tree node-based setup with a file tree model.
 * @author XMS
 */
public class ProjectTreeModel implements TreeModel {

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
	 * @param name
	 * @param directory
	 */
	public void createProject(File projectDir) {
		try {
			// grab template file
			File templateFile = Constants.TEMPLATE_PROJECT_FILE;
			
			// create xml file and source directory descriptors
			File xmlFile = new File(projectDir.getPath() + File.separator + projectDir.getName() + ".xml");
			if (xmlFile.exists()) {
				// TODO: warn that a project already exists in this directory
			}
			File srcDir = new File(projectDir.getPath() + File.separator + "modsrc");
			srcDir.mkdirs();

			// parse template XML into DOM structure
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(templateFile);

			// set name of new project to selected directory name
			doc.getElementsByTagName("name").item(0).setTextContent(projectDir.getName());
			// insert project source path
			doc.getElementsByTagName("source-root").item(0).setTextContent(srcDir.getPath());

			// save new xml file to new directory
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperties(Constants.PROJECT_XML_OUTPUT_PROPERTIES);

			// send DOM to file
			tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(xmlFile)));
			
			// add new project to tree
			this.openProject(xmlFile);
			
		} catch (Exception e) {
			logger.log(Level.INFO, "Failed to create project \'" + projectDir.getName() + "\'", e);
		}
	}
	
	/**
	 * Adds a new project detailed within the specified XML file.
	 * @param xmlFile the project XML file
	 */
	public void openProject(File xmlFile) {
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
	
	/**
	 * Removes the project of the specified index.
	 * @param index the project index
	 */
	public void removeProjectAt(int index) {
		TreeNode child = this.root.getChildAt(index);
		this.root.remove(index);
		this.fireTreeNodesRemoved(new int[] { index }, new Object[] { child });
	}
	
	/**
	 * Removes the specifed project from the hierarchy.
	 * @param project the project node to remove
	 */
	public void removeProject(ProjectNode project) {
		if (project != null) {
			int index = this.root.getIndex(project);
			if (index != -1) {
				this.root.remove(project);
				this.fireTreeNodesRemoved(new int[] { index }, new Object[] { project });
			}
		}
	}
	
	/**
	 * Removes the specified project from the hierarchy and deletes all associated files.
	 * @param project the project node to delete
	 */
	public void deleteProject(ProjectNode project) {
		this.removeProject(project);
		if (project != null) {
			project.getProjectFile().delete();
			project.getProjectDirectory().delete();
		}
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
//			return file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER)[index];
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
//			return (file.isFile()) ? 0 :
//				file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER).length;
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
			// leaves may be empty directories or files
			if (file.isDirectory()) {
				return (file.list().length == 0);
			} else {
				return true;
			}
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
	@SuppressWarnings("serial")
	public class ProjectNode extends DefaultMutableTreeNode {
		
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