import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.joauth2.upgrade.AppUpgrade;
import org.junit.Test;

import java.util.List;

/**
 * [Write Something]
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/5/20
 */
public class JsonTest {

    @Test
    public void upgradeJsonTest(){
        String str = "{\"object\":[{\"id\":12,\"zip\":false,\"rootPath\":\"123\",\"files\":[{\"id\":47,\"filePath\":\"http://127.0.0.1:926//assets/upload/upgrade/2019/4/19/2.0.6.zip\",\"fileName\":\"2.0.6.zip\",\"upgradeId\":12,\"size\":0}],\"title\":\"2.0.6\",\"appid\":4,\"upgradeTime\":\"2019-05-20\",\"ctime\":\"2019-05-19 20:10:46\",\"creator\":1,\"version\":\"2.0.6\"},{\"id\":13,\"zip\":true,\"rootPath\":\"123re\",\"files\":[{\"id\":48,\"filePath\":\"http://127.0.0.1:926//assets/upload/upgrade/2019/4/20/2.0.2.zip\",\"fileName\":\"2.0.2.zip\",\"upgradeId\":13,\"size\":0}],\"title\":\"压缩包测试\",\"appid\":4,\"upgradeTime\":\"2019-05-20\",\"ctime\":\"2019-05-20 10:33:08\",\"creator\":1,\"version\":\"2.0.3\"}],\"code\":10000,\"msg\":\"操作成功\"}";
        JSONObject resultJson = JSONUtil.parseObj(str);
        List<AppUpgrade> upgradeList = JSONUtil.toList(resultJson.getJSONArray("object"), AppUpgrade.class);
        Console.log(upgradeList);
        for (AppUpgrade upgrade : upgradeList) {
            boolean zip = upgrade.getZip();
            Console.log(zip);
        }
    }
}
