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
xmute: mutator (property)*? EOF;

mutator: 'mutator' EQ_OP value;

property: schemaProperty | schematronProperty | mutatorProperty;

schemaProperty: 'schema-' assertion (EQ_OP value)?;

schematronProperty: 'schematron-' assertion (EQ_OP value)? ;

assertion : 'valid' | 'invalid';

mutatorProperty: key EQ_OP value;


key: KEYWORD;

value: STRING;

// all literals from parser rules go before lexer rules lexer rules: First ambigous rule matches

EQ_OP: ('=');

// Rule before STRIGN and KEYWORD in order to ignore whitespace inbetween properties
WS: [ \t\n\r]+ -> skip;
// super important that this rule is non greedy
STRING: '"' (ESC | ~["])*? '"';
// super important that this rule is non greedy
KEYWORD: ([a-z] | '-' | ~[="])+?;

fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
