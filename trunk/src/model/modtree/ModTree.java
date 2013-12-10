package model.modtree;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Amineri
 */


public class ModTree {
	
	private class ModTreeListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent de) {
			if(de.getDocument() == doc) {
				if(de.getType() == DocumentEvent.EventType.INSERT) {
					docEvents.add(de);
				}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			if(de.getDocument() == doc) {
				if(de.getType() == DocumentEvent.EventType.REMOVE) {
					docEvents.add(de);
				}
			}
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
			if(de.getDocument() == doc) {
				if(de.getType() == DocumentEvent.EventType.CHANGE) {
					// do nothing for formatting changes
				}
			}
		}
	}
	
	/**
	 * The tree's DocumentListener implementation.
	 */
	private ModTreeListener mtListener;
	
	/**
	 * The tree's list of pending Document Events to be processed.
	 */
	private final List<DocumentEvent> docEvents;
	
	/**
	 * The tree's document it is listening to.
	 */
	private StyledDocument doc = null;
	
	/**
	 * The tree's root element.
	 */
    private ModTreeRootNode rootElement;
    
	/**
	 * The array of the tree's root elements. Always contains only a single element.
	 */
	@Deprecated
	private final ModTreeRootNode[] rootElements = new ModTreeRootNode[1];

	
	// TODO -- Probably should retrieve this information from ModDocument if stored there
	/**
	 * The version number of the document.
	 */
	private int fileVersion = -1;

	/**
	 * The filename of the UPK file associated with this document.
	 */
	private String upkName = "";

	/**
	 * The GUID of the UPK file associated with this document.
	 */
	private String guid = "";

	/**
	 * The name of the targeted UnrealScript function.
	 */
	private String functionName = "";

	
	
	public ModTree() {
		docEvents = new ArrayList<>(10);
	}

	public void setDocument(StyledDocument d) {
		this.doc = d;
		mtListener = new ModTreeListener();
		if(doc.getLength() > 0) {
			// TODO retrieve initial text for already non-empty Document
		}
		doc.addDocumentListener(mtListener);
	}
	
	public StyledDocument getDocument() {
		return doc;
	}
	
	private void updateDocument() {
		// TODO -- scan through ModTree and perform style/text changes to Document as appropriate
	}
	
	// TODO -- Event/thread trigger on this when docEvents not empty
	private void processNextEvent() {
		if(!docEvents.isEmpty()) {
			try {
				processDocumentEvent(docEvents.get(0));
				docEvents.remove(0);
				updateDocument();
			} catch(BadLocationException ex) {
				Logger.getLogger(ModTree.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private void processDocumentEvent(DocumentEvent de) throws BadLocationException {
		int offset = de.getOffset();
		int length = de.getLength();
		String s = doc.getText(offset, length);
		if(de.getType() == DocumentEvent.EventType.INSERT) {
			rootElement.insertString(offset, s, null);
			rootElement.reorganizeAfterInsertion();
		} else if (de.getType() == DocumentEvent.EventType.REMOVE) {
			rootElement.remove(offset, length);
			rootElement.reorganizeAfterDeletion();
		}
	}
	
    /**
     * Returns array of root elements.<br>
     * For <code>ModDocument</code> this will always be length 1 array.
     * @return
     */
	@Deprecated
	public ModTreeNode[] getRootElements() {
		if (this.rootElements[0] == null) {
			// lazily instantiate root element
			this.getDefaultRootElement();
		}
		return this.rootElements;
	}

	/**
	 * Returns the root element of the document.
	 * @return the root element
	 */
	public ModTreeRootNode getDefaultRootElement() {
		if (this.rootElement == null) {
			// lazily instantiate root element
			this.rootElement = new ModTreeRootNode(this);
			this.rootElements[0] = this.rootElement;
		}
		return this.rootElement;
	}

	// TODO: redirect these calls to the Document
	/**
	 * Returns the file version number.
	 * @return the version number
	 */
	public int getFileVersion() {
		return this.fileVersion;
	}

	/**
	 * Sets the file version number
	 * @param fileVersion the version number to set.
	 */
	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}

	/**
	 * Returns the filename of the UPK file associated with this document.
	 * @return the UPK filename
	 */
	public String getUpkName() {
		return this.upkName;
	}

	/**
	 * Sets the filename of the UPK file associated with this document.
	 * @param upkName the filename to set
	 */
	public void setUpkName(String upkName) {
		this.upkName = upkName;
	}

	/**
	 * Returns the GUID of the UPK file associated with this document.
	 * @return the GUID
	 */
	public String getGuid() {
		return this.guid;
	}

	/**
	 * Sets the GUID of the UPK file associated with this document.
	 * @param guid the GUID to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * Returns the name of the targeted UnrealScript function.
	 * @return the function name
	 */
	public String getFunctionName() {
		return this.functionName;
	}

	/**
	 * Sets the name of the targeted UnrealScript function.
	 * @param functionName the function name
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

}
