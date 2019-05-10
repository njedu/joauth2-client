package com.joauth2.upgrade;

import cn.hutool.db.Db;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.db.transaction.TxFunc;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.dialect.Props;
import com.joauth2.Attr;

import java.sql.SQLException;

/**
 * 数据源配置类
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/12
 */
public abstract class SqlRunner {

    private static Log log = LogFactory.get();

    private static SimpleDataSource simpleDataSource;

    /**
     * 创建数据源
     * @return
     */
    public static SimpleDataSource getDataSource(){
        // 优先使用@Override重写的方法
        /*JOAuthDataSource dataSource = simpleDataSource;
        if (dataSource != null) {
            simpleDataSource = dataSource;
            return simpleDataSource;
        }*/

        // 没有重写，就用配置文件
        if (simpleDataSource == null) {
            Props application = Attr.props;
            //String url, String user, String pass, String driver
            simpleDataSource = new SimpleDataSource(
                    application.getStr("auth.datasource.url"),
                    application.getStr("auth.datasource.user"),
                    application.getStr("auth.datasource.password"),
                    application.getStr("auth.datasource.drive")
            );
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
    public static void execute(final String sql) throws SQLException{
        SimpleDataSource dataSource = SqlRunner.getDataSource();
        // 事务
        Db.use(dataSource, dataSource.getDriver()).tx(new TxFunc() {
            @Override
            public void call(Db db) throws SQLException {
                log.info("[upgrade]: " + sql);
                db.execute(sql);
            }
        });
    }

}
