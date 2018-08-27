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

import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.http.HttpClient;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.DisasterLossUtils;
import tw.fondus.fews.adapter.pi.loss.richi.util.MapStacksArguments;
import tw.fondus.fews.adapter.pi.loss.richi.util.Parameter;
import tw.fondus.fews.adapter.pi.loss.richi.xml.Data;
import tw.fondus.fews.adapter.pi.loss.richi.xml.Loss;

public class DisasterLossAdapter extends PiCommandLineExecute {
	protected Logger log = LoggerFactory.getLogger( this.getClass() );
	private List<Loss> lossList = new ArrayList<>();
	private long timeLong;

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		MapStacksArguments mapStacksArguments = (MapStacksArguments) arguments;
		Path mapStacksPath = Paths.get( Strman.append( inputDir.getAbsolutePath(), StringUtils.PATH, mapStacksArguments.getMapStacks() ) );
		timeLong = DisasterLossUtils.getDataDateLong( mapStacksPath );
		
		try (Stream<Path> ascPaths = Files.list( inputDir.toPath() ) ) {
			HttpClient client = new HttpClient();
			client.setClient( DisasterLossUtils.buildClientWithTimeout( (long) 900 ));
			ascPaths.filter( asc -> asc.toFile().getName().endsWith( FileType.ASC.getType() ) )
					.forEach( asc -> {
						try {
							Data data = XMLUtils.fromXML( DisasterLossUtils.postDisasterLossAPI( client, asc ), Data.class );
							data.getLossList().forEach( loss -> {
								lossList.add( loss );
							} );
						} catch (IOException e) {
							log.error( "Post form has something wrong." );
						} catch (Exception e) {
							log.error( "Parsing xml has something wrong." );
						}
					} );
		}

		Stream.of( Parameter.values() ).forEach( parameter -> {
			SimpleTimeSeriesContentHandler handler = new SimpleTimeSeriesContentHandler();
			this.fillDataProcess( parameter, outputDir, handler );
		} );
	}

	private void fillDataProcess( Parameter parameter, File outputDir, SimpleTimeSeriesContentHandler handler ) {
		lossList.forEach( loss -> {
			switch ( parameter ) {
			case C1LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getC1Loss(), handler );
				break;
			case C1AREA:
				fillvalue( loss.getTownId(), parameter, loss.getC1Area(), handler );
				break;
			case C2LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getC2Loss(), handler );
				break;
			case C2AREA:
				fillvalue( loss.getTownId(), parameter, loss.getC2Area(), handler );
				break;
			case C3LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getC3Loss(), handler );
				break;
			case C3AREA:
				fillvalue( loss.getTownId(), parameter, loss.getC3Area(), handler );
				break;
			case C4LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getC4Loss(), handler );
				break;
			case C4AREA:
				fillvalue( loss.getTownId(), parameter, loss.getC4Area(), handler );
				break;
			case H1LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getH1Loss(), handler );
				break;
			case H1COUNT:
				fillvalue( loss.getTownId(), parameter, loss.getH1Count(), handler );
				break;
			case H2LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getH2Loss(), handler );
				break;
			case H2COUNT:
				fillvalue( loss.getTownId(), parameter, loss.getH2Count(), handler );
				break;
			case F1LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getF1Loss(), handler );
				break;
			case F1AREA:
				fillvalue( loss.getTownId(), parameter, loss.getF1Area(), handler );
				break;
			case F2LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getF2Loss(), handler );
				break;
			case F2AREA:
				fillvalue( loss.getTownId(), parameter, loss.getF2Area(), handler );
				break;
			case F3LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getF3Loss(), handler );
				break;
			case F3AREA:
				fillvalue( loss.getTownId(), parameter, loss.getF3Area(), handler );
				break;
			case F4LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getF4Loss(), handler );
				break;
			case F4AREA:
				fillvalue( loss.getTownId(), parameter, loss.getF4Area(), handler );
				break;
			case F5LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getF5Loss(), handler );
				break;
			case F5AREA:
				fillvalue( loss.getTownId(), parameter, loss.getF5Area(), handler );
				break;
			case F6LOSS:
				fillvalue( loss.getTownId(), parameter, loss.getF6Loss(), handler );
				break;
			case F6AREA:
				fillvalue( loss.getTownId(), parameter, loss.getF6Area(), handler );
				break;
			case PLOSS:
				fillvalue( loss.getTownId(), parameter, loss.getpLoss(), handler );
				break;
			case PAREA:
				fillvalue( loss.getTownId(), parameter, loss.getpArea(), handler );
				break;
			}
		} );
		try {
			TimeSeriesUtils.writePIFile( handler, Strman.append( outputDir.getAbsolutePath(), StringUtils.PATH,
					parameter.getType(), StringUtils.DOT, FileType.XML.getType() ) );
		} catch (InterruptedException e) {
			log.error( "Writing pi xml file has something worng." );
		} catch (IOException e) {
			log.error( "Writing pi xml file has something worng." );
		} finally {
			handler.clear();
		}
	}

	private void fillvalue( String townId, Parameter parameter, BigDecimal value, SimpleTimeSeriesContentHandler handler ) {
		TimeSeriesUtils.fillPiTimeSeriesHeader( handler, townId, parameter.getType().toLowerCase(),
				parameter.getUnit() );
		TimeSeriesUtils.addPiTimeSeriesValue( handler, timeLong, value.floatValue() );
	}
}
