package custom.diagram.actions;

import custom.diagram.Link;
import custom.diagram.Node;
import custom.diagram.Canvas;
import custom.diagram.JDiagramEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class CreateNewLinkAction extends UserAction {

    private boolean creatingNewLink = false;
    private Link link = null;

    @Override
    public void onSetup(JDiagramEditor sd) {
        sd.addMouseListener(new MouseAdapter() {


            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {

                    System.out.println("linking");
                    Canvas canvas = sd.getSketch().getCurrentCanvas();

                    Node node = canvas.findNodeAt(
                            Objects.requireNonNullElse(
                                    canvas.toCanvasSpace(sd.getMousePosition()), new Point(0, 0))
                    );


                    if (node != null) {
                        if (!creatingNewLink) {
                            link = new Link(canvas.toCanvasSpace(e.getPoint()), node);
                            link.create(sd);
                            canvas.addToFirst(link);
                            System.out.println("Adding link");

                            creatingNewLink = true;
                        } else {
                            link.setSocket(node);
                            node.onLinkCompleted(link);
                            link.getPlug().onLinkCompleted(link);

                            creatingNewLink = false;

                        }


                        sd.repaint();
                    }


                }
            }
        });
    }


}
