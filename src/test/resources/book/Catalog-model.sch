<pattern xmlns="http://purl.oclc.org/dsdl/schematron" abstract="true" id="model">
    <rule context="$BOOK">
        <assert test="$Book-1" flag="fatal" id="Book-1">[Book-1] A book must always have a publisher.</assert>
        <assert test="$Book-2" flag="fatal" id="Book-2">[Book-2]  A book must always have a number of chapters.</assert>
   </rule>
</pattern>