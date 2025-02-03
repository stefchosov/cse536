public class Sym {
    // Fields (instance variables)
    String type;

    // Constructor
    public Sym(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    // to String to return String type of method
    @Override
    public String toString() {
        return this.type;
    }
}