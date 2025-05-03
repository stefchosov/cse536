import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a bach program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and identifiers contain line and character 
// number information; for string literals and identifiers, they also 
// contain a string; for integer literals, they also contain an integer 
// value.
//
// Here are all the different kinds of AST nodes and what kinds of 
// children they have.  All of these kinds of AST nodes are subclasses
// of "ASTnode".  Indentation indicates further subclassing:
//
//     Subclass              Children
//     --------              --------
//     ProgramNode           DeclListNode
//     DeclListNode          linked list of DeclNode
//     DeclNode:
//       VarDeclNode         TypeNode, IdNode, int
//       FuncDeclNode        TypeNode, IdNode, FormalsListNode, FuncBodyNode
//       FormalDeclNode      TypeNode, IdNode
//       StructDeclNode      IdNode, DeclListNode
//
//     StmtListNode          linked list of StmtNode
//     ExpListNode           linked list of ExpNode
//     FormalsListNode       linked list of FormalDeclNode
//     FuncBodyNode          DeclListNode, StmtListNode
//
//     TypeNode:
//       BooleanNode         --- none ---
//       IntegerNode         --- none ---
//       VoidNode            --- none ---
//       StructNode          IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignExpNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       TrueNode            --- none ---
//       FalseNode           --- none ---
//       IdNode              --- none ---
//       IntLitNode          --- none ---
//       StrLitNode          --- none ---
//       StructAccessExpNode ExpNode, IdNode
//       AssignExpNode       ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         EqualsNode
//         NotEqNode
//         LessNode
//         LessEqNode
//         GreaterNode
//         GreaterEqNode
//         AndNode
//         OrNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, 
// or internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        BooleanNode,  IntegerNode,  VoidNode,    IdNode,  
//        TrueNode,     FalseNode,    IntLitNode,  StrLitNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, StmtListNode, ExpListNode, FormalsListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,         FuncDeclNode,  FormalDeclNode,
//        StructDeclNode,  FuncBodyNode,        StructNode,    AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode,     IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,        WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  StructAccessExpNode, AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,      NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,           TimesNode,     DivideNode,
//        EqualsNode,      NotEqNode,           LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,       AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************F

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

    static protected String exitFuncLabel = null;
}

// **********************************************************************
//   ProgramNode, DeclListNode, StmtListNode, ExpListNode, 
//   FormalsListNode, FuncBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /****
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     ****/
    public void nameAnalysis() {
        SymTab symTab = new SymTab();
        myDeclList.nameAnalysis(symTab);
        if (noMain) {
            ErrMsg.fatal(0, 0, "No main function");
        }
    }

    /***
     * typeCheck
     ***/
    public void typeCheck() {
        myDeclList.typeCheck();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myDeclList.codeGen();
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 child
    private DeclListNode myDeclList;

    public static boolean noMain = true; 
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     ****/
    public void nameAnalysis(SymTab symTab) {
        nameAnalysis(symTab, symTab);
    }
    
    /****
     * nameAnalysis
     * Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the 
     * decls in the list.
     ****/    
    public void nameAnalysis(SymTab symTab, SymTab globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }

    /***
     * typeCheck
     ***/
    public void typeCheck() {
        for (DeclNode node : myDecls) {
            node.typeCheck();
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        for (DeclNode node : myDecls) {
            node.codeGen();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of children (DeclNodes)
    private List<DeclNode> myDecls;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     ****/
    public void nameAnalysis(SymTab symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }

     /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        for(StmtNode node : myStmts) {
            node.typeCheck(retType);
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        for (StmtNode node : myStmts) {
            Codegen.p.print("# STATEMENT\n");
            node.codeGen();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        } 
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public int size() {
        return myExps.size();
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     ****/
    public void nameAnalysis(SymTab symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : myExps) {
                Type actualType = node.typeCheck();     // actual type of arg
                
                if (!actualType.isErrorType()) {        // if this is not an error
                    Type formalType = typeList.get(k);  // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(),
                                     "Actual type and formal type do not match");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        for (int c = myExps.size() - 1; c >= 0; c--) {
            myExps.get(c).codeGen();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) {         // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}
class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     ****/
    public List<Type> nameAnalysis(SymTab symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }  

    /****
     * Return the number of formals in this list.
     ****/
    public int length() {
        return myFormals.size();
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FuncBodyNode extends ASTnode {
    public FuncBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     ****/
    public void nameAnalysis(SymTab symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        myStmtList.typeCheck(retType);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myStmtList.codeGen();
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}


// **********************************************************************
// *****  DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /****
     * Note: a formal decl needs to return a sym
     ****/
    abstract public Sym nameAnalysis(SymTab symTab);

    // default version of typeCheck for non-function decls
    public void typeCheck() { }

    // default version of codeGen for non-function/non-var decls
    public void codeGen() { }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /****
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     ****/
    public Sym nameAnalysis(SymTab symTab) {
        return nameAnalysis(symTab, symTab);
    }
    
    public Sym nameAnalysis(SymTab symTab, SymTab globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Sym sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        else if (myType instanceof StructNode) {
            structId = ((StructNode)myType).idNode();
			try {
				sym = globalTab.lookupGlobal(structId.name());
            
				// if the name for the struct type is not found, 
				// or is not a struct type
				if (sym == null || !(sym instanceof StructDefSym)) {
					ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
								"Name of struct type invalid");
					badDecl = true;
				}
				else {
					structId.link(sym);
				}
			} catch (SymTabEmptyException ex) {
				System.err.println("Unexpected SymTabEmptyException " +
								    " in VarDeclNode.nameAnalysis");
				System.exit(-1);
			} 
        }
        
		try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Identifier multiply-declared");
				badDecl = true;            
			}
		} catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in VarDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        if (!badDecl) {  // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
                }
                else {
                    sym = new Sym(myType.type());
                    if (!globalTab.isGlobalScope()) {
                        int offset = globalTab.getOffset();
                        sym.setOffset(offset);
                        globalTab.setOffset(offset - 4); // vars are integer or logical
                    } else {
                            sym.setOffset(1);
                    }
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicateException ex) {
                System.err.println("Unexpected SymDuplicateException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected SymTabEmptyException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    } 

    /***
     * codeGen
     ***/
    public void codeGen() {
        if(mySize != NON_STRUCT) {
            throw new IllegalStateException("cannot generate code for structures");
        }

        // code will be generated only for global variables
        //
        // .data
        // .align 2
        // _v: .space 4

        Codegen.p.print("\t.data\n");
        Codegen.p.print("\t.align 2\n");
        Codegen.p.print("_" + myId.name() + ":" + "\t.space 4\n");
    }
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(".");
    }

    // 3 children
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NON_STRUCT if this is not a struct type

    public static int NON_STRUCT = -1;
}

class FuncDeclNode extends DeclNode {
    public FuncDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FuncBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     ****/
    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        FuncSym sym = null;
        try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(),
							"Identifier multiply-declared");
			}
        
			else { // add function name to local symbol table

                if (name.equals("main")) {
                    ProgramNode.noMain = false; 
                }

				try {
					sym = new FuncSym(myType.type(), myFormalsList.length());
					symTab.addDecl(name, sym);
					myId.link(sym);
				} catch (SymDuplicateException ex) {
					System.err.println("Unexpected SymDuplicateException " +
									" in FuncDeclNode.nameAnalysis");
					System.exit(-1);
				} catch (SymTabEmptyException ex) {
					System.err.println("Unexpected SymTabEmptyException " +
									" in FuncDeclNode.nameAnalysis");
					System.exit(-1);
				}
			}
		} catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in FuncDeclNode.nameAnalysis");
            System.exit(-1);
        } 

        symTab.setGlobalScope(false);
        symTab.setOffset(4);  // offset of first param         
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
            sym.setParamsSize(symTab.getOffset() - 4);
        }

        symTab.setOffset(-8);  // offset of first local
        int temp = symTab.getOffset();

        myBody.nameAnalysis(symTab); // process the function body

         if (sym != null) {
            sym.setLocalsSize(-1*(symTab.getOffset() - temp));
        }
        symTab.setGlobalScope(true);

        try {
            symTab.removeScope();  // exit scope
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in FuncDeclNode.nameAnalysis");
            System.exit(-1);
        }
        
        return null;
    }

    /***
     * typeCheck
     ***/
    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        String labelFunc = myId.name();
        Codegen.p.print(".text\n");
        if (myId.isMain()) {
            Codegen.p.print(".globl main\n");
        }
        else {
            labelFunc = "_" + labelFunc;
        }
        // function entry instructions
        Codegen.genLabel(labelFunc, "FUNCTION ENTRY: " + labelFunc);
        Codegen.genPush(Codegen.RA);
        Codegen.genPush(Codegen.FP);
        Codegen.generate("addu", Codegen.FP, Codegen.SP, 8);

        FuncSym funcSym = (FuncSym)myId.sym();
        if (funcSym.getLocalsSize() > 0) {
            Codegen.p.print("# LOCALS\n");
            Codegen.generate("subu", Codegen.SP, Codegen.SP, funcSym.getLocalsSize());
        }
        exitFuncLabel = labelFunc + "_Exit";

        // instructions for function body
        myBody.codeGen();

        // function exit instructions
        Codegen.genLabel(exitFuncLabel, "FUNCTION EXIT: " + labelFunc);
        Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, 0);
        Codegen.generate("move", Codegen.T0, Codegen.FP);
        Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -4);
        Codegen.generate("move", Codegen.SP, Codegen.T0);

        if (myId.isMain()) {
            Codegen.generate("li", Codegen.V0, 10);
            Codegen.generate("syscall");
        }
        else {
            Codegen.generate("jr", Codegen.RA);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("[");
        myFormalsList.unparse(p, 0);
        p.println("] [");
        myBody.unparse(p, indent+4);
        p.println("]\n");
    }

    // 4 children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FuncBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     ****/
    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        try { 
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Identifier multiply-declared");
				badDecl = true;
			}
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in FormalDeclNode.nameAnalysis");
            System.exit(-1);
        } 
        
        if (!badDecl) {  // insert into symbol table
            try {
                int offset = symTab.getOffset();
                sym = new Sym(myType.type());
                sym.setOffset(offset);
                symTab.setOffset(offset + 4); // only integer and boolean formals
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicateException ex) {
                System.err.println("Unexpected SymDuplicateException " +
                                   " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected SymTabEmptyException " +
                                   " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // 2 children
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     ****/
    public Sym nameAnalysis(SymTab symTab) {
        String name = myId.name();
        boolean badDecl = false;
        try {
			if (symTab.lookupLocal(name) != null) {
				ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
							"Identifier multiply-declared");
				badDecl = true;            
			}
		} catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in StructDeclNode.nameAnalysis");
            System.exit(-1);
        } 

        SymTab structSymTab = new SymTab();
        
        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            try {   // add entry to symbol table
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (SymDuplicateException ex) {
                System.err.println("Unexpected SymDuplicateException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (SymTabEmptyException ex) {
                System.err.println("Unexpected SymTabEmptyException " +
                                   " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println(" [");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("]\n");
    }

    // 2 children
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// ****  TypeNode and its subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class BooleanNode extends TypeNode {
    public BooleanNode() {
    }

    /****
     * type
     ****/
    public Type type() {
        return new BooleanType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("boolean");
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    /****
     * type
     ****/
    public Type type() {
        return new IntegerType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    /****
     * type
     ****/
    public Type type() {
        return new VoidType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }

    /****
     * type
     ****/
    public Type type() {
        return new StructType(myId);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
	
	// 1 child
    private IdNode myId;
}

// **********************************************************************
// ****  StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTab symTab); 
    abstract public void typeCheck(Type retType);
    abstract public void codeGen();
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myAssign.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        myAssign.typeCheck();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myAssign.codeGen();
        Codegen.genPop(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntegerType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator with non-integer operand");
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        if (!(myExp instanceof IdNode)) {
            throw new IllegalStateException("expression in postInc invalid");
        }
        IdNode id = (IdNode)myExp;
        id.loadValue(Codegen.T0);
        Codegen.generate("add", Codegen.T0, Codegen.T0, 1);
        id.genAddr(Codegen.T1);
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntegerType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator with non-integer operand");
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        if (!(myExp instanceof IdNode)) {
            throw new IllegalStateException("expression in postInc invalid");
        }
        IdNode id = (IdNode)myExp;
        id.loadValue(Codegen.T0);
        Codegen.generate("add", Codegen.T0, Codegen.T0, -1);
        id.genAddr(Codegen.T1);
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    // 1 child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

     /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBooleanType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-boolean expression in if condition");        
        }
        
        myStmtList.typeCheck(retType);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        String exitIfLabel = Codegen.nextLabel();
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, exitIfLabel);
        myStmtList.codeGen();
        Codegen.genLabel(exitIfLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");  
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBooleanType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-boolean expression in if condition");        
        }
        
        myThenStmtList.typeCheck(retType);
        myElseStmtList.typeCheck(retType);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        String elseLabel = Codegen.nextLabel();
        String exitIfElseLabel = Codegen.nextLabel();
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, elseLabel);
        myThenStmtList.codeGen();
        Codegen.generate("b", exitIfElseLabel);
        Codegen.genLabel(elseLabel);
        myElseStmtList.codeGen();
        Codegen.genLabel(exitIfElseLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}"); 
    }

    // 5 children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IfStmtNode.nameAnalysis");
            System.exit(-1);        
        }
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBooleanType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-boolean expression in while condition");        
        }
        
        myStmtList.typeCheck(retType);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        String beginWhileLabel = Codegen.nextLabel();
        String exitWhileLabel = Codegen.nextLabel();
        Codegen.genLabel(beginWhileLabel);
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, exitWhileLabel);
        myStmtList.codeGen();
        Codegen.generate("b", beginWhileLabel);
        Codegen.genLabel(exitWhileLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    } 

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFuncType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read function name");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read struct variable");
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        if (!(myExp instanceof IdNode)) {
            throw new IllegalStateException("expression in read invalid");
        }
        Codegen.generate("li", Codegen.V0, 5);
        Codegen.generate("syscall");

        IdNode id = (IdNode)myExp;
        if (id.sym().getType().isBooleanType()) {
            // need to generate 0 or 1 for the value
            Codegen.generate("sgt", Codegen.V0, Codegen.V0, 0);
        }
        id.genAddr(Codegen.T0);
        Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("input -> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    // 1 child (actually can only be an IdNode or a StructAccessExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFuncType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write function name");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write struct variable");
        }
        
        if (type.isVoidType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write void");
        }
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        Type t = myExp.typeCheck();

        myExp.codeGen();
        Codegen.genPop(Codegen.A0);
        
        if (t.isIntegerType() || t.isBooleanType()) {
            Codegen.generate("li", Codegen.V0, 1);
        }
        else if (t.isStringType()) {
            Codegen.generate("li", Codegen.V0, 4);
        }

        Codegen.generate("syscall");
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("disp <- (");
        myExp.unparse(p, 0);
        p.println(").");
    }

    // 2 children
    private ExpNode myExp;
    private Type myType;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myCall.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        myCall.typeCheck();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        boolean hasRet = myCall.codeGenRet();
        if (hasRet) {
            Codegen.genPop(Codegen.T0);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(".");
    }

    // 1 child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     ****/
    public void nameAnalysis(SymTab symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    /***
     * typeCheck
     ***/
    public void typeCheck(Type retType) {
        if (myExp != null) {  // return value given
            Type type = myExp.typeCheck();
            
            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Return value in void function");                
            }
            
            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)){
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Bad return value type");
            }
        }
        
        else {  // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(0, 0, "Missing return value");                
            }
        }

    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        if (myExp != null) {
            myExp.codeGen();
            Codegen.genPop(Codegen.V0);
        }
        Codegen.generateWithComment("b", "RETURN", exitFuncLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(".");
    }

    // 1 child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ****  ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /****
     * Default version for nodes with no names
     ****/
    public void nameAnalysis(SymTab symTab) { }

    abstract public Type typeCheck();
    abstract public int lineNum();
    abstract public int charNum();

    abstract public void codeGen();
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /***
     * Return the line number for this literal.
     ***/
    public int lineNum() {
        return myLineNum;
    }
    
    /***
     * Return the char number for this literal.
     ***/
    public int charNum() {
        return myCharNum;
    }
    
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        return new BooleanType();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        Codegen.generate("li", Codegen.T0, 1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("TRUE");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /***
     * Return the line number for this literal.
     ***/
    public int lineNum() {
        return myLineNum;
    }
    
    /***
     * Return the char number for this literal.
     ***/
    public int charNum() {
        return myCharNum;
    }

    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        return new BooleanType();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        Codegen.generate("li", Codegen.T0, 0);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("FALSE");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /****
     * Link the given symbol to this ID.
     ****/
    public void link(Sym sym) {
        mySym = sym;
    }
    
    /****
     * Return the name of this ID.
     ****/
    public String name() {
        return myStrVal;
    }
    
    /****
     * Return the symbol associated with this ID.
     ****/
    public Sym sym() {
        return mySym;
    }
    
    /****
     * Return the line number for this ID.
     ****/
    public int lineNum() {
        return myLineNum;
    }
    
    /****
     * Return the char number for this ID.
     ****/
    public int charNum() {
        return myCharNum;
    }

    /***
     * Return the total number of bytes for all local variables.
     * HINT: This method may be useful during code generation.
     ***/
    public int localsSize() {
        if(!(mySym instanceof FuncSym)) {
            throw new IllegalStateException("cannot call local size on a non-function");
        }
        return ((FuncSym)mySym).getLocalsSize();
    }    

    /***
     * Return the total number of bytes for all parameters.
     * HINT: This method may be useful during code generation.
     ***/
    public int paramsSize() {
        if(!(mySym instanceof FuncSym)) {
            throw new IllegalStateException("cannot call local size on a non-function");
        }
        return ((FuncSym)mySym).getParamsSize();
    }   

    /***
     * Is this function main?
     * HINT: This may be useful during code generation.
     ***/
    public boolean isMain() {
        return (myStrVal.equals("main"));
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     ****/
    public void nameAnalysis(SymTab symTab) {
		try {
            Sym sym = symTab.lookupGlobal(myStrVal);
            if (sym == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Identifier undeclared");
            } else {
                link(sym);
            }
        } catch (SymTabEmptyException ex) {
            System.err.println("Unexpected SymTabEmptyException " +
                               " in IdNode.nameAnalysis");
            System.exit(-1);
        } 
    }

    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        if (mySym != null) {
            return mySym.getType();
        } 
        else {
            System.err.println("ID with null sym field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        // local and global have code generated for them
        loadValue(Codegen.T0);
        Codegen.genPush(Codegen.T0);
    }

    public void genAddr(String regDst) {
        if (sym().isGlobal()) {
            String label = "_" +  name();
            Codegen.generate("la", regDst, label);
        }
        else {
            Codegen.generateIndexed("la", regDst, Codegen.FP, sym().getOffset());
        }
    }

    public void loadValue(String regDst) {
        if (mySym.isGlobal()) {
            String label = "_" + name();
            Codegen.generate("lw", regDst, label);
        }
        else {
            Codegen.generateIndexed("lw", regDst, Codegen.FP, mySym.getOffset());
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("{" + mySym + "}");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    /***
     * Return the line number for this literal.
     ***/
    public int lineNum() {
        return myLineNum;
    }
    
    /***
     * Return the char number for this literal.
     ***/
    public int charNum() {
        return myCharNum;
    }
        
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        return new IntegerType();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        Codegen.generate("li", Codegen.T0, myIntVal);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    private static HashMap<String, String> valueLabels = new HashMap<>();

    /***
     * Return the line number for this literal.
     ***/
    public int lineNum() {
        return myLineNum;
    }
    
    /***
     * Return the char number for this literal.
     ***/
    public int charNum() {
        return myCharNum;
    }
    
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        return new StringType();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        if (!valueLabels.containsKey(myStrVal)) {
            String label = Codegen.nextLabel();
            Codegen.p.print(".data\n");
            Codegen.p.print(label + ":" +  "\t.asciiz " + myStrVal + "\n");
            Codegen.p.print(".text\n");

            valueLabels.put(myStrVal, label);
        }

        Codegen.generate("la", Codegen.T0, valueLabels.get(myStrVal));
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class StructAccessExpNode extends ExpNode {
    public StructAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;	
        myId = id;
    }

    /****
     * Return the symbol associated with this colon-access node.
     ****/
    public Sym sym() {
        return mySym;
    }    
    
    /****
     * Return the line number for this colon-access node. 
     * The line number is the one corresponding to the RHS of the colon-access.
     ****/
    public int lineNum() {
        return myId.lineNum();
    }
    
    /****
     * Return the char number for this colon-access node.
     * The char number is the one corresponding to the RHS of the colon-access.
     ****/
    public int charNum() {
        return myId.charNum();
    }
    
    /****
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the colon-access
     * - process the RHS of the colon-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a colon-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     ****/
    public void nameAnalysis(SymTab symTab) {
        badAccess = false;
        SymTab structSymTab = null; // to lookup RHS of colon-access
        Sym sym = null;
        
        myLoc.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode)myLoc;
            sym = id.sym();
            
            // check ID has been declared to be of a struct type
            
            if (sym == null) { // ID was undeclared
                badAccess = true;
            }
            else if (sym instanceof StructSym) { 
                // get symbol table for struct type
                Sym tempSym = ((StructSym)sym).getStructType().sym();
                structSymTab = ((StructDefSym)tempSym).getSymTab();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Colon-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLoc is really a colon-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof StructAccessExpNode) {
            StructAccessExpNode loc = (StructAccessExpNode)myLoc;
            
            if (loc.badAccess) {  // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this colon-access
            }
            else { //  no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), 
                                 "Colon-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym)sym).getSymTab();
                    }
                    else {
                        System.err.println("Unexpected Sym type in StructAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of colon-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of colon-access in the struct's symbol table
        if (!badAccess) {
			try {
				sym = structSymTab.lookupGlobal(myId.name()); // lookup
				if (sym == null) { // not found - RHS is not a valid field name
					ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
								"Name of struct field invalid");
					badAccess = true;
				}
            
				else {
					myId.link(sym);  // link the symbol
					// if RHS is itself as struct type, link the symbol for its struct 
					// type to this colon-access node (to allow chained colon-access)
					if (sym instanceof StructSym) {
						mySym = ((StructSym)sym).getStructType().sym();
					}
				}
			} catch (SymTabEmptyException ex) {
				System.err.println("Unexpected SymTabEmptyException " +
								" in StructAccessExpNode.nameAnalysis");
				System.exit(-1);
			} 
        }
    }

    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        return myId.typeCheck();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        // Not needed, push 0 to continue calculating expression push 0 on stack
        Codegen.generate("li", Codegen.V0, 0);
        Codegen.genPush(Codegen.V0);
    }

    // **** unparse ****
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    // 4 children
    private ExpNode myLoc;	
    private IdNode myId;
    private Sym mySym;          // link to Sym for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    /***
     * Return the line number for this assignment node. 
     * The line number is the one corresponding to the left operand.
     ***/
    public int lineNum() {
        return myLhs.lineNum();
    }
    
    /***
     * Return the char number for this assignment node.
     * The char number is the one corresponding to the left operand.
     ***/
    public int charNum() {
        return myLhs.charNum();
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ****/
    public void nameAnalysis(SymTab symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type typeLhs = myLhs.typeCheck();
        Type typeExp = myExp.typeCheck();
        Type retType = typeLhs;
        
        if (typeLhs.isFuncType() && typeExp.isFuncType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Assignment to function name");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Assignment to struct name");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Assignment to struct variable");
            retType = new ErrorType();
        }        
        
        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }
        
        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        if (!(myLhs instanceof IdNode)) {
            throw new IllegalStateException("left side of assignment must be id");
        }
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        IdNode id = (IdNode)myLhs;
        id.genAddr(Codegen.T1);
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");   
    }

    // 2 children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /***
     * Return the line number for this call node. 
     * The line number is the one corresponding to the function name.
     ***/
    public int lineNum() {
        return myId.lineNum();
    }
    
    /***
     * Return the char number for this call node.
     * The char number is the one corresponding to the function name.
     ***/
    public int charNum() {
        return myId.charNum();
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ****/
    public void nameAnalysis(SymTab symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    } 

     /***
     * typeCheck
     ***/
    public Type typeCheck() {
        if (!myId.typeCheck().isFuncType()) {  
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Attempt to call non-function");
            return new ErrorType();
        }
        
        FuncSym fctnSym = (FuncSym)(myId.sym());
        
        if (fctnSym == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }
        
        if (myExpList.size() != fctnSym.getNumParams()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Wrong # of args in function call");
            return fctnSym.getReturnType();
        }
        
        myExpList.typeCheck(fctnSym.getParamTypes());
        return fctnSym.getReturnType();
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        codeGenRet();
    }

    public boolean codeGenRet() {
        myExpList.codeGen();
        Codegen.generate("jal", "_" + myId.name());
        if (myExpList.size() > 0) {
            // remove parameters from stack
            Codegen.generate("addu", Codegen.SP, myExpList.size() * 4);
        }
        FuncSym fctnSym = (FuncSym)(myId.sym());
        boolean hasReturn = !fctnSym.getReturnType().isVoidType();
        if (hasReturn) {
            Codegen.genPush(Codegen.V0);
        }
        return hasReturn;
    }

    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");   
    }

    // 2 children
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /***
     * Return the line number for this unary expression node. 
     * The line number is the one corresponding to the  operand.
     ***/
    public int lineNum() {
        return myExp.lineNum();
    }
    
    /***
     * Return the char number for this unary expression node.
     * The char number is the one corresponding to the  operand.
     ***/
    public int charNum() {
        return myExp.charNum();
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp.nameAnalysis(symTab);
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    /***
     * Return the line number for this binary expression node. 
     * The line number is the one corresponding to the left operand.
     ***/
    public int lineNum() {
        return myExp1.lineNum();
    }
    
    /***
     * Return the char number for this binary expression node.
     * The char number is the one corresponding to the left operand.
     ***/
    public int charNum() {
        return myExp1.charNum();
    }

    /****
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     ****/
    public void nameAnalysis(SymTab symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myExp1.codeGen();
        myExp2.codeGen();
        Codegen.genPop(Codegen.T1);
        Codegen.genPop(Codegen.T0);
    }

    // 2 children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// ****  Subclasses of UnaryExpNode
// **********************************************************************

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new BooleanType();
        
        if (!type.isErrorType() && !type.isBooleanType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Logical operator with non-boolean operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myExp.codeGen();
        Codegen.generate("xori", Codegen.T0, Codegen.T0, 1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(^");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new IntegerType();
        
        if (!type.isErrorType() && !type.isIntegerType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Arithmetic operator with non-integer operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myExp.codeGen();
        Codegen.generate("neg", Codegen.T0, Codegen.T0);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// ****  Subclasses of BinaryExpNode
// **********************************************************************

abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new IntegerType();
        
        if (!type1.isErrorType() && !type1.isIntegerType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Arithmetic operator with non-integer operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntegerType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Arithmetic operator with non-integer operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class BooleanExpNode extends BinaryExpNode {
    public BooleanExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BooleanType();
        
        if (!type1.isErrorType() && !type1.isBooleanType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Logical operator with non-boolean operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isBooleanType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Logical operator with non-boolean operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BooleanType();
        
        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to void function calls");
            retType = new ErrorType();
        }
        
        if (type1.isFuncType() && type2.isFuncType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to function names");
            retType = new ErrorType();
        }
        
        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct names");
            retType = new ErrorType();
        }
        
        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct variables");
            retType = new ErrorType();
        }        
        
        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Type mismatch");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        myExp1.codeGen();
        myExp2.codeGen();
        Codegen.genPop(Codegen.T1);
        Codegen.genPop(Codegen.T0);
    }

}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /***
     * typeCheck
     ***/
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BooleanType();
        
        if (!type1.isErrorType() && !type1.isIntegerType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Relational operator with non-integer operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntegerType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Relational operator with non-integer operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

class AndNode extends BooleanExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        String labelDone = Codegen.nextLabel();
        myExp1.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, labelDone);
        myExp2.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.genLabel(labelDone);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" & ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BooleanExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        String labelDone = Codegen.nextLabel();
        myExp1.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.TRUE, labelDone);
        myExp2.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.genLabel(labelDone);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" | ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class PlusNode extends ArithmeticExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generateWithComment("add", "ADD", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generateWithComment("sub", "SUBTRACT", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generateWithComment("mul", "MULTIPLY", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generateWithComment("div", "DIVIDE", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generate("seq", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqNode extends EqualityExpNode {
    public NotEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generate("sne", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" ^= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generate("sgt", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generate("sge", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generate("slt", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /***
     * codeGen
     ***/
    public void codeGen() {
        super.codeGen();
        Codegen.generate("sle", Codegen.T0, Codegen.T0, Codegen.T1);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}