package tw.fondus.fews.adapter.pi.senslink.v2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.senslink.v2.authentication.AuthInfoResponse;
import tw.fondus.commons.json.senslink.v2.authentication.AuthenticationAction;
import tw.fondus.commons.json.senslink.v2.data.PQDataWrite;
import tw.fondus.commons.json.senslink.v2.query.WriteQueryParameter;
import tw.fondus.commons.json.senslink.v2.util.SensLinkUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v2.argument.RunArguments;
import tw.fondus.fews.adapter.pi.senslink.v2.util.AdapterUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model adapter for export data to SensLink 2.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class ExportToSensLinkAdapter extends PiCommandLineExecute {
	
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new ExportToSensLinkAdapter().execute(args, arguments);
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		Path inputXML = Prevalidated.checkExists( 
				Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0)),
				"SensLink 2.0 Export Adapter: The input XML not exists!" );
		
		try {
			logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data." );
			
			int index = modelArguments.getIndex();
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			List<PQDataWrite> datas = SensLinkUtils.toWriteDatas( timeSeriesArrays, 0, index );
			
			if ( datas.size() > 0 ){
				logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: export {} datas to the SensLink System.", String.valueOf( datas.size() ) );
				
				String username = modelArguments.getUsername();
				String password = modelArguments.getPassword();
				String host = AdapterUtils.getHost( modelArguments.getServer() );
				
				// Login SensLink 2.0
				Optional<AuthInfoResponse> optAuth = SensLinkUtils.login( host, username, password );
				OptionalUtils.ifPresentOrElse( optAuth, auth -> {
					logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: The SensLink 2.0 system login successfully, try to write data to the SensLink 2.0 system." );
					
					try {
						/** Write historical **/
						AuthenticationAction authHistorical = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.WRITE_HISTORICAL );
						WriteQueryParameter qpHistorical = WriteQueryParameter.of( authHistorical, datas );
						int writedHistorical = SensLinkUtils.writeHistorical( host, qpHistorical );
						logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: success write {} historical datas to the SensLink System.", String.valueOf( writedHistorical ));
						
						/** Write real-time **/
						AuthenticationAction authRealTime = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.WRITE_REALTIME );
						WriteQueryParameter qpRealTime = WriteQueryParameter.of( authRealTime, datas );
						int writedRealTime = SensLinkUtils.writeRealTime( host, qpRealTime );
						logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: success write {} real-time datas to the SensLink System.", String.valueOf( writedRealTime ));
						logger.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: Finished Adapter process.");
						
					} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException
							| UnsupportedEncodingException e) {
						logger.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: adapter has something wrong!");
					} catch (IOException e) {
						logger.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: write data has something wrong!");
					}
				},
				() -> logger.log( LogLevel.WARN, "SensLink 2.0 Export Adapter: SensLink System Login failed." )  );
				
			} else {
				logger.log( LogLevel.WARN, "SensLink 2.0 Export Adapter: PI-XML hasn't datas to export." );
			}
			
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: Read XML not exists or content empty!" );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: Read XML or write the time meta-information has something faild!" );
		}
	}
}
