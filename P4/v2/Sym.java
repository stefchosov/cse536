import java.util.HashMap;

class Sym {
	private String type;
	
	public Sym(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public String toString() {
		return "{" + type + "}";
	}
}

class StructDefSym extends Sym {
	private HashMap<String, Sym> fields;
	
	public StructDefSym(String name, HashMap<String, Sym> fields) {
		super(name);
		this.fields = fields;
	}

	Sym getField(String name) { return fields.get(name); }
}

class FunctionDefSym extends Sym {
	public FunctionDefSym(String type) {
		super(type);
	}
}
