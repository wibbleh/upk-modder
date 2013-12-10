package model.modtree;

import javax.swing.text.AttributeSet;
import javax.swing.text.Segment;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import static org.junit.Assert.*;
//import parser.unrealhex.OperandTable;
import io.parser.OperandTableParser;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.text.BadLocationException;
import model.modtree.*;
import model.modtree.ModContext.*;
import static model.modtree.ModContext.ModContextType.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
//import org.junit.Ignore;

/**
 *
 * @author Amineri
 */


public class ModTreeTest
{
    
    public ModTreeTest()
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
     * Test of getDefaultRootElement method, of class ModDocument.
     */
    @Test
    public void testGetDefaultRootElement()
    {
        System.out.println("getDefaultRootElement");
        ModTree instance = new ModTree();
        ModTreeNode result = instance.getDefaultRootElement();
        assertNotNull(result);
    }

	/**
     * Test of setLocalContext method, of class ModTreeNode.
     */
    @Test
    public void testSetAndInLocalContext()
    {
        System.out.println("setLocalContext");
        ModTreeNode instance = new ModTreeNode(null);
        instance.setContextFlag(HEX_CODE, true);
        assertTrue(instance.getContextFlag(HEX_CODE));
        instance.setContextFlag(HEX_CODE, false);
        assertFalse(instance.getContextFlag(HEX_CODE));
        
        instance.setContextFlag(VALID_CODE, true);
        assertTrue(instance.getContextFlag(VALID_CODE));
        instance.setContextFlag(VALID_CODE, false);
        assertFalse(instance.getContextFlag(VALID_CODE));
        
        instance.setContextFlag(BEFORE_HEX, true);
        assertTrue(instance.getContextFlag(BEFORE_HEX));
        instance.setContextFlag(BEFORE_HEX, false);
         assertFalse(instance.getContextFlag(BEFORE_HEX));

        instance.setContextFlag(AFTER_HEX, true);
        assertTrue(instance.getContextFlag(AFTER_HEX));
        instance.setContextFlag(AFTER_HEX, false);
        assertFalse(instance.getContextFlag(AFTER_HEX));

        instance.setContextFlag(FILE_HEADER, true);
        assertTrue(instance.getContextFlag(FILE_HEADER));
        instance.setContextFlag(FILE_HEADER, false);
        assertFalse(instance.getContextFlag(FILE_HEADER));
    }

    /**
     * Test of resetContextFlags method, of class ModTreeNode.
     */
    @Test
    public void testResetContextFlags()
    {
        System.out.println("resetContextFlags");
        ModTreeRootNode instance = new ModTreeRootNode(null);
        instance.resetContextFlags();
        assertFalse(instance.getContextFlag(HEX_CODE));
        assertTrue(instance.getContextFlag(FILE_HEADER));
        
        ModTree d = new ModTree();
		ModTreeRootNode r = (ModTreeRootNode)d.getDefaultRootElement();
        r.resetContextFlags();
        assertFalse(r.getContextFlag(HEX_CODE));
        assertTrue(r.getContextFlag(FILE_HEADER));
    }

    /**
     * Test of getDocument method, of class ModTreeNode.
     */
    @Test
    public void testGetTree()
    {
        System.out.println("getTree");
        ModTreeNode instance = new ModTreeNode(null);
        ModTree expResult = null;
        ModTree result = instance.getTree();
        assertEquals(expResult, result);
		
		ModTree d = new ModTree();
		ModTreeRootNode r = (ModTreeRootNode)d.getDefaultRootElement();
		ModTreeNode e = new ModTreeNode(r);
		ModTreeLeaf t = new ModTreeLeaf(e, "foo", true);
		assertEquals(d, t.getTree());
		assertEquals(d, e.getTree());
		assertEquals(d, r.getTree());
	}


    /**
     * Test of isCode method, of class ModTreeNode.
     */
    @Test
    public void testIsCode()
    {
        System.out.println("isCode");
        ModTreeNode instance = new ModTreeNode(null);
        boolean expResult = false;
        boolean result = instance.isCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of parseUnrealHex method, of class ModTreeNode.
	 * @throws java.io.IOException
     */
    @Test
    public void testParseUnrealHex() throws IOException
    {
        System.out.println("parseUnrealHex");
		String in = "\t\t0F 00 34 D2 00 00 25 // comment\n";
		OperandTableParser parser = new OperandTableParser(Paths.get("operand_data.ini"));
		parser.parseFile();
		ModTreeNode e1 = new ModTreeNode(null, true);
		e1.setRange(0, 34);
		ModTreeLeaf t1 = new ModTreeLeaf(e1, in, true);
		e1.addElement(t1);
		t1.setRange(0, 34);
		e1.parseUnrealHex();
		assertEquals(3, e1.getChildElementCount());
    }

	/**
	 * Test that contexts are set properly.
	 * @throws javax.swing.text.BadLocationException
	 */
	@Test
	public void testContextMaintanence() throws BadLocationException
	{
		System.out.println("file-level context test");
		ModTree d = new ModTree();
		ModTreeRootNode r = (ModTreeRootNode) d.getDefaultRootElement();
		r.insertString(0, "MODFILEVERSION=1\n"
							+ "UPKFILE=a\n"
							+ "GUID=b\n"
							+ "FUNCTION=c\n"
							+ "[BEFORE_HEX]\n"
							+ "[CODE]\n"
							+ "//comment\n"
							+ "0B 0B 0B \n"  // <== should have HEX_CODE context
							+ "[/CODE]\n"
							+ "// comment\n"
							+ "[/BEGIN_HEX",null);
		assertEquals(1, r.getElementCount());
		r.reorganizeAfterInsertion();
		assertEquals(11, r.getElementCount());
		assertEquals("0B 0B 0B \n", r.getElement(7).toStr());
		assertTrue(r.getElement(7).getContextFlag(HEX_CODE)); // this assertion is failing
		assertTrue(r.getElement(6).getContextFlag(BEFORE_HEX)); // this assertion also fails
		assertFalse(r.getElement(9).getContextFlag(HEX_CODE));
	}
	
    /**
     * Test of isValidHexLine method, of class ModTreeNode.
     */
    @Test
    public void testIsValidHexLine()
    {
        System.out.println("isValidHexLine");
        ModTreeLeaf t1 = new ModTreeLeaf(null, "    A7 00 00 FF // comment\n", true);
        assertTrue(t1.isValidHexLine());
        ModTreeLeaf t2 = new ModTreeLeaf(null, "  foobar  A7 00 00 FF // comment\n", true);
        assertFalse(t2.isValidHexLine());
        ModTreeLeaf t3 = new ModTreeLeaf(null, "    A7  00 00 FF // comment\n", true);
        assertFalse(t3.isValidHexLine());
    }

    /**
     * Test of toHexStringArray method, of class ModTreeNode.
     */
    @Test
    public void testToHexStringArray()
    {
        System.out.println("toHexStringArray");
        ModTreeRootNode r = new ModTreeRootNode(null);
        ModTreeNode n = new ModTreeNode(r);
        ModTreeLeaf instance = new ModTreeLeaf(n, "     A7 57 BB FF //a comment\n");
        String[] expResult = {"     ", "A7 57 BB FF ", "//a comment\n"};
        String[] result = instance.toHexStringArray();
        assertArrayEquals(expResult, result);
        ModTreeLeaf instance2 = new ModTreeLeaf(n, "  foo     A7 57 BB FF //a comment\n");
        String[] expResult2 = null;
        String[] result2 = instance2.toHexStringArray();
        assertArrayEquals(expResult2, result2);
    }

    /**
     * Test of addElement method, of class ModTreeNode.
     */
    @Test
    public void testAddElement_ModTreeNode()
    {
        System.out.println("addElement");
        ModTreeNode e = new ModTreeNode(null);
        ModTreeNode instance = new ModTreeNode(null);
        instance.addElement(e);
    }

    /**
     * Test of addElement method, of class ModTreeNode.
     */
    @Test
    public void testAddElement_int_ModTreeNode()
    {
        System.out.println("addElement");
        int index = 0;
        ModTreeNode e = null;
        ModTreeNode instance = new ModTreeNode(null);
        instance.addElement(index, e);
    }

    /**
     * Test of updateContexts method, of class ModTreeNode.
     */
    @Test
    public void testUpdateContexts()
    {
        System.out.println("updateContexts");
        ModTree d = new ModTree();
        ModTreeRootNode r = (ModTreeRootNode) d.getDefaultRootElement();
        ModTreeNode e2 = new ModTreeNode(r);
        r.resetContextFlags();
        assertFalse("HEX_CODE true, s.b. false", r.getContextFlag(HEX_CODE));
        assertTrue("FILE_HEADER false, s.b. true", r.getContextFlag(FILE_HEADER));
    }

    /**
     * Test of foundHeader method, of class ModTreeNode.
     */
    @Test
    public void testFoundHeader()
    {
        System.out.println("foundHeader");
        ModTreeNode instance = new ModTreeNode(null);
        boolean expResult = false;
        boolean result = instance.foundHeader();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTag method, of class ModTreeNode.
     */
    @Test
    public void testGetTagValue()
    {
        System.out.println("getTag");
        ModTreeNode instance = new ModTreeNode(null);
        String expResult = "bar";
        String result = instance.getTagValue("foo=bar");
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class ModTreeNode.
     */
    @Test
    public void testRemove()
    {
        System.out.println("remove");
        int offset = 0;
        int length = 0;
        ModTreeNode instance = new ModTreeNode(null);
        instance.remove(offset, length);
    }

    /**
     * Test of removeModTreeNode method, of class ModTreeNode.
     */
    @Test
    public void testRemoveModElement()
    {
        System.out.println("removeModTreeNode");
        ModTreeRootNode r = new ModTreeRootNode(null);
        ModTreeNode e1 = new ModTreeNode(r);
        ModTreeNode e2 = new ModTreeNode(e1);
        ModTreeNode e3 = new ModTreeNode(e1);
        e1.addElement(e2);
        e1.addElement(e3);
        e2.removeModElement();
        assertEquals(e3, e1.getChildElementAt(0));
    }

    /**
     * Test of insertString method, of class ModTreeNode.
     */
    @Test
    public void testInsertString()
    {
        System.out.println("insertString");
        int offset = 0;
        String string = "";
        AttributeSet as = null;
        ModTreeNode instance = new ModTreeNode(null);
        instance.insertString(offset, string, as);
    }

    /**
     * Test of insertStringAtLeaf method, of class ModTreeNode.
     */
    @Test
    public void testInsertStringAtLeaf()
    {
        System.out.println("insertStringAtLeaf");
        int offset = 0;
        String string = "";
        AttributeSet as = null;
        ModTreeNode e = new ModTreeNode(null);
		e.setRange(0, 4);
		ModTreeLeaf t = new ModTreeLeaf(e, "test", true);
		t.setRange(0, 4);
        t.insertStringAtLeaf(2, "foo", as);
		String result = t.toStr();
		String expResult = "tefoost";
		assertEquals(expResult, result);
    }

    /**
     * Test of setString method, of class ModTreeNode.
     */
    @Test
    public void testSetString()
    {
        System.out.println("setString");
        String s = "foo";
        ModTreeLeaf instance = new ModTreeLeaf(null);
        instance.setString(s);
		String result = instance.getString();
		String expResult = "foo";
		assertEquals(expResult, result);
    }

    /**
     * Test of getString method, of class ModTreeNode.
     */
    @Test
    public void testGetString()
    {
        System.out.println("getString");
        ModTreeNode e = new ModTreeNode(null);
		ModTreeLeaf t = new ModTreeLeaf(e, "foobar", true);
		String expResult = "foobar";
		String result = t.getString();
		assertEquals(expResult, result);
//        expResult = "";
//        result = e.getString();
//        assertEquals(expResult, result);
    }
    
    /**
     * Test of getLineParent method, of class ModTreeNode.
     */
    @Test
    public void testGetLineParent()
    {
        System.out.println("getLineParent");
		ModTreeRootNode r = new ModTreeRootNode(null);
        ModTreeNode e = new ModTreeNode(r);
        ModTreeLeaf t = new ModTreeLeaf(e, "test", true);
        ModTreeNode expResult = e;
        ModTreeNode result = t.getLineParent();
		assertEquals(expResult, result);
		r = new ModTreeRootNode(null);
        e = new ModTreeNode(r);
        ModOperandNode oe1 = new ModOperandNode(e);
        ModOperandNode oe2 = new ModOperandNode(oe1);
        ModReferenceLeaf rt = new ModReferenceLeaf(oe2, true);
        assertEquals(e, oe1.getLineParent());
        assertEquals(e, oe2.getLineParent());
        assertEquals(e, rt.getLineParent());
    }

    /**
     * Test of toString method, of class ModTreeNode.
     */
    @Test
    public void testToString()
    {
        System.out.println("toString");
        ModTreeNode e = new ModTreeNode(null);
		ModTreeLeaf t1 = new ModTreeLeaf(e, "foo", true);
		e.addElement(t1);
		ModTreeLeaf t2 = new ModTreeLeaf(e, "bar", true);
		e.addElement(t2);
        String expResult = "foobar";
        String result = e.toStr();
        assertEquals(expResult, result);
    }

    /**
     * Test of getText method, of class ModTreeNode.
	 * @throws javax.swing.text.BadLocationException
     */
    @Test
    public void testGetText_int_int() throws BadLocationException
    {
        System.out.println("getText");
        int offset = 0;
        int length = 0;
        ModTreeNode instance = new ModTreeNode(null);
        String expResult = "";
        String result = instance.getText(offset, length);
        assertEquals(expResult, result);
		ModTree d = new ModTree();
		ModTreeRootNode r = (ModTreeRootNode) d.getDefaultRootElement();
		r.setRange(0, 19);
		ModTreeNode e1 = new ModTreeNode(r, true);
		r.addElement(e1);
		e1.setRange(0, 9);
		ModTreeLeaf t1 = new ModTreeLeaf(e1, "testing1\n", true);
		e1.addElement(t1);
		t1.setRange(0, 9);
		ModTreeNode e2 = new ModTreeNode(r, true);
		r.addElement(e2);
		e2.setRange(9, 19);
		ModTreeLeaf t2 = new ModTreeLeaf(e2, "testing2\n", true);
		e2.addElement(t2);
		t2.setRange(9, 19);
		String expResult2 = "g1\nte";
//		String result2 = d.getText(6, 5);
//		assertEquals(expResult2, result2);
    }

    /**
     * Test of getText method, of class ModTreeNode.
     */
    @Test
    public void testGetText_3args() throws BadLocationException
    {
        System.out.println("getText");
        Segment segment = new Segment();
        ModTreeNode e = new ModTreeNode(null);
        e.getText(0, 0, segment);
		String expResult = "";
		assertEquals(expResult, segment.toString());
		ModTree d = new ModTree();
		ModTreeRootNode r = (ModTreeRootNode) d.getDefaultRootElement();
		ModTreeNode e1 = new ModTreeNode(r, true);
		r.addElement(0, e1);
		r.setRange(0, 6);
		e1.setRange(0, 6);
		ModTreeLeaf t1 = new ModTreeLeaf(e1, "foobar", true);
		e1.addElement(t1);
		t1.setRange(0, 6);
//		d.getText(2, 2, segment);
//		assertEquals("ob", segment.toString());
//		r.getText(2, 2, segment);
//		assertEquals("ob", segment.toString());
//		e1.getText(2, 2, segment);
//		assertEquals("ob", segment.toString());
//		t1.getText(2, 2, segment);
//		assertEquals("ob", segment.toString());
		
		
				
    }

    /**
     * Test of getParentElement method, of class ModTreeNode.
     */
    @Test
    public void testGetParentElement()
    {
        System.out.println("getParentElement");
        ModTreeNode instance = new ModTreeNode(null);
        ModTreeNode expResult = null;
        ModTreeNode result = instance.getParentElement();
        assertEquals(expResult, result);
    }

    /**
     * Test of isLeaf method, of class ModTreeNode.
     */
    @Test
    public void testIsLeaf()
    {
        System.out.println("isLeaf");
        ModTreeNode instance = new ModTreeNode(null);
        boolean expResult = false;
        boolean result = instance.isLeaf();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class ModTreeNode.
     */
    @Test
    public void testGetName()
    {
        System.out.println("getName");
        ModTreeNode instance = new ModTreeNode(null);
        String expResult = "ModTreeNode";
        String result = instance.getName();
        assertEquals(expResult, result);
        ModOperandNode instance2 = new ModOperandNode(instance);
        String result2 = instance2.getName();
        String expResult2 = "ModOperandElement_";
        assertEquals(expResult2, result2);
    }

    /**
     * Test of getAttributes method, of class ModTreeNode.
     */
    @Test
    public void testGetAttributes()
    {
        System.out.println("getAttributes");
        ModTreeNode instance = new ModTreeNode(null);
        AttributeSet expResult = null;
        AttributeSet result = instance.getAttributes();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStartOffset method, of class ModTreeNode.
     */
    @Test
    public void testGetStartOffset()
    {
        System.out.println("getStartOffset");
        ModTreeNode instance = new ModTreeNode(null);
        int expResult = 0;
        int result = instance.getStartOffset();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEndOffset method, of class ModTreeNode.
     */
    @Test
    public void testGetEndOffset()
    {
        System.out.println("getEndOffset");
        ModTreeNode instance = new ModTreeNode(null);
        int expResult = 0;
        int result = instance.getEndOffset();
        assertEquals(expResult, result);
    }

    /**
     * Test of getElementIndex method, of class ModTreeNode.
	 * @throws javax.swing.text.BadLocationException
     */
    @Test
    public void testGetElementIndex() throws BadLocationException
    {
        System.out.println("getElementIndex");
        ModTree d = new ModTree();
        ModTreeRootNode r = (ModTreeRootNode) d.getDefaultRootElement();
        r.insertString(0, "foo\nbar\nstuff", null);
        r.reorganizeAfterInsertion();
        assertEquals(0, r.getElementIndex(-1));
        assertEquals(0, r.getElementIndex(3));
        assertEquals(1, r.getElementIndex(5));
        assertEquals(2, r.getElementIndex(8));
        assertEquals(2, r.getElementIndex(400));
    }

    /**
     * Test of getElementCount method, of class ModTreeNode.
     */
    @Test
    public void testGetElementCount()
    {
        System.out.println("getElementCount");
        ModTreeNode instance = new ModTreeNode(null);
        int expResult = 0;
        int result = instance.getElementCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getElement method, of class ModTreeNode.
     */
    @Test
    public void testGetElement()
    {
        System.out.println("getElement");
        int n = 0;
        ModTreeNode instance = new ModTreeNode(null);
        ModTreeNode expResult = null;
        ModTreeNode result = instance.getElement(n);
        assertEquals(expResult, result);
    }

    /**
     * Test of getContextFlag method, of class ModTreeNode.
     */
    @Test
    public void testSetAndInContext()
    {
        System.out.println("getContextFlag");
        ModTree d = new ModTree();
        ModTreeRootNode r = (ModTreeRootNode)d.getDefaultRootElement();
        ModTreeNode e = new ModTreeNode(d.getDefaultRootElement());
        assertFalse(e.getContextFlag(HEX_CODE));
    }

    /**
     * Test of setContext method, of class ModTreeNode.
     */
    @Test
    public void testSetContextFlag()
    {
        System.out.println("setContext");
        ModContextType type = null;
        boolean val = false;
        ModTreeNode instance = new ModTreeNode(null);
        instance.setContextFlag(type, val);
    }

    /**
     * Test of getMemorySize method, of class ModTreeNode.
     */
    @Test
    public void testGetMemorySize()
    {
        System.out.println("getMemorySize");
        ModTreeNode instance = new ModTreeNode(null);
        int expResult = 0;
        int result = instance.getMemorySize();
        assertEquals(expResult, result);
    }

    /**
     * Test of isVFFunctionRef method, of class ModTreeNode.
     */
    @Test
    public void testIsVFFunctionRef()
    {
        System.out.println("isVFFunctionRef");
        ModTreeNode instance = new ModTreeNode(null);
        boolean expResult = false;
        boolean result = instance.isVFFunctionRef();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOffset method, of class ModTreeNode.
     */
    @Test
    public void testGetOffset()
    {
        System.out.println("getOffset");
        ModTreeNode instance = new ModTreeNode(null);
        int expResult = -1;
        int result = instance.getOffset();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRefValue method, of class ModTreeNode.
     */
    @Test
    public void testGetRefValue()
    {
        System.out.println("getRefValue");
        ModTreeNode instance = new ModTreeNode(null);
        int expResult = -1;
        int result = instance.getRefValue();
        assertEquals(expResult, result);
    }
    
}
