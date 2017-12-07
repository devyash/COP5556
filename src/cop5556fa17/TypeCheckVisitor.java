package cop5556fa17;
import cop5556fa17.Scanner.Token;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.SymbolTable;
import cop5556fa17.TypeUtils.Type;

import java.net.URL;

import cop5556fa17.Scanner.Kind;



public class TypeCheckVisitor implements ASTVisitor {
  SymbolTable st = new SymbolTable(); 

    @SuppressWarnings("serial")
    public static class SemanticException extends Exception {
      Token t;

      public SemanticException(Token t, String message) {
        super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
        this.t = t;
      }

    }   
    

  
  /**
   * The program name is only used for naming the class.  It does not rule out
   * variables with the same name.  It is returned for convenience.
   * 
   * @throws Exception 
   */
  @Override
  public Object visitProgram(Program program, Object arg) throws Exception {
    for (ASTNode node: program.decsAndStatements) {
      node.visit(this, arg);
    }
    return program.name;
  }

  @Override
  public Object visitDeclaration_Variable(
      Declaration_Variable declaration_Variable, Object arg)
      throws Exception {
    if(declaration_Variable.e != null)  declaration_Variable.e.visit(this, null);
    if(st.search(declaration_Variable.name) == null) {
      st.insert(declaration_Variable.name,declaration_Variable); 
      declaration_Variable.Type = TypeUtils.getType(declaration_Variable.type);
    }
    if(declaration_Variable.e != null && declaration_Variable.Type != declaration_Variable.e.Type)
        throw new SemanticException(declaration_Variable.firstToken,"REQUIRE if (Expression !=  ε)  Declaration_Variable.Type == Expression.Type, It was not equal");
    return declaration_Variable;
  }

  @Override
  public Object visitExpression_Binary(Expression_Binary expression_Binary,
      Object arg) throws Exception {
    if(expression_Binary.e1 != null)  expression_Binary.e1.visit(this, null);
    if(expression_Binary.e0 != null)  expression_Binary.e0.visit(this, null);
    if(expression_Binary.e0 != null && expression_Binary.e1 != null && expression_Binary.e0.Type != expression_Binary.e1.Type)
      throw new SemanticException(expression_Binary.firstToken, "REQUIRE: Expression0.Type == Expression1.Type, it was not equal");
    if((expression_Binary.op == Scanner.Kind.OP_EQ || expression_Binary.op == Scanner.Kind.OP_NEQ) ||(expression_Binary.op == Scanner.Kind.OP_GE || expression_Binary.op == Scanner.Kind.OP_GT || expression_Binary.op == Scanner.Kind.OP_LT || expression_Binary.op == Scanner.Kind.OP_LE) && expression_Binary.e0.Type == Type.INTEGER) 
      expression_Binary.Type = Type.BOOLEAN;
    else if((expression_Binary.op == Scanner.Kind.OP_AND || expression_Binary.op == Scanner.Kind.OP_OR) && (expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e0.Type == Type.BOOLEAN )) 
      expression_Binary.Type = expression_Binary.e0.Type;
    else if((expression_Binary.op == Scanner.Kind.OP_DIV || expression_Binary.op == Scanner.Kind.OP_MINUS || expression_Binary.op == Scanner.Kind.OP_PLUS || expression_Binary.op == Scanner.Kind.OP_MOD || expression_Binary.op == Scanner.Kind.OP_POWER || expression_Binary.op == Scanner.Kind.OP_TIMES) && expression_Binary.e0.Type == Type.INTEGER)
      expression_Binary.Type = Type.INTEGER;
    else {
      expression_Binary.Type = null;
      throw new SemanticException(expression_Binary.firstToken, "REQUIRE: Expression_Binary.Type !=null, it was null");
    }
    return expression_Binary;
  }

  @Override
  public Object visitExpression_Unary(Expression_Unary expression_Unary,
      Object arg) throws Exception {
    if(expression_Unary.e != null)
      expression_Unary.e.visit(this, null);
    if(expression_Unary.op == Scanner.Kind.OP_EXCL && (expression_Unary.e.Type == Type.BOOLEAN || expression_Unary.e.Type == Type.INTEGER))
      expression_Unary.Type = expression_Unary.e.Type;
    else if((expression_Unary.op == Scanner.Kind.OP_PLUS || expression_Unary.op == Scanner.Kind.OP_MINUS) && (expression_Unary.e.Type == Type.INTEGER))
      expression_Unary.Type = Type.INTEGER;
    else {
      expression_Unary.Type = null;
      throw new SemanticException(expression_Unary.firstToken, "REQUIRE:  Expression_ Unary.Type != null, it was null");
    }
    return expression_Unary;
  }

  @Override
  public Object visitIndex(Index index, Object arg) throws Exception {
    if(index.e0 != null) index.e0.visit(this, null);
    if(index.e1 != null) index.e1.visit(this, null);
    if(index.e0.Type != Type.INTEGER && index.e1.Type != Type.INTEGER) {
      throw new SemanticException(index.firstToken,"REQUIRE: Expression0.Type == INTEGER &&  Expression1.Type == INTEGER, It was not integer.");
    }
    if(index.e0.getClass() == Expression_PredefinedName.class && index.e1.getClass() == Expression_PredefinedName.class ) {
      Expression_PredefinedName pred0 = (Expression_PredefinedName)index.e0;
      Expression_PredefinedName pred1 = (Expression_PredefinedName)index.e1;
      index.setCartesian(!(pred0.kind == Kind.KW_r && pred1.kind == Kind.KW_a));
    }
    else{
    		index.setCartesian(true);
    }
    return index;
  }

  @Override
  public Object visitExpression_PixelSelector(
      Expression_PixelSelector expression_PixelSelector, Object arg)
      throws Exception {
    if(expression_PixelSelector.index != null) expression_PixelSelector.index.visit(this, null);
    if(st.search(expression_PixelSelector.name) == null) 
      throw new SemanticException(expression_PixelSelector.firstToken, " REQUIRE:  Expression_PixelSelector.Type != null, it was null");
    if((st.search(expression_PixelSelector.name)).Type == Type.IMAGE) expression_PixelSelector.Type = Type.INTEGER;
    else if(expression_PixelSelector.index == null) 
      expression_PixelSelector.Type = st.search(expression_PixelSelector.name).Type;
    else {
      expression_PixelSelector.Type = null;
      throw new SemanticException(expression_PixelSelector.firstToken, "REQUIRE:  Expression_PixelSelector.Type != null, it was null");
    }
    return expression_PixelSelector;
  }

  @Override
  public Object visitExpression_Conditional(
      Expression_Conditional expression_Conditional, Object arg)
      throws Exception {
    if (expression_Conditional.condition != null) expression_Conditional.condition.visit(this, null);
    if (expression_Conditional.trueExpression != null) expression_Conditional.trueExpression.visit(this, null);
    if (expression_Conditional.falseExpression != null) expression_Conditional.falseExpression.visit(this, null);
    if (expression_Conditional.condition.Type == Type.BOOLEAN && expression_Conditional.trueExpression.Type == expression_Conditional.falseExpression.Type)
      expression_Conditional.Type = expression_Conditional.trueExpression.Type; //Expression_Conditional.Type <= Expressiontrue.Type
    else 
      throw new SemanticException(expression_Conditional.firstToken, "REQUIRE:  Expressioncondition.Type == BOOLEAN && Expressiontrue.Type ==Expressionfalse.Type, it was not of the same type");
    return expression_Conditional;
  }

  @Override
  public Object visitDeclaration_Image(Declaration_Image declaration_Image,
      Object arg) throws Exception {
    if (declaration_Image.source != null) declaration_Image.source.visit(this, null);
    if (declaration_Image.xSize != null)  declaration_Image.xSize.visit(this, null);
    if (declaration_Image.ySize != null)  declaration_Image.ySize.visit(this, null);
    if (st.search(declaration_Image.name) == null) {
      st.insert(declaration_Image.name, declaration_Image);
      declaration_Image.Type = Type.IMAGE;
      if (declaration_Image.xSize != null && !(declaration_Image.ySize != null && declaration_Image.xSize.Type == Type.INTEGER && declaration_Image.ySize.Type == Type.INTEGER))
          throw new SemanticException(declaration_Image.firstToken, "REQUIRE if xSize != ε then ySize != ε && xSize.Type == INTEGER && ySize.type == INTEGER");
    } else {
      throw new SemanticException(declaration_Image.firstToken, "There is already a Decleration Image.");
    }
    return declaration_Image;
  }

  @Override
//  Source_StringLIteral.Type <= if isValidURL(fileOrURL) then URL else FILE
  public Object visitSource_StringLiteral(
      Source_StringLiteral source_StringLiteral, Object arg)
      throws Exception {
    try {
      new URL(source_StringLiteral.fileOrUrl); //throws error if invalid URL
      source_StringLiteral.Type = Type.URL;
    } catch (Exception e) {
      source_StringLiteral.Type = Type.FILE;
    }
    return source_StringLiteral;
  }

  @Override
  public Object visitSource_CommandLineParam(
      Source_CommandLineParam source_CommandLineParam, Object arg)
      throws Exception {
    if (source_CommandLineParam.paramNum != null) source_CommandLineParam.paramNum.visit(this, null);
    source_CommandLineParam.Type = source_CommandLineParam.paramNum.Type;
    if (source_CommandLineParam.Type != Type.INTEGER)   throw new SemanticException(source_CommandLineParam.firstToken, "REQUIRE:  Source_CommandLineParam .Type == INTEGER, it was not integer");
    return source_CommandLineParam;
  }

  @Override
  public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
      throws Exception {
    if (st.search(source_Ident.name) == null) throw new SemanticException(source_Ident.firstToken, "Source_Ident == null, it cannot be null.");
    else {
      source_Ident.Type = (st.search(source_Ident.name)).Type;
      if (!(source_Ident.Type == Type.FILE || source_Ident.Type == Type.URL)) throw new SemanticException(source_Ident.firstToken, "REQUIRE:  Source_Ident.Type == FILE || Source_Ident.Type == URL, it was something else");
    }
    return source_Ident;
  }

//  Declaration_SourceSink  ::= Type name  Source
//            REQUIRE:  symbolTable.searchType(name) = Ʇ
//           symbolTable.insert(name, Declaration_SourceSink)
//Declaration_SourceSink.Type <= Type
//           REQUIRE Source.Type == Declaration_SourceSink.Type

//FROM HERE
  @Override
  public Object visitDeclaration_SourceSink(
      Declaration_SourceSink declaration_SourceSink, Object arg)
      throws Exception {
    if (declaration_SourceSink.source != null) declaration_SourceSink.source.visit(this, null);
    if (st.search(declaration_SourceSink.name) != null) {
      throw new SemanticException(declaration_SourceSink.firstToken, "  REQUIRE:  symbolTable.searchType(name) = null, it is already declared");
    }
    else {
      st.insert(declaration_SourceSink.name, declaration_SourceSink);
      if (declaration_SourceSink.type == Kind.KW_file) 
        declaration_SourceSink.Type = Type.FILE;
      else if (declaration_SourceSink.type == Kind.KW_url) //Source_StringLIteral.Type <= if isValidURL(fileOrURL) then URL else FILE
        declaration_SourceSink.Type = Type.URL;
      else 
        throw new SemanticException(declaration_SourceSink.firstToken,"REQUIRE:  Declaration_SourceSink.Type == FILE || Declaration_SourceSink.Type == URL");
      if (declaration_SourceSink.Type != declaration_SourceSink.source.Type && declaration_SourceSink.source.Type == null ) 
        throw new SemanticException(declaration_SourceSink.firstToken, "REQUIRE Source.Type == Declaration_SourceSink.Type, it does not match");
    } 

    return null;

  }

  @Override
  public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
      Object arg) throws Exception {
    expression_IntLit.Type = Type.INTEGER;
    return expression_IntLit;
  }

  @Override
  public Object visitExpression_FunctionAppWithExprArg(
      Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
      Object arg) throws Exception {
    if (expression_FunctionAppWithExprArg.arg != null) expression_FunctionAppWithExprArg.arg.visit(this, null);
    if (expression_FunctionAppWithExprArg.arg.Type == Type.INTEGER) 
      expression_FunctionAppWithExprArg.Type = Type.INTEGER;
    else 
      throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "REQUIRE:  Expression.Type == INTEGER, it is not integer");
    return expression_FunctionAppWithExprArg;
  }

  @Override
  public Object visitExpression_FunctionAppWithIndexArg(
      Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
      Object arg) throws Exception {
    if (expression_FunctionAppWithIndexArg.arg != null) expression_FunctionAppWithIndexArg.arg.visit(this, null);
    expression_FunctionAppWithIndexArg.Type = Type.INTEGER;
    return null;
  }

  @Override
  public Object visitExpression_PredefinedName(
      Expression_PredefinedName expression_PredefinedName, Object arg)
      throws Exception {
    expression_PredefinedName.Type = Type.INTEGER;
    return expression_PredefinedName;
  }

//  Statement_Out ::= name Sink
//      Statement_Out.Declaration <= name.Declaration
//                   REQUIRE:  (name.Declaration != null)
//                  REQUIRE:   ((name.Type == INTEGER || name.Type == BOOLEAN) && Sink.Type == SCREEN)
//                        ||  (name.Type == IMAGE && (Sink.Type ==FILE || Sink.Type == SCREEN))

  @Override
  public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
      throws Exception {
    if (statement_Out.sink != null) statement_Out.sink.visit(this, null);
    if (st.search(statement_Out.name) == null) throw new SemanticException(statement_Out.firstToken, "REQUIRE:  (name.Declaration != null), it is null");
    statement_Out.setDec(st.search(statement_Out.name));
    Declaration d = st.search(statement_Out.name);
    if (!(((d.Type == Type.INTEGER || d.Type == Type.BOOLEAN) && statement_Out.sink.Type == Type.SCREEN) || (d.Type == Type.IMAGE && (statement_Out.sink.Type == Type.FILE || statement_Out.sink.Type == Type.SCREEN)))) {
      throw new SemanticException(statement_Out.firstToken, "REQUIRE:   ((name.Type == INTEGER || name.Type == BOOLEAN) && Sink.Type == SCREEN) ||  (name.Type == IMAGE && (Sink.Type ==FILE || Sink.Type == SCREEN))");
    }
    return statement_Out;
  }

//  Statement_In ::= name Source
//      Statement_In.Declaration <= name.Declaration
//                   REQUIRE:  (name.Declaration != null) & (name.type == Source.type)
  @Override
  public Object visitStatement_In(Statement_In statement_In, Object arg)
      throws Exception {
    if (statement_In.source != null)  statement_In.source.visit(this, null);
    statement_In.setDec(st.search(statement_In.name));
    return statement_In;
  }

//  REQUIRE:  LHS.Type == Expression.Type
//  StatementAssign.isCartesian <= LHS.isCartesian

  @Override
  public Object visitStatement_Assign(Statement_Assign statement_Assign,
      Object arg) throws Exception {
    if (statement_Assign.lhs != null) statement_Assign.lhs.visit(this, null);
    if (statement_Assign.e != null) statement_Assign.e.visit(this, null);
    statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);
    //TODO
    if (!(statement_Assign.lhs.Type == statement_Assign.e.Type || (statement_Assign.lhs.Type == Type.IMAGE &&statement_Assign.e.Type == Type.INTEGER) )) throw new SemanticException(statement_Assign.firstToken, "REQUIRE:  LHS.Type == Expression.Type");
    return statement_Assign;
  }
  
//  LHS.Declaration <= symbolTable.searchDec(name)
//            LHS.Type <= LHS.Declaration.Type
//            LHS.isCarteisan <= Index.isCartesian


  @Override
  public Object visitLHS(LHS lhs, Object arg) throws Exception {
    if (lhs.index != null)  lhs.index.visit(this, null);
    if (st.search(lhs.name) == null)  throw new SemanticException(lhs.firstToken, "LHS cannot be null!");
    else {
        lhs.declaration = st.search(lhs.name);
        lhs.Type = st.search(lhs.name).Type;
      if (lhs.index != null)
        lhs.isCartesian = lhs.index.isCartesian();
      else
        lhs.isCartesian = false;
    }
    return lhs;
  }

  @Override
  public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
      throws Exception {
    sink_SCREEN.Type = Type.SCREEN;
    return sink_SCREEN;
  }

//  Sink_Ident ::= name
//      Sink_Ident.Type <= symbolTable.searchType(name) 
//                 REQUIRE:  Sink_Ident.Type  == FILE

  @Override
  public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
      throws Exception {
    if (st.search(sink_Ident.name) == null) throw new SemanticException(sink_Ident.firstToken, "Sink_Ident cannot be null!");
    else if(st.search(sink_Ident.name) != null) {
      sink_Ident.Type = (st.search(sink_Ident.name)).Type;
      if (sink_Ident.Type != Type.FILE)
        throw new SemanticException(sink_Ident.firstToken, "REQUIRE:  Sink_Ident.Type  == FILE, it was not FILE type.");
    }
    return sink_Ident;
  }

//  Expression_BooleanLit.Type <= BOOLEAN
  @Override
  public Object visitExpression_BooleanLit(
      Expression_BooleanLit expression_BooleanLit, Object arg)
      throws Exception {
    expression_BooleanLit.Type = Type.BOOLEAN;
    return expression_BooleanLit;
  }

  @Override
  public Object visitExpression_Ident(Expression_Ident expression_Ident,
      Object arg) throws Exception {
    if (st.search(expression_Ident.name) == null)
      throw new SemanticException(expression_Ident.firstToken, "Expression_Ident cannot be null!");
    else 
      expression_Ident.Type = (st.search(expression_Ident.name)).Type;
    return expression_Ident;
  }
}
