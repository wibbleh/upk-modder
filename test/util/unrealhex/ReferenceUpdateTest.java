package util.unrealhex;

import static org.junit.Assert.assertEquals;
import io.upk.UpkFileLoader;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import model.modtree.ModTree;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


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
	
	private String upkDstName_p5 = "XComGame.upk";
	private String dstGuid_p5 = "01 E9 EB 29 23 F4 DB 4F A8 2B 8E 46 A7 25 E5 D6"; // EU patch 5

	private String upkDstName_ew = "XComGame.upk";
	private String dstGuid_ew = "B1 1A D8 E4 48 29 FC 43 8E C0 7A B0 A3 3E 34 9F"; // EW release

	public ReferenceUpdateTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
		// initialize upks for all classes
		System.out.println("Reading upks.");
		upks = new UpkFileLoader();

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
		// initialize document from test file
		System.out.println("Read test_mod_RefUpdate.upk_mod");
		document = new DefaultStyledDocument();
		// arbitrary default AttributeSet
		AttributeSet as = new SimpleAttributeSet(); 
		StyleConstants.setForeground((MutableAttributeSet) as, Color.BLACK);
		StyleConstants.setItalic((MutableAttributeSet) as, false);
		String encoding = System.getProperty("file.encoding");
		try(Scanner s = new Scanner(Files.newBufferedReader(Paths.get("test/resources/test_mod_RefUpdate.upk_mod"), Charset.forName(encoding)))) {
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
	public void testReferenceUpdate_two_args() {
		System.out.println("getReferenceUpdateConstructor_three_args");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid));
	}
	
	/**
	 * Test of ReferenceUpdate constructor with four args.
	 */
	@Test
	public void testReferenceUpdate_three_args() {
		System.out.println("getReferenceUpdateConstructor_four_args");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
	}

	/**
	 * Test of getFailureMode method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailureMode() {
		System.out.println("getFailureMode");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
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
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		List<Integer> expResult = new ArrayList<>();
		List<Integer> result = r.getFailedMappings();
		assertEquals(expResult, result);

		r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		expResult = new ArrayList<>();
		expResult.add(38046); // ReturnValue@HasFoundHistory removed in EW
		expResult.add(13946); // HasFoundHistory removed in EW
		expResult.add(38046); // ReturnValue@HasFoundHistory removed in EW
		expResult.add(13946); // HasFoundHistory removed in EW
		result = r.getFailedMappings();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getFailedOffsets method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailedOffsets() {
		System.out.println("getFailedOffsets");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		List<Integer> expResult = new ArrayList<>();
		List<Integer> result = r.getFailedOffsets();
		assertEquals(expResult, result);

		r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		expResult = new ArrayList<>();
		expResult.add(1459); // ReturnValue@HasFoundHistory removed in EW
		expResult.add(1477); // HasFoundHistory removed in EW
		expResult.add(2123); // ReturnValue@HasFoundHistory removed in EW
		expResult.add(2141); // HasFoundHistory removed in EW
		result = r.getFailedOffsets();
		assertEquals(expResult, result);
}

	/**
	 * Test of getFailedTypes method, of class ReferenceUpdate.
	 */
	@Test
	public void testGetFailedTypes() {
		System.out.println("getFailedTypes");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		List<Integer> expResult = new ArrayList<>();
		List<Integer> result = r.getFailedTypes();
		assertEquals(expResult, result);

	
		r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		expResult = new ArrayList<>();
		expResult.add(2); // ReturnValue@HasFoundHistory removed in EW
		expResult.add(2); // HasFoundHistory removed in EW
		expResult.add(2); // ReturnValue@HasFoundHistory removed in EW
		expResult.add(2); // HasFoundHistory removed in EW
		result = r.getFailedTypes();
		assertEquals(expResult, result);

	}

	/**
	 * Test of updateDocumentToName method, of class ReferenceUpdate with success
	 */
	@Test
	public void testUpdateDocumentToName_P5Success() throws BadLocationException {
		System.out.println("updateDocumentToName");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		boolean expResult = true;
		tree.disableUpdating();
		boolean result = r.updateDocumentToName();
		assertEquals(expResult, result);
		
		String expResult2 = "MODFILEVERSION=3\n" +
						"UPKFILE=XComGame.upk \n" +
						"GUID= 33 2E 29 6A A5 DD FC 40 B5 CC 57 A5 A7 AA 8C 41 // EU patch 4\n" +
						"FUNCTION=ApplyActionCost@XGAbilityTree\n" +
						"// Increase max pod size Mod\n" +
						"// Author: Amineri \n" +
						"[BEFORE_HEX]\n" +
						"[CODE]\n" +
						"// PlayerIndex = 0;   PlayerIndex@InitAlienLoadout@XGBattleDesc\n" +
						"0F 00 {|PlayerIndex@InitAlienLoadoutInfos@XGBattleDesc|} 25 \n" +
						"[/CODE]\n" +
						"[/BEFORE_HEX]\n" +
						"\n" +
						"// line with parse error -- incorrect trailing 01 value\n" +
						"[AFTER_HEX]\n" +
						"[CODE]\n" +
						"// PlayerIndex = 1;   PlayerIndex@InitAlienLoadout@XGBattleDesc\n" +
						"0F 00 10 A0 00 00 26 01\n" +
						"[/CODE]\n" +
						"[/AFTER_HEX]\n" +
						"\n" +
						"\n" +
						"[BEFORE_HEX]\n" +
						"[CODE] // parsable unrealhex -- corresponds to full lines of code -- allows operand decoding\n" +
						"//iCost = XComGameReplicationInfo(class'Engine'.static.GetCurrentWorldInfo().GRI).m_kGameCore.GetAmmoCost(kAbility.m_kWeapon.GameplayType(), kAbility.GetType(), kAbility.m_kUnit.GetPlayer().HasFoundryHistory(10), kAbility.m_kUnit.GetCharacter().m_kChar, kAbility.m_bReactionFire); (259 file, 379 virtual bytes -- 120 extra)\n" +
						"0F 00 {|iCost@ApplyActionCost@XGAbilityTree|} 19 19 2E {|XComGameReplicationInfo|} 19 12 20 {|Core:Engine@Engine|} 0A 00 {|Core:ReturnValue@GetCurrentWorldInfo@Engine@Engine|} 00 1C {|Core:GetCurrentWorldInfo@Engine@Engine|} 16 09 00 {|Core:GRI@WorldInfo@Engine|} 00 01 {|Core:GRI@WorldInfo@Engine|} 09 00 {|m_kGameCore@XComGameReplicationInfo|} 00 01 {|m_kGameCore@XComGameReplicationInfo|} 13 01 {|ReturnValue@GetAmmoCost@XGTacticalGameCoreNativeBase|} 00 1B <|GetAmmoCost|> 00 00 00 00 38 3A 19 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 09 00 {|m_kWeapon@XGAbility_Targeted|} 00 01 {|m_kWeapon@XGAbility_Targeted|} 0A 00 {|ReturnValue@GameplayType@XGItem|} 00 1B <|GameplayType|> 00 00 00 00 16 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 0A 00 {|ReturnValue@GetType@XGAbility|} 00 1B <|GetType|> 00 00 00 00 16 19 19 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 09 00 {|m_kUnit@XGAbility|} 00 01 {|m_kUnit@XGAbility|} 0A 00 {|ReturnValue@GetPlayer@XGUnit|} 00 1B <|GetPlayer|> 00 00 00 00 16 0C 00 {|ReturnValue@HasFoundryHistory@XGPlayer|} 00 1B <|HasFoundryHistory|> 00 00 00 00 24 0A 16 19 19 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 09 00 {|m_kUnit@XGAbility|} 00 01 {|m_kUnit@XGAbility|} 0A 00 {|ReturnValue@GetCharacter@XGUnit|} 00 1B <|GetCharacter|> 00 00 00 00 16 09 00 {|m_kChar@XGCharacter|} 00 01 {|m_kChar@XGCharacter|} 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 0A 00 {|m_bReactionFire@XGAbility|} 00 2D 01 {|m_bReactionFire@XGAbility|} 16 \n" +
						"[/CODE]\n" +
						"[/BEFORE_HEX]\n" +
						"\n" +
						"[AFTER_HEX]\n" +
						"[CODE]\n" +
						"// iCost = kAbility.GraduatedOdds(0, kAbility, kAbility.m_kUnit.GetPlayer().HasFoundryHistory(10)); (92 file, 128 virtual bytes -- 36 extra)\n" +
						"0F 00 {|iCost@ApplyActionCost@XGAbilityTree|} 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 61 00 {|iFactor@GraduatedOdds@XGAbility_Targeted|} 00 1B <|GraduatedOdds|> 00 00 00 00 2C 00 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 19 19 19 00 {|kAbility@ApplyActionCost@XGAbilityTree|} 09 00 {|m_kUnit@XGAbility|} 00 01 {|m_kUnit@XGAbility|} 0A 00 {|ReturnValue@GetPlayer@XGUnit|} 00 1B <|GetPlayer|> 00 00 00 00 16 0C 00 {|ReturnValue@HasFoundryHistory@XGPlayer|} 00 1B <|HasFoundryHistory|> 00 00 00 00 24 0A 16 16 \n" +
						"\n" +
						"// null-ops\n" +
						"0B 0B 0B 0B 0B 0B 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 00 {|iCost@ApplyActionCost@XGAbilityTree|} 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B \n" +
						"[/CODE]\n" +
						"[/AFTER_HEX]\n";
		System.out.println(document.getText(0, document.getLength()));
		assertEquals(expResult2, document.getText(0, document.getLength()));

	}

	/**
	 * Test of updateDocumentToName method, of class ReferenceUpdate.
	 */
	@Test
	public void testUpdateDocumentToName_EWSuccess() throws BadLocationException {
		System.out.println("updateDocumentToName");
		
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		boolean expResult = true;
		tree.disableUpdating();
		boolean result = r.updateDocumentToName();
		assertEquals(expResult, result);
	}

	/**
	 * Test of updateDocumentToValue method, of class ReferenceUpdat with expected success.
	 */
	@Test
	public void testUpdateDocumentToValue_P5Success() throws BadLocationException {
		System.out.println("updateDocumentToValue");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		boolean expResult = true;
		tree.disableUpdating();
		boolean result = r.updateDocumentToValue();
		assertEquals(expResult, result);
//		System.out.println(document.getText(0, document.getLength()));
		
		String expResult2 = "MODFILEVERSION=3\n" +
									"UPKFILE=XComGame.upk \n" +
									"GUID= 01 E9 EB 29 23 F4 DB 4F A8 2B 8E 46 A7 25 E5 D6 // EU patch 4\n" +
									"FUNCTION=ApplyActionCost@XGAbilityTree\n" +
									"// Increase max pod size Mod\n" +
									"// Author: Amineri \n" +
									"[BEFORE_HEX]\n" +
									"[CODE]\n" +
									"// PlayerIndex = 0;   PlayerIndex@InitAlienLoadout@XGBattleDesc\n" +
									"0F 00 20 A0 00 00 25 \n" +
									"[/CODE]\n" +
									"[/BEFORE_HEX]\n" +
									"\n" +
									"// line with parse error -- incorrect trailing 01 value\n" +
									"[AFTER_HEX]\n" +
									"[CODE]\n" +
									"// PlayerIndex = 1;   PlayerIndex@InitAlienLoadout@XGBattleDesc\n" +
									"0F 00 10 A0 00 00 26 01\n" +
									"[/CODE]\n" +
									"[/AFTER_HEX]\n" +
									"\n" +
									"\n" +
									"[BEFORE_HEX]\n" +
									"[CODE] // parsable unrealhex -- corresponds to full lines of code -- allows operand decoding\n" +
									"//iCost = XComGameReplicationInfo(class'Engine'.static.GetCurrentWorldInfo().GRI).m_kGameCore.GetAmmoCost(kAbility.m_kWeapon.GameplayType(), kAbility.GetType(), kAbility.m_kUnit.GetPlayer().HasFoundryHistory(10), kAbility.m_kUnit.GetCharacter().m_kChar, kAbility.m_bReactionFire); (259 file, 379 virtual bytes -- 120 extra)\n" +
									"0F 00 D0 7E 00 00 19 19 2E FE 2C 00 00 19 12 20 4F FE FF FF 0A 00 D8 F9 FF FF 00 1C F6 FB FF FF 16 09 00 98 F9 FF FF 00 01 98 F9 FF FF 09 00 F0 2C 00 00 00 01 F0 2C 00 00 13 01 42 10 00 00 00 1B 19 31 00 00 00 00 00 00 38 3A 19 19 00 D1 7E 00 00 09 00 F9 BB 00 00 00 01 F9 BB 00 00 0A 00 F8 9B 00 00 00 1B 95 30 00 00 00 00 00 00 16 19 00 D1 7E 00 00 0A 00 29 7C 00 00 00 1B 22 35 00 00 00 00 00 00 16 19 19 19 00 D1 7E 00 00 09 00 F3 7B 00 00 00 01 F3 7B 00 00 0A 00 FB B2 00 00 00 1B 0E 34 00 00 00 00 00 00 16 0C 00 AE 94 00 00 00 1B 7E 36 00 00 00 00 00 00 24 0A 16 19 19 19 00 D1 7E 00 00 09 00 F3 7B 00 00 00 01 F3 7B 00 00 0A 00 73 B4 00 00 00 1B 7F 31 00 00 00 00 00 00 16 09 00 D3 A2 00 00 00 01 D3 A2 00 00 19 00 D1 7E 00 00 0A 00 DF 7B 00 00 00 2D 01 DF 7B 00 00 16 \n" +
									"[/CODE]\n" +
									"[/BEFORE_HEX]\n" +
									"\n" +
									"[AFTER_HEX]\n" +
									"[CODE]\n" +
									"// iCost = kAbility.GraduatedOdds(0, kAbility, kAbility.m_kUnit.GetPlayer().HasFoundryHistory(10)); (92 file, 128 virtual bytes -- 36 extra)\n" +
									"0F 00 D0 7E 00 00 19 00 D1 7E 00 00 61 00 C6 7C 00 00 00 1B E5 35 00 00 00 00 00 00 2C 00 00 D1 7E 00 00 19 19 19 00 D1 7E 00 00 09 00 F3 7B 00 00 00 01 F3 7B 00 00 0A 00 FB B2 00 00 00 1B 0E 34 00 00 00 00 00 00 16 0C 00 AE 94 00 00 00 1B 7E 36 00 00 00 00 00 00 24 0A 16 16 \n" +
									"\n" +
									"// null-ops\n" +
									"0B 0B 0B 0B 0B 0B 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 00 D0 7E 00 00 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B 0B \n" +
									"[/CODE]\n" +
									"[/AFTER_HEX]\n";
		assertEquals(expResult2, document.getText(0, document.getLength()));
	}

	/**
	 * Test of updateDocumentToValue method, of class ReferenceUpdate with expected failure.
	 */
	@Test
	public void testUpdateDocumentToValue_EWFail() throws BadLocationException {
		System.out.println("updateDocumentToValue");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		boolean expResult = false;
		tree.disableUpdating();
		boolean result = r.updateDocumentToValue();
		assertEquals(expResult, result);
//		System.out.println(document.getText(0, document.getLength()));
	}

	/**
	 * Test of replaceGUID method, of class ReferenceUpdate.
	 */
	@Test
	public void testReplaceGUID() throws BadLocationException {
		System.out.println("replaceGUID");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		tree.disableUpdating();
		boolean result = r.replaceGUID();
		assertEquals(true, result);
		int offset = 45;
		int length = 47;
		String testGUID = document.getText(offset, length);
		assertEquals(dstGuid_ew, testGUID);
	}

	/**
	 * Test of verifySourceGUID method, of class ReferenceUpdate.
	 */
	@Test
	public void testVerifySourceGUID() {
		System.out.println("verifySourceGUID");
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid));
		boolean expResult = true;
		boolean result = r.verifySourceGUID();
		assertEquals(expResult, result);
	}

	/**
	 * Test of testUpdateDocumentToName method, of class ReferenceUpdate with expected success.
	 */
	@Test
	public void testTestUpdateDocumentToName_P5Success() {
		System.out.println("testUpdateDocumentToName");
		boolean recordFailures = false;
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		boolean expResult = true;
		boolean result = r.testUpdateDocumentToName(recordFailures);
		assertEquals(expResult, result);
	}

	/**
	 * Test of testUpdateDocumentToName method, of class ReferenceUpdate with expected success.
	 */
	@Test
	public void testTestUpdateDocumentToName_EWSuccess() {
		System.out.println("testUpdateDocumentToName");
		boolean recordFailures = false;
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		boolean expResult = true;
		boolean result = r.testUpdateDocumentToName(recordFailures);
		assertEquals(expResult, result);
	}

	/**
	 * Test of testUpdateDocumentToValue method, of class ReferenceUpdate with expected success.
	 */
	@Test
	public void testTestUpdateDocumentToValue_P5Success() {
		System.out.println("testUpdateDocumentToValue");
		boolean recordFailures = false;
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_p5, dstGuid_p5));
		boolean expResult = true;
		boolean result = r.testUpdateDocumentToValue(recordFailures);
		assertEquals(expResult, result);
	}
	
	/**
	 * Test of testUpdateDocumentToValue method, of class ReferenceUpdate with expected failure.
	 */
	@Test
	public void testTestUpdateDocumentToValue_EWFail() {
		System.out.println("testUpdateDocumentToValue");
		boolean recordFailures = true;
		ReferenceUpdate r = new ReferenceUpdate(tree, upks.getUpk(upkSrcName, srcGuid), upks.getUpk(upkDstName_ew, dstGuid_ew));
		boolean expResult = false;
		boolean result = r.testUpdateDocumentToValue(recordFailures);
		assertEquals(expResult, result);
	}
	
}
