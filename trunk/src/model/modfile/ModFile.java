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

import java.util.ArrayList;


/**
 *
 * @author Amineri
 */

public class ModFile 
{
    private ArrayList<ModLine> header;
    private int headerCapacity = 10;
    private boolean inHeader;
    private ArrayList<ModUpk> upks;
    private int capacity = 10;
    
    public ModFile()
    {
        header = new ArrayList<>(headerCapacity);
        upks = new ArrayList<>(capacity);
        inHeader = true;
    }
    
    public void addLine(String s)
    {
        if(s.toUpperCase().startsWith("UPKFILE="))
        {
            if(upks.size() == capacity)
            {
                capacity += 5;
                upks.ensureCapacity(capacity);
            }
            String name = s.split("\\s")[0];
            upks.add(new ModUpk(name, this));
            inHeader = false;
        }
        if(inHeader)
        {
            if(header.size() == headerCapacity)
            {
                headerCapacity += 5;
                header.ensureCapacity(headerCapacity);
            }
            header.add(new ModLine(s));
        }
        else
        {
            upks.get(upks.size()-1).addLine(s);
        }
    }
    
    public int getNumLines()
    {
        int numLines = header.size();
        for(ModUpk upk : upks)
        {
            numLines += upk.getNumLines();
        }
        return numLines; 
    }
    
    public ModLine getLine(int index)
    {
        if(index < header.size())
        {
            return header.get(index);
        }
        index -= header.size();
        if(index >=0 && index < getNumLines())
        {
            int iCount = 0;
            for(ModUpk upk : upks)
            {
                if (iCount + upk.getNumLines() > index)
                {
                    return upk.getLine(index-iCount);
                }
            }
        }
        return null;
    }
    
    public boolean hasNextLine()
    {
        return true;
    }
    
    public String getNextLine()
    {
        return "";
    }
}

