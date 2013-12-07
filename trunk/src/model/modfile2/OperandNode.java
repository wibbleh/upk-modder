package model.modfile2;

/**
 *
 * @author Amineri
 */

public class OperandNode extends Node
{
    
    public OperandNode(Node owner)
    {
        super(owner);
        numTokens = 0;
    }

    public String parseToken(String s)
    {
        int sizeIn = s.split("\\s").length;
        data = s.split("\\s")[0];
        if(data.isEmpty())
        {
            return "ERROR";
        }
        String sOpCodes = opTable.getOperandString(data);
        if(!data.equalsIgnoreCase(sOpCodes.split("\\s",2)[0]))
        {
            System.out.println("/* opcode mismatch */");
            return "ERROR";
        }
        s = s.split("\\s",2)[1];
        String[] sParseItems = sOpCodes.split("\\s",3)[2].split("\\s");
        for(String sParseItem : sParseItems)
        {
            sParseItem = sParseItem.toUpperCase();
            if(sParseItem.matches("[0-9]"))
            {
                TokenNode n = new TokenNode(this);
                addToken(n);
                s = n.parseToken(s, Integer.parseInt(sParseItem));
                continue;
            }
            if(sParseItem.equals("G"))
            {
                OperandNode n = new OperandNode(this);
                addToken(n);
                s = n.parseToken(s);
                continue;
            }
            if(sParseItem.equals("P"))
            {
                while(!s.split("\\s")[0].equals("16"))
                {
                    OperandNode n = new OperandNode(this);
                    addToken(n);
                    s = n.parseToken(s);
                }
                continue;
            }
            if(sParseItem.equals("R"))
            {
                ReferenceNode n = new ReferenceNode(this, false);
                addToken(n);
                s = n.parseToken(s);
                continue;
            }
            if(sParseItem.equals("NR"))
            {
                ReferenceNode n = new ReferenceNode(this, true);
                addToken(n);
                s = n.parseToken(s);
                continue;
            }
            if(sParseItem.equals("N"))
            {
                StringNode n = new StringNode(this);
                addToken(n);
                s = n.parseToken(s);
                continue;
            }
            if(sParseItem.startsWith("S"))
            {
                OffsetRelativeNode n = new OffsetRelativeNode(this, sParseItem);
                addToken(n);
                s = n.parseToken(s);
            }
            if(sParseItem.equals("J"))
            {
                OffsetJumpNode n = new OffsetJumpNode(this);
                addToken(n);
                s = n.parseToken(s);
            }
            if(sParseItem.equals("C"))
            {
                if(s.split("\\s")[0].equalsIgnoreCase("FF") && s.split("\\s")[1].equalsIgnoreCase("FF"))
                {
                    TokenNode n = new TokenNode(this);
                    addToken(n);
                    s = n.parseToken(s, Integer.parseInt(sParseItem));
                }
                else
                {
                    OffsetJumpNode n1 = new OffsetJumpNode(this);
                    addToken(n1);
                    s = n1.parseToken(s);

                    OperandNode n2 = new OperandNode(this);
                    addToken(n2);
                    s = n2.parseToken(s);
                }
            }

        }
        int sizeOut = s.split("\\s").length;
        return s;
    }
        

    @Override
    protected OperandNode getToken()
    {
        return this;
    }
    
    public boolean isFunctionRef()
    {
        return false;
    }
    
    public int getOffset()
    {
        return -1;
    }
    
    static class TokenNode extends OperandNode
    {
        public TokenNode(Node owner)
        {
            super(owner);
            data = "";
        }

        protected String parseToken(String s, int num)
        {
            for(int i = 0; i < num; i++)
            {
                data += s.split("\\s", 2)[0] + " ";
                s = s.split("\\s", 2)[1];
            }
            return s;
        }
    }    

    class OffsetJumpNode extends TokenNode
    {
        private int jumpoffset;
        
        public OffsetJumpNode(Node owner)
        {
            super(owner);
        }
        
        @Override
        public String parseToken(String s)
        {
            s = super.parseToken(s, 2);
            int int0 = Integer.getInteger(data.split("\\s")[0], 16);
            int int1 = Integer.getInteger(data.split("\\s")[1], 16);
            jumpoffset = 256*int0 + int1;
            return s;
        }
        
        @Override
        public int getOffset()
        {
            return jumpoffset;
        }
    }

    class OffsetRelativeNode extends TokenNode
    {
        private int jumpoffset;

        public OffsetRelativeNode(Node owner, String operand)
        {
            super(owner);
        }

        @Override
        public String parseToken(String s)
        {
            s = super.parseToken(s, 2);
            int int0 = Integer.getInteger(data.split("\\s")[0], 16);
            int int1 = Integer.getInteger(data.split("\\s")[1], 16);
            jumpoffset = 256*int0 + int1;
            return s;
        }
        
        @Override
        public int getOffset()
        {
            return jumpoffset;
        }
        
    }

    class ReferenceNode extends TokenNode
    {
        private final boolean isFunction;
        private int value;

        public ReferenceNode(Node owner, boolean isFunction)
        {
            super(owner);
            this.isFunction = isFunction;
        }

        @Override
        public String parseToken(String s)
        {
            s = super.parseToken(s, 4);
            return s;
        }
        
        @Override
        public boolean isFunctionRef()
        {
            return isFunction;
        }
        
        @Override
        public int getMemorySize()
        {
            if(isFunctionRef())
            {
                return 4;
            }
            else
            {
                return 8;
            }
        }
    }

    class StringNode extends TokenNode
    {

        public StringNode(Node owner)
        {
            super(owner);
        }

        @Override
        public String parseToken(String s)
        {
            while(!s.split("\\s",2)[0].equals("00"))
            {
                s = super.parseToken(s, 1);
                if(s.isEmpty())
                {
                    return "ERROR";
                }
            }
            s = super.parseToken(s, 1);
            return s;
        }
        
    }
}
