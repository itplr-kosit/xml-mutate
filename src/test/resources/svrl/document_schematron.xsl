<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:brin="http://data.europa.eu/p27/eforms-business-registration-information-notice/1"
                xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                xmlns:can="urn:oasis:names:specification:ubl:schema:xsd:ContractAwardNotice-2"
                xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                xmlns:cn="urn:oasis:names:specification:ubl:schema:xsd:ContractNotice-2"
                xmlns:efac="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1"
  xmlns:efext="http://data.europa.eu/p27/eforms-ubl-extensions/1"
  xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
                xmlns:pin="urn:oasis:names:specification:ubl:schema:xsd:PriorInformationNotice-2"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:schxslt="https://doi.org/10.5281/zenodo.1495494"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">
   <rdf:Description
     xmlns:dct="http://purl.org/dc/terms/"
                     xmlns:skos="http://www.w3.org/2004/02/skos/core#">
      <dct:creator>
         <dct:Agent>
            <skos:prefLabel>SchXslt/${project.version} SAXON/HE 11.5</skos:prefLabel>
            <schxslt.compile.typed-variables xmlns="https://doi.org/10.5281/zenodo.1495494#">true</schxslt.compile.typed-variables>
         </dct:Agent>
      </dct:creator>
      <dct:created>2023-03-01T16:17:21.4034596+02:00</dct:created>
   </rdf:Description>
   <xsl:output indent="yes"/>
   <xsl:param name="EFORMS-DE-MAJOR-MINOR-VERSION" select="'1.0'"/>
   <xsl:param name="EFORMS-DE-ID"
               select="concat('eforms-de-', $EFORMS-DE-MAJOR-MINOR-VERSION)"/>
   <xsl:param name="SUBTYPES-ALL"
               select="' 1 2 3 4 5 6 E2 7 8 9 10 11 12 13 14 15 16 17 18 19 E3 20 21 22 23 24 25 26 27 28 29 30 31 32 E4 33 34 35 36 37 38 39 40 E5 '"/>
   <xsl:param name="ROOT-NODE"
               select="(/cn:ContractNotice | /pin:PriorInformationNotice | /can:ContractAwardNotice | /brin:BusinessRegistrationInformationNotice)"/>
   <xsl:param name="EXTENSION-NODE"
               select="$ROOT-NODE/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension"/>
   <xsl:param name="EXTENSION-ORG-NODE"
               select="$EXTENSION-NODE/efac:Organizations/efac:Organization"/>
   <xsl:param name="SUBTYPE-CODE-NODE"
               select="$EXTENSION-NODE/efac:NoticeSubType/cbc:SubTypeCode"/>
   <xsl:param name="schxslt.validate.initial-document-uri" as="xs:string?"/>
   <xsl:template name="schxslt.validate">
      <xsl:apply-templates select="document($schxslt.validate.initial-document-uri)"/>
   </xsl:template>
   <xsl:template match="root()">
      <xsl:param name="schxslt.validate.recursive-call"
                  as="xs:boolean"
                  select="false()"/>
      <xsl:choose>
         <xsl:when test="not($schxslt.validate.recursive-call) and (normalize-space($schxslt.validate.initial-document-uri) != '')">
            <xsl:apply-templates select="document($schxslt.validate.initial-document-uri)">
               <xsl:with-param name="schxslt.validate.recursive-call"
                                as="xs:boolean"
                                select="true()"/>
            </xsl:apply-templates>
         </xsl:when>
         <xsl:otherwise>
            <xsl:variable name="metadata" as="element()?">
               <svrl:metadata xmlns:dct="http://purl.org/dc/terms/"
                               xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                               xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
                  <dct:creator>
                     <dct:Agent>
                        <skos:prefLabel>
                           <xsl:value-of separator="/"
                                          select="(system-property('xsl:product-name'), system-property('xsl:product-version'))"/>
                        </skos:prefLabel>
                     </dct:Agent>
                  </dct:creator>
                  <dct:created>
                     <xsl:value-of select="current-dateTime()"/>
                  </dct:created>
                  <dct:source>
                     <rdf:Description>
                        <dct:creator>
                           <dct:Agent>
                              <skos:prefLabel>SchXslt/${project.version} SAXON/HE 11.5</skos:prefLabel>
                              <schxslt.compile.typed-variables xmlns="https://doi.org/10.5281/zenodo.1495494#">true</schxslt.compile.typed-variables>
                           </dct:Agent>
                        </dct:creator>
                        <dct:created>2023-03-01T16:17:21.4034596+02:00</dct:created>
                     </rdf:Description>
                  </dct:source>
               </svrl:metadata>
            </xsl:variable>
            <xsl:variable name="report" as="element(schxslt:report)">
               <schxslt:report>
                  <xsl:call-template name="d8e56"/>
               </schxslt:report>
            </xsl:variable>
            <xsl:variable name="schxslt:report" as="node()*">
               <xsl:sequence select="$metadata"/>
               <xsl:for-each select="$report/schxslt:document">
                  <xsl:for-each select="schxslt:pattern">
                     <xsl:sequence select="node()"/>
                     <xsl:sequence select="../schxslt:rule[@pattern = current()/@id]/node()"/>
                  </xsl:for-each>
               </xsl:for-each>
            </xsl:variable>
            <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                     phase="eforms-de-phase"
                                     title="Schematron Version @eforms-de-schematron.version.full@">
               <svrl:ns-prefix-in-attribute-values prefix="can"
                                                    uri="urn:oasis:names:specification:ubl:schema:xsd:ContractAwardNotice-2"/>
               <svrl:ns-prefix-in-attribute-values prefix="cn"
                                                    uri="urn:oasis:names:specification:ubl:schema:xsd:ContractNotice-2"/>
               <svrl:ns-prefix-in-attribute-values prefix="pin"
                                                    uri="urn:oasis:names:specification:ubl:schema:xsd:PriorInformationNotice-2"/>
               <svrl:ns-prefix-in-attribute-values prefix="brin"
                                                    uri="http://data.europa.eu/p27/eforms-business-registration-information-notice/1"/>
               <svrl:ns-prefix-in-attribute-values prefix="cbc"
                                                    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"/>
               <svrl:ns-prefix-in-attribute-values prefix="cac"
                                                    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"/>
               <svrl:ns-prefix-in-attribute-values prefix="ext"
                                                    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"/>
               <svrl:ns-prefix-in-attribute-values prefix="efac"
                                                    uri="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1"/>
               <svrl:ns-prefix-in-attribute-values prefix="efext" uri="http://data.europa.eu/p27/eforms-ubl-extensions/1"/>
               <svrl:ns-prefix-in-attribute-values prefix="efbc"
                                                    uri="http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1"/>
               <svrl:ns-prefix-in-attribute-values prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>
               <xsl:sequence select="$schxslt:report"/>
            </svrl:schematron-output>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="text() | @*" mode="#all" priority="-10"/>
   <xsl:template match="/" mode="#all" priority="-10">
      <xsl:apply-templates mode="#current" select="node()"/>
   </xsl:template>
   <xsl:template match="*" mode="#all" priority="-10">
      <xsl:apply-templates mode="#current" select="@*"/>
      <xsl:apply-templates mode="#current" select="node()"/>
   </xsl:template>
   <xsl:template name="d8e56">
      <schxslt:document>
         <schxslt:pattern id="d8e56">
            <xsl:if test="exists(base-uri(root()))">
               <xsl:attribute name="documents" select="base-uri(root())"/>
            </xsl:if>
            <xsl:for-each select="root()">
               <svrl:active-pattern xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                     name="cardinality-pattern"
                                     id="cardinality-pattern">
                  <xsl:attribute name="documents" select="base-uri(.)"/>
               </svrl:active-pattern>
            </xsl:for-each>
         </schxslt:pattern>
         <xsl:apply-templates mode="d8e56" select="root()"/>
      </schxslt:document>
   </xsl:template>
   <xsl:template match="$EXTENSION-NODE/efac:Organizations/efac:UltimateBeneficialOwner"
                  priority="0"
                  mode="d8e56">
      <xsl:param name="schxslt:patterns-matched" as="xs:string*"/>
      <xsl:choose>
         <xsl:when test="$schxslt:patterns-matched[. = 'd8e56']">
            <schxslt:rule pattern="d8e56">
               <xsl:comment xmlns:svrl="http://purl.oclc.org/dsdl/svrl">WARNING: Rule for context "$EXTENSION-NODE/efac:Organizations/efac:UltimateBeneficialOwner" shadowed by preceding rule</xsl:comment>
               <svrl:suppressed-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
                  <xsl:attribute name="context">$EXTENSION-NODE/efac:Organizations/efac:UltimateBeneficialOwner</xsl:attribute>
               </svrl:suppressed-rule>
            </schxslt:rule>
            <xsl:next-match>
               <xsl:with-param name="schxslt:patterns-matched"
                                as="xs:string*"
                                select="$schxslt:patterns-matched"/>
            </xsl:next-match>
         </xsl:when>
         <xsl:otherwise>
            <schxslt:rule pattern="d8e56">
               <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl">
                  <xsl:attribute name="context">$EXTENSION-NODE/efac:Organizations/efac:UltimateBeneficialOwner</xsl:attribute>
               </svrl:fired-rule>
               <xsl:if test="not(boolean(normalize-space(cac:ResidenceAddress/cbc:CityName)))">
                  <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                       location="{schxslt:location(.)}"
                                       flag="fatal"
                                       id="CR-DE-BT-513-UBO">
                     <xsl:attribute name="test">boolean(normalize-space(cac:ResidenceAddress/cbc:CityName))</xsl:attribute>
                     <svrl:text>Jeder UltimateBeneficialOwner und TouchPoint muss den Ort der Postanschrift nennen.</svrl:text>
                  </svrl:failed-assert>
               </xsl:if>
            </schxslt:rule>
            <xsl:next-match>
               <xsl:with-param name="schxslt:patterns-matched"
                                as="xs:string*"
                                select="($schxslt:patterns-matched, 'd8e56')"/>
            </xsl:next-match>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:function name="schxslt:location" as="xs:string">
      <xsl:param name="node" as="node()"/>
      <xsl:variable name="segments" as="xs:string*">
         <xsl:for-each select="($node/ancestor-or-self::node())">
            <xsl:variable name="position">
               <xsl:number level="single"/>
            </xsl:variable>
            <xsl:choose>
               <xsl:when test=". instance of element()">
                  <xsl:value-of select="concat('Q{', namespace-uri(.), '}', local-name(.), '[', $position, ']')"/>
               </xsl:when>
               <xsl:when test=". instance of attribute()">
                  <xsl:value-of select="concat('@Q{', namespace-uri(.), '}', local-name(.))"/>
               </xsl:when>
               <xsl:when test=". instance of processing-instruction()">
                  <xsl:value-of select="concat('processing-instruction(&#34;', name(.), '&#34;)[', $position, ']')"/>
               </xsl:when>
               <xsl:when test=". instance of comment()">
                  <xsl:value-of select="concat('comment()[', $position, ']')"/>
               </xsl:when>
               <xsl:when test=". instance of text()">
                  <xsl:value-of select="concat('text()[', $position, ']')"/>
               </xsl:when>
               <xsl:otherwise/>
            </xsl:choose>
         </xsl:for-each>
      </xsl:variable>
      <xsl:value-of select="concat('/', string-join($segments, '/'))"/>
   </xsl:function>
</xsl:transform>
