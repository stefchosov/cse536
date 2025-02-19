###
# This Makefile can be used to make a scanner for the bach language
# (Yylex.class) and to make a program that tests the scanner (P2.class).
#
# The default makes both the scanner and the test program.
#
# make clean removes all generated files.
#
# Note: P2.java will not compile unless Yylex.class exists.
#
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = ./deps:.

P2.class: P2.java Yylex.class sym.class
	$(JC) $(FLAGS) P2.java

Yylex.class: bach.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) bach.jlex.java

bach.jlex.java: bach.jlex sym.class
	java -cp $(CP) JLex.Main bach.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

	
###
# testing - add more here to run your tester and compare its results
# to expected results
###
test:
	java -cp $(CP) P2 2>error_and_warning.out
	diff allTokens.in allTokens.out
	diff testComments.out testComments_solution
	diff testString_solution testString.out
	diff testInt.out testInt_solution
	diff testInvalidCharacter.out testInvalidCharacter_solution
	diff error_and_warning.out error_and_warning_solution

#testcomments:
#	java -cp $(CP) P2
#	diff testComments.out testComments_solution

#teststring:
#	java -cp $(CP) P2 2>StringTestErr.txt
#	diff testString_solution testString.out
#	diff StringTestErr.txt errSolution


###
# clean up
###

clean:
	rm -f *~ *.class bach.jlex.java *.out

cleantest:
	rm -f *.out
