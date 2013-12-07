package ui.modeditorkit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.text.LabelView;
import model.modelement3.ModElement;


/**
 *
 * @author Amineri
 */


public class ModLabelView extends LabelView {
 
   
    public ModLabelView(ModElement elem) {
        super(elem);
    }
    
//    public void paint(Graphics g, Shape allocation) {
//        super.paint(g, allocation);
//        if (getAttributes().getAttribute(ModDocument.JAGGED_UDERLINE_ATTRIBUTE_NAME)!=null &&
//            (Boolean)getAttributes().getAttribute(ModDocument.JAGGED_UDERLINE_ATTRIBUTE_NAME)) {
//            paintJaggedLine(g, allocation);
//        }
//    }

 
    public void paintJaggedLine(Graphics g, Shape a) {
        int y = (int) (a.getBounds().getY() + a.getBounds().getHeight());
        int x1 = (int) a.getBounds().getX();
        int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());
 
        Color old = g.getColor();
        g.setColor(Color.red);
        for (int i = x1; i <= x2; i += 6) {
            g.drawArc(i + 3, y - 3, 3, 3, 0, 180);
            g.drawArc(i + 6, y - 3, 3, 3, 180, 181);
        }
        g.setColor(old);
    }

}
