package bgu.spl.app.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
	private int currentTick;

	public TickBroadcast(int currentTick) {
		this.currentTick = currentTick;
	}

	public int getCurrentTick() {
		return currentTick;
	}
}
