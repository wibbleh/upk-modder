package model.moddocument3;

import javax.swing.text.Position;

/**
 *
 * @author Amineri
 */


public class ModPosition implements Position
{
    private int position;
    
    public ModPosition(int i)
    {
        position = i;
    }
    
    @Override
    public int getOffset()
    {
        return position; //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setPosition(int i)
    {
        position = i;
    }
    
}
