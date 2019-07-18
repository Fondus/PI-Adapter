package tw.fondus.fews.adapter.pi.senslink.v2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import org.joda.time.DateTime;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.senslink.v2.authentication.AuthInfoResponse;
import tw.fondus.commons.json.senslink.v2.authentication.AuthenticationAction;
import tw.fondus.commons.json.senslink.v2.station.PQHistoricalData;
import tw.fondus.commons.json.senslink.v2.util.SensLinkUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v2.argument.RunArguments;
import tw.fondus.fews.adapter.pi.senslink.v2.util.AdapterUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model adapter for import data from the SensLink 2.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class ImportFromSensLinkAdapter extends PiCommandLineExecute {
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new ImportFromSensLinkAdapter().execute(args, arguments);
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		String inputXMLPath = Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0));
		Path inputXML = Paths.get( inputXMLPath );
		Prevalidated.checkExists( inputXML, "SensLink 2.0 Import Adapter: The input XML not exists!" );
		
		try {
			// Read PI-XML
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			List<String> locationIds = TimeSeriesUtils.toLocationIds( timeSeriesArrays );
			
			logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Start import {} datas from the SensLink System.", String.valueOf( locationIds.size() ) );
			
			DateTime timeZero = modelArguments.getTimeZero();
			String username = modelArguments.getUsername();
			String password = modelArguments.getPassword();
			String host = AdapterUtils.getHost( modelArguments.getServer() );
			
			// Login SensLink 2.0
			Optional<AuthInfoResponse> optAuth = SensLinkUtils.login( host, username, password );
			OptionalUtils.ifPresentOrElse( optAuth, auth -> {
				try {
					// Use API authentication to get data from SensLink by PQ Ids 
					AuthenticationAction authentication = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.GET_HISTORICAL_BYIDS );
					Optional<List<PQHistoricalData>> optionalDatas = SensLinkUtils.getDataByPQIds( authentication,
							host,
							locationIds,
							timeZero,
							modelArguments.getDuration(),
							0 );
					
					OptionalUtils.ifPresentOrElse( optionalDatas, datas -> {
						logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Start translate SensLink PhysicalQuantity Data to PI-XML." );
						
						try {
							// Write the PI-XML
							SimpleTimeSeriesContentHandler contentHandler = SensLinkUtils.toTimeSeriesArraysIrregular( datas, modelArguments.getParameter(), modelArguments.getUnit() );
							TimeSeriesLightUtils.writePIFile( contentHandler,
									Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get( 0 ) ) );
						} catch (InterruptedException | IOException e) {
							logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: adapter write PI-XML has something wrong!.");
						}
						
						logger.log( LogLevel.INFO, "SensLink 2.0 Import Adapter: Finished Adapter process." );
					},
					() -> logger.log( LogLevel.WARN, "SensLink 2.0 Import Adapter: Nothing PhysicalQuantity Histroical Data of Station from the SensLink System.") );
					
				} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException
						| UnsupportedEncodingException e) {
					logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: adapter has something wrong!." );
				} 
			},
			() -> logger.log( LogLevel.WARN, "SensLink 2.0 Import Adapter: SensLink System Login failed." ) ); 
			
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: Read XML not exists or content empty!" );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 2.0 Import Adapter: Read XML or write the time meta-information has something faild!" );
		}
	}
}
