# PI-Adapter TRIGRS
The adapter of landslide TRIGRS model to show how to integrate with Delft-FEWS system.

## Model Meta Information

Landslide model provide from NCDR.

#### Name:
- trigrs.exe

#### Environment PreSetting:
- No need

#### Runtime Command:
- **trigrs.exe**

#### Inputs:
- input.txt: time and value

#### Outputs:
- output.txt: time and value

#### Authors:
- USGS
- NCDR

## Adapter Information

Because the TRIGRS need be run the bin folder sub the work dir, so the model arguments should be:

```java
String[] args = new String[]{
    "-b",
    "workdir/bin",
    "-id",
    "../Input/",
    "-od",
    "../Output/",
    "-ld",
    "../Diagnostics/"
    };
```

#### Expand Arguments:

- `PostArguments`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -d / --duration | The Model time duration of end period. | - | true |

- `RunArguments`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -e / --executable | The model executable. | - | true |

#### Executable Adapter
- RunArguments
- Run execute TRIGRS model.

#### Post Adaprer
- PostArguments
- Read model output to Output.xml

#### Model Folder Structure:
- **Work/**: The model main work folder.
  - **Input/**
  - **Output/**
  - **Diagnostics/Diagnostics.xml**
  - **bin/**
  - **other..**

###### tags: `Github` `FEWS` `Adapter`