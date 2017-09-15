package Assignment1;

import Assignment3.Crypto;
import Assignment3.Transaction;
import Assignment3.UTXO;
import Assignment3.UTXOPool;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author cyang
 *
 */
public class TxHandler {
	private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	UTXOPool seenUTXOs = new UTXOPool();

    	List<Transaction.Output> outputs = tx.getOutputs();
    	List<Transaction.Input> inputs = tx.getInputs();

    	double inputSum = 0;
    	double outputSum = 0;

    	for (Transaction.Input input : inputs) {
    		// Case 1)
    		UTXO utxo = new UTXO(tx.getHash(), input.outputIndex);
    		Transaction.Output output = utxoPool.getTxOutput(utxo);

    		if (!utxoPool.contains(utxo)) return false;

    		// Case 2)
    		boolean isValidSig = Crypto.verifySignature(utxoPool.getTxOutput(utxo).address, tx.getRawDataToSign(input.outputIndex), input.signature);
    		if (!isValidSig) return false;

    		// Case 3)
    		if (!seenUTXOs.contains(utxo)) {
    			seenUTXOs.addUTXO(utxo, output);
    		} else return false;

    		// Input value from preTx
    		UTXO preUTXO = new UTXO(input.prevTxHash, input.outputIndex);
    		inputSum += utxoPool.getTxOutput(preUTXO).value;
    	}

		// Case 4)
    	for (Transaction.Output output : outputs) {
    		if (output.value < 0) return false;

    		outputSum += output.value;
    	}

    	// Case 5)
    	return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> validTxList = new ArrayList<Transaction>();
        boolean ready = false;

        while (!ready) {
        	ready = true;

        	for (int i = 0; i < possibleTxs.length; i ++) {
            	if (possibleTxs[i] == null) continue;

            	if (isValidTx(possibleTxs[i])) {
            		List<Transaction.Input> inputs = possibleTxs[i].getInputs();
            		for (int j = 0; j < inputs.size(); j ++) {
            			Transaction.Input tempInput = inputs.get(j);
            			// Remove consumed UTXO
            			UTXO tempUTXO = new UTXO(tempInput.prevTxHash,tempInput.outputIndex);
            			utxoPool.removeUTXO(tempUTXO);
            		}

            		List<Transaction.Output> outputs = possibleTxs[i].getOutputs();
            		for(int j = 0; j < outputs.size(); j ++) {
            			Transaction.Output output = outputs.get(j);
            			UTXO tempUTXO = new UTXO(possibleTxs[i].getHash(), j);
            			// Add new UTXO
            			utxoPool.addUTXO(tempUTXO, output);
            		}
            		validTxList.add(possibleTxs[i]);
            		possibleTxs[i++] = null;
            		ready = false;
            	}
        	}
        }

        // Transform to array
        Transaction[] trans = new Transaction[validTxList.size()];
        trans = validTxList.toArray(trans);
        return trans;
    }

	public UTXOPool getUTXOPool() {
		return utxoPool;
	}
}
