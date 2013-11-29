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
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author Amineri
 */
public class UpkFileHandler 
{
    private int m_iNumUpkTypes;
    
    String[] m_aBaseUpkNames;
    String[] m_aUpkOutputFiles;
    UpkComparison[] m_kUpkComparisons;
        
    Path m_kConfigFile;
    
    public UpkComparison getUpkComparison(String sSearchName)
    {
        for(int I = 0; I < m_iNumUpkTypes; I++)
        {
            if(sSearchName.equalsIgnoreCase(m_aBaseUpkNames[I]))
            {
                return m_kUpkComparisons[I];
            }
        }
        return null;
    }
    
    public String getUpkOutputFile(String sSearchName)
    {
        for(int I = 0; I < m_iNumUpkTypes; I++)
        {
            if(sSearchName.equalsIgnoreCase(m_aBaseUpkNames[I]))
            {
                return m_aUpkOutputFiles[I];
            }
        }
        return null;
    }
    
    public void Init(String sConfigFileName, boolean bVerbose) throws IOException
    {
        // Read the bytes with the proper encoding for this platform.  If
        // you skip this step, you might see something that looks like
        // Chinese characters when you expect Latin-style characters.
        String encoding = System.getProperty("file.encoding");
        String currLine;
        int iUpkCounter = -1;

        m_kConfigFile = Paths.get(sConfigFileName);
        // open file
        if(bVerbose && Files.exists(m_kConfigFile))
        {
            System.out.println("m_kConfigFile: " + sConfigFileName); 
        }
        try (Scanner kScanner = new Scanner(Files.newBufferedReader(m_kConfigFile, Charset.forName(encoding))))
        {
            while(kScanner.hasNextLine())
            {
                currLine = kScanner.nextLine();
                currLine = currLine.split("//")[0].trim();
                if(currLine.isEmpty())
                    continue;
                if(currLine.startsWith("NUM_UPKS="))
                {
                    String temp = currLine.split("=",2)[1];
                    m_iNumUpkTypes = Integer.parseInt(temp);
                    m_kUpkComparisons = new UpkComparison[m_iNumUpkTypes];
                    m_aBaseUpkNames = new String[m_iNumUpkTypes];
                    m_aUpkOutputFiles = new String[m_iNumUpkTypes]; 
                    if(bVerbose)
                    {
                        System.out.println("Attempting to read " + m_iNumUpkTypes + " upk files.");
                    }
                    for(int I = 0; I < m_iNumUpkTypes; I++)
                    {
                        m_kUpkComparisons[I] = new UpkComparison();
                    }
                }
                if(currLine.startsWith("UPKFILE="))
                {
                    iUpkCounter++;
                    m_aBaseUpkNames[iUpkCounter] = currLine.split("=",2)[1];
                    if(bVerbose)
                    {
                        System.out.println("Attempting to read " + m_aBaseUpkNames[iUpkCounter] + " files.");
                    }
                }
                if(currLine.startsWith("BEFORE="))
                {
                    Path kBeforeUpkFile = Paths.get(currLine.split("=",2)[1]);
                    if(bVerbose && Files.exists(kBeforeUpkFile))
                    {
                        System.out.println("Found file: " + currLine.split("=",2)[1]); 
                    }
                    m_kUpkComparisons[iUpkCounter].initBeforeUpk(kBeforeUpkFile, bVerbose);
                    if(bVerbose)
                    {
                        System.out.println("Read " + currLine.split("=",2)[1] + ". SUCCESS");
                    }

                }
                if(currLine.startsWith("AFTER="))
                {
                    Path kAfterUpkFile = Paths.get(currLine.split("=",2)[1]);
                    if(bVerbose && Files.exists(kAfterUpkFile))
                    {
                        System.out.println("Found file: " + currLine.split("=",2)[1]); 
                    }
                    m_kUpkComparisons[iUpkCounter].initAfterUpk(kAfterUpkFile, bVerbose);
                    if(bVerbose)
                    {
                        System.out.println("Read " + currLine.split("=",2)[1] + ". SUCCESS");
                        System.out.println();
                    }
                }
                if(currLine.startsWith("OUTPUT="))
                {
                    Path kOutputUpkFile = Paths.get(currLine.split("=",2)[1]);
                    if(bVerbose && Files.exists(kOutputUpkFile))
                    {
                        System.out.println("Found file: " + currLine.split("=",2)[1]); 
                    }
                    m_aUpkOutputFiles[iUpkCounter] = currLine.split("=",2)[1];
                    if(bVerbose)
                    {
                        System.out.println();
                    }
                }
            }
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
    }
}
