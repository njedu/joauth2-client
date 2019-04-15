package com.joauth2.db;

/**
 * [Write Something]
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/12
 */
public class DataSource {
    
    public DataSource(){}

    private String driver;
    private String url;
    private String user;
    private String password;
    private String DbType;

    public String getDriver() {
        return driver;
    }

    public DataSource setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DataSource setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DataSource setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DataSource setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDbType() {
        return DbType;
    }

    public DataSource setDbType(String dbType) {
        DbType = dbType;
        return this;
    }
}
