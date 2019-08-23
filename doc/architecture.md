# Architecture

Here it is about technical details of design decisions and the implementations.

## Domain Language and Objects

### Terms

* Test
* Mutation
* valid/invalid
* Evaluation
* Mermaid diag

## Standard Mutation Run

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

### Naming Scheme

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
