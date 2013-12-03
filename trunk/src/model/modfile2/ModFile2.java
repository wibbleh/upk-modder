package model.modfile2;

import parser.unrealhex.OperandTable;

/**
 *
 * @author Amineri
 */


public class ModFile2
{
    private RootNode rootNode = null;
    protected OperandTable opTable;
    
    public ModFile2()
    {
        if(rootNode == null)
        {
            rootNode = new RootNode();
        }
        opTable = null;
    }
    
    public ModFile2(OperandTable table)
    {
        if(rootNode == null)
        {
            rootNode = new RootNode(table);
        }
        opTable = table;
    }
    
    /**
     *
     * @param s
     */
    public void addLine(String s)
    {
        rootNode.addLine(s);
    }
    
    public void addLine(LineNode line, boolean test)
    {
        rootNode.addLine(line);
    }
    
    public LineNode getLine(int index)
    {
        return rootNode.getLine(index);
    }
       
    public int getNumLines()
    {
        return rootNode.getNumLines();
    }
    
    public OperandTable getOpTable()
    {
        return opTable;
    }

}
