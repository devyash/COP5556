package cop5556fa17;



import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	void program() throws SyntaxException {
		//TODO  implement this
		match(Kind.IDENTIFIER);
		//TODO IMPLEMENT DECLARATION while
		throw new UnsupportedOperationException();
	}
	
	void declaration() throws SyntaxException{
		
	}
	
//	VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
	void variableDeclaration() throws SyntaxException{
		varType();
		match(Kind.IDENTIFIER);
//		TODO Match OP_ASSIGN OR EXPRESSION //REDUCE TO LL1
		if(t.kind)
		
	}
	
//	VarType ::= KW_int | KW_boolean
	void varType() throws SyntaxException{
		if(t.kind==Kind.KW_int||t.kind==Kind.KW_boolean){
			consume();
		}
		else {
			error("Invalid varType ");
		}
	}
	
//	ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε) IDENTIFIER ( OP_LARROW Source | ε )   
	void imageDeclaration() throws SyntaxException{
		match(Kind.KW_image);
		//TODO
	}
//	SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
	void sourceSinkDeclaration() throws SyntaxException{
		sourceSinkType();
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		source();
		
	}
	
//	Source ::= STRING_LITERAL| OP_AT Expression | IDENTIFIER 
	void source() throws SyntaxException{
		//TODO
	}
	
//	SourceSinkType := KW_url | KW_file
	void sourceSinkType() throws SyntaxException{
		if(t.kind==Kind.KW_url||t.kind==Kind.KW_file){
			consume();
		}
		else {
			error("Invalid sourceSinkType ");
		}	
	}
	
	//Statement  ::= AssignmentStatement | ImageOutStatement | ImageInStatement  
	void statement() throws SyntaxException{
		//TODO Make them or
//		assignmentStatement();
//		imageOutStatement();
//		imageInStatement();	
	}
	
//	AssignmentStatement ::= Lhs OP_ASSIGN Expression
	void assignmentStatement() throws SyntaxException{
		//TODO  lhs
		match(Kind.OP_ASSIGN);
		expression();
		
	}
	
//	ImageOutStatement ::= IDENTIFIER OP_RARROW Sink
	void imageOutStatement() throws SyntaxException{
		match(Kind.IDENTIFIER);
		match(Kind.OP_RARROW);
		sink();
	}
	
//	ImageInStatement ::= IDENTIFIER OP_LARROW Source
	void imageInStatement() throws SyntaxException{
		match(Kind.IDENTIFIER);
		match(Kind.OP_LARROW);
		source();
	}
	
//	Sink ::= IDENTIFIER | KW_SCREEN  //ident must be file
	void sink() throws SyntaxException{
		//TODO
	}
	
	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
//	Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression|   OrExpression
	void expression() throws SyntaxException {
		//TODO implement this.
//		if(t.kind==)
		throw new UnsupportedOperationException();
	}

//	Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | ε )
	void lhs() throws SyntaxException{
		match(Kind.IDENTIFIER);
		//TODO
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private void matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		error(message);
	}
	
	private void match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			consume();
			return;
		}
		error("");
	}
	
	private void error(String msg) throws SyntaxException {
		String message;
		if(msg.length()==0) {
			message=  "Expected Token Type " + t.line + ":" + t.pos_in_line;
		}
			message=msg+ t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
	
	private void consume() {
		t=scanner.nextToken();
	}
	
	
}
