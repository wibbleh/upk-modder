package model.modtree;

import static model.modtree.ModContext.ModContextType.AFTER_HEX;
import static model.modtree.ModContext.ModContextType.BEFORE_HEX;
import static model.modtree.ModContext.ModContextType.FILE_HEADER;
import static model.modtree.ModContext.ModContextType.HEX_CODE;
import static model.modtree.ModContext.ModContextType.HEX_HEADER;
import static model.modtree.ModContext.ModContextType.VALID_CODE;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import model.upk.UpkFile;

/**
 *
 * @author Amineri
 */
public class ModTree implements TreeModel {
	
	/**
	 * The logger.
	 */
	public static final Logger logger = Logger.getLogger(ModTree.class.getName());
	
	/**
	 * The tree's DocumentListener implementation.
	 */
	protected ModTreeDocumentListener mtListener = new ModTreeDocumentListener();
	
	/**
	 * The tree's list (queue) of pending Document Events to be processed.
	 */
	protected final List<DocumentEvent> docEvents;
	
	/**
	 * The tree's document it is listening to.
	 */
	protected Document doc = null;
	
	/**
	 * Currently registered 
	 */
	private JTree treeViewer;
	
	/**
	 * The tree's root nodes.
	 */
    private ModTreeRootNode currRootNode; // current root node
	private ModTreeRootNode prevRootNode; // copy of previous tree -- used to determine what styles to re-apply
	
	/**
	 * The version number of the document.
	 * Added in MODFILEVERSION=3
	 */
	private int fileVersion = -1;

	/**
	 * The filename of the UPK file associated with this document.
	 * Added in MODFILEVERSION=3
	 */
	private String upkName = "";

	/**
	 * The GUID of the UPK file associated with this document.
	 * Added in MODFILEVERSION=3
	 */
	private String guid = "";

	/**
	 * The name of the targeted UnrealScript function.
	 * Added in MODFILEVERSION=3
	 */
	private String functionName = "";

	/**
	 * The amount to resize the function (optional).
	 * Added in MODFILEVERSION=4
	 */
	private int resizeAmount = 0;

	/**
	 * List of searchable keywords (optional).
	 * Added in MODFILEVERSION=4
	 */
	private List<String> keywords;

	/**
	 * The source UpkFile to use to generate reference mouse-over tips and name references
	 */
	private UpkFile sourceUpk = null;
	
	/**
	 * Flag indicating whether the tree is listening to document updates
	 */
	private boolean updatingEnabled = true;
	
	/**
	 * tracking counter used for debugging performance issues with document styling
	 */
	private static int restylingEvents;
	
	/**
	 * The tree model listeners.
	 */
	private List<TreeModelListener> listeners;
	
	/**
	 * ModTree constructor.
	 * Initializes queue of DocumentEvents.
	 * Registers a DocumentListener with the document.
	 * @param document 
	 * @throws BadLocationException 
	 */
	public ModTree(Document document) throws BadLocationException {
		docEvents = new ArrayList<>();
		this.listeners = new ArrayList<>();
		this.setDocument(document);
	}

	/**
	 * Associates a default document with the ModTree.
	 * Only used for testing purposes.
	 * @throws BadLocationException
	 */
	public ModTree() throws BadLocationException {
		this(new DefaultStyledDocument());
	}

	// TODO : turn off the listener
	public void disableUpdating() {
		this.updatingEnabled = false;
	}
	
	// TODO : turn on the listener
	public void enableUpdating() {
		this.updatingEnabled = true;
	}
	
	/**
	 * Notifies all registered listeners that tree nodes have been removed.
	 */
	private void fireTreeStructureChanged() {
		TreeModelEvent evt = new TreeModelEvent(this, new TreePath(this.currRootNode));
		for (TreeModelListener listener : this.listeners) {
			if(listener != null) {
				listener.treeStructureChanged(evt);
			}
		}
	}
	
	/**
	 * Stores the supplied JTree with the ModTree
	 * @param viewer
	 */
	public void setTreeViewer(JTree viewer) {
		this.treeViewer = viewer;
	}
	
	/**
	 * Sets the source upk to use for hex ref lookups
	 * @param upk
	 */
	public void setSourceUpk(UpkFile upk) {
		this.sourceUpk = upk;
	}
	
	/**
	 * Gets the source upk to use for hex ref lookups
	 * @return 
	 */
	public UpkFile getSourceUpk() {
		return this.sourceUpk;
	}
	
	public void forceRefreshFromDocument() {
		try {
			setDocument(this.doc);
		} catch(BadLocationException ex) {
			logger.log(Level.SEVERE, "Error Setting Document", ex);
		}
	}

	/**
	 * Associates a document with the ModTree.
	 * Registers a DocumentListener with the document.
	 * @param doc StyledDocument to be registered.
	 * @throws javax.swing.text.BadLocationException
	 */
	private void setDocument(Document doc) throws BadLocationException {
		this.doc = doc;
		if (doc.getLength() > 0) {
			String s = doc.getText(0, doc.getLength());
			ModTreeRootNode root = this.getRoot();
			root.insertString(0, s, null);
			root.reorganizeAfterInsertion();
			if(updatingEnabled) {
				this.updateDocument(0,0);
			}
		}
		doc.addDocumentListener(this.mtListener);
	}
	
	/**
	 * Retrieves current document associated with ModTree.
	 * @return
	 */
	public Document getDocument() {
		return doc;
	}
	
	/**
	 * Update the current document based on ModTree structure/content.
	 * Initially will only perform styling.
	 * Later may modify text to implement reference/offset corrections.
	 * @param deltaLines lines inserted or removed
	 * @param lineInsertPoint point where first line change occurs
	 */
	protected void updateDocument(int deltaLines, int lineInsertPoint) {
		if (this.getDocument() == null) {
			return;
		}
		restylingEvents = 0;
		int count = 0;
		int total = 0;
		int oldLineIndex;
		if((this.prevRootNode == null) || (this.currRootNode.getChildCount() <= 1) || (this.prevRootNode.getChildCount() <= 1)) {
			this.updateNodeStyles(this.currRootNode);
		} else if (this.currRootNode.getChildNodeCount() != this.prevRootNode.getChildCount()) {
			for (int newLineIndex = 0; newLineIndex < this.currRootNode.getChildNodeCount(); newLineIndex++) {
				// map newLineIndex to oldLineIndex
				if(deltaLines > 0) { // text added
					if(newLineIndex < lineInsertPoint) { // before the insertion point
						oldLineIndex = newLineIndex;
					} else if (newLineIndex > (lineInsertPoint + deltaLines)) { // after the insertion point
						oldLineIndex = newLineIndex - deltaLines;
					} else { // in the newly inserted text area -- no old lines
						oldLineIndex = -1;
					}
				} else { // text removed
					if (newLineIndex < lineInsertPoint) { // before the deletion area
						oldLineIndex = newLineIndex;
					} else { // above the deletion point
						oldLineIndex = newLineIndex + deltaLines; // skip ahead this number of lines
					}
				}
				if((oldLineIndex >= 0) && (oldLineIndex < this.prevRootNode.getChildCount())) {
					if(lineHasChanged(this.currRootNode.getChildNodeAt(newLineIndex), this.prevRootNode.getChildNodeAt(oldLineIndex))) {
							this.updateNodeStyles(this.currRootNode.getChildNodeAt(newLineIndex));
							count++;
					} 
				} else {
					this.updateNodeStyles(this.currRootNode.getChildNodeAt(newLineIndex));
					count++;
				}
				total++;
			}
		} else {
			for (int i = 0; i < this.currRootNode.getChildNodeCount(); i++) {
				if (lineHasChanged(this.currRootNode.getChildNodeAt(i), this.prevRootNode.getChildNodeAt(i))
						|| (i == lineInsertPoint)) {
						this.updateNodeStyles(this.currRootNode.getChildNodeAt(i));
						count ++;
				}
				total ++;
			}
		}
		this.fireTreeStructureChanged();

		logger.log(Level.INFO, count + " lines out of " + total + " re-styled: " + restylingEvents + " total restyling events");
	}
	
	protected boolean lineHasChanged(ModTreeNode newLine, ModTreeNode oldLine) {
		if((oldLine.getContextFlag(HEX_CODE) || newLine.getContextFlag(HEX_CODE))
						&& newLine.getContextFlag(FILE_HEADER) != oldLine.getContextFlag(FILE_HEADER)) {
							return true;
		}
		return (newLine.isPlainText() != oldLine.isPlainText()) 
				|| ! newLine.getFullText().equals(oldLine.getFullText()) 
				|| newLine.getContextFlag(HEX_CODE) != oldLine.getContextFlag(HEX_CODE) 
				|| newLine.getContextFlag(VALID_CODE) != oldLine.getContextFlag(VALID_CODE) 
				|| newLine.getContextFlag(FILE_HEADER) != oldLine.getContextFlag(FILE_HEADER) 
				|| newLine.getContextFlag(AFTER_HEX) != oldLine.getContextFlag(AFTER_HEX) 
				|| newLine.getContextFlag(BEFORE_HEX) != oldLine.getContextFlag(BEFORE_HEX) 
				|| newLine.getContextFlag(HEX_HEADER) != oldLine.getContextFlag(HEX_HEADER);
	}
	
	/**
	 * Updates associated document for a single node.
	 * Function is used recursively.
	 * @param node The current node being updated for.
	 */
	protected void updateNodeStyles(ModTreeNode node) {
		this.applyStyles(node);
		for (int i = 0; i < node.getChildNodeCount(); i++) {
			this.updateNodeStyles(node.getChildNodeAt(i));
		}
	}

	/**
	 * Applies any necessary styling for the current node to the document.
	 * WARNING : Do not make unnecessary text changes to the document.
	 * Attribute changes are ignored by ModTree.
	 * @param node The ModTreeNode originating the style. 
	 */
	protected void applyStyles(ModTreeNode node) {
		if(!node.isLeaf()) {
			return;
		}

		int start = node.getStartOffset();
		int end = node.getEndOffset();
		boolean replace = true;
		
		// perform attribute updates
		// TODO perform node-to-style mapping in more customizable way
		AttributeSet as = new SimpleAttributeSet(); 
		StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
		StyleConstants.setItalic((MutableAttributeSet) as, false);
		// attempt to style comments separately. 
		if(node.isPlainText()) {
			// find comment marker
			String s = node.getFullText();
			if(s.contains("//")) {
				int startComment = s.indexOf("//");
				start = node.getStartOffset() + startComment;
				end = node.getEndOffset();
				StyleConstants.setForeground((MutableAttributeSet) as, new Color( 128, 128, 128));  // grey
				StyleConstants.setItalic((MutableAttributeSet) as, replace);
			}
		}
		if (node instanceof ModReferenceLeaf) {
			if (node.isVirtualFunctionRef()) {
				StyleConstants.setForeground((MutableAttributeSet) as, new Color(160, 140, 100)); //Color.MAGENTA);
				StyleConstants.setUnderline((MutableAttributeSet) as, true);
			} else {
				StyleConstants.setForeground((MutableAttributeSet) as, new Color(220, 180, 50)); //Color.ORANGE);
				StyleConstants.setUnderline((MutableAttributeSet) as, true);
			}
			end--;
		}
		// invalid code
		if ((node.getContextFlag(HEX_CODE) &&  ! node.getContextFlag(VALID_CODE))) {
			StyleConstants.setForeground((MutableAttributeSet) as, new Color(255, 128, 128)); // red
			StyleConstants.setStrikeThrough((MutableAttributeSet) as, replace);
			end--;
		}
		if(node.getName().equals("OperandToken")) {
			if(node.getFullText().startsWith("0B")) {
				StyleConstants.setForeground((MutableAttributeSet) as, Color.DARK_GRAY);
				StyleConstants.setBold((MutableAttributeSet) as, false);
			} else {
				StyleConstants.setForeground((MutableAttributeSet) as, Color.BLUE);
				StyleConstants.setBold((MutableAttributeSet) as, true);
			}
			end--;
		}
		if (node instanceof ModOffsetLeaf) {
			if(((ModOffsetLeaf)node).getOperand() == null) { // is absolute jump offset
				StyleConstants.setBackground((MutableAttributeSet) as, new Color( 255, 200, 100));  // orange
			} else { // is relative jump offset
				StyleConstants.setBackground((MutableAttributeSet) as, new Color( 255, 255, 180));  // yellow
			}end--;
		}
		((StyledDocument) this.getDocument()).setCharacterAttributes(start, end-start, as, replace);
		restylingEvents ++;
	}
	
	/**
	 * Processes next DocumentEvent in the queue.
	 * Updates the ModTree model, then updates the registered document
	 * TODO -- Event/thread trigger on this when docEvents not empty
	 * @throws BadLocationException 
	 */
	public void processNextEvent() throws BadLocationException {
		if (!docEvents.isEmpty()) {
			this.processDocumentEvent(docEvents.get(0));
		}
	}
	
	/**
	 * Processes a single specified DocumentEvent.
	 * Inserts or removes text and then reorganizes the tree.
	 * @param de The DocumentEvent
	 * @throws BadLocationException
	 */
	protected void processDocumentEvent(DocumentEvent de) throws BadLocationException {
		if (de == null) {
			return;
		}
//		int offset = de.getOffset();
//		int length = de.getLength();
//		String s = de.getDocument().getText(offset, length);
//		ModTreeRootNode r = this.getRoot();
//		r.initUpdateFlags(false);
//		EventType type = de.getType();
//		if (type == EventType.INSERT) {
//			r.insertString(offset, s, null);
//			r.reorganizeAfterInsertion();
//		} else if (type == EventType.REMOVE) {
//			r.remove(offset, length);
//			r.reorganizeAfterDeletion();
//		}
		docEvents.clear();
		if(updatingEnabled) {
			if (de.getDocument().getLength() > 0) {
				// retrieve the new document text
				String s = de.getDocument().getText(0, doc.getLength());
				
				// calculate information about new lines and the insertion point
				String o = this.currRootNode.getFullText();
				int nLines = s.length() - s.replace("\n", "").length();
				int oLines = o.length() - o.replace("\n", "").length();
				int deltaLines = nLines - oLines;
				// TODO: fix out of bounds error when adding to end of document
				int lineInsertPoint = this.currRootNode.getNodeIndex(de.getOffset());
				
				this.prevRootNode = currRootNode;
				this.currRootNode = new ModTreeRootNode(this);
				long startTime = System.currentTimeMillis();
				this.currRootNode.insertString(0, s, null);
				logger.log(Level.INFO, "Inserted text, took " + (System.currentTimeMillis() - startTime) + "ms");
				startTime = System.currentTimeMillis();
				this.currRootNode.reorganizeAfterInsertion();
				logger.log(Level.INFO, "Parsed Text, took " + (System.currentTimeMillis() - startTime) + "ms");

				startTime = System.currentTimeMillis();
				this.updateDocument(deltaLines, lineInsertPoint);
				logger.log(Level.INFO, "Styled Document, took " + (System.currentTimeMillis() - startTime) + "ms");
			}
		}
	}

	/**
	 * Returns the root node of the document.
	 * @return the root node
	 */
	@Override
	public ModTreeRootNode getRoot() {
		if (this.currRootNode == null) {
			// lazily instantiate root node
			this.currRootNode = new ModTreeRootNode(this);
		}
		return this.currRootNode;
	}

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

	/**
	 * Returns the amount to resize this function.
	 * @return the function name
	 */
	public int getResizeAmount() {
		return this.resizeAmount;
	}

	/**
	 * Sets the amount to resize this function.
	 * @param amt the resize amount
	 */
	public void setResizeAmount(int amt) {
		this.resizeAmount = amt;
	}

	/**
	 * Returns the amount to resize this function.
	 * @return the function name
	 */
	public List<String> getKeywords() {
		return this.keywords;
	}

	/**
	 * Adds a keyword to the list of searchable keywords for this function.
	 * @param s the keyword
	 */
	public void addKeyword(String s) {
		// lazily instantiate keywords only if any are present
		if(this.keywords.isEmpty()) {
			this.keywords = new ArrayList<>();
		}
		this.keywords.add(s);
	}

	
	@Override
	public Object getChild(Object node, int i) {
		if(node == this.getRoot()) {
			return this.getRoot().getChildAt(i); 
		} else {
			return ((ModTreeNode) node).getChildAt(i);
		}
	}

	@Override
	public int getChildCount(Object node) {
		if(node == this.getRoot()) {
			return this.getRoot().getChildNodeCount();
		} else {
			return ((ModTreeNode) node).getChildNodeCount();
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		if(node == this.getRoot()) {
			return false;
		} else {
			if(((ModTreeNode) node).isPlainText()) {
				return true;
			} else {
				return ((ModTreeNode) node).isLeaf();
			}
		}
	}

	@Override
	public void valueForPathChanged(TreePath tp, Object o) {
		// not needed as ModTree is not editable within a Tree Pane
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent == this.getRoot()) {
			return this.getRoot().getIndex((ModTreeNode) child);
		} else {
			return ((ModTreeNode) parent).getIndex((ModTreeNode) child);
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener tl) {
			this.listeners.add(tl);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener tl) {
			this.listeners.remove(tl);
	}

	/**
	 * Implements the Listener to be registered with a StyledDocument
	 * // TODO: stop spawning more threads if the first is already running
	 * //		if new insert/remove update comes in could conceivably halt current styling
	 */
	protected class ModTreeDocumentListener implements DocumentListener {

		private Thread deHandler;
	
		@Override
		public void insertUpdate(DocumentEvent evt) {
			this.update(evt);
		}

		@Override
		public void removeUpdate(DocumentEvent evt) {
			this.update(evt);
		}

		@Override
		public void changedUpdate(DocumentEvent evt) {
//			System.out.println("new event: " + evt.getType());
			// do nothing for formatting changes
		}
		
		private void update(DocumentEvent evt) {
			docEvents.add(evt);
			
			// Amineri - my attempt to make the DocEvent processing more efficient -- doesn't work
//			if(this.deHandler == null) {
//				deHandler = new Thread() {
//				   public void run() {
//					   try {
//						   SwingUtilities.invokeAndWait(new Runnable() {
//							   @Override
//							   public void run() {
//								   while (!docEvents.isEmpty()) {
//									   try {
//										   ModTree.this.processNextEvent();
//									   } catch (BadLocationException e) {
//										   e.printStackTrace();
//									   }
//								   }
//							   }
//						   });
//					   } catch (Exception e) {
//						   e.printStackTrace();
//					   }
//				   };
//				};
//				deHandler.start();
//			} else if (this.deHandler.isAlive()) {
//				this.deHandler.interrupt();
//				deHandler = new Thread() {
//				   public void run() {
//					   try {
//						   SwingUtilities.invokeAndWait(new Runnable() {
//							   @Override
//							   public void run() {
//								   while (!docEvents.isEmpty()) {
//									   try {
//										   ModTree.this.processNextEvent();
//									   } catch (BadLocationException e) {
//										   e.printStackTrace();
//									   }
//								   }
//							   }
//						   });
//					   } catch (Exception e) {
//						   e.printStackTrace();
//					   }
//				   };
//				};
//				deHandler.start();
//			}	
			
			
			new Thread() {
				   @Override
				   public void run() {
					   try {
						   SwingUtilities.invokeAndWait(new Runnable() {
							   @Override
							   public void run() {
								   while (!docEvents.isEmpty()) {
									   try {
										   ModTree.this.processNextEvent();
									   } catch (BadLocationException e) {
											logger.log(Level.SEVERE, "Failure in ModTree event processing: " + e);
									   }
								   }
							   }
						   });
					   } catch (InterruptedException | InvocationTargetException e) {
							logger.log(Level.SEVERE, "Failure in ModTree event processing threading: " + e);
					   }
				   };
				}.start();
		}
	}

}
