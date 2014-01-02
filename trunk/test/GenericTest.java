import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import junit.framework.TestCase;

import org.junit.Test;


public class GenericTest extends TestCase {

	@Test
	public void testStuff() {
		int testInt = -0x0A;
		String testStr = "-0x0A";
		
		assertEquals(-10, testInt);
		assertEquals(-10, Integer.decode(testStr).intValue());
	}
	
	@Test
	public void testMoreStuff() {
		int testInt = 703710;
		
		byte[] bytes = ByteBuffer.allocate(4).putInt(testInt).array();
		
		String str = String.format("%02X %02X %02X %02X ", bytes[3], bytes[2], bytes[1], bytes[0]);

		System.out.println(DatatypeConverter.printHexBinary(bytes));
		System.out.println(DatatypeConverter.printHexBinary(bytes).replaceAll("(.{2})", "$1 "));
		
		assertEquals("DE BC 0A 00 ", str);
		
		byte[] bytes2 = DatatypeConverter.parseHexBinary(str.replace(" ", ""));
		
		System.out.println(Arrays.toString(bytes2));
	}
	
}
