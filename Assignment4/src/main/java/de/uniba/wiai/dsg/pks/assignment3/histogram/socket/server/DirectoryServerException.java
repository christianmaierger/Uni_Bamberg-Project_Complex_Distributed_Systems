package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

public class DirectoryServerException extends Exception {

	private static final long serialVersionUID = 1L;

	public DirectoryServerException() {
		super();
	}

	public DirectoryServerException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DirectoryServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DirectoryServerException(String message) {
		super(message);
	}

	public DirectoryServerException(Throwable cause) {
		super(cause);
	}

}
