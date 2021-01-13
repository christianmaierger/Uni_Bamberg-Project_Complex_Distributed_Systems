package de.uniba.wiai.dsg.pks.assignment.model;

public enum Service {

	SEQUENTIAL("SequentialHistogramService"), 
	LOW_LEVEL("LowLevelHistogramService"),
	HIGH_LEVEL("HighLevelHistogramService"), 
	EXECUTOR("ExecutorHistogramService"),
	FORK_JOIN("ForkJoinHistogramService"), 
	STREAM("StreamHistogramService"), 
	SOCKET("SocketHistogramService"),
	COMPLETABLE("CompletableHistogramService"),
	ACTOR("ActorHistogramService");

	private String text;

	Service(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
