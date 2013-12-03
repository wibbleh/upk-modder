package model.modfile2;

/**
 *
 * @author Amineri
 */


public class UpkNode extends Node
{
    private String GUID;
    private String rootName;

    public UpkNode(Node owner)
    {
        super(owner);
        linebranches.add(new CommentNode(this));
    }

    @Override
    public void addLine(String s)
    {
        if(s.toUpperCase().startsWith("UPKFILE"))
        {
            rootName = s.split("//")[0].split("=")[1];
        }
        if(s.toUpperCase().startsWith("GUID"))
        {
            GUID = s.split("//")[0].split("=")[1];
        }

        super.addLine(s);
    }

    @Override
    protected boolean addBranch(String s)
    {
         return s.toUpperCase().startsWith("FUNCTION");
    }

    @Override
    protected Node newBranch(String s)
    {
        return new FunctionNode(this);
    }

}
