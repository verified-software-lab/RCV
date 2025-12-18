package edu.udel.cis.vsl.rcv;

/**
 * An exception in the RCV election that is not related to I/O. Exceptions can
 * arise for a number of reasons including: a ballot that contains names not in
 * the candidate list, a ballot that lists a candidate more than once.
 */
public class RCVException extends Exception {

	private static final long serialVersionUID = 1L;

	RCVException(String msg) {
		super(msg);
	}

	public String toString() {
		return "Error: " + this.getMessage();
	}

}
