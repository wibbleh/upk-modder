package model.modtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.Segment;
import javax.swing.tree.TreeNode;
import model.modtree.ModContext.*;
import static model.modtree.ModContext.ModContextType.*;

/**
 * Basic <code>Element</code> implementation used in structuring <code>ModDocument</code> contents.
 * @author Amineri
 * @see {@link ModDocument}
 */
public class ModTreeNode implements TreeNode {
//public class ModTreeNode extends AbstractElement {
	
	/**
	 * The list of child elements.
	 */
    private List<ModTreeNode> children;
    
    /**
     * The reference to the parent element. Is <code>null</code> if this element is the root element.
     */
    protected ModTreeNode parent;
    
    /**
     * Flag denoting whether this element contains pure textual data.
     */
    protected boolean isSimpleString;
    
    /**
     * The name of this element.
     */
    protected String name;

    /**
     * The start offset of this element in the document.
     */
    private int startOffset;
    
    /**
     * The end offset of this element in the document.
     */
    private int endOffset;
    
    /**
     * The context flag container instance.
     */
    protected ModContext context;
    
    /**
     * The style attributes of this element.
     */
    protected AttributeSet attributes;

    /**
     * Constructs a mod element from the specified parent element.
     * @param parent the parent element
     */
	public ModTreeNode(ModTreeNode parent) {
		this(parent, false);
	}

	/**
	 * Constructs a mod element from the specified parent element and a flag
	 * denoting whether it contains pure textual data (i.e. non-parseable code).
	 * @param parent the parent element 
	 * @param isSimpleString <code>true</code> if this element contains only textual data,
	 *  <code>false</code> otherwise
	 */
	public ModTreeNode(ModTreeNode parent, boolean isSimpleString) {
		this.parent = parent;
		this.isSimpleString = isSimpleString;

		// init properties
		this.children = new ArrayList<>();
		this.context = new ModContext();
		this.name = "ModTreeNode";
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
	 * Invoke ModRootElement's Context Flag reset algorithm.
	 * In practice this function should never be called anywhere but the root element.
	 * @return 
	 */
//	protected void resetContextFlags() {
//		if(getParentElement() == null) {return;}
//		getParentElement().resetContextFlags();
//	}
    
	public ModTree getTree() {
		// fetch document from parent, only the root node carries the actual
		// reference to the document instance
		if(getParentElement() != null) {
			return this.getParentElement().getTree();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the child element at the specified position.
	 * @param index the child element's position
	 * @return the child element or <code>null</code> if the position is out of range
	 */
	public ModTreeNode getChildElementAt(int index) {
		// obligatory range check
		if ((index >= 0) && (index < this.children.size())) {
			return this.children.get(index);
		}
		return null;
	}
	
	/**
	 * Removes the child element at the specified position.
	 * @param index the position of the element to remove
	 * @return the removed element or <code>null</code> if the position is out of range
	 */
	public ModTreeNode removeChildElementAt(int index) {
		// obligatory range check
		if ((index >= 0) && (index < this.children.size())) {
			return this.children.remove(index);
		}
		return null;
	}
	
	/**
	 * Removes all child elements of this element.
	 */
	public void removeAllChildElements() {
		this.children.clear();
	}
	
	/**
	 * Returns the number of child elements of this element.
	 * @return the child element count
	 */
	public int getChildElementCount() {
		return this.children.size();
	}

	/**
	 * Returns whether this element contains parseable UnrealScript byte code.
	 * @return <code>true</code> if this element or its parent contains code, <code>false</code> otherwise
	 */
	protected boolean isCode() {
		if (this.getParentElement() == null) {
			// this element is the root element which never contains code
			return false;
		} else {
			// if this element is not flagged as containing hex code pass check on to parent element
			return this.getContextFlag(HEX_CODE) || getParentElement().isCode();
		}
	}
	
	/**
	 * Sets the offset range of this element in the document.
	 * @param startOffset the start offset of this element in the document
	 * @param endOffset the end offset of this element in the document
	 */
	public void setRange(int startOffset, int endOffset) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	/**
	 * Invokes method to break code lines into tokens based on operand.<br>
	 * Operates at the line level.
	 */
	protected void parseUnrealHex() {
		if (!this.isValidHexLine()) {
			return;
		}
		String oldString = this.toStr();
		try {
			// extract initial string representation of this element
			String[] linebreak = this.toHexStringArray();

			// remove all children
			this.children.clear();
			
			// start parsing the document from the beginning of this element
			int currOffset = this.startOffset;
			
			String prefix = linebreak[0];
			if (!prefix.isEmpty()) {
				// wrap leading text data in plain token
				ModTreeLeaf leadToken = new ModTreeLeaf(this, linebreak[0], true);
				leadToken.setRange(currOffset, currOffset + linebreak[0].length());
				this.addElement(leadToken);
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
				this.addElement(opElem);
				currOffset += lastLength;
			}
			String suffix = linebreak[2];
			// wrap trailing text data in plain token
			ModTreeLeaf trailToken = new ModTreeLeaf(this, suffix, true);
			trailToken.setRange(currOffset, currOffset + suffix.length());
			// currStart = newToken.endOffset;
			this.addElement(trailToken);
			this.setContextFlag(VALID_CODE, true);
			this.isSimpleString = false;
		} catch (Exception e) {
			// something went wrong, set error flag...
			setContextFlag(VALID_CODE, false);
			// ... remove any child elements that may have been inserted... 
			this.children.clear();
			// ... insert original text data as plain mod token
			ModTreeLeaf t = new ModTreeLeaf(this, oldString, true);
			this.addElement(t);
			t.setRange(this.startOffset, this.endOffset);
		}
	}
    
	/**
	 * Returns whether this element wraps hex string data.
	 * @return <code>true</code> if this element contains valid hex data, <code>false</code> otherwise
	 */
	protected boolean isValidHexLine() {
		String[] tokens = toStr().split("//")[0].trim().split("\\s");
		for (String token : tokens) {
			if (!token.matches("[0-9A-Fa-f][0-9A-Fa-f]")) {
				return false;
			}
		}
		return true;
	}
    
    /**
     * Returns an array containing a concatenated hex tokens and leading/trailing line contents.
     * @return a 3-element array containing hex tokens in its middle element
     */
	protected String[] toHexStringArray() {
		// get initial string representation of this element
		String in = this.toStr();

		// pre-allocate result array
		String outString[] = new String[3];
		Arrays.fill(outString, "");

		// tokenize string representation
		String[] tokens = in.split("//")[0].split("\\s");
		for (String token : tokens) {
			if (token.toUpperCase().matches("[0-9A-Fa-f][0-9A-Fa-f]")) {
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
     * Appends the specified element to the list of child elements.
     * @param element the element to append
     */
	protected void addElement(ModTreeNode element) {
		this.addElement(this.children.size(), element);
	}

	/**
	 * Inserts the specified element at the specified position into the list of
	 * child elements.
	 * @param index the position index
	 * @param element the element to insert
	 */
	protected void addElement(int index, ModTreeNode element) {
		this.children.add(index, element);
	}
    
	protected void updateContexts() {
		if(getTree() == null) {return;}
		String content = this.toStr().toUpperCase();
		if (this.isSimpleString) {
//			ModDocument document = getDocument();
//			String tagValue = this.getTagValue(content);
			if (content.startsWith("UPKFILE=")) {
				getTree().setUpkName(this.getTagValue(content));
			} else if (content.startsWith("FUNCTION=")) {
				getTree().setFunctionName(this.getTagValue(content));
			} else if (content.startsWith("GUID=")) {
				getTree().setGuid(this.getTagValue(content));
			} else if (content.startsWith("MODFILEVERSION=")) {
				getTree().setFileVersion(Integer.parseInt(this.getTagValue(content)));
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
	 * Returns whether this element wraps header-type content.
	 * @return <code>true</code> 
	 */
	// TODO: this method would probably better belong in the document itself, i.e. getDocument().hasValidHeader() or somesuch
	// Amineri : Good point. When updating a file after insert/remove, this header has to be reset
	//			 and each line the value updated, in case the insert/remove invalidated the header (i.e edits to header lines)
	protected boolean foundHeader() {
		ModTree document = getTree();
		if(document == null) {return false;}
		return !document.getUpkName().isEmpty()
				&& !document.getFunctionName().isEmpty()
				&& !document.getGuid().isEmpty()
				&& (document.getFileVersion() > 0);
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
        // adjust start and end offsets for element / leaf
        if(re < e) { // removal end occurs prior to element end -- cases 1, 2, 3
            if(re < s) { // removal entirely before current element -- case 1
                startOffset -= length;
                endOffset -= length;
            } else if (rs < s ) { // removal start occurs prior to element start -- case 2
                if(isLeaf()) { // remove early part of data string for leaf
                    setString(getString().substring(re-s, getString().length()));
                }
                startOffset -= s - rs;
                endOffset -= length;
            } else { // removal start happens within element -- case 3
                if(isLeaf()) { // remove middle part of data string for leaf
                    setString(getString().substring(0, rs-s) + getString().substring(e-re, getString().length()));
                }
                endOffset -= length;
            }
        } else { // removal end happens after element end -- cases 4, 5, 6
            if(rs < s) { // remove start occurs prior to element start -- case 6
                if(isLeaf()) { // delete data string
                    setString("");
                }
                endOffset = startOffset;
//                removeModElement();
            } else if(rs < e) { // remove start happens in middle of element -- case 4
                if(isLeaf()) { // remove end part of data string for leaf
                    setString(getString().substring(0, e-rs));
                }
                endOffset -= e - rs;
            } else { // removal is after element -- case 5
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
    protected void removeModElement()
    {
        int count = 0;
        for(ModTreeNode branch : getParentElement().children) { // scan through parent's branches
            if(branch.equals(this)) {  // found current element
                getParentElement().children.remove(count); // unlink element
                return;
            }
            count++;
        }
    }
    
    /**
     * Inserts string at given offset.
     * Implements required Document Interface method.
     * @param offset
     * @param string
     * @param as
     */
    public void insertString(int offset, String string, AttributeSet as)
    {
        int length = string.length();
        int s = startOffset;
        int e = endOffset;
        if(isLeaf()){
            if(offset >= s && offset <= e){ // insertion is at leaf
                insertStringAtLeaf(offset, string, as);
            }
        } else { // recursive step for nodes
            if(offset <= endOffset) {
                endOffset += length;
            }
            if(offset < startOffset) {
                startOffset += length;
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
    protected void insertStringAtLeaf(int offset, String string, AttributeSet as)
    {
        if(isSimpleString) {
        } else {
            ModTreeNode lineParent = getLineParent();
            String newSimpleString = lineParent.toStr();
            lineParent.children.clear();
            lineParent.addElement(new ModTreeLeaf(lineParent, newSimpleString, true));
            
        }
        setString(getString().substring(0, offset - startOffset) + string + getString().substring(offset - startOffset, getString().length()));
        endOffset += string.length();
    }
    
    protected void setString(String s)
    {
        throw new InternalError("Attempted to set string for Element of type: " + getName()); 
        // placeholder for overriding function
    }
    
	protected String getString() {
		throw new InternalError("Attempted to get string for Element of type: " + getName());
		// return "";
	}
    
    protected ModTreeNode getLineParent()
    {
		if(getParentElement() == null) { return null;}
        if(getName().equals("ModTreeNode") || getParentElement().getParentElement() == null) {
            return this;
        } else {
            return getParentElement().getLineParent();
        }
    }
    
    /**
     * Returns the text data of the element or token as a string.<br>
     * For elements returns the concatenation of all child elements/tokens.<br>
     * For tokens returns the current token string data.
     * @return the contents of this element in text form
     */
    public String toStr()
    {
		String newString = "";
		if (this.children.isEmpty()) {
			return this.getString();
		}
		for (ModTreeNode child : children) {
			newString += child.toStr();
		}
		return newString;
    }
    
    @Override
	public String toString(){
		if(getParentElement() == null) {
			return "ROOT";
		} else if (getParentElement().getParentElement() == null) {
//			String newString = ""; // "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
			String newString = "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
			return newString + toStr();
		} else if(isLeaf()) {
//			String newString = ""; // "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
			String newString = "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: ";
			return newString + toStr() + " (" + getName() + ")";
		} else {
			return "[" + Integer.toString(getStartOffset()) + ":" + Integer.toString(getEndOffset()) + "]: " + " (" + getName() + ")";
//			return "(" + getName() + ")";
		}
	}
	
    /**
     * Returns the element of type line that contains offset (measured in characters from start of file).
     * IMPLEMENTED
     * @param offset
     * @return
     */
    protected ModTreeNode getLine(int offset)
    {
        if(getName().equals("ModLineElement")) {
            return this;
        } else {
            return getElement(getElementIndex(offset)).getLine(offset);
        }
    }

    
    /**
     * Retrieves length characters starting at offset.
     * Implements the ModDocument.getText required method
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
        // adjust start and end offsets for element / leaf
        if(re < e) { // retrieval end occurs prior to element end -- cases 1, 2, 3
            if(re <= s) { // retrieval entirely before current element -- case 1
                // retrieve no text
            } else if (rs < s ) { // retrieval start occurs prior to element start -- case 2
                if(isLeaf()) { // retrieve early part of data string for leaf
                    returnString = getString().substring(0, re-s);
                } else {
                    retrieveBranches = true;
                }
            } else { // retrieval start happens within element -- case 3
                if(isLeaf()) { // retrieve middle part of data string for leaf
                    returnString = getString().substring(rs-s, rs-s+length);
                } else {
                    retrieveBranches = true;
                }
            }
        } else { // retrieval end happens after element end -- cases 4, 5, 6
            if(rs <= s) { // retrieve start occurs prior to element start -- case 6
                if(isLeaf()) { // retrieve entire string
                    returnString = getString();
                } else {
                    retrieveBranches = true;
                }
            } else if(rs < e) { // retrieve start happens in middle of element -- case 4
                if(isLeaf()) { // retrieve end part of data string for leaf
                    return getString().substring(rs-s, getString().length());
                } else {
                    retrieveBranches = true;
                }
            } else { // removal is after element -- case 5
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
     * Returns text of given length at offset via the segment.
     * Implements the ModDocument.getText required method
     * TODO -- Figure out if this is used and optimize if necessary
     * @param offset
     * @param length
     * @param segment
     */
    public void getText(int offset, int length, Segment segment)
    {
        String s = getText(offset, length);
        segment.array = s.toCharArray();
        segment.count = s.length();
        segment.offset = 0;
        
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        // TODO -- figure out if this call is needed
        // TODO -- figure out how to return segment as "out" variable
    }

    /**
     * Returns this element's parent element.
     * @return the parent or <code>null</code> if this element is the root element
     */
	public final ModTreeNode getParentElement() {
		return this.parent;
	}
	
	/**
	 * Returns whether this element is a leaf node (e.g. a "token").
	 * @return <code>true</code> if this element is a leaf node, <code>false</code> otherwise
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

    /**
     * Returns string name of element.
     * @return
     */
    public String getName()
    {
        return name;
    }

    // TODO -- FIGURE OUT HOW TO SET ATTRIBUTES
	// TODO: @Amineri, see StyleConstants class, contains lots of convenience setters for various attributes
	public AttributeSet getAttributes() {
		return attributes;
	}

    /**
     * Returns the start offset (in characters) of current element, measured from the start of the document.
     * @return the start offset
     */
	public int getStartOffset() {
		return this.startOffset;
	}

    /**
     * Returns the end offset (in characters) of this element, measured from the start of the document.
     * @return the end offset
     */
	public int getEndOffset() {
		return this.endOffset;
	}

    /**
     * Returns the child elements closest to offset (measured in characters from start of file).
     * IMPLEMENTED
     * @param offset
     * @return
     */
    public int getElementIndex(int offset)
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
     * Returns number of child elements of the current element.
     * IMPLEMENTED
     * @return
     */
    public int getElementCount()
    {
        if(isLeaf()) {
            return 0;
        } else {
            return children.size();
        }
    }
	
	/**
	 * Returns the root element of the tree
	 * @return
	 */
	protected ModTreeNode getRoot()
	{
		return getParentElement().getRoot();
	}

    /**
     * Returns the n-th child of the current element.
     * IMPLEMENTED
     * @param n
     * @return
     */
    public ModTreeNode getElement(int n)
    {
        if(n < children.size()) {
            return children.get(n);
        } else {
            return null;
        }
    }

    public int getMemorySize()
    {
        int num = 0;
        for(ModTreeNode branch : children)
        {
            num += branch.getMemorySize();
        }
        if(num < 0)
            return -1;
        return num;
    }

    public boolean isVFFunctionRef()
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

	@Override
	public TreeNode getChildAt(int i) {
		return getChildElementAt(i);
	}

	@Override
	public int getChildCount() {
		return getChildElementCount();
	}

	@Override
	public TreeNode getParent() {
		return getParentElement();
	}

	@Override
	public int getIndex(TreeNode tn) {
		int count = 0;
		for(ModTreeNode e : children) {
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

	@Override
	public Enumeration children() {
		return (Enumeration) children;
	}

	public ModTreeNode positionToElement(int pos){
		return getChildElementAt(getElementIndex(pos));
	}
	
	public void replace(int offset, int length, ModTreeNode[] elems){
		remove(offset, length);
		int index = getParentElement().getElementIndex(offset);
		for(ModTreeNode e : elems)
		{
			getParentElement().addElement(index, e);
		}
	}
	
        
}
