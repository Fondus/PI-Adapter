package tw.fondus.fews.adapter.pi.virtualiot.drwu.util;

import java.nio.file.Path;

import tw.fondus.commons.util.file.PathUtils;

/**
 * Data utils for virtual IoT model.
 * 
 * @author Chao
 *
 */
public class DataUtils {
	/**
	 * Copy all file without directory.
	 * 
	 * @param source source directory path
	 * @param dest target directory path
	 */
	public static void copiesWithoutDirectory( Path source, Path dest ) {
		if ( PathUtils.isDirectory( source ) && PathUtils.isDirectory( dest ) ) {
			PathUtils.list( source )
					.stream()
					.filter( path -> PathUtils.isNotDirectory( path ) )
					.forEach( path -> PathUtils.copy( path, dest ) );
		}
	}

	/**
	 * Copy all file include sub directory.
	 * 
	 * @param source source directory path
	 * @param dest target directory path
	 */
	public static void copiesWithSubDirectory( Path source, Path dest ) {
		if ( PathUtils.isDirectory( source ) && PathUtils.isDirectory( dest ) ) {
			PathUtils.list( source ).stream().forEach( path -> {
				if ( PathUtils.isDirectory( path ) ) {
					copiesWithSubDirectory( path, dest );
				} else {
					PathUtils.copy( path, dest );
				}
			} );
		}
	}
}
