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

package UPKmodder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 *
 * @author Amineri
 */
public class OperandTable 
{
    String[] m_arrOperandDecodes = new String[256];
    
    /**
     * Constructor for Operand Table using the configuration file. 
     * @param file
     * @param bVerbose
     * @throws IOException
     */
    public OperandTable(Path file, boolean bVerbose) throws IOException
    {
        // Read the bytes with the proper encoding for this platform.  If
        // you skip this step, you might see something that looks like
        // Chinese characters when you expect Latin-style characters.
        String encoding = System.getProperty("file.encoding");
        int iOpCount = 0;

        try (Scanner kScanner = new Scanner(Files.newBufferedReader(file, Charset.forName(encoding))))
        {
            while(kScanner.hasNextLine())
            {
                String currLine = kScanner.nextLine().split("//")[0];
                if(currLine.isEmpty()) 
                {
                    continue;
                }
                int iOpIndex = Integer.parseInt(currLine.split("\\s")[0], 16);
                if(m_arrOperandDecodes[iOpIndex] == null)
                {
                    m_arrOperandDecodes[iOpIndex] = currLine;
                }
                else
                {
                    System.out.println("Duplicate opcode " + iOpIndex );
                    System.out.println(m_arrOperandDecodes[iOpIndex]);
                    System.out.println(currLine);
                    System.exit(1);
                }
                iOpCount++;
            }
            if(bVerbose)
                System.out.println("Read " + iOpCount + " opcodes.");
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
    }
    
    /**
     * Retrieves the decode string for the given operand
     * @param sOpCode
     * @return String
     */
    public String getOpString(String sOpCode)
    {
        return m_arrOperandDecodes[Integer.parseInt(sOpCode, 16)];
    }
}
