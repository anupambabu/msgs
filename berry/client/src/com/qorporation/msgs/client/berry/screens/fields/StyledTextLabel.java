package com.qorporation.msgs.client.berry.screens.fields;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;

public class StyledTextLabel extends LabelField {
    
    private int color = Color.BLACK;

    public StyledTextLabel(String text, long style, int color) {
        this(text, style, color, Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight() - 1));
    }
    
    public StyledTextLabel(String text, long style, int color, Font font) {
        super(text, 0, text.length(), style | Field.READONLY);
        this.setFont(font);
        this.color = color;
    }

	protected void paint(Graphics g) {
        g.setColor(this.color);
        super.paint(g);
    }
    
} 
