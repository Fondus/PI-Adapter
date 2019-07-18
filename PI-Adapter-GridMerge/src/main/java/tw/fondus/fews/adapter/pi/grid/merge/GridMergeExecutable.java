package tw.fondus.fews.adapter.pi.grid.merge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.zeroturnaround.exec.InvalidExitValueException;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
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

/**
 * Model executable-adapter for running ESRI Grid ASCII merged model from
 * Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class GridMergeExecutable extends PiCommandLineExecute {
	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new GridMergeExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		Preconditions.checkState( modelArguments.getInputs().size() > 0 && modelArguments.getInputs().size() == 4,
				"GridMergeAdapter: The input Mapstacks.xml name, ZONE_List, ZONE_ID configuration and template ASC not give by command." );
		Preconditions.checkState( modelArguments.getOutputs().size() > 0 && modelArguments.getOutputs().size() == 3,
				"GridMergeAdapter: The output prefix name, Mapstacks.xml file name and name pattern and the not give by command." );
		
		String inputXMLPath = Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0) );
		Path inputXML = Paths.get( inputXMLPath );
		Prevalidated.checkExists( inputXML, "GridMergeAdapter: The input Mapstacks.xml do not exists!." );
		
		/** Create executable command **/
		String executable = modelArguments.getExecutable();
		Path executablePath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getExecutableDir() ) );
		String command = Strman.append( executablePath.toString(), PATH, executable );
		String argument1 = modelArguments.getInputs().get( 1 );
		String argument2 = modelArguments.getInputs().get( 2 );
		String argument3 = modelArguments.getOutputs().get( 0 );
		
		/** Prepare the variable **/
		Path tempASC = Paths.get( Strman.append( executablePath.toString(), PATH, modelArguments.getInputs().get( 3 ) ) );
		Path outputASC = Paths.get( Strman.append( executablePath.toString(), PATH, modelArguments.getOutputs().get( 0 ) ) );
		
		try {
			/** Create the output mapstacks.xml **/
			logger.log( LogLevel.INFO, "GridMergeAdapter: Create the output mapstack meta-infromation." );
			this.createOutputMapStacks( inputXML,
					Strman.append( outputPath.toString(), PATH, modelArguments.getOutputs().get( 1 ) ),
					modelArguments.getOutputs().get( 2 ) );
			
			/** Read the model inputs **/
			logger.log( LogLevel.INFO, "GridMergeAdapter: Read the input grid files." );
			Map<String, List<String>> map = this.readGridFiles( inputPath );
			int fileSize = map.get( modelArguments.getInputs().get( 0 ).substring( 0, 6 ) ).size();
			
			/** Run loop **/
			logger.log( LogLevel.INFO, "GridMergeAdapter: Prepare to run merged grid files." );
			Path tempFolderPath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getTempDir() ) );
			IntStream.range( 0, fileSize ).forEach( i -> {
				/** Moving the member grid to the temp dir. **/
				map.values().forEach( list -> {
					try {
						Path source = Paths.get( list.get( i ) );
						String fileName = PathUtils.getName( source );
						
						logger.log( LogLevel.INFO, "GridMergeAdapter: Moveing the {} to temporary folder, prepare to merged.", fileName );
						
						String target = Strman.append( PathUtils.getName( source ).split( "_" )[0], FileType.ASC.getExtension() );
						FileUtils.move( source.toString(), Strman.append( tempFolderPath.toString(), PATH, target ) );
						
					} catch ( IOException e ) {
						logger.log( LogLevel.ERROR, "GridMergeAdapter: move the unit member grid has somthing wrong!" );
					}
				} );
				
				try {
					/** Copy the merge template file to the write place. **/
					FileUtils.copy( tempASC.toFile(), outputASC.toFile() );
					
					/** Run the command **/
					logger.log( LogLevel.INFO, "GridMergeAdapter: Running the merge grids to combine. Unit Member: {}.", String.valueOf( i ) );
					Executions.execute( executor -> executor.directory( executablePath.toFile() ),
							command, argument1, argument2, argument3 );
					
					/** Copy to merged grid outputs **/
					Path mergedASC = Paths.get( Strman.append( outputPath.toString(), PATH,
							PathUtils.getNameWithoutExtension( outputASC ), String.format( "%04d", i ), FileType.ASC.getExtension() ) );
					FileUtils.copy( outputASC.toFile(), mergedASC.toFile() );
					
					/** Clear the tempDir **/
					FileUtils.delete( tempFolderPath.toFile().listFiles() );
					
				} catch ( IOException e ) {
					logger.log( LogLevel.ERROR, "GridMergeAdapter: Copy the merge template file has something wrong." );
				} catch ( InvalidExitValueException | InterruptedException | TimeoutException e ) {
					logger.log( LogLevel.ERROR, "GridMergeAdapter: Running executable has sonething wrong." );
				} 
			});
			
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "GridMergeAdapter: Write the output mapstacks has something wrong." );
		}
	}
	
	/**
	 * Create the output mapstacks.xml.
	 * 
	 * @param inputXML
	 * @param output
	 * @param pattern
	 * @throws Exception
	 */
	private void createOutputMapStacks( Path inputXML, String output, String pattern ) throws Exception {
		MapStacks mapstacks = XMLUtils.fromXML( inputXML.toFile(), MapStacks.class );
		MapStack mapstack = mapstacks.getMapStacks().get( 0 );
		mapstack.getFile().getPattern().setFile( pattern );
		XMLUtils.toXML( new File( output ), mapstacks );
	}
	
	/**
	 * Create the map with key: member id and value: list of member grid paths.
	 * 
	 * @param base
	 * @return
	 * @throws IOException
	 */
	private Map<String, List<String>> readGridFiles( Path base ) throws IOException {
		Map<String, List<String>> map = new TreeMap<>();
		/** One mapstack.xml create the mapping list **/
		try ( Stream<Path> paths = Files.list( base ) ) {
			paths.forEach( path -> {
					String name = PathUtils.getNameWithoutExtension( path );
					if ( PathUtils.getFileExtension( path ).equals( FileType.XML.getType() ) ) {
						map.putIfAbsent( name, new ArrayList<>() );
					} else {
						String key = name.substring( 0, 6 );
						map.putIfAbsent( key, new ArrayList<String>() );
						map.get( key ).add( path.toString() );
					}
				} );
		}

		return map;
	}
}
