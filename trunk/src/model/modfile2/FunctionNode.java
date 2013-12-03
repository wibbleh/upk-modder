package model.modfile2;

/**
 *
 * @author Amineri
 */


public class FunctionNode extends Node
{
    String functionName;

    public FunctionNode(Node owner)
    {
        super(owner);
        linebranches.add(new CommentNode(this));
    }

    @Override
    public void addLine(String s)
    {
        if(s.toUpperCase().startsWith("FUNCTION"))
        {
            functionName = s.split("//")[0].split("=")[1];
        }
        super.addLine(s);
    }

    @Override
    protected boolean addBranch(String s)
    {
         return s.toUpperCase().startsWith("[BEFORE_HEX]");
    }

    @Override
    protected Node newBranch(String s)
    {
        return new ReplacementNode(this);
    }

}
