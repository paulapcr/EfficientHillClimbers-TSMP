package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import neo.landscape.theory.apps.pseudoboolean.PBSolution;
import neo.landscape.theory.apps.pseudoboolean.problems.EmbeddedLandscape;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class TestingCost extends EmbeddedLandscape {

    public static final String TEST_PROPERTY = "tests";
    public static final String COSTS_PROPERTY = "costs";

    private static final String COST_SEPARATOR_REGEX = "\\s*,\\s*|\\s+";
    private double [] costs;

    @Override
    public double evaluateSubfunction(int sf, PBSolution pbs) {
        return -costs[sf] * (pbs.getBit(0));
    }

    @Override
    public double evaluateSubfunction(int sf, int value) {
        return -costs[sf] * (value == 0 ? 0 : 1);
    }


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

        masks = new int[m][];
        for(int i = 0; i < m; i++){
            masks[i] = new int[] { i };
        }
    }
}
