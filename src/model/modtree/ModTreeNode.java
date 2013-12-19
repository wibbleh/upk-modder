package model.modtree;

import static model.modtree.ModContext.ModContextType.AFTER_HEX;
import static model.modtree.ModContext.ModContextType.BEFORE_HEX;
import static model.modtree.ModContext.ModContextType.FILE_HEADER;
import static model.modtree.ModContext.ModContextType.HEX_CODE;
import static model.modtree.ModContext.ModContextType.HEX_HEADER;
import static model.modtree.ModContext.ModContextType.VALID_CODE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.tree.TreeNode;

import model.modtree.ModContext.ModContextType;

/**
 * Basic <code>TreeNode</code> implementation used in structuring modfile contents.
 * Tree can be displayed directly via JTree pane or used to format a <code>ModDocument</code>.
 * @author Amineri
 * @see {@link ModDocument}
 */
public class ModTreeNode implements TreeNode {
	
	/**
	 * The list of child nodes.
	 */
    private List<ModTreeNode> children;
    
    /**
     * The reference to the parent node. Is <code>null</code> if this node is the root node.
     */
    protected ModTreeNode parent;
    
    /**
     * Flag denoting whether this node contains pure textual data.
     */
    private boolean plainText;
    
    /**
     * The start offset of this node in the associated document.
     */
    private int startOffset;
    
    /**
     * The end offset of this node in the associated document.
     */
    private int endOffset;
    
    /**
     * The context flag container instance.
     */
    private ModContext context;
    
    /**
     * The style attributes of this node.
     */
    protected AttributeSet attributes;

	/**
	 * Flag indicating if the current node has been updated during the most recent insert/remove operation.
	 */
//	protected boolean hasBeenUpdated;
	
	/**
	 * Memory position of current line (if it is a valid hex line)
	 */
	protected int memoryPosition;
	
	/**
	 * File position of current line (if it is a valid hex line)
	 */
	protected int filePosition;
	
    /**
     * Constructs a mod node from the specified parent node.
     * @param parent the parent node
     */
	public ModTreeNode(ModTreeNode parent) {
		this(parent, false);
	}

	/**
	 * Indicates whether the node is expanded in the current view.
	 */
//	public boolean expanded;

	/**
	 * Indicates whether the node was updated during the most recent insert/remove operation.
	 * @return
	 */
//	@Deprecated
//	public boolean hasBeenUpdated() {
//		return hasBeenUpdated;
//	}
	
	/**
	 * Inits update flag of current node and all child nodes.
	 * @param b value to init flags to.
	 */
//	public void initUpdateFlags(boolean b) {
//		this.hasBeenUpdated = b;
//		if(isLeaf()) {
//			return;
//		}
//		for (int i = 0; i < this.getNodeCount(); i++) {
//			this.getChildNodeAt(i).initUpdateFlags(b);
//		}
//	}
	
	/**
	 * Sets update flag for the current node.
	 * @param b
	 */
//	protected void setUpdateFlag(boolean b) {
//		this.hasBeenUpdated = b;
//	}
	
	/**
	 * Constructs a mod node from the specified parent node and a flag
	 * denoting whether it contains pure textual data (i.e. non-parseable code).
	 * @param parent the parent node 
	 * @param isSimpleString <code>true</code> if this node contains only textual data,
	 *  <code>false</code> otherwise
	 */
	public ModTreeNode(ModTreeNode parent, boolean isSimpleString) {
		this.parent = parent;
		this.plainText = isSimpleString;

		// init properties
		this.children = new ArrayList<>();
		this.context = new ModContext();
	}

	/**
	 * Set the boolean flag associated with the specified context type.
	 * @param type the context type
	 * @param value the value to set
	 */
	public void setContextFlag(ModContextType type, boolean value) {
		this.context.setContextFlag(type, value);
	}
	
	/**
	 * Returns the boolean flag associated with the specified context type.
	 * @param type the context type
	 * @return either <code>true</code> or <code>false</code> 
	 */
	public boolean getContextFlag(ModContextType type) {
		return this.context.getContextFlag(type);
	}
	
	/**
	 * Re-initializes the context flag container instance.
	 */
	public void resetContextFlags() {
		this.context = new ModContext();
	}
    
	public ModTree getTree() {
		// fetch tree from parent, only the root node carries the actual
		// reference to the tree instance
		if(getParentNode() != null) {
			return this.getParentNode().getTree();
		} else {
			return null;
		}
	}

	/**
	 * Returns whether this node represents plain text content.
	 * @return <code>true</code> if this node contains plain text data,
	 *  <code>false</code> otherwise
	 */
	public boolean isPlainText() {
		return this.plainText;
	}
	
	/**
	 * Sets whether this node represents plain text content.
	 * @param plainText <code>true</code> if this node contains plain text data,
	 *  <code>false</code> otherwise
	 */
	public void setPlainText(boolean plainText) {
		this.plainText = plainText;
	}
	
	/**
	 * Returns the child node at the specified position.
	 * @param index the child node's position
	 * @return the child node or <code>null</code> if the position is out of range
	 */
	public ModTreeNode getChildNodeAt(int index) {
		// obligatory range check
		if ((index >= 0) && (index < this.children.size())) {
			return this.children.get(index);
		}
		return null;
	}
	
	/**
	 * Removes the child node at the specified position.
	 * @param index the position of the node to remove
	 * @return the removed node or <code>null</code> if the position is out of range
	 */
	public ModTreeNode removeChildNodeAt(int index) {
		// obligatory range check
		if ((index >= 0) && (index < this.children.size())) {
			return this.children.remove(index);
		}
		return null;
	}
	
	/**
	 * Removes all child nodes of this node.
	 */
	public void removeAllChildNodes() {
		this.children.clear();
	}
	
	/**
	 * Returns the number of child nodes of this node.
	 * @return the child node count
	 */
	public int getChildNodeCount() {
		return this.children.size();
	}

	/**
	 * Returns whether this node contains parseable UnrealScript byte code.
	 * @return <code>true</code> if this node or its parent contains code, <code>false</code> otherwise
	 */
	protected boolean isCode() {
		if (this.getParentNode() == null) {
			// this node is the root node which never contains code
			return false;
		} else {
			// if this node is not flagged as containing hex code pass check on to parent node
			return this.getContextFlag(HEX_CODE) || getParentNode().isCode();
		}
	}
	
	/**
	 * Sets the offset range of this node in the document.
	 * @param startOffset the start offset of this node in the document
	 * @param endOffset the end offset of this node in the document
	 */
	public void setRange(int startOffset, int endOffset) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	/**
	 * Invokes method to break code lines into tokens based on operand.<br>
	 * Operates at the line level.
	 * @param s
	 * @param num
	 * @return 
	 */
	public String parseUnrealHex(String s, int num) {
		if (!this.isValidHexLine()) {
			return null;
		}
		String oldString = this.getFullText();
		try {
			// extract initial string representation of this node
			String[] linebreak = this.toHexStringArray();

			// remove all children
			this.children.clear();
			
			// start parsing the document from the beginning of this node
			int currOffset = this.startOffset;
			
			String prefix = linebreak[0];
			if (!prefix.isEmpty()) {
				// wrap leading text data in plain token
				ModTreeLeaf leadToken = new ModTreeLeaf(this, linebreak[0], true);
				leadToken.setRange(currOffset, currOffset + linebreak[0].length());
				leadToken.setContextFlag(HEX_CODE, false);
				leadToken.setContextFlag(VALID_CODE, false);
				this.addNode(leadToken);
				// move offset to end of new token
				currOffset = leadToken.getEndOffset();
			}
			String hex = linebreak[1];
			while (!hex.isEmpty()) {
				int oldLength = hex.length();
				// cache length
				// create operand token
				ModOperandNode opElem = new ModOperandNode(this);
				opElem.setRange(currOffset, this.getEndOffset());
				hex = opElem.parseUnrealHex(hex);
				int lastLength = oldLength - hex.length();
				opElem.setRange(currOffset, currOffset + lastLength);
				this.addNode(opElem);
				currOffset += lastLength;
			}
			String suffix = linebreak[2];
			// wrap trailing text data in plain token
			ModTreeLeaf trailToken = new ModTreeLeaf(this, suffix, true);
			trailToken.setRange(currOffset, currOffset + suffix.length());
			trailToken.setContextFlag(HEX_CODE, false);
			trailToken.setContextFlag(VALID_CODE, false);
			// currStart = newToken.endOffset;
			this.addNode(trailToken);
			this.setContextFlag(VALID_CODE, true);
			this.plainText = false;
		} catch (Exception e) {
			// something went wrong, set error flag...
			setContextFlag(VALID_CODE, false);
			// mark node as plain text
			this.setPlainText(true);
			// ... remove any child nodes that may have been inserted... 
			this.children.clear();
			// ... insert original text data as plain mod token
			ModTreeLeaf t = new ModTreeLeaf(this, oldString, true);
			this.addNode(t);
			t.setRange(this.startOffset, this.endOffset);
		}
		return null;
	}
    
	/**
	 * Returns whether this node wraps hex string data.
	 * @return <code>true</code> if this node contains valid hex data, <code>false</code> otherwise
	 */
	public boolean isValidHexLine() {
		String[] tokens = getFullText().split("//")[0].trim().split("\\s");
		for (String token : tokens) {
			if (token.matches("[0-9A-Fa-f][0-9A-Fa-f]")
					|| (token.startsWith("<|") && token.endsWith("|>"))
					|| (token.startsWith("{|") && token.endsWith("|}"))
					) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
    
    /**
     * Returns an array containing a concatenated hex tokens and leading/trailing line contents.
     * @return a 3-element array containing hex tokens in its middle node
     */
	public String[] toHexStringArray() {
		// get initial string representation of this node
		String in = this.getFullText();

		// pre-allocate result array
		String outString[] = new String[3];
		Arrays.fill(outString, "");

		// tokenize string representation
		String[] tokens = in.split("//")[0].split("\\s");
		for (String token : tokens) {
			if (token.toUpperCase().matches("[0-9A-Fa-f][0-9A-Fa-f]")
					|| (token.startsWith("<|") && token.endsWith("|>"))
					|| (token.startsWith("{|") && token.endsWith("|}"))
					) {
				// concatenate hex tokens with whitespace delimiter
				outString[1] += token + " ";
			}
		}

		// extract prefix string
		int count = 0;
		while(tokens[count].isEmpty() && count < tokens.length) {count++;}
		if(count < tokens.length) {
			int first = in.indexOf(tokens[count]);
			outString[0] = in.substring(0, first);
		} else {
			outString[0] = "";
		}
		// extract suffix string
		int last = in.lastIndexOf(tokens[tokens.length - 1]);
		if ((last + 3) < in.length()) {
			outString[2] = in.substring(last + 3, in.length());
		}
		if((outString[0] + outString[1] + outString[2]).equals(in)) {
			return outString;
		} else { 
			return null;
		}
	}
    
    /**
     * Appends the specified node to the list of child nodes.
     * @param node the node to append
     */
	protected void addNode(ModTreeNode node) {
		this.addNode(this.children.size(), node);
	}

	/**
	 * Inserts the specified node at the specified position into the list of
	 * child nodes.
	 * @param index the position index
	 * @param node the node to insert
	 */
	protected void addNode(int index, ModTreeNode node) {
		this.children.add(index, node);
	}
    
	protected void updateContexts() {
		if(getTree() == null) {return;}
		String content = this.getFullText().toUpperCase().trim();
		if (this.plainText) {
			if (content.startsWith("UPKFILE=")) {
				getTree().setUpkName(this.getTagValue(this.getFullText()));
			} else if (content.startsWith("FUNCTION=")) {
				getTree().setFunctionName(this.getTagValue(this.getFullText()));
			} else if (content.startsWith("GUID=")) {
				getTree().setGuid(this.getTagValue(this.getFullText()));
			} else if (content.startsWith("MODFILEVERSION=")) {
				try {
					getTree().setFileVersion(Integer.parseInt(this.getTagValue(this.getFullText())));
				}
				catch (NumberFormatException x) {
					System.out.println("Invalid FileVersion: " +x);
				}
			}
		}
		// update global contexts
		if (this.foundHeader()) {
			getRoot().setContextFlag(FILE_HEADER, false);
		}
		if (getRoot().getContextFlag(FILE_HEADER)) {
			getRoot().setContextFlag(HEX_CODE, false);
			getRoot().setContextFlag(BEFORE_HEX, false);
			getRoot().setContextFlag(AFTER_HEX, false);
			getRoot().setContextFlag(HEX_HEADER, false);
			getRoot().setContextFlag(FILE_HEADER, true);
		} else {
			if (content.startsWith("[BEFORE_HEX]")) {
				getRoot().setContextFlag(BEFORE_HEX, true);
			} else if (content.startsWith("[/BEFORE_HEX]")) {
				getRoot().setContextFlag(BEFORE_HEX, false);
			} else if (content.startsWith("[AFTER_HEX]")) {
				getRoot().setContextFlag(AFTER_HEX, true);
			} else if (content.startsWith("[/AFTER_HEX]")) {
				getRoot().setContextFlag(AFTER_HEX, false);
			} else if (content.startsWith("[CODE]")) {
				getRoot().setContextFlag(HEX_CODE, true);
			} else if (content.startsWith("[/CODE]")) {
				getRoot().setContextFlag(HEX_CODE, false);
			} else if (content.startsWith("[HEADER]")) {
				getRoot().setContextFlag(HEX_HEADER, true);
			} else if (content.startsWith("[/HEADER]")) {
				getRoot().setContextFlag(HEX_HEADER, false);
			}
			getRoot().setContextFlag(FILE_HEADER, false);
		}
	}
    
	/**
	 * Returns whether this node wraps header-type content.
	 * @return <code>true</code> 
	 */
	protected boolean foundHeader() {
		ModTree tree = getTree();
		if(tree == null) {return false;}
		return !tree.getUpkName().isEmpty()
				&& !tree.getFunctionName().isEmpty()
				&& !tree.getGuid().isEmpty()
				&& (tree.getFileVersion() > 0);
	}

	/**
	 * Returns the value of a 'NAME=VALUE' tag.
	 * @param s the string to retrieve value from
	 * @return the tag value
	 */
	protected String getTagValue(String s) {
		// trim comments ("//"), split at tag name/value delimiter ("="), trim whitespace from value
		if(s.contains("=")) {
			return s.split("//", 2)[0].split("=", 2)[1].trim();
		} else {
			return "";
		}
	}
        
    /**
     * Removes length characters at offset (measured from beginning of model)
     * Updates startOffset and endOffset class values and deletes data
     * @param offset
     * @param length
     */
    public void remove(int offset, int length)
    {
        boolean removeInBranches = !isLeaf();
        if(length == 0)
            return;
        int rs = offset;
        int re = offset + length;
        int s = startOffset;
        int e = endOffset;
        // adjust start and end offsets for node / leaf
        if(re < e) { // removal end occurs prior to node end -- cases 1, 2, 3
            if(re < s) { // removal entirely before current node -- case 1
                startOffset -= length;
                endOffset -= length;
            } else if (rs < s ) { // removal start occurs prior to node start -- case 2
                if(isLeaf()) { // remove early part of data string for leaf
                    setText(getText().substring(re-s, getText().length()));
                }
                startOffset -= s - rs;
                endOffset -= length;
//				this.setUpdateFlag(true);
            } else { // removal start happens within node -- case 3
                if(isLeaf()) { // remove middle part of data string for leaf
                    setText(getText().substring(0, rs-s) + getText().substring(re-s, getText().length()));
                }
                endOffset -= length;
//				this.setUpdateFlag(true);
            }
        } else { // removal end happens after node end -- cases 4, 5, 6
            if(rs < s) { // remove start occurs prior to node start -- case 6
                if(isLeaf()) { // delete data string
                    setText("");
                }
                endOffset = startOffset;
//				this.setUpdateFlag(true);
//                removeModNode();
            } else if(rs < e) { // remove start happens in middle of node -- case 4
                if(isLeaf()) { // remove end part of data string for leaf
                    setText(getText().substring(0, e-rs));
                }
                endOffset -= e - rs;
//				this.setUpdateFlag(true);
            } else { // removal is after node -- case 5
                // no update needed
                removeInBranches = false;
            }
        }
        if(removeInBranches) {
            for(ModTreeNode branch : children) {
                branch.remove(offset, length);
        }
        }
    }
    
    /**
     * Removes a ModTreeNode, cleaning up references in the parent's branches list
     */
    protected void removeModNode()
    {
        int count = 0;
        for(ModTreeNode branch : getParentNode().children) { // scan through parent's branches
            if(branch.equals(this)) {  // found current node
                getParentNode().children.remove(count); // unlink node
                return;
            }
            count++;
        }
    }
    
    /**
     * Inserts string at given offset.
     * @param offset
     * @param string
     * @param as
     */
    public void insertString(int offset, String string, AttributeSet as)
    {
        int length = string.length();
        int s = startOffset;
        int e = endOffset;
		if(offset <= endOffset) {
			endOffset += length;
		}
		if(offset < startOffset) {
			startOffset += length;
		}
		if(isLeaf()){
            if(offset >= s && offset <= e){ // insertion is at leaf
                insertStringAtLeaf(offset, string, as);
            }
        } else { // recursive step for nodes
            if(offset >= s && offset <= e){ // insertion is at leaf
//				this.setUpdateFlag(true);
			}
            for(ModTreeNode branch : children) {
                branch.insertString(offset, string, as);
            }
        }
    }
    
    /**
     * Inserts string at leaf containing offset.
     * For non-simple leaves it consolidates the string into a single leaf.
     * @param offset
     * @param string
     * @param as
     */
	protected void insertStringAtLeaf(int offset, String string, AttributeSet as) {
		if (this.plainText) {
			String text = this.getText();
			this.setText(text.substring(0, offset - startOffset) + string
					+ text.substring(offset - startOffset, text.length()));
			this.endOffset += string.length();
//			this.setUpdateFlag(true);
		} else {
			ModTreeNode lineParent = getLineParent();
			String lineText = lineParent.getFullText();
			lineParent.children.clear();
			ModTreeLeaf newChild = new ModTreeLeaf(lineParent, lineText, true);
//			newChild.setUpdateFlag(true);
			lineParent.addNode(newChild);
			lineParent.setPlainText(true);
//			lineParent.setUpdateFlag(true);
			
		}
	}

	public void setText(String text) {
        throw new InternalError("Attempted to set string for Element of type: " + getName()); 
        // placeholder for overriding function
    }

	public String getText() {
		throw new InternalError("Attempted to get string for Element of type: " + getName());
		// return "";
	}
    
    /**
	 * Returns the text data of the node or token as a string.<br>
	 * For nodes returns the concatenation of all child nodes/tokens.<br>
	 * For tokens returns the current token string data.
	 * @return the contents of this node in text form
	 */
	public String getFullText() {
		String newString = "";
		if (this.children.isEmpty()) {
			return this.getText();
		}
		for (ModTreeNode child : children) {
			newString += child.getFullText();
		}
		return newString;
	}

	protected ModTreeNode getLineParent()
    {
		if(getParentNode() == null) { return null;}
        if(getName().equals("ModTreeNode") || getParentNode().getParentNode() == null) {
            return this;
        } else {
            return getParentNode().getLineParent();
        }
    }
    
    @Override
	public String toString(){
		// display memory size of line/component in tree view
		if(this.getMemorySize() == 0) {
			return "           " +getFullText(); 
		} else {
			return String.format("%04X/", this.getMemoryPosition()) + String.format("%04X: ", this.getFilePosition()) + getFullText();
		}
//		if (getParentNode() == null) {
//			return "ROOT";
//		} else if (getParentNode().getParentNode() == null) {
////			String newString = ""; // "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
//			String newString = "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
//			return newString + getFullText();
//		} else if(isLeaf()) {
////			String newString = ""; // "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
//			String newString = "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
//			return newString + getFullText() + " (" + getName() + ")";
//		} else {
//			return "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: " + " (" + getName() + ")";
////			return "(" + getName() + ")";
//		}
	}
	
	/**
	 * Overrides string naming for display via JTreePane
	 * @param expanded
	 * @return
	 */
	public String toString(boolean expanded) {
		return this.toString();
	}
	
    /**
     * Returns the node of type line that contains offset (measured in characters from start of file).
     * IMPLEMENTED
     * @param offset
     * @return
     */
	@Deprecated
    protected ModTreeNode getLine(int offset)
    {
        if(getName().equals("ModLineElement")) {
            return this;
        } else {
            return getNode(getNodeIndex(offset)).getLine(offset);
        }
    }

    
    /**
     * Retrieves length characters starting at offset.
     * @param offset - measured in characters from the beginning of the model
     * @param length - measured in characters
     * @return
     */
    public String getText(int offset, int length)
    {
        boolean retrieveBranches = false;
        String returnString = "";
        int rs = offset;
        int re = offset + length;
        int s = startOffset;
        int e = endOffset;
        // adjust start and end offsets for node / leaf
        if(re < e) { // retrieval end occurs prior to node end -- cases 1, 2, 3
            if(re <= s) { // retrieval entirely before current node -- case 1
                // retrieve no text
            } else if (rs < s ) { // retrieval start occurs prior to node start -- case 2
                if(isLeaf()) { // retrieve early part of data string for leaf
                    returnString = getText().substring(0, re-s);
                } else {
                    retrieveBranches = true;
                }
            } else { // retrieval start happens within node -- case 3
                if(isLeaf()) { // retrieve middle part of data string for leaf
                    returnString = getText().substring(rs-s, rs-s+length);
                } else {
                    retrieveBranches = true;
                }
            }
        } else { // retrieval end happens after node end -- cases 4, 5, 6
            if(rs <= s) { // retrieve start occurs prior to node start -- case 6
                if(isLeaf()) { // retrieve entire string
                    returnString = getText();
                } else {
                    retrieveBranches = true;
                }
            } else if(rs < e) { // retrieve start happens in middle of node -- case 4
                if(isLeaf()) { // retrieve end part of data string for leaf
                    return getText().substring(rs-s, getText().length());
                } else {
                    retrieveBranches = true;
                }
            } else { // removal is after node -- case 5
                // retrieve no text
            }
        }
        if(retrieveBranches) {
            for(ModTreeNode branch : children) {
                returnString += branch.getText(offset, length);
            }
        }
        return returnString;
    }

    /**
     * Returns this node's parent node.
     * @return the parent or <code>null</code> if this node is the root node
     */
	public final ModTreeNode getParentNode() {
		return this.parent;
	}
	
	/**
	 * Returns whether this node is a leaf node (e.g. a "token").
	 * @return <code>true</code> if this node is a leaf node, <code>false</code> otherwise
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

	/**
	 * Returns string name of node.
	 * @return
	 */
	public String getName() {
		return "ModTreeNode";
	}

    // TODO -- FIGURE OUT HOW TO SET ATTRIBUTES
	// TODO: @Amineri, see StyleConstants class, contains lots of convenience setters for various attributes
	public AttributeSet getAttributes() {
		return attributes;
	}

    /**
     * Returns the start offset (in characters) of current node, measured from the start of the document.
     * @return the start offset
     */
	public int getStartOffset() {
		return this.startOffset;
	}

    /**
     * Returns the end offset (in characters) of this node, measured from the start of the document.
     * @return the end offset
     */
	public int getEndOffset() {
		return this.endOffset;
	}

    /**
     * Returns the child nodes closest to offset (measured in characters from start of file).
     * IMPLEMENTED
     * @param offset
     * @return
     */
    public int getNodeIndex(int offset)
    {
        if(children.get(0).getStartOffset() > offset) {
            return 0;
        } else if(children.get(children.size()-1).getEndOffset() < offset) {
            return children.size()-1;
        } else {
            int index = 0;
            while(children.get(index).getEndOffset() <= offset) {
                index++;
            }
            return index;
        }
    }
    
    /**
     * Returns number of child nodes of the current node.
     * IMPLEMENTED
     * @return
     */
    public int getNodeCount()
    {
        if(isLeaf()) {
            return 0;
        } else {
            return children.size();
        }
    }
	
	/**
	 * Returns the root node of the tree
	 * @return
	 */
	protected ModTreeNode getRoot()
	{
		return this.getParentNode().getRoot();
	}

    /**
     * Returns the n-th child of the current node.
     * IMPLEMENTED
     * @param n
     * @return
     */
    public ModTreeNode getNode(int n)
    {
        if(n < children.size()) {
            return children.get(n);
        } else {
            return null;
        }
    }

	/**
	 * Computes unreal engine memory size of hex bytecodes represented as text
	 * @return
	 */
	public int getMemorySize() {
		int num = 0;
		for (ModTreeNode branch : children) {
			num += branch.getMemorySize();
		}
		if (num < 0)
			return 0;
		return num;
	}

	/**
	 * Computes unreal engine file size of hex bytecodes represented as text
	 * @return
	 */
	public int getFileSize() {
		int num = 0;
		for (ModTreeNode branch : children) {
			num += branch.getFileSize();
		}
		if (num < 0)
			return 0;
		return num;
	}

    public boolean isVirtualFunctionRef()
    {
        return false;
    }
    
    public int getOffset()
    {
        return -1;
    }

    public int getRefValue()
    {
        return -1;
    }
	
	public int getMemoryPosition() {
		return this.memoryPosition;
	}
	
	public void setMemoryPosition(int position) {
		this.memoryPosition = position;
	}

	public int getFilePosition() {
		return this.filePosition;
	}
	
	public void setFilePosition(int position) {
		this.filePosition = position;
	}

	public ModTreeNode positionToNode(int pos){
		return getChildNodeAt(getNodeIndex(pos));
	}
	
	public void replace(int offset, int length, ModTreeNode[] elems){
		remove(offset, length);
		int index = getParentNode().getNodeIndex(offset);
		for(ModTreeNode e : elems)
		{
			getParentNode().addNode(index, e);
		}
	}
	

	/* 
	 * Implementation of basic TreeNode compatibility methods
	 */
	
	@Override
	public TreeNode getChildAt(int i) {
		return getChildNodeAt(i);
	}

	@Override
	public int getChildCount() {
		return getChildNodeCount();
	}

	@Override
	public TreeNode getParent() {
		return getParentNode();
	}

	@Override
	public int getIndex(TreeNode tn) {
		int count = 0;
		for(ModTreeNode e : this.children) {
			if(e.equals(tn)) {
				return count;
			}
			count ++;
		} 
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {
		return !isLeaf();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<ModTreeNode> children() {
		return Collections.enumeration(this.children);
	}

	/* 
	 * End Implementation of basic TreeNode compatibility methods
	 */
}
