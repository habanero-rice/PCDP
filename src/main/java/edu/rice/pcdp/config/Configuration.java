package edu.rice.pcdp.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * <p>Configuration class for configuring the PCDP runtime.</p>
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public final class Configuration {

    /**
     * Flag to control display of warning messages.
     */
    public static boolean showWarning;

    /**
     * Flag to control abstract metrics execution graph display.
     */
    public static boolean SHOW_RUNTIME_STATS;

    /**
     * Flag to control abstract metrics execution graph display.
     */
    public static String BUILD_INFO;

    static {
        initializeFlags();
    }

    /**
     * Private no-args constructor that disallows instance creation.
     */
    private Configuration() {
        throw new IllegalStateException(
                "Emptyton, no instance creation expected!");
    }

    /**
     * Set up runtime configuration values.
     */
    private static void initializeFlags() {
        showWarning = readBooleanProperty(SystemProperty.showWarning);
        if (showWarning) {
            printConfiguredOptions();
        }
        SHOW_RUNTIME_STATS =
            readBooleanProperty(SystemProperty.showRuntimeStats);

        String buildInfo;
        try {
            final Properties buildProperties = new Properties();
            final InputStream buildPropsStream =
                Configuration.class.getResourceAsStream("/build.properties");
            buildProperties.load(buildPropsStream);
            buildInfo = buildProperties.getProperty("version") + ' '
                + buildProperties.getProperty("buildTimestamp");
        } catch (final Exception e) {
            buildInfo = "";
        }
        BUILD_INFO = buildInfo;
    }

    /**
     * <p>readBooleanProperty.</p>
     *
     * @param systemProperty The option whose value to return as an int.
     * @return The property value converted to a boolean if it is a valid
     *         string, else return the default value.
     */
    public static boolean readBooleanProperty(
            final SystemProperty systemProperty) {
        final Lambda<String, Boolean> converter =
            new Lambda<String, Boolean>() {
                @Override
                public Boolean apply(final String s) {
                    return Boolean.parseBoolean(s);
                }
            };
        return extractProperty(systemProperty, converter);
    }

    /**
     * <p>printConfiguredOptions.</p>
     */
    public static void printConfiguredOptions() {
        System.err.println("Interpreter flags: ");
        for (final SystemProperty systemProperty : SystemProperty.values()) {
            System.err.println(" " + systemProperty);
        }
    }

    /**
     * <p>readIntProperty.</p>
     *
     * @param systemProperty The option whose value to return as an int.
     * @return The property value converted to an int if it is a valid integer,
     *         else return the default value.
     */
    public static int readIntProperty(final SystemProperty systemProperty) {
        final Lambda<String, Integer> converter =
            new Lambda<String, Integer>() {
                @Override
                public Integer apply(final String s) {
                    return Integer.parseInt(s);
                }
            };
        return extractProperty(systemProperty, converter);
    }

    /**
     * @param propertyName The name of the property whose value to return.
     * @param converter    The converter from String to the type T
     * @param <T>          The polymorphic return type of the method, converter,
     *                     and default value
     * @return The property value converted if it is valid, else return the
     *         default value.
     */
    private static <T> T extractProperty(
        final SystemProperty propertyName,
        final Lambda<String, T> converter) {
        try {
            final String valueStr = propertyName.getPropertyValue();
            return converter.apply(valueStr);
        } catch (final Exception ex) {
            // value not configured properly, use default value
            throw new IllegalStateException("Error while converting property: "
                    + propertyName);
        }
    }

    /**
     * <p>readStringProperty.</p>
     *
     * @param systemProperty The option whose value to return as a string.
     * @return The property value as a string, else return the default value.
     */
    public static String readStringProperty(
            final SystemProperty systemProperty) {
        final Lambda<String, String> converter = new Lambda<String, String>() {
            @Override
            public String apply(final String s) {
                return s;
            }
        };
        return extractProperty(systemProperty, converter);
    }

    /**
     * A simple lambda interface with a single argument type and return type.
     *
     * @param <P> Type of the parameter to this lambda.
     * @param <R> Type of the return from this lambda.
     */
    private interface Lambda<P, R> {
        /**
         * Computational body of this lambda.
         *
         * @param p Single parameter to this lambda
         * @return User-defined return value.
         */
        R apply(P p);
    }
}

