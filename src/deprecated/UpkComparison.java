///*
// * Copyright (C) 2013 Rachel Norman
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package deprecated;
//
//import UPKmodder.HexStringLibrary;
//import java.io.IOException;
//import java.nio.file.Path;
//
//import model.upk.UpkHeader;
//
///**
// *
// * @author Amineri
// */
//public class UpkComparison 
//{
//    UpkHeader m_kSource;
//    UpkHeader m_kDest;
//    
//    int[] m_arrMappedRefs;
//    
//    int m_iNumExtraBeforeRefs = 0;
//    int[] m_arrExtraBeforeRefs;
//    
//    int m_iNumExtraAfterRefs = 0;
//    int[] m_arrExtraAfterRefs;
//    
//    public UpkComparison()
//    {
//        
//    }
//    
////    public Boolean initSourceUpkHeader(Path thisfile, boolean bVerbose) throws IOException
////    {
////        m_kSource = new Upk(thisfile, bVerbose);
////        String filename = thisfile.getFileName().toString();
////        m_kHeader_before.setUpkName(filename);
////        m_kHeader_before.parseUPKHeader(thisfile, bVerbose);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Read UPK Header");
////
////        m_kNamelist_before.Init(m_kHeader_before);
////        m_kNamelist_before.parseNamelist(thisfile);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Read UPK Namelist");
////        
////        m_kObjectlist_before.Init(m_kHeader_before);
////        m_kObjectlist_before.parseObjectlist(thisfile);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Read UPK Objectlist");
////        
////        m_kObjectlist_before.constructObjectNames(m_kNamelist_before);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Matched UPK Objectlist to Namelist");
////
////        return true;
////    }
////
////    public Boolean initAfterUpk(Path thisfile, boolean bVerbose) throws IOException
////    {
////        String filename = thisfile.getFileName().toString();
////        m_kHeader_after.setUpkName(filename);
////        m_kHeader_after.parseUPKHeader(thisfile, bVerbose);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Read UPK Header");
////
////        m_kNamelist_after.Init(m_kHeader_after);
////        m_kNamelist_after.parseNamelist(thisfile);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Read UPK Namelist");
////        
////        m_kObjectlist_after.Init(m_kHeader_after);
////        m_kObjectlist_after.parseObjectlist(thisfile);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Read UPK Objectlist");
////        
////        m_kObjectlist_after.constructObjectNames(m_kNamelist_after);
////        if(bVerbose)
////            System.out.println("\t" + filename  + ": Matched UPK Objectlist to Namelist");
////
////        return true;
////    }
//
//    public Boolean matchUpks()
//    {
//        String sBeforeString, sAfterString;
//        int iFoundIndex;
//        
//        HexStringLibrary kHexLibrary = new HexStringLibrary();
//        
//        m_arrMappedRefs = new int[m_kObjectlist_before.getNumObjects()+1];
//        m_arrExtraBeforeRefs = new int[100];
//        m_arrExtraAfterRefs = new int[100];
//
//        for(int I = 1; I <= m_kObjectlist_before.getNumObjects(); I++)
//        {
//            sBeforeString = m_kObjectlist_before.getObjectlistName(I);
//            iFoundIndex = m_kObjectlist_after.findString(sBeforeString);
//            if(iFoundIndex > 0)
//            {
//                m_arrMappedRefs[I] = iFoundIndex;
//                System.out.println("Match  : " + kHexLibrary.convertIntToHexString(I) + "  to " + kHexLibrary.convertIntToHexString(iFoundIndex) + " : " + m_kObjectlist_before.getObjectlistName(I));
//            }
//            else
//            {
//                m_arrExtraBeforeRefs[m_iNumExtraBeforeRefs] = I;
//                m_iNumExtraBeforeRefs++;
//            }
//        }
//        System.out.println(m_kObjectlist_before.getUpkName());
//        for(int I = 0; I < m_iNumExtraBeforeRefs; I++)
//        {
//            System.out.println("No match for: " + kHexLibrary.convertIntToHexString(m_arrExtraBeforeRefs[I]) + " : " + m_kObjectlist_before.getObjectlistName(m_arrExtraBeforeRefs[I]));
//        }
//        for(int I = 1; I <= m_kObjectlist_after.getNumObjects(); I++)
//        {
//            sAfterString = m_kObjectlist_after.getObjectlistName(I);
//            iFoundIndex = m_kObjectlist_before.findString(sAfterString);
//            if(iFoundIndex < 0)
//            {
//                m_arrExtraAfterRefs[m_iNumExtraAfterRefs] = I;
//                m_iNumExtraAfterRefs++;
//            }
//        }
//        System.out.println(m_kObjectlist_after.getUpkName());
//        for(int I = 0; I < m_iNumExtraAfterRefs; I++)
//        {
//            System.out.println("No match for: " + kHexLibrary.convertIntToHexString(m_arrExtraAfterRefs[I]) + " : " + m_kObjectlist_after.getObjectlistName(m_arrExtraAfterRefs[I]));
//        }
//
//        return true;
//    }
//}
