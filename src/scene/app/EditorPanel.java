package scene.app;

import scene.ui.JEditor;
import scene.ui.JEditorScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class EditorPanel extends Workspace {
    public EditorPanel() {
        super("Source");
        addSignal(new OpenFileSignal(){
            @Override
            public void open(File f) {
                System.out.println("got signal!");
            }
        });
        Workspaces.connect(this);



        add(new JEditorScrollPane(
                new JEditor(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            BorderLayout.CENTER
        );




    }

    public static class OpenFileSignal implements WorkspaceSignal {
        public void open(File f){}
    }
}
