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
			piDiagnostics.addMessage( LogLevel.INFO.value(), "SensLink 2.0 Export Adapter: Start translate PI-XML to SensLink PhysicalQuantity Data.");
			
			int index = modelArguments.getIndex();
			
			TimeSeriesArrays timeSeriesArrays = TimeSeriesUtils.readPiTimeSeries( xmlPath.toFile() );
			List<PQDataWrite> datas = SensLinkUtils.toWriteDatas( timeSeriesArrays, index );
			
			if ( datas.size() > 0 ){
				log.info("SensLink 2.0 Export Adapter: export {} datas to the SensLink System.", datas.size());
				piDiagnostics.addMessage( LogLevel.INFO.value(), Strman.append( "SensLink 2.0 Export Adapter: export ", String.valueOf( datas.size() ), " datas to the SensLink System." ));
				
				String username = modelArguments.getUsername();
				String password = modelArguments.getPassword();
				
				Optional<AuthInfoResponse> optAuth = SensLinkUtils.login( username, password );
				OptionalUtils.ifPresentOrElse( optAuth, auth -> {
					
						try {
							/** Historical **/
							AuthenticationAction authHistorical = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.ACTION_WRITE_HISTORICAL );
							WriteQueryParameter qpHistorical = new WriteQueryParameter();
							qpHistorical.setAuthAction( authHistorical );
							qpHistorical.setDatas( datas.toArray( new PQDataWrite[0] ) );
							
							int writedHistorical = SensLinkUtils.writeHistorical( qpHistorical );
							log.info("SensLink 2.0 Export Adapter: success write {} historical datas to the SensLink System.", writedHistorical);
							piDiagnostics.addMessage( LogLevel.INFO.value(), Strman.append( "SensLink 2.0 Export Adapter: success write ", String.valueOf( writedHistorical ), " historical datas to the SensLink System." ));
							
							/** Real-Time **/
							AuthenticationAction authRealTime = SensLinkUtils.createAuthentication( auth.getKey(), username, SensLinkUtils.ACTION_WRITE_REALTIME );
							WriteQueryParameter qpRealTime = new WriteQueryParameter();
							qpRealTime.setAuthAction( authRealTime );
							qpRealTime.setDatas( datas.toArray( new PQDataWrite[0] ) );
							
							int writedRealTime = SensLinkUtils.writeRealTime( qpRealTime );
							log.info("SensLink 2.0 Export Adapter: success write {} real-time datas to the SensLink System.", writedRealTime );
							piDiagnostics.addMessage( LogLevel.INFO.value(), Strman.append( "SensLink 2.0 Export Adapter: success write ", String.valueOf( writedRealTime ), " real-time datas to the SensLink System." ));
							
							log.info("SensLink 2.0 Export Adapter: Finished Adapter process.");
							piDiagnostics.addMessage( LogLevel.INFO.value(), "SensLink 2.0 Export Adapter: Finished Adapter process.");
							
						} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException
								| UnsupportedEncodingException e) {
							log.error( "SensLink 2.0 Export Adapter: adapter has something wrong!.", e );
							piDiagnostics.addMessage( LogLevel.ERROR.value(), "SensLink 2.0 Export Adapter: adapter has something wrong!." );
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error( "SensLink 2.0 Export Adapter: write data has something wrong!.", e );
							piDiagnostics.addMessage( LogLevel.ERROR.value(), "SensLink 2.0 Export Adapter: write data has something wrong!." );
						}
						
				}, () -> { 
					log.warn( "SensLink 2.0 Export Adapter: SensLink System Login failed." );
					piDiagnostics.addMessage( LogLevel.WARN.value(), "SensLink 2.0 Export Adapter: SensLink System Login failed." );
				} );
				
			} else {
				log.info("SensLink 2.0 Export Adapter: PI-XML hasn't datas to export.");
				piDiagnostics.addMessage( LogLevel.WARN.value(), "SensLink 2.0 Export Adapter: PI-XML hasn't datas to export.");
			}
			
		} catch (FileNotFoundException e) {
			log.error("SensLink 2.0 Export Adapter: Input XML not exits!", e);
			piDiagnostics.addMessage( LogLevel.ERROR.value(), "SensLink Export Adapter: Input XML not exits!");
		}
	}
}
