package edu.udel.cis.vsl.rcv;

/**
 * A candidate in the election, together with the candidate's current votes. Two
 * candidates are considered equal if they have the same name; the votes is
 * considered extrinsic data and is not used in the equals method.
 */
public class Candidate {

	/**
	 * Name of the candidate.
	 */
	String name;

	/**
	 * Current number of first-position votes this candidate has.
	 */
	int votes = 0;

	/**
	 * Creates new Candidate instance from given name, with 0 votes.
	 * 
	 * @param name candidate's name (non-null)
	 */
	public Candidate(String name) {
		if (name == null)
			throw new IllegalArgumentException("null candidate name");
		this.name = name;
	}

	@Override
	public String toString() {
		return name + " (" + votes + ")";
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Candidate && ((Candidate) obj).name.equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
