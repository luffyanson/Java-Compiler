package miniJava.ContextualAnalyzer;

import java.util.HashMap;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.FieldDecl;

public class IdentificationTable {
	public Stack<HashMap<String, Declaration>> table;
	
	public IdentificationTable(ErrorReporter reporter) {
		// TODO Auto-generated constructor stub
		table = new Stack<HashMap<String, Declaration>>(); 
	}

	public void openScope() {
		// TODO Auto-generated method stub
		HashMap<String, Declaration> newScope = new HashMap<String, Declaration>();
		table.push(newScope);
	}

	public void closeScope() {
		// TODO Auto-generated method stub
		table.pop();
	}

	public void enter(Declaration decl) {
		// TODO Auto-generated method stub
		HashMap<String, Declaration> currScope = table.peek();
		String name = decl.name;
		if (currScope.containsKey(name)) {
			// position ?
			System.out.println("*** line " + decl.posn.toString() + " Duplicate declarations at the same level *** ");
			System.exit(4);
		} else {
			currScope.put(name, decl);
		}
		return;
	}
	
	
	// search the stack from top down, use linear search
	public Declaration retrieve(String s) {
		Stack<HashMap<String, Declaration>> table2 = (Stack<HashMap<String, Declaration>>) table.clone();
		while (!table2.empty()) {
			HashMap<String, Declaration> top = table2.pop();
			if (top.containsKey(s)) {
				return top.get(s);
			}
		}
		return null;
	}

}
