package tw.fondus.fews.adapter.pi.senslink.v3;

import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.oauth2.model.OAuthToken;
import tw.fondus.commons.rest.senslink.v3.feign.SensLinkApiV3;
import tw.fondus.commons.rest.senslink.v3.feign.SensLinkApiV3Runtime;
import tw.fondus.commons.rest.senslink.v3.model.ResultMessage;
import tw.fondus.commons.rest.senslink.v3.model.record.Record;
import tw.fondus.commons.rest.senslink.v3.util.SensLinkApiV3Host;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.senslink.v3.argument.ExportArguments;

import java.io.IOException;
import java.util.List;

/**
 * The parent used to export data to SensLink 3.0 with Delft-FEWS.
 *
 * @author Brad Chen
 * @since 3.0.0
 */
public abstract class ParentExportToSensLinkAdapter extends PiCommandLineExecute {

	/**
	 * Thr process used to export records to SensLink 3.0.
	 *
	 * @param arguments arguments
	 * @param username username
	 * @param password password
	 * @param records records should be write
	 */
	protected void exportRecords( ExportArguments arguments, String username, String password, List<Record> records ){
		if ( records.size() > 0 ){
			this.getLogger().log( LogLevel.INFO, "SensLink 3.0 Export All Adapter: export {} records to the SensLink System.", records.size() );

			SensLinkApiV3 api;
			OAuthToken token;
			try {
				// Login SensLink 3.0 by OAuth 2.0
				if ( arguments.isCustom() ){
					api = SensLinkApiV3Runtime.buildRuntime( arguments.getHost() );
					token = api.requestAccessToken( username, password, arguments.getHost() );
				} else {
					api = SensLinkApiV3Runtime.DEFAULT;
					token = api.requestAccessToken( username, password, SensLinkApiV3Host.IOW );
				}
				if ( token.isSuccessful() ){
					this.getLogger().log( LogLevel.INFO, "SensLink 3.0 Export Adapter: The SensLink 3.0 system login successfully, try to write records to the SensLink 3.0 system." );
					ResultMessage message = api.writeFormulaTransferred( token.getAccess(), records );
					if ( message.isSuccessful() ) {
						this.getLogger().log( LogLevel.INFO,"SensLink 3.0 Export Adapter: success to write {} records to the SensLink System.", records.size() );
					} else {
						this.getLogger().log( LogLevel.WARN,"SensLink 3.0 Export Adapter: Failed to write records to the SensLink System." );
					}
				} else {
					this.getLogger().log( LogLevel.WARN, "SensLink 3.0 Export Adapter: SensLink System Login failed." );
				}
			} catch ( IOException e ){
				this.getLogger().log( LogLevel.ERROR, "SensLink 3.0 Export Adapter: Connection to SensLink 3.0 system timeout. {}", e ) ;
			}
		} else {
			this.getLogger().log( LogLevel.WARN, "SensLink 3.0 Export Adapter: PI-XML hasn't data to export." );
		}
	}
}
