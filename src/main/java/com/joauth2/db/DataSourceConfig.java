package com.joauth2.db;

import cn.hutool.db.Db;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.db.transaction.TxFunc;
import cn.hutool.setting.dialect.Props;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据源配置类
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/12
 */
public class DataSourceConfig {

    private static com.joauth2.db.DataSource dbSource;

    /**
     * 初始化数据源配置
     * @param ds
     * @return
     */
    public static void init(com.joauth2.db.DataSource ds){
        if (ds == null) {
            return;
        }
        dbSource = ds;
        /*Props props = new Props("application.properties");
        props.setProperty("auth.datasource.url", ds.getUrl());
        props.setProperty("auth.datasource.driver", ds.getDriver());
        props.setProperty("auth.datasource.user", ds.getUser());
        props.setProperty("auth.datasource.password", ds.getPassword());*/
    }

    /**
     * 执行SQL语句
     * @param sql
     * @param param
     * @return
     * @throws SQLException
     */
    public void execute(final String sql, final Object...param) throws SQLException{
        com.joauth2.db.DataSource ds = dbSource;
        DataSource dataSource = new SimpleDataSource(ds.getUrl(), ds.getUser(), ds.getPassword());
        Db.use(dataSource, dbSource.getDriver()).tx(new TxFunc() {
            @Override
            public void call(Db db) throws SQLException {
                db.execute(sql, param);
            }
        });
    }



}
