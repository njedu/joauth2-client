package com.joauth2.upgrade;

import cn.hutool.db.Db;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.db.transaction.TxFunc;
import cn.hutool.setting.dialect.Props;

import java.sql.SQLException;

/**
 * 数据源配置类
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/12
 */
public class SqlRunner {

    private static SimpleDataSource simpleDataSource;

    /**
     * 创建数据源
     * @return
     */
    public static SimpleDataSource getDataSource(){
        if (simpleDataSource == null) {
            Props application = new Props("application.properties");
            simpleDataSource.setDriver(application.getProperty("auth.datasource.driver"));
            simpleDataSource.setUrl(application.getProperty("auth.datasource.url"));
            simpleDataSource.setUser(application.getProperty("auth.datasource.user"));
            simpleDataSource.setPass(application.getProperty("auth.datasource.password"));
        }
        return simpleDataSource;
    }

    /**
     * 执行SQL语句
     * @param sql
     * @param param
     * @return
     * @throws SQLException
     */
    public void execute(final String sql, final Object...param) throws SQLException{
        SimpleDataSource dataSource = SqlRunner.getDataSource();

        // 事务
        Db.use(dataSource, dataSource.getDriver()).tx(new TxFunc() {
            @Override
            public void call(Db db) throws SQLException {
                db.execute(sql, param);
            }
        });
    }

}
