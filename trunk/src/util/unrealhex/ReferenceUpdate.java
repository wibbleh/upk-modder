package util.unrealhex;

import static util.unrealhex.HexStringLibrary.convertIntToHexString;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import model.modtree.*;
import model.upk.UpkFile;
import util.unrealhex.HexStringLibrary;
import static util.unrealhex.HexStringLibrary.convertByteArrayToHexString;

/**
 * Purpose of this class is to build a complete mapping of references for a given ModTree.
 * If the mapping is completely successful then the changes are applied to a given Document.
 * @author Amineri
 */


public class ReferenceUpdate {
	
	private Document document;									// Document reference updates to be applied to
	private List<Integer> referenceOffsets = new ArrayList<>();		// location of references within Document -- used for replacement

	private UpkFile sourceUpk;										// Source upk for value-to-name conversion
	private UpkFile destUpk;										// Destination upk for name-to-value conversion
	
	private ModTree tree;									// ModTree to use to locate references
	
	private List<Integer> sourceReferences = new ArrayList<>();		// List of all references -- found in ModTree
	private List<String> referenceFullNames = new ArrayList<>();	// FullyReference names for each reference in SourceReferences
	private List<Integer> destReferences = new ArrayList<>();		// New reference value for target upk
	private List<Boolean> isVirtualFunction = new ArrayList<>();	// boolean recording which references are virtual functions, so are references to namelist
	private List<Boolean> destRefError = new ArrayList<>();			// boolean recording whether destination reference found
	
	private List<Integer> failedMappings = new ArrayList<>();		// list of references that failed to map to a valid name
	private List<Integer> failedOffsets = new ArrayList<>();		// list of file offset positions of failed references
	private List<Integer> failedTypes = new ArrayList<>();			// TODO-- convert to enumeration of failure modes
																		// 1 = SOURCE_TO_NAME_FAIL = 1
																		// 2 = NAME_TO_DST_FAIL = 2
																		// 3 = SOURCE STRING MISMATCH = 3

	private int failureMode = 0;	// TODO -- convert to enumeration of high-level failure modes
									// 0 = NO ERROR
									// 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
									// 2 = FILE READ ERROR
									// 3 = SOURCE GUID MISMATCH
									// 4 = FILE WRITE ERROR
									// 5 = GUID MISMATCH
									// 6 = NO DEST UPK
	
	public ReferenceUpdate(ModTree tree, Document doc, UpkFile src, UpkFile dst) {
		this.document = doc;
		this.tree = tree;
		this.sourceUpk = src;
		this.destUpk = dst;
		buildSourceReferences();
		buildReferenceFullNames();
		buildDestReferences();
//		dumpData();
	}

	public ReferenceUpdate(ModTree tree, Document doc, UpkFile src) {
		this.document = doc;
		this.tree = tree;
		this.sourceUpk = src;
		this.destUpk = null;
		buildSourceReferences();
		buildReferenceFullNames();
	}
	
	private void dumpData() {
		for(int i = 0 ; i < sourceReferences.size(); i ++) {
			System.out.println("(" + isVirtualFunction.get(i) + ")" + i + " : " 
					+ sourceReferences.get(i) + " : " 
					+ referenceFullNames.get(i) + " : "
					+ destReferences.get(i) + " : " 
					+ referenceOffsets.get(i)); 
		}
	}
	
	public List<Integer> getSourceReferences() {
		return sourceReferences;
	}
	
	public List<Integer> getDestReferences() {
		return destReferences;
	}
	
	public List<Boolean> getDestRefErrors() {
		return destRefError;
	}
	
	public List<String> getReferenceNames() {
		return referenceFullNames;
	}
	
	// TODO : replace with new methods based on enumeration
	// temporary error reporting methods
	public int getFailureMode(){
		return failureMode;
	}
	
	public List<Integer> getFailedMappings(){
		return failedMappings;
	}
	
	public List<Integer> getFailedOffsets(){
		return failedOffsets;
	}
	
	public List<Integer> getFailedTypes(){
		return failedTypes;
	}

	/**
	 * Updates all reference values in document to the source upk name
	 * @return true if mapping completed successfully. false on error.
	 */
	public boolean updateDocumentToName() {
		boolean success = testUpdateDocumentToValue(false);
		success = success && replaceGUID();
		if(success) {
			int offsetIncrease = 0;
			String name;
			for(int i = 0; i < sourceReferences.size(); i ++) {
				try {
					// arbitrary default AttributeSet
					AttributeSet as = new SimpleAttributeSet(); // TODO perform node-to-style mapping
					StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
					StyleConstants.setItalic((MutableAttributeSet) as, false);
					
					// format name replacement
					name = this.referenceFullNames.get(i);
					if(this.isVirtualFunction.get(i)) {
						name = "<|" + name + "|> ";
					}
					else {
						name = "{|" + name + "|} ";
					}
					
					// remove old reference and insert new one
					document.remove(this.referenceOffsets.get(i) + offsetIncrease, 12);
					document.insertString(this.referenceOffsets.get(i) + offsetIncrease, name, as);
					offsetIncrease += (name.length() - 12);
				} catch(BadLocationException ex) {
					Logger.getLogger(ReferenceUpdate.class.getName()).log(Level.SEVERE, null, ex);
					failureMode = 4; // 2 = FILE WRITE ERROR
					return false;
				}
			}
		}
		return success;
	}

	
	/**
	 * Updates all reference values in document to the destination UPK's value
	 * @return true if mapping completed successfully. false on error.
	 */
	public boolean updateDocumentToValue() {
		if(destUpk == null) {
			failureMode = 6; // 6 = NO DEST UPK
			return false;
		}
		boolean success = testUpdateDocumentToValue(false);
		success = success && replaceGUID();
		if(success) {
			for(int i = 0; i < sourceReferences.size(); i ++) {
				if((this.destReferences.get(i) <= 0 && isVirtualFunction.get(i))
						|| (this.destReferences.get(i) == 0 && !isVirtualFunction.get(i))){
					success = false;
				} else {
					try {
						// arbitrary default AttributeSet
						AttributeSet as = new SimpleAttributeSet(); // TODO perform node-to-style mapping
						StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
						StyleConstants.setItalic((MutableAttributeSet) as, false);

						// remove old reference and insert new one
						document.remove(this.referenceOffsets.get(i), 12);
						String docNewString = convertIntToHexString(destReferences.get(i));
						document.insertString(this.referenceOffsets.get(i), docNewString, as);
					} catch(BadLocationException ex) {
						Logger.getLogger(ReferenceUpdate.class.getName()).log(Level.SEVERE, null, ex);
						failureMode = 4; // 2 = FILE WRITE ERROR
						return false;
					}
				}
			}
		}
		return success;
	}

	/**
	 * Replaces the GUID in the document with the GUID from the destination upk
	 * @return true if success, false if failure
	 */
	protected boolean replaceGUID() {
		boolean success = true;
		for (int i = 0; i < this.tree.getRoot().getChildNodeCount() ; i++) {
			ModTreeNode line = this.tree.getRoot().getChildNodeAt(i);
			if(line.getFullText().startsWith("GUID=")) {
				String guid = tree.getGuid();
				int lineOffset = line.getFullText().indexOf(guid);
				int offset = line.getStartOffset() + lineOffset;
				try {
					String docGUID = document.getText(offset, guid.length());
					if(!guid.equals(docGUID)) {
						failureMode = 5; // 5 = GUID MISMATCH 
						return false;
					}
					String destGUID = convertByteArrayToHexString(destUpk.getHeader().getGUID()).trim();
					document.remove(offset, guid.length());
					// arbitrary default AttributeSet
					AttributeSet as = new SimpleAttributeSet(); // TODO perform node-to-style mapping
					StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
					StyleConstants.setItalic((MutableAttributeSet) as, false);
					document.insertString(offset, destGUID, as);
				} catch(BadLocationException ex) {
					Logger.getLogger(ReferenceUpdate.class.getName()).log(Level.SEVERE, null, ex);
					success = false;
				}
			}
		}
		return success;
	}
	
	/**
	 * Verifies that the document GUID and designated source GUID match
	 * @return true if match, false if no match
	 */
	public boolean verifySourceGUID() {
		if(tree.getGuid().trim().equalsIgnoreCase(convertByteArrayToHexString(sourceUpk.getHeader().getGUID()).trim())) {
			return true;
		} else {
			failureMode = 5; // 5 = GUID MISMATCH 
			return false;
		}
	}

	/**
	 * Performs a non-destructive test to see whether the references can be replaced with source upk names
	 * @param recordFailures flag - record failed references to failure list
	 * @return true if success, false if failure
	 */
	public boolean testUpdateDocumentToName(boolean recordFailures)
	{
		boolean success = verifySourceGUID();
		if(sourceReferences.size() != referenceFullNames.size()) {
			failureMode = 1; // 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
			return false;
		}
		if(sourceReferences.size() != referenceOffsets.size()) {
			failureMode = 1; // 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
			return false;
		}
		return success && (this.failedMappings.isEmpty());
	}

	/**
	 * Performs a non-destructive test to see whether the references can be replaced with destination upk values
	 * @param recordFailures flag - record failed references to failure list
	 * @return true if success, false if failure
	 */
	public boolean testUpdateDocumentToValue(boolean recordFailures)
	{
		boolean success = verifySourceGUID();
		if(sourceReferences.size() != destReferences.size()) {
			failureMode = 1; // 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
			return false;
		}
		if(sourceReferences.size() != referenceOffsets.size()) {
			failureMode = 1; // 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
			return false;
		}
		for(int i = 0; i < sourceReferences.size(); i++) {
			String docOriginalString;
			if(!failedMappings.isEmpty()) {
				success = false;
			}
			if((this.destReferences.get(i) <= 0 && isVirtualFunction.get(i))
					|| (this.destReferences.get(i) == 0 && !isVirtualFunction.get(i))){
				success = false;
			} else {
				try {
					docOriginalString = this.document.getText(this.referenceOffsets.get(i), 12);
					String treeOriginalString = convertIntToHexString(sourceReferences.get(i));
					if(!docOriginalString.equals(treeOriginalString)) {
						success = false;
						if(recordFailures) {
							failedMappings.add(sourceReferences.get(i));
							failedOffsets.add(referenceOffsets.get(i));
							failedTypes.add(3); // 3 = SOURCE STRING MISMATCH 
						}
					}
				} catch(BadLocationException ex) {
					Logger.getLogger(ReferenceUpdate.class.getName()).log(Level.SEVERE, null, ex);
					failureMode = 2; // 2 = FILE READ ERROR
					return false;
				}
			}
		}
		return success;
	}
	
	/**
	 * Performs name-to-value mapping from referenceFullNames list to destReferences list.
	 * Uses destUpk to perform mapping.
	 * @return true if mapping completed successfully. false on error.
	 */
	private boolean buildDestReferences() {
		boolean success = true;
		for (int i = 0; i < referenceFullNames.size(); i++) {
			if(isVirtualFunction.get(i)) {
				this.destReferences.add(this.destUpk.findVFRefName(referenceFullNames.get(i)));
			} else {
				this.destReferences.add(this.destUpk.findRefName(referenceFullNames.get(i)));
			}
			if((this.destReferences.get(i) <= 0 && isVirtualFunction.get(i))
					|| (this.destReferences.get(i) == 0 && !isVirtualFunction.get(i))){
				destRefError.add(true);
				success = false;
				failedMappings.add(sourceReferences.get(i));
				failedOffsets.add(referenceOffsets.get(i));
				failedTypes.add(2); // 2 = NAME_TO_DST_FAIL
			} else {
				destRefError.add(false);
			}
		}
		return success;
	}
	
	/**
	 * Performs value-to-name mapping from sourceReferences list to referenceFullNames list.
	 * Uses sourceUpk to perform mapping.
	 * Performs error checking to ensure GUIDs match.
	 * @return true if mapping completed successfully. false on error.
	 */
	private boolean buildReferenceFullNames() {
		boolean success = true;
		for (int i = 0; i < sourceReferences.size(); i++) {
			if(isVirtualFunction.get(i)) {
				this.referenceFullNames.add(this.sourceUpk.getVFRefName(sourceReferences.get(i)));
			} else {
				this.referenceFullNames.add(this.sourceUpk.getRefName(sourceReferences.get(i)));
			}
			if(this.referenceFullNames.get(i).isEmpty()) {
				success = false;
				failedMappings.add(sourceReferences.get(i));
				failedOffsets.add(referenceOffsets.get(i));
				failedTypes.add(1); // 1 = SOURCE_TO_NAME_FAIL = 1
			}
		}
		return success;
	}
	
	/**
	 * Builds a complete list of references to be mapped from the source UPK.
	 * Uses tree supplied with constructor.
	 * @return true if all references found, false if error
	 */
	private boolean buildSourceReferences() {
		// DFS through ModTree, building SourceReferences List
		getReferences(this.tree.getRoot());
		return true;
	}
	
	/**
	 * Iterator for retrieving references from a subtree
	 * @param node current subtree root 
	 * @return true if references found, false if error
	 */
	private boolean getReferences(ModTreeNode node) {
		// recursive element for reference retrieval
		if(node.isLeaf()) {
			if(node.getName().equals("ModReferenceToken")) {
				sourceReferences.add(node.getRefValue());
				isVirtualFunction.add(node.isVirtualFunctionRef());
				referenceOffsets.add(node.getStartOffset());
			}
		} else {
			for (int i = 0; i < node.getChildNodeCount() ; i++) {
				getReferences(node.getChildNodeAt(i));
			}
		}
		return true;
	}
}
