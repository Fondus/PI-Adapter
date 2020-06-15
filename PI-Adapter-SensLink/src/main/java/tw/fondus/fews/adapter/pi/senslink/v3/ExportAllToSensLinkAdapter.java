package tw.fondus.fews.adapter.pi.senslink.v3;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.rest.senslink.v3.model.record.Record;
import tw.fondus.commons.rest.senslink.v3.util.SensLinkApiV3Utils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.ExportArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Model adapter for export all data to SensLink 3.0 with Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ExportAllToSensLinkAdapter extends ParentExportToSensLinkAdapter {
	public static void main(String[] args) {
		ExportArguments arguments = ExportArguments.instance();
		new ExportAllToSensLinkAdapter().execute(args, arguments);
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		ExportArguments modelArguments = this.asArguments( arguments, ExportArguments.class );

		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"SensLink 3.0 Export All Adapter: The input XML not exists!" );

		try {
			logger.log( LogLevel.INFO, "SensLink 3.0 Export All Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data." );

			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			List<Record> records = SensLinkApiV3Utils.toRecords( timeSeriesArrays, 0, 0 );

			int timeSize = timeSeriesArrays.get( 0 ).size();
			IntStream.range( 1, timeSize ).forEach( i ->
				records.addAll( SensLinkApiV3Utils.toRecords( timeSeriesArrays, i, i ) )
			);

			this.exportRecords( modelArguments.getUsername(), modelArguments.getPassword(), records );
			logger.log( LogLevel.INFO, "SensLink 3.0 Export All Adapter: Finished Adapter process.");

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Export All Adapter: No time series found in file in the model input files!" );
		}
	}
}
