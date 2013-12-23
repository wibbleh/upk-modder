package util.unrealhex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import io.upk.UpkFileLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import model.modtree.ModTree;
import model.upk.UpkFile;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ui.ModTab;

/**
 * Test suite for HexSearchAndReplace utilities
 * @author Amineri
 */


public class HexSearchAndReplaceTest {
	
	public HexSearchAndReplaceTest() {
	}
	
	private static UpkFileLoader upks;
	private ModTree tree;
	private DefaultStyledDocument document;
	
	private String upkSrcName = "XComGame.upk";
	private String srcGuid = "33 2E 29 6A A5 DD FC 40 B5 CC 57 A5 A7 AA 8C 41"; // EU patch 4
	
	private String upkDstName_p5 = "XComGame.upk";
	private String dstGuid_p5 = "01 E9 EB 29 23 F4 DB 4F A8 2B 8E 46 A7 25 E5 D6"; // EU patch 5

	private String upkDstName_ew = "XComGame.upk";
	private String dstGuid_ew = "B1 1A D8 E4 48 29 FC 43 8E C0 7A B0 A3 3E 34 9F"; // EW release

	@BeforeClass
	public static void setUpClass() {
//		// initialize upks for all classes
//		System.out.println("Reading upks.");
//		upks = new UpkFileLoader();
//
//		// initialize Operand Table for all tests that use it
//		System.out.println("Reading operand data.");
//		OperandTableParser parser = new OperandTableParser(Paths.get("config/operand_data.ini"));
//		try {
//			parser.parseFile();
//		} catch(IOException ex) {
//			Logger.getLogger(ModTreeTest.class.getName()).log(Level.SEVERE, null, ex);
//		}

	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() throws BadLocationException {
//		// initialize document from test file
//		System.out.println("Read test_mod_HexSearchAndReplace.upk_mod");
//		document = new DefaultStyledDocument();
//		// arbitrary default AttributeSet
//		AttributeSet as = new SimpleAttributeSet();
//		StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
//		StyleConstants.setItalic((MutableAttributeSet) as, false);
//		String encoding = System.getProperty("file.encoding");
//		try(Scanner s = new Scanner(Files.newBufferedReader(Paths.get("test/resources/test_mod_HexSearchAndReplace.upk_mod"), Charset.forName(encoding)))) {
//			while(s.hasNext()) {
//				document.insertString(document.getLength(), s.nextLine() + "\n", as);
//			}
//		} catch(IOException x) {
//			System.out.println("caught exception: " + x);
//		}
//
//		// initialize tree from document
//		tree = new ModTree(document);
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of consolidateBeforeHex method, of class HexSearchAndReplace.
	 */
	@Test
	public void testConsolidateBeforeHex() {
		System.out.println("consolidateBeforeHex");
		List<byte[]> expResult = new ArrayList<>(2);
		byte[] array = {(byte) 0x0F, (byte) 0x00, (byte) 0x10, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x25 };
		expResult.add(array);
		byte[] array2 = {(byte) 0x0F, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x19, 
			(byte) 0x2E, (byte) 0xFE, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x12, (byte) 0x20, (byte) 0x4F, 
			(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0x0A, (byte) 0x00, (byte) 0xD8, (byte) 0xF9, (byte) 0xFF, (byte) 0xFF, 
			(byte) 0x00, (byte) 0x1C, (byte) 0xF6, (byte) 0xFB, (byte) 0xFF, (byte) 0xFF, (byte) 0x16, (byte) 0x09, (byte) 0x00, 
			(byte) 0x98, (byte) 0xF9, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x98, (byte) 0xF9, (byte) 0xFF, 
			(byte) 0xFF, (byte) 0x09, (byte) 0x00, (byte) 0xF0, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, 
			(byte) 0xF0, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x13, (byte) 0x01, (byte) 0x42, (byte) 0x10, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0x16, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x3A, (byte) 0x19, (byte) 0x19, (byte) 0x00, (byte) 0xC4, (byte) 0x7E, 
			(byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0xE8, (byte) 0xBB, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0x01, (byte) 0xE8, (byte) 0xBB, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0xE8, (byte) 0x9B, 
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0x92, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x19, (byte) 0x00, (byte) 0xC4, (byte) 0x7E, (byte) 0x00, 
			(byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x1C, (byte) 0x7C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1B, 
			(byte) 0x1E, (byte) 0x35, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, 
			(byte) 0x19, (byte) 0x19, (byte) 0x19, (byte) 0x00, (byte) 0xC4, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x09, 
			(byte) 0x00, (byte) 0xE6, (byte) 0x7B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xE6, (byte) 0x7B, 
			(byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0xEB, (byte) 0xB2, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0x1B, (byte) 0x0A, (byte) 0x34, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0x16, (byte) 0x0C, (byte) 0x00, (byte) 0x9E, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1B, 
			(byte) 0x7A, (byte) 0x36, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x24, 
			(byte) 0x0A, (byte) 0x16, (byte) 0x19, (byte) 0x19, (byte) 0x19, (byte) 0x00, (byte) 0xC4, (byte) 0x7E, (byte) 0x00, 
			(byte) 0x00, (byte) 0x09, (byte) 0x00, (byte) 0xE6, (byte) 0x7B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, 
			(byte) 0xE6, (byte) 0x7B, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x63, (byte) 0xB4, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0x7B, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x09, (byte) 0x00, (byte) 0xC3, (byte) 0xA2, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0x01, (byte) 0xC3, (byte) 0xA2, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x00, (byte) 0xC4, 
			(byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0xD2, (byte) 0x7B, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0x2D, (byte) 0x01, (byte) 0xD2, (byte) 0x7B, (byte) 0x00, (byte) 0x00, (byte) 0x16
		};
		expResult.add(array2);
		UpkFile upk = upks.getUpk(upkSrcName, srcGuid);
		List<byte[]> result = HexSearchAndReplace.consolidateBeforeHex(tree, upk);
		assertEquals(2, result.size());
		assertArrayEquals(expResult.get(0), result.get(0));
		assertArrayEquals(expResult.get(1), result.get(1));
	}

	/**
	 * Test of consolidateAfterHex method, of class HexSearchAndReplace.
	 */
	@Test
	public void testConsolidateAfterHex() {
		System.out.println("consolidateAfterHex");
		List<byte[]> expResult = new ArrayList<>(2);
		byte[] array = {(byte) 0x0F, (byte) 0x00, (byte) 0x10, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x26 };
		expResult.add(array);
		byte[] array2 = {(byte) 0x0F, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x19, (byte) 0x00,
			(byte) 0xC4, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x61, (byte) 0x00, (byte) 0xB9, (byte) 0x7C, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0xE1, (byte) 0x35, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0xC4, (byte) 0x7E, (byte) 0x00, (byte) 0x00,
			(byte) 0x19, (byte) 0x19, (byte) 0x19, (byte) 0x00, (byte) 0xC4, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x09,
			(byte) 0x00, (byte) 0xE6, (byte) 0x7B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xE6, (byte) 0x7B,
			(byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0xEB, (byte) 0xB2, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x1B, (byte) 0x0A, (byte) 0x34, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x16, (byte) 0x0C, (byte) 0x00, (byte) 0x9E, (byte) 0x94, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1B,
			(byte) 0x7A, (byte) 0x36, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x24,
			(byte) 0x0A, (byte) 0x16, (byte) 0x16, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B,
			(byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3,
			(byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, 
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, 
			(byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
			(byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, 
			(byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, 
			(byte) 0x00, (byte) 0x00, (byte) 0xC3, (byte) 0x7E, (byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, 
			(byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, 
			(byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, 
			(byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, 
			(byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, 
			(byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, 
			(byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B, (byte) 0x0B
		};
		expResult.add(array2);
		UpkFile upk = upks.getUpk(upkSrcName, srcGuid);
		List<byte[]> result = HexSearchAndReplace.consolidateAfterHex(tree, upk);
		assertEquals(2, result.size());
		assertArrayEquals(expResult.get(0), result.get(0));
		assertArrayEquals(expResult.get(1), result.get(1));
	}

	/**
	 * Test of findFilePosition method, of class HexSearchAndReplace.
	 */
	@Test
	public void testFindFilePosition() throws Exception {
		System.out.println("findFilePosition");
		UpkFile upk = upks.getUpk(upkSrcName, srcGuid);
		List<byte[]> hex = HexSearchAndReplace.consolidateBeforeHex(tree, upk);
		long result1 = HexSearchAndReplace.findFilePosition(hex.get(0), upk, tree);
		assertEquals(-1, result1);

		long result2 = HexSearchAndReplace.findFilePosition(hex.get(1), upk, tree);
		assertEquals(0x7913BB, result2);
	}

	/**
	 * Test of resizeAndReplace method, of class HexSearchAndReplace.
	 * This is a potentially destructive test. Test upk should manually be replaced after each test.
	 * It applies and then reverts with a resize, which should leave a backup and upk identical to the originals.
	 */
	@Test
	public void testResizeAndReplace() {
		System.out.println("resizeAndReplace");
		ModTab tab = new ModTab(new File("test/resources/testResize.upk_mod"));
		UpkFile upk = new UpkFile(new File("test/resources/XComGame_EW_patch1_test_resize.upk"));
		boolean expResult = true;
		boolean result = HexSearchAndReplace.resizeAndReplace(true, tab.getTree(), upk);
		assertEquals(expResult, result);
		boolean result2 = HexSearchAndReplace.resizeAndReplace(false, tab.getTree(), upk);
		assertEquals(expResult, result2);
	}
	
}
