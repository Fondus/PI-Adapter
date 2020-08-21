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
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.Strings;
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
import java.util.stream.IntStream;

/**
 * The model adapter use to restructure the grid type of NetCDF, let can be import to Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
public class GridRestructureAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		RestructureArguments arguments = RestructureArguments.instance();
		new GridRestructureAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		RestructureArguments modelArguments = this.asArguments( arguments, RestructureArguments.class );

		Path netcdfPath = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"GridRestructureAdapter: The input NetCDf do not exist." );

		String timeName =  modelArguments.getTName();
		String yName =  modelArguments.getYName();
		String xName =  modelArguments.getXName();
		String variableName =  modelArguments.getVariableName();

		logger.log( LogLevel.INFO,"GridRestructureAdapter: The user specific T: {}, Y: {}, X: {}, V: {}.", timeName, yName, xName, variableName );
		try ( NetCDFReader reader = NetCDFReader.read( netcdfPath ) ){
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Start read the input NetCDF." );

			// Read the T, Y, X, V arrays.
			Optional<Variable> optionalTimeVariable = reader.findVariable( timeName );
			Optional<Variable> optionalYVariable = reader.findVariable( yName );
			Optional<Variable> optionalXVariable = reader.findVariable( xName );
			Optional<Array> optionalVariableArray= reader.readVariable( variableName );

			// Get time unit and T, Y ,X values.
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Read dimension values from the input NetCDF." );
			String parameter = modelArguments.getParameter();
			String unit = modelArguments.getUnit();

			OptionalUtils.ifPresent( optionalTimeVariable, optionalYVariable, optionalXVariable, optionalVariableArray,
				( time, y, x, variableArray ) -> {
					String timeUnit = NetCDFUtils.readVariableAttribute( time, VariableAttribute.KEY_UNITS, "" );
					if ( modelArguments.isTimeZoneFlag() ){
						timeUnit = Strman.append( timeUnit, Strings.SPACE, modelArguments.getTimeZone() );
					}

					try {
						List<BigDecimal> times = this.readOneDimensionValues( time );
						List<BigDecimal> yCoordinates = this.readOneDimensionValues( y );
						List<BigDecimal> xCoordinates = this.readOneDimensionValues( x );

						logger.log( LogLevel.INFO,"GridRestructureAdapter: Read grid values from the input NetCDF." );
						List<List<BigDecimal>> timeGrids = this.readGridValues( variableArray,
								modelArguments.getTOrder(), modelArguments.getYOrder(), modelArguments.getXOrder() );
						if ( timeGrids != null ){
							// Build the restructure NetCDF
							String outputNetCDF = modelArguments.getOutputs().get( 0 );
							Path outputNetCDFPath = outputPath.resolve( outputNetCDF );

							logger.log( LogLevel.INFO,"GridRestructureAdapter: Start build restructure NetCDF with path: {}.", outputNetCDF );
							this.buildNetCDF( outputNetCDFPath, timeUnit, parameter, unit, times, yCoordinates, xCoordinates, timeGrids );
							logger.log( LogLevel.INFO,"GridRestructureAdapter: Finished build restructure NetCDF with path: {}.", outputNetCDF );
						} else {
							logger.log( LogLevel.WARN,"GridRestructureAdapter: The NetCDF shape not same as user inputs." );
						}
					} catch ( IOException e ) {
						logger.log( LogLevel.ERROR, "GridRestructureAdapter: Read NetCDF has IO problem." );
					}
				} );
		} catch ( IOException e){
			logger.log( LogLevel.ERROR,"GridRestructureAdapter: Read NetCDF has IO problem. {}", e.toString() );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR,"GridRestructureAdapter: Read NetCDF has problem. {}", e.toString() );
		}
	}

	/**
	 * Define and build the restructure NetCDF.
	 *
	 * @param outputNetCDFPath output path of NetCDF
	 * @param timeUnit time unit
	 * @param parameter parameter
	 * @param unit unit
	 * @param times times
	 * @param yCoordinates y coordinates
	 * @param xCoordinates x coordinates
	 * @param timeGrids time grids
	 * @throws IOException has IO Exception
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
			.addVariable( VariableName.TIME, DataType.DOUBLE, DimensionName.TIME )
			.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_NAME, DimensionName.TIME )
			.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_UNITS, timeUnit )
			.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_TIME )
			.addVariable( VariableName.Y, DataType.DOUBLE, DimensionName.Y )
			.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_NAME, VariableAttribute.COORDINATES_Y_WGS84 )
			.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_Y_WGS84 )
			.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_Y )
			.addVariable( VariableName.X, DataType.DOUBLE, DimensionName.X )
			.addVariableAttribute( VariableName.X, VariableAttribute.KEY_NAME, VariableAttribute.COORDINATES_X_WGS84 )
			.addVariableAttribute( VariableName.X, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_X_WGS84 )
			.addVariableAttribute( VariableName.X, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_X )
			.addVariable( parameter, DataType.FLOAT, DimensionName.TIME, DimensionName.Y, DimensionName.X )
			.addVariableAttribute( parameter, VariableAttribute.KEY_NAME_LONG, parameter )
			.addVariableAttribute( parameter, VariableAttribute.KEY_UNITS, unit )
			.build();
		// Write data into NetCDF
		try {
			this.writeNetCDF( writer, parameter, times, yCoordinates, xCoordinates, timeGrids );
		} catch (InvalidRangeException e) {
			this.getLogger().log( LogLevel.ERROR, "GridRestructureAdapter: Write the NetCDF has problem." );
		}
	}

	/**
	 * Write values to NetCDF.
	 *
	 * @param writer writer
	 * @param parameter parameter
	 * @param times times
	 * @param yCoordinates y coordinates
	 * @param xCoordinates x coordinates
	 * @param timeGrids time grids
	 * @throws IOException has IO Exception
	 * @throws InvalidRangeException has InvalidRange Exception
	 */
	private void writeNetCDF( NetCDFWriter writer, String parameter, List<BigDecimal> times, List<BigDecimal> yCoordinates,
			List<BigDecimal> xCoordinates, List<List<BigDecimal>> timeGrids )
			throws IOException, InvalidRangeException {
		ArrayDouble.D1 yArray = NetCDFUtils.create1DArrayDouble( yCoordinates );
		ArrayDouble.D1 xArray = NetCDFUtils.create1DArrayDouble( xCoordinates );
		ArrayDouble.D1 timeArray = NetCDFUtils.create1DArrayDouble( times );
		ArrayFloat.D3 gridArray = NetCDFUtils.create3DArrayFloat( timeGrids, yCoordinates.size(), xCoordinates.size() );

		writer.writeValues( VariableName.TIME, timeArray )
				.writeValues( VariableName.Y, yArray )
				.writeValues( VariableName.X, xArray )
				.writeValues( parameter, gridArray )
				.close();
	}

	/**
	 * Read the one dimension array values to list.
	 *
	 * @param variable variable
	 * @return variable values
	 */
	private List<BigDecimal> readOneDimensionValues( Variable variable ) throws IOException {
		BigDecimal scale = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_SCALE, new BigDecimal( "1" ) );
		BigDecimal offset = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_OFFSET, BigDecimal.ZERO );
		BigDecimal missing = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_MISSING, VariableAttribute.MISSING );
		return NetCDFUtils.readOneDimensionArrayValues( variable.read(), scale, offset, missing );
	}

	/**
	 * Read the grid array values to list by orders.
	 *
	 * @param array array
	 * @param tOrder time order
	 * @param yOrder y order
	 * @param xOrder x otder
	 * @return time grid data
	 */
	private List<List<BigDecimal>> readGridValues( Array array, int tOrder, int yOrder, int xOrder ){
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

				IntStream.range( 0, ySize ).forEach( y ->
					IntStream.range( 0, xSize ).forEach( x ->
						timeGrids.get( time ).add(
								NetCDFUtils.readArrayValue( array,
										index.set( NetCDFUtils.createTYXIndexByOrder( time, y, x, tOrder, yOrder, xOrder ) ) )
						)
					)
				);
			} );
			return timeGrids;
		}
	}
}
