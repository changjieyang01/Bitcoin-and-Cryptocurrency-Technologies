package Assignment2;

import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/

/*
Each test is measured based on
- How large a set of nodes have reached consensus. A set of nodes only counts as having
reached consensus if they all output the same list of transactions.
- The size of the set that consensus is reached on. You should strive to make the consensus set
of transactions as large as possible.
- Execution time, which should be within reason (if your code takes too long, the grading script
will time out and you will be able to resubmit your code).
 */

public class CompliantNode implements Node {
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private double numRounds;
    private boolean[] followees;
    private Set<Transaction> pendingTransactions;
//    private Set<Candidate> candidates;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // Inititialize input params
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // This node follows the followee if true.
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // Random 500 txs will be send to each node based off p_txDistribution is .01, .05, or .10
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // Send my pending transactions to my followers
        // Need to account for probability of p_txDistribution and p_malicious nodes before deciding to send to followers?
        return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // Get TXs from sender/followee.  Since my followees are trusted, I will add to my pending transactions set
        for (Candidate candidate : candidates) {
            pendingTransactions.add(candidate.tx);
        }
    }
}
