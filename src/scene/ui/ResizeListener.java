package scene.ui;

public interface ResizeListener {

    enum ResizeAxis {
        HORIZONTAL,
        VERTICAL
    }

    void onResized(ResizeAxis resizeAxis);
}
