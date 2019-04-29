import cn.hutool.core.io.file.FileReader;
import com.joauth2.upgrade.FileManager;
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

}
