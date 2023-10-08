package custom.diagram.actions;

import custom.diagram.Canvas;
import custom.diagram.JDiagramEditor;

import java.awt.*;


public abstract class UserAction {
    public abstract void onSetup(JDiagramEditor sd);

    public void draw(Canvas withRespectTo, Graphics2D g2d){}

}
