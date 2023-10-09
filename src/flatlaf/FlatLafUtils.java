package flatlaf;

import javax.swing.*;
import java.awt.*;

public class FlatLafUtils {
    public static Color accentColor;

    public static void setup() {
        accentColor = UIManager.getColor( "Component.accentColor");
    }
}
