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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	 * @return ?
	 */
	public boolean createProject(Path projectPath) {
		try {
			// grab template file
			File templateFile = Constants.TEMPLATE_PROJECT_FILE;
			
			// create xml file and source directory descriptors
			String projectName = projectPath.getFileName().toString();
			Path xmlPath = projectPath.resolve(projectName + ".xml");
			if (Files.exists(xmlPath)) {
				// abort if a project file already exists
				throw new IOException("A project already exists in the specified directory.");
			}
			Path srcPath = projectPath.resolve("modrc");
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

	/**
	 * Creates and inserts a new mod file under the specified file node using
	 * the specified filename.
	 * @param dirNode the parent directory node
	 * @param filename the filename of the mod file to create
	 * @return the new ModFileNode or <code>null</code> if an error occurred
	 */
	@SuppressWarnings("unchecked")
	public ModFileNode createModFile(FileNode dirNode, String filename) {
		Path modPath = dirNode.getFilePath().resolve(filename);
		// TODO: check whether file already exists
		try {
			Files.copy(Constants.TEMPLATE_MOD_FILE, modPath);
			ModFileNode node = new ModFileNode(modPath);
			// find insertion point
			List<FileNode> list = Collections.list(dirNode.children());
			int index = Collections.binarySearch(list, node);
			if (index < 0) {	// sanity check, should always be negative
				index = Math.abs(index + 1);
				// insert new node
				this.insertNodeInto(node, dirNode, index);
				return node;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to create mod file " + modPath, e);
		}
		return null;
	}
	
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
	public class FileNode extends DefaultMutableTreeNode implements Comparable<FileNode> {
		
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

		//TODO: NetBeans IDE report warning "Generate Missing hashCode()"
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FileNode) {
				FileNode that = (FileNode) obj;
				return (this.getFilePath().equals(that.getFilePath()));
			}
			return false;
		}
		
		@Override
		public int compareTo(FileNode that) {
			return this.getFilePath().compareTo(that.getFilePath());
		}
		
	}
	
	/**
	 * Custom tree node representing a project in the project tree. Serves
	 * as the root directory for a project file structure.
	 * @author XMS
	 */
	public class ProjectNode extends FileNode {
		
		/**
		 * The path to the project XML file.
		 */
		private Path xmlPath;
		
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
			super(null);
			
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
			this.userObject = Paths.get(doc.getElementsByTagName("source-root").item(0).getTextContent());
			
			this.xmlPath = xmlPath;
		}

		/**
		 * Returns the path to the project XML file.
		 * @return the project XML path
		 */
		public Path getProjectFile() {
			return this.xmlPath;
		}
		
		/**
		 * Returns the path to the project's source directory.
		 * @return the project path
		 */
		public Path getProjectDirectory() {
			return this.getFilePath();
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
