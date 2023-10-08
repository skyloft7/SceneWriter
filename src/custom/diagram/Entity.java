package custom.diagram;

import custom.diagram.compose.DataComponent;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class Entity {


    private boolean disposed;

    public long getMyID() {
        return myID;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void setMyID(long myID) {
        this.myID = myID;
    }

    protected boolean isSelected;

    private long myID;

    public void create(JDiagramEditor sketchDesigner){
        disposed = false;
    };

    public abstract void draw(Graphics2D graphics2D);

    public Entity(){}

    public boolean isSelected() {
        return isSelected;
    }

    public void dispose(JDiagramEditor sketchDesigner) {
        disposed = true;
    }

    protected LinkedHashMap<String, DataComponent> myFields = new LinkedHashMap<>();

    public void addComponent(DataComponent dataComponent){
        myFields.put(dataComponent.getClass().getName(), dataComponent);
    }

    public <T extends DataComponent> T getComponent(Class clazz){
        return (T) myFields.get(clazz.getName());
    }
    public HashMap<String, DataComponent> getComponents() {
        return myFields;
    }

    public void setComponents(LinkedHashMap<String, DataComponent> myFields) {
        this.myFields = myFields;
    }

    public void componentChanged(DataComponent d){}

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
