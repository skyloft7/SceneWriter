package scene.ui.diagram.actions;

import scene.ui.diagram.JDiagramEditor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DeleteAnyEntityAction extends UserAction {
    @Override
    public void onSetup(JDiagramEditor sd) {
        sd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_DELETE) {

                    sd.getSketch().getCurrentCanvas().removeIf(sd, entity -> entity.isSelected());

                }
            }
        });
    }



}
