package org.esa.smos.ee2netcdf;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.ValidationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.ParameterDescriptorFactory;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.runtime.Engine;

import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line tool for converting SMOS products from Earth Explorer Format to netCDF.
 *
 * @author Ralf Quast
 */
public class NetcdfExportTool {

    private static final String TOOL_NAME = "smos-ee-to-nc";
    private static final String TOOL_VERSION = "5.8.0";

    private static final int ERROR = 1;
    private static final int USAGE_ERROR = 2;
    private static final int EXECUTION_ERROR = 3;

    private static final Map<String, String> PARAMETER_NAMES = new HashMap<>();

    static {
        Locale.setDefault(Locale.ENGLISH);

        PARAMETER_NAMES.put(ExportParameter.CONTACT, "contact");
        PARAMETER_NAMES.put(ExportParameter.INSTITUTION, "institution");
        PARAMETER_NAMES.put(ExportParameter.OVERWRITE_TARGET, "overwrite-target");
        PARAMETER_NAMES.put(ExportParameter.GEOMETRY, "region");
        PARAMETER_NAMES.put(ExportParameter.TARGET_DIRECTORY, "target-directory");
        PARAMETER_NAMES.put(ExportParameter.VARIABLES, "variables");
        PARAMETER_NAMES.put(ExportParameter.COMPRESSION_LEVEL, "compression-level");
    }

    private static final Level[] LOG_LEVELS = new Level[]{
            Level.ALL,
            Level.INFO,
            Level.CONFIG,
            Level.WARNING,
            Level.SEVERE,
            Level.OFF
    };

    private static final String LOG_LEVEL_OPTION_NAME = "log-level";
    private static final String ERROR_OPTION_NAME = "error";
    private static final String LOG_LEVEL_DESCRIPTION = "Set the logging level to <level> where <level> must be in "
            + Arrays.toString(LOG_LEVELS).replace("[", "{").replaceAll("]", "}")
            + ". The default logging level is '"
            + Level.INFO.toString()
            + "'.";

    private final Options options = new Options();

    private Logger logger;
    private Level logLevel = Level.INFO;
    private boolean produceErrorMessages;

    public static void main(String[] args) {
        new NetcdfExportTool().run(args);
    }

    public NetcdfExportTool() {
        defineOptions();
    }

    private void run(String[] arguments) {
        try {
            execute(arguments);
        } catch (ToolException e) {
            exit(e, e.getExitCode());
        } catch (Throwable e) {
            exit(e, ERROR);
        }
    }

    private void execute(String[] arguments) throws ToolException {
        final CommandLine commandLine;
        try {
            commandLine = parseCommandLine(arguments);

            if (commandLine.hasOption("help")) {
                printHelp();
                return;
            }
            if (commandLine.hasOption("version")) {
                printVersion();
                return;
            }
            if (commandLine.getArgs().length == 0 && !commandLine.hasOption("source-product-paths")) {
                printHelp();
                return;
            }
            if (commandLine.hasOption(ERROR_OPTION_NAME)) {
                produceErrorMessages = true;
            }
            if (commandLine.hasOption(LOG_LEVEL_OPTION_NAME)) {
                final String optionValue = commandLine.getOptionValue(LOG_LEVEL_OPTION_NAME);
                logLevel = Level.parse(optionValue);
            }
        } catch (ParseException | IllegalArgumentException e) {
            throw new ToolException(e, USAGE_ERROR);
        } finally {
            configureLogger();
        }

        final Engine engine = Engine.start();

        try {
            final ExportParameter exportParameter = new ExportParameter();
            setExportParameters(commandLine, exportParameter);

            final NetcdfExporter exporter = new NetcdfExporter(exportParameter);
            try {
                exporter.initialize();
            } catch (Exception e) {
                final File targetDirectory = exportParameter.getTargetDirectory();
                throw new ToolException(MessageFormat.format("The target directory ''{0}'' could not be created.", targetDirectory),
                        e, EXECUTION_ERROR);
            }

            for (final String path : commandLine.getArgs()) {
                final File file = new File(path);
                try {
                    exporter.exportFile(file, getLogger());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ToolException(
                            MessageFormat.format("An error has occurred while trying to convert file ''{0}''.", path), e,
                            EXECUTION_ERROR);
                }
            }

            if (commandLine.hasOption("source-product-paths")) {
                final String sourceProductPathsCSV = commandLine.getOptionValue("source-product-paths");
                final String[] sourceProductPaths = StringUtils.split(sourceProductPathsCSV, new char[]{','}, true);
                final TreeSet<File> inputFileSet = ExporterUtils.createInputFileSet(sourceProductPaths);

                for (File inputFile : inputFileSet) {
                    if (inputFile.isDirectory()) {
                        continue;
                    }
                    try {
                        exporter.exportFile(inputFile, getLogger());
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.severe(MessageFormat.format("An error has occurred while trying to convert file ''{0}''.", inputFile));
                    }
                }
            }
        } finally {
            engine.stop();
        }
    }

    private void setExportParameters(CommandLine commandLine, ExportParameter exportParameter) throws ToolException {
        final ParameterDescriptorFactory descriptorFactory = new ParameterDescriptorFactory();
        final PropertyContainer container = PropertyContainer.createObjectBacked(exportParameter, descriptorFactory);
        container.setDefaultValues();
        container.setValue(ExportParameter.ROI_TYPE, ExportParameter.ROI_TYPE_GEOMETRY);

        for (final String parameterName : PARAMETER_NAMES.keySet()) {
            final String optionName = getOptionName(parameterName);
            if (commandLine.hasOption(optionName)) {
                final String optionValue = commandLine.getOptionValue(optionName);
                final Property parameter = container.getProperty(parameterName);
                if (optionValue == null) {
                    if (parameter.getType().isAssignableFrom(boolean.class)) {
                        container.setValue(parameterName, true);
                        continue;
                    }
                }
                try {
                    parameter.setValueFromText(optionValue);
                } catch (ValidationException e) {
                    throw new ToolException(
                            MessageFormat.format("Missing or invalid value for option ''{0}''.", optionName), e,
                            USAGE_ERROR);
                }
            }
        }
    }

    private void exit(Throwable t, int exitCode) {
        if (produceErrorMessages) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
        }
        if (getLogger().isLoggable(Level.SEVERE)) {
            getLogger().log(Level.SEVERE, t.getMessage());
            if (getLogger().isLoggable(Level.FINE)) {
                for (StackTraceElement e : t.getStackTrace()) {
                    getLogger().log(Level.FINE, e.toString());
                }
            }
        }
        System.exit(exitCode);
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = SystemUtils.LOG;
        }
        return logger;
    }

    private void configureLogger() {
        final Logger logger = getLogger();
        logger.setLevel(logLevel);
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(logLevel);
        logger.addHandler(consoleHandler);
    }

    private void printVersion() {
        System.out.println(TOOL_NAME + " version " + TOOL_VERSION);
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setNewLine("\n");
        helpFormatter.setWidth(80);
        helpFormatter.printHelp(getSyntax(), "\nOptions:", options, "", false);
    }

    private String getSyntax() {
        return TOOL_NAME + " [options] file ...";
    }

    private CommandLine parseCommandLine(String[] arguments) throws ParseException {
        return new PosixParser().parse(options, arguments);
    }

    private void defineOptions() {
        options.addOption("e", "errors", false, "Produce execution error messages.");
        options.addOption("h", "help", false, "Display help information.");
        options.addOption("v", "version", false, "Display version information.");
        options.addOption(createOption("l", LOG_LEVEL_OPTION_NAME, Level.class, LOG_LEVEL_DESCRIPTION));
        OptionBuilder.withLongOpt("source-product-paths");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName(String.class.getSimpleName().toLowerCase());
        OptionBuilder.withDescription("Comma-separated list of file paths specifying the source products.\n" +
                "Each path may contain the wildcards '**' (matches recursively any directory),\n" +
                "'*' (matches any character sequence in path names) and\n" +
                "'?' (matches any single character).");
        options.addOption(OptionBuilder.create());

        final Set<String> parameterNames = PARAMETER_NAMES.keySet();
        final Field[] fields = ExportParameter.class.getDeclaredFields();

        for (final Field field : fields) {
            final Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null) {
                final String alias = parameter.alias();
                if (parameterNames.contains(alias)) {
                    final String optionName = getOptionName(alias);
                    OptionBuilder.withLongOpt(optionName);

                    final Class<?> type = getType(field);
                    OptionBuilder.withType(type);

                    final String argName = type.getSimpleName().toLowerCase();
                    final boolean noArg = type.isAssignableFrom(boolean.class);
                    if (noArg) {
                        OptionBuilder.hasArg(false);
                    } else {
                        OptionBuilder.hasArg(true);
                        OptionBuilder.withArgName(argName);
                    }

                    final String description = parameter.description();
                    final StringBuilder descriptionBuilder = new StringBuilder(description);
                    if (!description.isEmpty() && !description.endsWith(".")) {
                        descriptionBuilder.append(".");
                    }
                    if (!noArg) {
                        final String[] valueSet = parameter.valueSet();
                        if (valueSet.length != 0) {
                            descriptionBuilder
                                    .append(" The argument <")
                                    .append(argName)
                                    .append("> must be in ")
                                    .append(Arrays.toString(valueSet).replace("[", "{").replace("]", "}"))
                                    .append(".");
                        } else {
                            final String interval = parameter.interval();
                            if (!interval.isEmpty()) {
                                descriptionBuilder
                                        .append(" The argument <")
                                        .append(argName)
                                        .append("> must be in the interval ")
                                        .append(interval)
                                        .append(".");
                            }
                        }
                        final String defaultValue = parameter.defaultValue();
                        if (!defaultValue.isEmpty()) {
                            descriptionBuilder
                                    .append(" The default value is '")
                                    .append(defaultValue)
                                    .append("'.");
                        }
                    }
                    OptionBuilder.withDescription(descriptionBuilder.toString());
                    final Option option = OptionBuilder.create();

                    final boolean required = parameter.notNull() || parameter.notEmpty();
                    option.setRequired(required);

                    options.addOption(option);
                }
            }
        }
    }

    private static Option createOption(String opt, String optionName, Class<?> argType, String description) {
        final Option option = new Option(opt, optionName, true, description);
        option.setType(argType);
        option.setArgName(argType.getSimpleName().toLowerCase());
        return option;
    }

    private static String getOptionName(String alias) {
        return PARAMETER_NAMES.get(alias);
    }

    private static Class<?> getType(Field field) {
        final Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive() || fieldType.equals(File.class)) {
            return fieldType;
        }
        return String.class;
    }

    private static final class ToolException extends Exception {

        private final int exitCode;

        private ToolException(Throwable cause, int exitCode) {
            super(cause);
            this.exitCode = exitCode;
        }

        private ToolException(String message, Throwable cause, int exitCode) {
            super(message, cause);
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

}
