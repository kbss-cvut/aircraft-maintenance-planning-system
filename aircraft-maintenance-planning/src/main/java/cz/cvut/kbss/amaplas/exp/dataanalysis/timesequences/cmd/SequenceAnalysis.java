package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.cmd;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.kohsuke.args4j.spi.IntOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.Set;

//@Setter
//@Getter
public class SequenceAnalysis {
    private static final Logger LOG = LoggerFactory.getLogger(SequenceAnalysis.class);

    @Option(name = "-e", usage="sparql endpoint used to store the data.", required = true)
    protected String endpoint;

    @Option(name = "-o", usage="the folder where the output of the command is stored")

    protected String outputfolder = ".";

    @Option(name = "-p", usage="sparql endpoint used to store the data." )
    protected String outputPrefix;

    @Option(name = "-sup", usage="minimum support in percent 0 - 100.", handler = IntOptionHandler.class)
    @Max(value = 100, message = "support should be between 0 and 100")
    @Min(value = 0, message = "support should be between 0 and 100")
    protected int support = 50;

    @Option(name = "-md", usage="the minimum distance between the tasks in days")
    @Positive
    protected int minDistance = 1;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getOutputfolder() {
        return outputfolder;
    }

    public void setOutputfolder(String outputfolder) {
        this.outputfolder = outputfolder;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public void setOutputPrefix(String outputPrefix) {
        this.outputPrefix = outputPrefix;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public static void main(String[] args) throws IOException {
        SequenceAnalysis in = new SequenceAnalysis();
        CmdLineParser parser = new CmdLineParser(in);
        Set<ConstraintViolation<SequenceAnalysis>> violations = null;
        // ---------- arguments parsing ------------
        try {
            parser.parseArgument(args);
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            violations = validator.validate(in);
            if(violations != null && !violations.isEmpty())
                throw new CmdLineException(parser, "argument validation failed");
        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            if(violations != null && !violations.isEmpty()){
                for(ConstraintViolation<SequenceAnalysis> cv : violations) {
                    System.err.println(cv.getMessage());
                }
            }

            System.err.println("Usage:");
            System.err.println("java SequenceAnalysis [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java SampleMain"+parser.printExample(OptionHandlerFilter.ALL));
            return;
        }
        // TODO
//        new ExtractData().executeSequenceExtraction(in.endpoint, in.outputfolder, in.outputPrefix, in.minDistance, in.support);
    }

    public static void commandDummy(String a1, String a2, String a3, int a4, int a5){

        System.out.println("Running command commandDummy with arguments");
        System.out.println("a1 = " + a1 );
        System.out.println("a2 = " + a2 );
        System.out.println("a3 = " + a3 );
        System.out.println("a4 = " + a4 );
        System.out.println("a5 = " + a5 );
        System.out.println("...");
        System.out.println("Command commandDummy done!");
        System.out.println();
    }
}
