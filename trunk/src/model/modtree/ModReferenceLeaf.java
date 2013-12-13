package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * TODO: API
 * @author Amineri
 */
public class ModReferenceLeaf extends ModTreeLeaf {
	
	/**
	 * TODO: API
	 */
	private final boolean virtualFunction;
	
	/**
	 * TODO: API
	 */
	private int value;

	/**
	 * TODO: API
	 * @param o
	 * @param virtualFunction
	 */
	public ModReferenceLeaf(ModTreeNode o, boolean virtualFunction) {
		super(o);
		this.virtualFunction = virtualFunction;
		
		this.setContextFlag(ModContextType.VALID_CODE, true);
	}
	
	@Override
	public String getName() {
		return "ModReferenceToken";
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
		this.value = 0;
		int endOffset;
		if (s.startsWith("{|") || s.startsWith("<|")) {
			this.text = s.split("\\s", 2)[0].trim() + " ";
			endOffset = this.getEndOffset();
			endOffset += this.text.length();
			s = s.split("\\s", 2)[1];
			this.setRange(this.getStartOffset(), endOffset);
			return s;
		} else {
			String[] tokens = s.split("\\s");
			for (int i = 0; i < 4; i++) {
				this.value += Integer.parseInt(tokens[i], 16) << (8 * i);
			}
		}
		return super.parseUnrealHex(s, 4);
	}

	@Override
	public boolean isVirtualFunctionRef() {
		return this.virtualFunction;
	}

	@Override
	public int getMemorySize() {
		return (this.isVirtualFunctionRef()) ? 4 : 8;
	}

	@Override
	public int getRefValue() {
		return this.value;
	}
	
	public String getTextNoTags() {
		String s = this.text.trim();
		return s.substring(2, s.length()-2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModReferenceLeaf) {
			ModReferenceLeaf that = (ModReferenceLeaf) obj;
			return (that.getRefValue() == this.getRefValue());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.value;
	}
}
