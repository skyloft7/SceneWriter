package custom.designer.actions;

import custom.designer.Canvas;
import custom.designer.JDiagramEditor;

import java.awt.*;


public abstract class UserAction {
    public abstract void onSetup(JDiagramEditor sd);

    public void draw(Canvas withRespectTo, Graphics2D g2d){}

}
