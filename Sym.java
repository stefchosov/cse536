public class Sym {
    // Fields (instance variables)
    String type;

    // Constructor
    public Sym(String type) {
        this.type = type;
    }

    /**
     * This method will return the type of the Sym
     * @return String type of Sym
     */
    public String getType() {
        return this.type;
    }

    /**
     * This method will return the type of the Sym
     * @return String type of Sym
     */
    @Override
    public String toString() {
        return this.type;
    }
}