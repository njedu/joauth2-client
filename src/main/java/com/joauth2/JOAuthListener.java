package com.joauth2;

import java.util.Date;

import javax.annotation.PreDestroy;
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

	/**
	 * 检查配置文件
	 * @return
	 */
	private String checkProps() {
		StringBuilder sb = new StringBuilder("");
		Props props = Client.props;
		if (!props.containsKey("auth.app_key")) {
			sb.append("[auth.app_key] ");
		}

		if (!props.containsKey("auth.app_secret")) {
			sb.append("[auth.app_secret] ");
		}

		if (!props.containsKey("auth.url")) {
			sb.append("[auth.url] ");
		}

		if (!props.containsKey("auth.app_encrypt")) {
			sb.append( "[auth.app_encrypt] " );
		}

		if (!props.containsKey("auth.redirect_uri")) {
			sb.append( "[auth.redirect_uri] " );
		}

		return sb.toString();
	}

	public JOAuthListener() {
		String localIp = AuthSecureUtils.getInnetIp();
		log.info("当前IP: {}", localIp);

		// 检测配置文件
		String checkPropsStr = checkProps();
		if (StrUtil.isNotEmpty(checkPropsStr)) {
			MESSAGE = OAuth2Constants.INVALID_PROPERTIES + checkPropsStr;
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

	/**
	 * 在服务器卸载Servlet时运行
	 */
	@PreDestroy
	public void servletDestoryed(){
		ClientLogin.initApp();
	}

}
