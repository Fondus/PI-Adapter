package tw.fondus.fews.adapter.pi.loss.richi;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.loss.richi.util.DisasterLossUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.LossCollection;
import tw.fondus.fews.adapter.pi.loss.richi.util.Parameter;
import tw.fondus.fews.adapter.pi.loss.richi.xml.Data;
import tw.fondus.fews.adapter.pi.loss.richi.xml.ErrorData;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The adapter of RiChi Disaster Loss API for running from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class DisasterLossAdapter extends PiCommandLineExecute {
	private Map<String, LossCollection> lossMap = new HashMap<>();
	private static final HttpClient client = new HttpClient();

	public static void main( String[] args ) {
		PiIOArguments arguments = new PiIOArguments();
		new DisasterLossAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		PiIOArguments piArguments = (PiIOArguments) arguments;
		
		Path mapStacksPath = Prevalidated.checkExists( 
				Paths.get( Strman.append( inputPath.toString(), PATH, piArguments.getInputs().get( 0 ) ) ),
				"DisasterLossAdapter: Can not find map stack file exist." );
		
		client.setClient( DisasterLossUtils.buildClientWithTimeout( 900 ) );
		
		logger.log( LogLevel.INFO, "DisasterLossAdapter: Start adapter of RiChi Disaster Loss API." );

		try {
			MapStacks mapStacks = XMLUtils.fromXML( mapStacksPath, MapStacks.class );
			mapStacks.getMapStacks().forEach( mapStack -> {
				try {
					IntStream.rangeClosed( 0, DisasterLossUtils.calculateTimeSteps( mapStack ) ).forEach( i -> {
						try {
							Path asc = Prevalidated.checkExists( 
									Paths.get( DisasterLossUtils.getASCAbsolutePath( inputPath, mapStack, i ) ),
									"DisasterLossAdapter: Can not find ASC file exist." );
							
							String fileName = asc.toFile().getName();
							Path rename = DisasterLossUtils.renameToASC( asc, inputPath );
							
							logger.log( LogLevel.INFO, "DisasterLossAdapter: Connecting disaster loss API with {}.", fileName );
							
							String result = DisasterLossUtils.postDisasterLossAPI( client, rename );
							if ( result.contains( "<Code>" ) ) {
								ErrorData errorData = XMLUtils.fromXML( result, ErrorData.class );
								logger.log( LogLevel.ERROR, "DisasterLossAdapter: {}", errorData.getMessage() );
								throw new Exception();
							} else {
								Data data = XMLUtils.fromXML( result, Data.class );
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
										logger.log( LogLevel.ERROR, "DisasterLossAdapter: Get data time from map stack has something wrong." );
									}
								});
							}
						} catch (IOException e) {
							logger.log( LogLevel.ERROR, "DisasterLossAdapter: Post ASC to API has something wrong." );
						} catch (Exception e) {
							logger.log( LogLevel.ERROR, "DisasterLossAdapter: Parsing xml has something wrong." );
						}
					});
				} catch (ParseException e) {
					logger.log( LogLevel.ERROR, "DisasterLossAdapter: Parse has something wrong." );
				}
			});
			
			logger.log( LogLevel.INFO, "DisasterLossAdapter: Start create the FEWS PI-XML." );
			
			/** Fill all disaster loss data by loss type parameter **/
			if ( this.lossMap.size() > 0 ) {
				Stream.of( Parameter.values() ).forEach( parameter -> {
					SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
					this.fillDataProcess( logger, parameter, outputPath, handler );
				} );
			} else {
				logger.log( LogLevel.WARN, "DisasterLossAdapter: Nothing has the disaster loss data." );
			}
	
			logger.log( LogLevel.INFO, "DisasterLossAdapter: End the DisasterLossAdapter." );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "DisasterLossAdapter: Adapter has something wrong." );
		}
	}

	/**
	 * Fill the data process.
	 * 
	 * @param parameter
	 * @param outputPath
	 * @param handler
	 * @param piDiagnostics
	 */
	private void fillDataProcess( PiDiagnosticsLogger logger, Parameter parameter,
			Path outputPath, SimpleTimeSeriesContentHandler handler ) {
		this.lossMap.forEach( ( k, v ) -> {
			fillValue( k, parameter, v, handler );
		} );
		
		try {
			TimeSeriesLightUtils.writePIFile( handler, Strman.append( outputPath.toString(), PATH,
					parameter.getType(), StringUtils.DOT, FileType.XML.getType() ) );
		} catch (InterruptedException e) {
			logger.log( LogLevel.ERROR, "Writing pi xml file has something wrong." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "Writing pi xml file has something wrong." );
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
		TimeSeriesLightUtils.fillPiTimeSeriesHeader( handler, townId, parameter.getType().toLowerCase(),
				parameter.getUnit() );
		switch ( parameter ) {
		case C1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC1Loss().floatValue() );
			} );
			break;
		case C1AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC1Area().floatValue() );
			} );
			break;
		case C2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC2Loss().floatValue() );
			} );
			break;
		case C2AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC2Area().floatValue() );
			} );
			;
			break;
		case C3LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC3Loss().floatValue() );
			} );
			break;
		case C3AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC3Area().floatValue() );
			} );
			break;
		case C4LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC4Loss().floatValue() );
			} );
			break;
		case C4AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getC4Area().floatValue() );
			} );
			break;
		case H1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH1Loss().floatValue() );
			} );
			break;
		case H1UNIT:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH1Count().floatValue() );
			} );
			break;
		case H2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH2Loss().floatValue() );
			} );
			break;
		case H2UNIT:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getH2Count().floatValue() );
			} );
			break;
		case F1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF1Loss().floatValue() );
			} );
			break;
		case F1AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF1Area().floatValue() );
			} );
			break;
		case F2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF2Loss().floatValue() );
			} );
			break;
		case F2AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF2Area().floatValue() );
			} );
			break;
		case F3LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF3Loss().floatValue() );
			} );
			break;
		case F3AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF3Area().floatValue() );
			} );
			break;
		case F4LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF4Loss().floatValue() );
			} );
			break;
		case F4AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF4Area().floatValue() );
			} );
			break;
		case F5LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF5Loss().floatValue() );
			} );
			break;
		case F5AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF5Area().floatValue() );
			} );
			break;
		case F6LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF6Loss().floatValue() );
			} );
			break;
		case F6AREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getF6Area().floatValue() );
			} );
			break;
		case PLOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getpLoss().floatValue() );
			} );
			break;
		case PAREA:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getpArea().floatValue() );
			} );
			break;
		case L1LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL1Loss().floatValue() );
			} );
			break;
		case L1NUMBER:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL1Number().floatValue() );
			} );
			break;
		case L2LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL2Loss().floatValue() );
			} );
			break;
		case L2NUMBER:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL2Number().floatValue() );
			} );
			break;
		case L3LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL3Loss().floatValue() );
			} );
			break;
		case L3NUMBER:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL3Number().floatValue() );
			} );
			break;
		case L4LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL4Loss().floatValue() );
			} );
			break;
		case L4NUMBER:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL4Number().floatValue() );
			} );
			break;
		case L5LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL5Loss().floatValue() );
			} );
			break;
		case L5NUMBER:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL5Number().floatValue() );
			} );
			break;
		case L6LOSS:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL6Loss().floatValue() );
			} );
			break;
		case L6NUMBER:
			IntStream.range( 0, lossCollection.getLossList().size() ).forEach( i -> {
				TimeSeriesLightUtils.addPiTimeSeriesValue( handler, lossCollection.getDataTimeLongList().get( i ),
						lossCollection.getLossList().get( i ).getL6Number().floatValue() );
			} );
			break;
		}
	}
}
