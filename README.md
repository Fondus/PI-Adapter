# PI-Adapter
The PI-Adapter is use `Common General Adapter interface` to run Model Adapter connected to Delft-FEWS system.
Delft-FEWS Sysyem will pass arguments by `standard arguments object`, and `PiCommandLineExecute` will receive the need information, so you can implements your adater logic.

## Which Models implements with PI-Adapter now
- [USGS TRGIRS lansslide model](https://github.com/usgs/landslides-trigrs)
- [NCHC RainRunoff models](https://www.nchc.org.tw/tw/)

## How to work with PI-Adapter connected to Delft-FEWS
![Flow charts](https://i.imgur.com/BKosuN1.png)
