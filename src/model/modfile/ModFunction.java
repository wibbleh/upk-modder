package model.modfile;

import java.util.ArrayList;

/**
 *
 * @author Amineri
 */


public class ModFunction
{
    private ModChunk header;
    private ArrayList<ModReplacement> replacements;
    private ModUpk owner;
    private int capacity = 5;
    
    private boolean inHeader;
    
    private String functionName;
    
    public ModFunction(String name, ModUpk upk)
    {
        owner = upk;
        header = new ModChunk();
        replacements = new ArrayList<>(capacity);
        functionName = name;
        inHeader = true;
    }
    
    public ModUpk getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().contains("[BEFORE_HEX]"))
        {
            if(replacements.size() == capacity)
            {
                capacity +=5;
                replacements.ensureCapacity(capacity);
            }
            replacements.add(new ModReplacement(this));
            inHeader = false;
        }
        if(inHeader)
        {
            header.addLine(s);
        }
        else
        {
            int last = replacements.size()-1;
            replacements.get(last).addLine(s);
        }
    }
    

    public int getNumLines()
    {
        int numLines = header.getNumLines();
        for(ModReplacement r:replacements)
        {
            numLines += r.getNumLines();
        }
        return numLines;
    }

    public ModLine getLine(int index)
    {
        if(index >=0 && index < getNumLines())
        {
            if(index < header.getNumLines())
            {
                return header.getLine(index);
            }
            index -= header.getNumLines();
            int iCount = 0;
            for(ModReplacement r : replacements)
            {
                if (iCount + r.getNumLines() > index)
                {
                    return r.getLine(index-iCount);
                }
                iCount += r.getNumLines();
            }
        }
        return null;
    }
    
    public String getName()
    {
        return functionName;
    }
    
    public ModReplacement getReplacement(int i)
    {
        if(i <= replacements.size() && i >= 0)
        {
            return replacements.get(i);
        }
        else
        {
            return null;
        }
    }
}
