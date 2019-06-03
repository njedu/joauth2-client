package com.joauth2.upgrade;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
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

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


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
        try {
            long size = HttpUtil.downloadFile(fromUrl, FileUtil.mkdir(toUrl + File.separator));
            if (size > 0) {
                log.info("[upgrade]: " + toUrl);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载zip文件
     * @param fromUrl
     * @param toUrl
     * @return
     */
    public static String downloadZip(String fromUrl, String fromFileName, String toUrl) {
        try {
            long size = HttpUtil.downloadFile(fromUrl, FileUtil.mkdir(toUrl + File.separator));
            if (size > 0) {
                log.info("[upgrade]: " + toUrl);
                // 解压缩
                String path = toUrl + File.separator + fromFileName;
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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
                    boolean restart = false;
                    // 检查是否需要重启
                    for (AppUpgrade upgrade : upgradeList) {
                        restart = checkRestart(upgrade);
                        if (!restart) {
                            break;
                        }
                    }

                    // 下线App(开发应用不予上下线操作)
                    if (restart && Client.offline()) {
                        Attr.setMessage("应用正在更新");
                        Attr.canEncrypt = false;
                    }

                    // 发送重启提示
                    sendTipMail();

                    // 更新文件
                    for (AppUpgrade upgrade : upgradeList) {
                        // 更新SQL
                        String sql = upgrade.getSqls();
                        if (StrUtil.isNotBlank(sql)) {
                            SqlRunner.execute(sql);
                        }

                        downloadFile(upgrade);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void downloadFile(AppUpgrade upgrade){
        String rootPath = upgrade.getRootPath();
        if (upgrade.getZip()) {
            AppUpgradeFile upgradeFile = upgrade.getFiles().get(0);
            if (upgradeFile != null) {
                String downloadPath = rootPath + File.separator + upgradeFile.getFileName();//downloadZip(filePath, upgradeFile.getFileName(), rootPath);
                File file = FileUtil.newFile(downloadPath);
                if (file.exists()) {
                    ZipUtil.unzip(downloadPath, rootPath, CharsetUtil.CHARSET_GBK);
                    // 删除压缩文件
                    FileUtil.del(file);
                }
            }
        } else {
            for (AppUpgradeFile upgradeFile : upgrade.getFiles()) {
                String filePath = upgradeFile.getFilePath(),
                        writePath = rootPath + File.separator + upgradeFile.getWritePath();
                download(filePath, writePath);
            }
        }
    }

    /**
     * 检查是否需要重启
     * @param upgrade
     * @return
     */
    private static boolean checkRestart(AppUpgrade upgrade){
        boolean restart = false;
        String rootPath = upgrade.getRootPath();
        if (upgrade.getZip()) {
            AppUpgradeFile upgradeFile = upgrade.getFiles().get(0);
            if (upgradeFile != null) {
                String filePath = upgradeFile.getFilePath();
                String downloadPath = downloadZip(filePath, upgradeFile.getFileName(), rootPath);
                File file = FileUtil.newFile(downloadPath);
                if (file.exists()) {
                    restart = validateZipFile(downloadPath);
                }
            }
        } else {
            for (AppUpgradeFile upgradeFile : upgrade.getFiles()) {
                restart = validateRestartFile(upgradeFile.getFileName());
            }
        }
        return restart;
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
     * 校验文件是否是需要重启的类型
     * @param fileName
     * @return
     */
    private static boolean validateRestartFile(String fileName){
        String type = StrUtil.subAfter(fileName, ".", true);
        if (ArrayUtil.contains(Attr.FILE_RESTART_TYPE, type)) {
            return true;
        }
        return false;
    }

    /**
     * 校验压缩包内的文件是否是需要重启的类型
     * @param zipName
     * @return
     */
    private static boolean validateZipFile(String zipName){
        List<String> list = new ArrayList<>();
        try {
            list = readZipFileName(zipName);
            for (String s : list) {
                if (validateRestartFile(s)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.info("不支持的中文文件");
        }
        return false;
    }

    /**
     * 不解压，直接读取压缩包内的文件列表
     * @param file
     * @throws Exception
     */
    public static List<String> readZipFileName(String file) throws Exception {
        List<String> fileNameList = new ArrayList<>();
        ZipFile zf = new ZipFile(file);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (!ze.isDirectory()) {
                System.err.println("file - " + ze.getName() + " : "
                        + ze.getSize() + " bytes");
                fileNameList.add(ze.getName());
            }
        }
        zin.closeEntry();
        zin.close();
        in.close();
        return fileNameList;
    }

    /**
     * 每天检查更新情况
     */
    public static void autoUpgrade(){
        // 随机分钟，避免所有的应用都在同一时间发起请求
        int minute = RandomUtil.randomInt(1, 59),
                hour = RandomUtil.randomInt(1,3);
        String cron = minute + " "+ hour +" * * *";
        //cron = "*/2 * * * *";

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
