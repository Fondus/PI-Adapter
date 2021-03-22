# PI-Adapter
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)

The PI-Adapter is used `Commons Adapter Module` to run Model with Adapter connected integrate with Delft-FEWS system.

## How to work with PI-Adapter integrate to Delft-FEWS
![Flow charts](https://i.imgur.com/BKosuN1.png)

The Delft-FEWS System run the workflow with model can be three parts:
- **Pre Adapter**: Convert Delft-FEWS export to the model input.
- **Executable Adapter**: Control the model executable process.
- **Post Adapter**: Convert model output to the Delft-FEWS import.

Three adapters through the `Published Interface` to synchronous communication with Delft-FEWS System. 

## Commons Adapter Module

The [Commons Adapter Module](/PI-Adapter-Commons/)'s `PiCommandLineExecute` will receive the Delft-FEWS System pass information by `standard arguments`, so you can focus implements your adapter logic.

If you want see the **fully integrate model example** with PreAdapter, Executable Adapter and Post Adapter, please have a look at [here](/PI-Adapter-Example/).

The example implements code as blew:
```java
public class ExamplePreAdapter extends PiCommandLineExecute {
    // Main process
    public static void main(String[] args) {
        PiIOArguments arguments = PiIOArguments.instance();
        new ExamplePreAdapter().execute( args, arguments );
    }
    
    @Override
    protected void adapterRun( PiBasicArguments arguments, 
        PiDiagnosticsLogger logger,
        Path basePath, Path inputPath, Path outputPath ) {
        PiIOArguments modelArguments = this.asIOArguments( arguments );
        // Your adapter logic
    }
}
```

### The Model Folder Structure:
When start adapter with extends `PiCommandLineExecute`, program will check the model folder structure is **follow the Delft-FEWS standard**, the standard is show as below:

- **Work/**: The model main work folder.
  - **Input/**: The Delft-FEWS System exports place folder.
  - **Output/**: The Delft-FEWS System imports place folder.
  - **Diagnostics/Diagnostics.xml**: It's used to synchronous adaper logging message to Delft-FEWS System.

### The Standard Arguments:

The arguments is extends from the [BasicArguments](https://github.com/Fondus/Commons-CLI/blob/master/src/main/java/tw/fondus/commons/cli/argument/BasicArguments.java):

- `PiBasicArguments`: - Basic arguments to run the command-Line interface, **Not included input and output files**, if you need this, use `PiIOArguments` please.

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -b / --base | The current working directory. | - | true |
| -h / --help | Show how to usage. | - | false |
| -id / --idir | The input file folder, relative to the current working directory. | Input/ | false |
| -od / --odir | The output file folder, relative to the current working directory. | Output/ | false |
| -ld / --ldir | The Pi Diagnostics log folder, relative to the current working directory. | Diagnostics/ | false |
| -l / --log | The Pi Diagnostics log file name. | Diagnostics.xml | false |
| -t / --time | The T0. TimeZone is UTC. | - | false |

- `PiIOArguments`: Standard arguments use for the **included input, output files**, parameter and unit to run the command-Line interface.

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -i / --input | The input file list with comma, and order is fixed. | - | true |
| -o / --output | The output file list with comma, and order is fixed. | - | true |
| -p / --parameter | The parameter name of model output, use only when program need it. | - | false |
| -u / --unit | The unit name of model output, use only when program need it. | - | false |

## Which Models implements with PI-Adapter now
| Model | URL | Provider | Type | Adapter |
|:------ |:----------- |:-----------:|:-----------:|:-----------:|
| FondUS Commons adapter tools | [link](https://github.com/Fondus/PI-Adapter) | FondUS | - | [link](/PI-Adapter-Commons/) |
| FondUS NetCDF adapter tools | [link](https://github.com/Fondus/PI-Adapter) | FondUS | - | [link](/PI-Adapter-NetCDF/) |
| FondUS Rainfall adapter tools | [link](https://github.com/Fondus/PI-Adapter) | FondUS | - | [link](/PI-Adapter-Rainfall-Process/) |
| FondUS Stress test tools | [link](https://github.com/Fondus/PI-Adapter) | FondUS | - | [link](/PI-Adapter-StressTest/) |
| USGS TRGIRS landslide Model | [link](https://github.com/usgs/landslides-trigrs) | NCDR | 2D | [link](/PI-Adapter-TRIGRS/) |
| NCHC Rainfall Runoff Models | [link](https://www.nchc.org.tw/tw/) | NCHC | 1D | [link](/PI-Adapter-NCHC-RainRunoff/) |
| NCHC LongTimeFlow Model | [link](https://www.nchc.org.tw/tw/) | NCHC | 1D | [link](/PI-Adapter-NCHC-LongTimeFlow/) |
| NCHC Grid Merged Model | [link](https://www.nchc.org.tw/tw/) | NCHC | - | [link](/PI-Adapter-GridMerge/) |
| NCHC RTC-2D Model | [link](https://www.nchc.org.tw/tw/) | NCHC | 2D | [link](/PI-Adapter-NCHC-RTC-2D/) |
| NCHC Irrigation Optimize Model | [link](https://www.nchc.org.tw/tw/) | NCHC | 1D | [link](/PI-Adapter-NCHC-Irrigation-Optimize/) |
| NTU 2D FIM Model | [link](https://www.hy.ntu.edu.tw/) | NTU | 2D | [link](/PI-Adapter-NTU-2DFIM/) |
| NTU QPF-RIF Model | [link](https://www.hy.ntu.edu.tw/) | NTU | 2D | [link](/PI-Adapter-NTU-QPF-RIF/) |
| NUU Dr. Wu Visual IoT Model | [link](https://civil.nuu.edu.tw/p/405-1081-25910,c3611.php) | NUU | 1D | [link](/PI-Adapter-DrWu-VirtualIoT/) |
| SensLink 2.0 & 3.0 Import/Export | [link](http://www.anasystem.com.tw/) | AnaSystem | - | [link](/PI-Adapter-SensLink/) |
| S3 Import/Export | [link](https://docs.aws.amazon.com/AmazonS3/latest/API/Welcome.html) | Amazon | - | [link](/PI-Adapter-S3/) |
| NCTU DPWE AI Model Import | [link](http://dpwe.nctu.edu.tw/) | NCTU | - | [link](/PI-Adapter-NCTU-AI/) |
| WRAP Flood Search with GDAL| [link](https://www.wrap.gov.tw/) | WRAP | 2D | [link](/PI-Adapter-WRAP-Search/) |

## Dependencies
- [Commons-CLI](https://github.com/Fondus/Commons-CLI): The standard commons interface of FondUS to write command-line program.
- [Commons-FEWS-XML](https://github.com/Fondus/Commons-FEWS-XML): The tools of FondUS to communication with Delft-FEWS System parts `Published Interface` XML.
- Delft-FEWS library: You still need provide the Delft-FEWS library youself.

## Authors and Contributors
The PI-Adapter are developed by the FondUS Technology Co., Ltd. and are maintained by [@Vipcube](https://github.com/Vipcube).