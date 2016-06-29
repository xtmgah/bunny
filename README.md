# bunny
Reproducible Analyses for Bioinformatics - Java

Download the tarball from the [releases page](https://github.com/rabix/bunny/releases), extract it, `cd` into the uncompressed directory, and run the example with the following code:

```sh
./rabix examples/dna2protein/translate.cwl.json examples/dna2protein/inputs.json
```

At this time, the app argument accepts a local JSON or YAML encoding of CWL draft-2 tool or workflow in the following form:
`./rabix <tool.json | .yaml> <inputs.json | .yaml>`