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
import java.io.IOException;

public class EditorPanel extends Workspace {

    private JTextEditor textEditor = new JTextEditor();
    public EditorPanel() {
        super("Source");
        Workspaces.connect(this);





        addSignal(new OpenFileSignal(){
            @Override
            public void open() {
                open(FileChoosers.showOpenDialog(MarkdownFileFilter.filter));
            }


            @Override
            public void openNew() {
                File file = FileChoosers.showCreateDialog(MarkdownFileFilter.filter);


                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                catch (IOException e) {
                    System.err.println("Not allowed to create file " + file.getName() + ". do you have permissions?");
                    throw new RuntimeException(e);
                }


                open(file);
            }

            @Override
            public void open(File file) {
                if(file != null){
                    SceneManager.setFile(file);
                    MarkdownReader.load(file, textEditor);
                    SceneManager.getFrame().setTitle(SceneManager.getFile().getName());
                }
            }








        });

        Timer timer = new Timer(1000, e -> {
            getSignal(SaveFileSignal.class).save();
        });

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
                    MarkdownWriter.write(SceneManager.getFile(), textEditor.getText());
                }
            }
        });


        JTextEditorScrollPane comp = new JTextEditorScrollPane(
                textEditor,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        add(comp, BorderLayout.CENTER);






    }

    public static class OpenFileSignal implements WorkspaceSignal {
        public void open(){}
        public void open(File file){}

        public void openNew(){}
    }

    public static class SaveFileSignal implements WorkspaceSignal {
        public void save(){}
    }
}
