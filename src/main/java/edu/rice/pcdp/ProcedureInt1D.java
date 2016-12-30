package edu.rice.pcdp;

/**
 * An interface representing a function of 1 integer parameter.
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public interface ProcedureInt1D {

    /**
     * Apply the body of this function to the int argument.
     *
     * @param arg1 The first argument to the procedure.
     */
    void apply(int arg1);
}
