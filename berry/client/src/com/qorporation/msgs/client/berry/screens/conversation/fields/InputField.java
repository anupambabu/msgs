package com.qorporation.msgs.client.berry.screens.conversation.fields;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.EditField;

public class InputField extends Manager implements FieldChangeListener {
	
	public static interface InputFieldListener {
		public void redrawInputHeight();
		public void receivedInputMessage(String message);
	}
	
	private EditField editField = null;
	private InputFieldListener listener = null;
	
	public InputField(InputFieldListener listener) {
		super(0);
		
		this.listener = listener;
		
		this.editField = new AutoTextEditField("", "");
		this.editField.setChangeListener(this);
		
		this.add(this.editField);
	}
	
	public int getPreferredHeight() {
		return 50;
	}
	
	public void reset() {
		this.editField.setText("");
		this.listener.redrawInputHeight();
	}

	public void fieldChanged(Field field, int context) {
		this.listener.redrawInputHeight();
		
		if (this.editField.getTextLength() > 0 && this.editField.getText().endsWith("\n")) {
			this.listener.receivedInputMessage(this.editField.getText().substring(0, this.editField.getTextLength() - 1));
		}
	}
	
    protected void paint(Graphics g){      
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setDrawingStyle(Graphics.DRAWSTYLE_AALINES | Graphics.DRAWSTYLE_AAPOLYGONS, true);
        g.setColor(Color.WHITE);
        g.fillRoundRect(5, 5, this.getWidth() - 10, this.getHeight() - 10, 10, 10);
        g.setColor(Color.GRAY);
        g.drawRoundRect(5, 5, this.getWidth() - 10, this.getHeight() - 10, 10, 10);
        super.paint(g);
    }

	protected void sublayout(int width, int height) {
        this.layoutChild(this.editField, width - 20, Math.min(height, this.getPreferredHeight()) - 20);
        this.setPositionChild(this.editField, 10, 10);
        
        this.setExtent(width, this.editField.getHeight() + 20);
	}
	
}
