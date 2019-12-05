# Verfügbare Mutatoren

Im Folgenden werden Struktur und Zweck aller Mutatoren beschrieben, die in XMutate zur Verfügung stehen. Hierbei sind mandatorische Parameter (Pflichtangaben) **fett** gesetzt, während optionale Angaben *kursiv* dargestellt werden. Die Reihenfolge, in welcher die jeweiligen Parameter eines Mutators verwendet werden, ist unerheblich.

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
## Empty

Funktion: Entfernt den Textknoten-Inhalt des nachfolgenden Elements vollständig; der Elementknoten (also das Element selbst) bleibt erhalten.

Parameter: (Keine)

Beispiel:

## Remove

Name: "remove" // TODO: Sollte "remove-element" heißen, weil es auch remove-attribute geben könnte/sollte

Purpose: Removes an element.

Default: Removes next element.

Required items: none

Example:


## Add

in progress

Name: "add" // TODO: Sollte "add-element" heißen, weil es auch "add-attribute" geben könnte/sollte

Purpose: Adds an element

## Code

Name: "code"

Purpose: Iterate through a list of codes and test validity (Codelisten..)

Configuration:
* "_values_": optional, comma separated list of code values to use. This is for simple codelists or even simple list of values.
* "_genericode_": optional, URI of a genericode file with code values to test. This is for more complex codelists.
* "_codeKey_"_ required for genericode, the name of the code key column to use for values of a genericode code list
* "_attribute_": optional, the name of the attribute to mutate. If not configured the text content of the target element will be mutated

## Alternative

Name: "alternative"

Purpose: Iterates through comment children of the target node and mutually un-comments them.

Configuration: none

## Identity

Name: "identity"

Purpose: Test XML Schema and Schematron expectations on the unchanged original document.

## XSL

Name: "xsl"

Purpose: Mutate a document by transformation via XSLT which easily allows to define more complex mutations across many elements at different nesting levels,

Configuration:

* `name`: Symbolic name of the XSLT script. Tha mapping of name to script can be given at CLI with 1 to many `--xsl ${name}=${path_to_script_file}`
* `param-${name}`: param to be passed verbatim to the XSLT script e.g. `param-num="3"` pass the value 3 to the XSLT parameter with the name `num`.
