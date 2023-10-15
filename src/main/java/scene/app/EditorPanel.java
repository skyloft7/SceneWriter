package scene.app;

import scene.markdown.MarkdownLoader;
import scene.ui.JEditor;
import scene.ui.JEditorScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class EditorPanel extends Workspace {

    private JEditor jEditor = new JEditor();
    public EditorPanel() {
        super("Source");
        Workspaces.connect(this);



        addSignal(new OpenFileSignal(){
            @Override
            public void open(File f) {
                if(f != null){
                    MarkdownLoader.load(Files.readText(f), jEditor.getDocument());





                }
            }
        });




        add(new JEditorScrollPane(
                jEditor,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            BorderLayout.CENTER
        );




    }

    public static class OpenFileSignal implements WorkspaceSignal {
        public void open(File f){}
    }
}
