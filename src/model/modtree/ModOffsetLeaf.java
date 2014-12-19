package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * An ModTree Leaf node that contains a jump offset
 * Can be an absolute offset (relative to start of function) 
 * or relative offset (relative to current position)
 * There are three types of relative offsets.
 * @author Amineri
 */
public class ModOffsetLeaf extends ModTreeLeaf {
	
	/**
	 * Stores the operand type of the offset.
	 * Is null for absolute jumps.
	 * Is "S0, S1, or S2" for the three types of relative offset.
	 */
	private String operand;
	
	/**
	 * Current jump offset as an integer.
	 * Is a short int (2-byte, unsigned) in hex.
	 */
	private int jumpOffset;

	/**
	 * Constructs an offset-type leaf with the given parent.
	 * Operand is by default set to null.
	 * Use this constructor for absolute jump offsets.
	 * @param parent can only be a ModOperandNode
	 */
	public ModOffsetLeaf(ModOperandNode parent) {
		this(parent, null);
	}

	/**
	 * Overrides string naming for display via JTreePane
	 * @param expanded
	 * @return
	 */
	@Override
	public String toString(boolean expanded) {
		if(expanded) {
			if(this.operand == null) {
				return super.toString() + "  (Absolute Jump)";
			} else {
				return super.toString() + "  (Relative Jump)";
			}
		} else {
			return super.toString();
		}
	}
	
	/**
	 * Constructs an offset-type leaf with a specified parent and operand
	 * @param parent can only be a ModOperandNode
	 * @param operand the subtype of relative offset
	 *  "S0" indicates a context-type (0x12 or 0x19) relative skip (size of final G in context)
	 *  "S1" indicates an object-type skip relative offset (size of next single G object)
	 *  "S2" indicates a parameter-type relative skip (size of next G objects parsed until 0x16 read + 1 for 0x16 token)
	 */
	public ModOffsetLeaf(ModOperandNode parent, String operand) {
		super(parent);
		this.operand = operand;
		
		this.setContextFlag(ModContextType.VALID_CODE, true);
	}
	
	@Override
	public String getName() {
		return (this.operand == null) ? "ModJumpToken" : "ModRelativeJumpToken";
	}
	
	/**
	 * Parse the unreal hex 
	 * @param s hex string to parse
	 * @return remaining string of hex
	 * @throws java.lang.Exception for parsing errors
	 */
	public String parseUnrealHex(String s) throws Exception {
		return this.parseUnrealHex(s, 2);
	}

	/**
	 * Parse the unreal hex 
	 * @param s hex string to parse
	 * @param num number of bytes to consume -- must be 2 for this class
	 * @return remaining string of hex
	 * @throws java.lang.Exception for parsing errors
	 */
	@Override
	public String parseUnrealHex(String s, int num) throws Exception {
		String res = super.parseUnrealHex(s, num);
		
		// parse jump offset -- implicit exception casting if not enough elements or malformed hex
		String[] split = this.getText().split("\\s");
		int int0 = Integer.parseInt(split[0], 16);
		int int1 = Integer.parseInt(split[1], 16);
		this.jumpOffset = 256 * int1 + int0;
		
		return res;
	}

	public String getOperand() {
		return this.operand;
	}
	
	@Override
	public int getOffset() {
		return this.jumpOffset;
	}
}
