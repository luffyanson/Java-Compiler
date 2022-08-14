package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.TokenKind;

import java.util.Stack;

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
import miniJava.AbstractSyntaxTrees.ClassDeclList;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.ExprList;
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
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.Terminal;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;

public class Parser {
	private Scanner scanner;
	private ErrorReporter reporter;
	private Token currentToken;

	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.scanner = scanner;
		this.reporter = reporter;
	}


	/**
	 *  parse input, catch possible parse error
	 */
	public AST parse() throws SyntaxError{
		currentToken = scanner.scan();
		AST ast = null;
		try {
			ast = parseProgram();
		}
		catch (SyntaxError e) { }
		return ast;
	}
	
	private boolean startsDeclaration(TokenKind kind) throws SyntaxError{
		return kind == TokenKind.PUBLIC ||
				kind == TokenKind.PRIVATE ||
				 kind == TokenKind.STATIC ||
				  startsType(kind) ||
					 kind == TokenKind.VOID;
	}
	
	private boolean startsType(TokenKind kind) throws SyntaxError {
		return kind == TokenKind.INT ||
				   kind == TokenKind.BOOLEAN ||
					kind == TokenKind.ID;
	}

	
	public Package parseProgram() throws SyntaxError {
		ClassDeclList classList = new ClassDeclList();
		while (currentToken.kind != TokenKind.EOT) {
			FieldDeclList fieldList = new FieldDeclList();
            MethodDeclList methodList = new MethodDeclList();
            
    		accept(TokenKind.CLASS);
    		Identifier classId = parseId();
    		accept(TokenKind.LCURLY);
    		
//    		System.out.print(currentToken.kind);
    		while (startsDeclaration(currentToken.kind)) {
//    			System.out.println("------------starting declaration-----------");
    			// parse accessibility
    			boolean isPublic = true;
    			if (currentToken.kind == TokenKind.PRIVATE) {
    				acceptIt();
    				isPublic = false;
    			} else if (currentToken.kind == TokenKind.PUBLIC) {
    				acceptIt();
    			}
    			
    			// parse access
    			boolean isStatic = false;
    			if (currentToken.kind == TokenKind.STATIC) {
    				acceptIt();
    				isStatic = true;
    			}
    			
    			switch(currentToken.kind) {
    				// Method Declaration
    				case VOID:
    					TypeDenoter type = new BaseType(TypeKind.VOID, null);
    					acceptIt();
    					Identifier methodId = parseId();
    					accept(TokenKind.LPAREN);
    					ParameterDeclList paramList = new ParameterDeclList();
    					if (startsType(currentToken.kind)) {
    						paramList = parseParameterList();
    					}
    					accept(TokenKind.RPAREN);
    					accept(TokenKind.LCURLY);
    					
    					StatementList stateList = new StatementList();
    					while (startsStatement(currentToken.kind)) {
//    						System.out.println("IN VOID METHOD DECLARATION");
    						stateList.add(parseStatement());
//    						System.out.println("OUT VOID METHOD DECLARATION");
    					}
    					accept(TokenKind.RCURLY);
    					methodList.add(new MethodDecl(new FieldDecl(!isPublic, isStatic, type, methodId.spelling, null), paramList, stateList, null));
    					break;
    					
    				// both possible
    				default:
    					if (startsType(currentToken.kind)) {
    						TypeDenoter type2 = parseType();
    						Identifier id = parseId();
    						switch(currentToken.kind) {
    							// Field Declaration
    							case SEMICOLON:
    								acceptIt();
    								fieldList.add(new FieldDecl(!isPublic, isStatic, type2, id.spelling, null));
    								break;
    							
    							// Method Declaration
    							case LPAREN:
    								acceptIt();
    								ParameterDeclList paramList2 = new ParameterDeclList();
    								if (startsType(currentToken.kind)) {
    									paramList2 = parseParameterList();
    								}
    								accept(TokenKind.RPAREN);
    								accept(TokenKind.LCURLY);
    								
    								StatementList stateList2 = new StatementList();
    								while (startsStatement(currentToken.kind)) {
//    									System.out.println("IN METHOD DECLARATION");
    									stateList2.add(parseStatement());
//    									System.out.println("OUT METHOD DECLARATION");
    								}
    								accept(TokenKind.RCURLY);
    								methodList.add(new MethodDecl(new FieldDecl(!isPublic, isStatic, type2, id.spelling, null), paramList2, stateList2, null));
    								break;
    						}
    					}
    					else {
    						parseError("Parsing wrong in declarations, no types declared.");
    					}
    					break;
    					
    			}
//    			System.out.println("------------ending declaration-----------");
    		}
//    		System.out.println("out here end of program: " + currentToken.spelling);

    		accept(TokenKind.RCURLY);
    		classList.add(new ClassDecl(classId.spelling, fieldList, methodList, null));
            
		}
		

		accept(TokenKind.EOT);
		return new Package(classList,null);
	}
	
//	  public Statement(SourcePosition posn) {
//    public StatementList() {
//    public BlockStmt(StatementList sl, SourcePosition posn){
	private Statement parseStatement() throws SyntaxError {
		Statement stmt = null;
		switch (currentToken.kind) {
			// {statements*}
			case LCURLY:
				acceptIt();
				StatementList sl = new StatementList();
				while (startsStatement(currentToken.kind)) {
					sl.add(parseStatement());
				}
	//			System.out.println("in here: " + currentToken);
				accept(TokenKind.RCURLY);
				stmt = new BlockStmt(sl, null);
				break;
				
				
//				public VarDecl(TypeDenoter t, String name, SourcePosition posn) {
//			    public VarDeclStmt(VarDecl vd, Expression e, SourcePosition posn){
			// Type id = Expression;
			case INT: case BOOLEAN:
				String name = currentToken.spelling;
				TypeDenoter td = parseType();
				Identifier id = parseId();
				accept(TokenKind.EQUAL);
				Expression e = parseExpression();
				accept(TokenKind.SEMICOLON);
				
				VarDecl vd = new VarDecl(td, name, null);
				stmt = new VarDeclStmt(vd, e, null);
				break;
			
			case ID:			
//				System.out.println("in here0, " + currentToken.kind + " is***" + currentToken.spelling + "***");
				Identifier id2 = parseId();
//				System.out.println("in here1, " + currentToken.kind + " is***" + currentToken.spelling + "***");
				switch (currentToken.kind) {
					// starts with Type
					// Type id = Expression
					case ID:
						TypeDenoter td2 = new ClassType(id2, null);
						Identifier id3 = parseId();
						accept(TokenKind.EQUAL);
						Expression e2 = parseExpression();
						accept(TokenKind.SEMICOLON);
						VarDecl vd2 = new VarDecl(td2, id3.spelling, null);
						stmt = new VarDeclStmt(vd2, e2, null);
						break;
					
					// starts with Reference
					// Reference = Expression
					case EQUAL:
						Reference r = new IdRef(id2, null);
//						System.out.println("in here2, " + currentToken.kind + " is*" + currentToken.spelling + "*");
						acceptIt();
//						System.out.println("in here3, " + currentToken.kind + " is*" + currentToken.spelling + "*");
						Expression e3 = parseExpression();
//						System.out.println("inhere 4");
						accept(TokenKind.SEMICOLON);
						
						stmt = new AssignStmt(r, e3, null);
						break;
					
					// Reference (arglist?)
					case LPAREN:
						Reference r2 = new IdRef(id2, null);
						ExprList al = new ExprList();
						acceptIt();
						if (currentToken.kind == TokenKind.RPAREN) {
							acceptIt();
						} else {
							al = parseArgumentList();
							accept(TokenKind.RPAREN);
						}
						accept(TokenKind.SEMICOLON);
						stmt = new CallStmt(r2, al, null);
						break;
					
					// both
					case LBRACKET: 
						acceptIt();
						
						if (currentToken.kind == TokenKind.RBRACKET) {
							// in Type
							// Type id = Expression;
							// Type := id[]
							acceptIt();							
							ArrayType at = new ArrayType(new ClassType(id2, null), null);
							Identifier id4 = parseId();
							accept(TokenKind.EQUAL);
							Expression e4 =parseExpression();
							accept(TokenKind.SEMICOLON);
							VarDecl vd3 = new VarDecl(at, id4.spelling, null);
							stmt = new VarDeclStmt(vd3, e4, null);
						} else {
							// in Reference
							Reference r3 = new IdRef(id2, null);
							Expression e5 = parseExpression();
							accept(TokenKind.RBRACKET);
							accept(TokenKind.EQUAL);
							Expression e6 = parseExpression();
							accept(TokenKind.SEMICOLON);
							stmt = new IxAssignStmt(r3, e5, e6, null);
						}
						break;
					
					case DOT:
						// in Reference
						Reference r4 = new IdRef(id2,null);
						while (currentToken.kind == TokenKind.DOT) {
							acceptIt();
							Identifier id4 = parseId();
							r4 = new QualRef(r4, id4, null);
						}
						switch (currentToken.kind) {
							// Reference = Expression;
							case EQUAL:
								acceptIt();
								Expression e4 = parseExpression();
								accept(TokenKind.SEMICOLON);
								stmt = new AssignStmt(r4,e4,null);
								break;
							
							// Reference[Expression] = Expression;
							case LBRACKET: 
								acceptIt();
								Expression e5 = parseExpression();
								accept(TokenKind.RBRACKET);
								accept(TokenKind.EQUAL);
								Expression e6 = parseExpression();
								accept(TokenKind.SEMICOLON);
								stmt = new IxAssignStmt(r4, e5, e6, null);
								break;
							
							// Reference(arglist?);
							case LPAREN:
								acceptIt();
								ExprList el2 = new ExprList();
								if (currentToken.kind == TokenKind.RPAREN) {
									acceptIt();
								} else {
									el2 = parseArgumentList();
									accept(TokenKind.RPAREN);
								}
								accept(TokenKind.SEMICOLON);
								stmt = new CallStmt(r4, el2, null);
								break;
							
							default:
								parseError("parsing statement, in subcase ID, in subcase DOT, current token is: " + currentToken.spelling);
						}
						break;
					default:
						parseError("parsing statement, in subcase ID, current token is: " + currentToken.spelling);
						break;
				}
				break;
			
			
			// in Reference
			case THIS:
				Reference rInThis = new ThisRef(null);
				acceptIt();
				while (currentToken.kind == TokenKind.DOT) {
					acceptIt();
					Identifier idAfterThis = parseId();
					rInThis = new QualRef(rInThis, idAfterThis, null);
				}
				switch (currentToken.kind) {
				// Reference = Expression;
				case EQUAL:
					acceptIt();
//					System.out.println("before");
					Expression e5 =parseExpression();
//					System.out.println("after");
					accept(TokenKind.SEMICOLON);
					stmt = new AssignStmt(rInThis, e5, null);
					break;
				
				// Reference[Expression] = Expression;
				case LBRACKET:
					acceptIt();
					Expression e6 = parseExpression();
					accept(TokenKind.RBRACKET);
					accept(TokenKind.EQUAL);
					Expression e7 = parseExpression();
					accept(TokenKind.SEMICOLON);
					stmt = new IxAssignStmt(rInThis, e6, e7, null);
					break;
					
				// Reference(arglist?);
				case LPAREN:
					acceptIt();
					ExprList el = new ExprList();
					if (currentToken.kind == TokenKind.RPAREN) {
						acceptIt();
						accept(TokenKind.SEMICOLON);
					} else {
						el = parseArgumentList();
						accept(TokenKind.RPAREN);
						accept(TokenKind.SEMICOLON);
					}
					stmt = new CallStmt(rInThis, el, null);
					break;
				
				default:
					parseError("parsing statement, in subcase THIS, current token is: " + currentToken);
				}
				break;
				
			// in reference
			case RETURN:
				acceptIt();
				Expression e2 = null;
				if (currentToken.kind == TokenKind.SEMICOLON) {
					acceptIt();
				} else {
					e2 = parseExpression();
					accept(TokenKind.SEMICOLON);
				}
				stmt = new ReturnStmt(e2, null);
				break;
			
			case IF:
				acceptIt();
				accept(TokenKind.LPAREN);
				Expression ifE = parseExpression();
				accept(TokenKind.RPAREN);
				Statement ifS = parseStatement();
				if (currentToken.kind == TokenKind.ELSE) {
					acceptIt();
					Statement elseS = parseStatement();
					stmt = new IfStmt(ifE, ifS, elseS, null);
				} else {
					stmt = new IfStmt(ifE,ifS,null);
				}
				break;
				
			case WHILE:
				acceptIt();
				accept(TokenKind.LPAREN);
				Expression whileE = parseExpression();
				accept(TokenKind.RPAREN);
				Statement whileS = parseStatement();
				stmt = new WhileStmt(whileE, whileS, null);
				break;
			
			default:
				parseError("parsing statement, current token is:" + currentToken);
				break;
		}
		return stmt;
		
	}


	private ExprList parseArgumentList() throws SyntaxError {
		// TODO Auto-generated method stub
		ExprList el = new ExprList();
		Expression e = parseExpression();
		el.add(e);
		while (currentToken.kind == TokenKind.COMMA) {
			acceptIt();
			e = parseExpression();
			el.add(e);
		}
		return el;
	}

//	public BinaryExpr(Operator o, Expression e1, Expression e2, SourcePosition posn){
//	  public Operator (Token t) {
	private Expression parseExpression() throws SyntaxError {
		Expression e1 = parseExpressionA();
//		System.out.println(currentToken.spelling + " is of kind " + currentToken.kind);

		while (currentToken.kind == TokenKind.OR) {
			Operator o = new Operator(currentToken);
			acceptIt();
			Expression e2 = parseExpressionA();
			e1 = new BinaryExpr(o,e1,e2,null);
		}
		return e1;
	}
	
	private Expression parseExpressionA() throws SyntaxError {
//		System.out.println("into A");
		Expression e1 = parseExpressionB();
		while (currentToken.kind == TokenKind.AND) {
			Operator o = new Operator(currentToken);
			acceptIt();
			Expression e2 = parseExpressionB();
			e1 = new BinaryExpr(o,e1,e2,null);
		}
//		System.out.println("out A");
		return e1;
	}

	
	private Expression parseExpressionB() throws SyntaxError {
//		System.out.println("into B");
		Expression e1 = parseExpressionC();
		while (currentToken.kind == TokenKind.ISEQUAL || currentToken.kind == TokenKind.NOTEQUAL) {
			Operator o = new Operator(currentToken);
			acceptIt();
			Expression e2 = parseExpressionC();
			e1 = new BinaryExpr(o,e1,e2,null);
		}
//		System.out.println("out B");
		return e1;
	}


	private Expression parseExpressionC() throws SyntaxError {
//		System.out.println("into C");
		Expression e1 = parseExpressionD();
		while (currentToken.kind == TokenKind.LT || currentToken.kind == TokenKind.GT ||
				currentToken.kind == TokenKind.LTE || currentToken.kind == TokenKind.GTE) {
			Operator o = new Operator(currentToken);
			acceptIt();
			Expression e2 = parseExpressionD();
			e1 = new BinaryExpr(o,e1,e2,null);
		}
//		System.out.println("out C");
		return e1;
	}


	private Expression parseExpressionD() throws SyntaxError {
//		System.out.println("into D");
		Expression e1 = parseExpressionE();
		while (currentToken.kind == TokenKind.PLUS || currentToken.kind == TokenKind.MINUS) {
			Operator o = new Operator(currentToken);
			acceptIt();
			Expression e2 = parseExpressionE();
			e1 = new BinaryExpr(o,e1,e2,null);
		}
//		System.out.println("out D");
		return e1;
	}


	private Expression parseExpressionE() throws SyntaxError {
//		System.out.println("into E");
		Expression e1 = parseExpressionF();
		while (currentToken.kind == TokenKind.TIMES || currentToken.kind == TokenKind.DIVIDE) {
			Operator o = new Operator(currentToken);
			acceptIt();
			Expression e2 = parseExpressionF();
			e1 = new BinaryExpr(o,e1,e2,null);
		}
//		System.out.println("out E");
		return e1;
	}

//	   public UnaryExpr(Operator o, Expression e, SourcePosition posn){
	private Expression parseExpressionF() throws SyntaxError {
//		System.out.println("into F");
		Stack<Operator> s = new Stack<>();
		while (currentToken.kind == TokenKind.MINUS || currentToken.kind == TokenKind.EXCLA) {
			Operator o = new Operator(currentToken);
			s.add(o);
			acceptIt();
		}
		Expression e = parseExpressionG();
		while(!s.isEmpty()) {
			e = new UnaryExpr(s.pop(),e, null);
		}
//		System.out.println("out F");
		return e;
	}


	private Expression parseExpressionG() throws SyntaxError {
//		System.out.println("into G");
//		System.out.println(currentToken.spelling + " is of kind " + currentToken.kind);
		Expression e = null;
		switch (currentToken.kind) {
		case LPAREN:
			acceptIt();
			e = parseExpression();
			accept(TokenKind.RPAREN);
			break;
		
		
		case NUM:
//			  public IntLiteral(Token t) {
			IntLiteral il = new IntLiteral(currentToken);
			acceptIt();
			
//		    public LiteralExpr(Terminal t, SourcePosition posn){
			e = new LiteralExpr(il, null);
			break;
			
		case TRUE: case FALSE:
			BooleanLiteral bl = new BooleanLiteral(currentToken);
			acceptIt();
			e = new LiteralExpr(bl, null);
			break;
		
		case NEW:
			acceptIt();
			switch (currentToken.kind) {
				case ID:
//					  public Identifier (Token t) {
					Identifier id = new Identifier(currentToken);
					acceptIt();
					switch (currentToken.kind) {
					case LPAREN:
						acceptIt();
						accept(TokenKind.RPAREN);
						
//					    public NewObjectExpr(ClassType ct, SourcePosition posn){
//					    public ClassType(Identifier cn, SourcePosition posn){
						e =  new NewObjectExpr(new ClassType(id, null), null);
						break;
						
					case LBRACKET:
						acceptIt();
						e = parseExpression();
						accept(TokenKind.RBRACKET);
						
//					    public NewArrayExpr(TypeDenoter et, Expression e, SourcePosition posn){
//					    public TypeDenoter(TypeKind type, SourcePosition posn){
//						public class ClassType extends TypeDenoter
//						public ClassType(Identifier cn, SourcePosition posn){
						e = new NewArrayExpr(new ClassType(id,null), e, null);
						break;
						
					default:
						parseError("parsing expression, nothing after new.");
					
					}
					break;
					
				case INT:
					acceptIt();
					accept(TokenKind.LBRACKET);
					e = parseExpression();
					accept(TokenKind.RBRACKET);
					
//				    public BaseType(TypeKind t, SourcePosition posn){
					e =  new NewArrayExpr(new BaseType(TypeKind.INT, null), e, null);
					break;
				
				default:
					parseError("parsing expression, in subcase NEW, current token is: " + currentToken.spelling + "and its kind is: " + currentToken.kind);
			}
			break;
			
			
		// starts with reference
		case ID: case THIS:
			Reference r = parseReference();

			switch (currentToken.kind) {
			// reference[expression]
			case LBRACKET:
				acceptIt();
				e = parseExpression();
				
				accept(TokenKind.RBRACKET);
				
				//	public IxExpr(Reference r, Expression e, SourcePosition posn){
				e = new IxExpr(r, e, null);
				
				break;
			
			// reference(arglist?)
			case LPAREN:
				acceptIt();
				ExprList arglist = new ExprList();
				if (currentToken.kind == TokenKind.RPAREN) {
					acceptIt();
				} else {
					arglist = parseArgumentList();
					accept(TokenKind.RPAREN);
				}
				
//			    public CallExpr(Reference f, ExprList el, SourcePosition posn){
				e = new CallExpr(r,arglist, null);
				break;
					
			default:
//			    public RefExpr(Reference r, SourcePosition posn){
//				System.out.println(currentToken.spelling + " is of kind " + currentToken.kind);
				e = new RefExpr(r, null);
				break;
			}
			break;			
		
		default:
			parseError("parsing expressionG");
			
		}
		return e;
	}

//	public ThisRef(SourcePosition posn) {
//	public IdRef(Identifier id, SourcePosition posn){
//	public QualRef(Reference ref, Identifier id, SourcePosition posn){
//	 public Identifier (Token t) {
	private Reference parseReference() throws SyntaxError {
		if(currentToken.kind == TokenKind.ID || currentToken.kind == TokenKind.THIS) {
			Reference ref = null;
			if(currentToken.kind == TokenKind.ID) {
				ref = new IdRef(new Identifier(currentToken), null);
			} else {
				ref = new ThisRef(null);
			}
			acceptIt();
			
			// dealing with (.id)*
			while (currentToken.kind == TokenKind.DOT) {
				acceptIt();
				if (currentToken.kind == TokenKind.ID) {
					ref = new QualRef(ref, new Identifier(currentToken), null);
					acceptIt();
				} else {
					parseError("no identifier after reference and dot");
				}
			}
			return ref;
			
		} else {
			parseError("parsing reference");
			return null;
		}
	}


//	private void parseExpression() {
//		// TODO Auto-generated method stub
//		switch (currentToken.kind) {
//			// starts with reference
//			case ID: case THIS:
//				acceptIt();
//				// dealing with (.id)*
//				while (currentToken.kind == TokenKind.DOT) {
//					acceptIt();
//					accept(TokenKind.ID);
//				}
//				switch (currentToken.kind) {
//					// reference[expression]
//					case LBRACKET:
//						acceptIt();
//						parseExpression();
//						accept(TokenKind.RBRACKET);
//						break;
//					
//					// reference(arglist?)
//					case LPAREN:
//						acceptIt();
//						if (currentToken.kind == TokenKind.RPAREN) {
//							acceptIt();
//						} else {
//							parseArgumentList();
//							accept(TokenKind.RPAREN);
//						}
//						break;
//						
//					// just reference
//					default:
//						break;
////						parseError("parsing expression, in subcase ID/THIS, current token is: " + currentToken.spelling + "and its kind is: " + currentToken.kind);
//				}
//				break;
//				
//			case UNOP: case MINUS:
//				acceptIt();
//				parseExpression();
//				break;
//			
//			case LPAREN:
//				acceptIt();
//				parseExpression();
//				accept(TokenKind.RPAREN);
//				break;
//			
//			case NUM: case TRUE: case FALSE:
//				acceptIt();
//				break;
//			
//			case NEW:
//				acceptIt();
////				System.out.println("in hereeee");
//				switch (currentToken.kind) {
//					case ID:
//						acceptIt();
//						switch (currentToken.kind) {
//						case LPAREN:
//							acceptIt();
//							accept(TokenKind.RPAREN);
//							break;
//						case LBRACKET:
//							acceptIt();
//							parseExpression();
//							accept(TokenKind.RBRACKET);
//							break;
//						default:
//							parseError("parsing expression, nothing after new.");
//						
//						}
//						break;
//					
//					case INT:
//						acceptIt();
//						accept(TokenKind.LBRACKET);
//						parseExpression();
//						accept(TokenKind.RBRACKET);
//						break;
//					
//					default:
//						parseError("parsing expression, in subcase NEW, current token is: " + currentToken.spelling + "and its kind is: " + currentToken.kind);
//				}
//				break;
//			
//			default:
//				parseError("parsing expression, current token is: " + currentToken.spelling + "and its kind is: " + currentToken.kind);
//				break;
//		}
//		if (currentToken.kind == TokenKind.BINOP || currentToken.kind == TokenKind.MINUS 
//				|| currentToken.kind == TokenKind.EQUAL) {
//			acceptIt();
//			parseExpression();
//		}
//	}


	private boolean startsStatement(TokenKind kind) throws SyntaxError {
		return kind == TokenKind.LCURLY ||
				startsType(kind) ||
				 startsReference(kind) ||
				  kind == TokenKind.RETURN ||
				   kind == TokenKind.IF ||
					kind == TokenKind.WHILE;
	}


	private boolean startsReference(TokenKind kind) throws SyntaxError {
		return kind == TokenKind.ID ||
				kind == TokenKind.THIS;
	}


	private ParameterDeclList parseParameterList() throws SyntaxError {
		ParameterDeclList paramList = new ParameterDeclList();
		TypeDenoter td = parseType();
		Identifier id = parseId();
		paramList.add(new ParameterDecl(td, id.spelling, null));
		while (currentToken.kind == TokenKind.COMMA) {
			acceptIt();
			td = parseType();
			id = parseId();
			paramList.add(new ParameterDecl(td, id.spelling, null));
		}
		return paramList;
	}


	private TypeDenoter parseType() throws SyntaxError{
		TypeDenoter td = null;
		switch (currentToken.kind) {
		case INT:
//		    public BaseType(TypeKind t, SourcePosition posn){
			td = new BaseType(TypeKind.INT, null);
			acceptIt();
			if (currentToken.kind == TokenKind.LBRACKET) {
				acceptIt();
				accept(TokenKind.RBRACKET);
				
//			    public ArrayType(TypeDenoter eltType, SourcePosition posn){
				td = new ArrayType(td, null);
			}
			break;
		case BOOLEAN:
			td = new BaseType(TypeKind.BOOLEAN, null);
			acceptIt();
			break;
		case ID:
			Identifier id = parseId();
			
//		    public ClassType(Identifier cn, SourcePosition posn){
			td = new ClassType(id, null);
			if (currentToken.kind == TokenKind.LBRACKET) {
				acceptIt();
				accept(TokenKind.RBRACKET);
				td = new ArrayType(td, null);
			}
			break;
		default:
			parseError("Wrong in parsing Type, currentToken is: " + currentToken);
		}
		return td;
	}


	private Identifier parseId() throws SyntaxError {
		Identifier id = null;
		if(currentToken.kind == TokenKind.ID) {
			id = new Identifier(currentToken);
			acceptIt();
		} else {
			parseError("is not an identifier");
		}
		return id;
	}


	private void parseClassDeclaration() throws SyntaxError {
		while (currentToken.kind == TokenKind.CLASS) {
			parseClassDeclaration();
		}
		accept(TokenKind.EOT);
	}

	/**
	 * accept current token and advance to next token
	 */
	private void acceptIt() throws SyntaxError {
		accept(currentToken.kind);
	}

	/**
	 * verify that current token in input matches expected token and advance to next token
	 * @param expectedToken
	 * @throws SyntaxError  if match fails
	 */
	private void accept(TokenKind expectedTokenKind) throws SyntaxError {
		if (currentToken.kind == expectedTokenKind) {
//			System.out.println(currentToken.spelling);
			currentToken = scanner.scan();
		}
		else
			parseError("Parsing " + currentToken.spelling + " and expecting '" + expectedTokenKind +
					"' but found '" + currentToken.kind + "'");
	}

	/**
	 * report parse error and unwind call stack to start of parse
	 * @param e  string with error detail
	 * @throws SyntaxError
	 */
	private void parseError(String e) throws SyntaxError {
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}

	// show parse stack whenever terminal is  accepted
	private void pTrace() {
		StackTraceElement [] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + currentToken.kind + " (\"" + currentToken.spelling + "\")");
		System.out.println();
	}

}
