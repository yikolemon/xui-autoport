package com.yikolemon;

import cn.hutool.cron.CronUtil;


public class Main {

    public static void main(String[] args) {
        // 秒 分 时 日 月 周年
        String cronExpression = "0 0 4 * * ?";
        // 设置任务执行逻辑
        CronUtil.schedule(cronExpression, new PortRunnable());
        // 支持秒级别的定时任务
        CronUtil.setMatchSecond(true);
        // 启动定时任务调度
        CronUtil.start();
        sleep();
    }

    private static void sleep(){
        // 让主线程保持活动，直到程序需要退出
        // 这里使用 Thread.sleep(Long.MAX_VALUE) 来阻塞主线程
        try {
            // 主线程进入无限阻塞状态，直到程序被外部停止
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
