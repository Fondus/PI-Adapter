package tw.fondus.fews.adapter.pi.fim.ntu;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.zeroturnaround.exec.InvalidExitValueException;

import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.nc.NetCDFReader;
import tw.fondus.commons.nc.util.NetCDFUtils;
import tw.fondus.commons.nc.util.key.VariableName;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.io.PathWriter;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.fim.ntu.argument.RunArguments;
import tw.fondus.fews.adapter.pi.fim.ntu.entity.PointData;
import tw.fondus.fews.adapter.pi.fim.ntu.util.DataBuilder;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.util.time.TimeLightUtils;
import ucar.ma2.Array;

/**
 * Model Executable and Post-Adapter for running NTU 2DFIM model from
 * Delft-FEWS.
 * 
 * @author Chao
 *
 */
public class FIMExecutable extends PiCommandLineExecute {

	public static void main( String[] args ) {
		RunArguments arguments = RunArguments.instance();
		new FIMExecutable().execute( args, arguments );
	}

	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		RunArguments runArguments = (RunArguments) arguments;

		logger.log( LogLevel.INFO, "FINExecutable: Starting process of executable." );
		Path executablePath = Prevalidated.checkExists( basePath.resolve( runArguments.getExecutableDir() ),
				"FINExecutable: Can not find the directory of executable." );

		Path templatePath = Prevalidated.checkExists( basePath.resolve( runArguments.getTemplateDir() ),
				"FINExecutable: Can not find the directory of template." );

		Path inputNC = Prevalidated.checkExists( inputPath.resolve( runArguments.getInputs().get( 0 ) ),
				"FINExecutable: Can not find the file of input NetCDF." );
		try (NetCDFReader reader = NetCDFReader.read( inputNC.toString() ) ) {
			Optional<Array> optTimeArray = reader.readVariable( VariableName.TIME );
			optTimeArray.ifPresentOrElse( ( timeArray ) -> {
				try {
					PathUtils.copies( templatePath, executablePath );
					DateTime endTime = new DateTime(
							NetCDFUtils.readArrayValue( timeArray, (int) timeArray.getSize() - 1 ).longValue() * 60
									* 1000 );
					Files.copy( inputNC,
							executablePath.resolve(
									Strman.append( PathUtils.getNameWithoutExtension( inputNC ), Strings.UNDERLINE,
											TimeLightUtils.toString( endTime, "yyyyMMdd", TimeLightUtils.UTC0 ),
											FileType.NETCDF.getExtension() ) ),
							StandardCopyOption.REPLACE_EXISTING );
					PathUtils.copies( inputPath, executablePath );
					PathWriter.write( executablePath.resolve( runArguments.getInputs().get( 1 ) ),
							runArguments.getForecast().toString() );

					logger.log( LogLevel.INFO, "FINExecutable: Starting running model." );
					String command = executablePath.resolve( runArguments.getExecutable().get( 0 ) )
							.toAbsolutePath()
							.toString();
					Executions.execute( executor -> executor.directory( executablePath.toFile() ), command );

					logger.log( LogLevel.INFO, "FINExecutable: Starting PostAdapter for model output." );
					Path modelOutputPath = executablePath.resolve( "output" );
					List<List<PointData>> datas = new ArrayList<>();
					IntStream.rangeClosed( 1, runArguments.getForecast().intValue() ).forEach( i -> {
						try {
							List<String> list = Files.readAllLines( modelOutputPath
									.resolve( Strman.append( "the", String.format( "%2d", i ), ".00hr.dat" ) ) );
							List<PointData> points = new ArrayList<>();
							list.forEach( line -> {
								points.add( new PointData( new BigDecimal( line.substring( 0, 10 ).trim() ),
										new BigDecimal( line.substring( 10, 20 ).trim() ),
										new BigDecimal( line.substring( 20, 29 ).trim() ) ) );
							} );
							datas.add( points );
						} catch (IOException e) {
							logger.log( LogLevel.ERROR, "FINExecutable: Reading model output has something wrong." );
						}
					} );

					DataBuilder.buildNetCDF( datas, outputPath.resolve( runArguments.getOutputs().get( 0 ) ), endTime );
					logger.log( LogLevel.INFO, "FINExecutable: Finished process." );
				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "FINExecutable: Coping file has something wrong." );
				} catch (InvalidExitValueException | InterruptedException | TimeoutException e) {
					logger.log( LogLevel.ERROR, "FINExecutable: Running model has something wrong." );
				} catch (Exception e) {
					logger.log( LogLevel.ERROR, "FINExecutable: Building NetCDF file has something wrong." );
				}
			}, () -> {
				logger.log( LogLevel.ERROR, "FINExecutable: Can not find time variable from input NetCDF file." );
			} );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "FINExecutable: Reading NetCDF has something wrong." );
		}
	}
}
