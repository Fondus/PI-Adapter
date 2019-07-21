# PI-Adapter Grid Merged
The adapter of NCHC Grid Merged model to show how to integrate with Delft-FEWS system.

## Model Meta Information

NCHC Grid Merged model.

#### Name:
- Merged.exe

#### Environment PreSetting:
- Fortran 95

#### Runtime Command:
- **Merged.exe zonelist.dat zone_id.asc UNITALL.asc**
  - **Merged.exe**: main command
  - **zonelist.dat**: Zone list 
  - **zone_id.asc**: Zone id Grid
  - **UNITALL.asc**: Merged target Grid

#### Inputs:
- UNIT??.xml: Mapstack meta-information
- UNIT??_0000.???: Splite Zone Grid

#### Outputs:
- UNITALL.xml: Mapstack meta-information
- UNITALL????.asc: Merged grid

#### Authors:
- NCHC

## Adapter Information

#### Expand Arguments:

- `RunArguments`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -e / --executable | The model executable. | - | true |
| -td / --tdir | The model temp file store directory, relative to the current working directory. | - | true |
| -ed / --edir | The model executable directory, relative to the current working directory. | - | true |

#### Executable Adapter
- RunArguments
- Execute command

#### Model Folder Structure:
- **Work/**: The model main work folder.
  - **Input/**
  - **Output/**
  - **Diagnostics/Diagnostics.xml**
  - **Programs/**
  - **Temp/**

###### tags: `Github` `FEWS` `Adapter`