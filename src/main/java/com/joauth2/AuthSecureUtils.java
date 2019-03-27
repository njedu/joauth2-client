package com.joauth2;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/3.
 */
public class AuthSecureUtils {

    /**
     * AES解密请求参数
     * @param encryptHex
     * @return
     */
    public static Map<String, String> decodeKeys(String encryptHex){
        byte[] aesKey = HexUtil.decodeHex(OAuth2Constants.AES_KEY);
        AES aes = new AES(Mode.CTS, Padding.PKCS5Padding, aesKey, aesKey);
        String decryptStr = aes.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        cn.hutool.core.lang.Console.log(decryptStr);
        String[] paramArr = StrUtil.split(decryptStr, "&");
        Map<String, String> map = MapUtil.newHashMap();
        for (String s : paramArr) {
            String[] kv = StrUtil.split(s, "=");
            map.put(kv[0], HttpUtil.decode(kv[1], CharsetUtil.UTF_8));
        }
        return map;
    }

    /**
     * AES加密请求参数
     * @param params
     * @return String
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String encodeKeys(Map params){
        String paramsStr = HttpUtil.toParams(params);
        byte[] aesKey = HexUtil.decodeHex(OAuth2Constants.AES_KEY);
        AES aes = new AES(Mode.CTS, Padding.PKCS5Padding, aesKey, aesKey);
        if (paramsStr.length() < 8) {
            paramsStr += "&a=a&b=b&c=c&d=d&e=e";
        }
        String paramStrHex = aes.encryptHex(paramsStr);
        return paramStrHex;
    }
    
    /**
     * AES加密请求地址
     * @param requestUrl
     * @param params
     * @return
     */
    public static String encodeRequestUrl(String requestUrl, Map params) {
    	return requestUrl + "?"+ OAuth2Constants.AES_PARAM +"=" + encodeKeys(params);
	}
    
    /**
     * AES加密请求参数
     * @param params
     * @return Map
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map encodeKeysToMap(Map params) {
		Map map = MapUtil.newHashMap(1);
		map.put(OAuth2Constants.AES_PARAM, encodeKeys(params));
		return map;
	}
    
    /**
     * 	解密成APP数据
     * @param content
     * @return
     */
    public static Map<String, String> decrypToApp(String content) {
    	byte[] key = HexUtil.decodeHex(OAuth2Constants.AES_KEY);
    	AES aes = SecureUtil.aes(key);
    	String decryptStr = aes.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
    	Map<String, String> appMap = MapUtil.newHashMap();
    	String[] arr = StrUtil.split(decryptStr, "$");
    	if (arr.length != 6) {
			return null;
		}
    	appMap.put("appKey", arr[0]);
    	appMap.put("offline", arr[1]);
    	appMap.put("beginTime", arr[2]);
    	appMap.put("endTime", arr[3]);
    	appMap.put("maxUser", arr[4]);
    	appMap.put("innetIp", arr[5]);
    	return appMap;
	}
    
    /**
     * 	获取内网IP
     * @return
     */
    public static String getInnetIp() {
    	// 本机IP
    	String localIp = null; 
    	// 外网IP
    	String netIp = null;
    	Enumeration<NetworkInterface> netInterfaces = null;
    	
    	try {
    		netInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
    	
    	InetAddress ip = null;
    	// 是否找到外网IP
    	boolean finded = false;
		while (netInterfaces.hasMoreElements() && !finded) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
				// 获取外网IP
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
					netIp = ip.getHostAddress();
					finded = true;
					break;
				} 
				// 内网IP
				else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
					localIp = ip.getHostAddress();
				}
			}
		}
		
		if (StrUtil.isNotEmpty(netIp)) {
			return netIp;
		}
		return localIp;
	}
    

}
