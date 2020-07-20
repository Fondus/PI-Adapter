package tw.fondus.fews.adapter.pi.argument;

import org.junit.Assert;
import org.junit.Test;
import tw.fondus.fews.adapter.pi.argument.extend.AdjustTimeArguments;
import tw.fondus.fews.adapter.pi.argument.extend.MapStackArguments;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * The unit test of arguments.
 *
 * @author Brad Chen
 *
 */
public class ArgumentsTest {
	@Test
	public void testBasic(){
		PiBasicArguments arguments = PiBasicArguments.builder()
				.help( true )
				.basePath( Paths.get( "src/test/resources" ) )
				.build();

		Assert.assertTrue( arguments.isHelp() );
		Assert.assertEquals( Paths.get( "src/test/resources" ) , arguments.getBasePath() );
		Assert.assertEquals( "Input/", arguments.getInputPath() );
		Assert.assertEquals( "Output/", arguments.getOutputPath() );
		Assert.assertEquals( "Diagnostics.xml", arguments.getDiagnostics() );
		Assert.assertEquals( "Diagnostics/", arguments.getLogPath() );
	}

	@Test
	public void testIO(){
		PiIOArguments arguments = PiIOArguments.builder()
				.help( true )
				.basePath( Paths.get( "src/test/resources" ) )
				.inputs( Arrays.asList( "Input1.txt", "Input2.txt" ) )
				.outputs( Arrays.asList( "Output1.txt", "Output2.txt" ) )
				.parameter( "parameter" )
				.unit( "unit" )
				.build();

		Assert.assertTrue( arguments.isHelp() );
		Assert.assertEquals( Paths.get( "src/test/resources" ) , arguments.getBasePath() );
		Assert.assertEquals( "Input/", arguments.getInputPath() );
		Assert.assertEquals( "Output/", arguments.getOutputPath() );
		Assert.assertEquals( "Diagnostics.xml", arguments.getDiagnostics() );
		Assert.assertEquals( "Diagnostics/", arguments.getLogPath() );
		Assert.assertEquals( "Input1.txt", arguments.getInputs().get( 0 ) );
		Assert.assertEquals( "Input2.txt", arguments.getInputs().get( 1 ) );
		Assert.assertEquals( "Output1.txt", arguments.getOutputs().get( 0 ) );
		Assert.assertEquals( "Output2.txt", arguments.getOutputs().get( 1 ) );
	}

	@Test
	public void testMapStack(){
		MapStackArguments arguments = MapStackArguments.builder()
				.help( true )
				.basePath( Paths.get( "src/test/resources" ) )
				.inputs( Arrays.asList( "Input1.txt", "Input2.txt" ) )
				.outputs( Arrays.asList( "Output1.txt", "Output2.txt" ) )
				.parameter( "parameter" )
				.unit( "unit" )
				.duration( 1 )
				.filePattern( "pattern" )
				.build();

		Assert.assertTrue( arguments.isHelp() );
		Assert.assertEquals( Paths.get( "src/test/resources" ) , arguments.getBasePath() );
		Assert.assertEquals( "Input/", arguments.getInputPath() );
		Assert.assertEquals( "Output/", arguments.getOutputPath() );
		Assert.assertEquals( "Diagnostics.xml", arguments.getDiagnostics() );
		Assert.assertEquals( "Diagnostics/", arguments.getLogPath() );
		Assert.assertEquals( "Input1.txt", arguments.getInputs().get( 0 ) );
		Assert.assertEquals( "Input2.txt", arguments.getInputs().get( 1 ) );
		Assert.assertEquals( "Output1.txt", arguments.getOutputs().get( 0 ) );
		Assert.assertEquals( "Output2.txt", arguments.getOutputs().get( 1 ) );
		Assert.assertEquals( "end", arguments.getDirection() );
		Assert.assertEquals( "hour", arguments.getTimeStep() );
		Assert.assertEquals( 1, arguments.getDuration() );
		Assert.assertEquals( 1, arguments.getMultiplier() );
		Assert.assertEquals( "pattern" , arguments.getFilePattern() );
	}

	@Test
	public void testAdjustTime(){
		AdjustTimeArguments arguments = AdjustTimeArguments.builder()
				.help( true )
				.basePath( Paths.get( "src/test/resources" ) )
				.inputs( Arrays.asList( "Input1.txt", "Input2.txt" ) )
				.outputs( Arrays.asList( "Output1.txt", "Output2.txt" ) )
				.parameter( "parameter" )
				.unit( "unit" )
				.mode( 1 )
				.build();

		Assert.assertTrue( arguments.isHelp() );
		Assert.assertEquals( Paths.get( "src/test/resources" ) , arguments.getBasePath() );
		Assert.assertEquals( "Input/", arguments.getInputPath() );
		Assert.assertEquals( "Output/", arguments.getOutputPath() );
		Assert.assertEquals( "Diagnostics.xml", arguments.getDiagnostics() );
		Assert.assertEquals( "Diagnostics/", arguments.getLogPath() );
		Assert.assertEquals( "Input1.txt", arguments.getInputs().get( 0 ) );
		Assert.assertEquals( "Input2.txt", arguments.getInputs().get( 1 ) );
		Assert.assertEquals( "Output1.txt", arguments.getOutputs().get( 0 ) );
		Assert.assertEquals( "Output2.txt", arguments.getOutputs().get( 1 ) );
		Assert.assertEquals( 1, arguments.getMode() );
	}
}
