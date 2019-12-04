<pattern xmlns="http://purl.oclc.org/dsdl/schematron" abstract="true" id="model">
    <rule context="$BOOK(: //book :)">
        <assert test="$Book-1(: publisher :)" role="fatal" id="Book-1">[Book-1] A book must always have a publisher.</assert>
        <assert test="$Book-2(: chapters :)" role="fatal" id="Book-2">[Book-2] A book must always have a number of chapters.</assert> <!-- war: @flag statt @role! -->        
   </rule>
</pattern>