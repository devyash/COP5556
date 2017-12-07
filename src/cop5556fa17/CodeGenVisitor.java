package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Scanner.Kind;


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
	FieldVisitor fv;

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
//		CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectively
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//defining it local to make it easier to handle variables
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		//defining it static as it will become easier to access.
		cw.visitField(ACC_STATIC, "x", "I", null, 0).visitEnd();
		cw.visitField(ACC_STATIC, "y", "I", null, 0).visitEnd();
	

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

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		if(declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
		}
		if(declaration_Variable.Type == Type.BOOLEAN) {
			cw.visitField(ACC_STATIC, declaration_Variable.name, "Z", null, new Boolean(false)).visitEnd();
			if(declaration_Variable.e != null) {
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "Z");
			}
		}
		else if(declaration_Variable.Type == Type.INTEGER) {
				cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, new Integer(0)).visitEnd();
				if(declaration_Variable.e != null) {
					mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "I");
				}
		}
		else
			throw new UnsupportedOperationException(); // not sure
		
		return null;
	}

	//code it
	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		Label first = new Label(), second = new Label();
		
		if(expression_Binary.op == Kind.OP_AND) {
			mv.visitInsn(IAND);
		}
		else if(expression_Binary.op == Kind.OP_DIV) {
			mv.visitInsn(IDIV);
		}
		else if(expression_Binary.op == Kind.OP_EQ) {
			mv.visitJumpInsn(IF_ICMPEQ, first);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, second);
			mv.visitLabel(first);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(second);
		}
		else if(expression_Binary.op == Kind.OP_GE) {
			mv.visitJumpInsn(IF_ICMPGE, first);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, second);
			mv.visitLabel(first);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(second);
		}
		else if(expression_Binary.op == Kind.OP_GT) {
			mv.visitJumpInsn(IF_ICMPGT, first);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, second);
			mv.visitLabel(first);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(second);
		}
		else if(expression_Binary.op == Kind.OP_LE) {
			mv.visitJumpInsn(IF_ICMPLE, first);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, second);
			mv.visitLabel(first);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(second);
		}
		else if(expression_Binary.op == Kind.OP_LT) {
			mv.visitJumpInsn(IF_ICMPLT, first);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, second);
			mv.visitLabel(first);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(second);
		}
		else if(expression_Binary.op == Kind.OP_MINUS) {
			mv.visitInsn(ISUB);
		}
		else if(expression_Binary.op == Kind.OP_MOD) {
			mv.visitInsn(IREM);
		}
		else if(expression_Binary.op == Kind.OP_NEQ) {
			mv.visitJumpInsn(IF_ICMPNE, first);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, second);
			mv.visitLabel(first);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(second);
		}
		else if(expression_Binary.op == Kind.OP_OR) {
			mv.visitInsn(IOR);
		}
		else if(expression_Binary.op == Kind.OP_PLUS) {
			mv.visitInsn(IADD);
		}
		else if(expression_Binary.op == Kind.OP_TIMES) {
			mv.visitInsn(IMUL);
		}
		else
			throw new UnsupportedOperationException();

//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.Type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		expression_Unary.e.visit(this, arg);
		if(expression_Unary.op == Kind.OP_MINUS) {
			mv.visitInsn(INEG);
		}
		else if(expression_Unary.op == Kind.OP_EXCL) {
			if(expression_Unary.Type == Type.INTEGER) {
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
			}
			else if(expression_Unary.Type == Type.BOOLEAN) {
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
			}
//			else
//				throw new UnsupportedOperationException();
		}
		else if(expression_Unary.op == Kind.OP_PLUS) {
		
		}
		else
			throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.Type);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.e0.visit(this, arg);
		index.e1.visit(this, arg);
		
		if(index.isCartesian()) {
		}
		else {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		mv.visitFieldInsn(GETSTATIC, className,expression_PixelSelector.name , ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
//		throw new UnsupportedOperationException();
		expression_Conditional.condition.visit(this,arg);
		Label first = new Label(), second = new Label();
		mv.visitJumpInsn(IFNE, first);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, second);
		mv.visitLabel(first);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitLabel(second);		
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.Type);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null).visitEnd();
		if(declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);
			if(declaration_Image.xSize == null && declaration_Image.ySize == null) {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			else {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		}
		else {
			if(declaration_Image.xSize == null && declaration_Image.ySize == null) {
				mv.visitLdcInsn(256);
				mv.visitLdcInsn(256);
			}
			else {
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			}
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		}
		return null;
	}
	  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
//		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6 - check
		cw.visitField(ACC_STATIC, declaration_SourceSink.name, ImageSupport.StringDesc, null, null).visitEnd();
		if(declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);
		}
		
//		if(declaration_SourceSink.source != null) { //TODO: might not need it
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name,  ImageSupport.StringDesc);
//		}
		
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		mv.visitLdcInsn(expression_IntLit.value);
//		CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		
		if(expression_FunctionAppWithExprArg.function == Kind.KW_abs) {
			mv.visitMethodInsn(INVOKEVIRTUAL, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		else if(expression_FunctionAppWithExprArg.function == Kind.KW_log) {
			mv.visitMethodInsn(INVOKEVIRTUAL, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		
		if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_x) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
		}
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_y) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		else if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_r) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		}
		else{
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		
		if(expression_PredefinedName.kind == Kind.KW_DEF_X || expression_PredefinedName.kind == Kind.KW_DEF_Y)
			mv.visitLdcInsn(256);
		else if (expression_PredefinedName.kind == Kind.KW_Z) {
			mv.visitLdcInsn(0xFFFFFF);
		} else if (expression_PredefinedName.kind == Kind.KW_x) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
		} else if (expression_PredefinedName.kind == Kind.KW_y) {
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
		} else if (expression_PredefinedName.kind == Kind.KW_X) {
			mv.visitFieldInsn(GETSTATIC, className, (String) arg, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
		} else if (expression_PredefinedName.kind == Kind.KW_Y) {
			mv.visitFieldInsn(GETSTATIC, className, (String) arg, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
		} else if (expression_PredefinedName.kind == Kind.KW_r) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		} else if (expression_PredefinedName.kind == Kind.KW_a) {
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		} else if (expression_PredefinedName.kind == Kind.KW_R) {
			mv.visitFieldInsn(GETSTATIC, className, (String)arg, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(GETSTATIC, className, (String)arg, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
		} else if (expression_PredefinedName.kind == Kind.KW_A) {
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, className, (String)arg, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
		}
		
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
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
		else if(statement_Out.getDec().Type == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().Type);
			statement_Out.sink.visit(this, arg);
		}	
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
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		if(statement_In.source != null) {
			statement_In.source.visit(this, arg);
		}
		if(statement_In.getDec().Type == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");	
		} else if(statement_In.getDec().Type == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");	
		} else if(statement_In.getDec().Type == Type.IMAGE) {
			Declaration_Image d = (Declaration_Image) statement_In.getDec();
			if( d.xSize == null && d.ySize == null ) {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			} else {
				d.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				d.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}
		
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, d.name, ImageSupport.ImageDesc);
		}
	
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if(statement_Assign.lhs.Type == Type.IMAGE) {
			Label l1 = new Label(), l2 = new Label(), l3 = new Label(), l4 = new Label();
			mv.visitInsn(ICONST_0);
			mv.visitInsn(DUP);
			mv.visitLabel(l1);
			mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitJumpInsn(IF_ICMPGE, l4);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(DUP);
			mv.visitLabel(l2);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitJumpInsn(IF_ICMPGE, l3);
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l3);
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			mv.visitJumpInsn(GOTO, l1);
			mv.visitLabel(l4);
		}
		else if(statement_Assign.lhs.Type == Type.INTEGER || statement_Assign.lhs.Type == Type.BOOLEAN) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		return null;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
//		if(lhs.index != null) {
//			lhs.index.visit(this, arg);
//		}
		if(lhs.Type == Type.BOOLEAN) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		}
		else if(lhs.Type == Type.INTEGER) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		}
		else if (lhs.Type == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		}
		else
			throw new UnsupportedOperationException();
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception
	{
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception 
	{
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name,  "Ljava/lang/String;");
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		mv.visitLdcInsn(expression_BooleanLit.value);
//		CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(expression_Ident.Type == Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		}
		else if(expression_Ident.Type == Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		}
		else
			throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.Type);
		return null;
	}

}
