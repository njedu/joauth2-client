package com.joauth2;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端登录方法处理
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/3/23
 */
public class ClientLogin extends AbstractRequestor{

    private static Log log = LogFactory.get(ClientLogin.class);

    private static ConcurrentMap<String, String> userSessionMap = new ConcurrentHashMap<String, String>();

    /**
     * 登录
     * @param user
     * @param request
     * @param <T>
     * @return
     */
    public static <T> R login(ClientUser<T> user, HttpServletRequest request){
        HttpSession session = request.getSession();

        // 检查是否已登录
        if (ClientLogin.existLoginedUser(user.getId(), session)) {
            return new R(500, "当前账户已登录，无法再次登录");
        }

        if (Attr.MAX_USER == 0) {
            return new R(500, Attr.getMessage() + "，无法登录");
        }

        // 检查授权许可
        if (Attr.TOTAL_USER + 1 > Attr.MAX_USER) {
            return new R(500, "超过最大用户限制，无法登录");
        }

        // 保存内部登录数据
        ClientLogin.saveLoginInfo(user, request);

        // 增加登录人数
        plusTotalUser(user.getId(), session);
        session.setAttribute(OAuth2Constants.SESSION_CLIENT_ATTR, user);

        return new R(200, "success");
    }

    /**
     * 	保存内部用户登录数据
     * @param user
     * @param request
     */
    public static void saveLoginInfo(ClientUser user, HttpServletRequest request) {
        if (Attr.OFFLINE) {
            return;
        }

        String requestUrl = Attr.props.getStr("auth.url") + "/login_record";

        Map<String, Object> params = MapUtil.newHashMap();
        params.put("access_token", Attr.TOKEN);
        params.put("userid", user.getId());
        params.put("username", user.getUsername());
        params.put("nickname", user.getNickname());
        params.put("ext", user.getExtMsg());
        UserAgentGetter userAgentGetter = new UserAgentGetter(request);
        params.put("ip", userAgentGetter.getIpAddr());
        params.put("osName", userAgentGetter.getOS());
        params.put("device", userAgentGetter.getDevice());
        params.put("browserType", userAgentGetter.getBrowser());

        JSONObject resultJson = doPost(requestUrl, params);
        if (resultJson.getInt("code") == 10000) {
            // 获取记录id
            int id = resultJson.getJSONObject("object").getInt("id");
            request.getSession().setAttribute(OAuth2Constants.SESSION_LOGIN_RECORD_ID, id);
            log.info("登录数据保存成功");
        } else {
            log.info(resultJson.getStr("msg"));
        }
    }

    /**
     * 	设置登录记录-下线
     * @param recordId
     */
    public static void saveLogoutInfo(int recordId) {
        if (Attr.OFFLINE) {
            return;
        }

        String requestUrl = Attr.props.getStr("auth.url") + "/login_record/offline";
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("access_token", Attr.TOKEN);
        params.put("id", recordId);

        JSONObject resultJson = doPost(requestUrl, params);
        if (resultJson.getInt("code") == 10000) {
            log.info("下线成功");
        } else {
            log.info(resultJson.getStr("msg"));
        }
    }

    /**
     * 初始化应用
     * 程序重启时强制下线所有的登录记录
     */
    public static void initApp() {
        if (!Attr.OFFLINE && StrUtil.isNotEmpty(Attr.TOKEN)) {
            String requestUrl = Attr.props.getStr("auth.url") + "/login_record/init";
            Map<String, Object> params = MapUtil.newHashMap();
            params.put("app_key", Attr.props.getStr("auth.app_key"));
            JSONObject resultJson = doPost(requestUrl, params);
            if (resultJson.getInt("code") != 10000) {
                log.info(resultJson.getStr("msg"));
            }
        }

        Attr.TOTAL_USER = 0;
        userSessionMap = new ConcurrentHashMap<String, String>();
    }

    /**
     * 	是否存在已登录用户
     * 	@param userId
     *  @param session
     *  @return
     */
    public static boolean existLoginedUser(int userId, HttpSession session) {
        String currentKey = OAuth2Constants.SESSION_EXCLUDE_LOGIN + ":" + userId;
        for (String key : userSessionMap.keySet()) {
            // 存在相同的key，说明该用户已登录
            if (StrUtil.equals(key, currentKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 增加已登录用户数量
     */
    private static void plusTotalUser(int userId, HttpSession session){
        ++Attr.TOTAL_USER;
        String currentKey = OAuth2Constants.SESSION_EXCLUDE_LOGIN + ":" + userId;
        userSessionMap.put(currentKey, session.getId());
        log.info("目前的总用户数量:" + Attr.TOTAL_USER);
    }

    /**
     * 	注销
     * @param userAttrKey Session Attribute Name
     * @param userId 用户id
     * @param session
     */
    public static void logout(int userId, HttpSession session, String...userAttrKey) {
        logout(userId, session, true, userAttrKey);
    }

    /**
     * 注销
     * @param userAttrKey Session Attribute Name
     * @param userId
     * @param session
     * @param removeMap 是否删除userSessionMap中键值对
     */
    public static void logout(int userId, HttpSession session, boolean removeMap, String...userAttrKey) {
        if (removeMap) {
            String currentKey = OAuth2Constants.SESSION_EXCLUDE_LOGIN + ":" + userId;
            userSessionMap.remove(currentKey);
        }

        // 关闭会话
        if (ArrayUtil.isNotEmpty(userAttrKey)) {
            for (String attr : userAttrKey) {
                session.removeAttribute(attr);
            }
        }
        session.invalidate();
    }

    /**
     * 获取 [用户-SESSION] 关联的键值对
     * @return
     */
    public static ConcurrentMap<String, String> getUserSessionMap() {
        return userSessionMap;
    }

    /**
     * 根据sessionId获取userId
     * @param sessionId
     * @return
     */
    public static String getUserIdFromSession(String sessionId){
        for (String key : userSessionMap.keySet()) {
            String value = userSessionMap.get(key);
            if (StrUtil.equals(value, sessionId)) {
                return StrUtil.subAfter(value, ":", true);
            }
        }
        return "";
    }

}
