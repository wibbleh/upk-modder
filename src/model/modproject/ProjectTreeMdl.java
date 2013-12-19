package model.modproject;

import io.modproject.FileTreeModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * The model for a single project
 * @author Amineri
 */


public class ProjectTreeMdl extends FileTreeModel {

	private String projectName;
	
//	private FileTreeModel root;
		
	public ProjectTreeMdl() {
		super();
	}
	
	public ProjectTreeMdl(File project) {
		super();
		// open and parse project xml file
		try {
			org.w3c.dom.Document doc;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);  // Not important for this demo

			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(project);
			projectName = doc.getElementsByTagName("name").item(0).getTextContent();
			this.root = new File(doc.getElementsByTagName("source-root").item(0).getTextContent());
			System.out.println("XML parsed successfully");
			System.out.println(projectName);
			System.out.println(this.root.getAbsolutePath());
		} catch(FileNotFoundException fnfEx) {
			// TODO: hook up to logger
			System.out.println("Sample XML file not found: " + fnfEx);
		} catch(ParserConfigurationException | SAXException | IOException | DOMException ex) {
			// TODO: hook up to logger
			System.out.println("Unknown Exception: " + ex);
		}
		
	}
	
	public String getName() {
		return this.projectName;
	}

    @Override
    public Object getChild(Object parent, int index) {
		File f;
		if(parent instanceof ProjectTreeMdl) {
			f = ((ProjectTreeMdl) parent).root;
		} else {
			f = (File) parent;
		}
        return f.listFiles()[index];
    }

    @Override
    public int getChildCount(Object parent) {
		File f;
		if(parent instanceof ProjectTreeMdl) {
			f = ((ProjectTreeMdl) parent).root;
		} else {
			f = (File) parent;
		}
        if (!f.isDirectory()) {
            return 0;
        } else {
            return f.list().length;
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
		File par;
		if(parent instanceof ProjectTreeMdl) {
			par = ((ProjectTreeMdl) parent).root;
		} else {
			par = (File) parent;
		}
        File ch = (File) child;
        return Arrays.asList(par.listFiles()).indexOf(ch);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object node) {
		if(node == null ){
			return true;
		}
		if (node instanceof ProjectTreeMdl) {
			File f = ((ProjectTreeMdl) node).root;
			if(f == null) {
				return true;
			}
			return !f.isDirectory();
		} else {
			return super.isLeaf(node);
		}
    }

}
