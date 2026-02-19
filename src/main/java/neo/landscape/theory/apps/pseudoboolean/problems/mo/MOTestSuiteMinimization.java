package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import neo.landscape.theory.apps.pseudoboolean.problems.EmbeddedLandscape;

import java.util.Properties;

public class MOTestSuiteMinimization extends VectorMKLandscape {

    @Override
    public void setConfiguration(Properties prop) {

        TestingCost testingCost =  new TestingCost();
        testingCost.setConfiguration(prop);

        TestingCoverage testingCoverage =  new TestingCoverage();
        testingCoverage.setConfiguration(prop);



        configureEmbeddedLandscapes(new EmbeddedLandscape[]{testingCost,testingCoverage});

    }

}
