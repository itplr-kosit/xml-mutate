# XML-MutaTe

A declarative **XML** instance **Muta**ting and **Te**st management tool.

## Motivation/Background

There are several tools available to generate XML instance documents using XML Schema. These tools are very good in generating random documents within the constraints of the schema i.e. generating valid instances.
The generated content is most often not meaningful and does not reflect business requirements and business cases.
However, not only during the development of XML Schema definition languages it is often very important to additionally test schema definitions against invalid instances. Only then one can make sure that certain constraints/rules

* correctly exclude unwanted content and do not include content as **False Positives**,
* correctly include all valid content and do not exclude **False Negatives**

However, the maintenance of a large number of meaningful test instances often becomes cumbersome for several reasons:

* One needs to manage large number of XML instances for testing
* One needs to define new schemas or one requires invasive changes to existing schemas to annotate the XML test instances with assertions about test cases

## The declarative approach

XML-MutaTe takes a declarative non-invasive approach by allowing test writers to annotate original XML test instances with XML processing instructions. Original valid test instances can for example contain real business data. These specific instructions allow to define certain mutations which should be applied to original instances in order to generate new test instances as variations of the original instance on the fly. Moreover, a test writer can use all those instructions to make certain assertions about the validity of the mutated instances.

These can look like this for example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- namespace declarations omitted for brevity -->
<ubl:Invoice>
    <cbc:CustomizationID>urn:ce.eu:en16931:2017:xoev-de:kosit:standard:xrechnung_1.1</cbc:CustomizationID>
    <cbc:ID>123456XX</cbc:ID>
    <cbc:IssueDate>2016-04-04+01:00</cbc:IssueDate>
    <cbc:InvoiceTypeCode>380</cbc:InvoiceTypeCode>
    <cbc:Note>ADU</cbc:Note>
    <cbc:Note>Es gelten unsere Allgem. Geschäftsbedingungen, die Sie unter […] finden.</cbc:Note>
    <cbc:DocumentCurrencyCode>EUR</cbc:DocumentCurrencyCode>
    <cbc:TaxCurrencyCode>EUR</cbc:TaxCurrencyCode>
    <cbc:BuyerReference>04011000-12345-34</cbc:BuyerReference>
    <!-- Generate a new instances with next element removed-->
    <?xmute mutator="remove" description="dieses soll x testen" ?>
    <cac:AccountingSupplierParty>
        <cac:Party>
            <cac:PartyName>
                <cbc:Name>[Seller trading name]</cbc:Name>
            </cac:PartyName>
            <cac:PostalAddress>
                <!-- Generate new instances each with new random order of the following sibling elements-->
                <?xmute mutator="remove"  schematron-invalid="bt-br-03" ?>
                <cbc:StreetName>[Seller address line 1]</cbc:StreetName>
                <!-- Generate a new instance with next element content being empty -->
                <?xmute mutator="empty" schema-valid  schematron-invalid="bt-br-03" ?>
                <cbc:CityName>bremen</cbc:CityName>
                <!-- Rest omitted for brevity -->

```

Applying this declarative approach to a valid instance, keeps it valid and it can be used as usual. Because XML tools just ignore the `xmute` processing instructions.

This way many kinds of mutations can be defined and combined with assertions about the validity of the mutated instances.

## List of possible mutators

* Empty element
* randomize element order
* remove element
* add element/attribute
* change text content from code lists
* change text content from concrete values list as given in declaration
  * man koennte auch ein file angeben mit einem Value pro Zeile
* Length mutator
* character mutator: generiere sequenz von charactern, die aber Regeln entsprechen oder auch voellig random (koennte den laengen mutator beinhalten)
* xslt mutator
* eigene java based mutator (plugin maessig)

Externalisieren von mutator "content" (da womit ein  mutator arbeiten soll) in externe files a la yaml etc

[Documentation of available Mutators](../doc/mutator.md).

## Features

* Scan the XML documents to generate a report with all the rules
* Generate persisted mutations i.e. files of mutations
* Check valid mutator declarations
* Mutators can be written in XSLT

## Run Modes

There are four app runtime modes:

1. Scan xml and print report
2. Generate mutations mode (the default): Generate mutations only
3. Generate mutations and test expectations mode: Generate mutations and test the resulting instances if they meet expectations
4. Check mode: Check that xmute instructions are syntactically correct and executable
5. Generate Test Management Report

### Scan xml

You can scan one or more documents to list the rules contained within them.
If you add the --snippets option, you will get in the result the snippets of the XML element that the xmute refers to.

Scanning is implemented using FSH state machine which is described in state_machine.md

Example command for running scan:
```shell
java -jar target/xml-mutate-1.0-SNAPSHOT.jar \
   ./eforms_CN_E3_max-DE_valid_codelists.xml ./forms_CN_E3_max-DE.xml \
  scan \
  --snippets
```
Example result:

Without snippet(s):
```
File: C:\KoSIT\eforms_CN_E3_max-DE_valid_codelists.xml
	Rule: efde:CL-DE-BT-11
		Expectations: invalid
		Mutation: code
			Mutation Parameters: 
				Values: not-valid-code, kbeh
```

With snippet(s):

```
File: C:\KoSIT\eforms_CN_E3_max-DE_valid_codelists.xml
    Rule: efde:CL-DE-BT-11
            Expectations: invalid
            Mutation: code
                Mutation Parameters: 
                    Values: not-valid-code, kbeh
        <cbc:PartyTypeCode listName="buyer-legal-type">def-cont</cbc:PartyTypeCode>
```

### Mutation mode

One mutator after the other in the order of appearance in the XML tree.
Each independent of the other.

Per default a mutator gets executed on the next element sibling, otherwise xpath has to be defined. This xpath is then relative to the context in which the mutator is declared.

### Mutate and Test mode

Mutates like in Mutation mode and also tests each generated instance against XML Schema and Schematron.

## Application User Interface

Command line interface

* following GNU best practices and conventions

## XMute Instructions

Only XML processing instructions with name `xmute` are processed.

The general data structure of an instruction is a list of `key="value"` configuration items as shown in this example:

```xml
<?xmute mutator="remove" 
        schema-valid schematron-invalid="bt-br-03" ?>
```

All item keys are interpreted case-insensitive. Each item value must be surrounded by quotes `"`. Sometimes `value` is optional.

### Mutations

One and only one `mutator` key(word) is mandatory where value is the name of the mutator to be applied e.g. `mutator="empty"`.

There might be additional `key=value` items configuring the behavior of the mutator.

### Test Expectations

Each mutation (i.e. mutated document) is validated against XML Schema and Schematron rules if run in default mutate and test mode and the validation outcome is checked if it meets the declared expectations.

Therefore depending on the validation outcome and declaration of expectation the following results are possible:

| Outcome/Expectation | valid | invalid |
| ------------------- | ----- | ------- |
| valid               | +     | -       |
| invalid             | -     | +       |

#### XML Schema Expectations

`schema-valid` and `schema-invalid` items declare expectations about the outcome of an XML Schema validation on a mutation i.e. after a mutation was generated.

This allows to generate various tests about what an XML Schema should achieve.

Example 1:

We want to test that an XML Schema correctly allows an element to be optional. Hence we create a schema valid document with the optional element:

```xml
<element>with content</element>
```

We then can create a test case by removing the element and declare our expectation that the mutation still has to be schema valid:

```xml
<?xmute mutator="remove" schema-valid ?>
<element>with content</element>
```

In case the XML Schema is correct the outcome will be valid and it will meet the expectation. Hence the test result will be positive, otherwise negative.

Example 2:

We want to test that an XML Schema correctly requires an element to be always present, we only need to change our expectation:

```xml
<?xmute mutator="remove" schema-invalid ?>
<element>with content</element>
```

#### Schematron Evaluation of Expectations

`schematron-valid="some-rule-id"` and `schematron-invalid="some-rule-id"` declare expectations about the outcome of Schematron validations. The required value can be a list of space separated schematron rule identifiers and an optional schematron symbolic name. In case one or more rule-ids are listed, the expectations of only these rules will be evaluated. In case other rules fire, they will not be reported by default. Only the number of other fired rules will be mentioned.

There are two special keywords for convenience: `none` and `all` the meaning is defined as follows:

* `schematron-valid="all"`
  * All rules are expected to be valid
* `schematron-valid="none"`
  * None of the rules are expected to be valid i.e. all rules defined in a schematron fire invalid messages
and
* `schematron-invalid="all"`
  * All rules are expected to fire invalid messages
* `schematron-invalid="none"`
  * None of the rules are expected to fire messages about invalidity

Let's assume we have a Schematron rule `rule-1` if an element is present it has to have content (independent of the above question if the element is optional or required by the XML Schema). We can declare another test case based on the previous example in the same document as follows:

Simple Example:

```xml
<?xmute mutator="remove" schema-invalid ?>
<?xmute mutator="empty" schema-valid schematron-invalid="rule-1" ?>
<element>with content</element>
```

The `empty` mutator will generate a document similar to this one:

```xml
<element></element>
```

It will be Schema valid but if the Schematron `rule-1` correctly fires e.g. a fatal then it meets the expectation.

Complex Example:

We can define more than one rule to be checked per mutation:

```xml
<?xmute mutator="empty" schema-valid schematron-invalid="rule-1, rule-2, rule-3" ?>
<element>with content</element>
```

We can also give symbolic names to schematron rules in case we need to test with several schematron files:

```xml
<?xmute mutator="empty" schema-valid schematron-invalid="ubl:rule-1, ubl:rule-2, xr:rule-1, xr:rule-2" ?>
<element>with content</element>
```

Here, there are two rules from Schematron with symbolic name `ubl` and two more rules from `xr`. These symbolic names have to be defined as input to the xml-mutator.

## Mutate and Testing Result Report

Each MT-Run (Mutate and Test Run) generates a report about the mutations generated and the test results.

* It should basically be valid Markdown for copy and paste

Per original Document the output stdout looks as follows:

xrechnung-bug.xml

3 mutations: 1 expected and 2 unexpected test results

| Name   | No  | Line | Exp. | XSD Valid | XSD Exp | Sch    | Failure Text | Sch Exp | Description        |
| ------ | --- | ---- | ---- | --------- | ------- | ------ | ------------ | ------- | ------------------ |
| remove | 1   | 44   | Y    | Y         | Y       | BT1: Y |              |         | Is X correct       |
| empty  | 1   | 46   | Y    | Y         | N       | T4: N  | failure text | N       | B should not match |
| ident  | 1   | 100  | N    | N         | Y       | Xf: Y  |              | Y       | is cool            |
| "      | "   | "    | "    | "         | "       | as: N  | failure test | N       | "                  |


with columns:

* No = Number
* Line
* Exp. = Are all expectations matched
* XSD Valid = Is mutation valid against Schema?
* XSD Exp = Is result as expected?
* Sch = Is mutation valid against Schematron Rule?
* Sch Exp. = Is Schematron result as Expected?
* Description = Description of test case

Open questions:

* Do we differentiate between mutate only run and report or treat it the same as mutate and test report?
* How do we report many schematron results per mutation?
* Can we just have `XSD` as column header, knowing it is about valid or not valid XSD Schema?
