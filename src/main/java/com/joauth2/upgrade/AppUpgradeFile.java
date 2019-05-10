package com.joauth2.upgrade;

import java.io.Serializable;


/**
 * <p>
 * 
 * </p>
 *
 * @author wujiawei0926@yeah.net
 * @since 2019-04-26
 */
public class AppUpgradeFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;


    /**
     * 升级记录id
     */
    private Integer upgradeId;


    /**
     * 文件保存路径
     */
    private String filePath;


    /**
     * 升级时写入路径
     */
    private String writePath;


    /**
     * 大小(KB)
     */
    private Double size;


    /**
     * 文件名称
     */
    private String fileName;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUpgradeId() {
        return upgradeId;
    }

    public void setUpgradeId(Integer upgradeId) {
        this.upgradeId = upgradeId;
    }
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getWritePath() {
        return writePath;
    }

    public void setWritePath(String writePath) {
        this.writePath = writePath;
    }
    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "AppUpgradeFile{" +
        "id=" + id +
        ", upgradeId=" + upgradeId +
        ", filePath=" + filePath +
        ", writePath=" + writePath +
        ", size=" + size +
        ", fileName=" + fileName +
        "}";
    }
}
