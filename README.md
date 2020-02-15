# XML-MutaTe

A declarative **XML** instance **Muta**te and **Te**st tool.

Note: GitHub is just a mirror repository. Contact https://twitter.com/renzokott 


## Build

You need Maven > 3.x and JDK > 1.8

Get source code:
```
git clone https://gitlab.com/itplr-kosit/xml-mutate.git .
```

Build jar:

```
mvn verify
```

Test it works:
```
java -jar target/xml-mutate-1.0-SNAPSHOT.jar --help
```


## Concrete Example

Usually, XML Schemas and Schematrons get checked against positive test instances i.e. XML documents which are valid.

Now, let's take a positive example and add Mutations and Tests in such a way that it makes explicit a bug in XRechnung Schematron, which can hardly be found by testing using positive examples alone.

The [XRechnung Standard 1.1](https://www.xoev.de/die_standards/xrechnung/xrechnung_versionen/xrechnung_version_1_1-15369) states on p. 49f. that Business Group (=Gruppe) "Seller Contact" should exist and have `Seller contact point BT-41`, `Seller contact telephone number BT-42`, and `Seller contact email address BT-43`. This is further expressed on p.65 with

```
BR-DE-5 Das Element „Seller contact point“ (BT-41) muss übermittelt werden. vollständig (Schematron)

BR-DE-6 Das Element „Seller contact telephone number“ (BT-42) muss übermittelt
werden. vollständig (Schematron)

BR-DE-7 Das Element „Seller contact email address“ (BT-43) muss übermittelt werden. vollständig (Schematron)
```

This is expressed by the following rules on an UBL Invoice (excerpt with different order from [XRechnung Schematron file](https://raw.githubusercontent.com/itplr-kosit/xrechnung-schematron/xrechnung-1_1-schematron-2017-12-19/validation/schematron/ubl-inv/UBL/XRechnung-UBL-model.sch)):

```xml
<param name="BG-6_SELLER_CONTACT" value="//ubl:Invoice/cac:AccountingSupplierParty/cac:Party/cac:Contact"/>

<param name="BR-DE-5" value="cbc:Name"/>
<param name="BR-DE-6" value="cbc:Telephone"/>
<param name="BR-DE-7" value="cbc:ElectronicMail"/>
```

However, the Schematron rules only require the element to be present even if it has no content.

Now, we can use a positive example and XML-MutaTe it to check this issue. We take a valid [UBL Invoice](doc/example/xrechnung-bug.xml) and annotate it with the following declarations:

```xml
<cac:Contact>
  <?xmute mutator="empty" schema-valid schematron-invalid="BR-DE-5" ?>
  <cbc:Name>[Seller contact person]</cbc:Name>

  <?xmute mutator="empty" schema-valid schematron-invalid="BR-DE-6" ?>
  <cbc:Telephone>+49 123456789</cbc:Telephone>

  <?xmute mutator="empty" schema-valid schematron-invalid="BR-DE-7" ?>
  <cbc:ElectronicMail>xxx@schulung.de</cbc:ElectronicMail>
</cac:Contact>
```

The XML-MutaTe takes each declaration and mutates the document where the content of the next element is made empty. Additionally, we declared to expect it to validate against UBL XML Schema (keyword `schema-valid`) but we expect that the XRechnung Schematron does not validate against specific rules (e.g. `schematron-invalid="BR-DE-5"`).

We check this by running:

```shell
java -jar target/xml-mutate-1.0-SNAPSHOT.jar \
  --run-mode test \
  --schema ubl xrechnung-conf/build/resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd \
  --schematron xr-ubl-in xrechnung-conf/build/resources/xrechnung/1.1/xsl/XRechnung-UBL-validation-Invoice.xsl \
  --output-dir doc/example/ \
  doc/example/xrechnung-bug.xml
```

The following report tells us that documents with empty content are accepted against our declared expectation:

```
Generated 3 mutations from 1 original document(s) in directory doc\example.


Mutation xrechnung-bug-1
XML Schema Test: Mutation has expected outcome :) result=true and expected=true

Schematron Tests:
Test of br-de-5 result:
Expected: false Actual: true
As Expected? false

Mutation xrechnung-bug-2
XML Schema Test: Mutation has expected outcome :) result=true and expected=true

Schematron Tests:
Test of br-de-6 result:
Expected: false Actual: true
As Expected? false

Mutation xrechnung-bug-3
XML Schema Test: Mutation has expected outcome :) result=true and expected=true

Schematron Tests:
Test of br-de-7 result:
Expected: false Actual: true
As Expected? false
```

For details see content of [doc/example directory](doc/example).

