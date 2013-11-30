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
    
    public ModChunk(ModBlock block)
    {
        owner = block;
        initLines(capacity);
        chunkIsCode = false;
    }
    
    public ModChunk(boolean isCode, ModBlock block)
    {
        owner = block;
        initLines(capacity);
        chunkIsCode = isCode;
    }
    
    public ModBlock getOwner()
    {
        return owner;
    }

    private void initLines(int size)
    {
        if(size > 0)
        {
            lines = new ArrayList<>(size);
        }
        else
        {
            lines = new ArrayList<>(50);
        }
    }
    
    public void addLine(String line)
    {
        if(lines.size() == capacity)
        {
            capacity += 10;
            lines.ensureCapacity(capacity);
        }
        lines.add(new ModLine(line, this));
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
