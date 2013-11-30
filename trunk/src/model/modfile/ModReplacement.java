package model.modfile;

/**
 *
 * @author Amineri
 */


public class ModReplacement
{
    private ModBlock extraInitial;
    private ModBlock before;
    private ModBlock extraBetween;
    private ModBlock after;
    private ModBlock extraFollowing;
    
    private ModFunction owner;
    
    private boolean inBeforeBlock;
    private boolean writtenBeforeBlock;
    private boolean inAfterBlock;
    private boolean writtenAfterBlock;
    
    public ModReplacement(ModFunction function)
    {
        owner = function;
        extraInitial = new ModBlock(this);
        before = new ModBlock(this);
        extraBetween = new ModBlock(this);
        after = new ModBlock(this);
        extraFollowing = new ModBlock(this);

        inBeforeBlock = false;
        writtenBeforeBlock = false;
        inAfterBlock = false;
        writtenAfterBlock = false;
    }
    
    public ModFunction getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().contains("[BEFORE_HEX]"))
        {
            inBeforeBlock = true;
        }
        if(s.toUpperCase().contains("[AFTER_HEX]"))
        {
            inAfterBlock = true;
        }
        if(writtenAfterBlock)
        {
            extraFollowing.addLine(s);
        }
        else if(inAfterBlock)
        {
            after.addLine(s);
        }
        else if(writtenBeforeBlock)
        {
            extraBetween.addLine(s);
        }
        else if(inBeforeBlock)
        {
            before.addLine(s);
        }
        else
        {
            extraInitial.addLine(s);
        }
        if(s.toUpperCase().contains("[/BEFORE_HEX]"))
        {
            inBeforeBlock = false;
            writtenBeforeBlock = true;
        }
        if(s.toUpperCase().contains("[/AFTER_HEX]"))
        {
            inAfterBlock = false;
            writtenAfterBlock = true;
        }
    }

    public int getNumLines()
    {
        return extraInitial.getNumLines() +
                before.getNumLines() +
                extraBetween.getNumLines() +
                after.getNumLines() +
                extraFollowing.getNumLines();
    }
    
    public ModLine getLine(int index)
    {
        int iCount = index;
        if(iCount < extraInitial.getNumLines())
        {
            return extraInitial.getLine(iCount);
        }
        iCount -= extraInitial.getNumLines();
        if(iCount < before.getNumLines())
        {
            return before.getLine(iCount);
        }
        iCount -= before.getNumLines();
        if(iCount < extraBetween.getNumLines())
        {
            return extraBetween.getLine(iCount);
        }
        iCount -= extraBetween.getNumLines();
        if(iCount < after.getNumLines())
        {
            return after.getLine(iCount);
        }
        iCount -= after.getNumLines();
        if(iCount < extraFollowing.getNumLines())
        {
            return extraFollowing.getLine(iCount);
        }
        return null;
}

    public boolean bothBlocksWritten()
    {
        return writtenBeforeBlock && writtenAfterBlock;
    }

    public ModBlock getInitial()
    {
        return extraInitial;
    }
    
    public ModBlock getBeforeHex()
    {
        return before;
    }
    
    public ModBlock getBetween()
    {
        return extraBetween;
    }
    
    public ModBlock getAfterHex()
    {
        return after;
    }
    
    public ModBlock getFollowing()
    {
        return extraFollowing;
    }
}
