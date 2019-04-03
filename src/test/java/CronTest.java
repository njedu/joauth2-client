import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.joauth2.JOAuthListener;
import org.junit.Test;

import java.util.Date;

/**
 * [Write Something]
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/4/3
 */
public class CronTest {

    @Test
    public void cronTest(){
        /*String cron = CronUtil.schedule("*//*1 * * * * *", new Task() {
            @Override
            public void execute() {
            synchronized (this) {
                Console.log(1);
            }
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();*/
    }
}
