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
	private static String[] operandDecodes = new String[256];
	
	
	/**
	 * The list of operand names (for display only).
	 */
	private static String[] operandNames = new String[256];

	/**
	 * Display initialization flag
	 */
	private static boolean initialized = false;
	
	/**
	 * Reinitializes the operand table.
	 */
	public static void reinit() {
		if (!initialized) {
			operandDecodes = new String[256];
			operandNames = new String[256];
		}
	}

	/**
	 * Sets operand table as initialized.
	 */
	public static void setInitialized() {
		initialized = true;
	}
	
	/**
	 * Parses the specified string and stores its contents in the list of operand codes.
	 * @param data the operand code string to parse
	 */
	public static void parseData(String data) {
//		if(operandDecodes[0]!= null)  // TODO : handle re-initialization of the operand table -- this doesn't work
//				return;
		int iOpIndex = Integer.parseInt(data.split("\\s")[0], 16);
		if (operandDecodes[iOpIndex] == null) {
			operandDecodes[iOpIndex] = data.split(";")[0];
		} else {
			System.out.println("Duplicate opcode " + iOpIndex);
			System.out.println(operandDecodes[iOpIndex]);
			System.out.println(data);
			System.exit(1);
		}
		operandNames[iOpIndex] = data.split(";",3)[1];
	}
	
	/**
	 * Returns the operand code string associated with the specified opcode.
	 * @param opcode the opcode
	 * @return the operand code string
	 */
	public static String getOperandString(String opcode) {
		try
		{
			int i = Integer.parseInt(opcode, 16);
			if(i >= 0 && i < 256) {
				if(operandDecodes[i] != null) {
					return operandDecodes[i];
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
		catch (NumberFormatException x)
		{
			return "";
		}
	}

	
	public static String getOperandName(String opcode) {
		try
		{
			int i = Integer.parseInt(opcode, 16);
			if(i >= 0 && i < 256) {
				if(operandNames[i] != null) {
					return operandNames[i];
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
		catch (NumberFormatException x)
		{
			return "";
		}
	}
	
}
