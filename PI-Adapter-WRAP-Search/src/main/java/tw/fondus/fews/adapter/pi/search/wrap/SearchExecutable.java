package tw.fondus.fews.adapter.pi.search.wrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import nl.wldelft.util.timeseries.TimeSeriesArrays;
import strman.Strman;
import tw.fondus.commons.fews.pi.adapter.PiCommandLineExecute;
import tw.fondus.commons.fews.pi.config.xml.log.LogLevel;
import tw.fondus.commons.fews.pi.config.xml.log.PiDiagnostics;
import tw.fondus.commons.fews.pi.util.adapter.PiBasicArguments;
import tw.fondus.commons.fews.pi.util.timeseries.TimeSeriesUtils;
import tw.fondus.commons.util.file.FileType;
import tw.fondus.commons.util.string.StringUtils;
import tw.fondus.fews.adapter.pi.search.wrap.util.GDALUtils;
import tw.fondus.fews.adapter.pi.search.wrap.util.RunArguments;
import tw.fondus.fews.adapter.pi.search.wrap.util.SearchUtils;

/**
 * The model excute-adapter for running WRAP search map model from Delft-FEWS.
 * 
 * @author shepherd
 *
 */
public class SearchExecutable extends PiCommandLineExecute {

	private Logger log = LoggerFactory.getLogger( this.getClass() );

	public static void main( String[] args ) {

		RunArguments arguments = new RunArguments();
		new SearchExecutable().execute( args, arguments );
	}

	@Override
	protected void run( PiBasicArguments arguments, PiDiagnostics piDiagnostics, File baseDir, File inputDir,
			File outputDir ) {
		try {
			log.info( "Search: Step 1 Read rainfall.xml" );
			this.log( LogLevel.INFO, "Search: Step 1 Read rainfall.xml" );

			RunArguments moduleArguments = (RunArguments) arguments;
			String inputPath = inputDir.getAbsolutePath();
			String outputPath = outputDir.getAbsolutePath();
			String mergePath = Strman.append( moduleArguments.getBasePath(), StringUtils.PATH,
					moduleArguments.getMergeDir() );
			Preconditions.checkState( Paths.get( mergePath ).toFile().exists(),
					"WRAP Search Executable: The merge directory is not exist." );
			String floodMapPath = Strman.append( moduleArguments.getBasePath(), StringUtils.PATH,
					moduleArguments.getFloodMapDir(), StringUtils.PATH, moduleArguments.getRegion(), StringUtils.PATH,
					moduleArguments.getCounty() );
			Preconditions.checkState( Paths.get( floodMapPath ).toFile().exists(),
					"WRAP Search Executable: The floodMapData directory is not exist." );

			/** Get XML file of rainfall for rainfall data **/
			File rainfall = Paths.get( Strman.append( inputPath, StringUtils.PATH, "Rainfall.xml" ) ).toFile();
			TimeSeriesArrays rainfallTimeSeriesArrays = TimeSeriesUtils.readPiTimeSeries( rainfall );

			/** Get Rainfall Level file for judgment rainfall data. **/
			List<String> rainfallLevelFile = SearchUtils
					.readQuantitativeRainfallLevel( Strman.append( floodMapPath, StringUtils.PATH, "Level.txt" ) );

			/**
			 * Check every location rainfall level and save each flood map file
			 * path.
			 **/
			log.info( "Search: Step 2 Get location flood map file list." );
			this.log( LogLevel.INFO, "Search: Step 2 Get location flood map file list." );

			List<String> mergeFile = new ArrayList<>();
			rainfallTimeSeriesArrays.forEach( timeSeriseArray -> {
				String loctionID = timeSeriseArray.getHeader().getLocationId();
				String rainfallIntensity = SearchUtils.determineRainfallIntensity(
						Double.valueOf( timeSeriseArray.getValue( 0 ) ), rainfallLevelFile );
				if ( !rainfallIntensity.equals( StringUtils.BLANK ) ) {
					mergeFile.add( Strman.append( loctionID, StringUtils.UNDERLINE, moduleArguments.getDuration(),
							StringUtils.UNDERLINE, rainfallIntensity, FileType.ASC.getExtension() ) );
				}

			} );
			String depthFileName = "Depth0000.asc";

			/** Follow flood map file list to move file **/
			if ( mergeFile.size() > 0 ) {
				log.info( "Search: Step 3 Has merge files, Start Run." );
				this.log( LogLevel.INFO, "Search: Step 3 Has merge files, Start Run." );

				mergeFile.forEach( file -> {
					try {
						Files.copy( Paths.get( Strman.append( floodMapPath, StringUtils.PATH, file ) ),
								Paths.get( Strman.append( mergePath, StringUtils.PATH, file ) ),
								StandardCopyOption.REPLACE_EXISTING );
					} catch (IOException e) {
					}

				} );

				log.info( "Search: Run GDAL" );
				this.log( LogLevel.INFO, "Search: Run GDAL" );
				String GDALPath = "%GDAL_PATH%";

				/** Use GADL merge flood map file to GTIFF type. **/
				/**
				 * Use GDAL tansformation flood map form GTIFF type to ASCII
				 * type.
				 **/

				GDALUtils.GDALMerge( GDALPath, mergePath, mergePath, depthFileName, "AAIGrid" );
				GDALUtils.GDALTransformation( GDALPath, mergePath, mergePath, "GTiff", "AAIGrid" );
				/** Copy merged file result to output path. **/
				log.info( "Search: Step 4 Copy result." );
				this.log( LogLevel.INFO, "Search: Step 4 Copy result." );
				Files.copy( Paths.get( Strman.append( mergePath, StringUtils.PATH, depthFileName ) ),
						Paths.get( Strman.append( outputPath, StringUtils.PATH, depthFileName ) ),
						StandardCopyOption.REPLACE_EXISTING );

			} else {
				log.info( "Search: Step 3 Not found flood map, use default result." );
				this.log( LogLevel.INFO, "Search: Step 3 Not found flood map, use default result." );
				/**
				 * If no area be up to the standard. Will use maxaera flood map
				 * file be result .
				 **/
				log.info( "Search: Step 4 Copy default result." );
				this.log( LogLevel.INFO, "Search: Step 4 Copy default result." );
				try {
					Files.copy(
							Paths.get( Strman.append( floodMapPath, StringUtils.PATH, "MaxArea",
									FileType.ASC.getExtension() ) ),
							Paths.get( Strman.append( outputPath, StringUtils.PATH, depthFileName ) ),
							StandardCopyOption.REPLACE_EXISTING );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			/** Follow result to creat mapstacks.xml. **/

			log.info( "Search: Step 5 Follow result to creat mapstacks.xml." );
			this.log( LogLevel.INFO, "Search: Step 5 Follow result to creat mapstacks.xml." );
			SearchUtils.creatMapXml( outputPath, moduleArguments.getCounty(),
					SearchUtils.getStingTime( rainfallTimeSeriesArrays.get( 0 ).getForecastTime() ) );
		} catch (IOException | OperationNotSupportedException e) {
			log.error( "Read XML: has somthing wrong!", e );
			this.log( LogLevel.ERROR, "Read XML: has somthing wrong!" );
		} catch (Exception e) {
			log.error( "Search Excutable: has somthing wrong!", e );
			this.log( LogLevel.ERROR, "Search Excutable: has somthing wrong!" );
		}

	}
}
