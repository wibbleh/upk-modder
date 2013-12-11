package model.modelement3;

import model.moddocument3.ModDocument;
import model.modelement3.ModContext.ModContextType;
import static model.modelement3.ModContext.ModContextType.*;


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
     * The persistent global context maintained while parsing through the file.
     */
//    private ModContext globalContext;

	/**
	 * 
	 * @param document
	 */
	public ModRootElement(ModDocument document) {
		super(null);
		this.document = document;
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
				if (!child.toStr().isEmpty()) {
					if (child.toStr().contains("\n") && (index <= childCount)) {
						String[] strings = child.toStr().split("\n", 2);
						if (!strings[1].isEmpty()) {
        					strings[0] += "\n";
        					childCount++;
        					ModElement grandChild = child.getChildElementAt(0);
        					grandChild.setString(strings[0]);
        					int oldEndOffset = child.getEndOffset();
        					int childEndOffset = child.getEndOffset() - strings[1].length();
        					child.setRange(child.getStartOffset(), childEndOffset);
        					int grandChildEndOffset = child.getEndOffset() - strings[1].length();
        					grandChild.setRange(child.getStartOffset(), childEndOffset);

        					ModElement newElement = new ModElement(this, true);
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
        this.resetContextFlags();
        this.glueElementsWithoutNewline();
        this.buildContextsParseUnreal();
    }

	private void glueElementsWithoutNewline() {
		// iterate through array of lines and break into lines
		int count = 0;
		int numbranches = this.getChildElementCount();
		do {
			ModElement branch = this.getChildElementAt(count);
			if (branch.isSimpleString) {
				if (!branch.toStr().isEmpty()) {
					if (!branch.toStr().contains("\n") && count + 1 < numbranches) {
						numbranches--;
						String gluedString = branch.toStr()
								+ this.getChildElementAt(count + 1).toStr();
						ModElement branchBranch = branch.getChildElementAt(0);
						branchBranch.setString(gluedString);
						branch.setRange(branch.getStartOffset(),
								branch.getStartOffset() + gluedString.length());
						branchBranch.setRange(
								branchBranch.getStartOffset(),
								branchBranch.getStartOffset() + gluedString.length());
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

	protected void updateContexts(ModElement e){
		
	}
	
    private void buildContextsParseUnreal()
    {
        // iterate through array of lines 
		for (int i = 0; i < this.getChildElementCount(); i++) {
			ModElement b = this.getChildElementAt(i);
			
            // update contexts
            b.updateContexts();
			
			//store copy of global contexts at current child
			b.context.setContextFlag(FILE_HEADER, this.context.getContextFlag(FILE_HEADER));
			b.context.setContextFlag(BEFORE_HEX, this.context.getContextFlag(BEFORE_HEX));
			b.context.setContextFlag(AFTER_HEX, this.context.getContextFlag(AFTER_HEX));
			b.context.setContextFlag(HEX_HEADER, this.context.getContextFlag(HEX_HEADER));
			b.context.setContextFlag(HEX_CODE, this.context.getContextFlag(HEX_CODE));
			b.context.setContextFlag(VALID_CODE, this.context.getContextFlag(VALID_CODE));
            
            //  consolidate/expand code lines
            if(!b.getContextFlag(ModContextType.HEX_CODE) && !b.isSimpleString) { // consolidate string
                ModToken newToken = new ModToken(b, b.toStr(), true);
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

	@Override
	protected ModElement getRoot()
	{
		return this;
	}

	/**
	 * Resets the context flags to their default values (<code>false</code>).
	 * All flags set to false except for FILE_HEADER = true.
	 * Reset file attributes in case they were changed.
	 */
	protected void resetContextFlags() {
		this.context = new ModContext();
		this.setContextFlag(FILE_HEADER, true);
		// TODO: @Amineri why is any basic element capable of resetting values in the underlying document? Shouldn't only the root node be allowed to do this?
		// Amineri : Yes, this should only be getting called from the root element on an insertUpdate or removeUpdate method call
		if(getDocument() == null) {return;}
		getDocument().setFileVersion(-1);
		getDocument().setUpkName("");
		getDocument().setGuid("");
		getDocument().setFunctionName("");
	}

}
