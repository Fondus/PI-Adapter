package tw.fondus.fews.adapter.pi.report.rmo07.vo;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The record value object of river branch.
 *
 * @author Brad Chen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiverBranchRecord {
	@CsvBindByName( column = "CrossSectionId" )
	private String crossSectionId;
	@CsvBindByName( column = "Distance" )
	private BigDecimal distance;
	@CsvBindByName( column = "Bottom" )
	private BigDecimal bottom;
	@CsvBindByName( column = "Left" )
	private BigDecimal left;
	@CsvBindByName( column = "Right" )
	private BigDecimal right;
}
