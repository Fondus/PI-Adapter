package tw.fondus.fews.adapter.pi.senslink.v3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.senslink.v3.entity.data.RawData;
import tw.fondus.commons.json.senslink.v3.util.SensLinkUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.RunArguments;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model adapter for export data to SensLink 3.0 with Delft-FEWS.
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
				"SensLink 3.0 Export Adapter: The input XML not exists!" );
		
		try {
			logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data." );

			int start = modelArguments.getStart();
			int index = modelArguments.getIndex();
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			List<RawData> datas = SensLinkUtils.toWriteDatas(  timeSeriesArrays, start, index );
			
			if ( datas.size() > 0 ){
				logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: export {} datas to the SensLink System.", String.valueOf( datas.size() ));
				
				String username = modelArguments.getUsername();
				String password = modelArguments.getPassword();
				String host = SensLinkUtils.URL_WRA;
				
				// Login SensLink 3.0 by OAuth 2.0
				Optional<String> optToken = SensLinkUtils.getAccessToken( 
						username,
						password,
						Strman.append( host, SensLinkUtils.URL_OAUTH_TOKEN ));
				
				OptionalUtils.ifPresentOrElse( optToken, token -> {
					logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: The SensLink 3.0 system login successfully, try to write data to the SensLink 3.0 system.");
					
					/** Write SensLink 3.0 **/
					boolean writed = SensLinkUtils.writeFormulaTransferred( host, token, datas.toArray( new RawData[0] ) );
					if ( writed ){
						logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: success to write {} datas to the SensLink System.", String.valueOf( datas.size() ));
					} else {
						logger.log( LogLevel.WARN, "SensLink 3.0 Export Adapter: faild to write datas to the SensLink System.");
					}
					logger.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: Finished Adapter process.");
				},
				() -> logger.log( LogLevel.WARN, "SensLink 3.0 Export Adapter: SensLink System Login failed.") );
				
			} else {
				logger.log( LogLevel.WARN, "SensLink 3.0 Export Adapter: PI-XML hasn't datas to export.");
			}
			
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Export Adapter: Read XML not exists or content empty!" );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "SensLink 3.0 Export Adapter: Read XML or write the time meta-information has something faild!" );
		}
	}
}
