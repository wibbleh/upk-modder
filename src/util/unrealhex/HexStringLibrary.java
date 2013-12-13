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

	public static int byteArrayToInt(byte[] b) {
		return ((b[3]&0xff)<<24)+((b[2]&0xff)<<16)+((b[1]&0xff)<<8)+(b[0]&0xff);
	}

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
	
	public static String convertByteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for(byte b: bytes) {
		   sb.append(String.format("%02x ", b&0xff));
		}
		return sb.toString().toUpperCase();
	}

    public static byte[] convertIntArrayListToByteArray(ArrayList<Integer> list)
    {
        byte[] bytes = new byte[list.size()];
        for(int i=0, len = list.size(); i < len; i++)
           bytes[i] = (byte) (list.get(i) & 0xFF);
        return bytes;
    }
}
