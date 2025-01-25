public class Sym {
    // Fields (instance variables)
    String val;

    // Constructor
    public Sym(String type) {
        this.val = type;
    }

    public String getType() {
        return val;
    }

    // to String to return String type of method
    @Override
    public String toString() {
        return this.val;
    }
}