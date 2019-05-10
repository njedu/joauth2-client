package com.joauth2.upgrade;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.*;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.joauth2.Attr;

import java.io.*;
import java.net.URL;

/**
 * 服务器嗅探器
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/30
 */
public class ServerSniffer {

    private static Log log = LogFactory.get();

    private static final String netStart = "net start ";
    private static final String netStop = "net stop ";
    private static final String netRestart = "net stop # && ping -n 5 127.0.0.1 && net start #";

    public ServerSniffer(){}

    /**
     * 执行一个cmd命令
     * @param cmdCommand cmd命令
     * @return 命令执行结果字符串，如出现异常返回null
     */
    public static String excuteCMDCommand(String cmdCommand) {
        StringBuilder stringBuilder = new StringBuilder();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmdCommand);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"));
            String line = null;
            while((line=bufferedReader.readLine()) != null) {
                stringBuilder.append(line+"\n");
            }
            String result = stringBuilder.toString();
            log.info(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 开启服务
     * @param serviceName
     * @return
     * @throws IOException
     */
    public static String startService(String serviceName) throws IOException {
        String cmd = netStart + serviceName;
        return excuteCMDCommand(cmd);
    }

    /**
     * 关闭服务
     * @param serviceName
     * @return
     * @throws IOException
     */
    public static String stopService(String serviceName) throws IOException {
        String cmd = netStop + serviceName;
        return excuteCMDCommand(cmd);
    }

    /**
     * 重启服务
     * @param serviceName
     * @return
     */
    public static String restartService(String serviceName) {
        String cmd = StrUtil.replace(netRestart, "#", serviceName);
        return excuteCMDCommand(cmd);
    }

    /**
     * 通过服务重启Tomcat
     * @return
     */
    public String restartTomcatByService(String rootPath){
        String finalPath = "";
        try {
            File file = FileUtil.newFile(rootPath + "/restart-tomcat.bat");
            if (!file.exists()) {
                InputStream is = this.getClass().getResourceAsStream("/restart-tomcat.bat");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder content = new StringBuilder();
                String line = "";
                while((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }

                String serviceName = Attr.props.getStr("auth.upgrade.service");
                line = StrUtil.replace(content.toString(), "#", serviceName);
                file = FileUtil.writeUtf8String(line, file);
            }

            finalPath = file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*File file = FileUtil.file("restart-tomcat.bat");
        if (FileUtil.exist(file)) {
            String content = FileUtil.readString(file, CharsetUtil.UTF_8);
            String serviceName = Attr.props.getStr("auth.upgrade.service");
            content = StrUtil.replace(content, "#", serviceName);
            file = FileUtil.writeString(content, file, CharsetUtil.UTF_8);
        }
        String finalPath = file.getAbsolutePath();*/
        return excuteCMDCommand(finalPath);
    }


    public static void main(String[] args) {
        Attr.props = new Props("application.properties");
        ServerSniffer serverSniffer = new ServerSniffer();
        String root = "C:/";
        String path = serverSniffer.restartTomcatByService(root);
        cn.hutool.core.lang.Console.log(path);
    }


}