package tw.fondus.fews.adapter.pi.commons;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.stream.IntStream;

/**
 * The common adapter tool it used to filter location with contain missing value of input PI-XML.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class FilterMissingLocationXmlAdapter extends PiCommandLineExecute {
	public static void main( String[] args ) {
		PiIOArguments arguments = PiIOArguments.instance();
		new FilterMissingLocationXmlAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		PiIOArguments modelArguments = this.asIOArguments( arguments );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"FilterMissingLocationXmlPreAdapter: The input XML file is not exist." );
		Path outputXML = outputPath.resolve( modelArguments.getOutputs().get( 0 ) );

		try {
			TimeSeriesArrays inputTimeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			SimpleTimeSeriesContentHandler handler = this.filterMissingLocation( inputTimeSeriesArrays );
			TimeSeriesLightUtils.write( handler, outputXML );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "FilterMissingLocationXmlPreAdapter: Adapter read XML has something wrong." );
		}
	}

	/**
	 * Filter drop the location with contain missing value.
	 *
	 * @param timeSeriesArrays pi-timeseries arrays
	 * @return handler of filtered pi-timeseries arrays
	 */
	private SimpleTimeSeriesContentHandler filterMissingLocation( TimeSeriesArrays timeSeriesArrays ){
		this.getLogger().log( LogLevel.INFO, "FilterMissingLocationXmlPreAdapter: Start to filter location with contain missing value." );
		SimpleTimeSeriesContentHandler handler = TimeSeriesLightUtils.seriesHandler();
		TimeSeriesLightUtils.forEach( timeSeriesArrays, timeSeriesArray -> {
			int size = timeSeriesArray.size();
			boolean containMissing = IntStream.range( 0, size )
					.anyMatch( timeSeriesArray::isMissingValue );
			if ( !containMissing ){
				TimeSeriesHeader header = timeSeriesArray.getHeader();
				TimeSeriesLightUtils.addHeader( handler, header );
				IntStream.range( 0, size )
						.forEach( i -> TimeSeriesLightUtils.addValue( handler, timeSeriesArray.getTime( i ),
								new BigDecimal( String.valueOf( timeSeriesArray.getValue( i ) ) ) ) );
			} else {
				this.getLogger().log( LogLevel.INFO, "FilterMissingLocationXmlPreAdapter: Location: {} contain missing value, will be filter drop.",
						timeSeriesArray.getHeader().getLocationId() );
			}
		} );
		this.getLogger().log( LogLevel.INFO, "FilterMissingLocationXmlPreAdapter: End to filter location with contain missing value." );
		return handler;
	}
}
