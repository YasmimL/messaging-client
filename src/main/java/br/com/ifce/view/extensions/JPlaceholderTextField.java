package br.com.ifce.view.extensions;

import javax.swing.*;
import java.awt.*;

public class JPlaceholderTextField extends JTextField {

    private final String placeholder;

    public JPlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public String getText() {
        String text = super.getText();

        if (text.trim().length() == 0) {
            return this.placeholder;
        }

        return text;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (super.getText().length() > 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(super.getDisabledTextColor());
        g2.drawString(this.placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }
}
