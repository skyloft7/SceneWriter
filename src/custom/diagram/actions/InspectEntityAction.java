package custom.diagram.actions;

import custom.diagram.JDiagramEditor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InspectEntityAction extends UserAction {
    @Override
    public void onSetup(JDiagramEditor sd) {
        sd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

            }
        });
    }
}
