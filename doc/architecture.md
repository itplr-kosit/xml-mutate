# Architecture

Here it is about technical details of design decisions and the implementations.

## Domain Language and Objects

### Terms

* Test
* Mutation
* valid/invalid
* Evaluation
* Mermaid diag

## Mutation algorithm

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
