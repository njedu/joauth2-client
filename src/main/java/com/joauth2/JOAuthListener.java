package com.joauth2;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

@WebListener
public class JOAuthListener implements HttpSessionListener, HttpSessionAttributeListener {

	private static Log log = LogFactory.get(JOAuthListener.class);

	/**
	 * Servlet初始化时调用
	 */
	public void servletInit(){

	}

	/**
	 * 在服务器卸载Servlet时运行
	 */
	public void servletDestoryed(){

	}


	public JOAuthListener() {}
	
	@Override
	public void attributeAdded(HttpSessionBindingEvent event) {

	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent event) {
		
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent arg0) {
		
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		log.info("session created");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		if (Attr.TOTAL_USER > 0) {
			Attr.TOTAL_USER --;
		}

		HttpSession session = se.getSession();

		// 删除授权平台上保存的登录信息
		if (session.getAttribute(OAuth2Constants.SESSION_LOGIN_RECORD_ID) != null) {
			int id = (Integer)session.getAttribute(OAuth2Constants.SESSION_LOGIN_RECORD_ID);
			ClientLogin.saveLogoutInfo(id);
		}

		// 清空session
		ClientUser user = null;
		if (session.getAttribute(OAuth2Constants.SESSION_CLIENT_ATTR) != null) {
			user = (ClientUser) session.getAttribute(OAuth2Constants.SESSION_CLIENT_ATTR);
			ClientLogin.logout(user.getId(), session, OAuth2Constants.SESSION_CLIENT_ATTR);
		}
	}



}
