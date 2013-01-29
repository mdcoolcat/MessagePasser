package ds.lab.messagepasser;

import java.util.concurrent.atomic.AtomicInteger;

import ds.lab.bean.LogicalTimeStamp;
import ds.lab.bean.TimeStamp;
import ds.lab.message.TimeStampMessage;

public class LogicalClock extends ClockService {

	//private AtomicInteger currentTimeStamp=null;
	private LogicalTimeStamp lts;
	
	//TODO constructor
	public LogicalClock(String localName)
	{
		lts = new LogicalTimeStamp();
		//initialising the logical clock for the thread
		lts.getVector().put(localName, new AtomicInteger(-1));//Init timestamp of this thread with -1
		lts.getVector().get(localName).incrementAndGet();//Start timestamp of this thread with 0
		System.out.println("Logical clock started for "+localName+" with init value"+lts.getVector().get(localName).get());
	}
	
	@Override
	LogicalTimeStamp getNewTimeStamp(String localName) {
		// TODO Auto-generated method stub
		lts.getVector().get(localName).incrementAndGet();
		
		return lts;
	}
	
	@Override
	LogicalTimeStamp getCurrentTimeStamp(String localName){
		return lts;
	}
	
	LogicalTimeStamp updateTimeStampOnReceive(String localName, TimeStampMessage Tsm)
	{
		int selfTimeStamp=this.lts.getVector().get(localName).get();
		int senderTimeStamp=((LogicalTimeStamp) Tsm.getTimeStamp()).getVector().get(Tsm.getSrc()).get();
		int updateSelfTimeStamp=((selfTimeStamp>senderTimeStamp)?selfTimeStamp:senderTimeStamp)+1;
		this.lts.getVector().get(localName).set(updateSelfTimeStamp);
		System.err.println("Local time stamp on reception = "+this.lts.getVector().get(localName).get());
		return this.lts;
	}
}
