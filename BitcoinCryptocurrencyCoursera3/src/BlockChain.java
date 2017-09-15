

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private ByteArrayWrapper newest;
    private ByteArrayWrapper oldest;
    private int maxHeight;
    private TransactionPool transactionPool = new TransactionPool();
    private Map<ByteArrayWrapper, TreeNode> blocksMap;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        Transaction coinBase = genesisBlock.getCoinbase();

        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(coinBase.getHash(), 0);
        utxoPool.addUTXO(utxo, coinBase.getOutput(0));

        TreeNode blockNode = new TreeNode(genesisBlock, null, utxoPool);
        blockNode.height = 0;

        this.oldest = new ByteArrayWrapper(genesisBlock.getHash());
        this.newest = this.oldest;

        this.blocksMap = new HashMap<>();
        this.blocksMap.put(this.oldest, blockNode);
        this.transactionPool = new TransactionPool();
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return this.blocksMap.get(this.newest).block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return this.blocksMap.get(oldest).getUTXOPoolCopy();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        if (block.getPrevBlockHash() == null) return false;

        ByteArrayWrapper preHash = new ByteArrayWrapper(block.getPrevBlockHash());
        TreeNode parent = this.blocksMap.get(preHash);

        if (parent == null) return false;

        UTXOPool curPool = parent.getUTXOPoolCopy();
        TxHandler txHandler = new TxHandler(curPool);
        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);
        Transaction[] validTxs = txHandler.handleTxs(txs);
        if (validTxs.length != txs.length) {
            return false;
        }

        UTXOPool utxoPool = txHandler.getUTXOPool();
        Transaction coinBaseTransaction = block.getCoinbase();
        UTXO utxo = new UTXO(coinBaseTransaction.getHash(), 0);
        utxoPool.addUTXO(utxo, coinBaseTransaction.getOutput(0));

        TreeNode blockNode = new TreeNode(block, parent, utxoPool);
        blockNode.height = parent.height + 1;
        if (blockNode.height <= maxHeight - CUT_OFF_AGE) return false;

        ByteArrayWrapper curHash = new ByteArrayWrapper(blockNode.block.getHash());
        this.blocksMap.put(curHash, blockNode);

        if(blockNode.height > this.maxHeight) {
            this.maxHeight = blockNode.height;
            this.newest = curHash;
        }

        return true;

        // Add to blockchain
//        int children = this.maxNode.nodes.size();
//        TreeNode node = new TreeNode(block);
//        if (children > 0) {
//            this.maxNode.nodes.get(children - 1).addTreeNode(children - 1, node);
//        } else {
//            this.maxNode.nodes.get(0).addTreeNode(0, node);
//        }
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.transactionPool.addTransaction(tx);
    }

    /**
     * Tree representation of the blockchain
     */
    private class TreeNode {
        public Block block;
        public TreeNode parent;
        public List<TreeNode> children;
        public int height;
        private UTXOPool utxoPool;

        public TreeNode(Block block, TreeNode parent, UTXOPool utxoPool) {
            this.block = block;
            this.parent = parent;
            this.utxoPool = utxoPool;
            this.children = new ArrayList<>();

            if (parent != null) {
                parent.children.add(this);
            } else {
                this.height = parent != null ? parent.height + 1 : 1;
            }
        }

        public UTXOPool getUTXOPoolCopy() {
            return utxoPool;
        }
    }
}