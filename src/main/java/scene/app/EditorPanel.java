package scene.app;

import scene.markdown.MarkdownFileFilter;
import scene.markdown.MarkdownReader;
import scene.markdown.MarkdownWriter;
import scene.ui.DocumentAdapter;
import scene.ui.FileChoosers;
import scene.ui.JEditor;
import scene.ui.JEditorScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;

public class EditorPanel extends Workspace {

    private JEditor jEditor = new JEditor();
    public EditorPanel() {
        super("Source");
        Workspaces.connect(this);



        addSignal(new OpenFileSignal(){
            @Override
            public void open() {

                File file = FileChoosers.showOpenDialog(MarkdownFileFilter.filter);

                if(file != null){
                    AppManager.setFile(file);
                    MarkdownReader.load(file, jEditor.getDocument());
                }


            }
        });

        Timer timer = new Timer(1000, e -> getSignal(SaveFileSignal.class).save());

        timer.setRepeats(false);

        jEditor.getDocument().addDocumentListener(new DocumentAdapter(){
            @Override
            public void textUpdated(DocumentEvent e) {
                super.textUpdated(e);

                if(!timer.isRunning()){
                    timer.start();
                    return;
                }

                timer.restart();
            }
        });

        addSignal(new SaveFileSignal(){
            @Override
            public void save() {
                //There is a file already opened
                if(AppManager.getFile() != null){

                    System.out.println("Windows Line Ending: " + jEditor.getText().contains("\r"));

                    MarkdownWriter.write(AppManager.getFile(), jEditor.getText());
                }

                //The user just opened Scene and started typing like
                //some kind of monster
                else {

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
        public void open(){}
    }

    public static class SaveFileSignal implements WorkspaceSignal {
        public void save(){}
    }
}
