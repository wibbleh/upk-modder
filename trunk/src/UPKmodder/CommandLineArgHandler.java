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

/**
 *
 * @author Amineri
 */
public class CommandLineArgHandler 
{
    String m_kLastCommand = "";
    
    private UpkConfigData parseCommand(String sToken, UpkConfigData kConfigData)
    {
        if(!m_kLastCommand.isEmpty())
        {
            if(m_kLastCommand.equalsIgnoreCase("INPUTFILE"))
            {
                kConfigData.m_mod_filename_input = sToken;
                m_kLastCommand = "";
            }
            else if (m_kLastCommand.equalsIgnoreCase("OUTPUTFILE"))
            {
                kConfigData.m_mod_filename_output = sToken;
                m_kLastCommand = "";
            }
            else
            {
                System.out.println("Invalid Command");
            }
        }
        else // not carrying previous command
        {
            String sCommand;
            if(sToken.startsWith("--"))
            {
                sCommand = sToken.substring(2, sToken.length()).toUpperCase();
            }
            else if(sToken.startsWith("-"))
            {
                sCommand = sToken.substring(1, sToken.length());
            }
            else
            {
                sCommand = "";
            }

            switch(sCommand)
            {
                case "VERSION" :
                case "V" :
                    System.out.println(kConfigData.m_sVersion);
                    System.exit(0);
                    break;
                case "HELP" :
                case "h" :
                    System.out.println(kConfigData.m_sHelpText);
                    System.exit(0);
                    break;
                case "VERBOSE" :
                case "v" :
                    kConfigData.m_bVerbose = true;
                    break;
                case "WRITETOUPKS":
                case "w":
                    kConfigData.m_bWriteToUpks = true;
                    kConfigData.m_bRevertUpks = false;
                    break;
                case "REVERTUPKS":
                case "r":
                    kConfigData.m_bWriteToUpks = false;
                    kConfigData.m_bRevertUpks = true;
                    break;
                case "COMPRESSEDOUTPUT":
                case "c":
                    kConfigData.m_bMirroredOutput = false;
                    kConfigData.m_bCompressedOutput = true;
                    break;
                case "MIRROREDOUTPUT":
                case "m":
                    kConfigData.m_bMirroredOutput = true;
                    kConfigData.m_bCompressedOutput = false;
                    break;
                case "UPDATE_REFS":
                case "U":
                    kConfigData.m_bUpdateReferences = true;
                    break;
                case "REFS_HEXTOSTRING":
                case "s":
                    kConfigData.m_bRefsHexToString = true;
                    kConfigData.m_bRefsStringToHex = false;
                    kConfigData.m_bRefsTag = false;
                    kConfigData.m_bRefsUntag = false;
                    break;
                case "REFS_STRINGTOHEX":
                case "x":
                    kConfigData.m_bRefsHexToString = false;
                    kConfigData.m_bRefsStringToHex = true;
                    kConfigData.m_bRefsTag = false;
                    kConfigData.m_bRefsUntag = false;
                    break;
                case "REFS_TAG":
                case "t":
                    kConfigData.m_bRefsHexToString = false;
                    kConfigData.m_bRefsStringToHex = false;
                    kConfigData.m_bRefsTag = true;
                    kConfigData.m_bRefsUntag = false;
                    break;
                case "REFS_UNTAG":
                case "u":
                    kConfigData.m_bRefsHexToString = false;
                    kConfigData.m_bRefsStringToHex = false;
                    kConfigData.m_bRefsTag = false;
                    kConfigData.m_bRefsUntag = true;
                    break;
                case "INPUTFILE":
                case "i":
                    m_kLastCommand = "INPUTFILE";
                    break;
                case "OUTPUTFILE":
                case "o":
                    m_kLastCommand = "OUTPUTFILE";
                    break;
                default:
                    System.out.println("Unknown Command");
            }
        }
        return kConfigData;
    }
    
    public UpkConfigData Init(String[] args, UpkConfigData kConfigData)
    {
        for(String arg : args)
        {
            kConfigData = parseCommand(arg, kConfigData);
        }
        return kConfigData;
    }
}
