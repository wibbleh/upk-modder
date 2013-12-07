package model.modelement3;

import model.modelement3.ModContext.ModContextType;

/**
 *
 * @author Amineri
 */


public class ModGenericToken extends ModToken
{

    ModGenericToken(ModElement o)
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
