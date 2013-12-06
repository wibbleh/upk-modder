package model.modelement3;

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
    }

    @Override
    String parseUnrealHex(String s, int i)
    {
            s = super.parseUnrealHex(s, i);
            return s;
    }
    
}
