package tw.fondus.fews.adapter.pi.senslink.v3;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.senslink.v3.entity.data.RawData;
import tw.fondus.commons.json.senslink.v3.util.SensLinkUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.senslink.v3.util.RunArguments;

/**
 * Model adapter for export data to SensLink 3.0 with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class ExportToSensLinkAdapter extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		RunArguments arguments = new RunArguments();
		new ExportToSensLinkAdapter().execute(args, arguments);
	}
	
	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir, File outputDir)
			throws Exception {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		try {
			Path xmlPath = Paths.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get(0)) );
			if ( !PathUtils.exists( xmlPath ) ){
				throw new FileNotFoundException();
			}
			
			log.info("SensLink 3.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data.");
			this.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data.");
			
			int index = modelArguments.getIndex();
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.readPiTimeSeries( xmlPath.toFile() );
			List<RawData> datas = SensLinkUtils.toWriteDatas(  timeSeriesArrays, 0, index );
			
			if ( datas.size() > 0 ){
				log.info("SensLink 3.0 Export Adapter: export {} datas to the SensLink System.", datas.size());
				this.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: export {} datas to the SensLink System.", String.valueOf( datas.size() ));
				
				String username = modelArguments.getUsername();
				String password = modelArguments.getPassword();
				String host = SensLinkUtils.URL_WRA;
				
				Optional<String> optToken = SensLinkUtils.getAccessToken( 
						username,
						password,
						Strman.append( host, SensLinkUtils.URL_OAUTH_TOKEN ));
				
				OptionalUtils.ifPresentOrElse( optToken, token -> {
					log.info("SensLink 3.0 Export Adapter: The SensLink 3.0 system login successfully, try to write data to the SensLink 3.0 system.");
					this.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: The SensLink 3.0 system login successfully, try to write data to the SensLink 3.0 system.");
					
					boolean writed = SensLinkUtils.writeFormulaTransferred( host, token, datas.toArray( new RawData[0] ) );
					if ( writed ){
						log.info("SensLink 3.0 Export Adapter: success to write {} datas to the SensLink System.", datas.size() );
						this.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: success to write {} datas to the SensLink System.", String.valueOf( datas.size() ));
					} else {
						log.warn("SensLink 3.0 Export Adapter: faild to write datas to the SensLink System." );
						this.log( LogLevel.WARN, "SensLink 3.0 Export Adapter: faild to write datas to the SensLink System.");
					}
					
					log.info("SensLink 3.0 Export Adapter: Finished Adapter process.");
					this.log( LogLevel.INFO, "SensLink 3.0 Export Adapter: Finished Adapter process.");
					
				}, () -> { 
					log.warn( "SensLink 3.0 Export Adapter: SensLink System Login failed." );
					this.log( LogLevel.WARN, "SensLink 3.0 Export Adapter: SensLink System Login failed.");
				});
				
			} else {
				log.info("SensLink 3.0 Export Adapter: PI-XML hasn't datas to export.");
				this.log( LogLevel.WARN, "SensLink 3.0 Export Adapter: PI-XML hasn't datas to export.");
			}
			
		} catch (FileNotFoundException e) {
			log.error("SensLink 3.0 Export Adapter: Input XML not exits!", e);
			this.log( LogLevel.ERROR, "SensLink 3.0 Export Adapter: Input XML not exits!");
		}
	}
}
