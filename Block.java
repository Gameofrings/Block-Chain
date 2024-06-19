import java.util.Date;
import java.util.ArrayList;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); // Fixed the name to indicate multiple transactions
    private long timeStamp; // as number of milliseconds since 1/1/1970.
    private int nonce;

    // Block Constructor
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash(); // making sure we do this after we set the other values.
    }

    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedhash;
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions); // Fixed the name to match the corrected variable
        String target = StringUtil.getDificultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    // Add transaction to this block
    public boolean addTransaction(Transaction transaction) {
        // Process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null) return false;
        if (!previousHash.equals("0")) { // Fixed string comparison
            if (!transaction.processTransaction()) { // Fixed boolean comparison
                System.out.println("Transaction failed to process. Discarded");
                return false;
            }
        }
        transactions.add(transaction); // Fixed the variable to use the correct instance
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}
