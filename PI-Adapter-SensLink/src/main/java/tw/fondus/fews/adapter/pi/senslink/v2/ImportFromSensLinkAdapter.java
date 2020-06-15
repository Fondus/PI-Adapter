package tw.fondus.fews.adapter.pi.senslink.v2;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import org.joda.time.DateTime;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.rest.senslink.v2.feign.SensLinkApiV2;
import tw.fondus.commons.rest.senslink.v2.feign.SensLinkApiV2Runtime;
import tw.fondus.commons.rest.senslink.v2.model.auth.AuthInfoRequest;
import tw.fondus.commons.rest.senslink.v2.model.auth.AuthInfoResult;
import tw.fondus.commons.rest.senslink.v2.model.record.PQTimeSeriesRecord;
import tw.fondus.commons.rest.senslink.v2.util.SensLinkApiV2QueryMethod;
import tw.fondus.commons.rest.senslink.v2.util.SensLinkApiV2QueryTimeZone;
import tw.fondus.commons.rest.senslink.v2.util.SensLinkApiV2Utils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v2.argument.RunArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Model adapter for import data from the SensLink 2.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ImportFromSensLinkAdapter extends PiCommandLineExecute {
	public static void main(String[] args) {
		RunArguments arguments = RunArguments.instance();
		new ImportFromSensLinkAdapter().execute(args, arguments);
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		RunArguments modelArguments = this.asArguments( arguments, RunArguments.class );
		
		Path inputXML = Prevalidated.checkExists(
				inputPath.resolve( modelArguments.getInputs().get(0) ),
				"SensLink 2.0 Import Adapter: The input XML not exists!" );
		
		try {
			// Read PI-XML
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			List<String> locationIds = TimeSeriesUtils.toLocationIds( timeSeriesArrays );
			
			logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Try to import {} data from the SensLink System.", locationIds.size() );
			
			DateTime timeZero = modelArguments.getTimeZero();
			String username = modelArguments.getUsername();
			String password = modelArguments.getPassword();
			
			// Login SensLink 2.0
			SensLinkApiV2 api = SensLinkApiV2Runtime.DEFAULT;
			AuthInfoResult result = api.login( AuthInfoRequest.builder()
					.username( username )
					.password( password )
					.build() );

			if ( result.getState().isSuccessful() ){
				logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: The SensLink 2.0 system login successfully, try to get records from the SensLink 2.0 system." );

				try {
					// Use API authentication to get data from SensLink by PQ Ids
					List<PQTimeSeriesRecord> records = api.readTimeSeriesByPQs( result.getKey(), username, locationIds,
							timeZero, modelArguments.getDuration(), SensLinkApiV2QueryTimeZone.UTC8,
							SensLinkApiV2QueryMethod.RAW );

					if ( records.size() > 0 ){
						logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Start translate SensLink PhysicalQuantity records to PI-XML." );
						TimeSeriesArrays outputArrays = SensLinkApiV2Utils.fromRecordTimeSeries( records, modelArguments.getParameter(), modelArguments.getUnit() );
						try {
							TimeSeriesUtils.write( outputArrays, outputPath.resolve( modelArguments.getOutputs().get( 0 ) ) );
						} catch ( IOException e ){
							logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: Write the PI-XML has something wrong." );
						}
					} else {
						logger.log( LogLevel.WARN, "SensLink 2.0 Import Adapter: Not receive any records from SensLink." );
					}

				} catch ( NoSuchAlgorithmException | InvalidKeyException e ) {
					logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: Build the authentication has something wrong." );
				}
			} else {
				logger.log( LogLevel.WARN, "SensLink 2.0 Import Adapter: SensLink System Login failed." );
			}

			logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Finished Adapter process." );
			
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: No time series found in file in the model input files!" );
		}
	}
}
