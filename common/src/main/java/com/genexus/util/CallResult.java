package com.genexus.util;

import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;

public class CallResult {
	private boolean success = true;
	private boolean fail;
	private final GXBaseCollection<SdtMessages_Message> messages = new GXBaseCollection<>();

	public boolean success() {
		return success;
	}

	public void setFail() {
		fail = true;
		success =false;
	}

	public boolean fail() {
		return fail;
	}

	public void addMessage(SdtMessages_Message message) {
		messages.add(message);
	}
	public GXBaseCollection<SdtMessages_Message> getMessages() {
		return messages;
	}
}
