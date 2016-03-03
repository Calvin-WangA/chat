package quartz;

import java.io.File;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckFileChangeJob implements Job
{

	private static Logger _log = LoggerFactory
			.getLogger(CheckFileChangeJob.class);

	private File file = new File("friends.xml");
	
	private static Long firstTime = 0L;
	
	public void execute(JobExecutionContext context)
			                 throws JobExecutionException
	{
		CronTrigger trigger = (CronTrigger) context.getTrigger();

		_log.info("The next fire time is " + trigger.getNextFireTime());
		// String path = MyPath.getProjectPath()+"/friends.xml";

        Long lastTime = file.lastModified();
        System.out.println("The first time is "+firstTime);
        System.out.println("The last time is "+lastTime);
        //表示被更新过，重现成功界面
        if((lastTime - firstTime) > 0)
        {
        	System.out.println("The file was updated!");
        }
        
		firstTime = lastTime;

	}

}
