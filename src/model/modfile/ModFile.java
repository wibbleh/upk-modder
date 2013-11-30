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

package model.modfile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

import UPKmodder.ReferenceFinder;
import UPKmodder.UpkComparison;
import UPKmodder.UpkConfigData;
import UPKmodder.UpkFileHandler;
import UPKmodder.UpkObjectList;

/**
 *
 * @author Amineri
 */

public class ModFile 
{
    ReferenceFinder m_kRef;
    
    // input file variables
    private int m_iFilePosition_in;
    private int m_iFileSize_in;
    private Path m_kModFile_in;
    private int m_iCurrLineNum_in = 0;
    private ArrayList<String> m_kInputFileStrings = new ArrayList<>(50000);
    
    private Scanner m_kScanner = null;
    private String m_sLastToken;
    private String m_sCurrToken;

    // output file variables
    private int m_iFilePosition_out;
    private int m_iFileSize_out;
    private Path m_kModFile_out;
    private int m_iCurrLineNum_out = 0;
    private BufferedWriter m_kWriter = null;

    private String m_sCurrUpkContext;
    private String m_sCurrFunctionContext;
    private ArrayList<String> m_alUpkContexts = new ArrayList<>(1000);
    private ArrayList<String> m_alFunctionContexts = new ArrayList<>(1000);

    private boolean m_bHexBeforeContext = false;
    private int m_iCurrBeforeHex = 0;
    private ArrayList<Integer> m_alCurrBeforeHex; // = new ArrayList<>(10000);
    private ArrayList<ArrayList<Integer>> m_alBeforeHexCollection = new ArrayList<>(1000);

    
    private boolean m_bHexAfterContext=false;
    private int m_iCurrAfterHex = 0;
    private ArrayList<Integer> m_alCurrAfterHex; // = new ArrayList<>(10000);
    private ArrayList<ArrayList<Integer>> m_alAfterHexCollection = new ArrayList<>(1000);

    private boolean m_bReferenceContext=false;
    private boolean m_bFunctionReferenceContext = false;
    private boolean m_bHeaderContext = false;
    
    private boolean m_bCommentContext=false;
    
    private boolean m_bUpdateReferences = false;
    private int m_iCurrReplacementReferenceIndex = 0;
    private int m_arrCurrReplaceReference[] = new int[4];

    private boolean m_bChangeToNamedReferences = false;
    private boolean m_bChangeToHexReferences = false; 
    private boolean m_bMarkReferences = false;
    private boolean m_bStripReferences = false;

    private UpkFileHandler m_kUpkFileHandler;
    private UpkComparison m_kUpkComparison;
    
    public void setReferenceFinder(ReferenceFinder kRef)
    {
        m_kRef = kRef;
    }
    
    private void resetContexts()
    {
        m_sCurrUpkContext = "";
        m_sCurrFunctionContext = "";
        m_bHexBeforeContext = false;
        m_bHexAfterContext = false;
        m_bReferenceContext = false;
        m_bFunctionReferenceContext = false;
        m_bCommentContext=false;
        m_iCurrBeforeHex = 0;
        m_iCurrAfterHex = 0;
        m_iCurrReplacementReferenceIndex = 0;
    }
    
    public boolean openReadFile (String sFileName, boolean bVerbose) throws IOException
    {
        // Read the bytes with the proper encoding for this platform.  If
        // you skip this step, you might see something that looks like
        // Chinese characters when you expect Latin-style characters.
        String encoding = System.getProperty("file.encoding");

        resetContexts();
        Path mod_file_input = Paths.get(sFileName);
        // open file
        if(Files.exists(mod_file_input))
        {
            if(bVerbose)
            {
                System.out.println("Found file: " + sFileName); 
            }
        }
        else
        {
            System.out.println("File not found: " + sFileName);
            return false;
        }
        
        m_kModFile_in = mod_file_input;
        try 
        {
            m_kScanner = new Scanner(Files.newBufferedReader(m_kModFile_in, Charset.forName(encoding)));
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
            return false;
        }
        return true;
    }
    
    public void closeReadFile() throws IOException
    {
        if(m_kScanner != null)
            m_kScanner.close();
    }
    
    public boolean openWriteFile(String sFileName, boolean bVerbose) throws IOException
    {
        m_iFilePosition_out = 0;
        m_iFileSize_out = 0;
        m_iCurrLineNum_out = 0;
        Charset charset = Charset.forName("US-ASCII");
        Path mod_file_output = Paths.get(sFileName);
        // open file
        if(bVerbose && Files.exists(mod_file_output))
        {
            System.out.println("Found file: " + sFileName); 
        }
        m_kModFile_out = mod_file_output;
        try
        {
            m_kWriter = Files.newBufferedWriter(mod_file_output, charset);
        }
        catch (IOException x) 
        {
            System.err.format("IOException: %s%n", x);
            return false;
        }
        return true;
    }
    
    public void closeWriteFile() throws IOException
    {
        if(m_kWriter != null)
        {
           m_kWriter.close();
        }
    }
    
    public void setUpkHandler(UpkFileHandler kUpkHandler)
    {
        m_kUpkFileHandler = kUpkHandler;
    }
    
    public void setReferenceChanges(UpkConfigData kConfig)
    {
        m_bChangeToNamedReferences = kConfig.m_bRefsHexToString;
        m_bChangeToHexReferences = kConfig.m_bRefsStringToHex; 
        m_bMarkReferences = kConfig.m_bRefsTag;
        m_bStripReferences = kConfig.m_bRefsUntag;
        m_bUpdateReferences = kConfig.m_bUpdateReferences;
    }
    
    public boolean writeMirroredOutput(boolean bVerbose) throws IOException, ArrayIndexOutOfBoundsException
    {
        String sCommentLine;

        resetContexts();
        int I_before = 0, I_after = 0, J_before = 0, J_after = 0;
        m_iCurrLineNum_in = 0;
        for(String currLine : m_kInputFileStrings)
        {
            m_sCurrToken = "";
            
            m_iCurrLineNum_in++;
            if(currLine.isEmpty()) 
            {
                m_kWriter.newLine();
                if(bVerbose)
                    System.out.println();
                continue;
            }
            if(currLine.contains("//"))
            {
                sCommentLine = currLine.split("//", 2)[1];
                currLine = currLine.split("//")[0];
            }
            else
            {
                sCommentLine = "";
            }
            for (char c : currLine.toCharArray()) 
            {
                if (c == '\t') 
                {
                    m_kWriter.write("\t");
                    if(bVerbose)
                        System.out.print("\t");
                }
                else
                    break;
            }
            currLine = currLine.trim() + " ";
            if(currLine.equalsIgnoreCase("[/BEFORE_HEX] "))
            {
                I_before++;
                J_before = 0;
            }
            if(currLine.equalsIgnoreCase("[/AFTER_HEX] "))
            {
                I_after++;
                J_after = 0;
            }
            if(!m_bHeaderContext && m_bMarkReferences && (m_bHexBeforeContext || m_bHexAfterContext))
            {
                boolean bHexOnly = true;
                for(String token : currLine.split("\\s"))
                {
                    if(!token.matches("[0-9A-Fa-f][0-9A-Fa-f]"))
                    {
                        bHexOnly = false;
                        break;
                    }
                }
                if(bHexOnly)
                {
                    try
                    {
                        m_kRef.setString(currLine);
                        m_kRef.parseString();
                        currLine = m_kRef.getString();
                    }
                    catch (ArrayIndexOutOfBoundsException x)
                    {
                        System.out.println("Invalid line at : " + m_iCurrLineNum_in);
                    }
                }
            }
            String[] tokens = currLine.split("\\s"); 
            for (String token : tokens) 
            {
                m_sLastToken = m_sCurrToken;
                m_sCurrToken = token;
                if(token.isEmpty())
                    continue;

                updateContext(token);

                // handle converting hex references to strings
                if((m_bHexBeforeContext || m_bHexAfterContext) && m_bReferenceContext && m_bChangeToNamedReferences&& !(token.equals("{{") || token.equals("}}")))
                {
                    m_arrCurrReplaceReference[m_iCurrReplacementReferenceIndex++] = Integer.parseInt(token, 16);

                    if(m_iCurrReplacementReferenceIndex == 4)
                    {
                        int iOldReference = 0;
                        for(int J = 0; J < 4; J++)
                        {
                            iOldReference += m_arrCurrReplaceReference[J] << (8*J);
                        }
                        String sReferenceString =
                        		(m_bFunctionReferenceContext) ? m_kUpkComparison.getNameList(true).getNamelistEntry(iOldReference) :
                        			m_kUpkComparison.getObjectList(true).getObjectlistName(iOldReference);
                        m_kWriter.write("||" + sReferenceString + "|| ");
                        if(bVerbose)
                            System.out.print("||" + sReferenceString + "|| ");
                        if(m_bHexBeforeContext)
                        {
                            J_before+=4;
                        }
                        else if(m_bHexAfterContext)
                        {
                            J_after+=4;
                        }
                    }
                    continue;
                }

                if(token.matches("[0-9A-F][0-9A-F]"))
                {
                    try
                    {
                        if(m_bHexBeforeContext)
                        {
                            m_kWriter.write(String.format("%2s", Integer.toHexString(m_alBeforeHexCollection.get(I_before).get(J_before))).replace(' ', '0').toUpperCase() + " ");
                            if(bVerbose)
                                 System.out.print(String.format("%2s", Integer.toHexString(m_alBeforeHexCollection.get(I_before).get(J_before))).replace(' ', '0').toUpperCase() + " ");
                            J_before++;
                        }
                        else if(m_bHexAfterContext)
                        {
                            m_kWriter.write(String.format("%2s", Integer.toHexString(m_alAfterHexCollection.get(I_after).get(J_after))).replace(' ', '0').toUpperCase() + " ");
                            if(bVerbose)
                                System.out.print(String.format("%2s", Integer.toHexString(m_alAfterHexCollection.get(I_after).get(J_after))).replace(' ', '0').toUpperCase() + " ");
                            J_after++;
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException x)
                    {
                        System.out.println("Exception " + x + " at line: " + m_iCurrLineNum_in);
                    }
                }
                else
                {
                    if(token.startsWith("||"))
                    {
                        if(m_bChangeToHexReferences)
                        {
                            m_kWriter.write("{{ ");
                            for(int iCount = 0; iCount < 4; iCount++)
                            {
                                if(m_bHexBeforeContext)
                                {
                                    m_kWriter.write(String.format("%2s", Integer.toHexString(m_alBeforeHexCollection.get(I_before).get(J_before))).replace(' ', '0').toUpperCase() + " ");
                                    if(bVerbose)
                                        System.out.print(String.format("%2s", Integer.toHexString(m_alBeforeHexCollection.get(I_after).get(J_before))).replace(' ', '0').toUpperCase() + " ");
                                    J_before++;
                                }
                                else if(m_bHexAfterContext)
                                {
                                    m_kWriter.write(String.format("%2s", Integer.toHexString(m_alAfterHexCollection.get(I_after).get(J_after))).replace(' ', '0').toUpperCase() + " ");
                                    if(bVerbose)
                                        System.out.print(String.format("%2s", Integer.toHexString(m_alAfterHexCollection.get(I_after).get(J_after))).replace(' ', '0').toUpperCase() + " ");
                                    J_after++;
                                }
                            }
                            m_kWriter.write("}} ");
                        }
                        else
                        {
                            m_kWriter.write(token + " ");
                            if(bVerbose)
                                System.out.print(token + " ");
                            if(m_bHexBeforeContext)
                            {
                                J_before+=4;
                            }
                            else if(m_bHexAfterContext)
                            {
                                J_after+=4;
                            }
                        }
                    }
                    else
                    {
                        if((m_bChangeToNamedReferences || m_bStripReferences) && (token.equals("{{") || token.equals("}}"))){
                        }else
                        {
                            m_kWriter.write(token + " ");
                            if(bVerbose)
                                System.out.print(token + " ");
                        }
                    }
                }
//                if(bChangeToNamedReferences && (token.equals("{{") || token.equals("}}"))){
//                }else
//                {
//                    m_kWriter.write(" ");
////                    System.out.print(" ");
//                }
            }
            if(!sCommentLine.isEmpty())
            {
                m_kWriter.write("//" + sCommentLine);
                if(bVerbose)
                    System.out.print("//" + sCommentLine);
            }
            m_kWriter.newLine();
            if(bVerbose)
                System.out.println();
           
        }
        return true;
    }
    
    public boolean writeCompressedHex(boolean bVerbose) throws IOException
    {
        int J;
        int I = 0;

        while(I < m_alBeforeHexCollection.size())
        {
            m_kWriter.write("UPKFILE=" + m_alUpkContexts.get(I));
            m_kWriter.newLine();
            if(bVerbose)
            {
                System.out.println("UPKFILE=" + m_alUpkContexts.get(I));
            }
            m_kWriter.write("FUNCTION=" + m_alFunctionContexts.get(I));
            m_kWriter.newLine();
            if(bVerbose)
            {
                System.out.println("FUNCTION=" + m_alFunctionContexts.get(I));
            }
            m_kWriter.write("[BEFORE_HEX]");
            m_kWriter.newLine();
            if(bVerbose)
            {
                System.out.println("[BEFORE_HEX]");
            }
            J = 0;
            while (J < m_alBeforeHexCollection.get(I).size())
            {
                m_kWriter.write(String.format("%2s", Integer.toHexString(m_alBeforeHexCollection.get(I).get(J))).replace(' ', '0').toUpperCase() + " ");
                if(bVerbose)
                {
                    System.out.print(String.format("%2s", Integer.toHexString(m_alBeforeHexCollection.get(I).get(J))).replace(' ', '0').toUpperCase() + " ");
                }
                J++;
            }
            m_kWriter.newLine();
            m_kWriter.write("[/BEFORE_HEX]"); 
            m_kWriter.newLine();
            m_kWriter.newLine();
            if(bVerbose)
            {
                System.out.println();
                System.out.println("[/BEFORE_HEX]");
                System.out.println();
            }

            m_kWriter.write("[AFTER_HEX]"); 
            m_kWriter.newLine();
            if(bVerbose)
            {
                System.out.println("[AFTER_HEX]");
            }
            J = 0;
            while (J < m_alAfterHexCollection.get(I).size())
            {
                m_kWriter.write(String.format("%2s", Integer.toHexString(m_alAfterHexCollection.get(I).get(J))).replace(' ', '0').toUpperCase() + " ");
                if(bVerbose)
                {
                    System.out.print(String.format("%2s", Integer.toHexString(m_alAfterHexCollection.get(I).get(J))).replace(' ', '0').toUpperCase() + " ");
                }
                J++;
            }
            m_kWriter.newLine();
            m_kWriter.write("[/AFTER_HEX]");
            m_kWriter.newLine();
            m_kWriter.newLine();
            if(bVerbose)
            {
                System.out.println();
                System.out.println("[/AFTER_HEX]");
                System.out.println();
            }
            I++;
        }
        return true;
    }

    private boolean updateContext(String token)
    {
        if(m_bCommentContext)
        {
            if(token.equals("*/"))
            {
                m_bCommentContext = false;
            }
            return true;
        }
        else
        {
            if(token.equals("/*"))
            {
                m_bCommentContext = true;
                return true;
            }
        }
        if(token.toUpperCase().startsWith("UPKFILE"))
        {
            m_sCurrUpkContext = token.split("=")[1];
            m_kUpkComparison = m_kUpkFileHandler.getUpkComparison(m_sCurrUpkContext);
            return true;
        }
        if(token.toUpperCase().startsWith("FUNCTION"))
        {
            m_sCurrFunctionContext = token.split("=")[1];
            return true;
        }
        if(token.equalsIgnoreCase("[HEADER]"))
        {
            if(m_bHexBeforeContext || m_bHexAfterContext)
            {
                m_bHeaderContext = true;
            }
            else
            {
                System.out.println("Invalid [HEADER] tag location at line: " + m_iCurrLineNum_in);
            }
            return true;
        }
        if(token.equalsIgnoreCase("[/HEADER]"))
        {
            if(m_bHexBeforeContext || m_bHexAfterContext)
            {
                if(m_bHeaderContext)
                {
                    m_bHeaderContext = false;
                }
                else
                {
                    System.out.println("Invalid [/HEADER] tag location at line: " + m_iCurrLineNum_in);
                }
            }
            else
            {
                System.out.println("Invalid [/HEADER] tag location at line: " + m_iCurrLineNum_in);
            }
            return true;
        }

        if(token.equalsIgnoreCase("[BEFORE_HEX]"))
        {
            if(m_bHexBeforeContext || m_bHexAfterContext)
            {
                System.out.println("Incorrect [BEFORE_HEX] at line:" + m_iCurrLineNum_in);
            }
            m_bHexBeforeContext = true;
            m_alCurrBeforeHex = new ArrayList<>(10000);
            return true;
        }
        if(token.equalsIgnoreCase("[/BEFORE_HEX]"))
        {
            if(!m_bHexBeforeContext)
            {
                System.out.println("Incorrect [/BEFORE_HEX] at line:" + m_iCurrLineNum_in);
            }
            m_bHexBeforeContext = false;
            m_alBeforeHexCollection.add(m_alCurrBeforeHex);
            m_iCurrBeforeHex++;
//                m_alCurrBeforeHex.clear();
            return true;
        }
        if(token.equalsIgnoreCase("[AFTER_HEX]"))
        {
            if(m_bHexBeforeContext || m_bHexAfterContext)
            {
                System.out.println("Incorrect [AFTER_HEX] at line:" + m_iCurrLineNum_in);
            }
            m_bHexAfterContext = true;
            m_alCurrAfterHex = new ArrayList<>(10000);
            return true;
        }
        if(token.equalsIgnoreCase("[/AFTER_HEX]"))
        {
            if(!m_bHexAfterContext)
            {
                System.out.println("Incorrect [/AFTER_HEX] at line:" + m_iCurrLineNum_in);
            }
            m_bHexAfterContext = false;
            m_alAfterHexCollection.add(m_alCurrAfterHex);
            m_alUpkContexts.add(m_sCurrUpkContext);
            m_alFunctionContexts.add(m_sCurrFunctionContext);
            m_iCurrAfterHex++;
//                m_alCurrAfterHex.clear();
            return true;
        }
        if(token.equals("{{"))
        {
            if(m_bReferenceContext)
            {
                System.out.println("Incorrect '{{' at line:" + m_iCurrLineNum_in);
            }
            m_bReferenceContext = true;
            m_bFunctionReferenceContext = m_sLastToken.equalsIgnoreCase("1B");
            m_iCurrReplacementReferenceIndex = 0;
            return true;
        }
        if(token.equals("}}"))
        {
            if(!m_bReferenceContext)
            {
                System.out.println("Incorrect '}}' at line:" + m_iCurrLineNum_in);
            }
            m_bReferenceContext = false;
            m_iCurrReplacementReferenceIndex = 0;
            return true;
        }
        
        return false;
    }

    private void printIntAsHexString(int iValue)
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.LITTLE_ENDIAN); 
        b.putInt(iValue);
        byte[] result = b.array();
        if(m_bHexBeforeContext)
        {
            for(int J = 0; J < 4 ; J++)
            {
                m_alCurrBeforeHex.add(result[J] & 0xFF);
            }
        }
        else
        {
            for(int J = 0; J < 4 ; J++)
            {
                m_alCurrAfterHex.add(result[J] & 0xFF);
            }
        }
    }
    
    public boolean readFile() throws IOException
    {
        String currLine;
        
        m_iCurrLineNum_in = 0;
        m_kInputFileStrings.clear();
        while(m_kScanner.hasNextLine()) // reached EOF
        {
//            System.out.println("Reading Line: " + m_iCurrLineNum_in);
            m_iCurrLineNum_in++;
            currLine = m_kScanner.nextLine();
            m_kInputFileStrings.add(currLine);
            m_sCurrToken = "";
            if(currLine.isEmpty()) // skip empty lines
            {
                continue;
            }
            if(currLine.startsWith("//")) // skip comment lines
            {
                continue;
            }
            currLine = currLine.split("//")[0]; // strip EOL comments
            String[] tokens = currLine.split("\\s");    
            for (String token : tokens) 
            {
                m_sLastToken = m_sCurrToken;
                m_sCurrToken = token;
                if (token.isEmpty())
                    continue;
                if(updateContext(token))
                    continue;

                // handle named variable replacements
                if((m_bHexBeforeContext || m_bHexAfterContext) && token.startsWith("||") && token.endsWith("||"))
                {
                    String sReference = token.substring(2, token.length()-2);
                    int iReference = (m_sLastToken.equalsIgnoreCase("1B")) ?
                    		m_kUpkComparison.getNameList(false).findString(sReference) :
                    		m_kUpkComparison.getObjectList(false).findString(sReference);
                    printIntAsHexString(iReference);
                    continue;
                }

                // handle updating hex references
                if((m_bHexBeforeContext || m_bHexAfterContext) && m_bReferenceContext && m_bUpdateReferences)
                {
                    m_arrCurrReplaceReference[m_iCurrReplacementReferenceIndex++] = Integer.parseInt(token, 16);
                    if(m_iCurrReplacementReferenceIndex == 4)
                    {
                        int iOldReference = 0;
                        int iNewReference;
                        for(int J = 0; J < 4; J++)
                        {
                            iOldReference += m_arrCurrReplaceReference[J] << (8*J);
                        }
                        String sReferenceString;
                        if(m_bFunctionReferenceContext)
                        {
                            sReferenceString = m_kUpkComparison.getNameList(true).getNamelistEntry(iOldReference);
                            iNewReference = m_kUpkComparison.getNameList(false).findString(sReferenceString);
                        }
                        else
                        {
                            sReferenceString = m_kUpkComparison.getObjectList(true).getObjectlistName(iOldReference);
                            iNewReference = m_kUpkComparison.getObjectList(false).findString(sReferenceString);
                        }
                        printIntAsHexString(iNewReference);
                    }
                    continue;
                }

                // record simple 'before' hex value
                if(m_bHexBeforeContext)
                {
                    m_alCurrBeforeHex.add(Integer.parseInt(token, 16));
                    continue;
                }

                // record simple 'after' hex value
                if(m_bHexAfterContext)
                {
                    m_alCurrAfterHex.add(Integer.parseInt(token, 16));
    //                continue;
               }
            }
        }
        return true;
    }

    
    public boolean revertUpks(boolean bVerbose)
    {
        return findAndReplaceToUpk(m_alAfterHexCollection, m_alBeforeHexCollection, bVerbose);
    }
    
    public boolean writeUpks(boolean bVerbose)
    {
        return findAndReplaceToUpk(m_alBeforeHexCollection, m_alAfterHexCollection, bVerbose);
    }

    public boolean findAndReplaceToUpk(ArrayList<ArrayList<Integer>> FindHex, ArrayList<ArrayList<Integer>> ReplaceHex,  boolean bVerbose)
    {
        long[] aReplaceOffsets = findReplaceOffset(FindHex, ReplaceHex, bVerbose);
        for(int I = 0; I < aReplaceOffsets.length; I++)
        {
            if(aReplaceOffsets[I] == 0)
            {
                System.out.println("Did not locate FIND hex in FIND/REPLACE block " + I);
                return false;
            }
        }
        for(int I = 0; I < FindHex.size(); I++)
        {
            byte dataIn[] = convertIntArrayListToByteArray(ReplaceHex.get(I));
            ByteBuffer outBuf = ByteBuffer.wrap(dataIn);

            String sUpkOutFile = m_alUpkContexts.get(I);
            Path file = Paths.get(m_kUpkFileHandler.getUpkOutputFile(sUpkOutFile));
            
            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.WRITE)) 
            {
                sbc.position(aReplaceOffsets[I]); // set file position
                sbc.write(outBuf);
                if(bVerbose)
                {
                    System.out.println("Wrote block " + I + " to " + sUpkOutFile);
                }
            } 
            catch (IOException x) 
            {
                System.out.println("I/O Exception: " + x);
                return false;
            }
        }
        return true;
    }
    
    private long[] findReplaceOffset(ArrayList<ArrayList<Integer>> FindHex, ArrayList<ArrayList<Integer>> ReplaceHex,  boolean bVerbose)
    {
        long[] aReplaceOffsets = new long[1];
        aReplaceOffsets[0] = 0;
        
        if(FindHex.size() != ReplaceHex.size())
        {
            System.out.println("FIND/REPLACE blocks mismatch. " + FindHex.size() + " FIND blocks / " + ReplaceHex.size() + " REPLACE blocks.");
            return aReplaceOffsets;
        }
        if(bVerbose)
        {
            System.out.println("Matched: " + FindHex.size() + " FIND/REPLACE blocks.");
        }
        for(int I = 0 ; I < FindHex.size(); I++)
        {
            if(FindHex.get(I).size() != ReplaceHex.get(I).size())
            {
                System.out.println("FIND/REPLACE block size mismatch in block: " + I + ".\n"
                        + "\tFIND=" + FindHex.get(I).size() + " bytes, REPLACE=" + ReplaceHex.get(I).size() + " bytes.");
                return aReplaceOffsets;
            }
            if(bVerbose)
            {
                System.out.println("FIND/REPLACE block: " + I+1 + ".\n"
                        + "\t" + FindHex.get(I).size() + " bytes each.");
            }
        }
        aReplaceOffsets = new long[FindHex.size()];
        for(int I = 0; I < FindHex.size(); I++)
        {
            // find possible destination
            m_kUpkComparison = m_kUpkFileHandler.getUpkComparison(m_alUpkContexts.get(I));
            UpkObjectList objectList = m_kUpkComparison.getObjectList(false);
			int objectRef = objectList.findString(m_alFunctionContexts.get(I));
            int filePos = objectList.getObjectlistEntry(objectRef, 9);
            int fileExtent = objectList.getObjectlistEntry(objectRef, 8);

            byte dataIn[] = convertIntArrayListToByteArray(FindHex.get(I));
            ByteBuffer fileBuf = ByteBuffer.allocate(dataIn.length);
//            ByteBuffer outBuf = ByteBuffer.allocate(dataIn.length);
//            ByteBuffer outBuf = ByteBuffer.wrap(dataIn);


            Path file = Paths.get(m_kUpkFileHandler.getUpkOutputFile(m_alUpkContexts.get(I)));
            
            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.READ)) 
            {
                int iCount;
                for(iCount = 0; iCount < fileExtent - dataIn.length; iCount ++)
                {
                    boolean bMatch = true;
                    sbc.position(filePos+iCount); // set file position
                    sbc.read(fileBuf);
                    for(int jCount = 0 ; jCount < dataIn.length; jCount ++)
                    {
//                        System.out.println("Upk: " + fileBuf.get(jCount) + ", Mod: " + dataIn[jCount]);
                        if(fileBuf.get(jCount) != dataIn[jCount])
                        {
                            bMatch = false;
                        }
                    }
                    if(bMatch)
                    {
                        aReplaceOffsets[I] = filePos+iCount;
                        if(bVerbose)
                        {
                            System.out.println("Found block: " + I + " at file position: " + aReplaceOffsets[I]);
                        }
                        break;
                    }
                    fileBuf.clear();
                }
            } 
            catch (IOException x) 
            {
                System.out.println("I/O Exception: " + x);
            }
        }
        return aReplaceOffsets;        
    }
    
    private byte[] convertIntArrayListToByteArray(ArrayList<Integer> list)
    {
        byte[] bytes = new byte[list.size()];
        for(int i=0, len = list.size(); i < len; i++)
           bytes[i] = (byte) (list.get(i) & 0xFF);
        return bytes;
    }
    
}

