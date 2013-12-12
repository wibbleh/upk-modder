package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * TODO: API
 * @author Amineri
 */
public class ModTreeLeaf extends ModTreeNode {
    
	/**
	 * The string data of this token.
	 */
    private String text;
    
    /**
     * TODO: API
     * @param parent
     */
	public ModTreeLeaf(ModTreeNode parent) {
		this(parent, "");
	}
    
	/**
	 * TODO: API
	 * @param parent
	 * @param data
	 */
	public ModTreeLeaf(ModTreeNode parent, String data) {
		this(parent, data, false);
	}
    
	/**
	 * TODO: API
	 * @param parent
	 * @param text
	 * @param plainText
	 */
	public ModTreeLeaf(ModTreeNode parent, String text, boolean plainText) {
		super(parent);
        this.setContextFlag(ModContextType.HEX_CODE, this.isCode());
		this.text = text;
		this.setPlainText(plainText);
		this.hasBeenUpdated = true;
	}
	
	/**
	 * TODO: API
	 * @param s
	 * @param num
	 * @return
	 */
	@Override
	public String parseUnrealHex(String s, int num) {
		int endOffset = this.getEndOffset();
		for (int i = 0; i < num; i++) {
			endOffset += 3;
			text += s.split("\\s", 2)[0] + " ";
			s = s.split("\\s", 2)[1];
		}
		this.setRange(this.getStartOffset(), endOffset);
		return s;
	}

	@Override
	public String getName() {
		return "ModToken";
	}

	/**
	 * Computes unreal engine memory size of hex bytecodes represented as text
	 * @return
	 */
	@Override
	public int getMemorySize() {
		if (this.isPlainText()) {
			return 0;
		} else {
			return text.length() / 3;
		}
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.getLineParent().setPlainText(true);
		this.text = text;
	}

	@Override
	public boolean isVirtualFunctionRef() {
		return false;
	}

	@Override
	public int getOffset() {
		return -1;
	}

	@Override
	public int getRefValue() {
		return -1;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

}
