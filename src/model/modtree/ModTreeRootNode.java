package model.modtree;

import static model.modtree.ModContext.ModContextType.AFTER_HEX;
import static model.modtree.ModContext.ModContextType.BEFORE_HEX;
import static model.modtree.ModContext.ModContextType.FILE_HEADER;
import static model.modtree.ModContext.ModContextType.HEX_CODE;
import static model.modtree.ModContext.ModContextType.HEX_HEADER;
import static model.modtree.ModContext.ModContextType.VALID_CODE;
import model.modtree.ModContext.ModContextType;

/**
 *
 * @author Amineri
 */
public class ModTreeRootNode extends ModTreeNode {
	
	/**
	 * The reference to the document instance. This root node is the only node
	 * in the hierarchy actually storing this reference.
	 */
	private final ModTree tree;
	
    /**
     * The persistent global context maintained while parsing through the file.
     */
//    private ModContext globalContext;

	/**
	 * 
	 * @param tree
	 */
	public ModTreeRootNode(ModTree tree) {
		super(null);
		this.tree = tree;

		// add initial hierarchy
		ModTreeNode child = new ModTreeNode(this, true);
		child.addNode(new ModTreeLeaf(child, "", true));
		this.addNode(child);
	}
    
	/**
	 * Splits multi-line tokens into single Line tokens.<br>
	 * Re-computes contextsfor all elements and tokens.<br>
	 * Re-parses any modified unrealhex.
	 */
	public void reorganizeAfterInsertion() {
		this.resetContextFlags();
		this.splitElementsOnNewline();
		this.buildContextsParseUnreal();
		this.setMemoryPositions();
	}
    
	private void splitElementsOnNewline() {
        // iterate through array of lines and break into lines
        int index = 0;
        int childCount = this.getChildNodeCount();
        do {
        	ModTreeNode child = this.getChildNodeAt(index);
        	index ++;
			if (child.isPlainText()) {
				if (!child.getFullText().isEmpty()) {
					if (child.getFullText().contains("\n") && (index <= childCount)) {
						String[] strings = child.getFullText().split("\n", 2);
						if (!strings[1].isEmpty()) {
        					strings[0] += "\n";
        					childCount++;
        					ModTreeNode grandChild = child.getChildNodeAt(0);
        					grandChild.setText(strings[0]);
        					int oldEndOffset = child.getEndOffset();
        					int childEndOffset = child.getEndOffset() - strings[1].length();
        					child.setRange(child.getStartOffset(), childEndOffset);
        					int grandChildEndOffset = child.getEndOffset() - strings[1].length();
        					grandChild.setRange(child.getStartOffset(), childEndOffset);

        					ModTreeNode newElement = new ModTreeNode(this, true);
//							newElement.setUpdateFlag(true);
        					ModTreeLeaf newToken = new ModTreeLeaf(newElement, strings[1], true);
//							newToken.setUpdateFlag(true);

        					this.addNode(index, newElement);
        					newElement.addNode(newToken);
        					newElement.setRange(child.getEndOffset(), oldEndOffset);
        					newToken.setRange(child.getEndOffset(), oldEndOffset);
        				}
        			}
        		}
        	}
        } while(index < childCount); 
    }

    /**
     * Glues together lines not separated by newlines.
     * Re-computes contexts for all elements and tokens.
     * Re-parses any modified unrealhex.
     */
    public void reorganizeAfterDeletion()
    {
        this.resetContextFlags();
        this.glueElementsWithoutNewline();
        this.buildContextsParseUnreal();
		this.setMemoryPositions();
    }

	private void glueElementsWithoutNewline() {
		// iterate through array of lines and break into lines
		int count = 0;
		int numbranches = this.getChildNodeCount();
		do {
			ModTreeNode branch = this.getChildNodeAt(count);
			if (branch.isPlainText()) {
				if (!branch.getFullText().isEmpty()) {
					if (!branch.getFullText().contains("\n") && count + 1 < numbranches) {
						numbranches--;
						String gluedString = branch.getFullText()
								+ this.getChildNodeAt(count + 1).getFullText();
						ModTreeNode branchBranch = branch.getChildNodeAt(0);
						branchBranch.setText(gluedString);
						branch.setRange(branch.getStartOffset(),branch.getStartOffset() + gluedString.length());
						branchBranch.setRange(branchBranch.getStartOffset(),branchBranch.getStartOffset() + gluedString.length());
//						branch.setUpdateFlag(true);
//						branchBranch.setUpdateFlag(true);
						this.removeChildNodeAt(count + 1);
					} else {
						count++;
					}
				} else { // handle empty string removal
					if (tree.getRoot().getEndOffset() == 0) {
						// if document is empty exit out
						count++;
					} else {
						// otherwise remove the element with the empty leaf
						numbranches--;
						this.removeChildNodeAt(count);
					}
				}
			} else {
				count++;
			}
		} while (count < numbranches);
	}

	private void buildContextsParseUnreal() {
        // iterate through array of lines 
		for (int i = 0; i < this.getChildNodeCount(); i++) {
			ModTreeNode child = this.getChildNodeAt(i);
			
            // update contexts
            child.updateContexts();
			
			//store copy of global contexts at current child
			child.setContextFlag(FILE_HEADER, this.getContextFlag(FILE_HEADER));
			child.setContextFlag(BEFORE_HEX, this.getContextFlag(BEFORE_HEX));
			child.setContextFlag(AFTER_HEX, this.getContextFlag(AFTER_HEX));
			child.setContextFlag(HEX_HEADER, this.getContextFlag(HEX_HEADER));
			child.setContextFlag(HEX_CODE, this.getContextFlag(HEX_CODE));
			child.setContextFlag(VALID_CODE, this.getContextFlag(VALID_CODE));
            
            //  consolidate/expand code lines
            boolean isCode = child.getContextFlag(ModContextType.HEX_CODE);
			if (!isCode && !child.isPlainText()) {	// consolidate string
                ModTreeLeaf leaf = new ModTreeLeaf(child, child.getFullText(), true);
                leaf.setRange(child.getStartOffset(), child.getEndOffset());
                child.removeAllChildNodes();
                child.addNode(leaf);
                child.setPlainText(true);
            }
            if (isCode && child.isPlainText()) {
                child.parseUnrealHex(null, 0);
            }
            // (isCode && !isPlainText) means it was not update
            // (!isCode && isPlainText) does not need reconsolidating
        }
    }
    
    @Override
    public String getName() {
    	return "ModRootElement";
    }
	
	@Override
	public ModTree getTree() {
		return this.tree;
	}

	@Override
	protected ModTreeNode getRoot()
	{
		return this;
	}

	/**
	 * Resets the context flags to their default values (<code>false</code>).
	 * All flags set to false except for FILE_HEADER = true.
	 * Reset file attributes in case they were changed.
	 */
	@Override
	public void resetContextFlags() {
		super.resetContextFlags();
		this.setContextFlag(FILE_HEADER, true);
		
		ModTree aTree = this.getTree();
		if (aTree != null) {
			aTree.setFileVersion(-1);
			aTree.setUpkName("");
			aTree.setGuid("");
			aTree.setFunctionName("");
			aTree.setResizeAmount(0);
		}
	}

	@Override
	public void setText(String text) {
		// do nothing
	}

	@Override
	public String getText() {
		// return nothing
		return "";
	}

	/**
	 * Sets memory positions for each line 
	 */
	protected void setMemoryPositions() {
		int currMemoryPosition = 0;
		int currFilePosition = 0;
		for (int i = 0; i < this.getChildNodeCount(); i++) {
			// store current positions
			this.getChildNodeAt(i).setMemoryPosition(currMemoryPosition);
			this.getChildNodeAt(i).setFilePosition(currFilePosition);
			// increment position with current line size
			currMemoryPosition += this.getChildNodeAt(i).getMemorySize();
			currFilePosition += this.getChildNodeAt(i).getFileSize();
			// if not valid hex code reset the position
			if(!this.getChildNodeAt(i).getContextFlag(HEX_CODE)) {
				currMemoryPosition = 0;
				currFilePosition = 0;
			}
		}
	}
	
}
