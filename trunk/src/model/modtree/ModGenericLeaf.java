package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * TODO: API
 * @author Amineri
 */
public class ModGenericLeaf extends ModTreeLeaf {
	
	private boolean isOperand;

	/**
	 * TODO: API
	 * @param parent
	 */
	public ModGenericLeaf(ModTreeNode parent) {
		this(parent, false);
	}

	/**
	 * Overrides string naming for display via JTreePane
	 * @param expanded
	 * @return
	 */
	@Override
	public String toString(boolean expanded) {
		if(expanded) {
			if(this.isOperand) {
				return super.toString() + "  (Operand Token)";
			} else {
				if(this.text.startsWith("16") || this.text.startsWith("15")) {
					return super.toString() + "  (Function terminator)";
				} else {
					return super.toString() + "  (Unknown)";
				}
			}
		} else {
			return super.toString();
		}
	}
	
	/**
	 * TODO API
	 * @param parent
	 * @param operand
	 */
	public ModGenericLeaf(ModTreeNode parent, boolean operand) {
		super(parent);
		this.isOperand = operand;
		this.setContextFlag(ModContextType.VALID_CODE, true);
	}
	
	@Override
	public String getName() {
		return (this.isOperand) ? "OperandToken" : "GenericToken";
	}

}
