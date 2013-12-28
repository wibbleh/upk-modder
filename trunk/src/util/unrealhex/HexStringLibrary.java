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
import java.util.List;

import javax.xml.bind.DatatypeConverter;

/**
 * Utility class for manipulating hexadecimal between various formats
 * @author Amineri
 */
public class HexStringLibrary {

	/**
	 * Converts an array of 4 bytes into an integer. LITTLE_ENDIAN
	 * @param b byte array of size 4
	 * @return integer value
	 */
	public static int convertByteArrayToInt(byte[] b) {
		if (b.length != 4) {
			throw new IllegalArgumentException("Byte array length needs to be 4.");
		}
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res += (b[i] & 0xFF) << (i * 8);
		}
		return res;
	}
	
	/**
	 * Converts the specified integer to a 4-element byte array using little endian order.
	 * @param i the integer to convert
	 * @return a 4-element byte array
	 */
	public static byte[] convertIntToByteArray(int i) {
		return onvertIntToByteArray(i, ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * Converts the specified integer to a 4-element byte array using the specified byte order.
	 * @param i the integer to convert
	 * @param bo the byte order to use
	 * @return a 4-element byte array
	 */
	private static byte[] onvertIntToByteArray(int i, ByteOrder bo) {
		return ByteBuffer.allocate(4).order(bo).putInt(i).array();
	}

	/**
	 * Converts a single integer value into a string of four hex bytes.
	 * LITTLE_ENDIAN
	 * @param i integer to convert
	 * @return
	 */
	public static String convertIntToHexString(int i) {
		return convertByteArrayToHexString(convertIntToByteArray(i));
    }
	
	/**
	 * Converts a variable length byte array into a hex string.
	 * @param bytes the byte array to convert
	 * @return the hex string
	 */
	public static String convertByteArrayToHexString(byte[] bytes) {
		return DatatypeConverter.printHexBinary(bytes).replaceAll("(.{2})", "$1 ");
	}

	/**
	 * Converts a list of Byte objects into a byte array.
	 * @param byteList the Byte list
	 * @return a byte array containing the values of the Byte list
	 */
	public static byte[] convertByteListToByteArray(List<Byte> byteList) {
		byte[] bytes = new byte[byteList.size()];
		for (int i = 0, len = byteList.size(); i < len; i++)
			bytes[i] = byteList.get(i).byteValue();
		return bytes;
	}
	
	/**
	 * Converts a provided hex string into a byte array.
	 * @param hex
	 * @return the byte array, or null if an invalid string
	 */
	public static byte[] convertStringToByteArray(String hex) {
		return DatatypeConverter.parseHexBinary(hex.replace(" ", ""));
	}
}
