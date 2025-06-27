package tw.fondus.fews.adapter.pi.report.rmo07;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.TimeStep;
import org.jfree.data.time.Minute;
import org.joda.time.DateTime;

import nl.wldelft.fews.pi.PiVersion;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import org.joda.time.DateTimeZone;
import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesCollection;
import tw.fondus.commons.rest.pi.json.util.timeseries.PiSeriesMapper;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.commons.util.time.TimeFormats;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.ProcessArguments;
import tw.fondus.fews.adapter.pi.report.rmo07.util.WaterStationMetaUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.meta.WaterlevelMetaInfo;

/**
 * The process of export Pi-XML.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
public class PiXmlGenerateProcess extends PiCommandLineExecute {
	private Map<String, WaterlevelMetaInfo> metaInfos;
	final long THIRTY_MINUTES_MILLIS = 30 * 60 * 1000L;

	public static void main( String[] args ) {
		ProcessArguments arguments = ProcessArguments.instance();
		new PiXmlGenerateProcess().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "PiXmlGenerateProcess: Try to generate XML report file." );
		ProcessArguments processArguments = this.asArguments( arguments, ProcessArguments.class );
		int start = processArguments.getIndexStart();
		int end = processArguments.getIndexEnd();
		metaInfos = WaterStationMetaUtils.readWaterlevelMetaInfo(
				basePath.resolve( "templates/Taiwan_Stations_WaterLevel.csv" ),
				basePath.resolve( "templates/attributes.csv" ) );

		PathUtils.list( inputPath ).forEach( path -> {
			PiTimeSeriesCollection collection = GsonMapperRuntime.ISO8601.toBean( PathReader.readString( path ),
					PiTimeSeriesCollection.class );
			PiTimeSeriesCollection zonedCollection = collection.withZone( JodaTimeUtils.UTC8 );
			DateTime timeZero = zonedCollection.getTimeZero();
			String fileName = Strman.append( PathUtils.getNameWithoutExtension( path ), Strings.UNDERLINE,
					processArguments.getPrefix(), Strings.UNDERLINE,
					JodaTimeUtils.toString( timeZero, TimeFormats.YMDH_UNDIVIDED, timeZero.getZone() ),
					Strings.UNDERLINE, processArguments.getSuffix(), FileType.XML.getExtension() );
			this.generate( logger, outputPath, zonedCollection, fileName, start, end, processArguments.isForce1HFormat() );
		} );
	}

	@SuppressWarnings( "rawtypes" )
	private Path generate( PiDiagnosticsLogger logger, Path base, PiTimeSeriesCollection collection, String fileName,
			int start, int end, boolean isForce1HFormat) {
		Path outputPath = base.resolve( fileName );
		if ( collection.size() > 0 && collection.get( 0 ).size() >= end && start >= 0 ){
			PiTimeSeriesCollection subset = collection.subset( start, end );

			// Write the PI-XML
			TimeSeriesArrays timeSeriesArrays = this.renameCrossSectionIds( logger, PiSeriesMapper.from( subset ), isForce1HFormat );
			try {
				TimeSeriesUtils.write( timeSeriesArrays, outputPath, Numbers.MISSING, PiVersion.VERSION_1_3, JodaTimeUtils.UTC8 );
			} catch (IOException e){
				logger.log( LogLevel.ERROR, "PiXmlGenerateService: Generate XML report failed.", e );
			}

			// Reformat to 07RMO format
			this.reformatPiXml( logger, outputPath );

			logger.log( LogLevel.INFO, "PiXmlGenerateService: Finished to generate XML report file." );
		} else {
			logger.log( LogLevel.WARN, "PiXmlGenerateService: Generate use index range {} ~ {} not validate, skip generate process.", start, end );
		}
		return outputPath;
	}

	/**
	 * Rename time series array location id into cross-section id.
	 *
	 * @param timeSeriesArrays timeseries array
	 * @return renamed timeseries array
	 * @since 1.0.3
	 */
	@SuppressWarnings( "rawtypes" )
	private TimeSeriesArrays renameCrossSectionIds( PiDiagnosticsLogger logger, TimeSeriesArrays timeSeriesArrays, boolean isForce1HFormat ){
		logger.log( LogLevel.INFO, "PiXmlGenerateService: Rename timeseries location id into cross-section id." );
		TimeStep timeStep = timeSeriesArrays.get(0).getTimeStep();
		SimpleTimeSeriesContentHandler handler = TimeSeriesUtils.seriesHandler();
		handler.setOverrulingTimeStep(timeStep);
		TimeSeriesUtils.toList( timeSeriesArrays )
				.forEach( timeSeriesArray -> {
					TimeSeriesHeader header = timeSeriesArray.getHeader();
					String locationId = header.getLocationId();
					String crossSectionId = Optional.of( metaInfos.get( locationId ) )
							.map( WaterlevelMetaInfo::getCrossSectionId )
							.filter( id -> StringUtils.isNotBlank( id ) )
							.orElse( locationId );

					int size = timeSeriesArray.size();
					if (timeStep.getMinimumStepMillis() == THIRTY_MINUTES_MILLIS && isForce1HFormat) {
						TimeSeriesUtils.addHeader(handler, crossSectionId, header.getParameterId(), header.getUnit());
						for (int i = 0; i < size; i++) {
							Minute minute = new Minute(new Date(timeSeriesArray.getTime(i)));
							if (minute.getMinute() == 30) {
								TimeSeriesUtils.addValue(handler, timeSeriesArray.getTime(i), timeSeriesArray.getValue(i));
							}
						}
					} else {
						TimeSeriesUtils.addHeader(handler, crossSectionId, header.getParameterId(), header.getUnit(), header.getTimeStep());
						IntStream.range(0, size)
								.forEach(i -> TimeSeriesUtils.addValue(handler, timeSeriesArray.getTime(i), timeSeriesArray.getValue(i)));
					}
		} );
		return handler.getTimeSeriesArrays();
	}

	/**
	 * Reformat PI-XML content into 07RMO XML format.
	 *
	 * @param path path of xml
	 * @since 1.0.3
	 */
	private void reformatPiXml( PiDiagnosticsLogger logger, Path path ){
		logger.log( LogLevel.INFO, "PiXmlGenerateService: Reformat content to match 07RMO XML format." );
		List<String> lines = PathReader.readAllLines( path );
		String content = lines.stream()
				.filter( line -> !Strman.isEnclosedBetween( Strman.trimStart( line ).orElse( line ), "<stationName>", "</stationName>" ) )
				.map( line -> line.replace( "<timeStep unit=\"second\" multiplier=\"3600\"/>", "<timeStep multiplier=\"1\" unit=\"hour\"/>" ) )
				.map( line -> line.replace( " flag=\"0\"", Strings.BLANK ) )
				.map( line -> line.replace( "series", "Series" ) )
				.map( line -> line.replace( "startDate", "StartDate" ) )
				.collect( Collectors.joining( Strings.BREAKLINE ) );
		PathUtils.deleteIfExists( path );
		PathWriter.write( path, content );
	}
}
