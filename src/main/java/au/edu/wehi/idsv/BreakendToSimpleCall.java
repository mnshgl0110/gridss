package au.edu.wehi.idsv;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;

import picard.cmdline.CommandLineProgram;
import picard.cmdline.CommandLineProgramProperties;
import picard.cmdline.Option;
import picard.cmdline.StandardOptionDefinitions;
import au.edu.wehi.idsv.configuration.GridssConfiguration;

/**
 * Calls simple variants
 * @author Daniel Cameron
 *
 */
@CommandLineProgramProperties(
        usage = "Converts breakend calls to simple variant calls for DEL INS INV DUP events."
        		+ " This conversion strips all GRIDSS annotations and ignores all interchromosomal and complex events."
        		+ " It is strongly recommended that the pipeline invoking GRIDSS be updated to handle variants in VCF breakend notation.",  
        usageShort = "Converts breakend calls to simple variant calls."
)
public class BreakendToSimpleCall extends CommandLineProgram {
	@Option(shortName=StandardOptionDefinitions.INPUT_SHORT_NAME, doc="GRIDSS variant calls")
    public File INPUT;
	@Option(shortName=StandardOptionDefinitions.OUTPUT_SHORT_NAME, doc="Simple subset of GRIDSS variant calls")
    public File OUTPUT;
	@Option(shortName=StandardOptionDefinitions.REFERENCE_SHORT_NAME, doc="Reference used for alignment")
    public File REFERENCE;
	@Option(shortName="C", doc = "gridss configuration file containing overrides", optional=true)
    public File CONFIGURATION_FILE = null;
	public static void main(String[] argv) {
        System.exit(new BreakendToSimpleCall().instanceMain(argv));
    }
	@Override
	protected int doWork() {
		FileSystemContext fsc = new FileSystemContext(TMP_DIR.get(0), TMP_DIR.get(0), MAX_RECORDS_IN_RAM);
		GridssConfiguration config;
		try {
			config = new GridssConfiguration(CONFIGURATION_FILE, fsc.getIntermediateDirectory(OUTPUT));
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
		ProcessingContext processContext = new ProcessingContext(fsc, getDefaultHeaders(), config, REFERENCE, false);
		BreakendToSimpleCallImpl impl = new BreakendToSimpleCallImpl(processContext);
		impl.convert(INPUT, OUTPUT);
		return 0;
	}
}
