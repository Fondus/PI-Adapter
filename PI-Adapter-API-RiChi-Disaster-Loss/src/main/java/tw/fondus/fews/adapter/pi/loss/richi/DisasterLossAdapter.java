package tw.fondus.fews.adapter.pi.loss.richi;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.DisasterLossUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.Parameter;
import tw.fondus.fews.adapter.pi.loss.richi.xml.Data;
import tw.fondus.fews.adapter.pi.loss.richi.xml.Loss;

/**
 * The adapter of RiChi Disaster Loss API for running from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class DisasterLossAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );
	private List<Loss> lossList = new ArrayList<>();
	private long timeLong;

	public static void main( String[] args ) {
		PiBasicArguments arguments = new PiArguments();
		new DisasterLossAdapter().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		log.info( "DisasterLossAdapter: Start adapter of RiChi Disaster Loss API." );
		piDiagnostics.addMessage( LogLevel.INFO.value(),
				"DisasterLossAdapter: Start adapter of RiChi Disaster Loss API." );

		PiArguments piArguments = (PiArguments) arguments;
		Path mapStacksPath = Paths
				.get( Strman.append( inputDir.getAbsolutePath(), StringUtils.PATH, piArguments.getInputs().get( 0 ) ) );
		timeLong = DisasterLossUtils.getDataDateLong( mapStacksPath );

		try (Stream<Path> ascPaths = Files.list( inputDir.toPath() ) ) {
			HttpClient client = new HttpClient();
			client.setClient( DisasterLossUtils.buildClientWithTimeout( (long) 900 ) );

			/** Collect all disaster loss with ASC file **/
			ascPaths.filter( asc -> !FileUtils.getFileExt( asc.toFile() ).equals( FileType.XML.getType() ) )
				.forEach( asc -> {
					try {
						Path rename = DisasterLossUtils.renameToASC( asc, inputDir );
					
						log.info( "DisasterLossAdapter: Connecting disaster loss API with {}.", rename.toFile().getName() );
						piDiagnostics.addMessage( LogLevel.INFO.value(), Strman
								.append( "DisasterLossAdapter: Connecting disaster loss API with ", rename.toFile().getName() ) );
						
						Data data = XMLUtils.fromXML( DisasterLossUtils.postDisasterLossAPI( client, rename ), Data.class );
						data.getLossList().forEach( loss -> {
							lossList.add( loss );
						} );
					} catch (IOException e) {
						log.error( "DisasterLossAdapter: Post form has something wrong.", e );
						piDiagnostics.addMessage( LogLevel.ERROR.value(),
								"DisasterLossAdapter: Post form has something wrong." );
					} catch (Exception e) {
						log.error( "DisasterLossAdapter: Parsing xml has something wrong.", e );
						piDiagnostics.addMessage( LogLevel.ERROR.value(),
								"DisasterLossAdapter: Parsing xml has something wrong." );
					}
				} );
		}
		
		log.info( "DisasterLossAdapter: Start create the FEWS PI-XML." );
		piDiagnostics.addMessage( LogLevel.INFO.value(), "DisasterLossAdapter: Start create the FEWS PI-XML." );
		
		/** Fill all disaster loss data by loss type parameter **/
		Stream.of( Parameter.values() ).forEach( parameter -> {
			SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
			this.fillDataProcess( parameter, outputDir, handler, piDiagnostics );
		} );
		
		log.info( "DisasterLossAdapter: End the DisasterLossAdapter." );
		piDiagnostics.addMessage( LogLevel.INFO.value(), "DisasterLossAdapter: End the DisasterLossAdapter." );
	}

	/**
	 * 
	 * 
	 * @param parameter
	 * @param outputDir
	 * @param handler
	 * @param piDiagnostics
	 */
	private void fillDataProcess( Parameter parameter, File outputDir, SimpleTimeSeriesContentHandler handler,
			PiDiagnostics piDiagnostics ) {
		lossList.forEach( loss -> {
			switch ( parameter ) {
			case C1LOSS:
				fillValue( loss.getTownId(), parameter, loss.getC1Loss(), handler );
				break;
			case C1AREA:
				fillValue( loss.getTownId(), parameter, loss.getC1Area(), handler );
				break;
			case C2LOSS:
				fillValue( loss.getTownId(), parameter, loss.getC2Loss(), handler );
				break;
			case C2AREA:
				fillValue( loss.getTownId(), parameter, loss.getC2Area(), handler );
				break;
			case C3LOSS:
				fillValue( loss.getTownId(), parameter, loss.getC3Loss(), handler );
				break;
			case C3AREA:
				fillValue( loss.getTownId(), parameter, loss.getC3Area(), handler );
				break;
			case C4LOSS:
				fillValue( loss.getTownId(), parameter, loss.getC4Loss(), handler );
				break;
			case C4AREA:
				fillValue( loss.getTownId(), parameter, loss.getC4Area(), handler );
				break;
			case H1LOSS:
				fillValue( loss.getTownId(), parameter, loss.getH1Loss(), handler );
				break;
			case H1COUNT:
				fillValue( loss.getTownId(), parameter, loss.getH1Count(), handler );
				break;
			case H2LOSS:
				fillValue( loss.getTownId(), parameter, loss.getH2Loss(), handler );
				break;
			case H2COUNT:
				fillValue( loss.getTownId(), parameter, loss.getH2Count(), handler );
				break;
			case F1LOSS:
				fillValue( loss.getTownId(), parameter, loss.getF1Loss(), handler );
				break;
			case F1AREA:
				fillValue( loss.getTownId(), parameter, loss.getF1Area(), handler );
				break;
			case F2LOSS:
				fillValue( loss.getTownId(), parameter, loss.getF2Loss(), handler );
				break;
			case F2AREA:
				fillValue( loss.getTownId(), parameter, loss.getF2Area(), handler );
				break;
			case F3LOSS:
				fillValue( loss.getTownId(), parameter, loss.getF3Loss(), handler );
				break;
			case F3AREA:
				fillValue( loss.getTownId(), parameter, loss.getF3Area(), handler );
				break;
			case F4LOSS:
				fillValue( loss.getTownId(), parameter, loss.getF4Loss(), handler );
				break;
			case F4AREA:
				fillValue( loss.getTownId(), parameter, loss.getF4Area(), handler );
				break;
			case F5LOSS:
				fillValue( loss.getTownId(), parameter, loss.getF5Loss(), handler );
				break;
			case F5AREA:
				fillValue( loss.getTownId(), parameter, loss.getF5Area(), handler );
				break;
			case F6LOSS:
				fillValue( loss.getTownId(), parameter, loss.getF6Loss(), handler );
				break;
			case F6AREA:
				fillValue( loss.getTownId(), parameter, loss.getF6Area(), handler );
				break;
			case PLOSS:
				fillValue( loss.getTownId(), parameter, loss.getpLoss(), handler );
				break;
			case PAREA:
				fillValue( loss.getTownId(), parameter, loss.getpArea(), handler );
				break;
			}
		} );

		if ( this.lossList.size() > 0 ) {
			try {
				TimeSeriesUtils.writePIFile( handler, Strman.append( outputDir.getAbsolutePath(), StringUtils.PATH,
						parameter.getType(), StringUtils.DOT, FileType.XML.getType() ) );
			} catch (InterruptedException e) {
				log.error( "Writing pi xml file has something worng.", e );
				piDiagnostics.addMessage( LogLevel.ERROR.value(), "Writing pi xml file has something wrong." );
			} catch (IOException e) {
				log.error( "Writing pi xml file has something worng.", e );
				piDiagnostics.addMessage( LogLevel.ERROR.value(), "Writing pi xml file has something wrong." );
			} finally {
				handler.clear();
			}
		} else {
			log.warn( "DisasterLossAdapter: Nothing has the disaster loss data." );
			piDiagnostics.addMessage( LogLevel.WARN.value(),
					"DisasterLossAdapter: Nothing has the disaster loss data." );
		}
	}

	/**
	 * Fill the Delft-FEWS TimeSeries content.
	 * 
	 * @param townId
	 * @param parameter
	 * @param value
	 * @param handler
	 */
	private void fillValue( String townId, Parameter parameter, BigDecimal value,
			SimpleTimeSeriesContentHandler handler ) {
		TimeSeriesUtils.fillPiTimeSeriesHeader( handler, townId, parameter.getType().toLowerCase(),
				parameter.getUnit() );
		TimeSeriesUtils.addPiTimeSeriesValue( handler, timeLong, value.floatValue() );
	}
}
