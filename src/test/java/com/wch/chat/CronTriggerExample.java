package com.wch.chat;

import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quartz.CheckFileChangeJob;

public class CronTriggerExample
{
	
	@Test
	public void run()
	{
		try
		{
			Logger log = LoggerFactory.getLogger(CronTriggerExample.class);
			
			log.info("-----------Initializing------------");
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler = sf.getScheduler();
			log.info("-----------Initializing Complete----------");
			
			log.info("-----------Starting Scheduler------------");
			JobDetail job = JobBuilder.newJob(CheckFileChangeJob.class).withIdentity("myJob","group1")
					                  .build();
			//采用cronExpression来精确指定执行的时间,参考Developer Guide
			CronTrigger trigger = (CronTrigger)TriggerBuilder.newTrigger().withIdentity("trigger","group1")
					          .withSchedule(CronScheduleBuilder
					        		  .cronSchedule("0/10 * * * * ?"))
					          .build();
			scheduler.scheduleJob(job, trigger);
			
			scheduler.start();
			
			Thread.sleep(3000000L);
			
			log.info("----------Shutting down Scheduler-----------");
			scheduler.shutdown(true);
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
	}

}
