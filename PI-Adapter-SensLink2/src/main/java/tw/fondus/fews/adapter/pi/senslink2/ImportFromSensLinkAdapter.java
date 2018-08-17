package tw.fondus.fews.adapter.pi.senslink2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.senslink2.authentication.AuthInfoResponse;
import tw.fondus.commons.json.senslink2.authentication.AuthenticationAction;
import tw.fondus.commons.json.senslink2.station.PQHistoricalData;
import tw.fondus.commons.json.senslink2.util.SensLinkUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.senslink2.util.RunArguments;

/**
 * Model adapter for import data from the SensLink 2.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class ImportFromSensLinkAdapter extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new ImportFromSensLinkAdapter().execute(args, arguments);
	}
	
	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File logDir, File logFile, File inputDir, File outputDir)
			throws Exception {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		try {
			Path xmlPath = Paths.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get(0)) );
			if ( !PathUtils.exists( xmlPath ) ){
				throw new FileNotFoundException();
			}
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.readPiTimeSeries( xmlPath.toFile() );
			List<String> locationIds = TimeSeriesUtils.toLocationIds( timeSeriesArrays );
			
			log.info("SensLink 2.0 Import Adapter: Start import {} datas from the SensLink System.", locationIds.size());
			piDiagnostics.addMessage( LogLevel.INFO.value(), Strman.append( "SensLink 2.0 Import Adapter: import ", String.valueOf( locationIds.size() ), " datas from the SensLink System." ));
			
			DateTime timeZero = modelArguments.getTimeZero();
			String username = modelArguments.getUsername();
			String password = modelArguments.getPassword();
			
			Optional<AuthInfoResponse> optAuth = SensLinkUtils.login( username, password );
			OptionalUtils.ifPresentOrElse( optAuth, auth -> {
				
				try {
					AuthenticationAction authentication = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.ACTION_GET_HISTORICAL_BYIDS );
					
					Optional<List<PQHistoricalData>> optionalDatas = SensLinkUtils.getDataByPhysicalQuantityIds( authentication,
							locationIds.toArray( new String[0] ),
							timeZero,
							modelArguments.getDuration(),
							0 );
					
					OptionalUtils.ifPresentOrElse( optionalDatas, datas -> {
						
						log.info("SensLink 2.0 Import Adapter: Start translate SensLink PhysicalQuantity Data to PI-XML.");
						piDiagnostics.addMessage( LogLevel.INFO.value(), "SensLink 2.0 Import Adapter: Start translate SensLink PhysicalQuantity Data to PI-XML.");
						
						SimpleTimeSeriesContentHandler contentHandler = SensLinkUtils.toTimeSeriesArraysIrregular( datas, modelArguments.getParameter(), modelArguments.getUnit() );
						try {
							TimeSeriesUtils.writePIFile( contentHandler,
									Strman.append( outputDir.getPath(), StringUtils.PATH, modelArguments.getOutputs().get( 0 ) ) );
						} catch (InterruptedException | IOException e) {
							log.error( "SensLink 2.0 Import Adapter: adapter write PI-XML has something wrong!.", e );
							piDiagnostics.addMessage( LogLevel.ERROR.value(), "SensLink 2.0 Import Adapter: adapter write PI-XML has something wrong!." );
						}
						
						log.info("SensLink 2.0 Import Adapter: Finished Adapter process.");
						piDiagnostics.addMessage( LogLevel.INFO.value(), "SensLink 2.0 Import Adapter: Finished Adapter process.");
						
					}, () -> {
						log.warn("SensLink 2.0 Import Adapter: Nothing PhysicalQuantity Histroical Data of Station from the SensLink System.");
						piDiagnostics.addMessage( LogLevel.WARN.value(), "SensLink 2.0 Import Adapter: Nothing PhysicalQuantity Histroical Data of Station from the SensLink System." );
					} );
					
				} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException
						| UnsupportedEncodingException e) {
					log.error( "SensLink 2.0 Import Adapter: adapter has something wrong!.", e );
					piDiagnostics.addMessage( LogLevel.ERROR.value(), "SensLink 2.0 Import Adapter: adapter has something wrong!." );
				} 
				
			}, () -> { 
				log.warn( "SensLink 2.0 Import Adapter: SensLink System Login failed." );
				piDiagnostics.addMessage( LogLevel.WARN.value(), "SensLink 2.0 Import Adapter: SensLink System Login failed." );
			} );
			
		} catch (FileNotFoundException e) {
			log.error("SensLink 2.0 Import Adapter: Input XML not exits!", e);
			piDiagnostics.addMessage( LogLevel.ERROR.value(), "SensLink 2.0 Import Adapter: Input XML not exits!");
		}
	}
}
