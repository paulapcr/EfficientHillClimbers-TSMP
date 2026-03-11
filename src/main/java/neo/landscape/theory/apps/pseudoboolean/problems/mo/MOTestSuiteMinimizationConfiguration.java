package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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
 * {@link Properties}, then builds a configured test-suite minimization problem.
 * </p>
 */
public class MOTestSuiteMinimizationConfiguration implements VectorMKLandscapeConfigurator {

    public static final String INSTANCE_ARGUMENT = "instance";

    /**
     * Declares problem-specific command line flags shown in help for `-problem
     * tsm`.
     * Declares problem-specific command line flags shown in help for `-problem
     * tsm`.
     */
    @Override
    public void prepareOptionsForProblem(Options options) {
        options.addOption(INSTANCE_ARGUMENT, true,
                "path to an instance file in test-centric format (tests requirements; then binary coverage;cost per test)");
        options.addOption(TestingCost.TEST_PROPERTY, true, "number of test cases");
        options.addOption(TestingCost.COSTS_PROPERTY, true, "test case costs (comma or whitespace separated)");
        options.addOption(TestingCoverage.REQUIREMENTS_PROPERTY, true, "number of requirements (optional)");
        options.addOption(TestingCoverage.COVERAGE_PROPERTY, true,
                "coverage matrix rows separated by ';' (each row as sparse test indexes or binary vector)");
        options.addOption(INSTANCE_ARGUMENT, true, "path to instance file (overrides other parameters)");
    }

    /**
     * Converts parsed command line options into properties and delegates
     * creation.
     */
    @Override
    public VectorMKLandscape configureProblem(CommandLine commandLine, PrintStream ps) {
        Properties properties = new Properties();
        Properties properties = new Properties();

        if (commandLine.hasOption(TestingCost.TEST_PROPERTY)) {
        if (commandLine.hasOption(TestingCost.TEST_PROPERTY)) {
            properties.setProperty(TestingCost.TEST_PROPERTY, commandLine.getOptionValue(TestingCost.TEST_PROPERTY));
        }
        if (commandLine.hasOption(TestingCost.COSTS_PROPERTY)) {
        if (commandLine.hasOption(TestingCost.COSTS_PROPERTY)) {
            properties.setProperty(TestingCost.COSTS_PROPERTY, commandLine.getOptionValue(TestingCost.COSTS_PROPERTY));
        }
        if (commandLine.hasOption(TestingCoverage.REQUIREMENTS_PROPERTY)) {
        if (commandLine.hasOption(TestingCoverage.REQUIREMENTS_PROPERTY)) {
            properties.setProperty(
                    TestingCoverage.REQUIREMENTS_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.REQUIREMENTS_PROPERTY));
        }
        if (commandLine.hasOption(TestingCoverage.COVERAGE_PROPERTY)) {
        if (commandLine.hasOption(TestingCoverage.COVERAGE_PROPERTY)) {
            properties.setProperty(
                    TestingCoverage.COVERAGE_PROPERTY,
                    commandLine.getOptionValue(TestingCoverage.COVERAGE_PROPERTY));
        }

        if (commandLine.hasOption(INSTANCE_ARGUMENT)) {
            properties.setProperty(
                    INSTANCE_ARGUMENT,
                    commandLine.getOptionValue(INSTANCE_ARGUMENT));
        }

        System.out.println("CLI instance option = " + commandLine.getOptionValue("instance"));

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

        String instanceFile = properties.getProperty(INSTANCE_ARGUMENT);
        if (instanceFile != null) {
            normalizedProperties.putAll(loadPropertiesFromInstanceFile(instanceFile));
        }

        for (String propertyName : properties.stringPropertyNames()) {
            if (!INSTANCE_ARGUMENT.equals(propertyName)) {
                normalizedProperties.setProperty(propertyName, properties.getProperty(propertyName));
            }
        }

        // default execution
        MOTestSuiteMinimization problem = new MOTestSuiteMinimization();
        problem.setConfiguration(normalizedProperties);

        try (FileWriter fw = new FileWriter("debug_instance.properties")) {
            normalizedProperties.store(fw, "Converted instance");
        } catch (Exception e) {
            System.err.println("Error while writing debug instance file: " + e.getMessage());
        }

        /*
         * if (ps != null) {
         * ps.println("Tests: " +
         * normalizedProperties.getProperty(TestingCost.TEST_PROPERTY));
         * ps.println("Costs: " +
         * normalizedProperties.getProperty(TestingCost.COSTS_PROPERTY));
         * ps.println(
         * "Requirements: " +
         * normalizedProperties.getProperty(TestingCoverage.REQUIREMENTS_PROPERTY,
         * "auto"));
         * ps.println("Coverage: " +
         * normalizedProperties.getProperty(TestingCoverage.COVERAGE_PROPERTY));
         * }
         */

        Properties parsed = loadPropertiesFromInstanceFile(properties.getProperty(INSTANCE_ARGUMENT));
        normalizedProperties.putAll(parsed);

        System.out.println("---- Parsed instance ----");
        System.out.println("tests=" + parsed.getProperty("tests"));
        System.out.println("requirements=" + parsed.getProperty("requirements"));
        System.out.println("costs length=" + parsed.getProperty("costs").split(",").length);
        System.out.println("coverage rows=" + parsed.getProperty("coverage").split(";").length);
        System.out.println("-------------------------");

        return problem;
    }

    private Properties loadPropertiesFromInstanceFile(String instanceFile) {

        System.out.println(">> Loading instance file: " + instanceFile);

        Properties props = new Properties();

        try (BufferedReader br = new BufferedReader(new FileReader(instanceFile))) {

            String header = br.readLine().replace(";", "").trim();
            String[] parts = header.split("\\s+");

            int tests = Integer.parseInt(parts[0]);
            int requirements = Integer.parseInt(parts[1]);

            System.out.println("Header raw: " + header);
            System.out.println("Parsed tests: " + tests);
            System.out.println("Parsed requirements: " + requirements);

            List<boolean[]> coverageMatrix = new ArrayList<>();
            List<Double> costs = new ArrayList<>();

            String line;
            int testIndex = 0;

            while ((line = br.readLine()) != null) {

                String[] split = line.split(";");
                String binary = split[0].trim();
                String cost = split[1].trim();

                if (testIndex < 5) {
                    System.out.println("Test " + testIndex);
                    System.out.println("Binary length: " + binary.length());
                    System.out.println("Binary start: " +
                            binary.substring(0, Math.min(50, binary.length())));
                }

                if (binary.length() != requirements) {
                    System.out.println("WARNING: binary length != requirements");
                }

                boolean[] coverage = new boolean[requirements];

                for (int r = 0; r < requirements; r++) {
                    if (binary.charAt(r) == '1') {
                        coverage[r] = true;
                    }
                }

                coverageMatrix.add(coverage);
                costs.add(Double.parseDouble(cost));

                testIndex++;
            }

            System.out.println("Tests before preprocessing: " + coverageMatrix.size());

            removeDominatedTests(coverageMatrix, costs);

            System.out.println("Tests after preprocessing: " + coverageMatrix.size());

            tests = coverageMatrix.size();

            /*
             * -------------------------------------------------
             * Build requirement → tests mapping
             * -------------------------------------------------
             */

            List<List<Integer>> requirementToTests = new ArrayList<>();
            for (int r = 0; r < requirements; r++) {
                requirementToTests.add(new ArrayList<>());
            }

            for (int t = 0; t < coverageMatrix.size(); t++) {

                boolean[] coverage = coverageMatrix.get(t);

                for (int r = 0; r < requirements; r++) {
                    if (coverage[r]) {
                        requirementToTests.get(r).add(t);
                    }
                }
            }

            /*
             * -------------------------------------------------
             * Remove trivial requirements
             * -------------------------------------------------
             */

            List<Integer> trivialReqs = findTrivialRequirements(requirementToTests, tests);

            System.out.println("Removing trivial requirements: " + trivialReqs.size());

            coverageMatrix = removeRequirements(coverageMatrix, trivialReqs, requirements);
            requirements = requirements - trivialReqs.size();

            /*
             * -------------------------------------------------
             * Rebuild requirement → tests after removal
             * -------------------------------------------------
             */

            requirementToTests.clear();
            for (int r = 0; r < requirements; r++) {
                requirementToTests.add(new ArrayList<>());
            }

            for (int t = 0; t < coverageMatrix.size(); t++) {

                boolean[] coverage = coverageMatrix.get(t);

                for (int r = 0; r < requirements; r++) {
                    if (coverage[r]) {
                        requirementToTests.get(r).add(t);
                    }
                }
            }

            /*
             * -------------------------------------------------
             * Build coverage property
             * -------------------------------------------------
             */

            StringBuilder coverageBuilder = new StringBuilder();

            for (int r = 0; r < requirements; r++) {

                if (r > 0) {
                    coverageBuilder.append(";");
                }

                List<Integer> list = requirementToTests.get(r);

                for (int i = 0; i < list.size(); i++) {

                    if (i > 0) {
                        coverageBuilder.append(",");
                    }

                    coverageBuilder.append(list.get(i));
                }
            }

            /*
             * -------------------------------------------------
             * Build costs property
             * -------------------------------------------------
             */

            StringBuilder costsBuilder = new StringBuilder();

            for (int i = 0; i < costs.size(); i++) {

                if (i > 0) {
                    costsBuilder.append(",");
                }

                costsBuilder.append(costs.get(i));
            }

            props.setProperty("tests", String.valueOf(tests));
            props.setProperty("requirements", String.valueOf(requirements));
            props.setProperty("coverage", coverageBuilder.toString());
            props.setProperty("costs", costsBuilder.toString());

            System.out.println("Loaded instance:");
            System.out.println("tests=" + tests);
            System.out.println("requirements=" + requirements);

            analyzeInstance(requirementToTests, tests);

            return props;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<boolean[]> removeRequirements(
            List<boolean[]> coverageMatrix,
            List<Integer> trivialReqs,
            int requirements) {

        boolean[] remove = new boolean[requirements];

        for (int r : trivialReqs) {
            remove[r] = true;
        }

        List<boolean[]> newMatrix = new ArrayList<>();

        for (boolean[] oldCoverage : coverageMatrix) {

            boolean[] newCoverage = new boolean[requirements - trivialReqs.size()];

            int idx = 0;

            for (int r = 0; r < requirements; r++) {

                if (!remove[r]) {
                    newCoverage[idx++] = oldCoverage[r];
                }
            }

            newMatrix.add(newCoverage);
        }

        return newMatrix;
    }

    private void analyzeInstance(List<List<Integer>> requirementToTests, int tests) {

        int requirements = requirementToTests.size();

        int maxMask = 0;
        int minMask = Integer.MAX_VALUE;
        int sumMask = 0;

        for (List<Integer> list : requirementToTests) {
            int size = list.size();
            maxMask = Math.max(maxMask, size);
            minMask = Math.min(minMask, size);
            sumMask += size;
        }

        double avgMask = sumMask / (double) requirements;

        System.out.println("----- INSTANCE ANALYSIS -----");
        System.out.println("Tests: " + tests);
        System.out.println("Requirements: " + requirements);
        System.out.println("Min tests per requirement: " + minMask);
        System.out.println("Max tests per requirement: " + maxMask);
        System.out.println("Avg tests per requirement: " + avgMask);
        System.out.println("-----------------------------");
    }

    private void removeDominatedTests(List<boolean[]> coverageMatrix, List<Double> costs) {

        int n = coverageMatrix.size();
        boolean[] dominated = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (dominated[i])
                continue;

            for (int j = 0; j < n; j++) {
                if (i == j || dominated[j])
                    continue;

                if (costs.get(i) <= costs.get(j) &&
                        coversAll(coverageMatrix.get(i), coverageMatrix.get(j))) {

                    dominated[j] = true;
                }
            }
        }

        for (int i = dominated.length - 1; i >= 0; i--) {
            if (dominated[i]) {
                coverageMatrix.remove(i);
                costs.remove(i);
            }
        }
    }

    private boolean coversAll(boolean[] a, boolean[] b) {

        for (int r = 0; r < a.length; r++) {
            if (b[r] && !a[r]) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> findTrivialRequirements(List<List<Integer>> requirementToTests, int tests) {

        List<Integer> trivial = new ArrayList<>();

        for (int r = 0; r < requirementToTests.size(); r++) {

            int size = requirementToTests.get(r).size();

            if (size == 0 || size == tests) {
                trivial.add(r);
            }
        }

        return trivial;
    }

    private void removeRequirements(List<boolean[]> coverageMatrix, List<Integer> trivialReqs) {

        for (boolean[] coverage : coverageMatrix) {

            for (int r : trivialReqs) {
                coverage[r] = false;
            }
        }
    }

}
