package com.qorporation.msgs.client.berry.screens.settings;

import javax.microedition.global.Formatter;

import com.qorporation.msgs.client.berry.networking.HttpRequest;
import com.qorporation.msgs.client.berry.networking.HttpRequestDelegate;
import com.qorporation.msgs.client.berry.store.AccountStore;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

public class SettingsScreen extends MainScreen {

	private EditField nameField = null;
	private EditField emailField = null;
	private EditField phoneField = null;
	private ButtonField saveButton = null;
	private ButtonField cancelButton = null;
	
	public SettingsScreen() {
		this.setTitle("Settings");
		
		this.nameField = new EditField("Name: ", "", 255, 0);
		this.emailField = new EditField("Email: ", "", 255, EditField.FILTER_EMAIL);
		this.phoneField = new EditField("Phone: ", "", 255, EditField.FILTER_PHONE);
		
		this.saveButton = new ButtonField("Save", ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
		this.saveButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context){
            	HttpRequest req = new HttpRequest(Formatter.formatMessage(
        				HttpRequest.MSGS_UPDATE_ACCOUNT_FORMAT,
        				new String[] {
        					SettingsScreen.this.nameField.getText(),
        					SettingsScreen.this.emailField.getText(),
        					SettingsScreen.this.phoneField.getText(),
        					AccountStore.getInstance().getRequestAuthentication()
        				}), new HttpRequestDelegate() {
							public void onRequestSending(int sent, int total, HttpRequest request) {}
							public void onRequestReceiving(int received, int total, HttpRequest request) {}
							public void onRequestFailure(HttpRequest request) {}
							public void onRequestComplete(String response, HttpRequest request) {
								SettingsScreen.this.setClean();
							}
            			});
            	
            	req.send();  
			}});
		
		this.cancelButton = new ButtonField("Cancel");
		this.cancelButton.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				SettingsScreen.this.reloadData();
			}});
		
		this.add(this.nameField);
		this.add(this.emailField);
		this.add(this.phoneField);
		
		this.add(new HorizontalFieldManager() {{
			this.add(SettingsScreen.this.saveButton);
			this.add(SettingsScreen.this.cancelButton);
		}});
		
		this.reloadData();
	}

	private void setClean() {
		this.nameField.setDirty(false);
		this.emailField.setDirty(false);
		this.phoneField.setDirty(false);
	}

	private void reloadData() {
		this.nameField.setText(AccountStore.getInstance().getUserName());
		
		this.emailField.setText(AccountStore.getInstance().getUserEmail());
		String pendingEmail = AccountStore.getInstance().getUserEmailPending();
		if (pendingEmail.length() > 0 && !this.emailField.getText().equals(pendingEmail)) {
			this.emailField.setText(pendingEmail);
		}
		
		this.phoneField.setText(AccountStore.getInstance().getUserPhone());
		String pendingPhone = AccountStore.getInstance().getUserPhonePending();
		if (pendingPhone.length() > 0 && !this.phoneField.getText().equals(pendingPhone)) {
			this.phoneField.setText(pendingPhone);
		}
		
		this.setClean();
	}
	
}
