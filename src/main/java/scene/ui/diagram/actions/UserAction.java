package scene.ui.diagram.actions;

import scene.ui.diagram.Canvas;
import scene.ui.diagram.JDiagramEditor;

import java.awt.*;


public abstract class UserAction {
    public abstract void onSetup(JDiagramEditor sd);

    public void draw(Canvas withRespectTo, Graphics2D g2d){}

}
