package model.modelement3;

import java.util.ArrayList;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import model.moddocument3.ModDocument;
import parser.unrealhex.OperandTable;
import static model.modelement3.ModContextType.*;

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

    protected int startOffset;
    protected int endOffset;
    
    private boolean[] localContexts;
    
//    //static context identifiers used when reorganizing
//    protected static boolean inCodeContext, inHeaderContext, inBeforeBlockContext, inAfterBlockContext, inFileHeaderContext;

    // local tags to identify types of elements
//    boolean isCode, isValidCode, isSimpleString, isHeader, isInBeforeBlock, isInAfterBlock, isInFileHeader;
    
    boolean isSimpleString;
    
    protected AttributeSet attributes = null;

//    protected static int fileVersion;
//    protected static String upkName;
//    protected static String guid;
//    protected static String functionName;
    
    
    public ModElement()
    {
        
    }
    
    public ModElement(ModDocument d)
    {
        document = d;
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
        localContexts = new boolean[NUMCONTEXTS.getIndex()];
        setLocalContext(CODE, false);
        setLocalContext(HEADER, false);
        setLocalContext(BEFOREHEX, false);
        setLocalContext(AFTERHEX, false);
        setLocalContext(VALIDCODE, false);

        this.isSimpleString = false;
        name = "ModElement";
    }
    
    protected void setLocalContext(ModContextType type, boolean val)
    {
        localContexts[type.getIndex()] = val;
    }
    
    public boolean inLocalContext(ModContextType type)
    {
        return localContexts[type.getIndex()];
    }
    
    protected void resetContexts()
    {
        setContext(CODE, false);
        setContext(HEADER, false);
        setContext(BEFOREHEX, false);
        setContext(AFTERHEX, true);
        setContext(FILEHEADER, true);
        getDocument().setFileVersion(-1);
        getDocument().setUpkName("");
        getDocument().setGuid("");
        getDocument().setFunctionName("");
    }
    
    @Override
    public ModDocument getDocument()
    {
        return document;
    }
    
    protected void setOpTable(OperandTable table)
    {
        opTable = table;
    }
            
    protected OperandTable getOpTable()
    {
        return opTable;
    }
    
    protected boolean isCode()
    {
        if(getParentElement() == null){
            return false;
        } else if  (inLocalContext(CODE)) {
            return true;
        } else {
            return getParentElement().isCode();
        }
    }
    
    
    /**
     * Invokes method to break code lines into tokens based on operand.
     * Operates at the line level.
     */
    protected void parseUnrealHex() 
    {
        if(opTable == null || !isValidHexLine())
        {
            return;
        }
        int currStart = this.startOffset;
        String oldString = toString();
        String[] linebreak;
        try
            {
                linebreak = toHexStringArray();
                String s = linebreak[1];
                branches.clear();
                if(!linebreak[0].isEmpty()) {
                    ModToken newToken = new ModToken(this, linebreak[0], true);
                    newToken.startOffset = currStart;
                    newToken.endOffset = oldString.length();
                    currStart = newToken.endOffset;
                    this.addElement(newToken);
                }
                while(!s.isEmpty())
                {
                    String old_s = s;
                    ModOperandElement newop = new ModOperandElement(this);
                    addElement(newop);
                    newop.startOffset = currStart;
                    s = newop.parseUnrealHex(s);
                    newop.endOffset = newop.startOffset + old_s.length() - s.length();
                }
                ModToken newToken = new ModToken(this, linebreak[2], true);
                newToken.startOffset = currStart;
                newToken.endOffset = oldString.length();
//                currStart = newToken.endOffset;
                this.addElement(newToken);
                setLocalContext(VALIDCODE, true);
                isSimpleString = false;
            }
            catch(Throwable x)
            {
                setLocalContext(VALIDCODE, false);
                branches.clear();
                ModToken newToken = new ModToken(this, oldString, true);
                addElement(newToken);
//                System.out.println("Token parsing failed : " + x.getStackTrace());
            }
    }
    
    protected boolean isValidHexLine()
    {
        String[] tokens = toString().split("//")[0].trim().split("\\s");
        for(String token : tokens) {
            if(!token.matches("[0-9A-Fa-f][0-9A-Fa-f]")) {
                return false;
            }
        }
        return true;
    }
    
    protected String[] toHexStringArray()
    {
        String in = toString();
        String outString[] = new String[3];
        
        outString[0] = "";
        outString[1] = "";
        outString[2] = "";

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

        int last = in.lastIndexOf(tokens[tokens.length-1]);
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
    
    protected void updateContexts()
    {
        if(this.isSimpleString){
            if (toString().startsWith("UPKFILE=")) {
                getDocument().setUpkName(getTag());
            } else if (toString().startsWith("FUNCTION=")) {
                getDocument().setFunctionName(getTag());
            } else if (toString().startsWith("GUID=")) {
                getDocument().setGuid(getTag());
            } else if (toString().startsWith("MODFILEVERSION")) {
                getDocument().setFileVersion(Integer.parseInt(getTag()));
            }
        }
        if(foundHeader()) {
            setContext(FILEHEADER, false);
        }
        if(inContext(FILEHEADER)) {
            setLocalContext(CODE, false);
            setLocalContext(BEFOREHEX, false);
            setLocalContext(AFTERHEX, false);
            setLocalContext(HEADER, false);
            setLocalContext(FILEHEADER, true);
        } else {
            if(toString().startsWith("[BEFORE_HEX]")) {
                setContext(BEFOREHEX, true);
            } else if(toString().startsWith("[/BEFORE_HEX]")) {
                setContext(BEFOREHEX, false);
            } else if(toString().startsWith("[AFTER_HEX]")) {
                setContext(AFTERHEX, true);
            } else if(toString().startsWith("[/AFTER_HEX]")) {
                setContext(AFTERHEX, false);
            } else if(toString().startsWith("[CODE]")) {
                setContext(CODE, true);
            } else if(toString().startsWith("[/CODE]")) {
                setContext(CODE, false);
            } else if(toString().startsWith("[HEADER]")) {
                setContext(HEADER, true);
            } else if(toString().startsWith("[/HEADER]")) {
                setContext(HEADER, false);
            }
            setLocalContext(CODE, inContext(CODE));
            setLocalContext(BEFOREHEX, inContext(BEFOREHEX));
            setLocalContext(AFTERHEX, inContext(AFTERHEX));
            setLocalContext(HEADER, inContext(HEADER)); 
            setLocalContext(FILEHEADER, false);
        }
    }
    
    protected boolean foundHeader()
    {
        return !getDocument().getUpkName().isEmpty() && !getDocument().getFunctionName().isEmpty() && !getDocument().getGuid().isEmpty() && (getDocument().getFileVersion() > 0);
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
            for(ModElement branch : branches) {
                branch.remove(offset, length);
        }
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
            ModElement lineParent = getLineParent();
            String newSimpleString = lineParent.toString();
            lineParent.branches.clear();
            lineParent.addElement(new ModToken(lineParent, newSimpleString, true));
            
        }
        setString(getString().substring(0, offset - startOffset) + string + getString().substring(offset - startOffset, getString().length()));
        endOffset += string.length();
    }
    
    protected void setString(String s)
    {
        throw new InternalError("Attempted to set string for Element of type: " + getName()); 
        // placeholder for overriding function
    }
    
    protected String getString()
    {
        throw new InternalError("Attempted to get string for Element of type: " + getName()); 
//        return "";
    }
    
    protected ModElement getLineParent()
    {
        if(getName().equals("ModElement")){
            return this;
        } else {
            return getParentElement().getLineParent();
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
     * Returns true if element is leaf node (eg "token" ).
     * IMPLEMENTED
     * @return
     */
    @Override
    public boolean isLeaf()
    {
        return false;
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

    protected boolean inContext(ModContextType type)
    {
        return getDocument().getDefaultRootElement().inContext(type);
    }
    
    protected void setContext(ModContextType type, boolean val)
    {
        getDocument().getDefaultRootElement().setContext(type, val);
    }
    
    public int getMemorySize()
    {
        int num = 0;
        for(ModElement branch : branches)
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
        
}
