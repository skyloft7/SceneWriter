package custom.diagram;

import java.awt.*;
import java.util.LinkedList;
import java.util.function.Predicate;

public class Canvas {

    public int viewX = 0, viewY = 0;

    public float zoom = 1;


    private int maxZoomSteps = 2;


    private LinkedList<Entity> entities = new LinkedList<>();

    private float zoomIn = 2f, zoomOut = 0.5f;
    private long id;


    public void zoomIn(){
        this.zoom *= zoomIn;
        this.zoom = MathsUtil.floatClamp(this.zoom, (float) Math.pow(zoomIn, maxZoomSteps), (float) Math.pow(zoomOut, maxZoomSteps));
    }

    public LinkedList<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity en){
        entities.add(en);
    }

    public void removeEntity(Entity en) { entities.remove(en); }

    public void removeIf(JDiagramEditor sd, Predicate<Entity> condition){
        entities.removeIf(entity -> {

            //Remove <condition> Nodes
            if(condition.test(entity)) {
                entity.dispose(sd);
                return true;
            }
            return false;
        });


        //Remove stale Links
        entities.removeIf(entity -> {

            if(entity instanceof Link){
                Node plug = ((Link) entity).getPlug();
                Node socket = ((Link) entity).getSocket();

                if(plug.isDisposed() || socket.isDisposed()){
                    entity.dispose(sd);


                    return true;
                }
            }

            return false;
        });



        sd.repaint();

    }




    public void zoomOut(){
        this.zoom *= zoomOut;
        this.zoom = MathsUtil.floatClamp(this.zoom, (float) Math.pow(zoomIn, maxZoomSteps), (float) Math.pow(zoomOut, maxZoomSteps));
    }

    public Point toCanvasSpace(Point scr){

        if(scr == null){
            return new Point(0, 0);
        }

        return new Point((int) ((scr.x - viewX) / zoom), (int) ((scr.y - viewY) / zoom));
    }

    public Rectangle toCanvasSpace(Rectangle rect){

        Point start = toCanvasSpace(new Point(rect.x, rect.y));
        Point end = toCanvasSpace(new Point(rect.x + rect.width, rect.y + rect.height));

        return new Rectangle(start.x, start.y, end.x - start.x, end.y - start.y);
    }



    public void draw(Graphics2D g) {

        Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.translate(viewX, viewY);
        graphics2D.scale(zoom, zoom);

        for(Entity entity : entities){
            entity.draw(graphics2D);
        }

    }

    public void removeAllLinksFor(JDiagramEditor s, Node node) {
        entities.removeIf(possibleLink -> {
            if (possibleLink instanceof Link) {
                Link link = (Link) possibleLink;
                if(link.getPlug() == node || link.getSocket() == node){
                    link.dispose(s);
                    return true;
                }
            }
            return false;
        });
    }

    public Node findNodeAt(Point p){
        for(Entity entity : entities){
            if(entity instanceof Node){
                if( ((Node) entity).getBounds().contains(p))
                    return ((Node) entity);
            }
        }

        return null;
    }

    public Node getCurrentlySelectedNode(JDiagramEditor JDiagramEditor){
        Canvas canvas = JDiagramEditor.getSketch().getCurrentCanvas();

        for(Entity entity : canvas.getEntities()) {
            if (entity.isSelected() && entity instanceof Node) {
                return (Node) entity;
            }
        }

        return null;
    }



    public Point toScreenSpace(Point canvasPoint) {
        return new Point((int) ((canvasPoint.x * zoom) + viewX), (int) ((canvasPoint.y * zoom) + viewY));
    }

    public Point findZoomOffsetScr(Point mouseBeforeZoom, Point mouseAfterZoom){
        return new Point((int) ((mouseAfterZoom.x - mouseBeforeZoom.x) * zoom), (int) ((mouseAfterZoom.y - mouseBeforeZoom.y) * zoom));
    }



    public void setZoom(float zoom) {

        if(zoom > 0)
            this.zoom = zoom;
        else
            throw new IllegalArgumentException("Zoom cannot be less than 0! " + zoom + " is not a valid zoom level!");
    }

    public float getZoom() {
        return zoom;
    }

    public void addToFirst(Entity entity) {
        entities.addFirst(entity);
    }

    public void activate(JDiagramEditor sd){
        for(Entity entity : entities){
            entity.create(sd);
        }
    }

    public void deactivate(JDiagramEditor sd){
        for(Entity entity : entities){
            entity.dispose(sd);
        }
    }


    public void setMyID(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
