package model.modfile2;

import parser.unrealhex.OperandTable;

/**
 *
 * @author Amineri
 */


public class RootNode extends Node
{
    private int fileVersion;

    public RootNode()
    {
        super();
        linebranches.add(new CommentNode(this));
    }
    
    public RootNode(OperandTable table)
    {
        super(table);
        linebranches.add(new CommentNode(this));
    }

    @Override
    public void addLine(String s)
    {
        if(s.toUpperCase().startsWith("MODFILEVERSION"))
        {
            fileVersion = Integer.decode(s.split("//")[0].split("=")[1]);
        }
        super.addLine(s);
    }

    @Override
    protected boolean addBranch(String s)
    {
         return s.toUpperCase().startsWith("UPKFILE");
    }

    @Override
    protected Node newBranch(String s)
    {
        return new UpkNode(this);
    }

}
