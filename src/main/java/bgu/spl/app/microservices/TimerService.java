package bgu.spl.app.microservices;

import java.util.Timer;
import java.util.TimerTask;

import bgu.spl.app.Logger;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.MicroService;

public class TimerService extends MicroService {
	private final int speed;
	private final int duration;
	private int currentTick;
	private Timer timer;

	public TimerService(String name, int speed, int duration) {
		super(name);
		this.speed = speed;
		this.duration = duration;
		this.currentTick = 0;
		this.timer = new Timer();
	}
	
	class TickTask extends TimerTask {
        public void run() {
        	currentTick++;
        	if (currentTick > duration) {
        		timer.cancel();
        		Logger.getInstance().log("Timer - Sending Terminate broadcast");
        		sendBroadcast(new TerminateBroadcast());
        		
        		return;
        	}
        	
        	Logger.getInstance().log("Tick " + currentTick);
        	Logger.getInstance().log("--------");
            sendBroadcast(new TickBroadcast(currentTick));
        }
    }

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
			terminate();
		});
		
		TickTask tickTask = new TickTask();
		timer.schedule(tickTask, 0, speed);
	}
}
