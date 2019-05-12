package tw.fondus.fews.adapter.pi.ai.nctu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiArguments;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.file.ZipUtils;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.commons.util.time.TimeUtils;

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
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		PiArguments arguments = new PiArguments();
		new DPWE_AI_PostAdapter().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) throws Exception {
		/** Cast PiArguments to expand arguments **/
		PiArguments modelArguments = (PiArguments) arguments;
		
		Preconditions.checkState( modelArguments.getInputs().size() > 0 && modelArguments.getInputs().size() == 5,
				"DPWE AI PostAdapter: The input time.txt, T.zip, T+1.zip, T+2.zip and T+3.zip not give by command -i." );
		Preconditions.checkState( modelArguments.getOutputs().size() > 0 && modelArguments.getOutputs().size() == 1,
				"DPWE AI PostAdapter: The Output map stacks meta info file name not give by command -o." );
		
		try {
			log.info( "DPWE AI PostAdapter: Start the PostAdapter to process with model output." );
			this.log( LogLevel.INFO, "DPWE AI PostAdapter: Start the PostAdapter to process with model output.");
			
			/** Check state **/
			Path timeInfoPath = this.checkExists( inputDir.getPath(), modelArguments.getInputs().get( 0 ), "DPWE AI PostAdapter: The input time.txt do not exists." );
			Path time0ZIP = this.checkExists( inputDir.getPath(), modelArguments.getInputs().get( 1 ), "DPWE AI PostAdapter: The input T.zip do not exists." );
			Path time1ZIP = this.checkExists( inputDir.getPath(), modelArguments.getInputs().get( 2 ), "DPWE AI PostAdapter: The input T+1.zip do not exists." );
			Path time2ZIP = this.checkExists( inputDir.getPath(), modelArguments.getInputs().get( 3 ), "DPWE AI PostAdapter: The input T+2.zip do not exists." );
			Path time3ZIP = this.checkExists( inputDir.getPath(), modelArguments.getInputs().get( 4 ), "DPWE AI PostAdapter: The input T+3.zip do not exists." );
			
			/** Unzip the files and rename to pattern **/
			this.unzipProcess( time1ZIP, outputDir );
			this.unzipProcess( time2ZIP, outputDir );
			this.unzipProcess( time3ZIP, outputDir );
			this.unzipProcess( time0ZIP, outputDir ); // Do T0 last to avoid be rename place.
			
			/** Read time info and create the map stacks meta info **/
			DateTime timeZero = this.readTimeInfo( timeInfoPath );
			File mapMetaInfo = PathUtils.get( Strman.append( outputDir.getPath(), StringUtils.SLASH, modelArguments.getOutputs().get( 0 ) ) ).toFile();
			this.createMapStacks( timeZero, mapMetaInfo, modelArguments.getParameter() );
			
			log.info( "DPWE AI PostAdapter: End the PostAdapter to process with model output." );
			this.log( LogLevel.INFO, "DPWE AI PostAdapter: End the PostAdapter to process with model output.");
			
		} catch ( FileNotFoundException e ) {
			log.error("DPWE AI PostAdapter: The input files not exits!", e);
			this.log( LogLevel.ERROR, "DPWE AI PostAdapter: The input files not exits!");
		} catch ( IOException e ) {
			log.error("DPWE AI PostAdapter: Read input files has something wrong!", e);
			this.log( LogLevel.ERROR, "DPWE AI PostAdapter: Read input files has something wrong!");
		}
	}
	
	/**
	 * Check the file exists or not.
	 * 
	 * @param folder
	 * @param file
	 * @param message
	 * @return
	 * @throws FileNotFoundException
	 */
	private Path checkExists( String folder, String file, String message ) throws FileNotFoundException {
		Path path = PathUtils.get( Strman.append( folder, StringUtils.PATH, file ) );
		if ( !PathUtils.exists( path ) ) {
			throw new FileNotFoundException( message );
		}
		return path;
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
	 * @param timeZero
	 * @param target
	 * @param parameterId
	 * @throws Exception 
	 */
	private void createMapStacks( DateTime timeZero, File target, String parameterId ) throws Exception {
		log.info( "DPWE AI PostAdapter: Create the MapStacks.xml with file name: {}.", target.getName()  );
		this.log( LogLevel.INFO, "DPWE AI PostAdapter: Create the MapStacks.xml with file name: {}.", target.getName());
		
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
		mapStacks.getMapStacks().add( mapstack );
		
		XMLUtils.toXML( target, mapStacks );
	}
	
	/**
	 * The process of unzip files and rename to patterns.
	 * 
	 * @param zipPath
	 * @param target
	 * @throws IOException
	 */
	private void unzipProcess( Path zipPath, File target ) throws IOException {
		ZipEntry ze = ZipUtils.toList( zipPath.toFile().getPath() ).get( 0 );
		String sourceFileName = ze.getName();
		String outputFileName = this.toPatternName( PathUtils.getNameWithoutExtension( zipPath ) );
		String outputFolder = target.getPath();

		Path sourceFilePath = PathUtils.get( Strman.append( outputFolder, StringUtils.SLASH, sourceFileName ) );
		ZipUtils.unzip( zipPath.toFile().getPath(), outputFolder );
		
		PathUtils.rename( sourceFilePath, outputFileName );
		
		log.info( "DPWE AI PostAdapter: Unzip process with the zip file: {} to pattern name: {}.", PathUtils.getName( zipPath ), outputFileName );
		this.log( LogLevel.INFO, "DPWE AI PostAdapter: Unzip process with the zip file: {} to pattern name: {}.", PathUtils.getName( zipPath ), outputFileName );
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
