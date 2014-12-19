package model.modtree;

import model.modtree.ModContext.ModContextType;

/**
 * A generic tree leaf.
 * Can contain a generic single line string or be specialized for holding unreal bytecode.
 * @author Amineri
 */
public class ModTreeLeaf extends ModTreeNode {
    
	/**
	 * The string data of this token.
	 */
    protected String text;
    
    /**
     * Constructs a leaf node with the given parent.
     * @param parent -- can be ModTreeNode or ModOperandNode
     */
	public ModTreeLeaf(ModTreeNode parent) {
		this(parent, "");
	}
    
	/**
	 * Constructs a leaf node with the given parent and string data
	 * @param parent -- can be ModTreeNode or ModOperandNode
	 * @param data -- initial string data
	 */
	public ModTreeLeaf(ModTreeNode parent, String data) {
		this(parent, data, false);
	}
    
	/**
	 * Constructs a leaf node with the given parent and string data, marked as plain text (not unreal bytecode)
	 * @param parent -- can be ModTreeNode or ModOperandNode
	 * @param text -- initial string text
	 * @param plainText -- boolean indicating whether the node is plain text
	 */
	public ModTreeLeaf(ModTreeNode parent, String text, boolean plainText) {
		super(parent);
        this.setContextFlag(ModContextType.HEX_CODE, this.isCode());
		this.text = text;
		this.setPlainText(plainText);
//		this.hasBeenUpdated = true;
	}
	
	@Override
	public String toString(){
		// display memory size of line/component in tree view
		if(this.getMemorySize() == 0) {
			return "       " +getFullText(); 
		} else {
			return String.format("(%04X) ", this.getMemorySize()) + getFullText();
		}
	}
	/**
	 * Generic unreal bytecode parser.
	 * Removes the specified number of unreal bytecode entries.
	 * @param s -- the unreal bytecode as string
	 * @param num -- number of bytecodes to remove
	 * @return -- the remaining unreal bytecode string
	 * @throws java.lang.Exception for hex parsing error
	 */
	@Override
	public String parseUnrealHex(String s, int num) throws Exception {
		int endOffset = this.getEndOffset();
		for (int i = 0; i < num; i++) {
			if (s.contains(" ")) {
				endOffset += 3;
				text += s.split("\\s", 2)[0] + " ";
				s = s.split("\\s", 2)[1];
			} else if (!s.isEmpty()) {
				endOffset += 2;
				text += s;
				s = "";
			} else {
				throw new Exception ("Insufficent remaining hex bytes.");
			}				
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
			return (text.length()+1) / 3;
		}
	}

	/**
	 * Computes unreal engine file size of hex bytecodes represented as text
	 * @return
	 */
	@Override
	public int getFileSize() {
		if (this.isPlainText()) {
			return 0;
		} else {
			return (text.length()+1) / 3;
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
