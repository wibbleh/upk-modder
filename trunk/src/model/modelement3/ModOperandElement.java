package model.modelement3;

/**
 *
 * @author Amineri
 */


public class ModOperandElement extends ModElement
{

    private String operand;
    
    ModOperandElement(ModElement o)
    {
        super(o);
        name = "ModOperandElement";
    }

    /**
     * Parses a passed string into ModTokens
     * @param s
     * @return - the unparsed string remnant
     */
    public String parseUnrealHex(String s)
    {
        operand = s.split("\\s")[0];
        if(operand.isEmpty())
        {
            return "ERROR";
        }
        String sOpCodes = opTable.getOpString(operand);
        if(!operand.equalsIgnoreCase(sOpCodes.split("\\s",2)[0]))
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
                ModGenericToken n = new ModGenericToken(this);
                addElement(n);
                s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
                continue;
            }
            if(sParseItem.equals("G"))
            {
                ModOperandElement n = new ModOperandElement(this);
                addElement(n);
                s = n.parseUnrealHex(s);
                continue;
            }
            if(sParseItem.equals("P"))
            {
                while(!s.split("\\s")[0].equals("16"))
                {
                    ModOperandElement n = new ModOperandElement(this);
                    addElement(n);
                    s = n.parseUnrealHex(s);
                }
                continue;
            }
            if(sParseItem.equals("R"))
            {
                ModReferenceToken n = new ModReferenceToken(this, false);
                addElement(n);
                s = n.parseUnrealHex(s);
                continue;
            }
            if(sParseItem.equals("NR"))
            {
                ModReferenceToken n = new ModReferenceToken(this, true);
                addElement(n);
                s = n.parseUnrealHex(s);
                continue;
            }
            if(sParseItem.equals("N"))
            {
                ModStringToken n = new ModStringToken(this);
                addElement(n);
                s = n.parseUnrealHex(s);
                continue;
            }
            if(sParseItem.startsWith("S"))
            {
                ModOffsetToken n = new ModOffsetToken(this, sParseItem);
                addElement(n);
                s = n.parseUnrealHex(s);
            }
            if(sParseItem.equals("J"))
            {
                ModOffsetToken n = new ModOffsetToken(this);
                addElement(n);
                s = n.parseUnrealHex(s);
            }
            if(sParseItem.equals("C"))
            {
                if(s.split("\\s")[0].equalsIgnoreCase("FF") && s.split("\\s")[1].equalsIgnoreCase("FF"))
                {
                    ModGenericToken n = new ModGenericToken(this);
                    addElement(n);
                    s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
                }
                else
                {
                    ModOffsetToken n1 = new ModOffsetToken(this);
                    addElement(n1);
                    s = n1.parseUnrealHex(s);

                    ModOperandElement n2 = new ModOperandElement(this);
                    addElement(n2);
                    s = n2.parseUnrealHex(s);
                }
            }
        }
        return s;
    }
}
