package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * A specific type of Unreal hex bytecode leaf used for holding String data.
 * @author Amineri
 */
public class ModStringLeaf extends ModTreeLeaf {

	/*
	 * The string equivalent of the bytecode data
	 */
	private String stringData = null;
	
	/**
	 * Construct a ModstringLeaf with the designated parent
	 * @param parent - must always be a ModOperandNode
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
	 * Parses a variable length string from the passed unreal bytecode string.
	 * Parses until a 00 bytecode (string terminator) is encountered
	 * @param s -- original bytecode string
	 * @param num -- number of bytes to consume, or 0 if unknown
	 * @return -- remaining unparsed bytecode string
	 * @throws java.lang.Exception for errors parsing Unreal bytecode
	 */
	@Override
	public String parseUnrealHex(String s, int num) throws Exception {
		if (!s.split("\\s", 2)[0].equals("00")) {
			int length = s.split("00")[0].length() / 3;
			byte[] bArray = new byte[length];
			int count = 0;
			while (!s.split("\\s", 2)[0].equals("00")) {
				bArray[count] = (byte) (Integer.parseInt(s.split("\\s", 2)[0],
						16) & 0xFF);
				s = super.parseUnrealHex(s, 1);
				if (s.isEmpty()) {
					throw new Exception("Improperly terminated string.");
				}
				count++;
				stringData = new String(bArray);
			}
		}
		return super.parseUnrealHex(s, 1);
	}

	/**
	 * Overrides string naming for display via JTreePane
	 * @param expanded
	 * @return
	 */
	@Override
	public String toString(boolean expanded) {
		return (expanded) ? (this.toString() + " " + "\"" + stringData + "\"") : this.toString();
	}

}
