package tw.fondus.fews.adapter.pi.grid.merge;

import com.google.common.base.Preconditions;
import org.zeroturnaround.exec.InvalidExitValueException;
import strman.Strman;
import tw.fondus.commons.cli.exec.Executions;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.grid.merge.argument.RunArguments;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Model executable-adapter for running ESRI Grid ASCII merged model from
 * Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class GridMergeExecutable extends PiCommandLineExecute {
	public static void main( String[] args ) {
		RunArguments arguments = RunArguments.instance();
		new GridMergeExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		RunArguments modelArguments = this.asArguments( arguments, RunArguments.class );
		
		Preconditions.checkState( modelArguments.getInputs().size() == 4,
				"GridMergeAdapter: The input MapStacks.xml name, ZONE_List, ZONE_ID configuration and template ASC not give by command." );
		Preconditions.checkState( modelArguments.getOutputs().size() == 3,
				"GridMergeAdapter: The output prefix name, MapStacks.xml file name and name pattern and the not give by command." );
		
		Path inputXML = Prevalidated.checkExists( 
				inputPath.resolve( modelArguments.getInputs().get(0) ),
				"GridMergeAdapter: The input MapStacks.xml do not exists!." );
		
		// Create executable command
		String executable = modelArguments.getExecutable();
		Path executablePath = basePath.resolve( modelArguments.getExecutableDir() );
		String command = executablePath.resolve(  executable ).toString();
		String argument1 = modelArguments.getInputs().get( 1 );
		String argument2 = modelArguments.getInputs().get( 2 );
		String argument3 = modelArguments.getOutputs().get( 0 );
		
		// Prepare the variable
		Path tempASC = executablePath.resolve( modelArguments.getInputs().get( 3 ) );
		Path outputASC = executablePath.resolve( modelArguments.getOutputs().get( 0 ) );

		// Create the output mapstacks.xml
		try {
			logger.log( LogLevel.INFO, "GridMergeAdapter: Create the output map stack meta-information." );
			this.createOutputMapStacks( inputXML, outputPath.resolve( modelArguments.getOutputs().get( 1 ) ),
					modelArguments.getOutputs().get( 2 ) );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "GridMergeAdapter: Create the output map stack meta-information has something wrong." );
		}
			
		// Read the model inputs
		try {
			logger.log( LogLevel.INFO, "GridMergeAdapter: Read the input grid files." );
			Map<String, List<String>> map = this.readGridFiles( inputPath );
			int fileSize = map.get( modelArguments.getInputs().get( 0 ).substring( 0, 6 ) ).size();

			// Run loop
			logger.log( LogLevel.INFO, "GridMergeAdapter: Prepare to run merged grid files." );
			Path tempFolderPath = basePath.resolve( modelArguments.getTempDir() );
			IntStream.range( 0, fileSize ).forEach( i -> {
				// Moving the member grid to the temp dir.
				map.values().forEach( list -> {
					Path source = PathUtils.path( list.get( i ) );
					String fileName = PathUtils.getName( source );

					logger.log( LogLevel.INFO, "GridMergeAdapter: Moving the {} to temporary folder, prepare to merged.", fileName );

					String target = Strman.append( PathUtils.getName( source ).split( "_" )[0], FileType.ASC.getExtension() );
					PathUtils.move( source, tempFolderPath.resolve( target ) );
				} );

				try {
					// Copy the merge template file to the write place.
					PathUtils.copy( tempASC, outputASC );

					// Run the command
					logger.log( LogLevel.INFO, "GridMergeAdapter: Running the merge grids to combine. Unit Member: {}.", i );
					Executions.execute( executor -> executor.directory( executablePath.toFile() ),
							command, argument1, argument2, argument3 );

					// Copy to merged grid outputs
					Path mergedASC = outputPath.resolve( PathUtils.getNameWithoutExtension( outputASC ) + String.format( "%04d", i ) + FileType.ASC.getExtension() );
					PathUtils.copy( outputASC, mergedASC );

					// Clean the tempDir
					PathUtils.clean( tempFolderPath );

				} catch (IOException e) {
					logger.log( LogLevel.ERROR, "GridMergeAdapter: Copy the merge template file has something wrong." );
				} catch (InvalidExitValueException | InterruptedException | TimeoutException e) {
					logger.log( LogLevel.ERROR, "GridMergeAdapter: Running executable has something wrong." );
				}
			} );
			logger.log( LogLevel.INFO, "GridMergeAdapter: Finished to run merged grid files." );
		} catch ( IOException e ){
			logger.log( LogLevel.ERROR, "GridMergeAdapter: Read grid files has something wrong." );
		}
	}
	
	/**
	 * Create the output mapstacks.xml by template.
	 * 
	 * @param inputXML input XML path
	 * @param output output XML path
	 * @param pattern file pattern
	 * @throws Exception has Exception
	 */
	private void createOutputMapStacks( Path inputXML, Path output, String pattern ) throws Exception {
		MapStacks mapstacks = XMLUtils.fromXML( inputXML.toFile(), MapStacks.class );
		MapStack mapstack = mapstacks.getMapStacks().get( 0 );
		mapstack.getFile().getPattern().setFile( pattern );
		XMLUtils.toXML( output, mapstacks );
	}
	
	/**
	 * Create the map with key: member id and value: list of member grid paths.
	 * 
	 * @param base base path
	 * @return classify map
	 * @throws IOException has IO Exception
	 */
	private Map<String, List<String>> readGridFiles( Path base ) throws IOException {
		Map<String, List<String>> map = new TreeMap<>();
		// Create mapStack.xml mapping file list
		try ( Stream<Path> paths = Files.list( base ) ) {
			paths.forEach( path -> {
				String name = PathUtils.getNameWithoutExtension( path );
				if ( PathUtils.equalsExtension( path, FileType.XML ) ) {
					map.putIfAbsent( name, new ArrayList<>() );
				} else {
					String key = name.substring( 0, 6 );
					map.putIfAbsent( key, new ArrayList<>() );
					map.get( key ).add( path.toString() );
				}
			} );
		}
		return map;
	}
}
