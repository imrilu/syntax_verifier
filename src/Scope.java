package oop.ex6;
import java.util.LinkedList;


/**
 * The class responsible for the relations between the blocks and their variables.
 */
public class Scope {

    /** A scope object of current scope's parent block, if such exist. */
    Scope fatherScope;

    /** all of the method objects in the current program. */
    protected static LinkedList<Methods> programMethods = new LinkedList<Methods>();

    /** A list of scope's variables. */
    private LinkedList<Svar> scopeVariables;

    /**
     * constructor for the scope class. initializes a list and sets the parent scope.
     * @param fatherScope The parent scope to associate the object to.
     */
    protected Scope(Scope fatherScope) {
        this.fatherScope = fatherScope;
        scopeVariables = new LinkedList<Svar>();
    }

    /**
     * Gets a new method and adds it to the scopes methods list. if it's already there, throws exception.
     * @param newMethod the new method to be added to the list
     * @throws Exception if the method is already in the method list
     */
    protected void addMethod (Methods newMethod)throws Exception{
        for (Methods curMethod: programMethods){
            if (newMethod.getMethodName().equals(curMethod.getMethodName())){
                throw new Exception("2 methods with the same name!");
            }
        }
        programMethods.add(newMethod);
    }

    /**
     * finds the method based on it's name
     * @param methodToFind the method's to search
     * @return the method object if found, null otherwise
     */
    protected Methods findMethod(String methodToFind) {
        for (Methods curMethod : programMethods) {
            if (methodToFind.equals(curMethod.getMethodName())) {
                return curMethod;
            }
        }
        return null;
    }

    /**
     * Adds the input var to the current scope, if one doesn't exist with the same name
     * @param varName the Svar object to be added to the scope's list of vars
     * @throws Exception if the method is already in the method list
     */
    protected void addVar(Svar varName) throws Exception {
        for (Svar variable : scopeVariables){
            if (varName.name.equals(variable.name)){
                throw new Exception("declaring 2 variables with the same name");
            }
        }
        scopeVariables.add(varName);
    }

    /**
     * A helper method to clear the static methods list after each iteration.
     */
    public static void clear() {
        programMethods.clear();
    }

    /**
     * Searches for the varName object in the current scope alone.
     * @param varName The string name to be searched in the scope
     * @return the var if found, null otherwise.
     */
    protected Svar search(String varName) {
        for (Svar var : scopeVariables) {
            if (var.toString().equals(varName)) {
                return var;
            }
        }
        return null;
    }

    /**
     * searches for the varName object in the scope and all of it's parent scopes.
     * @param varName The string name to be searched in the scope
     * @return the var if found, null otherwise.
     */
    protected Svar findVar(String varName) {
        // check if the requested var is in current scope variable list
        for (Svar var : scopeVariables) {
            if (var.toString().equals(varName)) {
                return var;
            }
        }
        // varName wasn't found in current scope. checking if scope has father and if so checking var in it.
        if (fatherScope == null) {
            return null;
        }
        return fatherScope.findVar(varName);
        // varName wasn't found, returning null
    }

}
