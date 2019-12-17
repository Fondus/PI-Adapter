package tw.fondus.fews.adapter.pi.fim.ntu.util;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateTime;

import strman.Strman;
import tw.fondus.commons.nc.NetCDFBuilder;
import tw.fondus.commons.nc.util.key.DimensionName;
import tw.fondus.commons.nc.util.key.GlobalAttribute;
import tw.fondus.commons.nc.util.key.VariableAttribute;
import tw.fondus.commons.nc.util.key.VariableName;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.fews.adapter.pi.fim.ntu.entity.Coordinate;
import tw.fondus.fews.adapter.pi.fim.ntu.entity.PointData;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;

/**
 * The data builder for model output.
 * 
 * @author Chao
 *
 */
public class DataBuilder {
	public static final String DIMENSION_DEPTH = "depth_below_surface_simulated";

	public static void buildNetCDF( List<List<PointData>> datas, Path outputPath, DateTime inputEndTime )
			throws Exception {
		List<BigDecimal> yList = new ArrayList<>();
		datas.get( 0 )
				.stream()
				.sorted( Comparator.comparing( point -> point.getCoordinate().getY() ) )
				.forEach( point -> {
					if ( yList.size() == 0 ) {
						yList.add( point.getCoordinate().getY() );
					} else {
						if ( !(point.getCoordinate().getY().compareTo( yList.get( yList.size() - 1 ) ) == 0) ) {
							yList.add( point.getCoordinate().getY() );
						}
					}
				} );

		List<BigDecimal> xList = new ArrayList<>();
		datas.get( 0 )
				.stream()
				.sorted( Comparator.comparing( point -> point.getCoordinate().getX() ) )
				.forEach( point -> {
					if ( xList.size() == 0 ) {
						xList.add( point.getCoordinate().getX() );
					} else {
						if ( !(point.getCoordinate().getX().compareTo( xList.get( xList.size() - 1 ) ) == 0) ) {
							xList.add( point.getCoordinate().getX() );
						}
					}
				} );

		ArrayDouble.D1 y = new ArrayDouble.D1( yList.size() );
		ArrayDouble.D1 x = new ArrayDouble.D1( xList.size() );
		ArrayDouble.D1 times = new ArrayDouble.D1( datas.size() );
		ArrayFloat.D3 depth = new ArrayFloat.D3( datas.size(), yList.size(), xList.size() );

		IntStream.range( 0, yList.size() ).forEach( i -> {
			y.set( i, yList.get( i ).doubleValue() );
		} );

		IntStream.range( 0, xList.size() ).forEach( i -> {
			x.set( i, xList.get( i ).doubleValue() );
		} );

		IntStream.rangeClosed( 1, datas.size() ).forEach( i -> {
			long time = inputEndTime.plusHours( i ).getMillis() / (60 * 1000);

			times.set( i - 1, time );
		} );

		IntStream.range( 0, datas.size() ).forEach( t -> {
			Map<Coordinate, PointData> map = listToMap( datas.get( t ) );
			IntStream.range( 0, yList.size() ).forEach( j -> {
				IntStream.range( 0, xList.size() ).forEach( i -> {
					Optional<PointData> optValue = Optional
							.ofNullable( map.get( new Coordinate( xList.get( i ), yList.get( j ) ) ) );
					OptionalUtils.ifPresentOrElse( optValue, ( pointData ) -> {
						depth.set( t, j, i, pointData.getValue().floatValue() );
					}, () -> {
						depth.set( t, j, i, Float.NaN );
					} );
				} );
			} );
		} );

		Map<String, Array> valueMap = new HashMap<>();
		valueMap.put( DimensionName.X, x );
		valueMap.put( DimensionName.Y, y );
		valueMap.put( DimensionName.TIME, times );
		valueMap.put( DIMENSION_DEPTH, depth );
		writeNetCDF( valueMap, outputPath );
	}

	private static Map<Coordinate, PointData> listToMap( List<PointData> points ) {
		return points.stream().collect( Collectors.toMap( PointData::getCoordinate, point -> point ) );
	}

	private static void writeNetCDF( Map<String, Array> valueMap, Path outputPath ) throws Exception {
		DateTime createTime = new DateTime();
		NetCDFBuilder.create( outputPath.toString() )
				.addGlobalAttribute( GlobalAttribute.CONVENTIONS, "CF-1.6" )
				.addGlobalAttribute( GlobalAttribute.TITLE, "NTU_2DFIN model output" )
				.addGlobalAttribute( GlobalAttribute.INSTITUTION, "FondUS" )
				.addGlobalAttribute( GlobalAttribute.SOURCE, "NTU_2DFIN model output from FEWS-Taiwan" )
				.addGlobalAttribute( GlobalAttribute.HISTORY,
						Strman.append( createTime.toString(), " GMT: NTU_2DFIN model output from FEWS-Taiwan" ) )
				.addGlobalAttribute( GlobalAttribute.REFERENCES, "http://www.delft-fews.com" )
				.addGlobalAttribute( GlobalAttribute.METADATA_CONVENTIONS, "Unidata Dataset Discovery v1.0" )
				.addGlobalAttribute( GlobalAttribute.SUMMARY, "NTU_2DFIN model output from FEWS-Taiwan" )
				.addGlobalAttribute( GlobalAttribute.DATE_CREATE, Strman.append( createTime.toString(), " GMT" ) )
				.addDimension( DimensionName.TIME, (int) valueMap.get( DimensionName.TIME ).getSize() )
				.addDimension( DimensionName.Y, (int) valueMap.get( DimensionName.Y ).getSize() )
				.addDimension( DimensionName.X, (int) valueMap.get( DimensionName.X ).getSize() )
				.addVariable( VariableName.TIME, DataType.DOUBLE, new String[] { DimensionName.TIME } )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_NAME, "time" )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_NAME_LONG, "time" )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_TIME )
				.addVariableAttribute( VariableName.TIME, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_TIME )
				.addVariable( VariableName.Y, DataType.DOUBLE, new String[] { DimensionName.Y } )
				.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_NAME,
						VariableAttribute.COORDINATES_Y )
				.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_NAME_LONG, VariableAttribute.NAME_Y_TWD97 )
				.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_TWD97 )
				.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_Y )
				.addVariableAttribute( VariableName.Y, VariableAttribute.KEY_MISSINGVALUE,
						VariableAttribute.MISSINGVALUE_COORDINATES )
				.addVariable( VariableName.X, DataType.DOUBLE, new String[] { DimensionName.X } )
				.addVariableAttribute( VariableName.X, VariableAttribute.KEY_NAME,
						VariableAttribute.COORDINATES_X )
				.addVariableAttribute( VariableName.X, VariableAttribute.KEY_NAME_LONG, VariableAttribute.NAME_X_TWD97 )
				.addVariableAttribute( VariableName.X, VariableAttribute.KEY_UNITS, VariableAttribute.UNITS_TWD97 )
				.addVariableAttribute( VariableName.X, VariableAttribute.KEY_AXIS, VariableAttribute.AXIS_X )
				.addVariableAttribute( VariableName.X, VariableAttribute.KEY_MISSINGVALUE,
						VariableAttribute.MISSINGVALUE_COORDINATES )
				.addVariable( DIMENSION_DEPTH, DataType.FLOAT,
						new String[] { DimensionName.TIME, DimensionName.Y, DimensionName.X } )
				.addVariableAttribute( DIMENSION_DEPTH, VariableAttribute.KEY_NAME_LONG, DIMENSION_DEPTH )
				.addVariableAttribute( DIMENSION_DEPTH, VariableAttribute.KEY_UNITS, "m" )
				.addVariableAttribute( DIMENSION_DEPTH, VariableAttribute.KEY_MISSINGVALUE,
						VariableAttribute.MISSINGVALUE )
				.build() // Finished NetCDF file structures define mode
				.writeValues( VariableName.TIME, valueMap.get( DimensionName.TIME ) )
				.writeValues( VariableName.Y, valueMap.get( DimensionName.Y ) )
				.writeValues( VariableName.X, valueMap.get( DimensionName.X ) )
				.writeValues( DIMENSION_DEPTH, valueMap.get( DIMENSION_DEPTH ) )
				.close(); // close IO
	}
}
