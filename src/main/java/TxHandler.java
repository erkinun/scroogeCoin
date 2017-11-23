import java.util.function.BinaryOperator;

public class TxHandler {

    private UTXOPool _mUtxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        _mUtxoPool = new UTXOPool(utxoPool);
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
        // IMPLEMENT THIS

        // check 1
        boolean check1 = tx.getInputs().stream().reduce(true,
                (result, input) -> _mUtxoPool.contains(new UTXO(input.prevTxHash, input.outputIndex)),
                (aBoolean, aBoolean2) -> aBoolean && aBoolean2);

        if (!check1) return false;

        boolean check2 = tx.getInputs().stream().reduce(true,
                (result, input) -> {
                    Transaction.Output output = _mUtxoPool.getTxOutput(new UTXO(input.prevTxHash, input.outputIndex));
                    return Crypto.verifySignature(output.address, tx.getRawDataToSign(input.outputIndex), input.signature);
                },
                (bool1, bool2) -> bool1 && bool2);

        if (!check2) return false;

        throw new RuntimeException("not implemented yet!");
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        throw new RuntimeException("not implemented yet!");
    }

}
