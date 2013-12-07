package model.modelement3;

import static model.modelement3.ModContextType.*;

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
        setLocalContext(CODE, true);
        setLocalContext(VALIDCODE, true);
        operand = "";
    }

    @Override
    public String getName()
    {
        return name + "_" + operand;
    }
    
    /**
     * Parses a passed string into ModTokens.
     * TODO -- optimize this code
     * @param s
     * @return - the unparsed string remnant
     */
    protected String parseUnrealHex(String s)
    {
        int lastEnd = this.startOffset;
        operand = s.split("\\s")[0];
        if(operand.isEmpty())
        {
            return "ERROR";
        }
        String sOpCodes = opTable.getOpString(operand);
//        if(!operand.equalsIgnoreCase(sOpCodes.split("\\s",2)[0]))
//        {
//            System.out.println("/* opcode mismatch */");
//            return "ERROR";
//        }
//        s = s.split("\\s",2)[1];
        String[] sParseItems = sOpCodes.split("\\s",2)[1].split("\\s");
        for(String sParseItem : sParseItems)
        {
            sParseItem = sParseItem.toUpperCase();
            if(sParseItem.matches("[0-9]"))
            {
                ModGenericToken n = new ModGenericToken(this);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.equals("G"))
            {
                ModOperandElement n = new ModOperandElement(this);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s);
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.equals("P"))
            {
                while(!s.split("\\s")[0].equals("16"))
                {
                    ModOperandElement n = new ModOperandElement(this);
                    n.startOffset = lastEnd;
                    n.endOffset = lastEnd;
                    addElement(n);
                    s = n.parseUnrealHex(s);
                    lastEnd = n.endOffset;
                }
                continue;
            }
            if(sParseItem.equals("R"))
            {
                ModReferenceToken n = new ModReferenceToken(this, false);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s);
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.equals("NR"))
            {
                ModReferenceToken n = new ModReferenceToken(this, true);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s);
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.equals("N"))
            {
                ModStringToken n = new ModStringToken(this);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s);
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.startsWith("S"))
            {
                ModOffsetToken n = new ModOffsetToken(this, sParseItem);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s);
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.equals("J"))
            {
                ModOffsetToken n = new ModOffsetToken(this);
                n.startOffset = lastEnd;
                n.endOffset = lastEnd;
                addElement(n);
                s = n.parseUnrealHex(s);
                lastEnd = n.endOffset;
                continue;
            }
            if(sParseItem.equals("C"))
            {
                if(s.split("\\s")[0].equalsIgnoreCase("FF") && s.split("\\s")[1].equalsIgnoreCase("FF"))
                {
                    ModGenericToken n = new ModGenericToken(this);
                    n.startOffset = lastEnd;
                    n.endOffset = lastEnd;
                    addElement(n);
                    s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
                    lastEnd = n.endOffset;
                }
                else
                {
                    ModOffsetToken n1 = new ModOffsetToken(this);
                    n1.startOffset = lastEnd;
                    n1.endOffset = lastEnd;
                    addElement(n1);
                    s = n1.parseUnrealHex(s);
                    lastEnd = n1.endOffset;

                    ModOperandElement n2 = new ModOperandElement(this);
                    n2.startOffset = lastEnd;
                    n2.endOffset = lastEnd;
                    addElement(n2);
                    s = n2.parseUnrealHex(s);
                    lastEnd = n2.endOffset;
               }
            }
        }
        this.endOffset = lastEnd;
        return s;
    }
}
