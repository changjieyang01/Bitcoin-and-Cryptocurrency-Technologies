package Assignment2;

import java.util.Random;
import java.util.Set;

public class MaliciousNode implements Node {

    private Set<Transaction> pendingTransactions;
    // Same size used in simulation
    private int TX_SIZE = 500;

    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        Random random = new Random();
        for (int i = 0; i < TX_SIZE; i ++) {
            pendingTransactions.add(new Transaction(random.nextInt()));
        }
    }

    public void setFollowees(boolean[] followees) {
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        return;
    }

    public Set<Transaction> sendToFollowers() {
        // Be functionally dead and never actually broadcast any transactions.
        // constantly broadcasts its own set of transactions and never accept transactions given to it.
        // change behavior between rounds to avoid detection.
        Random random = new Random();

        if (random.nextBoolean()) {
            return null;
        } else {
            return pendingTransactions;
        }
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // 🤓🤓😧 Ignore Candidates since I'm a malicious node
        return;
    }
}
