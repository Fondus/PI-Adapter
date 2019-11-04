package tw.fondus.fews.adapter.pi.hecras.ntou;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.io.FileUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import nl.wldelft.util.timeseries.TimeSeriesArray;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.hecras.ntou.argument.ProcessArguments;
import tw.fondus.fews.adapter.pi.hecras.ntou.property.MappingProperties;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * Model Pre-Adapter for running NTOU HEC-RAS model from Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class HECRASPreAdapter extends PiCommandLineExecute {

	public static void main( String[] args ) {
		ProcessArguments arguments = new ProcessArguments();
		new HECRASPreAdapter().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		ProcessArguments processArguments = (ProcessArguments) arguments;

		Path templatePath = Prevalidated.checkExists(
				basePath.resolve( processArguments.getTemplateDir() ).resolve( processArguments.getCaseName() ),
				"HECRASPreAdapter: Can not find the directory of case template." );

		Path executablePath = Prevalidated.checkExists( basePath.resolve( processArguments.getExecutableDir() ),
				"HECRASPreAdapter: Can not find the directory of case executable." );

		try {
			PathUtils.copyDirectory( templatePath, executablePath, true );

			TimeSeriesArray flowTimeSeriesArray = TimeSeriesLightUtils
					.readPiTimeSeries( inputPath.resolve( processArguments.getInputs().get( 0 ) ) )
					.get( 0 );
			TimeSeriesArray tideTimeSeriesArray = TimeSeriesLightUtils
					.readPiTimeSeries( inputPath.resolve( processArguments.getInputs().get( 1 ) ) )
					.get( 0 );
			TimeSeriesArray leftRainfallTimeSeriesArray = TimeSeriesLightUtils
					.readPiTimeSeries( inputPath.resolve( processArguments.getInputs().get( 2 ) ) )
					.get( 0 );
			TimeSeriesArray rightRainfallTimeSeriesArray = TimeSeriesLightUtils
					.readPiTimeSeries( inputPath.resolve( processArguments.getInputs().get( 3 ) ) )
					.get( 0 );

			Path p03Path = executablePath.resolve( processArguments.getOutputs().get( 0 ) );
			DateTimeFormatter dtf = DateTimeFormat.forPattern( "ddMMMyyyy,hh" );
			List<String> p03List = Files.readAllLines( p03Path );
			p03List = p03List.stream()
					.map( line -> line
							.replace( MappingProperties.getProperty( MappingProperties.SIMULATE_DATE ),
									Strman.append(
											dtf.withLocale( Locale.ENGLISH )
													.print( rightRainfallTimeSeriesArray.getStartTime() ),
											StringUtils.COMMA,
											dtf.withLocale( Locale.ENGLISH )
													.print( rightRainfallTimeSeriesArray.getEndTime() ) ) ) )
					.collect( Collectors.toList() );
			FileUtils.writeStringToFile( p03Path.toFile(),
					p03List.stream().collect( Collectors.joining( StringUtils.BREAKLINE ) ) );

			Path b03Path = executablePath.resolve( processArguments.getOutputs().get( 1 ) );
			List<String> b03List = Files.readAllLines( b03Path );
			b03List = b03List.stream()
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.TIME_STEPS ),
							Strman.append( String.valueOf( rightRainfallTimeSeriesArray.size() - 1 ), ".00000" ) ) )
					.collect( Collectors.toList() );
			FileUtils.writeStringToFile( b03Path.toFile(),
					b03List.stream().collect( Collectors.joining( StringUtils.BREAKLINE ) ) );

			Path u01Path = executablePath.resolve( processArguments.getOutputs().get( 2 ) );
			List<String> u01List = Files.readAllLines( u01Path );
			u01List = u01List.stream()
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.UPSTREAM_FLOW_SIZE ),
							String.valueOf( flowTimeSeriesArray.size() ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.UPSTREAM_FLOW ),
							this.formatTimeSeriesData( flowTimeSeriesArray ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.UPSTREAM_TIDE_SIZE ),
							String.valueOf( tideTimeSeriesArray.size() ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.UPSTREAM_TIDE ),
							this.formatTimeSeriesData( tideTimeSeriesArray ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.RAINFALL_LEFT_SIZE ),
							String.valueOf( leftRainfallTimeSeriesArray.size() ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.RAINFALL_LEFT ),
							this.formatTimeSeriesData( leftRainfallTimeSeriesArray ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.RAINFALL_RIGHT_SIZE ),
							String.valueOf( rightRainfallTimeSeriesArray.size() ) ) )
					.map( line -> line.replace( MappingProperties.getProperty( MappingProperties.RAINFALL_RIGHT ),
							this.formatTimeSeriesData( rightRainfallTimeSeriesArray ) ) )
					.collect( Collectors.toList() );
			FileUtils.writeStringToFile( u01Path.toFile(),
					u01List.stream().collect( Collectors.joining( StringUtils.BREAKLINE ) ) );
		} catch (OperationNotSupportedException e) {
			logger.log( LogLevel.ERROR,
					"HECRASPreAdapter: Reading timeseries data from XML file has something wrong." );
		} catch (IOException e) {
			logger.log( LogLevel.ERROR, "HECRASPreAdapter: Reading timeseries or process file has something wrong." );
		}
	}

	/**
	 * Format timeseries data for model input.
	 * 
	 * @param timeSeriesArray
	 * @return
	 */
	private String formatTimeSeriesData( TimeSeriesArray timeSeriesArray ) {
		String format = "%8s";
		int lineCount = 0;
		StringJoiner joiner = new StringJoiner( StringUtils.BREAKLINE );
		String line = StringUtils.BLANK;
		for ( int i = 0; i < timeSeriesArray.size(); i++ ) {
			if ( lineCount == 9 ) {
				line = Strman.append( line,
						String.format( format, TimeSeriesLightUtils.getValue( timeSeriesArray, i, 0 ) ) );
				joiner.add( line );
				line = StringUtils.BLANK;
				lineCount = 0;
			} else {
				line = Strman.append( line,
						String.format( format, TimeSeriesLightUtils.getValue( timeSeriesArray, i, 0 ) ) );
				lineCount++;
			}
		}
		joiner.add( line );

		return joiner.toString();
	}
}
