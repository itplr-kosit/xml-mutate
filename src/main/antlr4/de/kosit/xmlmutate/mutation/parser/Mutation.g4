grammar Mutation;

options {
	// antlr will generate java lexer and parser
	language = Java;
	// generated parser should create abstract syntax tree
}

//as the generated lexer will reside in org.meri.antlr_step_by_step.parsers package, we have to add
// package declaration on top of it
@lexer::header {
//package  de.kosit.xmlmutate.mutation.parser;

}

//as the generated parser will reside in org.meri.antlr_step_by_step.parsers package, we have to add
// package declaration on top of it
@parser::header {
//package  de.kosit.xmlmutate.mutation.parser;

}

//override some methods and add new members to generated lexer public void
// reportError(RecognitionException e) { displayRecognitionError(this.getTokenNames(), e); throw new
// S005Error(":(", e); }
@lexer::members {
  //override method


}

//override some methods and add new members to generated parser
@parser::members {
  //override method

}

// lexer rules:
LPAREN: '(';
RPAREN: ')';
AND: 'AND';
OR: 'OR';
NOT: 'NOT';
EQUALITY_OPERATOR: ('=');
COLON: ':';
WS: [ \t\r\n\u000C]+ -> skip;
DASH:'-';

STRING_LITERAL:
	'"' (~('"' | '\\' | '\r' | '\n') | '\\' ('"' | '\\'))* '"';
QUOTE :'"';
LITERAL: [a-zA-Z_] CHARACTER*;

CHARACTER: ('0' ..'9' | 'a' ..'z' | 'A' ..'Z' | '_');

// parser rules:

mutation: mutator (configuration)*;

mutator: 'mutator' EQUALITY_OPERATOR name;

configuration: keyword | property;

keyword: schemaKeyword | schematronKeyword;

schemaKeyword: 'schema-' assertion identifier;

assertion: 'valid' | 'invalid';

schematronKeyword:
	'schematron-' assertion EQUALITY_OPERATOR identifier;

property: identifier EQUALITY_OPERATOR value;

value: identifier;


name: identifier;

identifier: STRING_LITERAL | (LITERAL (DASH (text|identifier))* );

text: CHARACTER+ ;

schematronRules: schematronRule+;

schematronRule: ruleName | schematronName (COLON ruleName)*;
schematronName: identifier;
ruleName: identifier;