package tw.fondus.fews.adapter.pi.nc;

import org.joda.time.DateTime;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.nc.util.key.VariableName;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The model adapter use to merge the test grid type of NetCDF, let can be import to Delft-FEWS.
 *
 * @author Brad Chen
 * @since 3.0.0
 */
public class TestGridMergeAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PiIOArguments arguments = PiIOArguments.instance();
		new TestGridMergeAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		PiIOArguments modelArguments = this.asIOArguments( arguments );

		logger.log( LogLevel.INFO, "TestGridMergeAdapter: Start the merge NetCDF grids process." );
		List<Path> paths = PathUtils.list( inputPath, path -> PathUtils.equalsExtension( path, FileType.NETCDF ) );
		if ( paths.isEmpty() ){
			logger.log( LogLevel.WARN, "TestGridMergeAdapter: The input path not found any NetCDF files." );
		} else {
			logger.log( LogLevel.INFO, "TestGridMergeAdapter: Try to read the input NetCDF grids." );

			List<StandardGrid> grids = CollectionUtils.emptyListArray();
			paths.forEach( path -> {
				try ( NetCDFReader reader = NetCDFReader.read( path ) ){
					grids.addAll( NetCDFGridMapper.fromTYXModel( reader, modelArguments.getParameter() ) );
				} catch ( IOException e){
					logger.log( LogLevel.ERROR,"TestGridMergeAdapter: Read NetCDF file: {} has IO problem.", path.toString() );
				}
			} );

			if ( grids.isEmpty() ){
				logger.log( LogLevel.WARN, "TestGridMergeAdapter: The no NetCDF files read succeeded." );
			} else {
				// Prepare to create fake time
				logger.log( LogLevel.INFO, "TestGridMergeAdapter: Finished to read the input NetCDF grids, prepare to create the fake times." );
				List<Long> times = grids.stream()
						.map( StandardGrid::getTime )
						.sorted( Comparator.naturalOrder() )
						.collect( Collectors.toList() );

				long timeStep = times.get( 1 ) - times.get( 0 );
				DateTime start = modelArguments.getTimeZero();
				logger.log( LogLevel.INFO, "TestGridMergeAdapter: The fake start time: {} and time step is {}, start to fill the fake time.", start.toString(), timeStep );

				// Fill fake time
				IntStream.range( 0, grids.size() )
						.forEach( i -> {
							StandardGrid grid = grids.get( i );
							grid.setProperty( VariableName.TIME, start.plus( timeStep * i ).getMillis() );
						} );

				Path mergePath = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
				logger.log( LogLevel.INFO, "TestGridMergeAdapter: Try to write the merge NetCDF grid with Path {}.", mergePath.toString() );
				try {
					NetCDFGridMapper.toTYXModel( mergePath, "Merge Grids for, Test", grids, true );
				} catch (IOException | InvalidRangeException e){
					logger.log( LogLevel.ERROR, "TestGridMergeAdapter: Write the merge NetCDF grid with Path {} has something wrong.", mergePath.toString() );
				}
			}
		}
		logger.log( LogLevel.INFO, "TestGridMergeAdapter: Finished the merge NetCDF grids process." );
	}
}
