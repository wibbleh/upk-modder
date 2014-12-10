package ui.trees;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ui.ApplyStatus;
import ui.Constants;

/**
 * A hybrid tree model combining a tree node-based setup with a file tree model.
 * @author XMS
 */
@SuppressWarnings("serial")
public class ProjectTreeModel extends DefaultTreeModel {

        private final WatchService watcher;
        private final Map<WatchKey,Path> watchKeyToPathMap;
        private final Map<Path, WatchKey> pathToWatchKeyMap;

	/**
	 * Persistent map storing path-to-node mappings.
	 */
	protected Map<Path, FileNode> nodeMap;
        
	/**
	 * The logger.
	 */
	public static final Logger logger = Logger.getLogger(ProjectTreeModel.class.getName());
	
	/**
	 * Constructs a project tree model using a default root node.
	 * @throws IOException
	 */
	public ProjectTreeModel() throws IOException {
		super(new DefaultMutableTreeNode("Project Root"));
		// TODO: maybe implement WatchService to detect project file system changes
		// TODO: implement manual tree structure refresh
                this.nodeMap = new HashMap<Path, FileNode>();
                this.watcher = FileSystems.getDefault().newWatchService();
                this.watchKeyToPathMap = new HashMap<WatchKey,Path>();
                this.pathToWatchKeyMap = new HashMap<Path,WatchKey>();
	}

        
     	/**
	 * Retrieves WatchKey for a given path
	 * @param p the path to be checked for an active WatchKey
	 * @return the WatchKey, or null if no active WatchKey exists
	 */
        public WatchKey getWatchKey(Path p) {
            return pathToWatchKeyMap.get(p);
        }

        /**
	 * Creates a new project the the given name at the specified location
	 * @param projectPath the directory the project will occupy
	 * @return the path to the project configuration file
	 */
	public Path createProject(Path projectPath) {
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
			Path srcPath = projectPath.resolve("modsrc");
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
			
			return xmlPath;
		} catch (IOException | ParserConfigurationException | TransformerException | DOMException | SAXException e) {
			logger.log(Level.INFO, "Failed to create project \'" + projectPath.getFileName() + "\'", e);
		}
		return null;
	}
	
	/**
	 * Adds a new project detailed within the specified XML file.
	 * @param xmlPath the project XML file
	 */
	@SuppressWarnings("unchecked")
	public void openProject(Path xmlPath) {
		try {
			final ProjectNode projectNode = new ProjectNode(xmlPath);
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) this.getRoot();
			// check whether project already exists
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				if (projectNode.equals(rootNode.getChildAt(i))) {
					// abort
					return;
				}
			}
			final Path srcDir = projectNode.getProjectDirectory();
                        
			// init map using project source directory
			nodeMap.put(srcDir, projectNode);

			// append new project node below root
			this.insertNodeInto(projectNode, rootNode,
					this.getChildCount(this.root));
			// walk file tree and add nodes for all files
			Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
//				/** Temporary map storing path-to-node mappings. */
//				private Map<Path, FileNode> nodeMap = new HashMap<>();
//				{	// init map using project source directory
//					nodeMap.put(srcDir, projectNode);
//				}
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					if (!dir.equals(srcDir)) {	// skip source directory
						// look up parent node
						FileNode parentNode = nodeMap.get(dir.getParent());
						// create new directory node
						FileNode childNode = new FileNode(dir);
						// find insertion point
						List<FileNode> list = Collections.list(parentNode.children());
						int index = Collections.binarySearch(list, childNode);
						if (index < 0) {	// sanity check, should always be negative
							index = Math.abs(index + 1);
							// insert new node
							ProjectTreeModel.this.insertNodeInto(childNode, parentNode, index);
						}	
//						ProjectTreeModel.this.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
						// store path-to-node mapping
						nodeMap.put(dir, childNode);
					}
                                        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                        ProjectTreeModel.this.watchKeyToPathMap.put(key, dir);
                                        ProjectTreeModel.this.pathToWatchKeyMap.put(dir, key);
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
					// find insertion point
					List<FileNode> list = Collections.list(parentNode.children());
					int index = Collections.binarySearch(list, childNode);
					if (index < 0) {	// sanity check, should always be negative
						index = Math.abs(index + 1);
						// insert new node
						ProjectTreeModel.this.insertNodeInto(childNode, parentNode, index);
					}	
					//ProjectTreeModel.this.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
					nodeMap.put(file, childNode);
					
					return super.visitFile(file, attrs);
				}
				
			});
			
			Enumeration<Object> dfe = rootNode.depthFirstEnumeration();
			while (dfe.hasMoreElements()) {
				Object object = (Object) dfe.nextElement();
				if (object instanceof FileNode) {
					FileNode fileNode = (FileNode) object;
					if (!fileNode.isExcluded()) {
						fileNode.setStatus(ApplyStatus.UNKNOWN);
					}
				}
			}
			
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
	 * Removes the specified project from the hierarchy.
	 * @param project the project node to remove
	 * @return <code>true</code> if project removal succeeded, <code>false</code> otherwise
	 */
	public boolean removeProject(ProjectNode project) {
		if (project != null) {
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
//	@SuppressWarnings("unchecked")
	public ModFileNode createModFile(FileNode dirNode, String filename) {
		Path modPath = dirNode.getFilePath().resolve(filename);
		// TODO: check whether file already exists
		try {
			// FIXME: file watcher interferes with mod file node creation after copying template
			Files.copy(Constants.TEMPLATE_MOD_FILE, modPath);
//			ModFileNode node = new ModFileNode(modPath);
//			// find insertion point
//			List<FileNode> nodes = Collections.list(dirNode.children());
//			int index = Collections.binarySearch(nodes, node);
//			// sanity check, should always be negative
//			if (index < 0) {
//				index = Math.abs(index + 1);
//				// insert new node
//				this.insertNodeInto(node, dirNode, index);
//				return node;
//			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to create mod file " + modPath, e);
		}
		return null;
	}

        @SuppressWarnings("unchecked")
        static <T> WatchEvent<T> cast(WatchEvent<?> event) {
            return (WatchEvent<T>) event;
        }
 
        /**
         * Register the given directory, and all its sub-directories, with the
         * WatchService.
         */
        private void registerAll(final Path start) throws IOException {
            // register directory and sub-directories
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!dir.equals(start)) {	// skip source directory
                        // look up parent node
                        FileNode parentNode = nodeMap.get(dir.getParent());
                        // create new directory node
                        FileNode childNode = new FileNode(dir);
                        ProjectTreeModel.this.insertNodeInto(
                                        childNode, parentNode, parentNode.getChildCount());
                        // store path-to-node mapping
                        nodeMap.put(dir, childNode);
                    }
                                
                    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    ProjectTreeModel.this.watchKeyToPathMap.put(key, dir);
                    ProjectTreeModel.this.pathToWatchKeyMap.put(dir, key);
                    return super.preVisitDirectory(dir, attrs);
                }

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// look up parent node
				FileNode parentNode = nodeMap.get(file.getParent());
				// create new generic file node or mod file node
				boolean isModFile = file.toString().endsWith(".upk_mod");
				FileNode childNode = (isModFile) ? new ModFileNode(file) : new FileNode(file);
				ProjectTreeModel.this.insertNodeInto(
						childNode, parentNode, parentNode.getChildCount());
				nodeMap.put(file, childNode);

				return super.visitFile(file, attrs);
			}
		});
	}

	/**
	 * Remove all child directories/files from both the watch service and node map
	 */
	private void deregisterAll(FileNode root) {
		for (int i = 0; i < root.getChildCount(); i++) {
			FileNode childNode = (FileNode) root.getChildAt(i);
			// update node map for changed directory/file
			Path childPath = childNode.getFilePath();
			if (Files.isDirectory(childPath)) {
				ProjectTreeModel.this.deregisterAll(childNode);
			}
			ProjectTreeModel.this.nodeMap.remove(childPath);
		}
	}

        /**
         * Process all events for keys queued to the watcher
	 * Code copied from <a href="http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java">Oracle docs example</a>
         */
	@SuppressWarnings("unchecked")
	public void processEvents() {
		while (true) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = watchKeyToPathMap.get(key);
			if (dir == null) {
				// FIXME: use logger
//				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				Kind<?> kind = event.kind();

				// TBD - handle OVERFLOW event
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path name = ev.context();
				Path child = dir.resolve(name);

				// FIXME: use logger for this, no sysouts please
//				// print out event
//				System.out.format("%s: %s\n", event.kind().name(), child);
				if (kind == ENTRY_CREATE) {
					// look up parent node
					FileNode parentNode = ProjectTreeModel.this.nodeMap.get(dir);
					if (ProjectTreeModel.this.nodeMap.get(child) == null) {
						// create new generic file node or mod file node
						boolean isModFile = child.toString().endsWith(".upk_mod");
						FileNode childNode = (isModFile) ? new ModFileNode(child) : new FileNode(child);
						// find insertion point
						List<FileNode> nodes = Collections.list(parentNode.children());
						int index = Collections.binarySearch(nodes, childNode);
						// sanity check, should always be negative
						if (index < 0) {
							index = Math.abs(index + 1);
							// insert new node
							ProjectTreeModel.this.insertNodeInto(childNode, parentNode, index);
						} else {
							// TODO: throw error? 
						}

						// update node map for new directory/file
						ProjectTreeModel.this.nodeMap.put(child, childNode);
						// set Unknown status for new child
						if (!childNode.isExcluded()) {
							childNode.setStatus(ApplyStatus.UNKNOWN);
						}
						if (Files.isDirectory(child)) {
							try {
								ProjectTreeModel.this.registerAll(child);
							} catch (IOException ex) {
								// TODO: fix logging
								Logger.getLogger(ProjectTreeModel.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				} else if (kind == ENTRY_DELETE) {
					// look up deleted child node
					FileNode childNode = ProjectTreeModel.this.nodeMap.get(child);
					if (childNode != null) {
						ProjectTreeModel.this.removeNodeFromParent(childNode);
						// update node map for changed directory/file
						if (Files.isDirectory(child)) {
							ProjectTreeModel.this.deregisterAll(childNode);
						}
						ProjectTreeModel.this.nodeMap.remove(child);
					}
				} else if (kind == ENTRY_MODIFY) {
//                        if(ev.count() != 1) { // TBD handle ENTRY_MODIFY events with count > 1
//                        }
//                        else {
//                            if(child.toFile().isDirectory()) {
//                            }
//                            else {
//                                // look up modified child node
//                                FileNode oldChildNode = ProjectTreeModel.this.nodeMap.get(child);
//                                if(oldChildNode != null) {
//                                    //create new child node
//                                    boolean isModFile = child.toString().endsWith(".upk_mod");
//                                    FileNode newChildNode = (isModFile) ? new ModFileNode(child) : new FileNode(child);
//                                    if(oldChildNode.getPath() != newChildNode.getPath()) {
//                                        TreePath treePath = new TreePath(oldChildNode.getPath());
//    //                                    ProjectTreeModel.this.valueForPathChanged(treePath, (Object) newChildNode);
//                                        //update nodeMap for changed directory/file
//                                        ProjectTreeModel.this.nodeMap.put(child, newChildNode);
//                                        if (!newChildNode.isExcluded()) {
//                                            newChildNode.setStatus(ApplyStatus.UNKNOWN);
//                                        }
//                                    }
//                                }
//                            }
//                        }
				}

			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				watchKeyToPathMap.remove(key);
				pathToWatchKeyMap.remove(dir);

				// all directories are inaccessible
				if (watchKeyToPathMap.isEmpty()) {
					break;
				}
			}
		}
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
		 * The last known status of the mod file. Used for display purposes.
		 */
		protected ApplyStatus status;
		
		/**
		 * Constructs a generic file node from the specified file or directory path.
		 * @param path the path to wrap
		 */
		public FileNode(Path path) {
			super(path);
		}
		
		/**
		 * Helper method returning whether this file node is supposed to be
		 * excluded from certain operations.
		 * @return <code>true</code> if excluded, <code>false</code> otherwise
		 */
		protected boolean isExcluded() {
			// double leading underscore is key to ignore 
			if (this.isLeaf() && !(this instanceof ModFileNode)) {
				return true;
			}
			if (userObject != null) {
				return this.getUserObject().getFileName().toString().startsWith("__");
			}
			return false;
		}
		
		/**
		 * Returns the path associated with this file node instance.
		 * @return the path
		 */
		public Path getFilePath() {
			return this.getUserObject();
		}
		
		/**
		 * Returns the project node below which this file node resides (or the
		 * node itself if it is a project node).
		 * @return the parent project node or <code>null</code> if no such node exists
		 */
		public ProjectNode getProject() {
			TreeNode[] path = this.getPath();
			if ((path.length > 1) && (path[1] instanceof ProjectNode)) {
				return (ProjectNode) path[1];
			}
			return null;
		}
		
		/**
		 * Returns the apply state of this file node.
		 * @return the apply state
		 */
		public ApplyStatus getStatus() {
			return status;
		}
		
		/**
		 * Sets the apply state of this file node.
		 * @param status the apply state to set
		 */
		public void setStatus(ApplyStatus status) {
			this.status = status;
			// notify model to force the tree to update
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) this.getParent();
			ProjectTreeModel.this.fireTreeNodesChanged(ProjectTreeModel.this, parent.getPath(),
					new int[] { parent.getIndex(this) }, new Object[] { this });
		}

		@Override
		public Path getUserObject() {
			return (Path) super.getUserObject();
		}
		
		@Override
		public String toString() {
			return this.getUserObject().getFileName().toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FileNode) {
				FileNode that = (FileNode) obj;
				return (this.getFilePath().equals(that.getFilePath()));
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return this.getFilePath().hashCode();
		}
		
		@Override
		public int compareTo(FileNode that) {
			return this.getFilePath().compareTo(that.getFilePath());
		}

		
		/**
		 * Helper method to determine the apply state of a file node by checking
		 * the states of its child nodes.
		 */
		public void determineStatus() {
			ApplyStatus tempStatus = null;
			if (!this.isExcluded()) {
				tempStatus = ApplyStatus.UNKNOWN;
				// init running variables
				boolean allBefore = true;
				boolean allAfter = true;
				boolean allUnknown = true;
				// iterate child nodes
				for (int i = 0; i < this.getChildCount(); i++) {
					FileNode child = (FileNode) this.getChildAt(i);
					// check status
					ApplyStatus childStatus = child.getStatus();
					// check for exclusion conditions
					if ((childStatus != null) && !child.isExcluded()) {
						
						tempStatus = childStatus;
						if (childStatus == ApplyStatus.APPLY_ERROR) {
							// break out of loop, no need to check further on error
							break;
						}
						// skip if mixed status, cannot get any better, but
						// continue to look for errors
						if (tempStatus != ApplyStatus.MIXED_STATUS) {
							// update running variables
							allBefore &= (tempStatus == ApplyStatus.BEFORE_HEX_PRESENT);
							allAfter &= (tempStatus == ApplyStatus.AFTER_HEX_PRESENT);
							allUnknown &= (tempStatus == ApplyStatus.UNKNOWN);
							
							// we have mixed state if all running variables turn
							// out to be the same (i.e. false)
							if (!allBefore && !allAfter && !allUnknown) {
								tempStatus = ApplyStatus.MIXED_STATUS;
							}
						}
					}
				}
			}
			this.setStatus(tempStatus);
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
		 * The map of UPK name-to-UPK path associations.
		 */
		private Map<String, Path> upkAssociations;
		
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
			this.xmlPath = xmlPath;
			this.upkAssociations = new HashMap<>();
			
			// create document builder
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// parse XML
			Document doc = db.parse(xmlPath.toFile());
			// extract project name and source directory
			Node dataNode = doc.getElementsByTagName("data").item(0);
			NodeList childNodes = dataNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				switch (child.getNodeName()) {
				case "name":
					this.projectName = child.getTextContent();
					break;
				case "source-root":
					Path path = Paths.get(child.getTextContent());
					if (!path.isAbsolute()) {
						path = xmlPath.getParent().resolve(path);
					}
					this.userObject = path;
					break;
				case "upk-file":
					NamedNodeMap attributes = child.getAttributes();
					this.upkAssociations.put(attributes.getNamedItem("name").getTextContent(),
							Paths.get(attributes.getNamedItem("path").getTextContent()));
					break;
				default:
					// ignore other tags
					break;
				}
			}
		}
		
		/**
		 * Writes the project meta-information to a new XML file overwriting the
		 * old project XML in the process.
		 */
		private void writeProjectXml() {
			try {
				// grab template file
				File templateFile = Constants.TEMPLATE_PROJECT_FILE;
	
				// parse template XML into DOM structure
				DocumentBuilder db;
					db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(templateFile);
	
				// set name of new project to selected directory name
				doc.getElementsByTagName("name").item(0).setTextContent(projectName);
				// insert project source path
				doc.getElementsByTagName("source-root").item(0).setTextContent(userObject.toString());
				
				Node dataNode = doc.getElementsByTagName("data").item(0);
				for (Entry<String, Path> entry : this.upkAssociations.entrySet()) {
					Element upkElem = doc.createElement("upk-file");
					upkElem.setAttribute("name", entry.getKey());
					upkElem.setAttribute("path", entry.getValue().toString());
					dataNode.appendChild(upkElem);
				}
	
				// send DOM to file
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperties(Constants.PROJECT_XML_OUTPUT_PROPERTIES);
				tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(xmlPath.toFile())));
			
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to save project file " + xmlPath, e);
			}
			
		}
		
		/**
		 * Returns the UPK file path mapped to the specified generic UPK file name.
		 * @param upkName the generic UPK file name
		 * @return the UPK file path
		 */
		public Path getUpkPath(String upkName) {
			return this.upkAssociations.get(upkName);
		}

		/**
		 * Adds a UPK file association mapping.
		 * @param upkName the generic UPK file name
		 * @param upkPath the UPK file path
		 * @return the previous UPK file association mapping or
		 *  <code>null</code> if no mapping existed before
		 */
		public Path addUpkPath(String upkName, Path upkPath) {
			Path prevPath = this.upkAssociations.put(upkName, upkPath);
			// re-create project XML using new associations
			this.writeProjectXml();
			return prevPath;
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
			this.status = ApplyStatus.UNKNOWN;
		}
		
	}

}
