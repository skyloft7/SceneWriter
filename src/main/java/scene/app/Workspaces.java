package scene.app;

import java.util.ArrayList;

public class Workspaces {
    private Workspaces(){}
    private static ArrayList<Workspace> workspaces = new ArrayList<>();

    public static void connect(Workspace workspace){
        workspaces.add(workspace);
    }

    public static Workspace get(String title){
        for(Workspace w : workspaces){
            if(w.getTitle().equals(title)){
                return w;
            }
        }
        return null;
    }



}
