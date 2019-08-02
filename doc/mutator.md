# Documentation of available mutators


## Empty

Name: "empty"

Purpose: Removes all contet from an element.

Default: Removes all content from next element.

Required items: none

Example:



## Remove Element

Name: "remove"

Purpose: Removes an element.

Default: Removes next element.

Required items: none

Example:

## Change Text Content

Name: "change-text-content" abbreviation "ch-txt"

Purpose: Changes text content of an element or attribute.

Default: Changes text content of next element with values given in `values` separated by comma `,`.

Required items: "values"

Optional items: "separator"

Example:

```xml
 <?xmute mutator="ch-txt" values="new text content 1, and after comma a second text content" separator="," ?>
```

## Add Element

in progress

Name: "add"

Purpose: Adds an element

## Codelisten Mutator

Name: "code"

Purpose: Iterate through list of codes and test validity

Configuration:
* "_values_": optional, comma separated list of code values to use. This is for simple codelists or even simple list of values.
* "_genericode_": optional, URI of a genericode file with code values to test. This is for more complex codelists.
* "_codeKey_"_ required for genericode, the name of the code key column to use for values of a genericode code list
* "_attribute_": optional, the name of the attribute to mutate. If not configured the text content of the target element will be mutated


## Alternative Mutator
Name: "alternative"

Purpose: Iterates through comment children  of the target node and mutually uncomments them.

Configuration: none 

## Noop-Mutator
Name: "noop"

Purpose: Test the original document validity