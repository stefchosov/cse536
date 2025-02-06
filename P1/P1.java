import java.io.*;
import java.util.*;

public class P1 {
    public static void main(String[] args) {
        // Calling methods for testing SymClass and SymTab
        boolean testSymClass = testSymClass();
        boolean testSymTab = testSymTabClass();
        
        // Summarizing the results of the tests
        if (!testSymClass && !testSymTab) {
            System.out.println("Neither SymClass nor SymTab are valid");
        } else if (testSymClass && !testSymTab) {
            System.out.println("Sym is valid, however SymTab is not");
        } else if (!testSymClass && testSymTab) {
            System.out.println("SymTab is valid, however Sym is not");
        } else {
            System.out.println("Sym and SymTab have both passed full validations");
        }
    }

    /**
     * Helper method to test Sym Class with different types
     * @return a boolean indicating if validations passed for creating Syms and its methods.
     */
    private static boolean testSymClass() {
        boolean validity = true;
        List<String> types = Arrays.asList("int", "char", "boolean", "double", "float");
        for (String type : types) {
            Sym test = new Sym(type);
            if (!test.toString().equals(type)) {
                System.out.println(test.toString() + " should actually equal " + type);
                validity = false;
            } else if (!test.getType().equals(type)) {
                System.out.println(test.getType() + " should actually equal " + type);
                validity = false;
            }
        }
        return validity;
    }

    // Method to organize and test all of the SymTab functionality
    // Does not take a parameter, returns a boolean indicating if the validations passed (true) or failed (false)
    public static boolean testSymTabClass() {
        SymTab test = new SymTab();
        boolean emptySymTabTests = symTabEmptyTest(test, "Decl") & 
                                   symTabEmptyTest(test, "local") & 
                                   symTabEmptyTest(test, "global") & 
                                   symTabEmptyTest(test, "removeScope");
        return emptySymTabTests && testAddAndRemoveScope(test) && testLookGlobal(test) && testPrint() && testAddDecl();
    }

    /** 
     * Helper method to test SymTab empty exception 
     * Takes a SymTab reference as a parameter and a method for which will be tested if it can correctly throw the exception 
     * Private method as it is only used for testing the SymTab class
     * Will test each case this empty exception is thrown and returns true if it fails and throws the correct exception
     * false otherwise
     * Returns true if the SymTab remains empty, false otherwise 
     */
    private static boolean symTabEmptyTest(SymTab test, String method) {
        // Test SymTab empty exception
        Sym testSym1 = new Sym("char");
        // Testing SymTab empty for each method type as a case
        switch (method) {
            // Test exception for Decl
            case "Decl":
                try {
                    test.addDecl("Wesley", testSym1);
                } catch (SymTabEmptyException | SymDuplicateException e) {
                    if (!e.getClass().getSimpleName().equals("SymTabEmptyException")) {
                        System.out.println("Incorrectly throwing wrong exception, should be throwing SymTabEmptyException");
                        return false;
                    } else {
                        return true;
                    }
                }
                break;
            // Test exception for localLookup
            case "local":
                try {
                    test.lookupLocal("Wesley");
                } catch (SymTabEmptyException e) {
                    return true;
                }
                break;
            // Test exception for globalLookup
            case "global":
                try {
                    test.lookupGlobal("Wesley");
                } catch (SymTabEmptyException e) {
                    return true;
                }
                break;
            // Test exception for removeScope
            case "removeScope":
                try {
                    test.removeScope();
                } catch (SymTabEmptyException e) {
                    return true;
                }
                break;
        }
        System.out.println("No exception thrown for: " + method);
        return false;
    }

    /**
     * Helper method to test adding and removing scope
     * Returns true if successfully added and removed scopes, false otherwise
     * Tests if exceptions are correctly thrown
     * Takes a SymTab reference as a parameter
     * Private method as it is only used for testing the SymTab class
     */
    private static boolean testAddAndRemoveScope(SymTab test) {
        // Test add scope
        test.addScope();
        if (test.list.size() != 1) {
            System.out.println("Add scope failed to add a HashMap");
            return false;
        }

        // Test add another scope
        test.addScope();
        if (test.list.size() != 2) {
            System.out.println("Add scope failed to add a second HashMap");
            return false;
        }

        // Test remove scope
        try {
            test.removeScope();
            if (test.list.size() != 1) {
                System.out.println("Remove scope failed to remove the top HashMap");
                return false;
            }
        } catch (SymTabEmptyException e) {
            System.out.println("Exception thrown during removeScope - SymTab should not be empty at this point: " + e.getMessage());
            return false;
        }

        // Test remove last scope
        try {
            test.removeScope();
            if (!test.list.isEmpty()) {
                System.out.println("Remove scope failed to remove the last HashMap");
                return false;
            }
        } catch (SymTabEmptyException e) {
            System.out.println("Exception incorrectly thrown during removeScope - believes it is empty when it is not: " + e.getMessage());
            return false;
        }

        // Test remove scope on empty SymTab
        try {
            test.removeScope();
            System.out.println("Remove scope should have thrown SymTabEmptyException");
            return false;
        } catch (SymTabEmptyException e) {
            return true;
        }
    }

    /**
     * Helper method to test global lookup in SymTab
     * Takes a SymTab reference as a parameter
     * Sets up the SymTab with multiple scopes and declarations
     * Tests if lookupGlobal correctly finds symbols across scopes
     * Returns true if all tests pass, false otherwise
     * Private method as it is only used for testing the SymTab class
     */
    private static boolean testLookGlobal(SymTab test) {
        try {
            // Add scopes
            test.addScope();
            // Add declarations
            Sym testSym1 = new Sym("int");
            test.addDecl("testSym1", testSym1);
            test.addScope();
            Sym testSym2 = new Sym("character");
            test.addDecl("testSym2", testSym2);
            // Test lookupGlobal
            if (test.lookupGlobal("testSym1") != testSym1) {
                System.out.println("Global lookup failed for testSym1");
                return false;
            }
            if (test.lookupGlobal("testSym2") != testSym2) {
                System.out.println("Global lookup failed for testSym2");
                return false;
            }
            if (test.lookupGlobal("testSym3") != null) {
                System.out.println("Global lookup should return null for testSym3");
                return false;
            }

            // Remove scopes and test lookupGlobal again
            test.removeScope();
            if (test.lookupGlobal("testSym1") == null) {
                System.out.println("Global lookup should return testSym1 after removing first Scope");
                return false;
            }
            if (test.lookupGlobal("testSym2") != null) {
                System.out.println("Global lookup should return null for testSym2 after removing first Scope");
                return false;
            }
        } catch (SymTabEmptyException | SymDuplicateException e) {
            System.out.println("Exception thrown: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Helper method to test the print function
     * Private method, returns boolean if output matches what is the expectation
     * Private method as it is only used for testing the SymTab class
     */
    private static boolean testPrint() {
        try {
            // Set up the SymTab for PrintTesting
            SymTab printTest = new SymTab();
            printTest.addScope();
            Sym testSym1 = new Sym("int");
            printTest.addDecl("testSym1", testSym1);
            printTest.addScope();
            Sym testSym2 = new Sym("character");
            printTest.addDecl("testSym2", testSym2);

            // Redirect standard output to be able to test against expected output
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(baos));
            printTest.print();
            System.setOut(originalOut);
            String printedOutput = baos.toString();
            String expectedOutput = "\n*** SymTab ***\n" +
                                    "{testSym2=character}\n\n" +
                                    "{testSym1=int}\n\n" +
                                    "\n*** DONE ***\n";

            // Compare the printed output with the expected output
            if (!printedOutput.equals(expectedOutput)) {
                System.out.println("Print method output is incorrect for a populated SymTab");
                System.out.println("Expected:\n" + expectedOutput);
                System.out.println("Got:\n" + printedOutput);
                return false;
            }

            // Now test empty SymTab
            baos.reset();
            SymTab empty = new SymTab();
            System.setOut(new PrintStream(baos));
            empty.print();
            System.setOut(originalOut);
            String printedEmpty = baos.toString();
            expectedOutput = "\n*** SymTab ***\n" +
                             "\n*** DONE ***\n";
            if (!printedEmpty.equals(expectedOutput)) {
                System.out.println("Print method output is incorrect for an empty SymTab");
                System.out.println("Expected:\n" + expectedOutput);
                System.out.println("Got:\n" + printedEmpty);
                return false;
            }
        } catch (SymTabEmptyException | SymDuplicateException e) {
            System.out.println("Exception thrown when not needed for testing print(): " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Helper method to test adding declarations in SymTab.
     * Takes a SymTab reference as a parameter.
     * Sets up the SymTab with a single scope and adds declarations.
     * Tests if addDecl correctly adds Sym to the current scope.
     * Returns true if all tests pass, false otherwise.
     */
    private static boolean testAddDecl() {
        SymTab test = new SymTab();
        try {
            // Add a scope
            test.addScope();

            // Add declarations
            Sym testSym1 = new Sym("int");
            Sym testSym2 = new Sym("character");
            test.addDecl("testSym1", testSym1);
            test.addDecl("testSym2", testSym2);

            // Test if declarations are added correctly
            if (test.lookupLocal("testSym1") != testSym1) {
                System.out.println("AddDecl failed for testSym1");
                return false;
            }
            if (test.lookupLocal("testSym2") != testSym2) {
                System.out.println("AddDecl failed for testSym2");
                return false;
            }

            // Test adding a duplicate
            try {
                test.addDecl("testSym1", new Sym("float"));
                System.out.println("AddDecl should have thrown SymDuplicateException for testSym1");
                return false;
            } catch (SymDuplicateException e) {
                // Expected
            }

            // Test adding a declaration when no scope exists
            test.removeScope();
            try {
                test.addDecl("testSym3", new Sym("double"));
                System.out.println("AddDecl should have thrown SymTabEmptyException when no scope exists");
                return false;
            } catch (SymTabEmptyException e) {
                // Expected exception
            }

        } catch (SymTabEmptyException | SymDuplicateException e) {
            System.out.println("Exception thrown: " + e.getMessage());
            return false;
        }
        return true;
    }
}