package io.upk;

import UPKmodder.UpkConfigData;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import model.upk.UpkFile;

/**
 *
 * @author Amineri
 */


public class UpkFileLoader
{
    public UpkConfigData configData;
    private boolean verbose;
    
    private ArrayList<String> baseUpkNames;
    private ArrayList<ArrayList<String>> guidMatrix;
    private ArrayList<ArrayList<UpkFile>> upkMatrix;

    public UpkFile getUpk(String upkName, String GUID)
    {
        if(baseUpkNames == null || guidMatrix == null || upkMatrix == null) {
            return null;
        }
        int upkIndex = baseUpkNames.indexOf(upkName);
        if(guidMatrix.get(upkIndex) == null){
            return null;
        }
        if(upkIndex < 0 || upkIndex >= guidMatrix.get(upkIndex).size()) {
            return null;
        }
        int guidIndex = guidMatrix.get(upkIndex).indexOf(GUID);
        
        // TODO -- add GUID check
        if(guidIndex < 0 || guidIndex >= upkMatrix.get(upkIndex).size()) {
            return null;
        }
        else {
            return upkMatrix.get(upkIndex).get(guidIndex);
        }
    }
    
    public UpkFileLoader()
    {
        this.configData = new UpkConfigData();
        verbose = configData.m_bVerbose;
        ArrayList<UpkFile> upkList = null;
        ArrayList<String> guidList = null;

        String encoding = System.getProperty("file.encoding");
        String currLine;
        boolean firstUpkType = true;

        if(Files.exists(Paths.get(configData.upkConfig))) {
            if(verbose) {
                System.out.println("m_kConfigFile: " + Paths.get(configData.upkConfig)); 
            }
            try (Scanner kScanner = new Scanner(Files.newBufferedReader(Paths.get(configData.upkConfig), Charset.forName(encoding))))
            {
                while(kScanner.hasNextLine())
                {
                    currLine = kScanner.nextLine();
                    currLine = currLine.split(";")[0].trim();
                    if(currLine.isEmpty())
                        continue;
                    if(currLine.startsWith("NUM_UPKS="))
                    {
                        int numUpkTypes = Integer.parseInt(currLine.split("=",2)[1]);
                        if(configData.m_bVerbose)
                        {
                            System.out.println("Attempting to read " + numUpkTypes + " types of upk files.");
                        }
                        baseUpkNames = new ArrayList<>(numUpkTypes);
                        guidMatrix = new ArrayList<>(numUpkTypes);
                        upkMatrix = new ArrayList<>(numUpkTypes);
                    }
                    else if(currLine.startsWith("UPKFILE="))
                    {
                        if(firstUpkType) {
                            firstUpkType = false;
                        } else {
                            guidMatrix.add(guidList);
                            upkMatrix.add(upkList);
                        }
                        
                        baseUpkNames.add(currLine.split("=",2)[1]);
                        if(verbose)
                        {
                            System.out.println("Attempting to read " + currLine.split("=",2)[1] + " files.");
                        }
                    }
                    else if(currLine.startsWith("NUM_VERSIONS="))
                    {
                        int numVersions = Integer.parseInt(currLine.split("=",2)[1]);
                        upkList = new ArrayList<>(numVersions);
                        guidList = new ArrayList<>(numVersions);
                    }
                    else if(currLine.startsWith("FILE="))
                    {
                        String upkPath = currLine.split("::")[0].split("=",2)[1].trim();
                        String GUID = currLine.split("::")[1].split("=",2)[1].trim();
                        try {
                            File file = new File(upkPath);		
                            upkList.add(new UpkFile(file));
                            guidList.add(GUID);
                        }
                        catch(Throwable x)
                        {
                            // TODO -- add exception handling
                        }
                        if(verbose) {
                            System.out.println("Read " + upkPath + ".");
                        }
                        
                        // TODO -- add GUID check

                    }
                }
                guidMatrix.add(guidList);
                upkMatrix.add(upkList);
            }
            catch (IOException x)
            {
                System.out.println("caught exception: " + x);
            }
        }
    }
}
