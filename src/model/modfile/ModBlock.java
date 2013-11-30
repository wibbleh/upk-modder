package model.modfile;

/**
 *
 * @author Amineri
 */


public class ModBlock
{
    private ModChunk header;
    private ModChunk code;
    
    private ModReplacement owner;
    
    private boolean isHeaderContext;
    
    public ModBlock(ModReplacement replacement)
    {
        owner = replacement;
        header = new ModChunk(false, this);
        code = new ModChunk(true, this);
        isHeaderContext = false;
    }

    public ModReplacement getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().contains("[HEADER]"))
        {
            isHeaderContext = true;
        }
        if(isHeaderContext)
        {
            header.addLine(s);
        }
        else
        {
            code.addLine(s);
        }
        if(s.toUpperCase().contains("[/HEADER]"))
        {
            isHeaderContext = false;
        }
    }
    
    public int getNumLines()
    {
        return header.getNumLines() +
                code.getNumLines();
    }

    public ModLine getLine(int index)
    {
        if(index < header.getNumLines())
        {
            return header.getLine(index);
        }
        return code.getLine(index- header.getNumLines());
    }

}
