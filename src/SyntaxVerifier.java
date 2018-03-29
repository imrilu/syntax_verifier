package oop.ex6;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class responsible for checking legitimacy for certain line parsing.
 */
public class SyntaxVerifier {

    /** A constant for the type string.     */
    static final String TYPE_STRING = "STRING";

    /** A constant for the type int.     */
    static final String TYPE_INT = "INT";

    /** A constant for the type double.     */
    static final String TYPE_DOUBLE = "DOUBLE";

    /** A constant for the type char.     */
    static final String TYPE_CHAR = "CHAR";

    /** A constant for the type boolean.     */
    static final String TYPE_BOOLEAN = "BOOLEAN";

    /** A constant for the function param's val.     */
    static final String FUNC_VAR="";

    /** A constant for the equals char.     */
    final static String EQUALS_CHAR = "=";

    /** represents value of variable that was sent to a function as parameter.
     * double, int, String, char, boolean.  */
    String[] regexFormats = {"[+-]?(\\d+(\\.\\d+)?)", "[+-]?\\d+", "\"(.*)\"", "'.'",
            "(true|false|[+-]?(\\d+(\\.\\d+)?))"};

    /** A constant for the type string.     */
    String[] typeNames = {"double", "int", "String", "char", "boolean"};

    /** A pattern constant for the double type.     */
    final Pattern doublePat = Pattern.compile(regexFormats[0]);

    /** A pattern constant for the int type.     */
    final Pattern intPat = Pattern.compile(regexFormats[1]);

    /** A pattern constant for the string type.     */
    final Pattern stringPat = Pattern.compile(regexFormats[2]);

    /** A pattern constant for the char type.     */
    final Pattern charPat = Pattern.compile(regexFormats[3]);

    /** A pattern constant for the equals char.     */
    final Pattern booleanPat = Pattern.compile(regexFormats[4]);

    /** A pattern constant for the var assignment.     */
    final static String nameFormat = "(\\s*[a-zA-Z]\\w*|_\\w+)(\\s*=\\s*(.)+)?";

    /** A pattern constant for the compare type.     */
    final static Pattern comparePat = Pattern.compile(nameFormat);

    /** A pattern constant for the method call.     */
    final static Pattern methodCallPat= Pattern.compile("\\s*([a-zA-Z]\\w*)\\s*\\(\\s*(.*)\\)\\s*;");

    /** A matcher data attribute for matching certain patterns.     */
    Matcher matcher;


    /**
     * checks if the condition string is legal
     * @param condition the condition to check
     * @param curScope our current scope
     * @throws Exception if any illegal compile line was verified
     */
    public void conditionVerefier(String condition, Scope curScope) throws Exception {
        String[] tempConditionVals = condition.split("\\|\\||&&");
        for (String curCondition : tempConditionVals) {
            curCondition = curCondition.trim();
            Svar tempVal = curScope.findVar(curCondition);
            if (tempVal != null && tempVal.getVal() == null) {
                // found the val, but not initialized
                throw new Exception("val not initialized");
            }
            if (!isCorrectFormat(curCondition, TYPE_BOOLEAN) && (tempVal.getType() == TYPE_CHAR ||
            tempVal.getType() == TYPE_STRING)) {
                throw new Exception("not a legit condition");
            }
        }
        // if reached here syntax is ok for the while/if line
    }

    /**
     * checks if the line received is a legal method call
     * @param line the line to check on
     * @param curMethod a pointer to the method object called
     * @param scope our current scope
     * @throws Exception if any illegal compile line was verified
     */
    protected void isLegalMethodCall(String line, Methods curMethod, Scope scope) throws Exception{
        LinkedList<String> methodVarTypes = curMethod.getMethodVarTypes();
        // splits the param by comma
        String[] lineVarArr = line.split(",");
        // checks if the number of param is identical to method's number
        if (methodVarTypes.size() != lineVarArr.length) {
            throw new Exception("error assigning vars");
        }
        // iterates on the method vars
        for (int i=0; i<methodVarTypes.size(); i++) {
            if (scope.findVar(lineVarArr[i]) != null && scope.findVar(lineVarArr[i]).getType() ==
                    methodVarTypes.get(i)) {
                continue;
            }
            else if (isCorrectFormat(lineVarArr[i].trim(), methodVarTypes.get(i)) == false) {
                // means var not in the correct format
                throw new Exception("wrong var");
            }
        }
    }

    /**
     * gets a line to process (decleration etc..) , scope to add to , and bool value that says rather it's an
     * line from the code or rather it's lines of parameters that comes with certain method.
     * @param line the line to parse
     * @param scope the current scope
     * @param isFuncPar is the line declare is within a function creation
     * @return true if the parser was successful, false otherwise
     * @throws Exception if any illegal compile line was verified
     */
    public boolean varDeclareParser(String line, Scope scope,boolean isFuncPar) throws Exception {
        LinkedList<String> varList = new LinkedList<String>();
        Matcher methodMatcher=methodCallPat.matcher(line);
        boolean isFinal = false;
        // checking if line is method declare
        if (methodMatcher.matches()) {
            if (scope.fatherScope == null) {
                // cant call methods from main scope, throwing exception
                throw new Exception("cant call for a method from the main scope");
            }
            Methods curMethod = scope.findMethod(methodMatcher.group(1));
            if (curMethod != null) {
                // calling the method var type check
                if (curMethod.getMethodVarTypes() != null) {
                    isLegalMethodCall(methodMatcher.group(2), curMethod, scope);
                }
                return true;
            } else {
                throw new Exception("no method exist");
            }
        }
        // line is var declare or var assignment
        String tempAssignment = line.split(EQUALS_CHAR)[0];
        Svar tempVar = scope.findVar(tempAssignment.trim());
        if (tempVar != null) {
            if (tempVar.isFinal) {
                throw new Exception("cannot change final value");
            }
            varList.add(reduceSpaces(line.split(";")[0]));
            // first var is initialized. calling the checking method
            varDeclareCheck(varList, tempVar.getType(), scope, isFinal, isFuncPar);
            return true;
        }
        line = line.trim();
        String[] lineArr = line.split("\\s+", 2);
        // checking if the assignment is 'final'
        if (lineArr[0] != null && lineArr[0].trim().equals("final")) {
            isFinal = true;
            lineArr = lineArr[1].trim().split("\\s+", 2);
        }
        // getting the correct type constant for the var
        String curType = getTypeConstant(lineArr[0].trim());
        // line matches the pattern
        // checking the current line's assignments type
        String[] varArr = lineArr[1].trim().split(",|;");
        for (int i = 0; i < varArr.length; i++) {
            matcher=comparePat.matcher(varArr[i].trim());
            if (matcher.matches()) {
                varList.add(varArr[i]);
            }
            else {
                throw new Exception("wrong variable name");
            }
        }
        varDeclareCheck(varList, curType, scope, isFinal,isFuncPar);
        // if reached here without exception, line is OK. returning true
        return true;
    }

    /**
     * gets the correct type constant for the string received
     * @param curType the input string to match the constant to
     * @return correct type constant
     * @throws Exception if the input string does not match any type
     */
    protected String getTypeConstant(String curType) throws Exception {
        if (curType.equals(typeNames[0])) {
            // type is double
            return TYPE_DOUBLE;
        } else if (curType.equals(typeNames[1])) {
            // type is int
            return TYPE_INT;
        } else if (curType.equals(typeNames[2])) {
            // type is string
            return TYPE_STRING;
        } else if (curType.equals(typeNames[3])) {
            // type is char
            return TYPE_CHAR;
        } else if (curType.equals(typeNames[4])) {
            // type is boolean
            return TYPE_BOOLEAN;
        }
        // if reached here, no correct type is found. throwing exception
        throw new Exception();
    }

    /**
     * Checks a list of variables for legitimacy according to the inputs
     * @param varList the list of vars to verify
     * @param TYPE the constant type of the line assignment
     * @param scope current scope we're in
     * @param isFinal is the var assignment final
     * @param isFuncPar is the var assignment part of a function param declare
     * @throws Exception if illegal assignment is detected
     */
    protected void varDeclareCheck(LinkedList<String> varList, String TYPE, Scope scope, boolean isFinal,
                                   boolean isFuncPar) throws Exception {
        String[] tempChecker;
        while (!varList.isEmpty()) {
            // splitting each var assignment to temp array of the var and the value that will be assigned to
            // it
            tempChecker = varList.poll().split(EQUALS_CHAR);
            if (tempChecker.length == 1) {
                if (isFinal) {
                    // var final declaration must be initialized in the same line
                    throw new Exception("final var assignment exception");
                }
                // only declaration is made, without assignment of value. creating new Svar in that scope
                if (!isFuncPar) {
                    Svar newVar = new Svar(tempChecker[0], null, TYPE);
                    scope.addVar(newVar);
                } else {
                    Svar newVar = new Svar(tempChecker[0].trim(), FUNC_VAR, TYPE);
                    scope.addVar(newVar);
                }
            } else if (tempChecker.length == 2) {
                // assignment of value is made
                Svar tempVal = scope.findVar(tempChecker[1].trim());
                if (tempVal != null) {
                    //tempVal is not null, checking it's validity in relation to the first var
                    if (tempVal.getVal() == null) {
                        // tempVal is not initialized with a value. throwing exception
                        throw new Exception("assignment val is not initialized");
                    }
                    if ((tempVal.getType() == TYPE) || (TYPE == TYPE_DOUBLE &&
                            tempVal.getType() == TYPE_INT) || (TYPE == TYPE_BOOLEAN &&
                            (tempVal.getType() == TYPE_DOUBLE || tempVal.getType() == TYPE_INT))) {
                        // value's type to be assigned is OK. creating svar object with it's values
                        if (scope.findVar(tempChecker[0].trim()) == null) {
                            // the left var is not declared. creating new svar object
                            scope.addVar(new Svar(tempChecker[0].trim(), tempVal.getVal(), TYPE, isFinal));
                        }
                    }
                }
                // tempVal wasn't found. checking if the value is a legit parameter in the correct format
                else if (isCorrectFormat(tempChecker[1].trim(), TYPE)) {
                    // only declaration is made, without assignment of value. creating new Svar in that scope
                    if (scope.findVar(tempChecker[0].trim()) != null) {
                        if (scope.search(tempChecker[0].trim()) != null) {
                            scope.search(tempChecker[0].trim()).setVal(tempChecker[1]);
                        }
                    } else {
                        Svar newVar = new Svar(tempChecker[0].trim(), tempChecker[1].trim(), TYPE,
                                isFinal);
                        scope.addVar(newVar);
                    }
                }
                else {
                    throw new Exception("bad apply value. expected TYPE format");
                }
            }
        }
    }

    /**
     * Checking if the string is in the correct format based on the TYPE constant inputted
     *
     * @param string The string to check the format on
     * @param TYPE   The type constant to base the check on
     * @return true if in correct format, false otherwise
     */
    private boolean isCorrectFormat(String string, String TYPE) {
        switch (TYPE) {
            case TYPE_DOUBLE:
                matcher = doublePat.matcher(string);
                break;
            case TYPE_INT:
                matcher = intPat.matcher(string);
                break;
            case TYPE_STRING:
                matcher = stringPat.matcher(string);
                break;
            case TYPE_CHAR:
                matcher = charPat.matcher(string);
                break;
            case TYPE_BOOLEAN:
                matcher = booleanPat.matcher(string);
                break;
        }
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    /**
     * reduces all spaces from a certain string
     * @param string the string to reduce the spaces from
     * @return the new string with no spaces
     */
    private String reduceSpaces(String string) {
        return string.replaceAll("\\s", "");
    }

}