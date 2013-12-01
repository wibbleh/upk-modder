package model.modfile;

/**
 *
 * @author Amineri
 */


public class ModReplacement
{
    private ModBlock before;
    private ModBlock after;
    
    private ModFunction owner;
    
    private boolean inBeforeBlock;
    
    public ModReplacement(ModFunction function)
    {
        owner = function;
        before = new ModBlock(this);
        after = new ModBlock(this);

        inBeforeBlock = true;
    }
    
    public ModFunction getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().contains("[AFTER_HEX]"))
        {
            inBeforeBlock = false;
        }
        if(inBeforeBlock)
        {
            before.addLine(s);
        }
        else 
        {
            after.addLine(s);
        }
    }

    public int getNumLines()
    {
        return before.getNumLines() + after.getNumLines();
    }
    
    public ModLine getLine(int index)
    {
        int iCount = index;
        if(iCount < before.getNumLines())
        {
            return before.getLine(iCount);
        }
        iCount -= before.getNumLines();
        if(iCount < after.getNumLines())
        {
            return after.getLine(iCount);
        }
        return null;
}

    public ModBlock getBeforeBlock()
    {
        return before;
    }
    
    public ModBlock getAfterBlock()
    {
        return after;
    }
}
