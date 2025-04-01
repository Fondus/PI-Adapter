package tw.fondus.fews.adapter.pi.report.rmo07.argument;

import com.beust.jcommander.Parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Process arguments for running CrossSectionChartProcess and ProfileChartProcess.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class CrossSectionChartArguments extends ProcessArguments {
	@Parameter(names = { "--width", "-w" }, required = true, description = "The width resolution of chart picture.")
	private int width;

	@Parameter(names = { "--height", "-he" }, required = true, description = "The height resolution of chart picture.")
	private int height;

	/**
	 * Create the argument instance.
	 *
	 * @return argument instance
	 * @since 3.0.0
	 */
	public static CrossSectionChartArguments instance(){
		return CrossSectionChartArguments.builder().build();
	}
}
