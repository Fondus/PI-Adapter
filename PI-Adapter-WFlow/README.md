# PI-Adapter WFlow
The adapter of Deltares WFlow model to run special case with Delft-FEWS system.

## Adapter Information

### Multi-case run adapter
#### Arguments:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -host / --host | The host of S3 API. | - | true |
| --bucket | The bucket of S3 API. | - | true |
| --object | The object name of bucket. | - | true |
| -us / --username | The account username. | - | true |
| -pw / --password | The account password. | - | true |
| --object-prefix | The object prefix path of S3 API. |  | false |
| -pd / --pdir | The multi-case parameter directory. | Parameter/ | true |


#### Model Folder Structure:
- **Work/**: The model main work folder.
  - **Input/**
  - **Output/**
  - **model/**
  - **Parameter/**
  - **docker-compose.yml**

###### tags: `Github` `FEWS` `Adapter`