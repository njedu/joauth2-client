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
public class Client extends AbstractRequestor{

	private static Log log = LogFactory.get(Client.class);
	
	private static Props props = Attr.props;


	/**
	 * 获取授权码
	 * @return
	 */
    public static synchronized String getCode() {

    	String clientId = props.getStr("auth.app_key");
        String redirectUri = StrUtil.isEmpty(props.getStr("auth.redirect_uri")) ? "/" : props.getStr("auth.redirect_uri");
        String requestUrl = props.getStr("auth.url") + "/authorize";
        String appEncrypt = props.getStr("auth.app_encrypt");
        
        // 优先处理离线模式
        if (isOffline(appEncrypt)) {
        	return Attr.getMessage();
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
				Attr.setMessage(getToken(code));
            	return Attr.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        JSONObject resultJson = JSONUtil.parseObj(result);
        if (resultJson.containsKey("code") ) {
        	if (resultJson.getInt("code") != 10000) {
        		return errorCommonHandle(resultJson);
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
        if (resultJson.getInt("code") != 10000) {
        	return errorCommonHandle(resultJson);
		}
        
        resultJson = resultJson.getJSONObject("object");
		Attr.TOKEN = resultJson.getStr("access_token");
        Attr.MAX_USER = resultJson.getInt("max_user");
		Attr.INTERVALS = resultJson.getInt("expires_in") == null ? 180 : resultJson.getInt("expires_in");
		Attr.RESTART_RECORD_ID = resultJson.getInt(OAuth2Constants.SESSION_RESTART_RECORD_ID);
		Attr.APP_NAME = resultJson.getStr("app_name");
		setEndTime(Attr.INTERVALS);
		Attr.canEncrypt = true;
        return "JOAuth2授权服务装载成功！";
    }

	/**
	 * 根据expiresIn计算自动任务下一次的执行时间
	 * @param expiresIn
	 */
	private static void setEndTime(int expiresIn){
		Attr.END_TIME = DateUtil.offset(new Date(), DateField.SECOND, expiresIn);
	}

	/**
	 * 获取code与token时的通用错误处理
	 * @param resultJson
	 * @return
	 */
	public static String errorCommonHandle(JSONObject resultJson){
		String msg = resultJson.getStr("msg");
		Attr.setMessage(msg);
		Attr.canEncrypt = false;
		Attr.MAX_USER = 0;

		// 授权间隔异常 -> 移除定时任务 -> 终止授权
		if (resultJson.getInt("code") == 403) {
			//setEndTime(2592000);
			try {
				CronUtil.remove(Attr.CRON_APPDATA_ID);
			} catch (Exception e) {}
		}
		else {
			ClientLogin.initApp();
			int expireIn = 60;
			if (resultJson.containsKey("object") && resultJson.getJSONObject("object").containsKey("expire_in")) {
				expireIn = resultJson.getJSONObject("object").getInt("expires_in");
			}
			setEndTime(expireIn);
		}

		return msg;
	}

	/**
	 * 	检查离线模式并进行加密
	 * @param encrypt
	 * @return
	 */
	public static boolean isOffline(String encrypt) {
		Map<String, String> appMap = AuthSecureUtils.decrypToApp(encrypt);
		if (appMap == null) {
			Attr.setMessage(OAuth2Constants.INVALID_PROPERTIES);
			Attr.canEncrypt = false;
			return true;
		}

		// 检查appKey
		if (!StrUtil.equals(MapUtil.getStr(appMap, "appKey"), props.getStr("auth.app_key"))) {
			Attr.setMessage(OAuth2Constants.INVALID_PROPERTIES + "[appKey错误]");
			Attr.canEncrypt = false;
			return true;
		}

		if (MapUtil.getInt(appMap, "offline") == 0) {
			return false;
		}

		Attr.OFFLINE = true;

		// 离线模式使用加密狗
		boolean initSuccess = ClientDog.init();
		if (!initSuccess) {
            Attr.canEncrypt = false;
        }
		return true;
	}

	/**
	 * 更新客户端数据
	 */
	public static void updateAppData(){
		if (Attr.OFFLINE) {
			return;
		}

		String requestUrl = Attr.props.getStr("auth.url") + "/data";
		Map<String, Object> params = MapUtil.newHashMap();
		params.put("access_token", Attr.TOKEN);

		JSONObject resultJson = doPost(requestUrl, params);
		if (resultJson.getInt("code") == 10000) {
			JSONObject json = resultJson.getJSONObject("object");
			Attr.MAX_USER = json.getInt("maxUser");
			Attr.INTERVALS = json.getInt("intervals");
			setEndTime(Attr.INTERVALS);
			Attr.canEncrypt = true;
		} else {
			String message = resultJson.getStr("msg");
			Attr.setMessage(message);
			Attr.canEncrypt = false;
			Attr.MAX_USER = 0;
			ClientLogin.initApp();
		}
	}

	/**
	 * 下线App
	 * @return true/false
	 */
	public static boolean offline(){
		if (Attr.OFFLINE || StrUtil.isEmpty(Attr.TOKEN)) {
			return false;
		}

		String requestUrl = Attr.props.getStr("auth.url") + "/offline";
		Map<String, Object> params = MapUtil.newHashMap();
		params.put("access_token", Attr.TOKEN);
		params.put(OAuth2Constants.SESSION_RESTART_RECORD_ID, Attr.RESTART_RECORD_ID);

		JSONObject resultJson = doPost(requestUrl, params);
		if (resultJson.getInt("code") == 10000) {
			log.info("App下机成功");
			return true;
		} else {
			String message = resultJson.getStr("msg");
			log.error(message);
			return false;
		}
	}



}
