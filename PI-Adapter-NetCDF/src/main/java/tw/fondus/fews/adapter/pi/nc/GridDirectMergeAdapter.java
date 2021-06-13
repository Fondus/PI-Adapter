package tw.fondus.fews.adapter.pi.nc;

import lombok.Builder;
import lombok.Getter;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.nc.util.NetCDFUtils;
import tw.fondus.commons.nc.util.key.VariableAttribute;
import tw.fondus.commons.spatial.model.grid.StandardGrid;
import tw.fondus.commons.spatial.util.crs.EPSG;
import tw.fondus.commons.spatial.util.nc.NetCDFGridMapper;
import tw.fondus.commons.spatial.util.nc.PiNetCDFBuilder;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.nc.argument.MergeArguments;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The model adapter use to merge the grid type of NetCDF with bottom api.
 *
 * @author Brad Chen
 *
 */
public class GridDirectMergeAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		MergeArguments arguments = MergeArguments.instance();
		new GridDirectMergeAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		MergeArguments modelArguments = this.asArguments( arguments, MergeArguments.class );

		logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Start the merge NetCDF grids process." );
		int fileSize = modelArguments.getCount();
		int timeRange = modelArguments.getTimeRange();
		String parameter = modelArguments.getParameter();
		List<Path> paths = PathUtils.list( inputPath, path -> PathUtils.equalsExtension( path, FileType.NETCDF ) );
		if ( paths.size() != fileSize ){
			logger.log( LogLevel.ERROR, "NetCDF GridMergeAdapter: Input NetCDF file size: {} not equal with user inputs: {}, skip process.", paths.size(), fileSize );
		} else {
			logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Try to read the input NetCDF grids." );

			this.readTXYInformation( paths.get( 0 ), parameter ).ifPresent( yxInfo -> {
				List<BigDecimal> totalTimes = CollectionUtils.emptyListArray();
				List<List<BigDecimal>> totalTYX = CollectionUtils.emptyListArray();
				paths.forEach( path -> {
					try {
						this.readTXYGridValues( path, totalTimes, totalTYX, yxInfo, parameter, timeRange );
					} catch (IOException e) {
						logger.log( LogLevel.ERROR, "NetCDF GridMergeAdapter: Read the input NetCDF: {} TYX values has problem.", path.toString() );
					}
				} );

				int timeSize = fileSize * timeRange;
				if ( totalTimes.size() != timeSize ){
					logger.log( LogLevel.WARN, "NetCDF GridMergeAdapter: The succeeded to read NetCDF grid time size: {} not equal with user inputs time range: {}, skip process.", totalTimes.size(), timeSize );
				} else {
					Path mergePath = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );
					logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Try to write the merge NetCDF grid with Path {}.", mergePath.toString() );
					try {
						PiNetCDFBuilder.create( mergePath, "Merge Grids" )
								.dimensionsGridTYX( totalTimes.size(), yxInfo.y.size(), yxInfo.x.size() )
								.variableXY( yxInfo.isWGS84() ? EPSG.WGS84 : EPSG.TWD97_TM2_121, false )
								.variableTime()
								.variableValueGridTYX( parameter, yxInfo.unit, Numbers.MISSING )
								.build()
								.writeGridTYX( parameter,
										NetCDFUtils.create1DArrayDouble( totalTimes ),
										NetCDFUtils.create1DArrayDouble( yxInfo.y ),
										NetCDFUtils.create1DArrayDouble( yxInfo.x ),
										NetCDFUtils.create3DArrayShort( totalTYX, yxInfo.y.size(), yxInfo.x.size() ) )
								.close();
					} catch (IOException | InvalidRangeException e){
						logger.log( LogLevel.ERROR, "NetCDF GridMergeAdapter: Write the merge NetCDF grid with Path {} has something wrong.", mergePath.toString() );
					}
				}

				if ( modelArguments.isDeleteInputs() ){
					logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Start to delete inputs." );
					paths.forEach( PathUtils::deleteIfExists );
					logger.log( LogLevel.INFO, "NetCDF GridMergeAdapter: Finished to delete inputs." );
				}
			} );
		}
	}

	/**
	 * The process to read grid TYX value with time range.
	 *
	 * @param path path
	 * @param totalTimes time store
	 * @param totalTYX tyx values store
	 * @param yxInfo grid information
	 * @param parameter parameter
	 * @param timeRange timeRange
	 * @throws IOException IOException
	 */
	private void readTXYGridValues( Path path, List<BigDecimal> totalTimes, List<List<BigDecimal>> totalTYX, XYInformation yxInfo, String parameter, int timeRange )
			throws IOException {
		try ( NetCDFReader reader = NetCDFReader.read( path ) ){
			List<Long> times = reader.findTimes();
			if ( times.size() < timeRange ){
				this.getLogger().log( LogLevel.WARN, "NetCDF GridMergeAdapter: The NetCDF grid time size: {} not exceed than user inputs time range: {}, skip process.",
						times.size(), timeRange );
			} else {
				// Times
				totalTimes.addAll( times.stream().map( NumberUtils::create ).limit( timeRange ).collect( Collectors.toList()) );

				// Values
				Optional<Variable> optionalVariable = reader.findVariable( parameter );
				if ( optionalVariable.isPresent() ){
					Variable variable = optionalVariable.get();
					BigDecimal missing = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_MISSING, VariableAttribute.MISSING );
					Array array = variable.read();
					IntStream.range( 0, timeRange ).forEach( i -> {
						List<BigDecimal> grid = NetCDFUtils.sliceTDimensionArrayYXValues( array, i, yxInfo.getScale(), yxInfo.getScale(), missing, false );
						totalTYX.add( grid );
					} );
				} else{
					this.getLogger().log( LogLevel.WARN, "NetCDFGridMapper: The NetCDF hasn't variable with id {}.", parameter );
				}
			}
		}
	}

	/**
	 * The process to read grid information.
	 *
	 * @param path path
	 * @param parameter parameter
	 * @return information
	 */
	private Optional<XYInformation> readTXYInformation( Path path, String parameter ){
		try ( NetCDFReader reader = NetCDFReader.read( path ) ){
			StandardGrid grid = NetCDFGridMapper.fromSliceTYXModel( reader, parameter, 0 );
			return Optional.of( XYInformation.builder()
					.x( grid.coordinateX() )
					.y( grid.coordinateY() )
					.offset( grid.getOffset() )
					.scale( grid.getScale() )
					.unit( grid.getUnit() )
					.isWGS84( reader.isWGS84() )
					.build() );
		} catch ( IOException e){
			this.getLogger().log( LogLevel.ERROR,"NetCDF GridMergeAdapter: Read NetCDF file: {} TXY information has IO problem.", path.toString() );
		}
		return Optional.empty();
	}

	/**
	 * The POJO use to store grid basic information.
	 */
	@Getter
	@Builder
	private static class XYInformation {
		private final List<BigDecimal> x;
		private final List<BigDecimal> y;
		private final BigDecimal offset;
		private final BigDecimal scale;
		private final String unit;
		private final boolean isWGS84;
	}
}
