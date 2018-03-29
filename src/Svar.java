package oop.ex6;

/**
 * The class represents a variable in the compiler.
 */
public class Svar {

    /** The name of the svar object. */
    String name;

    /** The val of the svar object. */
    String val;

    /** The type constant of the svar object. */
    String TYPE;

    /** A data member for marking if the var is final. */
    boolean isFinal = false;

    /** A data member for marking if the var is a method's argument var. */
    boolean isMethodVar = false;


    /**
     * default constructor for the svar object. initializes the needed variables.
     * @param name var's name
     * @param val var's value if declared
     * @param TYPE The type constant for the var
     */
    Svar(String name, String val, String TYPE) {
        this.name = name;
        this.val = val;
        this.TYPE = TYPE;
    }

    /**
     * constructor with the option of marking the var as final.
     * @param name var's name
     * @param val var's value if declared
     * @param TYPE The type constant for the var
     * @param isFinal marks the var as final
     */
    Svar(String name, String val, String TYPE, boolean isFinal) {
        this.name = name;
        this.val = val;
        this.TYPE = TYPE;
        this.isFinal = isFinal;
    }

    /**
     * sets the type constant
     * @param TYPE the type constant to be set
     */
    protected void setType(String TYPE) {
        this.TYPE = TYPE;
    }

    /**
     * Returns var's name representation as a string.
     * @return Current var name.
     */
    public String toString() {
        return name;
    }

    /**
     * Returns var's type representation as a string.
     * @return Current var type as a string
     */
    protected String getType() {
        return TYPE;
    }

    /**
     * Sets the value to be the newVal inputted, without any type checks.
     * @param newVal the new val to be set
     * @return true if the val was set to the var, false otherwise.
     */
    protected boolean setVal(String newVal) {
        if (isFinal) {
            return false;
        } else {
            val = newVal;
            return true;
        }
    }

    /**
     * Returns the var's val
     * @return var's val
     */
    protected String getVal() { return val; }

}
