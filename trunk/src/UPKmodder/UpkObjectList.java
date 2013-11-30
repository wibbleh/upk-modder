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
import static java.lang.Integer.toHexString;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import model.upk.UpkHeader;

/**
 *
 * @author Amineri
 */
public class UpkObjectList 
{
    // CONSTANTS
    int BYTES_PER_OBJECT_ENTRY = 100;
    int INTEGERS_PER_OBJECT_ENTRY = 20;
    Boolean DEBUG = false;
    
    int m_iNumObjectlistEntries = -1;
    int m_iPositionObjectlist = -1;
    int[][] m_arrObjectlist;
    String[] m_arrObjectNames;
    ArrayList<String> m_alObjectNames;
    String m_sUpkName;

    
    public void Init(UpkHeader kHeader)
    {
        setNumUpkObjectlistEntries(kHeader.getObjectListSize());
        setObjectlistPosition(kHeader.getObjectlistPosition());
        setUpkName(kHeader.getUpkName());
        m_arrObjectlist = new int[m_iNumObjectlistEntries+1][INTEGERS_PER_OBJECT_ENTRY];
        m_arrObjectNames = new String[m_iNumObjectlistEntries+1];
        m_alObjectNames = new ArrayList<>(m_iNumObjectlistEntries);
    }
    
    protected void setObjectlistPosition(int iPosition) 
    {
        m_iPositionObjectlist = iPosition;
    }

    protected void setNumUpkObjectlistEntries(int iNum) 
    {
        m_iNumObjectlistEntries = iNum;
    }
    
    protected void setUpkName(String sName) 
    {
        m_sUpkName = sName;
    }

    public String getUpkName()
    {
        return m_sUpkName;
    }
    
    public int getNumObjects()
    {
        return m_iNumObjectlistEntries;
    }
    
    public int getObjectlistEntry(int iIndex, int iField)
    {
        return m_arrObjectlist[iIndex][iField];
    }
    
    public String getObjectlistName(int iIndex)
    {
        return m_arrObjectNames[iIndex];
    }
    
    public int findString(String sSearchString)
    {
        return m_alObjectNames.indexOf(sSearchString);
    }
        
    public void parseObjectlist(Path thisfile) throws IOException
    {
        int iCurrPosition = m_iPositionObjectlist;
        int iExtraBytes;
        
        ByteBuffer buf = ByteBuffer.allocate(BYTES_PER_OBJECT_ENTRY);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        FileChannel fc = null;
        
        if (m_iNumObjectlistEntries == -1 || m_iPositionObjectlist == -1)
        {
            return;
        }
        try
        {
            fc = FileChannel.open(thisfile);
            for(int count = 1; count <= m_iNumObjectlistEntries ; count++)
            {
                fc.position(iCurrPosition); // seek to current namelist entry
                if(DEBUG)
                {
                    System.out.println("Position: " + fc.position());
                }
                fc.read(buf);
                
                iExtraBytes = buf.getInt(4*11);
                buf.rewind();
                for (int I = 0; I < 17 + iExtraBytes ; I++)
                {
                    m_arrObjectlist[count][I] = buf.getInt(4*I);
                }
                if(DEBUG)
                {
                    System.out.print("Objectlist[" + count + "]: ");
                    for (int I = 0; I < 17 + iExtraBytes ; I++)
                    {
                        System.out.print(m_arrObjectlist[count][I] + ", ");
                    }
                    System.out.println();
                }
                buf.clear();
                iCurrPosition += 4*(17 + iExtraBytes);
            }

        }
        catch (IOException x) 
        {
            System.out.println("caught exception: " + x);
        }
        finally
        {
            if(fc != null)
            {
                fc.close();
            }
        }
        return;
    }    
    
    public Boolean constructObjectNames(UpkNameList kNamelist)
    {
        int iTempNameIndex;
        int iOwnerIndex, iPrevOwnerIndex;
        int iType;
        String sTempString;
        
        for(int I = 1; I <= m_iNumObjectlistEntries; I++)
        {
            iType = getObjectlistEntry(I, 0);
            iPrevOwnerIndex = -1;
            iTempNameIndex = getObjectlistEntry(I, 3);
            sTempString = kNamelist.getNamelistEntry(iTempNameIndex);
            if(iType == -360)
            {
                kNamelist.setEntryAsFunction(iTempNameIndex);
            }
            m_arrObjectNames[I] = sTempString;
            iOwnerIndex = getObjectlistEntry(I, 2);
            while(iOwnerIndex <= m_iNumObjectlistEntries && iOwnerIndex > 0 && iOwnerIndex != I && iPrevOwnerIndex != iOwnerIndex)
            {
                m_arrObjectNames[I] = m_arrObjectNames[I].concat(".");
                iTempNameIndex = getObjectlistEntry(iOwnerIndex, 3);
                sTempString = kNamelist.getNamelistEntry(iTempNameIndex);
                m_arrObjectNames[I] = m_arrObjectNames[I].concat(sTempString);
                iPrevOwnerIndex = iOwnerIndex;
                iOwnerIndex = getObjectlistEntry(iOwnerIndex, 2);
            }
            if(DEBUG)
            {
                ByteBuffer b = ByteBuffer.allocate(4);
                b.order(ByteOrder.LITTLE_ENDIAN); 
                b.putInt(I);

                byte[] result = b.array();
                System.out.print("Objectlist[" + String.format("%5s", I) + "]: ");
                for(int J = 0; J < 4 ; J++)
                {
                    int temp = result[J] & 0xFF;
                    System.out.print(String.format("%2s", Integer.toHexString(temp)).replace(' ', '0').toUpperCase() + " ");
                }
                System.out.println(" : " + m_arrObjectNames[I]);
            }
        }
        m_alObjectNames = new ArrayList<>(Arrays.asList(m_arrObjectNames));
        return true;
    }
}
