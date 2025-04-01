package tw.fondus.fews.adapter.pi.report.rmo07.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import tw.fondus.commons.util.file.PathUtils;

public class WaterStationMetaUtilsTest {
	@Test
	public void test() {
		Assertions
				.assertTrue( WaterStationMetaUtils
						.readWaterlevelMetaInfo(
								PathUtils.path( "src/test/resources/templates/Taiwan_Stations_WaterLevel.csv" ),
								PathUtils.path( "src/test/resources/templates/attributes.csv" ))
						.size() > 0 );
	}
}
