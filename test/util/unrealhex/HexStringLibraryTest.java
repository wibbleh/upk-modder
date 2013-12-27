package util.unrealhex;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Amineri
 */


public class HexStringLibraryTest {
	
	public HexStringLibraryTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
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
	 * Test of convertIntToHexString method, of class HexStringLibrary.
	 */
	@Test
	public void testConvertIntToHexString() {
		System.out.println("convertIntToHexString");
		int I = 763456;
		String expResult = "40 A6 0B 00 ";
		String result = HexStringLibrary.convertIntToHexString(I);
		assertEquals(expResult, result);
	}

	/**
	 * Test of convertByteArrayToHexString method, of class HexStringLibrary.
	 */
	@Test
	public void testConvertByteArrayToHexString() {
		System.out.println("convertByteArrayToHexString");
		byte[] bytes = {27, 14, -36, 87};
		String expResult = "1B 0E DC 57 ";
		String result = HexStringLibrary.convertByteArrayToHexString(bytes);
		assertEquals(expResult, result);
	}
	
}
