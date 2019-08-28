# Architecture

Here it is about technical details of design decisions and the implementations.

## Domain Language and Objects

### Terms

* Original document
  > An original document is the XML instance with or without mutator instructions
* Mutator instructions
  > A Mutator instructions is the declaration of a mutator with additional data on Validation, Expectations for Evaluation, and for Test-Management
  * Implemented as XML Processing Instructions

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


TODO: Mermaid diag

## Standard Mutation Run

* CLI Parsing
* Create a Run Configuration `RunnerConfig` with folder in which to save the **Mutations**, the list of **Original Documents**, a single Schema for validation, List of (compiled) Schematron files for validation, Report Generator, List of xsl Transformators for transform Mutator, List of Actions to perform for this kind of run
  * According to Builder Pattern

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
