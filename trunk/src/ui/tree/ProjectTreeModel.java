package ui.tree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ui.Constants;

/**
 * A hybrid tree model combining a tree node-based setup with a file tree model.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ProjectTreeModel extends DefaultTreeModel {

	/**
	 * The logger.
	 */
	public static final Logger logger = Logger.getLogger(ProjectTreeModel.class.getName());
	
	/**
	 * Constructs a project tree model using a default root node.
	 */
	public ProjectTreeModel() {
		super(new DefaultMutableTreeNode("Project Root"));
		// TODO: maybe implement WatchService to detect project file system changes
		// TODO: implement manual tree structure refresh
	}

	/**
	 * Creates a new project the the given name at the specified location
	 * @param projectPath the directory the project will occupy
	 */
	public boolean createProject(Path projectPath) {
		try {
			// grab template file
			File templateFile = Constants.TEMPLATE_PROJECT_FILE;
			
			// create xml file and source directory descriptors
			String projectName = projectPath.getFileName().toString();
			Path xmlPath = Paths.get(projectName + ".xml").resolve(projectPath);
			if (Files.exists(xmlPath)) {
				// abort if a project file already exists
				throw new IOException("A project already exists in the specified directory.");
			}
			Path srcPath = Paths.get("modsrc").resolve(projectPath);
			Files.createDirectories(srcPath);

			// parse template XML into DOM structure
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(templateFile);

			// set name of new project to selected directory name
			doc.getElementsByTagName("name").item(0).setTextContent(projectName);
			// insert project source path
			doc.getElementsByTagName("source-root").item(0).setTextContent(srcPath.toString());

			// save new xml file to new directory
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperties(Constants.PROJECT_XML_OUTPUT_PROPERTIES);

			// send DOM to file
			tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(xmlPath.toFile())));
			
			// add new project to tree
			this.openProject(xmlPath);
			
			return true;
		} catch (IOException | ParserConfigurationException | TransformerException | DOMException | SAXException e) {
			logger.log(Level.INFO, "Failed to create project \'" + projectPath.getFileName() + "\'", e);
		}
		return false;
	}
	
	/**
	 * Adds a new project detailed within the specified XML file.
	 * @param xmlPath the project XML file
	 */
	public void openProject(Path xmlPath) {
		try {
			final ProjectNode projectNode = new ProjectNode(xmlPath);
			MutableTreeNode rootNode = this.getRoot();
			// check whether project already exists
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				if (projectNode.equals(rootNode.getChildAt(i))) {
					// abort
					return;
				}
			}
			final Path srcDir = projectNode.getProjectDirectory();
			// append new project node below root
			this.insertNodeInto(
					projectNode, rootNode,
					this.getChildCount(this.root));
			// walk file tree and add nodes for all files
			Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
				/** Temporary map storing path-to-node mappings. */
				private Map<Path, FileNode> nodeMap = new HashMap<>();
				{	// init map using project source directory
					nodeMap.put(srcDir, projectNode); }
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					if (!dir.equals(srcDir)) {	// skip source directory
						// look up parent node
						FileNode parentNode = nodeMap.get(dir.getParent());
						// create new directory node
						FileNode childNode = new FileNode(dir);
						ProjectTreeModel.this.insertNodeInto(
								childNode, parentNode, parentNode.getChildCount());
						// store path-to-node mapping
						nodeMap.put(dir, childNode);
					}
					return super.preVisitDirectory(dir, attrs);
				}
				
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					// look up parent node
					FileNode parentNode = nodeMap.get(file.getParent());
					// create new generic file node or mod file node
					boolean isModFile = file.toString().endsWith(".upk_mod");
					FileNode childNode = (isModFile) ? new ModFileNode(file) : new FileNode(file);
					ProjectTreeModel.this.insertNodeInto(
							childNode, parentNode, parentNode.getChildCount());
					
					return super.visitFile(file, attrs);
				}
			});
			
			logger.log(Level.INFO, "Project \'" + projectNode + "\' successfully loaded");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.log(Level.INFO, "Failed to load project file \'" + xmlPath.getFileName() + "\'", e);
		}
	}
	
	/**
	 * Removes the project of the specified index.
	 * @param index the project index
	 */
	public void removeProjectAt(int index) {
		this.removeNodeFromParent((MutableTreeNode) this.getChild(this.root, index));
	}
	
	/**
	 * Removes the specifed project from the hierarchy.
	 * @param project the project node to remove
	 * @return <code>true</code> if project removal succeeded, <code>false</code> otherwise
	 */
	public boolean removeProject(ProjectNode project) {
		if (project != null) {
//			int index = this.root.getIndex(project);
//			if (index != -1) {
//				this.root.remove(project);
//				this.fireTreeNodesRemoved(new int[] { index }, new Object[] { project });
//				return true;
//			}
			this.removeNodeFromParent(project);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes the specified project from the hierarchy and deletes all associated files.
	 * @param project the project node to delete
	 */
	public void deleteProject(ProjectNode project) {
		this.removeProject(project);
		// FIXME -- this really needs a warning dialogue !!!
		// TODO: @Amineri: but not here! This is the MainFrame's responsibility! :] 
		if (project != null) {
			try {
				Files.deleteIfExists(project.getProjectDirectory());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to delete project " + project, e);
			}
		}
	}
	
//	/**
//	 * Notifies all registered listeners that tree nodes have been added.
//	 */
//	private void fireTreeNodesInserted(int[] childIndices, Object[] children) {
//		TreeModelEvent evt = new TreeModelEvent(this, new TreePath(this.root), childIndices, children);
//		for (TreeModelListener listener : this.listeners) {
//			listener.treeNodesInserted(evt);
//		}
//	}
//
//	/**
//	 * Notifies all registered listeners that tree nodes have been removed.
//	 */
//	private void fireTreeNodesRemoved(int[] childIndices, Object[] children) {
//		TreeModelEvent evt = new TreeModelEvent(this, new TreePath(this.root), childIndices, children);
//		for (TreeModelListener listener : this.listeners) {
//			listener.treeNodesRemoved(evt);
//		}
//	}

//	/**
//	 * Convenience method checking whether the provided parent is a project
//	 * node or a file itself. In the former case returns the root directory
//	 * of the project, in the latter case returns the node cast to
//	 * <code>File</code>.
//	 * @param node the parent node
//	 * @return a file reference or <code>null</code> if the parent node does
//	 *  not reference a file
//	 */
//	// FIXME: API
//	private Path getPathForNode(Object node) {
//		Path path = null;
//		if (node instanceof ProjectNode) {
//			// node is a project node, use project root directory
//			path = ((ProjectNode) node).getProjectDirectory();
//		} else if (node instanceof Path) {
//			// is a directory below a project's root directory, return file at index
//			path = (Path) node;
//		}
//		return path;
//	}

//	@Override
//	public MutableTreeNode getRoot() {
//		return this.root;
//	}
//
//	@Override
//	public Object getChild(Object parent, int index) {
//		if (parent == this.root) {
//			// parent is the tree root, return project node at index
//			return this.root.getChildAt(index);
//		}
//		Path path = this.getPathForNode(parent);
//		if (path != null) {	// sanity check
//			// return file at index
////			return file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER)[index];
////			return path.listFiles()[index];
//			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
//				int i = 0;
//				for (Path childPath : dirStream) {
//					if (i == index) {
//						return childPath;
//					}
//					i++;
//				}
//			} catch (IOException e) {
//				logger.log(Level.SEVERE, "Unable to fetch children for " + parent, e);
//			}
//		}
//		// fallback value, we should actually never get here
//		logger.log(Level.SEVERE, "Unknown node type in project tree: " + parent);
//		return null;
//	}
//
//	@Override
//	public int getChildCount(Object parent) {
//		if (parent == this.root) {
//			// parent is the tree root, return the number of project nodes
//			return this.root.getChildCount();
//		}
//		Path path = this.getPathForNode(parent);
//		if (path != null) {	// sanity check
//			// if file is directory return number of files and subdirectories in it
////			return (file.isFile()) ? 0 :
////				file.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER).length;
////			return (path.isFile()) ? 0 :
////				path.listFiles().length;
//			if (Files.isRegularFile(path)) {
//				return 0;
//			} else {
//				try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
//					Iterator<Path> iter = dirStream.iterator();
//					int childCount;
//					for (childCount = 0; iter.hasNext(); iter.next()) {
//						childCount++;
//					}
//					return childCount;
//				} catch (IOException e) {
//					logger.log(Level.SEVERE, "Unable to fetch children for " + parent, e);
//				}
//			}
//		}
//		// fallback value, we should actually never get here
//		System.err.println("ERROR: unknown node type in project tree: " + parent);
//		return 0;
//	}
//	
//	@Override
//	public boolean isLeaf(Object node) {
//		if (node == this.root) {
//			// node is the tree root, return whether it has any children
//			return (this.root.getChildCount() == 0);
//		}
//		Path path = this.getPathForNode(node);
//		if (path != null) {	// sanity check
//			// leaves may be empty directories or files
//			if (Files.isDirectory(path)) {
//				try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
//					return !dirStream.iterator().hasNext();
//				} catch (IOException e) {
//					logger.log(Level.SEVERE, "Unable to fetch children for " + node, e);
//				}
//			} else {
//				return true;
//			}
//		}
//		// fallback value, we should actually never get here
//		System.err.println("ERROR: unknown node type in project tree: " + node);
//		return false;
//	}
//
//	@Override
//	public int getIndexOfChild(Object parent, Object child) {
//		if (parent == this.root) {
//			// parent is the tree root, child must be a project node, return its index
//			this.root.getIndex((TreeNode) child);
//		}
//		Path path = this.getPathForNode(parent);
//		if (path != null) {	// sanity check
//			// return index of file inside parent directory
////			return Arrays.asList(
////					path.listFiles((java.io.FileFilter) Constants.MOD_FILE_FILTER)).indexOf(child);
//			try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
//				int index = 0;
//				for (Path childPath : dirStream) {
//					if (childPath.equals(child)) {
//						return index;
//					}
//					index++;
//				}
//				return -1;
//			} catch (IOException e) {
//				logger.log(Level.SEVERE, "Unable to fetch children for " + parent, e);
//			}
//		}
//		// fallback value, we should actually never get here
//		System.err.println("ERROR: unknown node type in project tree: " + parent);
//		return -1;
//	}
//
//	@Override
//	public void addTreeModelListener(TreeModelListener l) {
//		this.listeners.add(l);
//	}
//	
//	@Override
//	public void removeTreeModelListener(TreeModelListener l) {
//		this.listeners.remove(l);
//	}
//	
//	@Override
//	public void valueForPathChanged(TreePath path, Object newValue) {
//		// don't need, tree model is not supposed to be editable
//	}
	
	@Override
	public MutableTreeNode getRoot() {
		return (MutableTreeNode) super.getRoot();
	}
	
	@Override
	public void setRoot(TreeNode root) {
		if (root instanceof MutableTreeNode) {
			super.setRoot(root);
		}
	}
	
	/**
	 * Custom tree node representing a file or directory in the project tree.
	 * @author XMS
	 */
	public class FileNode extends DefaultMutableTreeNode {
		
		/**
		 * Constructs a generic file node from the specified file or directory path.
		 * @param path the path to wrap
		 */
		public FileNode(Path path) {
			super(path);
		}
		
		/**
		 * Returns the path associated with this file node instance.
		 * @return the path
		 */
		public Path getFilePath() {
			return this.getUserObject();
		}
		
		@Override
		public Path getUserObject() {
			return (Path) super.getUserObject();
		}
		
		@Override
		public String toString() {
			return this.getUserObject().getFileName().toString();
		}
		
	}
	
	/**
	 * Custom tree node representing a project in the project tree. Serves
	 * as the root directory for a project file structure.
	 * @author XMS
	 */
	public class ProjectNode extends FileNode {
		
		/**
		 * The project's source directory.
		 */
		private Path projectPath;
		
		/**
		 * The project's name.
		 */
		private String projectName;
		
		/**
		 * Constructs a new project node by parsing XML file at the specified path.
		 * @param xmlPath the path to the project XML file
		 * @throws ParserConfigurationException if a document parser could not be created
		 * @throws IOException if any I/O errors occur
		 * @throws SAXException if any parse errors occur
		 */
		public ProjectNode(Path xmlPath) throws ParserConfigurationException, SAXException, IOException {
			super(xmlPath);
			
			this.parse(xmlPath);
		}

		/**
		 * Parses the project XML file at the specified path and extracts project information from it.
		 * @param xmlPath the path to the project XML file
		 * @throws ParserConfigurationException if a document parser could not be created
		 * @throws IOException if any I/O errors occur
		 * @throws SAXException if any parse errors occur
		 */
		private void parse(Path xmlPath) throws ParserConfigurationException, SAXException, IOException {
			// create document builder
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// parse XML
			Document doc = db.parse(xmlPath.toFile());
			// extract project name and source directory
			this.projectName = doc.getElementsByTagName("name").item(0).getTextContent();
			this.projectPath = Paths.get(doc.getElementsByTagName("source-root").item(0).getTextContent());
		}

		/**
		 * Returns the path to the project XML file.
		 * @return the project XML path
		 */
		public Path getProjectFile() {
			return this.getFilePath();
		}
		
		/**
		 * Returns the path to the project's source directory.
		 * @return the project path
		 */
		public Path getProjectDirectory() {
			return projectPath;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ProjectNode) {
				ProjectNode that = (ProjectNode) obj;
				return this.getProjectFile().equals(that.getProjectFile());
			}
			return false;
		}

		@Override
		public String toString() {
			return this.projectName;
		}
	}
	
	/**
	 * Custom tree node representing a mod file in the project tree.
	 * @author XMS
	 */
	public class ModFileNode extends FileNode {
		
		/**
		 * Constructs a mod file node from the specified mod file path.
		 * @param modPath the mod file path
		 */
		public ModFileNode(Path modPath) {
			super(modPath);
		}
		
	}
	
}