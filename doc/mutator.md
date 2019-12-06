# Aufruf
XMutate wird innerhalb des XML-Quelldokuments, auf welches Mutatoren angewandt werden sollen, 
unter Verwendung diverser Parameter als _processing instruction_ aufgerufen:  

```xml
<?xmute mutator="{Mutatorname}" [attribute="{Attributname}"] [values="{Wert|Wert1, Wert2, ...}"] [seperator="{Trennzeichen}"]
[schema-valid|schema-invalid] [schematron-valid|schematron-invalid="{SCH-Regel-ID}"] ?>
```  

## TODO
+ [ ] vervollständigen, vgl. [Ticket #65](https://projekte.kosit.org/kosit/xml-mutate/issues/65)

# Verfügbare Mutatoren
Im Folgenden werden Verwendungsweise und Zweck aller Mutatoren beschrieben, die in XMutate zur Verfügung stehen. 
Hierbei sind mandatorische Parameter (Pflichtangaben) **fett** gesetzt, während optionale Angaben in eckige Klammern gesetzt - z. B. [`parameter`\] - dargestellt werden. 
Die Reihenfolge, in welcher die jeweiligen Parameter eines Mutators verwendet werden, ist unerheblich.
Für einige Mutatoren existieren alternative Bezeichner, so genannte Aliase, 
die (bei identiscser Parametrisierung und Funktionsweise) an ihrer Statt verwendet werden können. 

+ [ ] TODO: ist die Reihenfolge der Parameterverwendung relevant?


## add
### Zweck
Fügt im Zieldokument an der Aufrufstelle ein Element oder Attribut hinzu.

### Parameter
(Keine)

### Beispiel
```xml
<?xmute mutator="add" ... ?>
```

### TODO
+ [ ] TODO: Können tatsächlich Elemente und auch Attribute hinzugefügt werden?
+ [ ] TODO: Fehlender mandatorischer Parameter `name` (Bezeichner des Zielknotens)?
+ [ ] TODO: Möglicher optionaler Parameter: `text-content` (Inhalt des Textknotens des neuen Elements)
+ [ ] TODO: Möglicher optionaler Parameter: `content-from-xpath` (evaluiert den XPath-Ausdruck und fügt dessen Ergebnis als Textknoten-Inhalt des neuen Elementes ein)   


## alternative
### Zweck
Iteriert durch sämtliche Kommentarknoten, die direkte Kinder eines Elementes sind, und entfernt bei 
jeweils einem von ihnen die Kommentarierung  ("un-kommentiert" diesen also); der Knoten, dessen Kommentierung 
entfernt wird, wechselt dabei mit jeder Iteration. Es wird für jeden vorhandenen Kommentarknoten 
ein neues Zieldokument (Mutation) erzeugt. 

Dieser Mutator ist beispielsweise nützlich, um unaufwändig mögliche Varianten aufgrund von Choice-Strukturen zu betesten.   

### Aufruf
```xml
<?xmute mutator="alternative" ?>
```

### Parameter
(Keine)

### Beispiel
...

### TODO
+ [ ] Beispiel erstellen


## change-text-content
### Zweck
Ändert den Textknoten-Inhalt desjenigen Elementes, das unmittelbar auf den Mutator-Aufruf folgt, 
bzw. - sofern angegeben - eines seiner Attribute. Der neue Inhalt wird in `values` mitgeteilt. 
Werden mehrere Inhalte übergeben (getrennt durch einen definierbaren 'separator'), 
entsteht mit jedem dieser Inhalte ein eigenes Zieldokument (Mutation).

In einem Aufruf ohne explizite Separator-Deklaration wird ein Komma als Trennsymbol erwartet.

### Aufruf
```xml
<?xmute mutator="change-text-content" values="{Textinhalt}" [attribute="{Attributname}"] [separator="{Trennzeichen}"] ?>
```

### Parameter
* **`values`**: Textinhalt (oder eine Liste von Inhalten).
* [`attribute`\]: Bezeichner des Attributknotens, dessen Inhalt geändert werden soll 
* [`separator`\]: Optionale Angabe eines Trennzeichens, mittels dessen die einzelnen Listeneinträge in `values` voneinander unterschieden werden. Das Trennzeichen muss aus genau einem Zeichen bestehen.

### Beispiele
#### Beispiel 1
```xml
<?xmute mutator="change-text-content" values="Montag ist Ruhetag*An Feiertagen geschlossen" separator="*" ?>
<hinweis>Dieser vorhandene Textinhalt wird ersetzt</hinweis>
```
erzeugt zwei Mutationen, in deren erster das Element `hinweis` den Inhalt "Montag ist Ruhetag" und im der zweiten 
"An Feiertagen geschlossen" hat.     

#### Beispiel 2
```xml
<?xmute mutator="change-text-content" attribute="datum" values="{current-date()}" ?>
<zeitstempel datum="Platzhalter"/>
```

erzeugt im Zieldokument als Inhalt von zeitstempel/@datum das zur XMutate-Laufzeit aktuelle Datum.

## TODO
+ [ ] Beispiele auf Richtigkeit prüfen, insbesondere die Verwendbarkeit des XPath-Ausdrucks in Beispiel 2!


## ch-text
### Zweck
`ch-txt` ist ein Alias für den Mutator [`change-text-content`](#change-text-content) und kann synonym verwendet werden.


## code
### Zweck
Erzeugt für eine gegebene Menge Codelisten-Literale je ein eigenes Zieldokument (Mutation), 
in welchem das betreffende Literal als Textknoten-Inhalt ausgegeben wird. Auf diese Weise kann die Validität 
der Codeliterale in einem gegebenen Kontext geprüft werden.

Es können entweder ein bzw., kommasepariert, mehrere Codelisterale explizit angegeben werden mittels `values`, oder 
unter Verwendung von `genericode` und `codeKey` auf die Spalte einer Genericode-Liste verwiesen werden, deren sämtliche 
Literale eingesetzt werden sollen.

### Aufruf
```xml
<?xmute mutator="code" [attribute="{Attributname}"] ( values="Wert1, Wert2, ..." | ( genericode="{Genericode-URI}" codeKey="" )) ?>
```

### Parameter
* [`values`\]: Kommaseparierte Liste einzusetzender Codeliterale (für eine selektive Auswahl).
* [`genericode`\]: URI der Genericode-Dateifile, deren Codeliterale eingesetzt werden sollen (iteriert durch sämtliche Codeliterale).
* **`codeKey`** required for genericode, the name of the code key column to use for values of a genericode code list
* [`attribute`\]: optional, the name of the attribute to mutate. If not configured the text content of the target element will be mutated

### Beispiele
#### Beispiel 1
```xml
<?xmute mutator="code" attribute="language" values="DE, EN, FR" ?>
<GuiSettings language="RU">...</GuiSettings>
```

erzeugt drei Mutationen des Quelldokuments, in denen der Textinhalt des Attributs GuiSettings/@language jeweils den 
Wert "DE", "EN" bzw. "FR" annimmt.

#### Beispiel 2
```xml
<?xmute mutator="code" genericode="bundesländer.xml" codeKey="SCHLUESSEL" ?>
<bundesland>00</bundesland>
```

Erzeugt für jedes in der referenzierten Genericode-Datei `bundesländer.xml` vorhandene Codeliteral in der Spalte `SCHLUESSEL` 
eine Mutation, in welcher der Textknoten das Elements `bundesland` dem jeweiligen Literal entspricht.


### TODO
+ [ ] Parameter "CodeKey" in "codekey" oder "code-key" umbenennen
+ [ ] Beispiele verifizieren


## empty
### Zweck
Funktion: Entfernt den Textknoten-Inhalt des nachfolgenden Elementes oder Attributes vollständig; der Zielknoten (also das Element oder Attribut selbst) bleibt jedoch erhalten.

### Parameter
(Keine)

### Beispiel
```xml
<?xmute mutator="empty" schema-valid schematron-invalid="business-rule-0123" ?>
<hinweis>Dieser Textinhalt wird gelöscht</hinweis>
```

### TODO
+ [ ] Parameter prüfen
+ [ ] Stimmt die Beschreibung?
+ [ ] Beispiel erstellen


## identity
### Zweck
Test XML Schema and Schematron expectations on the unchanged original document.

### Parameter
...

### Beispiel
...

### TODO
+ [ ] Paramter auflisten
+ [ ] Beispiel erstellen


## remove
### Zweck
Entfernt ein Element oder Attribut.

### Parameter
...

### Beispiele
```xml 
<?xmute mutator="remove" attribute="my-attr1" attribute="my-attr2" ?>
```

### TODO
+ [ ] Parameter beschreiben
+ [ ] Beispiele erstellen


## xsl
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