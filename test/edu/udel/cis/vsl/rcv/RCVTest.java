package edu.udel.cis.vsl.rcv;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RCVTest {
	PrintStream out = System.out;
	Candidate keith = new Candidate("Keith"), kathy = new Candidate("Kathy"), steve = new Candidate("Steve");

	@Test
	public void tie1() throws RCVException {
		out.println("Test tie1...\n");
		Ballot b1 = new Ballot(101, keith, steve, kathy);
		Ballot b2 = new Ballot(102, kathy, steve, keith);
		Set<Candidate> candidates = new HashSet<>(Arrays.asList(keith, kathy, steve));
		Collection<Ballot> ballots = Arrays.asList(b1, b2);
		RCV rcv = new RCV(candidates, ballots);
		ArrayList<Candidate> winners = rcv.execute(out, 1);
		assertEquals(0, winners.size());
		out.println();
	}

	@Test
	public void tie3() throws RCVException {
		out.println("Test tie3...\n");
		Ballot b1 = new Ballot(101, keith, steve, kathy);
		Ballot b2 = new Ballot(102, steve, kathy, keith);
		Ballot b3 = new Ballot(103, kathy, keith, steve);
		Set<Candidate> candidates = new HashSet<>(Arrays.asList(keith, kathy, steve));
		Collection<Ballot> ballots = Arrays.asList(b1, b2, b3);
		RCV rcv = new RCV(candidates, ballots);
		ArrayList<Candidate> winners = rcv.execute(out, 1);
		assertEquals(0, winners.size());
		out.println();
	}

	@Test
	public void keithWins() throws RCVException {
		out.println("Test keithWins...\n");
		Ballot b1 = new Ballot(101, keith, steve, kathy);
		Ballot b2 = new Ballot(102, keith, steve, kathy);
		Ballot b3 = new Ballot(103, keith, steve, kathy);

		Ballot b4 = new Ballot(201, kathy, steve, keith);
		Ballot b5 = new Ballot(202, kathy, steve, keith);
		Ballot b6 = new Ballot(203, kathy, steve, keith);
		Ballot b7 = new Ballot(204, kathy, steve, keith);

		Ballot b8 = new Ballot(301, steve, keith, kathy);
		Ballot b9 = new Ballot(302, steve, keith, kathy);

		Set<Candidate> candidates = new HashSet<>(Arrays.asList(keith, kathy, steve));
		Collection<Ballot> ballots = Arrays.asList(b1, b2, b3, b4, b5, b6, b7, b8, b9);
		RCV rcv = new RCV(candidates, ballots);
		ArrayList<Candidate> winners = rcv.execute(out, 3);
		assertEquals(3, winners.size());
		assertEquals(keith, winners.get(0));
		assertEquals(steve, winners.get(1));
		assertEquals(kathy, winners.get(2));
		out.println();
	}

	@Test
	public void kathyWins() throws RCVException {
		out.println("Test kathyWins...\n");
		Ballot b1 = new Ballot(101, keith, kathy, steve);
		Ballot b2 = new Ballot(102, steve, kathy, keith);
		Ballot b3 = new Ballot(103, kathy, steve, keith);
		Ballot b4 = new Ballot(104, kathy, steve, keith);
		Ballot b5 = new Ballot(105, keith, kathy, steve);
		Set<Candidate> candidates = new HashSet<>(Arrays.asList(keith, kathy, steve));
		Collection<Ballot> ballots = Arrays.asList(b1, b2, b3, b4, b5);
		RCV rcv = new RCV(candidates, ballots);
		ArrayList<Candidate> winners = rcv.execute(out, 3);
		assertEquals(3, winners.size());
		assertEquals(kathy, winners.get(0));
		assertEquals(steve, winners.get(1));
		assertEquals(keith, winners.get(2));
		out.println();
	}

	@Test
	public void kathyWinsFiles() throws FileNotFoundException, IOException, RCVException {
		out.println("Test kathyWinsFiles...\n");
		RCV rcv = RCV.parse("candidates.txt", "ballot1.txt", "ballot2.txt", "ballot3.txt", "ballot3.txt",
				"ballot1.txt");
		ArrayList<Candidate> winners = rcv.execute(out, 1);
		assertEquals(1, winners.size());
		assertEquals(kathy, winners.getFirst());
		out.println();
	}

	@Test
	public void tieFiles() throws FileNotFoundException, IOException, RCVException {
		out.println("Test tieFiles...\n");
		RCV rcv = RCV.parse("candidates.txt", "ballot", 3);
		ArrayList<Candidate> winners = rcv.execute(out, 1);
		assertEquals(0, winners.size());
	}

}
