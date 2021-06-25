package com.youloft.datamigration.cron;

import com.youloft.datamigration.Global;
import com.youloft.datamigration.main.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Schedule {
    @Autowired
    Job job;
    @Scheduled(cron = Global.CRONEXPRESS_MIGRATION)
    public void schedule(){
        job.autoJob();
    }
}
