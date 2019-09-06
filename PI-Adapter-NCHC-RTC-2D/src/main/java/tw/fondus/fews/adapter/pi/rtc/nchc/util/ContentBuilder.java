package tw.fondus.fews.adapter.pi.rtc.nchc.util;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
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
		sj.add( Strman.append( String.valueOf( forecast ),
				"                                   ! NTIME_VAL (NUMBER OF LEAD TIME)" ) );
		sj.add( Strman.append( String.valueOf( numberOfStation ), "                                   ! NTGAGE_VAL" ) );
		sj.add( "SUMMARY_FILES_INP_GAGES_DATA.TXT    ! FILENAME_IN_GAGE_EST_OBS" );
		sj.add( "SUMMARY_FILES_OUT_GAGES_DATA.TXT    ! FILENAME_OUT_GAGE_EST_OBS" );
		sj.add( "INPUT_PARS_RTEC_TSKF.TXT  INPUT_DATA_RTEC_TSKF.TXT         ! FILENAME_PAR_RTEC" );

		return sj.toString();
	}

	/**
	 * Build model input data from station of water level (observation and
	 * simulation).
	 * 
	 * @param similationTimeSeriesArray
	 * @param observationTimeSeriesArray
	 * @return
	 */
	public static String buildInputGauges( TimeSeriesArrays similationTimeSeriesArrays,
			TimeSeriesArrays observationTimeSeriesArrays ) {
		String locationInfo = " ! LOCATION OF GAGE ";
		String geoInfo = " (X_TM, Y_TM, ELEVATION)";
		String dataInfo = "                  ! SIMULATED WH, OBSERVED WH";
		String missingValue = "-999.0";
		String dataEnd = "	-999    -999                 ! INDICATOR OF STOP READING DATA";
		StringJoiner sj = new StringJoiner( StringUtils.BREAKLINE );

		IntStream.range( 0, observationTimeSeriesArrays.size() ).forEach( timeSeries -> {
			sj.add( Strman.append(
					String.valueOf( observationTimeSeriesArrays.get( timeSeries ).getHeader().getGeometry().getX( 0 ) ),
					StringUtils.SPACE_WHITE,
					String.valueOf( observationTimeSeriesArrays.get( timeSeries ).getHeader().getGeometry().getY( 0 ) ),
					StringUtils.SPACE_WHITE,
					String.valueOf( observationTimeSeriesArrays.get( timeSeries ).getHeader().getGeometry().getZ( 0 ) ),
					locationInfo, String.valueOf( timeSeries + 1 ), geoInfo ) );
			IntStream.range( 0, similationTimeSeriesArrays.get( timeSeries ).size() ).forEach( data -> {
				if ( data == 0 ) {
					sj.add( Strman.append( StringUtils.SPACE_WHITE,
							getDataString( similationTimeSeriesArrays.get( timeSeries ).getValue( data ) ),
							StringUtils.SPACE_WHITE,
							getDataString( observationTimeSeriesArrays.get( timeSeries ).getValue( data ) ),
							StringUtils.SPACE_WHITE, dataInfo ) );
				} else {
					if ( data >= observationTimeSeriesArrays.get( timeSeries ).size() ) {
						sj.add( Strman.append( StringUtils.SPACE_WHITE,
								getDataString( similationTimeSeriesArrays.get( timeSeries ).getValue( data ) ),
								StringUtils.SPACE_WHITE, missingValue ) );
					} else {
						sj.add( Strman.append( StringUtils.SPACE_WHITE,
								getDataString( similationTimeSeriesArrays.get( timeSeries ).getValue( data ) ),
								StringUtils.SPACE_WHITE,
								getDataString( observationTimeSeriesArrays.get( timeSeries ).getValue( data ) ) ) );
					}
				}
			} );

			sj.add( dataEnd );
			sj.add( StringUtils.BLANK );
		} );

		return sj.toString();
	}

	private static String getDataString( float value ) {
		String stringData = String.valueOf( value );
		if ( stringData.equals( "NaN" ) ) {
			stringData = "0";
			return stringData;
		} else {
			return stringData;
		}
	}
}
