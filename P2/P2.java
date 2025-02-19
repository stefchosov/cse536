import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the bach scanner.
 * This version is set up to test all tokens, but more code is needed to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
        CharNum.num = 1;
        testAllTokens();
    
        // ADD CALLS TO OTHER TEST METHODS HERE
        // test comments
        CharNum.num = 1;
        testComments();

        CharNum.num = 1;
        testValidStrings();

        CharNum.num = 1;
        testIntAndLargeInt();

        CharNum.num = 1;
        testInvalidCharacter();
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.txt
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens() throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("allTokens.in");
            outFile = new PrintWriter(new FileWriter("allTokens.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File allTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("allTokens.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOLEAN:
                outFile.println("boolean"); 
                break;
            case sym.INTEGER:
                outFile.println("integer");
                break;
            case sym.VOID:
                outFile.println("void");
                break;
            case sym.STRUCT:
                outFile.println("struct"); 
                break;
            case sym.IF:
                outFile.println("if");
                break;
            case sym.ELSE:
                outFile.println("else");
                break;
            case sym.WHILE:
                outFile.println("while");
                break;								
            case sym.INPUT:
                outFile.println("input"); 
                break;
            case sym.DISPLAY:
                outFile.println("disp");
                break;				
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.TRUE:
                outFile.println("TRUE"); 
                break;
            case sym.FALSE:
                outFile.println("FALSE"); 
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLIT:  
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRINGLIT: 
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;    
            case sym.LCURLY:
                outFile.println("{");
                break;
            case sym.RCURLY:
                outFile.println("}");
                break;
            case sym.LPAREN:
                outFile.println("(");
                break;
            case sym.RPAREN:
                outFile.println(")");
                break;
            case sym.LSQUARE:
                outFile.println("[");
                break;
            case sym.RSQUARE:
                outFile.println("]");
                break;
            case sym.COLON:
                outFile.println(":");
                break;
            case sym.COMMA:
                outFile.println(",");
                break;
            case sym.DOT:
                outFile.println(".");
                break;
            case sym.READOP:
                outFile.println("->");
                break;	
            case sym.WRITEOP:
                outFile.println("<-");
                break;			
            case sym.PLUSPLUS:
                outFile.println("++");
                break;
            case sym.MINUSMINUS:
                outFile.println("--");
                break;	
            case sym.PLUS:
                outFile.println("+");
                break;
            case sym.MINUS:
                outFile.println("-");
                break;
            case sym.TIMES:
                outFile.println("*");
                break;
            case sym.DIVIDE:
                outFile.println("/");
                break;
            case sym.NOT:
                outFile.println("^");
                break;
            case sym.AND:
                outFile.println("&");
                break;
            case sym.OR:
                outFile.println("|");
                break;
            case sym.EQUALS:
                outFile.println("==");
                break;
            case sym.NOTEQ:
                outFile.println("^=");
                break;
            case sym.LESS:
                outFile.println("<");
                break;
            case sym.GREATER:
                outFile.println(">");
                break;
            case sym.LESSEQ:
                outFile.println("<=");
                break;
            case sym.GREATEREQ:
                outFile.println(">=");
                break;
            case sym.ASSIGN:
                outFile.println("=");
                break;
            default:
                outFile.println("!!! UNKNOWN TOKEN !!!");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }

    /**
     * Private method to test comments by reading in a file: testComments.in
     * and printing out the comments to testComments.out
     * tests many cases of comments and we can verify correctness of the scanner
     * by comparing with the solution file
     * @throws IOException when file cannot be opened
     */
    private static void testComments() throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("testComments.in");
            outFile = new PrintWriter(new FileWriter("testComments.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File testComments.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("testComments.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.STRINGLIT: 
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;
            default:
                System.out.println("Token symbol: " + token.sym + " Value:" + token.value);
                System.out.println("!!! UNKNOWN TOKEN !!!");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }

    /**
     * Private method to test valid strings by reading in a file: testString.in
     * and printing out the valid strings to testString.out
     * It will error for invalid strings
     * We can verify correctness of the scanner by comparing with the solution file
     * @throws IOException when file cannot be opened
     */
    private static void testValidStrings() throws IOException {
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("testString.in");
            outFile = new PrintWriter(new FileWriter("testString.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File testString.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("testString.out cannot be opened.");
            System.exit(-1);
        }
        
        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym){
            case sym.STRINGLIT: 
                outFile.println(((StrLitTokenVal)token.value).strVal);
                break;
            default:
                System.out.println("Token symbol: " + token.sym + " Value:" + token.value);
                System.out.println("!!! UNKNOWN TOKEN !!!");
            }
            token = scanner.next_token();
        }
        outFile.close();
    }

    /**
     * Private method to test int and large int by reading in a file: testInt.in
     * and printing out the valid ints to: testInt.out
     * and sending a warning for large ints
     * It will print the max Large Integer value if the input is too large 
     * @throws IOException when file cannot be opened
     */
    private static void testIntAndLargeInt() throws IOException {
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("testInt.in");
            outFile = new PrintWriter(new FileWriter("testInt.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File testInt.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("testInt.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym){
            case sym.INTLIT: 
                outFile.println(((IntLitTokenVal)token.value).intVal);
                break;
            default:
                System.out.println("Token symbol: " + token.sym + " Value:" + token.value);
                System.out.println("!!! UNKNOWN TOKEN !!!");
            }
            token = scanner.next_token();
        }
        outFile.close();
    }

    /**
     * Private method to test valid and invalid characters by reading in a file
     * and printing out the valid tokens and erroring on invalid characters  
     * @throws IOException when file cannot be opened
     */
    private static void testInvalidCharacter() throws IOException {
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("testInvalidCharacter.in");
            outFile = new PrintWriter(new FileWriter("testInvalidCharacter.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File testInvalidCharacter.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("testInvalidCharacter.out cannot be opened.");
            System.exit(-1);
        }

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym){
            case sym.RETURN:
                outFile.println("return");
                break;
            case sym.TRUE:
                outFile.println("TRUE"); 
                break;
            case sym.FALSE:
                outFile.println("FALSE"); 
                break;
            case sym.ID:
                outFile.println(((IdTokenVal)token.value).idVal);
                break;
            default:
                System.out.println("Token symbol: " + token.sym + " Value:" + token.value);
                System.out.println("!!! UNKNOWN TOKEN !!!");
            }
            token = scanner.next_token();
        }
        outFile.close();
    }
}
