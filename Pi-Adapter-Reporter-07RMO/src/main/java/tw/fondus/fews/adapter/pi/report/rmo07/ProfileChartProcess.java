package tw.fondus.fews.adapter.pi.report.rmo07;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import strman.Strman;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiSeries;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesArray;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesCollection;
import tw.fondus.commons.util.collection.CollectionUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.math.NumberUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.CrossSectionChartArguments;
import tw.fondus.fews.adapter.pi.report.rmo07.util.WaterStationMetaUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.util.XYAreaRenderer;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.RiverBranchRecord;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.meta.WaterlevelMetaInfo;

/**
 * The process of export profile chart.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
public class ProfileChartProcess extends PiCommandLineExecute {
	private Map<String, WaterlevelMetaInfo> metaInfosMap;
	private Map<String, List<RiverBranchRecord>> branches = CollectionUtils.emptyMapHash();

	public static void main( String[] args ) {
		CrossSectionChartArguments arguments = CrossSectionChartArguments.instance();
		new ProfileChartProcess().execute( args, arguments );
	}

	@Override
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "ProfileChartProcess: Try to generate profile chart file." );
		CrossSectionChartArguments processArguments = this.asArguments( arguments, CrossSectionChartArguments.class );
		int start = processArguments.getIndexStart();
		int end = processArguments.getIndexEnd();
		this.metaInfosMap = WaterStationMetaUtils.readWaterlevelMetaInfo(
				basePath.resolve( "templates/Taiwan_Stations_WaterLevel.csv" ),
				basePath.resolve( "templates/attributes.csv" ) );

		Path templateBranch = basePath.resolve( "branch" );
		PathUtils.list( templateBranch ).forEach( path -> {
			String name = PathUtils.getNameWithoutExtension( path );
			try (FileReader reader = PathReader.newFileReader( path )){
				List<RiverBranchRecord> records = new CsvToBeanBuilder( reader )
						.withFieldAsNull( CSVReaderNullFieldIndicator.EMPTY_SEPARATORS )
						.withType( RiverBranchRecord.class )
						.build()
						.parse();
				this.branches.put( name, records );
			} catch (IOException e) {
				logger.log( LogLevel.ERROR, "ProfileChartProcess: Failed to parse branch records with file: {}.", name, e );
			}
		} );
		PathUtils.list( inputPath ).forEach( path -> {
			PiTimeSeriesCollection collection = GsonMapperRuntime.ISO8601.toBean( PathReader.readString( path ),
					PiTimeSeriesCollection.class );
			PiTimeSeriesCollection zonedCollection = collection.withZone( JodaTimeUtils.UTC8 );
			this.generate( logger, outputPath, zonedCollection, start, end, processArguments );
		} );
	}

	private void generate( PiDiagnosticsLogger logger, Path base, PiTimeSeriesCollection collection, int start, int end,
			CrossSectionChartArguments processArguments ) {
		if ( collection.size() > 0 && collection.get( 0 ).size() >= end && start >= 0 ) {
			PiTimeSeriesCollection subset = collection.subset( start, end );
			subset.stream().map( array -> {
				String locationId = array.getHeader().getLocationId();
				return Optional.ofNullable( this.metaInfosMap.get( locationId ) ).flatMap( metaInfo -> {
					String branch = metaInfo.getBranch();
					return Optional.ofNullable( this.branches.get( branch ) )
							.filter( records -> records.stream()
									.anyMatch( record -> metaInfo.getCrossSectionId()
											.contains( record.getCrossSectionId() ) ) )
							.flatMap( records -> {
								List<WaterlevelMetaInfo> metaInfos = WaterStationMetaUtils
										.searchMetaInfosByBranch( this.metaInfosMap, branch )
										.stream()
										.filter( info -> records.stream()
												.anyMatch( record -> info.getCrossSectionId()
														.contains( record.getCrossSectionId() ) ) )
										.collect( Collectors.toList() );
								logger.log( LogLevel.INFO,
										"ProfileChartProcess: Start to generate profile charts with location id: {} and branch: {}.",
										locationId, branch );
								return this.generateChart( logger, base, array, subset.toMap(), metaInfos,
										processArguments );
							} );
				} );
			} ).flatMap( Optional::stream ).collect( Collectors.toList() );
		} else {
			logger.log( LogLevel.WARN,
					"ProfileChartProcess: Generate use index range {} ~ {} not validate, skip generate process.", start,
					end );
		}
	}
	
	/**
	 * Generate image of profile-section chart.
	 * 
	 * @param base base path
	 * @param array main station array
	 * @param subsetMap map of all subset array
	 * @param metaInfoList meta-info list of main station branch
	 * @return path of image, it's optional
	 */
	private Optional<Path> generateChart( PiDiagnosticsLogger logger, Path base, PiTimeSeriesArray array,
			Map<String, PiTimeSeriesArray> subsetMap, List<WaterlevelMetaInfo> metaInfoList, CrossSectionChartArguments processArguments ) {
		List<BigDecimal> values = array.getCollection()
				.stream()
				.skip( 1 )
				.map( PiSeries::getValue )
				.collect( Collectors.toList() );
		int maxIndex = NumberUtils.findNearestIndex( values, NumberUtils.max( values ), false ) + 1;
		String locationId = array.getHeader().getLocationId();
		String branch = metaInfoList.get( 0 ).getBranch();

		return Optional.ofNullable( this.branches.get( branch ) ).map( records -> {
			Path outputPath = base.resolve( Strman.append( processArguments.getPrefix(), branch, Strings.UNDERLINE,
					locationId, FileType.PNG.getExtension() ) );
			JFreeChart chart = this.createChartAndLayout( array, maxIndex, subsetMap, metaInfoList, records );
			try {
				Plot plot = chart.getPlot();
				XYPlot xyPlot = (XYPlot) plot;
				// set X, Y and value legend font
			    xyPlot.getDomainAxis().setLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
			    xyPlot.getDomainAxis().setTickLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
			    xyPlot.getRangeAxis().setLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
			    xyPlot.getRangeAxis().setTickLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
			    chart.getLegend().setItemFont(new Font("Microsoft JhengHei", Font.BOLD, 11));
				ChartUtils.saveChartAsPNG( outputPath.toFile(), chart, processArguments.getWidth(),
						processArguments.getHeight() );
			} catch (IOException e) {
				logger.log( LogLevel.ERROR,
						"ProfileChartProcess: Generate chart image with location id: {}, branch: {} has something wrong.",
						locationId, branch, e );
			}
			
			return outputPath;
		} );
	}

	private JFreeChart createChartAndLayout( PiTimeSeriesArray array, int maxIndex,
			Map<String, PiTimeSeriesArray> subsetMap, List<WaterlevelMetaInfo> metaInfos,
			List<RiverBranchRecord> records ) {
		JFreeChart chart = ChartFactory.createXYLineChart( null, "距離(m)", "高程 (m)",
				this.createForecastWaterLevelDataset( array, maxIndex, metaInfos, records ) );
		// Legend
		LegendTitle legend = chart.getLegend();
		legend.setPosition( RectangleEdge.TOP );

		// Background color and grid line
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint( Color.WHITE );
		plot.setDomainGridlinePaint( Color.DARK_GRAY );
		plot.setRangeGridlinePaint( Color.DARK_GRAY );

		plot.getRangeAxis().setLowerMargin( 0.0 );

		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setTickMarkPaint( Color.BLACK );
		xAxis.setLowerMargin( 0.0 );
		xAxis.setUpperMargin( 0.0 );
		xAxis.setAutoRangeIncludesZero( false );

		// Set forecast water level line color
		XYAreaRenderer renderer = new XYAreaRenderer();
		renderer.setSeriesPaint( 0, new Color( 107, 230, 255 ) );

		// Set the not reversed value with min bottom.
		BigDecimal minBottom = NumberUtils
				.min( records.stream().map( RiverBranchRecord::getBottom ).collect( Collectors.toList() ) );
		if ( NumberUtils.less( minBottom, BigDecimal.ZERO ) ) {
			renderer.setBottomValue( minBottom.doubleValue() );
		}
		plot.setRenderer( 0, renderer );

		// Set water level observation warning marker
		XYShapeRenderer obsRenderer = new XYShapeRenderer();
		LookupPaintScale ps = new LookupPaintScale( 0.0, 3.0, Color.WHITE );
		ps.add( 1.0, Color.YELLOW );
		ps.add( 2.0, Color.ORANGE );
		ps.add( 3.0, Color.RED );
		obsRenderer.setPaintScale( ps );
		obsRenderer.setDefaultOutlinePaint( Color.BLACK );
		obsRenderer.setDrawOutlines( true );

		plot.setDataset( 3, createObservationMarkerDataset( subsetMap, metaInfos, records ) );
		plot.setRenderer( 3, obsRenderer );

		// Set profile section data
		// If there is a station closes start or end distance, expand five percent of all distance for display annotation
		BigDecimal distance = records.get( 0 )
				.getDistance()
				.subtract( records.get( records.size() - 1 ).getDistance() )
				.abs();
		BigDecimal fivePercentDistance = distance.divide( NumberUtils.create( 20 ), RoundingMode.HALF_UP );
		List<RiverBranchRecord> sortRecords = records.stream()
				.sorted( Comparator.comparing( RiverBranchRecord::getDistance ) )
				.collect( Collectors.toList() );
		if ( IntStream.range( 0, 2 )
				.anyMatch( i -> metaInfos.stream()
						.anyMatch( info -> info.getCrossSectionId()
								.contains( sortRecords.get( i ).getCrossSectionId() ) ) ) ) {
			RiverBranchRecord record = sortRecords.get( 0 );
			RiverBranchRecord expendRecord = RiverBranchRecord.builder()
					.crossSectionId( record.getCrossSectionId() )
					.distance( record.getDistance().subtract( fivePercentDistance ) )
					.bottom( record.getBottom() )
					.left( record.getLeft() )
					.right( record.getRight() )
					.build();
			sortRecords.add( 0, expendRecord );
		}
		
		if ( IntStream.range( sortRecords.size() - 3, sortRecords.size() )
				.anyMatch( i -> metaInfos.stream()
						.anyMatch( info -> info.getCrossSectionId()
								.contains( sortRecords.get( i ).getCrossSectionId() ) ) ) ) {
			RiverBranchRecord record = sortRecords.get( sortRecords.size() - 1 );
			RiverBranchRecord expendRecord = RiverBranchRecord.builder()
					.crossSectionId( record.getCrossSectionId() )
					.distance( record.getDistance().add( fivePercentDistance ) )
					.bottom( record.getBottom() )
					.left( record.getLeft() )
					.right( record.getRight() )
					.build();
			sortRecords.add( expendRecord );
		}
		
		XYSeries profileSectionSeries = new XYSeries( "底床" );
		sortRecords.forEach( record -> profileSectionSeries.add( record.getDistance(), record.getBottom() ) );
		XYSeriesCollection profileDataset = new XYSeriesCollection( profileSectionSeries );

		XYAreaRenderer areaRenderer = new XYAreaRenderer();
		if ( NumberUtils.less( minBottom, BigDecimal.ZERO ) ) {
			areaRenderer.setBottomValue( minBottom.doubleValue() );
		}
		areaRenderer.setOutline( true );
		areaRenderer.setDefaultOutlinePaint( Color.DARK_GRAY );
		areaRenderer.setSeriesPaint( 0, new Color( 255, 178, 102 ) );
		plot.setDataset( 2, profileDataset );
		plot.setRenderer( 2, areaRenderer );

		// Set left and right bank line
		XYSeries rightSeries = new XYSeries( "右堤岸" );
		records.forEach( record -> rightSeries.add( record.getDistance(), record.getRight() ) );
		XYSeries leftSeries = new XYSeries( "左堤岸" );
		records.forEach( record -> leftSeries.add( record.getDistance(), record.getLeft() ) );

		XYSeriesCollection bankDataset = new XYSeriesCollection();
		bankDataset.addSeries( rightSeries );
		bankDataset.addSeries( leftSeries );

		XYLineAndShapeRenderer bankRenderer = new XYLineAndShapeRenderer();
		bankRenderer.setSeriesPaint( 0, Color.ORANGE );
		bankRenderer.setSeriesShapesVisible( 0, false );
		bankRenderer.setSeriesStroke( 0, new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
				new float[] { 21, 9, 3, 9 }, 10 ) );
		bankRenderer.setSeriesPaint( 1, Color.PINK );
		bankRenderer.setSeriesShapesVisible( 1, false );
		bankRenderer.setSeriesStroke( 1, new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
				new float[] { 21, 9, 3, 9 }, 10 ) );
		plot.setDataset( 1, bankDataset );
		plot.setRenderer( 1, bankRenderer );

		// Set water level vertical line
		WaterlevelMetaInfo arrayMetaInfo = metaInfos.stream()
				.filter( info -> info.getId().equals( array.getHeader().getLocationId() ) )
				.findFirst()
				.get();
		BigDecimal arrayDistance = records.stream()
				.filter( record -> arrayMetaInfo.getCrossSectionId().contains( record.getCrossSectionId() ) )
				.findFirst()
				.get()
				.getDistance();
		final Marker target = new ValueMarker( arrayDistance.doubleValue() );
		target.setPaint( new Color( 96, 96, 96 ) );
		target.setLabel( arrayMetaInfo.getCrossSectionId() );
		target.setLabelFont( new Font( "Time New Roman", Font.BOLD, 11 ) );
		target.setLabelBackgroundColor( new Color( 0, 0, 0, 0 ) );
		target.setLabelAnchor( RectangleAnchor.TOP );
		target.setLabelTextAnchor( TextAnchor.TOP_CENTER );
		target.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
				new float[] { 21, 9, 3, 9 }, 10 ) );
		plot.addDomainMarker( target );

		// Change observation legend
		LegendItemCollection collection = plot.getLegendItems();
		LegendItem item = collection.get( 4 );
		LegendItem newItem = new LegendItem( item.getLabel(), item.getDescription(), item.getToolTipText(),
				item.getURLText(), new Ellipse2D.Double( 10, 10, 10, 10 ), Color.YELLOW, item.getOutlineStroke(),
				Color.BLACK );
		LegendItemCollection newCollection = new LegendItemCollection();
		newCollection.add( collection.get( 0 ) );
		newCollection.add( collection.get( 1 ) );
		newCollection.add( collection.get( 2 ) );
		newCollection.add( collection.get( 3 ) );
		newCollection.add( newItem );
		plot.setFixedLegendItems( newCollection );

		plot.setDatasetRenderingOrder( DatasetRenderingOrder.FORWARD );
		createStationAnnotation( chart, subsetMap, metaInfos, records );

		return chart;
	}

	/**
	 * Create station value and name annotation.
	 * 
	 * @param chart main chart
	 * @param subsetMap map of subset PiTimeSeriesArray
	 * @param metaInfoList meta info list of branch
	 * @param records branch records
	 */
	private void createStationAnnotation( JFreeChart chart, Map<String, PiTimeSeriesArray> subsetMap,
			List<WaterlevelMetaInfo> metaInfoList, List<RiverBranchRecord> records ) {
		XYPlot plot = chart.getXYPlot();
		IntStream.range( 0, metaInfoList.size() ).forEach( i -> {
			WaterlevelMetaInfo metaInfo = metaInfoList.get( i );
			BigDecimal obsValue = subsetMap.get( metaInfo.getId() ).get( 0 ).getValue();
			BigDecimal distance = records.stream()
					.filter( r -> metaInfo.getCrossSectionId().contains( r.getCrossSectionId() ) )
					.findFirst()
					.get()
					.getDistance();

			final XYTextAnnotation nameAnnotation = new XYTextAnnotation( metaInfo.getName(), distance.doubleValue(),
					obsValue.doubleValue() );
			nameAnnotation.setFont( new Font( "Noto Sans TC", Font.BOLD, 15 ) );
			nameAnnotation.setTextAnchor( TextAnchor.BOTTOM_CENTER );
			plot.addAnnotation( nameAnnotation );

			final XYTextAnnotation valueAnnotation = new XYTextAnnotation( obsValue.toString(), distance.doubleValue(),
					obsValue.doubleValue() );
			valueAnnotation.setFont( new Font( "Time New Roman", Font.BOLD, 15 ) );
			valueAnnotation.setTextAnchor( TextAnchor.TOP_CENTER );
			plot.addAnnotation( valueAnnotation );
		} );
	}

	/**
	 * Create observation warning marker dataset.
	 * 
	 * @param subsetMap map of subset PiTimeSeriesArray
	 * @param metaInfoList meta info list of branch
	 * @param records branch records
	 * @return dataset of observation marker
	 */
	private XYZDataset createObservationMarkerDataset( Map<String, PiTimeSeriesArray> subsetMap,
			List<WaterlevelMetaInfo> metaInfoList, List<RiverBranchRecord> records ) {
		DefaultXYZDataset dataset = new DefaultXYZDataset();
		double[] x = new double[metaInfoList.size()];
		double[] y = new double[metaInfoList.size()];
		double[] z = new double[metaInfoList.size()];
		IntStream.range( 0, metaInfoList.size() ).forEach( i -> {
			WaterlevelMetaInfo metaInfo = metaInfoList.get( i );
			BigDecimal obsValue = subsetMap.get( metaInfo.getId() ).get( 0 ).getValue();
			RiverBranchRecord record = records.stream()
					.filter( r -> metaInfo.getCrossSectionId().contains( r.getCrossSectionId() ) )
					.findFirst()
					.get();
			x[i] = record.getDistance().doubleValue();
			y[i] = obsValue.doubleValue();
			z[i] = 0;

			if ( metaInfo.isNotMissingWarningLevel3()
					&& NumberUtils.greaterEquals( obsValue, metaInfo.getWarningLevel3() ) ) {
				z[i] = 1;
			}
			if ( metaInfo.isNotMissingWarningLevel2()
					&& NumberUtils.greaterEquals( obsValue, metaInfo.getWarningLevel2() ) ) {
				z[i] = 2;
			}
			if ( metaInfo.isNotMissingWarningLevel1()
					&& NumberUtils.greaterEquals( obsValue, metaInfo.getWarningLevel1() ) ) {
				z[i] = 3;
			}
		} );

		double[][] series = new double[][] { x, y, z };
		dataset.addSeries( "觀測水位", series );
		return dataset;
	}

	/**
	 * Create forecast water level dataset.
	 * 
	 * @param array main station PiTimeSeriesArray
	 * @param maxIndex maximum value index of forecast
	 * @param metaInfoList meta info list of branch
	 * @param records branch records
	 * @return forecast water level dataset
	 */
	private XYDataset createForecastWaterLevelDataset( PiTimeSeriesArray array, int maxIndex,
			List<WaterlevelMetaInfo> metaInfoList, List<RiverBranchRecord> records ) {
		WaterlevelMetaInfo metaInfo = metaInfoList.stream()
				.filter( info -> info.getId().equals( array.getHeader().getLocationId() ) )
				.findAny()
				.get();
		RiverBranchRecord stationRecord = records.stream()
				.filter( record -> metaInfo.getCrossSectionId().contains( record.getCrossSectionId() ) )
				.findFirst()
				.get();
		BigDecimal waterDeep = array.get( maxIndex ).getValue().subtract( stationRecord.getBottom() );

		XYSeries waterlevelSeries = new XYSeries( Strman.append( "預測水位_T", String.valueOf( maxIndex ) ) );
		records.forEach( record -> waterlevelSeries.add( record.getDistance(), record.getBottom().add( waterDeep ) ) );
		return new XYSeriesCollection( waterlevelSeries );
	}
}
