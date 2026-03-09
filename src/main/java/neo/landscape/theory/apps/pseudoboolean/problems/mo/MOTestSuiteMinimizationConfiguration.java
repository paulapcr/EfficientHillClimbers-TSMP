package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * CLI/configuration adapter for {@link MOTestSuiteMinimization}.
 *
 * <p>
 * Collects problem parameters from command line options and/or direct
 * {@link Properties}, then builds a configured test-suite minimization
 * problem.</p>
 */
public class MOTestSuiteMinimizationConfiguration implements VectorMKLandscapeConfigurator {

    public static final String INSTANCE_ARGUMENT = "instance";

    /**
     * Declares problem-specific command line flags shown in help for `-problem
     * tsm`.
     */
    @Override
    public void prepareOptionsForProblem(Options options) {
        options.addOption(INSTANCE_ARGUMENT, true, "path to an instance file in test-centric format (tests requirements; then binary coverage;cost per test)");
        options.addOption(TestingCost.TEST_PROPERTY, true, "number of test cases");
        options.addOption(TestingCost.COSTS_PROPERTY, true, "test case costs (comma or whitespace separated)");
        options.addOption(TestingCoverage.REQUIREMENTS_PROPERTY, true, "number of requirements (optional)");
        options.addOption(TestingCoverage.COVERAGE_PROPERTY, true, "coverage matrix rows separated by ';' (each row as sparse test indexes or binary vector)");
    }

    /**
     * Converts parsed command line options into properties and delegates
     * creation.
     */
    @Override
    public VectorMKLandscape configureProblem(CommandLine commandLine, PrintStream ps) {
        Properties properties = new Properties();

        if (commandLine.hasOption(TestingCost.TEST_PROPERTY)) {
            properties.setProperty(TestingCost.TEST_PROPERTY, commandLine.getOptionValue(TestingCost.TEST_PROPERTY));
        }
        if (commandLine.hasOption(TestingCost.COSTS_PROPERTY)) {
            properties.setProperty(TestingCost.COSTS_PROPERTY, commandLine.getOptionValue(TestingCost.COSTS_PROPERTY));
        }
        if (commandLine.hasOption(TestingCoverage.REQUIREMENTS_PROPERTY)) {
            properties.setProperty(
                    TestingCoverage.REQUIREMENTS_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.REQUIREMENTS_PROPERTY)
            );
        }
        if (commandLine.hasOption(TestingCoverage.COVERAGE_PROPERTY)) {
            properties.setProperty(
                    TestingCoverage.COVERAGE_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.COVERAGE_PROPERTY)
            );
        }

        if (commandLine.hasOption(INSTANCE_ARGUMENT)) {
            properties.setProperty(
                    INSTANCE_ARGUMENT,
                    commandLine.getOptionValue(INSTANCE_ARGUMENT)
            );
        }

        return configureProblem(properties, ps);

    }

    /**
     * Creates and configures a full bi-objective test-suite minimization
     * problem. Optionally prints the resolved configuration in the experiment
     * output stream.
     */
    @Override
    public VectorMKLandscape configureProblem(Properties properties, PrintStream ps) {

        Properties normalizedProperties = new Properties();

        if (properties.containsKey(INSTANCE_ARGUMENT)) {
            normalizedProperties.putAll(
                    loadPropertiesFromInstanceFile(
                            properties.getProperty(INSTANCE_ARGUMENT)
                    )
            );
        }

        for (String propertyName : properties.stringPropertyNames()) {
            if (!INSTANCE_ARGUMENT.equals(propertyName)) {
                normalizedProperties.setProperty(
                        propertyName,
                        properties.getProperty(propertyName)
                );
            }
        }

        MOTestSuiteMinimization problem = new MOTestSuiteMinimization();
        System.out.println(">> normalizedProperties keys = " + normalizedProperties.keySet());
        problem.setConfiguration(normalizedProperties);

        if (ps != null) {
            ps.println("Tests: " + normalizedProperties.getProperty(TestingCost.TEST_PROPERTY));
            ps.println("Costs: " + normalizedProperties.getProperty(TestingCost.COSTS_PROPERTY));
            ps.println("Requirements: " + normalizedProperties.getProperty(TestingCoverage.REQUIREMENTS_PROPERTY, "auto"));
            ps.println("Coverage: " + normalizedProperties.getProperty(TestingCoverage.COVERAGE_PROPERTY));
        }

        return problem;
    }

    //  wip !!
    // this method transposes an instance file format (which is test-centric)
    // into the properties format (which is requirement-centric) 
    private Properties loadPropertiesFromInstanceFile(String instanceFile) {

        System.out.println(">> Loading instance file: " + instanceFile);
        Properties props = new Properties();

        try (BufferedReader br = new BufferedReader(new FileReader(instanceFile))) {

            String header = br.readLine().replace(";", "").trim();
            String[] parts = header.split("\\s+");

            int tests = Integer.parseInt(parts[0]);
            int requirements = Integer.parseInt(parts[1]);

            props.setProperty("tests", String.valueOf(tests));
            props.setProperty("requirements", String.valueOf(requirements));

            // here we start to transpose the instance format *****************************************
            List<List<Integer>> requirementToTests = new ArrayList<>();

            for (int r = 0; r < requirements; r++) {
                requirementToTests.add(new ArrayList<>());
            }

            StringBuilder costsBuilder = new StringBuilder();

            String line;
            int testIndex = 0;

            while ((line = br.readLine()) != null) {

                String[] split = line.split(";");
                String binary = split[0].trim();
                String cost = split[1].trim();

                // append cost
                if (testIndex > 0) {
                    costsBuilder.append(",");
                }
                costsBuilder.append(cost);

                // map requirement to tests
                for (int r = 0; r < binary.length(); r++) {
                    if (binary.charAt(r) == '1') {
                        requirementToTests.get(r).add(testIndex);
                    }
                }

                testIndex++;
            }

            // build coverage property
            StringBuilder coverageBuilder = new StringBuilder();

            for (int r = 0; r < requirements; r++) {

                if (r > 0) {
                    coverageBuilder.append(";");
                }

                List<Integer> testsCovering = requirementToTests.get(r);

                for (int i = 0; i < testsCovering.size(); i++) {

                    if (i > 0) {
                        coverageBuilder.append(",");
                    }
                    coverageBuilder.append(testsCovering.get(i));
                }
            }

            props.setProperty("coverage", coverageBuilder.toString());
            props.setProperty("costs", costsBuilder.toString());

            System.out.println("Loaded instance:");
            System.out.println("tests=" + tests);
            System.out.println("requirements=" + requirements);

            return props;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
