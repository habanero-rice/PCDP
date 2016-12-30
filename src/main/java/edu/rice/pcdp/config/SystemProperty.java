package edu.rice.pcdp.config;

/**
 * <p>SystemProperty class.</p>
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public enum SystemProperty {

    /**
     * Number of workers to use.
     */
    numWorkers("pcdp.numWorkers", "int", "Number of worker threads to create",
            Constants.AVAILABLE_PROCESSORS_STR),
    /**
     * Show warning/debug messages.
     */
    showWarning("pcdp.showWarning", "bool", "Show warning/debug messages",
            "false"),
    /**
     * Show runtime stats.
     */
    showRuntimeStats("pcdp.showRuntimeStats", "bool",
            "Show executor service stats", "false");

    /**
     * Helper method to set the property used by the Habanero runtime during
     * initialization.
     *
     * @param hjSystemProperty The system property to set
     * @param value            The new value of the property
     */
    public static void setSystemProperty(final SystemProperty hjSystemProperty,
            final Object value) {
        System.setProperty(hjSystemProperty.propertyKey(),
                String.valueOf(value));
    }

    /**
     *
     */
    private final String propertyKey;
    /**
     *
     */
    private final String propertyType;
    /**
     *
     */
    private final String propertyDescription;
    /**
     *
     */
    private final String defaultValue;

    /**
     * Constructor.
     *
     * @param pPropertyKey String key for this property.
     * @param pPropertyType String type (e.g. "int") for this property.
     * @param pPropertyDescription String description for this property.
     * @param pDefaultValue String default value.
     */
    SystemProperty(final String pPropertyKey,
            final String pPropertyType,
            final String pPropertyDescription,
            final String pDefaultValue) {
        this.propertyKey = pPropertyKey;
        this.propertyType = pPropertyType;
        this.propertyDescription = pPropertyDescription;
        this.defaultValue = pDefaultValue;
    }

    /**
     * <p>propertyKey.</p>
     *
     * @return a {@link String} object.
     */
    private String propertyKey() {
        return propertyKey;
    }

    /**
     * <p>getPropertyValue.</p>
     *
     * @return a {@link String} object.
     */
    public String getPropertyValue() {
        final String systemProperty = System.getProperty(propertyKey);
        if (systemProperty == null || systemProperty.trim().isEmpty()) {
            return defaultValue;
        } else {
            return systemProperty;
        }
    }

    /**
     * Helper method to set the property used by the Habanero runtime during
     * initialization.
     *
     * @param value The new value of the property
     */
    public void set(final Object value) {
        setProperty(value);
    }

    /**
     * Helper method to set the property used by the Habanero runtime during
     * initialization.
     *
     * @param value The new value of the property
     */
    public void setProperty(final Object value) {
        setSystemProperty(this, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String configuredValue = System.getProperty(propertyKey);
        final String displayValue;
        if (configuredValue != null) {
            displayValue = configuredValue;
        } else {
            displayValue = "";
        }

        return String.format("%20s : type=%-6s, default=%-6s, current=%-6s, "
                + "description=%s", propertyKey, propertyType, defaultValue,
                displayValue, propertyDescription);
    }

    /**
     * Statically defined defaults for various runtime parameters.
     */
    private static class Constants {
        /**
         * The number of available processors.
         */
        private static int availableProcessors =
            Runtime.getRuntime().availableProcessors();
        /**
         * String representation of the number of available processors.
         */
        private static final String AVAILABLE_PROCESSORS_STR =
            Integer.toString(availableProcessors);
        /**
         * String representation of the maximum number of threads configured by
         * default if user doesn't specify a value.
         */
        private static final String MAX_THREADS_DEFAULT =
            Integer.toString(Math.min(128, 10 * availableProcessors));
    }
}
