package com.joauth2;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.softkey.DESUtils;
import com.softkey.jsyunew3;

/**
 * 加密狗登录器
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/3/25
 */
public class ClientDog {

    private static Log log = LogFactory.get(ClientDog.class);
    
    private static final String pk = "sd3e2wyhwztrt9fiu5tgbsx8u7uopoik";

    private static String lockid;
    public static String beginpwd = "";
    public static String endpwd = "";


    /**
     * 读取加密狗配置信息
     * @return
     */
    private static boolean readVariable(){
        Props properties = Client.props;
        try {
            beginpwd = DESUtils.desDecode(properties.getStr("dog.beginpwd"), pk);
            endpwd = DESUtils.desDecode(properties.getStr("dog.endpwd"), pk);
            lockid = properties.getStr("dog.lockid");
        } catch (Exception e) {
            e.printStackTrace();
            JOAuthListener.setMESSAGE("加密狗配置文件读取失败");
            return false;
        }
        return true;
    }

    /**
     * 初始化加密狗
     * @return
     */
    public static boolean init() {
        boolean variableFlag = readVariable();
        if (!variableFlag) {
            return false;
        }
        
        // 实例化加密狗
        jsyunew3 j9 = new jsyunew3();
        String DevicePath;

        // 判断系统中是否存在着加密锁。不需要是指定的加密锁,
        DevicePath = j9.FindPort(0);
        if (j9.get_LastError() != 0) {
            JOAuthListener.setMESSAGE("未找到加密锁,请插入加密锁后，再进行操作");
            return false;
        }

		// 校验锁ID
        if (!checkLockId(j9, DevicePath)) {
		    return false;
        }

		// 从加密锁的指定的地址中读取一批数据,使用默认的读密码
		short address = 0;// 要读取的数据在加密锁中储存的起始地址
        int length = j9.YRead(address, beginpwd, endpwd, DevicePath);
		if (j9.get_LastError() != 0) {
		    log.info(j9.get_LastError() + "");
            JOAuthListener.setMESSAGE("读取储存器失败");
			return false;
		}

        // 读取数据
		short[] buf = new short[length];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			try {
				buf[i] = j9.GetBuf(i);
				sb.append((char) buf[i]);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
        JOAuthListener.setMESSAGE("加密狗加载成功！");
		int maxUserCnt = Integer.valueOf(sb.toString());
		log.info("加密狗数据解析：" + maxUserCnt);
		if (maxUserCnt == 0) {
            JOAuthListener.setMESSAGE(OAuth2Constants.INVALID_MAX_USER);
            return false;
        }
		Client.MAX_USER = maxUserCnt;
		return true;
    }

    /**
     * 校验加密狗锁ID
     * @param j9
     * @param DevicePath
     * @return
     */
    private static boolean checkLockId(jsyunew3 j9, String DevicePath){
        // 获取加密狗ID，加密匹配
        String dogId = null;
        long dogId1 = j9.GetID_1(DevicePath); // 前半部分
        if (j9.get_LastError() != 0) {
            JOAuthListener.setMESSAGE("加密狗ID读取失败，错误码：" + j9.get_LastError());
            return false;
        }
        long dogId2 = j9.GetID_2(DevicePath); // 后半部分
        if (j9.get_LastError() != 0) {
            JOAuthListener.setMESSAGE("加密狗ID读取失败，错误码：" + j9.get_LastError());
            return false;
        }
        String prefix = "Z2R";
        String suffix = "JW";
        dogId = prefix + (Long.toHexString(dogId1) + Long.toHexString(dogId2)).toUpperCase() + suffix;
        String encodeDogId = SecureUtil.md5(dogId).toUpperCase();
        if (StrUtil.equals(encodeDogId, lockid)) {
            return true;
        }
        JOAuthListener.setMESSAGE("加密狗ID校验失败");
        return false;
    }
}
