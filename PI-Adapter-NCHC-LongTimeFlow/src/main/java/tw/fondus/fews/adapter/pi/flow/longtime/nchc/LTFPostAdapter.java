package tw.fondus.fews.adapter.pi.flow.longtime.nchc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The model post-adapter for running NCHC long time flow model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class LTFPostAdapter extends PiCommandLineExecute {
	public static final long tenDaysMillis = (long) 10 * 24 * 60 * 60 * 1000;

	public static void main( String[] args ) {
		PiIOArguments arguments = new PiIOArguments();
		new LTFPostAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PiIOArguments modelArguments = (PiIOArguments) arguments;
		
		String inputXMLPath = Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0));
		Path inputXML = Paths.get( inputXMLPath );
		Prevalidated.checkExists( inputXML, "NCHC NCHC LTF PostAdapter: The input XML not exists!" );
		
		/** Get model output file **/
		Path modelOutput = Paths.get( Strman.append( outputPath.toString(), PATH, "OUTPUT_EST_FLOW_ANN_GA-SA_MTF.TXT" ) );
		Prevalidated.checkExists( inputXML, "NCHC LTF PostAdapter: The file of model output is not exist." );
		
		try {
			TimeSeriesArray timeSeriesArray = TimeSeriesLightUtils.readPiTimeSeries( inputXML ).get( 0 );
			DateTime dateTime = new DateTime( timeSeriesArray.getEndTime() );
			
			// Read the output content
			List<String> dataList = PathUtils.readAllLines( modelOutput );
		
			// Create the model PI-XML
			logger.log( LogLevel.INFO, "NCHC NCHC LTF PostAdapter: Start read model output files to PiXML." );
			
			SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
			TimeSeriesLightUtils.fillPiTimeSeriesHeader( handler, timeSeriesArray.getHeader().getLocationId(),
					timeSeriesArray.getHeader().getParameterId(), timeSeriesArray.getHeader().getUnit() );
			IntStream.range( 0, dataList.size() ).forEach( tenDays -> {
				if ( dataList.get( tenDays ).split( StringUtils.SPACE_MULTIPLE ).length > 1 ) {
					long tenDaysLong = (long) (tenDays + 1) * tenDaysMillis;
					tenDaysLong += dateTime.getMillis();
					TimeSeriesLightUtils.addPiTimeSeriesValue( handler, tenDaysLong,
							Float.valueOf( dataList.get( tenDays ).split( StringUtils.SPACE_MULTIPLE )[3] ) );
				}
			});
			
			TimeSeriesLightUtils.writePIFile( handler,
					Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get( 0 ) ) );
			
			logger.log( LogLevel.INFO, "NCHC NCHC LTF PostAdapter: Finished read model output files to PiXML." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "NCHC NCHC LTF PostAdapter: Read model output has something wrong." );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "NCHC NCHC LTF PostAdapter: Write PI-XML has something wrong." );
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "NCHC NCHC LTF PostAdapter: Read PI-XML has something wrong." );
		}
	}
}
