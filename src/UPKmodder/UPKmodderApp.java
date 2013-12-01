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

import io.parser.OperandTableParser;
import parser.unrealhex.ReferenceParser;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import model.modfile.ModFile;
import model.upk.UpkFile;

import model.modfile.ModFile;
import model.modfile.ModLine;
import parser.unrealhex.MemorySizeCalculator;

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
        
//        if(args.length == 0)
//        {
////            kConfigData.m_mod_filename_input = "C:/Games/sample_modfile_no_refs.txt";
////            kConfigData.m_mod_filename_output = "C:/Games/test_compressed_out.txt";
//        }
//        else
//        {
//            CommandLineArgHandler kCLHandler = new CommandLineArgHandler();
//            kConfigData = kCLHandler.Init(args, kConfigData);
//        }
        
//        UpkFileHandler kUpkFileHandler = new UpkFileHandler();
//        kUpkFileHandler.Init(kConfigData.m_sUpkConfig, kConfigData.m_bVerbose);
        
        
        OperandTableParser kOpParser = new OperandTableParser(Paths.get(kConfigData.m_sOperandData));
        ReferenceParser kRefParser = new ReferenceParser(kOpParser.parseFile());
        
        MemorySizeCalculator calc = new MemorySizeCalculator(kOpParser.parseFile());
        
        // reference parser test cases:
        if(kRefParser.parseString("07 DA 01 9B 38 3A 35 36 00 00 00 38 00 00 00 00 00 10 00 0F A0 00 00 35 3B 00 00 00 3C 00 00 00 00 00 01 AE 9F 00 00 38 3A 24 00 16 ").equals("07 DA 01 9B 38 3A 35 {{ 36 00 00 00 }} {{ 38 00 00 00 }} 00 00 10 00 {{ 0F A0 00 00 }} 35 {{ 3B 00 00 00 }} {{ 3C 00 00 00 }} 00 00 01 {{ AE 9F 00 00 }} 38 3A 24 00 16 "))
        {
            System.out.println("Reference Parser : Test 1 passed");
        }
        if(kRefParser.parseString("55 35 97 9F 00 00 98 9F 00 00 00 00 1A 00 10 A0 00 00 01 BF 9F 00 00 7D 00 1B 82 0D 00 00 00 00 00 00 35 36 00 00 00 38 00 00 00 00 00 10 00 0F A0 00 00 35 3B 00 00 00 3C 00 00 00 00 00 01 AE 9F 00 00 35 33 00 00 00 38 00 00 00 00 00 10 00 0F A0 00 00 35 3B 00 00 00 3C 00 00 00 00 00 01 AE 9F 00 00 16 16 ").equals("55 35 {{ 97 9F 00 00 }} {{ 98 9F 00 00 }} 00 00 1A 00 {{ 10 A0 00 00 }} 01 {{ BF 9F 00 00 }} 7D 00 1B << 82 0D 00 00 >> 00 00 00 00 35 {{ 36 00 00 00 }} {{ 38 00 00 00 }} 00 00 10 00 {{ 0F A0 00 00 }} 35 {{ 3B 00 00 00 }} {{ 3C 00 00 00 }} 00 00 01 {{ AE 9F 00 00 }} 35 {{ 33 00 00 00 }} {{ 38 00 00 00 }} 00 00 10 00 {{ 0F A0 00 00 }} 35 {{ 3B 00 00 00 }} {{ 3C 00 00 00 }} 00 00 01 {{ AE 9F 00 00 }} 16 16 "))
        {
            System.out.println("Reference Parser : Test 2 passed");
        }
        if(kRefParser.parseString("07 45 03 19 19 2E FE 2C 00 00 19 12 20 4F FE FF FF 0A 00 D8 F9 FF FF 00 1C F6 FB FF FF 16 09 00 98 F9 FF FF 00 01 98 F9 FF FF 09 00 F0 2C 00 00 00 01 F0 2C 00 00 01 00 F0 2C 00 00 00 28 ").equals("07 45 03 19 19 2E {{ FE 2C 00 00 }} 19 12 20 {{ 4F FE FF FF }} 0A 00 {{ D8 F9 FF FF }} 00 1C {{ F6 FB FF FF }} 16 09 00 {{ 98 F9 FF FF }} 00 01 {{ 98 F9 FF FF }} 09 00 {{ F0 2C 00 00 }} 00 01 {{ F0 2C 00 00 }} 01 00 {{ F0 2C 00 00 }} 00 28 "))
        {
            System.out.println("Reference Parser : Test 3 passed");
        }
//        UpkFile kUpkFile = new UpkFile(new File("C:/Games/XComGame_EU_patch4.upk"));
        
        ModFile myfile = new ModFile();

        String encoding = System.getProperty("file.encoding");
        try (Scanner s = new Scanner(Files.newBufferedReader(Paths.get("Larger_alien_pods_mod.upk_mod"), Charset.forName(encoding))))
        {
            while(s.hasNext())
            {
                myfile.addLine(s.nextLine());
            }
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
        System.out.println(myfile.getNumLines());
        for(int i = 0; i < myfile.getNumLines(); i++)
        {
            ModLine line = myfile.getLine(i);
            if(line != null)
            {
                if(line.isCode())
                {
                    System.out.print(String.format("%3s", i) + ":" + String.format("%4s",calc.parseString(line.asHex())) + ": ");
                }
                else
                {
                    System.out.print(String.format("%3s",i) + ":      ");
                }
//                    System.out.println(line.asHex());
                System.out.println(line.asString());
            }
            else
            {
                System.out.println("Null");
            }
        }
        
        for(int i = 0; i < myfile.getNumLines(); i++)
        {
            ModLine line = myfile.getLine(i);
            if(line != null)
            {
                System.out.print(String.format("%3s", i) + ":  ");
                if(line.isCode())
                {
                    for(int j=0;j<line.getIndentation();j++)
                        System.out.print("\t");
                    System.out.println(kRefParser.parseString(line.asHex()));
                }
                else
                {
                    System.out.println(line.asString());
                }
            }
            else
            {
                System.out.println("Null");
            }
        }

//        ModFile m_kModFile = new ModFile();
        
//        m_kModFile.setReferenceFinder(kRefParser);
//        m_kModFile.setUpkHandler(kUpkFileHandler);
                
//        if(m_kModFile.openReadFile(kConfigData.m_mod_filename_input, kConfigData.m_bVerbose))
//        {
//            m_kModFile.setReferenceChanges(kConfigData);
//
//            m_kModFile.readFile();
//            m_kModFile.closeReadFile();
//
//            if(kConfigData.m_bCompressedOutput)
//            {
//                if(m_kModFile.openWriteFile(kConfigData.m_mod_filename_output, kConfigData.m_bVerbose))
//                {            
//                    m_kModFile.writeCompressedHex(kConfigData.m_bVerbose);
//                    m_kModFile.closeWriteFile();
//                }
//            }
//            else if(kConfigData.m_bMirroredOutput)
//            {
//                if(m_kModFile.openWriteFile(kConfigData.m_mod_filename_output, kConfigData.m_bVerbose))
//                {
//                    m_kModFile.writeMirroredOutput(kConfigData.m_bVerbose);
//                    m_kModFile.closeWriteFile();
//                }
//            }
//            else if(kConfigData.m_bWriteToUpks)
//            {
//                m_kModFile.writeUpks(kConfigData.m_bVerbose);
//            }
//            else if(kConfigData.m_bRevertUpks)
//            {
//                m_kModFile.revertUpks(kConfigData.m_bVerbose);
//            }
//        }        
    }  
}
