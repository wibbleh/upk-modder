package parser.unrealhex;



/**
 *
 * @author Amineri
 */


public class MemorySizeCalculator
{
    private String s;
    private String[] tokens;
    private int memorySize;
    private int stringPosition;
    private boolean error;
    
    private OperandTable m_kOpTable;

    public MemorySizeCalculator(OperandTable kOpTable)
    {
        m_kOpTable = kOpTable;
    }

    public int parseString(String sHex)
    {
        error = false;
        s = sHex;
        if(s.isEmpty())
            return 0;
        tokens = s.split("\\s"); 
        stringPosition = 0;
        memorySize = 0;


        while(stringPosition < tokens.length)
        {
            parseGenericObject();
        }
        if(error)
        {
            return -1;
        }
        else
        {
            return memorySize;
        }
    }

    private void parseGenericObject()
    {
        String sOpCodes = OperandTable.getOperandString(tokens[stringPosition]);
        if(!tokens[stringPosition].equalsIgnoreCase(sOpCodes.split("\\s",2)[0]))
        {
            System.out.println("/* opcode mismatch */");
            error = true;
        }
        else
        {
            String[] sParseItems = sOpCodes.split("\\s",2)[1].split("\\s");
            for(String sParseItem : sParseItems)
            {
                sParseItem = sParseItem.toUpperCase();
                if(sParseItem.matches("[0-9]"))
                {
                    mirrorTokens(Integer.parseInt(sParseItem));
                    continue;
                }
                else if(sParseItem.equals("G"))
                {
                    parseGenericObject();
                    continue;
                }
                else if(sParseItem.equals("P"))
                {
                    while(!(tokens[stringPosition].equals("16") || tokens[stringPosition].equals("15")))
                    {
                        parseGenericObject();                        
                    }
                    continue;
                }
                else if(sParseItem.equals("R"))
                {
                    memorySize += 4;
                    mirrorTokens(4);
                    continue;
                }
                else if(sParseItem.equals("N"))
                {
                    while(!tokens[stringPosition].equals("00"))
                    {
                        mirrorTokens(1);                        
                    }
                    mirrorTokens(1);
                    continue;
                }
                else if(sParseItem.equals("NR"))
                {
                    mirrorTokens(4);
                    continue;
                }
                else if(sParseItem.startsWith("S") || sParseItem.equals("J"))
                {
                    mirrorTokens(2);
                }
                else if(sParseItem.equals("C"))
                {
                    if(tokens[stringPosition].equalsIgnoreCase("FF") && tokens[stringPosition+1].equalsIgnoreCase("FF"))
                    {
                        mirrorTokens(2);
                    }
                    else
                    {
                        mirrorTokens(2);
                        parseGenericObject();                        
                    }
                }
                else
                {
                    error = true;
                }
            }
        }
    }

    public void mirrorTokens(int iNum)
    {
        stringPosition += iNum;
        if(stringPosition > tokens.length)
            error = true;
        memorySize += iNum;
    }

}
