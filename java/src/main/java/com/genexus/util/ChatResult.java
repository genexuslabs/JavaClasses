package com.genexus.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatResult {
	private static final String END_MARKER = new String("__END__");
	private final BlockingQueue<String> chunks = new LinkedBlockingQueue<>();
	private volatile boolean done = false;

	public synchronized void addChunk(String chunk) {
		if (chunk != null) {
			chunks.offer(chunk);
		}
	}

	public void markDone() {
		done = true;
		chunks.offer(END_MARKER);
	}

	public String getMoreData() {
		try {
			String chunk = chunks.take();
			if (END_MARKER.equals(chunk)) {
				return "";
			}
			return chunk;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return "";
		}
	}

	public boolean hasMoreData() {
		return !(done && chunks.isEmpty());
	}
}