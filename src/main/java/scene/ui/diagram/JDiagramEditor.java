package scene.ui.diagram;

import scene.ui.Appearance;
import scene.ui.diagram.actions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class JDiagramEditor extends JPanel {

    private Sketch sketch = new Sketch();

    private ArrayList<UserAction> actions = new ArrayList<>();

    public JDiagramEditor(){

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });


        sketch.pushCanvas(new Canvas());


        setSketch(sketch);
        addAction(new CanvasInteraction());
        addAction(new CreateNewNodeAction());
        addAction(new CreateNewLinkAction());
        addAction(new DeleteAnyEntityAction());
        addAction(new InspectEntityAction());
        addAction(new MultipleSelectionAction());


        setFocusable(true);
        requestFocus();

    }




    public void setSketch(Sketch sketch) {

        this.sketch.getCurrentCanvas().deactivate(this);

        this.sketch = sketch;

        //For new Sketches, this line won't do anything, but for already saved Sketches, it should work
        sketch.getCurrentCanvas().activate(this);
        repaint();
    }

    public Sketch getSketch() {

        return sketch;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        FontMetricsManager.metrics = g.getFontMetrics(Appearance.font);


        for (Iterator<FontMetricsWaiter> iterator = FontMetricsManager.fontMetricsWaiters.iterator(); iterator.hasNext(); ) {
            iterator.next().onFontMetricsAvailable();
            iterator.remove();
        }

        Graphics2D graphics2D = (Graphics2D) g;

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Canvas canvas = sketch.getCurrentCanvas();

        //copy
        canvas.draw(graphics2D);

        //original
        for(UserAction a : actions){
            a.draw(canvas, graphics2D);
        }



    }


    public void addAction(UserAction a){
        a.onSetup(this);
        actions.add(a);
    }
}
