package scene.app;

import scene.ui.JDockableWindow;

import java.util.ArrayList;

public class Workspace extends JDockableWindow {

    private ArrayList<WorkspaceSignal> workspaceSignals = new ArrayList<>();

    public Workspace(String title) {
        super(title);
    }

    public void addSignal(WorkspaceSignal w){
        workspaceSignals.add(w);
    }


    public <T> T getSignal(Class<T> c) {

        for(WorkspaceSignal w : workspaceSignals){

            if(c.isAssignableFrom(w.getClass())) {
                return c.cast(w);
            }
        }

        return null;
    }

    public interface WorkspaceSignal {

    }
}
