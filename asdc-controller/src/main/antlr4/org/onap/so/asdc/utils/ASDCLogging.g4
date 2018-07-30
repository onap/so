grammar ASDCLogging;
doc : value+;
list : listName ':' '{' (obj (',' obj)*)? '}';
simplePair : key ':' keyValue;
complexPair : key ':' obj;
value : simplePair | list | complexPair ;
obj
  : '{' 'NULL' '}'
  | 'NULL'
  | '{' value+ '}'
  |  value+
  | '{' '}'
  ;
key : STRING;
keyValue : STRING;
listName :  LIST_NAME;
LIST_NAME : STRING 'List' | 'RelatedArtifacts';
STRING : ~[:\r\n{},]+;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
;
WS: [ \t\n\r]+ -> skip ;
