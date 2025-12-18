package edu.udel.cis.vsl.rcv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * RCV: Ranked-Choice Vote tabulator. This class is used to parse ballots for an
 * RCV (aka "instant run-off voting") election and execute one or more Elections
 * based on those ballots. Multiple elections may be run to find a first place,
 * second place, etc., winner.
 * </p>
 * 
 * <p>
 * This class provides a command line interface. The command line syntax can be
 * seen in the documentation for the main method.
 * </p>
 * 
 * <p>
 * The user provides: (1) a "candidate file" which contains a list of the names
 * of all candidates participating in the election, one name per line (blank
 * lines will be ignored), and (2) a sequence of "ballot files". Each ballot
 * file contains an ordered list of candidate names, one per line, ordered from
 * most to least preferred. Again, blank lines are ignored. All names in a
 * ballot must be in the candidate list, but the ballot does not have to include
 * every candidate.
 * </p>
 * 
 * @author Stephen Siegel
 */
public class RCV {
	/**
	 * The candidates. This set is immutable: it will not change. Though it is
	 * possible for the state of each Candidate in the collection to change.
	 */
	private Set<Candidate> candidates;

	/**
	 * The ballots. This collection is immutable: it will not change. The Ballot
	 * objects that belong to this collection are also immutable. However, the
	 * Candidates referenced from those Ballot objects may have changes of state.
	 */
	private Collection<Ballot> ballots;

	/**
	 * Constructs new instance based on given collections. The collections are used
	 * directly, nothing is copied.
	 * 
	 * @param candidates set of Candidates participating in the election(s)
	 * @param ballots    the ballots cast
	 */
	public RCV(Set<Candidate> candidates, Collection<Ballot> ballots) {
		this.candidates = candidates;
		this.ballots = ballots;
	}

	/**
	 * Parses the given files to create a new RCV instance.
	 * 
	 * @param candidateFile file containing list of names of all candidates
	 *                      participating in this election, one per line (blank
	 *                      lines are ignored)
	 * @param ballotFiles   one file for each ballot; each ballot contains an
	 *                      ordered list of candidate names, from most to least
	 *                      preferred
	 * @return the new RCV instance, initialized to round 1
	 * @throws FileNotFoundException if one of the files is not found
	 * @throws IOException           if an error occurs reading one of the files
	 * @throws RCVException          if the candidate list contains duplicates, or a
	 *                               ballot contains an error
	 */
	static RCV parse(File candidateFile, File[] ballotFiles) throws FileNotFoundException, IOException, RCVException {
		BufferedReader br = new BufferedReader(new FileReader(candidateFile));
		Map<String, Candidate> candidateMap = new HashMap<>();
		String line;
		while ((line = br.readLine()) != null) {
			if (line.isBlank())
				continue;
			String name = line.trim();
			Candidate candidate = candidateMap.get(name);
			if (candidate != null) {
				br.close();
				throw new RCVException("Duplicate candidate: " + name + " in " + candidateFile);
			}
			candidate = new Candidate(name);
			candidateMap.put(name, candidate);
		}
		br.close();
		int id = 1;
		ArrayList<Ballot> ballots = new ArrayList<>();
		for (File file : ballotFiles) {
			Ballot ballot = Ballot.parse(candidateMap, id, file);
			ballots.add(ballot);
			id++;
		}
		Set<Candidate> candidates = new HashSet<>(candidateMap.values());
		return new RCV(candidates, ballots);
	}

	/**
	 * Parses the files with the given file names to create a new RCV instance.
	 * 
	 * @param candidateFilename name of the candidate file
	 * @param ballotFilenames   names of the ballot files
	 * @return the new RCV instance
	 * @throws FileNotFoundException if one of the files is not found
	 * @throws IOException           if an error occurs reading one of the files
	 * @throws RCVException          if the candidate list contains a duplicate, or
	 *                               a ballot is erroneous
	 */
	public static RCV parse(String candidateFilename, String... ballotFilenames)
			throws FileNotFoundException, IOException, RCVException {
		File candidateFile = new File(candidateFilename);
		File[] ballotFiles = new File[ballotFilenames.length];
		for (int i = 0; i < ballotFilenames.length; i++)
			ballotFiles[i] = new File(ballotFilenames[i]);
		return parse(candidateFile, ballotFiles);
	}

	private static String[] ballotFilenames(String ballotRoot, int numBallots) {
		String[] ballotFilenames = new String[numBallots];
		for (int i = 0; i < numBallots; i++)
			ballotFilenames[i] = ballotRoot + (i + 1) + ".txt";
		return ballotFilenames;
	}

	/**
	 * Parses in case where ballot files are specified by a filename root and a
	 * number n. The ballot filenames are taken to be root1.txt, root2.txt, ...,
	 * rootn.txt.
	 * 
	 * @param candidateFilename the name of the file containing all candidates
	 * @param ballotRoot        root for all ballot filenames
	 * @param numBallots        the number of ballots
	 * @return RCV object constructed by parsing above
	 * @throws IOException           if something goes wrong reading the files
	 * @throws FileNotFoundException if one of the files is not found
	 * @throws RCVException          if the candidate list contains a duplicate, or
	 *                               a ballot is erroneous
	 */
	public static RCV parse(String candidateFilename, String ballotRoot, int numBallots)
			throws FileNotFoundException, IOException, RCVException {
		String[] ballotFilenames = ballotFilenames(ballotRoot, numBallots);
		return parse(candidateFilename, ballotFilenames);
	}

	/**
	 * Prints usage information to given stream.
	 * 
	 * @param out stream to which to print
	 */
	public static void usage(PrintStream out) {
		out.println("rcv: Ranked Choice Vote tabulator");
		out.println();
		out.println("RCV is also known as \"instant runoff voting\".");
		out.println("Given a file containing the list of candidates, and a sequence");
		out.println("of ballot files, rcv executes the RCV algorithm to determine");
		out.println("the winner of an election.");
		out.println();
		out.println("Usage: rcv candidates.txt [options] ballot1.txt ballot2.txt ... ");
		out.println("Options:");
		out.println("-m M ");
		out.println("  find top M winners (default: 1)");
		out.println("-r R");
		out.println("  root of ballot filenames, use with -n (default: null)");
		out.println("-n N");
		out.println("  ballot filenames are R1.txt, R2.txt, ..., RN.txt (default: 0)");
	}

	/**
	 * Executes numPlaces complete elections. After the first election, the winner
	 * is removed, then the second election is run to determine the second place
	 * winner. This proceeds numPlaces times.
	 * 
	 * @param out       where to print the output
	 * @param numPlaces the number of places to compute, same as the number of
	 *                  elections to run
	 * @return the winners, in order: first place, second place, etc. This list may
	 *         have length less than numPlaces if any election fails to produce a
	 *         winner.
	 * @throws RCVException if a ballot contains a name not in the candidate set
	 */
	public ArrayList<Candidate> execute(PrintStream out, int numPlaces) throws RCVException {
		Set<Candidate> cs = new HashSet<>(candidates);
		Collection<Ballot> bs = new ArrayList<>();
		ArrayList<Candidate> winners = new ArrayList<>();
		for (Ballot ballot : ballots)
			bs.add(ballot.duplicate());
		for (int i = 0; i < numPlaces; i++) {
			out.println("Computing winner in place " + (i + 1) + ":\n");
			Election el = new Election(cs, bs);
			Candidate winner = el.execute(out);
			if (winner == null)
				break;
			winners.add(winner);
			cs.remove(winner);
			for (Ballot ballot : bs)
				ballot.remove(winner);
		}
		return winners;
	}

	private static void clerr(String msg) {
		System.err.println("Command line error: " + msg);
		usage(System.out);
		System.exit(1);
	}

	/**
	 * Command line interface. See {@link #usage(PrintStream)} for details.
	 * 
	 * @param args command line arguments
	 */
	public final static void main(String[] args) {
		int narg = args.length;
		String candidateFilename = null;
		ArrayList<String> ballotFilenameList = new ArrayList<>();
		int m = 1; // number of places to compute
		int n = 0; // number of ballots if ballot filename pattern is used
		String root = null; // root of ballot filename (optional)

		try {
			for (int i = 0; i < narg; i++) {
				String arg = args[i];
				if (arg.equals("-h")) {
					usage(System.out);
					System.exit(0);
				} else if (arg.equals("-m")) {
					i++;
					if (i == narg)
						clerr("expected integer after -m");
					arg = args[i];
					try {
						m = Integer.valueOf(arg);
					} catch (NumberFormatException e) {
						clerr("expected integer after -m but saw " + arg);
					}
					if (m < 1)
						clerr("m must be at least 1, but saw " + m);
				} else if (arg.equals("-n")) {
					i++;
					if (i == narg)
						clerr("expected integer after -n");
					arg = args[i];
					try {
						n = Integer.valueOf(arg);
					} catch (NumberFormatException e) {
						clerr("expected integer after -n but saw " + arg);
					}
					if (n < 0)
						clerr("n must be at least 0, but saw " + n);
				} else if (arg.equals("-r")) {
					i++;
					if (i == narg)
						clerr("expected string after -r");
					arg = args[i];
					root = arg;
				} else {
					if (candidateFilename == null) {
						candidateFilename = arg;
					} else {
						ballotFilenameList.add(arg);
					}
				}
			}
			if (candidateFilename == null)
				clerr("no candidate file specified");
			if (n > 0) {
				if (root == null)
					clerr("must specify root if using -n");
				ballotFilenameList.addAll(Arrays.asList(ballotFilenames(root, n)));
			}
			int numBallots = ballotFilenameList.size();
			String[] ballotFilenames = ballotFilenameList.toArray(new String[numBallots]);
			RCV rcv = RCV.parse(candidateFilename, ballotFilenames);
			rcv.execute(System.out, m);
		} catch (RCVException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (FileNotFoundException e) {
			System.err.println("File not found error: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
		}
	}
}
