package com.joauth2;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;

import java.util.Date;

/**
 * 定时任务类
 *
 * @author wujiawei0926@yeah.net
 * @see
 * @since 2019/5/6
 */
public class Scheduler {

    /**
     * 定时获取最新的应用数据
     */
    public static void refershClient(){
        Attr.CRON_APPDATA_ID = CronUtil.schedule("*/1 * * * *", new Task() {
            @Override
            public void execute() {
                synchronized (this) {
                    Date endTime = Attr.END_TIME;
                    // 判断是否是间隔的结束时间
                    Date now = new Date();
                    if (endTime == null || endTime.getTime() < now.getTime()) {
                        Client.updateAppData();
                    }
                }
            }
        });
    }

}
