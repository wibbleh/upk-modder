package model.modfile2;

import java.util.ArrayList;
import model.modfile2.OperandNode.*;

/**
 *
 * @author Amineri
 */


//LineNodes contain a single line of data
public class LineNode extends Node
{

    private boolean inCode;
    private boolean inHeader;

    private String line;
    private int indentation;
    
    private boolean validCode;

    public LineNode(Node owner)
    {
        super(owner);
        if(owner.getClass().getSimpleName().equals("BlockNode"))
        {
            inCode = ((BlockNode) owner).inCode();
            inHeader = ((BlockNode) owner).inHeader();
        }
        else
        {
            inCode = false;
            inHeader = false;
        }
        numTokens = 0;
    }

    @Override
    public void addLine(String s)
    {
        data = s;
        inCode = inCode && !asHex().isEmpty();
        numLines++;
        indentation = s.lastIndexOf("\t")+1;
        if(opTable != null && isCode() && !s.isEmpty())
        {
            try
            {
                String temp = asHex();
                while(!temp.isEmpty())
                {
                    OperandNode newop = new OperandNode(owner);
                    addToken(newop);
                    temp = newop.parseToken(temp);
                }
            }
            catch(Throwable x)
            {
                validCode = false;
                tokenbranches = null;
                System.out.println("Token parsing failed");
                numTokens = 0;
            }
        }
        else
        {
            tokenbranches = null;
            validCode = false;
            numTokens = 0;
        }
    }
    
    protected void addToken(String s)
    {
        
    }

    @Override
    public int getNumTokens()
    {
        return super.getNumTokens()-1;
    }
    
    @Override
    public OperandNode getToken(int index)
    {
        int iCount = 0;
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
    
    @Override
    protected boolean addBranch(String s)
    {
         return true;
    }

    @Override
    protected Node newBranch(String s)
    {
        return new OperandNode(this);
    }

    public String asString()
    {
        return data;
    }

    public String asString(boolean comments)
    {
        if(comments)
        {
            return data;
        }
        else
        {
            if(data.contains("//"))
            {
                return data.split("//")[0];
            }
            else
            {
                return data;
            }
        }
    }

    public String asHex()
    {
        String outString = "";
        String[] tokens = asString(false).split("\\s");
        for(String token : tokens)
        {
            if(token.toUpperCase().matches("[0-9A-F][0-9A-F]"))
            {
                outString += token + " ";
            }
        }
        return outString;
    }

    public boolean isCode()
    {
        return inCode;
    }

    public boolean isHeader()
    {
        return inHeader;
    }
    
    public int getIndentation()
    {
        return indentation;
    }
    
    @Override
    public LineNode getLine()
    {
        return this;
    }
}
