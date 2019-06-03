# XML-MutaTe

A declarative **XML** instance **Muta**ting and **Te**sting tool.

## Motivation/Background

There are several tools available to generate XML instance documents using XML Schema. These tools are very good in generating random documents within the constraints of the schema i.e. generating valid instances.
The generated content is most often not meaningful and does not reflect business requirements and business cases.
However, not only during the development of XML Schema definition languages it is often very important to additionally test schema definitions against invalid instances. Only then one can make sure that certain constraints/rules

* correctly exclude unwanted content and do not include content as **False Positives**,
* correctly inlcude all valid content and do not exclude **False Negatives**

However, the maintainance of a large number of meaningful test instances often becomes cumbersome for several reasons:

* One needs to manage large number of XML instances for testing
* One needs to define new schemas or one requires invasive changes to existing schemas to annotate the XML test instances with assertions about test cases

## The declarative approach

XML-MutaTe takes a declarative non-invasive approach by allowing test writers to annotate original XML test instances with XML processing instructions. Original valid test instances can for example contain real business data. These specific instructions allow to define certain mutations which should be applied to original instances in order to generate new test instances as variations of the original instance on the fly. Moreover, a test writer can use all those instructions to make certain assertions about the validity of the mutated instances.

These can look like this for example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xmute testsuite="Real business case" ?>
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
    <?xmute mutator="remove-element" group="1" description="dieses soll x testen" recursive="yes" if="" ?>
    <cac:AccountingSupplierParty>
        <cac:Party>
            <cac:PartyName>
                <cbc:Name>[Seller trading name]</cbc:Name>
            </cac:PartyName>
            <cac:PostalAddress>
                <!-- Generate new instances each with new random order of the following sibling elements-->
                <?xmute mutator="randomize-element-order" xpath="."  group="1" schematron-invalid="bt-br-03" ?>
                <cbc:StreetName>[Seller address line 1]</cbc:StreetName>
                <!-- Generate a new instance with next element content being empty -->
                <?xmute mutator="empty" xpath="." schema-valid  schematron-invalid="bt-br-03" ?>
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
* laengen muator
* character mutator: generiere sequenz von charactern, die aber Regeln entsprechen oder auch voellig random (koennte den laengen mutator beinhalten)
* xslt mutator
* eigene java based mutator (plugin maessig)

Externalisieren von mutator "content" (da womit ein  mutator arbeiten soll) in externe files a la yaml etc


[Documentation of available Mutators](../doc/mutator.md).

## Features

* Generate persisted mutations i.e. files of mutations
* Check valid mutator declarations
* mutators can be written in XSLT

## Architecture/Design

There are three app runtime modes:

1. Generate mutations mode (the default): Generate mutations only
2. Test and generate mutations mode: Generate mutations and test the resulting instances
3. Check mode: Check that xmute instructions are syntactically correct and exectuable

### Mutation mode

One mutator after the other in the order of appearance in the XML tree.
Each independent of the other.

Per default a mutator gets executed on the next element sibling, otherwise xpath has to be defined. This xpath is then relative to the context in which the mutator is declared.

### Mutate and Test mode

Mutates like in Mutation mode and also tests each generated instance against XML Schema and Schematron.

### Application User Interface

Command line interface

* following GNU best practices and conventions

### XMute Instructions

Only XML processing instructions with name `xmute` are processed.

The general data structure of an instruction is a list of `key="value"` configuration items as shown in this example:

```xml
<?xmute mutator="randomize-element-order" xpath="."
        schema-valid schematron-invalid="bt-br-03" ?>
```

All item keys are interpreted case-insensitive. Each item value must be surrounded by quotes `"`. Sometimes `value` is optional.

#### Mutations

One and only one `mutator` key(word) is mandatory where value is the name of the mutator to be applied e.g. `mutator="empty"`.

There might be additional `key=value` items configuring the behavior of the mutator.

#### Testing

Each mutation (i.e. mutated document) is validated against XML Schema and Schematron if run in default mutate and test mode.

`schema-valid` and `schema-invalid` items declare expectations about the outcome of an XML Schema validation.
If no symbolic name (should be but does not need to be equal to namespace prefix) is given then `schema-valid` and `schema-invalid` are expectations about the default outcome of XML Schema validation.

`schematron-valid="bt-1,ubl:bt-1"` and `schematron-invalid="xrech:bt-2 ubl:bt-1"` declare expectations about the outcome of Schematron validations. The optional value can be a list of schematron rule identifier and optional schematron symbolic name.

### Mutate and Testing Reuslt Report

Each run MT-Run (muatate and Test Run) generates a report about the mutations generated and the test results.
* It should be basically be valid Markdown for copy and paste


Per original Document the output stdout looks as follows:


xrechnung-bug.xml

3 mutations: 1 expected and 2 unexpected test results

| No  | Line | Exp. | XSD Valid | XSD Exp. | Sch    | Sch Exp | Description        |
| --- | ---- | ---- | --------- | -------- | ------ | ------- | ------------------ |
| 1   | 44   | Y    | Y         | Y        | BT1: Y | Y       | Is X correct       |
| 2   | 46   | Y    | Y         | N        | T4: N  | N       | B should not match |
| 3   | 48   | N    | N         | Y        | Xf: Y  | Y       | is cool            |

with columns:

* No = Number
* Line
* Exp. = Are all expectation matched
* XSD Valid = Is mutation valid against Schema?
* XSD Exp = Is result as expected?
* Sch = Is mutation valid against Schemtron Rule?
* Sch Exp. = Is Schematron result as Expected?
* Description = Description of test case


Open questions:

* Do we differetiate between mutate only run and report or treat it the same as muatate and test report?
* How do we report many schematron results per mutation?
* Can we just have `XSD` as column header, knowing it is about valid or not valid XSD Schema?
