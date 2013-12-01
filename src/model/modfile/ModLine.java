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

/**
 *
 * @author Amineri
 */


public class ModLine
{
    private String line;
    
    private ModChunk owner;
    
    private ModFile master;
    
    private int memorySize;
    private int memoryPos;
    private int indentation;
    private LineType type;
    
    private boolean lineIsCode;

    public ModLine(String s)
    {
        line = s;
        indentation = s.lastIndexOf("\t")+1;
        owner = null;
//        System.out.println(s);
        lineIsCode = false;
    }

    public ModLine(String s, ModChunk chunk)
    {
        line = s;
        indentation = s.lastIndexOf("\t")+1;
        owner = chunk;
//        System.out.println(s);
        lineIsCode = false;
    }
    
    public ModLine(String s, ModChunk chunk, boolean isCode)
    {
        line = s;
        indentation = s.lastIndexOf("\t")+1;
        owner = chunk;
//        System.out.println(s);
        if(isCode)
        {
            lineIsCode = !asHex().isEmpty();
        }
    }

    public ModChunk getOwner()
    {
        return owner;
    }
    
    public String asString()
    {
        return line;
    }
    
    public String asString(boolean comments)
    {
        if(comments)
        {
            return line;
        }
        else
        {
            if(line.contains("//"))
            {
                return line.split("//")[0];
            }
            else
            {
                return line;
            }
        }
    }
    
    public String asHex()
    {
        String outString = "";
        String[] tokens = asString(false).split("\\s");
        for(String token : tokens)
        {
            if(token.toUpperCase().matches("[0-9A-F][0-9A-F]"))
            {
                outString += token + " ";
            }
        }
        return outString;
    }
    
    public LineType getType()
    {
        return type;
    }
    
    public int getMemoryPos()
    {
        return memoryPos;
    }
    
    public int getMemorySize()
    {
        return memorySize;
    }
    
    public int getIndentation()
    {
        return indentation;
    }
    
    public boolean isCode()
    {
        return lineIsCode;
    }
}
