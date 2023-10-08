package custom.diagram;

public class MathsUtil {
    public static float floatClamp(float input, float max, float min){
        if(input > max)
            return max;
        if(input < min)
            return min;

        return input;
    }

    public static int intClamp(int input, int max, int min){
        if(input > max)
            return max;
        if(input < min)
            return min;

        return input;
    }

    public static int intUpClamp(int input, int min){
        if(input > min)
            return input;
        if(input < min)
            return min;
        return input;
    }


}
