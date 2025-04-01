# Pi-Adapter-Reporter-07RMO
Generate 07RMO report files by FEWS-Taiwan Workflow.

## Adapter Information

### Multi-case run adapter
#### Arguments:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -is / --indexStart | The start index of generate report used. | - | true |
| -ie / --indexEnd | The end index of generate report used. | - | true |
| -pf / --prefix | The file name prefix of process output. | FEWS | false |
| -sf / --suffix | The file name suffix of process output. | QPESUMS_QPF | false |
| -sc / --specialCases | The special case list of location ID with comma, and order is fixed. | - | false |
| -w / --width | The width resolution of chart picture.(Only use in CrossSectionChart and ProfileChart process) | - | true |
| -he / --height | The height resolution of chart picture.(Only use in CrossSectionChart and ProfileChart process) | - | true |


#### Model Folder Structure:
- **Work/**: The model main work folder.
  - **branch/**
  - **crosssection/**
  - **Diagnostics/**
  - **Input/**
  - **Output/**
  - **templates/**

###### tags: `Github` `FEWS` `Adapter`