package tw.fondus.fews.adapter.pi.loss.richi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.DisasterLossUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.LossCollection;
import tw.fondus.fews.adapter.pi.loss.richi.util.Parameter;
import tw.fondus.fews.adapter.pi.loss.richi.xml.Data;

/**
 * The adapter of RiChi Disaster Loss API for running from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class DisasterLossAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );
	private Map<String, LossCollection> lossMap = new HashMap<>();
	private static HttpClient client = new HttpClient();

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
		Preconditions.checkState( Files.exists( mapStacksPath ),
				"DisasterLossAdapter: Can not find map stack file exist." );

		MapStacks mapStacks = XMLUtils.fromXML( mapStacksPath.toFile(), MapStacks.class );
		mapStacks.getMapStacks().forEach( mapStack -> {
			try {
				client.setClient( DisasterLossUtils.buildClientWithTimeout( 900 ) );
				IntStream.rangeClosed( 0, DisasterLossUtils.calculateTimeSteps( mapStack ) ).forEach( i -> {
					try {
						Path asc = Paths.get( DisasterLossUtils.getASCAbsolutePath( inputDir, mapStack, i ) );
						Preconditions.checkState( Files.exists( asc ),
								"DisasterLossAdapter: Can not find ASC file exist." );
						
						Path rename = DisasterLossUtils.renameToASC( asc, inputDir );

						log.info( "DisasterLossAdapter: Connecting disaster loss API with {}.",
								rename.toFile().getName() );
						piDiagnostics.addMessage( LogLevel.INFO.value(),
								Strman.append( "DisasterLossAdapter: Connecting disaster loss API with ",
										rename.toFile().getName() ) );

						Data data = XMLUtils.fromXML( DisasterLossUtils.postDisasterLossAPI( client, rename ),
								Data.class );
						data.getLossList().forEach( loss -> {
							try {
								if ( lossMap.containsKey( loss.getTownId() ) ) {
									lossMap.get( loss.getTownId() ).addData( loss,
											DisasterLossUtils.getDataDateLong( mapStack, i ) );
								} else {
									lossMap.put( loss.getTownId(), new LossCollection( loss,
											DisasterLossUtils.getDataDateLong( mapStack, i ) ) );
								}
							} catch (ParseException e) {
								log.error( "DisasterLossAdapter: Get data time from map stack has something wrong.",
										e );
								piDiagnostics.addMessage( LogLevel.ERROR.value(),
										"DisasterLossAdapter: Get data time from map stack has something wrong." );
							}
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
			} catch (Exception e) {
				log.error( "DisasterLossAdapter: Transform time has something wrong.", e );
				piDiagnostics.addMessage( LogLevel.ERROR.value(),
						"DisasterLossAdapter: Transform time has something wrong." );
			}
		} );

		log.info( "DisasterLossAdapter: Start create the FEWS PI-XML." );
		piDiagnostics.addMessage( LogLevel.INFO.value(), "DisasterLossAdapter: Start create the FEWS PI-XML." );

		/** Fill all disaster loss data by loss type parameter **/
		if ( this.lossMap.size() > 0 ) {
			Stream.of( Parameter.values() ).forEach( parameter -> {
				SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
				this.fillDataProcess( parameter, outputDir, handler, piDiagnostics );
			} );
		} else {
			log.warn( "DisasterLossAdapter: Nothing has the disaster loss data." );
			piDiagnostics.addMessage( LogLevel.WARN.value(),
					"DisasterLossAdapter: Nothing has the disaster loss data." );
		}

		log.info( "DisasterLossAdapter: End the DisasterLossAdapter." );
		piDiagnostics.addMessage( LogLevel.INFO.value(), "DisasterLossAdapter: End the DisasterLossAdapter." );
	}

	/**
	 * Fill the data process.
	 * 
	 * @param parameter
	 * @param outputDir
	 * @param handler
	 * @param piDiagnostics
	 */
	private void fillDataProcess( Parameter parameter, File outputDir, SimpleTimeSeriesContentHandler handler,
			PiDiagnostics piDiagnostics ) {
		this.lossMap.forEach( ( k, v ) -> {
			fillValue( k, parameter, v, handler );
		} );

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
	}

	/**
	 * Fill the Delft-FEWS TimeSeries content.
	 * 
	 * @param townId
	 * @param parameter
	 * @param value
	 * @param handler
	 */
	private void fillValue( String townId, Parameter parameter, LossCollection lossCollection,
			SimpleTimeSeriesContentHandler handler ) {
		TimeSeriesUtils.fillPiTimeSeriesHeader( handler, townId, parameter.getType().toLowerCase(),
				parameter.getUnit() );
		switch ( parameter ) {
		case C1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC1Loss().floatValue() );
			} );
			break;
		case C1AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC1Area().floatValue() );
			} );
			break;
		case C2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC2Loss().floatValue() );
			} );
			break;
		case C2AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC2Area().floatValue() );
			} );;
			break;
		case C3LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC3Loss().floatValue() );
			} );
			break;
		case C3AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC3Area().floatValue() );
			} );
			break;
		case C4LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC4Loss().floatValue() );
			} );
			break;
		case C4AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC4Area().floatValue() );
			} );
			break;
		case H1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH1Loss().floatValue() );
			} );
			break;
		case H1COUNT:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH1Count().floatValue() );
			} );
			break;
		case H2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH2Loss().floatValue() );
			} );
			break;
		case H2COUNT:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH2Count().floatValue() );
			} );
			break;
		case F1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF1Loss().floatValue() );
			} );
			break;
		case F1AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF1Area().floatValue() );
			} );
			break;
		case F2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF2Loss().floatValue() );
			} );
			break;
		case F2AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF2Area().floatValue() );
			} );
			break;
		case F3LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF3Loss().floatValue() );
			} );
			break;
		case F3AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF3Area().floatValue() );
			} );
			break;
		case F4LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF4Loss().floatValue() );
			} );
			break;
		case F4AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF4Area().floatValue() );
			} );
			break;
		case F5LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF5Loss().floatValue() );
			} );
			break;
		case F5AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF5Area().floatValue() );
			} );
			break;
		case F6LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF6Loss().floatValue() );
			} );
			break;
		case F6AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF6Area().floatValue() );
			} );
			break;
		case PLOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getpLoss().floatValue() );
			} );
			break;
		case PAREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getpArea().floatValue() );
			} );
			break;
		}
	}
}
