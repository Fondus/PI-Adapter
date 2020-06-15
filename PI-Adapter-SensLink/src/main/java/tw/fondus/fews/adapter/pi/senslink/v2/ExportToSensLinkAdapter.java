package tw.fondus.fews.adapter.pi.senslink.v2;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.rest.senslink.v2.feign.SensLinkApiV2;
import tw.fondus.commons.rest.senslink.v2.feign.SensLinkApiV2Runtime;
import tw.fondus.commons.rest.senslink.v2.model.auth.AuthInfoRequest;
import tw.fondus.commons.rest.senslink.v2.model.auth.AuthInfoResult;
import tw.fondus.commons.rest.senslink.v2.model.record.WriteRecord;
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
 * Model adapter for export data to SensLink 2.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
@SuppressWarnings( "rawtypes" )
public class ExportToSensLinkAdapter extends PiCommandLineExecute {
	
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
				"SensLink 2.0 Export Adapter: The input XML not exists!" );
		
		try {
			logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity records." );
			
			int index = modelArguments.getIndex();
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.read( inputXML );
			List<WriteRecord> records = SensLinkApiV2Utils.toRecords( timeSeriesArrays, 0, index );
			
			if ( records.size() > 0 ){
				logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: export {} records to the SensLink System.", records.size() );
				
				String username = modelArguments.getUsername();
				String password = modelArguments.getPassword();

				// Login SensLink 2.0
				SensLinkApiV2 api = SensLinkApiV2Runtime.DEFAULT;
				AuthInfoResult result = api.login( AuthInfoRequest.builder()
						.username( username )
						.password( password )
						.build() );

				if ( result.getState().isSuccessful() ){
					logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: The SensLink 2.0 system login successfully, try to write records to the SensLink 2.0 system." );
					try {
						int wroteResult = api.writeBoth( result.getKey(), username, records );
						logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: success write {} record to the SensLink System.", wroteResult );
					} catch ( NoSuchAlgorithmException | InvalidKeyException e ){
						logger.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: Build the authentication has something wrong." );
					}
				} else {
					logger.log( LogLevel.WARN, "SensLink 2.0 Export Adapter: SensLink System Login failed." );
				}

			} else {
				logger.log( LogLevel.WARN, "SensLink 2.0 Export Adapter: PI-XML hasn't records to export." );
			}
			logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: Finished Adapter process.");
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: No time series found in file in the model input files!" );
		}
	}
}
