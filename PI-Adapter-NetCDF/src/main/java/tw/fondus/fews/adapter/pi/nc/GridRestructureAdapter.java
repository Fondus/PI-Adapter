package tw.fondus.fews.adapter.pi.nc;

import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFBuilder;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.nc.NetCDFWriter;
import tw.fondus.commons.nc.util.NetCDFUtils;
import tw.fondus.commons.nc.util.key.DimensionName;
import tw.fondus.commons.nc.util.key.GlobalAttribute;
import tw.fondus.commons.nc.util.key.VariableAttribute;
import tw.fondus.commons.nc.util.key.VariableName;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.nc.argument.RestructureArguments;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The model adapter use to restructure the grid type of NetCDF, let can be import to Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class GridRestructureAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		RestructureArguments arguments = new RestructureArguments();
		new GridRestructureAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RestructureArguments modelArguments = (RestructureArguments) arguments;

		Path netcdfPath = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"GridRestructureAdapter: The input NetCDf do not exist." );

		String timeName =  modelArguments.getTName();
		String yName =  modelArguments.getYName();
		String xName =  modelArguments.getXName();
		String variableName =  modelArguments.getVariableName();

		logger.log( LogLevel.INFO,"GridRestructureAdapter: The user specific T: {}, Y: {}, X: {}, V: {}.", timeName, yName, xName, variableName );

		try ( NetCDFReader reader = NetCDFReader.read( netcdfPath.toString() ) ){
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Start read the input NetCDF." );

			// Read the T, Y, X, V arrays.
			Optional<Array> optionalTimeArray = reader.readVariable( timeName );
			Optional<Array> optionalYArray = reader.readVariable( yName );
			Optional<Array> optionalXArray = reader.readVariable( xName );
			Optional<Array> optionalVariableArray = reader.readVariable( variableName );

			// Get time unit and T, Y ,X values.
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Read dimension values from the input NetCDF." );
			String parameter = modelArguments.getParameter();
			String unit = modelArguments.getUnit();
			String timeUnit = this.readUnit( reader.findVariable( timeName ) );
			if ( modelArguments.isTimeZoneFlag() ){
				timeUnit = Strman.append( timeUnit, StringUtils.SPACE_WHITE, modelArguments.getTimeZone() );
			}

			List<BigDecimal> times = this.readOneDimensionValues( optionalTimeArray, timeName );
			List<BigDecimal> yCoordinates = this.readOneDimensionValues( optionalYArray, yName );
			List<BigDecimal> xCoordinates = this.readOneDimensionValues( optionalXArray, xName );

			// Read grid values.
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Read grid values from the input NetCDF." );
			List<List<BigDecimal>> timeGrids = this.readGridValues( optionalVariableArray,
					modelArguments.getTOrder(), modelArguments.getYOrder(), modelArguments.getXOrder() );

			// Build the restructure NetCDF
			String outputNetCDF = modelArguments.getOutputs().get( 0 );
			Path outputNetCDFPath = outputPath.resolve( outputNetCDF );
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Start build restructure NetCDF with path: {}.", outputNetCDF );
			this.buildNetCDF( outputNetCDFPath, timeUnit, parameter, unit, times, yCoordinates, xCoordinates, timeGrids );
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Finished build restructure NetCDF with path: {}.", outputNetCDF );

		} catch ( IOException e){
			logger.log( LogLevel.ERROR,"GridRestructureAdapter: Read NetCDF has IO problem." );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR,"GridRestructureAdapter: Read NetCDF has problem." );
		}
	}

	/**
	 * Define and build the restructure NetCDF.
	 *
	 * @param outputNetCDFPath
	 * @param timeUnit
	 * @param parameter
	 * @param unit
	 * @param times
	 * @param yCoordinates
	 * @param xCoordinates
	 * @param timeGrids
	 * @throws IOException
	 */
	private void buildNetCDF( Path outputNetCDFPath, String timeUnit, String parameter, String unit,
			List<BigDecimal> times, List<BigDecimal> yCoordinates,
			List<BigDecimal> xCoordinates, List<List<BigDecimal>> timeGrids )
			throws IOException {
		NetCDFWriter writer = NetCDFBuilder.create( outputNetCDFPath.toString() )
			.addGlobalAttribute( GlobalAttribute.CONVENTIONS, "CF-1.6" )
			.addGlobalAttribute( GlobalAttribute.TITLE, "Restructure file" )
			.addDimension( DimensionName.TIME, times.size() )
			.addDimension( DimensionName.Y, yCoordinates.size() )
			.addDimension( DimensionName.X, xCoordinates.size() )
			.addVariable( VariableName.TIME, DataType.DOUBLE, new String[] { DimensionName.TIME } )
			.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_NAME, DimensionName.TIME )
			.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_UNITS, timeUnit )
			.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_TIME )
			.addVariable( VariableName.Y, DataType.DOUBLE, new String[] { DimensionName.Y })
			.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_NAME, VariableAttribute.COORDINATES_Y_WGS84 )
			.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_Y_WGS84 )
			.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_Y )
			.addVariable( VariableName.X, DataType.DOUBLE, new String[] { DimensionName.X })
			.addVariableAttribute( VariableName.X, VariableAttribute.KEY_NAME, VariableAttribute.COORDINATES_X_WGS84 )
			.addVariableAttribute( VariableName.X, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_X_WGS84 )
			.addVariableAttribute( VariableName.X, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_X )
			.addVariable( parameter, DataType.FLOAT, new String[] { DimensionName.TIME, DimensionName.Y, DimensionName.X })
			.addVariableAttribute( parameter, VariableAttribute.KEY_NAME_LONG, parameter )
			.addVariableAttribute( parameter, VariableAttribute.KEY_UNITS, unit )
			.build();
		// Write data into NetCDF
		try {
			this.writeNetCDF( writer, parameter, times, yCoordinates, xCoordinates, timeGrids );
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write values to NetCDF.
	 *
	 * @param writer
	 * @param parameter
	 * @param times
	 * @param yCoordinates
	 * @param xCoordinates
	 * @param timeGrids
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	private void writeNetCDF( NetCDFWriter writer, String parameter, List<BigDecimal> times, List<BigDecimal> yCoordinates,
			List<BigDecimal> xCoordinates, List<List<BigDecimal>> timeGrids )
			throws IOException, InvalidRangeException {
		ArrayDouble.D1 yArray = new ArrayDouble.D1( yCoordinates.size() );
		ArrayDouble.D1 xArray = new ArrayDouble.D1( xCoordinates.size() );
		ArrayDouble.D1 timeArray = new ArrayDouble.D1( times.size() );
		ArrayFloat.D3 gridArray = new ArrayFloat.D3( times.size(), yCoordinates.size(), xCoordinates.size() );

		// Create the dimension value
		IntStream.range( 0, yCoordinates.size() ).forEach( y -> yArray.set( y, yCoordinates.get( y ).doubleValue() ) );
		IntStream.range( 0, xCoordinates.size() ).forEach( x -> xArray.set( x, xCoordinates.get( x ).doubleValue() ) );
		IntStream.range( 0, times.size() ).forEach( t -> timeArray.set( t, times.get( t ).longValue() ) );

		// Create the grid value
		IntStream.range( 0, times.size()  ).forEach( t -> {
			IntStream.range( 0, yCoordinates.size() ).forEach( y -> {
				IntStream.range( 0, xCoordinates.size() ).forEach( x -> {
					gridArray.set( t, y, x, timeGrids.get( t ).get( y * xCoordinates.size() + x ).floatValue() );
				} );
			} );
		} );

		writer.writeValues( VariableName.TIME, timeArray )
				.writeValues( VariableName.Y, yArray )
				.writeValues( VariableName.X, xArray )
				.writeValues( parameter, gridArray )
				.close();
	}

	/**
	 * Read the unit attribute from the variable.
	 *
	 * @param optional
	 * @return
	 */
	private String readUnit( Optional<Variable> optional ){
		return optional.map( variable -> variable.findAttribute( VariableAttribute.KEY_UNITS ) )
				.map( attribute -> attribute.getStringValue() )
				.orElseThrow( () -> new IllegalArgumentException( "GridRestructureAdapter: The variable attribute not exist." ) );
	}

	/**
	 * Read the one dimension array values to list.
	 *
	 * @param optional
	 * @param name
	 * @return
	 */
	private List<BigDecimal> readOneDimensionValues( Optional<Array> optional, String name ){
		return optional.map( array -> {
			int length = array.getShape()[0];
			return IntStream.range( 0, length )
					.mapToObj( i -> NetCDFUtils.readArrayValue( array, i ) )
					.collect( Collectors.toList());
		} ).orElseThrow( () -> new IllegalArgumentException( Strman.append( "GridRestructureAdapter: The 1D dimension variable: ", name, "not exist!" ) ) );
	}

	/**
	 * Read the grid array values to list by orders.
	 *
	 * @param optional
	 * @param tOrder
	 * @param yOrder
	 * @param xOrder
	 * @return
	 */
	private List<List<BigDecimal>> readGridValues( Optional<Array> optional, int tOrder, int yOrder, int xOrder ){
		return optional.map( array -> {
			int[] shape = array.getShape();
			if ( shape.length != 3 || tOrder >= 3 || yOrder >= 3 || xOrder >= 3 ){
				return null;
			} else {
				List<List<BigDecimal>> timeGrids = new ArrayList<>();
				int timeSize = shape[ tOrder ];
				int ySize = shape[ yOrder ];
				int xSize = shape[ xOrder ];
				Index index = array.getIndex();

				IntStream.range( 0, timeSize ).forEach( time -> {
					timeGrids.add( new ArrayList<>() );

					IntStream.range( 0, ySize ).forEach( y -> {
						IntStream.range( 0, xSize ).forEach( x -> {
							timeGrids.get( time ).add(
									NetCDFUtils.readArrayValue( array,
											index.set( this.createIndexByOrder( time, y, x, tOrder, yOrder, xOrder ) ) )
							);
						} );
					} );
				} );
				return timeGrids;
			}
		} ).orElseThrow( () -> new IllegalArgumentException( "GridRestructureAdapter: The value variable not exist." ) );
	}

	/**
	 * Create the index by values and orders.
	 *
	 * @param time
	 * @param y
	 * @param x
	 * @param tOrder
	 * @param yOrder
	 * @param xOrder
	 * @return
	 */
	private int[] createIndexByOrder( int time, int y, int x, int tOrder, int yOrder, int xOrder ){
		int[] index = new int[3];
		index[tOrder] = time;
		index[yOrder] = y;
		index[xOrder] = x;
		return index;
	}
}
