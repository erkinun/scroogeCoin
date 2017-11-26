import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // check 2
        boolean check2 = tx.getInputs().stream().reduce(true,
                (result, input) -> {
                    Transaction.Output output = _mUtxoPool.getTxOutput(new UTXO(input.prevTxHash, input.outputIndex));
                    return Crypto.verifySignature(output.address, tx.getRawDataToSign(input.outputIndex), input.signature);
                },
                (bool1, bool2) -> bool1 && bool2);

        if (!check2) return false;

        //check 3
        List<UTXO> allUtxos = tx.getInputs()
                .stream()
                .map(input -> new UTXO(input.prevTxHash, input.outputIndex))
                .collect(Collectors.toList());

        List<UTXO> distinctUtxos = allUtxos.stream().distinct().collect(Collectors.toList());
        boolean check3 = allUtxos.size() == distinctUtxos.size();

        if (!check3) return false;

        // check 4
        // all outputs are non-negative
        boolean check4 = tx.getOutputs()
                .stream()
                .reduce(true,
                        (result, output) -> Double.compare(output.value, 0.0) > 0.0,
                        (b1, b2) -> b1 && b2);

        if (!check4) return false;

        // check 5
        // sum of inputs must be greater than or equal to outputs
        Double inputSum = tx.getInputs()
                .stream()
                .map(input -> new UTXO(input.prevTxHash, input.outputIndex))
                .reduce(0.0,
                        (acc, utxo) -> acc + _mUtxoPool.getTxOutput(utxo).value,
                        (acc1, acc2) -> acc1 + acc2);

        Double outputSum = tx.getOutputs()
                .stream()
                .reduce(0.0,
                        (acc, output) -> acc + output.value,
                        (acc1, acc2) -> acc1 + acc2);

        boolean check5 = Double.compare(inputSum, outputSum) >= 0;

        return check5;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS

        List<Transaction> acceptedTrnx = Arrays.stream(possibleTxs)
                .filter(this::isValidTx).collect(Collectors.toList());

        // update utxo pool
        Stream<UTXO> toBeRemoved = acceptedTrnx
                .stream()
                .flatMap(tx -> tx.getInputs()
                        .stream()
                        .map(is -> new UTXO(is.prevTxHash, is.outputIndex)));

        toBeRemoved.forEach(utxo -> _mUtxoPool.removeUTXO(utxo));

        Map<UTXO, Transaction.Output> stupidMap = new HashMap<>();
        Stream<UTXO> toBeAdded = acceptedTrnx.stream()
                .flatMap(tx -> tx.getOutputs().stream()
                .map(output -> {

                    UTXO addMe = new UTXO(tx.getHash(),tx.getOutputs().indexOf(output));
                    stupidMap.put(addMe, output); // mutable operation !!
                    return addMe;
                }));

        toBeAdded.forEach(utxo -> _mUtxoPool.addUTXO(utxo, stupidMap.get(utxo)));

        return acceptedTrnx.toArray(new Transaction[0]);
    }

}
