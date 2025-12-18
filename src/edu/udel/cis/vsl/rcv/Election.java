package edu.udel.cis.vsl.rcv;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An instance of this class models a single RCV election, used to determine a
 * single winner. An instance is constructed from a set of Candidates and a
 * collection of Ballots. At that point it is in its initial state, known as
 * round 1. Calling method {@link #execute(PrintStream)} will cause the RCV
 * algorithm to run through all rounds until the winner is found or all
 * candidates are eliminated (and the election fails).
 */
public class Election {

	/**
	 * The active candidates. This is initially all candidates, but as rounds
	 * progress, candidates are removed. This is always sorted by the current number
	 * of votes assigned to a candidate, from most to least votes. For candidates
	 * with the same number of votes, alphabetical order is used.
	 */
	private ArrayList<Candidate> candidates;

	/**
	 * The active ballots. This is initially all ballots, then ballots are removed
	 * as they become empty.
	 */
	private Collection<Ballot> ballots;

	/**
	 * Current election run-off round. Round 1 is the initial state, in which no
	 * candidates have been eliminated and the votes have been tabulated by using
	 * the top lines only of all ballots.
	 */
	private int round = 0;

	/**
	 * Compare first by votes, then by name.
	 */
	private Comparator<Candidate> voteComparator = new Comparator<>() {
		@Override
		public int compare(Candidate o1, Candidate o2) {
			if (o1.votes < o2.votes)
				return 1;
			if (o1.votes > o2.votes)
				return -1;
			return o1.name.compareTo(o2.name);
		}
	};

	/**
	 * Creates new Election instance to carry out an election. Initializes all
	 * fields, sets round to 1. The candidateSet is copied, so candidateSet can be
	 * reclaimed after this constructor returns. The given Ballots are duplicated so
	 * the collection ballots and the individual Ballots in that collection can all
	 * be safely reclaimed as well.
	 * 
	 * @param candidateSet the set of candidates participating in the election
	 * @param ballots      the ballots
	 * @throws RCVException if two ballots have the same ID number
	 * @throws RCVException if a ballot contains a candidate not in the given
	 *                      candidateSet
	 */
	public Election(Set<Candidate> candidateSet, Collection<Ballot> ballots) throws RCVException {
		if (candidateSet == null)
			throw new IllegalArgumentException("null candidates");
		if (ballots == null)
			throw new IllegalArgumentException("null ballots");
		this.candidates = new ArrayList<>(candidateSet);
		this.ballots = new ArrayList<>();
		Set<Integer> idSet = new HashSet<>();
		for (Ballot ballot : ballots) {
			if (!idSet.add(ballot.id))
				throw new RCVException("Duplicate ballot id " + ballot.id);
			for (Candidate c : ballot.entries) {
				if (!candidateSet.contains(c))
					throw new RCVException("Ballot entry " + c + " does not occur in candidate list");
			}
			this.ballots.add(ballot.duplicate());
		}
		computeAndSort();
		round = 1;
	}

	/**
	 * Prints current round number and the vote total for each candidate.
	 * 
	 * @param out stream to which to print
	 */
	public void printState(PrintStream out) {
		out.println("Round " + round + ":");
		for (Candidate c : candidates)
			out.println(c);
	}

	/**
	 * Computes the current vote totals for each candidate, based on the current
	 * ballots. Then sorts the candidates from highest to lowest vote total (ties
	 * are broken by alphabetical order of names).
	 */
	private void computeAndSort() {
		for (Candidate c : this.candidates)
			c.votes = 0;
		for (Ballot ballot : ballots) {
			if (!ballot.entries.isEmpty())
				ballot.entries.getFirst().votes++;
		}
		this.candidates.sort(voteComparator);
	}

	/**
	 * Removes all candidates with votes less than or equal to bound from both the
	 * candidate list and all ballots. Then removes any ballots which have become
	 * empty.
	 * 
	 * @param bound upper bound on the votes of candidates to remove
	 */
	private void removeCandidatesAtOrBelow(int bound) {
		while (!candidates.isEmpty() && candidates.getLast().votes <= bound)
			candidates.removeLast();
		for (Ballot ballot : ballots)
			ballot.removeEntriesAtOrBelow(bound);
		// remove empty ballots...
		Predicate<Ballot> filter = new Predicate<>() {
			@Override
			public boolean test(Ballot t) {
				return t.entries.isEmpty();
			}
		};
		ballots.removeIf(filter);
	}

	/**
	 * Executes the election by iterating through the rounds, updating the
	 * candidates and ballots. Stops as soon as a winner is determined, or the well
	 * runs dry. Prints all information to out.
	 * 
	 * @param out stream to which to print
	 * @return the winner, or null if there is none
	 */
	public Candidate execute(PrintStream out) {
		while (true) {
			printState(out);
			if (candidates.isEmpty()) {
				out.println("No active candidates.   Election failed.");
				return null;
			}
			Candidate top = candidates.getFirst();
			if (2 * top.votes > ballots.size()) {
				out.println("\nWinner: " + top);
				return top;
			}
			int lowScore = candidates.getLast().votes;
			removeCandidatesAtOrBelow(lowScore);
			computeAndSort();
			round++;
			out.println();
		}
	}
}
