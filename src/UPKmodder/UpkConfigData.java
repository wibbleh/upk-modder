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
 * @author Administrator
 */
public class UpkConfigData
{
    public boolean m_bCompressedOutput;
    public boolean m_bMirroredOutput;
    public boolean m_bWriteToUpks;
    public boolean m_bRevertUpks;
    public boolean m_bVerbose;
    public boolean m_bUpdateReferences;
    public boolean m_bRefsHexToString;
    public boolean m_bRefsStringToHex;
    public boolean m_bRefsTag;
    public boolean m_bRefsUntag;

    public String m_mod_filename_input;
    public String m_mod_filename_output;
//    public String m_mod_filename_output = "C:/Games/test_mirrored_out.txt";
    public String m_sVersion;

    public String upkConfig;
    public String m_sOperandData;
    
    public String m_sHelpText;

    public UpkConfigData()
    {
        this.m_bCompressedOutput = false;
        this.m_bMirroredOutput = false;
        this.m_bWriteToUpks = false;
        this.m_bRevertUpks = false;
        this.m_bVerbose = false;
        this.m_bUpdateReferences = false;
        this.m_bRefsHexToString = false;
        this.m_bRefsStringToHex = false;
        this.m_bRefsTag = false;
        this.m_bRefsUntag = false;
//        this.m_mod_filename_input = "C:/Games/sample_modfile_no_refs.txt";
//        this.m_mod_filename_input = "C:/Games/test_modfile_refs_added.txt";
//        this.m_mod_filename_output = "C:/Games/test_modfile_refs_updated.txt";
//        this.m_mod_filename_output = "C:/Games/test_compressed_out.txt";
        this.m_mod_filename_input = "";
        this.m_mod_filename_output = "";
        this.m_sVersion = "XComModTool v 0.11";
        this.upkConfig = "upk_config.ini";
        this.m_sOperandData = "operand_data.ini";
        this.m_sHelpText = "XCOMMOD helper tool \n"
                + "Usage: XComModTool <options> -i <InputFile> -o <OutputFile>\n"
                + "Options:\n"
                + "-V, --VERSION : version info\n"
                + "-h, --HELP : this help file\n"
                + "-v, --VERBOSE : verbose output\n"
                + "-w, --WRITETOUPKS : write hex to configured upks (before->after)\n"
                + "-r, --REVERTUPKS : revert hex in configured upks (after->before)\n"
                + "-c, --COMPRESSEDOUTPUT : outputs compressed modfile\n"
                + "-m, --MIRROREDOUTPUT : outputs mirrored modfile\n"
                + "-U, --UPDATE_REFS : updates any tagged references or named references\n"
                + "-x, --REFS_STRINGTOHEX : converts string references to tagged hex references\n"
                + "-s, --REFS_HEXTOSTRING : converts tagged hex references to string references\n"
                + "-t, --REFS_TAG : tags references\n"
                + "-u, --REFS_UNTAG : removes reference tags";
    }
}
