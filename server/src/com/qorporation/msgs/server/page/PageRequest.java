package com.qorporation.msgs.server.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;

import com.qorporation.msgs.server.entity.definition.User;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.logic.LogicService;
import com.qorporation.qluster.view.ViewRequest;

public class PageRequest extends ViewRequest<PageView, User> {
	public static final String REDIRECT_PATH = "REDIRECT_PATH";

	public EntityService entityService = null;
	public Configuration templateConfig = null;
	
	private Map<String, Object> renderVars = null;
	
	public PageRequest(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	public PageRequest(EntityService entityService, LogicService logicService, Configuration templateConfig, HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		this.entityService = entityService;
		this.templateConfig = templateConfig;
		this.renderVars = new HashMap<String, Object>();
		updateTemplateWithGlobalVariables(this.renderVars);
	}
	
	public void renderTemplate(String path) {
		try {
			Template template = this.templateConfig.getTemplate(path);
			template.process(this.renderVars, this.response.getWriter());
	        this.response.flushBuffer();
		} catch (Exception e) {
			sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public void addRenderVar(String key, Object object) {
		this.renderVars.put(key, object);
	}
	
	public Object getRenderVar(String key) {
		return this.renderVars.get(key);
	}

	private void updateTemplateWithGlobalVariables(Map<String, Object> root) {
		Entity<User> user = this.getUser();
		root.put("me", user);
	}
	
	public void redirectTo(String path) {
		try {
			path = this.response.encodeRedirectURL(path);
			this.response.sendRedirect(path);
			this.response.flushBuffer();
		} catch (IOException e) {
			sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
