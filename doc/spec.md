# XML-MutaTe

A declarative **XML** instance **Muta**ting and **Te**sting tool.

## Motivation/Background

There are several tools available to generate XML instance documents using XML Schema. These tools are very good in generating random documents within the constraints of the schema i.e. generating valid instances.
The content is then most often not meaningful and do not reflect business requiremetns and business cases.
Additionally, it is often very important to also test schema defintions against invalid instances during the development of XML schema definition languages. Only then one can make sure that certain constraints/rules 

* correctly exclude unwanted content and do not include content as **False Positives**,
* correctly inlcude all valid content and do not exclude **False Negatives**

However, the maintainance of a large number of meaningful test instances often becomes cumbersome for several reasons:

* One needs to manage large number os XML instances for testing
* One needs to define new schemas or requiring invasive changes to existing schemas to annotate the XML test instances with assertions about test cases

## The declarative approach

XML-MutaTe takes a declarative non-invasive approach by allowing test writer to annotate original XML test instances with XML processing instructions. Original valid test instances can for example contain real business data. These specific instructions allow to define certain mutations which should be applied to original instances in order to generate new test instances as variations of the original instance on the fly. Moreover, a test writer can all use these instructions to make certain assertions about the validity of the mutated instances.

These can look like this for example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xmute testsuite="Real bsuniss case" ?>
<!-- namespace declartions omitted for brevity -->
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
    <?xmute mutator="remove-element" group="1" recursive="yes"?>
    <cac:AccountingSupplierParty>
        <cac:Party>
            <cac:PartyName>
                <cbc:Name>[Seller trading name]</cbc:Name>
            </cac:PartyName>
            <cac:PostalAddress>
                <!-- Generate new instances each with new random order of the following sibling elements-->
                <?xmute mutator="randomize-element-order" xpath="."  group="1" schematron-invalid="bt-br-03" ?>
                <cbc:StreetName>[Seller address line 1]</cbc:StreetName>
                <!-- Generate a new instance with next element being empty -->
                <?xmute mutator="empty" xpath="." schema-valid  schematron-invalid="bt-br-03" ?>
                <cbc:CityName>bremen</cbc:CityName>
                <!-- Rest omitted for brevity -->

```

This applied to valid instances, keep them valid and the XML instances can be used as usual by just igonoring the xmute processng instructions.

This way many kinds of mutations can be defined and combined with assertions about the validity of the mutated instances.

## List of possible mutators


## Features


## Architecture/Design

There are three processing modes:

1. Mutation mode: Generate mutations only
2. Test run mode: Generate mutations and test the resulting instances
3. And a check that xmute instructions are syntactically correct and exectuable 