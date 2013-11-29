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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 *
 * @author Amineri
 */
public class UpkHeader 
{
    private int numNamelistEntries;
    private int posNamelist;
    private int numObjectlistEntries;
    private int posObjectlist;
    private String m_sUpkName;

    public int getNumNamelistEntries()
    {
        return numNamelistEntries;
    }
    
    public int getNamelistPosition()
    {
        return posNamelist;
    }
    
    public int getNumObjectlistEntries()
    {
        return numObjectlistEntries;
    }
    
    public int getObjectlistPosition()
    {
        return posObjectlist;
    }
    
    public String getUpkName()
    {
        return m_sUpkName;
    }

    public void setUpkName(String sName) 
    {
        m_sUpkName = sName;
    }
    
    public void parseUPKHeader(Path thisfile, boolean bVerbose) throws IOException
    {
        try(FileChannel fc = FileChannel.open(thisfile))
        {
            ByteBuffer buf = ByteBuffer.allocate(100);
            buf.order(ByteOrder.LITTLE_ENDIAN);

            fc.position(0x19);
            fc.read(buf);
            
            numNamelistEntries = buf.getInt(0);
            if(bVerbose)
                System.out.println("\tNamelist entries : " + numNamelistEntries);
            
            posNamelist = buf.getInt(4);
            if(bVerbose)
                System.out.println("\tNamelist start pos: " + posNamelist);
            
            numObjectlistEntries = buf.getInt(8);
            if(bVerbose)
                System.out.println("\tObjectlist entries: " + numObjectlistEntries);
            
            posObjectlist = buf.getInt(12);
            if(bVerbose)
                System.out.println("\tObjectlist start pos: " + posObjectlist);
            
            buf.clear();
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
    }
}