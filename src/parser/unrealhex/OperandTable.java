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

package parser.unrealhex;

/**
 * Class holding global operand code strings.
 * @author Amineri, XMS
 */
// TODO: instead of storing an array strings that need to be decoded further store opcode data in map using opcode (int) as key and parsed Opcode token class as value (needs to be created)
public class OperandTable {
	
	/**
	 * The list of operand codes.
	 */
	private static final String[] m_arrOperandDecodes = new String[256];

	/**
	 * Parses the specified string and stores its contents in the list of operand codes.
	 * @param data the operand code string to parse
	 */
	public static void parseData(String data) {
		int iOpIndex = Integer.parseInt(data.split("\\s")[0], 16);
		if (m_arrOperandDecodes[iOpIndex] == null) {
			m_arrOperandDecodes[iOpIndex] = data;
		} else {
			System.out.println("Duplicate opcode " + iOpIndex);
			System.out.println(m_arrOperandDecodes[iOpIndex]);
			System.out.println(data);
			System.exit(1);
		}
	}

	/**
	 * Returns the operand code string associated with the specified opcode.
	 * @param opcode the opcode
	 * @return the operand code string
	 */
	public static String getOperandString(String opcode) {
		return m_arrOperandDecodes[Integer.parseInt(opcode, 16)];
	}

}
