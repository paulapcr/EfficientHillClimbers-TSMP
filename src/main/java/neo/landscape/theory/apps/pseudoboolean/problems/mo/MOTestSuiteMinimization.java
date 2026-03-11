package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import java.util.Properties;

import neo.landscape.theory.apps.pseudoboolean.problems.EmbeddedLandscape;

/**
 * Multi-objective Test Suite Minimization problem.
 *
 * <p>
 * This container combines two embedded landscapes:
 * <ul>
 * <li>{@link TestingCost}: minimize selected test costs (implemented as
 * maximize negative cost).</li>
 * <li>{@link TestingCoverage}: maximize covered requirements.</li>
 * </ul>
 * Both objectives share the same decision variables: one bit per test case.
 * </p>
 */
public class MOTestSuiteMinimization extends VectorMKLandscape {

    /**
     * Configures both objectives from the same properties and registers them as a
     * bi-objective problem.
     */
    @Override
    public void setConfiguration(Properties prop) {

        TestingCost testingCost = new TestingCost();
        testingCost.setConfiguration(prop);

        TestingCoverage testingCoverage = new TestingCoverage();
        testingCoverage.setConfiguration(prop);

        configureEmbeddedLandscapes(new EmbeddedLandscape[] {
                testingCoverage,
                testingCost
        });
    }

}
