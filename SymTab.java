import java.util.*;

public class SymTab {
   // Fields
    List<HashMap<String, Sym>> list;
   
    public SymTab() {
        list = new ArrayList<>();
    }

    public void addDecl(String name, Sym sym) throws SymDuplicateException, SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("list is empty lil bro");
        }
        else if(list.get(0).containsKey(name)) {
            throw new SymDuplicateException("we already got this one lil bro");
        }
        else if (name == null | sym == null) {
            throw new IllegalArgumentException("name or sym is null");
        }
        else {
            this.list.get(0).put(name, sym);
        }
    }

    public void addScope() {
        HashMap<String, Sym> add = new HashMap<>();
        this.list.add(0, add);
    }

    public Sym lookupLocal(String name) throws SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("that sym ain't in the first map");
        }
        else {
            return this.list.get(0).get(name);
        }
    }

    public Sym lookupGlobal(String name) throws SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("not going to find yourself that Sym");
        }
        for (HashMap<String,Sym> item: this.list) {
            if (item.containsKey(name)) {
                return item.get(name);
            }
        }
        return null;
    }

    public void removeScope() throws SymTabEmptyException {
        if(this.list.isEmpty()) {
            throw new SymTabEmptyException("No hashmaps to remove lil bro");
        }
        else this.list.remove(0);
    }

    public void print() {
        System.out.print("\n*** SymTab ***\n");
        for (HashMap<String,Sym> M: this.list) {
            System.out.println(M.toString());
            System.out.println();
        }
        System.out.print("\n*** DONE ***\n");
    }
}