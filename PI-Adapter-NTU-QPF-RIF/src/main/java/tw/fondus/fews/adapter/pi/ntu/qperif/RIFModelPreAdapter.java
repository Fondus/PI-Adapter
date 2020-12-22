package tw.fondus.fews.adapter.pi.ntu.qperif;

import com.opencsv.bean.CsvToBeanBuilder;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.math.Numbers;
import tw.fondus.commons.util.string.StringFormatter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.ntu.qperif.vo.SensorIdMapping;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The model pre-adapter for running NTU-QPE RIF model from Delft-FEWS.
 *
 * @author Brad Chen
 *
 */
@SuppressWarnings( { "unchecked", "rawtypes" } )
public class RIFModelPreAdapter extends PiCommandLineExecute {
	public static void main( String[] args ){
		PiIOArguments arguments = PiIOArguments.instance();
		new RIFModelPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "RIFModelPreAdapter: Starting Pre-Adapter process." );
		PiIOArguments adapterArguments = this.asIOArguments( arguments );

		var inputs = adapterArguments.getInputs();
		var outputs = adapterArguments.getOutputs();
		if ( inputs.size() != 3 && outputs.size() != 2 ){
			logger.log( LogLevel.ERROR, "RIFModelPreAdapter: The inputs or outputs argument size not match!" );
		} else {
			Path tideXmlPath = Prevalidated.checkExists( inputPath.resolve( inputs.get( 0 ) ),
					"RIFModelPreAdapter: Can not find tide XML file in input folder." );
			Path sensorXmlPath = Prevalidated.checkExists( inputPath.resolve( inputs.get( 1 ) ),
					"RIFModelPreAdapter: Can not find sensor XML file in input folder." );
			Path sensorOrderPath = Prevalidated.checkExists( basePath.resolve( inputs.get( 2 ) ),
					"RIFModelPreAdapter: Can not find sensor order CSV file in input folder." );

			try {
				var mappings = this.readSensorOrder( sensorOrderPath );
				if ( mappings.isEmpty() ){
					logger.log( LogLevel.ERROR, "RIFModelPreAdapter: Sensor order file is empty!" );
				} else {
					TimeSeriesArray tideTimeSeriesArray = TimeSeriesLightUtils.read( tideXmlPath ).get( 0 );
					this.writeTideInput( tideTimeSeriesArray, outputPath.resolve( outputs.get( 0 ) ) );

					TimeSeriesArrays sensorTimeSeriesArrays = TimeSeriesLightUtils.read( sensorXmlPath );
					this.writeSensorInput( sensorTimeSeriesArrays, mappings, outputPath.resolve( outputs.get( 1 ) ) );
				}
			} catch ( IOException e ) {
				logger.log( LogLevel.ERROR, "RIFModelPreAdapter: Read the input XML has something wrong.", e );
			}
		}
		logger.log( LogLevel.INFO, "RIFModelPreAdapter: Finished Pre-Adapter process." );
	}

	/**
	 * Read the model sensor order file.
	 *
	 * @param sensorOrderPath path of sensor order
	 * @return sensor mappings
	 */
	private List<SensorIdMapping> readSensorOrder( Path sensorOrderPath ){
		this.getLogger().log( LogLevel.INFO, "RIFModelPreAdapter: Read the model sensor order file." );
		try {
			return new CsvToBeanBuilder( new FileReader( sensorOrderPath.toFile(), Strings.UTF8_CHARSET ) )
					.withSkipLines( 1 )
					.withType( SensorIdMapping.class )
					.build()
					.parse();
		} catch (IOException e) {
			this.getLogger().log( LogLevel.ERROR, "RIFModelPreAdapter: Read the model sensor order file has something wrong.", e );
		}
		return CollectionUtils.emptyListArray();
	}

	private void writeSensorInput( TimeSeriesArrays timeSeriesArrays, List<SensorIdMapping> mappings, Path writePath ){
		this.getLogger().log( LogLevel.INFO, "RIFModelPreAdapter: Write the model sensor input file." );
		// Map
		var map = TimeSeriesUtils.toMap( timeSeriesArrays );

		// Header
		String header = mappings.stream()
				.map( SensorIdMapping::getModel )
				.collect( Collectors.joining( Strings.COMMA, "yyyymmddhh,T,", Strings.BREAKLINE ) );

		// Time
		TimeSeriesArray array = timeSeriesArrays.get( 0 );
		int timeSize = array.size();
		var times = IntStream.range( 0, timeSize )
			.mapToObj( array::getTime )
			.map( JodaTimeUtils::toDateTime )
			.map( dateTime -> JodaTimeUtils.toString( dateTime, "yyyyMMddHH") )
			.collect( Collectors.toList());

		PathWriter.write( writePath, header );
		IntStream.range( 0, timeSize ).forEach( i -> {
			String line = mappings.stream()
				.map( mapping -> map.containsKey( mapping.getId() ) ?
						TimeSeriesUtils.getValue( map.get( mapping.getId() ), i, Numbers.ZERO ) : Numbers.ZERO )
				.map( BigDecimal::toString )
				.collect( Collectors.joining(
						Strings.COMMA,
						StringFormatter.format( "{},{},", times.get( i ), i - timeSize + 1 ),
						Strings.BREAKLINE
				));

			PathWriter.append( writePath, line );
		} );
	}

	/**
	 * Write the model tide input file.
	 *
	 * @param tideTimeSeriesArray time series array of tide
	 * @param writePath path of model tide input file
	 */
	private void writeTideInput( TimeSeriesArray tideTimeSeriesArray, Path writePath ){
		this.getLogger().log( LogLevel.INFO, "RIFModelPreAdapter: Write the model tide input file." );
		var value = TimeSeriesLightUtils.getValue( tideTimeSeriesArray, 0 );
		PathWriter.write( writePath, value.toString() );
	}
}
