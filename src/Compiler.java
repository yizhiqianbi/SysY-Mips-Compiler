import Frontend.AST.CompUnitAST;
import Frontend.LexicalAnalyser;
import Frontend.Parser;
import Frontend.Token;
import Error.*;
import LLVMIR.IRModule;
import LLVMIR.Visitor;
import LLVMOUT.LLVMGenerator;
import Utils.Dumps.IRDump;
import Utils.Dumps.MipsDump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

public class Compiler {

    public static void main(String[] args) throws FileNotFoundException {

        File file = new File ("output.txt");
        PrintStream ps = new PrintStream(file);
        System.setOut(ps);

        Parser parser = Parser.getInstance();

        LexicalAnalyser lexicalAnalyser = LexicalAnalyser.getInstance();
        ErrorHandler errorHandler = ErrorHandler.getInstance();
        MipsDump mipsDump = MipsDump.getInstance();




//        LLVMGenerator llvmGenerator = LLVMGenerator.getInstance();

        CompUnitAST compUnitAST;
        try {
            lexicalAnalyser.lex();
            parser.parse();
            compUnitAST = parser.getCompUnitAST();
            errorHandler.compUnitError(parser.getCompUnitAST());
            boolean hasError = errorHandler.printErrors();


            if(!hasError){
                Visitor visitor = new Visitor();
                IRModule irModule = visitor.VisitCompUnit(compUnitAST);
                IRDump.getInstance().DumpModule(irModule);

//                mipsDump.loadIRModule(irModule);
//                mipsDump.dumpMips(irModule);
            }

//            llvmGenerator.OutPutAll(compUnitAST);
//            errorHandler.printErrors();
//            errorHandler.compUnitError();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
