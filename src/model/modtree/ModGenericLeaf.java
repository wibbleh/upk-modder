package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * TODO: API
 * @author Amineri
 */
public class ModGenericLeaf extends ModTreeLeaf {
	
	private boolean operand;

	/**
	 * TODO: API
	 * @param parent
	 */
	public ModGenericLeaf(ModTreeNode parent) {
		this(parent, false);
	}

	/**
	 * TODO API
	 * @param parent
	 * @param operand
	 */
	public ModGenericLeaf(ModTreeNode parent, boolean operand) {
		super(parent);
		
		this.setContextFlag(ModContextType.VALID_CODE, true);
	}
	
	@Override
	public String getName() {
		return (this.operand) ? "OperandToken" : "GenericToken";
	}

}
