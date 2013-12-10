package model.modtree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import static model.modtree.ModContext.ModContextType.*;

/**
 *
 * @author Amineri
 */


public class ModTree implements Runnable {

	@Override
	public void run() {
		try {
			Thread.sleep(50);
		} catch(InterruptedException ex) {
			Logger.getLogger(ModTree.class.getName()).log(Level.SEVERE, null, ex);
		}
		while(!docEvents.isEmpty()) {
			processNextEvent();
		}
	}
	
	/**
	 * Implements the Listener to be registered with a StyledDocument
	 */
	protected class ModTreeListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent de) {
			if(de.getDocument() == doc) {
				if(de.getType() == DocumentEvent.EventType.INSERT) {
					docEvents.add(de);
					(new Thread(new ModTree())).start();
				}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			if(de.getDocument() == doc) {
				if(de.getType() == DocumentEvent.EventType.REMOVE) {
					docEvents.add(de);
					(new Thread(new ModTree())).start();
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
	protected ModTreeListener mtListener;
	
	/**
	 * The tree's list (queue) of pending Document Events to be processed.
	 */
	protected final List<DocumentEvent> docEvents;
	
	/**
	 * The tree's document it is listening to.
	 */
	protected StyledDocument doc = null;
	
	/**
	 * The tree's root node.
	 */
    private ModTreeRootNode rootNode;
    
	/**
	 * The array of the tree's root nodes. Always contains only a single node.
	 */
	@Deprecated
	private final ModTreeRootNode[] rootElements = new ModTreeRootNode[1];

	
	// TODO -- Should retrieve this information from ModDocument if stored there
	// @ XMTS : Would it make more sense to store these values here? Does the document even need them?
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

	/**
	 * ModTree constructor.
	 * Initializes queue of 10 DocumentEvents.
	 */
	public ModTree() {
		docEvents = new ArrayList<>(10);
	}

	/**
	 * Associates a document with the ModTree.
	 * Registers a DocumentListener with the document.
	 * @param document StyledDocument to be registered.
	 * @throws javax.swing.text.BadLocationException
	 */
	public void setDocument(StyledDocument document) throws BadLocationException {
		this.doc = document;
		mtListener = new ModTreeListener();
		if(doc.getLength() > 0) {
			String s = doc.getText(0, doc.getLength());
			getDefaultRootNode().insertString(0, s, null);
			getDefaultRootNode().reorganizeAfterInsertion();
			updateDocument();
		}
		doc.addDocumentListener(mtListener);
	}
	
	/**
	 * Retrieves current document associated with ModTree.
	 * @return
	 */
	public StyledDocument getDocument() {
		return doc;
	}
	
	/**
	 * Update the current document based on ModTree structure/content.
	 * Initially will only perform styling.
	 * Later may modify text to implement reference/offset corrections.
	 */
	protected void updateDocument() {
		if(getDocument() == null) { return; }
		updateNode(getDefaultRootNode());
		
	}
	
	/**
	 * Updates associated document for a single node.
	 * Function is used recursively
	 * @param node The current node being updated for.
	 */
	protected void updateNode(ModTreeNode node) {
		applyStyle(node);
		for(int i = 0; i < node.getChildNodeCount() ; i ++ ) {
			updateNode(node.getChildNodeAt(i));
		}
	}

	/**
	 * Applies any necessary styling for the current node to the document.
	 * WARNING : Do not make unnecessary text changes to the document.
	 * Attribute changes are ignored by ModTree.
	 * @param node The ModTreeNode originating the style. 
	 */
	protected void applyStyle(ModTreeNode node) {
		int start = node.getStartOffset();
		int end = node.getEndOffset();
		boolean replace = true;
		
		// perform attribute updates
		AttributeSet as = new SimpleAttributeSet();  // TODO perform node-to-style mapping
		StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
		StyleConstants.setItalic((MutableAttributeSet) as, false);
		if(node.getName().equals("ModReferenceToken")) {
			if(node.isVFFunctionRef()) {
				StyleConstants.setForeground((MutableAttributeSet) as, Color.MAGENTA);
				StyleConstants.setUnderline((MutableAttributeSet) as, true);
			} else {
				StyleConstants.setForeground((MutableAttributeSet) as, Color.ORANGE);
				StyleConstants.setUnderline((MutableAttributeSet) as, true);
			}
		}
		if(node.getContextFlag(HEX_CODE) && !node.getContextFlag(VALID_CODE)) {
			StyleConstants.setBackground((MutableAttributeSet) as, Color.RED);
		}
		if(node.getName().equals("OperandToken")) {
			if(node.toStr().startsWith("0B")) {
				StyleConstants.setForeground((MutableAttributeSet) as, Color.DARK_GRAY);
				StyleConstants.setBold((MutableAttributeSet) as, false);
				
			} else {
				StyleConstants.setForeground((MutableAttributeSet) as, Color.BLUE);
				StyleConstants.setBold((MutableAttributeSet) as, true);
			}
		}
		if(node.getName().contains("Jump")) {
			StyleConstants.setBackground((MutableAttributeSet) as, new Color(255, 255, 128));
		}
		getDocument().setCharacterAttributes(start, end, as, replace);
		
	}
	
	/**
	 * Processes next DocumentEvent in the queue.
	 * Updates the ModTree model, then updates the registered document
	 * TODO -- Event/thread trigger on this when docEvents not empty
	 */
		public void processNextEvent() {
		if(!docEvents.isEmpty()) {
			try {
				processDocumentEvent(docEvents.get(0));
				docEvents.remove(0);
				if(getDocument() != null) {
					updateDocument();
				}
			} catch(BadLocationException ex) {
				Logger.getLogger(ModTree.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	/**
	 * Processes a single specified DocumentEvent.
	 * Inserts or removes text and then reorganizes the tree.
	 * @param de The DocumentEvent
	 * @throws BadLocationException
	 */
	protected void processDocumentEvent(DocumentEvent de) throws BadLocationException {
		if(de == null) {return;}
		int offset = de.getOffset();
		int length = de.getLength();
		String s = de.getDocument().getText(offset, length);
		ModTreeRootNode r = getDefaultRootNode();
		if(de.getType() == DocumentEvent.EventType.INSERT) {
			r.insertString(offset, s, null);
			r.reorganizeAfterInsertion();
		} else if (de.getType() == DocumentEvent.EventType.REMOVE) {
			r.remove(offset, length);
			r.reorganizeAfterDeletion();
		}
	}
	
	/**
	 * Returns the root node of the document.
	 * @return the root node
	 */
	public ModTreeRootNode getDefaultRootNode() {
		if (this.rootNode == null) {
			// lazily instantiate root node
			this.rootNode = new ModTreeRootNode(this);
		}
		return this.rootNode;
	}

	// TODO: redirect these calls to the Document if necessary
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
