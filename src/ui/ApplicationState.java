package ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Container class for storing (and re-storing) the application state between
 * sessions.
 * @author XMS
 */
public class ApplicationState implements Serializable {

	private static final long serialVersionUID = -4022886405181857720L;

	/**
	 * The collection of previously opened project files.
	 */
	private Set<String> openedProjectFiles;
	
	/**
	 * The list of recently opened project files. Element order corresponds to
	 * order in which files were opened with the most recently opened file at
	 * index 0.
	 */
	private List<String> recentProjectFiles;
	
	/**
	 * Creates a new application state container object instance.
	 */
	public ApplicationState() {
		openedProjectFiles = new HashSet<>();
		recentProjectFiles = new ArrayList<>();
	}
	
	/**
	 * Adds the specified project file path to the lists of currently opened and
	 * recently opened project files.
	 * @param xmlPath the project XML file path to add
	 */
	public void addProjectFile(Path xmlPath) {
		String pathStr = xmlPath.toString();
		openedProjectFiles.add(pathStr);
		
		if (recentProjectFiles.contains(pathStr)) {
			// remove already existing element (effectively moving it to the front)
			recentProjectFiles.remove(pathStr);
		}
		// insert path at front of list
		recentProjectFiles.add(0, pathStr);
		if (recentProjectFiles.size() > 10) {
			// truncate list
			recentProjectFiles.remove(recentProjectFiles.size() - 1);
		}
	}
	
	/**
	 * Removes the specified project file path from the list of currently opened
	 * project files.
	 * @param xmlPath the project XML file path to remove
	 */
	public void removeProjectFile(Path xmlPath) {
		openedProjectFiles.remove(xmlPath.toString());
	}
	
	/**
	 * Returns the collection of previously opened project files.
	 * @return the opened project files
	 */
	public Collection<String> getOpenedProjectFiles() {
		return openedProjectFiles;
	}
	
	/**
	 * Saves the application state to a binary file.
	 */
	public void storeState() {
		Path statePath = Constants.APPLICATION_STATE_FILE;
		
		try {
			// create new application state file
			Files.deleteIfExists(statePath);
			Files.createFile(statePath);

			// serialize app state
			try (ObjectOutputStream oos = new ObjectOutputStream(
					Files.newOutputStream(statePath, StandardOpenOption.WRITE))) {
				oos.writeObject(this);
			}
		} catch (IOException e) {
			// TODO: maybe add logging hook
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads a serialized application state from the default location.
	 * @return the deserialized application state or a new one if deserialization failed
	 */
	public static ApplicationState readState() {
		Path statePath = Constants.APPLICATION_STATE_FILE;
		
		ApplicationState appState;
		try (ObjectInputStream ois = new ObjectInputStream(
				Files.newInputStream(statePath, StandardOpenOption.READ))) {
			// deserialize app state
			appState = (ApplicationState) ois.readObject();
		} catch (Exception e) {
			// create new application properties object if no serialized object could be found/read
			appState = new ApplicationState();
		}
		return appState;
	}
	
}
