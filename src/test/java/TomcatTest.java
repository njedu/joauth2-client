import cn.hutool.core.lang.Console;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.joauth2.upgrade.ServerSniffer;
import org.junit.Test;

import java.io.IOException;

/**
 * [Write Something]
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/5/8
 */
public class TomcatTest {

    private static Log log = LogFactory.get();

    @Test
    public void start() throws IOException {
        String name = "Tomcat7-8080";
        log.info(ServerSniffer.restartService(name));
    }

    @Test
    public void service(){
        ServerSniffer serverSniffer = new ServerSniffer();
        String root = "C:/";
        String path = serverSniffer.restartTomcatByService(root);
        Console.log(path);
    }
}
