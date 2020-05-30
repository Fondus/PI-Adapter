package tw.fondus.fews.adapter.pi.kwgiuh.ntou;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.kwgiuh.ntou.argument.PreAdapterArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model pre-adapter for running NTOU KWGIUH model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class KWGIUHPreAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		PreAdapterArguments arguments = new PreAdapterArguments();
		new KWGIUHPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PreAdapterArguments preAdapterArguments = (PreAdapterArguments) arguments;

		try {
			logger.log( LogLevel.INFO, "KWGIUHPreAdapter: Starting KWGIUHPreAdapter process." );
			Path rainfallPath = Prevalidated.checkExists( inputPath.resolve( preAdapterArguments.getInputs().get( 0 ) ),
					"KWGIUHPreAdapter: Can not find input XML file of rainfall." );

			TimeSeriesArray rainfallSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( rainfallPath ).get( 0 );
			IntStream.range( 0, rainfallSeriesArray.size() ).forEach( data -> {
				/** Rainfall minus infiltration value **/
				float value = TimeSeriesLightUtils.getValue( rainfallSeriesArray, data )
						- preAdapterArguments.getInfiltration().floatValue();
				if ( value < 0 ) {
					rainfallSeriesArray.setFloatValues( data, 1, 0 );
				} else {
					rainfallSeriesArray.setFloatValues( data, 1, value );
				}
			} );

			logger.log( LogLevel.INFO, "KWGIUHPreAdapter: Building the model input from XML file of rainfall." );
			this.buildRainfallInput( rainfallSeriesArray, preAdapterArguments.getArea(),
					inputPath.resolve( preAdapterArguments.getInputs().get( 1 ) ) );

			logger.log( LogLevel.INFO, "KWGIUHPreAdapter: Building the model input arguments." );
			this.buildTempInput( inputPath,
					Strman.append( basePath.toString(), StringUtils.PATH, preAdapterArguments.getExecutableDir() ),
					preAdapterArguments.getGeomorphicFactor(),
					String.valueOf( rainfallSeriesArray.getHeader().getTimeStep().getStepMillis() / 60000 ),
					preAdapterArguments.getInputs().get( 1 ), preAdapterArguments.getOutputs().get( 0 ),
					preAdapterArguments.getNOverlandFlow(), preAdapterArguments.getNChannel(),
					preAdapterArguments.getWidth() );
		} catch (OperationNotSupportedException | IOException e) {
			logger.log( LogLevel.ERROR, "KWGIUHPreAdapter: Reading timeseries or writing file has something wrong." );
		}

		logger.log( LogLevel.INFO, "KWGIUHPreAdapter: End KWGIUHPreAdapter process." );
	}

	private void buildRainfallInput( TimeSeriesArray timeSeriesArray, BigDecimal area, Path rainfallInputPath )
			throws IOException {
		StringJoiner joiner = new StringJoiner( StringUtils.BREAKLINE );
		joiner.add( " ☆★☆ Effective rainfall & Direct runoff ☆★☆" );
		joiner.add( " " );
		joiner.add( " Dr. Lee, Kwan Tun" );
		joiner.add( " Fax:**-********" );
		joiner.add( " Email:ktlee@ntou.edu.tw" );
		joiner.add( " Watershed Hydrology and Hydraulic Laboratory" );
		joiner.add( " Department of River and Harbor Engineering" );
		joiner.add( " National Taiwan Ocean University" );
		joiner.add( " Keelung, Taiwan 202, R.O.C." );
		joiner.add( " " );
		joiner.add( " Rainfall station: -     " );
		joiner.add( Strman.append( " Interval of rainfall: ",
				TimeLightUtils.toString( new DateTime( timeSeriesArray.getStartTime() ), "yyyyMMddhh",
						TimeLightUtils.UTC8 ),
				StringUtils.HYPHEN, TimeLightUtils.toString( new DateTime( timeSeriesArray.getEndTime() ), "yyyyMMddhh",
						TimeLightUtils.UTC8 ) ) );
		joiner.add( " " );
		joiner.add( Strman.append( StringUtils.SPACE_WHITE, timeSeriesArray.getHeader().getLocationName(),
				"                         !Name of watershed" ) );
		joiner.add( Strman.append( "     ", area.setScale( 2, BigDecimal.ROUND_HALF_DOWN ).toString(),
				"                   !Area (km*km)" ) );
		joiner.add( Strman.append( "         ", String.valueOf( timeSeriesArray.size() ),
				"      0	          !Number of rainfall data, number of flow discharge" ) );
		joiner.add( "       3.00      0.00         !Phi index (mm/hr), Based flow (cms)" );
		joiner.add( " " );

		StringJoiner valueJoiner = new StringJoiner( StringUtils.TAB );
		for ( int data = 0; data < timeSeriesArray.size(); data++ ) {
			valueJoiner.add( String.valueOf( TimeSeriesLightUtils.getValue( timeSeriesArray, data, 0 ) ) );
		}
		joiner.add( valueJoiner.toString() );

		try (FileWriter fw = new FileWriter( rainfallInputPath.toFile() ) ) {
			fw.write( joiner.toString() );
		}
	}

	private void buildTempInput( Path inputPath, String workPath, String geomorphicFactor, String timeStep,
			String rainfallInputPath, String outputPath, BigDecimal nOverlandFlow, BigDecimal nChannel,
			BigDecimal width ) throws IOException {
		StringJoiner joiner = new StringJoiner( StringUtils.BREAKLINE );
		joiner.add( Strman.append( "\'", workPath, geomorphicFactor, "\'	!地文因子檔" ) );
		joiner.add( Strman.append( timeStep, "  !雨量、流量資料延時(min)" ) );
		joiner.add( Strman.append( nOverlandFlow.toString(), "  !no" ) );
		joiner.add( Strman.append( nChannel.toString(), " !nc" ) );
		joiner.add( Strman.append( width.toString(), "  !B" ) );
		joiner.add( Strman.append( "\'", workPath, rainfallInputPath, "\'		!雨量檔" ) );
		joiner.add( Strman.append( "\'", workPath, "\'" ) );
		joiner.add( "UH.txt		!單位歷線輸出檔案" );
		joiner.add( Strman.append( outputPath, "	!逕流模擬結果輸出檔案" ) );

		try (FileWriter fw = new FileWriter( inputPath.resolve( "temp.txt" ).toFile() ) ) {
			fw.write( joiner.toString() );
		}
	}
}
