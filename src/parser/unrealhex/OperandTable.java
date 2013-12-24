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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ui.Constants;

/**
 * Class holding global operand code strings.
 * @author Amineri, XMS
 */
// TODO: instead of storing an array strings that need to be decoded further store opcode data in map using opcode (int) as key and parsed Opcode token class as value (needs to be created)
public class OperandTable {
	
	/**
	 * Empty default constructor to prevent instantiation.
	 */
	private OperandTable() { }
	
	/**
	 * The list of operand names (for display only).
	 */
	private static String[] operandNames;
	
	/**
	 * The list of operand codes.
	 */
	private static String[] operandDecodes;

	/**
	 * Method used to lazily initialize the operand data.
	 */
	private static void initialize() {
		try {
			operandNames = new String[256];
			operandDecodes = new String[256];
			parseFile(Constants.OPERAND_DATA_FILE);
		} catch (IOException e) {
			System.err.println("Failed to read operand data.");
			e.printStackTrace();
			// reset variables
			operandNames = null;
			operandDecodes = null;
		}
	}

	/**
	 * Parses the specified operand data file and extract operand token data from it.
	 * @param file the operand data file
	 * @throws IOException if an I/O error occurs
	 */
	public static void parseFile(File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(";");
				if (!split[0].isEmpty()) {
					parseLine(line);
				}
			}
		}
	}
	
	/**
	 * Parses the specified string and stores its contents in the list of operand codes.
	 * @param line the operand code string to parse
	 */
	public static void parseLine(String line) {
		int iOpIndex = Integer.parseInt(line.split("\\s")[0], 16);
		if (operandDecodes[iOpIndex] == null) {
			operandDecodes[iOpIndex] = line.split(";")[0];
		} else {
			System.out.println("Duplicate opcode " + iOpIndex);
			System.out.println(operandDecodes[iOpIndex]);
			System.out.println(line);
			System.exit(1);
		}
		operandNames[iOpIndex] = line.split(";", 3)[1];
	}
	
	/**
	 * Returns the operand code string associated with the specified opcode.
	 * @param opcode the opcode
	 * @return the operand code string
	 */
	public static String getOperandString(String opcode) {
		if (operandNames == null) {
			initialize();
		}
		try {
			int i = Integer.parseInt(opcode, 16);
			if (i >= 0 && i < 256) {
				if (operandDecodes[i] != null) {
					return operandDecodes[i];
				} else {
					return "";
				}
			} else {
				return "";
			}
		} catch (NumberFormatException x) {
			return "";
		}
	}

	/**
	 * TODO: API
	 * @param opcode
	 * @return
	 */
	public static String getOperandName(String opcode) {
		if (operandNames == null) {
			initialize();
		}
		try {
			int i = Integer.parseInt(opcode, 16);
			if (i >= 0 && i < 256) {
				if (operandNames[i] != null) {
					return operandNames[i];
				} else {
					return "";
				}
			} else {
				return "";
			}
		} catch (NumberFormatException x) {
			return "";
		}
	}
	
}
