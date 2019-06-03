package com.joauth2.upgrade;


import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;


/**
 * <p>
 * 应用升级
 * </p>
 *
 * @author wujiawei0926@yeah.net
 * @since 2019-04-26
 */
public class AppUpgrade implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer appid;

    /**
     * 版本号
     */
    private String version;


    /**
     * SQL
     */
    private String sqls;


    /**
     * 升级标题
     */
    private String title;

    /**
     * 程序根路径
     */
    private String rootPath;

    private List<AppUpgradeFile> files;

    /**
     * 是否压缩包
     */
    private Boolean zip;

    public Boolean getZip() {
        return zip == Boolean.TRUE;
    }

    public void setZip(Boolean zip) {
        this.zip = zip;
    }

    public List<AppUpgradeFile> getFiles() {
        return files;
    }

    public void setFiles(List<AppUpgradeFile> files) {
        this.files = files;
    }

    public Integer getAppid() {
        return appid;
    }

    public void setAppid(Integer appid) {
        this.appid = appid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getSqls() {
        return sqls;
    }

    public void setSqls(String sqls) {
        this.sqls = sqls;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String toString() {
        return "AppUpgrade{" +
        "id=" + id +
        ", version=" + version +
        ", sql=" + sqls +
        ", title=" + title +
        "}";
    }
}
