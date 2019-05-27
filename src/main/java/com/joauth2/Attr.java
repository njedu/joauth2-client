package com.joauth2;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.Date;

/**
 * 静态变量统一管理
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/5/6
 */
public class Attr {


    // 令牌
    public static String TOKEN = "";
    // 间隔结束时间
    public static Date END_TIME = null;
    // 请求间隔
    public static Integer INTERVALS = 0;
    // 当前用户数量
    public static int TOTAL_USER = 0;
    // 最大用户数量
    public static int MAX_USER = 0;
    // 离线模式
    public static boolean OFFLINE = false;
    // 用于输出的信息
    public static String MESSAGE_TMP = "";
    // 配置文件
    public static Props props;
    // Cron的ID
    public static String CRON_UPGRADE_ID = null;
    public static String CRON_APPDATA_ID = null;

    // 支持加密
    public static boolean canEncrypt = true;

    // 统一提示信息
    private static String message = "";
    public static synchronized String getMessage() {
        return message;
    }
    public static synchronized void setMessage(String message, boolean canEncrypt) {
        Attr.message = message;
        Attr.canEncrypt = canEncrypt;
    }
    public static synchronized void setMessage(String message) {
        Attr.message = message;
    }

    // 重启记录ID
    public static int RESTART_RECORD_ID = -1;

    // app名称（用于邮件提醒）
    public static String APP_NAME = "";

    // 发送邮件相关数据
    public static String MAIL_HOST = "smtp.163.com";
    public static int MAIL_PORT = 25;
    public static String MAIL_FROM = "joauth2@163.com";
    public static String MAIL_PASS = "1q2w3e";
    public static String MAIL_USER = "joauth2";
    public static String MAIL_TO = "36677336@qq.com";

    // 需要重启的文件类型
    public static String[] FILE_RESTART_TYPE = {"class", "java", "xml", "properties", "jar"};

}
