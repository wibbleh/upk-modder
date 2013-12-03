package model.modfile2;

/**
 *
 * @author Amineri
 */


public class CommentNode extends Node
{

    CommentNode(Node owner)
    {
        super(owner);
    }
    
    @Override
    protected boolean addBranch(String s)
    {
         return true;
    }

    @Override
    protected Node newBranch(String s)
    {
        return new LineNode(this);
    }

}
