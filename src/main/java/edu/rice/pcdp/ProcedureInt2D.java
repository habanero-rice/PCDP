package edu.rice.pcdp;

/**
 * An interface representing a function of 2 integer parameters.
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public interface ProcedureInt2D {

    /**
     * Apply the body of this function to two integer arguments.
     *
     * @param arg1 The first argument to the procedure.
     * @param arg2 The second argument to the procedure
     */
    void apply(int arg1, int arg2);
}
