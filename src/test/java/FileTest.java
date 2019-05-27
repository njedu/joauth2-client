import cn.hutool.core.lang.Console;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.setting.dialect.Props;
import com.joauth2.Attr;
import com.joauth2.upgrade.FileManager;
import com.joauth2.upgrade.SqlRunner;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * [Write Something]
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/29
 */
public class FileTest {

    @Test
    public void copyFromWebTest() throws FileNotFoundException{
        String fromUrl = "https://g.csdnimg.cn/track/1.1.1/track.js";
        String toUrl = "D:/joauth2/asd";

        boolean success = FileManager.download(fromUrl, toUrl);
        System.out.println(success);
    }

    @Test
    public void testUpgrade(){
        String fromUrl = "http://192.168.31.178:8282/vcare/temp/2.0.2.zip",
                fileName = "2.0.2.zip",
                toUrl = "C:\\Program Files\\Apache Software Foundation\\Tomcat 7.0_Tomcat7-8282\\webapps\\vcare";
        FileManager.downloadZip(fromUrl, fileName, toUrl);
    }

    @Test
    public void getProjectPath(){
        Console.log(FileManager.getProjectPath());
        Console.log(System.getProperty("catalina.home"));

    }

    @Test
    public void testSqlRunner(){
        Attr.props = new Props("application.properties");
        SimpleDataSource simpleDataSource = SqlRunner.getDataSource();
        Console.log(simpleDataSource.toString());
    }

    @Test
    public void readZip(){

    }


}
