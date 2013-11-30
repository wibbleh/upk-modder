package model.modfile;

import java.util.ArrayList;

/**
 *
 * @author Amineri
 */


public class ModUpk
{
    private ArrayList<ModLine> header;
    private int headerCapacity = 10;
    private boolean inHeader;
    private ArrayList<ModFunction> functions;
    private int capacity = 10;
    
    private ModFile owner;
    
    String GUID;
    String rootName;
    
    public ModUpk(String name, ModFile f)
    {
        owner = f;
        header = new ArrayList<>(headerCapacity);
        functions = new ArrayList<>(capacity);
        inHeader = true;
        rootName = name;
    }
    
    public ModFile getOwner()
    {
        return owner;
    }

    public void addLine(String s)
    {
        if(s.toUpperCase().startsWith("FUNCTION="))
        {
            if(functions.size() == capacity)
            {
                capacity += 5;
                functions.ensureCapacity(capacity);
            }
            String name = s.split("\\s")[0];
            functions.add(new ModFunction(name, this));
            inHeader = false;
        }
        if(s.toUpperCase().startsWith("GUID="))
        {
            GUID = s.split("//")[0].split("=")[1];
        }
        if(inHeader)
        {
            if(header.size() == headerCapacity)
            {
                headerCapacity += 5;
                header.ensureCapacity(headerCapacity);
            }
            header.add(new ModLine(s));
        }
        else
        {
            functions.get(functions.size()-1).addLine(s);
        }
    }
    
    public int getNumLines()
    {
        int numLines = header.size();
        for(ModFunction f:functions)
        {
            numLines += f.getNumLines();
        }
        return numLines;
    }
    
    public ModLine getLine(int index)
    {
        if(index >=0 && index < getNumLines())
        {
            int iCount = 0;
            for(ModFunction f : functions)
            {
                if (iCount + f.getNumLines() > index)
                {
                    return f.getLine(index-iCount);
                }
            }
        }
        return null;
    }
    
    public String getGUID()
    {
        return GUID;
    }
}
