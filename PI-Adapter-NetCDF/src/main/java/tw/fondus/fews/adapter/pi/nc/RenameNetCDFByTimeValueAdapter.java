package tw.fondus.fews.adapter.pi.nc;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.nc.util.TimeFactor;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringFormatter;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.nc.argument.RenameByTimeValueArguments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The adapter used to rename NetCDF filename by NetCDF include data.
 *
 * @author Brad Chen
 * @since 3.2.5
 */
public class RenameNetCDFByTimeValueAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		RenameByTimeValueArguments arguments = RenameByTimeValueArguments.instance();
		new RenameNetCDFByTimeValueAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		RenameByTimeValueArguments modelArguments = this.asArguments( arguments, RenameByTimeValueArguments.class );

		String format = modelArguments.getTimeFormat();
		int index = modelArguments.getTimeIndex();
		boolean isGMT8 = modelArguments.isGMT8();

		logger.log( LogLevel.INFO,
				"Rename NetCDF Adapter: Start the to rename NetCDF process with time-format: {}, index: {}, and time zone {}.",
				format, index, isGMT8 ? "GMT8" : "UTC0" );

		Path netcdfPath = Prevalidated.checkExists( inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"Rename NetCDF Adapter: The input NetCDf do not exist." );

		try ( NetCDFReader reader = NetCDFReader.read( netcdfPath ) ) {
			if ( reader.hasTime() ) {
				List<Long> times = reader.findTimes( TimeFactor.ARCHIVE );
				if ( times.size() <= index ){
					logger.log( LogLevel.ERROR, "Rename NetCDF Adapter: User input index: {} greater equals than NetCDF time size: {}.", index, times.size() );
					throw new IllegalArgumentException( StringFormatter.format( "Rename NetCDF Adapter: User input index: {} greater equals than NetCDF time size: {}.", index, times.size()  ) );
				} else {
					DateTimeZone timeZone = isGMT8 ? JodaTimeUtils.UTC8 : JodaTimeUtils.UTC0;
					DateTime time = JodaTimeUtils.toDateTime( times.get( index ), timeZone );
					String fileName = JodaTimeUtils.toString( time, format, timeZone );
					Path output = outputPath.resolve( fileName + FileType.NETCDF.getExtension() );
					PathUtils.copy( netcdfPath, output );

					logger.log( LogLevel.INFO, "Rename NetCDF Adapter: Rename NetCDF file: {} into {}.", netcdfPath, output );
				}
			} else {
				logger.log( LogLevel.WARN, "Rename NetCDF Adapter: NetCDF not contain any times, skip process." );
			}
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "Rename NetCDF Adapter: Read NetCDF has IO problem. {}", e.toString() );
		}

		PathUtils.deleteIfExists( netcdfPath );
		logger.log( LogLevel.INFO,
				"Rename NetCDF Adapter: Finished the to rename NetCDF process with time-format: {}, index: {}, and time zone {}.",
				format, index, isGMT8 ? "GMT8" : "UTC0" );
	}
}
