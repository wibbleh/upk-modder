package util.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import javax.swing.tree.MutableTreeNode;
import ui.MainFrame.ProjectTreeModel;
import ui.ModTab;

/**
 * Saves the application state to disk for reloading when application is next launched.
 * @author Amineri
 */
public class UpkModderProperties {

	/*
	 * List of currently open projects
	 */
	private List<String> openProjects;

	/*
	 * List of currently open files
	 */
	private List<String> openFiles;

	private final String openPropertiesFileName = "appState.properties";

	/*
	 * Properties mapping from modfile name (relative) to target upk path (absolute)
	 */
	private final Properties fileToUpkMap;

	/*
	 * Target UPK properties filename to be used for to store properties for each project
	 */
	private final String upkPropertiesFilename = "upkState.properties";

	/*
	 * General application configuration properties
	 */
	private final Properties configProperties;
	
	
	/*
	 * General application configuration properties filename
	 */
	private final String configPropertiesFilename = "config.properties";
	
	/*
	 * Constructor for Properties
	 */
	public UpkModderProperties() {
		openProjects = new ArrayList<>();
		openFiles = new ArrayList<>();
		fileToUpkMap = new Properties();
		configProperties = new Properties();
	}
	
	public void setConfigProperty(String key, String value) {
		configProperties.setProperty(key, value);
		saveConfigState();
	}
	
	public String getConfigProperty(String key) {
		return configProperties.getProperty(key);
	}
	
	public void saveConfigState() {
		// delete old config properties file
		File file = new File(configPropertiesFilename);
		if(file.exists()) {
			// delete old file
			file.delete();
		}
		
		// store properties file
		try {
    		configProperties.store(new FileOutputStream(file), null);
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "Upk mapping properties written.");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error writing upk mapping properties", ex);
		}
	}

	public void restoreConfigState() {
		// check that properties file exists
		File file = new File(configPropertiesFilename);
		if( ! file.exists()) {
			return;
		}
		try {
			configProperties.load(new FileInputStream(file));
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "upk mapping properties read.");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error reading upk mapping properties", ex);
		}
	}
	
	public List<String> getOpenProjects() {
		return openProjects;
	}

	public List<String> getOpenFiles() {
		return openFiles;
	}

	/**
	 * Saves "snapshot" of open projects and files to file.
	 * Data stored as List of absolute path filename strings
	 * @param tabPane Contains listing of all open files
	 * @param projectTree Contains listing of all open projects
	 */
	public void saveOpenState(JTabbedPane tabPane, ProjectTreeModel projectTree) {
		// clear prior open projects state
		if(openProjects == null) {
			openProjects = new ArrayList<>();
		}
		openProjects.clear();

		// store open project absolute paths 
		if(projectTree != null) {
			MutableTreeNode root = projectTree.getRoot();
//			List<ProjectTreeMdl> root = (List<ProjectTreeMdl>) projectPaneTree.getRoot();
			int projectCount = root.getChildCount();
			for(int i = 0; i < projectCount; i ++) {
				openProjects.add(projectTree.getProjectFileAt(i).getAbsolutePath());
			}
		}
		// clear prior open files state
		if(openFiles == null) {
			openFiles = new ArrayList<>();
		}
		openFiles.clear();

		// store open file absolute paths
		if(tabPane != null) {
			int tabCount = tabPane.getTabCount();
			for(int i = 0; i < tabCount; i ++) {
				ModTab tab = (ModTab) tabPane.getComponentAt(i);
				File file = tab.getModFile();
//				if(file.exists()) {
					String filename = file.getAbsolutePath();
					openFiles.add(filename);
//				}
			}
		}
		
		File file = new File(openPropertiesFileName);
		if(file.exists()) {
			// delete old file
			file.delete();
		}
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
			out.writeObject(openProjects);
			out.writeObject(openFiles);

			out.flush();
			out.close();
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "Object written to file");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error", ex);
		}
	}

	/**
	 * Restores the internal state of the class (List of project and file names).
	 * Lists can be retrieved and opened.
	 */
	public void restoreOpenState() {
		File file = new File(openPropertiesFileName);
		if( ! file.exists()) {
			return;
		}
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
			openProjects = (List<String>) in.readObject();
			openFiles = (List<String>) in.readObject();

			in.close();
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "Object read from file");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error", ex);
		} catch(ClassNotFoundException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "Class not found", ex);
		}
	}
	
	
	/**
	 * Retrieves the previous upkfile associated with supplied modfile.
	 * @param file String name of the modfile
	 * @return Full pathname of the targeted upk, or null if there is nomatch
	 */
	public String getUpkProperty(String file) {
		return fileToUpkMap.getProperty(file);
	}

	/**
	 * Stores a upkfile to be associated with the given modfile. Overwrites previous association.
	 * @param file modfile used as key
	 * @param upk Full pathname for the target upk.
	 */
	public void setUpkProperty(String file, String upk) {
		fileToUpkMap.setProperty(file, upk);
		saveUpkState();
	}

	/**
	 * Stores target upk info as project properties file
	 */
	public void saveUpkState() {
		
		// delete old project properties file
		File file = new File(upkPropertiesFilename);
		if(file.exists()) {
			// delete old file
			file.delete();
		}
		
		// store properties file
		try {
    		fileToUpkMap.store(new FileOutputStream(file), null);
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "Upk mapping properties written.");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error writing upk mapping properties", ex);
		}
	}

	/**
	 * Restores the internal state of the class (Properties map from file to target upk).
	 * Properties can then be used to re-link target upks when file is opened.
	 */
	public void restoreUpkState() {

		// check that properties file exists
		File file = new File(upkPropertiesFilename);
		if( ! file.exists()) {
			return;
		}
		try {
			fileToUpkMap.load(new FileInputStream(file));
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "upk mapping properties read.");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error reading upk mapping properties", ex);
		}
	}

}
