package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * TODO: API
 * @author Amineri
 */
public class ModOffsetLeaf extends ModTreeLeaf {
	
	/**
	 * TODO: API
	 */
	private String operand;
	
	/**
	 * TODO: API
	 */
	private int jumpOffset;

	/**
	 * TODO: API
	 * @param parent
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
	 * TODO: API
	 * @param parent
	 * @param operand
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
	 * TODO: API
	 * @param s
	 * @return
	 */
	public String parseUnrealHex(String s) {
		return this.parseUnrealHex(s, 0);
	}

	@Override
	public String parseUnrealHex(String s, int num) {
		String res = super.parseUnrealHex(s, 2);
		
		// parse jump offset
		String[] split = this.getText().split("\\s");
		int int0 = Integer.parseInt(split[0], 16);
		int int1 = Integer.parseInt(split[1], 16);
		this.jumpOffset = 256 * int0 + int1;
		
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
