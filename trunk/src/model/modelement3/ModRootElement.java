package model.modelement3;


/**
 *
 * @author Amineri
 */


public class ModRootElement extends ModElement
{

    public ModRootElement()
    {
        super();
        this.parent = null;
        name = "ModRootElement";
        addElement(new ModElement(this, true));
        branches.get(0).addElement(new ModToken(branches.get(0), "", true));
    }
    
    /**
     * Splits multi-line tokens into single Line tokens.
     * Re-computes contexts for all elements and tokens.
     * Re-parses any modified unrealhex.
     */
    public void reorganize()
    {
        resetContexts();
        // iterate through array of lines and break into lines
        int count = 0;
        int numbranches = branches.size();
        do {
            ModElement branch = branches.get(count);
            count ++;
            if(branch.isSimpleString) {
                if(!branch.toString().isEmpty()) {
                    if(branch.toString().contains("\n")){
                        numbranches++;
                        String[] strings = branch.toString().split("\n",2);
                        strings[0] += "\n";
                        branch.branches.get(0).setString(strings[0]);
                        int oldEndOffset = branch.endOffset;
                        branch.endOffset -= strings[1].length();
                        branch.branches.get(0).endOffset -= strings[1].length();
                        ModElement newElement = new ModElement(getParentElement(), true);
                        ModToken newToken = new ModToken(newElement, strings[1], true);
                        addElement(count, newElement);
                        newElement.addElement(newToken);
                        newElement.startOffset = branch.endOffset;
                        newElement.endOffset = oldEndOffset;
                        newToken.startOffset = branch.endOffset;
                        newToken.endOffset = oldEndOffset;
                    }
                }
            }
        } while(count < numbranches);
        // iterate through array of lines 
        inFileHeaderContext = true;
        for(ModElement b : branches) {
            // update contexts
            b.updateContexts();
            
            //  consolidate/expand code lines
            if(!b.isCode && !b.isSimpleString) { // consolidate string
                ModToken newToken = new ModToken(b, b.toString(), true);
                newToken.startOffset = b.startOffset;
                newToken.endOffset = b.endOffset;
                b.branches.clear();
                b.addElement(newToken);
                b.isSimpleString = true;
            }
            if(b.isCode && b.isSimpleString) {
                b.parseUnrealHex();
            }
            // isCode and not isSimple String means it was not update
            // !isCode and isSimple string does not need reconsolidating
        }
    }
}
