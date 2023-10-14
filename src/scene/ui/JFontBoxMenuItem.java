package scene.ui;

import flatlaf.FlatLafUtils;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JFontBoxMenuItem extends JMenu {

    public int size;
    public int style;
    private int lazyLoadIndex = 0;

    private Font selectedFont;

    private JPanel panel = new JPanel();
    private GridBagConstraints cons = new GridBagConstraints();

    protected EventListenerList listenerList = new EventListenerList();




    public JFontBoxMenuItem(int size, int style, Font[] providedFonts){
        this.size = size;
        this.style = style;

        panel.setLayout(new GridBagLayout());
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 0;
        cons.gridx = 0;



        JScrollPane jScrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(jScrollPane);

        lazyLoadFonts(10, providedFonts);

        jScrollPane.getVerticalScrollBar().getModel().addChangeListener(event -> {
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

    private void lazyLoadFonts(int num, Font[] providedFonts) {



        int offset = 0;

        for (int i = lazyLoadIndex; i < lazyLoadIndex + num; i++) {



            //Do we actually have a font this far down?
            if(i < providedFonts.length) {

                Font font = providedFonts[i];

                JButton fontOption = new JButton(font.getFontName());
                fontOption.setBackground((Color) UIManager.get("Panel.background"));
                fontOption.setBorderPainted(false);

                fontOption.addActionListener(e -> {
                    selectedFont = font;
                    setSelectedFont(selectedFont.getFamily());
                    fireFontEvent();
                });

                fontOption.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        fontOption.setBackground(FlatLafUtils.accentColor);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        fontOption.setBackground((Color) UIManager.get("Panel.background"));
                    }
                });

                fontOption.setFont(new Font(providedFonts[i].getFontName(), style, size));
                fontOption.setHorizontalAlignment(SwingConstants.LEFT);

                panel.add(fontOption, cons);

                offset++;
            }
            else return;
        }

        lazyLoadIndex += offset;
    }

    private void fireFontEvent(){
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i+2) {

            if (listeners[i] == FontListener.class) {
                FontListener value = (FontListener) listeners[i + 1];

                value.onFontSelected(selectedFont);

            }
        }


    }

    public void addFontListener(FontListener listener) {
        listenerList.add(FontListener.class, listener);
    }
    public void removeFontListener(FontListener listener) {
        listenerList.remove(FontListener.class, listener);
    }

    public void setSelectedFont(String fontFamily) {
        setText(fontFamily);
        setFont(new Font(fontFamily, style, size));
    }
}
