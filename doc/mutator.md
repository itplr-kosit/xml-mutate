# Documentation of available mutators


## Empty

Name: "empty"

Purpose: Removes all contet from an element.

Default: Removes all content from next element.

Required declarations: none

Example:



## Remove Element

Name: "remove"

Purpose: Removes an element.

Default: Removes next element.

Required declarations: none

Example:

## Change Text Content

Name: "change-text-content" abbreviation "ch-txt"

Purpose: Changes text content of an element or attribute.

Default: Changes text content of next element with values given in `values` separated by comma `,`.

Required declarations: "values"

Optional declarations: "separator"

Example:

```xml
 <?xmute mutator="ch-txt" values="new text content 1, and after comma a second text content" separator="," ?>
```

## Add Element

in progress

Name: "add"

Purpose: Adds an element

