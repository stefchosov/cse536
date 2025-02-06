public class SymTabEmptyException extends Exception {
    /**
     * This method will throw an exception if the SymTab is empty
     * @param message for the message of the exception
     */
    public SymTabEmptyException(String message) {
        super(message);
    }
}