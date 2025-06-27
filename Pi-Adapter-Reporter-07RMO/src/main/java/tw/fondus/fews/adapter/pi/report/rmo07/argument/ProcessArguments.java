package tw.fondus.fews.adapter.pi.report.rmo07.argument;

import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.cli.argument.splitter.CommaSplitter;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;

/**
 * Process arguments for running ExcelReportStatisticsProcess and PiXmlGenerateProcess.
 * 
 * @author Chao
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class ProcessArguments extends PiBasicArguments {
	@Parameter(names = { "--indexStart", "-is" }, required = true, description = "The start index of generate report used.")
	private int indexStart;

	@Parameter(names = { "--indexEnd", "-ie" }, required = true, description = "The end index of generate report used.")
	private int indexEnd;

	@Builder.Default
	@Parameter( names = { "--prefix", "-pf" }, description = "The file name prefix of process output." )
	private String prefix = "FEWS";

	@Builder.Default
	@Parameter( names = { "--suffix", "-sf" }, description = "The file name suffix of process output." )
	private String suffix = "QPESUMS_QPF";

	@Builder.Default
	@Parameter( names = { "--specialCases", "-sc" }, description = "The special case list of location ID with comma, and order is fixed.",
			splitter = CommaSplitter.class )
	private List<String> specialCases = CollectionUtils.emptyListArray();

	@Builder.Default
	@Parameter( names = { "--timeZeroIndex", "-tzi" }, description = "Index of time zero of input data." )
	private int timeZeroIndex = 20;

	@Builder.Default
	@Parameter(names = { "--force1HFormat", "-f1h" }, description = "Whether to force formatting data to 1-hour interval, even if original data is 30-minute interval.")
	private boolean force1HFormat = false;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static ProcessArguments instance(){
		return ProcessArguments.builder().build();
	}
}
