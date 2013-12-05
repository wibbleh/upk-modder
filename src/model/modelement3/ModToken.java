package model.modelement3;

/**
 *
 * @author Amineri
 */


public class ModToken extends ModElement
{
    
    protected String data;
    
    public ModToken(ModElement o)
    {
        init(o);
        data = "";
    }
    
    public ModToken(ModElement o, String s)
    {
        init(o);
        data = s;
    }
    
    public ModToken(ModElement o, String s, boolean simple)
    {
        init(o);
        this.data = s;
        this.isSimpleString = simple;
    }
    
    private void init(ModElement o)
    {
        this.branches = null;
        this.parent = o;
        inCodeContext = false;
        inHeaderContext = false;
        inBeforeBlockContext = false;
        inAfterBlockContext = false;
        this.isCode = isCode();
        this.isValidCode = false;
        this.isSimpleString = false;
        this.isHeader = false;
        this.isInBeforeBlock = false;
        this.isInAfterBlock = false;
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

    public boolean isVFFunctionRef()
    {
        return false;
    }
    
    public int getOffset()
    {
        return -1;
    }

    public int getRefValue()
    {
        return -1;
    }
    
    String parseUnrealHex(String s, int num)
    {
        for(int i = 0; i < num; i++)
        {
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
