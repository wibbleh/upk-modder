package model.moddocument3;

import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import model.modelement3.ModElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import parser.unrealhex.OperandTable;

/**
 *
 * @author Administrator
 */


public class ModDocumentTest
{
    
    public ModDocumentTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of createDefaultRoot method, of class ModDocument.
     */
    @Test
    public void testCreateDefaultRoot()
    {
        System.out.println("createDefaultRoot");
        ModDocument instance = new ModDocument();
        instance.createDefaultRoot();
    }

    /**
     * Test of reorganize method, of class ModDocument.
     */
    @Test
    public void testReorganize()
    {
        System.out.println("reorganize");
        ModDocument instance = new ModDocument();
        instance.reorganize();
        instance.createDefaultRoot();
        instance.reorganize();
    }

    /**
     * Test of addDocumentListener method, of class ModDocument.
     */
    @Ignore("Not yet implemented") @Test
    public void testAddDocumentListener()
    {
        System.out.println("addDocumentListener");
        DocumentListener dl = null;
        ModDocument instance = new ModDocument();
        instance.addDocumentListener(dl);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeDocumentListener method, of class ModDocument.
     */
    @Ignore("Not yet implemented") @Test
    public void testRemoveDocumentListener()
    {
        System.out.println("removeDocumentListener");
        DocumentListener dl = null;
        ModDocument instance = new ModDocument();
        instance.removeDocumentListener(dl);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addUndoableEditListener method, of class ModDocument.
     */
    @Ignore("Not yet implemented") @Test
    public void testAddUndoableEditListener()
    {
        System.out.println("addUndoableEditListener");
        UndoableEditListener ul = null;
        ModDocument instance = new ModDocument();
        instance.addUndoableEditListener(ul);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeUndoableEditListener method, of class ModDocument.
     */
    @Ignore("Not yet implemented") @Test
    public void testRemoveUndoableEditListener()
    {
        System.out.println("removeUndoableEditListener");
        UndoableEditListener ul = null;
        ModDocument instance = new ModDocument();
        instance.removeUndoableEditListener(ul);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of render method, of class ModDocument.
     */
    @Ignore("Not yet implemented") @Test
    public void testRender()
    {
        System.out.println("render");
        Runnable r = null;
        ModDocument instance = new ModDocument();
        instance.render(r);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLength method, of class ModDocument.
     */
    @Test
    public void testGetLength()
    {
        System.out.println("getLength");
        ModDocument instance = new ModDocument();
        int expResult = 0;
        int result = instance.getLength();
        assertEquals(expResult, result);
        instance.createDefaultRoot();
        result = instance.getLength();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProperty method, of class ModDocument.
     */
    @Test
    public void testGetProperty()
    {
        System.out.println("getProperty");
        Object o = null;
        ModDocument instance = new ModDocument();
        Object expResult = null;
        Object result = instance.getProperty(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of putProperty method, of class ModDocument.
     */
    @Test
    public void testPutProperty()
    {
        System.out.println("putProperty");
        Object o = null;
        Object o1 = null;
        ModDocument instance = new ModDocument();
        instance.putProperty(o, o1);
    }

    /**
     * Test of remove method, of class ModDocument.
     */
    @Test
    public void testRemove()
    {
        System.out.println("remove");
        int offset = 0;
        int length = 0;
        ModDocument instance = new ModDocument();
        instance.createDefaultRoot();
        instance.remove(offset, length);
    }

    /**
     * Test of insertString method, of class ModDocument.
     */
    @Test
    public void testInsertString()
    {
        System.out.println("insertString");
        int offset = 0;
        String string = "";
        AttributeSet as = null;
        ModDocument instance = new ModDocument();
        instance.insertString(offset, string, as);
        instance.createDefaultRoot();
        instance.insertString(offset, string, as);
        instance.insertString(offset, "test", as);
        instance.insertString(offset, "line1 \n line2", as);
    }

    @Test
    public void testInsertGetDelete()
    {
        System.out.println("Insert, Get, Delete");
        int offset = 0;
        String string = "test1string";
        String expected = "st1";
        AttributeSet as = null;
        ModDocument instance = new ModDocument();
        instance.createDefaultRoot();
        instance.insertString(offset, string, as);
        String result = instance.getText(2, 3);
        assertEquals(expected, result);
        String string2 = "foo\nbarsnafu";
        instance.insertString(7, string2, as);
        expected = "test1stfoo\nbarsnafuring";
        expected = "rsnaf";
        result = instance.getText(13, 5);
        assertEquals(expected, result);
    }   
    
    /**
     * Test of getOpTable method, of class ModDocument.
     */
    @Test
    public void testGetOpTable()
    {
        System.out.println("getOpTable");
        ModDocument instance = new ModDocument();
        OperandTable expResult = null;
        OperandTable result = instance.getOpTable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getText method, of class ModDocument.
     */
    @Test
    public void testGetText_int_int()
    {
        System.out.println("getText");
        int offset = 0;
        int length = 0;
        ModDocument instance = new ModDocument();
        String expResult = "";
        String result = instance.getText(offset, length);
        assertEquals(expResult, result);
        instance.createDefaultRoot();
        result = instance.getText(offset, length);
        assertEquals(expResult, result);
    }

    /**
     * Test of getText method, of class ModDocument.
     */
    @Test
    public void testGetText_3args()
    {
        System.out.println("getText");
        int offset = 0;
        int length = 0;
        Segment segment = new Segment();
        ModDocument instance = new ModDocument();
        instance.getText(offset, length, segment);
        instance.createDefaultRoot();
        instance.getText(offset, length, segment);
        assertEquals(segment.count, 0);
        assertEquals(segment.offset, 0);
}

    /**
     * Test of getStartPosition method, of class ModDocument.
     */
    @Test
    public void testGetStartPosition()
    {
        System.out.println("getStartPosition");
        ModDocument instance = new ModDocument();
        Position expResult = new ModPosition(0);
        Position result = instance.getStartPosition();
        assertEquals(expResult.getOffset(), result.getOffset());
    }

    /**
     * Test of getEndPosition method, of class ModDocument.
     */
    @Test
    public void testGetEndPosition()
    {
        System.out.println("getEndPosition");
        ModDocument instance = new ModDocument();
        Position expResult = new ModPosition(0);
        Position result = instance.getEndPosition();
        assertEquals(expResult.getOffset(), result.getOffset());
    }

    /**
     * Test of createPosition method, of class ModDocument.
     * @throws java.lang.Exception
     */
    @Test
    public void testCreatePosition() throws Exception
    {
        System.out.println("createPosition");
        int i = 0;
        ModDocument instance = new ModDocument();
        Position expResult = new ModPosition(0);
        Position result = instance.createPosition(i);
        assertEquals(expResult.getOffset(), result.getOffset());
        instance.createDefaultRoot();
        result = instance.createPosition(i);
        assertEquals(expResult.getOffset(), result.getOffset());
    }

    /**
     * Test of getRootElements method, of class ModDocument.
     */
    @Test
    public void testGetRootElements()
    {
        System.out.println("getRootElements");
        ModDocument instance = new ModDocument();
        ModElement[] expResult = new ModElement[1];
        ModElement[] result = instance.getRootElements();
        assertArrayEquals(expResult, result);
        instance.createDefaultRoot();
        result = instance.getRootElements();
        assertNotNull(result);
    }

    /**
     * Test of getDefaultRootElement method, of class ModDocument.
     */
    @Test
    public void testGetDefaultRootElement()
    {
        System.out.println("getDefaultRootElement");
        ModDocument instance = new ModDocument();
        ModElement expResult = null;
        ModElement result = instance.getDefaultRootElement();
        assertEquals(expResult, result);
        instance.createDefaultRoot();
        result = instance.getDefaultRootElement();
        assertNotNull(result);
    }
    
}
