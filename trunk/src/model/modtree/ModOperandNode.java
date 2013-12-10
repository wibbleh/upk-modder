package model.modtree;

import parser.unrealhex.OperandTable;
import model.modtree.ModContext.*;
import static model.modtree.ModContext.ModContextType.*;

/**
 *
 * @author Amineri
 */


public class ModOperandNode extends ModTreeNode
{

    private String operand;
    
    ModOperandNode(ModTreeNode o)
    {
        super(o);
        name = "ModOperandNode";
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
		boolean isOperand = true;
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
				ModGenericLeaf n;
				if(isOperand) {
					n = new ModGenericLeaf(this, true);
					isOperand = false;
				} else { 
					n = new ModGenericLeaf(this);
				}
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("G")) {
				ModOperandNode n = new ModOperandNode(this);
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("P")) {
				while( ! s.split("\\s")[0].equals("16")) {
					ModOperandNode n = new ModOperandNode(this);
					n.setRange(lastEnd, lastEnd);
					addNode(n);
					s = n.parseUnrealHex(s);
					lastEnd = n.getEndOffset();
				}
				continue;
			} else if(sParseItem.equals("R")) {
				ModReferenceLeaf n = new ModReferenceLeaf(this, false);
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("NR")) {
				ModReferenceLeaf n = new ModReferenceLeaf(this, true);
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("N")) {
				ModStringLeaf n = new ModStringLeaf(this);
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.startsWith("S")) {
				ModOffsetLeaf n = new ModOffsetLeaf(this, sParseItem);
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("J")) {
				ModOffsetLeaf n = new ModOffsetLeaf(this);
				n.setRange(lastEnd, lastEnd);
				addNode(n);
				s = n.parseUnrealHex(s);
				lastEnd = n.getEndOffset();
				continue;
			} else if(sParseItem.equals("C")) {
				if(s.split("\\s")[0].equalsIgnoreCase("FF") && s.split("\\s")[1].equalsIgnoreCase("FF")) {
					ModGenericLeaf n = new ModGenericLeaf(this);
					n.setRange(lastEnd, lastEnd);
					addNode(n);
					s = n.parseUnrealHex(s, Integer.parseInt(sParseItem));
					lastEnd = n.getEndOffset();
				} else {
					ModOffsetLeaf n1 = new ModOffsetLeaf(this);
					n1.setRange(lastEnd, lastEnd);
					addNode(n1);
					s = n1.parseUnrealHex(s);
					lastEnd = n1.getEndOffset();

					ModOperandNode n2 = new ModOperandNode(this);
					n2.setRange(lastEnd, lastEnd);
					addNode(n2);
					s = n2.parseUnrealHex(s);
					lastEnd = n2.getEndOffset();
				}
			}
		}
		this.setRange(this.getStartOffset(), lastEnd);
		return s;
	}
}
