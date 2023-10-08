package custom.diagram.actions;

import custom.diagram.Canvas;
import custom.diagram.JDiagramEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CanvasInteraction extends UserAction {

    private int startPanX, startPanY;

    private long lastMouseScroll;
    private long scrollDelayMs = 200;

    private int keyNavigateSpeed = 40;
    @Override
    public void onSetup(JDiagramEditor sd) {



        sd.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    Canvas canvas = sd.getSketch().getCurrentCanvas();


                    canvas.viewX += (e.getX() - startPanX);
                    canvas.viewY += (e.getY() - startPanY);


                    startPanX = e.getX();
                    startPanY = e.getY();

                    sd.repaint();
                }
            }
        });

        sd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                Canvas canvas = sd.getSketch().getCurrentCanvas();


                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    canvas.viewY += keyNavigateSpeed;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    canvas.viewY -= keyNavigateSpeed;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    canvas.viewX -= keyNavigateSpeed;
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    canvas.viewX += keyNavigateSpeed;
                }

                sd.repaint();
            }
        });

        sd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_PERIOD) {
                    Canvas canvas = sd.getSketch().getCurrentCanvas();
                    doZoom(new Point(sd.getWidth() / 2, sd.getHeight() / 2), 1, canvas);
                }
                if (e.getKeyCode() == KeyEvent.VK_COMMA) {
                    Canvas canvas = sd.getSketch().getCurrentCanvas();
                    doZoom(new Point(sd.getWidth() / 2, sd.getHeight() / 2), -1, canvas);
                }

            }
        });

        sd.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    startPanX = e.getX();
                    startPanY = e.getY();
                }
            }
        });


        sd.addMouseWheelListener(e -> {

            double preciseWheelRotation = e.getPreciseWheelRotation();

            boolean scrollDelayHasPassed = System.currentTimeMillis() - lastMouseScroll >= scrollDelayMs;
            boolean isHighResWheel = !(preciseWheelRotation == 1 || preciseWheelRotation == -1);


            Canvas canvas = sd.getSketch().getCurrentCanvas();

            //Check for High Res Mouse Wheel (or Laptop Trackpad)
            if (isHighResWheel) {
                //Throttle back the number of MouseWheelEvents fired by this hi res mouse
                if (scrollDelayHasPassed) {
                    preciseWheelRotation = Math.signum(preciseWheelRotation);
                    lastMouseScroll = System.currentTimeMillis();
                    doZoom(e.getPoint(), preciseWheelRotation, canvas);
                }
            }
            //Nvm, just zoom incrementally
            else
                doZoom(e.getPoint(), preciseWheelRotation, canvas);


            sd.repaint();

        });


    }

    public void doZoom(Point p, double preciseWheelRotation, Canvas canvas){

        Point mouseBeforeZoom = canvas.toCanvasSpace(p);

        if(preciseWheelRotation > 0)
            canvas.zoomOut();
        else
            canvas.zoomIn();

        Point mouseAfterZoom = canvas.toCanvasSpace(p);


        Point offset = canvas.findZoomOffsetScr(mouseBeforeZoom, mouseAfterZoom);


        canvas.viewX += offset.x;
        canvas.viewY += offset.y;
    }
}
