package tw.fondus.fews.adapter.pi.ai.nctu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;

import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.ZipUtils;
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.argument.PiIOArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

/**
 * DPWE AI Model adapter for post process data with Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class DPWE_AI_PostAdapter extends PiCommandLineExecute {
	private static final String LOCATION_ID = "map";
	private static final String PATTERN = "dm1d????.asc";
	private static final String GEODATUM = "TWD 1997";
	private static final String TIME_ZONE = "8.0";
	private static final String TIME_UNIT = "hour";
	private static final String TIME_SUFFIX = ":00:00";
	private static final int TIME_STEP = 1;
	
	public static void main(String[] args) {
		PiIOArguments arguments = new PiIOArguments();
		new DPWE_AI_PostAdapter().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		PiIOArguments modelArguments = (PiIOArguments) arguments;
		
		Preconditions.checkState( modelArguments.getInputs().size() > 0 && modelArguments.getInputs().size() == 5,
				"DPWE AI PostAdapter: The input time.txt, T.zip, T+1.zip, T+2.zip and T+3.zip not give by command -i." );
		Preconditions.checkState( modelArguments.getOutputs().size() > 0 && modelArguments.getOutputs().size() == 1,
				"DPWE AI PostAdapter: The Output map stacks meta info file name not give by command -o." );
		
		try {
			logger.log( LogLevel.INFO, "DPWE AI PostAdapter: Start the PostAdapter to process with model output.");
			
			/** Check state **/
			Path timeInfoPath = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 0 ) ),
					"DPWE AI PostAdapter: The input time.txt do not exists." );
			
			Path time0ZIP = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 1 ) ),
					"DPWE AI PostAdapter: The input T.zip do not exists." );
			
			Path time1ZIP = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 2 ) ),
					"DPWE AI PostAdapter: The input T+1.zip do not exists." );
			
			Path time2ZIP = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 3 ) ),
					"DPWE AI PostAdapter: The input T+2.zip do not exists." );
			
			Path time3ZIP = Prevalidated.checkExists( 
					Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get( 4 ) ),
					"DPWE AI PostAdapter: The input T+3.zip do not exists." );
			
			/** Unzip the files and rename to pattern **/
			this.unzipProcess( logger, time1ZIP, outputPath );
			this.unzipProcess( logger, time2ZIP, outputPath );
			this.unzipProcess( logger, time3ZIP, outputPath );
			this.unzipProcess( logger, time0ZIP, outputPath ); // Do T0 last to avoid be rename place.
			
			/** Read time info and create the map stacks meta info **/
			DateTime timeZero = this.readTimeInfo( timeInfoPath );
			File mapMetaInfo = PathUtils.get( Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get( 0 ) ) ).toFile();
			this.createMapStacks( logger, timeZero, mapMetaInfo, modelArguments.getParameter() );
			
			logger.log( LogLevel.INFO, "DPWE AI PostAdapter: End the PostAdapter to process with model output.");
			
		} catch ( IOException e ) {
			logger.log( LogLevel.ERROR, "DPWE AI PostAdapter: Read input files has something wrong!");
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "DPWE AI PostAdapter: Create the map stacks has something wrong!");
		}
	}
	
	/**
	 * Read the time meta information.
	 * 
	 * @param timeInfoPath
	 * @throws IOException 
	 */
	private DateTime readTimeInfo( Path timeInfoPath ) throws IOException {
		List<String> lines = PathUtils.readAllLines( timeInfoPath );
		return TimeUtils.toDateTime( lines.get( 0 ), TimeUtils.YMDHMS, TimeUtils.UTC8 );
	}
	
	/**
	 * The process of create the map stacks xml.
	 * 
	 * @param logger
	 * @param timeZero
	 * @param target
	 * @param parameterId
	 * @throws Exception 
	 */
	private void createMapStacks( PiDiagnosticsLogger logger, DateTime timeZero, File target, String parameterId ) throws Exception {
		logger.log( LogLevel.INFO, "DPWE AI PostAdapter: Create the MapStacks.xml with file name: {}.", target.getName());
		
		MapStack mapstack = new MapStack();
		mapstack.getStartDate().setDate( TimeUtils.toString( timeZero, TimeUtils.YMD ) );
		mapstack.getStartDate().setTime( Strman.append( TimeUtils.toString( timeZero, "HH" ), TIME_SUFFIX ) );
		mapstack.getEndDate().setDate( TimeUtils.toString( timeZero.plusHours( 3 ), TimeUtils.YMD ) );
		mapstack.getEndDate().setTime( Strman.append( TimeUtils.toString( timeZero.plusHours( 3 ), "HH" ), TIME_SUFFIX ) );
		
		mapstack.getTimeStep().setMultiplier( TIME_STEP );
		mapstack.getTimeStep().setUnit( TIME_UNIT );;
		
		mapstack.getFile().getPattern().setFile( PATTERN );
		mapstack.setLocationId( LOCATION_ID );
		mapstack.setParameterId( parameterId );
		
		MapStacks mapStacks = new MapStacks();
		mapStacks.setGeoDatum( GEODATUM );
		mapStacks.setTimeZone( TIME_ZONE );
		mapStacks.add( mapstack );
		
		XMLUtils.toXML( target, mapStacks );
	}
	
	/**
	 * The process of unzip files and rename to patterns.
	 * 
	 * @param logger
	 * @param zipPath
	 * @param target
	 * @throws IOException
	 */
	private void unzipProcess( PiDiagnosticsLogger logger, Path zipPath, Path target ) throws IOException {
		ZipEntry ze = ZipUtils.toList( zipPath.toFile().getPath() ).get( 0 );
		String sourceFileName = ze.getName();
		String outputFileName = this.toPatternName( PathUtils.getNameWithoutExtension( zipPath ) );
		String outputFolder = target.toString();

		Path sourceFilePath = PathUtils.get( Strman.append( outputFolder, PATH, sourceFileName ) );
		ZipUtils.unzip( zipPath.toFile().getPath(), outputFolder );
		PathUtils.rename( sourceFilePath, outputFileName );
		
		logger.log( LogLevel.INFO, "DPWE AI PostAdapter: Unzip process with the zip file: {} to pattern name: {}.", PathUtils.getName( zipPath ), outputFileName );
	}
	
	/**
	 * Return the output pattern name by the zip file name.
	 * 
	 * @param zipFileName
	 * @return
	 */
	private String toPatternName( String zipFileName ) {
		switch ( zipFileName ) {
		case "T":
			return "dm1d0000.asc";
		case "T+1":
			return "dm1d0001.asc";
		case "T+2":
			return "dm1d0002.asc";
		case "T+3":
			return "dm1d0003.asc";
		default:
			return "dm1d0000.asc";
		}
	}
}
