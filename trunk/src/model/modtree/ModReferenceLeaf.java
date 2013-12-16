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
	 * Records whether the reference is a number or name
	 */
	private boolean isName;
	
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
	 * Overrides string naming for display via JTreePane
	 * @return
	 */
	@Override
	public String toString() {
		String refName = "";
		if (this.getTree().getSourceUpk() != null) {
			if(!this.isName()) {
				if(this.isVirtualFunctionRef()) {
					if(this.getRefValue() >= 0) {
						refName = this.getTree().getSourceUpk().getVFRefName(this.getRefValue());
					}
				} else {
					if(this.getRefValue() != 0) {
						refName = this.getTree().getSourceUpk().getRefName(this.getRefValue());
					}
				}
			}
		}
		if(getParentNode().expanded) {
			if(refName == "") {
				return super.toString() + "  (Reference)";
			} else {
				return super.toString() + "  (Reference " + refName + ")";
			}
		} else {
			return super.toString();
		}
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
			this.isName = true;
			return s;
		} else {
			String[] tokens = s.split("\\s");
			for (int i = 0; i < 4; i++) {
				this.value += Integer.parseInt(tokens[i], 16) << (8 * i);
			}
			this.isName = false;
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
	
	/**
	 * Sets the reference value;
	 * @param value the reference value to set
	 */
	public void setRefValue(int value) {
		this.value = value;
	}
	
	/**
	 * Returns whether the Reference is a name ref or hex ref
	 * @return true if name ref, false if hex ref
	 */
	public boolean isName() {
		return this.isName;
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
