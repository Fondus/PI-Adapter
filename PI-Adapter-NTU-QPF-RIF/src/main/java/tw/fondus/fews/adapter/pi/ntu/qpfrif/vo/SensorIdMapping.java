package tw.fondus.fews.adapter.pi.ntu.qpfrif.vo;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The value object of id mapping with sensor.
 *
 * @author Brad Chen
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorIdMapping {
	@CsvBindByPosition( position = 0 )
	private String id;

	@CsvBindByPosition( position = 1 )
	private String model;

	@CsvBindByPosition( position = 2 )
	private String name;
}
