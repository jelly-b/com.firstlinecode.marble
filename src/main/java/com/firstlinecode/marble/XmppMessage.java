package com.firstlinecode.marble;

public class XmppMessage {
	public enum Direction {
		SENT,
		RECEIVED
	}
	
	private Direction direction;
	private String message;
	
	public XmppMessage(Direction direction, String message) {
		this.direction = direction;
		this.message = message;
	}

	public Direction getDirection() {
		return direction;
	}

	public String getMessage() {
		return message;
	}
	
}
