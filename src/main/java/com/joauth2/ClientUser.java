package com.joauth2;


/**
 * 内部用户对象
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/3/23
 */
public class ClientUser<T>{

    private int id;
    private String username;
    private String nickname;
    private String avatar;
    private String sessionAttrName;
    private T user;
    private String extMsg;

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    public String getSessionAttrName() {
        return sessionAttrName;
    }

    public void setSessionAttrName(String sessionAttrName) {
        this.sessionAttrName = sessionAttrName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getExtMsg() {
        return extMsg;
    }

    public void setExtMsg(String extMsg) {
        this.extMsg = extMsg;
    }
}
