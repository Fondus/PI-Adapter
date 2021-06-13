package tw.fondus.fews.adapter.pi.nc;

import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.nc.argument.MergeArguments;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The model adapter use to merge the grid type of NetCDF.
 *
 * @author Brad Chen
 *
 */
public class GridMergeAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		MergeArguments arguments = MergeArguments.instance();
		new GridMergeAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		MergeArguments modelArguments = this.asArguments( arguments, MergeArguments.class );

		logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Start the merge NetCDF grids process." );
		int fileSize = modelArguments.getCount();
		int timeRange = modelArguments.getTimeRange();
		List<Path> paths = PathUtils.list( inputPath, path -> PathUtils.equalsExtension( path, FileType.NETCDF ) );
		if ( paths.size() != fileSize ){
			logger.log( LogLevel.ERROR, "NetCDF GridMergeAdapter: Input NetCDF file size: {} not equal with user inputs: {}, skip process.", paths.size(), fileSize );
		} else {
			logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Try to read the input NetCDF grids." );
			List<StandardGrid> grids = CollectionUtils.emptyListArray();
			paths.forEach( path -> {
				try ( NetCDFReader reader = NetCDFReader.read( path ) ){
					grids.addAll( NetCDFGridMapper.fromTYXModel( reader, modelArguments.getParameter(), 0, timeRange ) );
				} catch ( IOException e){
					logger.log( LogLevel.ERROR,"NetCDF GridMergeAdapter: Read NetCDF file: {} has IO problem.", path.toString() );
				}
			} );

			int timeSize = fileSize * timeRange;
			if ( grids.size() != timeSize ){
				logger.log( LogLevel.WARN, "NetCDF GridMergeAdapter: The succeeded to read NetCDF grid time size: {} not equal with user inputs time range: {}, skip process.", grids.size(), timeSize );
			} else {
				Path mergePath = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
				logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Try to write the merge NetCDF grid with Path {}.", mergePath.toString() );
				try {
					NetCDFGridMapper.toTYXModel( mergePath, "Merge Grids", grids, true );
				} catch (IOException | InvalidRangeException e){
					logger.log( LogLevel.ERROR, "NetCDF GridMergeAdapter: Write the merge NetCDF grid with Path {} has something wrong.", mergePath.toString() );
				}
			}

			if ( modelArguments.isDeleteInputs() ){
				logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Start to delete inputs." );
				paths.forEach( PathUtils::deleteIfExists );
				logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Finished to delete inputs." );
			}
		}
	}
}
