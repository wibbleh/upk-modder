package model.modelement3;

import model.moddocument3.ModDocument;
import model.modelement3.ModContext.ModContextType;


/**
 *
 * @author Amineri
 */
public class ModRootElement extends ModElement {
	
	/**
	 * The reference to the document instance. This root node is the only node
	 * in the hierarchy actually storing this reference.
	 */
	private ModDocument document;
	
	/**
	 * 
	 * @param d
	 */
	public ModRootElement(ModDocument document) {
		super(null);

		this.name = "ModRootElement";

		ModElement child = new ModElement(this, true);
		child.addElement(new ModToken(child, "", true));
		this.addElement(child);
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
	}
    
    private void splitElementsOnNewline()
    {
        // iterate through array of lines and break into lines
        int index = 0;
        int childCount = getChildElementCount();
        do {
        	ModElement child = this.getChildElementAt(index);
        	index ++;
			if (child.isSimpleString) {
				if (!child.toString().isEmpty()) {
					if (child.toString().contains("\n") && (index <= childCount)) {
						String[] strings = child.toString().split("\n", 2);
						if (!strings[1].isEmpty()) {
        					strings[0] += "\n";
        					childCount++;
        					ModElement grandChild = child.getChildElementAt(0);
        					grandChild.setString(strings[0]);
        					int oldEndOffset = child.getEndOffset();
        					int childEndOffset = child.getEndOffset() - strings[1].length();
        					child.setRange(child.getStartOffset(), childEndOffset);
        					int grandChildEndOffset = grandChild.getEndOffset() - strings[1].length();
        					grandChild.setRange(grandChild.getStartOffset(), grandChildEndOffset);

        					ModElement newElement = new ModElement(getParentElement(), true);
        					ModToken newToken = new ModToken(newElement, strings[1], true);

        					this.addElement(index, newElement);
        					newElement.addElement(newToken);
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
        resetContextFlags();
        glueElementsWithoutNewline();
        buildContextsParseUnreal();
    }

	private void glueElementsWithoutNewline() {
		// iterate through array of lines and break into lines
		int count = 0;
		int numbranches = this.getChildElementCount();
		do {
			ModElement branch = this.getChildElementAt(count);
			if (branch.isSimpleString) {
				if (!branch.toString().isEmpty()) {
					if (!branch.toString().contains("\n") && count + 1 < numbranches) {
						numbranches--;
						String gluedString = branch.toString()
								+ this.getChildElementAt(count + 1).toString();
						ModElement branchBranch = branch.getChildElementAt(0);
						branchBranch.setString(gluedString);
						branch.setRange(branch.getStartOffset(),
								branch.getEndOffset() + gluedString.length());
						branchBranch.setRange(
								branchBranch.getStartOffset(),
								branchBranch.getEndOffset() + gluedString.length());
						this.removeChildElementAt(count + 1);
					} else {
						count++;
					}
				} else { // handle empty string removal
					if (document.getDefaultRootElement().getEndOffset() == 0) {
						// if document is empty exit out
						count++;
					} else {
						// otherwise remove the element with the empty leaf
						numbranches--;
						this.removeChildElementAt(count);
					}
				}
			}
		} while (count < numbranches);
	}

    private void buildContextsParseUnreal()
    {
        // iterate through array of lines 
        setContextFlag(ModContextType.FILE_HEADER, true);
//        getDocument().inFileHeaderContext = true;
        for (int i = 0; i < this.getChildElementCount(); i++) {
			ModElement b = this.getChildElementAt(i);
			
            // update contexts
            b.updateContexts();
            
            //  consolidate/expand code lines
            if(!b.getContextFlag(ModContextType.HEX_CODE) && !b.isSimpleString) { // consolidate string
                ModToken newToken = new ModToken(b, b.toString(), true);
                newToken.setRange(b.getStartOffset(), b.getEndOffset());
                b.removeAllChildElements();
                b.addElement(newToken);
                b.isSimpleString = true;
            }
            if(b.getContextFlag(ModContextType.HEX_CODE) && b.isSimpleString) {
                b.parseUnrealHex();
            }
            // isCode and not isSimple String means it was not update
            // !isCode and isSimple string does not need reconsolidating
        }
    }
	
	@Override
	public ModDocument getDocument() {
		return this.document;
	}
    
}
