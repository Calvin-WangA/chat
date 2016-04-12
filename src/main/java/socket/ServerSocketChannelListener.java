package socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerSocketChannelListener
{
	private final int PORT = 5200;
	
	private int MAX_THREADS = 2;
	
	private ExecutorService pool;
	
	ServerSocketChannelListener()
	{
		MAX_THREADS = Runtime.getRuntime().availableProcessors();
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(MAX_THREADS
				, MAX_THREADS, 15L,TimeUnit.SECONDS
				, new LinkedBlockingDeque<Runnable>(MAX_THREADS)
				, new ThreadPoolExecutor.CallerRunsPolicy());
		threadPool.allowCoreThreadTimeOut(true);
		pool = threadPool;
		
		for(int i = 0; i < MAX_THREADS; i++)
		{
			//pool.execute(command);
		}
	}

}
