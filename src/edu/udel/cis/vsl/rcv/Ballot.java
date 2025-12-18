package edu.udel.cis.vsl.rcv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A Ballot is a sequence of Candidates ranked from most to least preferred. It
 * does not need to include all candidates participating in the election. A
 * ballot cannot contain duplicates. The ballot will change over time as the
 * rounds are executed: candidates will be removed.
 */
public class Ballot {

	/**
	 * ID number of this ballot, unique among all ballots participating in this
	 * election.
	 */
	int id;

	/**
	 * The entries in this ballot, from most to least preferred.
	 */
	ArrayList<Candidate> entries;

	/**
	 * Constructs new ballot. The entries are copied into a new array so the given
	 * one may be reclaimed.
	 * 
	 * @param id      the unique ID number for this ballot
	 * @param entries the entries in this ballot, ordered from most to least
	 *                preferred candidate
	 * @throws RCVException             if the ballot contains duplicate entries
	 * @throws IllegalArgumentException if the entries contains duplicates
	 */
	public Ballot(int id, ArrayList<Candidate> entries) throws RCVException {
		this.id = id;
		this.entries = new ArrayList<>(entries);
		// check no candidate occurs twice
		Set<Candidate> set = new HashSet<>();
		for (Candidate c : entries) {
			if (!set.add(c))
				throw new RCVException("Ballot contains duplicate entry: " + c);
		}
	}

	public Ballot(int id, Candidate... entries) throws RCVException {
		this(id, new ArrayList<Candidate>(Arrays.asList(entries)));
	}

	/**
	 * Parses a ballot file to produce a Ballot instance. This requires that the
	 * election candidates are already known and the Candidate instances have
	 * already been created and entered into a map.
	 * 
	 * @param candidateMap a map from candidate names to Candidate instances
	 * @param id           the ID number to assign to this ballot
	 * @param file         the file to parse
	 * @return the new Ballot object
	 * @throws FileNotFoundException if the file is not found
	 * @throws IOException           if an error occurs reading the file
	 * @throws RCVException          if the ballot contains a name not in the
	 *                               candidate list
	 */
	public static Ballot parse(Map<String, Candidate> candidateMap, int id, File file)
			throws FileNotFoundException, IOException, RCVException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		ArrayList<Candidate> entries = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.isBlank())
				continue;
			String name = line.trim();
			Candidate candidate = candidateMap.get(name);
			if (candidate == null) {
				br.close();
				throw new RCVException(
						"Ballot " + id + " (" + file + ") contains name not in candidate list:\n" + name);
			}
			entries.add(candidate);
		}
		br.close();
		return new Ballot(id, entries);
	}

	public Ballot duplicate() throws RCVException {
		return new Ballot(this.id, this.entries);
	}

	/**
	 * Gets the current top entry (most preferred) in this ballot, or null if the
	 * ballot is empty.
	 */
	public Candidate getTop() {
		return entries.isEmpty() ? null : entries.getFirst();
	}

	/**
	 * Removes the candidate c, if present, from this ballot.
	 * 
	 * @param c a candidate (not null)
	 * @return true iff c was presents
	 */
	public boolean remove(Candidate c) {
		return entries.remove(c);
	}

	/**
	 * Removes all candidates from this ballot whose current votes is less than or
	 * equal to bound.
	 */
	public void removeEntriesAtOrBelow(int bound) {
		Predicate<Candidate> filter = new Predicate<>() {
			@Override
			public boolean test(Candidate t) {
				return t.votes <= bound;
			}
		};
		entries.removeIf(filter);
	}

	/**
	 * Prints this ballot to the given stream. Prints the candidates in order, one
	 * on each line.
	 * 
	 * @param out stream to which to print
	 */
	public void print(PrintStream out) {
		for (Candidate c : entries) {
			out.println(c);
		}
	}
}
