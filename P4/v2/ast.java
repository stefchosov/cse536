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
//       StructAccessNode    ExpNode, IdNode
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
//        ProgramNode,     VarDeclNode,      FuncDeclNode,  FormalDeclNode,
//        StructDeclNode,  FuncBodyNode,     StructNode,    AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode,  IfStmtNode,    IfElseStmtNode,
//        WhileStmtNode,   ReadStmtNode,     WriteStmtNode, CallStmtNode,
//        ReturnStmtNode,  StructAccessNode, AssignExpNode, CallExpNode,
//        UnaryExpNode,    UnaryMinusNode,   NotNode,       BinaryExpNode,   
//        PlusNode,        MinusNode,        TimesNode,     DivideNode,
//        EqualsNode,      NotEqNode,        LessNode,      LessEqNode,
//        GreaterNode,     GreaterEqNode,    AndNode,       OrNode
//
// **********************************************************************

// **********************************************************************
//   ASTnode class (base class for all other kinds of nodes)
// **********************************************************************F

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    public void runNameAnalysis(SymTab table) {}

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
//   ProgramNode, DeclListNode, StmtListNode, ExpListNode, 
//   FormalsListNode, FuncBodyNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public void runNameAnalysis(SymTab table) {
        myDeclList.runNameAnalysis(table);
    }

    // 1 child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    HashMap<String, Sym> getSyms() {
        HashMap<String, Sym> t = new HashMap<String, Sym>();
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).addSym(t);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.getSyms");
            System.exit(-1);
        }
        return t;
    }

    public void runNameAnalysis(SymTab table) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).runNameAnalysis(table);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.runNameAnalysis");
            System.exit(-1);
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

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        } 
    }

    public void runNameAnalysis(SymTab table) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().runNameAnalysis(table);
        } 
    }

    // list of children (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
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

    public void runNameAnalysis(SymTab table) {
        Iterator<ExpNode> it = myExps.iterator();
        while (it.hasNext()) {
            it.next().runNameAnalysis(table);
        } 
    }

    // list of children (ExpNodes)
    private List<ExpNode> myExps;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
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

    public void runNameAnalysis(SymTab table) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        while (it.hasNext()) {
            it.next().runNameAnalysis(table);
        }
    }

    public String getTypeName() {
        String res = "";
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) {
            res = it.next().getTypeName();
        }
        while (it.hasNext()) {
            res += ",";
            res += it.next().getTypeName();
        }
        return res;
    }

    // list of children (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FuncBodyNode extends ASTnode {
    public FuncBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void runNameAnalysis(SymTab table) {
        myDeclList.runNameAnalysis(table);
        myStmtList.runNameAnalysis(table);
    }

    // 2 children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

// **********************************************************************
// *****  DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    public void addSym(HashMap<String, Sym> t) {}
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
        myId.disablePrintType();
    }

    public void addSym(HashMap<String, Sym> t) {
        t.put(myId.getName(), myId.getSym());
    }

    public void runNameAnalysis(SymTab table) {
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Non-function declared void.");
        }
        else {
            try {
                if (table.lookupLocal(myId.getName()) != null) {
                    ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Identifier multiply-declared.");
                }
                else {
                    if (mySize == NON_STRUCT) {
                        Sym s = new Sym(myType.getName());
                        table.addDecl(myId.getName(), s);
                        myId.attachSym(s);
                    }
                    else {
                        StructNode myStructType = (StructNode)myType;
                        Sym structSym = table.lookupGlobal(myStructType.getName());
                        if (structSym == null || !(structSym instanceof StructDefSym)) {
                            ErrMsg.fatal(myStructType.getLineNum(), myStructType.getCharNum(), "Name of struct type invalid.");
                        }
                        else {
                            Sym s = table.lookupGlobal(myId.getName());
                            if (s != null) {
                                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Identifier multiply-declared.");
                            }
                            else {
                                table.addDecl(myId.getName(), structSym);
                                myId.attachSym(structSym);
                            }
                        }
                    }
                }
            }
            catch (SymTabEmptyException | SymDuplicateException e) {}
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
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
        myId.disablePrintType();
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("[");
        myFormalsList.unparse(p, 0);
        p.println("] [");
        myBody.unparse(p, indent+4);
        p.println("]\n");
    }

    public void runNameAnalysis(SymTab table) {
        try {
            if (table.lookupLocal(myId.getName()) != null) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Identifier multiply-declared.");
            }
            else {
                String type = myFormalsList.getTypeName() + "->" + myType.getName();
                Sym s = new FunctionDefSym(type);
                table.addDecl(myId.getName(), s);
                myId.attachSym(s);
        
                table.addScope();
                myFormalsList.runNameAnalysis(table);
                myBody.runNameAnalysis(table);
                //table.print();
                table.removeScope();
            }
        }
        catch (SymTabEmptyException | SymDuplicateException e) {}
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
        myId.disablePrintType();
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    public void runNameAnalysis(SymTab table) {
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Non-function declared void.");
        }
        else {
            try {
                if (table.lookupLocal(myId.getName()) != null) {
                    ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Identifier multiply-declared.");
                }
                else {
                    Sym s = new Sym(myType.getName());
                    table.addDecl(myId.getName(), s);
                    myId.attachSym(s);
                }
            }
            catch (SymTabEmptyException | SymDuplicateException e) {}
        }
    }

    String getTypeName() { return myType.getName(); }

    // 2 children
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void runNameAnalysis(SymTab table) {
        try {
            if (table.lookupLocal(myId.getName()) != null) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Identifier multiply-declared.");
            }
            else {
                table.addScope();
                myDeclList.runNameAnalysis(table);
                table.removeScope();
                Sym s = new StructDefSym(myId.getName(), myDeclList.getSyms());
                table.addDecl(myId.getName(), s);
                myId.attachSym(s);
                myId.disablePrintType();
            }
        }
        catch (SymTabEmptyException | SymDuplicateException e) {}
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
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
    public String getName() { return ""; }
}

class BooleanNode extends TypeNode {
    public BooleanNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("boolean");
    }

    public String getName() {
        return "boolean";
    }
}

class IntegerNode extends TypeNode {
    public IntegerNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("integer");
    }

    public String getName() {
        return "integer";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    public String getName() {
        return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }
	
    public String getName() { return myId.getName(); }
    public int getLineNum() { return myId.getLineNum(); }
    public int getCharNum() { return myId.getCharNum(); }

    // 1 child
    private IdNode myId;
}

// **********************************************************************
// ****  StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignExpNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(".");
    }

    public void runNameAnalysis(SymTab table) {
        myAssign.runNameAnalysis(table);
    }

    // 1 child
    private AssignExpNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++.");
    }

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
    }

    // 1 child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--.");
    }

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
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

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
        try {
            table.addScope();
            myDeclList.runNameAnalysis(table);
            myStmtList.runNameAnalysis(table);
            table.removeScope();
        }
        catch (SymTabEmptyException e) {}
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

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);

        try {
            table.addScope();
            myThenDeclList.runNameAnalysis(table);
            myThenStmtList.runNameAnalysis(table);
            table.removeScope();

            table.addScope();
            myElseStmtList.runNameAnalysis(table);
            myElseDeclList.runNameAnalysis(table);
            table.removeScope();
        }
        catch (SymTabEmptyException e) {}
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

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
        try {
            table.addScope();
            myDeclList.runNameAnalysis(table);
            myStmtList.runNameAnalysis(table);
            table.removeScope();
        }
        catch (SymTabEmptyException e) {}
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

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("input -> ");
        myExp.unparse(p, 0);
        p.println(".");
    }

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
    }

    // 1 child (actually can only be an IdNode or a StructAccessNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("disp <- (");
        myExp.unparse(p, 0);
        p.println(").");
    }

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
    }

    // 1 child
    private ExpNode myExp;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void runNameAnalysis(SymTab table) {
        myCall.runNameAnalysis(table);
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

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
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
    Sym getSym() { return null; }
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
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
        mySym = null;
        doPrintType = true;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (doPrintType) {
            printType(p);
        }
    }

    public void printType(PrintWriter p) {
        if (mySym != null) {
            p.print(mySym.toString());
        }
    }

    public int getLineNum() { return myLineNum; }
    public int getCharNum() { return myCharNum; }
    public String getName() { return myStrVal; }
    public void attachSym(Sym s) { mySym = s; }
    public Sym getSym() { return mySym; }
    public void disablePrintType() { doPrintType = false; }

    public void runNameAnalysis(SymTab table) {
        try {
            Sym s = table.lookupGlobal(myStrVal);
            if (s == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Identifier undeclared.");
            }
            else {
                mySym = s;
            }
        }
        catch (SymTabEmptyException e) {}
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
    private boolean doPrintType;
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
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

    public Sym getSym() { return myId.getSym(); }

    public void runNameAnalysis(SymTab table) {
        myLoc.runNameAnalysis(table);
        Sym s = myLoc.getSym();
        if (s == null) {
            if (myLoc instanceof IdNode) {
                IdNode i = (IdNode)myLoc;
                ErrMsg.fatal(i.getLineNum(), i.getCharNum(), "Identifier undeclared.");
            }
        }
        else if (!(s instanceof StructDefSym)) {
            ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Colon-access of non-struct type.");
        }
        else { 
            Sym f = ((StructDefSym)s).getField(myId.getName());
            if (f == null) {
                ErrMsg.fatal(myId.getLineNum(), myId.getCharNum(), "Name of struct field invalid.");
            }
            else {
                myId.attachSym(f);
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print("):");
        myId.unparse(p, 0);
    }

    // 2 children
    private ExpNode myLoc;
    private IdNode myId;
}

class AssignExpNode extends ExpNode {
    public AssignExpNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)  p.print(")");   
    }

    public void runNameAnalysis(SymTab table) {
        myLhs.runNameAnalysis(table);
        myExp.runNameAnalysis(table);
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

    public void runNameAnalysis(SymTab table) {
        myId.runNameAnalysis(table);
        myExpList.runNameAnalysis(table);
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

    public void runNameAnalysis(SymTab table) {
        myExp.runNameAnalysis(table);
    }

    // 1 child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void runNameAnalysis(SymTab table) {
        myExp1.runNameAnalysis(table);
        myExp2.runNameAnalysis(table);
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

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// ****  Subclasses of BinaryExpNode
// **********************************************************************

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" & ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" | ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqNode extends BinaryExpNode {
    public NotEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" ^= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
