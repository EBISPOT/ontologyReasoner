package uk.ac.ebi.fgpt.efo;

import org.apache.commons.cli.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.efo.exception.OWLConversionException;
import uk.ac.ebi.fgpt.efo.service.OntologyReasoningService;
import uk.ac.ebi.fgpt.efo.utils.OntologyConfiguration;

/**
 * Hello world!
 *
 */
public class OntologyReasoningDriver
{

    private static String assertedOntologyFile;
    private static String inferredOntologyFile;


//    private static IRI assertedOntologyFile;
//    private static IRI inferredOntologyFile;

    public static void main( String[] args )
    {
        try {
            // parse arguments
            int parseArgs = parseArguments(args);
            if (parseArgs == 0) {
                // execute publisher
                OntologyReasoningDriver driver = new OntologyReasoningDriver();
                driver.reasonAndSave(assertedOntologyFile, inferredOntologyFile);
            }
            else {
                // could not parse arguments, exit with exit code >1 (depending on parsing problem)
                System.err.println("Failed to parse supplied arguments");
                System.exit(1 + parseArgs);
            }
        }
        catch (Exception e) {
            // failed to execute, exit with exit code 1
            System.err.println("An unexpected error occurred\n\t(" + e.getMessage() + ")");
            System.exit(1);
        }
       

    }


    private static int parseArguments(String[] args) {
        CommandLineParser parser = new GnuParser();
        HelpFormatter help = new HelpFormatter();
        Options options = bindOptions();

        int parseArgs = 0;
        try {
            CommandLine cl = parser.parse(options, args, true);

            // check for mode help option
            if (cl.hasOption("")) {
                // print out mode help
                help.printHelp("publish", options, true);
                parseArgs += 1;
            }
            else {
                // find -o option (for asserted output file)
                if (cl.hasOption("i")) {
                    String assertedOutputFileName = cl.getOptionValue("i");
                    assertedOntologyFile = assertedOutputFileName;
//                    assertedOntologyFile = IRI.create(assertedOutputFileName);

                    if (cl.hasOption("o")) {
                        String outputFileName = cl.getOptionValue("o");
                        inferredOntologyFile = outputFileName;
//                        inferredOntologyFile = IRI.create(outputFileName);
                    }


                }
                else {
                    System.err.println("-o (ontology output file) argument is required");
                    help.printHelp("publish", options, true);
                    parseArgs += 2;
                }
            }
        }
        catch (ParseException e) {
            System.err.println("Failed to read supplied arguments");
            help.printHelp("publish", options, true);
            parseArgs += 4;
        }
        return parseArgs;
    }

    private static Options bindOptions() {
        Options options = new Options();

        // help
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        // add output file arguments
        Option outputFileOption = new Option("o", "output", true,
                "The output file to write the inferred ontology to");
        outputFileOption.setArgName("file");
        outputFileOption.setRequired(true);
        options.addOption(outputFileOption);

        Option inputFileOption = new Option("i", "input", true,
                                                     "The input file to read the ontology from");
        inputFileOption.setArgName("file");
        options.addOption(inputFileOption);

        return options;
    }

    private OntologyReasoningService reasoningService;
    private OntologyConfiguration config;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public OntologyReasoningDriver(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("ontologyReasoner.xml");
        reasoningService = ctx.getBean("reasoningService", OntologyReasoningService.class);
        config = ctx.getBean("config", OntologyConfiguration.class);

    }

    public void reasonAndSave(String assertedOntologyFile, String inferredOntologyFile) throws RuntimeException {

        try {
            getLog().debug("Evaluating inferred view...");
            OWLReasoner reasoner = reasoningService.reasonOverOntology(assertedOntologyFile);

            // now save inferred view
            getLog().debug("Ontology fully classified, saving inferred results...");
            reasoningService.saveInferredOntology(reasoner, inferredOntologyFile);
            getLog().debug("..done!");

        }

        catch (OWLConversionException e) {
            System.err.println("Failed to publish data to OWL: " + e.getMessage());
            getLog().error("Failed to publish data to OWL: ", e);
            throw new RuntimeException(e);
        }

        catch (Exception e) {
            System.err.println("Failed to publish data to OWL (an unexpected exception occurred): " + e.getMessage());
            getLog().error("Failed to publish data to OWL (an unexpected exception occurred): ", e);
            throw new RuntimeException(e);
        }
    }
}
