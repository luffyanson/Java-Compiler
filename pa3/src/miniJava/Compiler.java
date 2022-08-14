package miniJava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ContextualAnalyzer.Identification;
import miniJava.ContextualAnalyzer.TypeChecking;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;

public class Compiler {
	public static void main(String[] args) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(args[0]);
		} catch (FileNotFoundException e) {
			System.out.println("Input file " + args[0] + " not found");
			System.exit(3);
		}
		
		ErrorReporter errorReporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, errorReporter);
		Parser parser = new Parser(scanner, errorReporter);
		
		
		System.out.println("Syntactic analysis ... ");
		Package ast = (Package) parser.parse();
		System.out.print("Sytactic analysis complete: ");
		
		
		System.out.println("Starting contextual analysis...");
		new Identification(ast, errorReporter);
		new TypeChecking(ast,errorReporter);
		
		
		
		if (errorReporter.hasErrors()) {
			System.out.println("Invalid miniJava program");
			System.exit(4);
		}
		else {
			System.out.println("Valid miniJava program");
//			new ASTDisplay().showTree(ast);
		}
		
		System.exit(0);
		
	}
}
