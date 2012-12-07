package com.qorporation.msgs.client.berry.screens.fields;

import com.qorporation.msgs.client.berry.screens.delegates.RedrawResponder;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class GroupExpander extends VerticalFieldManager {

	private String label = null;
	private GroupHeader header = null;
	private Field group = null;
	private boolean expanded = false;
	private RedrawResponder container = null;
	
	public GroupExpander(String label, Field group, RedrawResponder container, boolean expanded) {
		super(Manager.VERTICAL_SCROLL | Manager.USE_ALL_WIDTH);
		
		this.label = label;
		this.header = new GroupHeader(this.label);
		this.group = group;
		this.container = container;
		this.expanded = expanded;
		
		this.add(header);
		if (this.expanded) {
			this.add(this.group);
		}
	}
	
	private class GroupHeader extends Field {
		private String label = null;
		private Font font = null;
		
		public GroupHeader(String label) {
			this.label = label;
			this.font = Font.getDefault().derive(Font.PLAIN, Font.getDefault().getHeight() + 1);
		}
		
        protected void paint(Graphics g){                  
            g.drawShadedFilledPath(new int[]{0, 0, this.getWidth(), this.getWidth()}, 
            						new int[]{0, this.getHeight(), this.getHeight(), 0}, 
            						null, 
            						new int[]{0xFFFFFF, 0xCCCCCC, 0xCCCCCC, 0xFFFFFF}, 
            						null);

            g.setColor(0xCCCCCC);
            g.drawLine(0, 0, this.getWidth(), 0);
            
            g.setColor(0x000000);
            g.setFont(this.font);
            g.drawText(this.label, 10, 10);
        }

		protected void layout(int width, int height) {
			this.setExtent(width, this.getPreferredHeight());
		}
		
		public int getPreferredHeight() {
			return this.font.getHeight() + 20;
		}
		
		public int getPreferredWidth() {
			return Display.getWidth();
		}
		
		public boolean isFocusable() {  
			return true;  
		} 
		
		protected boolean keyChar(char character, int status, int time) {  
		    if (character == Keypad.KEY_ENTER) {
		    	GroupExpander.this.toggle();
		        return true;  
		    }
		    
		    return super.keyChar(character, status, time);  
		}  
		  
		protected boolean navigationClick(int status, int time) {
			GroupExpander.this.toggle();
		    return true;  
		}
	}

	protected void toggle() {
		synchronized (Application.getEventLock()) {
			this.expanded = !this.expanded;
			
			if (this.expanded) {
				this.add(this.group);
			} else {
				this.delete(this.group);
			}
			
			this.updateLayout();
			this.container.redraw(this);
		}
	}

	public void hide() {
		if (this.getFieldCount() < 1) return;
		
		this.delete(this.header);
		this.delete(this.group);
		
		this.updateLayout();
		this.container.redraw(this);
	}

	public void show() {
		if (this.getFieldCount() > 0) return;
		
		this.add(this.header);
		
		if (this.expanded) {
			this.add(this.group);
		}
		
		this.updateLayout();
		this.container.redraw(this);
	}

}
