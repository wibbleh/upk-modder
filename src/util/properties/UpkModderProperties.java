package util.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Saves the application state to disk for reloading when application is next launched.
 * @author Amineri
 */
public class UpkModderProperties {

	/*
	 * List of currently open projects
	 */
	private static Set<String> openProjects = null;

	/*
	 * List of currently open files
	 */
	private static Set<String> openFiles = null;

	private static final String openPropertiesFileName = "appState.properties";

	/*
	 * Properties mapping from modfile name (relative) to target upk path (absolute)
	 */
	private static Properties fileToUpkMap = null;

	/*
	 * Target UPK properties filename to be used for to store properties for each project
	 */
	private static final String upkPropertiesFilename = "upkState.properties";

	/*
	 * General application configuration properties
	 */
	private static Properties configProperties = null;
	
	
	/*
	 * General application configuration properties filename
	 */
	private static final String configPropertiesFilename = "config.properties";
	
	/*
	 * Constructor for Properties
	 */
	public UpkModderProperties() {
//		openProjects = new HashSet<>();
//		openFiles = new HashSet<>();
//		fileToUpkMap = new Properties();
//		configProperties = new Properties();
	}
	
	public static void setConfigProperty(String key, String value) {
		if(configProperties == null) {
			configProperties = new Properties();
		}
		configProperties.setProperty(key, value);
		saveConfigState();
	}
	
	public static String getConfigProperty(String key) {
		restoreConfigState();
		if(configProperties != null) {
			return configProperties.getProperty(key);
		} else {
			return null;
		}
	}
	
	private static void saveConfigState() {
		if (configProperties == null) {
			return;
		}
		// delete old config properties file
		File file = new File(configPropertiesFilename);
		if (file.exists()) {
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

	private static void restoreConfigState() {
		if(configProperties == null) {
			configProperties = new Properties();
		}
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
	
	public static Set<String> getOpenProjects() {
		restoreOpenState();
		return openProjects;
	}

	public static Set<String> getOpenFiles() {
		restoreOpenState();
		return openFiles;
	}

	public static void addOpenProject(File file) {
		if(openProjects == null) {
			openProjects = new HashSet<>();
		}
		if(openProjects.add(file.getAbsolutePath())) {
			saveOpenState();
		}
	}
	
	public static void removeOpenProject(File file) {
		if (openProjects == null) {
			return;
		}
		if(openProjects.remove(file.getAbsolutePath())) {
			saveOpenState();
		}
	}
	
	public static void addOpenModFile(File file) {
		if(openFiles == null) {
			openFiles = new HashSet<>();
		}
		if(openFiles.add(file.getAbsolutePath())) {
			saveOpenState();
		}
		saveOpenState();
	}
	
	public static void removeOpenModFile(File file) {
		if(openFiles == null) {
			return;
		}
		if(file != null) {
			if(openFiles.remove(file.getAbsolutePath())) {
				saveOpenState();
			}
		}
	}
	
	public static void removeAllOpenModFiles() {
		if(openFiles == null) {
			return;
		}
		openFiles.clear();
		saveOpenState();
	}
	
	/**
	 * Saves "snapshot" of open projects and files to file.
	 * Data stored as List of absolute path filename strings
	 * @param tabPane Contains listing of all open files
	 * @param projectTree Contains listing of all open projects
	 */
	private static void saveOpenState() {
		
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
	private static void restoreOpenState() {
		if(openProjects == null) {
			openProjects = new HashSet<>();
		}
		if(openFiles == null) {
			openFiles = new HashSet<>();
		}
		File file = new File(openPropertiesFileName);
		if( ! file.exists()) {
			return;
		}
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
			openProjects = (Set<String>) in.readObject();
			openFiles = (Set<String>) in.readObject();

				in.close();
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.FINE, "Object read from file");
		} catch(IOException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "IO Error", ex);
		} catch(ClassNotFoundException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.SEVERE, "Class not found", ex);
		} catch(ClassCastException ex) {
			Logger.getLogger(UpkModderProperties.class.getName()).log(Level.INFO, "Properties file invalid");
		}
		
	}
	
	
	/**
	 * Retrieves the previous upkfile associated with supplied modfile.
	 * @param file String name of the modfile
	 * @return Full pathname of the targeted upk, or null if there is nomatch
	 */
	public static String getUpkProperty(String file) {
		if(fileToUpkMap == null) {
			fileToUpkMap = new Properties();
		}
		restoreUpkState();
		return fileToUpkMap.getProperty(file);
	}

	/**
	 * Stores a upkfile to be associated with the given modfile. Overwrites previous association.
	 * @param file modfile used as key
	 * @param upk Full pathname for the target upk.
	 */
	public static void setUpkProperty(String file, String upk) {
		if(fileToUpkMap == null) {
			fileToUpkMap = new Properties();
		}
		if (file != null) {
			fileToUpkMap.setProperty(file, upk);
			saveUpkState();
		}
	}

	/**
	 * Stores target upk info as project properties file
	 */
	private static void saveUpkState() {
		if(fileToUpkMap == null) {
			fileToUpkMap = new Properties();
		}
		
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
	private static void restoreUpkState() {
		if(fileToUpkMap == null) {
			fileToUpkMap = new Properties();
		}

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
