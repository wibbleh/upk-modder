package util.unrealhex;

import io.parser.OperandTableParser;
import io.upk.UpkFileLoader;
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import model.modtree.ModTree;
import model.modtree.ModTreeTest;
import model.upk.UpkFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Amineri
 */


public class ReferenceUpdateTest {
	
	private static UpkFileLoader upks;
	private ModTree tree;
	private DefaultStyledDocument document;
	
	private String upkSrcName = "XComGame.upk";
	private String srcGuid = "33 2E 29 6A A5 DD FC 40 B5 CC 57 A5 A7 AA 8C 41"; // EU patch 4
	
	private String upkDst2Name = "XComGame.upk";
	private String dst2Guid = "01 E9 EB 29 23 F4 DB 4F A8 2B 8E 46 A7 25 E5 D6"; // EU patch 5

	private String upkDst1Name = "XComGame.upk";
	private String dst1Guid = "B1 1A D8 E4 48 29 FC 43 8E C0 7A B0 A3 3E 34 9F"; // EW release

	public ReferenceUpdateTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
		// initialize upks for all classes
		System.out.println("Reading upks.");
		upks = new UpkFileLoader();

		// initialize Operand Table for all tests that use it
		System.out.println("Reading operand data.");
		OperandTableParser parser = new OperandTableParser(Paths.get("operand_data.ini"));
		try {
			parser.parseFile();
		} catch(IOException ex) {
			Logger.getLogger(ModTreeTest.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() throws BadLocationException {
		// initialize document from test file
		System.out.println("Read test_mod_v3.upk_mod");
		document = new DefaultStyledDocument();
		// arbitrary default AttributeSet
		AttributeSet as = new SimpleAttributeSet(); // TODO perform node-to-style mapping
		StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
		StyleConstants.setItalic((MutableAttributeSet) as, false);
		String encoding = System.getProperty("file.encoding");
		try(Scanner s = new Scanner(Files.newBufferedReader(Paths.get("test/resources/test_mod_v3.upk_mod"), Charset.forName(encoding)))) {
			while(s.hasNext()) {
				document.insertString(document.getLength(), s.nextLine() + "\n", as);
			}
		} catch(IOException x) {
			System.out.println("caught exception: " + x);
		}

		// initialize tree from document
		tree = new ModTree(document);
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of ReferenceUpdate constructor with three args.
	 */
	@Test
	public void testReferenceUpdate_three_args() {
		System.out.println("getReferenceUpdateConstructor_three_args");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid));
	}
	
	/**
	 * Test of ReferenceUpdate constructor with four args.
	 */
	@Test
	public void testReferenceUpdate_four_args() {
		System.out.println("getReferenceUpdateConstructor_four_args");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
	}

	/**
	 * Test of getFailureMode method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailureMode() {
		System.out.println("getFailureMode");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		int expResult = 0;
		int result = r.getFailureMode();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFailedMappings method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailedMappings() {
		System.out.println("getFailedMappings");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		List<Integer> expResult = new ArrayList<>();
		List<Integer> result = r.getFailedMappings();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFailedOffsets method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailedOffsets() {
		System.out.println("getFailedOffsets");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		List<Integer> expResult = new ArrayList<>();
		List<Integer> result = r.getFailedOffsets();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFailedTypes method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailedTypes() {
		System.out.println("getFailedTypes");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		List<Integer> expResult = new ArrayList<>();
		List<Integer> result = r.getFailedTypes();
		assertEquals(expResult, result);
	}

	/**
	 * Test of updateDocumentToName method, of class ReferenceUpdate.
	 */
	@Test
	public void testUpdateDocumentToName() {
		System.out.println("updateDocumentToName");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		boolean expResult = true;
		tree.disableUpdating();
		boolean result = r.updateDocumentToName();
		assertEquals(expResult, result);
	}

	/**
	 * Test of updateDocumentToValue method, of class ReferenceUpdate.
	 */
	@Test
	public void testUpdateDocumentToValue() {
		System.out.println("updateDocumentToValue");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		boolean expResult = true;
		tree.disableUpdating();
		boolean result = r.updateDocumentToValue();
		assertEquals(expResult, result);
	}

	/**
	 * Test of replaceGUID method, of class ReferenceUpdate.
	 */
	@Test
	public void testReplaceGUID() throws BadLocationException {
		System.out.println("replaceGUID");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		tree.disableUpdating();
		boolean result = r.replaceGUID();
		assertEquals(true, result);
		int offset = 45;
		int length = 47;
		String testGUID = document.getText(offset, length);
		assertEquals(dst1Guid, testGUID);
	}

	/**
	 * Test of verifySourceGUID method, of class ReferenceUpdate.
	 */
	@Test
	public void testVerifySourceGUID() {
		System.out.println("verifySourceGUID");
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid));
		boolean expResult = true;
		boolean result = r.verifySourceGUID();
		assertEquals(expResult, result);
	}

	/**
	 * Test of testUpdateDocumentToName method, of class ReferenceUpdate.
	 */
	@Test
	public void testTestUpdateDocumentToName() {
		System.out.println("testUpdateDocumentToName");
		boolean recordFailures = false;
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		boolean expResult = true;
		boolean result = r.testUpdateDocumentToName(recordFailures);
		assertEquals(expResult, result);
	}

	/**
	 * Test of testUpdateDocumentToValue method, of class ReferenceUpdate.
	 */
	@Test
	public void testTestUpdateDocumentToValue() {
		System.out.println("testUpdateDocumentToValue");
		boolean recordFailures = false;
		ReferenceUpdate r = new ReferenceUpdate(tree, document, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDst1Name, dst1Guid));
		boolean expResult = true;
		boolean result = r.testUpdateDocumentToValue(recordFailures);
		assertEquals(expResult, result);
	}
	
}
