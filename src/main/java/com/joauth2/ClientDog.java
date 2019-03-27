package com.joauth2;

import cn.hutool.core.util.HexUtil;
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
    
    private static final String pk = "1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik";

    private static String lockid;
    public static int codetype = 0;
    public static long decode = 0l;
    public static long encode = 0l;
    public static String beginpwd = "";
    public static String endpwd = "";
    public static boolean nodog;


    private static void initVariable(){
        Props properties = Client.props;
        
        try {
            codetype = Integer.valueOf(DESUtils.desDecode(properties.getProperty("dog.codetype"), pk));
            decode = Long.valueOf(DESUtils.desDecode(properties.getProperty("dog.decode"), pk));
            encode = Long.valueOf(DESUtils.desDecode(properties.getProperty("dog.encode"), pk));
            beginpwd = DESUtils.desDecode(properties.getProperty("dog.beginpwd"), pk);
            endpwd = DESUtils.desDecode(properties.getProperty("dog.endpwd"), pk);
            lockid = properties.getProperty("dog.lockid");
        } catch (Exception e) {
            log.error("加密狗配置文件读取失败！");
        }
    }

    public static void init() {
        initVariable();
        
        // 实例化加密狗
        jsyunew3 j9 = new jsyunew3();
        String DevicePath = "";
        String outstring;
        int ver;
        int version;

        // 判断系统中是否存在着加密锁。不需要是指定的加密锁,
        DevicePath = j9.FindPort(0);
        if (j9.get_LastError() != 0) {
            log.error("未找到加密锁,请插入加密锁后，再进行操作。");
            return;
        }

		DevicePath = j9.FindPort_2(codetype, decode, encode);
		if (j9.get_LastError() != 0) {
			nodog = true;
			log.info("未找到指定的加密锁");
		} else {
			nodog = false;
			log.info("成功找到指定的加密锁");
		}

		// 校验锁ID
        if (!checkLockId(j9, DevicePath)) {
		    return;
        }

		// 从加密锁的指定的地址中读取一批数据,使用默认的读密码
		short address = 0;// 要读取的数据在加密锁中储存的起始地址
		short len = 3;// 要读取20个字节的数据
		if (j9.YReadEx(address, len, beginpwd, endpwd, DevicePath) != 0) {
			log.info("读取储存器失败");
			return;
		}
		short[] buf = new short[3];
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			try {
				buf[i] = j9.GetBuf(i);
				sb.append((char) buf[i]);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
		log.info("加密狗加载成功！");
		Client.MAX_USER = Integer.valueOf(sb.toString());
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
            log.info("加密狗ID读取失败，错误码：{}", j9.get_LastError());
            return false;
        }
        long dogId2 = j9.GetID_2(DevicePath); // 后半部分
        if (j9.get_LastError() != 0) {
            log.info("加密狗ID读取失败，错误码：{}", j9.get_LastError());
            return false;
        }
        dogId = (Long.toHexString(dogId1) + Long.toHexString(dogId2)).toUpperCase();
        log.info(dogId);
        String encodeDogId = SecureUtil.md5(dogId).toUpperCase();
        log.info("加密后锁ID：{}", encodeDogId);
        if (StrUtil.equals(encodeDogId, lockid)) {
            log.info("加密狗ID读取成功");
            return true;
        }
        log.info("加密狗ID校验失败！");
        return false;
    }
}
