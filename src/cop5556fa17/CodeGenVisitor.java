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
        //  and instructions to main method, respectivley
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
        
        //custom code
        //handles parameters and local variables of main. Right now, only args
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
        cw.visitField(ACC_STATIC, "x", "I", null, 0).visitEnd();
        cw.visitField(ACC_STATIC, "y", "I", null, 0).visitEnd();
        cw.visitField(ACC_STATIC, "r", "I", null, new Integer(0)).visitEnd();
        cw.visitField(ACC_STATIC, "a", "I", null, new Integer(0)).visitEnd();
        cw.visitField(ACC_STATIC, "Z", "I", null, new Integer(16777215)).visitEnd();
        cw.visitField(ACC_STATIC, "X", "I", null, 0).visitEnd();
        cw.visitField(ACC_STATIC, "Y", "I", null, 0).visitEnd();
        cw.visitField(ACC_STATIC, "R", "I", null, new Integer(0)).visitEnd();
        cw.visitField(ACC_STATIC, "A", "I", null, new Integer(0)).visitEnd();
        cw.visitField(ACC_STATIC, "DEF_X", "I", null, new Integer(256)).visitEnd();
        cw.visitField(ACC_STATIC, "DEF_Y", "I", null, new Integer(256)).visitEnd();
        
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
        String type;
        if(declaration_Variable.e != null)  declaration_Variable.e.visit(this, arg);
      if(declaration_Variable.Type == Type.BOOLEAN) {
        Boolean value = new Boolean(false);
        type = "Z";
        cw.visitField(ACC_STATIC, declaration_Variable.name, type, null, value).visitEnd();
        }
      else if(declaration_Variable.Type == Type.INTEGER) {
        Integer value =  new Integer(0);
        type = "I";
        cw.visitField(ACC_STATIC, declaration_Variable.name, type, null, value).visitEnd();
        }
        else
          throw new UnsupportedOperationException(); // not sure
        if(declaration_Variable.e != null)  mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, type);
        return null;
    }
    
    @Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
    	  if(expression_Binary.e0 != null)	expression_Binary.e0.visit(this, arg);
    		if(expression_Binary.e1 != null)	expression_Binary.e1.visit(this, arg);
		Label l1 = new Label();
		Label l2 = new Label();
		int opcode;
		if(expression_Binary.op == Kind.OP_AND && (expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(IAND);
		else if(expression_Binary.op == Kind.OP_DIV && (expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(IDIV);
		else if(expression_Binary.op == Kind.OP_OR && (expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(IOR);
		else if(expression_Binary.op == Kind.OP_PLUS &&(expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(IADD);
		else if(expression_Binary.op == Kind.OP_TIMES &&(expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(IMUL);
		else if(expression_Binary.op == Kind.OP_EQ) {
			if(expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitLdcInsn(new Boolean(false));
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
			} else {
				mv.visitJumpInsn(IF_ACMPEQ, l1);
				mv.visitLdcInsn(new Boolean(false));
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
			}
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(l2);
		}
		else if(expression_Binary.op == Kind.OP_GE) {
			mv.visitJumpInsn(IF_ICMPGE, l1);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(l2);
		}
		else if(expression_Binary.op == Kind.OP_GT) {
			mv.visitJumpInsn(IF_ICMPGT, l1);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(l2);
		}
		else if(expression_Binary.op == Kind.OP_LE) {
			mv.visitJumpInsn(IF_ICMPLE, l1);
			mv.visitLdcInsn(new Boolean(false));
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(l2);
		}
		else if(expression_Binary.op == Kind.OP_LT) {
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitLdcInsn( new Boolean(false));
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(l2);
		}
		else if(expression_Binary.op == Kind.OP_MINUS &&(expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(ISUB);
		else if(expression_Binary.op == Kind.OP_MOD && (expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN))	mv.visitInsn(IREM);
		else if(expression_Binary.op == Kind.OP_NEQ) {
			if(expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e1.Type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitLdcInsn(new Boolean(false));
			} else {
				mv.visitJumpInsn(IF_ACMPNE, l1);
				mv.visitLdcInsn(new Boolean(false));
			}
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitLdcInsn(new Boolean(true));
			mv.visitLabel(l2);
		}
		return null;
	}
    @Override
    public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
    	  if(expression_Unary.e!=null)  expression_Unary.e.visit(this, arg);
    	  if(expression_Unary.op == Kind.OP_MINUS)  mv.visitInsn(INEG);
    	  else if(expression_Unary.op == Kind.OP_EXCL) {
    	    if(expression_Unary.Type == Type.INTEGER) {
    	      mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
    	      mv.visitInsn(IXOR);
    	    }
    	    else if(expression_Unary.Type == Type.BOOLEAN) {
    	      mv.visitInsn(ICONST_1);
    	      mv.visitInsn(IXOR);
    	    }
    	  }
    	  else if(expression_Unary.op != Kind.OP_PLUS) {
    	    throw new UnsupportedOperationException();
    	    }
    	  return null;
    }
    
    // generate code to leave the two values on the stack
    @Override
    public Object visitIndex(Index index, Object arg) throws Exception {
        if(index.e0!=null)  index.e0.visit(this, arg);
        if(index.e1!=null)  index.e1.visit(this, arg);
        boolean isNotCaretesian = !index.isCartesian();
        int code; 
        if(isNotCaretesian) {
        	  code = DUP2;
          mv.visitInsn(code);
          mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
          code = DUP_X2;
          mv.visitInsn(code);
          code = POP;
          mv.visitInsn(POP);
          mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
        }
        return null;
    }
    
    @Override
    public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
    throws Exception {
        int code =	GETSTATIC;
        boolean itf = false;
        mv.visitFieldInsn(code , className, expression_PixelSelector.name, ImageSupport.ImageDesc);
        expression_PixelSelector.index.visit(this, arg);
        code = INVOKESTATIC;        
        mv.visitMethodInsn(code, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, itf);
        return null;
    }
    
    @Override
    public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
    throws Exception {
        expression_Conditional.condition.visit(this,arg);
        
        Label first = new Label(), second = new Label();
        int opcode = IFNE;
        mv.visitJumpInsn(opcode, first);
        
        expression_Conditional.falseExpression.visit(this, arg);
        
        opcode = GOTO;
        mv.visitJumpInsn(opcode, second);
        
        mv.visitLabel(first);
        
        expression_Conditional.trueExpression.visit(this, arg);
        
        mv.visitLabel(second);
        
        //		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.Type);
        return null;
    }
    
    
    @Override
    public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
        // TODO HW6
        int access = ACC_STATIC;
        String name = declaration_Image.name;
        String desc = ImageSupport.ImageDesc;
        String signature = null;
        Object value = null;
        cw.visitField(access, name, desc, signature, value).visitEnd();
        int opcode;
        String owner;
        boolean itf = false;
        
        if(declaration_Image.source != null) {
            declaration_Image.source.visit(this, arg);
            if(declaration_Image.xSize == null && declaration_Image.ySize == null) {
                opcode = ACONST_NULL;
                mv.visitInsn(opcode);
                mv.visitInsn(opcode);
            }
            else {
                declaration_Image.xSize.visit(this, arg);
                
                opcode = INVOKESTATIC;
                owner = "java/lang/Integer";
                name =  "valueOf";
                desc = "(I)Ljava/lang/Integer;";
                mv.visitMethodInsn(opcode, owner, name, desc, itf);
                
                declaration_Image.ySize.visit(this, arg);
                
                mv.visitMethodInsn(opcode, owner, name, desc, itf);
            }
            
            opcode = INVOKESTATIC;
            owner = ImageSupport.className;
            name =  "readImage";
            desc = ImageSupport.readImageSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode = PUTSTATIC;
            owner = className;
            name =  declaration_Image.name;
            desc = ImageSupport.ImageDesc;
            mv.visitFieldInsn(opcode, owner, name, desc);
            
        }
        else {
            if(declaration_Image.xSize == null && declaration_Image.ySize == null) {
                //TODO: not sure
                int cst = 256;
                mv.visitLdcInsn(cst);
                mv.visitLdcInsn(cst);
            }
            else {
                declaration_Image.xSize.visit(this, arg);
                declaration_Image.ySize.visit(this, arg);
            }
            
            opcode = INVOKESTATIC;
            owner = ImageSupport.className;
            name =  "makeImage";
            desc = ImageSupport.makeImageSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode = PUTSTATIC;
            owner = className;
            name =  declaration_Image.name;
            desc = ImageSupport.ImageDesc;
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
        return null;
    }
    
    @Override
    public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
        
        String cst = source_StringLiteral.fileOrUrl;
        mv.visitLdcInsn(cst);
        
        return null;
    }
    
    @Override
    public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
    throws Exception {
        
        int opcode = ALOAD;
        int var = 0;
        mv.visitVarInsn(opcode, var);
        
        source_CommandLineParam.paramNum.visit(this, arg);
        
        opcode = AALOAD;
        mv.visitInsn(opcode);
        
        return null;
    }
    
    @Override
    public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
        
        int opcode = GETSTATIC;
        String owner = className;
        String name = source_Ident.name;
        String desc = ImageSupport.StringDesc;
        mv.visitFieldInsn(opcode, owner, name, desc);
        
        return null;
    }
    
    @Override
    public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
    throws Exception {
        // TODO HW6 - check
        
        int access = ACC_STATIC;
        String name = declaration_SourceSink.name;
        String desc = ImageSupport.StringDesc;
        String signature = null;
        Object value = null;
        cw.visitField(access, name, desc, signature, value).visitEnd();
        int opcode = PUTSTATIC;
        String owner =className;
        
        if(declaration_SourceSink.source != null) {
            declaration_SourceSink.source.visit(this, arg);
        }
        
        mv.visitFieldInsn(opcode, owner, name, desc);
        
        return null;
    }
    
    @Override
    public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
        // TODO
        
        int cst = expression_IntLit.value;
        mv.visitLdcInsn(cst);
        
        //		CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
        return null;
    }
    
    @Override
    public Object visitExpression_FunctionAppWithExprArg(
                                                         Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
        
        int opcode = INVOKESTATIC;
        String owner = RuntimeFunctions.className ;
        String name = "abs";
        String desc = RuntimeFunctions.absSig;
        boolean itf = false;
        expression_FunctionAppWithExprArg.arg.visit(this, arg);
        
        if(expression_FunctionAppWithExprArg.function == Kind.KW_abs) {
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else if(expression_FunctionAppWithExprArg.function == Kind.KW_log) {
            name = "log";
            desc = RuntimeFunctions.logSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        return null;
    }
    
    @Override
    public Object visitExpression_FunctionAppWithIndexArg(
                                                          Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
        
        expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
        expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
        
        int opcode = INVOKESTATIC;
        String owner = RuntimeFunctions.className ;
        String name = "cart_x";
        String desc = RuntimeFunctions.cart_xSig;
        boolean itf = false;
        
        if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_x) {
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else if(expression_FunctionAppWithIndexArg.function == Kind.KW_cart_y) {
            name = "cart_y";
            desc = RuntimeFunctions.cart_ySig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else if(expression_FunctionAppWithIndexArg.function == Kind.KW_polar_r) {
            name = "polar_r";
            desc = RuntimeFunctions.polar_rSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else{
            name = "polar_a";
            desc = RuntimeFunctions.polar_aSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        
        return null;
    }
    
    @Override
    public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
    throws Exception {
        // TODO HW6
        
        
        int opcode = GETSTATIC;
        String owner = className;
        String name = "DEF_X";
        String desc = "I";
        boolean itf = false;
        if(expression_PredefinedName.kind == Kind.KW_DEF_X) {
            mv.visitFieldInsn(opcode, owner, name, desc);
        }else if(expression_PredefinedName.kind == Kind.KW_DEF_Y) {
            name = "DEF_Y";
            mv.visitFieldInsn(opcode, owner, name, desc);
        }else if (expression_PredefinedName.kind == Kind.KW_Z) {
            name = "Z";
            mv.visitFieldInsn(opcode, owner, name, desc);
        } else if (expression_PredefinedName.kind == Kind.KW_x) {
            name = "x";
            mv.visitFieldInsn(opcode, owner, name, desc);
        } else if (expression_PredefinedName.kind == Kind.KW_y) {
            name = "y";
            mv.visitFieldInsn(opcode, owner, name, desc);
        } else if (expression_PredefinedName.kind == Kind.KW_X) {
            name = "X";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
        } else if (expression_PredefinedName.kind == Kind.KW_Y) {
            name = "Y";
            mv.visitFieldInsn(opcode, owner, name, desc);
        } else if (expression_PredefinedName.kind == Kind.KW_r) {
            name = "x";
            mv.visitFieldInsn(opcode, owner, name, desc);
            name = "y";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = RuntimeFunctions.className;
            name = "polar_r";
            desc = RuntimeFunctions.polar_rSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        } else if (expression_PredefinedName.kind == Kind.KW_a) {
            
            name = "x";
            mv.visitFieldInsn(opcode, owner, name, desc);
            name = "y";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = RuntimeFunctions.className;
            name = "polar_a";
            desc = RuntimeFunctions.polar_aSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        } else if (expression_PredefinedName.kind == Kind.KW_R) {
            
            name = "X";
            mv.visitFieldInsn(opcode, owner, name, desc);
            name = "Y";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = RuntimeFunctions.className;
            name = "polar_r";
            desc = RuntimeFunctions.polar_rSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        } else if (expression_PredefinedName.kind == Kind.KW_A) {
            name = "X";
            mv.visitFieldInsn(opcode, owner, name, desc);
            name = "Y";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = RuntimeFunctions.className;
            name = "polar_a";
            desc = RuntimeFunctions.polar_aSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
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
        
        int opcode = GETSTATIC;
        String owner = "java/lang/System";
        String name = "out";
        String desc = "Ljava/io/PrintStream;";
        boolean itf = false;
        mv.visitFieldInsn(opcode, owner, name, desc);
        
        if(statement_Out.getDec().Type == Type.INTEGER) {
            owner = className;
            name = statement_Out.name;
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().Type);
            
            opcode = INVOKEVIRTUAL;
            owner = "java/io/PrintStream";
            name = "println";
            desc = "(I)V";
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else if(statement_Out.getDec().Type == Type.BOOLEAN) {
            owner = className;
            name = statement_Out.name;
            desc = "Z";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().Type);
            
            opcode = INVOKEVIRTUAL;
            owner = "java/io/PrintStream";
            name = "println";
            desc = "(Z)V";
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else if(statement_Out.getDec().Type == Type.IMAGE) {
            owner = className;
            name = statement_Out.name;
            desc = ImageSupport.ImageDesc;
            mv.visitFieldInsn(opcode, owner, name, desc);
            
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
        int opcode = INVOKESTATIC;
        String owner;
        String name;
        String desc;
        boolean itf = false;
        if(statement_In.getDec().Type == Type.BOOLEAN) {
            owner = "java/lang/Boolean";
            name = "parseBoolean";
            desc = "(Ljava/lang/String;)Z";
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode = PUTSTATIC;
            owner = className;
            name = statement_In.name;
            desc = "Z";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
        } else if(statement_In.getDec().Type == Type.INTEGER) {
            owner = "java/lang/Integer";
            name = "parseInt";
            desc = "(Ljava/lang/String;)I";
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode = PUTSTATIC;
            owner = className;
            name = statement_In.name;
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
        } else if(statement_In.getDec().Type == Type.IMAGE) {
            Declaration_Image d = (Declaration_Image) statement_In.getDec();
            if( d.xSize == null && d.ySize == null ) {
                opcode = ACONST_NULL;
                mv.visitInsn(opcode);
                mv.visitInsn(opcode);
            } else {
                d.xSize.visit(this, arg);
                owner = "java/lang/Integer";
                name = "valueOf";
                desc = "(I)Ljava/lang/Integer;";
                mv.visitMethodInsn(opcode, owner, name, desc, itf);
                
                d.ySize.visit(this, arg);
                mv.visitMethodInsn(opcode, owner, name, desc, itf);
            }
            
            opcode = INVOKESTATIC;
            owner = ImageSupport.className;
            name = "readImage";
            desc = ImageSupport.readImageSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode =PUTSTATIC;
            owner = className;
            name = d.name;
            desc = ImageSupport.ImageDesc;
            mv.visitFieldInsn(opcode, owner, name, desc);
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
            
            //			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
            //			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
            //			mv.visitFieldInsn(PUTSTATIC, className,"Y", "I");
            //
            //			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
            //			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
            //			mv.visitFieldInsn(PUTSTATIC, className,"X", "I");
            //
            //			mv.visitInsn(ICONST_0);
            //			mv.visitInsn(DUP);
            //			mv.visitLabel(l1);
            //			mv.visitFieldInsn(PUTSTATIC, className,"y", "I");
            //			mv.visitFieldInsn(GETSTATIC, className,"Y", "I");
            //			mv.visitJumpInsn(IF_ICMPGE, l4);
            //			mv.visitInsn(ICONST_0);
            //			mv.visitInsn(DUP);
            //			mv.visitLabel(l2);
            //			mv.visitFieldInsn(PUTSTATIC, className,"x", "I");
            //			mv.visitFieldInsn(GETSTATIC, className,"X", "I");
            //			mv.visitJumpInsn(IF_ICMPGE, l3);
            //			statement_Assign.e.visit(this, arg);
            //			statement_Assign.lhs.visit(this, arg);
            //			mv.visitFieldInsn(GETSTATIC, className,"x", "I");
            //			mv.visitInsn(ICONST_1);
            //			mv.visitInsn(IADD);
            //			mv.visitInsn(DUP);
            //			mv.visitJumpInsn(GOTO, l2);
            //			mv.visitLabel(l3);
            //			mv.visitFieldInsn(GETSTATIC, className,"y", "I");
            //			mv.visitInsn(ICONST_1);
            //			mv.visitInsn(IADD);
            //			mv.visitInsn(DUP);
            //			mv.visitJumpInsn(GOTO, l1);
            //			mv.visitLabel(l4);
            
            //Assign Y once outside loop
            int opcode = GETSTATIC;
            String owner = className;
            String name = statement_Assign.lhs.name;
            String desc = ImageSupport.ImageDesc;
            boolean itf = false;
            
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = ImageSupport.className;
            name = "getY";
            desc = ImageSupport.getYSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode = PUTSTATIC;
            owner = className;
            name = "Y";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            //Assign X once outside loop
            opcode = GETSTATIC;
            owner = className;
            name = statement_Assign.lhs.name;
            desc = ImageSupport.ImageDesc;
            
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = ImageSupport.className;
            name = "getX";
            desc = ImageSupport.getXSig;
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
            
            opcode = PUTSTATIC;
            owner = className;
            name = "X";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            
            opcode = ICONST_0;
            mv.visitInsn(opcode);
            
            opcode = DUP;
            mv.visitInsn(opcode);
            
            mv.visitLabel(l1);
            
            opcode = PUTSTATIC;
            owner = className;
            name = "y";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = GETSTATIC;
            owner = className;
            name = "Y";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = IF_ICMPGE;
            mv.visitJumpInsn(opcode, l4);
            
            opcode = ICONST_0;
            mv.visitInsn(opcode);
            
            opcode = DUP;
            mv.visitInsn(opcode);
            
            mv.visitLabel(l2);
            
            opcode = PUTSTATIC;
            owner = className;
            name = "x";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = GETSTATIC;
            owner = className;
            name = "X";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = IF_ICMPGE;
            mv.visitJumpInsn(opcode, l3);
            
            statement_Assign.e.visit(this, arg);
            statement_Assign.lhs.visit(this, arg);
            
            opcode = GETSTATIC;
            owner = className;
            name = "x";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = ICONST_1;
            mv.visitInsn(opcode);
            
            opcode = IADD;
            mv.visitInsn(opcode);
            
            opcode = DUP;
            mv.visitInsn(opcode);
            
            opcode = GOTO;
            mv.visitJumpInsn(opcode, l2);
            
            mv.visitLabel(l3);
            
            opcode = GETSTATIC;
            owner = className;
            name = "y";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = ICONST_1;
            mv.visitInsn(opcode);
            
            opcode = IADD;
            mv.visitInsn(opcode);
            
            opcode = DUP;
            mv.visitInsn(opcode);
            
            opcode = GOTO;
            mv.visitJumpInsn(opcode, l1);
            
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
        int opcode;
        String owner;
        String name;
        String desc;
        boolean itf = false;
        if(lhs.Type == Type.BOOLEAN) {
            opcode = PUTSTATIC;
            owner = className;
            name = lhs.name;
            desc = "Z";
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
        else if(lhs.Type == Type.INTEGER) {
            opcode = PUTSTATIC;
            owner = className;
            name = lhs.name;
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
        else if (lhs.Type == Type.IMAGE) {
            opcode = GETSTATIC;
            owner = className;
            name = lhs.name;
            desc = ImageSupport.ImageDesc;
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            name = "x";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            name = "y";
            desc = "I";
            mv.visitFieldInsn(opcode, owner, name, desc);
            
            opcode = INVOKESTATIC;
            owner = ImageSupport.className;
            name = "setPixel";
            desc = ImageSupport.setPixelSig;
            
            mv.visitMethodInsn(opcode, owner, name, desc, itf);
        }
        else
            throw new UnsupportedOperationException();
        return null;
    }
    
    
    @Override
    public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception
    {
        
        int opcode = INVOKESTATIC;
        String owner = ImageSupport.className;
        String name = "makeFrame";
        String desc = ImageSupport.makeFrameSig;
        boolean itf = false;
        
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
        
        opcode = POP;
        mv.visitInsn(opcode);
        return null;
    }
    
    @Override
    public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception 
    {
        int opcode = GETSTATIC;
        String owner = className;
        String name = sink_Ident.name;
        String desc = "Ljava/lang/String;";
        boolean itf = false;
        
        mv.visitFieldInsn(opcode, owner, name, desc);
        
        opcode = INVOKESTATIC;
        owner = ImageSupport.className;
        name = "write";
        desc = ImageSupport.writeSig;
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
        
        return null;
    }
    
    @Override
    public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
        boolean cst = expression_BooleanLit.value;
        mv.visitLdcInsn(cst);
        //		CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
        return null;
    }
    
    @Override
    public Object visitExpression_Ident(Expression_Ident expression_Ident,
                                        Object arg) throws Exception {
        
        int opcode = GETSTATIC;
        String owner = className;
        String name = expression_Ident.name;
        String desc = "I";
        boolean itf = false;
        
        if(expression_Ident.Type == Type.INTEGER) {
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
        else if(expression_Ident.Type == Type.BOOLEAN) {
            desc = "Z";
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
        else
            throw new UnsupportedOperationException();
        //		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.Type);
        return null;
    }
    
}
