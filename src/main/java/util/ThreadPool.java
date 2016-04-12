package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPool
{
	private static Logger log = LoggerFactory.getLogger(ThreadPool.class);
	
	private static ExecutorService pool = null;
	
	private static ReentrantLock lock = new ReentrantLock();
	
	private ThreadPool()
	{
		
	}
	
	/**
	 * 获得创建的线程池
	 * @param max 线程最大数目
	 * @return
	 */
    public static ExecutorService newThreadPool(int max)
    {
    	lock.lock();
    	try{
        	if(null == pool)
        	{
        		 pool = new ThreadPoolExecutor(
        	    			2, max
        	    			,30, TimeUnit.SECONDS
        	    			,new LinkedBlockingQueue<Runnable>());
        	}
    	}
        catch(Exception e){
        	log.info("Build pool error:" + e.getMessage());
    	}
    	finally{
    		lock.unlock();
    	}
    	
    	return pool;
    }
}
