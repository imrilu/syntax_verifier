package oop.ex6;
import java.util.LinkedList;

/**
 * A class responsible for the method related data that will be associated to the respective block.
 */
public class Methods {

    /** the name of the method. */
    String name;

    /** A SyntaxVerefier static constant for using class's methods easily. */
    final static SyntaxVerifier ver = new SyntaxVerifier();

    /** A chronological list of variable types that are part of method's argument params. */
    LinkedList<String> methodVariablesTypes;

    /**
     * constructor for the method object. initializes it's name and method variable types
     * @param methodName
     * @param methodVarLine
     * @throws Exception if any illegal compile line was verified
     */
    public Methods(String methodName, String methodVarLine) throws Exception{
        name = methodName;
        methodVariablesTypes = methodVarsType(methodVarLine);
    }

    /**
     * A method that creates the list of method var types
     * @param line the param line to parse
     * @return a list of method params
     * @throws Exception if any illegal compile line was verified
     */
    protected LinkedList<String> methodVarsType(String line) throws Exception {
        if (line == null) {
            return null;
        }
        String[] methodVarList = line.split(",");
        LinkedList<String> varsType = new LinkedList<String>();
        for (String tempStr : methodVarList) {

            varsType.add(ver.getTypeConstant(tempStr.trim().split("\\s+")[0]));
        }
        return varsType;
    }

    /**
     * returns the method var type list
     * @return the method var type list
     */
    public LinkedList<String> getMethodVarTypes() {
        return methodVariablesTypes;
    }

    /**
     * returns the method's name
     * @return method's name
     */
    public String getMethodName(){return name;}
}
