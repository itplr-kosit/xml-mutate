# Verfügbare Mutatoren

Im Folgenden werden Verwendungsweise und Zweck aller Mutatoren beschrieben, die in XMutate zur Verfügung stehen. 
Hierbei sind mandatorische Parameter (Pflichtangaben) **fett** gesetzt, während optionale Angaben in eckige Klammern gesetzt - z. B. [`parameter`\] - dargestellt werden. 
Die Reihenfolge, in welcher die jeweiligen Parameter eines Mutators verwendet werden, ist unerheblich.
Für einige Mutatoren existieren alternative Bezeichner, so genannte Aliase, 
die (bei identischer Parametrisierung und Funktionsweise) an ihrer Statt verwendet werden können. 

+ [ ] TODO: ist die Reihenfolge der Parameterverwendung relevant?


## add
### Zweck
Fügt im Zieldokument an der Aufrufstelle ein Element hinzu.

### Parameter
(Keine)

### Beispiel
```xml
 <?xmute mutator="add" ?>
```

### TODO
+ [ ] TODO: Sollte "add-element" heißen, weil es auch "add-attribute" geben könnte/sollte
+ [ ] TODO: Fehlender mandatorischer Parameter `name` (Bezeichner des Zielknotens)
+ [ ] TODO: Möglicher optionaler Parameter: `text-content` (Inhalt des Textknotens des neuen Elements)
+ [ ] TODO: Möglicher optionaler Parameter: `content-from-xpath` (evaluiert den XPath-Ausdruck und fügt dessen Ergebnis als Textknoten-Inhalt des neuen Elementes ein)   


## alternative
### Zweck
Iteriert durch sämtliche Kommentarknoten, die direkte Kinder eines Elementes sind, und entfernt bei 
jeweils einem von ihnen die Kommentarierung  ("un-kommentiert" diesen also); der Knoten, dessen Kommentierung 
entfernt wird, wechselt dabei mit jeder Iteration. Es wird für jeden Kommentaknoten eine Mutation erzeugt. 

Dieser Mutator ist beispielsweise nützlich, um unaufwändig Varianten aufgrund von Choice-Strukturen zu betesten.   

### Parameter
(Keine)

### Beispiel
...

### TODO
+ [ ] Beispiel erstellen


## change-text-content
### Zweck
Ändert den Textknoten-Inhalt desjenigen Elementes, das unmittelbar auf den Mutator-Aufruf folgt. Der neue Inhalt 
wird in `values` mitgeteilt. Werden mehrere Inhalte übergeben (getrennt durch einen definierbaren 'separator'), 
entsteht mit jedem dieser Inhalte eine eigenes Zieldokument (Mutation).

In einem Aufruf ohne explizite Separator-Deklaration wird ein Komma als Trennsymbol erwartet.

## Parameter
* **`values`**: Textinhalt (oder eine Liste von Inhalten).
* [`separator`\]: Optionale Angabe eines Trennzeichens, mittels dessen die einzelnen Listeneinträge in `values` voneinander unterschieden werden.

## Beispiele

```xml
<?xmute mutator="change-text-content" values="Listeneintrag Nr. 1*und ein weiterer" separator="*" ?>
```


## ch-text
###Zweck
`ch-txt` ist ein Alias für den Mutator [`change-text-content`](#change-text-content) und kann synonym verwendet werden.


## Code
### Zweck
Erzeugt für jedes gegebene Literal einer Codeliste ein eigenes Zieldokument (Mutation), 
in welchem dieses als Textknoten-Inhalt ausgegeben wird. Auf diese Weise kann die Validität 
sämtlicher Einträge einer Codeliste in einem gegebenen Kontext geprüft werden.

###Parameter
* [`values`\]: optional, comma separated list of code values to use. This is for simple codelists or even simple list of values.
* [`genericode`\]: optional, URI of a genericode file with code values to test. This is for more complex codelists.
* **`codeKey`** required for genericode, the name of the code key column to use for values of a genericode code list
* [`attribute`\]: optional, the name of the attribute to mutate. If not configured the text content of the target element will be mutated

### Beispiele
...

### TODO
+ [ ] Mehrere Beispiele (Varianten) erstellen


## Empty
### Zweck
Funktion: Entfernt den Textknoten-Inhalt des nachfolgenden Elementes oder Attributes vollständig; der Zielknoten (also das Element oder Attribut selbst) bleibt jedoch erhalten.

### Parameter
(Keine)

### Beispiel
...

### TODO
+ [ ] Parameter prüfen
+ [ ] Stimmt die Beschreibung?
+ [ ] Beispiel erstellen


## Identity
## Zweck
Test XML Schema and Schematron expectations on the unchanged original document.

### Parameter
...

### Beispiel
...

### TODO
+ [ ] Paramter auflisten
+ [ ] Beispiel erstellen


## Remove
### Zweck
Entfernt ein Element oder Attribut.
Default: Removes next element.
Required items: none

### Parameter
...

### Beispiele
```xml 
<?xmute mutator="remove" attribute="my-attr1" attribute="my-attr2" ?>
```

### TODO
+ [ ] Parameter beschreiben
+ [ ] Beispiele erstellen


## XSL
### Zweck
Mutate a document by transformation via XSLT which easily allows to define more complex mutations across many elements at different nesting levels,

### Paramter
* `name`: Symbolic name of the XSLT script. Tha mapping of name to script can be given at CLI with 1 to many `--xsl ${name}=${path_to_script_file}`
* `param-${name}`: param to be passed verbatim to the XSLT script e.g. `param-num="3"` pass the value 3 to the XSLT parameter with the name `num`.

### Beispiele
...

### TODO
+ [ ] Zweck vervollständigen
+ [ ] Parameter beschreiben
+ [ ] Beispiele erzeugen