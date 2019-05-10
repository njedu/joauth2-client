package com.joauth2;

import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.joauth2.upgrade.FileManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Servlet监听
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/5/7
 */
@WebListener
public class JOAuthContextListener implements ServletContextListener{

    private Log log = LogFactory.get();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String localIp = AuthSecureUtils.getInnetIp();
        log.info("当前IP: {}", localIp);

        // 检测配置文件
        String checkPropsStr = checkProps();
        if (StrUtil.isNotEmpty(checkPropsStr)) {
            Attr.setMessage(OAuth2Constants.INVALID_PROPERTIES + checkPropsStr);
            Attr.canEncrypt = false;
        }

        // 检测token和expire_in
        if (StrUtil.isNotEmpty(Attr.TOKEN) && Attr.INTERVALS != null) {
            Attr.setMessage("无效的TOKEN，请检查Client文件是否损坏");
            Attr.canEncrypt = false;
        }

        // 获取token
        if (Attr.canEncrypt) {
            Attr.setMessage(Client.getCode());
        }
        Attr.MESSAGE_TMP = Attr.getMessage();
        log.info(Attr.getMessage());

        // 初始化应用
        ClientLogin.initApp();

        if (!Attr.OFFLINE && StrUtil.isNotEmpty(Attr.TOKEN)) {
            // 延迟刷新Client数据
            Scheduler.refershClient();
            // 开启自动更新
            FileManager.autoUpgrade();
            // 开启定时任务
            CronUtil.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.debug("------------contextDestroyed------------");
        ClientLogin.initApp();
        Client.offline();
    }

    /**
     * 检查配置文件
     * @return
     */
    private String checkProps() {
        StringBuilder sb = new StringBuilder("");
        Props props = null;

        // 读取配置文件
        try {
            Attr.props = new Props("application.properties");
            props = Attr.props;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (props == null) {
            log.error("缺少配置项，请检查配置文件application.properties和授权相关配置");
        }

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

        return sb.toString();
    }
}
