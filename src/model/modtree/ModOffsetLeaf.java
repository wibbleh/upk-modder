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
	ModOffsetLeaf(ModOperandNode parent) {
		this(parent, null);
	}

	/**
	 * TODO: API
	 * @param parent
	 * @param operand
	 */
	ModOffsetLeaf(ModOperandNode parent, String operand) {
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

	@Override
	public int getOffset() {
		return this.jumpOffset;
	}
}
