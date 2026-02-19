package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintStream;
import java.util.Properties;

public class MOTestSuiteMinimizationConfiguration implements VectorMKLandscapeConfigurator {
    @Override
    public void prepareOptionsForProblem(Options options) {
        options.addOption(TestingCost.TEST_PROPERTY, true, "number of test cases");
        options.addOption(TestingCost.COSTS_PROPERTY, true, "test case costs (comma or whitespace separated)");
        options.addOption(TestingCoverage.REQUIREMENTS_PROPERTY, true, "number of requirements (optional)");
        options.addOption(TestingCoverage.COVERAGE_PROPERTY, true, "coverage matrix rows separated by ';' (each row as sparse test indexes or binary vector)");
    }

    @Override
    public VectorMKLandscape configureProblem(CommandLine commandLine, PrintStream ps) {
        Properties properties =  new Properties();
        if(commandLine.hasOption(TestingCost.TEST_PROPERTY)) {
            properties.setProperty(TestingCost.TEST_PROPERTY, commandLine.getOptionValue(TestingCost.TEST_PROPERTY));
        }
        if(commandLine.hasOption(TestingCost.COSTS_PROPERTY)) {
            properties.setProperty(TestingCost.COSTS_PROPERTY, commandLine.getOptionValue(TestingCost.COSTS_PROPERTY));
        }
        if(commandLine.hasOption(TestingCoverage.REQUIREMENTS_PROPERTY)) {
            properties.setProperty(
                    TestingCoverage.REQUIREMENTS_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.REQUIREMENTS_PROPERTY)
            );
        }
        if(commandLine.hasOption(TestingCoverage.COVERAGE_PROPERTY)) {
            properties.setProperty(
                    TestingCoverage.REQUIREMENTS_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.COVERAGE_PROPERTY)
            );
        }

        return configureProblem(properties, ps);

    }

    @Override
    public VectorMKLandscape configureProblem(Properties properties, PrintStream ps) {
        MOTestSuiteMinimization problem = new MOTestSuiteMinimization();
        problem.setConfiguration(properties);

        if (ps != null) {
            ps.println("Tests: "+ properties.getProperty(TestingCost.TEST_PROPERTY));
            ps.println("Costs: "+ properties.getProperty(TestingCost.COSTS_PROPERTY));
            ps.println("Requirements: "+ properties.getProperty(TestingCoverage.REQUIREMENTS_PROPERTY, "auto"));
            ps.println("Coverage: "+ properties.getProperty(TestingCoverage.COVERAGE_PROPERTY));
        }

        return problem;
    }
}
