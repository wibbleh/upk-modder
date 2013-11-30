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

import java.io.*;

import model.modfile.ModFile;

/**
 *
 * @author Amineri
 */



public class UPKmodderApp {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    
    int numNamelistEntries;
    int posNamelist;
    int numObjectlistEntries;
    int posObjectlist;
    String[] nameListStrings;
    
    public static void main(String[] args) throws IOException 
    {
        UpkConfigData kConfigData = new UpkConfigData();
        
        if(args.length == 0)
        {
//            kConfigData.m_mod_filename_input = "C:/Games/sample_modfile_no_refs.txt";
//            kConfigData.m_mod_filename_output = "C:/Games/test_compressed_out.txt";
        }
        else
        {
            CommandLineArgHandler kCLHandler = new CommandLineArgHandler();
            kConfigData = kCLHandler.Init(args, kConfigData);
        }
        
        UpkFileHandler kUpkFileHandler = new UpkFileHandler();
        kUpkFileHandler.Init(kConfigData.m_sUpkConfig, kConfigData.m_bVerbose);
        
        ReferenceFinder kRef = new ReferenceFinder();
        kRef.Init(kConfigData.m_sOperandData, kConfigData.m_bVerbose);
        
        ModFile m_kModFile = new ModFile();
        
        m_kModFile.setReferenceFinder(kRef);
        m_kModFile.setUpkHandler(kUpkFileHandler);
                
        if(m_kModFile.openReadFile(kConfigData.m_mod_filename_input, kConfigData.m_bVerbose))
        {
            m_kModFile.setReferenceChanges(kConfigData);

            m_kModFile.readFile();
            m_kModFile.closeReadFile();

            if(kConfigData.m_bCompressedOutput)
            {
                if(m_kModFile.openWriteFile(kConfigData.m_mod_filename_output, kConfigData.m_bVerbose))
                {            
                    m_kModFile.writeCompressedHex(kConfigData.m_bVerbose);
                    m_kModFile.closeWriteFile();
                }
            }
            else if(kConfigData.m_bMirroredOutput)
            {
                if(m_kModFile.openWriteFile(kConfigData.m_mod_filename_output, kConfigData.m_bVerbose))
                {
                    m_kModFile.writeMirroredOutput(kConfigData.m_bVerbose);
                    m_kModFile.closeWriteFile();
                }
            }
            else if(kConfigData.m_bWriteToUpks)
            {
                m_kModFile.writeUpks(kConfigData.m_bVerbose);
            }
            else if(kConfigData.m_bRevertUpks)
            {
                m_kModFile.revertUpks(kConfigData.m_bVerbose);
            }
        }        
    }  
}
