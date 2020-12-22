package tw.fondus.fews.adapter.pi.senslink.v3;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.rest.senslink.v3.model.record.Record;
import tw.fondus.commons.rest.senslink.v3.util.SensLinkApiV3Utils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Model adapter for export data to SensLink 3.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ExportToSensLinkAdapter extends ParentExportToSensLinkAdapter {
	
	public static void main(String[] args) {
		RunArguments arguments = RunArguments.instance();
		new ExportToSensLinkAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		RunArguments modelArguments = this.asArguments( arguments, RunArguments.class );
		
		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get( 0 ) ),
				"SensLink 3.0 Export Adapter: The input XML not exists!" );

		try {
			logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data." );

			int start = modelArguments.getStart();
			int index = modelArguments.getIndex();
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			List<Record> records = SensLinkApiV3Utils.toRecords( timeSeriesArrays, start, index );

			this.exportRecords( modelArguments, modelArguments.getUsername(), modelArguments.getPassword(), records );
			logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: Finished Adapter process.");

		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Export Adapter: No time series found in file in the model input files!" );
		}
	}
}
