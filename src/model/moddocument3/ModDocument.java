package model.moddocument3;

import java.util.ArrayList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import model.modelement3.*;
import parser.unrealhex.OperandTable;

/**
 *
 * @author Amineri
 */


public class ModDocument extends PlainDocument
{
    private ModPosition currPosition;  // maintains current position
    private ModPosition endPosition; // maintains size of file in characters

    private ModRootElement rootElement;
    private final ModRootElement[] rootElements = new ModRootElement[1];
    
//    // context identifiers used when updating after insert or remove operation
//    private boolean inCodeContext, inHeaderContext, inBeforeBlockContext, inAfterBlockContext, inFileHeaderContext;

    private int fileVersion;
    private String upkName;
    private String guid;
    private String functionName;

    ArrayList<Object> docProperties;
    ArrayList<Object> propertyKeys;
    
    protected OperandTable opTable;

    /**
     * Creates a new ModDocument.
     */
    public ModDocument()
    {
        super();
        opTable = null;
        initVars();
    }

    /**
     * Creates a new ModDocument with attached OperandTable table.
     * OperandTable is used to perform unreal bytecode parsing.
     * @param table
     */
    public ModDocument(OperandTable table)
    {
        super();
        opTable = table;
        initVars();
    }
    
    private void initVars()
    {
//        inCodeContext = false;
//        inHeaderContext = false;
//        inBeforeBlockContext = false;
//        inAfterBlockContext = false;
//        inFileHeaderContext = false;

        fileVersion = -1;
        upkName = "";
        guid = "";
        functionName = "";
    }
    
    /**
     * Creates and initializes the root element of the document.
     */
    public void createRoot()
    {
        if(rootElement == null){
            rootElement = new ModRootElement(this, opTable);
//            rootElement.setOpTable(opTable);
            rootElements[0] = rootElement;
        }
    }
    
    /**
     * Refreshes contexts and re-parses tokens after an insert operation.
     * Parameters chng and attr currently unused.
     * @param chng
     * @param attr
     */
    @Override
    public void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr)
    {
        if(rootElement != null)
            rootElement.reorganizeAfterInsertion();
    }
    
    /**
     * Refreshes contexts and re-parses tokens after a remove operation.
     * Parameter chng currently unused.
     * @param chng
     */
    @Override
    public void removeUpdate(AbstractDocument.DefaultDocumentEvent chng)
    {
        if(rootElement != null)
            rootElement.reorganizeAfterDeletion();
    }

    /**
     * Returns length of document.
     * Measured in characters.
     * @return
     */
    @Override
    public int getLength()
    {
        if (rootElement == null)
            return 0;
        return rootElement.getEndOffset();
    }

    /**
     * Removes length characters, starting at position offset.
     * Offset is measured from the beginning of the document.
     * @param offset
     * @param length
     */
    @Override
    public void remove(int offset, int length)
    {
        if(rootElement == null) {
            return;
        }
        rootElement.remove(offset, length);
    }

    /**
     * Inserts string at position offset.
     * AttributeSet as currently is unused.
     * Does not reset contexts or parse unreal bytecode.
     * Require insertUpdate call to reset contexts and parse unreal bytecode.
     * @param offset
     * @param string
     * @param as
     */
    @Override
    public void insertString(int offset, String string, AttributeSet as)
    {
        if (rootElement == null) {
            return;
        }
        rootElement.insertString(offset, string, as);
    }

    /**
     * Retrieves String of length starting at position offset.
     * Returns String.
     * @param offset
     * @param length
     * @return
     */
    @Override
    public String getText(int offset, int length)
    {
        if(rootElement == null)
            return "";
        return rootElement.getText(offset, length);
    }

    /**
     * Retrieves String of length starting at position offset.
     * String returned through parameter segment.
     * @param offset
     * @param length
     * @param segment
     */
    @Override
    public void getText(int offset, int length, Segment segment)
    {
        if(rootElement == null)
            return;
        rootElement.getText(offset, length, segment);
    }

//    @Override
//    public Position getStartPosition()
//    {
//        return new ModPosition(0);
//    }

//    @Override
//    public Position getEndPosition()
//    {
//        if(rootElement == null)
//            return new ModPosition(0);
//        endPosition.setPosition(rootElement.getEndOffset());
//        return endPosition;
//    }

//    @Override
//    public Position createPosition(int i) throws BadLocationException
//    {
//        
//        currPosition.setPosition(i);
//        return currPosition;
//    }

    /**
     * Returns array of root elements.
     * For ModDocument this will always be length 1 array.
     * @return
     */
    
    @Override
    public ModElement[] getRootElements()
    {
        return rootElements;
    }

    /**
     * Returns root element of document.
     * @return
     */
    @Override
    public ModElement getDefaultRootElement()
    {
        return rootElement;
    }
    
//    public boolean inFileHeaderContext()
//    {
//        return this.inFileHeaderContext;
//    }
//    
//    public boolean inCodeContext()
//    {
//        return this.inCodeContext;
//    }
//    
//    public boolean inHeaderContext()
//    {
//        return this.inHeaderContext;
//    }
//    
//    public boolean inBeforeBlockContext()
//    {
//        return this.inBeforeBlockContext;
//    }
//    
//    public boolean inAfterBlockContext()
//    {
//        return this.inAfterBlockContext;
//    }

    public int getFileVersion()
    {
        return fileVersion;
    }
    
    public void setFileVersion(int n)
    {
        fileVersion = n;
    }
    
    public String getUpkName()
    {
        return upkName;
    }
    
    public void setUpkName(String s)
    {
        upkName = s;
    }
    
    public String getGuid()
    {
        return guid;
    }
    
    public void setGuid(String s)
    {
        guid = s;
    }

    public String getFunctionName()
    {
        return functionName;
    }

    public void setFunctionName(String s)
    {
        functionName = s;
    }

}
