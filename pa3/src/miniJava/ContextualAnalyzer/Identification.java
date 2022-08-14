package miniJava.ContextualAnalyzer;

import java.util.HashMap;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.AbstractSyntaxTrees.Package;

public class Identification implements Visitor<Object, Object>{
	public IdentificationTable table;
	private ErrorReporter reporter;
	private boolean hasMain;
	private boolean isStaticMember;
	private ClassDecl currentClass;
	
	public Identification(Package ast, ErrorReporter reporter) {
		this.reporter = reporter;
		table = new IdentificationTable(reporter);
		ast.visit(this, null);
		hasMain = false;
		isStaticMember = false;
	}
	
	@Override
	public Object visitPackage(miniJava.AbstractSyntaxTrees.Package prog, Object arg) {
		// TODO Auto-generated method stub
		
		table.openScope();
		
		// lauching the environment
		FieldDeclList fdl = new FieldDeclList();
		Token t1 = new Token(TokenKind.ID, "_PrintStream", null);
		Identifier id1 = new Identifier(t1);
		ClassType _PrintStream = new ClassType(id1, null);
		FieldDecl fd1 = new FieldDecl(false, true, _PrintStream, "out", null);
		fdl.add(fd1);
		MethodDeclList mdl = new MethodDeclList();
		ParameterDeclList pdl = new ParameterDeclList();
		BaseType bt1 = new BaseType(TypeKind.INT, null);
		ParameterDecl pd1 = new ParameterDecl(bt1, "n", null);
		pdl.add(pd1);
		BaseType bt2 = new BaseType(TypeKind.VOID, null);
		FieldDecl fd2 = new FieldDecl(false, false, bt2, "println", null);
		MethodDecl md1 = new MethodDecl(fd2, pdl, new StatementList(), null);
		mdl.add(md1);
		
		ClassDecl cd1 = new ClassDecl("System", fdl, new MethodDeclList(), null);
		table.enter(cd1);
		
		ClassDecl cd2 = new ClassDecl("_PrintStream", new FieldDeclList(), mdl, null);
		table.enter(cd2);
		
		ClassDecl cd3 = new ClassDecl("String", new FieldDeclList(), new MethodDeclList(), null);
		table.enter(cd3);
		
		
		for (ClassDecl cd: prog.classDeclList) {
			if (table.retrieve(cd.name) != null) {
				reporter.reportError("Class " + cd.name + " is already declared.");
			}
			table.enter(cd);
		}
		
		for (ClassDecl cd: prog.classDeclList) {
			currentClass = cd;
			cd.visit(this, null);
		}
		
		if (!hasMain) {
			reporter.reportError("Don't has main method.");
		}
		
		table.closeScope();
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, Object arg) {
		// TODO Auto-generated method stub
		ClassDecl currentClass = cd;
		table.openScope();
		
		for(FieldDecl fd: cd.fieldDeclList) {
			if(table.retrieve(fd.name) != null) {
				reporter.reportError("Field " + fd.name + " already declared.");
			}
			table.enter(fd);
		}
		
		for(MethodDecl md: cd.methodDeclList) {
			if(table.retrieve(md.name) != null) {
				reporter.reportError("Field " + md.name + " already declared.");
			}
			table.enter(md);
		}
		
		for(FieldDecl fd: cd.fieldDeclList) {
			isStaticMember = fd.isStatic;
			fd.visit(this, null);
		}
		
		for(MethodDecl md: cd.methodDeclList) {
			// the declaration of a main method public static void main(String [] args).
			if(md.name.equals("main")) {
				if(!md.isPrivate&&md.isStatic&&md.type.typeKind == TypeKind.VOID&& md.parameterDeclList.size() == 1 
						&&md.parameterDeclList.get(0).type instanceof ArrayType) {
					if (((ClassType)((ArrayType)md.parameterDeclList.get(0).type).eltType).className.spelling.equals("String")) {
						if(!hasMain) {
							hasMain = true;
						} else {
							reporter.reportError("More than one main methods declared.");
						}
					}
						
				}
			}
			isStaticMember = md.isStatic;
			md.visit(this, null);
		}
		table.closeScope();
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		// TODO Auto-generated method stub
		fd.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) {
		// TODO Auto-generated method stub
		md.type.visit(this, null);
		table.openScope();
		for(ParameterDecl pd: md.parameterDeclList) {
			pd.visit(this, null);
		}
		table.openScope();
		for(Statement st: md.statementList) {
			st.visit(this, null);
		}
		table.closeScope();
		table.closeScope();
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) {
		// TODO Auto-generated method stub
		pd.type.visit(this, null);
		
		if(duplicatedInSameScope(pd)) {
			reporter.reportError("Duplicate declarations of " + pd.name + " in same scope.");
		}
		table.enter(pd);
		return null;
	}

	private boolean duplicatedInSameScope(Declaration pd) {
		// TODO Auto-generated method stub
		return table.table.peek().containsKey(pd.name);
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) {
		// TODO Auto-generated method stub
		decl.type.visit(this, null);
		if(localVarDuplicated(decl)) {
			reporter.reportError("Local variable " + decl.name + " hide a name impropriately.");
		} else {
			table.enter(decl);
		}
		return null;
	}

	private boolean localVarDuplicated(VarDecl decl) {
		// TODO Auto-generated method stub
		Stack<HashMap<String, Declaration>> temp = (Stack<HashMap<String, Declaration>>) table.table.clone();
		while (temp.size() > 2) {
			if(temp.pop().containsKey(decl.name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) {
		// TODO Auto-generated method stub
		// find the class
		ClassDecl cd = (ClassDecl) table.retrieve(type.className.spelling);
		// if not, report
		if (cd != null) {
			type.className.decl = cd;
		} else {
			reporter.reportError("can't find class " + type.className.spelling + ".");
		}
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) {
		// TODO Auto-generated method stub
		type.eltType.visit(this, null);
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		table.openScope();
		
		for (Statement st: stmt.sl) {
			st.visit(this, null);
		}
		
		table.closeScope();
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.initExp.visit(this, null);
		stmt.varDecl.visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.ref.visit(this, null);
		stmt.val.visit(this, null);
		return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.ref.visit(this, null);
		stmt.ix.visit(this, null);
		stmt.exp.visit(this, null);
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.methodRef.visit(this, null);
		
		MethodDecl md = (MethodDecl) stmt.methodRef.decl;
		if (md.parameterDeclList.size() != stmt.argList.size()) {
			reporter.reportError("argument number not equal to parameter number.");
		}
		
		for (Expression e: stmt.argList) {
			e.visit(this, null);
		}
		
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		if (stmt.returnExpr != null) {
			stmt.returnExpr.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.cond.visit(this, null);
		stmt.thenStmt.visit(this, null);
		if (stmt.elseStmt != null) {
			stmt.elseStmt.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
		// TODO Auto-generated method stub
		stmt.cond.visit(this, null);
		table.openScope();
		stmt.body.visit(this, null);
		table.closeScope();
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.operator.visit(this, null);
		expr.expr.visit(this, null);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.operator.visit(this, null);
		expr.left.visit(this, null);
		expr.right.visit(this, null);
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.ref.visit(this, null);
		return null;
	}

	@Override
	public Object visitIxExpr(IxExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.ref.visit(this, null);
		expr.ixExpr.visit(this, null);
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.functionRef.visit(this, null);
		
		
		// ?
		MethodDecl md = (MethodDecl) expr.functionRef.decl;
		
		if(md.parameterDeclList.size()!= expr.argList.size()) {
			reporter.reportError("Number of arguments doesn't match number of parameters.");
		}
		
		for (Expression e: expr.argList) {
			e.visit(this, null);
		}
		
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.lit.visit(this, null);
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.classtype.visit(this, null);
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		// TODO Auto-generated method stub
		expr.eltType.visit(this, null);
		expr.sizeExpr.visit(this, null);
		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) {
		// TODO Auto-generated method stub
		if (isStaticMember) {
			reporter.reportError("Can't access static component via 'this'");
		} else {
			ref.decl = currentClass;
		}
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, Object arg) {
		// TODO Auto-generated method stub
		ref.id.visit(this, null);
		
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitOperator(Operator op, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}
}
