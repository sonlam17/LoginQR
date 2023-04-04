package com.secsign.java.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * helper class to check Public Suffix
 *
 */
public class SecSignIDPublicSuffixInterpreter {
    private static class Rule {
        /**
         * The plain rule string.
         */
        private final String ruleString;

        /**
         * Constructor to create a rule with the plain rule string.
         * @param ruleString the plain rule string
         */
        public Rule(String ruleString) {
            this.ruleString = ruleString;
        }

        /**
         * Checks whether the rule is matching with the host.
         * @param host the host
         * @return true if the rule is matching, otherwise false
         */
        public boolean isMatching(String host) {
            String regex = getPublicSuffixRegex();
            return host.matches(regex);
        }

        /**
         * Get the public suffix of the rule.
         * @return the public suffix of the rule
         */
        public String getPublicSuffix() {
            if (isException()) {
                return ruleString.replaceFirst("!", "");
            }

            return ruleString;
        }

        /**
         * Get all labels of the public suffix.
         * @return the labels of the public suffix
         */
        public String[] getLabels() {
            return getPublicSuffix().split("\\.");
        }

        /**
         * Get the label count of the public suffix.
         * @return the label count of the public suffix
         */
        public int getLabelCount() {
            int labelCount = 0;
            for (char c : ruleString.toCharArray()) {
                if (c == '.') {
                    labelCount++;
                }
            }

            return labelCount;
        }

        /**
         * Checks whether the rule is a exception or not.
         * @return true if the rule is a exception, otherwise false
         */
        public boolean isException() {
            return ruleString.startsWith("!");
        }

        /**
         * Checks whether the rule contains a wildcard or not.
         * @return true if the rule contains a wildcard, otherwise false
         */
        public boolean isWildcard() {
            return ruleString.contains("*");
        }

        /**
         * Get the priority for the rule.
         * The priority is calculated by the primary factor (is the rule a exception)
         * and the secondary factor (how many labels the rule have).
         * @return the priority of the rule
         */
        public int getPriority() {
            int priority = getLabelCount();
            if (isException()) {
                priority += 100_000;
            }

            return priority;
        }

        /**
         * Get the public suffix regex to match a host.
         * @return the public suffix regex
         */
        private String getPublicSuffixRegex() {
            String regex = getPublicSuffix().replace(".", "\\.");
            if (isWildcard()) {
                regex = regex.replace("*", ".*?");
                regex = "(" + regex + ")";
            }

            return ".*?" + regex + "$";
        }
    }

    /**
     * Logger for this class
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecSignIDPublicSuffixInterpreter.class);

    /**
     * Static In-Memory Cache to faster retrieve the registered domain which were queried before.
     * e.g. [host: "portal.secsign.com", registerDomain: "portal.secsign"] is saved.
     */
    private static final Map<String, String> registeredDomainCache = new HashMap<>();

    /**
     * Rules which are loaded from the public_suffix_list.dat file.
     */
    private final List<Rule> rules = new ArrayList<>();

    /**
     * Get the registered domain of the specified URI.
     * @param uri the URI to find the registered domain for
     * @return the registered domain or null if there's no registered domain
     */
    public String getRegisteredDomain(URI uri) {
        String host = uri.getHost();
        if (host == null) {
            host = uri.getPath();
        }

        if (host == null || host.length() == 0 || host.startsWith(".")) {
            return null;
        }

        if (!host.contains(".")) {
            return host;
        }

        host = host.toLowerCase();

        // Check if cache contains host and the registered domain.
        String registeredDomainCacheValue = registeredDomainCache.getOrDefault(host, null);
        if (registeredDomainCacheValue != null) {
            return registeredDomainCacheValue;
        }

        loadRulesIfNecessary();
        List<Rule> matchingRules = new ArrayList<>();

        // 1. Match domain against all rules and take note of the matching ones.
        for (Rule rule : rules) {
            if (rule.isMatching(host)) {
                matchingRules.add(rule);
            }
        }

        // 2. If no rules match, the prevailing rule is "*".
        if (matchingRules.size() == 0) {
            // Unlisted TLD
            String[] splittedHost = host.split("\\.");
            if (splittedHost.length >= 2) {
                return splittedHost[ splittedHost.length - 2] + "." + splittedHost[splittedHost.length - 1];
            }

            return host;
        }

        // 3. If more than one rule matches, the prevailing rule is the one which is an exception rule.
        // 4. If there is no matching exception rule, the prevailing rule is the one with the most labels.
        Rule selectedRule = matchingRules.get(0);
        for (Rule rule : matchingRules) {
            if (rule.getPriority() > selectedRule.getPriority()) {
                selectedRule = rule;
            }
        }

        // 5. If the prevailing rule is a exception rule, modify it by removing the leftmost label.
        String ruleLabels = selectedRule.getPublicSuffix();
        if (selectedRule.isException()) {
            String leftMostLabel = selectedRule.getLabels()[0];
            ruleLabels = ruleLabels.replace(leftMostLabel + ".", "");
        }

        // 6. The public suffix is the set of labels from the domain which
        //    match the labels of the prevailing rule, using the matching algorithm above.
        String publicSuffix;
        if (selectedRule.isWildcard()) {
            String[] hostLabelsSplitted = host.split("\\.");
            String[] ruleLabelsSplitted = ruleLabels.split("\\.");

            // Replace all wildcard by their real values provided by the host
            publicSuffix = ruleLabelsSplitted[ruleLabelsSplitted.length - 1];
            for(int i = ruleLabelsSplitted.length - 2; i >= 0; i--) {
                String ruleLabel = ruleLabelsSplitted[i];
                if (ruleLabel.equals("*")) {
                    int relativeIndex = ruleLabelsSplitted.length - i;
                    ruleLabel = hostLabelsSplitted[hostLabelsSplitted.length - relativeIndex];
                }

                publicSuffix = ruleLabel + "." + publicSuffix;
            }
        } else {
            publicSuffix = ruleLabels;
        }

        // 7. The registered or registrable domain is the public suffix plus one additional label.
        // NOTE Yes but not really. A public suffix also can be a registered domain, so no additional label might be needed.
        if (host.equals(publicSuffix)) {
            
            return host;
        }

        String[] hostLabels = host.replace("." + publicSuffix, "").split("\\.");
        String registeredDomain = hostLabels[hostLabels.length - 1] + "." + publicSuffix;

        registeredDomainCache.put(host, registeredDomain);
        return registeredDomain;
    }

    /**
     * Load all rules from the public_suffix_list.dat file if not loaded yet.
     */
    private void loadRulesIfNecessary() {
        if (rules.size() == 0) {
            logger.debug("Trying to load public suffix list.");

            InputStream in = getClass().getResourceAsStream("/public_suffix_list.dat");
            if (in == null) {
                logger.warn("Couldn't load public suffix list.");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            List<String> lines = reader.lines().collect(Collectors.toList());
            rules.addAll(interpretLinesToRules(lines));

            logger.debug("Successfully loaded public suffix list.");
        }
    }

    /**
     * Interpret the lines to rules.
     * @param lines the lines
     * @return the rules
     */
    private List<Rule> interpretLinesToRules(List<String> lines) {
        List<Rule> rulesObject = new ArrayList<>();
        // Specification 1: The list is a set of rules, with one rule per line.

        // Specification 2: Each line is only read up to the first whitespace; entire lines can also be commented using //.
        // Specification 3: Each line which is not entirely whitespace or begins with a comment contains a rule.
        for (String line : lines) {
            // Skip if line starts with "//".
            if (line.startsWith("//")) {
                continue;
            }

            // Only read until the first whitespace
            String[] splittedLine = line.split(" ");
            if (splittedLine.length == 0 || splittedLine[0].length() == 0) {
                // Skip if line is empty.
                continue;
            }

            String interpretedLine = splittedLine[0];
            boolean onlyContainsWhitespace = true;
            for (char c : interpretedLine.toCharArray()) {
                if (c != ' ') {
                    onlyContainsWhitespace = false;
                    break;
                }
            }

            if (onlyContainsWhitespace) {
                 // Skip if line is only whitespaces.
                continue;
            }

            rulesObject.add(new Rule(interpretedLine));
        }

        return rulesObject;
    }
}
