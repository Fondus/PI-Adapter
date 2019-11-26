package tw.fondus.fews.adapter.pi.util.timeseries;

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
import nl.wldelft.util.timeseries.TimeStep;
import tw.fondus.commons.cli.util.Prevalidated;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

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
	 * Read PiXML file and transform to Delft-FEWS PiTimeSeries.
	 * 
	 * @param file
	 * @return
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public static TimeSeriesArrays readPiTimeSeries( Path path ) throws OperationNotSupportedException, IOException {
		Prevalidated.checkNonNull( path, "TimeSeriesLightUtils: path." );
		return readPiTimeSeries( path.toFile() );
	}
	
	/**
	 * Read PiXML file and transform to Delft-FEWS PiTimeSeries.
	 * 
	 * @param file
	 * @return
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public static TimeSeriesArrays readPiTimeSeries( File file ) throws IOException, OperationNotSupportedException {
		Prevalidated.checkNonNull( file, "TimeSeriesLightUtils: file." );
		SimpleTimeSeriesContentHandler contentHandler = new SimpleTimeSeriesContentHandler();
		PiTimeSeriesParser piTimeSeriesParser = new PiTimeSeriesParser();
		FileUtils.parse( file, piTimeSeriesParser, contentHandler );
		TimeSeriesArrays timeSeries = contentHandler.getTimeSeriesArrays();
		if ( timeSeries.size() == 0 ) {
			throw new OperationNotSupportedException( "No time series found in file in the model input files" );
		} else {
			return timeSeries;
		}
	}
	
	/**
	 * Apply Delft-FEWS PiTimeSeries Header
	 * 
	 * @param contentHandler
	 * @param locationId
	 * @param parameterId
	 * @param unitId
	 * @return
	 */
	public static TimeSeriesContentHandler fillPiTimeSeriesHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId ) {
		return fillPiTimeSeriesHeader( contentHandler, locationId, parameterId, unitId, 3600000 );
	}

	/**
	 * Apply Delft-FEWS PiTimeSeries Header with specified stepMillis.
	 * 
	 * @param contentHandler
	 * @param locationId
	 * @param parameterId
	 * @param unitId
	 * @param stepMillis
	 * @return
	 */
	public static TimeSeriesContentHandler fillPiTimeSeriesHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, long stepMillis ) {
		return fillPiTimeSeriesHeader( contentHandler, locationId, parameterId, unitId, stepMillis, TYPE_INSTANTANEOUS );
	}
	
	/**
	 * Apply Delft-FEWS PiTimeSeries Header with specified type.
	 * 
	 * @param contentHandler
	 * @param locationId
	 * @param parameterId
	 * @param unitId
	 * @param stepMillis
	 * @param type
	 * @return
	 */
	public static TimeSeriesContentHandler fillPiTimeSeriesHeader( TimeSeriesContentHandler contentHandler, String locationId,
			String parameterId, String unitId, long stepMillis, ParameterType type ) {
		Prevalidated.checkNonNull( contentHandler, "TimeSeriesLightUtils: contentHandler." );
		
		DefaultTimeSeriesHeader header = new DefaultTimeSeriesHeader();
		header.setParameterId( parameterId );
		header.setUnit( unitId );
		header.setLocationId( locationId );
		header.setTimeStep( getTimeStep( stepMillis ) );
		header.setParameterType( type );
		contentHandler.setNewTimeSeriesHeader( header );
		return contentHandler;
	}

	/**
	 * Apply Delft-FEWS PiTimeSeries Irregular Header.
	 *
	 * @param contentHandler
	 * @param locationId
	 * @param parameterId
	 * @param unitId
	 * @return
	 */
	public static TimeSeriesContentHandler fillPiTimeSeriesHeaderIrregular( TimeSeriesContentHandler contentHandler,
			String locationId, String parameterId, String unitId ) {
		return fillPiTimeSeriesHeaderIrregular( contentHandler, locationId, parameterId, unitId, TYPE_INSTANTANEOUS );
	}

	/**
	 * Apply Delft-FEWS PiTimeSeries Irregular Header with specified type.
	 *
	 * @param contentHandler
	 * @param locationId
	 * @param parameterId
	 * @param unitId
	 * @param type
	 * @return
	 */
	public static TimeSeriesContentHandler fillPiTimeSeriesHeaderIrregular( TimeSeriesContentHandler contentHandler,
			String locationId, String parameterId, String unitId, ParameterType type ) {
		Prevalidated.checkNonNull( contentHandler, "TimeSeriesLightUtils: contentHandler." );

		DefaultTimeSeriesHeader header = new DefaultTimeSeriesHeader();
		header.setParameterId( parameterId );
		header.setUnit( unitId );
		header.setLocationId( locationId );
		header.setTimeStep( getTimeStepIrregular() );
		header.setParameterType( type );
		contentHandler.setNewTimeSeriesHeader( header );
		return contentHandler;
	}
	
	/**
	 * Apply Delft-FEWS PiTimeSeries current fields.
	 * 
	 * @param contentHandler
	 * @param time
	 * @param value
	 * @return
	 */
	public static TimeSeriesContentHandler addPiTimeSeriesValue( TimeSeriesContentHandler contentHandler, long time, float value ){
		Prevalidated.checkNonNull( contentHandler, "TimeSeriesLightUtils: contentHandler." );
		contentHandler.setTime( time );
		contentHandler.setValue( value );
		contentHandler.applyCurrentFields();
		return contentHandler;
	}
	
	/**
	 * Write PiXML to target path with default missing value.
	 * 
	 * @param contentHandler
	 * @param outputFileName
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void writePIFile( SimpleTimeSeriesContentHandler contentHandler, String outputFileName )
			throws InterruptedException, IOException {
		writePIFile( contentHandler, outputFileName, -999F );
	}

	/**
	 * Write PiXML to target path with missing value.
	 * 
	 * @param contentHandler
	 * @param outputFileName
	 * @param missingValue
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void writePIFile( SimpleTimeSeriesContentHandler contentHandler, String outputFileName,
			float missingValue ) throws InterruptedException, IOException {
		Prevalidated.checkNonNull( contentHandler, "TimeSeriesUtils: contentHandler." );
		
		TimeSeriesArrays timeSeriesArrays = contentHandler.getTimeSeriesArrays();
		SimpleTimeSeriesContent content = new SimpleTimeSeriesContent( timeSeriesArrays );
		content.setOmitMissingValues( true );
		content.setDefaultMissingValue( missingValue );

		File outputPiFile = new File( outputFileName );
		FileUtils.write( outputPiFile, content, new PiTimeSeriesSerializer() );
	}
	
	/**
	 * Return TimeSeriesArray value with index, if is missing value, return with
	 * default missing value.
	 * 
	 * @param timeSeriesArray
	 * @param index
	 * @return
	 */
	public static float getValue( TimeSeriesArray timeSeriesArray, int index ) {
		return getValue( timeSeriesArray, index, -999F );
	}

	/**
	 * Return TimeSeriesArray value with index, if is missing value, return with
	 * specified missing value.
	 * 
	 * @param timeSeriesArray
	 * @param index
	 * @param missingValue
	 * @return
	 */
	public static float getValue( TimeSeriesArray timeSeriesArray, int index, float missingValue ) {
		Prevalidated.checkNonNull( timeSeriesArray, "TimeSeriesUtils: timeSeriesArray." );
		Prevalidated.checkElementIndex( index, timeSeriesArray.size() );
		return timeSeriesArray.isMissingValue( index ) ? missingValue : timeSeriesArray.getValue( index );
	}
	
	/**
	 * Get TimeStep with 1 hour.
	 * 
	 * @return
	 */
	public static TimeStep getTimeStep() {
		return SimpleEquidistantTimeStep.getInstance( 3600000 );
	}
	
	/**
	 * Get TimeStep with stepMillis.
	 * 
	 * @param stepMillis
	 * @return
	 */
	public static TimeStep getTimeStep( long stepMillis ) {
		return SimpleEquidistantTimeStep.getInstance( stepMillis );
	}

	/**
	 * Get Irregular TimeStep.
	 *
	 * @return
	 */
	public static TimeStep getTimeStepIrregular() {
		return IrregularTimeStep.INSTANCE;
	}
}
