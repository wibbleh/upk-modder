package model.modelement3;

import model.modelement3.ModContext.ModContextType;
import parser.unrealhex.OperandTable;

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
        setContextFlag(ModContextType.HEX_CODE, true);
        setContextFlag(ModContextType.VALID_CODE, true);
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
	protected String parseUnrealHex(String s) {
		int lastEnd = this.getStartOffset();
		operand = s.split("\\s")[0];
		if(operand.isEmpty()) {
			return "ERROR";
		}
		String sOpCodes = OperandTable.getOperandString(operand);
//        if(!operand.equalsIgnoreCase(sOpCodes.split("\\s",2)[0]))
//        {
//            System.out.println("/* opcode mismatch */");
//            return "ERROR";
//        }
//        s = s.split("\\s",2)[1];
		String[] sParseItems = sOpCodes.split("\\s", 2)[1].split("\\s");
		for(String sParseItem : sParseItems) {
			sParseItem = sParseItem.toUpperCase();
			if(sParseItem.matches("[0-9]")) {
				ModGenericToken n = new ModGenericToken(this);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("G")) {
				ModOperandElement n = new ModOperandElement(this);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("P")) {
				while( ! s.split("\\s")[0].equals("16")) {
					ModOperandElement n = new ModOperandElement(this);
					n.setRange(lastEnd, lastEnd);
					addElement(n);
					s = n.parseUnrealHex(s);
					lastEnd = n.getEndOffset();
				}
				continue;
			} else if(sParseItem.equals("R")) {
				ModReferenceToken n = new ModReferenceToken(this, false);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("NR")) {
				ModReferenceToken n = new ModReferenceToken(this, true);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("N")) {
				ModStringToken n = new ModStringToken(this);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.startsWith("S")) {
				ModOffsetToken n = new ModOffsetToken(this, sParseItem);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("J")) {
				ModOffsetToken n = new ModOffsetToken(this);
				n.setRange(lastEnd, lastEnd);
				addElement(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("C")) {
				if(s.split("\\s")[0].equalsIgnoreCase("FF") && s.split("\\s")[1].equalsIgnoreCase("FF")) {
					ModGenericToken n = new ModGenericToken(this);
					n.setRange(lastEnd, lastEnd);
					addElement(n);
					s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
					lastEnd = n.getEndOffset();
				} else {
					ModOffsetToken n1 = new ModOffsetToken(this);
					n1.setRange(lastEnd, lastEnd);
					addElement(n1);
					s = n1.parseUnrealHex(s);
					lastEnd = n1.getEndOffset();

					ModOperandElement n2 = new ModOperandElement(this);
					n2.setRange(lastEnd, lastEnd);
					addElement(n2);
					s = n2.parseUnrealHex(s);
					lastEnd = n2.getEndOffset();
				}
			}
		}
		this.setRange(this.getStartOffset(), lastEnd);
		return s;
	}
}
