package neo.landscape.theory.apps.pseudoboolean.problems.mo;

import neo.landscape.theory.apps.pseudoboolean.PBSolution;
import neo.landscape.theory.apps.pseudoboolean.problems.EmbeddedLandscape;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Objective function for test-suite minimization that rewards requirement
 * coverage.
 *
 * <p>
 * There is one subfunction per requirement. A requirement contributes
 * {@code 1.0} if at least one selected test covering that requirement is
 * active, and {@code 0.0} otherwise.</p>
 */
public class TestingCoverage extends EmbeddedLandscape {

    // Number of tests in the suite
    public static final String TESTS_PROPERTY = "tests";
    // Number of requirements (optional. If omitted, inferred from coverage rows).
    public static final String REQUIREMENTS_PROPERTY = "requirements";
    // Coverage matrix encoded by rows (one row per requirement).
    public static final String COVERAGE_PROPERTY = "coverage";

    private static final String ROW_SEPARATOR_REGEX = "\\s*;\\s*|\\s*\\n\\s*";
    private static final String COLUMN_SEPARATOR_REGEX = "\\s*,\\s*|\\s+";

    /**
     * Evaluates one requirement from a subsolution containing only variables in
     * that requirement mask.
     */
    @Override
    public double evaluateSubfunction(int sf, PBSolution pbs) {
        for (int var = 0; var < masks[sf].length; var++) {
            if (pbs.getBit(var) == 1) {
                return 1.0;
            }
        }
        return 0.0;
    }

    /**
     * Shortcut evaluation for binary value used by internal incremental
     * machinery.
     */
    @Override
    public double evaluateSubfunction(int sf, int value) {
        return value == 0 ? 0.0 : 1.0;
    }

    /**
     * Serializes tests/requirements/coverage in properties format.
     */
    @Override
    public void writeInstance(Writer writer) {
        // TODO
        try {
            writer.write(TESTS_PROPERTY + "=" + n + System.lineSeparator());
            writer.write(REQUIREMENTS_PROPERTY + "=" + m + System.lineSeparator());
            writer.write(COVERAGE_PROPERTY + "=");

            for (int requirement = 0; requirement < m; requirement++) {
                if (requirement > 0) {
                    writer.write(";");
                }
                for (int i = 0; i < masks[requirement].length; i++) {
                    if (i > 0) {
                        writer.write(",");
                    }
                    writer.write(Integer.toString(masks[requirement][i]));
                }
            }
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException("Error writing testing coverage instance", e);
        }
    }

    /**
     * Loads objective configuration from properties.
     *
     * <p>
     * {@code coverage} supports two row formats:</p>
     * <ul>
     * <li>Binary row with {@code tests} columns, e.g. {@code 0 1 0 1}.</li>
     * <li>Sparse row with covered test indexes, e.g. {@code 1,3}.</li>
     * </ul>
     */
    @Override
    public void setConfiguration(Properties prop) {
        // TODO
        String tests = prop.getProperty(TESTS_PROPERTY);
        if (tests == null) {
            throw new RuntimeException("Missing test property: " + TESTS_PROPERTY);
        }

        n = Integer.parseInt(tests);

        String coverageDescription = prop.getProperty(COVERAGE_PROPERTY);
        if (coverageDescription == null) {
            throw new RuntimeException("Missing coverage property: " + COVERAGE_PROPERTY);
        }

        String[] rows = coverageDescription.trim().isEmpty()
                ? new String[0]
                : coverageDescription.trim().split(ROW_SEPARATOR_REGEX);

        String requirementsProperty = prop.getProperty(REQUIREMENTS_PROPERTY);
        if (requirementsProperty != null) {
            m = Integer.parseInt(requirementsProperty);
            if (m != rows.length) {
                throw new IllegalArgumentException("Mismatch between requirements and rows due to Property " + REQUIREMENTS_PROPERTY);
            }
        } else {
            m = rows.length;
        }

        // one subfunction per requirement. Each mask contains test indexes that cover that requirement.
        masks = new int[m][];

        for (int requirements = 0; requirements < m; requirements++) {
            masks[requirements] = parseCoverageRow(rows[requirements], n, requirements);
        }

    }

    /**
     * Parses one requirement row in either binary or sparse notation.
     */
    private int[] parseCoverageRow(String row, int numberOfTests, int requirement) {
        String trimmedRow = row.trim();
        if (trimmedRow.isEmpty()) {
            return new int[0];
        }

        String[] tokens = trimmedRow.split(COLUMN_SEPARATOR_REGEX);
        if (isBinaryRow(tokens, numberOfTests)) {
            return parseBinaryRow(tokens);
        }

        return parseSparseRow(tokens, numberOfTests, requirement);
    }

    /**
     * Checks if a row is binary (length == numberOfTests and only 0/1 tokens).
     */
    private boolean isBinaryRow(String[] tokens, int numberOfTests) {
        if (tokens.length != numberOfTests) {
            return false;
        }

        for (String token : tokens) {
            if (!"0".equals(token) && !"1".equals(token)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a binary row into sparse mask indexes.
     */
    private int[] parseBinaryRow(String[] tokens) {
        int coveredTests = 0;
        for (String token : tokens) {
            if ("1".equals(token)) {
                coveredTests++;
            }
        }

        int[] parsedMask = new int[coveredTests];
        int index = 0;
        for (int test = 0; test < tokens.length; test++) {
            if ("1".equals(tokens[test])) {
                parsedMask[index++] = test;
            }
        }

        return parsedMask;
    }

    /**
     * Parses sparse coverage row, validates bounds and removes duplicated test
     * indexes.
     */
    private int[] parseSparseRow(String[] tokens, int numberOfTests, int requirement) {
        boolean[] alreadyAdded = new boolean[numberOfTests];
        List<Integer> testCases = new ArrayList<Integer>(tokens.length);

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }

            int test = Integer.parseInt(token);
            if (test < 0 || test >= numberOfTests) {
                throw new IllegalArgumentException("Invalid test: " + test + " for requirement: " + requirement + ". The index must be in [0, "
                        + (numberOfTests - 1) + "]");
            }

            if (!alreadyAdded[test]) {
                testCases.add(test);
                alreadyAdded[test] = true;
            }
        }

        int[] parsedMask = new int[testCases.size()];
        for (int i = 0; i < testCases.size(); i++) {
            parsedMask[i] = testCases.get(i);
        }

        return parsedMask;
    }
}
