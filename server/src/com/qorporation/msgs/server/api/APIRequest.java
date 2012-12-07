package com.qorporation.msgs.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;
import com.qorporation.qluster.view.ViewRequest;

public class APIRequest extends ViewRequest<APIView, User> {

	public APIRequest(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	public void sendResponse(Object res) {
		try{
			JSON json = null;
			
			if (res instanceof JSON) {
				json = (JSON) res;
			} else {
				json = JSONSerializer.toJSON(res);
			}
			
			String jsonString = json.toString();
			byte[] jsonStringBytes = Serialization.serialize(jsonString);
			
			this.response.setContentLength(jsonStringBytes.length);
			this.response.getOutputStream().write(jsonStringBytes);
			this.response.getOutputStream().flush();
			this.response.getOutputStream().close();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

}
