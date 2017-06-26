package edu.umass.cs.utils;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import edu.umass.cs.reconfiguration.ReconfigurationConfig.RC;

/**
 * This is a utility class to profile throughput 
 * of a function or between any two arbitrary points in a code. 
 * This class provides methods to record the incoming 
 * rate at the beginning of a function, or any arbitrary entry point,
 * and exit rate at the end of a function or any arbitrary exit point. 
 * All methods of this class are thread-safe
 * 
 * @author ayadav
 *
 */
public class ThroughputProfiler 
{
	private static final ConcurrentHashMap<String, Long> incomingRateMap 
							= new ConcurrentHashMap<String, Long>();
	
	private static final ConcurrentHashMap<String, Long> outgoingRateMap
							= new ConcurrentHashMap<String, Long>();
	
	private static PrintThread printThread = new PrintThread();
	
	static
	{
		if(Config.getGlobalBoolean(RC.ENABLE_INSTRUMENTATION))
			new Thread(printThread).start();
	}
	
	// in seconds
	private static long measurementDelay = 5;
	
	public static void recordIncomingEvent(String tag)
	{
		synchronized(incomingRateMap)
		{
			Long val = incomingRateMap.get(tag);
			if(val == null)
			{
				incomingRateMap.put(tag, (long)1);
			}
			else
			{
				incomingRateMap.put(tag, val+1);
			}
		}
	}
	
	
	public static void recordOutgoingEvent(String tag)
	{
		synchronized(outgoingRateMap)
		{
			Long val = outgoingRateMap.get(tag);
			if(val == null)
			{
				outgoingRateMap.put(tag, (long)1);
			}
			else
			{
				outgoingRateMap.put(tag, val+1);
			}
		}
	}
	
	
	private static class PrintThread implements Runnable
	{
		@Override
		public void run() 
		{
			while(true)
			{
				
				try
				{
					Thread.sleep(measurementDelay*1000);
					String str = "";
					Iterator<String> outIter = incomingRateMap.keySet().iterator();
					
					while(outIter.hasNext())
					{
						String tag = outIter.next();
						long incomingReqs =  incomingRateMap.get(tag);
						long outgoingReqs = 0;
						
						if(outgoingRateMap.containsKey(tag))
						{
							outgoingReqs = outgoingRateMap.get(tag);
						}
						str = str+" ["+tag+",incoming="
								+(incomingReqs/measurementDelay)+"/s"
								+",outgoing="+(outgoingReqs/measurementDelay)+"/s"
								+"]";
					}
					
					System.out.println(ThroughputProfiler.class.getName()+":"+str);
					incomingRateMap.clear();
					outgoingRateMap.clear();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				
			}
		}
	}
	
	public static void main(String[] args)
	{
		if(!Config.getGlobalBoolean(RC.ENABLE_INSTRUMENTATION))
			new Thread(printThread).start();
		int i = 0;
		while(i<100)
		{
			i++;
			recordIncomingEvent("test");
			recordOutgoingEvent("test");
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
}