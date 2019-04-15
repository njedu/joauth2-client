import com.joauth2.db.DataSource;
import com.joauth2.db.DataSourceConfig;
import org.junit.Test;

/**
 * 数据库配置 - 测试
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/12
 */
public class DbTest{

    @Test
    public void test() throws Exception{
        DataSource source = new DataSource();
        source.setPassword("123").setUrl("jdbc:xxxx").setDriver("mysql").setUser("root");
        DataSourceConfig.init(source);

        DataSourceConfig config = new DataSourceConfig();
        config.execute("123", null);
    }



}
