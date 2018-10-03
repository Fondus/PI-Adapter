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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.senslink2.authentication.AuthInfoResponse;
import tw.fondus.commons.json.senslink2.authentication.AuthenticationAction;
import tw.fondus.commons.json.senslink2.data.PQDataWrite;
import tw.fondus.commons.json.senslink2.query.WriteQueryParameter;
import tw.fondus.commons.json.senslink2.util.SensLinkUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.optional.OptionalUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.senslink2.util.AdapterUtils;
import tw.fondus.fews.adapter.pi.senslink2.util.RunArguments;

/**
 * Model adapter for export data to SensLink 2.0 with Delft-FEWS.
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
			
			log.info("SensLink 2.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data.");
			this.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data.");
			
			int index = modelArguments.getIndex();
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.readPiTimeSeries( xmlPath.toFile() );
			List<PQDataWrite> datas = SensLinkUtils.toWriteDatas( timeSeriesArrays, 0, index );
			
			if ( datas.size() > 0 ){
				log.info("SensLink 2.0 Export Adapter: export {} datas to the SensLink System.", datas.size());
				this.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: export {} datas to the SensLink System.", String.valueOf( datas.size() ));
				
				String username = modelArguments.getUsername();
				String password = modelArguments.getPassword();
				String host = AdapterUtils.getHost( modelArguments.getServer() );
				
				Optional<AuthInfoResponse> optAuth = SensLinkUtils.login( host, username, password );
				OptionalUtils.ifPresentOrElse( optAuth, auth -> {
					log.info("SensLink 2.0 Export Adapter: The SensLink 2.0 system login successfully, try to write data to the SensLink 2.0 system.");
					this.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: The SensLink 2.0 system login successfully, try to write data to the SensLink 2.0 system.");
					
					try {
						/** Historical **/
						AuthenticationAction authHistorical = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.WRITE_HISTORICAL );
						WriteQueryParameter qpHistorical = new WriteQueryParameter();
						qpHistorical.setAuthAction( authHistorical );
						qpHistorical.setDatas( datas.toArray( new PQDataWrite[0] ) );
							
						int writedHistorical = SensLinkUtils.writeHistorical( host, qpHistorical );
						log.info("SensLink 2.0 Export Adapter: success write {} historical datas to the SensLink System.", writedHistorical);
						this.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: success write {} historical datas to the SensLink System.", String.valueOf( writedHistorical ));
						
						/** Real-Time **/
						AuthenticationAction authRealTime = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.WRITE_REALTIME );
						WriteQueryParameter qpRealTime = new WriteQueryParameter();
						qpRealTime.setAuthAction( authRealTime );
						qpRealTime.setDatas( datas.toArray( new PQDataWrite[0] ) );
							
						int writedRealTime = SensLinkUtils.writeRealTime( host, qpRealTime );
						log.info("SensLink 2.0 Export Adapter: success write {} real-time datas to the SensLink System.", writedRealTime );
						this.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: success write {} real-time datas to the SensLink System.", String.valueOf( writedRealTime ));
						
						log.info("SensLink 2.0 Export Adapter: Finished Adapter process.");
						this.log( LogLevel.INFO, "SensLink 2.0 Export Adapter: Finished Adapter process.");
						
					} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException
							| UnsupportedEncodingException e) {
						log.error( "SensLink 2.0 Export Adapter: adapter has something wrong!.", e );
						this.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: adapter has something wrong!.");
					} catch (IOException e) {
						log.error( "SensLink 2.0 Export Adapter: write data has something wrong!.", e );
						this.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: write data has something wrong!.");
					}
						
				}, () -> { 
					log.warn( "SensLink 2.0 Export Adapter: SensLink System Login failed." );
					this.log( LogLevel.WARN, "SensLink 2.0 Export Adapter: SensLink System Login failed.");
				} );
				
			} else {
				log.info("SensLink 2.0 Export Adapter: PI-XML hasn't datas to export.");
				this.log( LogLevel.WARN, "SensLink 2.0 Export Adapter: PI-XML hasn't datas to export.");
			}
			
		} catch (FileNotFoundException e) {
			log.error("SensLink 2.0 Export Adapter: Input XML not exits!", e);
			this.log( LogLevel.ERROR, "SensLink 2.0 Export Adapter: Input XML not exits!");
		}
	}
}
