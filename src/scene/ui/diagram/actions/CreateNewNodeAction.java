package scene.ui.diagram.actions;

import scene.ui.diagram.Node;
import scene.ui.diagram.Canvas;
import scene.ui.diagram.JDiagramEditor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CreateNewNodeAction extends UserAction {
    @Override
    public void onSetup(JDiagramEditor sd) {
        sd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_T) {
                    System.out.println("adding node");
                    Node node = new Node();
                    node.create(sd);

                    Canvas canvas = sd.getSketch().getCurrentCanvas();

                    node.setPosition(canvas.toCanvasSpace(e.getComponent().getMousePosition()));


                    canvas.addEntity(node);


                    sd.repaint();



                }
            }
        });
    }
}
