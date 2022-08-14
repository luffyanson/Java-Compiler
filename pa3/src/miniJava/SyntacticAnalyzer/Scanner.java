package miniJava.SyntacticAnalyzer;

import java.io.IOException;
import java.io.InputStream;

import miniJava.ErrorReporter;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;

public class Scanner {
	private boolean isComment;
	
//	comment state = 0 not comment
//			= 1 show /
//			= 2 show second /
//			= 12 show first *
//			= 13 show second *
//			= 3 show third /
//			= 4 show fourth / end of comment
//			= 14 show second / end of comment
	private int commentState;
	
	private InputStream inputStream;
	private ErrorReporter reporter;
	
	private char currentChar;
	private byte currentKind;
	private StringBuilder currentSpelling;
	
	// true when end of line is found
	private boolean eot = false; 
	
	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.isComment = false;
		this.commentState = 0;
		this.inputStream = inputStream;
		this.reporter = reporter;

		// initialize scanner state
		readChar();
	}	
	
	public void skipUntilStar() {
		while (currentChar != '*') {
			// TODO: could have multi-lines in it
			
			skipIt();
		}
	}
	
	/**
	 * skip whitespace and scan next token
	 */	
// ? SINGLE SLASH
//	comment state = 0 not comment
//	= 1 show /
//	= 2 show second /
//	= 12 show first *
//	= 13 show second *
//	= 14 show second / end of comment
	public void scanSeparator() {
		switch (commentState) {
			case 0:
				while (!eot && 
						(currentChar == ' ' ||
							currentChar == '\t' ||
								currentChar == '\n' ||
									currentChar == '\r')) {
					skipIt();
				}
				if (currentChar == '/') {
					commentState = 1;
					skipIt();
					scanSeparator();
				}
				break;
			
			case 1:
				if (currentChar == '/') {
					commentState = 2;
					isComment = true;
					skipIt();
					scanSeparator();
				} else if (currentChar == '*') {
					commentState = 12;
					isComment = true;
					skipIt();
					scanSeparator();
				} else {
					scanError("single use of /");
					System.exit(4);
				}
				break;
			
			case 2:
				if (currentChar == '\r' || currentChar == '\n' || eot) {
					commentState = 0;
					isComment = false;
					scanSeparator();
				} else {
					skipIt();
					scanSeparator();		
				}
				break;
				
			case 12:
				if (eot) {
					scanError("multi-line comment end wrongly");
					System.exit(4);
				}
				if (currentChar == '*') {
					skipIt();
					commentState = 13;
					scanSeparator();
				} else {
					skipIt();
					scanSeparator();
				}
				break;
			
			case 13:
				if (eot) {
					System.exit(4);
				}
				if (currentChar != '/') {
					skipIt();
					commentState = 12;
					scanSeparator();
				} else {
					skipIt();
					commentState = 0;
					isComment = false;
					scanSeparator();
				}
				break;	
				
			default:
				break;
		}		
	}
	

	public Token scan() {
		// skip whitespace and comments
		scanSeparator();
		if (isComment) {
			scanError("comment didnt end");
		}
		// start of a token: collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();
		String spelling = currentSpelling.toString();
		
		if (kind == TokenKind.ID) {
			switch (spelling) {
			case "class":
				kind = TokenKind.CLASS;
				break;
			case "public":
				kind = TokenKind.PUBLIC;
				break;
			case "private":
				kind = TokenKind.PRIVATE;
				break;
			case "static":
				kind = TokenKind.STATIC;
				break;
			case "int":
				kind = TokenKind.INT;
				break;
			case "boolean":
				kind = TokenKind.BOOLEAN;
				break;
			case "void":
				kind = TokenKind.VOID;
				break;
			case "this":
				kind = TokenKind.THIS;
				break;
			case "return":
				kind = TokenKind.RETURN;
				break;
			case "if":
				kind = TokenKind.IF;
				break;
			case "else":
				kind = TokenKind.ELSE;
				break;
			case "while":
				kind = TokenKind.WHILE;
				break;
			case "true":
				kind = TokenKind.TRUE;
				break;
			case "false":
				kind = TokenKind.FALSE;
				break;
			default:
				String[] ss = {"class","public","private","static","int","boolean","void","this","return","if","else","while","true","false"};
				for (String s : ss) {
					if (spelling.toLowerCase().equals(s)) {
						reporter.reportError("Key word should not be used as identifiers.");
						System.exit(4);
					}
				}
				break;
			}
		}

		// return new token
		return new Token(kind, spelling, null);
	}

	
	/**
	 * determine token kind
	 */
	public TokenKind scanToken() {
		if (eot) {
			return TokenKind.EOT; 
		}
			
		switch (currentChar) {
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
	        case 'u':  case 'v':  case 'w':  case 'x':
	        case 'y':  case 'z':
	        
	        case 'A':  case 'B':  case 'C':  case 'D':
	        case 'E':  case 'F':  case 'G':  case 'H':
	        case 'I':  case 'J':  case 'K':  case 'L':
	        case 'M':  case 'N':  case 'O':	 case 'P':
	        case 'Q':  case 'R':  case 'S':  case 'T':
	        case 'U':  case 'V':  case 'W':  case 'X':
	        case 'Y':  case 'Z':
	        	if (currentChar == 'n') {
	        		takeIt();
	        		if (currentChar == 'e') {
	        			takeIt();
	        			if (currentChar == 'w') {
	        				takeIt();
	        				return TokenKind.NEW;
	        			}
	        		}
	        	}
	        	while(Character.isLetter(currentChar) 
	        			|| Character.isDigit(currentChar)
	        				|| currentChar == '_') {
	        		takeIt();
	        	}
	        	return TokenKind.ID;
	        
	        case '0':  case '1':  case '2':  case '3':  case '4':
	        case '5':  case '6':  case '7':  case '8':  case '9':
	            takeIt();
	            while(Character.isDigit(currentChar)) {
	            	takeIt();
	            }
	            return TokenKind.NUM;
	        
	        case '>':
	            takeIt();
	            if(currentChar == '=') {
	                takeIt();
	                return TokenKind.GTE;
	            } 
	            return TokenKind.GT;
	            
	        case '<':
	            takeIt();
	            if(currentChar == '=') {
	                takeIt();
	                return TokenKind.LTE;
	            } 
	            return TokenKind.LT;
	            
	        case '=':
	            takeIt();
	            if(currentChar == '=') {
	                takeIt();
	                return TokenKind.ISEQUAL;
	            } 
	            return TokenKind.EQUAL;
	            
	        case '+':
	        	takeIt();
	        	return TokenKind.PLUS;
	        	
	        case '*':
	        	takeIt();
	        	return TokenKind.TIMES;
	        	
	        case '/':
//	        	char temp = '/';
//	        	skipIt();
//	        	switch (currentChar) {
//	        	case '/':
//	        		skipIt();
//	        		while (currentChar != '\r' || currentChar != '\n') {
//	        			skipIt();
//	        		}
//	        		
//	        		// skip the newline char
//	        		skipIt();
//	        		
//	        		// scan next token and return its kind
//	        		return scan().kind;
//	        		
//	        	case '*':
//	        		skipIt();
//	        		while(true) {
//	        			if(currentChar == '*') {
//	        				skipIt();
//	        				if (currentChar == '/') {
//	        					skipIt();
//	        					break;
//	        				} else {
//	        					continue;
//	        				}
//	        			} else {
//	        				skipIt();
//	        			}
//	        		}
//	        		
//	        		return scan().kind;
//	        	
//	        	default:
//	        		take(temp);
//	        		return TokenKind.DIVIDE;
//	        	}
	        	takeIt();
	        	return TokenKind.DIVIDE;
	        
	        case '&':
	            takeIt();
	            if(currentChar != '&') {
	                System.exit(4);
	            }
	            takeIt();
	            return TokenKind.AND;

	        case '|':
	            takeIt();
	            if(currentChar != '|')
	                System.exit(4);
	            takeIt();
	            return TokenKind.OR;
	        
	        case '!':
	            takeIt();
	            if(currentChar == '=') {
	                takeIt();
	                return TokenKind.NOTEQUAL;
	            }
	            return TokenKind.EXCLA;
	            
	        case '-':
	            takeIt();
	            return TokenKind.MINUS;
	        
	        case '.':
	            takeIt();
	            return TokenKind.DOT;

	        case ',':
	            takeIt();
	            return TokenKind.COMMA;

	        case ';':
	            takeIt();
	            return TokenKind.SEMICOLON;

	        case '(':
	            takeIt();
	            return TokenKind.LPAREN;

	        case ')':
	            takeIt();
	            return TokenKind.RPAREN;

	        case '[':
	            takeIt();
	            return TokenKind.LBRACKET;

	        case ']':
	            takeIt();
	            return TokenKind.RBRACKET;

	        case '{':
	            takeIt();
	            return TokenKind.LCURLY;

	        case '}':
	            takeIt();
	            return TokenKind.RCURLY;
	        
	        default:
				scanError("Unrecognized character '" + currentChar + "' in input");
				return TokenKind.ERROR; 
		}
	}

	
	private void takeIt() {
		currentSpelling.append(currentChar);
		nextChar();
	}
	
	private void take(char expectedChar) {
		if (currentChar == expectedChar) {
			takeIt();
		}
	}

	private void skipIt() {
//		System.out.println("skip " + currentChar);
		nextChar();
	}


	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}


	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';

	/**
	 * advance to next char in inputstream
	 * detect end of file or end of line as end of input
	 */
	private void nextChar() {
		if (!eot)
			readChar();
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1) {
//				System.out.print("1" + (c == -1));
//				System.out.print("2" + (currentChar == eolUnix ));
//				System.out.print("3" + (currentChar == eolWindows));
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}
	


}
