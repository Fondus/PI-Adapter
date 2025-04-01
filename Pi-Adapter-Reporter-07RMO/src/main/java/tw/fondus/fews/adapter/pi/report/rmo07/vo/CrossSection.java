package tw.fondus.fews.adapter.pi.report.rmo07.vo;

import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import tw.fondus.commons.util.collection.CollectionUtils;

import java.util.List;

/**
 * The value object of cross section.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
public class CrossSection {
	private String id;
	@Builder.Default
	private List<Coordinate> coordinates = CollectionUtils.emptyListArray();
}
