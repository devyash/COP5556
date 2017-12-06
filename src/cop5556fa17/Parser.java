package cop5556fa17;




import static cop5556fa17.Scanner.Kind.EOF;
import static cop5556fa17.Scanner.Kind.KW_SCREEN;

import java.util.ArrayList;
import java.util.HashSet;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionApp;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class Parser {

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

	Parser(Scanner scanner) {
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
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
//	Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	Program program() throws SyntaxException {
		Token temp=t;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<>();
			match(Kind.IDENTIFIER);
//			TODO: Return what? PRgram?
			while(declarationsHS.contains(t.kind) || statementHS.contains(t.kind)) {
				if(statementHS.contains(t.kind))
					decsAndStatements.add(statement());
				else
					decsAndStatements.add(declaration());
				match(Kind.SEMI);
			}
			return new Program(temp,temp,decsAndStatements);
	}
	
//	Declaration :: =  VariableDeclaration     |    ImageDeclaration   |   SourceSinkDeclaration  
	Declaration declaration() throws SyntaxException{
		if(variableDeclarationHS.contains(t.kind) || t.kind==Kind.KW_image ||sourceSinkDeclarationHS.contains(t.kind)) {
			if(variableDeclarationHS.contains(t.kind))
				return variableDeclaration();
			else if(t.kind==Kind.KW_image)
				return imageDeclaration();
			else if(sourceSinkDeclarationHS.contains(t.kind))
				return sourceSinkDeclaration();
		}
		else {
			error("Not a Valid Declaration!");
		}
		return null;
	}
	
//	VariableDeclaration  ::=  VarType IDENTIFIER  (  OP_ASSIGN  Expression  | ε )
	Declaration_Variable variableDeclaration() throws SyntaxException{
		if(t.kind==Kind.KW_int || t.kind==Kind.KW_boolean) {
			Token temp = t;
			Expression e = null;
			varType();
			Token ident_temp=t;
			match(Kind.IDENTIFIER);
			if(t.kind==Kind.OP_ASSIGN) {
				match(Kind.OP_ASSIGN);
				e = expression();
			}
			return new Declaration_Variable(temp, temp, ident_temp, e);
		}
		else 
			error("Not a variableDeclaration!");
		return null;
	}
	
//	VarType ::= KW_int | KW_boolean
	void varType() throws SyntaxException{
		if(t.kind==Kind.KW_int||t.kind==Kind.KW_boolean)
			consume();
		else 
			error("Invalid varType ");
	}
	
//	ImageDeclaration ::=  KW_image  (LSQUARE Expression COMMA Expression RSQUARE | ε) IDENTIFIER ( OP_LARROW Source | ε )   
	Declaration_Image imageDeclaration() throws SyntaxException{
			Token temp = t;
			Expression xSize=null;
			Expression ySize=null;
			Token ident_temp=null;
			Source s=null;
			match(Kind.KW_image);
			if(t.kind==Kind.LSQUARE) {
				match(Kind.LSQUARE);
				xSize=expression();
				match(Kind.COMMA);
				ySize=expression();
				match(Kind.RSQUARE);				
			}
			ident_temp=t;
			match(Kind.IDENTIFIER);
			if(t.kind==Kind.OP_LARROW) {
				match(Kind.OP_LARROW);
				s=source();
			}
			return new Declaration_Image(temp, xSize, ySize, ident_temp, s); //TODO: confirm this
	}
	
//	SourceSinkDeclaration ::= SourceSinkType IDENTIFIER  OP_ASSIGN  Source
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException{
		Token temp =t;
		sourceSinkType();
		Token ident_temp=t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		Source s = source();
		return new Declaration_SourceSink(temp, temp, ident_temp, s); 
		
	}
	
//	Source ::= STRING_LITERAL| OP_AT Expression | IDENTIFIER 
	Source source() throws SyntaxException{
		if(t.kind==Kind.STRING_LITERAL || t.kind==Kind.OP_AT || t.kind==Kind.IDENTIFIER) {
			Token temp=t;
			if(t.kind==Kind.OP_AT){
				consume();
				Expression e = expression();
				return new Source_CommandLineParam(temp,e);
			}
			else if(t.kind==Kind.STRING_LITERAL) {
				consume();
				return new Source_StringLiteral(temp,temp.getText()); //TODO: What is fileorURL?
			}
			else if(t.kind==Kind.IDENTIFIER) {
					consume();
					return new Source_Ident(temp, temp);
			}
		}
		else 
			error("Invalid source!");
		return null;
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
	Statement statement() throws SyntaxException{
		Statement s= null;
		if(t.kind==Kind.IDENTIFIER) {
			Token tmp = scanner.peek();
			if(tmp.kind==Kind.LSQUARE || tmp.kind == Kind.OP_ASSIGN)
				s=assignmentStatement();
			else if(tmp.kind==Kind.OP_RARROW)
				s=imageOutStatement();
			else if(tmp.kind==Kind.OP_LARROW)
				s=imageInStatement();	
			else
				error("Expecting Statement here");
		}
		else 
			error("Expecting Statement here");
		return s;
	}
	
//	AssignmentStatement ::= Lhs OP_ASSIGN Expression
	Statement_Assign assignmentStatement() throws SyntaxException{
		Token temp=t;
		LHS l=lhs();
		match(Kind.OP_ASSIGN);
		Expression e = expression();
		return new Statement_Assign(temp, l, e);
		
	}
	
//	ImageOutStatement ::= IDENTIFIER OP_RARROW Sink
	Statement_Out imageOutStatement() throws SyntaxException{
		Token temp=t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_RARROW);
		Sink s = sink();
		return new Statement_Out(temp, temp, s);
	}
	
//	ImageInStatement ::= IDENTIFIER OP_LARROW Source
	Statement_In imageInStatement() throws SyntaxException{
		Token temp=t;
		match(Kind.IDENTIFIER);
		match(Kind.OP_LARROW);
		Source s = source();
		return new Statement_In(temp, temp, s);
		
	}
	
//	Sink ::= IDENTIFIER | KW_SCREEN  //ident must be file //TODO what is ident must be a file?
	Sink sink() throws SyntaxException{
		if(t.kind==Kind.IDENTIFIER || t.kind == Kind.KW_SCREEN ) {
			Token temp =t;
			if(t.kind==Kind.IDENTIFIER) {
				match(Kind.IDENTIFIER);
				return new Sink_Ident(temp,temp);
			}
			if(t.kind==KW_SCREEN) {
				match(Kind.KW_SCREEN);
				return new Sink_SCREEN(temp);
			}
						
		}
		return null;
	}
	
	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * @return 
	 * 
	 * @throws SyntaxException
	 */
Expression expression() throws SyntaxException {
		Expression condition = null;
		Expression trueExpression =null;
		Expression falseExpression =null;
		Token temp = t;
		if(expressionHS.contains(t.kind)) {
			condition=orExpression();
			if(t.kind==Kind.OP_Q) {
				match(Kind.OP_Q);
				trueExpression=expression();
				match(Kind.OP_COLON);
				falseExpression=expression();
				return new Expression_Conditional(temp, condition, trueExpression, falseExpression);
			}
			return condition;
		}
		else
			error("Expected Expression found something else!");
		return null;

	}
//	OrExpression ::= AndExpression   (  OP_OR  AndExpression)*
Expression orExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			Token op=null;
			e0=andExpression();
			while(t.kind==Kind.OP_OR) {
				op=t;
				match(Kind.OP_OR);
				e1=andExpression();
				e0=new Expression_Binary(temp, e0, op, e1);
			}
			return e0;
		}
		else 
			error("Expected Expression found something else!");
		return  null;
	}
	
	//AndExpression ::= EqExpression ( OP_AND  EqExpression )*
	Expression andExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			Token op=null;
			e0=eqExpression();
			while(t.kind==Kind.OP_AND) {
				op=t;
				match(Kind.OP_AND);
				e1=andExpression();
				e0=new Expression_Binary(temp, e0, op, e1);
			}
			return e0;
		}
		else 
			error("Expected Expression found something else!");
		return  null;
	}
	
//	EqExpression ::= RelExpression  (  (OP_EQ | OP_NEQ )  RelExpression )*
	Expression eqExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			Token op=null;
			e0=relExpression();
			while(t.kind==Kind.OP_EQ || t.kind==Kind.OP_NEQ) {
				op=t;
				if(t.kind==Kind.OP_EQ) {
					match(Kind.OP_EQ);
				}
				else if(t.kind==Kind.OP_NEQ) {
					match(Kind.OP_NEQ);
				}
				e1=relExpression();
				e0=new Expression_Binary(temp, e0, op, e1);
			}
			return e0;
		}
		else 
			error("Expected Expression found something else!");
		return  null;
	}
	
	
//	RelExpression ::= AddExpression (  ( OP_LT  | OP_GT |  OP_LE  | OP_GE )   AddExpression)*
	Expression relExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			Token op=null;
			e0=addExpression();
			while(t.kind==Kind.OP_LT || t.kind==Kind.OP_GT || t.kind==Kind.OP_LE || t.kind==Kind.OP_GE ) {
				op=t;
				if(t.kind==Kind.OP_LT) 
					match(Kind.OP_LT);
				else if(t.kind==Kind.OP_GT) 
					match(Kind.OP_GT);
				else if(t.kind==Kind.OP_LE) 
					match(Kind.OP_LE);
				else if(t.kind==Kind.OP_GE) 
					match(Kind.OP_GE);
				e1=addExpression();
				e0=new Expression_Binary(temp, e0, op, e1);
			}
			return e0;
		}
		else 
			error("Expected Expression found something else!");
		return  null;
	}
//	AddExpression ::= MultExpression   (  (OP_PLUS | OP_MINUS ) MultExpression )*
	Expression addExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			Token op=null;
			e0=multExpression();
			while(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS) {
				op=t;
				if(t.kind==Kind.OP_PLUS) 
					match(Kind.OP_PLUS);
				else if(t.kind==Kind.OP_MINUS) 
					match(Kind.OP_MINUS);
				e1=multExpression();
				e0=new Expression_Binary(temp, e0, op, e1);
			}
			return e0;
		}
	else 
		error("Expected Expression found something else!");
	return  null;
}
//	MultExpression := UnaryExpression ( ( OP_TIMES | OP_DIV  | OP_MOD ) UnaryExpression )*
	Expression multExpression() throws SyntaxException {
		if(expressionHS.contains(t.kind)) {
			Token temp=t;
			Expression e0=null;
			Expression e1=null;
			Token op=null;
			e0=unaryExpression();
			while(t.kind==Kind.OP_TIMES || t.kind==Kind.OP_DIV || t.kind==Kind.OP_MOD) {
				op=t;
				if(t.kind==Kind.OP_TIMES) 
					match(Kind.OP_TIMES);
				else if(t.kind==Kind.OP_DIV) 
					match(Kind.OP_DIV);
				else if(t.kind==Kind.OP_MOD) 
					match(Kind.OP_MOD);
				e1=unaryExpression();
				e0=new Expression_Binary(temp, e0, op, e1);
			}
			return e0;
		}
		else 
			error("Expected Expression found something else!");
		return  null;
	}
	
//	UnaryExpression ::= OP_PLUS UnaryExpression 
//    | OP_MINUS UnaryExpression 
//    | UnaryExpressionNotPlusMinus

	Expression unaryExpression() throws SyntaxException {
		Token temp=t;
		if(t.kind==Kind.OP_PLUS){
			match(Kind.OP_PLUS);
			Expression e = unaryExpression();
			return new Expression_Unary(temp, temp, e);
		}
		else if(t.kind==Kind.OP_MINUS) {
			match(Kind.OP_MINUS);
			Expression e = unaryExpression();
			return new Expression_Unary(temp, temp, e);
		}
		else if(unaryExpressionNotPlusMinusHS.contains(t.kind)) {
			return unaryExpressionNotPlusMinus();
		}
		else 
			error("Expected Expression found something else!");
		return null;
	}
	
//	UnaryExpressionNotPlusMinus ::=  OP_EXCL  UnaryExpression  | Primary 
//	| IdentOrPixelSelectorExpression | KW_x | KW_y | KW_r | KW_a | KW_X | KW_Y | KW_Z | KW_A | KW_R | KW_DEF_X | KW_DEF_Y
	Expression unaryExpressionNotPlusMinus() throws SyntaxException {
			Token temp = t;
			if(t.kind==Kind.OP_EXCL) {
				Token op_token=t;
				match(Kind.OP_EXCL);
				Expression e=unaryExpression();
				return new Expression_Unary(temp, op_token, e);
			}
			else if(primaryHS.contains(t.kind)) {
				return primary();
			}
			else if(t.kind==Kind.IDENTIFIER) {
				return identOrPixelSelectorExpression();
			}
			else if(t.kind==Kind.KW_x) {
				match(Kind.KW_x);
				return new Expression_PredefinedName(temp, Kind.KW_x);
			}
			else if(t.kind==Kind.KW_y) {
				match(Kind.KW_y);
				return new Expression_PredefinedName(temp,Kind.KW_y);
			}
			else if(t.kind==Kind.KW_r) {
				match(Kind.KW_r);
				return new Expression_PredefinedName(temp,Kind.KW_r);
			}
			else if(t.kind==Kind.KW_a) {
				match(Kind.KW_a);
				return new Expression_PredefinedName(temp,Kind.KW_a);
			}
			else if(t.kind==Kind.KW_X) {
				match(Kind.KW_X);
				return new Expression_PredefinedName(temp,Kind.KW_X);
			}
			else if(t.kind==Kind.KW_Y) {
				match(Kind.KW_Y);
				return new Expression_PredefinedName(temp,Kind.KW_Y);
			}
			else if(t.kind==Kind.KW_Z) {
				match(Kind.KW_Z);
				return new Expression_PredefinedName(temp,Kind.KW_Z);
			}
			else if(t.kind==Kind.KW_A) {
				match(Kind.KW_A);
				return new Expression_PredefinedName(temp,Kind.KW_A);
			}
			else if(t.kind==Kind.KW_R) {
				match(Kind.KW_R);
				return new Expression_PredefinedName(temp,Kind.KW_R);
			}
			else if(t.kind==Kind.KW_DEF_X) {
				match(Kind.KW_DEF_X);
				return new Expression_PredefinedName(temp,Kind.KW_DEF_X);
			}
			else if(t.kind==Kind.KW_DEF_Y) {
				match(Kind.KW_DEF_Y);
				return new Expression_PredefinedName(temp,Kind.KW_DEF_Y);
			}
			else 
				error("Expected Expression found something else!");
			return null;
	}
	
	
//	Primary ::= INTEGER_LITERAL | LPAREN Expression RPAREN | FunctionApplication | BOOLEAN_LITERAL
	Expression primary() throws SyntaxException {
		Token temp=t;
		if(t.kind==Kind.INTEGER_LITERAL) {
			match(Kind.INTEGER_LITERAL);
			return new Expression_IntLit(temp, temp.intVal());
		}
		else if(t.kind==Kind.LPAREN) {
			match(Kind.LPAREN);
			Expression e = expression();
			match(Kind.RPAREN);
			return e;
		}
		else if (t.kind==Kind.BOOLEAN_LITERAL) {
			match(Kind.BOOLEAN_LITERAL);
			return new Expression_BooleanLit(temp, Boolean.parseBoolean(temp.getText()));		
			}
		else if(functionNameHS.contains(t.kind)) {
			return functionApplication();
		}
		else 
			error("Expected Expression found something else!");
		return null;
	}
	
//	IdentOrPixelSelectorExpression::=  IDENTIFIER LSQUARE Selector RSQUARE   | IDENTIFIER
	Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token temp=t;
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			match(Kind.LSQUARE);
			Index index=selector();
			match(Kind.RSQUARE);
			return new Expression_PixelSelector(temp, temp, index);
		}
		return new Expression_Ident(temp, temp);		
	}
	
//	Lhs::=  IDENTIFIER ( LSQUARE LhsSelector RSQUARE   | ε )
	LHS lhs() throws SyntaxException{
		Token temp = t;
		Index index=null;
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			match(Kind.LSQUARE);
			index=lhsSelector();
			match(Kind.RSQUARE);
		}
		return new LHS(temp, temp, index);
	}

//	LhsSelector ::= LSQUARE  ( XySelector  | RaSelector  )   RSQUARE
	Index lhsSelector() throws SyntaxException{
		Index index=null;
		match(Kind.LSQUARE);
		if(t.kind==Kind.KW_x || t.kind==Kind.COMMA || t.kind==Kind.KW_y ||t.kind==Kind.KW_r ||t.kind==Kind.COMMA ||t.kind==Kind.KW_A) {
			if(t.kind==Kind.KW_x || t.kind==Kind.COMMA || t.kind==Kind.KW_y)
				index = xySelector();
			if(t.kind==Kind.KW_r ||t.kind==Kind.COMMA ||t.kind==Kind.KW_A)
				index= raSelector();
			match(Kind.RSQUARE);
		}
		else
			error("Wrong lhsSelector");

		return index;
	}
	
//	XySelector ::= KW_x COMMA KW_y
	Index xySelector() throws SyntaxException{
		Expression e0=null;
		Expression e1=null;
		Token temp = t;
		e0 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_x);
		match(Kind.COMMA);
		e1 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_y);
		return new Index(temp, e0, e1);
	}
	
	//RaSelector ::= KW_r COMMA KW_A
	Index raSelector() throws SyntaxException{
		Expression e0=null;
		Expression e1=null;
		Token temp = t;
		e0 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_r);
		match(Kind.COMMA);
		e1 = new Expression_PredefinedName(t, t.kind);
		match(Kind.KW_A);
		return new Index(temp, e0, e1);
	}
	
	
	//	Selector ::=  Expression COMMA Expression 
	Index selector() throws SyntaxException{
		Expression e0=null;
		Expression e1=null;
		Token temp = t;
		e0=expression();
		match(Kind.COMMA);
		e1=expression();
		return new Index(temp, e0, e1);
	}
	
//	FunctionApplication ::= FunctionName LPAREN Expression RPAREN  
//			| FunctionName  LSQUARE Selector RSQUARE 
	Expression_FunctionApp functionApplication() throws SyntaxException {
		Token temp=t;
		if(functionNameHS.contains(t.kind)) {
			functionName();
			if(t.kind==Kind.LPAREN) {
				match(Kind.LPAREN);
				Expression arg =expression();
				match(Kind.RPAREN);
				return new Expression_FunctionAppWithExprArg(temp, temp.kind, arg);
			}
			else if(t.kind==Kind.LSQUARE) {
				match(Kind.LSQUARE);
				Index arg=selector();
				match(Kind.RSQUARE);
				return new Expression_FunctionAppWithIndexArg(temp, temp.kind, arg);
			}
		}
		else 
			error("Expected Expression found something else!");
		return null;
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
