package custom;

public interface ResizeListener {

    enum ResizeAxis {
        HORIZONTAL,
        VERTICAL
    }

    void onResized(ResizeAxis resizeAxis);
}
