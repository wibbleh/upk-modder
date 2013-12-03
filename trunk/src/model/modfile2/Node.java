package model.modfile2;

import java.util.ArrayList;
import parser.unrealhex.OperandTable;

/**
 *
 * @author Amineri
 */


public class Node 
{
    protected ArrayList<Node> linebranches;
    protected int numLines;
    protected int capacity = 5;

    protected ArrayList<Node> tokenbranches;
    protected int numTokens;
    protected int tokenCapacity = 5;

    protected static OperandTable opTable;
    
    protected Node owner;

    protected String data;

    public Node()
    {
        this.linebranches = new ArrayList<>(capacity);
        this.tokenbranches = new ArrayList<>(tokenCapacity);
        this.owner = null;
    }

    public Node(Node owner)
    {
        this.linebranches = new ArrayList<>(capacity);
        this.tokenbranches = new ArrayList<>(tokenCapacity);
        this.owner = owner;
    }
    
    public Node(OperandTable table)
    {
        this.linebranches = new ArrayList<>(capacity);
        this.tokenbranches = new ArrayList<>(tokenCapacity);
        this.owner = null;
        Node.opTable = table;
    }

    public Node(Node owner, OperandTable table)
    {
        this.linebranches = new ArrayList<>(capacity);
        this.tokenbranches = new ArrayList<>(tokenCapacity);
        this.owner = owner;
        Node.opTable = table;
    }

    public void addToken(Node n)
    {
        if(tokenCapacity < tokenbranches.size() +1)
        {
            tokenCapacity +=5;
            tokenbranches.ensureCapacity(tokenCapacity);
        }
        tokenbranches.add(n);
        numTokens++;
    }
    
    public void addLine(String s)
    {
        if(capacity < linebranches.size()+1)
        {
            capacity += 5;
            linebranches.ensureCapacity(capacity);
        }
        if(addBranch(s))
        {
            linebranches.add(newBranch(s));
        }
        linebranches.get(linebranches.size()-1).addLine(s);
        numLines++;
    }

    public void addLine(LineNode line)
    {
        addLine(line.data); // TODO : allow full writing
    }

    protected boolean addBranch(String s)
    {
        return true;
    }

    protected Node newBranch(String s)
    {
        return new Node(this);
    }

    public int getNumLines()
    {
        return numLines; 
    }

    public int getNumTokens()
    {
        int num = 1;
        for(Node branch : tokenbranches )
        {
            num += branch.getNumTokens();
        }
        return num;
    }
    
    protected int getMemorySizeBranches()
    {
        int num = 0;
        for(Node branch : tokenbranches)
        {
            num += branch.getMemorySize();
        }
        return num;
    }
    
    public int getMemorySize()
    {
        return data.split("\\s").length + getMemorySizeBranches();
    }
    
    public LineNode getLine()
    {
        return null; 
    }
    
    public String getData()
    {
        return data;
    }
    
    public LineNode getLine(int index)
    {
        int iCount = 0;
        if(linebranches.isEmpty())
        {
            return getLine();
        }
        for(Node branch : linebranches)
        {
            if (iCount + branch.getNumLines() > index)
            {
                return branch.getLine(index-iCount);
            }
            iCount += branch.getNumLines();
        }
        return null;
    }

    protected OperandNode getToken()
    {
        return null;
    }
    
    public OperandNode getToken(int index)
    {
        int iCount = 0;
        if(index == 0)
        {
            return getToken();
        }
        iCount++;
        for(Node branch : tokenbranches)
        {
            if (iCount + branch.getNumTokens() > index)
            {
                return branch.getToken(index-iCount);
            }
            iCount += branch.getNumTokens();
        }
        return null;
    }
    
}
