grammar MutatorInstruction;

options {
	// antlr will generate java lexer and parser
	language = Java;
	// generated parser should create abstract syntax tree
}

// package declaration on top of it
@lexer::header {
//package  de.kosit.xmlmutate.mutation.parser;

}

// parser rules: start rule, begin parsing here
xmute: mutator property* EOF;

mutator: 'mutator=' value;

property: schemaProperty 
          | schematronProperty 
          | mutatorProperty ;

schemaProperty: 'schema-' assertion ('=' value)?;

schematronProperty: 'schematron-'assertion ('=' value)? ;

assertion : 'valid' | 'invalid';

mutatorProperty: key '=' value ;


key: KEYWORD;

value: STRING;

// all literals from parser rules go before lexer rules lexer rules: First ambigous rule matches

// super important that this rule is non greedy
STRING: '"' (ESC | ~["])*? '"';

// has to have at least two lower case signs, 
// starting with a-z and can inlcude -, stops before = or "
KEYWORD: [a-z]([a-z] | [0-9] )+ ;


// fragment EQ_SIGN: '=';
fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];

WS: [ \t\n\r]+ -> skip;
