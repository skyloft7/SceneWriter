package custom.diagram;

import java.util.Stack;

public class Sketch extends Stack<Canvas> {


    public Canvas getCurrentCanvas(){
        return peek();
    }

    public void pushCanvas(Canvas canvas){
        push(canvas);
    }

    public void popCanvas(){
        pop();
    }
}
