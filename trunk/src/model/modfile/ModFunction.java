package model.modfile;

import java.util.ArrayList;

/**
 *
 * @author Amineri
 */


public class ModFunction
{
    private ArrayList<ModReplacement> replacements;
    private ModUpk owner;
    private int capacity = 5;
    
    private String functionName;
    
    public ModFunction(String name, ModUpk upk)
    {
        owner = upk;
        replacements = new ArrayList<>(20);
        replacements.add(new ModReplacement(this));
        functionName = name;
    }
    
    public ModUpk getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().contains("[BEFORE_HEX"))
        {
            if(replacements.size() == capacity)
            {
                capacity +=5;
                replacements.ensureCapacity(capacity);
            }
            replacements.add(new ModReplacement(this));
        }
        int last = replacements.size()-1;
        replacements.get(last).addLine(s);
    }
    

    public int getNumLines()
    {
        int numLines = 0;
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
            int iCount = 0;
            for(ModReplacement r : replacements)
            {
                if (iCount + r.getNumLines() > index)
                {
                    return r.getLine(index-iCount);
                }
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
