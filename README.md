# PI-Adapter
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)

The PI-Adapter is use `Commons General Adapter interface` to run Model Adapter connected to Delft-FEWS system.
Delft-FEWS System will pass arguments by `standard arguments object`, and `PiCommandLineExecute` will receive the need information, so you can implements your adater logic.

## Which Models implements with PI-Adapter now
- [USGS TRGIRS landslide model](https://github.com/usgs/landslides-trigrs)
- [NCHC Rainfall Runoff models](https://www.nchc.org.tw/tw/)
- [NCHC Grid models](https://www.nchc.org.tw/tw/)
- [NCHC RTC-2D models](https://www.nchc.org.tw/tw/)
- [SensLink 2.0 & 3.0](http://www.anasystem.com.tw/senslink/)
- [NCTU DPWE AI Model](http://dpwe.nctu.edu.tw/)

## How to work with PI-Adapter connected to Delft-FEWS
![Flow charts](https://i.imgur.com/BKosuN1.png)

## The Standard Published Interface Arguments

- `PiBasicArguments`: If you **don't need the input/output files list**, just need execute program with Delft-FEWS System, use this please.

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -b / --base | The current working directory. | - | true |
| -t / --time | The T0. TimeZone is UTC. | - | false |
| -h / --help | Show how to usage. | - | false |
| -l / --log | The Pi Diagnostics log file name. | Diagnostics.xml | false |
| -ld / --ldir | The Pi Diagnostics log folder, relative to the current working directory. | Diagnostics/ | false |
| -id / --idir | The input file folder, relative to the current working directory. | Input/ | false |
| -od / --odir | The output file folder, relative to the current working directory. | Output/ | false |

- `PiArguments`: If you **need the input/output files list**, use this please.

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -i / --input | The input file list with comma, and order is fixed. | - | true |
| -o / --output | The output file list with comma, and order is fixed. | - | true |
| -p / --parameter | The parameter name of model output, use only when program need it. | - | false |
| -u / --unit | The unit name of model output, use only when program need it. | - | false |

- `Expand Arguments`: The customermized standard arguments.

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -e / --executable | The model executable. | - | true |
| -td / --tdir | The temp folder, relative to the current working directory. | - | true |
| -ed / --edir | The executable folder, relative to the current working directory. | - | true |
| -pd / --pdir | The parameter folder, relative to the current working directory. | - | true |
| -ti / --timeindex | The time series array index. | - | true |
| -d / --duration | The index duration. | - | true |
| -us / --username | The username. | - | true |
| -pw / --password | The password. | - | true |
