package io.upk;

import junit.framework.TestCase;

import org.junit.Test;

/**
 *
 * @author Amineri
 */


public class UPKFileLoaderTest extends TestCase
{
    @Test
    public void testUpkFileLoading()
    {
        UpkFileLoader upks = new UpkFileLoader();
        assertNotNull(upks.getUpk("XComGame.upk", "33 2E 29 6A A5 DD FC 40 B5 CC 57 A5 A7 AA 8C 41")); // EU patch 4
        assertNotNull(upks.getUpk("XComGame.upk", "01 E9 EB 29 23 F4 DB 4F A8 2B 8E 46 A7 25 E5 D6")); // EU patch 5
        assertNotNull(upks.getUpk("XComGame.upk", "B1 1A D8 E4 48 29 FC 43 8E C0 7A B0 A3 3E 34 9F")); // EW release
        assertNotNull(upks.getUpk("XComStrategyGame.upk", "A8 46 50 30 6F 48 84 42 AC 1A 72 B6 8D 2E 6D 23")); // EU patch 4
        assertNotNull(upks.getUpk("XComStrategyGame.upk", "1D EE 33 F1 66 88 FB 49 B1 72 93 44 01 BC 85 39")); // EU patch 5
        assertNotNull(upks.getUpk("XComStrategyGame.upk", "9C F8 70 10 21 38 A0 41 8B 33 A4 2E E8 71 23 00")); // EW release
    }
}
