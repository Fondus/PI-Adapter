package tw.fondus.fews.adapter.pi.report.rmo07;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesArray;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesCollection;
import tw.fondus.commons.rest.pi.json.util.timeseries.PiSeriesUtils;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.commons.util.time.TimeFormats;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.ProcessArguments;
import tw.fondus.fews.adapter.pi.report.rmo07.util.WaterStationMetaUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.InstantStatistics;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.meta.WaterlevelMetaInfo;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.report.WaterlevelGroup;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.report.WaterlevelRecord;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.report.WaterlevelReport;

/**
 * The process of export excel report.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
public class ExcelReportProcess extends PiCommandLineExecute {
	private final static String REPORT_HEADER_TIME_FORMAT = "MM/dd HH";
	private Map<String, WaterlevelMetaInfo> metaInfos;
	private int index02End = 1;
	private int index06End = 5;
	private int index12End = 11;
	private int index24End = 23;
	private int index07To24Start = 6;

	public static void main( String[] args ) {
		ProcessArguments arguments = ProcessArguments.instance();
		new ExcelReportProcess().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "ExcelReportProcess: Try to generate excel report file." );
		ProcessArguments processArguments = this.asArguments( arguments, ProcessArguments.class );
		int start = processArguments.getIndexStart();
		int end = processArguments.getIndexEnd();
		metaInfos = WaterStationMetaUtils.readWaterlevelMetaInfo(
				basePath.resolve( "templates/Taiwan_Stations_WaterLevel.csv" ),
				basePath.resolve( "templates/attributes.csv" ) );
		Path templateReport = basePath.resolve( "templates/report.xlsx" );
		PathUtils.list( inputPath ).forEach( path -> {
			PiTimeSeriesCollection collection = GsonMapperRuntime.ISO8601.toBean( PathReader.readString( path ),
					PiTimeSeriesCollection.class );
			PiTimeSeriesCollection zonedCollection = collection.withZone( JodaTimeUtils.UTC8 );
			DateTime timeZero = zonedCollection.getTimeZero();
			String fileName = Strman.append( PathUtils.getNameWithoutExtension( path ), Strings.UNDERLINE,
					processArguments.getPrefix(), Strings.UNDERLINE,
					JodaTimeUtils.toString( timeZero, TimeFormats.YMDH_UNDIVIDED, timeZero.getZone() ),
					Strings.UNDERLINE, processArguments.getSuffix(), FileType.EXCEL_XLSX.getExtension() );

			// Check time step is 30min or not
			int timeStepMinutes = Minutes.minutesBetween(collection.get(0).get(0).getTime(), collection.get(0).get(1).getTime()).getMinutes();
			if (timeStepMinutes == 30) {
				index02End = 2;
				index06End = 11;
				index12End = 23;
				index24End = 47;
				index07To24Start = 12;
			}
			this.generate(logger, outputPath, zonedCollection, fileName, start, end, templateReport, processArguments.getTimeZeroIndex(),
					processArguments.getSpecialCases());
		});
	}

	private Path generate( PiDiagnosticsLogger logger, Path base, PiTimeSeriesCollection collection, String fileName,
			int start, int end, Path templatePath, int timeZero, List<String> specialCases ) {
		Path outputPath = base.resolve( fileName );
		if ( collection.size() > 0 && collection.get( 0 ).size() >= end && start >= 0 ) {
			PiTimeSeriesCollection subset = collection.subset( start, end );
			PiTimeSeriesArray array = subset.get( 0 );
			// statistics
			List<InstantStatistics> statistics = instants( logger, subset, timeZero, specialCases );

			// Merge statistics with meta-info to records
			logger.log( LogLevel.INFO, "ExcelReportService: Try to merged statistics with meta-info to records." );
			List<WaterlevelRecord> rangeMax01To02 = mergeStatisticsWithMetaInfoToRecords( statistics,
					InstantStatistics::getTimeRangeMax01To02, InstantStatistics::getRangeMax01To02 );

			List<WaterlevelRecord> rangeMax01To06 = mergeStatisticsWithMetaInfoToRecords( statistics,
					InstantStatistics::getTimeRangeMax01To06, InstantStatistics::getRangeMax01To06 );

			List<WaterlevelRecord> rangeMax01To12 = mergeStatisticsWithMetaInfoToRecords( statistics,
					InstantStatistics::getTimeRangeMax01To12, InstantStatistics::getRangeMax01To12 );

			List<WaterlevelRecord> rangeMax01To24 = mergeStatisticsWithMetaInfoToRecords( statistics,
					InstantStatistics::getTimeRangeMax01To24, InstantStatistics::getRangeMax01To24 );

			List<WaterlevelRecord> rangeMax07To24 = mergeStatisticsWithMetaInfoToRecords( statistics,
					InstantStatistics::getTimeRangeMax07To24, InstantStatistics::getRangeMax07To24 );

			logger.log( LogLevel.INFO,
					"ExcelReportService: Success merged {} records with statistics with time range T01 ~ T02.",
					rangeMax01To02.size() );
			logger.log( LogLevel.INFO,
					"ExcelReportService: Success merged {} records with statistics with time range T01 ~ T06.",
					rangeMax01To06.size() );
			logger.log( LogLevel.INFO,
					"ExcelReportService: Success merged {} records with statistics with time range T01 ~ T12.",
					rangeMax01To12.size() );
			logger.log( LogLevel.INFO,
					"ExcelReportService: Success merged {} records with statistics with time range T01 ~ T24.",
					rangeMax01To24.size() );
			logger.log( LogLevel.INFO,
					"ExcelReportService: Success merged {} records with statistics with time range T07 ~ T24.",
					rangeMax07To24.size() );

			// Create report
			logger.log( LogLevel.INFO, "ExcelReportService: Prepare to create report entities with records." );
			List<WaterlevelReport> reports = CollectionUtils.emptyListArray();
			reports.add( createReportEntities( logger, rangeMax01To02, WaterlevelReport.NAMES[0], array.get( 0 ).getTime(),
					array.get(index02End).getTime() ) );
			reports.add( createReportEntities( logger, rangeMax01To06, WaterlevelReport.NAMES[1], array.get( 0 ).getTime(),
					array.get(index06End).getTime() ) );
			reports.add( createReportEntities( logger, rangeMax01To12, WaterlevelReport.NAMES[2], array.get( 0 ).getTime(),
					array.get(index12End).getTime() ) );
			reports.add( createReportEntities( logger, rangeMax07To24, WaterlevelReport.NAMES[3], array.get(index07To24Start).getTime(),
					array.get(index24End).getTime() ) );
			reports.add( createReportEntities( logger, rangeMax01To24, WaterlevelReport.NAMES[4], array.get( 0 ).getTime(),
					array.get(index24End).getTime() ) );

			// Generate Excel Report
			generateReport( logger, outputPath, reports, templatePath );
		} else {
			logger.log( LogLevel.WARN,
					"ExcelReportService: Generate use index range {} ~ {} not validate, skip generate process.", start,
					end );
		}
		return outputPath;
	}

	/**
	 * Generate excel report with collection of report entity.
	 *
	 * @param outputPath output path
	 * @param reports collection of report entity
	 * @throws IOException
	 */
	private static void generateReport( PiDiagnosticsLogger logger, Path outputPath, List<WaterlevelReport> reports,
			Path templatePath ) {
		logger.log( LogLevel.INFO, "ExcelReportService: Try to generate excel reports with entities." );
		if ( reports.isEmpty() ) {
			logger.log( LogLevel.INFO, "ExcelReportService: Reports entities size is zero, skip generate process." );
		} else {
			if ( PathUtils.isExists( templatePath ) ) {
				try {
					try (InputStream is = PathReader.readInputStream( templatePath ) ) {
						try (OutputStream os = Files.newOutputStream( outputPath ) ) {
							Context context = new Context();
							context.putVar( "reports", reports );
							JxlsHelper.getInstance().processTemplate( is, os, context );
						}
					}
				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "ExcelReportService: Failed to read template report from inner resource." );
				}
			} else {
				throw new IllegalStateException("ExcelReportService: Failed to read template report because inner resource not exists." );
			}
		}
	}

	/**
	 * Create one kind report entities with merged records.
	 *
	 * @param records merged records of one kind report
	 * @param name name of report
	 * @param from from time of report
	 * @param to to time of report
	 * @return report
	 */
	private WaterlevelReport createReportEntities( PiDiagnosticsLogger logger, List<WaterlevelRecord> records, String name, DateTime from,
			DateTime to ) {
		logger.log( LogLevel.INFO, "ExcelReportService: Create report: {} entities with records.", name );
		Map<String, List<WaterlevelRecord>> branchGroups = records.stream()
				.collect( Collectors.groupingBy( WaterlevelRecord::getBranch ) );

		List<WaterlevelGroup> groups = branchGroups.values()
				.stream()
				.map( groupRecords -> WaterlevelGroup.builder().records( groupRecords ).build() )
				.collect( Collectors.toList() );
		return WaterlevelReport.builder()
				.name( name )
				.from( JodaTimeUtils.toString( from, REPORT_HEADER_TIME_FORMAT, JodaTimeUtils.UTC8 ) )
				.to( JodaTimeUtils.toString( to, REPORT_HEADER_TIME_FORMAT, JodaTimeUtils.UTC8 ) )
				.groups( groups )
				.build();
	}

	/**
	 * The logic used merge statistics with meta-info to warning records of one
	 * kind report.
	 *
	 * @param statistics statistics
	 * @param timeFunction the function to select time used to make report
	 * @param valueFunction the function to select value used to make report
	 * @return merged records of one kind report
	 */
	private List<WaterlevelRecord> mergeStatisticsWithMetaInfoToRecords( List<InstantStatistics> statistics,
			Function<InstantStatistics, String> timeFunction, Function<InstantStatistics, BigDecimal> valueFunction ) {
		List<WaterlevelRecord> records = CollectionUtils.emptyListArray();

		statistics.forEach( statistic -> {
			String locationId = statistic.getId();
			Optional<WaterlevelMetaInfo> optional = Optional.ofNullable( metaInfos.get( locationId ) );
			optional.ifPresent( metaInfo -> {
				WaterlevelRecord record = WaterlevelRecord.builder()
						.branch( metaInfo.getBranch() )
						.town( metaInfo.getTown() )
						.name( metaInfo.getName() )
						.build();

				BigDecimal value = valueFunction.apply( statistic );
				String time = timeFunction.apply( statistic );

				BigDecimal warning3 = metaInfo.getWarningLevel3();
				BigDecimal warning2 = metaInfo.getWarningLevel2();
				BigDecimal warning1 = metaInfo.getWarningLevel1();
				BigDecimal topLevel = metaInfo.getTopLevel();

				// Default missing
				if ( metaInfo.isMissingWarningLevel3() ) {
					record.setWarningLevel3( "－" );
				}

				if ( metaInfo.isMissingWarningLevel2() ) {
					record.setWarningLevel2( "－" );
				}

				if ( metaInfo.isMissingWarningLevel1() ) {
					record.setWarningLevel1( "－" );
				}

				if ( metaInfo.isMissingTopLevel() ) {
					record.setOverDike( "－" );
				}

				if ( metaInfo.isMissingWarningLevel3() ) {
					if ( metaInfo.isMissingWarningLevel2() ) {
						if ( metaInfo.isMissingWarningLevel1() ) {
							if ( metaInfo.isNotMissingTopLevel() && NumberUtils.greaterEquals( value, topLevel ) ) {
								record.setOverDike( "1" );
								record.setValue( value );
								record.setTime( time );
							} else {
								record.setWarningNon( "1" );
							}
						} else {
							// Not Missing Level 1
							if ( metaInfo.isNotMissingTopLevel() && NumberUtils.greaterEquals( value, warning1 )
									&& NumberUtils.less( value, topLevel ) ) {
								record.setWarningLevel1( "1" );
								record.setValue( value );
								record.setTime( time );
							} else if ( metaInfo.isNotMissingTopLevel()
									&& NumberUtils.greaterEquals( value, topLevel ) ) {
								record.setOverDike( "1" );
								record.setValue( value );
								record.setTime( time );
							} else if ( metaInfo.isMissingTopLevel() && NumberUtils.greaterEquals( value, warning1 ) ) {
								record.setWarningLevel1( "1" );
								record.setValue( value );
								record.setTime( time );
							}
						}
					} else {
						// Not Missing Level 2
						if ( NumberUtils.less( value, warning2 ) ) {
							record.setWarningNon( "1" );
						} else if ( (metaInfo.isNotMissingWarningLevel1()
								&& NumberUtils.greaterEquals( value, warning2 ) && NumberUtils.less( value, warning1 ))
								|| (metaInfo.isMissingWarningLevel1()
										&& NumberUtils.greaterEquals( value, warning2 )) ) {
							record.setWarningLevel2( "1" );
							record.setValue( value );
							record.setTime( time );
						} else if ( (metaInfo.isNotMissingWarningLevel1() && metaInfo.isNotMissingTopLevel()
								&& NumberUtils.greaterEquals( value, warning1 ) && NumberUtils.less( value, topLevel ))
								|| (metaInfo.isNotMissingWarningLevel1() && metaInfo.isMissingTopLevel()
										&& NumberUtils.greaterEquals( value, warning1 )) ) {
							record.setWarningLevel1( "1" );
							record.setValue( value );
							record.setTime( time );
						} else if ( metaInfo.isNotMissingTopLevel() && NumberUtils.greaterEquals( value, topLevel ) ) {
							record.setOverDike( "1" );
							record.setValue( value );
							record.setTime( time );
						}
					}
				} else {
					// Not Missing Level 3
					if ( NumberUtils.less( value, warning3 ) ) {
						record.setWarningNon( "1" );
					} else if ( (metaInfo.isNotMissingWarningLevel2() && NumberUtils.greaterEquals( value, warning3 )
							&& NumberUtils.less( value, warning2 ))
							|| (metaInfo.isMissingWarningLevel2() && NumberUtils.greaterEquals( value, warning3 )) ) {
						record.setWarningLevel3( "1" );
						record.setValue( value );
						record.setTime( time );
					} else if ( (metaInfo.isNotMissingWarningLevel1() && NumberUtils.greaterEquals( value, warning2 )
							&& NumberUtils.less( value, warning1 ))
							|| (metaInfo.isMissingWarningLevel1() && NumberUtils.greaterEquals( value, warning2 )) ) {
						record.setWarningLevel2( "1" );
						record.setValue( value );
						record.setTime( time );
					} else if ( (metaInfo.isNotMissingWarningLevel1() && metaInfo.isNotMissingTopLevel()
							&& NumberUtils.greaterEquals( value, warning1 ) && NumberUtils.less( value, topLevel ))
							|| (metaInfo.isNotMissingWarningLevel1() && metaInfo.isMissingTopLevel()
									&& NumberUtils.greaterEquals( value, warning1 )) ) {
						record.setWarningLevel1( "1" );
						record.setValue( value );
						record.setTime( time );
					} else if ( metaInfo.isNotMissingTopLevel() && NumberUtils.greaterEquals( value, topLevel ) ) {
						record.setOverDike( "1" );
						record.setValue( value );
						record.setTime( time );
					}
				}
				records.add( record );
			} );
		} );
		return records;
	}

	private List<InstantStatistics> instants( PiDiagnosticsLogger logger, PiTimeSeriesCollection collection, int timeZero,
			List<String> specialCases ) {
		logger.log( LogLevel.INFO, "ExcelReportService: Instants statistics the windows max of pi-timeseries collection." );
		return collection.stream().map( array -> {
			String locationId = array.getHeader().getLocationId();
			if ( specialCases.stream().anyMatch( specialCase -> specialCase.equals( locationId ) ) ) {
				logger.log( LogLevel.INFO, "ExcelReportService: Statistics with special cases: {}.", locationId );
				return InstantStatistics.builder()
						.id( locationId )
						.rangeMax01To02( PiSeriesUtils.getValue( array, timeZero ) )
						.rangeMax01To06( PiSeriesUtils.getValue( array, timeZero ) )
						.rangeMax01To12( PiSeriesUtils.getValue( array, timeZero ) )
						.rangeMax01To24( PiSeriesUtils.getValue( array, timeZero ) )
						.rangeMax07To24( PiSeriesUtils.getValue( array, timeZero ) )
						.timeRangeMax01To02( formatTime( array.get( timeZero ).getTime() ) )
						.timeRangeMax01To06( formatTime( array.get( timeZero ).getTime() ) )
						.timeRangeMax01To12( formatTime( array.get( timeZero ).getTime() ) )
						.timeRangeMax01To24( formatTime( array.get( timeZero ).getTime() ) )
						.timeRangeMax07To24( formatTime( array.get( timeZero ).getTime() ) )
						.build();
			} else {
				logger.log( LogLevel.INFO, "ExcelReportService: Statistics with normal cases: {}.", locationId );
				BigDecimal rangeMax01To02 = NumberUtils.max( PiSeriesUtils.getValuesClosed( array, 0, index02End) );
				BigDecimal rangeMax01To06 = NumberUtils.max( PiSeriesUtils.getValuesClosed( array, 0, index06End) );
				BigDecimal rangeMax01To12 = NumberUtils.max( PiSeriesUtils.getValuesClosed( array, 0, index12End) );
				BigDecimal rangeMax01To24 = NumberUtils.max( PiSeriesUtils.getValuesClosed( array, 0, index24End) );
				BigDecimal rangeMax07To24 = NumberUtils.max( PiSeriesUtils.getValuesClosed( array, index07To24Start, index24End) );

				int rangeMaxIndex01To02 = PiSeriesUtils.findValueIndexAtRangeClosed( array, 0, index02End, rangeMax01To02 )
						.orElse( -1 );
				int rangeMaxIndex01To06 = PiSeriesUtils.findValueIndexAtRangeClosed( array, 0, index06End, rangeMax01To06 )
						.orElse( -1 );
				int rangeMaxIndex01To12 = PiSeriesUtils.findValueIndexAtRangeClosed( array, 0, index12End, rangeMax01To12 )
						.orElse( -1 );
				int rangeMaxIndex01To24 = PiSeriesUtils.findValueIndexAtRangeClosed( array, 0, index24End, rangeMax01To24 )
						.orElse( -1 );
				int rangeMaxIndex07To24 = PiSeriesUtils.findValueIndexAtRangeClosed( array, index07To24Start, index24End, rangeMax07To24 )
						.orElse( -1 );

				return InstantStatistics.builder()
						.id( locationId )
						.rangeMax01To02( rangeMax01To02 )
						.rangeMax01To06( rangeMax01To06 )
						.rangeMax01To12( rangeMax01To12 )
						.rangeMax01To24( rangeMax01To24 )
						.rangeMax07To24( rangeMax07To24 )
						.timeRangeMax01To02( formatTime( array.get( rangeMaxIndex01To02 ).getTime() ) )
						.timeRangeMax01To06( formatTime( array.get( rangeMaxIndex01To06 ).getTime() ) )
						.timeRangeMax01To12( formatTime( array.get( rangeMaxIndex01To12 ).getTime() ) )
						.timeRangeMax01To24( formatTime( array.get( rangeMaxIndex01To24 ).getTime() ) )
						.timeRangeMax07To24( formatTime( array.get( rangeMaxIndex07To24 ).getTime() ) )
						.build();
			}

		} ).collect( Collectors.toList() );
	}

	/**
	 * Format the report time.
	 *
	 * @param dateTime time
	 * @return format string
	 */
	private String formatTime( DateTime dateTime ) {
		return JodaTimeUtils.toString( dateTime, TimeFormats.YMDHMS, JodaTimeUtils.UTC8 );
	}

}
