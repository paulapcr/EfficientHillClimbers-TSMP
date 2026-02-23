package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;


/**
 * CLI/configuration adapter for {@link MOTestSuiteMinimization}.
 *
 * <p>Collects problem parameters from command line options and/or direct
 * {@link Properties}, then builds a configured test-suite minimization problem.</p>
 */
public class MOTestSuiteMinimizationConfiguration implements VectorMKLandscapeConfigurator {

    public static final String INSTANCE_ARGUMENT = "instance";

    /**
     * Declares problem-specific command line flags shown in help for `-problem tsm`.
     */
    @Override
    public void prepareOptionsForProblem(Options options) {

        options.addOption(TestingCost.TEST_PROPERTY, true, "number of test cases");
        options.addOption(TestingCost.COSTS_PROPERTY, true, "test case costs (comma or whitespace separated)");
        options.addOption(TestingCoverage.REQUIREMENTS_PROPERTY, true, "number of requirements (optional)");
        options.addOption(TestingCoverage.COVERAGE_PROPERTY, true, "coverage matrix rows separated by ';' (each row as sparse test indexes or binary vector)");
    }

    /**
     * Converts parsed command line options into properties and delegates creation.
     */
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
                    TestingCoverage.COVERAGE_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.COVERAGE_PROPERTY)
            );
        }

        return configureProblem(properties, ps);

    }

    /**
     * Creates and configures a full bi-objective test-suite minimization problem.
     * Optionally prints the resolved configuration in the experiment output stream.
     */
    @Override
    public VectorMKLandscape configureProblem(Properties properties, PrintStream ps) {

        // from file.properties execution (not working)
        Properties normalizedProperties = new Properties();
        if(properties.containsKey(INSTANCE_ARGUMENT)) {
            normalizedProperties.putAll(loadPropertiesFromInstanceFile(properties.getProperty(INSTANCE_ARGUMENT)));
        }

        for(String propertyName : properties.stringPropertyNames()) {
            if(!INSTANCE_ARGUMENT.equals(propertyName)) {
                normalizedProperties.setProperty(propertyName, properties.getProperty(propertyName));
            }
        }

        // default execution
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

    private Properties loadPropertiesFromInstanceFile(String instanceFile){
        Properties loadedProperties = new Properties();
        try(InputStream input = new FileInputStream(instanceFile)){
            loadedProperties.load(input);
            return loadedProperties;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
