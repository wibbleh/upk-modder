package model.upk;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Amineri
 */


public class UpkFileTest {
	
	public UpkFileTest() {
	}
	static Path file = Paths.get("test/resources/XComGame_EU_patch4.upk");
	
	static UpkFile upkFile;
	static UpkHeader header;
	
	@BeforeClass
	public static void setUpClass() {
		upkFile = new UpkFile(file);
		header = upkFile.getHeader();
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}


	/**
	 * Test of getRefName method, of class UpkFile.
	 */
	@Test
	public void testGetRefName() {
		System.out.println("getRefName");
		int ref = 32116;
		String result = upkFile.getRefName(ref);
		String expResult = "m_bForceReplicate@ExecutionReplicationInfo_XGAbility_BullRush@XGAbility_BullRush";
		assertEquals(expResult, result);
		ref = -433;
		result = upkFile.getRefName(ref);
		expResult = "Core:Engine@Engine";
		assertEquals(expResult, result);
	}

	/**
	 * Test of getVFRefName method, of class UpkFile.
	 */
	@Test
	public void testGetVFRefName() {
		System.out.println("getVFRefName");
		int ref = 29902;
		String expResult = "XGUnit";
		String result = upkFile.getVFRefName(ref);
		assertEquals(expResult, result);
		ref = -4;
		expResult = "";
		result = upkFile.getVFRefName(ref);
		assertEquals(expResult, result);
	}

	/**
	 * Test of findRefName method, of class UpkFile.
	 */
	@Test
	public void testFindRefName() {
		System.out.println("findRefName");
		String name = "Core:GRI@WorldInfo@Engine";
		int expResult = -1640;
		int result = upkFile.findRefByName(name);
		assertEquals(expResult, result);
	}

	/**
	 * Test of findVFRefName method, of class UpkFile.
	 */
	@Test
	public void testFindVFRefName() {
		System.out.println("findVFRefName");
		String name = "XGUnit";
		int expResult = 29902;
		int result = upkFile.findVFRefByName(name);
		assertEquals(expResult, result);
	}
	
}
