import java.util.*;

public class SymTab {
   // Fields
    List<HashMap<String, Sym>> list;
   
    /**
     * Constructor for SymTab, created a linked list with an empty hashmap
     */
    public SymTab() {
        list = new LinkedList<>();
        list.addFirst(new HashMap<>());
    }

    /**
     * This method will add a new Sym to the first hashmap
     * @param name
     * @param sym
     * @throws SymDuplicateException
     * @throws SymTabEmptyException
     */
    public void addDecl(String name, Sym sym) throws 
    SymDuplicateException, SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("List is empty");
        }
        else if(list.getFirst().containsKey(name)) {
            throw new SymDuplicateException("Already got this Key");
        }
        else if (name == null | sym == null) {
            throw new IllegalArgumentException("Name or Sym is null");
        }
        else {
            this.list.getFirst().put(name, sym);
        }
    }

    /**
     * This method will add a new hashmap to the list
     */
    public void addScope() {
        HashMap<String, Sym> add = new HashMap<>();
        this.list.addFirst(add);
    }

    /**
     * This method will look up the Sym in the first hashmap
     * @param name of Sym
     * @return Sym of param name
     * @throws SymTabEmptyException if the list is empty
     */
    public Sym lookupLocal(String name) throws SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("This Sym does not exist" + 
                                           "in the first map");
        }
        else {
            return this.list.getFirst().get(name);
        }
    }

    /**
     * This method will look up the Sym in the list of hashmaps
     * @param name of the Sym
     * @return the found Sym of name, or null if not found
     * @throws SymTabEmptyException if the list is empty
     */
    public Sym lookupGlobal(String name) throws SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("SymTab is empty");
        }
        for (HashMap<String,Sym> item: this.list) {
            if (item.containsKey(name)) {
                return item.get(name);
            }
        }
        return null;
    }

    /**
     * This method will remove the first hashmap in the list
     * @throws SymTabEmptyException if the list is empty
     */
    public void removeScope() throws SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("No hashmaps able to be removed");
        }
        else this.list.remove(0);
    }

    /**
     * This method will print out the SymTab
     */
    public void print() {
        System.out.print("\n*** SymTab ***\n");
        for (HashMap<String,Sym> M: this.list) {
            System.out.println(M.toString());
        }
        System.out.print("\n*** DONE ***\n");
    }
}