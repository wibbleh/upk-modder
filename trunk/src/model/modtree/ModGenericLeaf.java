package model.modtree;

import model.modtree.ModContext.ModContextType;


/**
 *
 * @author Amineri
 */


public class ModGenericLeaf extends ModTreeLeaf
{

    ModGenericLeaf(ModTreeNode o)
    {
        super(o);
        name = "GenericToken";
        isSimpleString = false;
        setContextFlag(ModContextType.VALID_CODE, true);
    }

    @Override
    String parseUnrealHex(String s, int i)
    {
            s = super.parseUnrealHex(s, i);
            return s;
    }
    
}
