package model.modelement3;

import static model.modelement3.ModContextType.*;

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
        setLocalContext(VALIDCODE, true);
    }

    @Override
    String parseUnrealHex(String s, int i)
    {
            s = super.parseUnrealHex(s, i);
            return s;
    }
    
}
