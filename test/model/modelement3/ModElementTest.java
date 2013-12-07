package model.modelement3;

import javax.swing.text.AttributeSet;
import javax.swing.text.Segment;
import model.moddocument3.ModDocument;
import model.modelement3.ModContext.ModContextType;
import static model.modelement3.ModContext.ModContextType.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import parser.unrealhex.OperandTable;
import io.parser.OperandTableParser;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Administrator
 */


public class ModElementTest
{
    
    public ModElementTest()
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
     * Test of setLocalContext method, of class ModElement.
     */
    @Test
    public void testSetAndInLocalContext()
    {
        System.out.println("setLocalContext");
        ModElement instance = new ModElement(null);
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
     * Test of resetContextFlags method, of class ModElement.
     */
    @Test
    public void testResetContextFlags()
    {
        System.out.println("resetContextFlags");
        ModElement instance = new ModElement(null);
        instance.resetContextFlags();
        assertFalse(instance.getContextFlag(HEX_CODE));
        assertFalse(instance.getContextFlag(FILE_HEADER));
        
        ModDocument d = new ModDocument();
		ModRootElement r = (ModRootElement)d.getDefaultRootElement();
        r.resetContextFlags();
        assertFalse(r.getContextFlag(HEX_CODE));
        assertTrue(r.getContextFlag(FILE_HEADER));
    }

    /**
     * Test of getDocument method, of class ModElement.
     */
    @Test
    public void testGetDocument()
    {
        System.out.println("getDocument");
        ModElement instance = new ModElement(null);
        ModDocument expResult = null;
        ModDocument result = instance.getDocument();
        assertEquals(expResult, result);
		
		ModDocument d = new ModDocument();
		ModRootElement r = (ModRootElement)d.getDefaultRootElement();
		ModElement e = new ModElement(r);
		ModToken t = new ModToken(e, "foo", true);
		assertEquals(d, t.getDocument());
		assertEquals(d, e.getDocument());
		assertEquals(d, r.getDocument());
	}


    /**
     * Test of isCode method, of class ModElement.
     */
    @Test
    public void testIsCode()
    {
        System.out.println("isCode");
        ModElement instance = new ModElement(null);
        boolean expResult = false;
        boolean result = instance.isCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of parseUnrealHex method, of class ModElement.
     */
    @Test
    public void testParseUnrealHex() throws IOException
    {
        System.out.println("parseUnrealHex");
		String in = "\t\t0F 00 34 D2 00 00 25 // comment\n";
		OperandTableParser parser = new OperandTableParser(Paths.get("operand_data.ini"));
		parser.parseFile();
		ModElement e1 = new ModElement(null, true);
		e1.setRange(0, 34);
		ModToken t1 = new ModToken(e1, in, true);
		e1.addElement(t1);
		t1.setRange(0, 34);
        e1.parseUnrealHex();
    }

    /**
     * Test of isValidHexLine method, of class ModElement.
     */
    @Test
    public void testIsValidHexLine()
    {
        System.out.println("isValidHexLine");
        ModToken t1 = new ModToken(null, "    A7 00 00 FF // comment\n", true);
        assertTrue(t1.isValidHexLine());
        ModToken t2 = new ModToken(null, "  foobar  A7 00 00 FF // comment\n", true);
        assertFalse(t2.isValidHexLine());
        ModToken t3 = new ModToken(null, "    A7  00 00 FF // comment\n", true);
        assertFalse(t3.isValidHexLine());
    }

    /**
     * Test of toHexStringArray method, of class ModElement.
     */
    @Test
    public void testToHexStringArray()
    {
        System.out.println("toHexStringArray");
        ModRootElement r = new ModRootElement(null);
        ModElement n = new ModElement(r);
        ModToken instance = new ModToken(n, "     A7 57 BB FF //a comment\n");
        String[] expResult = {"     ", "A7 57 BB FF ", "//a comment\n"};
        String[] result = instance.toHexStringArray();
        assertArrayEquals(expResult, result);
        ModToken instance2 = new ModToken(n, "  foo     A7 57 BB FF //a comment\n");
        String[] expResult2 = null;
        String[] result2 = instance2.toHexStringArray();
        assertArrayEquals(expResult2, result2);
    }

    /**
     * Test of addElement method, of class ModElement.
     */
    @Test
    public void testAddElement_ModElement()
    {
        System.out.println("addElement");
        ModElement e = new ModElement(null);
        ModElement instance = new ModElement(null);
        instance.addElement(e);
    }

    /**
     * Test of addElement method, of class ModElement.
     */
    @Test
    public void testAddElement_int_ModElement()
    {
        System.out.println("addElement");
        int index = 0;
        ModElement e = null;
        ModElement instance = new ModElement(null);
        instance.addElement(index, e);
    }

    /**
     * Test of updateContexts method, of class ModElement.
     */
    @Test
    public void testUpdateContexts()
    {
        System.out.println("updateContexts");
        ModElement e = new ModElement(null);
        e.updateContexts();
        ModDocument d = new ModDocument();
        ModElement r = d.getDefaultRootElement();
        ModElement e2 = new ModElement(r);
        e.resetContextFlags();
        r.resetContextFlags();
        assertFalse(r.getContextFlag(HEX_CODE));
        assertTrue(r.getContextFlag(FILE_HEADER));
    }

    /**
     * Test of foundHeader method, of class ModElement.
     */
    @Test
    public void testFoundHeader()
    {
        System.out.println("foundHeader");
        ModElement instance = new ModElement(null);
        boolean expResult = false;
        boolean result = instance.foundHeader();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTag method, of class ModElement.
     */
    @Test
    public void testGetTagValue()
    {
        System.out.println("getTag");
        ModElement instance = new ModElement(null);
        String expResult = "bar";
        String result = instance.getTagValue("foo=bar");
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class ModElement.
     */
    @Test
    public void testRemove()
    {
        System.out.println("remove");
        int offset = 0;
        int length = 0;
        ModElement instance = new ModElement(null);
        instance.remove(offset, length);
    }

    /**
     * Test of removeModElement method, of class ModElement.
     */
    @Test
    public void testRemoveModElement()
    {
        System.out.println("removeModElement");
        ModRootElement r = new ModRootElement(null);
        ModElement e1 = new ModElement(r);
        ModElement e2 = new ModElement(e1);
        ModElement e3 = new ModElement(e1);
        e1.addElement(e2);
        e1.addElement(e3);
        e2.removeModElement();
        assertEquals(e3, e1.getChildElementAt(0));
    }

    /**
     * Test of insertString method, of class ModElement.
     */
    @Test
    public void testInsertString()
    {
        System.out.println("insertString");
        int offset = 0;
        String string = "";
        AttributeSet as = null;
        ModElement instance = new ModElement(null);
        instance.insertString(offset, string, as);
    }

    /**
     * Test of insertStringAtLeaf method, of class ModElement.
     */
    @Test
    public void testInsertStringAtLeaf()
    {
        System.out.println("insertStringAtLeaf");
        int offset = 0;
        String string = "";
        AttributeSet as = null;
        ModElement e = new ModElement(null);
		e.setRange(0, 4);
		ModToken t = new ModToken(e, "test", true);
		t.setRange(0, 4);
        t.insertStringAtLeaf(2, "foo", as);
		String result = t.toString();
		String expResult = "tefoost";
		assertEquals(expResult, result);
    }

    /**
     * Test of setString method, of class ModElement.
     */
    @Test
    public void testSetString()
    {
        System.out.println("setString");
        String s = "foo";
        ModToken instance = new ModToken(null);
        instance.setString(s);
		String result = instance.getString();
		String expResult = "foo";
		assertEquals(expResult, result);
    }

    /**
     * Test of getString method, of class ModElement.
     */
    @Test
    public void testGetString()
    {
        System.out.println("getString");
        ModElement e = new ModElement(null);
		ModToken t = new ModToken(e, "foobar", true);
		String expResult = "foobar";
		String result = t.getString();
		assertEquals(expResult, result);
//        expResult = "";
//        result = e.getString();
//        assertEquals(expResult, result);
    }
    
    /**
     * Test of getLineParent method, of class ModElement.
     */
    @Test
    public void testGetLineParent()
    {
        System.out.println("getLineParent");
        ModElement e = new ModElement(null);
        ModToken t = new ModToken(e, "test", true);
        ModElement expResult = e;
        ModElement result = t.getLineParent();
        assertEquals(expResult, result);
        e = new ModElement(null);
        ModOperandElement oe1 = new ModOperandElement(e);
        ModOperandElement oe2 = new ModOperandElement(oe1);
        ModReferenceToken rt = new ModReferenceToken(oe2, true);
        assertEquals(e, oe1.getLineParent());
        assertEquals(e, oe2.getLineParent());
        assertEquals(e, rt.getLineParent());
    }

    /**
     * Test of toString method, of class ModElement.
     */
    @Test
    public void testToString()
    {
        System.out.println("toString");
        ModElement e = new ModElement(null);
		ModToken t1 = new ModToken(e, "foo", true);
		e.addElement(t1);
		ModToken t2 = new ModToken(e, "bar", true);
		e.addElement(t2);
        String expResult = "foobar";
        String result = e.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getText method, of class ModElement.
     */
    @Test
    public void testGetText_int_int()
    {
        System.out.println("getText");
        int offset = 0;
        int length = 0;
        ModElement instance = new ModElement(null);
        String expResult = "";
        String result = instance.getText(offset, length);
        assertEquals(expResult, result);
		ModDocument d = new ModDocument();
		ModRootElement r = (ModRootElement) d.getDefaultRootElement();
		r.setRange(0, 19);
		ModElement e1 = new ModElement(r, true);
		r.addElement(e1);
		e1.setRange(0, 9);
		ModToken t1 = new ModToken(e1, "testing1\n", true);
		e1.addElement(t1);
		t1.setRange(0, 9);
		ModElement e2 = new ModElement(r, true);
		r.addElement(e2);
		e2.setRange(9, 19);
		ModToken t2 = new ModToken(e2, "testing2\n", true);
		e2.addElement(t2);
		t2.setRange(9, 19);
		String expResult2 = "g1\nte";
		String result2 = d.getText(6, 5);
		assertEquals(expResult2, result2);
    }

    /**
     * Test of getText method, of class ModElement.
     */
    @Test
    public void testGetText_3args()
    {
        System.out.println("getText");
        Segment segment = new Segment();
        ModElement e = new ModElement(null);
        e.getText(0, 0, segment);
		String expResult = "";
		assertEquals(expResult, segment.toString());
		ModDocument d = new ModDocument();
		ModRootElement r = (ModRootElement) d.getDefaultRootElement();
		ModElement e1 = new ModElement(r, true);
		r.addElement(0, e1);
		r.setRange(0, 6);
		e1.setRange(0, 6);
		ModToken t1 = new ModToken(e1, "foobar", true);
		e1.addElement(t1);
		t1.setRange(0, 6);
		d.getText(2, 2, segment);
		assertEquals("ob", segment.toString());
		r.getText(2, 2, segment);
		assertEquals("ob", segment.toString());
		e1.getText(2, 2, segment);
		assertEquals("ob", segment.toString());
		t1.getText(2, 2, segment);
		assertEquals("ob", segment.toString());
		
		
				
    }

    /**
     * Test of getParentElement method, of class ModElement.
     */
    @Test
    public void testGetParentElement()
    {
        System.out.println("getParentElement");
        ModElement instance = new ModElement(null);
        ModElement expResult = null;
        ModElement result = instance.getParentElement();
        assertEquals(expResult, result);
    }

    /**
     * Test of isLeaf method, of class ModElement.
     */
    @Test
    public void testIsLeaf()
    {
        System.out.println("isLeaf");
        ModElement instance = new ModElement(null);
        boolean expResult = false;
        boolean result = instance.isLeaf();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class ModElement.
     */
    @Test
    public void testGetName()
    {
        System.out.println("getName");
        ModElement instance = new ModElement(null);
        String expResult = "ModElement";
        String result = instance.getName();
        assertEquals(expResult, result);
        ModOperandElement instance2 = new ModOperandElement(instance);
        String result2 = instance2.getName();
        String expResult2 = "ModOperandElement_";
        assertEquals(expResult2, result2);
    }

    /**
     * Test of getAttributes method, of class ModElement.
     */
    @Test
    public void testGetAttributes()
    {
        System.out.println("getAttributes");
        ModElement instance = new ModElement(null);
        AttributeSet expResult = null;
        AttributeSet result = instance.getAttributes();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStartOffset method, of class ModElement.
     */
    @Test
    public void testGetStartOffset()
    {
        System.out.println("getStartOffset");
        ModElement instance = new ModElement(null);
        int expResult = 0;
        int result = instance.getStartOffset();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEndOffset method, of class ModElement.
     */
    @Test
    public void testGetEndOffset()
    {
        System.out.println("getEndOffset");
        ModElement instance = new ModElement(null);
        int expResult = 0;
        int result = instance.getEndOffset();
        assertEquals(expResult, result);
    }

    /**
     * Test of getElementIndex method, of class ModElement.
     */
    @Test
    public void testGetElementIndex()
    {
        System.out.println("getElementIndex");
        ModDocument d = new ModDocument();
        ModRootElement r = (ModRootElement) d.getDefaultRootElement();
        d.insertString(0, "foo\nbar\nstuff", null);
        d.insertUpdate(null, null);
        assertEquals(0, r.getElementIndex(-1));
        assertEquals(0, r.getElementIndex(3));
        assertEquals(1, r.getElementIndex(5));
        assertEquals(2, r.getElementIndex(8));
        assertEquals(2, r.getElementIndex(400));
    }

    /**
     * Test of getElementCount method, of class ModElement.
     */
    @Test
    public void testGetElementCount()
    {
        System.out.println("getElementCount");
        ModElement instance = new ModElement(null);
        int expResult = 0;
        int result = instance.getElementCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getElement method, of class ModElement.
     */
    @Test
    public void testGetElement()
    {
        System.out.println("getElement");
        int n = 0;
        ModElement instance = new ModElement(null);
        ModElement expResult = null;
        ModElement result = instance.getElement(n);
        assertEquals(expResult, result);
    }

    /**
     * Test of getContextFlag method, of class ModElement.
     */
    @Test
    public void testSetAndInContext()
    {
        System.out.println("getContextFlag");
        ModDocument d = new ModDocument();
        ModRootElement r = (ModRootElement)d.getDefaultRootElement();
        ModElement e = new ModElement(d.getDefaultRootElement());
        assertFalse(e.getContextFlag(HEX_CODE));
    }

    /**
     * Test of setContext method, of class ModElement.
     */
    @Test
    public void testSetContextFlag()
    {
        System.out.println("setContext");
        ModContextType type = null;
        boolean val = false;
        ModElement instance = new ModElement(null);
        instance.setContextFlag(type, val);
    }

    /**
     * Test of getMemorySize method, of class ModElement.
     */
    @Test
    public void testGetMemorySize()
    {
        System.out.println("getMemorySize");
        ModElement instance = new ModElement(null);
        int expResult = 0;
        int result = instance.getMemorySize();
        assertEquals(expResult, result);
    }

    /**
     * Test of isVFFunctionRef method, of class ModElement.
     */
    @Test
    public void testIsVFFunctionRef()
    {
        System.out.println("isVFFunctionRef");
        ModElement instance = new ModElement(null);
        boolean expResult = false;
        boolean result = instance.isVFFunctionRef();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOffset method, of class ModElement.
     */
    @Test
    public void testGetOffset()
    {
        System.out.println("getOffset");
        ModElement instance = new ModElement(null);
        int expResult = -1;
        int result = instance.getOffset();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRefValue method, of class ModElement.
     */
    @Test
    public void testGetRefValue()
    {
        System.out.println("getRefValue");
        ModElement instance = new ModElement(null);
        int expResult = -1;
        int result = instance.getRefValue();
        assertEquals(expResult, result);
    }
    
}
