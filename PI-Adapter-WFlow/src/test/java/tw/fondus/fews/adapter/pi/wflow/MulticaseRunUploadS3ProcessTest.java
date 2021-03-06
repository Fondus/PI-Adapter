package tw.fondus.fews.adapter.pi.wflow;

import org.junit.Test;

import tw.fondus.fews.adapter.pi.wflow.argument.MulticaseArguments;

/**
 * The unit test of WFlow multi-case run.
 * 
 * @author Chao
 *
 */
public class MulticaseRunUploadS3ProcessTest {	
	@Test
	public void test() {
		String[] args = new String[] { 
				"-b", 
				"src/test/resources", 
				"--host", 
				"http://localhost:9000", 
				"--bucket",
				"wflow", 
				"--object", 
				"", 
				"-i", 
				"WaterLevelAttribute.csv,fews2wflow.nc,Input.nc,Observation.xml", 
				"-o", 
				"wflow_outputs.nc,Output.nc,Simulation.json,Parameter.zip", 
				"-us",
				"minio", 
				"-pw", 
				"minio123", 
				"--object-prefix", 
				"07RMO/Shanping/201609290000" };

		MulticaseArguments arguments = MulticaseArguments.instance();
		new MulticaseRunUploadS3Process().execute( args, arguments );
	}
}
