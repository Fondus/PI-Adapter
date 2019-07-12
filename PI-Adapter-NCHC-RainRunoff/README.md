# PI-Adapter NCHC Rain Runoff
The adapter of NCHC Rain Runoff models to show how to integrate with Delft-FEWS system.

## Model Meta Information

The models use to simulation rain-runoff of NCHC.

#### Name:
- Sacramento
    - Est_Runoff__SACSMA_NCHC.exe
- MSFRM
    - pro_est_flow_msfrm.exe
- Tank
    - est_flow_Tank.exe

#### Environment PreSetting:
- No need

#### Runtime Command:
- Sacramento
    - Est_Runoff__SACSMA_NCHC.exe
- MSFRM
    - pro_est_flow_msfrm.exe
- Tank
    - est_flow_Tank.exe

#### Inputs:
- Sacramento
    - INPUT_DATA_RAIN.TXT
    - INPUT_PARS_SACSMA.TXT
- MSFRM
    - INPUT_DATA_RAIN_EV.TXT
    - INPUT_EST_FLOW_MSFRM.TXT
- Tank
    - INPUT_RAIN_TANK.txt
    - INPUT_PARS_TANK.TXT

#### Outputs:
- Sacramento
    - OUTPUT_EST_FLOW.TXT
- MSFRM
    - OUTPUT_EST_FLOW_MSFRM.TXT
- Tank
    - OUTPUT_FLOW_TANK.TXT

#### Authors:
- NCHC

## Adapter Information

#### Expand Arguments:

- `RunArguments`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -e / --executable | The model executable. | - | true |
| --pdir / -pd | The model parameters directory, relative to the current working directory. | - | true |
| --edir / -ed | The model executable directory path, relative to the current working directory. | - | true |

#### PreAdapter
- PiIOArguments
- Read Input.xml to `Location-Id`.txt.

#### Executable Adapter
- RunArguments
- Execute command

#### Post Adaprer
- PiIOArguments
- Read model output to Output.xml


#### Model Folder Structure:
- **Work/**: The model main work folder.
  - **Input/**
  - **Output/**
  - **Diagnostics/Diagnostics.xml**
  - **Parameters/**
  - **Work/ or Tank/**

###### tags: `Github` `FEWS` `Adapter`