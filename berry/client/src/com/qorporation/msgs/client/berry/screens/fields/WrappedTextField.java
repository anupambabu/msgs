package com.qorporation.msgs.client.berry.screens.fields;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.TextField;

public class WrappedTextField extends TextField {
    
    private int color = Color.BLACK;

    public WrappedTextField(String text, long style, int color) {
        this(text, style, color, Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight() - 1));
    }
    
    public WrappedTextField(String text, long style, int color, Font font) {
        super(style | Field.READONLY); 
        this.setText(text);
        this.setLabel("");
        this.setFont(font);
        this.color = color;
    }

	protected void paint(Graphics g) {
        g.setColor(this.color);
        super.paint(g);
    }
    
} 
