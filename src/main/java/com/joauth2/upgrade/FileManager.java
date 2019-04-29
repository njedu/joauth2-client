package com.joauth2.upgrade;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.joauth2.AbstractRequestor;
import com.joauth2.Client;
import com.joauth2.JOAuthListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;


/**
 * 文件管理器
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/29
 */
public class FileManager extends AbstractRequestor{

    /**
     * 拷贝文件
     * @param fromUrl
     * @param toUrl
     * @return
     * @throws FileNotFoundException
     */
    public static boolean copy(String fromUrl, String toUrl) throws FileNotFoundException {
        String fileName = StrUtil.subAfter(fromUrl, "/", true);
        BufferedInputStream in = FileUtil.getInputStream(fromUrl);
        BufferedOutputStream out = FileUtil.getOutputStream(toUrl + File.separator + fileName);
        long copySize = IoUtil.copy(in, out, IoUtil.DEFAULT_BUFFER_SIZE);
        if (copySize > 0) {
            return true;
        }
        return false;
    }

    /**
     * 下载网络文件
     * @param fromUrl
     * @param toUrl
     * @return
     */
    public static boolean download(String fromUrl, String toUrl) {
        long size = HttpUtil.downloadFile(fromUrl, FileUtil.mkdir(toUrl + File.separator));
        if (size > 0) {
            return true;
        }
        return false;
    }

    private void getUpgrade(){
        String url = Client.props.getStr("auth.url") + "/upgrade";
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("access_token", Client.TOKEN);

        JSONObject resultJson = doPost(url, params);
        if (resultJson.getInt("code") == 10000) {

        }
    }

    /**
     * 每天检查更新情况
     */
    public static void autoUpgrade(){
        // 随机分钟，避免所有的应用都在同一时间发起请求
        int minute = RandomUtil.randomInt(1, 59),
                hour = RandomUtil.randomInt(1,3);
        String cron = minute + " "+ hour +" * * *";
        CronUtil.schedule(cron, new Task() {
            @Override
            public void execute() {
                synchronized (this) {

                }
            }
        });
        CronUtil.start();
    }

}
