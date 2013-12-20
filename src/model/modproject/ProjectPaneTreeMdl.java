package model.modproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Container class for multiple projects to display in project pane.
 * @author Aminero
 */


public class ProjectPaneTreeMdl extends ProjectTreeMdl{

		private final List<ProjectTreeMdl> projectRoot;

	/**
	 * Construct an empty list with no projects.
	 */
	public ProjectPaneTreeMdl() {
		this.projectRoot = new ArrayList<>();
	}

	/**
	 * Construct a list initially populated with the specified project.
	 * @param project modproject xml file
	 */
	public ProjectPaneTreeMdl(File project) {
		this.projectRoot = new ArrayList<>();
		this.projectRoot.add(new ProjectTreeMdl(project));
	}

	/**
	 * Adds an existing project to the list.
	 * @param project File link to the project xml file
	 */
	public void addProject(File project) {
		projectRoot.add(new ProjectTreeMdl(project));
	}

	/**
	 * Creates a new project directory and xml file at the specified directory.
	 * @param directory File link to the directory to create the new project at
	 */
	public void createNewProject(File directory) {
		// TODO: implement
	}

	/**
	 * Removes the designated project.
	 * @param i project index to remove.
	 */
	public void removeProjectAt(int i) {
		if(i >= 0 && i < this.projectRoot.size()) {
			this.projectRoot.remove(i);
		}
	}

	@Override
	public Object getRoot() {
		return this.projectRoot;
	}

	@Override
	public Object getChild(Object o, int i) {
		if (o.equals(this.projectRoot)) { // getting project
			return this.projectRoot.get(i);
		} else {
			return super.getChild(o, i);
		}
	}

	@Override
	public int getChildCount(Object o) {
		if(o.equals(this.projectRoot)) {
			return this.projectRoot.size();
		} else {
			return super.getChildCount(o);
		}
	}

	@Override
	public boolean isLeaf(Object o) {
		if(o.equals(this.projectRoot)) {
			return this.projectRoot.isEmpty();
		} else {
			return super.isLeaf(o);
		}
	}

	@Override
	public int getIndexOfChild(Object o, Object o1) {
		if(o.equals(this.projectRoot)) {
			return this.projectRoot.indexOf(o1);
		} else {
			return super.getIndexOfChild(o, o1);
		}
	}
}


