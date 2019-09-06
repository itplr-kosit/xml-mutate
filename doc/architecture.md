# Architecture

Here it is about technical details of design decisions and the implementations.

## Domain Language and Objects

### Terms

* Original document
  > An original document is the XML instance with or without mutator instructions
* Mutator instructions
  > A Mutator instructions is the declaration of a mutator with additional data on Validation, Expectations for Evaluation, and for Test-Management
  * Declared as XML Processing Instruction in Original Documents

* Mutator
  > A mutator mutates (changes() an Original Document according to the mutator instructions and generates one or more Mutations
* Mutation
  > Is the outcome (changed document) of a Mutator
* Validation: valid/invalid
  > A mutation can be Schema valid or invalid and Schematron valid or invalid as determined by Schema Validation and Schematron Validation
* Expectation
  > Is the declaration of what the validation outcome of (after) a Mutation is expected to be
* Evaluation
  > Is the process of determining if the expectation meets the outcome of Validation
* Test
  > Is what a Test Writer declares within an Original Document. It is mostly the combination of declaring a Mutator, Expectations and Test Cases organized in Test Groups
* Test Case
  > Is the human readable identification of a mutator instruction often combined with an explanatory description
* Test Group
  > Is a declared set of one or more Test Cases for higher level Test purpose
* Test Suite
  > Is a declared set of one or more Test Cases and/or Test Groups
* Mutation Runner
  > Is the configured and coordinated execution of all `Mutator`s on all Original Documents
* Mutation Run Context
  > Is the changing context of a Mutation runner process of a single document

A `MutatorInstruction` OR `Mutator` is the result of parsing a PI. It has a `name`. It contains `SchemaExpectation`s and `SchematronRuleExpectation`s. It might have a single `TestCase` (incl. id and description) and might belong to a `TestGroup`.

A `MutationRunner` reads each single Original Document, parses the Mutation Instructions and executes the mutation. The result of mutating a document is a list of `Mutation`s

These `Mutation`s are written out as files at configured locations with configurable names. They get **validated** against Schema and Schematron Rules. Afterwards the validation results get evaluated against the declared `Expectation`s.

And a `DocumentMutationReport` gets generated containing all information necessary to generate an overall MutationRunnerReport.


Current implementation:

A `MutatorInstruction` has a `MutatorDocumentContext` containing all information about original document and where it is located.

* What is a valid Instruction?
* A call to execute generates a List of Mutations, which still have to be created
* Therefore a `Mutation` has different states see `MutationState`
  * UNDEFINED, ERROR, CREATED, MUTATED, VALIDATED, CHECKED;
* A Mutation based on a valid `MutatorInstruction` has state `CREATED` otherwise if  `MutatorInstruction` already has a parsing error it has to have state `ERROR`


Todos:

* Move DocumentParser to parser package
* translate above doc
* Fix DocumentParserTest
  * no hard coded resources add from string and inputstream
  * what if something is null


## Error Handling and Reporting

```
-fae,--fail-at-end                     Only fail the build afterwards;
                                        allow all non-impacted builds to
                                        continue
 -ff,--fail-fast                        Stop at first failure in
                                        reactorized builds
 -fn,--fail-never                       NEVER fail the build, regardless
                                        of project result
```

Taken from Maven here default is `fail-at-end` that is exit value > 0 if only one thing went wrong incl. some validation did not have expected outcome.

## Standard Mutation and Test Run

* CLI Parsing
* Create a Run Configuration `RunnerConfig` with folder in which to save the **Mutations**, the list of **Original Documents**, a single Schema for validation, List of (compiled) Schematron files for validation, Report Generator, List of xsl Transformators for transform Mutator, List of Actions to perform for this kind of run
  * According to Builder Pattern

### Processing steps

1. Original Document is read
2. Processing Instructions are parsed and MutatorInstructions generated
3. For each MutatorInstruction 1 or more Mutations are generated
4. Each  Mutation gets
   1. serialized
   2. validated against schema
   3. validated against schematron(s)
   4. evaluated against expectations

* All steps happen in memory on a w3c DOM Document.
* In order to keep memory footprint low and a bit speed up, a MutatorInstruction keeps the original XML Element which should be mutated as deep clone DocumentFragment and also as a second deep clone DocumentFragment which is input to a Mutator and mutates it





### Actions





All steps are designed similar to a simple Command Pattern and are called Actions.

Currently, configured actions in order of execution are:

* InsertCommentAction
* MutateAction
* SerializeAction(this.config.getTargetFolder()));
* ValidateAction(this.config.getSchema(), this.config.getSchematronRules(),
                            config.getTargetFolder()));
* EvaluateSchematronExpectationsAction());
* ResetAction());
* new RemoveCommentAction());

## Mutation algorithm

Order of processing is by nesting depth from inside out, because otherwise it might happen that xmute PIs are loosing their Kontext.

### File Naming Scheme

Different file naming schemes are implemented using the Strategy Pattern.

The strategy has basically the generate() function, which gets a NamingData object holding the necessary data for name generation.

The default file naming scheme is:



## Evaluation of Expectations

* Expectation definition

## Reporting

Currently, we use [JANSI](https://github.com/fusesource/jansi) to generate colored output to the console.

## Command Line Interface

The main intended use of XML-Mutate is based on the command line interface.

We use Picocli library to implement a GNU/Posix style interface.

## Semantic Versioning

We plan Semantic Versioning.

* What is our definition of Breaking Change

## Brainstorm

* What happens if schematron rule does not exist?
* What to do if many more then declared fired
* design schematron fire levels warn, etc...
* Naming Scheme of resulting documents
* Test Documentation testsuite has many test cases across documents
