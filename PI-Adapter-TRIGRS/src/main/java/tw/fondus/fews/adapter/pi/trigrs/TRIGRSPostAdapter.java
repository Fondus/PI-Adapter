package tw.fondus.fews.adapter.pi.trigrs;

import org.joda.time.DateTime;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStack;
import tw.fondus.commons.fews.pi.config.xml.mapstacks.MapStacks;
import tw.fondus.commons.fews.pi.config.xml.util.XMLUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.file.PathUtils;
import tw.fondus.commons.util.string.Strings;
import tw.fondus.commons.util.time.JodaTimeUtils;
import tw.fondus.commons.util.time.TimeFormats;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.trigrs.argument.PostArguments;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Model post-adapter for running TRIGRS landslide model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TRIGRSPostAdapter extends PiCommandLineExecute {
	
	public static void main(String[] args) {
		PostArguments arguments = PostArguments.instance();
		new TRIGRSPostAdapter().execute(args, arguments);
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		// Cast PiArguments to expand arguments
		PostArguments modelArguments = this.asArguments( arguments, PostArguments.class );
		
		Path inputXML = Prevalidated.checkExists( 
				inputPath.resolve( modelArguments.getInputs().get(0)),
				"SensLink 3.0 Import Adapter: The input XML not exists!" );
		
		String namePrefix = modelArguments.getOutputs().get(0);
		
		try {
			logger.log( LogLevel.INFO, "TRIGRS Post Adapter: create model output mapstacks XML." );	
			// Create the model output map stacks meta-information
			this.createMapStacks( inputXML, outputPath,
					namePrefix,
					modelArguments.getParameter(),
					modelArguments.getAfter());
			
			// Rename the model output to map stacks name pattern
			this.applyMapStacksNamePattern( outputPath, namePrefix );
			
			logger.log( LogLevel.INFO, "TRIGRS Post Adapter: apply name to model output files." );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "TRIGRS Post Adapter: create model output mapstacks XML has something wrong." );
		}
	}
	
	/**
	 * Read input MapStacks.xml information, and create output MapStacks.xml.
	 * 
	 * @param inputXML input XML
	 * @param outputPath output path
	 * @param namePrefix name prefix
	 * @param parameter parameter
	 * @param after after
	 * @throws Exception Exception
	 */
	private void createMapStacks( Path inputXML, Path outputPath, String namePrefix, String parameter, int after ) throws Exception{
		MapStacks mapstacks = XMLUtils.fromXML( inputXML, MapStacks.class );
		MapStack mapstack = mapstacks.getMapStacks().get(0);
		
		String startTimeString = Strman.append( mapstack.getStartDate().getDate(), " ", mapstack.getStartDate().getTime() );
		DateTime startDate = JodaTimeUtils.toDateTime( startTimeString, TimeFormats.YMDHMS );
		long startTime = startDate.getMillis();
		
		// Calculate time
		long endTime = startTime + after * 3600000;
		DateTime endDate = new DateTime( endTime );
		String[] endTimeStrings = JodaTimeUtils.toString( endDate, TimeFormats.YMDHMS ).split( Strings.SPACE );
		
		// Write to output MapStacks.xml
		if ( Strman.isBlank( parameter ) ){
			parameter = "Factor.safety";
		}
		
		mapstack.setParameterId( parameter );
		mapstack.getEndDate().setDate( endTimeStrings[0]);
		mapstack.getEndDate().setTime( endTimeStrings[1] );
		mapstack.getFile().getPattern().setFile( Strman.append( namePrefix, "????", FileType.ASC.getExtension()) );
		
		XMLUtils.toXML( outputPath.resolve( namePrefix + FileType.XML.getExtension() ) , mapstacks );
	}
	
	/**
	 * Rename TRIGRS Model output for import to Delft-FEWS System.
	 *
	 * @param outputPath output path
	 * @param namePrefix name prefix
	 */
	private void applyMapStacksNamePattern( Path outputPath, String namePrefix ){
		List<Path> paths = PathUtils.list( outputPath, path -> PathUtils.equalsExtension( path, FileType.TXT ) );
		IntStream.range( 0, paths.size() ).forEach( i -> {
			Path sourcePath = paths.get( 0 );
			Path targetPath = outputPath.resolve( namePrefix + String.format( "%04d", i ) + FileType.ASC.getExtension() );
			PathUtils.copy( sourcePath, targetPath );
			PathUtils.deleteIfExists( sourcePath );
		} );
	}
}
