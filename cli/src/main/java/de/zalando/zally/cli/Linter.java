package de.zalando.zally.cli;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class Linter {
    public static final List<String> violationTypes = Arrays.asList("must", "should", "could", "hint");

    private final ZallyApiClient client;
    private final ResultPrinter printer;
    private final String mustViolationType = "must";

    public Linter(ZallyApiClient client, ResultPrinter printer) {
        this.client = client;
        this.printer = printer;
    }

    public boolean lint(SpecsReader reader) throws IOException, CliException {
        final RequestDecorator decorator = new RequestDecorator(reader);
        final ZallyApiResponse response = client.validate(decorator.getRequestBody());
        final ViolationsFilter violationsFilter = new ViolationsFilter(response.getViolations());

        if (response.getMessage() != null) {
            printer.printMessage(response.getMessage());
        }

        boolean hasMustViolations = false;
        for (String violationType : violationTypes) {
            List<Violation> violations = violationsFilter.getViolations(violationType);
            if (mustViolationType.equals(violationType)) {
                hasMustViolations = violations.isEmpty();
            }
            printer.printViolations(violations, violationType);
        }

        printer.printSummary(violationTypes, response.getCounters());

        return hasMustViolations;
    }
}

