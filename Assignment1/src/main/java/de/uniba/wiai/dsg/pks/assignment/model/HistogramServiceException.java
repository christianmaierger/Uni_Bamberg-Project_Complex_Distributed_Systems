package de.uniba.wiai.dsg.pks.assignment.model;

public class HistogramServiceException extends Exception {

	private static final long serialVersionUID = -6760250165531427291L;

	public HistogramServiceException() {
		super();
	}

	public HistogramServiceException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public HistogramServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public HistogramServiceException(String arg0) {
		super(arg0);
	}

	public HistogramServiceException(Throwable arg0) {
		super(arg0);
	}
}
