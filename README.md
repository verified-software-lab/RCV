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



