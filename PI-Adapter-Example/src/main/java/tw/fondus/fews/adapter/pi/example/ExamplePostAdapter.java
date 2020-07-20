package tw.fondus.fews.adapter.pi.example;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The Model post-adapter for running example model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class ExamplePostAdapter extends PiCommandLineExecute {
	private static final String TIME_FORMAT = "yyyyMMddHHmm";
	
	public static void main(String[] args) {
		PiIOArguments arguments = PiIOArguments.instance();
		new ExamplePostAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments modelArguments = this.asIOArguments( arguments );
		
		try {
			Path modelOutputPath = Prevalidated.checkExists( 
					outputPath.resolve( modelArguments.getInputs().get( 0 ) ),
					"Example PostAdapter: The model output do not exists!" );
			
			logger.log( LogLevel.INFO, "Example PostAdapter: Start read model output files to PiXML.");
			
			// The handler is used to create XML content.
			SimpleTimeSeriesContentHandler contentHandler = TimeSeriesLightUtils.seriesHandler();
			this.parseModelOutput( modelArguments, contentHandler, modelOutputPath );
			
			// Write the handler to PiXML
			TimeSeriesLightUtils.write( contentHandler,
					outputPath.resolve( modelArguments.getOutputs().get( 0 ) ) );
			
			logger.log( LogLevel.INFO, "Example PostAdapter: Finished read model output files to PiXML.");
			
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "Example PostAdapter: Read model output files has something wrong!");
		}
	}
	
	/**
	 * Read model output content -> SimpleTimeSeriesContentHandler logic.
	 * 
	 * @param arguments arguments
	 * @param contentHandler content handler
	 * @param modelOutputPath model output path
	 * @throws IOException has IO Exception
	 */
	private void parseModelOutput( PiIOArguments arguments, SimpleTimeSeriesContentHandler contentHandler,
			Path modelOutputPath ) throws IOException {
		String locationId = FileUtils.getNameWithoutExt( modelOutputPath.toFile() ).split( "-" )[0];

		// Fill the header
		TimeSeriesLightUtils.addHeader( contentHandler, locationId,
				arguments.getParameter(), arguments.getUnit() );
		
		// Read the model output content
		List<String> fileLines = Files.readAllLines( modelOutputPath, StandardCharsets.UTF_8 );
		fileLines.stream()
			.map( lines -> lines.split( "," ) )
			.forEach( data ->
				TimeSeriesLightUtils.addValue( contentHandler,
						TimeLightUtils.toDateTime( data[0], TIME_FORMAT, TimeLightUtils.UTC0 ).getMillis(),
						new BigDecimal( data[1] ) )
			);
	}
}
