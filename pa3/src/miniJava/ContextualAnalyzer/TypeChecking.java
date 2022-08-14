package miniJava.ContextualAnalyzer;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
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
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class TypeChecking implements Visitor<TypeDenoter, TypeDenoter>{
	AST ast;
	ErrorReporter reporter;
	
	public TypeChecking(AST ast, ErrorReporter reporter) {
		this.ast = ast;
		this.reporter = reporter;
	}

	@Override
	public TypeDenoter visitPackage(Package prog, TypeDenoter arg) {
		// TODO Auto-generated method stub
		for (ClassDecl classDecl : prog.classDeclList) {
			classDecl.visit(this, arg);
		}
		return null;
	}

	@Override
	public TypeDenoter visitClassDecl(ClassDecl cd, TypeDenoter arg) {
		// TODO Auto-generated method stub
		for (FieldDecl fieldDecl : cd.fieldDeclList) {
            fieldDecl.visit(this, arg);
        }
        for (MethodDecl methodDecl : cd.methodDeclList) {
            methodDecl.visit(this, arg);
        }
		return null;
	}

	@Override
	public TypeDenoter visitFieldDecl(FieldDecl fd, TypeDenoter arg) {
		// TODO Auto-generated method stub
		fd.type = fd.type.visit(this, arg);
		return null;
	}

	@Override
	public TypeDenoter visitMethodDecl(MethodDecl md, TypeDenoter arg) {
		// TODO Auto-generated method stub
		md.type = md.type.visit(this, arg);
		for (ParameterDecl parameterDecl : md.parameterDeclList) {
			parameterDecl.visit(this, arg);
		}

		for (Statement statement : md.statementList) {
			statement.decl = md;
			statement.visit(this, arg);
		}
		
		
		return md.type;
	}

	@Override
	public TypeDenoter visitParameterDecl(ParameterDecl pd, TypeDenoter arg) {
		// TODO Auto-generated method stub
		pd.type = pd.type.visit(this, arg);
		return pd.type;
	}

	@Override
	public TypeDenoter visitVarDecl(VarDecl decl, TypeDenoter arg) {
		// TODO Auto-generated method stub
		decl.type = decl.type.visit(this, arg);
		return decl.type;
	}

	@Override
	public TypeDenoter visitBaseType(BaseType type, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public TypeDenoter visitClassType(ClassType type, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public TypeDenoter visitArrayType(ArrayType type, TypeDenoter arg) {
		// TODO Auto-generated method stub
		type.eltType = type.eltType.visit(this, arg);
		return type.eltType;
	}

	@Override
	public TypeDenoter visitBlockStmt(BlockStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		for (Statement statement : stmt.sl) {
            statement.visit(this, arg);
        }
		return null;
	}

	@Override
	public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		TypeDenoter v = stmt.varDecl.visit(this, arg);
		TypeDenoter e = stmt.initExp.visit(this, arg);
		if (!v.equals(e)) {
			reporter.reportError("VardeclStmt type wrong.");
		}
		return null;
	}

	@Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		TypeDenoter v = stmt.ref.visit(this, arg);
		TypeDenoter e = stmt.val.visit(this, arg);
		if (!v.equals(e)) {
			reporter.reportError("AssignStmt v and e type incompatible.");
		}
		return null;
	}

	@Override
	public TypeDenoter visitIxAssignStmt(IxAssignStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitCallStmt(CallStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		if (!(stmt.methodRef.decl instanceof MethodDecl)) {
			reporter.reportError("What is called is not a method.");
		} else {
			MethodDecl md = (MethodDecl) stmt.methodRef.decl;
			if (stmt.argList.size() != md.parameterDeclList.size()) {
				reporter.reportError("Number of parameters doesn't match number of arguments.");
			} else {
				for (int i = 0; i<stmt.argList.size();i++) {
					TypeDenoter v = stmt.argList.get(i).visit(this, arg);
					TypeDenoter e = md.parameterDeclList.get(i).visit(this, arg);
					if(!v.equals(e)) {
						reporter.reportError("Type of parameters doesn't match type of arguments.");
					}
				}
			}
		}
		return null;
	}

	@Override
	public TypeDenoter visitReturnStmt(ReturnStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitIfStmt(IfStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitWhileStmt(WhileStmt stmt, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitUnaryExpr(UnaryExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitBinaryExpr(BinaryExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitRefExpr(RefExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitIxExpr(IxExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitCallExpr(CallExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitLiteralExpr(LiteralExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitThisRef(ThisRef ref, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitIdRef(IdRef ref, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitQRef(QualRef ref, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitIdentifier(Identifier id, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitOperator(Operator op, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitIntLiteral(IntLiteral num, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, TypeDenoter arg) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
