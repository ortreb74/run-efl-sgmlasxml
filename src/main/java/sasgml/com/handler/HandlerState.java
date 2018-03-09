package sasgml.com.handler;

public class HandlerState {
	private boolean isActive;
	private int length;

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		this.length = 0;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void incrementLength() {
		this.length++;
	}

	public void decrementLength() {
		this.length--;
	}

	public void reset() {
		setLength(0);
		setActive(false);
	}

	public HandlerState() {
		reset();
	}

}
