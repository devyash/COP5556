package cop5556fa17;



import java.util.Arrays;
import java.util.HashSet;

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
//	HashSet for various Non Terminals
	HashSet <Kind>declarationsHS;
	HashSet <Kind>statementHS;
	HashSet <Kind> variableDeclarationHS;
	HashSet <Kind> sourceSinkDeclarationHS;
	HashSet <Kind> unaryExpressionNotPlusMinusHS;
	HashSet <Kind> primaryHS;
	HashSet <Kind> functionNameHS;
	HashSet <Kind> expressionHS;
	void intializeHashSets(){
		declarationsHS = new HashSet<Kind>();
		declarationsHS.add(Kind.KW_int);
		declarationsHS.add(Kind.KW_boolean);
		declarationsHS.add(Kind.KW_image);
		declarationsHS.add(Kind.KW_url);
		declarationsHS.add(Kind.KW_file);
		
		statementHS = new HashSet<Kind>();
		statementHS.add(Kind.IDENTIFIER);
		
		variableDeclarationHS = new HashSet<Kind>();
		variableDeclarationHS.add(Kind.KW_int);
		variableDeclarationHS.add(Kind.KW_boolean);
		
		sourceSinkDeclarationHS = new HashSet<Kind>();
		sourceSinkDeclarationHS.add(Kind.KW_url);
		sourceSinkDeclarationHS.add(Kind.KW_file);
		
		expressionHS = new HashSet<Kind>();
		expressionHS.add(Kind.OP_PLUS);
		expressionHS.add(Kind.OP_MINUS);
		expressionHS.add(Kind.OP_EXCL);
		
//		UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
//		| IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
		unaryExpressionNotPlusMinusHS = new HashSet<Kind>();
		unaryExpressionNotPlusMinusHS.add(Kind.OP_EXCL);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_x);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_y);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_r);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_a);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_X);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_Y);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_Z);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_A);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_R);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_DEF_X);
		unaryExpressionNotPlusMinusHS.add(Kind.KW_DEF_Y);
		unaryExpressionNotPlusMinusHS.add(Kind.IDENTIFIER); //IdentOrPixelSelectorExpression
		unaryExpressionNotPlusMinusHS.add(Kind.INTEGER_LITERAL);
		unaryExpressionNotPlusMinusHS.add(Kind.BOOLEAN_LITERAL);
		unaryExpressionNotPlusMinusHS.add(Kind.LPAREN);

		

//		Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
		primaryHS = new HashSet<Kind>();
		primaryHS.add(Kind.INTEGER_LITERAL);
		primaryHS.add(Kind.BOOLEAN_LITERAL);
		primaryHS.add(Kind.LPAREN);
		primaryHS.add(Kind.KW_sin); //FunctionApplication 
		primaryHS.add(Kind.KW_cos);
		primaryHS.add(Kind.KW_atan);
		primaryHS.add(Kind.KW_abs);
		primaryHS.add(Kind.KW_cart_x);
		primaryHS.add(Kind.KW_cart_y);
		primaryHS.add(Kind.KW_polar_a);
		primaryHS.add(Kind.KW_polar_r);
		
//		FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs 
//		| KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r

		functionNameHS = new HashSet<Kind>();
		functionNameHS.add(Kind.INTEGER_LITERAL);
		functionNameHS.add(Kind.KW_sin);
		functionNameHS.add(Kind.KW_cos);
		functionNameHS.add(Kind.KW_atan);
		functionNameHS.add(Kind.KW_abs);
		functionNameHS.add(Kind.KW_cart_x);
		functionNameHS.add(Kind.KW_cart_y);
		functionNameHS.add(Kind.KW_polar_a);
		functionNameHS.add(Kind.KW_polar_r);

		unaryExpressionNotPlusMinusHS.addAll(functionNameHS); //FunctionApplication 
		expressionHS.addAll(unaryExpressionNotPlusMinusHS);	


//		TODO: ADD more Hash Sets	
	}

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		intializeHashSets();
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
//	Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	void program() throws SyntaxException {
			match(Kind.IDENTIFIER);
			while(declarationsHS.contains(t.kind) || statementHS.contains(t.kind)) {
				if(statementHS.contains(t.kind))
					statement();
				else
					declaration();
				match(Kind.SEMI);
			}
	}
	
//	Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  
	void declaration() throws SyntaxException{
		if(variableDeclarationHS.contains(t.kind) || t.kind==Kind.KW_image ||sourceSinkDeclarationHS.contains(t.kind)) {
			if(variableDeclarationHS.contains(t.kind))
				variableDeclaration();
			else if(t.kind==Kind.KW_image)
				imageDeclaration();
			else if(sourceSinkDeclarationHS.contains(t.kind))
				sourceSinkDeclaration();
		}
		else {
			error("Not a Valid Declaration!");
		}
	}
	
//	VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
	void variableDeclaration() throws SyntaxException{
		if(t.kind==Kind.KW_int || t.kind==Kind.KW_boolean) {
			varType();
			match(Kind.IDENTIFIER);
			if(t.kind==Kind.OP_ASSIGN) {
				match(Kind.OP_ASSIGN);
				expression();
			}
		}
		else 
			error("Not a variableDeclaration!");
	}
	
//	VarType ::= KW_int | KW_boolean
	void varType() throws SyntaxException{
		if(t.kind==Kind.KW_int||t.kind==Kind.KW_boolean)
			consume();
		else 
			error("Invalid varType ");
	}
	
//	ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε) IDENTIFIER ( OP_LARROW Source | ε )   
	void imageDeclaration() throws SyntaxException{
			match(Kind.KW_image);
			if(t.kind==Kind.LSQUARE) {
				match(Kind.LSQUARE);
				expression();
				match(Kind.COMMA);
				expression();
				match(Kind.RSQUARE);				
			}
			match(Kind.IDENTIFIER);
			if(t.kind==Kind.OP_LARROW) {
				match(Kind.OP_LARROW);
				source();
			}
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
		if(t.kind==Kind.STRING_LITERAL || t.kind==Kind.OP_AT || t.kind==Kind.IDENTIFIER) {
			if(t.kind==Kind.OP_AT){
				consume();
				expression();
			}
			else if(t.kind==Kind.STRING_LITERAL || t.kind==Kind.IDENTIFIER)
				consume();
		}
		else 
			error("Invalid source!");
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
		if(t.kind==Kind.IDENTIFIER) {
//			TODO Make this OR
			Token tmp = scanner.peek();
			if(tmp.kind==Kind.LSQUARE || tmp.kind == Kind.OP_ASSIGN)
				assignmentStatement();
			else if(tmp.kind==Kind.OP_RARROW)
				imageOutStatement();
			else if(tmp.kind==Kind.OP_LARROW)
				imageInStatement();	
			else
				error("Expecting Statement here");
		}
		else 
			error("Expecting Statement here");
	}
	
//	AssignmentStatement ::= Lhs OP_ASSIGN Expression
	void assignmentStatement() throws SyntaxException{
		lhs();
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
	
//	Sink ::= IDENTIFIER | KW_SCREEN  //ident must be file //TODO what is ident must be a file?
	void sink() throws SyntaxException{
		if(t.kind==Kind.IDENTIFIER || t.kind == Kind.KW_SCREEN ) {
			if(t.kind==Kind.IDENTIFIER)
				match(Kind.IDENTIFIER);
			if(t.kind==KW_SCREEN)
				match(Kind.KW_SCREEN);
		}
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
//		TODO: Confirm this
		if(expressionHS.contains(t.kind)) {
			orExpression();
			if(t.kind==Kind.OP_Q) {
				match(Kind.OP_Q);
				expression();
				match(Kind.OP_COLON);
				expression();
			}
		}
		else
			error("Expected Expression found something else!");

	}
//	OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
	void orExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			andExpression();
			while(t.kind==Kind.OP_OR) {
				//TODO: Confirm if this is right
				match(Kind.OP_OR);
				andExpression();
			}
		}
		else 
			error("Expected Expression found something else!");
	}
	
	//AndExpression ::= EqExpression ( OP_AND  EqExpression )*
	void andExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			eqExpression();
			while(t.kind==Kind.OP_AND) {
				//TODO: Confirm if this is right
				match(Kind.OP_AND);
				andExpression();
			}
		}
		else 
			error("Expected Expression found something else!");
	}
	
//	EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
	void eqExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			relExpression();
			while(t.kind==Kind.OP_EQ || t.kind==Kind.OP_NEQ) {
				if(t.kind==Kind.OP_EQ) {
					match(Kind.OP_EQ);
				}
				else if(t.kind==Kind.OP_NEQ) {
					match(Kind.OP_NEQ);
				}
				relExpression();
			}
		}
		else 
			error("Expected Expression found something else!");
	}
	
	
//	RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
	void relExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			addExpression();
			while(t.kind==Kind.OP_LT || t.kind==Kind.OP_GT || t.kind==Kind.OP_LE || t.kind==Kind.OP_GE ) {
				if(t.kind==Kind.OP_LT) 
					match(Kind.OP_LT);
				else if(t.kind==Kind.OP_GT) 
					match(Kind.OP_GT);
				else if(t.kind==Kind.OP_LE) 
					match(Kind.OP_LE);
				else if(t.kind==Kind.OP_GE) 
					match(Kind.OP_GE);
				addExpression();
			}
		}
		else 
			error("Expected Expression found something else!");
	}
//	AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
	void addExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			multExpression();
			while(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS) {
				if(t.kind==Kind.OP_PLUS) 
					match(Kind.OP_PLUS);
				else if(t.kind==Kind.OP_MINUS) 
					match(Kind.OP_MINUS);
				multExpression();
			}
		}
		else 
			error("Expected Expression found something else!");
	}
//	MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
	void multExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			unaryExpression();
			while(t.kind==Kind.OP_TIMES || t.kind==Kind.OP_DIV || t.kind==Kind.OP_MOD) {
				if(t.kind==Kind.OP_TIMES) 
					match(Kind.OP_TIMES);
				else if(t.kind==Kind.OP_DIV) 
					match(Kind.OP_DIV);
				else if(t.kind==Kind.OP_MOD) 
					match(Kind.OP_MOD);
				unaryExpression();
			}
		}
		else 
			error("Expected Expression found something else!");
	}
	
//	UnaryExpression ::= OP_PLUS UnaryExpression 
//    | OP_MINUS UnaryExpression 
//    | UnaryExpressionNotPlusMinus

	void unaryExpression() throws SyntaxException {
		if(t.kind==Kind.OP_PLUS) {
			match(Kind.OP_PLUS);
			unaryExpression();
		}
		else if(t.kind==Kind.OP_MINUS) {
			match(Kind.OP_MINUS);
			unaryExpression();
		}
		else if(unaryExpressionNotPlusMinusHS.contains(t.kind)) {
			unaryExpressionNotPlusMinus();
		}
		else 
			error("Expected Expression found something else!");
	}
	
//	UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
//	| IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
	void unaryExpressionNotPlusMinus() throws SyntaxException {
			if(t.kind==Kind.OP_EXCL) {
				match(Kind.OP_EXCL);
				unaryExpression();
			}
			else if(primaryHS.contains(t.kind)) {
				primary();
			}
			else if(t.kind==Kind.IDENTIFIER) {
				identOrPixelSelectorExpression();
			}
			else if(t.kind==Kind.KW_x) {
				match(Kind.KW_x);
			}
			else if(t.kind==Kind.KW_y) {
				match(Kind.KW_y);
			}
			else if(t.kind==Kind.KW_r) {
				match(Kind.KW_r);
			}
			else if(t.kind==Kind.KW_a) {
				match(Kind.KW_a);
			}
			else if(t.kind==Kind.KW_X) {
				match(Kind.KW_X);
			}
			else if(t.kind==Kind.KW_Y) {
				match(Kind.KW_Y);
			}
			else if(t.kind==Kind.KW_Z) {
				match(Kind.KW_Z);
			}
			else if(t.kind==Kind.KW_A) {
				match(Kind.KW_A);
			}
			else if(t.kind==Kind.KW_R) {
				match(Kind.KW_R);
			}
			else if(t.kind==Kind.KW_DEF_X) {
				match(Kind.KW_DEF_X);
			}
			else if(t.kind==Kind.KW_DEF_Y) {
				match(Kind.KW_DEF_Y);
			}
			else 
				error("Expected Expression found something else!");
	}
	
	
//	Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
	void primary() throws SyntaxException {
		if(t.kind==Kind.INTEGER_LITERAL) {
			match(Kind.INTEGER_LITERAL);
		}
		else if(t.kind==Kind.LPAREN) {
			match(Kind.LPAREN);
			expression();
			match(Kind.RPAREN);
		}
		else if (t.kind==Kind.BOOLEAN_LITERAL) {
			match(Kind.BOOLEAN_LITERAL);
		}
		else if(functionNameHS.contains(t.kind)) {
			functionApplication();
		}
		else 
			error("Expected Expression found something else!");
	}
	
//	IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER
	void identOrPixelSelectorExpression() throws SyntaxException {
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			match(Kind.LSQUARE);
			selector();
			match(Kind.RSQUARE);
		}
	}
	
//	Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | ε )
	void lhs() throws SyntaxException{
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			match(Kind.LSQUARE);
			lhsSelector();
			match(Kind.RSQUARE);
		}
	}

//	LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
	void lhsSelector() throws SyntaxException{
		match(Kind.LSQUARE);
		if(t.kind==Kind.KW_x || t.kind==Kind.COMMA || t.kind==Kind.KW_y ||t.kind==Kind.KW_r ||t.kind==Kind.COMMA ||t.kind==Kind.KW_A) {
			if(t.kind==Kind.KW_x || t.kind==Kind.COMMA || t.kind==Kind.KW_y)
				xySelector();
			if(t.kind==Kind.KW_r ||t.kind==Kind.COMMA ||t.kind==Kind.KW_A)
				raSelector();
			match(Kind.RSQUARE);
		}
		else
			error("Wrong lhsSelector");
	}
	
//	XySelector ::= KW_x COMMA KW_y
	void xySelector() throws SyntaxException{
		match(Kind.KW_x);
		match(Kind.COMMA);
		match(Kind.KW_y);
	}
	
	//RaSelector ::= KW_r COMMA KW_A
	void raSelector() throws SyntaxException{
		match(Kind.KW_r);
		match(Kind.COMMA);
		match(Kind.KW_A);
	}
	
	
	//	Selector ::=  Expression COMMA Expression 
	void selector() throws SyntaxException{
		expression();
		match(Kind.COMMA);
		expression();
	}
	
//	FunctionApplication ::= FunctionName LPAREN Expression RPAREN  
//			| FunctionName  LSQUARE Selector RSQUARE 
	void functionApplication() throws SyntaxException {
		if(functionNameHS.contains(t.kind)) {
			functionName();
			if(t.kind==Kind.LPAREN) {
				match(Kind.LPAREN);
				expression();
				match(Kind.RPAREN);
			}
			else if(t.kind==Kind.LSQUARE) {
				match(Kind.LSQUARE);
				selector();
				match(Kind.RSQUARE);
			}
		}
		else 
			error("Expected Expression found something else!");
	}

//	FunctionName ::= KW_sin | KW_cos | KW_atan | KW_abs 
//			| KW_cart_x | KW_cart_y | KW_polar_a | KW_polar_r
	void functionName() throws SyntaxException {
		if(functionNameHS.contains(t.kind)) {
			if(t.kind==Kind.KW_sin) {
				match(Kind.KW_sin);
			}
			else if(t.kind==Kind.KW_cos) {
				match(Kind.KW_cos);
			}
			else if(t.kind==Kind.KW_atan) {
				match(Kind.KW_atan);
			}
			else if(t.kind==Kind.KW_abs) {
				match(Kind.KW_abs);
			}
			else if(t.kind==Kind.KW_cart_x) {
				match(Kind.KW_cart_x);
			}
			else if(t.kind==Kind.KW_cart_y) {
				match(Kind.KW_cart_y);
			}
			else if(t.kind==Kind.KW_polar_a) {
				match(Kind.KW_polar_a);
			}
			else if(t.kind==Kind.KW_polar_r) {
				match(Kind.KW_polar_r);
			}
			else 
				error("Expected Expression found something else!");	
		}
		else 
			error("Expected Expression found something else!");
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
		error("Invalid ");
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
