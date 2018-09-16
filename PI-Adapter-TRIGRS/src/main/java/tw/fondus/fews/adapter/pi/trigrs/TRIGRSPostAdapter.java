package tw.fondus.fews.adapter.pi.trigrs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import tw.fondus.commons.util.time.TimeUtils;
import tw.fondus.fews.adapter.pi.trigrs.util.PostArguments;

/**
 * Model post-adapter for running TRIGRS landslide model from Delft-FEWS.
 * 
 * @author Brad Chen
 *
 */
public class TRIGRSPostAdapter extends PiCommandLineExecute {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		PostArguments arguments = new PostArguments();
		new TRIGRSPostAdapter().execute(args, arguments);
	}
	
	@Override
	protected void run(PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir, File outputDir)
			throws Exception {
		/** Cast PiArguments to expand arguments **/
		PostArguments modelArguments = (PostArguments) arguments;
		try {
			
			String inputXMLPath = Strman.append(inputDir.getPath(), StringUtils.PATH, modelArguments.getInputs().get(0));
			File inputXML = new File(inputXMLPath);
			if ( !inputXML.exists() ){
				throw new FileNotFoundException();
			}
			
			String namePrefix = modelArguments.getOutputs().get(0);
			String outputPath = Strman.append(outputDir.getPath(), StringUtils.PATH);
			
			log.info("TRIGRS Post Adapter: create model output MapStacks XML.");
			this.log( LogLevel.INFO, "TRIGRS Post Adapter: create model output MapStacks XML." );
			
			this.createMapStacks(inputXML, outputPath,
					namePrefix,
					modelArguments.getParameter(),
					modelArguments.getAfter());
			
			this.applyMapStacksNamePattern(outputPath, namePrefix);
			
			log.info("TRIGRS Post Adapter: apply name to model output files.");
			this.log( LogLevel.INFO, "TRIGRS Post Adapter: apply name to model output files." );
		
		} catch (FileNotFoundException e) {
			log.error("TRIGRS Post Adapter: MapStacks XML or Output Directory not exits!", e);
			this.log( LogLevel.ERROR, "TRIGRS Post Adapter: MapStacks XML or Output Directory not exits!");
		} catch (Exception e) {
			log.error("TRIGRS Post Adapter: has somthing wrong!", e);
			this.log( LogLevel.ERROR, "TRIGRS Post Adapter: has somthing wrong!");
		} 
	}
	
	/**
	 * Rename TRIGRS Model output for import to Delft-FEWS System.
	 * 
	 * @param outputDir
	 * @param namePrefix
	 * @throws IOException 
	 */
	private void applyMapStacksNamePattern(String outputDir, String namePrefix) throws IOException{
		File baseDir = new File(outputDir);
		if ( baseDir.exists() && baseDir.isDirectory() ){
			
			StringBuilder sb = new StringBuilder();
			File[] mapStacksFiles = baseDir.listFiles(FileUtils.TXT_FILE_FILTER);
			IntStream.range(0, mapStacksFiles.length).forEach(i -> {
				sb.append( Strman.append(outputDir, namePrefix, String.format( "%04d", i), ".asc") );
				
				try {
					FileUtils.copy( mapStacksFiles[i].getPath(), sb.toString());
					mapStacksFiles[i].delete();
					sb.delete(0, sb.length());
				} catch (IOException e) {
					log.error("TRIGRS Post Adapter: apply name to model output files has something wrong.");
				}
			});
			
		} else {
			throw new FileNotFoundException();
		}
	}
	
	/**
	 * Read input MapStacks.xml information, and create output MapStacks.xml.
	 * 
	 * @param inputXML
	 * @param outputDir
	 * @param namePrefix
	 * @param parameter
	 * @param after
	 * @throws Exception 
	 */
	private void createMapStacks(File inputXML, String outputDir, String namePrefix, String parameter, int after) throws Exception{
		MapStacks mapstacks = XMLUtils.fromXML(inputXML, MapStacks.class);
		MapStack mapstack = mapstacks.getMapStacks().get(0);
		
		String startTimeString = Strman.append( mapstack.getStartDate().getDate(), " ", mapstack.getStartDate().getTime());
		DateTime startDate = TimeUtils.toDateTime(startTimeString, TimeUtils.YMDHMS);
		long startTime = startDate.getMillis();
		
		/** 計算實際時間 **/
		long endTime = startTime + after * 3600000;
		DateTime endDate = new DateTime(endTime);
		String endTimeString = TimeUtils.toString(endDate, TimeUtils.YMDHMS);
		String[] endTimeStrings = endTimeString.split(" ");
		
		/** Write to output MapStacks.xml **/
		if (Strman.isBlank(parameter)){
			parameter = "Factor.safety";
		}
		
		mapstack.setParameterId(parameter);
		mapstack.getEndDate().setDate(endTimeStrings[0]);
		mapstack.getEndDate().setTime(endTimeStrings[1]);
		mapstack.getFile().getPattern().setFile( Strman.append(namePrefix, "????.asc") );
		
		XMLUtils.toXML(new File( Strman.append(outputDir, namePrefix, StringUtils.DOT, FileType.XML.getType() ) ), mapstacks);
	}
}
