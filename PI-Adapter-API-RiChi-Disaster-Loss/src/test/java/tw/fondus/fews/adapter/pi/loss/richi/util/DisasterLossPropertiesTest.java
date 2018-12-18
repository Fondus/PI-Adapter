package tw.fondus.fews.adapter.pi.loss.richi.util;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * The unit test of DisasterLossProperties.
 * 
 * @author Chao
 *
 */
public class DisasterLossPropertiesTest {
	@Test
	public void testGetValue() {
		Optional<String> optValue = DisasterLossProperties.getProperty( DisasterLossProperties.API_KEY_VALUE );
		
		Assert.assertTrue( optValue.isPresent() );
		
		optValue.ifPresent( value -> {
			System.out.println( value );
		} );
	}
}
