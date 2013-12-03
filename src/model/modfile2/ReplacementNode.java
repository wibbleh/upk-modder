package model.modfile2;

/**
 *
 * @author Amineri
 */


public class ReplacementNode extends Node
{
    public ReplacementNode(Node owner)
    {
        super(owner);
        linebranches.add(new BlockNode(this));
    }

    @Override
    public void addLine(String s)
    {
        super.addLine(s);
    }

    @Override
    protected boolean addBranch(String s)
    {
         return s.toUpperCase().startsWith("[/BEFORE_HEX]") ||
                 s.toUpperCase().startsWith("[AFTER_HEX]") ||
                 s.toUpperCase().startsWith("[/AFTER_HEX]");
    }

    @Override
    protected Node newBranch(String s)
    {
        if(s.toUpperCase().startsWith("[/BEFORE_HEX]"))
        {
            return new CommentNode(this);
        }
        if(s.toUpperCase().startsWith("[AFTER_HEX]"))
        {
            return new BlockNode(this);
        }
        if(s.toUpperCase().startsWith("[/AFTER_HEX]"))
        {
            return new CommentNode(this);
        }
        return new BlockNode(this);
    }
}
