package com.joauth2;

import java.util.Date;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.dialect.Props;

@WebListener
public class JOAuthListener implements HttpSessionListener, HttpSessionAttributeListener {

	private static Log log = LogFactory.get(JOAuthListener.class);

	/**
	 * 支持加密
	 */
	public static boolean canEncrypt = true;
	
	/**
	 * 统一提示信息
	 */
	private static String MESSAGE = "";
	public static synchronized String getMESSAGE() {
		return MESSAGE;
	}
	public static synchronized void setMESSAGE(String message) {
		MESSAGE = message;
	}

	public JOAuthListener() {
		String localIp = AuthSecureUtils.getInnetIp();
		log.info("当前IP: {}", localIp);

		// 检测配置文件
		Props props = Client.props;
		boolean containsKey = props.containsKey("auth.app_key") &&
				props.containsKey("auth.app_secret") &&
				props.containsKey("auth.url") &&
				props.containsKey("auth.app_encrypt") &&
				props.containsKey("auth.redirect_uri");
		
		if (!containsKey) {
			MESSAGE = OAuth2Constants.INVALID_PROPERTIES;
			canEncrypt = false;
		}
		
		// 检测token和expire_in
		if (StrUtil.isNotEmpty(Client.TOKEN) && Client.END_TIME != null) {
			MESSAGE = "无效的TOKEN，请检查Client文件是否损坏";
			canEncrypt = false;
		}
		
		// 获取token
		if (canEncrypt) {
			MESSAGE = Client.getCode();
		}
		Client.MESSAGE_TMP = MESSAGE;
		log.info(MESSAGE);

		// 初始化应用
		ClientLogin.initApp();

		if (!Client.OFFLINE) {
			// 开启定时任务-刷新Token
			Client.refreshToken();
		}
	}
	
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
		if (Client.TOTAL_USER > 0) {
			Client.TOTAL_USER --;
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
