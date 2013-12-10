package model.moddocument3;

import UPKmodder.UpkConfigData;
import io.parser.OperandTableParser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
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
 * @author Amineri
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
//    @Test
//    public void testCreateRoot()
//    {
//        System.out.println("createDefaultRoot");
//        ModDocument instance = new ModDocument();
//        instance.createRoot();
//    }

    /**
     * Test of reorganize method, of class ModDocument.
     */
//    @Test
//    public void testReorganize()
//    {
//        System.out.println("reorganize");
//        ModDocument instance = new ModDocument();
////        instance.reorganize();
//        instance.createRoot();
////        instance.reorganize();
//    }

    /**
     * Test of addDocumentListener method, of class ModDocument.
     */
//    @Ignore("Not yet implemented")
    @Test
    public void testAddDocumentListener()
    {
        System.out.println("addDocumentListener");
        DocumentListener dl = null;
        ModDocument instance = new ModDocument();
        instance.addDocumentListener(dl);
    }

    /**
     * Test of removeDocumentListener method, of class ModDocument.
     */
//    @Ignore("Not yet implemented") 
    @Test
    public void testRemoveDocumentListener()
    {
        System.out.println("removeDocumentListener");
        DocumentListener dl = null;
        ModDocument instance = new ModDocument();
        instance.removeDocumentListener(dl);
    }

    /**
     * Test of addUndoableEditListener method, of class ModDocument.
     */
//    @Ignore("Not yet implemented") 
    @Test
    public void testAddUndoableEditListener()
    {
        System.out.println("addUndoableEditListener");
        UndoableEditListener ul = null;
        ModDocument instance = new ModDocument();
        instance.addUndoableEditListener(ul);
    }

    /**
     * Test of removeUndoableEditListener method, of class ModDocument.
     */
//    @Ignore("Not yet implemented") 
    @Test
    public void testRemoveUndoableEditListener()
    {
        System.out.println("removeUndoableEditListener");
        UndoableEditListener ul = null;
        ModDocument instance = new ModDocument();
        instance.removeUndoableEditListener(ul);
    }

    /**
     * Test of render method, of class ModDocument.
     */
//    @Ignore("Not yet implemented") 
    @Test
    public void testRender()
    {
        System.out.println("render");
        Runnable r = new Runnable()
        {

            @Override
            public void run()
            {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        ModDocument instance = new ModDocument();
        instance.render(r);
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
//        instance.createRoot();
        result = instance.getLength();
        assertEquals(expResult, result);
    }

    /**
     * Test of putProperty and getProperty methods, of class ModDocument.
     */
    @Test
    public void testPutProperty()
    {
        System.out.println("putProperty");
        String key = "bar";
        String value = "foo";
        ModDocument instance = new ModDocument();
        instance.putProperty(key, value);
        System.out.println("getProperty");
        String expResult = value;
        Object result = instance.getProperty(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of remove method, of class ModDocument.
     */
    @Test
    public void testRemove() throws BadLocationException
    {
        System.out.println("remove");
        int offset = 0;
        int length = 0;
        ModDocument instance = new ModDocument();
//        instance.createRoot();
        instance.remove(offset, length);
        instance.removeUpdate(null);
    }

    /**
     * Test of insertString method, of class ModDocument.
     */
    @Test
    public void testInsertString() throws BadLocationException
    {
        System.out.println("insertString");
        int offset = 0;
        String string = "";
        AttributeSet as = null;
        ModDocument instance = new ModDocument();
        instance.insertString(offset, string, as);
//        instance.createRoot();
        instance.insertString(offset, string, as);
        instance.insertString(offset, "test", as);
        instance.insertString(offset, "line1 \n line2", as);
        instance.insertUpdate(null, as);
    }

    /**
     * Test of multiple insertions and deletions.
     */
    @Test
    public void testInsertGetRemove() throws BadLocationException
    {
        System.out.println("Insert, Get, Delete");
        int offset = 0;
        String string = "test1string";
        String expected = "st1";
        AttributeSet as = null;
        ModDocument instance = new ModDocument();
		ModElement r = instance.getDefaultRootElement();
//        instance.createRoot();
        instance.insertString(offset, string, as);
        instance.insertUpdate(null, as);
        String result = instance.getText(2, 3);
        assertEquals(expected, result);
        String string2 = "foo\nbarsnafu";
        instance.insertString(7, string2, as);
        instance.insertUpdate(null, as);
        expected = "test1stfoo\nbarsnafuring";
        expected = "rsnaf";
        result = instance.getText(13, 5);
        assertEquals(expected, result);
        instance.remove(14, 3);
        instance.removeUpdate(null);
        expected = "test1stfoo\nbarfuring";
        result = instance.getText(11, 4);
        expected = "barf";
        assertEquals(expected, result);
    }   
    
    /**
     * Test actually reading a upk_mod file.
     * @throws java.io.IOException
     */
    @Test
    public void testReadUpkModFile() throws IOException, BadLocationException
    {
        System.out.println("Read test_mod_v3.upk_mod");
        AttributeSet as = null;
		OperandTableParser parser = new OperandTableParser(Paths.get("operand_data.ini"));
		parser.parseFile();
        ModDocument myDoc = new ModDocument();
		ModElement r = myDoc.getDefaultRootElement();
//        myDoc.createRoot();
        String encoding = System.getProperty("file.encoding");
        long startTime;
        try (Scanner s = new Scanner(Files.newBufferedReader(Paths.get("test/resources/test_mod_v3.upk_mod"), Charset.forName(encoding))))
        {
            System.out.print("Reading modfile... ");
            startTime = System.currentTimeMillis();
            while(s.hasNext())
            {
                myDoc.insertString(myDoc.getLength(), s.nextLine() + "\n", as);
            }
            System.out.print(" done, took " + (System.currentTimeMillis() - startTime) + "ms\nParsing modfile... ");
            startTime = System.currentTimeMillis();
//            myDoc.insertUpdate(null, as);
            System.out.print(" done, took " + (System.currentTimeMillis() - startTime) + "ms\n");
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
        String result = myDoc.getText(57, 10);
        System.out.println(myDoc.getLength());
//        System.out.print(myDoc.getDefaultRootElement().toString());
        result = myDoc.getText(892, 900);
//		System.out.println(myDoc.getText(892, 900));
        String expResult;
        expResult = "19 19 2E FE 2C 00 00 19 12 20 4F FE FF FF 0A 00 D8 F9 FF FF 00 1C F6 FB FF FF 16 09 00 98 F9 FF FF 00 01 98 F9 FF FF 09 00 F0 2C 00 00 00 01 F0 2C 00 00 13 01 42 10 00 00 00 1B 16 31 00 00 00 00 00 00 38 3A 19 19 00 C4 7E 00 00 09 00 E8 BB 00 00 00 01 E8 BB 00 00 0A 00 E8 9B 00 00 00 1B 92 30 00 00 00 00 00 00 16 19 00 C4 7E 00 00 0A 00 1C 7C 00 00 00 1B 1E 35 00 00 00 00 00 00 16 19 19 19 00 C4 7E 00 00 09 00 E6 7B 00 00 00 01 E6 7B 00 00 0A 00 EB B2 00 00 00 1B 0A 34 00 00 00 00 00 00 16 0C 00 9E 94 00 00 00 1B 7A 36 00 00 00 00 00 00 24 0A 16 19 19 19 00 C4 7E 00 00 09 00 E6 7B 00 00 00 01 E6 7B 00 00 0A 00 63 B4 00 00 00 1B 7B 31 00 00 00 00 00 00 16 09 00 C3 A2 00 00 00 01 C3 A2 00 00 19 00 C4 7E 00 00 0A 00 D2 7B 00 00 00 2D 01 D2 7B 00 00 16 \n" +
                "[/CODE]\n" +
                "[/BEFORE_HEX]\n" +
                "\n" +
                "[AFTER_HEX]\n" +
                "[CODE]\n" +
                "// iCost = kAbility.GraduatedOdds(0, kAbility, kAbility.m_kUnit.GetPlayer().HasFoundryHistory(10))";
//        System.out.print(myDoc.getText(0, myDoc.getLength()));
//        System.out.println(myDoc.getDefaultRootElement().toString());
        assertEquals(900, result.length());
        startTime = System.currentTimeMillis();
        System.out.print("Re-parsing modfile... ");
        myDoc.insertUpdate(null, as);
        System.out.print(" done, took " + (System.currentTimeMillis() - startTime) + "ms\n");
        result = myDoc.getText(892, 900);
        assertEquals(900, result.length());
		assertTrue(myDoc.getDefaultRootElement().getChildElementAt(9).getElementCount() > 1); // check that line 9 (unreal) was parsed
    }   
    
	    /**
     * Test actually reading and editing a upk_mod file.
     * @throws java.io.IOException
     */
    @Test
    public void testReadAndEditUpkModFile() throws IOException, BadLocationException {
        System.out.println("Edit test_mod_v3.upk_mod");
        AttributeSet as = null;
		OperandTableParser parser = new OperandTableParser(Paths.get("operand_data.ini"));
		parser.parseFile();
        ModDocument myDoc = new ModDocument();
		ModElement r = myDoc.getDefaultRootElement();
//        myDoc.createRoot();
        String encoding = System.getProperty("file.encoding");
        long startTime;
        try (Scanner s = new Scanner(Files.newBufferedReader(Paths.get("test/resources/test_mod_v3.upk_mod"), Charset.forName(encoding))))
        {
            while(s.hasNext())
            {
                myDoc.insertString(myDoc.getLength(), s.nextLine() + "\n", as);
            }
            myDoc.insertUpdate(null, as);
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }

	}

	
    /**
     * Test of getText method, of class ModDocument.
	 * @throws javax.swing.text.BadLocationException
     */
    @Test
    public void testGetText_int_int() throws BadLocationException
    {
        System.out.println("getText");
        int offset = 0;
        int length = 0;
        ModDocument d = new ModDocument();
        String expResult = "";
        String result = d.getText(offset, length);
        assertEquals(expResult, result);
		d.insertString(0, "testing\nfoo\nbar", null);
		result = d.getText(3, 2);
		expResult = "ti";
		assertEquals(expResult, result);
    }

    /**
     * Test of getText method, of class ModDocument.
	 * @throws javax.swing.text.BadLocationException
     */
    @Test
    public void testGetText_3args() throws BadLocationException
    {
        System.out.println("getText");
        int offset = 0;
        int length = 0;
        Segment segment = new Segment();
        ModDocument instance = new ModDocument();
        instance.getText(offset, length, segment);
//        instance.createRoot();
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
        Position expResult = new ModPosition(1);
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
//        instance.createRoot();
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
        ModElement[] expResult = {instance.getDefaultRootElement()};
        ModElement[] result = instance.getRootElements();
        assertArrayEquals(expResult, result);
//        instance.createRoot();
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
        ModElement result = instance.getDefaultRootElement();
        assertNotNull(result);
    }

    /**
     * Test of insertUpdate method, of class ModDocument.
     */
    @Test
    public void testInsertUpdate()
    {
        System.out.println("insertUpdate");
        AbstractDocument.DefaultDocumentEvent chng = null;
        AttributeSet attr = null;
        ModDocument instance = new ModDocument();
        instance.insertUpdate(chng, attr);
//        instance.createRoot();
        instance.insertUpdate(chng, attr);
    }

    /**
     * Test of removeUpdate method, of class ModDocument.
     */
    @Test
    public void testRemoveUpdate()
    {
        System.out.println("removeUpdate");
        AbstractDocument.DefaultDocumentEvent chng = null;
        ModDocument instance = new ModDocument();
        instance.removeUpdate(chng);
//        instance.createRoot();
        instance.removeUpdate(chng);
   }
    
}
