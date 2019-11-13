<?xml version="1.0" encoding="UTF-8"?>

<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematron Book</title>

    <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>

    <phase id="Catalog_1.1_model">
        <active pattern="Book-model"/>
    </phase>

    <!-- Abstract CEN BII patterns -->
    <!-- ========================= -->
    <include href="Catalog-model.sch"/>

    <!-- Data Binding parameters -->
    <!-- ======================= -->
    <include href="Catalog-Book-model.sch"/>
    
</schema>