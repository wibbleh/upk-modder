package model.modelement3;

import static model.modelement3.ModContextType.*;

/**
 *
 * @author Amineri
 */


public class ModStringToken extends ModToken
{

    ModStringToken(ModOperandElement o)
    {
        super(o);
        name = "ModStringToken";
        isSimpleString = false;
        setLocalContext(VALIDCODE, true);
    }

    String parseUnrealHex(String s)
    {
            while(!s.split("\\s",2)[0].equals("00"))
            {
                s = super.parseUnrealHex(s, 1);
                if(s.isEmpty())
                {
                    return "ERROR";
                }
            }
            s = super.parseUnrealHex(s, 1);
            return s;
    }
    
}
