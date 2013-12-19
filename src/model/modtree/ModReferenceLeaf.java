package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * A specific type of Leaf Node used when parsing Unreal hex bytecode.
 * References are always 4 bytes long, signed integers.
 * References can be Virtual Function references - which index to the namelist
 * Virtual Function references are always non-negative >= 0
 * References can be "regular" - which index to the objectlist or importlist
 * Positive regular references index the objectlist
 * Negative regular references index the importlist
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
	 * Construct a ModReferenceLeaf, indicating whether the Reference is a Virtual Function reference.
	 * @param o - parent node, must always be a ModOperandNode
	 * @param virtualFunction - boolean indicating if the reference is a virtual function or not
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
		return super.toString();
	}
	
	/**
	 * Overrides string naming for display via JTreePane
	 * @param expanded
	 * @return
	 */
	@Override
	public String toString(boolean expanded) {
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
		if(expanded) {
			if(refName.isEmpty()) {
				return super.toString() + "  (Reference)";
			} else {
				return super.toString() + "  (Reference " + refName + ")";
			}
		} else {
			return super.toString();
		}
	}
	
	/**
	 * Parses a string of unreal hex byte code (represented as a string)
	 * removes the appropriate number of bytecodes and returns the remaining string portion
	 * @param s - unreal bytecode as string
	 * @return - remainder of bytecode as string
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
