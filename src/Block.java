package oop.ex6;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * the class responsible for parsing the blocks (methods/if/while) of the program.
 */
public class Block {

    /** A string constant for the regular expression of a new block.     */
    final static String REGULAR_EXPRESSION_NEW_BLOCK = ".*\\{\\s*";

    /** A string constant for the regular expression for closing a block.     */
    final static String REGULAR_EXPRESSION_CLOSE_BLOCK = "\\s*}\\s*"; //"[a-zA-Z]\\w*\\}";

    /** A string constant for the regular expression a new method declaration.     */
    final static String REGULAR_EXPRESSION_METHOD_BLOCK = "\\s*void\\s+([a-zA-Z]\\w*)\\s*\\(" +
            "\\s*((double|int|String|char|boolean)\\s+" + SyntaxVerifier.nameFormat + "\\s*" +
            "(\\s*,\\s*(double|int|String|char|boolean)\\s+" + SyntaxVerifier.nameFormat + ")*)?" +
            "\\s*\\)\\s*\\{\\s*";

    /** A string constant for the regular expression of an if/while new block.     */
    final static String REGULAR_EXPRESSION_CONDITION_BLOCK = "\\s*(?:while|if)\\s*\\(\\s*(.*)\\s*\\)" +
            "\\s*\\{\\s*";

    /** A string constant for the regular expression of an empty line.     */
    final static String REGULAR_EXPRESSION_EMPTY_LINE = "\\s*";

    /** A string constant for the regular expression of a return line.     */
    final static String REGULAR_EXPRESSION_RETURN_LINE = "\\s*(return)\\s*;";

    /** A SyntaxVerefier static constant for using class's methods easily.     */
    final static SyntaxVerifier ver = new SyntaxVerifier();

    /** A scope object to mark our current position in relate to the rest of the program blocks.     */
    Scope curScope;

    /** An if/while condition string, if such exist.     */
    String conditionField;

    /** A boolean used to mark if the block is a method block.  */
    boolean isMethod;

    /** The block's self lines that haven't been parsed.     */
    LinkedList<String> selfLines;

    /** A list of the block's block sons.   */
    LinkedList<Block> blockSons;

    /** A pattern constant for the regular expression of a new method.     */
    final static Pattern METHOD_BLOCK_PATTERN = Pattern.compile(REGULAR_EXPRESSION_METHOD_BLOCK);

    /** A pattern constant for the regular expression of a new block.     */
    final static Pattern OPEN_BLOCK_PATTERN = Pattern.compile(REGULAR_EXPRESSION_NEW_BLOCK);

    /** A pattern constant for the regular expression of a block closer.     */
    final static Pattern CLOSE_BLOCK_PATTERN = Pattern.compile(REGULAR_EXPRESSION_CLOSE_BLOCK);

    /** A pattern constant for the regular expression of an if/while block.     */
    final static Pattern CONDITION_BLOCK_PATTERN = Pattern.compile(REGULAR_EXPRESSION_CONDITION_BLOCK);

    /** A pattern constant for the regular expression of an empty line.     */
    final static Pattern EMPTY_LINE_PATTERN = Pattern.compile(REGULAR_EXPRESSION_EMPTY_LINE);

    /** A pattern constant for the regular expression of a new block.     */
    final static Pattern RETURN_LINE_PATTERN = Pattern.compile(REGULAR_EXPRESSION_RETURN_LINE);

    /**
     * constructor for the block object. constructs and initializes all the necessery variables, and calls
     * the block's main loop which operates the rest of the parsing for the block.
     * @param fatherScope The father scope, if such exist
     * @param isMethod marks if the block is a method block
     * @param rawLines the raw line list to poll the lines from
     * @throws Exception if any illegal compile line was verified
     */
    public Block(Scope fatherScope, boolean isMethod, LinkedList<String> rawLines) throws Exception {
        this.isMethod = isMethod;
        curScope = new Scope(fatherScope);
        // initializing the list of the unhandled lines
        selfLines = new LinkedList<String>();
        blockSons = new LinkedList<Block>();
        blocksMainLoop(rawLines);
    }

    /**
     * blocks main loop. it creates the blocks and the relationship between them.
     * @param rawLines the raw lines to parse the block with
     * @throws Exception if any illegal compile line was verified
     */
    protected void blocksMainLoop(LinkedList<String> rawLines) throws Exception {
        Matcher openMatcher, closedMatcher, emptyMatcher, returnMatcher;
        String currentLine;
        Block newBlock;
        while (!rawLines.isEmpty()) {
            currentLine = rawLines.poll();
            emptyMatcher = EMPTY_LINE_PATTERN.matcher(currentLine);
            if (currentLine.startsWith("//") || emptyMatcher.matches()) {
                if (rawLines.peek() == null) {
                    break;
                } else {
                    continue;
                }
            }
            // initializes the respective matchers
            returnMatcher = RETURN_LINE_PATTERN.matcher(currentLine);
            openMatcher = OPEN_BLOCK_PATTERN.matcher(currentLine);
            closedMatcher = CLOSE_BLOCK_PATTERN.matcher(currentLine);
            if (openMatcher.matches()) {
                // checks for an open block line
                newBlock = blockAnalyzer(currentLine, curScope, rawLines);
                blockSons.add(newBlock);
            } else if (returnMatcher.matches()) {
                // checks for a return line
                if (curScope.fatherScope == null) {
                    // we're in main scope, return is illegal
                    throw new Exception("wrong return statement");
                } else if (isMethod == true) {
                    selfLines.add(currentLine);
                }
            } else if (closedMatcher.matches()) {
                // checks for a block closer line
                break;
            } else {
                selfLines.add(currentLine);
            }
        }
    }

    /** parsing the first line of a new block. if method, will create a method object and associate it to the
     * block. it will also create a new block which represent the block and add the parameters to the correct
     * scope. If a condition block is detected, it will create a new block and add the condition line to a
     * list of conditions to be checked at the second parse.
     * @param blockFirstLine first block's line
     * @param curScope current block's scope
     * @param rawLines the list to draw the lines from
     * @return a new block object after finishing the parsing
     * @throws Exception if any illegal compile line was verified
     */
    private Block blockAnalyzer(String blockFirstLine, Scope curScope, LinkedList<String> rawLines) throws
            Exception {
        Block newBlock;
        Matcher methodMatcher = METHOD_BLOCK_PATTERN.matcher(blockFirstLine);
        Matcher conditionMatcher = CONDITION_BLOCK_PATTERN.matcher(blockFirstLine);
        if (methodMatcher.matches()) {
            String methodName = methodMatcher.group(1), methodVarLine = methodMatcher.group(2);
            Methods newMethod = new Methods(methodName, methodVarLine);
            curScope.addMethod(newMethod);
            newBlock = new Block(curScope, true, rawLines);
            newBlock.addToScope(methodVarLine, newBlock.getCurScope());
            return newBlock;
        } else if (conditionMatcher.matches()) {
            // adding the condition field to the block
            newBlock = new Block(curScope, false, rawLines);
            newBlock.conditionField = conditionMatcher.group(1);
            return newBlock;
        }
        throw new Exception("wrong blocks declartion");
    }

    /**
     * gets the condition string.
     * @return the block's condition string. null if none exists.
     */
    public String getCondition() { return conditionField; }

    /**
     * returns the scope of this block
     * @return block's current scope
     */
    public Scope getCurScope() {
        return curScope;
    }

    /**
     * returns the boolean indicator for being a method block
     * @return true if block is method, false otherwise.
     */
    public boolean isMethod(){
        return isMethod;
    }

    /**
     * returns the blocks which are related to this block (his "sons").
     * @return block's block sons.
     */
    public LinkedList<Block> getBlocksSons() {
        return blockSons;
    }

    /**
     * gets variables string and the scope to add to and adds the variables to the scope.
     */
    private void addToScope(String varLine, Scope curScope) throws Exception {
        if (varLine == null) {
            return;
        }
        String[] separatedVariables = varLine.split(",");
        for (String variable : separatedVariables) {
            if (variable.equals("")) {
                break;
            }
            ver.varDeclareParser(variable + ";", curScope, true);
        }
    }

    /**
     * returns the lines in this specific block.
     * @return current block's self lines
     */
    public LinkedList<String> getSelfLines() {
        return selfLines;
    }

}
