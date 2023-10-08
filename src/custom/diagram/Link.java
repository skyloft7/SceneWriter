package custom.diagram;

import custom.Appearance;
import custom.diagram.compose.*;
import custom.diagram.compose.TextComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;

public class Link extends Entity {

    private Node plug, socket;
    private Point mouse;
    private ArrayList<LinkHandle> vertices = new ArrayList<>();

    public ArrayList<LinkHandle> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<LinkHandle> vertices) {
        this.vertices = vertices;
    }

    public enum LinkState {
        UNDECIDED,
        NORMAL
    }

    private LinkState linkState;


    public Link(Point initialMousePos, Node plug){
        super();
        this.plug = plug;
        linkState = LinkState.UNDECIDED;
        isSelected = true;

        mouse = initialMousePos;

        addComponent(new ColorComponent(Appearance.neutral));
        addComponent(new FontComponent(Appearance.font));
        addComponent(new ArrowComponent(false));

    }

    public Link(){
        super();
        linkState = LinkState.UNDECIDED;
        isSelected = true;

        mouse = new Point(0, 0);

        addComponent(new ColorComponent(Appearance.neutral));
        addComponent(new FontComponent(Appearance.font));
        addComponent(new ArrowComponent(false));

    }



    private int selectionProximity = 20;
    private int selectionProxSqrd = (int) Math.pow(selectionProximity, 2);

    private Point textPos;

    private int textWidth = 0;
    private int textHeight = 0;


    MouseAdapter myMouseAdapter;
    MouseMotionAdapter myMouseMotionAdapter;

    MouseMotionAdapter myLinkHandleMotionAdapter;

    MouseAdapter myHandleSelectionAdapter;
    KeyAdapter myLinkHandleKeyAdapter;



    @Override
    public void create(JDiagramEditor sketchDesigner) {
        super.create(sketchDesigner);
        myMouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

                Canvas canvas = sketchDesigner.getSketch().getCurrentCanvas();
                if(getLinkState() == LinkState.UNDECIDED) {
                    mouse = canvas.toCanvasSpace(e.getPoint());
                    sketchDesigner.repaint();
                }

                if(getLinkState() == LinkState.NORMAL){
                    Rectangle plugBounds = plug.getBounds();
                    Rectangle socketBounds = socket.getBounds();

                    Point canvasMouse = canvas.toCanvasSpace(e.getPoint());

                    isSelected = checkIfSelected(
                            plugBounds.x + plugBounds.width / 2,
                            plugBounds.y + plugBounds.height / 2,
                            socketBounds.x + socketBounds.width / 2,
                            socketBounds.y + socketBounds.height / 2,
                            canvasMouse.x,
                            canvasMouse.y
                    );

                    sketchDesigner.repaint();

                }
            }
        };

        myMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(getLinkState() == LinkState.UNDECIDED && SwingUtilities.isRightMouseButton(e)){
                    Point p = sketchDesigner.getSketch().getCurrentCanvas().toCanvasSpace(e.getPoint());
                    LinkHandle possibleVertex = new LinkHandle(p.x, p.y);
                    vertices.add(possibleVertex);

                }
            }
        };

        myLinkHandleMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if(linkState == LinkState.NORMAL) {
                    Point mouse = sketchDesigner.getSketch().getCurrentCanvas().toCanvasSpace(e.getPoint());

                    for (LinkHandle linkHandle : vertices) {

                        int deltaX = linkHandle.x - mouse.x;
                        int deltaY = linkHandle.y - mouse.y;

                        int hypotenuseSquared = deltaX * deltaX + deltaY * deltaY;

                        if (hypotenuseSquared <= LinkHandle.proximity * LinkHandle.proximity) {
                            linkHandle.setHighlighted(true);
                        } else {
                            linkHandle.setHighlighted(false);
                        }

                        sketchDesigner.repaint();

                    }

                    for(LinkHandle linkHandle : vertices){
                        if(linkHandle.isSelected()) {
                            setSelected(false);
                            Canvas canvas = sketchDesigner.getSketch().getCurrentCanvas();

                            Point p = canvas.toCanvasSpace(e.getPoint());

                            linkHandle.x = p.x;
                            linkHandle.y = p.y;

                            sketchDesigner.repaint();

                        }
                    }
                }


            }
        };

        myHandleSelectionAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    for (LinkHandle linkHandle : vertices) {
                        if (linkHandle.isHighlighted()) {

                            Link.this.setSelected(false);
                            linkHandle.setSelected(!linkHandle.isSelected());
                            sketchDesigner.repaint();
                        }
                    }
                }
            }
        };

        myLinkHandleKeyAdapter = new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {

                if(linkState == LinkState.NORMAL) {

                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        for (Iterator<LinkHandle> iterator = vertices.iterator(); iterator.hasNext(); ) {
                            LinkHandle linkHandle = iterator.next();
                            if (linkHandle.isSelected()) {
                                iterator.remove();
                                sketchDesigner.repaint();
                            }
                        }
                    }

                }
            }
        };



        sketchDesigner.addMouseMotionListener(myMouseMotionAdapter);
        sketchDesigner.addMouseListener(myMouseAdapter);
        sketchDesigner.addMouseMotionListener(myLinkHandleMotionAdapter);
        sketchDesigner.addMouseListener(myHandleSelectionAdapter);
        sketchDesigner.addKeyListener(myLinkHandleKeyAdapter);

    }

    @Override
    public void dispose(JDiagramEditor sketchDesigner) {
        super.dispose(sketchDesigner);
        sketchDesigner.removeMouseMotionListener(myMouseMotionAdapter);
        sketchDesigner.removeMouseMotionListener(myLinkHandleMotionAdapter);
        sketchDesigner.removeMouseListener(myMouseAdapter);
        sketchDesigner.removeMouseListener(myHandleSelectionAdapter);
        sketchDesigner.removeKeyListener(myLinkHandleKeyAdapter);
    }


    @Override
    public void componentChanged(DataComponent d) {
        if(d instanceof TextComponent && linkState == LinkState.NORMAL){
            determineTextSize();
            textPos = findSuitableTextPos();
        }
    }



    public Node getSocket() {
        return socket;
    }

    public void setSocket(Node socket) {
        this.socket = socket;
        linkState = LinkState.NORMAL;
        isSelected = false;

        //Sometimes we set a socket to a different node
        if(!getComponents().containsKey(TextComponent.class.getName()))
            addComponent(new TextComponent(""));



        vertices.removeIf(linkHandle -> socket.getBounds().contains(linkHandle));
    }

    private BasicStroke basicStroke = new BasicStroke(4);

    public void setPlug(Node plug) {
        this.plug = plug;
    }

    public Node getPlug() {
        return plug;
    }

    @Override
    public void draw(Graphics2D graphics2D) {
        System.out.println(getClass().getName());


        graphics2D.setStroke(basicStroke);

        if(isSelected)
            graphics2D.setColor(Color.RED);
        else {
            ColorComponent colorComponent = getComponent(ColorComponent.class);
            graphics2D.setColor(colorComponent.Color);

        }

        Rectangle plugBounds = plug.getBounds();




        if(linkState == LinkState.UNDECIDED){

            //Draw the normal path to the mouse pos
            drawPath(
                    graphics2D,
                    plugBounds.x + plugBounds.width / 2,
                    plugBounds.y + plugBounds.height / 2,
                    mouse.x,
                    mouse.y
            );

        }

        else if(linkState == LinkState.NORMAL) {
            Rectangle socketBounds = socket.getBounds();


            //Draw the normal path
            drawPath(
                    graphics2D,
                    plugBounds.x + plugBounds.width / 2,
                    plugBounds.y + plugBounds.height / 2,
                    socketBounds.x + socketBounds.width / 2,
                    socketBounds.y + socketBounds.height / 2
            );

            TextComponent textComponent = getComponent(TextComponent.class);

            ArrowComponent arrowComponent = getComponent(ArrowComponent.class);

            textPos = findSuitableTextPos();

            drawText(textPos, textComponent.Text, graphics2D);


            if(arrowComponent.Arrowed){
                Graphics2D g = (Graphics2D) graphics2D.create();


                double theta = findThetaForArrow();

                Point arrowPos = findSuitableArrowPos();

                AffineTransform at = AffineTransform.getTranslateInstance(arrowPos.x, arrowPos.y);

                at.concatenate(AffineTransform.getRotateInstance(theta));
                g.transform(at);


                //All coordinates are relative to theta
                int ARROW_SIZE = 15;

                g.fillPolygon(
                        new int[]{0, 0, ARROW_SIZE, 0},
                        new int[]{0, ARROW_SIZE, 0, -ARROW_SIZE},
                        4
                );


                g.dispose();

            }

        }

        if(linkState == LinkState.NORMAL){
            for(LinkHandle linkHandle : vertices){
                if(linkHandle.isHighlighted()){


                    graphics2D.setColor(Color.RED);


                    graphics2D.fillOval(
                            linkHandle.x - (LinkHandle.proximity / 2),
                            linkHandle.y - (LinkHandle.proximity / 2),
                            LinkHandle.proximity,
                            LinkHandle.proximity
                    );

                    if(linkHandle.isSelected()){
                        graphics2D.setStroke(new BasicStroke(3));
                        graphics2D.setColor(Color.CYAN);
                        graphics2D.drawOval(
                                linkHandle.x - (LinkHandle.proximity / 2),
                                linkHandle.y - (LinkHandle.proximity / 2),
                                LinkHandle.proximity,
                                LinkHandle.proximity
                        );
                    }


                }
            }
        }

    }



    public void determineTextSize(){

        if(FontMetricsManager.metrics != null){
            TextComponent text = getComponent(TextComponent.class);


            /*

            Iterable<String> pieces = Splitter.fixedLength(Appearance.maxCharsPerLine).split(text.Text);


            textWidth = UIUtil.maxLineWidth(pieces);
            textHeight = FontMetricsManager.metrics.getHeight() * Iterators.size(pieces.iterator());


             */
        }
        else {
            FontMetricsManager.fontMetricsWaiters.add(() -> determineTextSize());
        }
    }

    public void drawText(Point textCenterPos, String text, Graphics2D graphics2D){
        /*

        if(!text.isEmpty() && !text.isBlank()) {

            FontComponent fontComponent = getComponent(FontComponent.class);

            Iterable<String> pieces = Splitter.fixedLength(Appearance.maxCharsPerLine).split(text);


            graphics2D.setColor(JBColor.PanelBackground);
            graphics2D.fillRect(textCenterPos.x - (textWidth / 2), textCenterPos.y - (textHeight / 2), textWidth, textHeight);


            graphics2D.setFont(fontComponent.Font);
            graphics2D.setColor(Appearance.neutral);

            int heightIndex = 0;

            for(String string : pieces){
                heightIndex += FontMetricsManager.metrics.getHeight();
                graphics2D.drawString(string, textCenterPos.x - (textWidth / 2), ((textCenterPos.y - (textHeight / 2)) + heightIndex) - (FontMetricsManager.metrics.getHeight() / 4));
            }


        }

         */
    }


    public boolean checkIfSelected(int startX, int startY, int endX, int endY, int mouseX, int mouseY){

        for (Point vertex : vertices) {

            //Save some time by avoiding square roots
            if(Line2D.ptSegDistSq(
                    startX,
                    startY,
                    vertex.x,
                    vertex.y,
                    mouseX,
                    mouseY
            ) < selectionProxSqrd)
                return true;

            startX = vertex.x;
            startY = vertex.y;

        }

        return Line2D.ptSegDistSq(startX, startY, endX, endY, mouseX, mouseY) < selectionProxSqrd;
    }



    public void drawPath(Graphics2D graphics2D, int startX, int startY, int endX, int endY){

        for (Point vertex : vertices) {
            graphics2D.drawLine(startX, startY, vertex.x, vertex.y);

            startX = vertex.x;
            startY = vertex.y;

        }

        graphics2D.drawLine(startX, startY, endX, endY);

    }


    //Optimize
    private Point findSuitableTextPos() {

        ArrowComponent arrowComponent = getComponent(ArrowComponent.class);

        if(arrowComponent.Arrowed)
            return findSuitableTextPosWithArrow();



        int numOfBends = vertices.size();

        Point rightTextPos;

        if(numOfBends != 0) {
            //Even
            if (numOfBends % 2 == 0) {

                Point p1 = vertices.get(((numOfBends) / 2) - 1);
                Point p2 = vertices.get(((numOfBends) / 2));


                rightTextPos = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            }
            //Odd
            else {
                Point p = vertices.get(((numOfBends + 1) / 2) - 1);

                rightTextPos = (Point) p.clone();
            }
        }
        else {
            rightTextPos = new Point();



            int x1 = plug.getBounds().x + plug.getBounds().width / 2;
            int y1 = plug.getBounds().y + plug.getBounds().height / 2;

            //What if user sets text without attaching it?
            int x2 = socket.getBounds().x + socket.getBounds().width / 2;
            int y2 = socket.getBounds().y + socket.getBounds().height / 2;

            rightTextPos.x = (x1 + x2) / 2;
            rightTextPos.y = (y1 + y2) / 2;

        }
        return rightTextPos;
    }

    //Optimize
    private Point findSuitableTextPosWithArrow(){

        Point p = new Point();

        int numOfBends = vertices.size();
        float determinant = 1.5f;

        Node plug = this.plug;
        Node socket = this.socket;



        int startX = plug.getBounds().x + (plug.getBounds().width / 2);
        int startY = plug.getBounds().y + (plug.getBounds().height / 2);

        if(numOfBends != 0) {
            Point bendPoint1 = vertices.get(0);


            int endX = bendPoint1.x;
            int endY = bendPoint1.y;

            p.x = (int) (startX + ((endX - startX) / determinant));
            p.y = (int) (startY + ((endY - startY) / determinant));


        }
        else {
            int endX = socket.getBounds().x + (socket.getBounds().width / 2);
            int endY = socket.getBounds().y + (socket.getBounds().height / 2);

            p.x = (int) (startX + ((endX - startX) / determinant));
            p.y = (int) (startY + ((endY - startY) / determinant));
        }



        return p;
    }


    //Optimize
    private double findThetaForArrow(){

        double x1, x2;
        double y1, y2;

        int numOfBends = vertices.size();
        if(numOfBends != 0){
            Point p1;
            Point p2;
            if(numOfBends % 2 == 0){
                //Even
                p1 = vertices.get(((numOfBends) / 2) - 1);
                p2 = vertices.get(((numOfBends) / 2));
            }
            else {
                //Odd
                p1 = vertices.get(((numOfBends + 1) / 2) - 1);
                if(numOfBends != 1)
                    p2 = vertices.get(((numOfBends + 1) / 2));
                else
                    p2 = new Point(socket.getBounds().x + socket.getBounds().width / 2,
                            socket.getBounds().y + socket.getBounds().height / 2);
            }
            x1 = p1.x;
            y1 = p1.y;
            x2 = p2.x;
            y2 = p2.y;
        }
        else {
            x1 = plug.getBounds().x + plug.getBounds().width / 2;
            y1 = plug.getBounds().y + plug.getBounds().height / 2;

            x2 = socket.getBounds().x + socket.getBounds().width / 2;
            y2 = socket.getBounds().y + socket.getBounds().height / 2;
        }

        return Math.atan2(y2 - y1, x2 - x1);
    }


    //Optimize
    private Point findSuitableArrowPos() {

        Point j;

        int numOfBends = vertices.size();

        if(numOfBends != 0) {
            //Even
            if (numOfBends % 2 == 0) {

                Point p1 = vertices.get(((numOfBends) / 2) - 1);
                Point p2 = vertices.get(((numOfBends) / 2));


                j = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            }
            //Odd
            else {
                j = vertices.get(((numOfBends + 1) / 2) - 1);
            }
        }
        else {
            j = new Point();

            int x1 = plug.getBounds().x + plug.getBounds().width / 2;
            int y1 = plug.getBounds().y + plug.getBounds().height / 2;

            int x2 = socket.getBounds().x + socket.getBounds().width / 2;
            int y2 = socket.getBounds().y + socket.getBounds().height / 2;

            j.x = (x1 + x2) / 2;
            j.y = (y1 + y2) / 2;
        }

        return j;
    }

    public LinkState getLinkState() {
        return linkState;
    }



    public static class LinkHandle extends Point {
        public static final int proximity = 20;

        private boolean highlighted;
        private boolean selected;
        public LinkHandle(int x, int y) {
            super(x, y);
        }

        public boolean isHighlighted() {
            return highlighted;
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

}
