package scene.app;

import scene.markdown.MarkdownFileFilter;
import scene.markdown.MarkdownReader;
import scene.markdown.MarkdownWriter;
import scene.ui.DocumentAdapter;
import scene.ui.JTextEditor;
import scene.ui.JTextEditorScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;

public class EditorPanel extends Workspace {

    private JTextEditor textEditor = new JTextEditor();
    public EditorPanel() {
        super("Source");
        Workspaces.connect(this);



        addSignal(new OpenFileSignal(){
            @Override
            public void open() {

                File file = FileChoosers.showOpenDialog(MarkdownFileFilter.filter);

                if(file != null){
                    SceneManager.setFile(file);
                    MarkdownReader.load(file, textEditor.getDocument());
                }


            }
        });

        Timer timer = new Timer(1000, e -> getSignal(SaveFileSignal.class).save());

        timer.setRepeats(false);

        textEditor.getDocument().addDocumentListener(new DocumentAdapter(){
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
                if(SceneManager.getFile() != null){

                    System.out.println("Windows Line Ending: " + textEditor.getText().contains("\r"));

                    MarkdownWriter.write(SceneManager.getFile(), textEditor.getText());
                }

                //The user just opened Scene and started typing like
                //some kind of monster
                else {

                }



            }
        });




        add(new JTextEditorScrollPane(
                        textEditor,
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
