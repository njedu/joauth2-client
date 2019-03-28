package com.joauth2;

import java.util.Date;
import java.util.Map;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;

/**
 * 授权登录器
 */
public class Client{

	private static Log log = LogFactory.get(Client.class);
	
	// 令牌
	public static String TOKEN = "";
	// 间隔结束时间
	public static Date END_TIME = null;
	// 当前用户数量
	public static int TOTAL_USER = 0;
	// 最大用户数量
	public static int MAX_USER = 0;
	// 离线模式
	public static boolean OFFLINE = false;
	
	public static String MESSAGE_TMP = "";

	public static Props props;

	static {
		try {
			props = new Props("application.properties");
		} catch (Exception e) {
			try {
				props = new Props("db.properties");
			} catch (Exception e2) {}
		}
		if (props == null) {
			log.error("缺少配置项，请检查配置文件application.properties和授权相关配置");
		}
	}

	/**
	 * 获取授权码
	 * @return
	 */
    public static synchronized String getCode() {

    	String clientId = props.getStr("auth.app_key");
        String redirectUri = props.getStr("auth.redirect_uri");
        String requestUrl = props.getStr("auth.url") + "/authorize";
        String appEncrypt = props.getStr("auth.app_encrypt");
        
        // 优先处理离线模式
        if (isOffline(appEncrypt)) {
        	return JOAuthListener.getMESSAGE();
		}

        // 请求参数
        Map<String, Object> params = MapUtil.newHashMap(3);
        params.put("app_key", clientId);
        params.put("response_type", "code");
        params.put("redirect_uri", redirectUri);

        HttpResponse httpResponse = doGet(requestUrl, params);
        String result = httpResponse.body();
        int statusCode = httpResponse.getStatus();
        if (statusCode == 302) {
            try {
            	String resultLocation = httpResponse.header("Location");
            	String code = resultLocation.substring(resultLocation.lastIndexOf("=") + 1);
            	JOAuthListener.setMESSAGE(getToken(code));
            	return JOAuthListener.getMESSAGE();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        JSONObject resultJson = JSONUtil.parseObj(result);
        if (resultJson.containsKey("code") ) {
        	if (resultJson.getInt("code") == 10021) {
        		return errorCommonHandle(resultJson);
			} else if (resultJson.getInt("code") == 926) {
				return result;
			}
		}
        
        return result;
    }

    /**
     * 获取Token
     * @param code 授权码
     * @return
     */
    public static synchronized String getToken(String code){
        String clientId = props.getStr("auth.app_key");
        String clientSecret = props.getStr("auth.app_secret");
        String requestUrl = props.getStr("auth.url") + "/access_token";

        // 请求参数
        Map<String, Object> params = MapUtil.newHashMap(3);
        params.put("app_key", clientId);
        params.put("app_secret", clientSecret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        // 发起post请求
        JSONObject resultJson = doPost(requestUrl, params);
        if (resultJson.containsKey("code") && resultJson.getInt("code") == 10021) {
        	return errorCommonHandle(resultJson);
		}
        
        resultJson = resultJson.getJSONObject("object");
        TOKEN = resultJson.getStr("access_token");
        MAX_USER = resultJson.getInt("max_user");
        // 计算授权结束时间
        setEndTime(resultJson.getInt("expires_in"));
        JOAuthListener.canEncrypt = true;
        return "JOAuth2授权服务装载成功！";
    }

	/**
	 * 设置过期时间
	 * @param expiresIn
	 */
	private static void setEndTime(int expiresIn){
		END_TIME = DateUtil.offset(new Date(), DateField.SECOND, expiresIn);
	}

	/**
	 * 获取code与token时的通用错误处理
	 * @param resultJson
	 * @return
	 */
	private static String errorCommonHandle(JSONObject resultJson){
		String msg = resultJson.getStr("message");
		JOAuthListener.setMESSAGE(msg);
		JOAuthListener.canEncrypt = false;
		MAX_USER = 0;
		ClientLogin.initApp();
		if (resultJson.containsKey("data")) {
			setEndTime(resultJson.getJSONObject("data").getInt("expires_in"));
		}
		return msg;
	}
    
    
	/**
	 * 刷新Token
	 */
	public static void refreshToken() {
		CronUtil.schedule("*/1 * * * * *", new Task() {
			@Override
			public void execute() {
			synchronized (this) {
				// 判断是否是间隔的结束时间
				Date now = new Date();
				if (END_TIME == null || END_TIME.getTime() < now.getTime()) {
					getCode();
					if (!StrUtil.equals(MESSAGE_TMP, JOAuthListener.getMESSAGE())) {
						MESSAGE_TMP = JOAuthListener.getMESSAGE();
						log.info(JOAuthListener.getMESSAGE());
					}
				}
			}
			}
		});
		CronUtil.setMatchSecond(true);
		CronUtil.start();
	}

	/**
	 * 	检查离线模式并进行加密
	 * @param encrypt
	 * @return
	 */
	public static boolean isOffline(String encrypt) {
		Map<String, String> appMap = AuthSecureUtils.decrypToApp(encrypt);
		if (appMap == null) {
			JOAuthListener.setMESSAGE(OAuth2Constants.INVALID_PROPERTIES);
			JOAuthListener.canEncrypt = false;
			return true;
		}

		// 检查appKey
		if (!StrUtil.equals(MapUtil.getStr(appMap, "appKey"), props.getStr("auth.app_key"))) {
			JOAuthListener.setMESSAGE(OAuth2Constants.INVALID_PROPERTIES);
			JOAuthListener.canEncrypt = false;
			return true;
		}

		if (MapUtil.getInt(appMap, "offline") == 0) {
			return false;
		}

		OFFLINE = true;

		// 内网IP检查
		String innerIp = MapUtil.getStr(appMap, "innetIp");
		String localIp = AuthSecureUtils.getInnetIp();
		if (!StrUtil.equals(innerIp, localIp)) {
			JOAuthListener.setMESSAGE(OAuth2Constants.INVALID_IP);
			JOAuthListener.canEncrypt = false;
			return true;
		}

		// 授权时间检查
		Date beginTime = MapUtil.getDate(appMap, "beginTime"),
				endTime = MapUtil.getDate(appMap, "endTime"),
				now = new Date();
		END_TIME = endTime;
		if (beginTime.getTime() > now.getTime() || endTime.getTime() < now.getTime()) {
			JOAuthListener.setMESSAGE(OAuth2Constants.EXPIRED_TIME);
			JOAuthListener.canEncrypt = false;
			return true;
		}

		// 最大登录人数检查
		int maxUser = MapUtil.getInt(appMap, "maxUser");
		MAX_USER = maxUser;

		// 加密狗
		//ClientDog.init();

		JOAuthListener.setMESSAGE("JOAuth2授权服务（离线模式）装载成功！");
		return true;
	}

    /**
     * POST请求
     * @param url
     * @param params
     * @return
     */
    public static JSONObject doPost(String url, Map params) {
    	String result = HttpUtil.createPost(url)
    			.header("Content-Type", "application/x-www-form-urlencoded")
    			.form(AuthSecureUtils.encodeKeysToMap(params))
    			.execute().body();
        JSONObject resultJson = JSONUtil.parseObj(result);
        
        // 解密
        if (resultJson.containsKey("object") && resultJson.getJSONObject("object").containsKey(OAuth2Constants.AES_PARAM)) {
			resultJson.getJSONObject("object")
				.putAll(
						AuthSecureUtils.decodeKeys(
								resultJson.getJSONObject("object").getStr(OAuth2Constants.AES_PARAM))
						);
		}
        
        return resultJson;
	}
    
    /**
     * GET请求
     * @param url
     * @param params
     * @return
     */
    public static HttpResponse doGet(String url, Map params) {
        HttpResponse httpResponse =  HttpUtil.createGet(AuthSecureUtils.encodeRequestUrl(url, params)).execute();
        return httpResponse;
	}


}
