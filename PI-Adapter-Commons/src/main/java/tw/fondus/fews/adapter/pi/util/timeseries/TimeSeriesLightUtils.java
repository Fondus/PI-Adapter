package tw.fondus.fews.adapter.pi.util.timeseries;

import com.google.common.base.Preconditions;
import nl.wldelft.fews.pi.PiTimeSeriesParser;
import nl.wldelft.fews.pi.PiTimeSeriesSerializer;
import nl.wldelft.util.FileUtils;
import nl.wldelft.util.timeseries.DefaultTimeSeriesHeader;
import nl.wldelft.util.timeseries.IrregularTimeStep;
import nl.wldelft.util.timeseries.ParameterType;
import nl.wldelft.util.timeseries.SimpleEquidistantTimeStep;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContent;
import nl.wldelft.util.timeseries.SimpleTimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesArray;
import nl.wldelft.util.timeseries.TimeSeriesArrays;
import nl.wldelft.util.timeseries.TimeSeriesContentHandler;
import nl.wldelft.util.timeseries.TimeSeriesHeader;
import nl.wldelft.util.timeseries.TimeStep;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A light version of FondUS SDK time-series tools, <br/>
 * it's used to read, write PI-XML base on FEWS library. 
 * 
 * @author Brad Chen
 *
 */
public class TimeSeriesLightUtils {
	/**
	 * The default missing value. 
	 */
	public static final BigDecimal MISSING_VALUE = new BigDecimal( "-999" );

	/**
	 * The TimeSeriesContentHandler type of accumulated.
	 */
	public static final ParameterType TYPE_ACCUMULATED = ParameterType.ACCUMULATIVE;

	/**
	 * The TimeSeriesContentHandler type of instantaneous.
	 */
	public static final ParameterType TYPE_INSTANTANEOUS = ParameterType.INSTANTANEOUS;
	
	private TimeSeriesLightUtils() {}

	/**
	 * Use to for each time series array to avoid 2018.02 generic type.
	 *
	 * @param timeSeriesArrays time series array
	 * @param consumer consumer
	 * @since 3.0.0
	 */
	@SuppressWarnings( "rawtypes" )
	public static void forEach( TimeSeriesArrays timeSeriesArrays, Consumer<TimeSeriesArray> consumer ){
		Stream.of( timeSeriesArrays.toArray() ).forEach( consumer );
	}

	/**
	 * Create the time series content handler.
	 *
	 * @return time series handler
	 * @since 3.0.0
	 */
	public static SimpleTimeSeriesContentHandler seriesHandler(){
		return new SimpleTimeSeriesContentHandler();
	}

	/**
	 * Create the modifiable time series header handler.
	 *
	 * @return time series header handler
	 * @since 3.0.0
	 */
	public static DefaultTimeSeriesHeader headerHandler(){
		return new DefaultTimeSeriesHeader();
	}

	/**
	 * Create the unmodifiable time series header handler.
	 *
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param timeStep time step
	 * @param type parameter type
	 * @return time series header handler
	 * @since 3.0.0
	 */
	public static TimeSeriesHeader headerHandler( String locationId, String parameterId, String unitId, TimeStep timeStep, ParameterType type ){
		Preconditions.checkNotNull( type, "TimeSeriesLightUtils: type." );

		DefaultTimeSeriesHeader header = headerHandler();
		header.setParameterId( parameterId );
		header.setUnit( unitId );
		header.setLocationId( locationId );
		header.setTimeStep( timeStep );
		header.setParameterType( type );
		return header;
	}
	
	/**
	 * Read time series arrays from thr PI-XML file.
	 * 
	 * @param path path
	 * @return time series arrays
	 * @throws IOException has IO problem
	 */
	@Deprecated
	@SuppressWarnings( "rawtypes" )
	public static TimeSeriesArrays readPiTimeSeries( Path path ) throws IOException {
		return read( path );
	}
	
	/**
	 * Read time series arrays from thr PI-XML file.
	 * 
	 * @param file file
	 * @return time series arrays
	 * @throws IOException has IO problem
	 */
	@Deprecated
	@SuppressWarnings( "rawtypes" )
	public static TimeSeriesArrays readPiTimeSeries( File file ) throws IOException {
		Preconditions.checkNotNull( file, "TimeSeriesLightUtils: file." );
		return read( file.toPath() );
	}

	/**
	 * Read time series arrays from thr PI-XML file.
	 *
	 * @param path path
	 * @return time series arrays
	 * @throws IOException has IO problem
	 * @since 3.0.0
	 */
	@SuppressWarnings( "rawtypes" )
	public static TimeSeriesArrays read( Path path )throws IOException{
		Preconditions.checkNotNull( path, "TimeSeriesLightUtils: path." );
		if ( !Files.exists( path ) ){
			throw new IOException( "TimeSeriesLightUtils: Path is not exists!" );
		}

		if ( Files.isDirectory( path ) ){
			throw new IOException( "TimeSeriesLightUtils: Path is not a file !" );
		}

		SimpleTimeSeriesContentHandler contentHandler = new SimpleTimeSeriesContentHandler();
		PiTimeSeriesParser piTimeSeriesParser = new PiTimeSeriesParser();
		FileUtils.parse( path.toFile(), piTimeSeriesParser, contentHandler );
		TimeSeriesArrays timeSeries = contentHandler.getTimeSeriesArrays();
		if ( timeSeries.size() == 0 ) {
			throw new IOException( "TimeSeriesLightUtils: No time series found in file in the model input files" );
		} else {
			return timeSeries;
		}
	}

	/**
	 * Add the time series header information into content handler with instantaneous type and irregular step.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @return time series content handler
	 */
	@Deprecated
	public static TimeSeriesContentHandler fillPiTimeSeriesHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStep(), TYPE_INSTANTANEOUS );
	}

	/**
	 * Add the time series header information into content handler.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param stepMillis millis second
	 * @return time series content handler
	 */
	@Deprecated
	public static TimeSeriesContentHandler fillPiTimeSeriesHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, long stepMillis ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStep( stepMillis ), TYPE_INSTANTANEOUS );
	}

	/**
	 * Add the time series header information into content handler.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param stepMillis millis second
	 * @param type parameter type
	 * @return time series content handler
	 */
	@Deprecated
	public static TimeSeriesContentHandler fillPiTimeSeriesHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, long stepMillis, ParameterType type ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStep( stepMillis ), type );
	}

	/**
	 * Add the time series header information into content handler with instantaneous type and default hour step.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStep(), TYPE_INSTANTANEOUS );
	}

	/**
	 * Add the time series header information into content handler with instantaneous type.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param millis millis second
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, long millis ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStep( millis ), TYPE_INSTANTANEOUS );
	}

	/**
	 * Add the time series header information into content handler.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param millis millis second
	 * @param type parameter type
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, long millis, ParameterType type ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStep( millis ), type );
	}

	/**
	 * Add the time series header information into content handler.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param timeStep time step
	 * @param type parameter type
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, TimeStep timeStep, ParameterType type ) {
		TimeSeriesHeader header = headerHandler( locationId, parameterId, unitId, timeStep, type );
		return addHeader( contentHandler, header );
	}

	/**
	 * Add the time series header into content handler.
	 *
	 * @param contentHandler contentHandler
	 * @param header time series header
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeader( TimeSeriesContentHandler contentHandler, TimeSeriesHeader header ) {
		Preconditions.checkNotNull( contentHandler, "TimeSeriesLightUtils: contentHandler" );
		Preconditions.checkNotNull( header, "TimeSeriesLightUtils: header" );
		contentHandler.setNewTimeSeriesHeader( header );
		return contentHandler;
	}

	/**
	 * Add the time series header information into content handler with irregular step.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @return time series content handler
	 */
	@Deprecated
	public static TimeSeriesContentHandler fillPiTimeSeriesHeaderIrregular( TimeSeriesContentHandler contentHandler,
			String locationId, String parameterId, String unitId ) {
		return fillPiTimeSeriesHeaderIrregular( contentHandler, locationId, parameterId, unitId, TYPE_INSTANTANEOUS );
	}

	/**
	 * Add the time series header information into content handler with irregular step.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param type parameter type
	 * @return time series content handler
	 */
	@Deprecated
	public static TimeSeriesContentHandler fillPiTimeSeriesHeaderIrregular( TimeSeriesContentHandler contentHandler,
			String locationId, String parameterId, String unitId, ParameterType type ) {
		return addHeaderIrregular( contentHandler, locationId, parameterId, unitId, type );
	}

	/**
	 * Add the time series header information into content handler with instantaneous type and irregular step.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeaderIrregular( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId ) {
		return addHeaderIrregular( contentHandler, locationId, parameterId, unitId, TYPE_INSTANTANEOUS );
	}

	/**
	 * Add the time series header information into content handler with irregular step.
	 *
	 * @param contentHandler content handler
	 * @param locationId location id
	 * @param parameterId parameter id
	 * @param unitId unit id
	 * @param type parameter type
	 * @return time series content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addHeaderIrregular( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, ParameterType type ) {
		return addHeader( contentHandler, locationId, parameterId, unitId, getTimeStepIrregular(), type );
	}
	
	/**
	 * Apply Delft-FEWS PiTimeSeries current fields.
	 * 
	 * @param contentHandler content handler
	 * @param time time in millis second
	 * @param value value
	 * @return content handler
	 */
	@Deprecated
	public static TimeSeriesContentHandler addPiTimeSeriesValue( TimeSeriesContentHandler contentHandler, long time, float value ){
		return addValue( contentHandler, time, new BigDecimal( String.valueOf( value ) ) );
	}

	/**
	 * Add value into time series content handler.
	 *
	 * @param contentHandler content handler
	 * @param time time in millis second
	 * @param value value
	 * @return content handler
	 * @since 3.0.0
	 */
	public static TimeSeriesContentHandler addValue( TimeSeriesContentHandler contentHandler, long time, BigDecimal value ){
		Preconditions.checkNotNull( contentHandler, "TimeSeriesLightUtils: contentHandler." );
		Preconditions.checkNotNull( value, "TimeSeriesLightUtils: value." );
		contentHandler.setTime( time );
		contentHandler.setValue( value.floatValue() );
		contentHandler.applyCurrentFields();
		return contentHandler;
	}
	
	/**
	 * Write series data into PI-XML with output path with default missing value.
	 * 
	 * @param contentHandler content handler
	 * @param outputFileName full string path of output PI-XML file
	 * @throws IOException has IO problem
	 */
	@Deprecated
	public static void writePIFile( SimpleTimeSeriesContentHandler contentHandler, String outputFileName )
			throws IOException {
		write( contentHandler, Paths.get( outputFileName ) );
	}

	/**
	 * Write series data into PI-XML with output path with missing value.
	 *
	 * @param contentHandler content handler
	 * @param outputFileName full string path of output PI-XML file
	 * @param missingValue default missing value in PI-XML file
	 * @throws IOException has IO problem
	 */
	@Deprecated
	public static void writePIFile( SimpleTimeSeriesContentHandler contentHandler, String outputFileName,
			BigDecimal missingValue ) throws IOException {
		write( contentHandler, Paths.get( outputFileName ), missingValue );
	}

	/**
	 * Write series data into PI-XML with output path with default missing value.
	 *
	 * @param contentHandler content handler
	 * @param outputPath output path of PI-XML file
	 * @return wrote PI-XML file path
	 * @throws IOException has IO problem
	 * @since 3.0.0
	 */
	public static Path write( SimpleTimeSeriesContentHandler contentHandler, Path outputPath ) throws IOException {
		return write( contentHandler, outputPath, MISSING_VALUE );
	}

	/**
	 * Write series data into PI-XML with output path with missing value.
	 *
	 * @param contentHandler content handler
	 * @param outputPath output path of PI-XML file
	 * @param missingValue default missing value in PI-XML file
	 * @return wrote PI-XML file path
	 * @throws IOException has IO problem
	 * @since 3.0.0
	 */
	@SuppressWarnings( "rawtypes" )
	public static Path write( SimpleTimeSeriesContentHandler contentHandler, Path outputPath,
			BigDecimal missingValue ) throws IOException {
		Preconditions.checkNotNull( contentHandler, "TimeSeriesUtils: contentHandler." );

		TimeSeriesArrays timeSeriesArrays = contentHandler.getTimeSeriesArrays();
		SimpleTimeSeriesContent content = new SimpleTimeSeriesContent( timeSeriesArrays );
		content.setOmitMissingValues( true );
		content.setDefaultMissingValue( missingValue.floatValue() );

		FileUtils.write( outputPath.toFile(), content, new PiTimeSeriesSerializer() );
		return outputPath;
	}

	/**
	 * Return time series array value with index, if is missing value, return with default missing value.
	 *
	 * @param timeSeriesArray time series array
	 * @param index index
	 * @return value at array index
	 */
	@SuppressWarnings( "rawtypes" )
	public static BigDecimal getValue( TimeSeriesArray timeSeriesArray, int index ) {
		return getValue( timeSeriesArray, index, MISSING_VALUE );
	}

	/**
	 * Return time series array value with index, if is missing value, return with specified missing value.
	 *
	 * @param timeSeriesArray time series array
	 * @param index index
	 * @param missingValue missing value
	 * @return value at array index
	 */
	@SuppressWarnings( "rawtypes" )
	public static BigDecimal getValue( TimeSeriesArray timeSeriesArray, int index, BigDecimal missingValue ) {
		Preconditions.checkNotNull( timeSeriesArray, "TimeSeriesUtils: timeSeriesArray." );
		Preconditions.checkElementIndex( index, timeSeriesArray.size() );
		return timeSeriesArray.isMissingValue( index ) ? missingValue : new BigDecimal( String.valueOf( timeSeriesArray.getValue( index ) ) );
	}
	
	/**
	 * Get TimeStep with 1 hour.
	 * 
	 * @return time step
	 */
	public static TimeStep getTimeStep() {
		return SimpleEquidistantTimeStep.getInstance( 3600000 );
	}
	
	/**
	 * Get TimeStep with stepMillis.
	 * 
	 * @param stepMillis millis second
	 * @return time step
	 */
	public static TimeStep getTimeStep( long stepMillis ) {
		return SimpleEquidistantTimeStep.getInstance( stepMillis );
	}

	/**
	 * Get Irregular TimeStep.
	 *
	 * @return time step
	 */
	public static TimeStep getTimeStepIrregular() {
		return IrregularTimeStep.INSTANCE;
	}
}
