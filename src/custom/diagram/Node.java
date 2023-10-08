package custom.diagram;

import custom.Appearance;
import custom.diagram.compose.TextComponent;
import custom.diagram.compose.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

public class Node extends Entity {

    private Rectangle bounds = new Rectangle(60, 60);

    MouseAdapter myMouseAdapter;
    MouseMotionAdapter myMouseMotionAdapter;

    private Color accentColor;

    public Node() {
        super();

        Random rand = new Random();
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();

        accentColor = new Color(r, g, b);

        addComponent(new TextComponent("New Node"));
        addComponent(new ColorComponent(Appearance.nodeColor));
        addComponent(new FontComponent(Appearance.font));

        determineSize();
    }


    @Override
    public void create(JDiagramEditor sketchDesigner) {
        super.create(sketchDesigner);


        myMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if(SwingUtilities.isLeftMouseButton(e)) {
                    if (bounds.contains(sketchDesigner.getSketch().getCurrentCanvas().toCanvasSpace(e.getPoint()))) {
                        isSelected = !isSelected;


                    }

                    sketchDesigner.repaint();


                }
            }
        };

        myMouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if(isSelected){
                    Point canvasPoint = sketchDesigner.getSketch().getCurrentCanvas().toCanvasSpace(e.getPoint());
                    bounds.x = canvasPoint.x - bounds.width / 2;
                    bounds.y = canvasPoint.y - bounds.height / 2;

                    sketchDesigner.repaint();

                }
            }
        };

        sketchDesigner.addMouseListener(myMouseAdapter);
        sketchDesigner.addMouseMotionListener(myMouseMotionAdapter);
    }

    @Override
    public void dispose(JDiagramEditor sketchDesigner) {
        super.dispose(sketchDesigner);
        sketchDesigner.removeMouseMotionListener(myMouseMotionAdapter);
        sketchDesigner.removeMouseListener(myMouseAdapter);
    }

    public void setPosition(Point pos) {
        bounds.x = pos.x - (bounds.width / 2);
        bounds.y = pos.y - (bounds.height / 2);
    }

    public Point getPosition(){
        return new Point(bounds.x, bounds.y);
    }

    private BasicStroke selectionOutlineStroke = new BasicStroke(3);

    private int textWidth;
    private int textHeight;

    //Stupid FontMetric :)

    public void determineSize() {


        if(FontMetricsManager.metrics != null){
            TextComponent text = getComponent(TextComponent.class);

            textWidth = FontMetricsManager.metrics.stringWidth(text.Text);
            textHeight = FontMetricsManager.metrics.getHeight();

            bounds.width = MathsUtil.intUpClamp(textWidth, 150) + (Appearance.nodePadding * 2);

            bounds.height = textHeight + (Appearance.nodePadding * 2);
        }
        else {
            FontMetricsManager.fontMetricsWaiters.add(() -> determineSize());
        }

    }


    private BasicStroke basicStroke = new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    @Override
    public void draw(Graphics2D graphics2D) {



        ColorComponent color = getComponent(ColorComponent.class);
        TextComponent text = getComponent(TextComponent.class);
        FontComponent font = getComponent(FontComponent.class);



        graphics2D.setColor(color.Color);
        graphics2D.fill(bounds);


        graphics2D.setColor(Color.WHITE);

        graphics2D.setFont(font.Font);
        graphics2D.drawString(text.Text, (bounds.x + (bounds.width / 2)) - (textWidth / 2), (bounds.y + (bounds.height / 2)) + (textHeight / 4));


        graphics2D.setColor(accentColor);


        graphics2D.setStroke(basicStroke);
        graphics2D.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);





        if(isSelected){
            Stroke stroke = graphics2D.getStroke();

            graphics2D.setStroke(selectionOutlineStroke);
            graphics2D.setColor(Color.CYAN);
            graphics2D.draw(bounds);

            graphics2D.setStroke(stroke);
        }


    }


    @Override
    public void componentChanged(DataComponent d) {
        if(d instanceof TextComponent){
            determineSize();
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean contains(Point p) {
        return bounds.contains(p);
    }

    public void onLinkCompleted(Link link) {}



}
