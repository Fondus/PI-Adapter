# PI-Adapter
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)

The PI-Adapter is use `Commons General Adapter interface` to run Model Adapter connected to Delft-FEWS system.
Delft-FEWS System will pass arguments by `standard arguments object`, and `PiCommandLineExecute` will receive the need information, so you can implements your adater logic.

## Which Models implements with PI-Adapter now
- [USGS TRGIRS landslide model](https://github.com/usgs/landslides-trigrs)
- [NCHC Rainfall Runoff models](https://www.nchc.org.tw/tw/)

## How to work with PI-Adapter connected to Delft-FEWS
![Flow charts](https://i.imgur.com/BKosuN1.png)
