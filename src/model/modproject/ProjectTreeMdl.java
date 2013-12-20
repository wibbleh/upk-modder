package model.modproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * The model for a single project
 * @author Amineri
 */


public class ProjectTreeMdl extends FileTreeMdl {

	private String projectName;
	
//	private FileTreeMdl root;
		
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
		if(parent instanceof ProjectTreeMdl) {
			parent = ((ProjectTreeMdl) parent).root;
		} 
		return super.getChild(parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
		if(parent instanceof ProjectTreeMdl) {
			parent = ((ProjectTreeMdl) parent).root;
		} 
		return super.getChildCount(parent);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
		if(parent instanceof ProjectTreeMdl) {
			parent = ((ProjectTreeMdl) parent).root;
		}
		return super.getIndexOfChild(parent, child);
    }

    @Override
    public boolean isLeaf(Object node) {
		if(node == null ){
			return true;
		}
		if (node instanceof ProjectTreeMdl) {
			node = ((ProjectTreeMdl) node).root;
			if(node == null) {
				return true;
			}
		}
		return super.isLeaf(node);
    }

}
