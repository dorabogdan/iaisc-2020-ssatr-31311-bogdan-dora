package net.agten.heatersimulator.domain.algorithm;

/**
 * This class implements a proportional–integral–derivative (PID) algorithm for
 * controlling a heater's temperature. The algorithm uses only integer
 * calculations.
 *
 * This class is based on the Barebones PID for Espresso by Tim Hirzel:
 * http://www.arduino.cc/playground/Main/BarebonesPIDForEspresso
 *
 * @author Pieter Agten
 *
 */
public class PID32 implements ControllerAlgorithm {
    /**
     * The maximum result returned by the PID algorithm.
     */
    public final static int MAX_RESULT = 255;
    /**
     * The initial target ADC value.
     */
    public final static int INITIAL_TARGET_ADC = 830;

    /**
     * The gain of the proportional component.
     */
    private final int pGain;
    /**
     * The gain of the integral component.
     */
    private final int iGain;
    /**
     * The gain of the differential component.
     */
    private final int dGain;
    /**
     * The maximum absolute value of the integral component.
     */
    private final int iWindupGuard;
    /**
     * Integer by which to divide the algorithm's output.
     */
    private final int outputDivisor;

    /**
     * The target ADC value.
     */
    private int targetAdc;

    /**
     * The current integral state.
     */
    private int iState = 0;
    /**
     * The last seen ADC value.
     */
    private int lastAdc = 1024;

    /**
     * Constructs a new PID algorithm instance, using the given algorithm
     * parameters. The initial target ADC value is set to 830.
     *
     * @param pGain	the gain for proportional component of the algorithm
     * @param iGain the gain for integral component of the algorithm
     * @param dGain the gain for derivative component of the algorithm
     * @param iWindupGuard the maximum absolute value of the integral component
     * 						of the algorithm
     * @param outputDivisor integer by which to divide the algorithm's output
     */
    public PID32(int pGain, int iGain, int dGain, int iWindupGuard,
                 int outputDivisor) {
        if (outputDivisor == 0) {
            throw new IllegalArgumentException("outputDivisor cannot be 0");
        }
        this.pGain = pGain;
        this.iGain = iGain;
        this.dGain = dGain;
        this.iWindupGuard = iWindupGuard;
        this.outputDivisor = outputDivisor;
        this.targetAdc = INITIAL_TARGET_ADC;
    }

    /**
     * Calculate the output value of this PID algorithm, based on a given 10-bit
     * ADC value.
     *
     * @param curAdc	a 10-bit ADC value representing the current reading
     * @return			a value between 0 and MAX_RESULT representing the
     * 					relative amount of power to apply in order to reach the
     * 					target ADC value
     */
    public short nextValue(short curAdc) {
        int error = this.targetAdc - curAdc;

        // Calculate the proportional term. This term uses information from the
        // now.
        int pTerm = pGain * error;

        // iState keeps track of the accumulated error.
        this.iState += error;

        // To prevent the iTerm from getting huge because of the limited maximum
        // power of the heating element, we use a 'windup guard'.
        if (iState > iWindupGuard) {
            iState = iWindupGuard;
        } else if (iState < -iWindupGuard) {
            iState = -iWindupGuard;
        }
        // Calculate the integral term. This term uses information from the past.
        int iTerm = iGain * iState;

        // Calculate the differential term. This term uses the difference between
        // the current ADC value and the last ADC value to determine the speed
        // by which the ADC value is changing.
        int dTerm = dGain * (lastAdc - curAdc);
        this.lastAdc = curAdc;

        // Calculate the sum of each term, divided by the output divisor
        int result = (pTerm + iTerm + dTerm)/outputDivisor;

        // Cap the result to [0,MAX_RESULT]
        return (short)Math.max(Math.min(result,MAX_RESULT), 0);
    }

    /**
     * Sets the target ADC value.
     *
     * @param targetAdc	the 10-bit target ADC value
     */
    public void setTargetAdc(short targetAdc) {
        this.targetAdc = targetAdc;
    }


}