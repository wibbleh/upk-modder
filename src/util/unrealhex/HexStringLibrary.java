/*
 * Copyright (C) 2013 Rachel Norman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package util.unrealhex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Utility class for manipulating hexadecimal between various formats
 * @author Amineri
 */
public class HexStringLibrary 
{

	/**
	 * Converts an array of 4 bytes into an integer. LITTLE_ENDIAN
	 * @param b byte array of size 4
	 * @return integer value
	 */
	public static int byteArrayToInt(byte[] b) {
		return ((b[3]&0xff)<<24)+((b[2]&0xff)<<16)+((b[1]&0xff)<<8)+(b[0]&0xff);
	}

	/**
	 * Converts a single integer value into a string of four hex bytes.
	 * LITTLE_ENDIAN
	 * @param I integer to convert
	 * @return
	 */
	public static String convertIntToHexString(int I)
    {
        String sOutString = "";
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.LITTLE_ENDIAN); 
        b.putInt(I);

        byte[] result = b.array();
        for(int J = 0; J < 4 ; J++)
        {
            int temp = result[J] & 0xFF;
            sOutString = sOutString + String.format("%2s", Integer.toHexString(temp)).replace(' ', '0').toUpperCase()+ " " ;
        }
        return sOutString;
    }
	
	/**
	 * Converts a variable length byte array into a hex string.
	 * @param bytes
	 * @return
	 */
	public static String convertByteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for(byte b: bytes) {
		   sb.append(String.format("%02x ", b&0xff));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * Converts an array of Integers (each holding a single byte value) into a byte array.
	 * @param list
	 * @return
	 */
	public static byte[] convertIntArrayListToByteArray(ArrayList<Integer> list)
    {
        byte[] bytes = new byte[list.size()];
        for(int i=0, len = list.size(); i < len; i++)
           bytes[i] = (byte) (list.get(i) & 0xFF);
        return bytes;
    }
	
	/**
	 * Converts a provided hex string into a byte array.
	 * @param hex
	 * @return the byte array, or null if an invalid string
	 */
	public static byte[] convertStringToByteArray(String hex) {
		String[] tokens = hex.split("\\s+");
		byte[] returnArr = new byte[tokens.length];
		int count = 0;
		for(String token : tokens) {
			if(token.matches("[0-9A-Fa-f][0-9A-Fa-f]")) {
				returnArr[count] = (byte) (Integer.parseInt(token, 16));
			} else {
				return null;
			}
			
		}
		return returnArr;
	}
}
