<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
defaultPhase="active-phase">
  <phase id="active-phase">
    <active pattern="first_pattern" />
    <active pattern="second_pattern" />
  </phase>
  <pattern id="first_pattern">
    <rule context="/*/book">
      <assert id="bookWithPageCount" flag="error" test="exists(@pagecount)">A book must have a pagecount attribute</assert>
    </rule>
    <rule context="/*/magazine">
      <assert id="magazineWithArticleCount" flag="error" test="exists(@articlecount)">A magazine must have an articlecount attribute</assert>
    </rule>
  </pattern>
  <pattern id="second_pattern">
    <rule context="/*/*">
      <assert id="anyWithCodeLength" flag="error" test="string-length(@code) eq 4">A code must be 4 characters long</assert>
    </rule>
  </pattern>
</schema>
