package scene.ui;

import javax.swing.*;
import java.awt.*;

public class JFontComboBox extends JComboBox<String> {

    public int size;
    public int style;

    private int lazyLoadIndex = 0;

    public JFontComboBox(int size, int style, Font[] providedFonts){
        super.setRenderer(new FontRenderer());


        this.size = size;
        this.style = style;

        lazyLoadFonts(10, providedFonts);

        //Lazy Loading stuff
        Object comp = getUI().getAccessibleChild(this, 0);
        if (comp instanceof JPopupMenu popup) {
            JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
            scrollPane.getVerticalScrollBar().getModel().addChangeListener(event -> {
                BoundedRangeModel model = (BoundedRangeModel) event.getSource();
                int extent = model.getExtent();
                int maximum = model.getMaximum();
                int value = model.getValue();

                int pos = extent + value;

                if(pos == maximum){
                    lazyLoadFonts(10, providedFonts);
                }
            });
        }

    }

    private void lazyLoadFonts(int num, Font[] providedFonts) {

        int offset = 0;

        for (int i = lazyLoadIndex; i < lazyLoadIndex + num; i++) {

            //Do we actually have a font this far down?
            if(i < providedFonts.length) {
                super.addItem(providedFonts[i].getFontName());

                offset++;
            }
            else return;
        }

        lazyLoadIndex += offset;
    }

    class FontRenderer implements ListCellRenderer<String> {


        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel jLabel = new JLabel(value);

            jLabel.setFont(new Font(String.valueOf(value), style, size));

            return jLabel;
        }
    }
}