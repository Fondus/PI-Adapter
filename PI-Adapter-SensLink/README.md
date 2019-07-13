# PI-Adapter Senslinks
The adapter of Senslinks API to show how to integrate with Delft-FEWS system.

## Adapter Information

#### Expand Arguments:
- `Senslink 2.0`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -ti / --timeindex | The time index of the TimeSeriesArray index position. | - | true |
| -d / --duration | The minus duration relative to the current time. | - | true |
| -us / --username | The account username. | - | true |
| -pw / --password | The account password. | - | true |
| -s / --server | The target server, value can be 0 or 1 only. | - | true |
  
- `Senslink 3.0`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -ti / --timeindex | The time index of the TimeSeriesArray index position. | - | true |
| -d / --duration | The minus duration relative to the current time. | - | true |
| -us / --username | The account username. | - | true |
| -pw / --password | The account password. | - | true |

#### Import Adapter
Get the data from the Senslink system throw the API.
- `Senslink 2.0`
- `Senslink 3.0`

#### Export Adapter
Export the data to the Senslink system throw the API.
- `Senslink 2.0`
- `Senslink 3.0`

#### Model Folder Structure:
- **Work/**: The model main work folder.
  - **Input/**
  - **Output/**

###### tags: `Github` `FEWS` `Adapter`