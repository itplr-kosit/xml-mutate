# Documentation of available mutators

## Add Mutator (TODO Issue #2)
| | |  
|---|---|  
| Name | "add" |  
| Purpose| Adds an element |  
| Default|  |  
| Required items|none|
| Optional items| none|

**Example**: (in development)

## Alternative Mutator (not working)

| | |  
|---|---|  
| Name | "alternative" |  
| Purpose| This mutator operates on a set of children comments, which are used as alternatives to the target element. |  
| Default|  |  
| Required items|none|
| Optional items| none|

**Example**: (in development)
 
## Code Mutator
| | |  
|---|---|  
| Name | "code" |  
| Purpose| Changes text content of an element or attribute with values given in `values` separated by comma `,`|  
| Default| Changes text content of target element creating 4 mutations: 1) Same text length as actual content  2) Max length of 10.000 3) Length of 9.999 4) Length of 10.001 |  
| Required items| "values" |
| Optional items| "separator" , "attribute", "trim"|

**Configuration**:
* `values`:  separated list of code values to use. 
* `separator`: the separator for the values property, default is `,`
* `trim`: if the codes provided in `values` should be trimmed or not
* `attribute`: the name of the attribute which text content should be replaced


**Examples**:

Values given
```xml
 <?xmute mutator="text" values="01, 02, 03" schema-valid schematron-valid = "schematron:BR-DE-2"?>
```
Values and separator given
```xml
 <?xmute mutator="text" values="01|02|03" separator ="|" schema-valid schematron-valid = "schematron:BR-DE-2"?>
```

## Empty Mutator

| | |  
|---|---|  
| Name | "empty" |  
| Purpose| Mutator for emptying an element. If the element to be emptied contains any subelements, they will be removed. If it contains text it will be removed.  |  
| Default| Empty next element |  
| Required items| none|
| Optional items| none|

**Example**:
```xml
<?xmute mutator="empty" schema-valid schematron-valid = "schematron:BR-DE-2" ?>
```    

## GeneriCode Mutator

| | |  
|---|---|  
| Name | "genericcode"|  
| Purpose| Iterate through a code list and create as many mutations as codes present in the code list. If no attribute parameter has been given, the text content of the target node will be replace with the code. |  
| Default| Replacement of the text content. |  
| Required items|"genericode" , "codekey"|
| Optional items| "attribute"|

**Configuration**:
* `genericode` : URI of a genericode file with code values to test. The URI can be an absolute URI (e.g. loaded from web), a path relative to the document or path relative to current working directory. 
* `codeKey` : the name of the code key column to use for values of a genericode code list
* `attribute` : the name of the attribute to mutate. If not configured the text content of the target element will be mutated

**Example**:
```xml
<?xmute mutator="code" genericode="https://www.xrepository.de/api/xrepository/urn:de:xoev:codeliste:erreichbarkeit_3:technischerBestandteilGenericode"  codekey="SimpleValue" schema-valid schematron-valid = "schematron:BR-DE-2" ?>
```       

## Identity Mutator
| | |  
|---|---|  
| Name | "identity" or "noop" |  
| Purpose| Test XML Schema and Schematron expectations on the unchanged original document.|  
| Default| Purpose with without different behaviours |  
| Required items|none|
| Optional items| none|

**Example**:
```xml
<?xmute mutator="noop" schema-valid schematron-valid = "schematron:BR-DE-2" ?>
```  


## Length Mutator
| | |  
|---|---|  
| Name | "length" |  
| Purpose| Changes text content of an element or attribute with random values but a certain min or max length.|  
| Default| Changes text content of target element creating 4 mutations: 1) Same text length as actual content  2) Max length of 10.000 3) Length of 9.999 4) Length of 10.001 |  
| Required items|  |
| Optional items| "min-length", "max-length"|

**Configuration**:
* `min-length`: the minimum length for the random generated text content
* `max-length`: the maximum length for the random generated text content

**Examples**:

Default behaviour
```xml
 <?xmute mutator="text" schema-valid schematron-valid = "schematron:BR-DE-2"?>
```
Min and max length given
```xml
 <?xmute mutator="text" min-length="10" max-length="100" schema-valid schematron-valid = "schematron:BR-DE-2"?>
```


## Remove Mutator
| | |  
|---|---|  
| Name | "remove" |  
| Purpose| This mutator deletes the target element from the document and replaces it with a comment. This mutator can also delete attributes. |  
| Default| Removes next element. |  
| Required items| none|
| Optional items| "attribute"|

**Configuration**:
* `attribute` : the name of the attribute to remove. If not set the target node will be removed. If several attributes shall me be removed, property "attribute" must be repeatead.

**Example**:
```xml
<?xmute mutator="remove" attribute="language" attribute "country" schema-valid schematron-valid = "schematron:BR-DE-2" ?>
```  

## XSLT Mutator
| | |  
|---|---|  
| Name | "xslt" |  
| Purpose| Mutate a document by transformation which easily allows to define more complex mutations across many elements at different nesting levels.|  
| Default|  |  
| Required items| "template"|
| Optional items| "param-${name}"|

**Configuration**:

* `template`: Symbolic name of the XSLT script. Tha mapping of name to script can be given at CLI with 1 to many `--xsl ${name}=${path_to_script_file}`
* `param-${name}`: param to be passed verbatim to the XSLT script e.g. `param-num="3"` pass the value 3 to the XSLT parameter with the name `num`.

**Example**:

```xml
<?xmute mutator="xslt" schema-valid template="simple.xsl" param-someParameter="should_be_transferred"?>
```

## Whitespace Mutator
| | |  
|---|---|  
| Name | "whitespace" |  
| Purpose| Adds randomly generated whitespaces (` `, `\n` or `\t`) with a given length as prefix or suffix of a text content, or it replaces whole text content with whitespaces |  
| Default| Create 3 mutations with a combination of all whitespaces with a length of 5 and: 1) Adds as a prefix 2) Adds as a suffix 3) Replaces text cotent|  
| Required items|none|
| Optional items| "position", "length", "list"|

**Configuration**:
* `position`: defines where to add the whitespace characters. Possible values "suffix", "prefix", "replace" or "mix" (will produce default behaviour) 
    * Values can be written in small or capital letters.
    * Can be declared more than once.
    * Can have more than one value (repeated ones will be ignored)
* `length`: defines how many whitespace characters are to be added.  Default value is "5"
    * Can only be declared once.
    * Can only have one value.
    * Can only be an integer value.
* `list`: defines which whitespace characters are to be added. Possible values "space", "tab" or "newline". Default is all of them
    * Values can be written in small or capital letters.
    * Can be declared more than once.
    * Can have more than one value (repeated ones will be ignored)
    
    
**Examples**:

Default behaviour (position="mix", length="5", list="space,tab,newline")
```xml
<?xmute mutator="whitespace" schema-valid?>
```
Only prefix and sufix, length 10 and only spaces and newlines
```xml
<?xmute mutator="whitespace" position="prefix, sufix" length="10" list="space,newline" schema-valid?>
```