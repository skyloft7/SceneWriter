package custom.diagram.actions;

import custom.diagram.Entity;
import custom.diagram.Node;
import custom.diagram.Canvas;
import custom.diagram.JDiagramEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class MultipleSelectionAction extends UserAction {

    Point start = null;
    Point end = null;

    Path2D currentRegion = new Path2D.Float();

    ArrayList<Entity> multipleSelectedEntities = new ArrayList<>();
    private boolean lostSelection;


    public boolean pathIsInRegion(Rectangle bounds, Rectangle rect) {

        return bounds.intersects(rect);

        /*

        if (rect.width >= 0 && rect.height >= 0) {
            // Normal intersection test
            return path.intersects(rect);
        } else {
            // Adjust rectangle to normal orientation
            int x = rect.x + rect.width;
            int y = rect.y + rect.height;
            int width = Math.abs(rect.width);
            int height = Math.abs(rect.height);
            // Test intersection with adjusted rectangle

            System.out.println(x + ", " + y + ", " + width + ", " + height);

            return path.intersects(x, y, width, height);
        }

         */
    }


    @Override
    public void onSetup(JDiagramEditor sd) {

        sd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_DELETE){


                    //Mass delete any Entities in our multiple selection region
                    sd.getSketch().getCurrentCanvas().removeIf(sd, entity -> multipleSelectedEntities.contains(entity));
                    multipleSelectedEntities.clear();
                    sd.repaint();


                }
            }
        });


        sd.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if(lostSelection){
                    multipleSelectedEntities.clear();
                    lostSelection = false;
                    sd.repaint();
                }


                if(SwingUtilities.isRightMouseButton(e)){
                    start = e.getPoint();
                    sd.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if(start != null && end != null){
                    lostSelection = true;
                }

                start = null;
                end = null;
                currentRegion = new Path2D.Float();
                sd.repaint();
            }
        });

        sd.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if(SwingUtilities.isRightMouseButton(e)) {


                    end = e.getPoint();

                    Canvas currentCanvas = sd.getSketch().getCurrentCanvas();

                    multipleSelectedEntities.clear();

                    for (Entity entity : currentCanvas.getEntities()) {
                        if (entity instanceof Node) {
                            if (pathIsInRegion(((Node) entity).getBounds(), currentCanvas.toCanvasSpace(currentRegion.getBounds()))) {
                                multipleSelectedEntities.add(entity);
                            }
                        }
                    }


                    sd.repaint();

                }
            }
        });
    }

    private AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);

    private BasicStroke outline = new BasicStroke(3);

    @Override
    public void draw(Canvas withRespectTo, Graphics2D g2d) {
        super.draw(withRespectTo, g2d);

        //Selection Region is always drawn as SketchDesigner coordinates
        if(start != null && end != null) {

            int width = end.x - start.x;
            int height = end.y - start.y;


            Composite oldComposite = g2d.getComposite();

            g2d.setColor(Color.CYAN);
            g2d.setComposite(alphaComposite);

            currentRegion = new Path2D.Float();


            currentRegion.moveTo(start.x, start.y);
            currentRegion.lineTo(start.x + width, start.y);
            currentRegion.lineTo(start.x + width, start.y + height);
            currentRegion.lineTo(start.x, start.y + height);
            currentRegion.lineTo(start.x, start.y);

            g2d.fill(currentRegion);

            g2d.setComposite(oldComposite);

            g2d.setStroke(outline);
            g2d.draw(currentRegion);
        }



        //Draw the multiple selected Nodes in Canvas coordinates
        Graphics2D actionGraphics = (Graphics2D) g2d.create();
        actionGraphics.translate(withRespectTo.viewX, withRespectTo.viewY);
        actionGraphics.scale(withRespectTo.zoom, withRespectTo.zoom);

        for(Entity entity : multipleSelectedEntities){

            if(entity instanceof Node) {


                Stroke oldStroke = actionGraphics.getStroke();
                Color oldColor = actionGraphics.getColor();


                actionGraphics.setStroke(outline);
                actionGraphics.setColor(Color.CYAN);
                actionGraphics.draw(((Node) entity).getBounds());


                actionGraphics.setColor(oldColor);
                actionGraphics.setStroke(oldStroke);


            }

        }
    }
}
