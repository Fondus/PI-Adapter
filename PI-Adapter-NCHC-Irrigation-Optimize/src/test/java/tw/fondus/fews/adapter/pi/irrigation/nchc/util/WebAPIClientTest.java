package tw.fondus.fews.adapter.pi.irrigation.nchc.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.CaseParameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.entity.Parameter;
import tw.fondus.fews.adapter.pi.irrigation.nchc.type.CaseName;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The unit test of WebAPIClient.
 *
 * @author Brad Chen
 *
 */
@Ignore
public class WebAPIClientTest {
	private final static String URL = "";
	private final static String TOKEN = "";
	private List<CaseParameter> caseParameters;

	@Before
	public void testGetCaseParameters() throws IOException {
		this.caseParameters = WebAPIClient.get( URL, TOKEN );
		Assert.assertTrue( caseParameters.size() > 0 );

		caseParameters.forEach( caseParameter -> {
			System.out.println( caseParameter.getDescription() );

			caseParameter.getParameters().forEach( parameter -> System.out.println( "\t" + parameter.getDescription() ) );
		} );
	}

	@Test
	public void testFilters(){
		List<Parameter> filterAreaParameters = ParameterUtils.filterArea( this.caseParameters.get( 0 ) );
		Assert.assertTrue( filterAreaParameters.size() > 0 );

		filterAreaParameters.forEach( parameter -> System.out.println( parameter.getDescription() ) );
	}

	@Test
	public void testFindCaseName(){
		Optional<CaseParameter> opt = ModelUtils.findCase( this.caseParameters, "Case1" );
		Assert.assertTrue( opt.isPresent() );
		Assert.assertEquals( opt.get().getDescription(), CaseName.CASE_1.getDescription() );
	}
}
