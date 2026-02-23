package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import neo.landscape.theory.apps.pseudoboolean.PBSolution;
import neo.landscape.theory.apps.pseudoboolean.problems.EmbeddedLandscape;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * Objective function for test-suite minimization that rewards low execution cost.
 *
 * <p>The framework maximizes objective values. To model cost minimization,
 * this objective returns negative values: selecting a test with cost {@code c}
 * contributes {@code -c}. As a consequence, maximizing this objective is
 * equivalent to minimizing total cost.</p>
 */
public class TestingCost extends EmbeddedLandscape {

    // Number of tests in the test suite.
    public static final String TEST_PROPERTY = "tests";
    // Cost list for all tests.
    public static final String COSTS_PROPERTY = "costs";

    private static final String COST_SEPARATOR_REGEX = "\\s*,\\s*|\\s+";
    private double [] costs;

    /**
     * Evaluates one subfunction (one test) from a subsolution of length 1.
     */
    @Override
    public double evaluateSubfunction(int sf, PBSolution pbs) {
        return -costs[sf] * (pbs.getBit(0));
    }


    /**
     * Same as {@link #evaluateSubfunction(int, PBSolution)} but using the binary value directly.
     */
    @Override
    public double evaluateSubfunction(int sf, int value) {
        return -costs[sf] * (value == 0 ? 0 : 1);
    }


    /**
     * Serializes the objective configuration using standard properties format.
     */
    @Override
    public void writeInstance(Writer writer) {
        try{
            writer.write(TEST_PROPERTY + "=" + n + System.lineSeparator());
            writer.write(COSTS_PROPERTY + "=");
            for(int i = 0; i < costs.length; i++){
                if(i>0){
                    writer.write(",");
                }
                writer.write(Double.toString(costs[i]));
            }
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("Error while writing instance", e);
        }

    }

    /**
     * Loads objective configuration from properties.
     *
     * <ul>
     *   <li>{@code tests}: number of tests.</li>
     *   <li>{@code costs}: list of test costs (comma/space separated) with exact size {@code tests}.</li>
     * </ul>
     */
    @Override
    public void setConfiguration(Properties prop) {
        /* TODO: read costs
        m = Integer.parseInt(prop.getProperty("tests"));
        n = m;
        masks = new int[m][];
        for (int i = 0; i < m; i++) {
            masks[i] = new int[] {i};

         */

        String testsProperty = prop.getProperty(TEST_PROPERTY);
        if(testsProperty == null){
            throw new IllegalArgumentException("The tests property is null");
        }

        n = Integer.parseInt(testsProperty);
        m = n;

        String costsProperty = prop.getProperty(COSTS_PROPERTY);
        if (costsProperty == null) {
            throw new IllegalArgumentException("The costs property is null");
        }

        String[] tokens = costsProperty.trim().isEmpty() ? new String[0] : costsProperty.trim().split(COST_SEPARATOR_REGEX);

        if(tokens.length != n){
            throw new IllegalArgumentException("Property "+COSTS_PROPERTY+" must containt exactly "+n+" values");
        }

        costs = new double[n];
        for (int i = 0; i < n; i++) {
            costs[i] = Double.parseDouble(tokens[i]);
        }

        // One subfunction per test. Each subfunction depends on a single variable (that test selection bit).
        masks = new int[m][];
        for(int i = 0; i < m; i++){
            masks[i] = new int[] { i };
        }
    }
}
