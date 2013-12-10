package model.modtree;

import model.modtree.ModContext.*;
import static model.modtree.ModContext.ModContextType.*;

/**
 *
 * @author Amineri
 */


public class ModReferenceLeaf extends ModTreeLeaf
{
    private final boolean isVFFunction;
    private int value;

    ModReferenceLeaf(ModTreeNode o, boolean vf)
    {
        super(o);
        this.isVFFunction = vf;
        name = "ModReferenceToken";
        isSimpleString = false;
        setContextFlag(ModContextType.VALID_CODE, true);
    }

	protected String parseUnrealHex(String s) {
		value = 0;
		String[] tokens = s.split("\\s");
		for (int i = 0; i < 4; i++) {
			value += Integer.parseInt(tokens[i], 16) << (8 * i);
		}
		return super.parseUnrealHex(s, 4);
	}
    
    @Override
    public boolean isVFFunctionRef()
    {
        return isVFFunction;
    }

    @Override
    public int getMemorySize()
    {
        if(isVFFunctionRef())
        {
            return 4;
        }
        else
        {
            return 8;
        }
    }
    
    @Override
    public int getRefValue()
    {
        return value;
    }
}
