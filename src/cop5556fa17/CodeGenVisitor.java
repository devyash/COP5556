package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

  /**
   * All methods and variable static.
   */


  /**
   * @param DEVEL
   *            used as parameter to genPrint and genPrintTOS
   * @param GRADE
   *            used as parameter to genPrint and genPrintTOS
   * @param sourceFileName
   *            name of source file, may be null.
   */
  public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
    super();
    this.DEVEL = DEVEL;
    this.GRADE = GRADE;
    this.sourceFileName = sourceFileName;
  }

  ClassWriter cw;
  String className;
  String classDesc;
  String sourceFileName;

  MethodVisitor mv; // visitor of method currently under construction

  /** Indicates whether genPrint and genPrintTOS should generate code. */
  final boolean DEVEL;
  final boolean GRADE;
  


  @Override
  public Object visitProgram(Program program, Object arg) throws Exception {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    className = program.name;  
    classDesc = "L" + className + ";";
    String sourceFileName = (String) arg;
    cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
    cw.visitSource(sourceFileName, null);
    // create main method
    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    // initialize
    mv.visitCode();   
    //add label before first instruction
    Label mainStart = new Label();
    mv.visitLabel(mainStart);   
    // if GRADE, generates code to add string to log
    CodeGenUtils.genLog(GRADE, mv, "entering main");

    // visit decs and statements to add field to class
    //  and instructions to main method, respectivley
    ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
    for (ASTNode node : decsAndStatements) {
      node.visit(this, arg);
    }

    //generates code to add string to log
    CodeGenUtils.genLog(GRADE, mv, "leaving main");
    
    //adds the required (by the JVM) return statement to main
    mv.visitInsn(RETURN);
    
    //adds label at end of code
    Label mainEnd = new Label();
    mv.visitLabel(mainEnd);
    
    //handles parameters and local variables of main. Right now, only args
    mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

    //Sets max stack size and number of local vars.
    //Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
    //asm will calculate this itself and the parameters are ignored.
    //If you have trouble with failures in this routine, it may be useful
    //to temporarily set the parameter in the ClassWriter constructor to 0.
    //The generated classfile will not be correct, but you will at least be
    //able to see what is in it.
    mv.visitMaxs(0, 0);
    
    //terminate construction of main method
    mv.visitEnd();
    
    //terminate class construction
    cw.visitEnd();

    //generate classfile as byte array and return
    return cw.toByteArray();
  }

////  Declaration_Variable ::=  Type name (Expression | ε )
//  Add field, name, as static member of class.  
//  If there is an expression, generate code to evaluate it and store the results in the field.
//  See comment about this below.

  @Override
  public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
    if(declaration_Variable.e != null) 
      declaration_Variable.e.visit(this, arg);
    String type = "";
    if(declaration_Variable.Type == Type.INTEGER) {
      type = "I";
      cw.visitField(ACC_STATIC, declaration_Variable.name, type, null, new Integer(0)).visitEnd();
    }
    else if(declaration_Variable.Type == Type.BOOLEAN) {
      type = "Z";
      cw.visitField(ACC_STATIC, declaration_Variable.name, type, null, new Boolean(false)).visitEnd();
    }
    else
      throw new UnsupportedOperationException(); // TBH in assignment 6
    if(declaration_Variable.e != null) 
      mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, type);
    return null;
  }
  
//  Expression_Binary ::= Expression0 op Expression1
//      Generate code to evaluate the expression and leave the value on top of the stack.
//      Visiting the nodes for Expression0 and Expression1  will generate code to leave those values on the stack.  Then just generate code to perform the op.

  @Override
  public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
    expression_Binary.e1.visit(this, arg);
//    TODO:Visit e2 
    expression_Binary.e0.visit(this, arg);
    Label l1 = new Label();
    Label l2 = new Label();
    if(expression_Binary.op == Kind.OP_MINUS) mv.visitInsn(ISUB);
    else if(expression_Binary.op == Kind.OP_PLUS) mv.visitInsn(IADD);
    else if(expression_Binary.op == Kind.OP_TIMES)  mv.visitInsn(IMUL);
    else if(expression_Binary.op == Kind.OP_MOD)  mv.visitInsn(IREM);
    else if(expression_Binary.op == Kind.OP_DIV)  mv.visitInsn(IDIV);
    else if(expression_Binary.op == Kind.OP_OR) mv.visitInsn(IOR);
    else if(expression_Binary.op == Kind.OP_AND)  mv.visitInsn(IAND);
    else if(expression_Binary.op == Kind.OP_NEQ){
      mv.visitJumpInsn(IF_ICMPNE, l1);
      mv.visitLdcInsn(new Boolean(false));
      mv.visitJumpInsn(GOTO, l2);
      mv.visitLabel(l1);
      mv.visitLdcInsn(new Boolean(true));
      mv.visitLabel(l2);
    }
    else if(expression_Binary.op == Kind.OP_EQ){
      mv.visitJumpInsn(IF_ICMPEQ, l1);
      mv.visitLdcInsn(new Boolean(false));
      mv.visitJumpInsn(GOTO, l2);
      mv.visitLabel(l1);
      mv.visitLdcInsn(new Boolean(true));
      mv.visitLabel(l2);
    }
    else if(expression_Binary.op == Kind.OP_GT){ 
      mv.visitJumpInsn(IF_ICMPGT, l1);
      mv.visitLdcInsn(new Boolean(false));
      mv.visitJumpInsn(GOTO, l2);
      mv.visitLabel(l1);
      mv.visitLdcInsn(new Boolean(true));
      mv.visitLabel(l2);
    }
    else if(expression_Binary.op == Kind.OP_LE){ 
      mv.visitJumpInsn(IF_ICMPLE, l1);
      mv.visitLdcInsn(new Boolean(false));
      mv.visitJumpInsn(GOTO, l2);
      mv.visitLabel(l1);
      mv.visitLdcInsn(new Boolean(true));
      mv.visitLabel(l2);
    }
    else if(expression_Binary.op == Kind.OP_LT){
      mv.visitJumpInsn(IF_ICMPLT, l1);
      mv.visitLdcInsn(new Boolean(false));
      mv.visitJumpInsn(GOTO, l2);
      mv.visitLabel(l1);
      mv.visitLdcInsn(new Boolean(true));
      mv.visitLabel(l2);
    }
    else throw new UnsupportedOperationException();
    CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.Type);
    return null;
  }

  @Override
  public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
	  if(expression_Unary.e != null) 
		  expression_Unary.e.visit(this, arg);
	  if(expression_Unary.op == Kind.OP_EXCL) {
		  if(expression_Unary.Type == Type.BOOLEAN)	{
			  mv.visitInsn(ICONST_1);
			  mv.visitInsn(IXOR);
		  }
		  else if(expression_Unary.Type == Type.INTEGER) {
		    mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
		    mv.visitInsn(IXOR);
		  }
		}
	  else if(expression_Unary.op == Kind.OP_MINUS)
		  mv.visitInsn(INEG);
	  else if(expression_Unary.op != Kind.OP_PLUS) 
		  throw new UnsupportedOperationException();
	  CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.Type);
	  return null;
  }

  // generate code to leave the two values on the stack
  @Override
  public Object visitIndex(Index index, Object arg) throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }

  @Override
  public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
      throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }

//  Generate code to evaluate the Expressioncondition and depending on its
//    Value, to leave the value of either Expressiontrue  or Expressionfalse on top of the stack.
//    Hint:  you will need to use labels,  a conditional instruction, and goto.

  @Override
  public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
      throws Exception {
	  if(expression_Conditional.condition != null)  expression_Conditional.condition.visit(this,arg);
	  Label l1 = new Label();
	  Label l2 = new Label();
	  mv.visitJumpInsn(IFNE, l1);
	  expression_Conditional.falseExpression.visit(this, arg);
	  mv.visitJumpInsn(GOTO, l2);
	  mv.visitLabel(l1);
	  expression_Conditional.trueExpression.visit(this, arg);
	  mv.visitLabel(l2);    
	  CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.Type);
	  return null;
  }


  @Override
  public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }
  
  
  @Override
  public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }

  
//  Generate code to evaluate the expression and use aaload to read the element from the command line array using the expression value as the index.  The command line array is the String[] args param passed to main.
//
  @Override
  public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
      throws Exception {
    mv.visitVarInsn(ALOAD, 0);
    source_CommandLineParam.paramNum.visit(this, arg);
    mv.visitInsn(AALOAD);
    return null;
  }

  @Override
  public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }


  @Override
  public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
      throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }
  

//  Generate code to leave constant on stack.
  @Override
  public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
    mv.visitLdcInsn(expression_IntLit.value);
    CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
    return null;
  }

  @Override
  public Object visitExpression_FunctionAppWithExprArg(
      Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }

  @Override
  public Object visitExpression_FunctionAppWithIndexArg(
      Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }

  @Override
  public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
      throws Exception {
    // TODO HW6
    throw new UnsupportedOperationException();
  }

  /** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
   * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
   */
//  For INTEGERS and BOOLEANS, the only “sink” is the screen, so generate code to print to the console here.  Use java.io.PrintStream .println.  This is a virtual method, you can use the static field PrintStream “out” from class java.lang.System as the object. 
  @Override
  public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
    // TODO HW6 remaining cases
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    if(statement_Out.getDec().Type == Type.INTEGER) {
      mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
      CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().Type);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }
    else if(statement_Out.getDec().Type == Type.BOOLEAN) {
      mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
      CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().Type);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
    }
    else
      throw new UnsupportedOperationException();
    return null;
  }

  /**
   * Visit source to load rhs, which will be a String, onto the stack
   * 
   *  In HW5, you only need to handle INTEGER and BOOLEAN
   *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
   *  to convert String to actual type. 
   *  
   *  TODO HW6 remaining types
   */
//  Generate code to get value from the source and store it in variable name.
//  For Assignment 5, the only source that needs to be handled is the command line.
//
//Visit source to leave string representation of the value on top of stack
//               Convert to a value of correct type:  If name.type == INTEGER generate code to invoke
//Java.lang.Integer.parseInt.   If BOOLEAN, invoke java/lang/Boolean.parseBoolean

  @Override
  public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
    statement_In.source.visit(this, arg);
    String type;
    if(statement_In.getDec().Type == Type.BOOLEAN) {
    	  type = "Z";
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
      mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, type);  
    }
    else if(statement_In.getDec().Type == Type.INTEGER) {
  	  type = "I";
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
      mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, type);  
    }
    else
      throw new UnsupportedOperationException();
    return null;
  }

  
  /**
   * In HW5, only handle INTEGER and BOOLEAN types.
   */
//  Statement_Assign ::=  LHS  Expression
//      REQUIRE:  LHS.Type == Expression.Type
//      StatementAssign.isCartesian <= LHS.isCartesian

  @Override
  public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
    statement_Assign.e.visit(this, arg);
    statement_Assign.lhs.visit(this, arg);
    return null;
  }

  /**
   * In HW5, only handle INTEGER and BOOLEAN types.
   */
//  LHS ::= name Index
//  If LHS.Type  is INTEGER or BOOLEAN, generate code to 
//    store the value on top of the stack in variable name.
//TODO Assignment 6:  handle case where LHS.Type is IMAGE
  @Override
  public Object visitLHS(LHS lhs, Object arg) throws Exception {
    if(lhs.index != null)	lhs.index.visit(this, arg);
    String type;
    if(lhs.Type == Type.BOOLEAN) {
    		type = "Z";
    		mv.visitFieldInsn(PUTSTATIC, className, lhs.name, type);
    }
    else if(lhs.Type == Type.INTEGER) {
		type = "I";
    		mv.visitFieldInsn(PUTSTATIC, className, lhs.name, type);
    }
    else if(lhs.Type == Type.IMAGE) {
    		//TODO
    }
    else
      throw new UnsupportedOperationException();
    return null;
  }
  

  @Override
  public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
    //TODO HW6
    throw new UnsupportedOperationException();
  }

  @Override
  public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
    //TODO HW6
    throw new UnsupportedOperationException();
  }

//  Expression_BooleanLit ::=  value
//      Generate code to leave the value of the literal on top of the stack
  @Override
  public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
    mv.visitLdcInsn(expression_BooleanLit.value);
    CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
    return null;
  }

//  Expression_Ident  ::=   name
//      Generate code to get the value of the variable and leave it on top of the stack.
  
  @Override
  public Object visitExpression_Ident(Expression_Ident expression_Ident,
      Object arg) throws Exception {
	  String type;
	  if(expression_Ident.Type == Type.INTEGER) type = "I";
	  else if(expression_Ident.Type == Type.BOOLEAN)	type = "Z";
	  else	throw new UnsupportedOperationException();
	  mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, type);
	  CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.Type);
	  return null;
  }

}
