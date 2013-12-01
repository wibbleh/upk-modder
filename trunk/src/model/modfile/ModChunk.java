package model.modfile;

import java.util.ArrayList;

/**
 *
 * @author Amineri
 */


public class ModChunk
{
    private ArrayList<ModLine> lines;
    private ModBlock owner;
    
    private boolean chunkIsCode;
    
    private int capacity = 20;
    
    public ModChunk()
    {
        lines = new ArrayList<>(capacity);
        chunkIsCode = false;
    }
    
    public ModChunk(ModBlock block)
    {
        owner = block;
        lines = new ArrayList<>(capacity);
        chunkIsCode = false;
    }
    
    public ModChunk(boolean isCode, ModBlock block)
    {
        owner = block;
        lines = new ArrayList<>(capacity);
        chunkIsCode = isCode;
    }
    
    public ModBlock getOwner()
    {
        return owner;
    }
    
    public void addLine(String s)
    {
        if(lines.size() == capacity)
        {
            capacity += 10;
            lines.ensureCapacity(capacity);
        }
        lines.add(new ModLine(s, this, chunkIsCode));
    }
    
    public int getNumLines()
    {
        return lines.size();
    }

    public ModLine getLine(int index)
    {
        return lines.get(index);
    }
    
    public boolean hasNextLine()
    {
        return false;
    }
    
    public String getNextLine()
    {
        return "";
    }
}
