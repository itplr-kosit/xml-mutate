<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  queryBinding="xslt2" defaultPhase="eforms-de-phase">

  <title>Schematron Version @eforms-de-schematron.version.full@</title>

  <!-- working on four different UBL Structure -->
  <ns prefix="can"
    uri="urn:oasis:names:specification:ubl:schema:xsd:ContractAwardNotice-2" />
  <ns prefix="cn"
    uri="urn:oasis:names:specification:ubl:schema:xsd:ContractNotice-2" />
  <ns prefix="pin"
    uri="urn:oasis:names:specification:ubl:schema:xsd:PriorInformationNotice-2" />
  <ns prefix="brin"
    uri="http://data.europa.eu/p27/eforms-business-registration-information-notice/1" />
  <!-- And the subordinate namespaces -->
  <ns prefix="cbc"
    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" />
  <ns prefix="cac"
    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" />
  <ns prefix="ext"
    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2" />
  <ns prefix="efac"
    uri="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1" />
  <ns prefix="efext" uri="http://data.europa.eu/p27/eforms-ubl-extensions/1" />
  <ns prefix="efbc"
    uri="http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1" />
  <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema" />

  <phase id="eforms-de-phase">
    <active pattern="cardinality-pattern" />
  </phase>

  <let name="EFORMS-DE-MAJOR-MINOR-VERSION" value="'1.0'" />
  <let name="EFORMS-DE-ID"
    value="concat('eforms-de-', $EFORMS-DE-MAJOR-MINOR-VERSION)" />
  <let name="SUBTYPES-ALL"
    value="' 1 2 3 4 5 6 E2 7 8 9 10 11 12 13 14 15 16 17 18 19 E3 20 21 22 23 24 25 26 27 28 29 30 31 32 E4 33 34 35 36 37 38 39 40 E5 '" />
  <!-- let us name each variable which contains an xpath with suffix NODE (XML lingo for general name XML parts like attribute, element, text, comment,...  -->
  <let name="ROOT-NODE"
    value="(/cn:ContractNotice | /pin:PriorInformationNotice | /can:ContractAwardNotice | /brin:BusinessRegistrationInformationNotice)" />
  <let name="EXTENSION-NODE"
    value="$ROOT-NODE/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension" />

  <let name="EXTENSION-ORG-NODE"
    value="$EXTENSION-NODE/efac:Organizations/efac:Organization" />
  <let name="SUBTYPE-CODE-NODE"
    value="$EXTENSION-NODE/efac:NoticeSubType/cbc:SubTypeCode" />


  <pattern id="cardinality-pattern">
    <rule context="$EXTENSION-NODE/efac:Organizations/efac:UltimateBeneficialOwner">
    <assert id="CR-DE-BT-513-UBO" test="boolean(normalize-space(cac:ResidenceAddress/cbc:CityName))">
      Failed: <value-of select="cac:ResidenceAddress/cbc:CityName"/>
    </assert>
    </rule>
  </pattern>

</schema>
