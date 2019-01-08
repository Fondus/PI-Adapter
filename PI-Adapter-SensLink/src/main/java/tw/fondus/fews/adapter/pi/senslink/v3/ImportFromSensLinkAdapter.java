package tw.fondus.fews.adapter.pi.senslink.v3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import tw.fondus.commons.json.senslink.v3.entity.data.TimeSeries;
import tw.fondus.commons.json.senslink.v3.util.SensLinkUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.senslink.v3.util.RunArguments;

/**
 * Model adapter for import data from the SensLink 3.0 with Delft-FEWS.
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
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		try {
			Path xmlPath = Paths.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get(0)) );
			if ( !PathUtils.exists( xmlPath ) ){
				throw new FileNotFoundException();
			}
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.readPiTimeSeries( xmlPath.toFile() );
			List<String> locationIds = TimeSeriesUtils.toLocationIds( timeSeriesArrays );
			
			log.info("SensLink 3.0 Import Adapter: Start import {} datas from the SensLink System.", locationIds.size());
			this.log( LogLevel.INFO, "SensLink 3.0 Import Adapter: Start import {} datas from the SensLink System.", String.valueOf( locationIds.size() ) );
			
			DateTime timeZero = modelArguments.getTimeZero();
			String username = modelArguments.getUsername();
			String password = modelArguments.getPassword();
			String host = SensLinkUtils.URL_WRA;
			
			Optional<String> optToken = SensLinkUtils.getAccessToken( 
					username,
					password,
					Strman.append( host, SensLinkUtils.URL_OAUTH_TOKEN ));
			
			OptionalUtils.ifPresentOrElse( optToken, token -> {
				log.info("SensLink 3.0 Import Adapter: The SensLink 3.0 system login successfully, try to get data from the SensLink 3.0 system.");
				this.log( LogLevel.INFO, "SensLink 3.0 Import Adapter: The SensLink 3.0 system login successfully, try to get data from the SensLink 3.0 system.");
				
				DateTime start = timeZero.minusDays( modelArguments.getDuration() );
				String startString = TimeUtils.toString( start, TimeUtils.YMDTHMS_DOT, TimeUtils.UTC0 );
				String endString = TimeUtils.toString( timeZero, TimeUtils.YMDTHMS_DOT, TimeUtils.UTC0 );
				
				List<TimeSeries> datas = new ArrayList<>();
				locationIds.forEach( locationId -> {
					SensLinkUtils.readTimeSeries( host, token, locationId, startString, endString, true ).ifPresent( series -> {
						datas.add( series );
					} );
				} );
				
				if ( datas.size() > 0 ){
					log.info("SensLink 3.0 Import Adapter: Start translate SensLink PhysicalQuantity Data to PI-XML.");
					this.log( LogLevel.INFO, "SensLink 3.0 Import Adapter: Start translate SensLink PhysicalQuantity Data to PI-XML.");
					
					SimpleTimeSeriesContentHandler contentHandler = SensLinkUtils.toTimeSeriesArraysIrregular( datas,
							modelArguments.getParameter(), modelArguments.getUnit() );
					try {
						TimeSeriesUtils.writePIFile( contentHandler,
								Strman.append( outputDir.getPath(), StringUtils.PATH, modelArguments.getOutputs().get( 0 ) ) );
					} catch (InterruptedException | IOException e) {
						log.error( "SensLink 3.0 Import Adapter: adapter write PI-XML has something wrong!.", e );
						this.log( LogLevel.ERROR, "SensLink 3.0 Import Adapter: adapter write PI-XML has something wrong!.");
					}
					
				} else {
					log.warn( "SensLink 3.0 Import Adapter: Nothing datas from the SensLink System." );
					this.log( LogLevel.WARN, "SensLink 3.0 Import Adapter: Nothing datas from the  SensLink System.");
				}
				
			}, () -> { 
				log.warn( "SensLink 3.0 Import Adapter: SensLink System Login failed." );
				this.log( LogLevel.WARN, "SensLink 3.0 Import Adapter: SensLink System Login failed.");
			});
			
		} catch (FileNotFoundException e) {
			log.error("SensLink 3.0 Import Adapter: Input XML not exits!", e);
			this.log( LogLevel.ERROR, "SensLink 3.0 Import Adapter: Input XML not exits!");
		}
	}
}
