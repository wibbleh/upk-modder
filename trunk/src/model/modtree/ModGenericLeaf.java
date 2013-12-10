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

    ModGenericLeaf(ModTreeNode o, boolean operand)
    {
        super(o);
        name = "GenericToken";
        isSimpleString = false;
        setContextFlag(ModContextType.VALID_CODE, true);
		if(operand) {
			name = "OperandToken";
		}
    }
	
    @Override
    String parseUnrealHex(String s, int i)
    {
            s = super.parseUnrealHex(s, i);
            return s;
    }
    
}
