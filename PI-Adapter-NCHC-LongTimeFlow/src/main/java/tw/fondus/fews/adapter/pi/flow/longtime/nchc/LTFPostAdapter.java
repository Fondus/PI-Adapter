package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;

/**
 * The model post-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPostAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );
	public static final long tenDaysMillis = (long) 10 * 24 * 60 * 60 * 1000;

	public static void main( String[] args ) {
		PiArguments arguments = new PiArguments();
		new LTFPostAdapter().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		PiArguments modelArguments = (PiArguments) arguments;
		
		/** Get XML file of flow for simulation end time **/
		Path flowPath = Paths
				.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 0 ) ) );
		Preconditions.checkState( Files.exists( flowPath ),
				"NCHC LTF PostAdapter: The XML file of flow is not exist." );
		TimeSeriesArray[] flowArrays = TimeSeriesUtils.readPiTimeSeries( flowPath.toFile() ).toArray();
		Date date = new Date( flowArrays[0].getEndTime() );

		/** Get model output file **/
		Path modelOutput = Paths.get(
				Strman.append( outputDir.getAbsolutePath(), StringUtils.PATH, "OUTPUT_EST_FLOW_ANN_GA-SA_MTF.TXT" ) );
		Preconditions.checkState( Files.exists( modelOutput ),
				"NCHC LTF PostAdapter: The file of model output is not exist." );
		List<String> dataList = PathUtils.readAllLines( modelOutput );

		/** Create model output of XML format **/
		SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
		TimeSeriesUtils.fillPiTimeSeriesHeader( handler, flowArrays[0].getHeader().getLocationId(),
				flowArrays[0].getHeader().getParameterId(), flowArrays[0].getHeader().getUnit() );
		
		IntStream.range( 0, dataList.size() ).forEach( tenDays -> {
			if ( dataList.get( tenDays ).split( StringUtils.SPACE_MULTIPLE ).length > 1 ) {
				long tenDaysLong = (long) (tenDays + 1) * tenDaysMillis;
				tenDaysLong += date.getTime();
				TimeSeriesUtils.addPiTimeSeriesValue( handler, tenDaysLong,
						Float.valueOf( dataList.get( tenDays ).split( StringUtils.SPACE_MULTIPLE )[3] ) );
			}
		} );

		TimeSeriesUtils.writePIFile( handler,
				Strman.append( outputDir.getAbsolutePath(), StringUtils.PATH, modelArguments.getOutputs().get( 0 ) ) );
	}

}
