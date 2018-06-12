grammar ASDCLogging;
doc : value+;
simplePair : key ':' keyValue;
complexPair : key ':' obj;
list : listName ':' '{' obj (',' obj)* '}';
value : simplePair | complexPair | list;
obj
  : '{' 'NULL' '}'
  | 'NULL'
  | '{' '}'
  | '{' value+ '}'
  |  value+
  ;
key : STRING;
keyValue : STRING;
listName : LIST_NAME;
LIST_NAME : STRING'List';
STRING : ~[:\r\n{},]+;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
;
WS: [ \t\n\r]+ -> skip ;
