package model.modelement3;

/**
 *
 * @author Amineri
 */


public class ModGenericToken extends ModToken
{
    private int jumpoffset;

    ModGenericToken(ModElement o)
    {
        super(o);
        name = "GenericToken";
        isSimpleString = false;
    }

    @Override
    String parseUnrealHex(String s, int parseInt)
    {
            s = super.parseUnrealHex(s, 2);
            int int0 = Integer.getInteger(data.split("\\s")[0], 16);
            int int1 = Integer.getInteger(data.split("\\s")[1], 16);
            jumpoffset = 256*int0 + int1;
            return s;
    }
    
}
