package model.moddocument3;

import java.util.ArrayList;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import model.modelement3.*;
import parser.unrealhex.OperandTable;

/**
 *
 * @author Amineri
 */


public class ModDocument implements Document
{
    private ModPosition currPosition;  // maintains current position
    private ModPosition endPosition; // maintains size of file in characters

    private ModRootElement rootElement;
    private ModRootElement[] rootElements = new ModRootElement[1];
    
    ArrayList<Object> docProperties;
    ArrayList<Object> propertyKeys;
    
    protected OperandTable opTable;

    public ModDocument()
    {
        opTable = null;
        currPosition = new ModPosition(0);
        endPosition = new ModPosition(0);
        
    }

    public ModDocument(OperandTable table)
    {
        opTable = table;
        currPosition = new ModPosition(0);
        endPosition = new ModPosition(0);
    }
    
    public void createDefaultRoot()
    {
        if(rootElement == null){
            rootElement = new ModRootElement();
            rootElement.setDocument(this);
            rootElement.setOpTable(opTable);
            rootElements[0] = rootElement;
        }
    }
    
    /**
     * Updates the document as the result of a text insertion or deletion.
     * Called internally automatically on any insertion/deletion.
     */
    public void reorganize()
    {
        if(rootElement != null)
            rootElement.reorganize();
    }
    
    @Override
    public void addDocumentListener(DocumentListener dl)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeDocumentListener(DocumentListener dl)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addUndoableEditListener(UndoableEditListener ul)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeUndoableEditListener(UndoableEditListener ul)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render(Runnable r)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLength()
    {
        if (rootElement == null)
            return 0;
        return rootElement.getEndOffset();
    }

    @Override
    public Object getProperty(Object o)
    {
        if(docProperties == null)
            return null;
        return docProperties.get(propertyKeys.indexOf(o));
    }

    @Override
    public void putProperty(Object o, Object o1)
    {
        if(docProperties == null || propertyKeys == null) {
            docProperties = new ArrayList<>(5);
            propertyKeys = new ArrayList<>(5);
        }
        docProperties.ensureCapacity(docProperties.size()+5);
        propertyKeys.ensureCapacity(docProperties.size()+5);
        docProperties.add(o1);
        propertyKeys.add(o);
    }

    @Override
    public void remove(int offset, int length)
    {
        if(rootElement == null) {
            return;
        }
        rootElement.remove(offset, length);
        reorganize();
    }

    @Override
    public void insertString(int offset, String string, AttributeSet as)
    {
        if (rootElement == null) {
            return;
        }
        rootElement.insertString(offset, string, as);
        reorganize();
    }

    public OperandTable getOpTable()
    {
        if(opTable == null)
            return null;
        return opTable;
    }

    @Override
    public String getText(int offset, int length)
    {
        if(rootElement == null)
            return "";
        return rootElement.getText(offset, length);
    }

    @Override
    public void getText(int offset, int length, Segment segment)
    {
        if(rootElement == null)
            return;
        rootElement.getText(offset, length, segment);
    }

    @Override
    public Position getStartPosition()
    {
        return new ModPosition(0);
    }

    @Override
    public Position getEndPosition()
    {
        if(rootElement == null)
            return new ModPosition(0);
        endPosition.setPosition(rootElement.getEndOffset());
        return endPosition;
    }

    @Override
    public Position createPosition(int i) throws BadLocationException
    {
        
        currPosition.setPosition(i);
        return currPosition;
    }

    @Override
    public ModElement[] getRootElements()
    {
        return rootElements;
    }

    @Override
    public ModElement getDefaultRootElement()
    {
        return rootElement;
    }

}
