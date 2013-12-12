package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * TODO: API
 * @author Amineri
 */
public class ModStringLeaf extends ModTreeLeaf {

	/**
	 * TODO: API
	 * 
	 * @param parent
	 */
	public ModStringLeaf(ModOperandNode parent) {
		super(parent);
		
		this.setContextFlag(ModContextType.VALID_CODE, true);
	}
	
	@Override
	public String getName() {
		return "ModStringToken";
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
		while (!s.split("\\s", 2)[0].equals("00")) {
			s = super.parseUnrealHex(s, 1);
			if (s.isEmpty()) {
				return "ERROR";
			}
		}
		return super.parseUnrealHex(s, 1);
	}
	
}
