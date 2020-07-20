# PI-Adapter Example
The adapter of FondUS example model to show how to integrate with Delft-FEWS system.

## Model Meta Information

Example model.

#### Name:
- Model.jar

#### Environment PreSetting:
- JVM

#### Runtime Command:
- **java -jar model.jar input.txt output.txt**
  - **java -jar model.jar**: main command
  - **input.txt**: model input path
  - **output.txt**: model output file path

#### Inputs:
- input.txt: time and value

```
201907041000,0.12970397
201907041100,0.7155926
201907041200,0.44737074
201907041300,0.87245566
201907041400,0.5827064
201907041500,0.1401003
201907041600,0.7398107
201907041700,0.30581763
```

#### Outputs:
- output.txt: time and value
```
201907041000,100.12970397
201907041100,100.7155926
201907041200,100.44737074
201907041300,100.87245566
201907041400,100.5827064
201907041500,100.1401003
201907041600,100.7398107
201907041700,100.30581763
```

#### Authors:
- FondUS

## Adapter Information

#### Expand Arguments:

- `ExecutableArguments`:

| Argument | Description | Default | Required |
|:------ |:----------- |:-----------:|:-----------:|
| -e / --executable | The model executable. | - | true |

#### PreAdapter
- PiIOArguments
- Read Input.xml to `Location-Id`.txt.

#### Executable Adapter
- ExecutableArguments
- Execute command

#### Post Adapter
- PiIOArguments
- Read model output to Output.xml