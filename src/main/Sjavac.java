package oop.ex6.main;
import oop.ex6.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * this class holds the main method and responsible of using all of the class one at it's time. in a
 * logical order.
 */
public class Sjavac {
    static SyntaxVerifier ver = new SyntaxVerifier();
    final static String REGULAR_EXPRESSION_RETURN_LINE = "\\s*(return)\\s*;\\s*";
    final static Pattern RETURN_LINE_PATTERN = Pattern.compile(REGULAR_EXPRESSION_RETURN_LINE);


    /**
     * the main gets the args (File Path) and process it's lines into a string linked list.* and then sending it' to a block constructor that creates all of the blocks recursively (and actully
     * the whole structure of the file)and if the structure was correct according to java structure, no
     * exception would be thorwn, and then we will process a second check (function) that makes sure the
     * syntax (the actual lines ) are correct according to sjava file demands.
     * @param args program arguments list as received from cmd
     */
    public static void main(String[] args){
        try {
            if (args.length!=1){
                throw new Exception("wrong program arguments.format is" +
                        ":java oop.ex6.main.Sjavac source file name");
            }
            Scope.clear();
            LinkedList<String> rawLines = new LinkedList<String>();
            FileReader reader ;
            BufferedReader buffer ;
            reader = new FileReader(args[0]);
            buffer = new BufferedReader(reader);
            String newLine = buffer.readLine();
            while (newLine != null) {
                rawLines.add(newLine);
                newLine = buffer.readLine();
            }
            try {
                Block mainBlock = new Block(null, false, rawLines);
                secondCheck(mainBlock);
                System.out.println("0");
            } catch (Exception e) {
                System.out.println("1");
                System.err.print(e);
            }
        } catch (Exception e){
            System.out.println("2");
            System.err.print(e);
        }
    }

    /** performs a syntax check on the main block , which calls itself recursively in order to check every
     * single block in the program "tree"\structure
     * @param curBlock The main block to parse from
     * @throws Exception if any illegal compile line was verified or an IO exception has occurred.
     */
    private static  void secondCheck(Block curBlock) throws Exception {
        LinkedList<String>blocksSelfLines=curBlock.getSelfLines();
        // checking if block is condition block. is so, checks if the the condition is valid
        if (curBlock.getCondition() != null) {
            ver.conditionVerefier(curBlock.getCondition(), curBlock.getCurScope());
        }
        if (curBlock.isMethod()) {
            // checking if the last line of the method is a return statement
            String lastLine = curBlock.getSelfLines().peekLast();
            Matcher matcher;
            matcher = RETURN_LINE_PATTERN.matcher(lastLine);
            if (!matcher.matches()) {
                throw new Exception("return statement not in the current position");
            } else {
                curBlock.getSelfLines().pollLast();
            }
        }
        for(String line:blocksSelfLines){
            ver.varDeclareParser(line,curBlock.getCurScope(),false);
        }
        for (Block son: curBlock.getBlocksSons()){
            secondCheck(son);
        }
    }

}
