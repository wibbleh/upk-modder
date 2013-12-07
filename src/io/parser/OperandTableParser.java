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

package io.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import parser.unrealhex.OperandTable;

/**
 * 
 * @author Amineri
 */
public class OperandTableParser {
	Path m_kOpTableFile;

	/**
	 * Constructor for Operand Table using the configuration file.
	 * 
	 * @param file
	 * @param bVerbose
	 * @throws IOException
	 */
	public OperandTableParser(Path file) {
		m_kOpTableFile = file;
	}

	public void parseFile() throws IOException {
		// Read the bytes with the proper encoding for this platform. If
		// you skip this step, you might see something that looks like
		// Chinese characters when you expect Latin-style characters.
		String encoding = System.getProperty("file.encoding");

		try (Scanner kScanner = new Scanner(Files.newBufferedReader(
				m_kOpTableFile, Charset.forName(encoding)))) {
			while (kScanner.hasNextLine()) {
				String currLine = kScanner.nextLine().split(";")[0];
				if (currLine.isEmpty()) {
					continue;
				}
				OperandTable.parseData(currLine);
			}
		} catch (IOException x) {
			System.out.println("caught exception: " + x);
		}
	}
}
