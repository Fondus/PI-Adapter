package tw.fondus.fews.adapter.pi.nc;

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
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

/**
 * The model adapter used to restructure time x y variable to time lon lat grid type NetCDF, let can be used with process need this format.
 *
 * @author Brad Chen
 *
 */
public class GridXYToLatLonAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		PiIOArguments arguments = PiIOArguments.instance();
		new GridXYToLatLonAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "GridXYToLatLonAdapter: Starting adapter process." );
		PiIOArguments adapterArguments = this.asIOArguments( arguments );

		Path netcdfPath = Prevalidated.checkExists(
				inputPath.resolve( adapterArguments.getInputs().get( 0 ) ),
				"GridXYToLatLonAdapter: The input NetCDf do not exist." );

		try ( NetCDFReader reader = NetCDFReader.read( netcdfPath ) ){
			logger.log( LogLevel.INFO,"GridRestructureAdapter: Start read the input NetCDF." );

			// Read the T, Y, X, V arrays.
			String parameterId = adapterArguments.getParameter();
			var optionalTimeVariable = reader.findVariable( VariableName.TIME );
			var optionalXArray = reader.readVariable( VariableName.X );
			var optionalYArray = reader.readVariable( VariableName.Y  );
			var optionalVariable = reader.findVariable( parameterId );

			OptionalUtils.ifPresent( optionalTimeVariable, optionalXArray, optionalYArray, optionalVariable,
					(timeVariable, xArray, yArray, variable) -> {
				logger.log( LogLevel.INFO,"GridRestructureAdapter: Finished read the input NetCDF, try to restructure NetCDF." );
				var timeUnit = NetCDFUtils.readVariableAttribute( timeVariable, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_TIME_MINUTES );
				var parameterUnit = NetCDFUtils.readVariableAttribute( variable, VariableAttribute.KEY_UNITS, "-" );
				var missing = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_MISSING, Numbers.MISSING );
				var offset = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_OFFSET, Numbers.ZERO );
				var scale = NetCDFUtils.readVariableAttributeAsNumber( variable, VariableAttribute.KEY_SCALE, Numbers.ONE );

				// Coordinates
				var xCoordinates = NetCDFUtils.readOneDimensionArrayValues( xArray );
				var yCoordinates = NetCDFUtils.readOneDimensionArrayValues( yArray );
				int xSize = xCoordinates.size();
				int ySize = yCoordinates.size();

				try {
					var times = NetCDFUtils.readOneDimensionArrayValues( timeVariable.read() );
					var values = NetCDFUtils.readTYXDimensionArrayValues( variable.read() );

					var y = NetCDFUtils.create1DArrayDouble( yCoordinates );
					var x = NetCDFUtils.create1DArrayDouble( xCoordinates );
					var time = NetCDFUtils.create1DArrayDouble( times );
					var tyxValues = NetCDFUtils.create3DArrayFloat( values, ySize, xSize );
					var writer = this.buildNetCDF( outputPath.resolve( adapterArguments.getOutputs().get( 0 ) ),
							times.size(), xSize, ySize, timeUnit, parameterUnit, parameterId, missing, offset, scale );

					writer.writeValues( VariableName.TIME, time )
							.writeValues( VariableName.LAT, y )
							.writeValues( VariableName.LON, x )
							.writeValues( parameterId, tyxValues )
							.close();

				} catch (IOException | InvalidRangeException e) {
					logger.log( LogLevel.ERROR, "GridXYToLatLonAdapter: Read time variable has something wrong.", e );
				}
			} );

		} catch (IOException e) {
			logger.log( LogLevel.ERROR,"GridXYToLatLonAdapter: Read NetCDF has IO problem. {}", e.toString() );
		}
		logger.log( LogLevel.INFO, "GridXYToLatLonAdapter: Finished adapter process." );
	}

	/**
	 * Build the NetCDF.
	 *
	 * @param path write path
	 * @param tSize size of t
	 * @param xSize size of x
	 * @param ySize size if y
	 * @param timeUnit unit of time
	 * @param parameterUnit unit of parameter
	 * @param parameterId id of parameter
	 * @param missing missing
	 * @param offset offset
	 * @param scale scale
	 * @return NetCDF writer
	 * @throws IOException has IOException
	 */
	private NetCDFWriter buildNetCDF( Path path, int tSize, int xSize, int ySize,
			String timeUnit, String parameterUnit, String parameterId,
			BigDecimal missing, BigDecimal offset, BigDecimal scale ) throws IOException{
		return NetCDFBuilder.create( path )
				.addGlobalAttribute( GlobalAttribute.CONVENTIONS, "CF-1.6" )
				.addDimension( DimensionName.TIME, tSize )
				.addDimension( DimensionName.LAT, ySize )
				.addDimension( DimensionName.LON, xSize )
				.addVariable( VariableName.TIME, DataType.DOUBLE, DimensionName.TIME )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_NAME, DimensionName.TIME )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_NAME_LONG, DimensionName.TIME )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_UNITS, timeUnit )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_TIME )
				.addVariable( VariableName.LAT, DataType.DOUBLE, DimensionName.LAT )
				.addVariableAttribute( VariableName.LAT, VariableAttribute.KEY_NAME, VariableAttribute.COORDINATES_Y_WGS84 )
				.addVariableAttribute( VariableName.LAT, VariableAttribute.KEY_NAME_LONG, VariableAttribute.NAME_Y_WGS84 )
				.addVariableAttribute( VariableName.LAT, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_Y_WGS84 )
				.addVariableAttribute( VariableName.LAT, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_Y )
				.addVariable( VariableName.LON, DataType.DOUBLE, DimensionName.LON )
				.addVariableAttribute( VariableName.LON, VariableAttribute.KEY_NAME, VariableAttribute.COORDINATES_X_WGS84 )
				.addVariableAttribute( VariableName.LON, VariableAttribute.KEY_NAME_LONG, VariableAttribute.NAME_X_WGS84 )
				.addVariableAttribute( VariableName.LON, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_X_WGS84 )
				.addVariableAttribute( VariableName.LON, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_X )
				.addVariable( parameterId, DataType.FLOAT, DimensionName.TIME, DimensionName.LAT, DimensionName.LON )
				.addVariableAttribute( parameterId, VariableAttribute.KEY_NAME_LONG, parameterId )
				.addVariableAttribute( parameterId, VariableAttribute.KEY_UNITS, parameterUnit )
				.addVariableAttribute( parameterId, VariableAttribute.KEY_SCALE, scale.floatValue() )
				.addVariableAttribute( parameterId, VariableAttribute.KEY_OFFSET, offset.floatValue() )
				.addVariableAttribute( parameterId, VariableAttribute.KEY_MISSING, missing.floatValue() )
				.build();
	}
}
