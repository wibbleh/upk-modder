package model.modfile;

/**
 *
 * @author Amineri
 */


public class ModBlock
{
    private ModChunk header;
    private ModChunk hexHeader;
    private ModChunk code;
    
    private ModReplacement owner;
    
    private boolean isInHexHeader;
    private boolean isInHeader;
    
    public ModBlock(ModReplacement replacement)
    {
        owner = replacement;
        header = new ModChunk(false, this);
        hexHeader = new ModChunk(false, this);
        code = new ModChunk(true, this);
        isInHexHeader = false;
        isInHeader = true;
    }

    public ModReplacement getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().contains("[HEADER]"))
        {
            isInHexHeader = true;
            isInHeader = false;
        }
        if(isInHexHeader)
        {
            hexHeader.addLine(s);
        }
        else if(isInHeader)
        {
            header.addLine(s);
        }
        else
        {
            code.addLine(s);
        }
        if(s.toUpperCase().contains("[/HEADER]"))
        {
            isInHexHeader = false;
        }
    }
    
    public int getNumLines()
    {
        return header.getNumLines() + hexHeader.getNumLines() + code.getNumLines();
    }

    public ModLine getLine(int index)
    {
        int iCount = index;
        if(iCount < header.getNumLines())
        {
            return header.getLine(iCount);
        }
        iCount -= header.getNumLines();
        if(iCount < hexHeader.getNumLines())
        {
            return hexHeader.getLine(iCount);
        }
        iCount -= hexHeader.getNumLines();
        if(iCount < code.getNumLines())
        {
            return code.getLine(iCount);
        }
        return null;
    }

}
