package tw.fondus.fews.adapter.pi.rtc.nchc.util;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.util.coordinate.CoordinatePoint;
import tw.fondus.commons.util.coordinate.CoordinateUtils;
import tw.fondus.commons.util.string.StringUtils;

/**
 * Build model input data content.
 * 
 * @author Chao
 *
 */
public class ContentBuilder {

	/**
	 * Build model input parameter.
	 * 
	 * @param numberOfStation
	 * @param forecast
	 * @return
	 */
	public static String buildInputCorr( int numberOfStation, int forecast ) {
		StringJoiner sj = new StringJoiner( StringUtils.BREAKLINE );
		sj.add( "3                                   ! NTYPE_SIM_WH" );
		sj.add( Strman.append( String.valueOf( forecast ),"                                   ! NTIME_VAL (NUMBER OF LEAD TIME)" ));
		sj.add( Strman.append( String.valueOf( numberOfStation ), "                                   ! NTGAGE_VAL" ) );
		sj.add( "SUMMARY_FILES_INP_GAGES_DATA.TXT    ! FILENAME_IN_GAGE_EST_OBS" );
		sj.add( "SUMMARY_FILES_OUT_GAGES_DATA.TXT    ! FILENAME_OUT_GAGE_EST_OBS" );
		sj.add( "INPUT_PARS_RTEC_TSKF.TXT  INPUT_DATA_RTEC_TSKF.TXT         ! FILENAME_PAR_RTEC" );

		return sj.toString();
	}

	/**
	 * Build model input data from station of water level (observation and simulation).
	 * 
	 * @param similationTimeSeriesArray
	 * @param observationTimeSeriesArray
	 * @return
	 */
	public static String buildInputGauges( TimeSeriesArray[] similationTimeSeriesArray,
			TimeSeriesArray[] observationTimeSeriesArray ) {
		String locationInfo = " ! LOCATION OF GAGE ";
		String geoInfo = " (X_TM, Y_TM, ELEVATION)";
		String dataInfo = "                  ! SIMULATED WH, OBSERVED WH";
		String missingValue = "-999.0";
		String dataEnd = "	-999    -999                 ! INDICATOR OF STOP READING DATA";
		StringJoiner sj = new StringJoiner( StringUtils.BREAKLINE );

		IntStream.range( 0, observationTimeSeriesArray.length ).forEach( timeSeries -> {
			CoordinatePoint point = CoordinateUtils.transformWGS84ToTWD97(
					observationTimeSeriesArray[timeSeries].getHeader().getGeometry().getX( 0 ),
					observationTimeSeriesArray[timeSeries].getHeader().getGeometry().getY( 0 ) );
			sj.add( Strman.append( String.valueOf( point.getX() ), StringUtils.SPACE_WHITE,
					String.valueOf( point.getY() ), StringUtils.SPACE_WHITE,
					String.valueOf( observationTimeSeriesArray[timeSeries].getHeader().getGeometry().getZ( 0 ) ),
					locationInfo, String.valueOf( timeSeries + 1 ), geoInfo ) );
			IntStream.range( 0, similationTimeSeriesArray[timeSeries].size() ).forEach( data -> {
				if ( data == 0 ) {
					sj.add( Strman.append( StringUtils.SPACE_WHITE,
							String.valueOf( similationTimeSeriesArray[timeSeries].getValue( data ) ),
							StringUtils.SPACE_WHITE,
							String.valueOf( observationTimeSeriesArray[timeSeries].getValue( data ) ),
							StringUtils.SPACE_WHITE, dataInfo ) );
				} else {
					if ( data >= observationTimeSeriesArray[timeSeries].size() ) {
						sj.add( Strman.append( StringUtils.SPACE_WHITE,
								String.valueOf( similationTimeSeriesArray[timeSeries].getValue( data ) ),
								StringUtils.SPACE_WHITE, missingValue ) );
					} else {
						sj.add( Strman.append( StringUtils.SPACE_WHITE,
								String.valueOf( similationTimeSeriesArray[timeSeries].getValue( data ) ),
								StringUtils.SPACE_WHITE,
								String.valueOf( observationTimeSeriesArray[timeSeries].getValue( data ) ) ) );
					}
				}
			} );
			
			sj.add( dataEnd );
			sj.add( StringUtils.BLANK );
		} );

		return sj.toString();
	}
}
