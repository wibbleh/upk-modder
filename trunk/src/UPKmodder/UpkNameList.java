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
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Amineri
 */

public class UpkNameList 
{
    //CONSTANTS
    Boolean DEBUG = false;
    
    String m_sUpkName;
    int m_iNumNamelistEntries = -1;
    int m_iPositionNamelist = -1;
    
    ArrayList<String> m_alNameListStrings;
    String[] m_arrNameListStrings;
    boolean[] m_arrIsFunction;
    
    public void Init(UpkHeader kHeader)
    {
        m_iNumNamelistEntries = kHeader.getNumNamelistEntries();
        m_iPositionNamelist = kHeader.getNamelistPosition();
        m_sUpkName = kHeader.getUpkName();
        m_arrNameListStrings = new String[m_iNumNamelistEntries];
        m_alNameListStrings = new ArrayList<>(m_iNumNamelistEntries);
        m_arrIsFunction = new boolean[m_iNumNamelistEntries];
    }
    
    public void setNamelistPosition(int iPosition) 
    {
        m_iPositionNamelist = iPosition;
    }

    public void setNumUpkNamelistEntries(int iNum) 
    {
        m_iNumNamelistEntries = iNum;
    }
    
    public void setUpkName(String sName) 
    {
        m_sUpkName = sName;
    }

    public void setEntryAsFunction(int iIndex)
    {
        m_arrIsFunction[iIndex] = true;
    }
    
    public int findString(String sSearchString)
    {
        int iTemp = m_alNameListStrings.indexOf(sSearchString);
        if(m_arrIsFunction[iTemp])
        {
            return iTemp;
        }
        else
        {
            return -1;
        }
    }
        
    public String getNamelistEntry(int iIndex)
    {
        if(iIndex <= 0 || iIndex > m_iNumNamelistEntries)
        {
           return "Invalid Entry";
        }
        return m_arrNameListStrings[iIndex];
    }
    
    public int getNumNamelistEntries()
    {
        return m_iNumNamelistEntries;
    }
    public void parseNamelist(Path thisfile) throws IOException
    {
        int iCurrPosition = m_iPositionNamelist;
        int iStringLength;  
        
        ByteBuffer buf = ByteBuffer.allocate(100);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        FileChannel fc = null;

        // Read the bytes with the proper encoding for this platform.  If
        // you skip this step, you might see something that looks like
        // Chinese characters when you expect Latin-style characters.
        String encoding = System.getProperty("file.encoding");

        if (m_iNumNamelistEntries == -1 || m_iPositionNamelist == -1)
        {
            return;
        }
        try
        {
            fc = FileChannel.open(thisfile);
            for(int count = 0; count < m_iNumNamelistEntries ; count++)
            {
                StringBuilder testString = new StringBuilder();
                fc.position(iCurrPosition); // seek to current namelist entry
                fc.read(buf);
                iStringLength = buf.getInt(0);
                buf.rewind();
                testString.append(Charset.forName(encoding).decode(buf), 4, 3+iStringLength);
                m_arrNameListStrings[count] = testString.toString();
                if(DEBUG)
                {
                  System.out.println("Namelist[" + count + "]: " + m_arrNameListStrings[count]);
                }
                buf.clear();
                iCurrPosition += 12 + iStringLength;
            }
            m_alNameListStrings = new ArrayList<>(Arrays.asList(m_arrNameListStrings));
        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
        finally
        {
            if(fc !=null)
            {
                fc.close();
            }
        }
        return;
    }
}
