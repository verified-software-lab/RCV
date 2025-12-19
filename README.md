# RCV: Ranked Choice Voting Tabulator

Ranked Choice Voting, also known as Instant Run-off Voting, is a voting protocol used
to select a single winner from a list of candidates.
See [this Wikipedia article](https://en.wikipedia.org/wiki/Instant-runoff_voting) for
a description of the protocol and its properties.

## Summary

This program is used to compute the results of an RCV election.
The user provides (1) a candidate file, and (2) one or more ballot files.
The candidate file is a plain text document containing the names of all
candidates participating in the election, one per line.  A ballot file
is a plain text document containing an ordered list of candidates,
from most to least preferred, one per line.  For both types of files,
blank lines are ignored.  Given these inputs, the program executes
the RCV algorithm to determine the winner (or failure).

The program can also be used to find second-place, third-place, etc.
winners.   The protocol for this is as follows: after the first-place
winner is found using the standard algorithm, this winner is deleted
from all ballots, and a new election is executed on the resulting
ballots.  The winner of this new election is the second-place winner.
This process is repeated as many times as requested.

## Installation and Usage

You need to have a Java runtime installed on your machine to use RCV.

RCV has a command line interface.   To install the tool, download the
file `RCV.jar` from the latest release.  Place this file anywhere you like
on your local machine.  In your shell start-up configuration
script (e.g., `.profile`, `.bash_profile`, or `.zprofile`) create an alias
as follows:

```
alias rcv="java -jar /path/to/RCV.jar"
```
where `/path/to/RCV.jar` is replaced with the absolute path to `RCV.jar`
on your machine.  Source the script (or log out and back in).
The command `rcv` should now be available to you from the shell.
Typing `rcv -h` will show the command line syntax.

## Examples

Help:
```
siegel@giacomo examples % rcv -h
rcv: Ranked Choice Vote tabulator

RCV is also known as "instant runoff voting".
Given a file containing the list of candidates, and a sequence
of ballot files, rcv executes the RCV algorithm to determine
the winner of an election.

Usage: rcv candidates.txt [options] ballot1.txt ballot2.txt ... 
Options:
-m M 
  find top M winners (default: 1)
-r R
  root of ballot filenames, use with -n (default: null)
-n N
  ballot filenames are R1.txt, R2.txt, ..., RN.txt (default: 0)
```
Example candidate list and ballots:
```
siegel@giacomo examples % cat candidates.txt 
Steve
Keith
Kathy
siegel@giacomo examples % cat ballot1.txt 
Keith
Kathy
Steve
siegel@giacomo examples % cat ballot2.txt 
Steve
Kathy
Keith
siegel@giacomo examples % cat ballot3.txt 
Kathy
Steve
Keith
```

Run an election that ends in failure:
```
siegel@giacomo examples % rcv candidates.txt ballot1.txt ballot2.txt ballot3.txt 
Computing winner in place 1:

Round 1:
Kathy (1)
Keith (1)
Steve (1)

Round 2:
No active candidates.   Election failed.
```

An election that succeeds:
```
siegel@giacomo examples % rcv candidates.txt ballot1.txt ballot1.txt ballot2.txt ballot2.txt ballot3.txt 
Computing winner in place 1:

Round 1:
Keith (2)
Steve (2)
Kathy (1)

Round 2:
Steve (3)
Keith (2)

Winner: Steve (3)
```

Same election, but also find second and third place winners:
```
siegel@giacomo examples % rcv -m 3 candidates.txt ballot1.txt ballot1.txt ballot2.txt ballot2.txt ballot3.txt
Computing winner in place 1:

Round 1:
Keith (2)
Steve (2)
Kathy (1)

Round 2:
Steve (3)
Keith (2)

Winner: Steve (3)
Computing winner in place 2:

Round 1:
Kathy (3)
Keith (2)

Winner: Kathy (3)
Computing winner in place 3:

Round 1:
Keith (5)

Winner: Keith (5)
```
Ballot filename pattern option:
```
siegel@giacomo examples % rcv candidates.txt -n 3 -r ballot                             
Computing winner in place 1:

Round 1:
Kathy (1)
Keith (1)
Steve (1)

Round 2:
No active candidates.   Election failed.
```
