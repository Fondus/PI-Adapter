package tw.fondus.fews.adapter.pi.report.rmo07;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Coordinate;

import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.json.util.gson.GsonMapperRuntime;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesArray;
import tw.fondus.commons.rest.pi.json.model.timeseries.PiTimeSeriesCollection;
import tw.fondus.commons.rest.pi.json.util.timeseries.PiSeriesUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathReader;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.report.rmo07.argument.CrossSectionChartArguments;
import tw.fondus.fews.adapter.pi.report.rmo07.util.CrossSectionUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.util.WaterStationMetaUtils;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.CrossSection;
import tw.fondus.fews.adapter.pi.report.rmo07.vo.meta.WaterlevelMetaInfo;

/**
 * The process of export cross-section chart.
 * 
 * @author Brad Chen (Original Author)
 * @author Chao (Refactored by)
 *
 */
public class CrossSectionChartProcess extends PiCommandLineExecute {
	private Map<String, WaterlevelMetaInfo> metaInfos;

	public static void main( String[] args ) {
		CrossSectionChartArguments arguments = CrossSectionChartArguments.instance();
		new CrossSectionChartProcess().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		logger.log( LogLevel.INFO, "CrossSectionChartProcess: Try to generate cross section chart file." );
		CrossSectionChartArguments processArguments = this.asArguments( arguments, CrossSectionChartArguments.class );
		int start = processArguments.getIndexStart();
		int end = processArguments.getIndexEnd();
		metaInfos = WaterStationMetaUtils.readWaterlevelMetaInfo(
				basePath.resolve( "templates/Taiwan_Stations_WaterLevel.csv" ),
				basePath.resolve( "templates/attributes.csv" ) );
		Path templateCrossSection = basePath.resolve( "crosssection" );
		PathUtils.list( inputPath ).forEach( path -> {
			PiTimeSeriesCollection collection = GsonMapperRuntime.ISO8601.toBean( PathReader.readString( path ),
					PiTimeSeriesCollection.class );
			PiTimeSeriesCollection zonedCollection = collection.withZone( JodaTimeUtils.UTC8 );
			this.generate( logger, outputPath, zonedCollection, start, end, templateCrossSection, 20,
					processArguments.getWidth(), processArguments.getHeight() );
		} );
	}

	private void generate( PiDiagnosticsLogger logger, Path base, PiTimeSeriesCollection collection,
			int start, int end, Path templatePath, int timeZero, int width, int height ) {
		if ( collection.size() > 0 && collection.get( 0 ).size() >= end && start >= 0 ) {
			PiTimeSeriesCollection subset = collection.subset( start, end );
			subset.forEach( array -> this.generateChart( logger, base, array, templatePath, timeZero, width, height ) );
		} else {
			logger.log( LogLevel.WARN,
					"CrossSectionChartProcess: Generate use index range {} ~ {} not validate, skip generate process.", start,
					end );
		}
	}
	
	@SuppressWarnings( "all" )
	private Path generateChart( PiDiagnosticsLogger logger, Path base, PiTimeSeriesArray array, Path templatePath, int timeZero, int width, int height ){
		String locationId = array.getHeader().getLocationId();
		Path outputPath = base.resolve( locationId + FileType.PNG.getExtension() );
		logger.log( LogLevel.INFO, "CrossSectionChartProcess: Try to generate cross-section chart with location id: {}.", locationId );
		Map<String, CrossSection> sectionMap = CrossSectionUtils.readCrossSection( templatePath );
		Optional<CrossSection> optionalCrossSection = Optional.ofNullable( sectionMap.get( locationId ) );
		Optional<WaterlevelMetaInfo> optionalMetaInfo = Optional.ofNullable( metaInfos.get( locationId ) );

		logger.log( LogLevel.INFO, "CrossSectionChartProcess: Start to generate cross-section chart with location id: {}.", locationId );
		logger.log( LogLevel.INFO, "CrossSectionChartProcess: Initialize the chart with location Id: {}.", locationId );
		JFreeChart chart = createChartAndLayout( logger, array, timeZero );
		optionalMetaInfo.ifPresentOrElse( metaInfo -> this.applyWarningLines( logger, chart, metaInfo ),
				() -> logger.log( LogLevel.WARN, "CrossSectionChartProcess: Meta-Info not exists with location id: {}, skip apply process.", locationId ));
		optionalCrossSection.ifPresentOrElse( crossSection -> this.applyCrossSection( logger, chart, crossSection ),
				() -> logger.log( LogLevel.WARN, "CrossSectionChartProcess: CrossSection not exists with location id: {}, skip apply process.", locationId ) );
		try {
			Plot plot = chart.getPlot();
			XYPlot xyPlot = (XYPlot) plot;
			// set X and Y legend font
			xyPlot.getDomainAxis().setLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
			xyPlot.getDomainAxis().setTickLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
			xyPlot.getRangeAxis().setLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
			xyPlot.getRangeAxis().setTickLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 12));

			ChartUtils.saveChartAsPNG( outputPath.toFile(), chart, width, height );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "CrossSectionChartProcess: Generate chart image with location id: {} has something wrong.",
					locationId, e );
		}
		return outputPath;
	}
	
	/**
	 * Create chart and apply basic layout.
	 *
	 * @param array pi-series array
	 * @return chart
	 */
	private JFreeChart createChartAndLayout( PiDiagnosticsLogger logger, PiTimeSeriesArray array, int timeZero ){
		JFreeChart chart = ChartFactory.createTimeSeriesChart( null, "時間", "高程 (m)",
				this.createTimeSeriesDataset( logger, array, timeZero ), true, true, false );
		// Legend
		LegendTitle legend = chart.getLegend();
		legend.setPosition( RectangleEdge.TOP );

		// Background color and grid line
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint( Color.WHITE );
		plot.setDomainGridlinePaint( Color.DARK_GRAY );
		plot.setRangeGridlinePaint( Color.DARK_GRAY );

		// X-axis data format and fit axis left and right
		DateAxis xAxis = (DateAxis) plot.getDomainAxis();
		xAxis.setTickMarkPaint( Color.BLACK );
		xAxis.setLowerMargin( 0.0 );
		xAxis.setUpperMargin( 0.0 );
		xAxis.setDateFormatOverride( new SimpleDateFormat( "MM/dd HH:mm" ) );

		// Set timeseries line shape and color
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesShape( 0, new Ellipse2D.Double( -3, -3, 6, 6 ) );
		renderer.setSeriesPaint( 0, Color.BLUE );
		renderer.setLegendTextFont( 0, new Font("Microsoft JhengHei", Font.BOLD, 12) );
		renderer.setSeriesShape( 1, new Ellipse2D.Double( -3, -3, 6, 6 ) );
		renderer.setSeriesPaint( 1, new Color( 51, 153, 255 ) );
		renderer.setLegendTextFont( 1, new Font("Microsoft JhengHei", Font.BOLD, 12) );
		plot.setRenderer( 0, renderer );
		return chart;
	}
	
	/**
	 * Apply the cross-section area into chart from cross-section.
	 *
	 * @param chart chart
	 * @param crossSection cross-section vo
	 */
	private void applyCrossSection( PiDiagnosticsLogger logger, JFreeChart chart, CrossSection crossSection ){
		logger.log( LogLevel.INFO, "CrossSectionChartService: Try to apply the cross section to chart with location Id: {}.", crossSection.getId() );

		XYPlot plot = chart.getXYPlot();
		List<Coordinate> coordinates = crossSection.getCoordinates();
		if ( coordinates.size() > 0 ){
			logger.log( LogLevel.INFO, "CrossSectionChartService: Apply the cross section to chart with location Id: {}.", crossSection.getId() );

			XYSeries crossSectionSeries = new XYSeries( "斷面" );
			coordinates.forEach( coordinate -> crossSectionSeries.add( coordinate.getX(), coordinate.getY() ) );

			XYSeriesCollection dataset = new XYSeriesCollection( crossSectionSeries );

			NumberAxis xAxis = new NumberAxis( "斷面" );
			xAxis.setLowerMargin( 0 );
			xAxis.setUpperMargin( 0 );
			xAxis.setAutoRangeIncludesZero( false ); // Because some cross-section not start at zero
			XYAreaRenderer renderer = new XYAreaRenderer();
			renderer.setOutline( true );
			renderer.setDefaultOutlinePaint( Color.DARK_GRAY );
			renderer.setSeriesPaint( 0, new Color( 255, 178, 102 ) );
			renderer.setLegendTextFont( 0, new Font("Microsoft JhengHei", Font.BOLD, 12) );
			plot.setDomainAxis( 1, xAxis );
			plot.mapDatasetToDomainAxis( 1, 1 );
			plot.setDataset( 1, dataset );
			plot.setRenderer( 1, renderer );
			plot.setDatasetRenderingOrder( DatasetRenderingOrder.REVERSE );
			plot.getDomainAxis(1 ).setVisible( false ); // Disable cross section axis label
		}
	}

	/**
	 * Apply the warning levels into chart from meta-info.
	 *
	 * @param chart chart
	 * @param metaInfo meta-info vo
	 */
	private void applyWarningLines( PiDiagnosticsLogger logger, JFreeChart chart, WaterlevelMetaInfo metaInfo ){
		logger.log( LogLevel.INFO, "CrossSectionChartService: Try to apply the warning level to chart with location Id: {}.", metaInfo.getId() );
		XYPlot plot = chart.getXYPlot();
		if ( metaInfo.isNotMissingWarningLevel1() ){
			logger.log( LogLevel.DEBUG, "CrossSectionChartService: Apply the warning level 1 to chart with location Id: {}.", metaInfo.getId() );
			final Marker target = new ValueMarker( metaInfo.getWarningLevel1().doubleValue() );
			target.setPaint( Color.RED );
			target.setLabel( "一級警戒" );
			target.setLabelBackgroundColor( new Color( 0, 0, 0, 0 ) );
			target.setLabelAnchor( RectangleAnchor.TOP_RIGHT );
			target.setLabelTextAnchor( TextAnchor.BOTTOM_RIGHT );
			target.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 0, new float[] { 21, 9, 3, 9 }, 10 ) );
			plot.addRangeMarker( target );
		}

		if ( metaInfo.isNotMissingWarningLevel2() ){
			logger.log( LogLevel.DEBUG, "CrossSectionChartService: Apply the warning level 2 to chart with location Id: {}.", metaInfo.getId() );
			final Marker target = new ValueMarker( metaInfo.getWarningLevel2().doubleValue() );
			target.setPaint( Color.ORANGE );
			target.setLabel( "二級警戒" );
			target.setLabelBackgroundColor( new Color( 0, 0, 0, 0 ) );
			target.setLabelAnchor( RectangleAnchor.TOP_RIGHT );
			target.setLabelTextAnchor( TextAnchor.BOTTOM_RIGHT );
			target.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 0, new float[] { 21, 9, 3, 9 }, 10 ) );
			plot.addRangeMarker( target );
		}

		if ( metaInfo.isNotMissingWarningLevel3() ){
			logger.log( LogLevel.DEBUG, "CrossSectionChartService: Apply the warning level 3 to chart with location Id: {}.", metaInfo.getId() );
			final Marker target = new ValueMarker( metaInfo.getWarningLevel3().doubleValue() );
			target.setPaint( Color.YELLOW );
			target.setLabel( "三級警戒" );
			target.setLabelBackgroundColor( new Color( 0, 0, 0, 0 ) );
			target.setLabelAnchor( RectangleAnchor.TOP_RIGHT );
			target.setLabelTextAnchor( TextAnchor.BOTTOM_RIGHT );
			target.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND, 0, new float[] { 18, 9, 3, 9 }, 10 ) );
			plot.addRangeMarker( target );
		}
	}

	/**
	 * Create X Y dataset from pi-series array.
	 *
	 * @param array pi-series array
	 * @return x y dataset
	 */
	private XYDataset createTimeSeriesDataset( PiDiagnosticsLogger logger, PiTimeSeriesArray array, int timeZero ){
		logger.log( LogLevel.DEBUG, "CrossSectionChartService: Initialize the time-series data will used to generate chart with location Id: {}.", array.getHeader().getLocationId() );
		TimeSeries observation = new TimeSeries( "觀測水位" );
		TimeSeries forecasting = new TimeSeries( "預測水位" );

		DateTime startTime = array.get( 0 ).getTime();
		Hour hour = new Hour( startTime.toDate() );

		for ( int i = 0; i < array.size(); i++ ) {
			if ( i <= timeZero ){
				observation.add( hour, PiSeriesUtils.getValue( array, i, null ) );
			}

			if ( i >= timeZero ){
				forecasting.add( hour, PiSeriesUtils.getValue( array, i, null ) );
			}
			hour = (Hour) hour.next();
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries( observation );
		dataset.addSeries( forecasting );
		return dataset;
	}

}
