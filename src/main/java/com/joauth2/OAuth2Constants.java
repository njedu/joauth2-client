package com.joauth2;

import cn.hutool.setting.dialect.Props;

import java.util.Date;

/**
 * OAuth2Constants
 *
 * @name: OAuth2Constants
 * @author: wujiawei
 * @date: 2019/1/6
 * @time: 22:22
 */
public class OAuth2Constants {

    /**************加解密****************/
    public static final String AES_KEY = "d2060d591c0d4c50b393415420b460da";
    public static final String AES_PARAM = "s";

    public static String OAUTH_AUTHORIZE_FAILED_KEY = "OAUTH_AUTHORIZE_FAILED_KEY";


    /***************提示信息********************/
    public static final String INVALID_CLIENT_ID = "无效的APP KEY";
    public static final String INVALID_CLIENT_SECRET = "无效的APP SECRET";
    public static final String INVALID_AUTH_CODE = "无效的授权码";
    public static final String INVALID_IP = "不匹配的IP";
    public static final String EXPIRED_TIME = "不在允许的授权时间内";
    public static final String INVALID_INTERVAL = "无效的授权间隔";
    public static final String INVALID_ACCESS_TOKEN = "accessToken无效或已过期。";
    public static final String INVALID_REDIRECT_URI = "缺少授权成功后的回调地址。";
    public static final String INVALID_MAX_USER = "超过最大用户限制";
    public static final String INVALID_PROPERTIES = "授权配置错误，请添加配置信息后，再进行操作";

    /****************** 会话相关 *****************/
    public static final String SESSION_LOGIN_RECORD_ID = "joauth2_login_record_id";
    public static final String SESSION_EXCLUDE_LOGIN = "exclude_login_user";
    public static final String SESSION_CLIENT_ATTR = "session_user_attr";
    public static final String SESSION_RESTART_RECORD_ID = "restart_record_id";


}