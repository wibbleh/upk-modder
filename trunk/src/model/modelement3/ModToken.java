package model.modelement3;

import static model.modelement3.ModContextType.*;

/**
 *
 * @author Amineri
 */


public class ModToken extends ModElement
{
    
    protected String data;
    
    public ModToken(ModElement o)
    {
        super(o);
        init(o);
        data = "";
        name = "ModToken";
    }
    
    public ModToken(ModElement o, String s)
    {
        super(o);
        init(o);
        data = s;
        name = "ModToken";
    }
    
    public ModToken(ModElement o, String s, boolean simple)
    {
        super(o);
        init(o);
        this.data = s;
        this.isSimpleString = simple;
        name = "ModToken";
    }
    
    @Override
    public int getMemorySize()
    {
        if(isSimpleString) {
            return -1;
        } else {
            return data.length()/3;
        }
    }
    
    private void init(ModElement o)
    {
        this.branches = null;
        this.parent = o;
        this.isSimpleString = false;
        setLocalContext(CODE, isCode());
        setLocalContext(VALIDCODE, false);
        setLocalContext(HEADER, false);
        setLocalContext(BEFOREHEX, false);
        setLocalContext(AFTERHEX, false);
    }

    
    @Override
    protected String getString()
    {
        return data;
    }

    @Override
    protected void setString(String s)
    {
        data = s;
    }

    @Override
    public boolean isVFFunctionRef()
    {
        return false;
    }
    
    @Override
    public int getOffset()
    {
        return -1;
    }

    @Override
    public int getRefValue()
    {
        return -1;
    }
    
    String parseUnrealHex(String s, int num)
    {
        for(int i = 0; i < num; i++)
        {
            this.endOffset += 3;
            data += s.split("\\s", 2)[0] + " ";
            s = s.split("\\s", 2)[1];
        }
        return s;
    }

    /**
     * Returns true if element is leaf node (eg "token" )
     * IMPLEMENTED
     * @return
     */
    @Override
    public boolean isLeaf()
    {
        return true;
    }

}
