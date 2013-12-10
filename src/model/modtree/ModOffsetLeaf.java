package model.modtree;

import model.modtree.ModContext.*;
import static model.modtree.ModContext.ModContextType.*;

/**
 *
 * @author Amineri
 */


public class ModOffsetLeaf extends ModTreeLeaf
{
    private String operand;
    private int jumpoffset;
    
    ModOffsetLeaf(ModOperandNode o)
    {
        super(o);
        name = "ModJumpToken";
        operand = null;
        isSimpleString = false;
        setContextFlag(ModContextType.VALID_CODE, true);
    }


    ModOffsetLeaf(ModOperandNode o, String sParseItem)
    {
        super(o);
        operand = sParseItem;
        name = "ModRelativeJumpToken";
    }

	protected String parseUnrealHex(String s) {
		s = super.parseUnrealHex(s, 2);
		int int0 = Integer.getInteger(data.split("\\s")[0], 16);
		int int1 = Integer.getInteger(data.split("\\s")[1], 16);
		jumpoffset = 256 * int0 + int1;
		return s;
	}
    
    @Override
    public int getOffset()
    {
        return jumpoffset;
    }
}
