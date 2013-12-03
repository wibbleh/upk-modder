package parser.unrealhex;

import model.upk.UpkFile;

/**
 *
 * @author amineri
 */


public class RefNameParser
{
    private UpkFile sourceUpk;
    private UpkFile destUpk;
    
    public void RefNameParser(UpkFile src, UpkFile dst)
    {
        this.sourceUpk = src;
        this.destUpk = dst;
    }
    
    public String hexToName(int ref, boolean srctgt)
    {
        if(srctgt)
        {
            return sourceUpk.getRefName(ref);
        }
        else
        {
            return destUpk.getRefName(ref);
        }
    }

    public String hexToVFName(int ref, boolean srctgt)
    {
        if(srctgt)
        {
            return sourceUpk.getVFRefName(ref);
        }
        else
        {
            return destUpk.getVFRefName(ref);
        }
    }

    public int nameToHex(String name, boolean srctgt)
    {
        if(srctgt)
        {
//            return sourceUpk.findRefName(name);
        }
        else
        {
//            return destUpk.findRefName(name);
        }
        return 0;
    }

    public int nameToVFHex(String name, boolean srctgt)
    {
        if(srctgt)
        {
//            return sourceUpk.findVFRefName(name);
        }
        else
        {
//            return destUpk.findVFRefName(name);
        }
        return 0;
    }

    public int updateHexRef(int ref)
    {
        return nameToHex(hexToName(ref, true), false);
    }
    
    public int updateVFHexRef(int ref)
    {
        return nameToVFHex(hexToVFName(ref, true), false);
    }
}
