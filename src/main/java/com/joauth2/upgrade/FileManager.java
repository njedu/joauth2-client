package com.joauth2.upgrade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.joauth2.AbstractRequestor;
import com.joauth2.Attr;
import com.joauth2.Client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 文件管理器
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/29
 */
public class FileManager extends AbstractRequestor{

    private static Log log = LogFactory.get();

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
            log.info("[upgrade]: " + toUrl);
            return true;
        }
        return false;
    }

    /**
     * 获取项目路径
     * @return
     */
    public static String getProjectPath(){
        //String path = Class.class.getClass().getResource("/").getPath();
        URL path = FileManager.class.getClassLoader().getResource("application.properties");
        return path.toString();
    }

    /**
     * 检查并获取更新
     */
    public static void getUpgrade(){
        String url = Attr.props.getStr("auth.url") + "/upgrade";
        Map<String, Object> params = MapUtil.newHashMap();
        params.put("access_token", Attr.TOKEN);

        JSONObject resultJson = doPost(url, params);
        if (resultJson.getInt("code") == 10000) {
            List<AppUpgrade> upgradeList = JSONUtil.toList(resultJson.getJSONArray("object"), AppUpgrade.class);
            if (CollectionUtil.isNotEmpty(upgradeList)) {
                try {
                    for (AppUpgrade upgrade : upgradeList) {
                        // 更新SQL
                        String sql = upgrade.getSqls();
                        if (StrUtil.isNotBlank(sql)) {
                            SqlRunner.execute(sql);
                        }

                        // 更新文件
                        String rootPath = upgrade.getRootPath();
                        for (AppUpgradeFile upgradeFile : upgrade.getFiles()) {
                            String filePath = upgradeFile.getFilePath(),
                                    writePath = rootPath + File.separator + upgradeFile.getWritePath();
                            download(filePath, writePath);
                        }

                        // 下线App
                        if (Client.offline()) {
                            Attr.setMessage("应用正在更新");
                            Attr.canEncrypt = false;
                        }

                        // 发送重启提示
                        sendTipMail();

                        // 重启服务器（仅支持Tomcat）
                        /*if (Attr.props.getBool("auth.upgrade.auto")) {
                            ServerSniffer sniffer = new ServerSniffer();
                            sniffer.restartTomcatByService(rootPath);
                        }*/

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 发送重启提示邮件
     */
    private static void sendTipMail(){
        MailAccount account = new MailAccount();
        account.setHost(Attr.MAIL_HOST);
        account.setPort(Attr.MAIL_PORT);
        account.setFrom(Attr.MAIL_FROM);
        account.setUser(Attr.MAIL_USER);
        account.setPass(Attr.MAIL_PASS);
        try {
            MailUtil.send(
                    account,
                    Attr.MAIL_TO,
                    "【JOAuth2授权平台】更新提示",
                    "<h1>应用名称："+ Attr.APP_NAME +"</h1>" +
                            "<p>客户端已完成自动更新，请前往重启Tomcat</p>" +
                            "<p>对于已启用热更新功能的客户端（修改Tomcat/conf/server.xml），仍应检查可否正常访问，以防热更新失败</p>",
                    true);
        } catch (Exception e) {
            log.error("邮件发送失败");
            e.printStackTrace();
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

        Attr.CRON_UPGRADE_ID = CronUtil.schedule(cron, new Task() {
            @Override
            public void execute() {
                synchronized (this) {
                    getUpgrade();
                }
            }
        });
    }

}
