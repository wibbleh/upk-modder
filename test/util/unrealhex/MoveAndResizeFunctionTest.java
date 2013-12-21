package util.unrealhex;

import java.io.File;
import model.upk.UpkFile;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Amineri
 */


public class MoveAndResizeFunctionTest {
	
	public MoveAndResizeFunctionTest() {
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
	 * Test of moveAndResizeFunction method, of class MoveAndResizeFunction.
	 * Destructive test that modifies the target upk.
	 * Must manually check (using UE Explorer) that function has been increased in size.
	 * Then reset the upk back to the original using a backup.
	 */
	@Test
	public void testMoveAndResizeFunction() {
		System.out.println("moveAndResizeFunction");
		int bytesToAdd = 1024;
		String targetFunction = "GetCharacterBalanceMods@XGTacticalGameCore";
		UpkFile upk = new UpkFile(new File("XComGame_EW_patch1_test_increase.upk"));
		// test case should yield object entry position at 3366064
		boolean expResult = true;
		boolean result = MoveAndResizeFunction.moveAndResizeFunction(bytesToAdd, targetFunction, upk);
		assertEquals(expResult, result);
	}
	
}
