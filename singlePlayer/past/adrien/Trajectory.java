/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tracks.singlePlayer.past.adrien;

/**
 * @author acouetoux
 */
class Trajectory {

    public final boolean isFinal;

    public final IntArrayOfDoubleHashMap[] basisFunctionValues1;

    public final IntArrayOfDoubleHashMap[] basisFunctionValues2;

    public Trajectory(boolean _final, IntArrayOfDoubleHashMap[] _bf1, IntArrayOfDoubleHashMap[] _bf2) {
        isFinal = _final;
        basisFunctionValues1 = _bf1;
        basisFunctionValues2 = _bf2;
    }
}
