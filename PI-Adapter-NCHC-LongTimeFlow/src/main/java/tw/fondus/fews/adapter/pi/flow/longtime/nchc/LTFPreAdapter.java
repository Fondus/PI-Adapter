package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.string.StringUtils;

/**
 * The model pre-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPreAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );

	public static void main( String[] args ) {
		PiArguments arguments = new PiArguments();
		new LTFPreAdapter().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		PiArguments modelArguments = (PiArguments) arguments;
		
		/** Check input file and data is ten days or not **/
		Path rainfallPath = Paths
				.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 0 ) ) );
		Preconditions.checkState( Files.exists( rainfallPath ),
				"NCHC LTF PreAdapter: The XML file of rainfall is not exist." );

		TimeSeriesArray[] rainfallArrays = TimeSeriesUtils.readPiTimeSeries( rainfallPath.toFile() ).toArray();
		Preconditions.checkState( rainfallArrays[0].size() % 10 == 0,
				"NCHC LTF PreAdapter: The rainfall data are not divisible by 10 days." );

		Path flowPath = Paths
				.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 1 ) ) );
		Preconditions.checkState( Files.exists( flowPath ), "NCHC LTF PreAdapter: The XML file of flow is not exist." );

		TimeSeriesArray[] flowArrays = TimeSeriesUtils.readPiTimeSeries( flowPath.toFile() ).toArray();
		Preconditions.checkState( flowArrays[0].size() % 10 == 0,
				"NCHC LTF PreAdapter: The flow data are not divisible by 10 days." );

		this.writeModelInput( inputDir, rainfallArrays, flowArrays );
	}

	/**
	 * Write model input file.
	 * 
	 * @param inputDir
	 * @param rainfallArrays
	 * @param flowArrays
	 */
	private void writeModelInput( File inputDir, TimeSeriesArray[] rainfallArrays, TimeSeriesArray[] flowArrays ) {
		List<Float> tenDaysRainfall = new ArrayList<>();
		List<Float> tenDaysFlow = new ArrayList<>();

		IntStream.range( 0, rainfallArrays[0].size() / 10 ).forEach( tenDays -> {
			float rainfall = 0;
			float flow = 0;
			for ( int data = 0; data < 10; data++ ) {
				if ( Float.isNaN( rainfallArrays[0].getValue( (tenDays * 10) + data ) ) ) {
					rainfall += 0;
				} else {
					rainfall += rainfallArrays[0].getValue( (tenDays * 10) + data );
				}
				if ( Float.isNaN( flowArrays[0].getValue( (tenDays * 10) + data ) ) ) {
					flow += 0;
				} else {
					flow += flowArrays[0].getValue( (tenDays * 10) + data );
				}
			}
			tenDaysRainfall.add( rainfall );
			tenDaysFlow.add( flow );
		} );

		String rainfall = StringUtils.BLANK;
		String flow = StringUtils.BLANK;
		String endLine = StringUtils.BLANK;
		for ( int tenDaysData = 0; tenDaysData < tenDaysRainfall.size(); tenDaysData++ ) {
			rainfall += Strman.append( String.valueOf( tenDaysRainfall.get( tenDaysData ) ), StringUtils.TAB );
			flow += Strman.append( String.valueOf( tenDaysFlow.get( tenDaysData ) ), StringUtils.TAB );
			endLine += Strman.append( "-999", StringUtils.TAB );
		}

		try {
			StringJoiner sj = new StringJoiner( StringUtils.BREAKLINE );
			sj.add( rainfall );
			sj.add( StringUtils.BLANK );
			sj.add( endLine );
			FileUtils.writeText( Strman.append( inputDir.getAbsolutePath(), StringUtils.PATH, "DATA_INP_RAIN.TXT" ),
					sj.toString() );

			sj = new StringJoiner( StringUtils.BREAKLINE );
			sj.add( flow );
			sj.add( StringUtils.BLANK );
			sj.add( endLine );
			FileUtils.writeText( Strman.append( inputDir.getAbsolutePath(), StringUtils.PATH, "DATA_INP_FLOW.TXT" ),
					sj.toString() );
		} catch (IOException e) {
			log.error( "NCHC LTF PreAdapter: Writing model input file has something wrong." );
			this.log( LogLevel.ERROR, "NCHC LTF PreAdapter: Writing model input file has something wrong." );
		}
	}
}
