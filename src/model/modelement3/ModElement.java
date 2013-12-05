package model.modelement3;

import java.util.ArrayList;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import model.moddocument3.ModDocument;
import parser.unrealhex.OperandTable;

/**
 *
 * @author Amineri
 */


public class ModElement implements Element
{
    protected ArrayList<ModElement> branches;
    protected int numBranches;
    protected int capacity = 5;
    protected int deltaCapacity = 5;

    protected static OperandTable opTable;
    
    protected ModElement parent;
    protected static ModDocument document;
    
    protected String name;

    protected String datad;
    
    protected int startOffset;
    protected int endOffset;
    
    //static context identifiers used when reorganizing
    protected static boolean inCodeContext, inHeaderContext, inBeforeBlockContext, inAfterBlockContext, inFileHeaderContext;

    // local tags to identify types of elements
    boolean isCode, isValidCode, isSimpleString, isHeader, isInBeforeBlock, isInAfterBlock, isInFileHeader;
    
    protected AttributeSet attributes = null;

    protected static int fileVersion;
    protected static String upkName;
    protected static String guid;
    protected static String functionName;
    
    public ModElement()
    {
        this.init();
    }
    
    public ModElement(ModElement o)
    {
        this.parent = o;
        this.init();
    }
    
    public ModElement(ModElement o, boolean isSimple)
    {
        this.parent = o;
        this.init();
        this.isSimpleString = isSimple;
    }
    
    private void init()
    {
        this.branches = new ArrayList<>(capacity);
        inCodeContext = false;
        inHeaderContext = false;
        inBeforeBlockContext = false;
        inAfterBlockContext = false;
        
        this.isCode = false;
        this.isValidCode = false;
        this.isSimpleString = false;
        this.isHeader = false;
        this.isInBeforeBlock = false;
        this.isInAfterBlock = false;
    }
    
    protected void resetContexts()
    {
        inCodeContext = false;
        inHeaderContext = false;
        inBeforeBlockContext = false;
        inAfterBlockContext = false;
        inFileHeaderContext = true;
        fileVersion = -1;
        upkName = "";
        guid = "";
        functionName = "";
    }
    
    public void setDocument(ModDocument p)
    {
        document = p;
    }

    @Override
    public Document getDocument()
    {
        return document;
    }
    
    public void setOpTable(OperandTable table)
    {
        opTable = table;
    }
            
    public OperandTable getOpTable()
    {
        return opTable;
    }
    
    protected boolean isCode()
    {
        if(getParentElement() == null){
            return false;
        } else if  (isCode) {
            return true;
        } else {
            return getParentElement().isCode();
        }
    }
    
    
    /**
     * Invokes method to break code lines into tokens based on operand.
     * TODO -- test whether preserves character count
     */
    protected void parseUnrealHex() 
    {
        ArrayList<ModElement> oldElements = this.branches;
        String[] linebreak;
        try
            {
                linebreak = toHexStringArray();
                String s = linebreak[1];
                branches.clear();
                this.addElement(new ModToken(this, linebreak[0], true));
                while(!s.isEmpty())
                {
                    ModOperandElement newop = new ModOperandElement(this);
                    addElement(newop);
                    s = newop.parseUnrealHex(s);
                }
                this.addElement(new ModToken(this, linebreak[2], true));
                isValidCode = true;
            }
            catch(Throwable x)
            {
                isValidCode = false;
                branches = oldElements;
                System.out.println("Token parsing failed : " + x.getStackTrace());
            }
    }
    
    protected String[] toHexStringArray()
    {
        String in = toString();
        String outString[] = new String[3];
        
        String[] tokens = in.split("//")[0].split("\\s");
        for(String token : tokens)
        {
            if(token.toUpperCase().matches("[0-9A-Fa-f][0-9A-Fa-f]"))
            {
                outString[1] += token + " ";
            }
        }
        
        int first = in.indexOf(tokens[0]);
        outString[0] = in.substring(0, first);

        int last = in.lastIndexOf(tokens[tokens.length]);
        if (last + 3 < in.length()) {
            outString[2] = in.substring(last+3, in.length());
        }
        return outString;
    }
    
    protected void addElement(ModElement e)
    {
        if(capacity < branches.size()+1)
        {
            capacity += 5;
            branches.ensureCapacity(capacity);
        }
        branches.add(e);
    }

    protected void addElement( int index, ModElement e)
    {
        if(capacity < branches.size()+1)
        {
            capacity += 5;
            branches.ensureCapacity(capacity);
        }
        branches.add(index, e);
    }
    
    protected int getMemorySize()
    {
        int num = 0;
        for(ModElement branch : branches)
        {
            num += branch.getMemorySize();
        }
        return num;
    }

    protected void updateContexts()
    {
        if(this.isSimpleString){
            if (toString().startsWith("UPKFILE=")) {
                upkName = getTag();
            } else if (toString().startsWith("FUNCTION=")) {
                functionName = getTag();
            } else if (toString().startsWith("GUID=")) {
                guid = getTag();
            } else  if (toString().startsWith("MODFILEVERSION")) {
                fileVersion = Integer.parseInt(getTag());
            }
        }
        if(foundHeader()) {
            inFileHeaderContext = false;
        }
        if(!inFileHeaderContext) {
            if(toString().startsWith("[BEFORE_HEX]")) {
                inBeforeBlockContext = true ;                
            } else if(toString().startsWith("[/BEFORE_HEX]")) {
                inBeforeBlockContext = false ;                
            } else if(toString().startsWith("[AFTER_HEX]")) {
                inAfterBlockContext = true;
            } else if(toString().startsWith("[/AFTER_HEX]")) {
                inAfterBlockContext = false;
            } else if(toString().startsWith("[CODE]")) {
                inCodeContext = true;
            } else if(toString().startsWith("[/CODE]")) {
                inCodeContext = false;
            } else if(toString().startsWith("[HEADER]")) {
                inHeaderContext = true;
            } else if(toString().startsWith("[/HEADER]")) {
                inHeaderContext = false;
            }
            this.isCode = inCodeContext;
            this.isInBeforeBlock = inBeforeBlockContext;
            this.isInAfterBlock = inAfterBlockContext;
            this.isHeader = inHeaderContext;
            this.isInFileHeader = false;
        } else {
            this.isCode = false;
            this.isInBeforeBlock = false;
            this.isInAfterBlock = false;
            this.isHeader = false;
            this.isInFileHeader = true;
        }
    }
    
    protected static boolean foundHeader()
    {
        return !(upkName.isEmpty() || functionName.isEmpty() || guid.isEmpty() || (fileVersion > 0));
    }
    
    protected String getTag()
    {
        return toString().split("//",2)[0].split("=",2)[1].trim();
    }
        
    /**
     * Removes length characters at offset (measured from beginning of model)
     * Updates startOffset and endOffset class values and deletes data
     * @param offset
     * @param length
     */
    public void remove(int offset, int length)
    {
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
                startOffset -= length;
                endOffset -= length;
            }
        } else { // removal end happens after element end -- cases 4, 5, 6
            if(rs < s) { // remove start occurs prior to element start -- case 6
                if(isLeaf()) { // delete data string
                    setString("");
                }
                endOffset = startOffset;
                removeModElement();
            } else if(rs < e) { // remove start happens in middle of element -- case 4
                if(isLeaf()) { // remove end part of data string for leaf
                    setString(getString().substring(0, e-rs));
                }
                endOffset -= e - rs;
            } else { // removal is after element -- case 5
                // no update needed
            }
        }
        for(ModElement branch : branches) {
            branch.remove(offset, length);
        }
    }
    
    /**
     * Removes a ModElement, cleaning up references in the parent's branches list
     */
    protected void removeModElement()
    {
        int count = 0;
        for(ModElement branch : getParentElement().branches) { // scan through parent's branches
            if(branch.equals(this)) {  // found current element
                getParentElement().branches.remove(count); // unlink element
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
            for(ModElement branch : branches) {
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
            ModElement simpleParent = getSimpleParent();
            String newSimpleString = simpleParent.toString();
            simpleParent.branches.clear();
            simpleParent.addElement(new ModToken(simpleParent, newSimpleString, true));
            
        }
        setString(getString().substring(0, offset - startOffset) + string + getString().substring(offset - startOffset, getString().length()));
        endOffset += string.length();
    }
    
    protected void setString(String s)
    {
        throw new InternalError("Attempted to set string for Node of type " + getName()); 
        // placeholder for overriding function
    }
    
    protected String getString()
    {
        throw new InternalError("Attempted to get string for Node of type " + getName()); 
//        return "";
    }
    
    protected ModElement getSimpleParent()
    {
        if(isSimpleString){
            return this;
        } else {
            return getParentElement().getSimpleParent();
        }
    }
    
    /**
     * Returns the text data of the element or token as a string.
     * For elements returns the concatenation of all child elements/tokens.
     * For tokens returns the current token string data.
     * @return
     */
    @Override
    public String toString()
    {
        if(branches == null)
            return getString();
        String newString = "";
        for(ModElement branch : branches) {
            newString += branch.toString();
        }
        return newString;
    }
    
    /**
     * Returns the element of type line that contains offset (measured in characters from start of file).
     * IMPLEMENTED
     * @param offset
     * @return
     */
    protected ModElement getLine(int offset)
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
            if(re < s) { // retrieval entirely before current element -- case 1
                // retrieve no text
            } else if (rs < s ) { // retrieval start occurs prior to element start -- case 2
                if(isLeaf()) { // retrieve early part of data string for leaf
                    returnString = getString().substring(0, re-s-1);
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
            if(rs < s) { // retrieve start occurs prior to element start -- case 6
                if(isLeaf()) { // retrieve entire string
                    returnString = getString();
                } else {
                    retrieveBranches = true;
                }
            } else if(rs < e) { // retrieve start happens in middle of element -- case 4
                if(isLeaf()) { // retrieve end part of data string for leaf
                    return getString().substring(e-rs, getString().length()-1);
                } else {
                    retrieveBranches = true;
                }
            } else { // removal is after element -- case 5
                // retrieve no text
            }
        }
        if(retrieveBranches) {
            for(ModElement branch : branches) {
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
     * Returns element's parent element, or null for root node.
     * IMPLEMENTED
     * @return
     */
    @Override
    public final ModElement getParentElement()
    {
        return this.parent;
    }

    /**
     * Returns string name of element.
     * IMPLEMENTED
     * @return
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns attributes of element
     * TODO -- FIGURE OUT HOW TO SET ATTRIBUTES
     * @return
     */
    @Override
    public AttributeSet getAttributes()
    {
        return attributes;
    }

    /**
     * Returns start offset (in characters) of current element, measured from start of file.
     * IMPLEMENTED
     * @return
     */
    @Override
    public int getStartOffset()
    {
        return startOffset;
    }

    /**
     * Returns end offset (in characters) of current element, measured from start of file.
     * IMPLEMENTED
     * @return
     */
    @Override
    public int getEndOffset()
    {
        return endOffset;
    }

    /**
     * Returns the child elements closest to offset (measured in characters from start of file).
     * IMPLEMENTED
     * @param offset
     * @return
     */
    @Override
    public int getElementIndex(int offset)
    {
        if(branches.get(0).getStartOffset() > offset) {
            return 0;
        } else if(branches.get(branches.size()-1).getEndOffset() < offset) {
            return branches.size()-1;
        } else {
            int index = 0;
            while(branches.get(index).getEndOffset() < offset) {
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
    @Override
    public int getElementCount()
    {
        if(isLeaf()) {
            return 0;
        } else {
            return branches.size();
        }
    }

    /**
     * Returns the n-th child of the current element.
     * IMPLEMENTED
     * @param n
     * @return
     */
    @Override
    public ModElement getElement(int n)
    {
        if(n < branches.size()) {
            return branches.get(n);
        } else {
            return null;
        }
    }

    /**
     * Returns true if element is leaf node (eg "token" ).
     * IMPLEMENTED
     * @return
     */
    @Override
    public boolean isLeaf()
    {
        return false;
    }

    
}
