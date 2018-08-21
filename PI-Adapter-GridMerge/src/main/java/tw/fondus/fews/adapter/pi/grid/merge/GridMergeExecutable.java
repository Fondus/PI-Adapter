package tw.fondus.fews.adapter.pi.grid.merge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.magiclen.magiccommand.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.FileUtils;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.grid.merge.util.RunArguments;

/**
 * Model executable-adapter for running ESRI Grid ASCII merged model from
 * Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class GridMergeExecutable extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger( this.getClass() );
	
	public GridMergeExecutable(){
		super();
	}
	
	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new GridMergeExecutable().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir, File outputDir ) throws Exception {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;

		Preconditions.checkState( modelArguments.getInputs().size() > 0 && modelArguments.getInputs().size() == 4,
				"GridMergeAdapter: The input Mapstacks.xml name, ZONE_List, ZONE_ID configuration and template ASC not give by command." );
		Preconditions.checkState( modelArguments.getOutputs().size() > 0 && modelArguments.getOutputs().size() == 3,
				"GridMergeAdapter: The output prefix name, Mapstacks.xml file name and name pattern and the  not give by command." );
		
		Path inputXML = Paths
				.get( Strman.append( inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 0 ) ) );
		if ( !Files.exists( inputXML ) ) {
			log.error( "GridMergeAdapter: The input Mapstacks.xml do not exists!.", new FileNotFoundException() );
			piDiagnostics.addMessage( LogLevel.ERROR.value(),
					"GridMergeAdapter: The input Mapstacks.xml do not exists!." );
		} else {
			/** Create command **/
			String executable = modelArguments.getExecutable();
			File programDir = Paths.get( Strman.append( baseDir.getPath(), StringUtils.PATH, modelArguments.getExecutableDir() ) ).toFile();
			String commandString = Strman.append( programDir.getPath(), StringUtils.PATH, executable,
					StringUtils.SPACE_WHITE, modelArguments.getInputs().get( 1 ),
					StringUtils.SPACE_WHITE, modelArguments.getInputs().get( 2 ),
					StringUtils.SPACE_WHITE, modelArguments.getOutputs().get( 0 ) );

			Command command = new Command( commandString );

			/** Create the output mapstacks.xml **/
			this.createOutputMapStacks( inputXML,
					Strman.append( outputDir.getPath(), StringUtils.PATH, modelArguments.getOutputs().get( 1 ) ),
					modelArguments.getOutputs().get( 2 ) );

			/** Read the model inputs **/
			Map<String, List<String>> map = this.readGridFiles( inputDir );
			int fileSize = map.get( modelArguments.getInputs().get( 0 ).substring( 0, 6 ) ).size();
			
			/** Run loop **/
			File tempDir = Paths.get( Strman.append( baseDir.getPath(), StringUtils.PATH, modelArguments.getTempDir() ) ).toFile();
			IntStream.range( 0, fileSize ).forEach( i -> {
				/** Moving the member grid to the temp dir. **/
				map.values().forEach( list -> {
					try {
						File source = Paths.get( list.get( i ) ).toFile();
						String target = Strman.append( source.getName().split( "_" )[0], StringUtils.DOT, FileType.ASC.getType() );
						
						FileUtils.move( source.getPath(), Strman.append( tempDir.getPath(), StringUtils.PATH, target ) );
					} catch ( IOException e ) {
						log.error( "GridMergeAdapter: move the unit member grid has somthing wrong!", e );
						piDiagnostics.addMessage( LogLevel.ERROR.value(), "GridMergeAdapter: move the unit member grid has somthing wrong!" );
					}
				} );
				
				try {
					/** Copy the template file to write place. **/
					File tempASC = Paths.get( Strman.append( programDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get( 3 ) ) ).toFile();
					File outputASC = Paths.get( Strman.append( programDir.getPath(), StringUtils.PATH, modelArguments.getOutputs().get( 0 ) ) ).toFile();
					FileUtils.copy( tempASC, outputASC );
					
					/** Run the command **/
					command.run( programDir );
					
					log.info( "GridMergeAdapter: Run the merge grids to combine. Unit Member: {}.", i );
					piDiagnostics.addMessage( LogLevel.INFO.value(), Strman.append( "GridMergeAdapter: Run the merge grids to combine. Unit Member: ", String.valueOf( i ), "." ) );
					
					/** Copy to merged grid outputs **/
					File memberOutputASC = Paths.get( Strman.append( outputDir.getPath(), StringUtils.PATH,
							FileUtils.getNameWithoutExt( outputASC ), String.format( "%04d", i ), StringUtils.DOT, FileType.ASC.getType() ) ).toFile();
					FileUtils.copy( outputASC, memberOutputASC);
					
					/** Clear the tempDir **/
					FileUtils.delete( tempDir.listFiles() );
					
				} catch ( IOException e ) {
					log.error( "GridMergeAdapter: copy the grid has somthing wrong!", e );
					piDiagnostics.addMessage( LogLevel.ERROR.value(), "GridMergeAdapter: copy the grid has somthing wrong!" );
				}	
			} );
		}
	}

	/**
	 * Create the map with key: member id and value: list of member grid paths.
	 * 
	 * @param base
	 * @return
	 * @throws IOException
	 */
	private Map<String, List<String>> readGridFiles( File base ) throws IOException {
		Map<String, List<String>> map = new TreeMap<>();
		/** One mapstack.xml create the mapping list **/
		try (Stream<Path> paths = Files.list( base.toPath() ) ) {
			paths.map( path -> path.toFile() )
					.filter( file -> FileUtils.getFileExt( file ).equals( FileType.XML.getType() ) )
					.forEach( file -> {
						String name = FileUtils.getNameWithoutExt( file );
						map.putIfAbsent( name, new ArrayList<String>() );
					} );
		}

		/** Put the ESRI ASCII Grid path to the map **/
		try (Stream<Path> paths = Files.list( base.toPath() ) ) {
			paths.map( path -> path.toFile() )
					.filter( file -> !FileUtils.getFileExt( file ).equals( FileType.XML.getType() ) )
					.forEach( file -> {
						String name = FileUtils.getNameWithoutExt( file ).substring( 0, 6 );
						map.get( name ).add( file.getPath() );
					} );
		}

		return map;
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
		XMLUtils.toXML( new File( Strman.append( output ) ), mapstacks );
	}
}
