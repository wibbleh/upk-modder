package util.unrealhex;

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
import util.unrealhex.HexStringLibrary.*;
import static util.unrealhex.HexStringLibrary.convertIntToHexString;
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
	private List<Boolean> hasDestRefError = new ArrayList<>();			// boolean recording whether destination reference found
	
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
	
	public ReferenceUpdate(ModTree tree, UpkFile src, UpkFile dst) {
		this.tree = tree;
		this.document = tree.getDocument();
		this.sourceUpk = src;
		this.destUpk = dst;
		buildSourceReferences();
		buildReferenceFullNames();
		buildDestReferences();
//		dumpData();
	}

	public ReferenceUpdate(ModTree tree, UpkFile src) {
		this.tree = tree;
		this.document = tree.getDocument();
		this.sourceUpk = src;
		this.destUpk = null;
		buildSourceReferences();
		buildReferenceFullNames();
	}
	
	public ReferenceUpdate(ModTree tree) {
		this.sourceUpk = null;
		this.destUpk = null;
		this.tree = tree;
		if(this.tree != null) {
			this.document = this.tree.getDocument();
			buildSourceReferences();
		}
	}
	
	public void setSourceUpk(UpkFile src) {
		if(src == null)
			return;
		this.sourceUpk = src;
		buildSourceReferences();
		buildReferenceFullNames();
		if(this.destUpk != null) {
			buildDestReferences();
		}
	}
	
	public void setDestUpk(UpkFile dst) {
		this.destUpk = dst;
		if(this.sourceUpk != null) {
			buildDestReferences();
		}
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
	
	public int length() {
		return sourceReferences.size();
	}
	
	public int getSrcCount() {
		int count = 0;
		for(int i = 0; i< length(); i++) {
			if(!getSourceReference(i).isEmpty()) {
				count++;
			}
		}
		return count;
	}
	
	public int getNameCount() {
		return referenceFullNames.size();
	}
	
	public int getDstCount() {
		return destReferences.size();
	}
	
	public String getSourceReference(int num) {
		if(num >=0 && num < sourceReferences.size()) {
			int val = sourceReferences.get(num);
			if(val == 0) {
				return "";
			} else {
				return convertIntToHexString(val);
			}
		} else {
			return "";
		}
	}
	
	public String getDestReference(int num) {
		if(num >=0 && num < destReferences.size()) {
			int val = destReferences.get(num);
			if(val == 0) {
				return "";
			} else {
				return convertIntToHexString(val);
			}
		} else {
			return "";
		}
	}
	
	public boolean hasDestRefError(int num) {
		if(num >=0 && num < hasDestRefError.size()) {
			return hasDestRefError.get(num);
		} else {
			return true;
		}
	}
	
	public String getReferenceName(int num) {
		if(num >=0 && num < referenceFullNames.size()) {
			String name = referenceFullNames.get(num);
			if(sourceReferences.get(num) == 0) {
				name = name.substring(2, name.length()-2);
			}
			return name;
		} else {
			return "";
		}
	}
	
	protected String getReferenceNameWithTags(int num) {
		if(num >=0 && num < referenceFullNames.size()) {
			String name = referenceFullNames.get(num);
			return name;
		} else {
			return "";
		}
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
		boolean success = testUpdateDocumentToName(false);
//		success = success && replaceGUID();
		if(success) {
			int offsetIncrease = 0;
			String name;
			for(int i = 0; i < sourceReferences.size(); i ++) {
				try {
					// arbitrary default AttributeSet
					AttributeSet as = new SimpleAttributeSet(); // TODO perform node-to-style mapping
					StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
					StyleConstants.setItalic((MutableAttributeSet) as, false);
					
					if(this.sourceReferences.get(i) != 0) {
						// format name replacement
						name = this.referenceFullNames.get(i);
						if(this.sourceReferences.get(i) != 0) {
							if(this.isVirtualFunction.get(i)) {
								name = "<|" + name + "|> ";
							}
							else {
								name = "{|" + name + "|} ";
							}
						}
						// remove old reference and insert new one
						document.remove(this.referenceOffsets.get(i) + offsetIncrease, 12);
						document.insertString(this.referenceOffsets.get(i) + offsetIncrease, name, as);
						offsetIncrease += (name.length() - 12);
					}
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
		boolean guidReplaced = replaceGUID();
		success = success && ((getSrcCount()==0) || guidReplaced);
		if(success) {
			int offsetIncrease = 0;
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
						if(sourceReferences.get(i) == 0) { // was a name reference in document
							int strLen = this.getReferenceNameWithTags(i).length() + 1;
							document.remove(this.referenceOffsets.get(i) + offsetIncrease, strLen);
							String docNewString = convertIntToHexString(destReferences.get(i));
							document.insertString(this.referenceOffsets.get(i) + offsetIncrease, docNewString, as);
							offsetIncrease += 12 - strLen;
						} else {  // was a value reference in document
							document.remove(this.referenceOffsets.get(i) + offsetIncrease, 12);
							String docNewString = convertIntToHexString(destReferences.get(i));
							document.insertString(this.referenceOffsets.get(i) + offsetIncrease, docNewString, as);
						}
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
		if(sourceUpk != null) {
			if(tree.getGuid().trim().equalsIgnoreCase(convertByteArrayToHexString(sourceUpk.getHeader().getGUID()).trim())) {
				return true;
			} else {
				failureMode = 5; // 5 = GUID MISMATCH 
				return false;
			}
		} else {
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
		return success; // && (this.failedMappings.isEmpty());
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
			System.out.println("Cannot update - source and dest lists different size.");
			failureMode = 1; // 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
			return false;
		}
		if(sourceReferences.size() != referenceOffsets.size()) {
			System.out.println("Cannot update - source and dest file pos lists different size.");
			failureMode = 1; // 1 = UNEQUAL ARRAY SIZE DURING REPLACEMENT
			return false;
		}
		if(!failedMappings.isEmpty()) {
			success = false;
		}
		String docOriginalString;
		String treeOriginalString;
		for(int i = 0; i < sourceReferences.size(); i++) {
			if((this.destReferences.get(i) < 0 && isVirtualFunction.get(i))
					|| (this.destReferences.get(i) == 0 && !isVirtualFunction.get(i))){
				success = false;
			} else {
				try {
					if(this.sourceReferences.get(i) == 0) {
						treeOriginalString = getReferenceNameWithTags(i);
						docOriginalString = this.document.getText(this.referenceOffsets.get(i), treeOriginalString.length());
					} else {
						docOriginalString = this.document.getText(this.referenceOffsets.get(i), 12);
						treeOriginalString = convertIntToHexString(sourceReferences.get(i));
					}
					if(!docOriginalString.equals(treeOriginalString)) {
						System.out.println("Cannot update - tree/document source reference " + i + "mismatch.");
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
		if(getSrcCount() == 0) {
			success = true; // allow partial updating if there are no current source reference values
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
		destReferences.clear();
		hasDestRefError.clear();
		for (int i = 0; i < sourceReferences.size(); i++) {
			if(isVirtualFunction.get(i)) {
				this.destReferences.add(this.destUpk.findVFRefName(getReferenceName(i)));
			} else {
				this.destReferences.add(this.destUpk.findRefName(getReferenceName(i)));
			}
			if((this.destReferences.get(i) < 0 && isVirtualFunction.get(i))
					|| (this.destReferences.get(i) == 0 && !isVirtualFunction.get(i))){
				hasDestRefError.add(true);
				success = false;
				failedMappings.add(sourceReferences.get(i));
				failedOffsets.add(referenceOffsets.get(i));
				failedTypes.add(2); // 2 = NAME_TO_DST_FAIL
			} else {
				hasDestRefError.add(false);
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
		failedMappings.clear();
		failedOffsets.clear();
		failedTypes.clear();

		for (int i = 0; i < sourceReferences.size(); i++) {
			if(this.referenceFullNames.get(i).isEmpty()) {
				if(isVirtualFunction.get(i)) {
					this.referenceFullNames.set(i, this.sourceUpk.getVFRefName(sourceReferences.get(i)));
				} else {
					this.referenceFullNames.set(i, this.sourceUpk.getRefName(sourceReferences.get(i)));
				}
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
		referenceFullNames.clear();
		sourceReferences.clear();
		isVirtualFunction.clear();
		referenceOffsets.clear();
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
				if(node.getRefValue() == 0) {
					String name = node.getText().trim();
					referenceFullNames.add(name);
				} else {
					referenceFullNames.add("");
				}
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
