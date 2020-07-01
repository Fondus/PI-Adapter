package tw.fondus.fews.adapter.pi.annfsm.ntou;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.annfsm.ntou.argument.PreAdapterArguments;
import tw.fondus.fews.adapter.pi.annfsm.ntou.entity.Point;
import tw.fondus.fews.adapter.pi.annfsm.ntou.util.DataUtils;
import tw.fondus.fews.adapter.pi.annfsm.ntou.util.HTTPUtils;
import tw.fondus.fews.adapter.pi.annfsm.ntou.util.OpenDataProperties;
import tw.fondus.fews.adapter.pi.annfsm.ntou.util.ParsingOpenTyphoonTrack;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model pre-adapter for running NTOU ANNFSM model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
@SuppressWarnings( "rawtypes" )
public class ANNFSMPreAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		PreAdapterArguments arguments = PreAdapterArguments.instance();
		new ANNFSMPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "ANNFSMPreAdapter: Starting ANNFSMPreAdapter process." );
		PreAdapterArguments preAdapterArguments = (PreAdapterArguments) arguments;

		Optional<String> optToken = OpenDataProperties.getProperty( OpenDataProperties.TOKEN );
		Optional<String> optId = OpenDataProperties.getProperty( OpenDataProperties.SOURCE_ID );
		OptionalUtils.ifPresentOrElse( optToken, optId, ( token, dataId ) -> {
			logger.log( LogLevel.INFO, "ANNFSMPreAdapter: Downloading CWB typhoon track data from opendata." );
			Path typhoonTrackPath = Prevalidated.checkExists(
					HTTPUtils.getFile( dataId, token,
							inputPath.resolve( Strman.append( dataId, FileType.JSON.getExtension() ) )
									.toAbsolutePath()
									.toString() ),
					"ANNFSMPreAdapter: Can not find the download file of typhoon track from CWB opendata." );

			try {
				logger.log( LogLevel.INFO, "ANNFSMPreAdapter: Parsing CWB typhoon track data from opendata." );
				String jsonString = new String( Files.readAllBytes( typhoonTrackPath ) );
				List<Point> points = DataUtils.interpolationPoints( ParsingOpenTyphoonTrack.parsing( jsonString ) );

				Path tidePath = Prevalidated.checkExists( inputPath.resolve( preAdapterArguments.getInputs().get( 0 ) ),
						"ANNFSMPreAdapter: Can not find input XML file of tide." );
				TimeSeriesArray tideSeriesArray = TimeSeriesLightUtils.read( tidePath ).get( 0 );

				/**
				 * Make sure typhoon data can be calculated(speed and angle)
				 **/
				if ( points.stream()
						.filter( point -> point.getTime().getMillis() == tideSeriesArray.getEndTime() )
						.count() > 0 && points.get( 0 ).getTime().getMillis() < tideSeriesArray.getEndTime() ) {
					logger.log( LogLevel.INFO, "ANNFSMPreAdapter: Writing input data for model." );
					this.writeModelInput( points, tideSeriesArray, preAdapterArguments.getCoordinate(), inputPath,
							preAdapterArguments.getOutputs() );
				} else {
					logger.log( LogLevel.ERROR, "ANNFSMPreAdapter: The typhoon track data and tide data mismatched." );
				}
			} catch (IOException e) {
				logger.log( LogLevel.ERROR,
						"ANNFSMPreAdapter: Reading typhoon track file from CWB opendata has something wrong." );
			}
		}, () -> {
			logger.log( LogLevel.ERROR, "ANNFSMPreAdapter: There are empty data of CWB opendata token or id." );
		} );
		
		logger.log( LogLevel.INFO, "ANNFSMPreAdapter: Finished ANNFSMPreAdapter process." );
	}

	/**
	 * Write model input data.
	 * 
	 * @param points
	 * @param tideSeriesArray
	 * @param coordinate
	 * @param inputPath
	 * @param outputs
	 */
	private void writeModelInput( List<Point> points, TimeSeriesArray tideSeriesArray, List<String> coordinate,
			Path inputPath, List<String> outputs ) {
		IntStream.range( 0, points.size() ).forEach( point -> {
			if ( points.get( point ).getTime().getMillis() == tideSeriesArray.getEndTime() ) {
				Point start = points.get( point - 1 );
				Point end = points.get( point );
				/** Degree to kilometer **/
				BigDecimal distanceFactor = new BigDecimal( "111" );
				BigDecimal typhoonSpeed = DataUtils.calculatedDistance( start, end ).multiply( distanceFactor );
				BigDecimal typhoonAngle = DataUtils.calculatedAngle( start, end );

				BigDecimal stationLat = new BigDecimal( coordinate.get( 0 ) );
				BigDecimal stationLon = new BigDecimal( coordinate.get( 1 ) );
				BigDecimal distance = DataUtils.calculatedDistance( end.getLatitude(), end.getLongitude(), stationLat,
						stationLon );
				BigDecimal angle = DataUtils.calculatedAngle( end.getLatitude(), end.getLongitude(), stationLat,
						stationLon );

				StringJoiner joiner = new StringJoiner( Strings.TAB );
				joiner.add( String.valueOf( TimeSeriesLightUtils.getValue( tideSeriesArray, 0 ) ) );
				joiner.add( end.getCentralPressure().toString() );
				joiner.add( end.getGust().toString() );
				joiner.add( end.getWindDistribution().getRadius().toString() );
				joiner.add( typhoonSpeed.toString() );
				joiner.add( typhoonAngle.toString() );
				joiner.add( distance.toString() );
				joiner.add( angle.toString() );
				
				PathWriter.write( inputPath.resolve( outputs.get( 0 ) ), joiner.toString() );
				PathWriter.write( inputPath.resolve( outputs.get( 1 ) ), String.valueOf( tideSeriesArray.size() ) );
			}
		} );
	}
}
