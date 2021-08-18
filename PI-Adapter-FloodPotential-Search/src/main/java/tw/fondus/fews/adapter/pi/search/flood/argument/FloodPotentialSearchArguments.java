package tw.fondus.fews.adapter.pi.search.flood.argument;

import com.beust.jcommander.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tw.fondus.commons.util.zone.AreaTaiwan;
import tw.fondus.commons.util.zone.ZoneCounty;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.search.flood.converter.AccumulatedRainfallDurationConverter;
import tw.fondus.fews.adapter.pi.search.flood.converter.AreaTaiwanConverter;
import tw.fondus.fews.adapter.pi.search.flood.converter.ZoneCountyConverter;
import tw.fondus.fews.adapter.pi.search.flood.util.AccumulatedRainfallDuration;

/**
 * Adapter arguments for running search flood potential with feature value.
 *
 * @author Brad Chen
 *
 */
@Data
@SuperBuilder
@ToString( callSuper = true )
@EqualsAndHashCode( callSuper = true )
public class FloodPotentialSearchArguments extends PiIOArguments {
	@Builder.Default
	@Parameter( names = { "--feature-dir", "-fd" }, description = "The features folder, relative to the current working directory." )
	private String featurePath = "Features/";

	@Parameter( names = { "--feature-file", "-ff" }, description = "The features file, should inside the features folder." )
	private String featureFile;

	@Builder.Default
	@Parameter( names = { "--database-dir", "-dd" }, description = "The flood potential map database folder, relative to the current working directory." )
	private String databasePath = "Database/";

	@Parameter( names = { "--area-id", "-ai" }, required = true,
				converter = AreaTaiwanConverter.class,
				description = "The id of area." )
	private AreaTaiwan area;

	@Parameter( names = { "---zone-id", "-zi" }, required = true,
				converter = ZoneCountyConverter.class,
				description = "The id of zone." )
	private ZoneCounty zone;

	@Builder.Default
	@Parameter( names = { "--accumulated-duration", "-ad" },
				converter = AccumulatedRainfallDurationConverter.class,
				description = "The target duration of accumulated rainfall used for search flood potential map, it's supports 06h, 12h, 24h only, default is 06h." )
	private AccumulatedRainfallDuration accumulatedRainfallDuration = AccumulatedRainfallDuration.D06H;

	public static FloodPotentialSearchArguments instance(){
		return FloodPotentialSearchArguments.builder().build();
	}
}
