package tw.fondus.fews.adapter.pi.search.wrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.cli.util.Prevalidated;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.argument.PiBasicArguments;
import tw.fondus.fews.adapter.pi.cli.PiCommandLineExecute;
import tw.fondus.fews.adapter.pi.log.PiDiagnosticsLogger;
import tw.fondus.fews.adapter.pi.search.wrap.argument.RunArguments;
import tw.fondus.fews.adapter.pi.search.wrap.util.GDALUtils;
import tw.fondus.fews.adapter.pi.search.wrap.util.SearchUtils;
import tw.fondus.fews.adapter.pi.util.timeseries.TimeSeriesLightUtils;

/**
 * The model excute-adapter for running WRAP search map model from Delft-FEWS.
 * 
 * @author shepherd
 * @author Brad Chen :improve the code
 */
public class SearchExecutable extends PiCommandLineExecute {
	
	public static void main( String[] args ) {
		RunArguments arguments = new RunArguments();
		new SearchExecutable().execute( args, arguments );
	}
	
	@Override
	protected void adapterRun( PiBasicArguments arguments, PiDiagnosticsLogger logger, Path basePath, Path inputPath,
			Path outputPath ) {
		/** Cast PiArguments to expand arguments **/
		RunArguments modelArguments = (RunArguments) arguments;
		
		Path mergePath = Paths.get( Strman.append( basePath.toString(), PATH, modelArguments.getMergeDir()) );
		Prevalidated.checkExists( mergePath, "WRAP Flood Search GDAL Adapter: The merge path not exists!" );
		
		Path searchTargetPath = Paths.get( Strman.append( basePath.toString(),
				PATH, modelArguments.getFloodMapDir(),
				PATH, modelArguments.getRegion(), PATH, modelArguments.getCounty() ));
		Prevalidated.checkExists( searchTargetPath, "WRAP Flood Search GDAL Adapter: The flood map database search target not exists!" );
		
		Path inputXML = Paths.get( Strman.append( inputPath.toString(), PATH, modelArguments.getInputs().get(0)) );
		Prevalidated.checkExists( inputXML, "WRAP Flood Search GDAL Adapter: The input PI-XML not exists!" );
		
		Path thresholdPath = Paths.get( Strman.append( searchTargetPath.toString(), PATH, modelArguments.getInputs().get(1) ) );
		Prevalidated.checkExists( thresholdPath, "WRAP Flood Search GDAL Adapter: The flood map database search target threshold not exists!" );
		
		try {
			logger.log( LogLevel.INFO, "WRAP Flood Search GDAL Adapter: Read time-series PI-XML." );
			TimeSeriesArrays timeSeriesArrays = TimeSeriesLightUtils.readPiTimeSeries( inputXML );
			
			logger.log( LogLevel.INFO, "WRAP Flood Search GDAL Adapter: Collect the files need to be merge from flood database when cross the rainfall threshold." );
			List<Double> thresholds = SearchUtils.readQuantitativeRainfallThreshold( thresholdPath );
			List<String> mergeFiles = new ArrayList<>();
			timeSeriesArrays.forEach( array -> {
				String loctionID = array.getHeader().getLocationId();
				
				Optional<String> optIntensity = SearchUtils.determineRainfallIntensity( array.getValue( 0 ), thresholds );
				optIntensity.ifPresent( intensity -> {
					mergeFiles.add( Strman.append( loctionID, StringUtils.UNDERLINE, modelArguments.getDuration(),
							StringUtils.UNDERLINE, intensity, FileType.ASC.getExtension() ) );
				} );
			});
			
			/** Follow flood map file list to move file **/
			String depthFileName = "Depth0000.asc";
			if ( mergeFiles.size() > 0 ) {
				logger.log( LogLevel.INFO, "WRAP Flood Search GDAL Adapter: Found the merge files from flood database, start prepare process for merged." );
				
				mergeFiles.forEach( mergeFile -> {
					try {
						Files.copy( Paths.get( Strman.append( searchTargetPath.toString(), PATH, mergeFile ) ),
								Paths.get( Strman.append( mergePath.toString(), PATH, mergeFile ) ),
								StandardCopyOption.REPLACE_EXISTING );
					} catch (IOException e) {
						logger.log( LogLevel.ERROR, "WRAP Flood Search GDAL Adapter: Copy merge file has something wrong." );
					}
				} );
				
				logger.log( LogLevel.INFO, "WRAP Flood Search GDAL Adapter: Copy done, start process for merged." );
				
				/** Use GDAL to merge files to GTIFF type, then transformation to ASCII. **/
				String GDALPath = "%GDAL_PATH%";
				GDALUtils.merge( GDALPath, mergePath.toString(), mergePath.toString(), depthFileName, "AAIGrid" );
				GDALUtils.transformation( GDALPath, mergePath.toString(), mergePath.toString(), "GTiff", "AAIGrid" );
				
				/** Copy merged file result to output path. **/
				logger.log( LogLevel.INFO, "Search: Step 4 Copy result." );
				Files.copy( Paths.get( Strman.append( mergePath.toString(), PATH, depthFileName ) ),
						Paths.get( Strman.append( outputPath.toString(), PATH, depthFileName ) ),
						StandardCopyOption.REPLACE_EXISTING );
			} else {
				logger.log( LogLevel.INFO, "WRAP Flood Search GDAL Adapter: Not found merge files from flood database, use result of default." );
				
				Files.copy( Paths.get( Strman.append( searchTargetPath.toString(), PATH, "MaxArea", FileType.ASC.getExtension() ) ),
							Paths.get( Strman.append( outputPath.toString(), PATH, depthFileName ) ),
							StandardCopyOption.REPLACE_EXISTING );
			}
			
			/** Follow result to create mapstacks.xml. **/
			logger.log( LogLevel.INFO, "WRAP Flood Search GDAL Adapter: Follow result to create the mapstacks meta-information." );
			SearchUtils.creatMapXml( outputPath.toString(), modelArguments.getCounty(),
					SearchUtils.getStringTime( timeSeriesArrays.get( 0 ).getForecastTime() ) );
			
		} catch ( OperationNotSupportedException | IOException e ) {
			logger.log( LogLevel.ERROR, "WRAP Flood Search GDAL Adapter: Read the PI-XML has something wrong!" );
		} catch (ParseException e) {
			logger.log( LogLevel.ERROR, "WRAP Flood Search GDAL Adapter: Parse time has something wrong!" );
		} catch (Exception e) {
			logger.log( LogLevel.ERROR, "WRAP Flood Search GDAL Adapter: Adapter has something wrong!" );
		} 
	}
}
