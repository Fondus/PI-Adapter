package tw.fondus.fews.adapter.pi.hecras.ntou;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.spatial.util.crs.EPSG;
import tw.fondus.commons.spatial.util.jts.JTSUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.commons.util.time.TimeFormats;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.hecras.ntou.argument.ProcessArguments;
import tw.fondus.fews.adapter.pi.hecras.ntou.entity.PointData;
import tw.fondus.fews.adapter.pi.hecras.ntou.property.MappingProperties;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;

/**
 * Model Post-Adapter for running NTOU HEC-RAS model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
@SuppressWarnings( "rawtypes" )
public class HECRASPostAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ProcessArguments arguments = ProcessArguments.instance();
		new HECRASPostAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "HECRASPostAdapter: Starting HECRASPostAdapter." );
		ProcessArguments processArguments = (ProcessArguments) arguments;
		
		Path executablePath = Prevalidated.checkExists( basePath.resolve( processArguments.getExecutableDir() ),
				"HECRASPreAdapter: Can not find the directory of case executable." );

		try (NetCDFReader geometryReader = NetCDFReader
				.read( executablePath.resolve( processArguments.getInputs().get( 0 ) ).toString() ) ;
				NetCDFReader resultReader = NetCDFReader
						.read( executablePath.resolve( processArguments.getInputs().get( 1 ) ).toString() ) ) {
			List<PointData> pointDatas = new ArrayList<>();

			ArrayDouble.D2 rightCoorArray = (ArrayDouble.D2) geometryReader
					.readVariable( MappingProperties.getProperty( MappingProperties.HDF5_COORDINATE_RIGHT ) )
					.get();

			ArrayFloat.D2 rightResultArray = (ArrayFloat.D2) resultReader
					.readVariable( MappingProperties.getProperty( MappingProperties.HDF5_DEPTH_RIGHT ) )
					.get();

			for ( int point = 0; point < rightCoorArray.getShape()[0]; point++ ) {
				PointData pointData = new PointData( NumberUtils.create( rightCoorArray.get( point, 0 ) ),
						NumberUtils.create( rightCoorArray.get( point, 1 ) ) );
				for ( int data = 0; data < rightResultArray.getShape()[0]; data++ ) {
					pointData.addvalue( rightResultArray.get( data, point ) );
				}
				pointDatas.add( pointData );
			}

			ArrayDouble.D2 leftCoorArray = (ArrayDouble.D2) geometryReader
					.readVariable( MappingProperties.getProperty( MappingProperties.HDF5_COORDINATE_LEFT ) )
					.get();

			ArrayFloat.D2 leftResultArray = (ArrayFloat.D2) resultReader
					.readVariable( MappingProperties.getProperty( MappingProperties.HDF5_DEPTH_LEFT ) )
					.get();

			for ( int point = 0; point < leftCoorArray.getShape()[0]; point++ ) {
				PointData pointData = new PointData( new BigDecimal( leftCoorArray.get( point, 0 ) ),
						new BigDecimal( leftCoorArray.get( point, 1 ) ) );
				for ( int data = 0; data < leftResultArray.getShape()[0]; data++ ) {
					pointData.addvalue( leftResultArray.get( data, point ) );
				}
				pointDatas.add( pointData );
			}

			/** Write CSV data for locationSet **/
			List<String> location = new ArrayList<>();
			location.add( "locationId,x,y" );
			for ( int i = 0; i < pointDatas.size(); i++ ) {
				location.add( Strman.append( processArguments.getCaseName(), Strings.UNDERLINE, String.valueOf( i ),
						Strings.COMMA, pointDatas.get( i ).getX().toString(), Strings.COMMA,
						pointDatas.get( i ).getY().toString() ) );
			}
			PathWriter.write( outputPath.resolve( processArguments.getOutputs().get( 0 ) ),
					location.stream().collect( Collectors.joining( Strings.BREAKLINE ) ) );

			/** Write CSV format data of model output **/
			List<String> values = new ArrayList<>();
			StringJoiner commaJoiner = new StringJoiner( Strings.COMMA );
			commaJoiner.add( "locationName" );
			for ( int i = 0; i < pointDatas.size(); i++ ) {
				commaJoiner
						.add( Strman.append( processArguments.getCaseName(), Strings.UNDERLINE, String.valueOf( i ) ) );
			}
			values.add( commaJoiner.toString() );

			commaJoiner = new StringJoiner( Strings.COMMA );
			commaJoiner.add( "locationId" );
			for ( int i = 0; i < pointDatas.size(); i++ ) {
				commaJoiner
						.add( Strman.append( processArguments.getCaseName(), Strings.UNDERLINE, String.valueOf( i ) ) );
			}
			values.add( commaJoiner.toString() );

			commaJoiner = new StringJoiner( Strings.COMMA );
			commaJoiner.add( "Time" );
			for ( int i = 0; i < pointDatas.size(); i++ ) {
				commaJoiner.add( processArguments.getParameter() );
			}
			values.add( commaJoiner.toString() );

			String startTime = resultReader.findVariable( MappingProperties.getProperty( MappingProperties.HDF5_TIME ) )
					.get()
					.getAttributes()
					.stream()
					.filter( attribute -> attribute.getFullName()
							.equals( MappingProperties.getProperty( MappingProperties.HDF5_TIME_ATTRIBUTE ) ) )
					.findFirst()
					.get()
					.getStringValue()
					.trim();
			startTime = this.checkTime( startTime );
			DateTimeFormatter formatter = DateTimeFormat.forPattern( "ddMMMyyyy HHmm" );
			DateTime dateTime = formatter.withLocale( Locale.ENGLISH ).parseDateTime( startTime );
			for ( int time = 0; time < pointDatas.get( 0 ).getValues().size(); time++ ) {
				commaJoiner = new StringJoiner( Strings.COMMA );
				commaJoiner.add( JodaTimeUtils.toString( dateTime.plusHours( time ), TimeFormats.YMDHMS ) );
				for ( int i = 0; i < pointDatas.size(); i++ ) {
					commaJoiner.add( String.valueOf( pointDatas.get( i ).getValues().get( time ) ) );
				}
				values.add( commaJoiner.toString() );
			}
			PathWriter.write( outputPath.resolve( processArguments.getOutputs().get( 1 ) ),
					values.stream().collect( Collectors.joining( Strings.BREAKLINE ) ) );

			/** Get the flow data from cross section **/
			TimeSeriesArrays waterLevelTimeSeriesArrays = TimeSeriesLightUtils
					.read( inputPath.resolve( processArguments.getInputs().get( 2 ) ) );
			ArrayDouble.D2 flowCoordinate = (ArrayDouble.D2) resultReader
					.readVariable( MappingProperties.getProperty( MappingProperties.HDF5_FLOW_COORDINATE ) )
					.get();
			ArrayFloat.D2 flowArray = (ArrayFloat.D2) resultReader
					.readVariable( MappingProperties.getProperty( MappingProperties.HDF5_FLOW_VALUE ) )
					.get();

			SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
			IntStream.range( 0, waterLevelTimeSeriesArrays.size() ).forEach( station -> {
				try {
					Coordinate waterLevelPoint = JTSUtils.transformCRS( JTSUtils.coordinate(
							NumberUtils.create(
									waterLevelTimeSeriesArrays.get( station ).getHeader().getGeometry().getX( 0 ) ),
							NumberUtils.create(
									waterLevelTimeSeriesArrays.get( station ).getHeader().getGeometry().getY( 0 ) ) ),
							EPSG.WGS84, EPSG.TWD97 );

					TimeSeriesLightUtils.addHeader( handler,
							waterLevelTimeSeriesArrays.get( station ).getHeader().getLocationId(), "Q.simulated",
							"m3/s" );
					IntStream.range( 0, flowArray.getShape()[0] ).forEach( i -> {
						TimeSeriesLightUtils.addValue( handler, dateTime.plusHours( i ).getMillis(), new BigDecimal(
								flowArray.get( i, findClosestPoint( waterLevelPoint, flowCoordinate ) ) ) );
					} );
				} catch (FactoryException | TransformException e) {
					logger.log( LogLevel.ERROR, "HECRASPostAdapter: Transform coordinate has something wrong." );
				}
			} );

			TimeSeriesLightUtils.write( handler, outputPath.resolve( processArguments.getOutputs().get( 2 ) ) );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "HECRASPostAdapter: Reading NetCDF file has something wrong." );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "HECRASPostAdapter: Reading NetCDF file has something wrong." );
		}
	}

	/**
	 * Check the time format of model output.
	 * 
	 * @param time
	 * @return
	 */
	private String checkTime( String time ) {
		if ( time.substring( time.length() - 4, time.length() ).equals( "2400" ) ) {
			int day = Integer.valueOf( time.substring( 0, 2 ) );
			time = Strman.append( String.valueOf( day + 1 ), time.substring( 2, time.length() ) );
			time = time.replace( "2400", "0000" );
		}
		return time;
	}

	/**
	 * Find the closest point to the water level station.
	 * 
	 * @param waterLevelPoint
	 * @param flowCoordinate
	 * @return
	 */
	private int findClosestPoint( Coordinate waterLevelPoint, ArrayDouble.D2 flowCoordinate ) {
		double distance = 0;
		int pointIndex = 0;
		for ( int point = 0; point < flowCoordinate.getShape()[0]; point++ ) {
			if ( distance == 0 ) {
				distance = Math.sqrt( Math.pow( waterLevelPoint.getX() - flowCoordinate.get( point, 0 ), 2 )
						+ Math.pow( waterLevelPoint.getY() - flowCoordinate.get( point, 1 ), 2 ) );
			} else {
				if ( Math.sqrt( Math.pow( waterLevelPoint.getX() - flowCoordinate.get( point, 0 ), 2 )
						+ Math.pow( waterLevelPoint.getY() - flowCoordinate.get( point, 1 ), 2 ) ) < distance ) {
					distance = Math.sqrt( Math.pow( waterLevelPoint.getX() - flowCoordinate.get( point, 0 ), 2 )
							+ Math.pow( waterLevelPoint.getY() - flowCoordinate.get( point, 1 ), 2 ) );
					/** each cross section has 5 point data **/
					pointIndex = point / 5;
				}
			}
		}
		return pointIndex;
	}
}
