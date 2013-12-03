package model.modfile2;

/**
 *
 * @author Amineri
 */


    //BlockNodes contain a single hex block of lines
public class BlockNode extends Node
{
    private boolean inCode;
    private boolean inHeader;


    public BlockNode(Node owner)
    {
        super(owner);
        inCode = false;
        inHeader = false;
        linebranches.add(new LineNode(this));
    }

    @Override
    public void addLine(String s)
    {
        if(s.toUpperCase().startsWith("[CODE]"))
        {
            inCode = true;
        }
        if(s.toUpperCase().startsWith("[/CODE]"))
        {
            inCode = false;
        }
        if(s.toUpperCase().startsWith("[HEADER]"))
        {
            inHeader = true;
        }
        if(s.toUpperCase().startsWith("[/HEADER]"))
        {
            inHeader = false;
        }
        super.addLine(s);
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

    public boolean inCode()
    {
        return this.inCode;
    }

    public boolean inHeader()
    {
        return this.inHeader;
    }
}
